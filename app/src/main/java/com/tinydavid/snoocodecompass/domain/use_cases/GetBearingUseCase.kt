package com.tinydavid.snoocodecompass.domain.use_cases

import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class GetBearingUseCase @Inject constructor() {
    operator fun invoke(pointA: LatLng, pointB: LatLng): Double {
        val lat1 = pointA.latitude
        val lng1 = pointA.longitude

        val lat2 = pointB.latitude
        val lng2 = pointB.longitude

        val lat1Radian =  Math.toRadians(lat1)
        val lat2Radian =  Math.toRadians(lat2)

        val lng1Radian =  Math.toRadians(lng1)
        val lng2Radian =  Math.toRadians(lng2)

        val y = sin(lng2Radian - lng1Radian) * cos(lat2Radian)

        val x = cos(lat1Radian) * sin(lat2Radian) -
                sin(lat1Radian) * cos(lat2Radian) * cos(lng2Radian - lng1Radian)

        val θ = atan2(y, x)

        val bearing = (θ * 180 / Math.PI + 360) % 360 // in degrees

        return bearing

    }
}