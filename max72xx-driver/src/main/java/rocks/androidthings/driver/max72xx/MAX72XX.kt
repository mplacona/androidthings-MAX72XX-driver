package rocks.androidthings.driver.max72xx

import com.google.android.things.pio.PeripheralManagerService
import com.google.android.things.pio.SpiDevice
import java.io.IOException
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or


class MAX72XX(numDevices: Int)  : AutoCloseable{
    //the opcodes for the MAX7221 and MAX7219
    private val OP_NOOP: Byte = 0
    private val OP_DIGIT0: Byte = 1
    private val OP_DIGIT1: Byte = 2
    private val OP_DIGIT2: Byte = 3
    private val OP_DIGIT3: Byte = 4
    private val OP_DIGIT4: Byte = 5
    private val OP_DIGIT5: Byte = 6
    private val OP_DIGIT6: Byte = 7
    private val OP_DIGIT7: Byte = 8
    private val OP_DECODEMODE: Byte = 9
    private val OP_INTENSITY: Byte = 10
    private val OP_SCANLIMIT: Byte = 11
    private val OP_SHUTDOWN: Byte = 12
    private val OP_DISPLAYTEST: Byte = 15

    /**
     * Segments to be switched on for characters and digits on 7-Segment Displays
     */
    private val CHAR_TABLE = byteArrayOf(126.toByte(), 48.toByte(), 109.toByte(), 121.toByte(), 51.toByte(), 91.toByte(), 95.toByte(), 112.toByte(), 127.toByte(), 123.toByte(), 119.toByte(), 31.toByte(), 13.toByte(), 61.toByte(), 79.toByte(), 71.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 128.toByte(), 1.toByte(), 128.toByte(), 0.toByte(), 126.toByte(), 48.toByte(), 109.toByte(), 121.toByte(), 51.toByte(), 91.toByte(), 95.toByte(), 112.toByte(), 127.toByte(), 123.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 119.toByte(), 31.toByte(), 13.toByte(), 61.toByte(), 79.toByte(), 71.toByte(), 0.toByte(), 55.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 14.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 103.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 8.toByte(), 0.toByte(), 119.toByte(), 31.toByte(), 13.toByte(), 61.toByte(), 79.toByte(), 71.toByte(), 0.toByte(), 55.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 14.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 103.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(), 0.toByte())

    lateinit var spiDevice: SpiDevice

    /* The array for shifting the data to the devices */
    private val spidata = ByteArray(16)

    /* We keep track of the led-status for all 8 devices in this array */
    private val status = ByteArray(64)

    /* The maximum number of devices we use */
    private var maxDevices: Int = numDevices

    @Throws(IOException::class)
    constructor(device: SpiDevice, numDevices: Int) : this(numDevices){
        spiDevice = device
        spiDevice.setMode(SpiDevice.MODE0)
        spiDevice.setFrequency(1_000_000)
        spiDevice.setBitsPerWord(8)
        spiDevice.setBitJustification(false)
        if (numDevices < 1 || numDevices > 8) {
            maxDevices = 8
        }
        for (i in 0..maxDevices - 1) {
            spiTransfer(i, OP_DISPLAYTEST, 0)
            setScanLimit(i, 7) // scanlimit: 8 LEDs
            spiTransfer(i, OP_DECODEMODE, 0) // decoding： BCD
            clearDisplay(i)
            // we go into shutdown-mode on startup
            shutdown(i, true)
        }
    }

    @Throws(IOException::class)
    constructor(spiGpio: String, numDevices: Int) : this(numDevices) {
        val pioService = PeripheralManagerService()
        spiDevice = pioService.openSpiDevice(spiGpio)
        spiDevice.setMode(SpiDevice.MODE0)
        spiDevice.setFrequency(1_000_000)
        spiDevice.setBitsPerWord(8)
        spiDevice.setBitJustification(false)
        if (numDevices < 1 || numDevices > 8) {
            maxDevices = 8
        }
        for (i in 0..maxDevices - 1) {
            spiTransfer(i, OP_DISPLAYTEST, 0)
            setScanLimit(i, 7) // scanlimit: 8 LEDs
            spiTransfer(i, OP_DECODEMODE, 0) // decoding： BCD
            clearDisplay(i)
            // we go into shutdown-mode on startup
            shutdown(i, true)
        }
    }

    override fun close() {
        spiDevice.close()
    }

    /**
     * Get the number of devices attached to this LedControl.

     * @return the number of devices on this LedControl
     */
    fun getDeviceCount(): Int {
        return maxDevices
    }


    /**
     * Set the shutdown (power saving) mode for the device

     * @param addr   the address of the display to control
     * *
     * @param status if true the device goes into power-down mode. Set to false for normal operation.
     */
    @Throws(IOException::class)
    fun shutdown(addr: Int, status: Boolean) {
        if (addr < 0 || addr >= maxDevices) {
            return
        }

        spiTransfer(addr, OP_SHUTDOWN, if (status) 0 else 1)
    }

    /**
     * Set the number of digits (or rows) to be displayed.
     *
     *
     * See datasheet for sideeffects of the scanlimit on the brightness of the display
     *

     * @param addr  the address of the display to control
     * *
     * @param limit number of digits to be displayed (1..8)
     */
    @Throws(IOException::class)
    fun setScanLimit(addr: Int, limit: Int) {
        if (addr < 0 || addr >= maxDevices) {
            return
        }

        if (limit >= 0 || limit < 8) {
            spiTransfer(addr, OP_SCANLIMIT, limit)
        }
    }

    /**
     * Set the brightness of the display

     * @param addr      the address of the display to control
     * *
     * @param intensity the brightness of the display. (0..15)
     */
    @Throws(IOException::class)
    fun setIntensity(addr: Int, intensity: Int) {
        if (addr < 0 || addr >= maxDevices) {
            return
        }

        if (intensity >= 0 || intensity < 16) {
            spiTransfer(addr, OP_INTENSITY, intensity)
        }
    }

    /**
     * Switch all Leds on the display off

     * @param addr the address of the display to control
     */
    @Throws(IOException::class)
    fun clearDisplay(addr: Int) {
        if (addr < 0 || addr >= maxDevices) {
            return
        }

        val offset = addr * 8
        for (i in 0..7) {
            status[offset + i] = 0
            spiTransfer(addr, (OP_DIGIT0 + i).toByte(), status[offset + i].toInt())
        }
    }

    /**
     * Set the status of a single Led

     * @param addr  the address of the display to control
     * *
     * @param row   the row of the Led (0..7)
     * *
     * @param col   the column of the Led (0..7)
     * *
     * @param state if true the led is switched on, if false it is switched off
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun setLed(addr: Int, row: Int, col: Int, state: Boolean) {
        if (addr < 0 || addr >= maxDevices) {
            return
        }
        if (row < 0 || row > 7 || col < 0 || col > 7) {
            return
        }

        val offset = addr * 8
        var value = (128 shr col).toByte()
        if (state) {
            status[offset + row] = (status[offset + row] or value)
        } else {
            value = value.inv()
            status[offset + row] = (status[offset + row] and value)
        }
        spiTransfer(addr, (OP_DIGIT0 + row).toByte(), status[offset + row].toInt())
    }

    /**
     * Set all 8 Led's in a row to a new state

     * @param addr  the address of the display to control
     * *
     * @param row   row which is to be set (0..7)
     * *
     * @param value each bit set to 1 will light up the corresponding Led.
     */
    @Throws(IOException::class)
    fun setRow(addr: Int, row: Int, value: Byte) {
        if (addr < 0 || addr >= maxDevices) {
            return
        }
        if (row < 0 || row > 7) {
            return
        }

        val offset = addr * 8
        status[offset + row] = value
        spiTransfer(addr, (OP_DIGIT0 + row).toByte(), status[offset + row].toInt())
    }

    /**
     * Set all 8 Led's in a column to a new state

     * @param addr  the address of the display to control
     * *
     * @param col   column which is to be set (0..7)
     * *
     * @param value each bit set to 1 will light up the corresponding Led.
     */
    @Throws(IOException::class)
    fun setColumn(addr: Int, col: Int, value: Byte) {
        var `val`: Byte

        if (addr < 0 || addr >= maxDevices) {
            return
        }
        if (col < 0 || col > 7) {
            return
        }

        for (row in 0..7) {
            `val` = (value.toInt() shr (7 - row)).toByte()
            `val` = `val` and 0x01
            setLed(addr, row, col, `val` != 0.toByte())
        }
    }

    /**
     * Display a hexadecimal digit on a 7-Segment Display

     * @param addr  the address of the display to control
     * *
     * @param digit the position of the digit on the display (0..7)
     * *
     * @param value the value to be displayed. (0x00..0x0F. 0x10 to clear digit)
     * *
     * @param dp    sets the decimal point.
     */
    @Throws(IOException::class)
    fun setDigit(addr: Int, digit: Int, value: Byte, dp: Boolean) {
        if (addr < 0 || addr >= maxDevices) {
            return
        }
        if (digit < 0 || digit > 7 || value > 16) {
            return
        }

        val offset = addr * 8
        var v = CHAR_TABLE[value.toInt()]
        if (dp) {
            v = v or 128.toByte()
        }
        status[offset + digit] = v
        spiTransfer(addr, (OP_DIGIT0 + digit).toByte(), v.toInt())
    }

    /**
     * Display a character on a 7-Segment display.
     * <pre>
     * There are only a few characters that make sense here :
     * '0','1','2','3','4','5','6','7','8','9','0',
     * 'A','b','c','d','E','F','H','L','P',
     * '.','-','_',' '
    </pre> *

     * @param addr  the address of the display to control
     * *
     * @param digit the position of the character on the display (0..7)
     * *
     * @param value the character to be displayed.
     * *
     * @param dp    sets the decimal point.
     */
    @Throws(IOException::class)
    fun setChar(addr: Int, digit: Int, value: Char, dp: Boolean) {
        if (addr < 0 || addr >= maxDevices) {
            return
        }
        if (digit < 0 || digit > 7) {
            return
        }

        val offset = addr * 8
        var index = value.toInt()
        if (index >= CHAR_TABLE.size) {
            // no defined beyond index 127, so we use the space char
            index = 32
        }
        var v = CHAR_TABLE[index]
        if (dp) {
            v = v or 128.toByte()
        }
        status[offset + digit] = v
        spiTransfer(addr, (OP_DIGIT0 + digit).toByte(), v.toInt())
    }


    /**
     * Send out a single command to the device
     */
    @Throws(IOException::class)
    private fun spiTransfer(addr: Int, opcode: Byte, data: Int) {
        val offset = addr * 2
        val maxBytes = maxDevices * 2

        for (i in 0..maxBytes - 1) {
            spidata[i] = 0.toByte()
        }

        // put our device data into the array
        spidata[maxBytes - offset - 2] = opcode
        spidata[maxBytes - offset - 1] = data.toByte()
        spiDevice.write(spidata, maxBytes)
    }
}