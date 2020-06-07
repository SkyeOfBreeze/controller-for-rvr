package org.btelman.spherosdk.android.commands

import android.bluetooth.BluetoothClass
import org.btelman.spherosdk.android.enums.Devices
import org.btelman.spherosdk.android.enums.drive.Commands
import org.btelman.spherosdk.android.protocols.SpheroPacketBuilder
import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.xor
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Created by Brendon on 2/14/2020.
 */
class Drive{
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

    fun drive(leftMode: Int, leftSpeed: Int, rightMode: Int, rightSpeed: Int): SpheroPacketBuilder {
        val data = ByteBuffer.allocate(4).also {
            it.put(leftMode.toByte())
            it.put(leftSpeed.toByte())
            it.put(rightMode.toByte())
            it.put(rightSpeed.toByte())
        }.array()
        return SpheroPacketBuilder().also {
            it.did = Devices.DRIVE
            it.cid = Commands.RAW_MOTORS
            //not sending target or source
        }
    }
}