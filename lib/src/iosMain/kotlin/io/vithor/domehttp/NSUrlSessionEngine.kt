@file:Suppress("NOTHING_TO_INLINE")

package io.vithor.domehttp

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import platform.darwin.NSObject

class NSUrlSessionEngine private constructor() : HttpEngine {
    companion object {
        operator fun invoke(): HttpEngineFactory = object : HttpEngineFactory {

            override fun config(config: DomeClient.Config): HttpEngineFactory {
                return this
            }

            override fun build(): HttpEngine {
                return NSUrlSessionEngine()
            }
        }
    }

    override suspend fun <R : Any> request(raw: DomeClient.Request<R>): DomeClient.RawResponse {
        return suspendCancellableCoroutine { continuation ->
            try {
                validatePayload(raw)
            } catch (exception: Exception) {
                continuation.resumeWith(Result.failure(HTTPError(exception)))
                return@suspendCancellableCoroutine
            }

            val processedRequest: DomeClient.Request<R> = intercept(raw)

            val delegate = object : NSObject(), NSURLSessionDataDelegateProtocol {
                val receivedData = NSMutableData()

                override fun URLSession(session: NSURLSession, didBecomeInvalidWithError: NSError?) {
                    println("URLSession didBecomeInvalidWithError")
                }

                override fun URLSession(session: NSURLSession, dataTask: NSURLSessionDataTask, didReceiveData: NSData) {
                    println("URLSession didReceiveData")
                    receivedData.appendData(didReceiveData)
                }

                override fun URLSession(session: NSURLSession, task: NSURLSessionTask, didCompleteWithError: NSError?) {
                    println("URLSession didComplete")
//                requestInProgress = false // Only main thread accesses the fetcher, so it's safe to clear the flag here.

                    val response = task.response
                    if (response == null || (response as NSHTTPURLResponse).statusCode.toInt() !in 200..299) {
                        println("URLSession error response")

                        if (response == null) return

                        val httpResponse = (response as NSHTTPURLResponse)

                        val responseObj = processHttpErrorResponse(processedRequest, receivedData, httpResponse)

                        continuation.resumeWith(
                            Result.success(
                                responseObj
                            )
                        )
                        return
                    }

                    if (didCompleteWithError != null) {
                        println("URLSession completed with error")

                        continuation.resumeWith(
                            Result.failure(
                                RequestError.with(
                                    receivedData,
                                    didCompleteWithError.toThrowable()
                                )
                            )
                        )
                        return
                    }

                    println("URLSession Almost there")
                    try {
                        println("URLSession Almost success")
                        val responseObj = processResponse(processedRequest, receivedData, response)
                        continuation.resumeWith(Result.success(responseObj))
                        println("URLSession success")
                    } catch (e: Throwable) {
                        println("URLSession mapping failure")
                        continuation.resumeWith(Result.failure(RequestError.with(receivedData, e)))
                    }
                }
            }

            val session = NSURLSession.sessionWithConfiguration(
                NSURLSessionConfiguration.defaultSessionConfiguration(),
                delegate,
                delegateQueue = NSOperationQueue.mainQueue()
            )

            continuation.invokeOnCancellation {
                session.invalidateAndCancel()
            }

            try {
                val url = buildUrl(processedRequest.url, processedRequest.payload)

                val request = NSMutableURLRequest.requestWithURL(url.URL!!)
                request.setHTTPMethod(processedRequest.method.methodName)

                processRequestBody(processedRequest, request)

                processRequestHeaders(processedRequest, request)

                session.dataTaskWithRequest(request).resume()
            } catch (e: Throwable) {
                println(e)
                continuation.resumeWith(
                    Result.failure(
                        HTTPError(cause = e)
                    )
                )
            }
        }
    }

    private fun <R : Any> processHttpErrorResponse(
        processedRequest: DomeClient.Request<R>,
        receivedData: NSMutableData,
        response: NSHTTPURLResponse
    ): DomeClient.RawResponse {
        return DomeClient.RawResponse(
            processedRequest,
            statusCode = response.statusCode.toInt(),
            result = DomeClient.Result.Failure(
                RawData.of(receivedData), HTTPError(
                    statusCode = response.statusCode.toInt(),
                    headers = mutableMapOf<String, List<String?>?>().also {
                        for ((key, value) in response.allHeaderFields) {
                            it[key.toString()] = listOf(value.toString())
                        }
                    },
                    responseAsString = receivedData.string()
                )
            )
        )
    }

    private inline fun <R : Any> processResponse(
        processedRequest: DomeClient.Request<R>,
        receivedData: NSMutableData,
        response: NSHTTPURLResponse
    ): DomeClient.RawResponse {
        val responseString = receivedData.string()!!
        println("Received: $responseString")
        val rawData = RawData.of(receivedData)
        val statusCode = response.statusCode.toInt()
        return DomeClient.RawResponse(
            processedRequest,
            statusCode,
            DomeClient.Result.Success(rawData)
        )
    }

    private inline fun <R : Any> processRequestHeaders(
        processedRequest: DomeClient.Request<R>,
        request: NSMutableURLRequest
    ) {
        when (processedRequest.payload) {
            is DomeClient.Request.Payload.Form -> {
                request.addValue("application/x-www-form-urlencoded", "Content-Type")
            }
            is DomeClient.Request.Payload.Json -> {
                request.addValue("application/json", "Content-Type")
            }
        }

        request.addValue("application/json", "Accept")

        for ((key, value) in processedRequest.headers) {
            request.addValue(value, key)
        }
    }

    private inline fun <R : Any> processRequestBody(
        processedRequest: DomeClient.Request<R>,
        request: NSMutableURLRequest
    ) {
        when (processedRequest.method) {
            HTTPMethod.Patch,
            HTTPMethod.Post,
            HTTPMethod.Put,
            HTTPMethod.Delete -> {
                request.HTTPBody = processedRequest.payload?.toData()
            }
        }
    }

    private inline fun buildUrl(url: String, payload: DomeClient.Request.Payload?): NSURLComponents {
        return NSURLComponents(url).apply {
            if (payload is DomeClient.Request.Payload.Query) {
                queryItems = payload.params.map { (name, value) ->
                    NSURLQueryItem(name, value)
                }
            }
        }
    }

    private inline fun DomeClient.Request.Payload?.toData(): NSData? {
        return when (this) {
            is DomeClient.Request.Payload.Form -> {
                fields.map { (key, value) ->
                    "$key=${percentEscapeString(value)}"
                }.joinToString("&").toNSData()
            }

            is DomeClient.Request.Payload.Json -> {
//            NSJSONSerialization.dataWithJSONObject(
//                NSJSONSerialization.JSONObjectWithData(this.body
//                    .also { println("Sending: $it") }
//                    .toNSData()!!, NSJSONReadingAllowFragments, null)!!,
//                NSJSONReadingAllowFragments, null
//            )

                this.body.also { println("Sending: $it") }
                    .toNSData()!!
            }

            is DomeClient.Request.Payload.Empty, null -> NSMutableData()

            else -> throw IllegalArgumentException("Payload is invalid $this")
        }
    }
}


fun RequestError.Companion.with(receivedData: NSMutableData?, e: Throwable): RequestError {
    println("With error: ")
    println(receivedData?.string())
    println(e)
    return RequestError(
        mapOf(
            "receivedData" to receivedData
        ), e
    )
}