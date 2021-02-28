package org.btelman.controller.rvr

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.btelman.controller.rvr.drivers.bluetooth.Connection
import org.btelman.controller.rvr.utils.RVRManager
import org.btelman.logutil.kotlin.LogUtil

/**
 * Created by Brendon on 12/7/2019.
 */
class RVRViewModel(application: Application) : AndroidViewModel(application) {
    private var manager : RVRManager? = null
    private var logUtil = LogUtil("RVRViewModel")
    private var bleDevice : BluetoothDevice? = null

    val connectionState: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(Connection.STATE_IDLE)
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
            manager = RVRManager(getApplication(), bluetoothDevice).also {
                it.bluetooth.onStateChange {state ->
                    connectionState.postValue(state)
                    if(state == Connection.STATE_CONNECTED)
                        manager?.wake()
                    logUtil.d{
                        "onStateChange ${state == Connection.STATE_CONNECTED}"
                    }
                }
            }
        }
        manager?.connect()
    }

    fun sendCommand(command : ByteArray){
        manager?.send(command)
    }

    fun disconnect(){
        bleDevice = null
        logUtil.d{
            "disconnect manually called"
        }
        manager?.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    fun sleep() {
        manager?.sleep()
    }

    fun wakeup() {
        manager?.wake()
    }
}