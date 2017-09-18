package net.lzbook.kit.user

import android.preference.PreferenceManager
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.MurmurHash
import net.lzbook.kit.utils.OpenUDID
import net.lzbook.kit.utils.log
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.URL
import java.net.URLDecoder

/**
 * Created by xian on 2017/7/24.
 */
class CommonParamsInterceptor : Interceptor {

    val MAIN_HOST by lazy {
        val sp = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext())
        sp.getString(Constants.NOVEL_HOST, UrlUtils.BOOK_NOVEL_DEPLOY_HOST)
    }

    val commonParams = mutableMapOf<String, String>()

    fun buildCommonParams(): Map<String, String> {
        if (commonParams["packageName"] == null) {
            val channelId = AppUtils.getChannelId()
            val version = AppUtils.getVersionCode().toString()
            val packageName = AppUtils.getPackageName()
            val os = Constants.APP_SYSTEM_PLATFORM
            val udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext())

            commonParams.put("packageName", packageName)
            commonParams.put("version", version)
            commonParams.put("channelId", channelId)
            commonParams.put("os", os)
            commonParams.put("udid", udid)
        }


        val longitude = Constants.longitude.toString() + ""
        val latitude = Constants.latitude.toString() + ""
        val cityCode = Constants.cityCode

        commonParams.put("longitude", longitude)
        commonParams.put("latitude", latitude)
        commonParams.put("cityCode", cityCode)

        return commonParams
    }

    fun genTokenParams(request: Request, params: Map<String, String>): String? {

        val urlBuilderIntterface = BaseBookApplication.getUrlBuilderIntterface()
        if (urlBuilderIntterface != null) {
            val url = urlBuilderIntterface!!.buildUrl("${request.url().scheme()}://${request.url().host()}:${request.url().port()}", URLDecoder.decode(request.url().encodedPath(), "UTF-8"), params)
            return url
        }
        return null
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.url().host().equals(URL(MAIN_HOST).host)) {
            when (request.method().toUpperCase()) {
                "GET" -> {
                    request = buildGetRequest(request)
                }

                "POST" -> {
                    if (request.body() != null && request.body() is FormBody) {
                        val formBody = request.body() as FormBody
                        var data: String? = null
                        for (index in 0..formBody.size() - 1) {
                            if (formBody.name(index).equals("data")) {
                                data = formBody.value(index)
                                break
                            }
                        }

                        if (data != null) {
                            val map = mutableMapOf<String, String>()
                            val hash = MurmurHash.hash32(data)
                            map.put("hash", hash.toString())
                            val url = genTokenParams(request, map)
                            if (url != null) {
                                request = request.newBuilder().url(url).build()
                            }
                        }
                    } else {
                        log("intercept", "POST as GET")
                        request = buildGetRequest(request)
                    }
                }

                else -> {

                }
            }
        } else {
            log("intercept", "other host, not add token")
        }

        return chain.proceed(request)
    }

    private fun buildGetRequest(request: Request): Request {
        var newRequest = request
        val originParams = mutableMapOf<String, String>()
        val querySize = newRequest.url().querySize()

        for (index in 0..querySize - 1) {
            originParams.put(newRequest.url().queryParameterName(index), newRequest.url().queryParameterValue(index))
        }
        originParams.putAll(buildCommonParams())

        val url = genTokenParams(newRequest, originParams)
        if (url == null) {
            return request
        }
        newRequest = newRequest.newBuilder().url(url).build()
        return newRequest
    }
}