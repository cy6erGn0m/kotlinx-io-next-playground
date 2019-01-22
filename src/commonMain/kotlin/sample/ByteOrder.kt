package sample

expect enum class ByteOrder {
    BIG_ENDIAN,
    LITTLE_ENDIAN;

    companion object
}

val ByteOrder.Companion.nativeOrder: ByteOrder get() = nativeByteOrder

internal expect val nativeByteOrder: ByteOrder
