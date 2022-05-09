package com.tinydavid.snoocodecompass.domain.use_cases

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import dagger.hilt.android.qualifiers.ActivityContext
import java.io.IOException
import java.util.*
import javax.inject.Inject

class GetAddressUseCase @Inject constructor(@ActivityContext private val context: Context) {

    operator fun invoke(lat: Double, lng: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val obj = addresses[0]
            var address = obj.getAddressLine(0)
            address = """
            $address
            ${obj.countryName}
            """.trimIndent()
            address = """
            $address
            ${obj.countryCode}
            """.trimIndent()
            address = """
            $address
            ${obj.adminArea}
            """.trimIndent()
            address = """
            $address
            ${obj.postalCode}
            """.trimIndent()
            address = """
            $address
            ${obj.subAdminArea}
            """.trimIndent()
            address = """
            $address
            ${obj.locality}
            """.trimIndent()
            address = """
            $address
            ${obj.subThoroughfare}
            """.trimIndent()
            Log.v("IGA", "Address$address")
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            address

            // TennisAppActivity.showDialog(add);
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            ""
        }
    }
}