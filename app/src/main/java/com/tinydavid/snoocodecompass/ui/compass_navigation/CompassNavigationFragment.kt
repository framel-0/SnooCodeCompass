package com.tinydavid.snoocodecompass.ui.compass_navigation

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.tinydavid.snoocodecompass.R
import com.tinydavid.snoocodecompass.common.Contants
import com.tinydavid.snoocodecompass.databinding.FragmentCompassNavigationBinding
import com.tinydavid.snoocodecompass.domain.models.HealthCare
import com.tinydavid.snoocodecompass.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class CompassNavigationFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentCompassNavigationBinding? = null

    private val mBinding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private val mViewModel: CompassNavigationViewModel by viewModels()

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

    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private val args: CompassNavigationFragmentArgs by navArgs()

    private var accuracyReceived = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompassNavigationBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.scrollCompassNavigation.requestFocus()

        val healthCare: HealthCare = args.healthCare

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        mLocationRequest = LocationRequest.create().apply {
            interval = 1000 * Contants.DEFAULT_UPDATE_INTERVAL
            fastestInterval = 1000 * Contants.FAST_UPDATE_INTERVAL
        }

        if (hasGps) {
            Log.d(TAG, "This hardware has GPS.")
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        } else {
            Log.d(TAG, "This hardware doesn't have GPS.")
            mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }


        if (hasMagnetometer)
            mMagnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (hasAccelerometer)
            mAccelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

//        val widthDp = resources.displayMetrics.run { widthPixels / density }
//        val heightDp = resources.displayMetrics.run { heightPixels / density }


        mViewModel.location.observe(viewLifecycleOwner) { location ->
            Log.d(TAG, "Location: ${location.latitude}, ${location.longitude}")

            mViewModel.updatePointA(LatLng(location.latitude, location.longitude))

            if (healthCare != null) {
                mViewModel.updatePointB(LatLng(healthCare.latitude, healthCare.longitude))

                mViewModel.calDistance()
                mViewModel.calBearing()
            } else {
                Log.d(TAG, "Destination Unavialable")

            }

        }

        mViewModel.distance.observe(viewLifecycleOwner) { dis ->
            Log.d(TAG, "Distance btn point A and B: $dis")

            mBinding.textDistance.text = if (dis > 999) {
                val disKm = convertMeterToKilometer(dis)
                String.format(Locale.getDefault(), "%,.2f km", disKm)
            } else {
                String.format(Locale.getDefault(), "%,.2f m", dis)
            }

        }

        mViewModel.bearing.observe(viewLifecycleOwner) { brng ->
            Log.d(TAG, "Bearing btn point A and B: $brng")

            val myOptions = BitmapFactory.Options()
            myOptions.inScaled = false
            myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 // important


            val bitmap =
                BitmapFactory.decodeResource(resources, R.drawable.compass_index, myOptions)

            val workingBitmap = Bitmap.createBitmap(bitmap)
            val mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val mBitmap = getCroppedBitmap(workingBitmap)

            val canvas = Canvas(mutableBitmap)

            canvas.save()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            canvas.rotate(brng.toFloat(), rect.exactCenterX(), rect.exactCenterY())
            drawDestinationCircle(canvas, rect, 0.025f, 0.03738f)
            canvas.save()

            mBinding.imageCompassIndex.setImageBitmap(mutableBitmap)

            mBinding.textBearing.text = when (brng) {
                0.0 ->
                    String.format(Locale.getDefault(), "%.2f° N", brng)
                in 1.0..89.0 ->
                    String.format(Locale.getDefault(), "%.2f° NE", brng)
                90.0 ->
                    String.format(Locale.getDefault(), "%.2f° E", brng)
                in 91.0..179.0 ->
                    String.format(Locale.getDefault(), "%.2f° SE", brng)
                180.0 ->
                    String.format(Locale.getDefault(), "%.2f° S", brng)
                in 181.0..269.0 ->
                    String.format(Locale.getDefault(), "%.2f° SW", brng)
                270.0 ->
                    String.format(Locale.getDefault(), "%.2f° W", brng)
                in 271.0..359.0 ->
                    String.format(Locale.getDefault(), "%.2f° NW", brng)

                else -> String.format(Locale.getDefault(), "%.2f", brng)
            }


        }

        mainViewModel.magnetometerAccuracy.observe(viewLifecycleOwner) { accuracy ->

            if (accuracy <= 1) {
                val action =
                    CompassNavigationFragmentDirections.actionFragmentCompassNavigationToFragmentCompassCalibration()
                findNavController().navigate(action)
            }

        }

    }

    private val hasGps: Boolean
        get() =
            requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

    private val hasMagnetometer: Boolean
        get() =
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null

    private val hasAccelerometer: Boolean
        get() =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null

    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            mViewModel.updateLocation(lastLocation)
        }

        override fun onLocationAvailability(p0: LocationAvailability) {
            p0.isLocationAvailable
            super.onLocationAvailability(p0)
            if (p0.isLocationAvailable) {
                Log.d(TAG, "Location Availability lost")

            } else {
                Log.d(TAG, "Location Availability lost")

            }
        }
    }

    private fun convertMeterToKilometer(meter: Double): Double {
        return (meter * 0.001)
    }

    private fun getCroppedBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(
            (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
            (bitmap.width / 2).toFloat(), paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output
    }

    private fun drawDestinationCircle(
        canvas: Canvas,
        bounds: Rect,
        radiusFraction: Float,
        gapBetweenOuterCircleAndBorderFraction: Float
    ) {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.GREEN

        // X and Y coordinates of the center of the circle.
        val centerX = 0.5f * bounds.width().toFloat()
        val centerY = bounds.width() * (gapBetweenOuterCircleAndBorderFraction + radiusFraction)

        canvas.drawCircle(
            centerX,
            centerY,
            radiusFraction * bounds.width(),
            paint
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        accuracyReceived = true;
        // Do something here if sensor accuracy changes.
        if (sensor == mAccelerometerSensor) {
            Log.d(TAG, "accelerometer accuracy: $accuracy")
        }

        if (sensor == mMagnetometerSensor) {
            Log.d(TAG, "magnetometer accuracy: $accuracy")

            mainViewModel.updateMagnetometerAccuracy(accuracy)
        }

    }


    override fun onSensorChanged(event: SensorEvent) {

        if (!accuracyReceived) {
            onAccuracyChanged(event.sensor, event.accuracy);
        }

//        val azimuth: Float = event.values[0]
//        val pitch: Float = event.values[1]
//        val roll: Float = event.values[2]
//
//        val tesla: Double = sqrt(((azimuth.pow(2)) + (pitch.pow(2)) + (roll.pow(2))).toDouble())

        if (event.sensor == mAccelerometerSensor) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            isLastAccelerometerArrayCopied = true
        }

        if (event.sensor == mMagnetometerSensor) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            isLastMagnetometerArrayCopied = true
        }

        if (mViewModel.isLastAccelerometerArrayCopied && isLastMagnetometerArrayCopied && System.currentTimeMillis() - lastUpdatedTime > 250) {

            SensorManager.getRotationMatrix(
                mViewModel.rMatrix,
                mViewModel.lMatrix,
                mViewModel.lastAccelerometer,
                lastMagnetometer
            )

            SensorManager.getOrientation(rMatrix, orientation)

            val azimuthRadius: Float = orientation[0]
            var azimuthDegrees: Float = Math.toDegrees(azimuthRadius.toDouble()).toFloat()
            azimuthDegrees = (azimuthDegrees + 360) % 360

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

            mBinding.imageCompassIndex.startAnimation(rotationAnimation)

            currentDegrees = -azimuthDegrees
            lastUpdatedTime = System.currentTimeMillis()

            val x = azimuthDegrees.toInt()

            mBinding.textCompassIndex.text = when (x) {
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

            mBinding.textCompassDegree.text = "$x°"

        }

    }

    override fun onResume() {
        super.onResume()
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.getMainLooper()
        )

        mAccelerometerSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        mMagnetometerSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
//        this.unregisterReceiver(locationBroadcastReceiver)
        sensorManager.unregisterListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "Compass_Navigation"

    }
}