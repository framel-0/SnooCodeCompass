package com.tinydavid.snoocodecompass.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HealthCare(
    val name: String,
    val amenity: String,
    val latitude: Double,
    val longitude: Double
) : Parcelable

