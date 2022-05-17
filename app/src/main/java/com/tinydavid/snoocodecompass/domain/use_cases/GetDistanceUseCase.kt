package com.tinydavid.snoocodecompass.domain.use_cases

import com.google.android.gms.maps.model.LatLng
import com.tinydavid.snoocodecompass.common.Contants
import javax.inject.Inject

class GetDistanceUseCase @Inject constructor(private val calRadian: CalRadianUseCase) {

    operator fun invoke(pointA: LatLng, pointB: LatLng): Double {
        val lat1 = pointA.latitude
        val lng1 = pointA.longitude

        val lat2 = pointB.latitude
        val lng2 = pointB.longitude

        val lat1Radian = calRadian(lat1)
        val lat2Radian = calRadian(lat2)

        val latDiffRadian = calRadian(lat2 - lat1)
        val lngDiffRadian = calRadian(lng2 - lng1)

        val a = Math.sin(latDiffRadian / 2) * Math.sin(latDiffRadian / 2) +
                Math.cos(lat1Radian) * Math.cos(lat2Radian) *
                Math.sin(lngDiffRadian / 2) * Math.sin(lngDiffRadian / 2);

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        val distance = Contants.EARTH_RADIUS * c; // in metres

        return distance

    }
}