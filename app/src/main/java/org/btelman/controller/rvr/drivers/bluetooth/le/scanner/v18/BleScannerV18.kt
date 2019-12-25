package org.btelman.controller.rvr.drivers.bluetooth.le.scanner.v18

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.ParcelUuid
import org.btelman.controller.rvr.drivers.bluetooth.le.scanner.BleScanner
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Brendon on 12/24/2019.
 */
class BleScannerV18(context: Context) : BleScanner(context) {
    override fun startScan(deviceFilter: ArrayList<ParcelUuid>) {
        mBluetoothAdapter.startLeScan(mLeScanCallback)
    }

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