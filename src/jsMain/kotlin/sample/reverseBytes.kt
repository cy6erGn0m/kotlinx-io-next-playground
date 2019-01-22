@file:Suppress("NOTHING_TO_INLINE")

package sample

import org.khronos.webgl.*

@Suppress("ACTUAL_WITHOUT_EXPECT", "NOTHING_TO_INLINE", "EXPERIMENTAL_FEATURE_WARNING")
actual inline class Memory(val view: DataView) {
    inline val length: Int get() = view.byteLength

    actual inline val byteOrder: ByteOrder get() = nativeByteOrder

    @Suppress("NOTHING_TO_INLINE")
    actual inline fun getAt(index: Int): Byte {
        return view.getInt8(index)
    }

    actual fun slice(offset: Int, length: Int): Memory {
        require(offset >= 0)
        require(length <= view.byteLength)

        return Memory(DataView(view.buffer, view.byteOffset + offset, length))
    }

    actual companion object {
        actual val Empty: Memory = Memory(DataView(ArrayBuffer(0)))
    }
}

actual inline fun Memory.getShortAt(offset: Int): Short {
    return view.getInt16(offset)
}

actual inline fun Memory.getIntAt(offset: Int): Int {
    return view.getInt32(offset)
}

actual inline fun Memory.getLongAt(offset: Int): Long {
    val first = view.getInt32(offset)
    val second = view.getInt32(offset + 4)

    TODO("Not implemented: check native byte order and construct")
}

actual inline fun Memory.getFloatAt(offset: Int): Float {
    return view.getFloat32(offset)
}

actual inline fun Memory.getDoubleAt(offset: Int): Double {
    return view.getFloat64(offset)
}

actual fun Short.reverseByteOrder(): Short = swap(this)
actual fun Int.reverseByteOrder(): Int = swap(this)
actual fun Long.reverseByteOrder(): Long = swap(this)
actual fun Float.reverseByteOrder(): Float = swap(this)
actual fun Double.reverseByteOrder(): Double = swap(this)

private inline fun swap(s: Short): Short = (((s.toInt() and 0xff) shl 8) or ((s.toInt() and 0xffff) ushr 8)).toShort()

private inline fun swap(s: Int): Int =
    (swap((s and 0xffff).toShort()).toInt() shl 16) or (swap((s ushr 16).toShort()).toInt() and 0xffff)

private inline fun swap(s: Long): Long =
    (swap((s and 0xffffffff).toInt()).toLong() shl 32) or (swap((s ushr 32).toInt()).toLong() and 0xffffffff)

private inline fun swap(s: Float): Float = Float.fromBits(swap(s.toRawBits()))

private inline fun swap(s: Double): Double = Double.fromBits(swap(s.toRawBits()))

@Suppress("UnsafeCastFromDynamic")
actual val Memory.array: ByteArray?
    get() = view.asDynamic()