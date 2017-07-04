[![CircleCI](https://circleci.com/gh/mplacona/androidthings-MAX72XX-driver/tree/master.svg?style=shield&circle-token=a66a3161b18d4496384813831347fbbe942574b9)](https://circleci.com/gh/mplacona/androidthings-MAX72XX-driver/tree/master)

# androidthings-MAX72XX-driver
Android Things driver for the MAX7219 and MAX7221 Led drivers

This is a Kotlin port of the [LedControlAndroid Things](https://github.com/Nilhcem/ledcontrol-androidthings) 
library which is also a port of the original [Arduino version](https://github.com/digistump/DigistumpArduino).

Download
--------------
```
dependencies {
    compile 'rocks.androidthings:driver-max72xx:0.0.1'
}
```

Usage
--------------

#### Initialising
```
try {
    ledControl = MAX72XX("SPI0.0", NB_DEVICES)
    for (i in 0..ledControl.getDeviceCount() - 1) {
        ledControl.setIntensity(i, 15)
        ledControl.shutdown(i, false)
        ledControl.clearDisplay(i)
    }

} catch (e: IOException) {
    Log.e(TAG, "Error initializing LED matrix", e)
}
```

#### Turn on one pixel
```
// set row 0, column 1
ledControl.setLed(0, 0, 1, true)
// set row 0, column 2
ledControl.setLed(0, 0, 2, true)
```

### Sample Schematics (with [dot matrix](http://amzn.to/2uiDyX6))
![max72xx](https://user-images.githubusercontent.com/221627/27806299-9c378310-6031-11e7-885c-49f00e91848b.png)

