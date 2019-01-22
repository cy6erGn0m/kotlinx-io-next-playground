@file:Suppress("NOTHING_TO_INLINE")

package sample

import kotlinx.cinterop.*

@Suppress("ACTUAL_WITHOUT_EXPECT", "EXPERIMENTAL_FEATURE_WARNING")
actual inline class Memory(@PublishedApi internal val pointer: CPointer<ByteVar>) {
    actual inline val byteOrder: ByteOrder
        get() = ByteOrder.nativeOrder

    actual inline fun getAt(index: Int): Byte = pointer[index]

    actual fun slice(offset: Int, length: Int): Memory {
        return Memory(pointer.plus(offset)!!)
    }

    actual companion object {
        actual val Empty: Memory = Memory(1L.toCPointer()!!)
    }
}

actual inline fun Memory.getShortAt(offset: Int): Short = pointer.plus(offset)!!.reinterpret<ShortVar>().pointed.value

actual inline fun Memory.getIntAt(offset: Int): Int = pointer.plus(offset)!!.reinterpret<IntVar>().pointed.value
actual inline fun Memory.getLongAt(offset: Int): Long = pointer.plus(offset)!!.reinterpret<LongVar>().pointed.value
actual inline fun Memory.getFloatAt(offset: Int): Float = pointer.plus(offset)!!.reinterpret<FloatVar>().pointed.value
actual inline fun Memory.getDoubleAt(offset: Int): Double = pointer.plus(offset)!!.reinterpret<DoubleVar>().pointed.value

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

actual val Memory.array: ByteArray? get() = null