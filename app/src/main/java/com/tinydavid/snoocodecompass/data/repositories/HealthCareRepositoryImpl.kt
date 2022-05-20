package com.tinydavid.snoocodecompass.data.repositories

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tinydavid.snoocodecompass.common.Utils
import com.tinydavid.snoocodecompass.domain.models.HealthCare
import com.tinydavid.snoocodecompass.domain.repositories.HealthCareRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.reflect.Type
import javax.inject.Inject


class HealthCareRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context
) : HealthCareRepository {
    override val healthCares: List<HealthCare>
        get() {
            val jsonFileString: String? =
                Utils.getJsonFromAssets(context, "healthcare_data.json")
            val gson = Gson()
            val listHealthCareType: Type = object : TypeToken<List<HealthCare>>() {}.type

            return if (jsonFileString != null) {
                val healthCares: List<HealthCare> =
                    gson.fromJson(jsonFileString, listHealthCareType)

                healthCares

            } else {
                emptyList()
            }

        }


}

