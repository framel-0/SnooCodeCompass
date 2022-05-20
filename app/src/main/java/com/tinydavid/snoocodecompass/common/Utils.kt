package com.tinydavid.snoocodecompass.common

import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.tinydavid.snoocodecompass.domain.models.HealthCare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import kotlin.math.roundToInt

object Utils {
    fun getJsonFromAssets(context: Context, fileName: String): String? {
        val jsonString: String = try {
            val stream: InputStream = context.assets.open(fileName)
            val size: Int = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
            val charset: Charset = Charsets.UTF_8
            String(buffer, charset)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return jsonString
    }

    suspend fun getJsonFromAsset(context: Context, fileName: String): String? = withContext(
        Dispatchers.IO
    ) {
        val jsonString: String = try {

            val stream: InputStream = context.assets.open(fileName)
            val size: Int = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
            val charset: Charset = Charsets.UTF_8
            String(buffer, charset)
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext null
        }
        return@withContext jsonString
    }

    class LocationComparator(private val myLocation: Location) : Comparator<HealthCare> {
        override fun compare(h1: HealthCare, h2: HealthCare): Int {
            val l1 = Location(LocationManager.GPS_PROVIDER).apply {
                latitude = h1.latitude
                longitude = h1.longitude
            }

            val l2 = Location(LocationManager.GPS_PROVIDER).apply {
                latitude = h2.latitude
                longitude = h2.longitude
            }

            return (l1.distanceTo(myLocation) - l2.distanceTo(myLocation)).roundToInt()
        }

    }


}
