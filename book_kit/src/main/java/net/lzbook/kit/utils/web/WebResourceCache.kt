package net.lzbook.kit.utils.web

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.webkit.WebResourceResponse
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.ding.basic.net.Config
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.doAsync
import net.lzbook.kit.utils.file.FileUtils
import net.lzbook.kit.utils.file.ZIPUtils
import java.io.File
import java.io.Serializable
import java.net.URL

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei911228@gmail.com
 * Date 2018/9/20 16:38
 */
class WebResourceCache {

    companion object {

        //TODO 各壳存储位置不一致，需要上线修改
        const val embeddedFile = "qbmfkkydq/web.zip"

        @Volatile
        private var webResourceCache: WebResourceCache? = null

        @Volatile
        private var cachingResource = ArrayList<String>()

        @Volatile
        private var resourceResponseHashMap = HashMap<String, CachedWebResource>()

        private var packageName = ""

        fun loadWebResourceCache(): WebResourceCache {
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
    fun handleOtherRequest(url: String, mimeType: String?): WebResourceResponse? {

        val localFile = url.replace("https://sta-cnqbmfkkydqreader.bookapi.cn/cn-qbmfkkydq-reader", "file://" + ReplaceConstants.getReplaceConstants().APP_PATH_CACHE + "web")

        return WebResourceResponse(mimeType, "UTF-8", URL(localFile).openConnection().getInputStream())
//
//        val caching = checkWebViewResourceCaching(url)
//
//        return if (caching) {
//            Logger.e("文件正在缓存中: $url")
//            null
//        } else {
//            val filePath = requestUrlFilePath(url)
//
//            val file = File(filePath)
//
//            synchronized(this) {
//                return if (file.exists()) {
//                    Logger.e("文件缓存命中成功: $url")
//
//                    val cachedWebResource = CachedWebResource()
//
//                    cachedWebResource.file = file
//                    cachedWebResource.encoded = "UTF-8"
//                    cachedWebResource.mimeType = mimeType
//
//                    resourceResponseHashMap[url] = cachedWebResource
//
//                    WebResourceResponse(mimeType, "UTF-8", file.inputStream())
//                } else {
//                    Logger.e("文件缓存命中失败: $url  $cachingResource")
//                    null
//                }
//            }
//        }
    }


    /***
     * 缓存获取到的文件流
     * **/
    private fun cacheWebViewSource(url: String, filePath: String) {
        val interimFile = File("$filePath.tmp")

        try {
            val parentFile = interimFile.parentFile

            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }

            if (!interimFile.exists()) {
                interimFile.createNewFile()
            }

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

        Logger.e("缓存网络请求地址: $url  缓存文件格式:  $cachingResource")
    }

    /***
     * 检查资源是否正在缓存
     * **/
    @Synchronized
    fun checkWebViewResourceCaching(url: String): Boolean {
        if (cachingResource.contains(url)) {
            return true
        } else {
            val filePath = requestUrlFilePath(url)

            if (filePath.isNotEmpty()) {

                val file = File(filePath)

                return if (!file.exists()) {
                    cachingResource.add(url)

                    Observable.create<String> {
                        it.onNext(url)
                        it.onComplete()
                    }.observeOn(Schedulers.io()).subscribeBy { cacheWebViewSource(url, filePath) }

                    true
                } else {
                    cachingResource.remove(url)
                    false
                }
            } else {
                return false
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
     * 获取Url对应的文件地址路径
     * **/
    private fun requestUrlFilePath(url: String): String {

        if (url.contains("vendor.js")) {
            return ReplaceConstants.getReplaceConstants().APP_PATH_CACHE + "/web/vendor.js"
        }

        val fileSubPath = when {
            url.contains("app.js") -> "/js/app.js"
            url.contains("app.css") -> "/css/app.css"
            url.contains("manifest.js") -> "/js/manifest.js"
            url.endsWith("index.html") -> "index.html"
            else -> ""
        }

        return if (fileSubPath.isNotEmpty()) {
            val time = requestUrlTimeStamp(url)

            if (time.isNotEmpty()) {
                ReplaceConstants.getReplaceConstants().APP_PATH_CACHE + "/web" + "/" + time + fileSubPath
            } else {
                ""
            }
        } else {
            ""
        }
    }

    /***
     * 获取Url的时间戳
     * **/
    private fun requestUrlTimeStamp(url: String): String {
        if (packageName.isEmpty()) {
            packageName = AppUtils.getPackageName().replace(".", "-")
        }

        val start = url.lastIndexOf(packageName) + packageName.length + 1

        val stop = when {
            url.contains("/js/app.js") -> url.lastIndexOf("/js/app.js")
            url.contains("/css/app.css") -> url.lastIndexOf("/css/app.css")
            url.contains("/js/manifest.js") -> url.lastIndexOf("/js/manifest.js")
            else -> -1
        }

        val length = url.length

        return if (start > -1 && start < length && stop > -1 && stop <= length) {
            val time = url.substring(start, stop)
            time
        } else {
            ""
        }
    }


    /***
     * 解压预埋的H5文件
     * **/
    fun copyFileFromAssets(context: Context) {
        doAsync {
            try {
                val file = File(ReplaceConstants.getReplaceConstants().APP_PATH_CACHE + "/web")

                if (file.exists()) {
                    FileUtils.deleteFolderFile(file.absolutePath, false)
                }

                file.mkdirs()

                ZIPUtils.unZipAssets(context, embeddedFile, ReplaceConstants.getReplaceConstants().APP_PATH_CACHE, true)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    /***
     * 检查本地最新文件是否存在
     * **/
    fun checkLocalResourceFile(url: String) {
        doAsync {
            val index = url.lastIndexOf("/")

            if (index > -1 && index < url.length) {
                val timeStamp = url.substring(url.lastIndexOf("/"), url.length)

                if (!timeStamp.isEmpty()) {

                    val filePath = (ReplaceConstants.getReplaceConstants().APP_PATH_CACHE
                            + "web/" + timeStamp + "/index.html")

                    val file = File(filePath)

                    if (!file.exists()) {

                        val resourceList = java.util.ArrayList<String>()
                        resourceList.add("$url/index.html")
                        resourceList.add("$url/js/app.js")
                        resourceList.add("$url/css/app.css")
                        resourceList.add("$url/js/manifest.js")

                        for (resource in resourceList) {
                            if (!resource.isEmpty()) {
                                checkWebViewResourceCaching(resource)
                            }
                        }
                    }
                }
            }
        }
    }

    inner class CachedWebResource : Serializable {
        var file: File? = null
        var encoded: String? = null
        var mimeType: String? = null
    }
}