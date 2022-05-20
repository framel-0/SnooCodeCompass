package com.tinydavid.snoocodecompass.domain.sensors

abstract class MeasurableSensor(
    protected val sensorType: Int
) {

    protected var onSensorValueChanged: ((List<Float>) -> Unit)? = null
    protected var onSensorAccuracyChanged: ((Int) -> Unit)? = null
    abstract val doesSensorExist: Boolean

    abstract fun startListening()
    abstract fun stopListening()

    fun setOnSensorValueChangedListener(listener: (List<Float>) -> Unit) {
        onSensorValueChanged = listener
    }

    fun setOnSensorAccuracyChangedListener(listener: (Int) -> Unit) {
        onSensorAccuracyChanged = listener
    }

}