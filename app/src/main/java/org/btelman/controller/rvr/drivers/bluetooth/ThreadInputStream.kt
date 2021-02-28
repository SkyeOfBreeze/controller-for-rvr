package org.btelman.controller.rvr.drivers.bluetooth

import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Read the input stream
 */
class ThreadInputStream(val inputStream: InputStream?) {
    private val data = ArrayDeque<ByteArray>()

    private val _error = AtomicBoolean(false)
    val error : Boolean
        get() {
            return _error.get()
        }

    private val thread = Thread{
        while (tryBlockingRead() != -1);
    }

    init {
        openMonitor()
    }

    private fun openMonitor(){
        thread.start()
    }

    fun close(){
        inputStream?.close()
    }

    @Synchronized
    fun next() : ByteArray{
        return data.poll()
    }

    @Synchronized
    fun hasNext() : Boolean{
        return data.peek() != null
    }

    @Synchronized
    fun put(array: ByteArray){
        data.offer(array)
    }

    private fun tryBlockingRead() : Int{
        val array = ByteArray(64)
        var bytesRead = -1
        try {
            bytesRead = inputStream?.read(array) ?: -1
            put(array)
        } catch (e: IOException) {
            _error.set(true)
        }
        return bytesRead
    }
}
