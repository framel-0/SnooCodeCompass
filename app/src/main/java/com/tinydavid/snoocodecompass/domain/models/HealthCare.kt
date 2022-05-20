package com.tinydavid.snoocodecompass.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HealthCare(
    val name: String,
    val amenity: String,
    val latitude: Double,
    val longitude: Double
) : Parcelable, Comparable<HealthCare> {
    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: HealthCare): Int {
        TODO("Not yet implemented")
    }
}

