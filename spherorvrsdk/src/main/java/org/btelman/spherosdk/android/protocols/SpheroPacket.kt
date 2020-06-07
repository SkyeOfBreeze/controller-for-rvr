package org.btelman.spherosdk.android.protocols

import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.xor

/**
 * Created by Brendon on 2/15/2020.
 */
open class SpheroPacketBuilder {
    var flags : Byte = 0x00
    var tid : Byte? = null
    var sid : Byte? = null
    var did : Byte = 0x00
    var cid : Byte = 0x00
    var seq : Byte = 0x00
    var err : Byte? = null
    var data : ByteArray? = null
    var chk : Byte? = null

    /**
     * Get the current data as a ByteArray to send. It is best to call this again to up the
     * request counter before sending
     */
    open fun get(requireResponse : Boolean, resetInactivity : Boolean = true) : ByteArray{
        currentResponseIndex++ //increase the response
        seq = currentResponseIndex
        refreshFlags(requireResponse, resetInactivity)
        val size = getSize()
        return buildArray(size)
    }

    protected open fun refreshFlags(requireResponse : Boolean, resetInactivity : Boolean){
        flags = 0x00.toByte().minusFlag(FLAG_RESPONSE)
        flags = if(requireResponse)
            flags.withFlag(FLAG_REQUEST_RESPONSE)
        else
            flags.withFlag(FLAG_REQUEST_ONLY_ERROR_RESPONSE)

        if(resetInactivity)
            flags = flags.withFlag(FLAG_ACTIVITY_COUNTER_RESET)

        if(tid != null){
            flags = flags.withFlag(FLAG_TARGET_ID_PRESENT)
        }
        if(sid != null){
            flags = flags.withFlag(FLAG_SOURCE_ID_PRESENT)
        }
    }

    open fun getSize() : Int{
        //start of with the known minimum size
        var size = 5 //flags, did, cid, seq, chk
        if(err != null){
            size++ //add 1
        }
        if(tid != null){
            size++ //add 1
        }
        if(sid != null){
            size++ //add 1
        }
        data?.let {//add data to it
            size += it.size
        }
        return size
    }

    protected open fun buildArray(size : Int): ByteArray {
        return ByteBuffer.allocate(size).also { buffer ->
            buffer.put(flags)
                .put(did)
                .put(cid)
                .put(seq)
            tid?.let {
                buffer.put(it)
            }
            sid?.let {
                buffer.put(it)
            }
            err?.let {
                buffer.put(it)
            }
            data?.let {
                buffer.put(it)
            }
            chk = checksum(buffer.array(), buffer.position()).also {
                buffer.put(it)
            }
        }.array()
    }

    protected open fun checksum(driveCommand: ByteArray, checksumIndex: Int): Byte {
        var sum: Byte = 0
        //skip index 0
        for (i in 1 until checksumIndex) {
            sum = (sum + driveCommand[i]).toByte()
        }
        //sum += 1;
        return (sum and 0xFF.toByte() xor 0xFF.toByte())
    }

    companion object{

        //error codes

        /**
         * Command executed successfully
         */
        val ERROR_SUCCESS = 0x00
        /**
         * Device ID is invalid (or is invisible with current permissions)
         */
        val ERROR_BAD_DEVICE_ID = 0x01
        /**
         * Command ID is invalid (or is invisible with current permissions)
         */
        val ERROR_BAD_COMMAND_ID = 0x02
        /**
         * Command is not yet implemented or has a null handler
         */
        val ERROR_NOT_YET_IMPLEMENTED = 0x03
        /**
         * Command cannot be executed in the current state or mode
         */
        val ERROR_COMMAND_RESTRICTED = 0x04
        /**
         * Payload data length is invalid
         */
        val ERROR_BAD_DATA_LENGTH = 0x05
        /**
         * Command failed to execute for a command-specific reason
         */
        val ERROR_COMMAND_FAILED = 0x06
        /**
         * At least one data parameter is invalid
         */
        val ERROR_BAD_PARAMETER_VALUE = 0x07
        /**
         * The operation is already in progress or the module is busy
         */
        val ERROR_BUSY = 0x08
        /**
         * Target does not exist
         */
        val ERROR_BAD_TARGET_ID = 0x09
        /**
         * Target exists but is unavailable (e.g., it is asleep or disconnected)
         */
        val ERROR_TARGET_UNAVAILABLE = 0x0A

        //packet start and end
        /**
         * Escape
         */
        private val ESC : Byte = 0xAB.toByte()
        /**
         * Start of Packet
         */
        private val SOP : Byte = 0x8D.toByte()
        /**
         * End of Packet
         */
        private val EOP : Byte = 0xD8.toByte()

        //escape values
        //used as `ESC, VALUE`
        /**
         * Escaped Escape
         */
        private val ESC_ESC : Byte = 0x23.toByte()
        /**
         * Escaped Start of Packet
         */
        private val ESC_SOP : Byte = 0x05.toByte()
        /**
         * Escaped End of Packet
         */
        private val ESC_EOP : Byte = 0x50.toByte()

        //flags
        /**
         * Packet is a response
         */
        val FLAG_RESPONSE : Byte = 0x00
        /**
         * Request Response
         */
        val FLAG_REQUEST_RESPONSE : Byte = 0x01

        /**
         * Request Only Error Response. If set, we only receive a response if there is an error
         */
        val FLAG_REQUEST_ONLY_ERROR_RESPONSE : Byte = 0x02

        /**
         * Resets the inactivity timer if set
         */
        val FLAG_ACTIVITY_COUNTER_RESET : Byte = 0x03

        /**
         * Packet has Target ID byte in header if set
         */
        val FLAG_TARGET_ID_PRESENT : Byte = 0x04

        /**
         * Packet has Source ID byte in header if set
         */
        val FLAG_SOURCE_ID_PRESENT : Byte = 0x05

        /**
         * This flag has no use right now
         */
        val FLAG_UNUSED_6 : Byte = 0x06

        /**
         * If set, the next byte is also used for flags
         */
        val FLAG_EXTENDED_FLAGS : Byte = 0x07

        var currentResponseIndex : Byte = 0x00
    }
}