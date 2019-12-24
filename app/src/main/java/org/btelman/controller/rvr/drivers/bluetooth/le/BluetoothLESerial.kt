package org.btelman.controller.rvr.drivers.bluetooth.le

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Message
import org.btelman.controller.rvr.drivers.bluetooth.Connection
import java.util.*

class BluetoothLESerial(context : Context, val address: String) : BluetoothGattInterface {
    var serial = BluetoothLeHandler(context) //todo do we really need context?

    @Connection.Status
    override fun getStatus(): Int {
        return serial.status
    }

    override fun connect() {
        Message.obtain(serial.serviceHandler,BluetoothLeHandler.REQUEST_CONNECT, address).sendToTarget()
    }

    override fun disconnect() {
        serial.serviceHandler.sendEmptyMessage(BluetoothLeHandler.REQUEST_DISCONNECT)
    }

    override fun subscribe(service : UUID, characteristic: UUID, callback : (BluetoothGattCharacteristic)->Unit){
        Message.obtain(serial.serviceHandler).also {
            it.what = BluetoothLeHandler.REGISTER_UUID
            it.obj = callback
            it.data.also { bundle ->
                bundle.putSerializable(BluetoothLeHandler.SERVICE_UUID_KEY, service)
                bundle.putSerializable(BluetoothLeHandler.CHARACTERISTIC_UUID_KEY, characteristic)
            }
        }.sendToTarget()
    }

    override fun writeBytes(service: UUID, characteristic: UUID, bytes: ByteArray) {
        Message.obtain(serial.serviceHandler).also {
            it.what = BluetoothLeHandler.SEND_MESSAGE
            it.obj = bytes
            it.data.also { bundle ->
                bundle.putSerializable(BluetoothLeHandler.SERVICE_UUID_KEY, service)
                bundle.putSerializable(BluetoothLeHandler.CHARACTERISTIC_UUID_KEY, characteristic)
            }
        }.sendToTarget()
    }

    override fun onStateChange(function: (state: Int) -> Unit) {
        serial.onStateChange(function)
    }
}
