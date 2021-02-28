package org.btelman.controller.rvr.drivers.bluetooth.le

import android.bluetooth.BluetoothGattCharacteristic
import org.btelman.controller.rvr.drivers.bluetooth.BluetoothInterface
import java.util.*

/**
 * Created by Brendon on 12/23/2019.
 */
interface BluetoothGattInterface : BluetoothInterface {
    fun writeBytes(service : UUID, characteristic: UUID, bytes : ByteArray)
    fun subscribe(service : UUID, characteristic: UUID, callback : (BluetoothGattCharacteristic)->Unit)

}