package com.tinydavid.snoocodecompass.domain.use_cases

import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

class GetPointUseCase @Inject constructor(private val calRadian: CalRadianUseCase) {
    operator fun invoke(pointA: LatLng, pointB: LatLng): Pair<Double, Double> {
        val lat1 = pointA.latitude
        val lng1 = pointA.longitude

        val lat2 = pointB.latitude
        val lng2 = pointB.longitude

        val lat1Radian = calRadian(lat1)
        val lat2Radian = calRadian(lat2)

        val lng1Radian = calRadian(lng1)
        val lng2Radian = calRadian(lng2)

        val y = sin(lng2Radian - lng1Radian) * cos(lat2Radian)

        val x = cos(lat1Radian) * sin(lat2Radian) -
                sin(lat1Radian) * cos(lat2Radian) * cos(lng2Radian - lng1Radian)

        return Pair(x, y)

    }
}