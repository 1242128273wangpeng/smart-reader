package com.ding.basic.util

import com.ding.basic.Config
import com.ding.basic.config.ParameterConfig
import com.ding.basic.token.Token
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.net.URLDecoder
import java.util.*

/**
 * Desc 网络请求相关的扩展方法
 * Author crazylei
 * Mail crazylei911228@gmail.com
 * Date 2018/10/19 15:15
 */

/***
 * 获取字符串中的参数，主要用于打点
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
fun buildMicroParameters(parameters: MutableMap<String, String>): MutableMap<String, String> {
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

    if (parameters["latitude"].isNullOrEmpty()) {
        parameters["latitude"] = Config.loadRequestParameter("latitude")
    }

    if (parameters["longitude"].isNullOrEmpty()) {
        parameters["longitude"] = Config.loadRequestParameter("longitude")
    }

    if (parameters["cityCode"].isNullOrEmpty()) {
        parameters["cityCode"] = Config.loadRequestParameter("cityCode")
    }

    if (parameters["packageName"].isNullOrEmpty()) {
        parameters["packageName"] = Config.loadRequestParameter("packageName")
    }

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
 * 获取网络请求签名
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

    if (Config.loadPrivateKey().isNotEmpty()) {
        stringBuilder.append("privateKey=")
        stringBuilder.append(Config.loadPrivateKey())
    }

    if (stringBuilder.isNotEmpty()) {
        try {
            return String(Hex.encodeHex(DigestUtils.md5(stringBuilder.toString())))
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    return ""
}

/***
 * 获取网络请求链接
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

    return (Config.loadMicroAPIHost() + requestTag + joiner + Token.Companion.escapeParameters(parametersMap))
}