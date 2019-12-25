package org.btelman.controller.rvr.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.btelman.controller.rvr.drivers.bluetooth.le.scanner.v21.BleScannerV21
import org.btelman.controller.rvr.R
import org.btelman.controller.rvr.drivers.bluetooth.le.scanner.BleScanner
import org.btelman.logutil.kotlin.LogUtil

/**
 * ViewHolder for holding info of bluetooth scans
 */
class ScanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val log = LogUtil("ScanViewHolder")
    val scanAddress = itemView.findViewById<TextView>(R.id.scanAddress)
    val scanName = itemView.findViewById<TextView>(R.id.scanName)
    val scanIndicator = itemView.findViewById<View>(R.id.scanIndicator)
    var scanResult : BleScanner.ScanResult? = null
    var onClickListener : ((BleScanner.ScanResult)->Unit)? = null

    fun bind(result : BleScanner.ScanResult){
        log.v{
            "bind ${result.device.name} : ${result.rssi} dbm"
        }
        scanResult = result
        scanName.text = result.device.name ?: "???"
        scanAddress.text = "${result.rssi} dbm"
        scanIndicator.visibility = View.VISIBLE
        itemView.setOnClickListener {
            scanResult?.let { scanResultSafe ->
                onClickListener?.invoke(scanResultSafe)
            }
        }
    }

    companion object{
        class Adapter(val context : Context, val list : ArrayList<BleScanner.ScanResult>, var onItemClickListener : ((BleScanner.ScanResult)->Unit)? = null) : RecyclerView.Adapter<ScanViewHolder>() {

            val log = LogUtil("ScanViewHolder.Adapter")
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
                log.v{
                    "onCreateViewHolder"
                }
                val view = LayoutInflater.from(context).inflate(R.layout.ble_view_holder_searchable, parent, false)
                return ScanViewHolder(view)
            }

            override fun getItemCount(): Int {
                log.v{
                    "getItemCount = ${list.size}"
                }
                return list.size
            }

            override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
                log.v{
                    "onBindViewHolder $position ${list[position].device.name}"
                }
                holder.onClickListener = onItemClickListener
                holder.bind(list[position])
            }
        }
    }
}