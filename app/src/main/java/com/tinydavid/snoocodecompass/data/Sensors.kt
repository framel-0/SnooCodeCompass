package com.tinydavid.snoocodecompass.data

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import com.tinydavid.snoocodecompass.domain.sensors.AndroidSensor
import javax.inject.Inject

class AccelerometerSensor @Inject constructor(
    context: Context
) : AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_ACCELEROMETER,
    sensorType = Sensor.TYPE_ACCELEROMETER
)

class MagnetometerSensor @Inject constructor(
    context: Context
) : AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_COMPASS,
    sensorType = Sensor.TYPE_MAGNETIC_FIELD
)
