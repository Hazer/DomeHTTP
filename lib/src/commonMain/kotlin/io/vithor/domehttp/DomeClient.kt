@file:Suppress("NOTHING_TO_INLINE", "unused", "UNUSED_PARAMETER")

package io.vithor.domehttp

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlin.coroutines.CoroutineContext

class DomeClient(engine: HttpEngineFactory, configurer: DomeClient.Config.() -> Unit) {
    @PublishedApi
    internal var serializer: SerializerContract

    @PublishedApi
    internal val engine: HttpEngine

    internal val interceptors = mutableListOf<Interceptor>()

    init {
        val config = Config().apply(configurer)
        this.serializer = config.serializationConfig.serializer
        this.engine = engine
            .config(config)
            .build()
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    suspend inline fun <reified R : Any> get(
        url: String,
        crossinline requestConfig: Request.QueryConfig<R>.() -> Unit = {}
    ): Request<R> {
        val typeInfo = typeInfo<R>()
        val type = typeInfo.type
        val reifiedType = typeInfo.reifiedType

        println("Request typeInfo: $typeInfo,\n$type,\n$reifiedType")

        when (typeInfo.type) {
            is List<*> -> throw IllegalArgumentException("Right now you cannot call get with List<T>, use getList instead.")
        }

        return request(HTTPMethod.Get, url, typeInfo, Request.QueryConfig(), requestConfig)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    suspend inline fun <reified R : Any> getList(
        url: String,
        crossinline requestConfig: Request.QueryConfig<List<R>>.() -> Unit = {}
    ): Request<List<R>> {
        return request(HTTPMethod.Get, url, typeInfo<R>(true), Request.QueryConfig(), requestConfig)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    suspend inline fun <reified R : Any> head(
        url: String,
        crossinline requestConfig: Request.QueryConfig<R>.() -> Unit = {}
    ): Request<R> {
        return request(HTTPMethod.Head, url, typeInfo<R>(), Request.QueryConfig(), requestConfig)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    suspend inline fun <reified R : Any> post(
        url: String,
        crossinline requestConfig: Request.SendingConfig<R>.() -> Unit
    ): Request<R> {
        return request(HTTPMethod.Post, url, typeInfo<R>(), Request.SendingConfig(serializer), requestConfig)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    suspend inline fun <reified R : Any> put(
        url: String,
        crossinline requestConfig: Request.SendingConfig<R>.() -> Unit
    ): Request<R> {
        return request(HTTPMethod.Put, url, typeInfo<R>(), Request.SendingConfig(serializer), requestConfig)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    suspend inline fun <reified R : Any> patch(
        url: String,
        crossinline requestConfig: Request.SendingConfig<R>.() -> Unit
    ): Request<R> {
        return request(HTTPMethod.Patch, url, typeInfo<R>(), Request.SendingConfig(serializer), requestConfig)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    suspend inline fun <reified R : Any> delete(
        url: String,
        crossinline requestConfig: Request.SendingConfig<R>.() -> Unit
    ): Request<R> {
        return request(HTTPMethod.Delete, url, typeInfo<R>(), Request.SendingConfig(serializer), requestConfig)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    @PublishedApi
    internal suspend inline fun <reified T : Any, reified C : Request.Config<T>> request(
        method: HTTPMethod,
        url: String,
        responseType: TypeInfo,
        configObj: C,
        crossinline requestConfig: C.() -> Unit
    ): Request<T> {
        println("Dome Client ${method.methodName}")

        return coroutineScope {
            val config = configObj.apply(requestConfig)
            Request<T>(this@DomeClient, method, url, responseType).also {
                config.apply(it)
            }
        }
    }

    interface Interceptor {
        fun <R : Any> intercept(request: Request<R>): Request<R> {
            return request.copy()
        }
    }

    data class Request<ResponseType : Any> @PublishedApi internal constructor(
        val domeClient: DomeClient,
        val method: HTTPMethod,
        val url: String,
        val responseType: TypeInfo,
        var payload: Payload? = Payload.Empty,
        var headers: Map<String, String> = mapOf()
    ) {
        enum class State {
            Initialized, Cancelled, Finished
        }

        sealed class Payload {
            object Empty : Payload()

            class Query(val params: Params) : Payload()

            abstract class DataPayload : Payload()
            class Form(val fields: Fields) : DataPayload()
            class Json(val body: String) : DataPayload()
        }

        abstract class Config<R : Any> @PublishedApi internal constructor(
            request: Request<R>? = null
        ) {
            private var internalHeaders: MutableMap<String, String> = request?.headers?.toMutableMap()
                ?: mutableMapOf()

            var headers: Map<String, String>
                get() = internalHeaders
                set(value) {
                    println("Discarding old headers ${internalHeaders}")
                    internalHeaders = value.toMutableMap()
                }

            fun addHeader(key: String, value: String) {
                internalHeaders[key] = value
            }

            fun removeHeader(key: String) = internalHeaders.remove(key)

            open fun apply(request: Request<R>) {
                request.headers = headers
            }
        }

        @DslConfig
        class QueryConfig<R : Any> @PublishedApi internal constructor(
            request: Request<R>? = null
        ) : Config<R>(request) {
            private var internalParams = mutableListOf<QueryParam>().also { newList ->
                val oldParams = (request?.payload as? Payload.Query)?.params

                oldParams?.let {
                    newList.addAll(it)
                }
            }

            var params: Params
                get() = internalParams
                set(value) {
                    println("Discarding old params ${internalParams}")
                    internalParams = value.toMutableList()
                }

            fun addQueryParam(name: String, value: String?) {
                internalParams.add(QueryParam(name, value))
            }

            fun removeQueryParam(name: String) {
                internalParams.removeAll { it.first == name }
            }

            fun removeFirstQueryParam(name: String) {
                val index = internalParams.indexOfFirst { it.first == name }
                internalParams.removeAt(index)
            }

            fun removeLastQueryParam(name: String) {
                val index = internalParams.indexOfLast { it.first == name }
                internalParams.removeAt(index)
            }

            override fun apply(request: Request<R>) {
                super.apply(request)
                request.payload = if (params.isEmpty()) Payload.Empty else Payload.Query(params)
            }
        }

        @DslConfig
        class SendingConfig<R : Any> @PublishedApi internal constructor(
            private val serializer: SerializerContract,
            request: Request<R>? = null
        ) : Config<R>(request) {
            sealed class Payload {
                object Empty : Payload()
                class Form(val fields: Fields) : Payload()
                class Object<T : Any>(val value: T) : Payload() {
                    @PublishedApi
                    internal var token: TypeInfo? = null
                }
            }

            @PublishedApi
            internal var body: Payload = Payload.Empty

            inline fun <reified T : Any> jsonOf(value: T) {
                body = Payload.Object(value).also {
                    it.token = typeInfo<T>()
                }
            }

            inline fun formOf(vararg fields: Field) {
                body = Payload.Form(fields.toList())
            }

            inline fun form(fields: Fields) {
                body = Payload.Form(fields)
            }

            inline fun customOf(contentType: String, content: String) {
                TODO("Not implemented yet.")
            }

            inline fun customOf(contentType: String, content: ByteArray) {
                TODO("Not implemented yet.")
            }

            override fun apply(request: Request<R>) {
                super.apply(request)

                request.payload = when (val body = body) {
                    is Payload.Object<*> -> {
                        val jsonString = serializer.stringify(body.value, body.token!!)
                        Request.Payload.Json(jsonString)
                    }

                    is Payload.Form -> {
                        Request.Payload.Form(body.fields.toList())
                    }

                    else -> if (body is Payload.Empty && request.method == HTTPMethod.Delete) {
                        Request.Payload.Empty
                    } else {
                        throw IllegalStateException("A ${request.method} request must have a body")
                    }
                }
            }
        }

        var raw: DomeClient.Request<ResponseType>? = null
            internal set

        var state: State = State.Initialized
            internal set

        val isInitialized
            get() = state == State.Initialized

        val isCancelled
            get() = state == State.Cancelled

        val isFinished
            get() = state == State.Finished

        var id: String = "Generate some unique id"
            internal set

        override fun hashCode(): Int {
            return id.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return if (other is Request<*>) other.id == this.id else super.equals(other)
        }

        suspend fun awaitResponse(coroutineContext: CoroutineContext = DefaultDispatcher): Response<ResponseType> =
            withContext(coroutineContext) {
                val rawResponse = domeClient.engine.request(this@Request)

                when (val rawDataResult = rawResponse.result) {
                    is Result.Success -> {
                        try {
                            val result: ResponseType =
                                domeClient.serializer.parse(rawDataResult.value, responseType)
                            Response(
                                rawResponse,
                                Result.Success(result)
                            )
                        } catch (e: Throwable) {
                            Response<ResponseType>(
                                rawResponse,
                                Result.Failure(null, DomeError(rawDataResult.valueOrNull, e))
                            )
                        }
                    }
                    is Result.Failure -> Response<ResponseType>(
                        rawResponse,
                        Result.Failure(null, DomeError(rawDataResult.valueOrNull, rawDataResult.error))
                    )
                }
            }

        suspend fun await(coroutineContext: CoroutineContext = DefaultDispatcher): ResponseType =
            awaitResponse(coroutineContext).body.valueOrThrow

        suspend fun awaitString(coroutineContext: CoroutineContext = DefaultDispatcher): String? =
            withContext(coroutineContext) {
                domeClient.engine.request(this@Request).result.valueOrNull?.asString()
            }

        inline fun <reified R: Any> asList(): Request<List<R>> {
            return Request(
                domeClient, method, url, typeInfo<R>(true), payload, headers
            )
        }
    }

    class RawResponse @PublishedApi internal constructor(
        val request: Request<*>,
        val statusCode: Int,
        val result: Result<RawData>
    ) {
        val url: String
            get() = request.url
    }

    sealed class Result<T> {
        class Success<T>(val value: T) : Result<T>()
        class Failure<T>(val value: T?, val error: Throwable) : Result<T>()

        val valueOrThrow: T
            get() = when (this) {
                is Success<T> -> value
                is Failure<T> -> throw error
            }

        val valueOrNull
            get() = if (this is Success<T>) value else null

        val errorOrNull
            get() = if (this is Failure<T>) error else null
    }

    class Response<ResponseType : Any> @PublishedApi internal constructor(
        val raw: RawResponse,
        val body: DomeClient.Result<ResponseType>
    ) {
        val statusCode
            get() = raw.statusCode
    }

    @DslMarker
    internal annotation class DslConfig

    @DslConfig
    class Config @PublishedApi internal constructor() {
        //        val interceptors = mutableListOf<Interceptor>()
        var timeoutConfig: Timeouts = Timeouts()
        var serializationConfig: Serialization = Serialization(KotlinxSerializer())

        fun DomeClient.Config.timeouts(config: DomeClient.Config.Timeouts.() -> Unit) {
            config(this.timeoutConfig)
        }

        fun DomeClient.Config.serialization(
            mapper: SerializerContract = KotlinxSerializer(),
            config: DomeClient.Config.Serialization.() -> Unit
        ) {
            this.serializationConfig = Serialization(mapper)
            config(this.serializationConfig)
        }

        @DslConfig
        class Serialization @PublishedApi internal constructor(
            @PublishedApi
            internal val serializer: SerializerContract
        ) {
            inline fun <reified T : Any> register(mapper: KSerializer<T>) {
                serializer.setMapper(T::class, mapper)
                serializer.setListMapper(T::class, mapper)
            }
        }

        @DslConfig
        class Timeouts @PublishedApi internal constructor() {
            var connect: Long = 30
            var read: Long = 30
            var write: Long = 30

            fun connect(timeout: Long, unit: TimeUnit) {
                connect = unit.toMillis(timeout)
            }

            fun read(timeout: Long, unit: TimeUnit) {
                read = unit.toMillis(timeout)
            }

            fun write(timeout: Long, unit: TimeUnit) {
                write = unit.toMillis(timeout)
            }

            val Int.millis: Long
                get() = TimeUnit.MILLISECONDS.toMillis(this.toLong())

            val Long.millis: Long
                get() = TimeUnit.MILLISECONDS.toMillis(this)

            val Int.seconds: Long
                get() = TimeUnit.SECONDS.toMillis(this.toLong())

            val Long.seconds: Long
                get() = TimeUnit.SECONDS.toMillis(this)

            val Int.minutes: Long
                get() = TimeUnit.MINUTES.toMillis(this.toLong())

            val Long.minutes: Long
                get() = TimeUnit.MINUTES.toMillis(this)
        }
    }
}