package com.tinydavid.snoocodecompass.ui.compass_navigation

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.tinydavid.snoocodecompass.data.AccelerometerSensor
import com.tinydavid.snoocodecompass.domain.use_cases.GetBearingUseCase
import com.tinydavid.snoocodecompass.domain.use_cases.GetDistanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CompassNavigationViewModel @Inject constructor(
    private var accelerometerSensor: AccelerometerSensor,
    val distanceUseCase: GetDistanceUseCase,
    val bearingUseCase: GetBearingUseCase
) : ViewModel() {

    val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)

    val rMatrix = FloatArray(9)
    val lMatrix = FloatArray(9)

    private val orientation = FloatArray(3)


    var isLastAccelerometerArrayCopied = false
    private var isLastMagnetometerArrayCopied = false

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private val _pointA = MutableLiveData<LatLng>(LatLng(-0.186964, 5.603716))
    val pointA: LiveData<LatLng> = _pointA

    private var _pointB = MutableLiveData<LatLng>(LatLng(-0.086964, 5.703716))
    val pointB: LiveData<LatLng> = _pointB

    private val _distance = MutableLiveData<Double>()
    val distance: LiveData<Double> = _distance

    private val _bearing = MutableLiveData<Double>()
    val bearing: LiveData<Double> = _bearing


    init {
        accelerometerSensor.startListening()
        accelerometerSensor.setOnSensorValueChangedListener { values ->
            System.arraycopy(values, 0, lastAccelerometer, 0, values.size)
            isLastAccelerometerArrayCopied = true
        }
    }

    fun updateLocation(location: Location?) {
        if (location != null)
            _location.value = location
    }

    fun updatePointA(point: LatLng) {
        _pointA.value = point
    }

    fun updatePointB(point: LatLng) {
        _pointB.value = point
    }

    fun calDistance() {
        val pointA = pointA.value!!
        val pointB = pointB.value!!
        val dis = distanceUseCase(pointA, pointB)
        _distance.value = dis
    }

    fun calBearing() {
        val pointA = pointA.value!!
        val pointB = pointB.value!!
        val brng = bearingUseCase(pointA, pointB)
        _bearing.value = brng
    }

}