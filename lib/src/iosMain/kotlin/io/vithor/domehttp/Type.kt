package io.vithor.domehttp

/**
 * Information about type.
 */

actual interface Type {}

object IosType : Type {}

@PublishedApi
internal open class TypeBase<T>

@PublishedApi
internal actual inline fun <reified T> typeInfo(): TypeInfo {
    val kClass = T::class
    return TypeInfo(kClass, IosType)
}