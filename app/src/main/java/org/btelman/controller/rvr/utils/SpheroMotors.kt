package org.btelman.controller.rvr.utils

import kotlin.experimental.and
import kotlin.experimental.xor
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Created by Brendon on 10/23/2019.
 */
object SpheroMotors {
    var driveCommand = byteArrayOf(
        (-115).toByte(),
        24.toByte(),
        2.toByte(),
        22.toByte(),
        1.toByte(),
        0.toByte(),
        2.toByte(),
        (-119).toByte(),
        1.toByte(),
        (-119).toByte(),
        (-87).toByte(),
        (-40).toByte()
    )

    /**
     * Assumes range of -1.0 to 1.0. using anything outside this may break things
     */
    fun drive(leftSpeed : Float, rightSpeed : Float) : ByteArray{
        val adjustedLeft = abs(leftSpeed*255f)
        val adjustedRight = abs(rightSpeed*255f)
        val leftMode = if(leftSpeed > 0) 0x1 else if(leftSpeed < 0) 0x2 else 0x00
        val rightMode = if(rightSpeed > 0) 0x1 else if(rightSpeed < 0) 0x2 else 0x00
        return drive(leftMode, adjustedLeft.roundToInt(), rightMode, adjustedRight.roundToInt())
    }

    fun drive(leftMode: Int, leftSpeed: Int, rightMode: Int, rightSpeed: Int): ByteArray {
        driveCommand[5]++
        driveCommand[6] = leftMode.toByte()
        driveCommand[7] = leftSpeed.toByte()
        driveCommand[8] = rightMode.toByte()
        driveCommand[9] = rightSpeed.toByte()
        driveCommand[10] = checksum(driveCommand, 10)
        return driveCommand
    }

    private fun checksum(driveCommand: ByteArray, checksumIndex: Int): Byte {
        var sum: Byte = 0
        //skip index 0
        for (i in 1 until checksumIndex) {
            sum = (sum + driveCommand[i]).toByte()
        }
        //sum += 1;
        return (sum and 0xFF.toByte() xor 0xFF.toByte()).toByte()
    }
}
