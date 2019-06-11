package io.vithor.domehttp

@PublishedApi
internal fun safe(block: () -> String?): String? = try {
    block()
} catch (throwable: Throwable) {
    null
}