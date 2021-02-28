package org.btelman.controller.rvr.utils

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import org.btelman.controller.rvr.Constants
import org.btelman.controller.rvr.drivers.bluetooth.BluetoothBuilder
import org.btelman.controller.rvr.drivers.bluetooth.Connection
import org.btelman.controller.rvr.drivers.bluetooth.le.BluetoothGattInterface
import org.btelman.logutil.kotlin.LogUtil
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.xor

/**
 * Created by Brendon on 12/7/2019.
 */
class RVRManager(context: Context, device : BluetoothDevice){
    val log = LogUtil("RVRManager")
    private val RVR_MAIN_SERVICE = UUID.fromString("00010001-574f-4f20-5370-6865726f2121")
    private val RVR_COMMS_CHAR = UUID.fromString("00010002-574f-4f20-5370-6865726f2121")

    fun wake(){
        Constants.wakeUp[5] = SpheroMotors.getId()
        Constants.wakeUp[6] = SpheroMotors.checksum(Constants.wakeUp, 6)
        send(Constants.wakeUp)
    }

    fun sleep(){
        Constants.powerDown[5] = SpheroMotors.getId()
        Constants.powerDown[6] = SpheroMotors.checksum(Constants.powerDown, 6)
        send(Constants.powerDown)
    }

    val bluetooth = BluetoothBuilder(context, device, BluetoothBuilder.TYPE_GATT_LE).build() as BluetoothGattInterface

    private val onCommCharacteristicUpdate =  { characteristic: BluetoothGattCharacteristic ->
        log.d(characteristic.getStringValue(0))
    }

    fun connect(){
        bluetooth.subscribe(RVR_MAIN_SERVICE, RVR_COMMS_CHAR, onCommCharacteristicUpdate)
        bluetooth.connect()
    }

    fun disconnect(){
        bluetooth.disconnect()
    }

    fun send(packet : ByteArray) {
        // Are we connected?
        if (bluetooth.getStatus() != Connection.STATE_CONNECTED)
            return
        bluetooth.writeBytes(RVR_MAIN_SERVICE, RVR_COMMS_CHAR, packet)
    }
}