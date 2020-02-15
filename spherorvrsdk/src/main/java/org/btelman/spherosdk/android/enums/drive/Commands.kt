package org.btelman.spherosdk.android.enums.drive

object Commands{
        val RAW_MOTORS : Byte = 0X01
        val RESET_YAW : Byte = 0X06
        val DRIVE_WITH_HEADING : Byte = 0X07
        val ENABLE_MOTOR_STALL_NOTIFY : Byte = 0X25
        val MOTOR_STALL_NOTIFY : Byte = 0X26
        val ENABLE_MOTOR_FAULT_NOTIFY : Byte = 0X27
        val MOTOR_FAULT_NOTIFY : Byte = 0X28
        val GET_MOTOR_FAULT_STATE : Byte = 0X29
}