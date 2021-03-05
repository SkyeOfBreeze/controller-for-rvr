package org.btelman.controller.rvr.activities

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
import org.btelman.controller.rvr.utils.*
import org.btelman.controller.rvr.utils.RemoReceiver
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private var previousCommand: Long = System.currentTimeMillis()
    private var timedOut = false
    private lateinit var inputHandler: InputHandler
    private lateinit var prefsManager: PrefsManager
    private var right = 0.0f
    private var left = 0.0f
    private var viewModelRVR: RVRViewModel? = null
    private var handler : Handler? = null
    private var allowPermissionClickedTime = 0L
    private var bleLayout: BLEScanSnackBarThing? = null
    private lateinit var remoInterface : RemoReceiver

    val log = LogUtil("MainActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        prefsManager = PrefsManager(this)
        inputHandler = InputHandler(this, prefsManager, this::onInputUpdated)
        if(!packageManager.hasSystemFeature(FEATURE_BLUETOOTH_LE)){
            connectionStatusView.text = "Device does not support required bluetooth mode"
            log.e { "Device does not support Bluetooth LE" }
            return
        }
        linearSpeedMaxValue.progress = (prefsManager.maxSpeed*100.0f).roundToInt()
        rotationSpeedMaxValue.progress = (prefsManager.maxTurnSpeed*100.0f).roundToInt()
        handler = Handler()
        viewModelRVR = ViewModelProviders.of(this)[RVRViewModel::class.java]
        viewModelRVR!!.connected.observe(this, Observer<Boolean> {
            connectionStatusView.text = if(it) "connected" else "disconnected"
            mainCoordinatorLayout.keepScreenOn = if(it) prefsManager.keepScreenAwake else false
        })
        linearSpeedMaxValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefsManager.maxSpeed = progress/100.0f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        rotationSpeedMaxValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefsManager.maxTurnSpeed = progress/100.0f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        remoInterface = RemoReceiver(this, inputHandler)
        remoInterface.register()

        joystickSurfaceView.setListener(inputHandler)

        connectionStatusView.setOnClickListener {
            disconnectFromDevice()
        }
        fab.setOnClickListener { view ->
            if(bleLayout?.isShown != true){
                disconnectFromDevice()
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

    override fun onDestroy() {
        super.onDestroy()
        remoInterface.unregister()
        inputHandler.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu).also {
            menu?.findItem(R.id.action_keep_screen_on)?.isChecked = prefsManager.keepScreenAwake
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_privacy -> {
                startActivity(Intent(Intent.ACTION_VIEW).also {
                    it.data = Uri.parse("https://btelman.org/privacy/controller-for-rvr.html")
                })
            }
            R.id.action_keep_screen_on -> {
                val checked = !item.isChecked
                item.isChecked = checked
                prefsManager.keepScreenAwake = checked
                if(viewModelRVR?.connected?.value == true)
                    mainCoordinatorLayout.keepScreenOn = checked
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH && resultCode == RESULT_OK) {
            showScanLayout()
        }
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

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        return inputHandler.processMotionEvent(event) || super.onGenericMotionEvent(event)
    }

    fun onInputUpdated(left : Float, right: Float){
        this.left = left
        this.right = right
        sendMotorCommandFrame()
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
            PERM_REQUEST_LOCATION
        )
    }

    fun showScanLayout() {
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
            if(!BluetoothAdapter.getDefaultAdapter().isEnabled) {
                Snackbar.make(mainCoordinatorLayout, "Bluetooth needs to be enabled", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Turn on"){
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
                    }.show()
                return
            }
            fab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            if(bleLayout?.isShown != true && !isFinishing)
                bleLayout?.show()
        }
    }

    fun hideScanLayout(){
        fab.setImageResource(android.R.drawable.stat_sys_data_bluetooth)
        if(bleLayout?.isShown == true) {
            bleLayout?.dismiss()
            bleLayout?.onItemClickedListener = null
        }
    }

    private fun disconnectFromDevice(){
        viewModelRVR?.disconnect()
    }

    private fun connectToDevice(device: BluetoothDevice) {
        log.d { "connectToDevice" }
        disconnectFromDevice()
        connectionStatusView.text = "connecting..."
        viewModelRVR?.connect(device)
    }

    private val motorLooper = Runnable {
        sendMotorCommandFrame()
        scheduleNewMotorLooper()
    }

    private fun sendMotorCommandFrame() {
        viewModelRVR?.let { viewModel->
            if(viewModel.connected.value == true){
                val lastInputCommand = System.currentTimeMillis() - inputHandler.lastUpdated
                if(lastInputCommand > prefsManager.timeoutMs){
                    if(timedOut && lastInputCommand > prefsManager.timeoutMs + 1000) return //allow it to send the stop command for a second
                    timedOut = true
                    left = 0f
                    right = 0f
                }
                else{
                    if(timedOut || System.currentTimeMillis() - previousCommand > prefsManager.timeoutMs){ //we hit the timeout at some point, better make sure RVR is awake
                        viewModel.wake()
                    }
                    timedOut = false
                }
                val axes = joystickSurfaceView.joystickAxes
                var command : ByteArray
                if(axes[0] != 0.0f || axes[1] != 0.0f){
                    DriveUtil.rcDrive(-axes[1]*prefsManager.maxSpeed, -axes[0]*prefsManager.maxTurnSpeed, true).also {
                        val left = it.first
                        val right = it.second
                        command = SpheroMotors.drive(left, right)
                    }
                } else{
                    command = SpheroMotors.drive(left, right)
                }
                viewModel.sendCommand(command)
                previousCommand = inputHandler.lastUpdated
            }
        }
    }

    private fun scheduleNewMotorLooper() {
        handler?.postDelayed(motorLooper, 45)
    }

    companion object {
        private const val PERM_REQUEST_LOCATION = 234
        private const val REQUEST_ENABLE_BLUETOOTH = 2221
    }
}
