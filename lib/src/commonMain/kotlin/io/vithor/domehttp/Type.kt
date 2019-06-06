package io.vithor.domehttp

import kotlin.reflect.KClass

/**
 * Information about type.
 */
expect interface Type

/**
 * Ktor type information.
 * @param type: source KClass<*>
 * @param reifiedType: type with substituted generics
 */
data class TypeInfo(val type: KClass<*>, val reifiedType: Type)

/**
 * Returns [TypeInfo] for the specified type [T]
 */
@PublishedApi
internal expect inline fun <reified T> typeInfo(): TypeInfo