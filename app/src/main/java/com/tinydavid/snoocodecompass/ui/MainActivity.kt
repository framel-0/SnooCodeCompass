package com.tinydavid.snoocodecompass.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.tinydavid.snoocodecompass.databinding.ActivityMainBinding
import com.tinydavid.snoocodecompass.domain.use_cases.GetAddressUseCase
import com.tinydavid.snoocodecompass.domain.use_cases.RoundOffDecimalUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : Activity() {

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var sensorManager: SensorManager

    @Inject
    private  lateinit var viewModel: MainViewModel

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var mRoundOffDecimalUseCase: RoundOffDecimalUseCase

    @Inject
    lateinit var mGetAddressUseCase: GetAddressUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)

        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            mBinding.textMagStatus.text = "There's a magnetometer."
            // Success! There's a magnetometer.
        } else {
            // Failure! No magnetometer.
            mBinding.textMagStatus.text = "No magnetometer."
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

/*        viewModel.location.observe(this) { location ->
            if (location != null) {

                val lat = location.latitude
                val lng = location.longitude
                mBinding.textLocationLat.text = mRoundOffDecimalUseCase(lat).toString()
                mBinding.textLocationLng.text = mRoundOffDecimalUseCase(lng).toString()

                val locationAddress = mGetAddressUseCase(lat, lng)

                mBinding.textLocationAddress.text = locationAddress
            }
        }*/

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

/*
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
*/

        getLastLocation()

    }


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

    companion object {
        private const val PERMISSION_ID = 42
        private const val INCREMEANT = 0.1

    }
}