package io.vithor.domehttp

import kotlinx.coroutines.channels.Channel

interface RawData {
    companion object

    fun asString(): String?
    fun asByteArray(): ByteArray?
    fun asByteStream(capacity: Int = Short.MAX_VALUE.toInt()): RawStream?
}

interface RawStream {
    suspend fun toChannel(): Channel<ByteArray>
}