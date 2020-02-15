package org.btelman.spherosdk.android.enums.power

object Commands{
        val SLEEP : Byte = 0X01
        val WAKE : Byte = 0X0D
        val GET_BATTERY_PERCENTAGE : Byte = 0X10
        val GET_BATTERY_VOLTAGE_STATE : Byte = 0X17
        val WILL_SLEEP_NOTIFY : Byte = 0X19
        val DID_SLEEP_NOTIFY : Byte = 0X1A
        val ENABLE_BATTERY_VOLTAGE_STATE_CHANGE_NOTIFY : Byte = 0X1B
        val BATTERY_VOLTAGE_STATE_CHANGE_NOTIFY : Byte = 0X1C
        val GET_BATTERY_VOLTAGE_IN_VOLTS : Byte = 0X25
        val GET_BATTERY_VOLTAGE_STATE_THRESHOLDS : Byte = 0X26
        val GET_CURRENT_SENSE_AMPLIFIER_CURRENT : Byte = 0X27
}