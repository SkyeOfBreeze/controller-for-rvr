package org.btelman.controller.rvr.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import no.nordicsemi.android.support.v18.scanner.ScanResult
import org.btelman.controller.rvr.R
import org.btelman.logutil.kotlin.LogUtil

/**
 * ViewHolder for holding info of bluetooth scans
 */
class ScanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val log = LogUtil("ScanViewHolder")
    val scanAddress = itemView.findViewById<TextView>(R.id.scanAddress)
    val scanName = itemView.findViewById<TextView>(R.id.scanName)
    val scanIndicator = itemView.findViewById<View>(R.id.scanIndicator)

    fun bind(result : ScanResult){
        log.v{
            "bind ${result.device.name} : ${result.rssi} dbm : isConnectable = ${result.isConnectable}"
        }
        scanName.text = result.scanRecord?.deviceName ?: "???"
        scanAddress.text = "${result.rssi} dbm"
        scanIndicator.visibility = if(result.isConnectable) View.VISIBLE else View.INVISIBLE
    }

    companion object{
        class Adapter(val context : Context, val list : ArrayList<ScanResult>) : RecyclerView.Adapter<ScanViewHolder>() {
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
                holder.bind(list[position])
            }
        }
    }
}