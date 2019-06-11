package io.vithor.domehttp

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val DefaultDispatcher: CoroutineDispatcher
    get() = Dispatchers.IO