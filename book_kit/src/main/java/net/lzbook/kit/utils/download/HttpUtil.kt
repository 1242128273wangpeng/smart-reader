package net.lzbook.kit.utils.download

import com.google.gson.GsonBuilder
import org.apache.http.HttpStatus
import java.io.ByteArrayOutputStream
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


fun getHttpData(url: String): ByteArray {
    val th: Throwable
    var connection: HttpURLConnection? = null
    try {
        val openConnection = URL(url).openConnection() ?: throw TypeCastException("null cannot be cast to non-null type java.net.HttpURLConnection")
        connection = openConnection as HttpURLConnection
        connection.connectTimeout = HTTP_CONNECTION_TIMEOUT
        connection.readTimeout = HTTP_READ_TIMEOUT
        connection.doInput = true
        connection.connect()
        if (connection.responseCode == 200) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val inputStream = connection.inputStream
            try {

                inputStream.copyTo(byteArrayOutputStream)
                inputStream.close()
                return byteArrayOutputStream.toByteArray()
            } catch (e: Exception) {
                if (inputStream != null) {
                    try {
                        inputStream.close()
                    } catch (e2: Exception) {
                    }

                }
                throw e
            } catch (th2: Throwable) {
                th = th2
                inputStream!!.close()
                throw th
            }

        }
        throw IllegalArgumentException("HTTP返回码错误:" + connection.responseCode)
    } finally {
        if (connection != null) {
            connection.disconnect()
        }
    }
}