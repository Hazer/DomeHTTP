package io.vithor.domehttp

import kotlinx.serialization.*
import kotlinx.serialization.internal.defaultSerializer
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

@UseExperimental(ImplicitReflectionSerializer::class)
class KotlinxSerializer(
    @PublishedApi
    internal val json: Json
) : SerializerContract {
    companion object {
        private val EMPTY_LIST_SERIALIZER = String.serializer().list

        @UseExperimental(UnstableDefault::class)
        operator fun invoke(strict: Boolean = true): KotlinxSerializer {
            return KotlinxSerializer(if (strict) Json.plain else Json.nonstrict)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private val mappers: MutableMap<KClass<*>, KSerializer<*>> = mutableMapOf()
    private val listMappers: MutableMap<KClass<*>, KSerializer<*>> = mutableMapOf()

    /**
     * Set mapping from [type] to generated [KSerializer].
     */
    override fun <T : Any> setMapper(type: KClass<T>, serializer: KSerializer<T>) {
        @Suppress("UNCHECKED_CAST")
        mappers[type as KClass<Any>] = serializer as KSerializer<Any>
    }

    /**
     * Set mapping from [type] to generated [KSerializer].
     */
    override fun <T : Any> setListMapper(type: KClass<T>, serializer: KSerializer<T>) {
        @Suppress("UNCHECKED_CAST")
        listMappers[type] = serializer.list as KSerializer<List<Any>>
    }

    /** Set the mapping from [T] to [mapper]. */
    inline fun <reified T : Any> register(mapper: KSerializer<T>) {
        setMapper(T::class, mapper)
    }

    /** Set the mapping from [List<T>] to [mapper]. */
    inline fun <reified T : Any> registerList(mapper: KSerializer<T>) {
        setListMapper(T::class, mapper)
    }

    /**
     * Set the mapping from [T] to it's [KSerializer]. This method only works for non-parameterized types.
     */
    inline fun <reified T : Any> register() {
        register(T::class.serializer())
    }

    /**
     * Set the mapping from [List<T>] to it's [KSerializer]. This method only works for non-parameterized types.
     */
    inline fun <reified T : Any> registerList() {
        registerList(T::class.serializer())
    }

    @PublishedApi
    internal fun lookupSerializerByData(data: Any): KSerializer<*> {
        if (data is List<*>) {
            val item = data.find { it != null }
            return item?.let { listMappers[item::class] } ?: EMPTY_LIST_SERIALIZER
        }

        val type = data::class
        mappers[type]?.let { return it }
        return (type.defaultSerializer() ?: type.serializer())
    }

    @PublishedApi
    internal fun lookupSerializerByType(type: KClass<*>, inList: Boolean): KSerializer<*> {
       if (inList) listMappers[type]?.let { return it }
        mappers[type]?.let { return it }
        return (type.defaultSerializer() ?: type.serializer())
    }

    override fun <T : Any> stringify(value: T): String {
        val serializer = lookupSerializerByData(value)

        @Suppress("UNCHECKED_CAST")
        return json.stringify(serializer as KSerializer<Any>, value)
    }

    override fun stringify(value: Any, typeInfo: TypeInfo): String {
        val mapper = lookupSerializerByType(typeInfo.type, typeInfo.inList)

        @Suppress("UNCHECKED_CAST")
        return json.stringify(mapper as KSerializer<Any>, value)
    }

    override fun <T : Any> parse(data: RawData, typeInfo: TypeInfo): T {
        val mapper = lookupSerializerByType(typeInfo.type, typeInfo.inList)

        @Suppress("UNCHECKED_CAST")
        return json.parse(mapper as KSerializer<Any>, data.asString()!!) as T
    }

    override fun hasTypeRegistered(type: KClass<*>): Boolean {
//        return mappers[type]?.let { it } ?: type.defaultSerializer() != null
        return mappers[type]?.let { it } != null
    }
}

//interface Factory {
//
//}
//
//class KxFactory : Factory {
//    override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>,
//                                       retrofit: Retrofit): Converter<ResponseBody, *>? {
//        val loader = serializerByTypeToken(type)
//        return DeserializationStrategyConverter(loader, serializer)
//    }
//
//    override fun requestBodyConverter(type: Type, parameterAnnotations: Array<out Annotation>,
//                                      methodAnnotations: Array<out Annotation>, retrofit: Retrofit): Converter<*, RequestBody>? {
//        val saver = serializerByTypeToken(type)
//        return SerializationStrategyConverter(contentType, saver, serializer)
//    }
//}
