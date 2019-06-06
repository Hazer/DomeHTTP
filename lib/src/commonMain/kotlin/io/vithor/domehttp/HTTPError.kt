package io.vithor.domehttp

class HTTPError @PublishedApi internal constructor(
    cause: Throwable? = null,
    val statusCode: Int? = null,
    val headers: Map<String, List<String?>?>? = null,
    val responseAsString: String? = null
) : Throwable(cause) {
    companion object {
        fun from(cause: Throwable): HTTPError {
            return HTTPError(cause)
        }

        fun from(statusCode: Int, headers: Map<String, List<String?>?>, responseAsString: String): HTTPError {
            return HTTPError(null, statusCode, headers, responseAsString)
        }
    }

}
