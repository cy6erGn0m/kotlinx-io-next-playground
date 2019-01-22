@file:Suppress("NOTHING_TO_INLINE")

package sample

class IoBuffer(val memory: Memory,
               var start: Int = 0,
               var endExclusive: Int) {
    val byteOrder: ByteOrder get() = memory.byteOrder

    val size: Int get() = endExclusive - start
    val readRemaining: Int get() = size

    inline operator fun get(index: Int): Byte {
        return memory[index + start]
    }

    fun discard(n: Int) {
        require(n >= 0)

        val newStart = start + n
        if (newStart > endExclusive) throw IllegalArgumentException("n $n > $size")

        start = newStart
    }
}
