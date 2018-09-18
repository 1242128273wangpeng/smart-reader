package com.ding.basic.net.interceptor

import android.text.TextUtils
import com.ding.basic.Config
import com.ding.basic.net.token.Token
import com.orhanobut.logger.Logger
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.codec.binary.Hex
import java.net.URLDecoder
import org.apache.commons.codec.digest.DigestUtils
import java.util.*


class MicroRequestInterceptor : Interceptor {

    private val requestParameters = mutableMapOf<String, String>()

    private fun buildRequestParameters(): Map<String, String> {
        if (requestParameters["packageName"] == null) {
            requestParameters["os"] = Config.loadRequestParameter("os")
            requestParameters["udid"] = Config.loadRequestParameter("udid")
            requestParameters["version"] = Config.loadRequestParameter("version")
            requestParameters["channelId"] = Config.loadRequestParameter("channelId")
            requestParameters["packageName"] = Config.loadRequestParameter("packageName")
        }

        requestParameters["latitude"] = Config.loadRequestParameter("latitude")
        requestParameters["cityCode"] = Config.loadRequestParameter("cityCode")
        requestParameters["longitude"] = Config.loadRequestParameter("longitude")

        if(!TextUtils.isEmpty(Config.loadRequestParameter("loginToken"))){
            requestParameters["loginToken"]=Config.loadRequestParameter("loginToken")
        }

        return requestParameters
    }

    private fun initRequestUrl(request: Request, params: Map<String, String>): String? {
        val requestTag = Token.encodeRequestTag(URLDecoder.decode(request.url().encodedPath(), "UTF-8"))
        val parametersMap = Token.encodeParameters(params)

        return "${request.url().scheme()}://${request.url().host()}:${request.url().port()}" + requestTag + (if (requestTag != null && requestTag.contains("?")) "&" else "?") + Token.escapeParameters(parametersMap)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        request = buildRequest(request)

        return chain.proceed(request)
    }

    private fun buildRequest(request: Request): Request {
        var otherRequest = request

        val parameters = mutableMapOf<String, String>()

        val querySize = otherRequest.url().querySize()

        for (index in 0 until querySize) {
            parameters[otherRequest.url().queryParameterName(index)] = otherRequest.url().queryParameterValue(index)
        }

        if (!parameters.containsKey("packageName")) {
            parameters.putAll(buildRequestParameters())
        }

        val sign = loadRequestSign(parameters)
        Logger.e("Sign: $sign")
        parameters["sign"] = sign

        val url = initRequestUrl(otherRequest, parameters) ?: return request

        otherRequest = otherRequest.newBuilder()
                .addHeader("accessKey", Config.loadAccessKey())
                .addHeader("publicKey", Config.loadPublicKey())
                .addHeader("privateKey", Config.loadPrivateKey())
                .url(url)
                .build()

        Logger.v("Request: " + otherRequest.url().toString() + " : " + Config.loadPublicKey() + " : " + Config.loadPrivateKey())

        return otherRequest
    }

    private fun loadRequestSign(parameters: Map<String, String>): String {
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

        Logger.e("String: " + stringBuilder.toString())

        if (stringBuilder.isNotEmpty()) {
            try {
                return String(Hex.encodeHex(DigestUtils.md5(stringBuilder.toString())))
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        return ""
    }
}