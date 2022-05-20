package com.tinydavid.snoocodecompass.domain.use_cases

import com.google.android.gms.maps.model.LatLng
import com.tinydavid.snoocodecompass.common.Contants
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GetDistanceUseCase @Inject constructor() {

    operator fun invoke(pointA: LatLng, pointB: LatLng): Double {
        val lat1 = pointA.latitude
        val lng1 = pointA.longitude

        val lat2 = pointB.latitude
        val lng2 = pointB.longitude

        val lat1Radian = Math.toRadians(lat1)
        val lat2Radian =  Math.toRadians(lat2)

        val latDiffRadian =  Math.toRadians(lat2 - lat1)
        val lngDiffRadian =  Math.toRadians(lng2 - lng1)

        val a = sin(latDiffRadian / 2) * sin(latDiffRadian / 2) +
                cos(lat1Radian) * cos(lat2Radian) *
                sin(lngDiffRadian / 2) * sin(lngDiffRadian / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val distance = Contants.EARTH_RADIUS * c // in metres

        return distance

    }
}