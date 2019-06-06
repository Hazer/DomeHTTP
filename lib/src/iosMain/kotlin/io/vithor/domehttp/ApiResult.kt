package io.vithor.domehttp


sealed class ApiResult<T> {
    data class Success<T>(val value: T) : ApiResult<T>()
    data class Failure<T>(val error: Throwable) : ApiResult<T>()

    fun success(callback: (value: T) -> Unit): ApiResult<T> {
        if (this is Success) {
            callback.invoke(this.value)
        }
        return this
    }

    fun failure(callback: (error: Throwable) -> Unit): ApiResult<T> {
        if (this is Failure) {
            callback.invoke(this.error)
        }
        return this
    }

    companion object {
        fun <T> success(value: T): ApiResult<T> {
            return Success(value)
        }

        fun <T> failure(error: Throwable): ApiResult<T> {
            return Failure(error)
        }
    }
}
