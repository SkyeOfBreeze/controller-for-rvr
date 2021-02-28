package org.btelman.controller.rvr.utils

import android.content.Context
import android.content.SharedPreferences

class PrefsManager (
    context: Context,
    private val sharedPrefs : SharedPreferences = context.getSharedPreferences("RVR", Context.MODE_PRIVATE)
){

    /**
     * Handle input timeout
     */
    val timeoutMs: Long = 1000 //a little long, but will prevent runaway robots
    var keepScreenAwake : Boolean
        get() {
            return sharedPrefs.getBoolean("keepScreenAwake", false)
        }
        set(value) {
            sharedPrefs.edit().putBoolean("keepScreenAwake", value).apply()
        }

    private var _maxSpeed : Float? = null
    var maxSpeed : Float
        get() {
            _maxSpeed?: run {
                _maxSpeed = sharedPrefs.getFloat("maxSpeed", .7f)
            }
            return _maxSpeed!!
        }
        set(value) {
            _maxSpeed = value
            sharedPrefs.edit().putFloat("maxSpeed", value).apply()
        }

    private var _maxTurnSpeed : Float? = null
    var maxTurnSpeed : Float
        get() {
            _maxTurnSpeed?: run {
                _maxTurnSpeed = sharedPrefs.getFloat("maxTurnSpeed", .7f)
            }
            return _maxTurnSpeed!!
        }
        set(value) {
            _maxTurnSpeed = value
            sharedPrefs.edit().putFloat("maxTurnSpeed", value).apply()
        }
}