package rocks.androidthings.demo_app

import android.app.Activity
import android.os.Bundle
import android.util.Log
import rocks.androidthings.driver.max72xx.LedControl
import java.io.IOException

class MainActivity : Activity() {

    private val TAG = MainActivity::class.java.simpleName
    private val NB_DEVICES = 1

    lateinit var ledControl: LedControl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            ledControl = LedControl("SPI0.0", NB_DEVICES)
            for (i in 0..ledControl.getDeviceCount() - 1) {
                ledControl.setIntensity(i, 15)
                ledControl.shutdown(i, false)
                ledControl.clearDisplay(i)
            }

            ledControl.setLed(0, 0, 1, true)
            ledControl.setLed(0, 0, 2, true)

            ledControl.setLed(0, 1, 1, true)
            ledControl.setLed(0, 1, 2, true)
            ledControl.setLed(0, 1, 3, true)
            ledControl.setLed(0, 1, 4, true)

            ledControl.setLed(0, 2, 1, true)
            ledControl.setLed(0, 2, 2, true)
            ledControl.setLed(0, 2, 3, true)

            ledControl.setLed(0, 3, 1, true)
            ledControl.setLed(0, 3, 2, true)
            ledControl.setLed(0, 3, 3, true)
            ledControl.setLed(0, 3, 4, true)

        } catch (e: IOException) {
            Log.e(TAG, "Error initializing LED matrix", e)
        }

    }
}
