@file:Suppress("NOTHING_TO_INLINE")

package io.vithor.domehttp

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.io.InputStream
import okhttp3.Response
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

data class OkHttpRawData(val response: Response) : RawData {

    override fun asString(): String? {
        return response.body()?.string()
    }

    override fun asByteArray(): ByteArray? {
        return response.body()?.bytes()
    }

    override fun asByteStream(capacity: Int): RawStream? {
        val ins = response.body()?.byteStream() ?: return null
        return OkRawStream(ins, capacity)
    }
}

inline fun RawData.Companion.of(response: Response): RawData {
    return OkHttpRawData(response)
}


class OkRawStream(private val inputStream: InputStream, capacity: Int) : RawStream {
    private val bufferSize = min(Short.MAX_VALUE.toInt(), capacity)

    private val channel: Channel<ByteArray> = Channel(Channel.UNLIMITED)
    private val started = AtomicBoolean(false)

    private suspend fun execute() {
        if (started.compareAndSet(false, true)) {
            inputStream.use {
                val buffer = ByteArray(bufferSize)
                var readSize: Int

                do {
                    readSize = inputStream.read(buffer)

                    when {
                        readSize > 0 -> {
                            channel.send(buffer)
                        }

                        readSize < 0 -> {
                            // throw InputStreamError.unreadableStream(inputStream)
                            channel.close(Exception("Unreadable Stream"))
                        }

                    }

                } while (readSize > 0)

                if (!channel.isClosedForSend) {
                    channel.close()
                }
            }
        }
    }

    override suspend fun toChannel(): Channel<ByteArray> {
        return coroutineScope {
            launch {
                execute()
            }

            channel
        }
    }
}