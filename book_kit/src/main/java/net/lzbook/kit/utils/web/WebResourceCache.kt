package net.lzbook.kit.utils.web

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.webkit.WebResourceResponse
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.orhanobut.logger.Logger
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.doAsync
import net.lzbook.kit.utils.file.FileUtils
import net.lzbook.kit.utils.file.ZIPUtils
import java.io.File
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
        const val embeddedFile = "qbmfkkydq/201811282000.zip"

        const val internetPath = "https://sta-cnqbmfkkydqreader.bookapi.cn/cn-qbmfkkydq-reader/"

        val localPath = "file://" + ReplaceConstants.getReplaceConstants().APP_PATH_CACHE

        @Volatile
        private var webResourceCache: WebResourceCache? = null

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
    fun handleOtherRequest(url: String, mimeType: String?): WebResourceResponse? {
        val localFileUrl = url.replace(internetPath, localPath)

        return try {
            WebResourceResponse(mimeType, "UTF-8", URL(localFileUrl).openConnection().getInputStream())
        } catch (exception: Exception) {
            exception.printStackTrace()
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

        Logger.e("缓存网络请求地址: $url  缓存文件格式:  $cachingResource")
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
            val filePath = url.replace(internetPath, ReplaceConstants.getReplaceConstants().APP_PATH_CACHE) + "/index.html"

            val file = File(filePath)

            if (!file.exists()) {

                val resourceList = java.util.ArrayList<String>()
                resourceList.add("$url/index.html")
                resourceList.add("$url/js/vendor.js")
                resourceList.add("$url/js/app.js")
                resourceList.add("$url/css/app.css")
                resourceList.add("$url/js/manifest.js")

                for (resource in resourceList) {
                    if (!resource.isEmpty()) {

                        val resourceFilePath = resource.replace(internetPath, ReplaceConstants.getReplaceConstants().APP_PATH_CACHE)

                        val resourceFile = File(resourceFilePath)

                        if (!resourceFile.exists()) {
                            doAsync {
                                cacheWebViewSource(resource, resourceFilePath)
                            }
                        }
                    }
                }
            }
        }
    }
}