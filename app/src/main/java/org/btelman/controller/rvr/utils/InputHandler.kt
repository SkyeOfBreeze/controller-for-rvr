package org.btelman.controller.rvr.utils

import android.content.Context
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.widget.Toast
import org.btelman.controller.rvr.ui.JoystickSurfaceView

class InputHandler(
    val context: Context,
    val prefsManager: PrefsManager,
    val onInputUpdate: (left: Float, right: Float)->Unit
) : RemoReceiver.RemoListener, JoystickSurfaceView.JoystickUpdateListener,
    GamepadHandler.GamepadListener {
    private var lastGamepadId: Int? = null
    var right = 0.0f
    var left = 0.0f
    var lastUpdated = System.currentTimeMillis()
    var gamepadHandler = GamepadHandler(context)

    init {
        gamepadHandler.registerListener(this)
    }

    fun onDestroy(){
        gamepadHandler.onDestroy()
    }

    private fun sendInputUpdatedEvent() {
        lastUpdated = System.currentTimeMillis()
        onInputUpdate(left, right)
    }

    fun processMotionEvent(event: MotionEvent) : Boolean{
        // Check that the event came from a game controller
        if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK && event.action == MotionEvent.ACTION_MOVE) {
            processJoystickInput(event, -1)
            return true
        }
        return false
    }

    private fun getCenteredAxis(
        event: MotionEvent,
        device: InputDevice, axis: Int, historyPos: Int
    ): Float {
        val range = device.getMotionRange(axis, event.source)

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            val flat = range.flat
            val value = if (historyPos < 0)
                event.getAxisValue(axis)
            else
                event.getHistoricalAxisValue(axis, historyPos)

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value
            }
        }
        return 0f
    }

    private fun processJoystickInput(
        event: MotionEvent,
        historyPos: Int
    ) {

        val mInputDevice = event.device

        // Calculate the vertical distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat switch, or the right control stick.
        val linearSpeed = -getCenteredAxis(
            event, mInputDevice,
            MotionEvent.AXIS_Y, historyPos
        )
        val rotateSpeed = -getCenteredAxis(
            event, mInputDevice,
            MotionEvent.AXIS_Z, historyPos
        )
        DriveUtil.rcDrive(linearSpeed*prefsManager.maxSpeed, rotateSpeed*prefsManager.maxTurnSpeed,true).also {
            left = it.first
            right = it.second
            lastGamepadId = mInputDevice.id
        }
        sendInputUpdatedEvent()
    }

    override fun onCommand(command: String) {
        Log.d("InputHandler",command)
        val linearSpeed : Float
        val rotateSpeed : Float
        when (command.replace("\r\n", "")) {
            "f" -> {
                linearSpeed = 1f
                rotateSpeed = 0f
            }
            "b" -> {
                linearSpeed = -1f
                rotateSpeed = 0f
            }
            "r" -> {
                linearSpeed = 0f
                rotateSpeed = -1f
            }
            "l" -> {
                linearSpeed = 0f
                rotateSpeed = 1f
            }
            else -> {
                linearSpeed = 0f
                rotateSpeed = 0f
            }
        }
        DriveUtil.rcDrive(linearSpeed*prefsManager.maxSpeed, rotateSpeed*prefsManager.maxTurnSpeed,true).also {
            left = it.first
            right = it.second
        }
        sendInputUpdatedEvent()
    }

    override fun OnJoystickUpdate(id: Int, joystickAxes: FloatArray?) {
        if(joystickAxes == null) return
        val y = joystickAxes[0]
        val x = joystickAxes[1]

        //we will ignore the ID since we are only using one joystick for this
        DriveUtil.rcDrive(-y*prefsManager.maxSpeed, -x*prefsManager.maxTurnSpeed, true).also {
            left = it.first
            right = it.second
        }
        sendInputUpdatedEvent()
    }

    override fun onConnection(device: Int) {
        //ignore
    }

    override fun onDisconnect(device: Int) {
        if(lastGamepadId == device){
            Toast.makeText(context, "Lost connection to gamepad", Toast.LENGTH_SHORT).show()
            left = 0f
            right = 0f
            sendInputUpdatedEvent()
        }
    }
}