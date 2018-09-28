package com.ding.basic.request

import android.text.TextUtils
import com.ding.basic.Config
import com.ding.basic.token.Token
import com.ding.basic.util.ReplaceConstants
import com.orhanobut.logger.Logger
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.collections.HashMap

class RequestInterceptor : Interceptor {

    private val requestParameters = mutableMapOf<String, String>()

    private fun buildRequestParameters(): Map<String, String> {

        if (requestParameters["packageName"] == null) {
            requestParameters["packageName"] = Config.loadRequestParameter("packageName")
        }

        if (requestParameters["os"] == null) {
            requestParameters["os"] = Config.loadRequestParameter("os")
        }

        if (requestParameters["udid"] == null) {
            requestParameters["udid"] = Config.loadRequestParameter("udid")
        }

        if (requestParameters["version"] == null) {
            requestParameters["version"] = Config.loadRequestParameter("version")
        }

        if (requestParameters["channelId"] == null) {
            requestParameters["channelId"] = Config.loadRequestParameter("channelId")
        }

        if (requestParameters["latitude"] == null) {
            requestParameters["latitude"] = Config.loadRequestParameter("latitude")
        }

        if (requestParameters["longitude"] == null) {
            requestParameters["longitude"] = Config.loadRequestParameter("longitude")
        }

        if (requestParameters["cityCode"] == null) {
            requestParameters["cityCode"] = Config.loadRequestParameter("cityCode")
        }

        if (!TextUtils.isEmpty(Config.loadRequestParameter("loginToken"))) {
            requestParameters["loginToken"] = Config.loadRequestParameter("loginToken")
        }

        return requestParameters
    }

    private fun initializeToken(request: Request, params: Map<String, String>): String? {
        val requestTag = Token.encodeRequestTag(URLDecoder.decode(request.url().encodedPath(), "UTF-8"))
        val parametersMap = Token.encodeParameters(params)

        when (requestTag) {

            "/v3/book/chaptersContents" -> {
                val url = URL(request.url().toString())
                val parameters: HashMap<String, String> = Token.loadRequestParameters(url.query)

                parameters.putAll(parametersMap)

                val token = Token.loadRequestToken(requestTag, parameters)

                var encode = ""
                try {
                    encode = URLEncoder.encode(token, "UTF-8")
                } catch (unsupportedEncodingException: UnsupportedEncodingException) {
                    unsupportedEncodingException.printStackTrace()
                }

                val stringBuffer = StringBuffer()

                stringBuffer.append(url.protocol + "://")
                stringBuffer.append(url.host)
                stringBuffer.append(if (url.port == -1) "" else ":" + url.port)
                stringBuffer.append(url.path)
                stringBuffer.append("?")
                stringBuffer.append(Token.escapeParameters(parameters))
                stringBuffer.append("&token=")
                stringBuffer.append(encode)

                return stringBuffer.toString()
            }

            "/api/bookapp/batch_chapter.m" -> {
                val url = URL(request.url().toString())
                val parameters = Token.analyzeParameters(url.query)

                val postParameters = String.format(Token.postParameterString, parameters["gid"], parameters["nid"], parameters["sort"], parameters["gsort"], parameters["chapter_name"])
                val batchParameters = String.format(Token.batchParameterString, Token.loadUUID())
                return try {
                    Token.buildRequestUrl(batchParameters, postParameters)
                } catch (malformedURLException: MalformedURLException) {
                    malformedURLException.printStackTrace()
                    null
                }
            }

            else -> {
                val token = Token.loadRequestToken(requestTag, parametersMap)
                val encode = URLEncoder.encode(token, "UTF-8")
                return "${request.url().scheme()}://${request.url().host()}:${request.url().port()}" + requestTag + (if (requestTag != null && requestTag.contains("?")) "&" else "?") + Token.escapeParameters(parametersMap) + "&token=" + encode
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val host = request.url().host()

        if (host == URL("https://api.weixin.qq.com").host
                || host == URL("https://graph.qq.com").host
                || host == URL("https://public.lsread.cn/dpzn").host
                || host == URL("https://public.dingyueads.com/dpzn").host
                || host == URL("https://public.qingoo.cn/dpzn").host
                || host == URL("http://ad.dingyueads.com:8010/insertData").host) {
            Logger.e("请求微信或者QQ的接口: " + request.url().toString())
        } else {
            request = buildRequest(request)
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

        parameters.putAll(buildRequestParameters())

        val url = initializeToken(otherRequest, parameters) ?: return request

        otherRequest = otherRequest.newBuilder().url(url).build()

        Logger.v("Request: " + otherRequest.url().toString())

        return otherRequest
    }
}