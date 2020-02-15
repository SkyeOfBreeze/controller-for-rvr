package org.btelman.spherosdk.android.enums

object Colors {
    val RED = arrayOf(0xFF.toByte(), 0x00, 0x00)
    val GREEN = arrayOf(0x00, 0xFF.toByte(), 0x00)
    val BLUE = arrayOf(0x00, 0x00, 0xFF.toByte())
    val OFF = arrayOf(0x00, 0x00, 0x00)
    val WHITE = arrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
    val YELLOW = arrayOf(0xFF.toByte(), 0x90.toByte(), 0x00)
    val PURPLE = arrayOf(0xFF.toByte(), 0x00, 0xFF.toByte())
    val ORANGE = arrayOf(0xFF.toByte(), 0x20.toByte(), 0x00)
    val PINK = arrayOf(0xFF.toByte(), 0x66.toByte(), 0xB2.toByte())

    @Suppress("NAME_SHADOWING")
    fun parse(red : Float, green : Float, blue : Float) : Array<Byte>{
        val red = red.coerceAtMost(255f).coerceAtLeast(0f)
        val green = green.coerceAtMost(255f).coerceAtLeast(0f)
        val blue = blue.coerceAtMost(255f).coerceAtLeast(0f)
        return arrayOf(red.toByte(), green.toByte(), blue.toByte())
    }
}