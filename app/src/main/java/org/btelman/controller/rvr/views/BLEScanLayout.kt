package org.btelman.controller.rvr.views

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.ContentViewCallback
import kotlinx.android.synthetic.main.ble_scan_layout.view.*
import org.btelman.controller.rvr.drivers.bluetooth.le.scanner.BleScanner
import org.btelman.controller.rvr.drivers.bluetooth.le.scanner.v21.BleScannerV21
import org.btelman.controller.rvr.utils.RVRProps
import org.btelman.logutil.kotlin.LogUtil

/**
 * BLE Layout that handles scanning for bluetooth devices
 */
class BLEScanLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) , ContentViewCallback {
    var onItemClicked : ((BleScanner.ScanResult)->Unit)? = null
    val log = LogUtil("BLEScanLayout")
    val listRaw = HashMap<String, Pair<Long, BleScanner.ScanResult>>()
    val list = ArrayList<BleScanner.ScanResult>()
    val bleScanner = BleScanner.Builder(context).also {
        it.legacy = false //use v18 api for now
    }.build()

    private val onScannedDevices = {
            results: HashMap<String, BleScanner.ScanResult> ->
        log.d {
            val resultsStr = results.values.joinToString {
                "${it.device.name}:${it.device.address}"
            }
            /*return*/ "onBatchScanResults $resultsStr"
        }
        val botsToRemove = ArrayList<String>()
        results.forEach {
            listRaw[it.key] = Pair(System.currentTimeMillis(), it.value)
        }
        list.clear()
        listRaw.forEach {
            if (System.currentTimeMillis() - it.value.first > 10000) {
                botsToRemove.add(it.key)
            } else {
                list.add(it.value.second)
            }
        }
        botsToRemove.forEach {
            listRaw.remove(it)
        }
        scanRecyclerView.adapter?.notifyDataSetChanged()
    }

    private val onScanningChanged = {
            isScanning : Boolean ->

    }

    init {
        log.d{
            "init"
        }
        bleScanner.onDiscoveredDevices = onScannedDevices
        bleScanner.onScanningChanged = onScanningChanged
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        scanRecyclerView.layoutManager = LinearLayoutManager(context)
        scanRecyclerView.adapter = ScanViewHolder.Companion.Adapter(context, list, onItemClicked)
    }

    override fun animateContentOut(p0: Int, p1: Int) {
        log.d{
            "animateContentOut"
        }
    }

    override fun animateContentIn(p0: Int, p1: Int) {
        log.d{
            "animateContentIn"
        }
        startScan()
        val scaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 0f, 1f)
        val animatorSet = AnimatorSet().apply {
            interpolator = OvershootInterpolator()
            setDuration(500)
            playTogether(scaleX, scaleY)
        }
        animatorSet.start()
    }

    fun startScan(){
        log.d{
            "startScan"
        }
        (scanRecyclerView.adapter as ScanViewHolder.Companion.Adapter).onItemClickListener = onItemClicked
        bleScanner.setDeviceFilter(RVRProps.ControlService)
        bleScanner.scanLeDevice(true)
    }

    fun stopScan(){
        log.d{
            "stopScan"
        }
        list.clear()
        listRaw.clear()
        scanRecyclerView.adapter?.notifyDataSetChanged()
        bleScanner.scanLeDevice(false)
    }
}