package io.vithor.domehttp

import kotlin.reflect.KClass

/**
 * Information about type.
 */

actual interface Type {}

object IosType : Type {}

@PublishedApi
internal open class TypeBase<T>

@PublishedApi
internal actual inline fun <reified T> typeInfo(inList: Boolean): TypeInfo {
    val kClass = T::class
    return TypeInfo(kClass, IosType, inList)
}

/**
 * Check [this] is instance of [type].
 */
internal actual fun Any.instanceOf(type: KClass<*>): Boolean = type.isInstance(this)