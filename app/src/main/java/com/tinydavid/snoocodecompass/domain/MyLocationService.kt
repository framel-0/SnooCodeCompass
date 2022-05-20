package com.tinydavid.snoocodecompass.domain

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.tinydavid.snoocodecompass.common.Contants

class MyLocationService(
    private val context: Context,
    private val callback: LocationCallback,
) {

    val hasGps: Boolean
        get() = context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

    private var locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 1000 * Contants.DEFAULT_UPDATE_INTERVAL
        fastestInterval = 1000 * Contants.FAST_UPDATE_INTERVAL
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    fun startList() {
        if (hasGps) {
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        } else {
            locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        if (!::fusedLocationClient.isInitialized) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest, callback,
            Looper.getMainLooper()
        )
    }

    fun stopList() {
        fusedLocationClient.removeLocationUpdates(callback)
    }


}