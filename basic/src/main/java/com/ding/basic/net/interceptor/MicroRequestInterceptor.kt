package com.ding.basic.net.interceptor

import android.text.TextUtils
import com.ding.basic.bean.Access
import com.ding.basic.bean.BasicResult
import com.ding.basic.config.ParameterConfig
import com.ding.basic.net.Config
import com.ding.basic.net.api.service.MicroService.Companion.AUTH_ACCESS
import com.ding.basic.net.token.Token
import com.ding.basic.util.AESUtil
import com.ding.basic.util.buildMicroRequest
import com.ding.basic.util.loadMicroRequestSign
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.orhanobut.logger.Logger
import okhttp3.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream

class MicroRequestInterceptor : Interceptor {

    private val requestParameters = mutableMapOf<String, String>()

    override fun intercept(chain: Interceptor.Chain): Response {
        return handleMicroInterceptAction(chain)
    }

    /***
     * 处理数据流请求拦截
     * **/
    private fun handleMicroInterceptAction(chain: Interceptor.Chain): Response {
        try {
            var request = chain.request()

            request = buildMicroRequest(request)

            val response = chain.proceed(request)

            return if (request.url().toString().contains(AUTH_ACCESS)) {
                response
            } else {
                handleMicroResponse(chain, request, response)
            }
        } catch (exception: Exception) {
            Logger.e("HandleMicroInterceptAction: $exception")
            throw exception
        }
    }

    /***
     * 处理数据流请求的Response，判断返回结果是否鉴权失败
     * **/
    private fun handleMicroResponse(chain: Interceptor.Chain, request: Request, response: Response): Response {
        return if (response.isSuccessful && response.code() == 200) {
            val responseBody = response.body()

            if (responseBody != null) {
                handleMicroResponseBody(chain, request, response, responseBody)
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
    private fun handleMicroResponseBody(chain: Interceptor.Chain, request: Request, response: Response, responseBody: ResponseBody): Response {
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
                val basicResult = Gson().fromJson(responseResult, BasicResult::class.java)

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
     * 判断返回的Response头中是否包含Gzip
     * **/
    private fun checkHeadersContainsGzip(headers: Headers): Boolean {
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
    private fun uncompressGzipString(bytes: ByteArray?): String? {
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
        val authenticationUrl = buildMicroRequest(AUTH_ACCESS, false)

        val authenticationRequest = Request.Builder().headers(request.headers())
                .removeHeader("publicKey")
                .addHeader("accessKey", Config.loadAccessKey()).url(authenticationUrl).build()

        return buildMicroRequest(authenticationRequest)
    }

    /***
     * 处理鉴权请求返回结果
     * **/
    @Throws(Exception::class)
    private fun handleAuthResponseBody(chain: Interceptor.Chain, request: Request, response: Response, responseBody: ResponseBody): Response {
        try {
            val bytes = responseBody.bytes()

            var responseResult: String? = null

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
                val basicResult = Gson().fromJson<BasicResult<String>>(responseResult, object : TypeToken<BasicResult<String>>() {}.type)

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
            val access = Gson().fromJson(message, Access::class.java)
            if (access != null) {
                if (access.publicKey != null) {
                    Config.insertPublicKey(access.publicKey!!)
                }

                if (access.privateKey != null) {
                    Config.insertPrivateKey(access.privateKey!!)
                }

                if (access.expire > 0) {
                    Config.insertAuthExpire(access.expire.toLong())
                }

                Logger.e("拦截器中处理鉴权请求: ${access.publicKey} : ${access.privateKey}")
            }
        }
    }

    /***
     * 重新请求原有请求
     * **/
    private fun handleOriginalRequest(chain: Interceptor.Chain, request: Request): Response {
        val originalRequest = buildMicroRequest(request)
        return chain.proceed(originalRequest)
    }

    /***
     * 重构数据流请求，添加公共参数，请求头等数据信息
     * **/
    private fun buildMicroRequest(request: Request): Request {
        var interimRequest = request

        val parameters = mutableMapOf<String, String>()

        val querySize = interimRequest.url().querySize()

        for (index in 0 until querySize) {
            if (interimRequest.url().queryParameterName(index) != "sign" && interimRequest.url().queryParameterName(index) != "privateKey") {
                parameters[interimRequest.url().queryParameterName(index)] = interimRequest.url().queryParameterValue(index)
            }
        }

        parameters.putAll(buildRequestParameters())

        val sign = loadMicroRequestSign(parameters)
        parameters["sign"] = sign

        Logger.v("Sign: $sign")

        val url = initMicroRequestUrl(interimRequest, parameters) ?: return request

        val builder = interimRequest.newBuilder().url(url)

        if (url.contains(AUTH_ACCESS)) {
            builder.addHeader("accessKey", Config.loadAccessKey())
        } else {
            builder.addHeader("publicKey", Config.loadPublicKey())
        }

        interimRequest = builder.build()

        Logger.v("Request: " + interimRequest.url().toString() + " : " + Config.loadPublicKey() + " : " + Config.loadPrivateKey())

        return interimRequest
    }

    /***
     * 拼接链接中的公共参数
     * **/
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

        requestParameters["cityCode"] = ParameterConfig.cityCode
        requestParameters["latitude"] = ParameterConfig.latitude
        requestParameters["longitude"] = ParameterConfig.longitude

        if (!TextUtils.isEmpty(Config.loadRequestParameter("loginToken"))) {
            requestParameters["loginToken"] = Config.loadRequestParameter("loginToken")
        }

        return requestParameters
    }

    /***
     * 重新初始化请求链接
     * **/
    private fun initMicroRequestUrl(request: Request, params: Map<String, String>): String? {
        val requestTag = Token.encodeRequestTag(URLDecoder.decode(request.url().encodedPath(), "UTF-8"))
        val parametersMap = Token.encodeParameters(params)

        return "${request.url().scheme()}://${request.url().host()}:${request.url().port()}" + requestTag + (if (requestTag != null && requestTag.contains("?")) "&" else "?") + Token.escapeParameters(parametersMap)
    }
}