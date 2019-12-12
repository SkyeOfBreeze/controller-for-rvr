package org.btelman.controller.rvr.activities

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import android.content.pm.PackageManager.FEATURE_BLUETOOTH_LE
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.view.*
import android.widget.SeekBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.content_main.*
import org.btelman.controller.rvr.RVRViewModel
import org.btelman.controller.rvr.utils.DriveUtil
import org.btelman.controller.rvr.utils.RemoReceiver
import org.btelman.controller.rvr.utils.SpheroMotors
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), RemoReceiver.RemoListener {
    private var maxSpeed = 1.0f
    private var maxTurnSpeed = 1.0f
    private var right = 0.0f
    private var left = 0.0f
    private var viewModelRVR: RVRViewModel? = null
    private var handler : Handler? = null
    private var allowPermissionClickedTime = 0L
    private val PERM_REQUEST_LOCATION = 234
    private var bleLayout: BLEScanSnackBarThing? = null
    private lateinit var remoInterface : RemoReceiver

    val log = LogUtil("MainActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        if(!packageManager.hasSystemFeature(FEATURE_BLUETOOTH_LE)){
            connectionStatusView.text = "Device does not support required bluetooth mode"
            log.e { "Device does not support Bluetooth LE" }
            return
        }
        maxSpeed = getSharedPreferences("RVR", Context.MODE_PRIVATE).getFloat("maxSpeed", .7f).also {
            linearSpeedMaxValue.progress = (it*100.0f).roundToInt()
        }
        maxTurnSpeed = getSharedPreferences("RVR", Context.MODE_PRIVATE).getFloat("maxTurnSpeed", .7f).also {
            rotationSpeedMaxValue.progress = (it*100.0f).roundToInt()
        }
        handler = Handler()
        viewModelRVR = ViewModelProviders.of(this)[RVRViewModel::class.java]
        viewModelRVR!!.connected.observe(this, Observer<Boolean> {
            connectionStatusView.text = if(it) "connected" else "disconnected"
        })
        linearSpeedMaxValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                maxSpeed = progress/100.0f
                getSharedPreferences("RVR", Context.MODE_PRIVATE).edit().putFloat("maxSpeed", maxSpeed).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        rotationSpeedMaxValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                maxTurnSpeed = progress/100.0f
                getSharedPreferences("RVR", Context.MODE_PRIVATE).edit().putFloat("maxTurnSpeed", maxTurnSpeed).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        remoInterface = RemoReceiver(this, this)
        remoInterface.register()

        connectionStatusView.setOnClickListener {
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

    override fun onResume() {
        super.onResume()
        right = 0.0f
        left = 0.0f
        scheduleNewMotorLooper()
    }

    override fun onPause() {
        super.onPause()
        hideScanLayout()
        handler?.removeCallbacks(motorLooper)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_privacy -> {
                startActivity(Intent(Intent.ACTION_VIEW).also {
                    it.data = Uri.parse("https://btelman.org/privacy/controller-for-rvr.html")
                })
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        remoInterface.unregister()
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
        if (Build.VERSION.SDK_INT >= 23) {
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
        }
        else return true
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

    fun showScanLayout() {
        fab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        bleLayout ?: let {
            bleLayout = BLEScanSnackBarThing.make(mainCoordinatorLayout)
        }
        if(bleLayout?.isShown != true) {
            bleLayout?.onItemClickedListener = {
                log.d { it.toString() }
                hideScanLayout()
                handler?.postDelayed({
                    connectToDevice(it.device)
                }, 2000)
            }
            BluetoothAdapter.getDefaultAdapter().also {
                if(!it.isEnabled)
                    it.enable()
            }
            handler?.postDelayed({
                if(!BluetoothAdapter.getDefaultAdapter().isEnabled) {
                    Toast.makeText(this, "Please ensure bluetooth is on and try again", Toast.LENGTH_SHORT).show()
                    return@postDelayed
                }
                if(bleLayout?.isShown != true && !isFinishing)
                    bleLayout?.show()
            }, 500)
        }
    }

    private fun disconnectFromDevice(){
        viewModelRVR?.disconnect()
    }

    private fun connectToDevice(device: BluetoothDevice) {
        log.d { "connectToDevice" }
        viewModelRVR?.connect(device)
        connectionStatusView.text = "connecting..."
    }

    private val motorLooper = Runnable {
        viewModelRVR?.let { viewModel->
            if(viewModel.connected.value == true){
                val axes = joystickSurfaceView.joystickAxes
                var command : ByteArray
                if(axes[0] != 0.0f || axes[1] != 0.0f){
                    DriveUtil.rcDrive(-axes[1]*maxSpeed, -axes[0]*maxTurnSpeed, true).also {
                        val left = it.first
                        val right = it.second
                        command = SpheroMotors.drive(left, right)
                    }
                } else{
                    command = SpheroMotors.drive(left, right)
                }
                viewModel.sendCommand(command)
            }
            scheduleNewMotorLooper()
        }
    }

    private fun scheduleNewMotorLooper() {
        handler?.postDelayed(motorLooper, 45)
    }

    fun hideScanLayout(){
        fab.setImageResource(android.R.drawable.stat_sys_data_bluetooth)
        if(bleLayout?.isShown == true) {
            bleLayout?.dismiss()
            bleLayout?.onItemClickedListener = null
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
        val linearSpeed = -getCenteredAxis(
            event, mInputDevice,
            MotionEvent.AXIS_Y, historyPos
        )
        val rotateSpeed = -getCenteredAxis(
            event, mInputDevice,
            MotionEvent.AXIS_Z, historyPos
        )
        DriveUtil.rcDrive(linearSpeed*maxSpeed, rotateSpeed*maxTurnSpeed,true).also {
            left = it.first
            right = it.second
        }
    }

    override fun onCommand(command: String) {
        var linearSpeed : Float
        var rotateSpeed : Float
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
        DriveUtil.rcDrive(linearSpeed*maxSpeed, rotateSpeed*maxTurnSpeed,true).also {
            left = it.first
            right = it.second
        }
    }
}
