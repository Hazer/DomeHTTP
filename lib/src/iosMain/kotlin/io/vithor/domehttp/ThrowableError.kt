package io.vithor.domehttp

import platform.Foundation.NSError

class ThrowableError(val throwable: Throwable) : NSError(null, 0, mapOf(
    "message" to throwable.message
))