package com.ding.basic.token

import com.easou.novel.commons.encryp.util.SignatureUtil
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern

/**
 * Created on 2018/3/14.
 * Created by crazylei.
 */
class Token {

    companion object {

        var postParameterString = "gid=%s&nid=%s&sort=%s&gsort=%s&sequence=0&chapter_name=%s"
        var batchParameterString = "http://api.easou.com/api/bookapp/batch_chapter.m?a=1&session_id=&cid=eef_easou_book&version=002&os=android&udid=%s&appverion=1034&ch=blf1298_12237_001"


        fun encodeParameters(parameters: Map<String, String>): Map<String, String> {

            val result: HashMap<String, String> = HashMap()
            val iterator = parameters.keys.iterator()

            while (iterator.hasNext()) {
                val entry = iterator.next()

                try {
                    result[entry] = URLEncoder.encode(parameters[entry] as String, "UTF-8")
                } catch (unsupportedEncodingException: UnsupportedEncodingException) {
                    unsupportedEncodingException.printStackTrace()
                }
            }
            return result
        }

        fun encodeRequestTag(url: String?): String? {
            val requestUrl: String
            var requestParameters: String

            if (url != null && url.contains("?")) {
                requestUrl = url.substring(0, url.indexOf("?"))
                requestParameters = url.substring(url.indexOf("?") + 1, url.length)

                val parameters = requestParameters.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                requestParameters = ""

                val count = parameters.size

                for (index in 0 until count) {
                    val parameter = parameters[index]

                    try {
                        requestParameters = requestParameters + "&" + parameter.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + "=" + URLEncoder.encode(parameter.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1], "UTF-8")
                    } catch (var9: UnsupportedEncodingException) {
                        var9.printStackTrace()
                    }
                }

                requestParameters = requestParameters.substring(1, requestParameters.length)

                return "$requestUrl?$requestParameters"
            } else {
                return url
            }
        }

        fun loadRequestParameters(query: String?): HashMap<String, String> {
            val result:HashMap<String, String> = HashMap()

            return if ("" != query && null != query) {
                val parameters = query.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                for (index in parameters.indices) {
                    val parameter = parameters[index].split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (parameter.size == 2) {
                        result[parameter[0]] = parameter[1]
                    }
                }
                result
            } else {
                result
            }
        }

        fun loadRequestToken(uri: String?, parameters: Map<String, String>): String {
            val requestHash = initializeHash(uri, parameters)
            val content = requestHash.toString() + "_" + System.currentTimeMillis()

            var token = ""

            try {
                token = encode(content)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return token
        }

        private fun initializeHash(uri: String?, parameters: Map<String, String>): Long {
            val parameterString = loadRequestParameters(parameters, true)
            val stringBuilder = StringBuilder()
            stringBuilder.append(uri).append(parameterString).append("mVjdXyqwjlEptwYY")
            return MurmurHash.hash32(stringBuilder.toString()).toLong()
        }

        private fun loadRequestParameters(parameters: Map<String, String>?, sort: Boolean): String {
            if (parameters != null && !parameters.isEmpty()) {
                val stringBuffer = StringBuffer()
                val keys = ArrayList(parameters.keys)

                if (sort) {
                    keys.sort()
                }

                for (i in keys.indices) {
                    val key = keys[i] as String
                    val value = (parameters[key] as String).toString()
                    stringBuffer.append("$key=$value")
                    stringBuffer.append("&")
                }

                var result = stringBuffer.toString()

                if (result.endsWith("&")) {
                    result = result.substring(0, result.lastIndexOf("&"))
                }

                return result
            } else {
                return ""
            }
        }

        @Throws(Exception::class)
        private fun encode(content: String): String {
            return AES.encodeAES(content, "7A3II/M5Ja0S4gdf")
        }

        fun escapeParameters(parameters: Map<String, String>): String {
            val stringBuffer = StringBuffer()
            for (key in parameters.keys) {
                stringBuffer.append("&" + key + "=" + parameters[key])
            }
            return stringBuffer.substring(1)
        }

        fun analyzeParameters(query: String): Map<String, String> {
            val key = "&(\\w+)="
            val input = "&$query"
            val pattern = Pattern.compile(key)
            val matcher = pattern.matcher(input)

            val parameters = HashMap<String, String>()

            val split = input.split(key.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            var i = 0

            while (matcher.find()) {
                val group = matcher.group()
                parameters[group.substring(1, group.length - 1)] = split[++i]
            }

            return parameters
        }

        fun loadUUID(): String {
            return UUID.randomUUID().toString().replace("-".toRegex(), "").toUpperCase()
        }

        @Throws(MalformedURLException::class)
        fun buildRequestUrl(request: String, parameters: String?): String {
            val url = URL(request)
            val query = url.query

            val values = HashMap<String, String>()

            values.putAll(analyzeParameters(query))

            if (parameters != null && parameters.isNotEmpty()) {
                values.putAll(analyzeParameters(parameters))
            }
            var sign = ""

            try {
                sign = SignatureUtil.sign(url.path, values)
            } catch (unsupportedEncodingException: UnsupportedEncodingException) {
                unsupportedEncodingException.printStackTrace()
            }

            return request + (if (parameters != null) "&$parameters" else "") + "&statId=" + sign
        }
    }
}