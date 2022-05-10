package com.tinydavid.snoocodecompass.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.tinydavid.snoocodecompass.databinding.ActivityMainBinding
import com.tinydavid.snoocodecompass.domain.use_cases.GetAddressUseCase
import com.tinydavid.snoocodecompass.domain.use_cases.RoundOffDecimalUseCase
import com.tinydavid.snoocodecompass.ui.compass_navigation.CompassNavigationActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var sensorManager: SensorManager

    private val viewModel: MainViewModel by viewModels()

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var mRoundOffDecimalUseCase: RoundOffDecimalUseCase

    @Inject
    lateinit var mGetAddressUseCase: GetAddressUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.scrollMain.requestFocus()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

//        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

         if (hasMagnetometer) {
             mBinding.textMagStatus.text ="There's a magnetometer."
             mBinding.buttonCompassNavigation.isEnabled = true
        } else {
             mBinding.textMagStatus.text = "No magnetometer."
             mBinding.buttonCompassNavigation.isEnabled = false
        }

        mBinding.textGpsStatus.text = if (!hasGps) {
            Log.d(TAG, "This hardware doesn't have GPS.")
            "There's a GPS."
            // Fall back to functionality that doesn't use location or
            // warn the user that location function isn't available.
        } else {
            "No GPS."
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel.location.observe(this) { location ->
            if (location != null) {

                val lat = location.latitude
                val lng = location.longitude
                mBinding.textLocationLat.text = mRoundOffDecimalUseCase(lat).toString()
                mBinding.textLocationLng.text = mRoundOffDecimalUseCase(lng).toString()

                val locationAddress = mGetAddressUseCase(lat, lng)

                mBinding.textLocationAddress.text = locationAddress
            }
        }

        mBinding.buttonLocation.setOnClickListener { getLastLocation() }
        mBinding.buttonDestination.setOnClickListener {
            val location = viewModel.location.value
            if (location != null) {

                val lat = location.latitude
                val lng = location.longitude

                val latDes = mRoundOffDecimalUseCase((lat.plus(INCREMEANT)))
                val lngDes = mRoundOffDecimalUseCase((lng.plus(INCREMEANT)))

                viewModel.setDestination(LatLng(latDes, lngDes))

                mBinding.textDestinationLat.text =
                    latDes.toString()
                mBinding.textDestinationLng.text =
                    lngDes.toString()

                val destinationAddress = mGetAddressUseCase(latDes, lngDes)

                mBinding.textDestinationAddress.text = destinationAddress
            }
        }


        mBinding.buttonMap.setOnClickListener {
            val location = viewModel.location.value
            val destination = viewModel.destination.value
            if (location != null && destination != null) {
                val lat = location.latitude
                val lng = location.longitude

                val latDes = destination.latitude
                val lngDes = destination.longitude

                val uriStr1 = "google.streetview:cbll=$lat,$lng"
                val uriStr2 = "geo:$lat,$lng?z=18"
                val uriStr = "google.navigation:q=$latDes,$lngDes"
                val gmnIntentUri = Uri.parse(uriStr)

                val mapIntent = Intent(Intent.ACTION_VIEW, gmnIntentUri)

                mapIntent.setPackage("com.google.android.apps.maps")

                mapIntent.resolveActivity(packageManager)?.let {
                    startActivity(mapIntent)
                }

            }
        }

        mBinding.buttonCompassNavigation.setOnClickListener {
            startActivity(Intent(this, CompassNavigationActivity::class.java))
        }

        adjustInset()

        getLastLocation()

    }

    private val hasGps: Boolean
        get() =
            packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

    private val hasMagnetometer: Boolean
        get() =
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null

    private val hasAccelerometer: Boolean
        get() =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null


    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        viewModel.setLocation(location)

                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
//        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        locationRequest.interval = 0
//        locationRequest.fastestInterval = 0
//        locationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            locationRequest, mLocationCallback,
            Looper.getMainLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            viewModel.setLocation(lastLocation)
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

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    private fun adjustInset() {

        if (applicationContext.resources.configuration.isScreenRound) {

            val inset = (FACTOR * Resources.getSystem().displayMetrics.widthPixels).toInt()

            mBinding.layoutContent.setPadding(inset, inset, inset, inset)

        }

    }

    companion object {
        private const val PERMISSION_ID = 42
        private const val INCREMEANT = 0.1
        private const val TAG = "MainActivity"

        private const val FACTOR = 0.146467f // c = a * sqrt(2)

    }
}