package io.vithor.sample.domehttp

import android.arch.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.Closeable
import java.io.IOException
import kotlin.coroutines.CoroutineContext


private const val JOB_KEY = "androidx.lifecycle.ViewModelCoroutineScope.JOB_KEY"

val ViewModel.viewModelScope: CoroutineScope
    get() {
        val scope: CoroutineScope? = this.getTag(JOB_KEY)
        if (scope != null) {
            return scope
        }
        return setTagIfAbsent(JOB_KEY,
            CloseableCoroutineScope(SupervisorJob() + Dispatchers.Main))
    }

val mBagOfTags = mutableMapOf<String, Any?>()

fun <T> ViewModel.setTagIfAbsent(key: String, newValue: T): T {
    val previous: T?
    synchronized(mBagOfTags) {

        previous = mBagOfTags[key] as T?
        if (previous == null) {
            mBagOfTags[key] = newValue
        }
    }
    val result = previous ?: newValue
//    if (mCleared) {
//        // It is possible that we'll call close() multiple times on the same object, but
//        // Closeable interface requires close method to be idempotent:
//        // "if the stream is already closed then invoking this method has no effect." (c)
//        closeWithRuntimeException(result)
//    }
    return result
}

private fun <T> ViewModel.getTag(key: String): T? {
    synchronized(mBagOfTags) {
        @Suppress("UNCHECKED_CAST")
        return mBagOfTags[key] as T?
    }
}


internal class CloseableCoroutineScope(context: CoroutineContext) : Closeable, CoroutineScope {
    override val coroutineContext: CoroutineContext = context
    override fun close() {
        coroutineContext.cancel()
    }
}

private fun closeWithRuntimeException(obj: Any) {
    if (obj is Closeable) {
        try {
            obj.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}