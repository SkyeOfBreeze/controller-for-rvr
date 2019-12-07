package org.btelman.controller.rvr.utils

import android.content.Context
import no.nordicsemi.android.ble.BleManager
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGatt
import java.util.*

/**
 * Created by Brendon on 12/7/2019.
 */
class RVRManager(context: Context) : BleManager<RVRManagerCallbacks>(context) {
    val RVR_MAIN_SERVICE = UUID.fromString("00001523-1212-efde-1523-785feabcd123")

    private val RVR_COMMS_CHAR = UUID.fromString("00001524-1212-efde-1523-785feabcd123")

    private var comms: BluetoothGattCharacteristic? = null

    override fun getGattCallback(): BleManagerGattCallback {
        return mGattCallback
    }

    private val mGattCallback = object : BleManagerGattCallback() {
        protected override fun initialize() {

        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(RVR_MAIN_SERVICE)
            if (service != null) {
                comms = service.getCharacteristic(RVR_COMMS_CHAR)
            }

            var writeRequest = false
            comms?.let {
                val rxProperties = it.properties
                writeRequest = rxProperties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0
            }
            return comms != null && writeRequest
        }

        protected override fun onDeviceDisconnected() {
            comms = null
        }
    }

    fun send(on: Boolean) {
        // Are we connected?
        if (comms == null)
            return
        writeCharacteristic(comms, ByteArray(0)).enqueue()
    }
}