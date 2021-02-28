package org.btelman.controller.rvr.drivers.bluetooth.classic
import org.btelman.controller.rvr.drivers.bluetooth.Connection
import org.btelman.controller.rvr.drivers.bluetooth.classic.BluetoothClassicHandler.Companion.SEND_MESSAGE

/**
 * Bluetooth class to handle classic bluetooth (serial) connections
 */
class BluetoothClassic(val address : String) : BluetoothClassicInterface {
    private val bluetoothService =
        BluetoothClassicHandler()

    @Connection.Status
    override fun getStatus(): Int {
        return bluetoothService.status
    }

    /**
     * Connect to given device
     */
    override fun connect(){
        bluetoothService.serviceHandler
                .obtainMessage(BluetoothClassicHandler.REQUEST_CONNECT, address).sendToTarget()
    }

    /**
     * Disconnect from given device
     */
    override fun disconnect(){
        bluetoothService.serviceHandler.sendEmptyMessage(BluetoothClassicHandler.REQUEST_DISCONNECT)
    }

    /**
     * Send ByteArray to bluetooth processing
     */
    override fun writeBytes(bytes : ByteArray){
        bluetoothService.serviceHandler.obtainMessage(SEND_MESSAGE, bytes).sendToTarget()
    }

    /**
     * Lambda for receiving messages
     */
    override fun onMessage(function : (bytes:ByteArray)->Unit){
        bluetoothService.onMessage(function)
    }

    /**
     * Lambda for state changes
     */
    override fun onStateChange(function: (state: Int) -> Unit){
        bluetoothService.onStateChange(function)
    }

    companion object {
        private const val TAG = "BluetoothClassic"
    }
}
