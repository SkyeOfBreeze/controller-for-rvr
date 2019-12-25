package org.btelman.controller.rvr.drivers.bluetooth.le.scanner.v18

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.ParcelUuid
import org.btelman.controller.rvr.drivers.bluetooth.le.scanner.BleScanner
import kotlin.collections.ArrayList

/**
 * Legacy scanning methods for ble
 */
class BleScannerV18(context: Context) : BleScanner(context) {
    @Suppress("DEPRECATION")
    override fun startScan(deviceFilter: ArrayList<ParcelUuid>) {
        mBluetoothAdapter.startLeScan(mLeScanCallback)
    }

    @Suppress("DEPRECATION")
    override fun stopScan() {
        mBluetoothAdapter.stopLeScan(mLeScanCallback)
    }

    // Device scan callback.
    protected val mLeScanCallback =
        BluetoothAdapter.LeScanCallback { device, rssi, _ ->
            discoveredDevices[device.address] =
                ScanResult(
                    device,
                    rssi
                )
            onDiscoveredDevices?.invoke(discoveredDevices)
        }
}