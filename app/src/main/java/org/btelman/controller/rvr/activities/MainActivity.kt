package org.btelman.controller.rvr.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import org.btelman.controller.rvr.R
import org.btelman.controller.rvr.views.BLEScanSnackBarThing
import org.btelman.logutil.kotlin.LogUtil


class MainActivity : AppCompatActivity() {
    private var bleLayout: BLEScanSnackBarThing? = null
    val log = LogUtil("MainActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            if(bleLayout?.isShown != true){
                showScanLayout()
            }
            else{
                hideScanLayout()
            }
        }
    }

    fun showScanLayout(){
        fab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        bleLayout ?: let {
            bleLayout = BLEScanSnackBarThing.make(mainCoordinatorLayout)
        }
        bleLayout?.onItemClickedListener = {
            log.d { it.toString() }
            hideScanLayout()
        }
        bleLayout?.show()
    }

    fun hideScanLayout(){
        fab.setImageResource(android.R.drawable.stat_sys_data_bluetooth)
        bleLayout?.dismiss()
        bleLayout?.onItemClickedListener = null
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
