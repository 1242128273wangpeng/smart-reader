package net.lzbook.kit.utils.web

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.webkit.MimeTypeMap
import android.webkit.WebResourceResponse
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.ding.basic.config.WebViewConfig
import com.ding.basic.net.Config
import com.orhanobut.logger.Logger
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.ReplaceConstants
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

        val localPath = "file://" + ReplaceConstants.getReplaceConstants().APP_PATH_CACHE

        @Volatile
        private var webResourceCache: WebResourceCache? = null

        @Volatile
        var webResourceCachedMap = HashMap<String, WebResourceCached>()

        @Volatile
        private var cachingResource = ArrayList<String>()

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
    @Throws(Exception::class)
    fun handleOtherRequest(url: String, mimeType: String?): WebResourceResponse? {
        return if (url.startsWith("http")) {
            val filePath = url.replace(WebViewConfig.urlPath, localPath)
            val file = File(filePath)

            if (file.exists()) {
                webResourceCachedMap[url] = WebResourceCached(mimeType, "UTF-8", file)
                return WebResourceResponse(mimeType, "UTF-8", file.inputStream())
            } else {
                cacheWebViewSource(url, filePath)
                null
            }
        } else if (url.startsWith("file://")) {
            val filePath = url.replace("file://", "")
            val file = File(filePath)

            if (file.exists()) {
                webResourceCachedMap[url] = WebResourceCached(mimeType, "UTF-8", file)
                return WebResourceResponse(mimeType, "UTF-8", file.inputStream())
            } else {
                cacheWebViewSource(url.replace(localPath, WebViewConfig.urlPath), filePath)
                null
            }
        } else {
            null
        }
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

        Logger.e("缓存网络请求地址: $url")
    }

    /***
     * 解压预埋的H5文件
     * **/
    fun copyFileFromAssets(context: Context) {
        doAsync {
            try {
                val file = File(ReplaceConstants.getReplaceConstants().APP_PATH_CACHE)

                if (file.exists()) {
                    FileUtils.deleteFolderFile(file.absolutePath, false)
                }

                file.mkdirs()

                ZIPUtils.unZipAssets(context, WebViewConfig.zipPath, ReplaceConstants.getReplaceConstants().APP_PATH_CACHE, true)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    fun loadWebViewCache(url: String): WebResourceCached? {
        return webResourceCachedMap[url]
    }

    /***
     * 检查本地最新文件是否存在
     * **/
    fun checkLocalResourceFile(url: String) {
        doAsync {
            val resourceList = ArrayList<String>()
            resourceList.add("$url/js/vendor.js")
            resourceList.add("$url/js/app.js")
            resourceList.add("$url/css/app.css")
            resourceList.add("$url/js/manifest.js")
            resourceList.add("$url/index.html")

            for (resource in resourceList) {
                if (!resource.isEmpty()) {

                    val resourceFilePath = resource.replace(WebViewConfig.urlPath, ReplaceConstants.getReplaceConstants().APP_PATH_CACHE)

                    val resourceFile = File(resourceFilePath)

                    if (!resourceFile.exists()) {
                        Config.webCacheAvailable = false

                        doAsync {
                            cacheWebViewSource(resource, resourceFilePath)
                        }
                    } else {
                        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(resource)
                        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)

                        webResourceCachedMap[resource] = WebResourceCached(mimeType, "UTF-8", resourceFile)

                        webResourceCachedMap[resource.replace(WebViewConfig.urlPath, localPath)] = WebResourceCached(mimeType, "UTF-8", resourceFile)
                    }
                }
            }
        }
    }

    inner class WebResourceCached(var mimeType: String?, var encoding: String?, var file: File) : Serializable
}