package io.vithor.domehttp

class RequestError internal constructor(val info: Map<String, Any?>, cause: Throwable) : Throwable(cause = cause) {
    companion object
}
