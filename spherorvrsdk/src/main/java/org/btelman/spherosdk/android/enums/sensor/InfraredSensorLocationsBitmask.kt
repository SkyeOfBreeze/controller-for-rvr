package org.btelman.spherosdk.android.enums.sensor

/**
 * Created by Brendon on 2/15/2020.
 */
object InfraredSensorLocationsBitmask {
    val NONE = 0
    val FRONT_LEFT = 0X000000FF
    val FRONT_RIGHT = 0X0000FF00
    val BACK_RIGHT = 0X00FF0000
    val BACK_LEFT = 0XFF000000
}