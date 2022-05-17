package com.tinydavid.snoocodecompass.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.LocationResult
import com.tinydavid.snoocodecompass.ui.compass_navigation.CompassNavigationViewModel
import javax.inject.Inject
import javax.inject.Singleton

class LocationBroadcastReceiver constructor(
    private val viewModel: CompassNavigationViewModel
): BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (LocationResult.hasResult(intent)) {
            val locationResult: LocationResult = LocationResult.extractResult(intent)
            val location: Location = locationResult.lastLocation
            viewModel.updateLocation(location)
        }
    }
}