package org.btelman.controller.rvr.drivers.bluetooth.le

import android.bluetooth.*
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import org.btelman.controller.rvr.drivers.ble.DeviceScanActivity
import org.btelman.controller.rvr.drivers.bluetooth.Connection
import org.btelman.controller.rvr.drivers.bluetooth.classic.BluetoothClassicHandler
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random

class BluetoothLeHandler(val context: Context){
    private val registrantQueue = ArrayDeque<RegisterRequest>()
    private var mBluetoothManager: BluetoothManager? = null
    var mBluetoothAdapter: BluetoothAdapter? = null
    private var selectedDevice : BluetoothDevice? = null
    private var gattDevice : BluetoothGatt? = null

    @Connection.Status
    var status = Connection.STATE_IDLE

    private var messageListener: ((ByteArray) -> Unit)? = null
    private var stateListener: ((Int) -> Unit)? = null

    data class CharacteristicRegistrant(
       var characteristic : BluetoothGattCharacteristic?,
       val callback : (BluetoothGattCharacteristic)->Unit
    )

    init {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager == null) {
                throw UnsupportedOperationException("Device does not support bluetooth!")
            }
        }
        mBluetoothAdapter = mBluetoothManager?.adapter ?:
                throw UnsupportedOperationException("Device does not support bluetooth!")
        if(mBluetoothAdapter?.isEnabled != true)
            throw ExceptionInInitializerError("Bluetooth must be enabled!")
    }

    private val characteristicHolder = HashMap<UUID, HashMap<UUID, CharacteristicRegistrant>>()

    fun unregister(service : UUID, characteristicUUID: UUID){
        //TODO see if there are reasons to dispose of it differently
        //TODO use handlerthread for thread safety
        characteristicHolder[service]?.remove(characteristicUUID)
    }

    private val handlerThread = HandlerThread("BluetoothLE ${Random.nextInt()}").also {
        it.start()
    }

    internal val serviceHandler = Handler(handlerThread.looper){ message ->
        when(message.what){
            SEND_MESSAGE -> tryWriteBytes(message)
            REQUEST_CONNECT -> tryConnect(message.obj as String)
            REQUEST_DISCONNECT -> (message.obj as? Boolean)?.let {
                tryDisconnect(it)
            } ?: tryDisconnect()
            REGISTER_UUID -> registerUUIDWhenReady(message)
        }
        true
    }

    private fun registerUUIDWhenReady(message: Message?) {
        message?:return
        val service : UUID = message.data[SERVICE_UUID_KEY] as UUID
        val characteristicUUID : UUID = message.data[CHARACTERISTIC_UUID_KEY] as UUID
        //we know what we are doing. This is the only thing that it will receive. Crash if that is not correct
        @Suppress("UNCHECKED_CAST")
        val callback = message.obj as (BluetoothGattCharacteristic) -> Unit
        enqueueRegister(service, characteristicUUID, callback)
    }

    private fun enqueueRegister(
        service: UUID,
        characteristicUUID: UUID,
        callback: (BluetoothGattCharacteristic) -> Unit
    ) {
        registrantQueue.push(RegisterRequest(service, characteristicUUID, callback))
    }

    private fun tryPublishState(@Connection.Status state : Int) {
        if(status == state) return
        status = state
        stateListener?.invoke(status)
    }

    private fun tryDequeueRegisterEvents() {
        gattDevice?:return //cannot run if gatt device is null
        registrantQueue.forEach {
            //dequeue a registrant
            val data = it
            val service = data.service
            val characteristicUUID = data.characteristicUUID
            val callback = data.callback
            characteristicHolder[service]?:let{
                characteristicHolder[service] = HashMap()
            }
            characteristicHolder[service]!![characteristicUUID]?:let {
                gattDevice?.getService(service)?.getCharacteristic(characteristicUUID)?.let { characteristic ->
                    characteristicHolder[service]!![characteristicUUID] = CharacteristicRegistrant(characteristic, callback)
                }
            }
        }
        registrantQueue.clear()

        //scan for ones that have no characteristic, as it gets cleared on disconnects
        characteristicHolder.forEach{ entry ->
            //iterate through our registered services
            val service = entry.key //grab our service uuid
            entry.value.forEach { registrantEntry ->
                //iterate through the registered characteristics in our service
                val characteristicUUID = registrantEntry.key //grab our characteristic uuid
                //check to see if our registered characteristic exists
                //try to get the characteristic instance from gatt
                gattDevice?.getService(service)?.getCharacteristic(characteristicUUID)?.let { characteristic ->
                    //put the characteristic in our holder
                    registrantEntry.value.characteristic = characteristic
                }
            }
        }
    }

    val callback = object : BluetoothGattCallback(){
        private var lastRssi = 0
        private var disconnectCounter = 0
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            characteristic?:return
            characteristicHolder[characteristic.service.uuid]?.get(characteristic.uuid)?.
                callback?.invoke(characteristic)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.d("BluetoothLeHandler", "onServicesDiscovered")
            tryDequeueRegisterEvents()
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when(newState){
                BluetoothProfile.STATE_CONNECTED -> {
                    tryPublishState(Connection.STATE_CONNECTED)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    tryPublishState(Connection.STATE_DISCONNECTED)
                    //set all characteristics to null
                    characteristicHolder.forEach{ entry ->
                        //iterate through our registered services
                        entry.value.forEach { registrantEntry ->
                            //iterate through the registered characteristics in our service
                            registrantEntry.value.characteristic = null
                        }
                    }
                }
            }
        }

        /**
         * Because bluetooth takes a long time to disconnect, we will disconnect prematurely,
         * and try to connect again
         */
        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            if (lastRssi == rssi) {
                disconnectCounter++
                if (disconnectCounter > 70) {
//                    tryDisconnect(true)
//                    selectedDevice?.address?.let {
//                        tryConnect(it)
//                    }
//                    disconnectCounter = 0
                }
            } else {
                disconnectCounter = 0
            }
            lastRssi = rssi
        }
    }

    private fun tryConnect(address : String){
        mBluetoothAdapter?:return
        tryPublishState(Connection.STATE_CONNECTING)
        if (!mBluetoothAdapter!!.isEnabled) {
            //TODO ASK INSTEAD OF ENABLING
            if(!mBluetoothAdapter!!.enable()){
                tryPublishState(Connection.STATE_ERROR)
                return
            }
        }
        mBluetoothAdapter!!.cancelDiscovery()
        selectedDevice = mBluetoothAdapter!!.getRemoteDevice(address)
        gattDevice = selectedDevice?.connectGatt(context, false, callback)
    }

    private fun tryDisconnect(errorOccurred : Boolean = false){
        selectedDevice = null
        if(errorOccurred)
            tryPublishState(Connection.STATE_DISCONNECTED)
        else
            tryPublishState(Connection.STATE_IDLE) //connection is now idle. Do not attempt to restart
    }

    /**
     * Attempt to write bytes. Failure points are if we are not connected
     */
    private fun tryWriteBytes(message: Message?) {
        message?:return
        val service : UUID = message.data[SERVICE_UUID_KEY] as UUID
        val characteristicUUID : UUID = message.data[CHARACTERISTIC_UUID_KEY] as UUID
        val data = message.obj as ByteArray
        characteristicHolder[service]?.get(characteristicUUID)?.characteristic?.let { characteristic ->
            characteristic.value = data
            gattDevice?.writeCharacteristic(characteristic)
        }
    }

    fun enqueueDisconnectFromError(){
        serviceHandler.sendMessageAtFrontOfQueue(serviceHandler.obtainMessage(
            BluetoothClassicHandler.REQUEST_DISCONNECT, true))
    }

    fun onMessage(function : (bytes:ByteArray)->Unit){
        messageListener = function
    }

    fun onStateChange(function: (state: Int) -> Unit){
        stateListener = function
    }

    companion object{
        internal const val SEND_MESSAGE = 0
        internal const val REQUEST_CONNECT = 1
        internal const val REQUEST_DISCONNECT = 2
        internal const val REGISTER_UUID = 4

        internal const val SERVICE_UUID_KEY = "SERVICE"
        internal const val CHARACTERISTIC_UUID_KEY = "CHARACTERISTIC_UUID"
    }
}

private data class RegisterRequest(
    val service: UUID,
    val characteristicUUID: UUID,
    val callback: (BluetoothGattCharacteristic) -> Unit
)