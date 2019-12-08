package org.btelman.controller.rvr.activities

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.activity_main.*
import org.btelman.controller.rvr.R
import org.btelman.controller.rvr.views.BLEScanSnackBarThing
import org.btelman.logutil.kotlin.LogUtil
import android.net.Uri.fromParts
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.content.Intent
import android.os.Handler
import android.view.InputDevice
import android.view.MotionEvent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.content_main.*
import org.btelman.controller.rvr.RVRViewModel
import org.btelman.controller.rvr.utils.SpheroMotors


class MainActivity : AppCompatActivity() {
    private lateinit var viewModelRVR: RVRViewModel
    private lateinit var handler : Handler
    private var allowPermissionClickedTime = 0L
    private val PERM_REQUEST_LOCATION = 234
    private var bleLayout: BLEScanSnackBarThing? = null

    val log = LogUtil("MainActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        handler = Handler()
        viewModelRVR = ViewModelProviders.of(this)[RVRViewModel::class.java]
        viewModelRVR.connected.observe(this, Observer<Boolean> {
            jelloWorldTextView.text = if(it) "connected" else "disconnected"
        })
        jelloWorldTextView.setOnClickListener {
            disconnectFromDevice()
        }
        fab.setOnClickListener { view ->
            if(bleLayout?.isShown != true){
                val ready = checkPerms()
                if(ready)
                    showScanLayout()
                else
                    showPermissionsRationale()
            }
            else{
                hideScanLayout()
            }
        }
    }

    private fun showPermissionsRationale() {
        Snackbar.make(mainCoordinatorLayout, R.string.btPermRequestText, Snackbar.LENGTH_INDEFINITE).also {
            it.setAction("Allow"){
                allowPermissionClickedTime = System.currentTimeMillis()
                requestPerms()
            }
        }.show()
    }

    private fun checkPerms() : Boolean{
        return !(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
    }

    fun requestPerms(){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERM_REQUEST_LOCATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERM_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    showScanLayout()
                } else {
                    if(System.currentTimeMillis() - allowPermissionClickedTime < 500){
                        val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                        Toast.makeText(this,
                            "Please enable the location permission in Permissions section", Toast.LENGTH_LONG).show()
                    }
                    else{
                        Toast.makeText(this,
                            "Location permission denied! Unable to scan for RVR. " +
                                    "Location permission only is used for bluetooth and data is not shared.", Toast.LENGTH_LONG).show()
                    }
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hideScanLayout()
    }

    fun showScanLayout() {
        fab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        bleLayout ?: let {
            bleLayout = BLEScanSnackBarThing.make(mainCoordinatorLayout)
        }
        if(bleLayout?.isShown != true) {
            bleLayout?.onItemClickedListener = {
                log.d { it.toString() }
                hideScanLayout()
                handler.postDelayed({
                    connectToDevice(it.device)
                }, 2000)
            }
            bleLayout?.show()
        }
    }

    private fun disconnectFromDevice(){
        viewModelRVR.disconnect()
    }

    private fun connectToDevice(device: BluetoothDevice) {
        log.d { "connectToDevice" }
        viewModelRVR.connect(device)
    }

    fun hideScanLayout(){
        fab.setImageResource(android.R.drawable.stat_sys_data_bluetooth)
        if(bleLayout?.isShown == true) {
            bleLayout?.dismiss()
            bleLayout?.onItemClickedListener = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        // Check that the event came from a game controller
        if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK && event.action == MotionEvent.ACTION_MOVE) {
            processJoystickInput(event, -1)
            return true
        }
        return super.onGenericMotionEvent(event)
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
        val left = -getCenteredAxis(
            event, mInputDevice,
            MotionEvent.AXIS_Y, historyPos
        )
        val right = -getCenteredAxis(
            event, mInputDevice,
            MotionEvent.AXIS_RZ, historyPos
        )
        val command = SpheroMotors.drive(left, right)
        viewModelRVR.sendCommand(command)
    }

    /* TODO fun onCommand(command: String) {
        val speed = .5f
        var leftMode = 0x0
        var rightMode = 0x0
        when (command.replace("\r\n", "")) {
            "f" -> {
                leftMode = 0x1
                rightMode = 0x1
            }
            "b" -> {
                leftMode = 0x2
                rightMode = 0x2
            }
            "r" -> {
                leftMode = 0x1
                rightMode = 0x2
            }
            "l" -> {
                leftMode = 0x2
                rightMode = 0x1
            }
        }
        val commandByteArray = SpheroMotors.drive(leftMode, leftSpeed, rightMode, rightSpeed)
        viewModelRVR.sendCommand(commandByteArray)
    } */
}
