package com.dy.reader.service

import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import com.dingyue.statistics.utils.ToastUtil
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Function：字体下载服务
 *
 * Created by JoannChen on 2018/9/10 0010 22:56
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class FontDownLoadService : Service() {
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

    }


    fun downLoad(name: String, url: String) {
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {

            }

            override fun onResponse(call: Call?, response: Response?) {
                var inputStream: InputStream?
                val buf = ByteArray(2048)
                var len = -1
                var fos: FileOutputStream?
                var progress = 0
                // 储存下载文件的目录
                val savePath = Environment.getExternalStorageDirectory().absolutePath + "/font/" + name + ".ttf"
                response?.body()?.apply {

                    inputStream = byteStream()
                    val total = contentLength()
                    val file = File(savePath)

                    file.parentFile.let {
                        if (!it.exists()) {
                            it.mkdirs()
                        }
                    }

                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    fos = FileOutputStream(file)
                    var sum = 0L
                    inputStream?.read(buf) != 1

                    while ({ len = inputStream?.read(buf) ?: -1;len }() != -1) {
                        fos?.write(buf, 0, len)
                        sum += len
                        progress = (sum * 1.0f / total * 100).toInt()
                        ToastUtil.showToastMessage("进度" + progress)
                    }

                    fos?.flush()
                }

            }
        })
    }
}