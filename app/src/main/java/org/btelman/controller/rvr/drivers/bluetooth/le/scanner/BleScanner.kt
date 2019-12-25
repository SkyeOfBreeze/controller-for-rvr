package org.btelman.controller.rvr.drivers.bluetooth.le.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanRecord
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import org.btelman.controller.rvr.drivers.bluetooth.le.scanner.v18.BleScannerV18
import org.btelman.controller.rvr.drivers.bluetooth.le.scanner.v21.BleScannerV21
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by Brendon on 12/24/2019.
 */
abstract class BleScanner protected constructor(context: Context){
    data class ScanResult(
        val device : BluetoothDevice,
        val rssi : Int,
        val scanRecord : ScanRecord? = null
    )

    val discoveredDevices = HashMap<String, ScanResult>()
    var serviceFilter = ArrayList<ParcelUuid>()

    protected var handler = Handler(Looper.getMainLooper())
    protected var mScanning = false
    // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
    // BluetoothAdapter through BluetoothManager.
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as BluetoothManager
    protected var mBluetoothAdapter = bluetoothManager.adapter

    var onScanningChanged : ((Boolean)->Unit?)? = null
    var onDiscoveredDevices : ((HashMap<String, ScanResult>)->Unit?)? = null

    fun setDeviceFilter(vararg serviceFilter : ParcelUuid){
        this.serviceFilter.clear()
        this.serviceFilter.addAll(serviceFilter)
    }

    fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
//            handler.postDelayed({
//                mScanning = false
//                stopScan()
//            },
//                SCAN_PERIOD
//            )
            discoveredDevices.clear()
            mScanning = true
            startScan(serviceFilter)
        } else {
            mScanning = false
            discoveredDevices.clear()
            stopScan()
        }
    }

    protected abstract fun startScan(deviceFilter: ArrayList<ParcelUuid>)
    protected abstract fun stopScan()

    class Builder(val context: Context){
        var legacy = false

        /**
         * Compat builder for the different scanning APIs, with the ability to choose legacy over
         * api supported method
         */
        fun build() : BleScanner{
            return if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP || legacy)
                BleScannerV18(context)
            else
                BleScannerV21(context)
        }
    }

    companion object{
        const val SCAN_PERIOD: Long = 10000
    }
}