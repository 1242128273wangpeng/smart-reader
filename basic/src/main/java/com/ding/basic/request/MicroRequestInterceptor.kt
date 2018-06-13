package com.ding.basic.request

import com.ding.basic.Config
import com.ding.basic.token.Token
import com.orhanobut.logger.Logger
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.codec.binary.Hex
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
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

        return requestParameters
    }

    private fun initRequestToken(request: Request, params: Map<String, String>): String {
        val requestTag = Token.encodeRequestTag(URLDecoder.decode(request.url().encodedPath(), "UTF-8"))
        val parametersMap = Token.encodeParameters(params)

        val token = Token.loadRequestToken(requestTag, parametersMap)
        return URLEncoder.encode(token, "UTF-8")
    }

    private fun initRequestUrl(request: Request, params: Map<String, String>): String? {
        val requestTag = Token.encodeRequestTag(URLDecoder.decode(request.url().encodedPath(), "UTF-8"))
        val parametersMap = Token.encodeParameters(params)

        return "${request.url().scheme()}://${request.url().host()}:${request.url().port()}" + requestTag + (if (requestTag != null && requestTag.contains("?")) "&" else "?") + Token.escapeParameters(parametersMap)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (request.url().host() == URL(Config.loadMicroAPIHost()).host || request.url().host() == Config.loadBookContent()) {
            when (request.method().toUpperCase()) {
                "GET" -> {
                    request = buildRequest(request)
                }

                "POST" -> {
                    if (request.body() != null && request.body() is FormBody) {
                        val map = mutableMapOf<String, String>()
                        val url = Config.buildUrl(request.url().toString().replace(Config.loadRequestAPIHost(), ""), map)
                        if (url != null) {
                            request = request.newBuilder().url(url).build()
                        }
                    } else {
                        request = buildRequest(request)
                    }
                }

                else -> {

                }
            }
        } else {
            Logger.e("other host, not add token")
        }

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