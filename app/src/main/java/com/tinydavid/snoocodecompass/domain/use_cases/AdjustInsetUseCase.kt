package com.tinydavid.snoocodecompass.domain.use_cases

import android.content.Context
import android.content.res.Resources
import android.view.View
import com.tinydavid.snoocodecompass.common.Contants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AdjustInsetUseCase @Inject constructor(@ApplicationContext private val appContext: Context) {

    operator fun invoke(view: View) {
        if (appContext.resources.configuration.isScreenRound) {

            val inset = (Contants.FACTOR * Resources.getSystem().displayMetrics.widthPixels).toInt()

            view.setPadding(inset, inset, inset, inset)

        }
    }
}