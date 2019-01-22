package sample



fun Input.test2(): Char {
    return readIf2 { it < 0x80 }.toChar()
}

inline fun Input.readIf2(block: (Byte) -> Boolean): Int {
    do {
        val head = headByteBuffer
        if (head != null && head.hasRemaining()) {
            val v = head.get()
            if (block(v)) {
                return v.toInt() and 0xff
            }
            head.position(head.position() - 1)
            return -1
        }

        if (!tryFill(1)) return -1
    } while (true)
}
