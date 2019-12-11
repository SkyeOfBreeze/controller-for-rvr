package org.btelman.controller.rvr

import android.app.Application

import org.btelman.logutil.kotlin.LogLevel
import org.btelman.logutil.kotlin.LogUtil
import org.btelman.logutil.kotlin.LogUtilInstance

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        LogUtil.default = LogUtilInstance("RVRCONTROL")
        LogUtil.logLevel = if(BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR
    }
}
