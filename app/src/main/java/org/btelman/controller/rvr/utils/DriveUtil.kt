package org.btelman.controller.rvr.utils

import kotlin.math.max

/**
 * Created by Brendon on 12/10/2019.
 */
object DriveUtil {
    fun rcDrive(linearSpeed : Float, turnSpeed : Float, squaredInputs : Boolean) : Pair<Float, Float>{
        var linearSpeed = linearSpeed
        var turnSpeed = turnSpeed
        val leftMotorSpeed: Float
        val rightMotorSpeed: Float

        if (squaredInputs) {
            // square the inputs (while preserving the sign) to increase fine control while permitting full power
            linearSpeed = if (linearSpeed >= 0.0) {
                (linearSpeed * linearSpeed)
            } else {
                -(linearSpeed * linearSpeed)
            }
            turnSpeed = if (turnSpeed >= 0.0) {
                (turnSpeed * turnSpeed)
            } else {
                -(turnSpeed * turnSpeed)
            }
        }

        if (linearSpeed > 0.0) {
            if (turnSpeed > 0.0) {
                leftMotorSpeed = linearSpeed - turnSpeed
                rightMotorSpeed = max(linearSpeed, turnSpeed)
            } else {
                leftMotorSpeed = max(linearSpeed, -turnSpeed)
                rightMotorSpeed = linearSpeed + turnSpeed;
            }
        } else {
            if (turnSpeed > 0.0) {
                leftMotorSpeed = -max(-linearSpeed, turnSpeed)
                rightMotorSpeed = linearSpeed + turnSpeed
            } else {
                leftMotorSpeed = linearSpeed - turnSpeed
                rightMotorSpeed = -max(-linearSpeed, -turnSpeed)
            }
        }
        return Pair(leftMotorSpeed, rightMotorSpeed)
    }
}