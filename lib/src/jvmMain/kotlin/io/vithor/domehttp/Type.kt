package io.vithor.domehttp

/**
 * Information about type.
 */
import java.lang.reflect.*
import kotlin.reflect.*

actual typealias Type = java.lang.reflect.Type

@PublishedApi
internal open class TypeBase<T>

@PublishedApi
internal actual inline fun <reified T> typeInfo(inList: Boolean): TypeInfo {
    val base = object : TypeBase<T>() {}
    val superType = base::class.java.genericSuperclass!!

    val reifiedType = (superType as ParameterizedType).actualTypeArguments.first()!!
    return TypeInfo(T::class, reifiedType, inList)
}


/**
 * Check [this] is instance of [type].
 */
internal actual fun Any.instanceOf(type: KClass<*>): Boolean = type.java.isInstance(this)