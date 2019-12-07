package org.btelman.controller.rvr.views

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.ContentViewCallback
import kotlinx.android.synthetic.main.ble_scan_layout.view.*
import no.nordicsemi.android.support.v18.scanner.*
import org.btelman.controller.rvr.utils.RVRProps
import org.btelman.logutil.kotlin.LogUtil

/**
 * BLE Layout that handles scanning for bluetooth devices
 */
class BLEScanLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) , ContentViewCallback {

    var onItemClicked : ((ScanResult)->Unit)? = null
    val log = LogUtil("BLEScanLayout")
    val listRaw = HashMap<String, Pair<Long, ScanResult>>()
    val list = ArrayList<ScanResult>()

    val scanner = BluetoothLeScannerCompat.getScanner()
    val settings = ScanSettings.Builder()
        .setLegacy(false)
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setReportDelay(1000)
        .setUseHardwareBatchingIfSupported(true)
        .build()
    val filters = ArrayList<ScanFilter>().also {
        it.add(ScanFilter.Builder().setServiceUuid(RVRProps.ControlService).build())
    }

    val scanCallback = object : ScanCallback(){
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            log.d {
                "onScanFailed $errorCode"
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            log.d {
                "scanResult ${result.device.name}"
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            log.d {
                var resultsStr = results.joinToString {
                    "${it.scanRecord?.deviceName}:${it.device.address}"
                }
                /*return*/ "onBatchScanResults $resultsStr"
            }
            val botsToRemove = ArrayList<String>()
            results.forEach {
                listRaw[it.device.address] = Pair(System.currentTimeMillis(), it)
            }
            list.clear()
            listRaw.forEach {
                if(System.currentTimeMillis() - it.value.first > 10000){
                    botsToRemove.add(it.key)
                }
                else{
                    list.add(it.value.second)
                }
            }
            botsToRemove.forEach {
                listRaw.remove(it)
            }
            scanRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    init {
        log.d{
            "init"
        }
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
        scanner.startScan(filters, settings, scanCallback)
    }

    fun stopScan(){
        log.d{
            "stopScan"
        }
        list.clear()
        listRaw.clear()
        scanRecyclerView.adapter?.notifyDataSetChanged()
        scanner.stopScan(scanCallback)
    }
}