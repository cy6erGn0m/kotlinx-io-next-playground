package sample

import kotlinx.cinterop.*

actual enum class ByteOrder {
    BIG_ENDIAN, LITTLE_ENDIAN;

    actual companion object
}

internal actual val nativeByteOrder: ByteOrder = run {
    memScoped {
        val i = alloc<IntVar>()
        i.value = 1
        val bytes = i.reinterpret<ByteVar>()
        if (bytes.value == 0.toByte()) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN
    }
}
