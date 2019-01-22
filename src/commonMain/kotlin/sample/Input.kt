package sample

annotation class DangerousIoApi

/**
 * Represents an buffered input from a initial, file or a virtual source
 */
expect abstract class Input(initial: IoBuffer) {
    constructor(initial: Memory, size: Int)

    /**
     * Head chunk or `null` if empty
     */
    @DangerousIoApi
    var head: IoBuffer?
        protected set

    /**
     * Unsafe head memory chunk. Prefer using read functions instead.
     */
    @DangerousIoApi
    var headChunkMemory: Memory?
        internal set

    /**
     * A byte order that is used to decode multibyte primitives
     */
    var byteOrder: ByteOrder

    /**
     * Reads and returns a byte or fails if no more bytes available
     * @throws EOFException
     */
    fun readByte(): Byte

    /**
     * Reads and returns a byte or `-1` if no more bytes available
     */
    fun tryReadByte(): Int

    /**
     * Reads and returns a byte or `-1` if no more bytes available. Unlike [tryReadByte] it doesn't consume any bytes
     */
    fun peekByte(): Int

    /**
     * Initiate reading data from the underlying source
     * @return `true` if [required] bytes available
     */
    fun tryFill(required: Int): Boolean

    /**
     * Provide/compute the next data chunk.
     * @return `true` if it could be invoked later, `false` if end of source encountered
     */
    protected abstract fun fill(dst: IoBuffer): Boolean
}

fun Input.test2() {
    read { memory, start, end ->
        var sum = 0L
        for (index in start until end) {
            sum += memory[index]
        }
        end - start
    }
}

inline fun Input.read(required: Int = 1, block: (buffer: Memory, start: Int, endExclusive: Int) -> Int) {
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
    val head = head
    if (head != null && head.size >= 1) {
        return head[0]
    }

    return myPeekByteFallback()
}

private fun Input.myPeekByteFallback(): Byte {
    return ensureFilled(1)[0]
}

/**
 * Start reading from this input. After using a returned instance it should be completed using [next] or [commit]
 * @return memory consist of at least [required] bytes
 */
@DangerousIoApi
expect fun Input.start(required: Int): Memory?

/**
 * Continue reading, mark [consumed] bytes, requesting [next] bytes.
 * After invoking this function the [chunk] should be never used anymore
 * unless it is returned back by this function
 * @return next chunk that should be completed
 */
@DangerousIoApi
expect fun Input.next(chunk: Memory, consumed: Int, next: Int): Memory?

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

expect fun Input.readLineUtf8(lengthHint: Int): String?

fun Input.readLineUtf8(): String? = readLineUtf8(16)

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

            for (index in 0 until head.size) {
                val b = memory[index]
                if (b == '\n'.toByte()) {
                    commit(memory, index + 1)
                    break@outer
                }
                if (b == '\r'.toByte()) continue
                append(b.toChar())
            }

            commit(memory, head.size)
        }
    }
}
