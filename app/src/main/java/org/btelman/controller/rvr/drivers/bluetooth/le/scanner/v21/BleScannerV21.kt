package org.btelman.controller.rvr.drivers.bluetooth.le.scanner.v21

import android.annotation.TargetApi
import android.content.Context
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import org.btelman.controller.rvr.drivers.bluetooth.le.scanner.BleScanner
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Brendon on 12/24/2019.
 */
@RequiresApi(21)
class BleScannerV21(context: Context) : BleScanner(context) {
    override fun startScan(deviceFilter: ArrayList<ParcelUuid>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stopScan() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}