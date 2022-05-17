package com.tinydavid.snoocodecompass.ui.home

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private val _destination = MutableLiveData<LatLng>()
    val destination: LiveData<LatLng> = _destination

    fun setLocation(location: Location?) {
        if (location != null)
            _location.value = location
    }

    fun setDestination(location: LatLng) {
        _destination.value = location
    }

}