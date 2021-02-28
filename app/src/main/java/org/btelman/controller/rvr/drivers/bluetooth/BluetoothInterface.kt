package org.btelman.controller.rvr.drivers.bluetooth

/**
 * Created by Brendon on 12/22/2019.
 */
interface BluetoothInterface {
    @Connection.Status
    fun getStatus() : Int

    fun connect()

    fun disconnect()

    /**
     * Lambda for receiving state changes
     */
    fun onStateChange(function: (state: Int) -> Unit)
}