package sample

/**
 * @throws EOFException
 */
actual fun Input.readInt(): Int {
    val value: Int

    read(4) { buffer, start, _ ->
        value = swapIfNeeded(buffer.getIntAt(start))
        4
    }

    return value
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Input.swapIfNeeded(value: Int): Int = when {
    nativeByteOrder -> value
    else -> value.reverseByteOrder()
}