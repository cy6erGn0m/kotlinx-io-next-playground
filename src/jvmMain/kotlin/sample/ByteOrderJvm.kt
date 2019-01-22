package sample

actual enum class ByteOrder(val javaByteOrder: java.nio.ByteOrder) {
    BIG_ENDIAN(java.nio.ByteOrder.BIG_ENDIAN),
    LITTLE_ENDIAN(java.nio.ByteOrder.LITTLE_ENDIAN);

    actual companion object
}

operator fun ByteOrder.Companion.invoke(javaByteOrder: java.nio.ByteOrder): ByteOrder {
    return when (javaByteOrder) {
        java.nio.ByteOrder.BIG_ENDIAN -> ByteOrder.BIG_ENDIAN
        else -> ByteOrder.LITTLE_ENDIAN
    }
}

internal actual inline val nativeByteOrder: ByteOrder
    get() = when (java.nio.ByteOrder.nativeOrder()) {
        java.nio.ByteOrder.BIG_ENDIAN -> ByteOrder.BIG_ENDIAN
        java.nio.ByteOrder.LITTLE_ENDIAN -> ByteOrder.LITTLE_ENDIAN
        else -> throw NoWhenBranchMatchedException("")
    }

