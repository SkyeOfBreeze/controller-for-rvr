package org.btelman.controller.rvr.activities

import android.Manifest
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


class MainActivity : AppCompatActivity() {
    private var allowPermissionClickedTime = 0L
    private val PERM_REQUEST_LOCATION = 234
    private var bleLayout: BLEScanSnackBarThing? = null
    val log = LogUtil("MainActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

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
                connectToDevice(it.device)
                log.d { it.toString() }
                hideScanLayout()
            }
            bleLayout?.show()
        }
    }

    private fun disconnectFromDevice(){

    }

    private fun connectToDevice(device: BluetoothDevice) {

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
}
