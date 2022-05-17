package com.tinydavid.snoocodecompass.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.tinydavid.snoocodecompass.common.Contants
import com.tinydavid.snoocodecompass.databinding.FragmentHomeBinding
import com.tinydavid.snoocodecompass.domain.use_cases.AdjustInsetUseCase
import com.tinydavid.snoocodecompass.domain.use_cases.GetAddressUseCase
import com.tinydavid.snoocodecompass.domain.use_cases.RoundOffDecimalUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val mBinding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var sensorManager: SensorManager

    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var mRoundOffDecimalUseCase: RoundOffDecimalUseCase

    @Inject
    lateinit var mGetAddressUseCase: GetAddressUseCase

    @Inject
    lateinit var adjustInsetUseCase: AdjustInsetUseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return mBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.scrollHome.requestFocus()

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mLocationRequest = LocationRequest.create().apply {
            interval = 1000 * Contants.DEFAULT_UPDATE_INTERVAL
            fastestInterval = 1000 * Contants.FAST_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (hasMagnetometer) {
            mBinding.textMagStatus.text = "Magnetometer Avail"
            mBinding.buttonCompassNavigation.isEnabled = true
        } else {
            mBinding.textMagStatus.text = "No magnetometer."
            mBinding.buttonCompassNavigation.isEnabled = false
        }

        if (hasGps) {
            Log.d(TAG, "This hardware has GPS.")
            mBinding.textGpsStatus.text = "GPS Avail"
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        } else {
            // Fall back to functionality that doesn't use location or
            // warn the user that location function isn't available.
            Log.d(TAG, "This hardware doesn't have GPS.")
            mBinding.textGpsStatus.text = "No GPS."
            mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        viewModel.location.observe(viewLifecycleOwner) { location ->
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

                mapIntent.resolveActivity(requireActivity().packageManager)?.let {
                    startActivity(mapIntent)
                }

            }
        }

        mBinding.buttonCompassNavigation.setOnClickListener {
            val action = HomeFragmentDirections.actionFragmentHomeToFragmentHealthCare()
            view.findNavController().navigate(action)
        }

        adjustInsetUseCase(mBinding.scrollHome)

        getLastLocation()


    }


    private val hasGps: Boolean
        get() =
            requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

    private val hasMagnetometer: Boolean
        get() =
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null


    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnSuccessListener {

                }

                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        viewModel.setLocation(location)

                    }
                }
            } else {
                Toast.makeText(requireContext(), "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
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
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_ID
            )
    }


    @Deprecated("Deprecated in Java")
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PERMISSION_ID = 42
        private const val INCREMEANT = 0.1


        private const val TAG = "HomeFragment"
    }
}