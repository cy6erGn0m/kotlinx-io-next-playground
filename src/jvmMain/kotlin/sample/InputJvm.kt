package sample

import java.nio.*

/**
 * Represents an buffered input from a initial, file or a virtual source
 */
actual abstract class Input actual constructor(initial: IoBuffer) {
    actual constructor(initial: Memory, size: Int) : this(IoBuffer(initial, size))

    @DangerousIoApi
    actual var head: IoBuffer? = initial
        protected set

    actual var headChunkMemory: Memory? = head?.memory
        internal set

    @DangerousIoApi
    inline val headByteBuffer: ByteBuffer?
        get() = head?.memory?.buffer

    /**
     * A byte order that is used to decode multibyte primitives
     */
    actual var byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN
        set(newValue) {
            field = newValue
            headByteBuffer?.order(newValue.javaByteOrder)
        }

    /**
     * Reads and returns a byte or fails if no more bytes available
     * @throws EOFException
     */
    actual fun readByte(): Byte {
        val head = headByteBuffer
        if (head != null && head.hasRemaining()) return head.get()
        return readByteFallback()
    }

    private fun readByteFallback(): Byte {
        return ensureFilled(1).memory.buffer.get()
    }

    /**
     * Reads and returns a byte or `-1` if no more bytes available
     */
    actual fun tryReadByte(): Int {
        val head = headByteBuffer
        if (head != null && head.hasRemaining()) return head.get().toInt() and 0xff
        else if (tryFill(1)) return tryReadByte()
        else return -1
    }

    /**
     * Reads and returns an unsigned byte or `-1` if no more bytes available. Unlike [tryReadByte] it doesn't consume any bytes
     */
    actual fun peekByte(): Int {
        val head = headByteBuffer
        if (head != null) {
            val position = head.position()
            val limit = head.limit()
            if (position < limit) return head[position].toInt() and 0xff
        }

        return peekByteFallback()
    }

    private fun peekByteFallback(): Int {
        if (tryFill(1)) {
            return peekByte()
        } else {
            return -1
        }
    }

    actual fun tryFill(required: Int): Boolean {
        // TODO
        val chunk = IoBuffer(Memory(ByteBuffer.allocate(8192)), endExclusive = 8192)
        val result = fill(chunk)
        this.head = chunk
        this.headChunkMemory = chunk.memory
        return true
    }

    protected actual abstract fun fill(dst: IoBuffer): Boolean
}

@DangerousIoApi
actual fun Input.start(required: Int): Memory? {
    val head = headChunkMemory
    if (head != null && head.buffer.remaining() >= required) {
        return head
    }
    return startFallback(required)
}

private fun Input.startFallback(required: Int): Memory? {
    if (tryFill(required)) {
        val head = head!!
        check(head.size >= required)

        return head.memory
    }

    return null
}

@DangerousIoApi
actual fun Input.next(chunk: Memory, consumed: Int, next: Int): Memory? {
    TODO()
}

@DangerousIoApi
actual fun Input.commit(chunk: Memory, consumed: Int) {
    TODO()
}

actual fun Input.readInt(): Int {
    val head = headByteBuffer
    if (head != null && head.remaining() >= 4) {
        return head.getInt() // we don't care about byte order since head is always properly configured
    }

    return readIntFallback()
}

private fun Input.readIntFallback(): Int {
    ensureFilled(4)
    return headByteBuffer!!.getInt()
}

actual fun Input.readLineUtf8(lengthHint: Int): String? {
    return buildString(lengthHint) {
        do {
            val head = headByteBuffer
            if (head != null) {
                while (head.hasRemaining()) {
                    val next = head.get()
                    if (next == '\n'.toByte()) {
                        return@buildString
                    }
                    if (next == '\r'.toByte()) continue
                    if (next >= 0x80) {
                        // fallback to UTF-8?
                        TODO()
                    }
                    append(next.toChar())
                }
            }
        } while (tryFill(1))

        if (length == 0) return null
    }
}