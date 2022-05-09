package com.tinydavid.snoocodecompass.domain.use_cases

import java.math.RoundingMode
import java.text.DecimalFormat
import javax.inject.Inject

class RoundOffDecimalUseCase @Inject constructor(){
    operator fun invoke(number: Double): Double {
        val df = DecimalFormat("#.######")
        df.roundingMode = RoundingMode.FLOOR
        return df.format(number).toDouble()

    }
}