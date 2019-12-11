package org.btelman.controller.rvr.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

internal class RemoReceiver(private val context: Context, private val listener: RemoListener) :
    BroadcastReceiver() {

    fun register() {
        val filter = IntentFilter(REMO_SDK_CONTROL_SOCKET)
        context.registerReceiver(this, filter)
        listener.onCommand("stop")
    }

    fun unregister() {
        context.unregisterReceiver(this)
        listener.onCommand("stop")
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (REMO_SDK_CONTROL_SOCKET == intent.action) {
            listener.onCommand(intent.getStringExtra("command"))
        }
    }

    interface RemoListener {
        fun onCommand(command: String)
    }

    companion object {

        val REMO_SDK_CONTROL_SOCKET = "tv.remo.android.controller.sdk.socket.controls"
    }
}