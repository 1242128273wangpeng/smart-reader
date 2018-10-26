package net.lzbook.kit.utils.web

import android.annotation.SuppressLint
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Base64
import android.webkit.MimeTypeMap
import android.webkit.WebResourceResponse
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.ding.basic.util.ReplaceConstants
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.base.BaseBookApplication
import java.io.File
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei911228@gmail.com
 * Date 2018/9/20 16:38
 */
class WebResourceCache {

    companion object {

        @Volatile
        @SuppressLint("StaticFieldLeak")
        private var webResourceCache: WebResourceCache? = null

        @Volatile
        private var cachingResource = ArrayList<String>()

        fun loadCustomWebViewCache(): WebResourceCache {
            if (webResourceCache == null) {
                synchronized(WebResourceCache::class.java) {
                    if (webResourceCache == null) {
                        webResourceCache = WebResourceCache()
                    }
                }
            }
            return webResourceCache!!
        }
    }

    /***
     * 处理图片拦截请求
     * **/
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun handleImageRequest(url: String, mimeType: String?): WebResourceResponse? {
        try {
            val cacheFile = Glide.with(BaseBookApplication.getGlobalContext())
                    .load(url)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get()

            return try {
                WebResourceResponse(mimeType, null, cacheFile.inputStream())
            } catch (exception: Exception) {
                exception.printStackTrace()
                null
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return null
        }
    }

    /***
     * 处理其他拦截请求
     * **/
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun handleOtherRequest(url: String, mimeType: String?, fileExtension: String): WebResourceResponse? {
        val caching = checkWebViewResourceCached(url)
        return if (caching) {
            Logger.e("文件正在缓存中: $url")
            null
        } else {
            val fileName = loadCacheFileName(url, "MD5")
            val filePath = ReplaceConstants.getReplaceConstants().APP_PATH_CACHE + fileName + "." + fileExtension

            val file = File(filePath)

            synchronized(this) {
                if (file.exists()) {
                    Logger.e("文件缓存命中成功: $url")
                    WebResourceResponse(mimeType, "UTF-8", file.inputStream())
                } else {
                    Logger.e("文件缓存命中失败: $url  $cachingResource")
                    return null
                }
            }
        }
    }

    /***
     * 获取缓存文件名
     * **/
    private fun loadCacheFileName(url: String, method: String): String? {
        return try {
            val messageDigest = MessageDigest.getInstance(method)
            messageDigest?.update(url.toByteArray())
            BigInteger(1, messageDigest.digest()).toString(16)
        } catch (exception: Exception) {
            exception.printStackTrace()
            Base64.encodeToString(url.toByteArray(), Base64.DEFAULT)
        }
    }

    /***
     * 检查资源是否正在缓存
     * **/
    @Synchronized
    fun checkWebViewResourceCached(url: String): Boolean {
        if (cachingResource.contains(url)) {
            return true
        } else {
            val fileName = loadCacheFileName(url, "MD5")
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(url)

            val filePath = ReplaceConstants.getReplaceConstants().APP_PATH_CACHE + fileName + "." + fileExtension

            val file = File(filePath)

            return if (!file.exists()) {
                file.delete()

                cachingResource.add(url)

                Observable.create<String> {
                    it.onNext(url)
                    it.onComplete()
                }.observeOn(Schedulers.io()).subscribeBy { cacheWebViewSource(url, filePath, fileExtension) }

                true
            } else {
                false
            }
        }
    }


    /***
     * 缓存获取到的文件流
     * **/
    private fun cacheWebViewSource(url: String, filePath: String, fileExtension: String) {
        val interimFile = File("$filePath.tmp")

        try {
            val connection = URL(url).openConnection()

            var read: Int = -1
            connection.inputStream?.use { input ->
                interimFile.outputStream().use {
                    while (input.read().also { read = it } != -1) {
                        it.write(read)
                    }
                }
            }
            val file = File(filePath)

            if (!file.exists()) {
                interimFile.renameTo(file)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            interimFile.delete()
        }

        cachingResource.remove(url)

        Logger.e("缓存网络请求地址: $url  缓存文件格式: $fileExtension  $cachingResource")
    }
}