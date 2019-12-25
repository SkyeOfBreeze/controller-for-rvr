package org.btelman.controller.rvr.drivers.bluetooth.le.scanner.v21

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import org.btelman.controller.rvr.drivers.bluetooth.le.scanner.BleScanner
import kotlin.collections.ArrayList

/**
 * Bluetooth scanning for above api 21
 */
@RequiresApi(21)
class BleScannerV21(context: Context) : BleScanner(context) {
    override fun startScan(deviceFilter: ArrayList<ParcelUuid>) {
        val scanFilterList = ArrayList<ScanFilter>()
        deviceFilter.forEach { serviceUUID ->
            ScanFilter.Builder().also {
                it.setServiceUuid(serviceUUID)
            }.build().also {filter ->
                scanFilterList.add(filter)
            }
        }
        val scanSettings = ScanSettings.Builder().also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                it.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                it.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            }
            it.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            it.setReportDelay(0)
        }.build()
        mBluetoothAdapter.bluetoothLeScanner.startScan(scanFilterList, scanSettings, scanCallback)
    }

    override fun stopScan() {
        mBluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
    }

    var scanCallback = object : ScanCallback(){
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                discoveredDevices[it.device.address] =
                    ScanResult(
                        it.device,
                        it.rssi,
                        it.scanRecord
                    )
            }
            onDiscoveredDevices?.invoke(discoveredDevices)
        }

        override fun onBatchScanResults(results: MutableList<android.bluetooth.le.ScanResult>?) {
            super.onBatchScanResults(results)
            results?.forEach {
                discoveredDevices[it.device.address] =
                    ScanResult(
                        it.device,
                        it.rssi,
                        it.scanRecord
                    )
            }
            onDiscoveredDevices?.invoke(discoveredDevices)
        }
    }
}