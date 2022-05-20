package com.tinydavid.snoocodecompass.ui.compass_calibration

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.tinydavid.snoocodecompass.R
import com.tinydavid.snoocodecompass.databinding.FragmentCompassCalibrationBinding
import com.tinydavid.snoocodecompass.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompassCalibrationFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentCompassCalibrationBinding? = null

    private val mBinding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var sensorManager: SensorManager
    private var mMagnetometerSensor: Sensor? = null
    private var mAccelerometerSensor: Sensor? = null

    private var accuracyReceived = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompassCalibrationBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mMagnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        mAccelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        Glide.with(this).load(R.drawable.infinity).into(mBinding.imageView)

        mainViewModel.magnetometerAccuracy.observe(viewLifecycleOwner) { accuracy ->

            if (accuracy >= 2)
                findNavController().popBackStack()

        }

    }




    override fun onSensorChanged(event: SensorEvent) {

        if (!accuracyReceived) {
            onAccuracyChanged(event.sensor, event.accuracy);
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        accuracyReceived = true;
        if (sensor == mAccelerometerSensor) {
            Log.d(TAG, "accelerometer accuracy: $accuracy")
        }

        if (sensor == mMagnetometerSensor) {
            Log.d(TAG, "magnetometer accuracy: $accuracy")

            mainViewModel.updateMagnetometerAccuracy(accuracy)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "Compass_Calibration"

    }
}