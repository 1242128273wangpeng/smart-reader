package net.lzbook.kit.book.component.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.text.TextUtils
import android.widget.Toast
import com.quduquxie.network.DataService
import com.tencent.mm.opensdk.utils.Log
import net.lzbook.kit.R
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.*
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.BookTask
import net.lzbook.kit.data.bean.CacheTaskConfig
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.encrypt.v17.util.NovelException
import net.lzbook.kit.net.CacheNetApi
import net.lzbook.kit.net.PackageInfo
import net.lzbook.kit.request.DataCache
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.*
import java.io.ByteArrayInputStream
import java.io.IOException


/**
 * Created by Danny on 2017/12/17.
 */
class DownloadService : Service(), Runnable {
    private val NOTIFY_ID_PROGRESS = 20000

    inner class MyBinder : Binder() {
        val service: DownloadService
            get() = this@DownloadService
    }

    override fun onBind(intent: Intent?): IBinder {
        return MyBinder()
    }


    private var shouldExit = false

    val lock = this as Object

    override fun onCreate() {
        super.onCreate()
        val thread = Thread(this)
        thread.priority = Thread.MIN_PRIORITY
        thread.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        shouldExit = true
    }


    override fun run() {

        while (!shouldExit) {
            val task = CacheManager.getNextTask()
            if (task != null) {

                if (task.state != DownloadState.WAITTING) {
                    CacheManager.innerListener.onTaskStatusChange(task.book_id)
                    continue
                }

                task.startSequence = Math.max(0, task.startSequence)
                task.state = DownloadState.DOWNLOADING
                if (task.book_id == null) {
                    Log.e("cache", "cache book_id == null ${task.book}")
                    task.state = DownloadState.PAUSEED
                    continue
                }
                CacheManager.innerListener.onTaskStatusChange(task.book_id)

                if (task.isOldTaskProgress) {
                    val editor = getSharedPreferences(CacheManager.DOWN_INDEX, Context.MODE_PRIVATE).edit()
                    editor.remove(task.book_id)
                    editor.apply()
                    DataCache.deleteOtherSourceCache(task.book)
                }

                val bookChapterDao = BookChapterDao(this, task.book_id)

                if (bookChapterDao.count <= 0) {

                    CacheNetApi.getChapterList(task.book).subscribekt(
                            onNext = { ret ->
                                if (ret.size > 0) {
                                    bookChapterDao.deleteBookChapters(0);
                                    bookChapterDao.insertBookChapter(ret);
                                    downBook(task, ret, bookChapterDao)
                                } else {
                                    CacheManager.innerListener.onTaskFailed(task.book_id,
                                            IllegalArgumentException("server return null chapter list"))
                                }
                            },
                            onError = { t ->
                                t.printStackTrace()
                                CacheManager.innerListener.onTaskFailed(task.book_id, t)
                            }
                    )

                } else {
                    downBook(task, bookChapterDao.queryBookChapter(), bookChapterDao)
                }

            } else {
                synchronized(lock) {
                    try {
                        lock.wait()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        shouldExit = true
                    }
                }
            }
        }
    }

    fun downBook(task: BookTask, chapterList: List<Chapter>, chapterDao: BookChapterDao) {

        if (task.state != DownloadState.DOWNLOADING) {
            CacheManager.innerListener.onTaskStatusChange(task.book_id)
            return
        }

        task.startSequence = Math.min(task.startSequence, chapterList.size - 1)

        task.beginSequence = task.startSequence
        task.endSequence = chapterList.size
        task.cache_times = 0
        task.cache_chapters = 0
        task.processChapterCount = 0
        task.shouldCacheCount = task.endSequence - task.startSequence
        task.isFullCache = task.startSequence == 0
        task.start_chapterid = chapterList[task.startSequence].chapter_id
        task.end_chapterid = chapterList.last().chapter_id

        val chapterMap = mutableMapOf<String, Chapter>()

        chapterList.forEach {
            chapterMap.put(it.chapter_id, it)
        }

        if (!task.isAutoState && !Constants.QG_SOURCE.equals(task.book.site)) {
            CacheNetApi.getTaskConfig(task.book, if (task.startSequence != 0) 1 else 0
                    , chapterList[task.startSequence].chapter_id).subscribekt(
                    onNext = { ret ->
                        if ("20000" == ret.respCode) {

                            //清空上次的内容
                            parsedList.clear()

                            for (url in (ret.data as CacheTaskConfig).fileUrlList) {
                                if (task.state == DownloadState.DOWNLOADING) {
                                    val path = url.substring(url.lastIndexOf("/") + 1)

                                    val info = PackageInfo.parse(path)
                                    if (info == null) {

                                        CacheManager.innerListener.onTaskFailed(task.book_id, IllegalArgumentException("package file name err"))
                                        break
                                    }
                                    if (info.startIndex < task.endSequence && info!!.startIndex < info.endIndex) {

                                        val subList = chapterList.subList(Math.max(info!!.startIndex - 1, 0), Math.min(info!!.endIndex, chapterList.size))

                                        if (!DataCache.isRangeCached(task.book, subList)) {
                                            val start = System.currentTimeMillis()

                                            val list = (ret.data as CacheTaskConfig).fileUrlList
                                            if (!parsePackage(list, info, url, task, chapterMap)) {
                                                CacheManager.innerListener.onTaskFailed(task.book_id, IOException("cant save chapter file"))
                                                return@subscribekt
                                            }

                                            task.cache_times += System.currentTimeMillis() - start
                                            if (task.state == DownloadState.DOWNLOADING) {
                                                task.startSequence = Math.min(info.endIndex, task.endSequence)
                                                task.progress = DataCache.getCacheChapterIDs(task.book).size * 100 / task.endSequence
                                                task.progress = Math.min(100, task.progress)
                                                progressNofitycation(task)
                                                CacheManager.innerListener.onTaskProgressUpdate(task.book_id)
                                            }
                                        } else {
                                            if (task.state == DownloadState.DOWNLOADING) {
                                                task.startSequence = Math.min(info.endIndex, task.endSequence)
                                                task.progress = DataCache.getCacheChapterIDs(task.book).size * 100 / task.endSequence
                                                task.progress = Math.min(100, task.progress)
                                                progressNofitycation(task)
                                                CacheManager.innerListener.onTaskProgressUpdate(task.book_id)
                                            }
                                            parsedList.addAll(subList)
                                            task.processChapterCount += subList.size
                                            task.startSequence += subList.size
                                        }
                                    }

                                } else {
                                    break
                                }
                            }
                            if (task.state == DownloadState.DOWNLOADING) {

                                if (task.processChapterCount >= task.shouldCacheCount && (task.shouldCacheCount != chapterList.size || task.progress == 100)) {
                                    stopForeground(true)
                                    task.startSequence = task.endSequence
                                    if (task.progress == 100) {
                                        task.state = DownloadState.FINISH
                                    } else {
                                        task.state = DownloadState.PAUSEED
                                    }
                                    CacheManager.innerListener.onTaskFinish(task.book_id)
                                    stopForeground(true)
                                } else {

                                    val unCacheList = chapterList.subList(task.beginSequence, chapterList.size).filter {
                                        !parsedList.contains(it)
                                    }
                                    downChapters(task, unCacheList)

                                    parsedList.clear()
                                }
                            } else {
                                CacheManager.innerListener.onTaskStatusChange(task.book_id)
                                stopForeground(true)
                            }
                        } else if ("60001" == ret.respCode) {
                            downChapterOneByOne(task, chapterList, false)
                        } else {

                            CacheManager.innerListener.onTaskFailed(task.book_id, IllegalArgumentException("server err : " + ret.respCode))
                        }
                    },
                    onError = { t ->
                        t.printStackTrace()
                        CacheManager.innerListener.onTaskFailed(task.book_id, t)
                    }
            )
        } else {
            downChapterOneByOne(task, chapterList, false)
        }
    }

    private fun parsePackage(fileUrlList: List<String>, info: PackageInfo, url: String, task: BookTask, chapterMap: Map<String, Chapter>): Boolean {
        if (info != null) {
            val data = HashMap<String, String>()
            val startDownTime = System.currentTimeMillis()
            var bytes: ByteArray? = null
            var tryTimes = 0
            while (tryTimes < RETRY_TIMES) {
                tryTimes++
                try {
                    bytes = getHttpData(url)

                    data.put("STATUS", "1")
                    data.put("bookid", task.book.book_id)
                    data.put("type", if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_MOBILE) "0" else "1")
                    data.put("url1", fileUrlList.get(0))
                    data.put("url2", fileUrlList.last())
                    data.put("url3", url)
                    data.put("starttime", "" + startDownTime);
                    data.put("endtime", "" + System.currentTimeMillis())
                    data.put("times", "" + (System.currentTimeMillis() - startDownTime))
                    StartLogClickUtil.upLoadEventLog(CacheManager.app, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.DOWNLOADPACKE, data)

                    break
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (tryTimes >= RETRY_TIMES) {
                        data.put("STATUS", "2");
                        data.put("reason", e.javaClass.simpleName + ":" + e.message)
                        data.put("bookid", task.book.book_id)
                        data.put("type", if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_MOBILE) "0" else "1")
                        data.put("url1", fileUrlList.get(0))
                        data.put("url2", fileUrlList.last())
                        data.put("url3", url);
                        data.put("starttime", "" + startDownTime)
                        data.put("endtime", "" + System.currentTimeMillis())
                        data.put("times", "" + (System.currentTimeMillis() - startDownTime))
                        StartLogClickUtil.upLoadEventLog(CacheManager.app, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.DOWNLOADPACKE, data)
                        break
                    } else {
                        runOnMain {
                            Toast.makeText(this@DownloadService, R.string.toast_net_weak, Toast.LENGTH_SHORT).show()
                        }
                        synchronized(lock) {
                            try {
                                lock.wait(tryTimes * 1000L)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }


                }
            }

            if (bytes != null) {
                val startParseTime = System.currentTimeMillis()
                try {
                    val z = parse(bytes!!, info, task, chapterMap)
                    val data = HashMap<String, String>()
                    data.put("STATUS", "1")
                    data.put("bookid", task.book.book_id);
                    data.put("type", if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_MOBILE) "0" else "1")
                    data.put("url1", fileUrlList.get(0))
                    data.put("url2", fileUrlList.last())
                    data.put("url3", url)
                    data.put("starttime", "" + startParseTime);
                    data.put("endtime", "" + System.currentTimeMillis())
                    data.put("times", "" + (System.currentTimeMillis() - startParseTime))
                    StartLogClickUtil.upLoadEventLog(CacheManager.app, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.RESOLVEPACKE, data)
                    return z
                } catch (e2: Exception) {
                    e2.printStackTrace()
                    val data = HashMap<String, String>()
                    data.put("STATUS", "2")
                    data.put("reason", e2.javaClass.simpleName + ":" + e2.message)
                    data.put("bookid", task.book.book_id);
                    data.put("type", if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_MOBILE) "0" else "1")
                    data.put("url1", fileUrlList.get(0))
                    data.put("url2", fileUrlList.last())
                    data.put("url3", url);
                    data.put("starttime", "" + startParseTime)
                    data.put("endtime", "" + System.currentTimeMillis())
                    data.put("times", "" + (System.currentTimeMillis() - startParseTime))
                    StartLogClickUtil.upLoadEventLog(CacheManager.app, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.RESOLVEPACKE, data)
                }
            }
        }
        return false;
    }

    val parsedList = ArrayList<Chapter>()

    @Throws(Exception::class)
    private fun parse(bytes: ByteArray, info: PackageInfo, task: BookTask, chapterMap: Map<String, Chapter>): Boolean {
        val inputStream = ByteArrayInputStream(bytes)
        var currentSequence = info.startIndex
        while (inputStream.available() > 0 && task.state == DownloadState.DOWNLOADING) {
            val byteArray = ByteArray(4)
            inputStream.read(byteArray)
            val chapterIDbytes = ByteArray(NumberUtil.byte4ToInt(byteArray))
            inputStream.read(chapterIDbytes)
            val chapterID = String(chapterIDbytes, Charsets.UTF_8)
            inputStream.read(byteArray)
            val contentLength = NumberUtil.byte4ToInt(byteArray)
            if (contentLength > 0) {
                val contentBytes = ByteArray(contentLength)
                if (inputStream.read(contentBytes) == contentLength) {
                    if (currentSequence >= task.startSequence) {
                        if (chapterMap.containsKey(chapterID)) {
                            if (!DataCache.isNewCacheExists(chapterMap[chapterID])) {
                                if (!DataCache.saveEncryptedChapter(GZipUtils.decompress(contentBytes), chapterMap[chapterID] as Chapter)) {
                                    throw IOException("cant save chapter")
                                }

                                task.cache_chapters++
                            }
                            parsedList.add(chapterMap[chapterID]!!)
                            task.processChapterCount++
                        } else {
                            println("package contain invalid chapterID")
                        }
                    }
                    currentSequence++
                } else {
                    throw IOException("readBytes != contentLength")
                }
            }
        }
        return true
    }


    private fun downChapters(task: BookTask, uncacheList: List<Chapter>) {
        val startTime = System.currentTimeMillis()

        for (i in 0 until uncacheList.size) {
            val chapter = uncacheList[i]
            task.startSequence = chapter.sequence
            if (task.state != DownloadState.DOWNLOADING) {
                stopForeground(true)
                CacheManager.innerListener.onTaskStatusChange(task.book_id)
                return
            }


            if (!DataCache.isChapterExists(chapter)) {
                var tryTimes = 0
                while (tryTimes < RETRY_TIMES) {
                    tryTimes++
                    try {
                        val sourceChapter = getSourceChapter(chapter)
                        if (TextUtils.isEmpty(sourceChapter.content)) {
                            sourceChapter.content = "null"
                        }
                        if (!DataCache.saveChapter(sourceChapter.content, sourceChapter)) {
                            throw IOException("cant save chapter")
                        }
                    } catch (e: Exception) {
                        if (tryTimes >= RETRY_TIMES || e is NovelException) {
                            CacheManager.innerListener.onTaskFailed(task.book_id, e)
                            stopForeground(true)
                            return
                        } else {

                            runOnMain {
                                Toast.makeText(this@DownloadService, R.string.toast_net_weak, Toast.LENGTH_SHORT).show()
                            }

                            synchronized(lock) {
                                try {
                                    lock.wait(tryTimes * 1000L)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }

            if (i % 20 == 0) {
                if (task.state == DownloadState.DOWNLOADING) {
                    task.progress = DataCache.getCacheChapterIDs(task.book).size * 100 / task.endSequence
                    task.progress = Math.min(100, task.progress)
                    progressNofitycation(task)
                    CacheManager.innerListener.onTaskProgressUpdate(task.book_id)
                }
            }
        }

        task.cache_times += System.currentTimeMillis() - startTime
        task.cache_chapters += uncacheList.size

        if (task.state == DownloadState.DOWNLOADING) {
            task.progress = DataCache.getCacheChapterIDs(task.book).size * 100 / task.endSequence
            task.progress = Math.min(100, task.progress)

            if (task.progress == 100) {
                task.state = DownloadState.FINISH
            } else {
                task.state = DownloadState.PAUSEED
            }
            CacheManager.innerListener.onTaskFinish(task.book_id)
        }

        stopForeground(true)
    }


    private val DOWN_SIZE = 10

    fun downChapterOneByOne(task: BookTask, chapterList: List<Chapter>, checkMiss: Boolean) {
        if (checkMiss) {
            task.startSequence = task.beginSequence
        }

        while (task.startSequence < task.endSequence) {
            if (task.state != DownloadState.DOWNLOADING) {
                stopForeground(true)
                CacheManager.innerListener.onTaskStatusChange(task.book_id)
                return
            }


            val startTime = System.currentTimeMillis()
            var tryTimes = 0
            while (tryTimes < RETRY_TIMES) {
                tryTimes++

                var tempCacheChapterCount = 0
                var tempChapterCount = 0

                try {

                    //青果
                    if (Constants.QG_SOURCE == task.book.site) {
                        val udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext())
                        val list = BeanParser.buildQGChapterList(ArrayList(chapterList), task.startSequence, DOWN_SIZE)
                        if (list.size > 0) {
                            DataService.getChapterBatch(this@DownloadService, list, list[0], DOWN_SIZE, true, udid)
                            tempCacheChapterCount += list.size
                            tempChapterCount += list.size
                        }
                    } else {
                        for (i in 0 until DOWN_SIZE) {
                            val index = task.startSequence + tempChapterCount
                            if (index < chapterList.size) {
                                if (!DataCache.isChapterExists(chapterList[index])) {
                                    val sourceChapter = getSourceChapter(chapterList[index])
                                    if (TextUtils.isEmpty(sourceChapter.content)) {
                                        sourceChapter.content = "null"
                                    }
//                                    if(DataCache.isChapterContentAvailable(sourceChapter.content)) {
                                    if (!DataCache.saveChapter(sourceChapter.content, sourceChapter)) {
                                        throw IOException("cant save chapter")
                                    }
                                    tempCacheChapterCount++
//                                    }
                                }

                                tempChapterCount++
                            }
                        }
                    }


                    if (task.state == DownloadState.DOWNLOADING) {

                        task.cache_chapters += tempCacheChapterCount
                        task.startSequence += tempChapterCount

                        task.cache_times += System.currentTimeMillis() - startTime
                        task.progress = DataCache.getCacheChapterIDs(task.book).size * 100 / task.endSequence
                        task.progress = Math.min(100, task.progress)
                        progressNofitycation(task)
                        CacheManager.innerListener.onTaskProgressUpdate(task.book_id)
                    }

                    break
                } catch (e: Exception) {
                    e.printStackTrace()

                    if (tryTimes >= RETRY_TIMES || e is NovelException) {
                        CacheManager.innerListener.onTaskFailed(task.book_id, e)
                        stopForeground(true)
                        return
                    } else {

                        runOnMain {
                            Toast.makeText(this@DownloadService, R.string.toast_net_weak, Toast.LENGTH_SHORT).show()
                        }

                        synchronized(lock) {
                            try {
                                lock.wait(tryTimes * 1000L)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                }
            }
        }


        if (task.state == DownloadState.DOWNLOADING) {
            task.progress = DataCache.getCacheChapterIDs(task.book).size * 100 / task.endSequence
            task.progress = Math.min(100, task.progress)

            if (task.progress == 100) {
                task.state = DownloadState.FINISH
            } else {
                task.state = DownloadState.PAUSEED
            }
            CacheManager.innerListener.onTaskFinish(task.book_id)
        }

        stopForeground(true)
    }

    @Throws(Exception::class)
    private fun getSourceChapter(chapter: Chapter): Chapter {
        if (!(chapter == null || TextUtils.isEmpty(chapter.curl))) {
            chapter.content = BaseBookApplication.getGlobalContext().mainExtractorInterface.extract(UrlUtils.buildContentUrl(chapter.curl))
            if (!TextUtils.isEmpty(chapter.content)) {
                chapter.content = chapter.content.replace("\\n", "\n")
                chapter.content = chapter.content.replace("\\n\\n", "\n")
                chapter.content = chapter.content.replace("\\n \\n", "\n")
                chapter.content = chapter.content.replace("\\", "")
            }
        }
        return chapter
    }

    var lastUpdateTime = 0L

    private fun progressNofitycation(task: BookTask) {
        if (System.currentTimeMillis() - lastUpdateTime > 500) {
            lastUpdateTime = System.currentTimeMillis()
            if (!task.isAutoState) {
                val content = "缓存中  " + task.progress + '%'

                var notifyIntent: Intent? = null
                try {
                    notifyIntent = Intent(CacheManager.app, Class.forName("com.intelligent.reader.activity.DownloadManagerActivity"))
                } catch (e: Exception) {
                    e.printStackTrace()
                    return
                }
                notifyIntent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent = PendingIntent.getActivity(CacheManager.app, NOTIFY_ID_PROGRESS
                        , notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)


                val notify = NotificationCompat.Builder(CacheManager.app).setSmallIcon(R.drawable.icon)
                        .setContentTitle(CacheManager.app.getString(R.string.downloadservice_nofify_ticker) + task.book.name + "》")
                        .setContentText(content)
                        .setContentIntent(pendingIntent)
                        .build()
                notify.`when` = System.currentTimeMillis()
                notify.flags = 16
                startForeground(this.NOTIFY_ID_PROGRESS, notify)
            }
        }
    }
}