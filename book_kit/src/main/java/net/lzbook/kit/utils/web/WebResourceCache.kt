package net.lzbook.kit.utils.web

import android.annotation.SuppressLint
import android.content.Context
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
import net.lzbook.kit.constants.Constants
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
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

        @Volatile
        private var resourceResponseHashMap = HashMap<String, CachedWebResource>()

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
                return if (file.exists()) {
                    Logger.e("文件缓存命中成功: $url")

                    val cachedWebResource = CachedWebResource()

                    cachedWebResource.file = file
                    cachedWebResource.encoded = "UTF-8"
                    cachedWebResource.mimeType = mimeType

                    resourceResponseHashMap[url] = cachedWebResource

                    WebResourceResponse(mimeType, "UTF-8", file.inputStream())
                } else {
                    Logger.e("文件缓存命中失败: $url  $cachingResource")
                    null
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
     * 缓存获取到的文件流
     * **/
    private fun cacheWebViewSource(url: String, filePath: String, fileExtension: String) {
        val interimFile = File("$filePath.tmp")

        try {
            val connection = URL(url).openConnection()

            var read: Int = -1
            connection.inputStream?.use { input ->
                interimFile.outputStream().use { fileOutputStream ->
                    while (input.read().also { read = it } != -1) {
                        fileOutputStream.write(read)
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

    /***
     * 检查资源是否正在缓存
     * **/
    @Synchronized
    fun checkWebViewResourceCached(url: String): Boolean {
        if (cachingResource.contains(url)) {
            return true
        } else {
            cachingResource.add(url)

            val fileName = loadCacheFileName(url, "MD5")
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(url)

            val filePath = ReplaceConstants.getReplaceConstants().APP_PATH_CACHE + fileName + "." + fileExtension

            val file = File(filePath)

            return if (!file.exists()) {
                cachingResource.add(url)

                Observable.create<String> {
                    it.onNext(url)
                    it.onComplete()
                }.observeOn(Schedulers.io()).subscribeBy { cacheWebViewSource(url, filePath, fileExtension) }

                true
            } else {
                cachingResource.remove(url)

                false
            }
        }
    }

    /***
     * 检查资源是否已经加载
     * **/
    fun checkWebResourceResponse(url: String): CachedWebResource? {
        return if (resourceResponseHashMap.containsKey(url)) {
            resourceResponseHashMap[url]
        } else {
            null
        }
    }

    /***
     * 从Assets文件夹下拷贝文件到SD卡
     * TODO 上线修改为线上配置的地址
     * **/
    fun copyVendorFromAssets(context: Context) {
        val sourceHashMap = HashMap<String, String>()

        sourceHashMap["vendor.js"] = "https://zn-h5-dev.bookapi.cn/cn-qbmfkkydq-reader/vendor.js"
        sourceHashMap["app.js"] = "https://zn-h5-dev.bookapi.cn/cn-qbmfkkydq-reader/201811191515/js/app.js"
        sourceHashMap["app.css"] = "https://zn-h5-dev.bookapi.cn/cn-qbmfkkydq-reader/201811191515/css/app.css"
        sourceHashMap["manifest.js"] = "https://zn-h5-dev.bookapi.cn/cn-qbmfkkydq-reader/201811191515/js/manifest.js"

        sourceHashMap.filter { it.key.isNotEmpty() && it.value.isNotEmpty() }
                .forEach { entry ->
                    try {
                        val inputStream = context.assets.open(entry.key)

                        val fileName = loadCacheFileName(entry.value, "MD5")

                        val fileSuffix = if (entry.key == "app.css") "css" else "js"

                        val filePath = ReplaceConstants.getReplaceConstants().APP_PATH_CACHE + fileName + "." + fileSuffix

                        val file = File(filePath)

                        if (!file.exists()) {
                            file.createNewFile()

                            Logger.e("解压Assets文件: ${entry.key}")

                            var read: Int = -1

                            inputStream?.use { input ->
                                file.outputStream().use { fileOutputStream ->
                                    while (input.read().also { read = it } != -1) {
                                        fileOutputStream.write(read)
                                    }
                                }
                            }
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
    }

    inner class CachedWebResource : Serializable {
        var file: File? = null
        var encoded: String? = null
        var mimeType: String? = null
    }
}