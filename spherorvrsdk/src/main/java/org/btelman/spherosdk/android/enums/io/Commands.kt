package org.btelman.spherosdk.android.enums.io

/**
 * Created by Brendon on 2/15/2020.
 */
object Commands {
    val SET_ALL_LEDS : Byte= 0X1A
    val GET_ACTIVE_COLOR_PALETTE : Byte = 0X44
    val SET_ACTIVE_COLOR_PALETTE : Byte = 0X45
    val GET_COLOR_IDENTIFICATION_REPORT : Byte = 0X46
    val LOAD_COLOR_PALETTE : Byte = 0X47
    val SAVE_COLOR_PALETTE : Byte = 0X48
    val RELEASE_LED_REQUESTS : Byte = 0X4E
}