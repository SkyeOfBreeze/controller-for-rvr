package org.btelman.controller.rvr.utils

import android.content.Context
import android.hardware.input.InputManager
import android.os.Handler
import android.view.InputDevice

class GamepadHandler(
    val context: Context,
    val inputManager: InputManager =
        context.getSystemService(Context.INPUT_SERVICE) as InputManager,
    handler : Handler = Handler()
) : InputManager.InputDeviceListener {
    private var listener: GamepadListener? = null
    private var devices = HashMap<Int, InputDevice>()

    init {
        val deviceIds = InputDevice.getDeviceIds()
        deviceIds.forEach {
            maybeAttachDevice(it)
        }
        inputManager.registerInputDeviceListener(this, handler)
    }

    fun getDevice(id : Int) : InputDevice?{
        return devices[id]
    }

    fun getGamepads() : MutableCollection<InputDevice>{
        return devices.values
    }

    fun registerListener(listener: GamepadListener){
        this.listener = listener
    }

    fun onDestroy(){
        this.listener = null
        inputManager.unregisterInputDeviceListener(this)
    }

    interface GamepadListener{
        fun onConnection(device : Int)
        fun onDisconnect(device : Int)
    }

    override fun onInputDeviceRemoved(deviceId: Int) {
        if(devices.containsKey(deviceId)){
            devices.remove(deviceId)
            listener?.onDisconnect(deviceId)
        }
    }

    override fun onInputDeviceAdded(deviceId: Int) {
        maybeAttachDevice(deviceId)
    }

    override fun onInputDeviceChanged(deviceId: Int) {
        //TODO?
    }

    fun maybeAttachDevice(deviceId: Int){
        val device = inputManager.getInputDevice(deviceId) ?: return
        val hasFlags = InputDevice.SOURCE_GAMEPAD or InputDevice.SOURCE_JOYSTICK
        val isGamepad = device.sources and hasFlags == hasFlags
        if(isGamepad){
            devices[deviceId] = device
            listener?.onConnection(deviceId)
        }
    }
}