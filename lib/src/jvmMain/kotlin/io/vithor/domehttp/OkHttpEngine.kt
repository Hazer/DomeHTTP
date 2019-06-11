package io.vithor.domehttp

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.internal.Util
import java.io.IOException
import java.util.concurrent.TimeUnit.MILLISECONDS

class OkHttpEngine(
    @PublishedApi
    internal val ok: OkHttpClient
) : HttpEngine {

    companion object {
        operator fun invoke(): HttpEngineFactory = object : HttpEngineFactory {

            private lateinit var builder: OkHttpClient.Builder

            override fun config(config: DomeClient.Config): HttpEngineFactory {
                with(config.timeoutConfig) {
                    builder = OkHttpClient.Builder()
                        .connectTimeout(connect, MILLISECONDS)
                        .readTimeout(read, MILLISECONDS)
                        .writeTimeout(write, MILLISECONDS)
                }
                return this
            }

            override fun build() = OkHttpEngine(builder.build())
        }
    }

    override suspend fun <R: Any> request(raw: DomeClient.Request<R>): DomeClient.RawResponse {
        return suspendCancellableCoroutine { continuation ->
            try {
                validatePayload(raw)
            } catch (exception: Exception) {
                continuation.resumeWith(Result.failure(HTTPError(exception)))
                return@suspendCancellableCoroutine
            }

            val processedRequest: DomeClient.Request<R> = intercept(raw)

            val requestBuilder = Request.Builder()

            when (processedRequest.method) {
                HTTPMethod.Delete -> {
                    val body: RequestBody
                    try {
                        body = processedRequest.payload.toRequestBody()
                        requestBuilder.delete(body)
                    } catch (e: Throwable) {
                        println("Must not be here!!")
                        requestBuilder.delete()
                    }
                }

                HTTPMethod.Get -> requestBuilder.get()
                HTTPMethod.Head -> requestBuilder.head()
                HTTPMethod.Patch -> requestBuilder.patch(processedRequest.payload.toRequestBody())
                HTTPMethod.Post -> requestBuilder.post(processedRequest.payload.toRequestBody())
                HTTPMethod.Put -> requestBuilder.put(processedRequest.payload.toRequestBody())
            }

            val baseUrl = HttpUrl.parse(processedRequest.url) ?: throw IllegalArgumentException("Url is invalid ${processedRequest.url}")
            val urlBuilder = baseUrl.newBuilder()

            when (val payload = processedRequest.payload) {
                is DomeClient.Request.Payload.Query -> {
                    urlBuilder.run {
                        for ((key, value) in payload.params) {
                            addQueryParameter(key, value)
                        }
                    }
                }
                is DomeClient.Request.Payload.Form -> {
                    requestBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded")
                }
                is DomeClient.Request.Payload.Json -> {
                    requestBuilder.addHeader("Content-Type", "application/json")
                }
            }

            processRequestHeaders(processedRequest, requestBuilder)

            requestBuilder.url(urlBuilder.build())

            val request = requestBuilder.build()

            val call = ok.newCall(request)

            continuation.invokeOnCancellation {
                call.cancel()
            }

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWith(Result.failure(HTTPError(e)))
                }

                override fun onResponse(call: Call, response: Response) {
                    val responsePayload = if (response.isSuccessful)
                        DomeClient.Result.Success(RawData.of(response))
                    else
                        DomeClient.Result.Failure(RawData.of(response), HTTPError(
                            statusCode = response.code(),
                            headers = request.headers().toMultimap(),
                            responseAsString = safe { response.body()?.string() }
                        ))

                    val responseObj = DomeClient.RawResponse(
                        processedRequest,
                        response.code(),
                        responsePayload
                    )
                    continuation.resumeWith(Result.success(responseObj))
                }
            })
        }
    }

    private fun <R : Any> processRequestHeaders(processedRequest: DomeClient.Request<R>, requestBuilder: Request.Builder) {
        for ((key, value) in processedRequest.headers) {
            requestBuilder.addHeader(key, value)
        }
    }
}

private fun DomeClient.Request.Payload?.toRequestBody(): RequestBody {
    return when (this) {
        is DomeClient.Request.Payload.Form -> {
            FormBody.Builder().apply {
                for ((name, value) in fields) {
                    add(name, value)
                }
            }.build()
        }

        is DomeClient.Request.Payload.Json -> RequestBody.create(
            MediaType.get("application/json"),
            body
        )

        is DomeClient.Request.Payload.Empty, null -> Util.EMPTY_REQUEST

        else -> throw IllegalArgumentException("Payload is invalid $this")
    }
}