package com.ding.basic.net.interceptor

import com.ding.basic.bean.Access
import com.ding.basic.bean.BasicResult
import com.ding.basic.net.Config
import com.ding.basic.net.api.ContentAPI
import com.ding.basic.net.api.service.ContentService.Companion.AUTH_ACCESS
import com.ding.basic.net.token.Token
import com.ding.basic.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.orhanobut.logger.Logger
import okhttp3.*
import java.net.URLDecoder
import java.nio.charset.Charset

class ContentInterceptor : Interceptor {

    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        return handleMicroInterceptAction(chain)
    }

    /***
     * 处理数据流请求拦截
     * **/
    private fun handleMicroInterceptAction(chain: Interceptor.Chain): Response {
        try {
            var request = chain.request()

            request = buildContentRequest(request)

            val response = chain.proceed(request)

            return if (request.url().toString().contains(AUTH_ACCESS)) {
                response
            } else {
                handleContentResponse(chain, request, response)
            }
        } catch (exception: Exception) {
            Logger.e("HandleMicroInterceptAction: $exception")
            throw exception
        }
    }

    /***
     * 处理数据流请求的Response，判断返回结果是否鉴权失败
     * **/
    private fun handleContentResponse(chain: Interceptor.Chain, request: Request, response: Response): Response {
        return if (response.isSuccessful && response.code() == 200) {
            val responseBody = response.body()

            if (responseBody != null) {
                handleContentResponseBody(chain, request, response, responseBody)
            } else {
                response
            }
        } else {
            response
        }
    }

    /***
     * 处理数据流请求返回结果
     * **/
    @Throws(Exception::class)
    private fun handleContentResponseBody(chain: Interceptor.Chain, request: Request, response: Response, responseBody: ResponseBody): Response {
        try {
            val bytes = responseBody.bytes()

            val mediaType = responseBody.contentType()

            val responseResult = if (checkHeadersContainsGzip(response.headers())) {
                try {
                    uncompressGzipString(bytes)
                } catch (exception: Exception) {
                    Logger.e("HandleMicroResponseBody GZip: $exception")
                    throw exception
                }
            } else {
                try {
                    bytes.toString(Charset.forName("UTF-8"))
                } catch (exception: Exception) {
                    Logger.e("HandleMicroResponseBody Byte: $exception")
                    throw exception
                }
            }

            return try {
                val basicResult = gson.fromJson(responseResult, BasicResult::class.java)

                if (basicResult.checkPrivateKeyExpire()) {
                    Logger.e("网络请求鉴权异常: ${basicResult.code} : ${basicResult.msg}")
                    requestAuthentication(chain, request)
                } else {
                    Logger.e("网络请求结果正常: ${request.url()}")
                    response.newBuilder().body(ResponseBody.create(mediaType, bytes)).build()
                }
            } catch (exception: Exception) {
                Logger.e("HandleMicroResponseBody Gson: $exception")
                throw exception
            }
        } catch (exception: Exception) {
            Logger.e("HandleMicroResponseBody: $exception")
            throw exception
        }
    }

    /***
     * 请求鉴权接口
     * **/
    private fun requestAuthentication(chain: Interceptor.Chain, request: Request): Response {
        val authRequest = reconfigurationRequest(request)

        val authResponse: Response
        try {
            authResponse = chain.proceed(authRequest)
        } catch (exception: Exception) {
            Logger.e("RequestAuthentication: $exception")
            throw exception
        }

        if (authResponse.isSuccessful && authResponse.code() == 200) {
            val responseBody = authResponse.body()

            if (responseBody != null) {
                return handleAuthResponseBody(chain, request, authResponse, responseBody)
            }
        } else {
            Logger.e("RequestAuthentication: 鉴权请求结果异常 ${authResponse.code()}")
            throw IllegalArgumentException("鉴权请求结果异常: ${authResponse.code()}")
        }

        return authResponse
    }

    /***
     * 重构鉴权的请求体
     * **/
    private fun reconfigurationRequest(request: Request): Request {
        val authenticationUrl = buildContentRequest(AUTH_ACCESS)

        val authenticationRequest = Request.Builder().headers(request.headers())
                .removeHeader("publicKey")
                .addHeader("accessKey", Config.loadAccessKey()).url(authenticationUrl).build()

        return buildContentRequest(authenticationRequest)
    }

    /***
     * 处理鉴权请求返回结果
     * **/
    @Throws(Exception::class)
    private fun handleAuthResponseBody(chain: Interceptor.Chain, request: Request, response: Response, responseBody: ResponseBody): Response {
        try {
            val bytes = responseBody.bytes()

            val responseResult: String?

            responseResult = if (checkHeadersContainsGzip(response.headers())) {
                try {
                    uncompressGzipString(bytes)
                } catch (exception: Exception) {
                    Logger.e("HandleAuthResponseBody GZip: $exception")
                    throw exception
                }
            } else {
                try {
                    bytes.toString(Charset.forName("UTF-8"))
                } catch (exception: Exception) {
                    Logger.e("HandleAuthResponseBody Byte: $exception")
                    throw exception
                }
            }

            try {
                val basicResult = gson.fromJson<BasicResult<String>>(responseResult, object : TypeToken<BasicResult<String>>() {}.type)

                if (basicResult.checkResultAvailable()) {
                    handleAuthResult(basicResult)
                    return handleOriginalRequest(chain, request)
                } else {
                    Logger.e("HandleAuthResponseBody: 鉴权请求返回结果异常 ${basicResult.code} ${basicResult.msg}")
                    throw IllegalArgumentException("鉴权请求返回结果异常: ${basicResult.code} ${basicResult.msg}")
                }
            } catch (exception: Exception) {
                Logger.e("HandleAuthResponseBody Gson: $exception")
                throw exception
            }
        } catch (exception: Exception) {
            Logger.e("HandleAuthResponseBody: $exception")
            throw exception
        }
    }

    /***
     * 处理鉴权请求结果
     * **/
    private fun handleAuthResult(result: BasicResult<String>) {
        val message = AESUtil.decrypt(result.data, Config.loadAccessKey())

        if (message != null && message.isNotEmpty()) {
            val access = gson.fromJson(message, Access::class.java)
            if (access != null) {
                if (access.publicKey != null) {
                    ContentAPI.publicKey = access.publicKey
                }

                if (access.privateKey != null) {
                    ContentAPI.privateKey = access.privateKey
                }

                if (access.expire > 0) {
                    ContentAPI.expire = access.expire.toLong()
                }

                Logger.e("内容拦截器中处理鉴权请求: ${access.publicKey} : ${access.privateKey}")
            }
        }
    }

    /***
     * 重新请求原有请求
     * **/
    private fun handleOriginalRequest(chain: Interceptor.Chain, request: Request): Response {
        val originalRequest = buildContentRequest(request)
        return chain.proceed(originalRequest)
    }

    /***
     * 重构数据流请求，添加公共参数，请求头等数据信息
     * **/
    private fun buildContentRequest(request: Request): Request {
        var interimRequest = request

        val parameters = mutableMapOf<String, String>()

        val querySize = interimRequest.url().querySize()

        for (index in 0 until querySize) {
            if (interimRequest.url().queryParameterName(index) != "sign" && interimRequest.url().queryParameterName(index) != "privateKey") {
                parameters[interimRequest.url().queryParameterName(index)] = interimRequest.url().queryParameterValue(index)
            }
        }

        buildRequestParameters(parameters)

        val sign = loadContentRequestSign(parameters)
        parameters["sign"] = sign

        Logger.v("Sign: $sign")

        val url = initContentRequestUrl(interimRequest, parameters) ?: return request

        val builder = interimRequest.newBuilder().url(url)

        if (url.contains(AUTH_ACCESS)) {
            builder.addHeader("accessKey", ContentAPI.accessKey)
        } else {
            builder.addHeader("publicKey", ContentAPI.publicKey ?: "")
        }

        interimRequest = builder.build()

        Logger.v("Request: " + interimRequest.url().toString() + " : " + ContentAPI.publicKey + " : " + ContentAPI.privateKey)

        return interimRequest
    }

    /***
     * 重新初始化请求链接
     * **/
    private fun initContentRequestUrl(request: Request, params: Map<String, String>): String? {
        val requestTag = Token.encodeRequestTag(URLDecoder.decode(request.url().encodedPath(), "UTF-8"))
        val parametersMap = Token.encodeParameters(params)

        return "${request.url().scheme()}://${request.url().host()}:${request.url().port()}" + requestTag + (if (requestTag != null && requestTag.contains("?")) "&" else "?") + Token.escapeParameters(parametersMap)
    }
}