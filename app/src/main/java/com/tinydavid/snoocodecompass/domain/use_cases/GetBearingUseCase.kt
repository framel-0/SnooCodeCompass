package com.tinydavid.snoocodecompass.domain.use_cases

import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class GetBearingUseCase @Inject constructor(private val calRadian: CalRadianUseCase) {
    operator fun invoke(pointA: LatLng, pointB: LatLng): Double {
        val lat1 = pointA.latitude
        val lng1 = pointA.longitude

        val lat2 = pointB.latitude
        val lng2 = pointB.longitude

        val lat1Radian = calRadian(lat1)
        val lat2Radian = calRadian(lat2)

        val lng1Radian = calRadian(lng1)
        val lng2Radian = calRadian(lng2)

        val y = Math.sin(lng2Radian - lng1Radian) * Math.cos(lat2Radian);

        val x = Math.cos(lat1Radian) * Math.sin(lat2Radian) -
                Math.sin(lat1Radian) * Math.cos(lat2Radian) * Math.cos(lng2Radian - lng1Radian);

        val θ = Math.atan2(y, x);

        val bearing = (θ * 180 / Math.PI + 360) % 360; // in degrees

        return bearing

    }
}