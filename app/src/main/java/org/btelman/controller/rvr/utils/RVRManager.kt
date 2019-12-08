package org.btelman.controller.rvr.utils

import android.bluetooth.BluetoothDevice
import android.content.Context
import no.nordicsemi.android.ble.BleManager
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGatt
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.data.Data
import org.btelman.logutil.kotlin.LogUtil
import java.util.*

/**
 * Created by Brendon on 12/7/2019.
 */
class RVRManager(context: Context) : BleManager<RVRManagerCallbacks>(context) {
    val log = LogUtil("RVRManager")
    val RVR_MAIN_SERVICE = UUID.fromString("00010001-574f-4f20-5370-6865726f2121")

    private val RVR_COMMS_CHAR = UUID.fromString("00010002-574f-4f20-5370-6865726f2121")

    private var comms: BluetoothGattCharacteristic? = null

    override fun getGattCallback(): BleManagerGattCallback {
        log.d {
            "getGattCallback"
        }
        return mGattCallback
    }

    private val mGattCallback = object : BleManagerGattCallback(), DataReceivedCallback {
        override fun onDataReceived(device: BluetoothDevice, data: Data) {
            log.d {
                "onDataReceived"
            }
        }

        override fun initialize() {
            log.d {
                "initialize"
            }
            setNotificationCallback(comms).with(this)
            readCharacteristic(comms).with(this).enqueue()
            enableNotifications(comms).enqueue()
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            log.d {
                "isRequiredServiceSupported"
            }
            val service = gatt.getService(RVR_MAIN_SERVICE)
            if (service != null) {
                comms = service.getCharacteristic(RVR_COMMS_CHAR)
            }

            var writeRequest = false
            comms?.let {
                val rxProperties = it.properties
                writeRequest = rxProperties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0
            }
            log.d {
                "${comms != null && writeRequest}"
            }
            return comms != null && writeRequest
        }

        protected override fun onDeviceDisconnected() {
            log.d {
                "onDeviceDisconnected"
            }
            comms = null
        }
    }

    override fun log(priority: Int, message: String) {
        log.d {
            "$priority $message"
        }
    }

    fun send(packet : ByteArray) {
        // Are we connected?
        if (comms == null)
            return
        writeCharacteristic(comms, packet).enqueue()
    }
}