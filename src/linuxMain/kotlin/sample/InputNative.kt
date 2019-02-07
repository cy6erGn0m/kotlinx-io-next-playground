package sample

import kotlinx.cinterop.*

/**
 * Represents an buffered input from a initial, file or a virtual source
 */
actual abstract class Input actual constructor(initial: IoBuffer) {
    actual constructor(initial: Memory, size: Int) : this(IoBuffer(initial, 0, size))

    /**
     * Head chunk or `null` if empty
     */
    actual var head: IoBuffer? = initial

    /**
     * Unsafe head memory chunk. Prefer using read functions instead.
     */
    actual var headChunkMemory: Memory? = initial.memory

    private var ptr: Long = headChunkMemory?.pointer.toLong()
    private var end: Long = ptr + initial.size

    /**
     * A byte order that is used to decode multibyte primitives
     */
    actual var byteOrder: ByteOrder = ByteOrder.nativeOrder
        protected set(newOrder) {
            field = newOrder
            nativeByteOrder = newOrder == ByteOrder.nativeOrder
        }

    var nativeByteOrder: Boolean = byteOrder == ByteOrder.nativeOrder
        protected set


    /**
     * Reads and returns a byte or fails if no more bytes available
     * @throws EOFException
     */
    actual fun readByte(): Byte {
        val ptr = ptr
        val pointer = ptr.toCPointer<ByteVar>()

        if (pointer != null) {
            if (ptr == end) return readByteFallback()
            val value = pointer.pointed.value
            this.ptr = ptr + 1
            return value
        }

        return readByteFallback()
    }

    private fun readByteFallback(): Byte {
        ensureFilled(1).let {
            val value = it[0]
            it.discard(1)
            return value
        }
    }

    /**
     * Reads and returns a byte or `-1` if no more bytes available
     */
    actual fun tryReadByte(): Int {
        val ptr = ptr
        val pointer = ptr.toCPointer<ByteVar>()

        if (pointer != null) {
            if (ptr == end) return tryReadByteFallback()
            val value = pointer.pointed.value
            this.ptr = ptr + 1
            return value.toInt() and 0xff
        }

        return tryReadByteFallback()
    }

    private fun tryReadByteFallback(): Int {
        if (!tryFill(1)) return -1
        return tryReadByte()
    }

    /**
     * Reads and returns a byte or `-1` if no more bytes available. Unlike [tryReadByte] it doesn't consume any bytes
     */
    actual fun peekByte(): Int {
        val ptr = ptr
        val pointer = ptr.toCPointer<ByteVar>()

        if (pointer != null) {
            if (ptr == end) return tryReadByteFallback()
            return pointer.pointed.value.toInt() and 0xff
        }

        return peekByteFallback()
    }

    private fun peekByteFallback(): Int {
        if (!tryFill(1)) return -1
        return peekByte()
    }

    actual fun tryFill(required: Int): Boolean {
        TODO()
    }

    /**
     * Provide/compute the next data chunk. The provided [destination] should be only used inside of [fill]
     * and shouldn't be captured outside.
     *
     * @param destination buffer to write data to
     * @return `true` if this function could be invoked later, `false` if the end of source encountered.
     */
    protected actual abstract fun fill(destination: IoBuffer): Boolean
}
