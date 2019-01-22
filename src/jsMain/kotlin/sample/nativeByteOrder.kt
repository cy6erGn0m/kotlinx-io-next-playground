package sample

import org.khronos.webgl.*


actual enum class ByteOrder {
    BIG_ENDIAN, LITTLE_ENDIAN;

    actual companion object
}

internal actual val nativeByteOrder: ByteOrder = run {

    val buffer = ArrayBuffer(4)
    val arr = Int32Array(buffer)
    val view = DataView(buffer)

    arr[0] = 0x11223344

    if (view.getInt32(0, true) == 0x11223344) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN
}
