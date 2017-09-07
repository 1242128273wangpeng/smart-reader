package net.lzbook.kit.book.download

import com.google.gson.GsonBuilder
import org.apache.http.HttpStatus
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by xian on 17-6-5.
 */

fun <T> getHttpData(url: String, t: Class<T>): T? {
    val httpDataString = getHttpDataString(url)
    if (httpDataString != null) {
        try {
            val ret = GsonBuilder().create().fromJson<T>(httpDataString, t)
            return ret
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    return t.newInstance()
}

fun getHttpDataString(url: String): String? {
    var connection: HttpURLConnection? = null
    try {
        connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = HTTP_CONNECTION_TIMEOUT
        connection.readTimeout = HTTP_READ_TIMEOUT
        connection.doInput = true

        connection.connect()
        if (connection.responseCode == HttpStatus.SC_OK) {
            val readText = connection.inputStream.reader().readText()
            return readText
        }
    } catch (e: Exception) {
        e.printStackTrace()

    } finally {
        connection?.disconnect()
    }


    return null
}