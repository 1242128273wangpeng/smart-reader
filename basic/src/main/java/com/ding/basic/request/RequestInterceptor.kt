package com.ding.basic.request

import com.ding.basic.Config
import com.ding.basic.token.Token
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

        if (request.url().host() == URL(Config.loadRequestAPIHost()).host || request.url().host() == Config.loadBookContent()) {
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

        val url = initializeToken(otherRequest, parameters) ?: return request

        otherRequest = otherRequest.newBuilder().url(url).build()

        Logger.v("Request: " + otherRequest.url().toString())

        return otherRequest
    }
}