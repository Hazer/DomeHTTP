package io.vithor.domehttp

import kotlinx.coroutines.CoroutineDispatcher

typealias QueryParam = Pair<String, String?>
typealias Params = List<QueryParam>
typealias Field = Pair<String, String>
typealias Fields = List<Field>

expect val DefaultDispatcher: CoroutineDispatcher

interface HttpEngine {
    suspend fun <R : Any> request(raw: DomeClient.Request<R>): DomeClient.RawResponse<R>
}

internal fun <R : Any> intercept(raw: DomeClient.Request<R>): DomeClient.Request<R> {
    val interceptors = listOf<DomeClient.Interceptor>()
    var last = raw.copy()
    for (interceptor in interceptors) {
        last = interceptor.intercept(last).also {
            it.raw = last
        }
    }
    return last
}

internal fun <R : Any> validatePayload(raw: DomeClient.Request<R>) {
    when (raw.method) {
        HTTPMethod.Delete -> isValidDataPayload(raw)

        HTTPMethod.Get,
        HTTPMethod.Head -> isValidQueryPayload(raw)

        HTTPMethod.Patch,
        HTTPMethod.Post,
        HTTPMethod.Put -> isValidDataPayload(raw)
    }
}

internal fun <R : Any> isValidQueryPayload(raw: DomeClient.Request<R>) {
    val payload = raw.payload
    if (payload != null && payload !is DomeClient.Request.Payload.Query &&
        payload !is DomeClient.Request.Payload.Empty
    ) {
        throw IllegalArgumentException("${raw.method} must not have payload of type ${payload::class}")
    }
}

internal fun <R : Any> isValidDataPayload(raw: DomeClient.Request<R>) {
    val payload = raw.payload
    if (raw.method != HTTPMethod.Delete &&
        (payload == null || raw.payload is DomeClient.Request.Payload.Empty)
    ) {
        throw IllegalArgumentException("${raw.method} must have a payload")
    }

    if (payload is DomeClient.Request.Payload.Query) {
        throw IllegalArgumentException("${raw.method} must not have payload of type ${payload::class}")
    }
}