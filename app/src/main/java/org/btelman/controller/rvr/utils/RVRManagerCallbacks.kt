package org.btelman.controller.rvr.utils

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.BleManagerCallbacks

interface RVRManagerCallbacks : BleManagerCallbacks {
    override fun onDeviceDisconnecting(device: BluetoothDevice) {}

    override fun onDeviceDisconnected(device: BluetoothDevice) {}

    override fun onDeviceConnected(device: BluetoothDevice){}

    override fun onDeviceNotSupported(device: BluetoothDevice) {}

    override fun onBondingFailed(device: BluetoothDevice) {}

    override fun onServicesDiscovered(device: BluetoothDevice, optionalServicesFound: Boolean) {}

    override fun onBondingRequired(device: BluetoothDevice) {}

    override fun onLinkLossOccurred(device: BluetoothDevice) {}

    override fun onBonded(device: BluetoothDevice) {}

    override fun onDeviceReady(device: BluetoothDevice) {}

    override fun onError(device: BluetoothDevice, message: String, errorCode: Int) {}

    override fun onDeviceConnecting(device: BluetoothDevice) {}
}
