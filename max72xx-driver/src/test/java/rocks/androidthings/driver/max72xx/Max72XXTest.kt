package rocks.androidthings.driver.max72xx
import com.google.android.things.pio.SpiDevice
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.*

class Max72XXTest {

    private val NB_DEVICES = 1
    private val device = mock(SpiDevice::class.java)
    lateinit var ledControl : MAX72XX

    @Before
    fun setUp() {
        ledControl = MAX72XX(device, NB_DEVICES)
    }

    @Test
    @Throws(Exception::class)
    fun configures1MHzClockFrequencyWhenCreated() {
        verify(device).setFrequency(1000000)
    }

    @Test
    @Throws(Exception::class)
    fun configuresClockToTransmitOnLeadingEdgeModeWhenCreated() {
        verify(device).setMode(SpiDevice.MODE0)
    }

    @Test
    @Throws(Exception::class)
    fun configuresBusToSend8BitsPerWordComponentWhenCreated() {
        verify(device).setBitsPerWord(8)
    }

    @Test
    @Throws(Exception::class)
    fun configuresBitJustificationComponentWhenCreated() {
        verify(device).setBitJustification(false)
    }

    @Test
    @Throws(Exception::class)
    fun verifyDeviceAssignment() {
        val anotherControl = MAX72XX(device, 3)
        Assert.assertEquals(3, anotherControl.getDeviceCount())
    }

    @Test
    @Throws(Exception::class)
    fun verifyMaxDevicesUpTo8() {
        val anotherControl = MAX72XX(device, 10)
        Assert.assertEquals(8, anotherControl.getDeviceCount())
    }

    @Test
    @Throws(Exception::class)
    fun writesToSpiDeviceWhenWritingScanLimit() {
        ledControl.setScanLimit(anyInt(), anyInt())
        verify(device, atLeastOnce()).write(any(ByteArray::class.java), anyInt())
    }

    @Test
    @Throws(Exception::class)
    fun writesToSpiDeviceWhenClearingDisplay() {
        ledControl.clearDisplay(1)
        verify(device, atLeastOnce()).write(any(ByteArray::class.java), anyInt())
    }

    @Test
    @Throws(Exception::class)
    fun writesToSpiDeviceWhenSettingLED() {
        ledControl.setLed(1, 1, 1, true)
        verify(device, atLeastOnce()).write(any(ByteArray::class.java), anyInt())
    }

    @Test
    @Throws(Exception::class)
    fun writesToSpiDeviceWhenSettingRow() {
        ledControl.setRow(1, 1, 0x1)
        verify(device, atLeastOnce()).write(any(ByteArray::class.java), anyInt())
    }

    @Test
    @Throws(Exception::class)
    fun writesToSpiDeviceWhenSettingColumn() {
        ledControl.setColumn(1, 1, 0x1)
        verify(device, atLeastOnce()).write(any(ByteArray::class.java), anyInt())
    }

    @Test
    @Throws(Exception::class)
    fun writesToSpiDeviceWhenSettingDigit() {
        ledControl.setDigit(1, 1, 0x1, true)
        verify(device, atLeastOnce()).write(any(ByteArray::class.java), anyInt())
    }

    @Test
    @Throws(Exception::class)
    fun writesToSpiDeviceWhenSettingChar() {
        ledControl.setChar(1, 1, Char.MIN_SURROGATE, true)
        verify(device, atLeastOnce()).write(any(ByteArray::class.java), anyInt())
    }

    @Test
    @Throws(Exception::class)
    fun closesSpiDeviceWhenClosing() {
        ledControl.close()

        verify(device).close()
    }

    @Test
    @Throws(Exception::class)
    fun shouldFail() {
        Assert.assertTrue(false)
    }
}