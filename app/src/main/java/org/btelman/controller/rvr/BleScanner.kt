package org.btelman.controller.rvr

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import androidx.lifecycle.MutableLiveData

/**
 * Created by Brendon on 12/24/2019.
 */
class BleScanner(context: Context){
    data class ScanResult(
        val device : BluetoothDevice,
        val rssi : Int
    )

    val discoveredDevices = HashMap<String, ScanResult>()
    var serviceFilter = ArrayList<java.util.ArrayList<ParcelUuid>>()

    private var handler = Handler(Looper.getMainLooper())
    private var mScanning = false
    // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
    // BluetoothAdapter through BluetoothManager.
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as BluetoothManager
    private var mBluetoothAdapter = bluetoothManager.adapter

    val isScanning: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    var onScanningChanged : ((Boolean)->Unit?)? = null
    var onDiscoveredDevices : ((HashMap<String, ScanResult>)->Unit?)? = null

    fun setDeviceFilter(vararg serviceFilter : ArrayList<ParcelUuid>){
        this.serviceFilter.clear()
        this.serviceFilter.addAll(serviceFilter)
    }

    fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                mScanning = false
                mBluetoothAdapter.stopLeScan(mLeScanCallback)
            }, SCAN_PERIOD)
            discoveredDevices.clear()
            mScanning = true
            mBluetoothAdapter.startLeScan(mLeScanCallback)
        } else {
            mScanning = false
            discoveredDevices.clear()
            mBluetoothAdapter.stopLeScan(mLeScanCallback)
        }
    }

    // Device scan callback.
    private val mLeScanCallback =
        BluetoothAdapter.LeScanCallback { device, rssi, _ ->
            discoveredDevices[device.address] = ScanResult(device, rssi)
            onDiscoveredDevices?.invoke(discoveredDevices)
        }

    companion object{
        private const val SCAN_PERIOD: Long = 10000
    }
}