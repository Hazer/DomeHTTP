package io.vithor.domehttp

import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

interface SerializerContract {
    companion object

    fun <T : Any> setMapper(type: KClass<T>, serializer: KSerializer<T>)
    fun <T : Any> setListMapper(type: KClass<T>, serializer: KSerializer<T>)
    fun <T : Any> stringify(value: T): String
    fun <T : Any> parse(data: RawData, typeInfo: TypeInfo): T
    fun hasTypeRegistered(type: KClass<*>): Boolean

    fun stringify(value: Any, typeInfo: TypeInfo): String
}