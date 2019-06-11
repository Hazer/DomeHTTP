package io.vithor.domehttp

import kotlin.reflect.KClass

/**
 * Information about type.
 */
expect interface Type

/**
 * Type information.
 * @param type: source KClass<*>
 * @param reifiedType: type with substituted generics
 */
data class TypeInfo(val type: KClass<*>, val reifiedType: Type, val inList: Boolean = false)

/**
 * Returns [TypeInfo] for the specified type [T]
 */
@PublishedApi
internal expect inline fun <reified T> typeInfo(inList: Boolean = false): TypeInfo

/**
 * Check [this] is instance of [type].
 */
internal expect fun Any.instanceOf(type: KClass<*>): Boolean