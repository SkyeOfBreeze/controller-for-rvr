package org.btelman.spherosdk.android.enums.systemInfo

/**
 * Created by Brendon on 2/15/2020.
 */
object Commands {
    val GET_MAIN_APPLICATION_VERSION : Byte = 0X00
    val GET_BOOTLOADER_VERSION : Byte = 0X01
    val GET_BOARD_REVISION : Byte = 0X03
    val GET_MAC_ADDRESS : Byte = 0X06
    val GET_STATS_ID : Byte = 0X13
    val GET_PROCESSOR_NAME : Byte = 0X1F
    val GET_SKU : Byte = 0X38
    val GET_CORE_UP_TIME_IN_MILLISECONDS : Byte = 0X39
}