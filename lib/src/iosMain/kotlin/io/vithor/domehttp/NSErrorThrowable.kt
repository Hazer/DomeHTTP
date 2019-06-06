package io.vithor.domehttp

import platform.Foundation.NSError

data class NSErrorThrowable(
    val code: Int,
    val domain: String?,
    val localizedDescription: String?,
    val localizedFailureReason: String?,
    val localizedRecoveryOptions: List<*>?,
    val localizedRecoverySuggestion: String?,
    val userInfo: Map<String, String>?
) : Throwable(message = localizedDescription)

fun NSError.toThrowable(): Throwable {
    return NSErrorThrowable(this.code.toInt(),
        this.domain,
        this.localizedDescription,
        this.localizedFailureReason,
        this.localizedRecoveryOptions,
        this.localizedRecoverySuggestion,
        this.userInfo.mapping { (key, value) ->
            key.toString() to value.toString()
        })
}

internal inline fun <reified K, V, NK, NV> Map<K, V>.mapping(transform: (Map.Entry<K, V>) -> Pair<NK, NV>): Map<NK, NV> {
    val newMap = mutableMapOf<NK, NV>()
    newMap.putAll(map(transform))
    return newMap
}
