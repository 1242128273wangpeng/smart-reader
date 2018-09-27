package com.dy.reader.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Environment
import com.ding.basic.RequestRepositoryFactory
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.utils.loge
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Function：字体下载服务
 *
 * Created by JoannChen on 2018/9/10 0010 22:56
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class FontDownLoadService : IntentService("font_download_service") {

    fun start(context: Context, fontName: String, position: Int) {
        val intent = Intent(context, FontDownLoadService::class.java)
        intent.putExtra(FONT_NAME_KEY, fontName)
        intent.putExtra(FONT_POSITION_KEY, position)
        context.startService(intent)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        val fontName = intent.getStringExtra(FONT_NAME_KEY)
        val fontPosition = intent.getIntExtra(FONT_POSITION_KEY, 0)

        if (fontName?.isEmpty() == true) return

        EventBus.getDefault().postSticky(Event(fontName, 0, fontPosition, STATUS_DOWNLOADING))

        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .downloadFont(fontName)
                .observeOn(Schedulers.io())
                .subscribeBy(onNext = {
                    writeToFile(it, fontName, fontPosition)
                }, onError = {
                    onDownloadError(fontName, fontPosition, it)
                })
    }

    private fun writeToFile(it: ResponseBody, fontName: String, fontPosition: Int) {
        var inputStream: InputStream? = null
        var fos: FileOutputStream? = null
        try {
            inputStream = it.byteStream()
            val buf = ByteArray(1024 * 4)
            var len = 0
            var progress: Int

            // 储存下载文件的目录
            val savePath = getFontPath(fontName)
            val total = it.contentLength()
            val file = File("$savePath.tmp") //临时命名
            loge("create file: $savePath")

            file.parentFile?.let {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }

            if (!file.exists()) {
                file.createNewFile()
            } else {
                file.delete()
            }

            fos = FileOutputStream(file)
            var sum = 0L
            while (inputStream.read(buf).apply { len = this } > 0) {
                fos.write(buf, 0, len)
                sum += len
                progress = (sum * 1.0f / total * 100).toInt()
                onProgress(progress, fontName, fontPosition)
                if (sum == total) {
                    onDownloadFinish(progress, fontName, fontPosition)
                }
            }

            file.renameTo(File(savePath)) //改回原来的名字

            fos.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            onDownloadError(fontName, fontPosition, e)
        } finally {
            try {
                inputStream?.close()
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onProgress(progress: Int, fontName: String, fontPosition: Int) {
        EventBus.getDefault().postSticky(Event(fontName, progress, fontPosition, STATUS_DOWNLOADING))
    }

    private fun onDownloadFinish(progress: Int, fontName: String, fontPosition: Int) {
        EventBus.getDefault().postSticky(Event(fontName, progress, fontPosition, STATUS_FINISH))
    }

    private fun onDownloadError(fontName: String, position: Int, t: Throwable) {
        EventBus.getDefault().postSticky(Event(fontName, -1, position, STATUS_ERROR))
    }

    companion object {

        const val FONT_NAME_KEY = "font_name_key"
        const val FONT_POSITION_KEY = "font_position_key"

        const val FONT_DEFAULT = "font_default"
        const val FONT_SIYUAN_SONG = "siyuan_song_font.otf"
        const val FONT_ZHUSHITI = "zhushiti_font.ttf"
        const val FONT_SIYUAN_HEI = "siyuan_hei_font.otf"

        const val STATUS_DOWNLOADING = 0
        const val STATUS_FINISH = 1
        const val STATUS_ERROR = -1

        fun getFontPath(name: String): String {
            return Environment.getExternalStorageDirectory().absolutePath + "/font/" + name
        }

        fun isFontExists(fontName: String): Boolean {
            val path = getFontPath(fontName)
            return File(path).exists()
        }
    }

    data class Event(
            val fontName: String,
            val progress: Int,
            val fontPosition: Int,
            val status: Int = STATUS_DOWNLOADING
    )
}