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
import net.lzbook.kit.utils.doAsync
import net.lzbook.kit.utils.file.FileUtils
import net.lzbook.kit.utils.file.ZIPUtils
import java.io.File
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

        //        val publishHost = "https://sta-cnqbmfkkydqreader.bookapi.cn/cn-qbmfkkydq-reader"
        const val publishHost = "https://zn-h5-dev.bookapi.cn/cn-qbmfkkydq-reader"
        const val publishTime = "201811241823"

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


        /***
         * 获取缓存文件名
         * **/
        fun loadCacheFileName(url: String, method: String): String? {
            return try {
                val messageDigest = MessageDigest.getInstance(method)
                messageDigest?.update(url.toByteArray())
                BigInteger(1, messageDigest.digest()).toString(16)
            } catch (exception: Exception) {
                exception.printStackTrace()
                Base64.encodeToString(url.toByteArray(), Base64.DEFAULT)
            }
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

        /*  val sourceHashMap = HashMap<String, String>()
          sourceHashMap["qbmfkkydq/vendor.js"] = "$publishHost/vendor.js"
          sourceHashMap["qbmfkkydq/app.js"] = "$publishHost/$publishTime/js/app.js"
          sourceHashMap["qbmfkkydq/app.css"] = "$publishHost/$publishTime/css/app.css"
          sourceHashMap["qbmfkkydq/manifest.js"] = "$publishHost/$publishTime/js/manifest.js"
          cachingResource.addAll(sourceHashMap.values)

          val iterator = sourceHashMap.entries.iterator()
          while (iterator.hasNext()) {
              val entry = iterator.next()
              copyFileFromAssets(context, entry.key, entry.value)
          }*/

        cachingResource.add("$publishHost/vendor.js")
        cachingResource.add("$publishHost/$publishTime/js/app.js")
        cachingResource.add("$publishHost/$publishTime/css/app.css")
        cachingResource.add("$publishHost/$publishTime/js/manifest.js")

        copyFileFromAssets(context)
    }

    /**
     * copyzip包解压到本地
     */
    private fun copyFileFromAssets(context: Context) {
        doAsync {
            //解压耗时100ms左右
            Logger.e("JoannChen开始:" + System.currentTimeMillis())
            val start = System.currentTimeMillis()
            try {
                //该版本首次打开
                val file = File(ReplaceConstants.getReplaceConstants().APP_PATH_CACHE)
                if (file.exists()) {
                    //删除文件夹下所有文件
                    FileUtils.deleteFolderFile(file.absolutePath, false)
                }
                file.mkdirs()

                //第一次启动，从assets目录解压
                ZIPUtils.unZipAssets(context, "qbmfkkydq.zip", ReplaceConstants.getReplaceConstants().APP_PATH_CACHE, true)
                Logger.e("JoannChen结束: ${System.currentTimeMillis() - start}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 直接copy文件
     */
    private fun copyFileFromAssets(context: Context, key: String, value: String) {
        doAsync {
            val fileName = loadCacheFileName(value, "MD5")

            val fileSuffix = if (key == "app.css") "css" else "js"

            val filePath = ReplaceConstants.getReplaceConstants().APP_PATH_CACHE + fileName + "." + fileSuffix

            val file = File(filePath)

            try {
                val inputStream = context.assets.open(key)

                if (!file.exists()) {
                    file.createNewFile()

                    Logger.e("解压Assets文件: ${key}")

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
                file.delete()
            }
            cachingResource.remove(value)
        }
    }

    inner class CachedWebResource : Serializable {
        var file: File? = null
        var encoded: String? = null
        var mimeType: String? = null
    }
}