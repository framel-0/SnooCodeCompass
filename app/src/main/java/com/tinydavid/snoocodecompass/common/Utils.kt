package com.tinydavid.snoocodecompass.common

import android.content.Context
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

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
}
