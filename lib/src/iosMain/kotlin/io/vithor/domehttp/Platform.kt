@file:Suppress("NOTHING_TO_INLINE")

package io.vithor.domehttp

import kotlinx.cinterop.convert
import platform.darwin.NSUInteger

actual object Platform {
    actual fun name(): String = "iOS"
}

inline fun Int.toNSUInteger(): NSUInteger {
    return convert()
}