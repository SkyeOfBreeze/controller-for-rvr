package org.btelman.controller.rvr

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.btelman.controller.rvr.utils.RVRManager
import org.btelman.controller.rvr.utils.RVRManagerCallbacks
import org.btelman.logutil.kotlin.LogUtil

/**
 * Created by Brendon on 12/7/2019.
 */
class RVRViewModel(application: Application) : AndroidViewModel(application), RVRManagerCallbacks {
    private var isAutoConnect = false
    private var manager : RVRManager? = null
    private var logUtil = LogUtil("RVRViewModel")
    private var bleDevice : BluetoothDevice? = null

    val connected: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private val batteryLevel: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>()
    }

    val lastResponse: MutableLiveData<ByteArray> by lazy {
        MutableLiveData<ByteArray>()
    }

    fun connect(bluetoothDevice: BluetoothDevice){
        bleDevice = bluetoothDevice
        logUtil.d{
            "connect ${bluetoothDevice.address}"
        }
        manager?:run{
            logUtil.d{
                "init RVRManager since null"
            }
            manager = RVRManager(getApplication()).also {
                it.setGattCallbacks(this)
            }
        }
        manager?.connect(bluetoothDevice)?.retry(3, 2000)?.enqueue()
    }

    fun sendCommand(command : ByteArray){
        manager?.send(command)
    }

    fun disconnect(){
        bleDevice = null
        logUtil.d{
            "disconnect manually called"
        }
        isAutoConnect = false
        manager?.disconnect()?.enqueue()
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        super.onDeviceConnecting(device)
        logUtil.d{
            "onDeviceConnecting ${device.address}"
        }
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        super.onDeviceConnected(device)
        logUtil.d{
            "onDeviceConnected ${device.address}"
        }
        connected.postValue(true)
    }

    override fun onServicesDiscovered(device: BluetoothDevice, optionalServicesFound: Boolean) {
        super.onServicesDiscovered(device, optionalServicesFound)
        logUtil.d{
            "onServicesDiscovered ${device.address} optionalServicesFound $optionalServicesFound"
        }
    }

    override fun onBondingRequired(device: BluetoothDevice) {
        super.onBondingRequired(device)
        logUtil.d{
            "onBondingRequired ${device.address}"
        }
    }

    override fun onBondingFailed(device: BluetoothDevice) {
        super.onBondingRequired(device)
        logUtil.d{
            "onBondingFailed ${device.address}"
        }
    }

    override fun onBonded(device: BluetoothDevice) {
        super.onBonded(device)
        logUtil.d{
            "onBonded ${device.address}"
        }
    }

    override fun onLinkLossOccurred(device: BluetoothDevice) {
        super.onLinkLossOccurred(device)
        logUtil.d{
            "onLinkLossOccurred ${device.address}"
        }
        connected.postValue(false)
    }

    override fun onDeviceNotSupported(device: BluetoothDevice) {
        super.onDeviceNotSupported(device)
        logUtil.d{
            "onDeviceNotSupported ${device.address}"
        }
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        super.onDeviceReady(device)
        logUtil.d{
            "onDeviceReady ${device.address}"
        }
    }

    override fun onError(device: BluetoothDevice, message: String, errorCode: Int) {
        super.onError(device, message, errorCode)
        logUtil.e{
            "onError ${device.address} : $message : $errorCode"
        }
        connected.postValue(false)
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        super.onDeviceDisconnecting(device)
        logUtil.d{
            "onDeviceDisconnecting ${device.address}"
        }
    }

    override fun onDeviceDisconnected(device: BluetoothDevice) {
        super.onDeviceDisconnected(device)
        logUtil.d{
            "onDeviceDisconnected ${device.address}"
        }
        connected.postValue(false)
        bleDevice?.takeIf { !isAutoConnect }?.let { //reconnect using the auto connect functionality
            isAutoConnect = true
            manager?.connect(it)?.useAutoConnect(true)?.retry(3, 2000)?.enqueue()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}