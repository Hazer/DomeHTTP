@file:Suppress("NOTHING_TO_INLINE")

package io.vithor.domehttp

import kotlinx.atomicfu.atomic
import kotlinx.cinterop.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import platform.Foundation.NSData
import platform.Foundation.NSInputStream
import platform.Foundation.inputStreamWithData
import platform.darwin.NSUInteger
import platform.darwin.UInt16
import platform.posix.uint8_tVar
import kotlin.math.min

data class NSRawData(val data: NSData) : RawData {

    override fun asString(): String? {
//        return NSString.create(data, NSUTF8StringEncoding).toString()
        return data.string()
    }

    override fun asByteArray(): ByteArray? {
        return data.bytes()?.readBytes(data.length().toInt())
    }

    override fun asByteStream(capacity: Int): RawStream? {
        val nsIS = NSInputStream.inputStreamWithData(data) ?: return null

        return NSRawStream(nsIS, capacity)
    }
}

inline fun RawData.Companion.of(data: NSData): RawData {
    return NSRawData(data)
}


class NSRawStream(private val inputStream: NSInputStream, capacity: Int) : RawStream {
    private val bufferSize = min(UInt16.MAX_VALUE.toInt(), capacity)

    private val channel: Channel<ByteArray> = Channel(Channel.UNLIMITED)
    private val started = atomic(false)

    @UseExperimental(ExperimentalCoroutinesApi::class)
    private suspend fun execute() {
        if (started.compareAndSet(expect = false, update = true)) {
            memScoped {
                val buffer: CPointer<uint8_tVar> = UByteArray(bufferSize).toCValues().getPointer(memScope)
                var readSize: Int

                inputStream.open()

                do {
                    readSize = inputStream.read(buffer = buffer, maxLength = bufferSize.toNSUInteger()).toInt()

                    when {
                        readSize > 0 -> {
                            channel.send(buffer.readBytes(bufferSize))
                        }

                        readSize < 0 -> {
                            // throw InputStreamError.unreadableStream(inputStream)
                            channel.close(Exception("Unreadable Stream"))
                        }

                        readSize == 0 -> channel.close()
                    }

                } while (readSize > 0)

                inputStream.close()

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


//suspend fun <T: NSStream, R> T.use(block: suspend (T) -> R): R {
//    var throwable: Throwable? = null
//    try {
//        this.open()
//        return block(this)
//    } catch (e: Throwable) {
//        throwable = e
//        throw e
//    } finally {
//        try {
//            if (throwable == null) {
//                this.close()
//            }
//        } catch (ex: Throwable) {
//            // ignore
//        }
//    }
//}
