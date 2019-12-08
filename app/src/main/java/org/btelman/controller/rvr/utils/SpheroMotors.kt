package org.btelman.controller.rvr.utils

import kotlin.experimental.and
import kotlin.experimental.xor

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
