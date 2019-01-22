package sample

import java.nio.*

@Suppress("ACTUAL_WITHOUT_EXPECT", "NOTHING_TO_INLINE", "EXPERIMENTAL_FEATURE_WARNING")
actual inline class Memory(val buffer: ByteBuffer) {
    actual inline var byteOrder: ByteOrder
        get() = ByteOrder(buffer.order())
        set(newOrder) {
            buffer.order(newOrder.javaByteOrder)
        }

    inline val size: Int get() = buffer.limit()

    actual inline fun getAt(index: Int): Byte = buffer.get(index)

    actual fun slice(offset: Int, length: Int): Memory =
        Memory(buffer.myDuplicate().apply { position(offset); limit(offset + length) }.mySlice())

    actual companion object {
        actual val Empty: Memory = Memory(ByteBuffer.allocate(0))
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun ByteBuffer.myDuplicate(): ByteBuffer {
    duplicate().apply { return suppressNullCheck() }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun ByteBuffer.mySlice(): ByteBuffer {
    slice().apply { return suppressNullCheck() }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun ByteBuffer.suppressNullCheck(): ByteBuffer {
    return this
}

@Suppress("NOTHING_TO_INLINE")
actual inline fun Memory.getShortAt(offset: Int): Short {
    return buffer.getShort(offset)
}

@Suppress("NOTHING_TO_INLINE")
actual inline fun Memory.getIntAt(offset: Int): Int {
    return buffer.getInt(offset)
}

@Suppress("NOTHING_TO_INLINE")
actual inline fun Memory.getLongAt(offset: Int): Long {
    return buffer.getLong(offset)
}

@Suppress("NOTHING_TO_INLINE")
actual inline fun Memory.getFloatAt(offset: Int): Float {
    return buffer.getFloat(offset)
}

@Suppress("NOTHING_TO_INLINE")
actual inline fun Memory.getDoubleAt(offset: Int): Double {
    return buffer.getDouble(offset)
}

@Suppress("NOTHING_TO_INLINE")
actual inline fun Short.reverseByteOrder(): Short = java.lang.Short.reverseBytes(this)

@Suppress("NOTHING_TO_INLINE")
actual inline fun Int.reverseByteOrder(): Int = java.lang.Integer.reverseBytes(this)

@Suppress("NOTHING_TO_INLINE")
actual inline fun Long.reverseByteOrder(): Long = java.lang.Long.reverseBytes(this)

@Suppress("NOTHING_TO_INLINE")
actual inline fun Float.reverseByteOrder(): Float =
    java.lang.Float.intBitsToFloat(
        java.lang.Integer.reverseBytes(
            java.lang.Float.floatToRawIntBits(this)
        )
    )

@Suppress("NOTHING_TO_INLINE")
actual inline fun Double.reverseByteOrder(): Double =
    java.lang.Double.longBitsToDouble(
        java.lang.Long.reverseBytes(
            java.lang.Double.doubleToRawLongBits(this)
        )
    )

actual val Memory.array: ByteArray?
    get() = when {
        !buffer.hasArray() -> null
        buffer.isReadOnly -> null
        else -> buffer.array()
    }