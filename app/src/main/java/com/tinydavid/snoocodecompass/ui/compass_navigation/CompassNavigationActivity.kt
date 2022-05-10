package com.tinydavid.snoocodecompass.ui.compass_navigation

import android.content.Context
import android.content.res.Resources
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.fragment.app.FragmentActivity
import com.tinydavid.snoocodecompass.databinding.ActivityCompassNavigationBinding
import kotlin.math.pow
import kotlin.math.sqrt

class CompassNavigationActivity : FragmentActivity(), SensorEventListener {

    private lateinit var binding: ActivityCompassNavigationBinding

    private lateinit var sensorManager: SensorManager
    private var mMagnetometerSensor: Sensor? = null
    private var mAccelerometerSensor: Sensor? = null

    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private val rMatrix = FloatArray(9)
    private val lMatrix = FloatArray(9)
    private val orientation = FloatArray(3)


    private var isLastAccelerometerArrayCopied = false
    private var isLastMagnetometerArrayCopied = false

    private var lastUpdatedTime: Long = 0
    private var currentDegrees: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompassNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (hasMagnetometer)
            mMagnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (hasAccelerometer)
            mAccelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        adjustInset()
    }

    private val hasMagnetometer: Boolean
        get() =
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null

    private val hasAccelerometer: Boolean
        get() =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }


    override fun onSensorChanged(event: SensorEvent) {
        val azimuth: Float = event.values[0]
        val pitch: Float = event.values[1]
        val roll: Float = event.values[2]

        val tesla: Double = sqrt(((azimuth.pow(2)) + (pitch.pow(2)) + (roll.pow(2))).toDouble())

        if (event.sensor == mAccelerometerSensor) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            isLastAccelerometerArrayCopied = true
        }

        if (event.sensor == mMagnetometerSensor) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            isLastMagnetometerArrayCopied = true
        }

        if (isLastAccelerometerArrayCopied && isLastMagnetometerArrayCopied && System.currentTimeMillis() - lastUpdatedTime > 250) {

            val matrix = SensorManager.getRotationMatrix(
                rMatrix,
                lMatrix,
                lastAccelerometer,
                lastMagnetometer
            )

            SensorManager.getOrientation(rMatrix, orientation)

            val azimuthRadius: Float = orientation[0]
            val azimuthDegrees: Float = Math.toDegrees(azimuthRadius.toDouble()).toFloat()

            val rotationAnimation = RotateAnimation(
                currentDegrees,
                -azimuthDegrees,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )

            rotationAnimation.duration = 250
            rotationAnimation.fillAfter = true

            binding.imageCompassIndex.startAnimation(rotationAnimation)

            currentDegrees = -azimuthDegrees
            lastUpdatedTime = System.currentTimeMillis()

            val x = azimuthDegrees.toInt()

            binding.textCompassIndex.text = when (x) {
                0 -> "N"
                in 1..89 -> "NE"
                90 -> "E"
                in 91..179 -> "SE"
                180 -> "S"
                in 181..269 -> "SW"
                270 -> "W"
                in 271..359 -> "NW"
                else -> ""
            }

            binding.textCompassDegree.text = "$x"

        }

    }

    override fun onResume() {
        super.onResume()
        mAccelerometerSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        mMagnetometerSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun adjustInset() {

        if (applicationContext.resources.configuration.isScreenRound) {

            val inset = (FACTOR * Resources.getSystem().displayMetrics.widthPixels).toInt()

//            layout_content.setPadding(inset, inset, inset, inset)

        }

    }


    companion object {

        private const val FACTOR = 0.146467f // c = a * sqrt(2)

    }

}