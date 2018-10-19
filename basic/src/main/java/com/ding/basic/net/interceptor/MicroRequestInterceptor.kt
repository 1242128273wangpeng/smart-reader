package com.ding.basic.net.interceptor

import android.text.TextUtils
import com.ding.basic.net.Config
import com.ding.basic.net.api.service.MicroService.Companion.AUTH_ACCESS
import com.ding.basic.net.token.Token
import com.ding.basic.util.loadMicroRequestSign
import com.orhanobut.logger.Logger
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.URLDecoder

class MicroRequestInterceptor : Interceptor {

    private val requestParameters = mutableMapOf<String, String>()

    private fun buildRequestParameters(): Map<String, String> {
        if (requestParameters["packageName"].isNullOrEmpty()) {
            requestParameters["packageName"] = Config.loadRequestParameter("packageName")
        }

        if (requestParameters["os"].isNullOrEmpty()) {
            requestParameters["os"] = Config.loadRequestParameter("os")
        }

        if (requestParameters["udid"].isNullOrEmpty()) {
            requestParameters["udid"] = Config.loadRequestParameter("udid")
        }

        if (requestParameters["version"].isNullOrEmpty()) {
            requestParameters["version"] = Config.loadRequestParameter("version")
        }

        if (requestParameters["channelId"].isNullOrEmpty()) {
            requestParameters["channelId"] = Config.loadRequestParameter("channelId")
        }

        if (requestParameters["latitude"].isNullOrEmpty()) {
            requestParameters["latitude"] = Config.loadRequestParameter("latitude")
        }

        if (requestParameters["longitude"].isNullOrEmpty()) {
            requestParameters["longitude"] = Config.loadRequestParameter("longitude")
        }

        if (requestParameters["cityCode"].isNullOrEmpty()) {
            requestParameters["cityCode"] = Config.loadRequestParameter("cityCode")
        }

        if(!TextUtils.isEmpty(Config.loadRequestParameter("loginToken"))){
            requestParameters["loginToken"]= Config.loadRequestParameter("loginToken")
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
        var interimRequest = request

        val parameters = mutableMapOf<String, String>()

        val querySize = interimRequest.url().querySize()

        for (index in 0 until querySize) {
            parameters[interimRequest.url().queryParameterName(index)] = interimRequest.url().queryParameterValue(index)
        }

        if (!parameters.containsKey("packageName")) {
            parameters.putAll(buildRequestParameters())
        }

        val sign = loadMicroRequestSign(parameters)
        Logger.e("Sign: $sign")
        parameters["sign"] = sign

        val url = initRequestUrl(interimRequest, parameters) ?: return request

        val builder = interimRequest.newBuilder()
                .addHeader("publicKey", Config.loadPublicKey())
                .url(url)

        if (url.contains(AUTH_ACCESS)) {
            builder.addHeader("accessKey", Config.loadAccessKey())
        }

        interimRequest = builder.build()

        Logger.v("Request: " + interimRequest.url().toString() + " : " + Config.loadPublicKey() + " : " + Config.loadPrivateKey())

        return interimRequest
    }
}