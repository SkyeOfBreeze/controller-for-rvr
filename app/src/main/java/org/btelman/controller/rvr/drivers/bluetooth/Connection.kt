package org.btelman.controller.rvr.drivers.bluetooth

import androidx.annotation.IntDef

/**
 * Bluetooth Connection Info. May be used for all connections at some point
 */
object Connection{
    @IntDef(STATE_IDLE, STATE_CONNECTING, STATE_CONNECTED, STATE_ERROR, STATE_DISCONNECTED)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class Status

    /**
     * Connection has no active connections that failed prematurely
     */
    const val STATE_IDLE = 0
    const val STATE_CONNECTING = 1
    const val STATE_CONNECTED = 2
    const val STATE_ERROR = 3
    const val STATE_DISCONNECTED = 4
}
