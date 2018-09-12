package com.dy.reader.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Environment
import com.ding.basic.repository.InternetRequestRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
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

    var onProgressChange: ((progress: Int, fontPosition: Int) -> Unit)? = null

    var onDownloadError: ((t: Throwable) -> Unit)? = null

    private var curDownloadFile: File? = null

    var fontProgressMap: HashMap<String, Int>? = null
        get() {
            if (field == null) {
                field = HashMap()
            }
            return field
        }

    fun start(context: Context, fontName: String, position: Int) {
        if (fontProgressMap?.containsKey(fontName) == true) return

        val intent = Intent(context, FontDownLoadService::class.java)
        intent.putExtra(FONT_NAME_KEY, fontName)
        intent.putExtra(FONT_POSITION_KEY, position)
        context.startService(intent)

        EventBus.getDefault().postSticky(Event(0, position, STATUS_DOWNLOADING))
        fontProgressMap?.put(fontName, 0)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        val fontName = intent.getStringExtra(FONT_NAME_KEY)
        if (fontName?.isEmpty() == true) return
        val fontPosition = intent.getIntExtra(FONT_POSITION_KEY, 0)

        InternetRequestRepository.loadInternetRequestRepository(this)
                .downloadFont(fontName)
                .observeOn(AndroidSchedulers.mainThread())
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
            val buf = ByteArray(2048)
            var len = -1
            var progress: Int

            // 储存下载文件的目录
            val savePath = getFontPath(fontName)
            val total = it.contentLength()
            curDownloadFile = File(savePath)
            loge("create file: $savePath")

            curDownloadFile?.parentFile?.let {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }

            if (curDownloadFile?.exists() == false) {
                curDownloadFile?.createNewFile()
            } else {
                curDownloadFile?.delete()
            }

            fos = FileOutputStream(curDownloadFile)
            var sum = 0L
            inputStream.read(buf) != 1

            while ({ len = inputStream.read(buf);len }() != -1) {
                fos.write(buf, 0, len)
                sum += len
                progress = (sum / total.toFloat() * 100).toInt()
                onProgress(progress, fontName, fontPosition)
            }

            fos.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            onDownloadError(fontName, fontPosition, e)
        } finally {
            inputStream?.close()
            fos?.close()
        }
    }

    private var lastUpdateTime = 0L

    private fun onProgress(progress: Int, fontName: String, fontPosition: Int) {
        fontProgressMap?.put(fontName, progress)
        val curTime = System.currentTimeMillis()
        if (curTime - lastUpdateTime > 500) {
            EventBus.getDefault().postSticky(Event(progress, fontPosition, STATUS_DOWNLOADING))
            lastUpdateTime = curTime
        }
        if (progress >= 99) {
            fontProgressMap?.remove(fontName)
            EventBus.getDefault().postSticky(Event(progress, fontPosition, STATUS_FINISH))
        }
    }

    private fun onDownloadError(fontName: String, position: Int, t: Throwable) {
        curDownloadFile?.delete()
        fontProgressMap?.remove(fontName)
        EventBus.getDefault().postSticky(Event(-1, position, STATUS_ERROR))
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

    override fun onDestroy() {
        super.onDestroy()
        fontProgressMap = null
    }

    data class Event(
            val progress: Int,
            val fontPosition: Int,
            val status: Int = STATUS_DOWNLOADING
    )
}