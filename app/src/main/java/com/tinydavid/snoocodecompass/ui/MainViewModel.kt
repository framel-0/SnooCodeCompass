package com.tinydavid.snoocodecompass.ui

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.tinydavid.snoocodecompass.domain.models.HealthCare
import com.tinydavid.snoocodecompass.domain.repositories.HealthCareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    healthCareRepository: HealthCareRepository
) : ViewModel() {

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private val _destination = MutableLiveData<LatLng>()
    val destination: LiveData<LatLng> = _destination

    private val _healthCares = MutableLiveData<List<HealthCare>>()
    val healthCares: LiveData<List<HealthCare>> = _healthCares

    private val _magnetometerAccuracy = MutableLiveData<Int>()
    val magnetometerAccuracy: LiveData<Int> = _magnetometerAccuracy

    init {

        viewModelScope.launch {
            _healthCares.value = healthCareRepository.healthCares
        }
    }

    fun setLocation(location: Location?) {
        if (location != null)
            _location.value = location
    }

    fun setDestination(location: LatLng) {
        _destination.value = location
    }

    fun updateMagnetometerAccuracy(accuracy: Int) {
        _magnetometerAccuracy.value = accuracy
    }

}