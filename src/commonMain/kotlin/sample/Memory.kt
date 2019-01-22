package sample

/**
 * Represents linear non-interleaved range of bytes
 */
expect class Memory {
    /**
     * Memory's byte order in which primitive numeric values are read and should be written.
     * On some platforms this property could be modifiable
     */
    val byteOrder: ByteOrder

    /**
     * Returns byte at [index] position. Depending on the platform it could be safe or unsafe.
     */
    inline fun getAt(index: Int): Byte

    /**
     * Returns memory's subrange. On some platforms it could do range checks but it is not guaranteed to be safe.
     */
    fun slice(offset: Int, length: Int): Memory

    companion object {
        /**
         * Represents an empty memory region
         */
        val Empty: Memory
    }
}

/**
 * Access to the backing array or `null` if not accessible or not array-backed memory
 * TODO questionable
 */
expect val Memory.array: ByteArray?

@Suppress("NOTHING_TO_INLINE")
inline operator fun Memory.get(index: Int): Byte = getAt(index)

/**
 * Read short signed 16bit integer with byte order specified in [Memory.byteOrder]
 */
expect inline fun Memory.getShortAt(offset: Int): Short

/**
 * Read short signed 32bit integer with byte order specified in [Memory.byteOrder]
 */
expect inline fun Memory.getIntAt(offset: Int): Int

/**
 * Read short signed 64bit integer with byte order specified in [Memory.byteOrder]
 */
expect inline fun Memory.getLongAt(offset: Int): Long

/**
 * Read short signed 32bit floating point number with byte order specified in [Memory.byteOrder]
 */
expect inline fun Memory.getFloatAt(offset: Int): Float

/**
 * Read short signed 64bit floating point number with byte order specified in [Memory.byteOrder]
 */
expect inline fun Memory.getDoubleAt(offset: Int): Double

/**
 * Reverse number's byte order
 */
expect fun Short.reverseByteOrder(): Short

/**
 * Reverse number's byte order
 */
expect fun Int.reverseByteOrder(): Int

/**
 * Reverse number's byte order
 */
expect fun Long.reverseByteOrder(): Long

/**
 * Reverse number's byte order
 */
expect fun Float.reverseByteOrder(): Float

/**
 * Reverse number's byte order
 */
expect fun Double.reverseByteOrder(): Double

fun Memory.printAll(size: Int): String = buildString(size * 2) {
    for (i in 0 until size) {
        val v = getAt(i).toInt() and 0xff
        append(((v shr 4) + 0x30).toChar())
        append((v and 0x0f).toChar())
    }
}

fun Memory.getMyValue() = getIntAt(7).reverseByteOrder()