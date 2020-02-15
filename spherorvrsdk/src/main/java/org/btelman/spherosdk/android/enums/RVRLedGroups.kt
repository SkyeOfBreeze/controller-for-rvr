package org.btelman.spherosdk.android.enums

import org.btelman.spherosdk.android.enums.rvr.SpheroRvrLedBitmasks

/**
 * Created by Brendon on 2/15/2020.
 */
@Suppress("MemberVisibilityCanBePrivate")
object RVRLedGroups {
    val STATUS_INDICATION_LEFT = SpheroRvrLedBitmasks.LEFT_STATUS_INDICATION_RED or
        SpheroRvrLedBitmasks.LEFT_STATUS_INDICATION_GREEN or
        SpheroRvrLedBitmasks.LEFT_STATUS_INDICATION_BLUE

    val STATUS_INDICATION_RIGHT = SpheroRvrLedBitmasks.RIGHT_STATUS_INDICATION_RED or
    SpheroRvrLedBitmasks.RIGHT_STATUS_INDICATION_GREEN or
    SpheroRvrLedBitmasks.RIGHT_STATUS_INDICATION_BLUE

    val HEADLIGHT_LEFT = SpheroRvrLedBitmasks.LEFT_HEADLIGHT_RED or
            SpheroRvrLedBitmasks.LEFT_HEADLIGHT_GREEN or
            SpheroRvrLedBitmasks.LEFT_HEADLIGHT_BLUE

    val HEADLIGHT_RIGHT = SpheroRvrLedBitmasks.RIGHT_HEADLIGHT_RED or
            SpheroRvrLedBitmasks.RIGHT_HEADLIGHT_GREEN or
            SpheroRvrLedBitmasks.RIGHT_HEADLIGHT_BLUE

    val BATTERY_DOOR_FRONT = SpheroRvrLedBitmasks.BATTERY_DOOR_FRONT_RED or
            SpheroRvrLedBitmasks.BATTERY_DOOR_FRONT_GREEN or
            SpheroRvrLedBitmasks.BATTERY_DOOR_FRONT_BLUE

    val BATTERY_DOOR_REAR = SpheroRvrLedBitmasks.BATTERY_DOOR_REAR_RED or
            SpheroRvrLedBitmasks.BATTERY_DOOR_REAR_GREEN or
            SpheroRvrLedBitmasks.BATTERY_DOOR_REAR_BLUE

    val POWER_BUTTON_FRONT = SpheroRvrLedBitmasks.POWER_BUTTON_FRONT_RED or
            SpheroRvrLedBitmasks.POWER_BUTTON_FRONT_GREEN or
            SpheroRvrLedBitmasks.POWER_BUTTON_FRONT_BLUE

    val POWER_BUTTON_REAR = SpheroRvrLedBitmasks.POWER_BUTTON_REAR_RED or
            SpheroRvrLedBitmasks.POWER_BUTTON_REAR_GREEN or
            SpheroRvrLedBitmasks.POWER_BUTTON_REAR_BLUE

    val BRAKELIGHT_LEFT = SpheroRvrLedBitmasks.LEFT_BRAKELIGHT_RED or
            SpheroRvrLedBitmasks.LEFT_BRAKELIGHT_GREEN or
            SpheroRvrLedBitmasks.LEFT_BRAKELIGHT_BLUE

    val BRAKELIGHT_RIGHT = SpheroRvrLedBitmasks.RIGHT_BRAKELIGHT_RED or
            SpheroRvrLedBitmasks.RIGHT_BRAKELIGHT_GREEN or
            SpheroRvrLedBitmasks.RIGHT_BRAKELIGHT_BLUE

    val ALL_LIGHTS = STATUS_INDICATION_LEFT or
        STATUS_INDICATION_RIGHT or
        HEADLIGHT_LEFT or
        HEADLIGHT_RIGHT or
        BATTERY_DOOR_FRONT or
        BATTERY_DOOR_REAR or
        POWER_BUTTON_FRONT or
        POWER_BUTTON_REAR or
        BRAKELIGHT_LEFT or
        BRAKELIGHT_RIGHT

    val UNDERCARRIAGE_WHITE = SpheroRvrLedBitmasks.UNDERCARRIAGE_WHITE
}