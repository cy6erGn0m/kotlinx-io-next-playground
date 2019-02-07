package sample

import kotlin.contracts.*

annotation class DangerousIoApi

/**
 * Represents an buffered input from a initial, file or a virtual source
 */
expect abstract class Input(initial: IoBuffer) {
    constructor(initial: Memory, size: Int)

    /**
     * Head chunk or `null` if empty or not yet populated from the underlying source.
     */
    @DangerousIoApi
    var head: IoBuffer?
        protected set

    /**
     * Unsafe head memory chunk or `null` if empty or not yet populated from the underlying source.
     * Prefer using read functions instead.
     */
    @DangerousIoApi
    var headChunkMemory: Memory?
        protected set

    /**
     * A byte order that is used to decode multibyte primitives
     */
    var byteOrder: ByteOrder

    /**
     * Reads and returns a byte or fails if no more bytes available.
     * May rethrow exceptions from the underlying source.
     *
     * @throws EOFException
     */
    fun readByte(): Byte

    /**
     * Reads and returns a byte or `-1` if no more bytes available.
     * May rethrow exceptions from the underlying source.
     */
    fun tryReadByte(): Int

    /**
     * Reads and returns a byte or `-1` if no more bytes available.
     * Unlike [readByte] and [tryReadByte] it doesn't consume any bytes.
     *
     * May rethrow exceptions from the underlying source.
     */
    fun peekByte(): Int

    /**
     * Initiate reading data from the underlying source to receive at least [required] bytes.
     * It is not guaranteed that all [required] bytes will be represented as a single chunk.
     * May rethrow exceptions from the underlying source or block for indefinite period of time
     * because of the source.
     * If there are already all [required] bytes available, the underlying source could be untouched.
     *
     * @param required bytes to be available after returning `true`, should not be negative.
     * @return `true` if at least [required] bytes available
     */
    fun tryFill(required: Int): Boolean

    /**
     * Provide/compute the next data chunk. The provided [destination] should be only used inside of [fill]
     * and shouldn't be captured outside.
     *
     * @param destination buffer to write data to
     * @return `true` if this function could be invoked later, `false` if the end of source encountered.
     */
    protected abstract fun fill(destination: IoBuffer): Boolean
}

private fun Input.test2() {
    read { memory, start, end ->
        var sum = 0L
        for (index in start until end) {
            sum += memory[index]
        }
        end - start
    }
}

@UseExperimental(ExperimentalContracts::class)
inline fun Input.read(required: Int = 1, block: (buffer: Memory, start: Int, endExclusive: Int) -> Int) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    var chunk = head ?: ensureFilled(required)
    var start = chunk.start
    var endExclusive = chunk.endExclusive

    if (start == endExclusive || endExclusive - start < required) {
        chunk = ensureFilled(required)
        start = chunk.start
        endExclusive = chunk.endExclusive
    }

    val consumed = block(chunk.memory, start, endExclusive)
    chunk.discard(consumed)
    if (chunk.readRemaining == 0) {
        // TODO drop chunk
    }
}

private fun Input.readStart(required: Int): IoBuffer {
    return ensureFilled(required)
}

fun Input.ensureFilled(required: Int): IoBuffer {
    if (!tryFill(required)) {
        throw EOFException("At least $required bytes required but failed to comply")
    }
    val head = head
    if (head == null || head.size < required) {
        throw EOFException("At least $required bytes required but failed to comply")
    }
    return head
}

fun Input.test(): Char {
    return readIf { it < 0x80 }.toChar()
}

inline fun Input.readIf(block: (Byte) -> Boolean): Int {
    val head = head
    if (head != null && head.size >= 1) {
        val v = head[0]
        if (block(v)) {
            head.discard(1)
            return v.toInt() and 0xff
        }
        return -1
    }

    val v = peekByte()
    if (v == -1 || block(v.toByte())) return v

    return -1
}

fun Input.myPeekByte(): Byte {
    val head = headChunkMemory
    if (head != null) {
        return head[0]
    }

    return myPeekByteFallback()
}

private fun Input.myPeekByteFallback(): Byte {
    return ensureFilled(1)[0]
}

/**
 * Start reading from this input. After using a returned instance should be completed using [next] or [commit]
 * @return memory consist of at least [required] bytes
 */
@DangerousIoApi
expect fun Input.start(required: Int = 1): Memory?

/**
 * Continue reading, mark [consumed] bytes, requesting [next] bytes.
 * After invoking this function the [chunk] should be never used anymore
 * unless it is returned back by this function
 * @return next chunk that should be completed
 */
@DangerousIoApi
expect fun Input.next(chunk: Memory, consumed: Int, next: Int = 1): Memory?

/**
 * Complete reading from [chunk], mark [consumed] bytes
 */
@DangerousIoApi
expect fun Input.commit(chunk: Memory, consumed: Int)

class EOFException(message: String) : Exception(message)

/**
 * @throws EOFException
 */
expect fun Input.readInt(): Int

expect fun Input.readLineUtf8(lengthHint: Int = 16): String?

internal fun Input.readLineUtf8Impl(lengthHint: Int): String? {
    return buildString(lengthHint) {
        do {
            val head = head
            if (head != null) {
                val memory = head.memory
                for (index in head.start until head.endExclusive) {
                    val next = memory[index]
                    if (next == '\n'.toByte()) {
                        head.start = index
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

internal fun Input.readLineUtf8Slow(lengthHint: Int = 16): String {
    return buildString(lengthHint) {
        while (true) {
            val b = readByte()
            if (b == '\n'.toByte()) {
                break
            }
            if (b == '\r'.toByte()) continue
            append(b.toChar())
        }
    }
}

internal fun Input.readLineUtf8Slow2(lengthHint: Int = 16): String {
    return buildString(lengthHint) {
        outer@while (true) {
            val memory = start(1) ?: break
            val head = head!!
            val start = head.start
            val endExclusive = head.endExclusive

            for (index in start until endExclusive) {
                val b = memory[index]
                if (b == '\n'.toByte()) {
                    commit(memory, consumed = index - start + 1)
                    break@outer
                }
                if (b == '\r'.toByte()) continue
                append(b.toChar())
            }

            commit(memory, consumed = head.size)
        }
    }
}
