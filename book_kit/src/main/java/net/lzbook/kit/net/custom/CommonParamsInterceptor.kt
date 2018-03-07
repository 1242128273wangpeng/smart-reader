package net.lzbook.kit.net.custom

import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.net.token.Token.*
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.user.UserManager
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.OpenUDID
import net.lzbook.kit.utils.log
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

/**
 * Created by xian on 2017/7/24.
 */
class CommonParamsInterceptor : Interceptor {

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
        val mUserInfo = UserManager.mUserInfo
        var loginToken: String? = null
        if (null != mUserInfo) {
            loginToken = mUserInfo.login_token
        }

        commonParams.put("longitude", longitude)
        commonParams.put("latitude", latitude)
        commonParams.put("cityCode", cityCode)
        commonParams.put("loginToken", if (loginToken != null) loginToken else "")

        return commonParams
    }

    fun genTokenParams(request: Request, params: Map<String, String>): String? {
        val encodeMap = EncodeMap(params)
        val encodeUriTag = encodeUriTag(URLDecoder.decode(request.url().encodedPath(), "UTF-8"))
        //单张的请求TOKEN和其他不一样
        if ("/v3/book/chaptersContents" == encodeUriTag) {
            val u: URL = URL(request.url().toString())
            val urlParams: HashMap<String, String> = getUrlParams(u.query)
            urlParams.putAll(encodeMap)
            val token = getToken(encodeUriTag, urlParams)
            var encode = ""
            try {
                encode = URLEncoder.encode(token, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            val sb = StringBuffer()
            sb.append(u.protocol + "://")
            sb.append(u.host)
            sb.append(if (u.port == -1) "" else ":" + u.port)
            sb.append(u.path)
            sb.append("?" + mapToUrlParams(urlParams))
            sb.append("&token=" + encode)
            return sb.toString()
        } else if ("/api/bookapp/batch_chapter.m" == encodeUriTag) {
            val u: URL = request.url().toString() as URL
            val urlParams = urlParamsToMap(u.query)
            val postParam = String.format(contentPostParam, urlParams["gid"], urlParams["nid"], urlParams["sort"], urlParams["gsort"], urlParams["chapter_name"])
            val original = String.format(content, udid())
            try {
                return reurl(original, postParam)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                return null
            }
        } else {
            val token = getToken(encodeUriTag, encodeMap)
            val encode = URLEncoder.encode(token, "UTF-8")
            return "${request.url().scheme()}://${request.url().host()}:${request.url().port()}" + encodeUriTag + (if (encodeUriTag.contains("?")) "&" else "?") + mapToUrlParams(encodeMap) + "&token=" + encode
        }

    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.url().host().equals(URL(UrlUtils.getBookNovelDeployHost()).host) || request.url().host().equals(UrlUtils.BOOK_CONTENT)) {
            when (request.method().toUpperCase()) {
                "GET" -> {
                    request = buildGetRequest(request)
                }

                "POST" -> {
                    if (request.body() != null && request.body() is FormBody) {
                        val map = mutableMapOf<String, String>()
                        val url = UrlUtils.buildUrl(request.url().toString().replace(UrlUtils.getBookNovelDeployHost(), ""), map)
                            if (url != null) {
                                request = request.newBuilder().url(url).build()
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
        if(!originParams.containsKey("packageName")) {
            originParams.putAll(buildCommonParams())
        }

        val url = genTokenParams(newRequest, originParams) ?: return request
        newRequest = newRequest.newBuilder().url(url).build()

        return newRequest
    }
}