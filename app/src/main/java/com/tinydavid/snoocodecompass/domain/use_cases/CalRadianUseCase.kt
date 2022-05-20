package com.tinydavid.snoocodecompass.domain.use_cases

import javax.inject.Inject

class CalRadianUseCase @Inject constructor() {

    operator fun invoke(digit: Double): Double {
        val radian = digit * Math.PI / 180
        return radian
    }
}