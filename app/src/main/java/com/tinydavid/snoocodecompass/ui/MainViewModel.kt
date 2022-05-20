package com.tinydavid.snoocodecompass.ui

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.tinydavid.snoocodecompass.data.MagnetometerSensor
import com.tinydavid.snoocodecompass.domain.models.HealthCare
import com.tinydavid.snoocodecompass.domain.repositories.HealthCareRepository
import com.tinydavid.snoocodecompass.ui.home.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val healthCareRepository: HealthCareRepository,
    private val magnetometerSensor: MagnetometerSensor,
) : ViewModel() {

    private val _location = MutableLiveData<Location?>()
    val location: LiveData<Location?> = _location

    private val _destination = MutableLiveData<LatLng>()
    val destination: LiveData<LatLng> = _destination

    private val _healthCares = MutableLiveData<List<HealthCare>>()
    val healthCares: LiveData<List<HealthCare>> = _healthCares

    private val _magnetometerAccuracy = MutableLiveData<Int>()
    val magnetometerAccuracy: LiveData<Int> = _magnetometerAccuracy

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    val uiState: StateFlow<HomeUiState> = _uiState

    var healthCaresLoaded: Boolean = false
    var healthCaresSorted: Boolean = false

    fun getHealthCares(location: Location?) {
        viewModelScope.launch {

            if (location != null && !healthCaresLoaded) {

                val healthCares = healthCareRepository.healthCares

//                Collections.sort(healthCares, Utils.LocationComparator(location))
                _uiState.value = HomeUiState.Success(healthCares)

                healthCaresLoaded = true
            }

        }

    }

    fun setLocation(location: Location?) {
        if (location != null)
            _location.value = location
    }

    fun setHealthCares(healthCares: List<HealthCare>) {
        _healthCares.value = healthCares
    }

    fun setDestination(location: LatLng) {
        _destination.value = location
    }

    fun updateMagnetometerAccuracy(accuracy: Int) {
        _magnetometerAccuracy.value = accuracy
    }

}