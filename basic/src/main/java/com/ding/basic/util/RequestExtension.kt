package com.ding.basic.util

import com.ding.basic.config.ParameterConfig
import com.ding.basic.net.Config
import com.ding.basic.net.api.ContentAPI
import com.ding.basic.net.api.MicroAPI
import com.ding.basic.net.token.Token
import com.orhanobut.logger.Logger
import okhttp3.Headers
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.util.*
import java.util.zip.GZIPInputStream

/**
 * Desc 网络请求相关的扩展方法
 * Author crazylei
 * Mail crazylei911228@gmail.com
 * Date 2018/10/19 15:15
 */

/************************************************网络请求公共的扩展方法************************************************/

/***
 * 获取字符串中的参数，主要用于WebView通过前端打点使用
 * **/
fun loadMassageParameters(message: String?): HashMap<String, String> {
    val parameters = HashMap<String, String>()

    if (message.isNullOrEmpty()) {
        return parameters
    }

    val parameterArray = message?.split("#".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()

    if (parameterArray == null || parameterArray.isEmpty()) {
        return parameters
    }

    for (parameter in parameterArray) {
        val index = parameter.indexOf("=")
        val param = parameter.split(parameter[index].toString().toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (param.size >= 2) {
            parameters[param[0]] = param[1]
        } else if (param.size == 1) {
            parameters[param[0]] = ""
        }
    }
    return parameters
}

/***
 * 获取网络链接中的参数
 * **/
fun loadRequestUrlParameters(url: String?): HashMap<String, String> {
    val parameters = HashMap<String, String>()

    if (url.isNullOrEmpty()) {
        return parameters
    }

    val parameterArray = url?.split("&".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()

    if (parameterArray == null || parameterArray.isEmpty()) {
        return parameters
    }

    for (parameter in parameterArray) {
        val param = parameter.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (param.size == 2) {
            parameters[param[0]] = param[1]
        } else if (param.size == 1) {
            parameters[param[0]] = ""
        }
    }
    return parameters
}

/***
 * 拼接请求的公共参数
 * **/
fun buildRequestParameters(parameters: MutableMap<String, String>): MutableMap<String, String> {
    if (parameters["os"].isNullOrEmpty()) {
        parameters["os"] = Config.loadRequestParameter("os")
    }

    if (parameters["udid"].isNullOrEmpty()) {
        parameters["udid"] = Config.loadRequestParameter("udid")
    }

    if (parameters["version"].isNullOrEmpty()) {
        parameters["version"] = Config.loadRequestParameter("version")
    }

    if (parameters["channelId"].isNullOrEmpty()) {
        parameters["channelId"] = Config.loadRequestParameter("channelId")
    }

    if (parameters["packageName"].isNullOrEmpty()) {
        parameters["packageName"] = Config.loadRequestParameter("packageName")
    }

    parameters["cityCode"] = ParameterConfig.cityCode
    parameters["latitude"] = ParameterConfig.latitude
    parameters["longitude"] = ParameterConfig.longitude

    if (parameters["gender"].isNullOrEmpty()) {
        if (ParameterConfig.GENDER_TYPE == ParameterConfig.GENDER_BOY) {
            parameters["gender"] = "male"
        } else if (ParameterConfig.GENDER_TYPE == ParameterConfig.GENDER_GIRL) {
            parameters["gender"] = "female"
        }
    }

    if (Config.loadRequestParameter("loginToken").isNotEmpty()) {
        parameters["loginToken"] = Config.loadRequestParameter("loginToken")
    }

    return parameters
}

/***
 * 判断返回的Response头中是否包含Gzip
 * **/
fun checkHeadersContainsGzip(headers: Headers): Boolean {
    var gzip = false
    for (key in headers.names()) {
        if (key.equals("Accept-Encoding", ignoreCase = true) && headers.get(key)?.contains("gzip") == true || key.equals("Content-Encoding", ignoreCase = true) && headers.get(key)?.contains("gzip") == true) {
            gzip = true
            break
        }
    }
    return gzip
}

/***
 * 解析GZip格式的返回值
 * **/
@Throws(Exception::class)
fun uncompressGzipString(bytes: ByteArray?): String? {
    if (bytes == null || bytes.isEmpty()) {
        return null
    }

    val byteArrayInputStream = ByteArrayInputStream(bytes)

    val byteArrayOutputStream = ByteArrayOutputStream()

    val zipInputStream = GZIPInputStream(byteArrayInputStream)

    var read: Int = -1

    zipInputStream.use { input ->
        byteArrayOutputStream.use {
            while (input.read().also { read = it } != -1) {
                it.write(read)
            }
        }
    }

    return byteArrayOutputStream.toString("UTF-8")
}




/************************************************内容接口相关的扩展方法************************************************/

/***
 * 拼接内容网络请求链接
 * **/
fun buildContentRequest(message: String?): String {
    var url = message

    var parameters: MutableMap<String, String> = HashMap()

    if (!url.isNullOrEmpty()) {

        val array = url!!.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        url = array[0]

        if (array.size == 2) {
            parameters = loadRequestUrlParameters(array[1])
        }
    }

    parameters = buildRequestParameters(parameters)

    val sign = loadContentRequestSign(parameters)

    parameters["sign"] = sign

    Logger.e("签名完成后，携带的公钥为: " + ContentAPI.publicKey + " : " + ContentAPI.privateKey + " : " + sign)

    return buildContentRequestAction(url, parameters)
}


/***
 * 获取内容网络请求签名
 * **/
fun loadContentRequestSign(parameters: Map<String, String>): String {
    val parameterMap = TreeMap(parameters)
    val stringBuilder = StringBuilder()

    parameterMap.entries.forEach {
        if ("sign" != it.key) {
            stringBuilder.append(it.key)
            stringBuilder.append("=")
            stringBuilder.append(it.value)
        }
    }

    if (ContentAPI.privateKey?.isNotEmpty() == true) {
        stringBuilder.append("privateKey=")
        stringBuilder.append(ContentAPI.privateKey)
    }

    if (stringBuilder.isNotEmpty()) {
        Logger.i("String: $stringBuilder")
        try {
            return String(Hex.encodeHex(DigestUtils.md5(stringBuilder.toString())))
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    return ""
}

/***
 * 获取内容网络请求链接
 * **/
fun buildContentRequestAction(url: String?, parameters: Map<String, String>): String {
    var result = url

    try {
        result = URLDecoder.decode(url, "UTF-8")
    } catch (exception: Exception) {
        exception.printStackTrace()
    }

    val requestTag = Token.encodeRequestTag(result)

    val parametersMap = Token.encodeParameters(parameters)

    val joiner = if (requestTag != null && requestTag.contains("?")) {
        "&"
    } else {
        "?"
    }

    return (ContentAPI.contentHost + requestTag + joiner + Token.escapeParameters(parametersMap))
}




/************************************************数据流接口相关的扩展方法************************************************/

/***
 * 拼接数据流网络请求链接
 * **/
fun buildMicroRequest(message: String?, fromWebView: Boolean): String {
    var url = message

    var parameters: MutableMap<String, String> = HashMap()

    if (!url.isNullOrEmpty()) {

        val array = url!!.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        url = array[0]

        if (array.size == 2) {
            parameters = loadRequestUrlParameters(array[1])
        }
    }

    parameters = buildRequestParameters(parameters)

    val sign = loadMicroRequestSign(parameters)

    parameters["sign"] = sign

    if (fromWebView) {
        parameters["p"] = MicroAPI.publicKey ?: ""
    }

    Logger.e("签名完成后，携带的公钥为: " + MicroAPI.publicKey + " : " + sign)

    return buildMicroRequestAction(url, parameters)
}

/***
 * 获取数据流网络请求签名
 * **/
fun loadMicroRequestSign(parameters: Map<String, String>): String {
    val parameterMap = TreeMap(parameters)
    val stringBuilder = StringBuilder()

    parameterMap.entries.forEach {
        if ("sign" != it.key) {
            stringBuilder.append(it.key)
            stringBuilder.append("=")
            stringBuilder.append(it.value)
        }
    }

    if (MicroAPI.privateKey?.isNotEmpty() == true) {
        stringBuilder.append("privateKey=")
        stringBuilder.append(MicroAPI.privateKey)
    }

    if (stringBuilder.isNotEmpty()) {
        Logger.i("String: $stringBuilder")
        try {
            return String(Hex.encodeHex(DigestUtils.md5(stringBuilder.toString())))
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    return ""
}

/***
 * 获取数据流网络请求链接
 * **/
fun buildMicroRequestAction(url: String?, parameters: Map<String, String>): String {
    var result = url

    try {
        result = URLDecoder.decode(url, "UTF-8")
    } catch (exception: Exception) {
        exception.printStackTrace()
    }

    val requestTag = Token.encodeRequestTag(result)

    val parametersMap = Token.encodeParameters(parameters)

    val joiner = if (requestTag != null && requestTag.contains("?")) {
        "&"
    } else {
        "?"
    }

    return (MicroAPI.microHost + requestTag + joiner + Token.escapeParameters(parametersMap))
}