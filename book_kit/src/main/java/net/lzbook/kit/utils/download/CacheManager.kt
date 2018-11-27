package net.lzbook.kit.utils.download

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.LocalBroadcastManager
import android.widget.Toast
import com.ding.basic.bean.Book
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.util.DataCache
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.R
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.bean.BlockingLinkedHashMap
import net.lzbook.kit.bean.BookTask
import net.lzbook.kit.constants.ActionConstants
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.service.DownloadService
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.book.BaseBookHelper
import net.lzbook.kit.utils.file.FileUtils
import net.lzbook.kit.utils.runOnMain
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import java.io.File
import java.io.IOException


/**
 * 缓存管理类
 * Created by Danny on 2017/12/17.
 */
object CacheManager {
    val DOWN_INDEX = "down_index"
    val DOWN_START = "start"
    val DOWN_WIFIAUTO = "wifi_auto"
    val NOTIFY_ID: Int = 10000
    private val MAX_SIZE = 1000

    val app by lazy {
        BaseBookApplication.getGlobalContext()
    }

    val notificationManager by lazy {
        app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Volatile private var downloadService: DownloadService? = null

    val listeners: ArrayList<CallBackDownload> = ArrayList<CallBackDownload>()

    private var workMap: MutableMap<String, BookTask> = BlockingLinkedHashMap<String, BookTask>(MAX_SIZE)


    private val sc = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {
            downloadService = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            try {
                downloadService = (service as DownloadService.MyBinder).service
            } catch (e: Exception) {
            }
            freshBooksAsync(true)
        }
    }

    fun checkService(): Boolean {
        if (downloadService == null) {
            val context = app
            val intent = Intent()
            intent.setClass(context, DownloadService::class.java)
            context.startService(intent)
            context.bindService(intent, sc, Context.BIND_AUTO_CREATE)
            return false
        }
        return true
    }

    object innerListener {

        var lastUpdateTime = 0L

        fun onTaskFailed(book_id: String?, t: Throwable) {
            val bookTask = workMap[book_id]
            if (bookTask != null) {


                saveTaskInfo(bookTask)

                notifyTaskStatusChange()

                val data = HashMap<String, String>()
                data.put("status", "2")
                data.put("reason", t.javaClass.simpleName + ":" + t.message)
                data.put("bookId", bookTask.book.book_id)
                val str = "type"
                val obj = if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_MOBILE) "0" else if (NetWorkUtils.NETWORK_TYPE == 81) "1" else "2"
                data.put(str, obj)
                data.put("cache_tyte", if (bookTask.isFullCache) "全本缓存" else "从当前章缓存")
                data.put("host", bookTask.book.host!!)
                data.put("start_chapterid", bookTask.start_chapterid)
                data.put("end_chapterid", bookTask.end_chapterid)
                data.put("cache_chapters", "" + bookTask.cache_chapters)
                data.put("cache_times", "" + bookTask.cache_times)
                StartLogClickUtil.upLoadEventLog(app, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.CASHERESULT, data)

                bookTask.state = DownloadState.PAUSEED
                bookTask.isAutoState = false

                runOnMain {

                    if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
                        Toast.makeText(app, app.getString(R.string.game_network_none), Toast.LENGTH_SHORT).show()
                    } else if (t is IOException && !FileUtils.checkLeftSpace()) {
                        Toast.makeText(app, app.getString(R.string.tip_space_not_enough), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(app, String.format(app.getString(R.string.toast_cache_paused)
                                , bookTask.book.name), Toast.LENGTH_SHORT).show()
                    }

                    listeners.forEach {
                        it.onTaskFailed(book_id, t)
                    }
                }
            }
        }

        fun onTaskStatusChange(book_id: String) {
            val bookTask = workMap[book_id]
            if (bookTask != null) {
//                saveTaskInfo(bookTask)
                notifyTaskStatusChange()
                runOnMain {

                    if (bookTask.state == DownloadState.PAUSEED && !bookTask.isAutoState) {
                        Toast.makeText(app, String.format(app.getString(R.string.toast_cache_paused)
                                , bookTask.book.name), Toast.LENGTH_SHORT).show()
                    }

                    listeners.forEach {
                        it.onTaskStatusChange(book_id)
                    }
                }
            }
        }

        fun onTaskFinish(book_id: String) {
            val bookTask = workMap[book_id]
            if (bookTask != null) {


                saveTaskInfo(bookTask)

                val data = HashMap<String, String>()
                data.put("status", "1")
                data.put("bookId", bookTask.book.book_id)
                val str = "type"
                val obj = if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_MOBILE) "0" else if (NetWorkUtils.NETWORK_TYPE == 81) "1" else "2"
                data.put(str, obj)
                data.put("cache_tyte", if (bookTask.isFullCache) "全本缓存" else "从当前章缓存")
                data.put("host", bookTask.book.host!!)
                data.put("start_chapterid", bookTask.start_chapterid)
                data.put("end_chapterid", bookTask.end_chapterid)
                data.put("cache_chapters", "" + bookTask.cache_chapters)
                data.put("cache_times", "" + bookTask.cache_times)
                StartLogClickUtil.upLoadEventLog(app, StartLogClickUtil.SYSTEM_PAGE
                        , StartLogClickUtil.CASHERESULT, data)

                notifyTaskStatusChange()



                if (!bookTask.isAutoState && bookTask.state == DownloadState.FINISH) {
                    showFinishNotify(bookTask.book)
                }

                if (bookTask.isFullCache && bookTask.state != DownloadState.FINISH) {

                    val intent = Intent()
                    intent.action = ActionConstants.ACTION_CACHE_COMPLETE_WITH_ERR
                    intent.putExtra(Constants.REQUEST_ITEM, bookTask.book)
                    LocalBroadcastManager.getInstance(BaseBookApplication.getGlobalContext()).sendBroadcast(intent)
                } else if (!bookTask.isAutoState) {
                    runOnMain {
                        Toast.makeText(app, String.format(app.getString(R.string.toast_cache_complete), bookTask.book.name), Toast.LENGTH_SHORT).show()
                    }
                }

                bookTask.isAutoState = false
                bookTask.isWifiAuto = false

                runOnMain {

                    listeners.forEach {
                        it.onTaskFinish(book_id)
                    }
                }
            }
        }

        fun onTaskProgressUpdate(book_id: String) {

            val bookTask = workMap[book_id]
            if (bookTask != null) {
                saveTaskInfo(bookTask)

                if (System.currentTimeMillis() - lastUpdateTime > 500) {
                    lastUpdateTime = System.currentTimeMillis()
                    runOnMain {
                        listeners.forEach {
                            it.onTaskProgressUpdate(book_id)
                        }
                    }
                }
            }
        }

    }

    private fun notifyTaskStatusChange() {
        val intent = Intent()
        intent.action = ActionConstants.ACTION_CACHE_STATUS_CHANGE
        LocalBroadcastManager.getInstance(BaseBookApplication.getGlobalContext()).sendBroadcast(intent)
    }

    private fun showFinishNotify(book: Book) {

        val notifyIntent: Intent?
        try {
            notifyIntent = Intent(app, Class.forName("net.lzbook.kit.ui.activity.GoToCoverOrReadActivity"))
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        notifyIntent.putExtra(Constants.REQUEST_ITEM, book)
        notifyIntent.putExtra(Constants.NOTIFY_ID, Constants.DOWNLOAD)
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(app, Constants.DOWNLOAD, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(app)
        val build = builder.setSmallIcon(R.drawable.icon)
                .setAutoCancel(true)
                .setContentTitle(book.name)
                .setContentText("缓存已经完成，点击查看")
                .setContentIntent(pendingIntent)
                .build()

        NotificationManagerCompat.from(app).notify(Constants.DOWNLOAD, build)
        Constants.DOWNLOAD++

    }


    private fun saveTaskInfo(task: BookTask) {
        val edit = app.getSharedPreferences(DOWN_INDEX + task.book.book_id, 0).edit()
        edit.putInt(DOWN_START + task.book.book_source_id, task.startSequence)
        edit.putInt(DOWN_WIFIAUTO + task.book.book_source_id, if (task.isWifiAuto) 1 else 0)
        edit.apply()
    }

    private fun restroeTaskInfo(book: Book): BookTask {
        val count = book.chapter_count
        val preferences = app.getSharedPreferences(DOWN_INDEX + book.book_id, 0)
        var start: Int
        var state: DownloadState
        var cacheSize: Int
        val progress: Int
        val bookTask: BookTask
        if (preferences.all.isEmpty()) {
            start = app.getSharedPreferences(DOWN_INDEX, 0).getInt(book.book_id, -1)

            if (start > 0) {
                state = DownloadState.PAUSEED
                if (start != -1 && start >= book.chapter_count) {
                    state = DownloadState.FINISH
                } else if (start == -1) {
                    state = DownloadState.NOSTART
                }
                var list: Array<String>? = null
                if (book.fromQingoo()) {
                    list = File(Constants.QG_CACHE_PATH + book.book_id).list()
                } else {
                    list = File(ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book.book_id).list()
                }
                cacheSize = 0
                if (list != null) {
                    cacheSize = (list as Array<*>).size
                }
                progress = if (count != 0) Math.min(cacheSize * 100 / count, 100) else 0
                if (state == DownloadState.FINISH && progress != 100) {
                    state = DownloadState.PAUSEED
                }

                bookTask = BookTask(book, state, start, book.chapter_count)
                bookTask.startSequence = start
                bookTask.progress = progress
                bookTask.isOldTaskProgress = true
                return bookTask
            }
        }

        start = preferences.getInt(DOWN_START + book.book_source_id, -1)
        val wifiauto = preferences.getInt(DOWN_WIFIAUTO + book.book_source_id, 0)
        cacheSize = DataCache.getCacheChapterIDs(book).size
        progress = if (count != 0) Math.min(cacheSize * 100 / count, 100) else 0
//        if (book.chapter_count > 0 && book.chapter_count <= cacheSize) {
//            progress = 100
//        }
        state = DownloadState.PAUSEED
        if (start != -1 && progress == 100) {
            state = DownloadState.FINISH
        } else if (start == -1 && cacheSize == 0) {
            state = DownloadState.NOSTART
        }
        bookTask = BookTask(book, state, start, book.chapter_count)
        bookTask.startSequence = start
        bookTask.progress = progress

        bookTask.isWifiAuto = wifiauto != 0

        return bookTask
    }

    private fun resetTaskInfo(book: Book) {
        val edit = app.getSharedPreferences(DOWN_INDEX + book.book_id, 0).edit()
//        edit.remove(DOWN_START + book.book_source_id)
//        edit.remove(DOWN_WIFIAUTO + book.book_source_id)
        edit.clear()
        edit.apply()

        val editOld = app.getSharedPreferences(DOWN_INDEX, 0).edit()
        editOld.remove(book.book_id)
        editOld.apply()
    }

    fun hasOtherSourceStatus(book: Book): Boolean {

        if (book.fromQingoo()) {
            return false
        }

        val all = app.getSharedPreferences(DOWN_INDEX + book.book_id, 0).getAll()

        all.keys.forEach {
            if (it.startsWith(DOWN_START) && it != (DOWN_START + book.book_source_id)) {
                return true
            }
        }

        return false
    }


    @Synchronized
    fun getBookStatus(book: Book): DownloadState {

        return if (workMap.containsKey(book.book_id)) {
            workMap[book.book_id]!!.state
        } else {
            getBookTask(book).state
        }
    }


    @Synchronized
    fun getBookTask(book: Book): BookTask {
        return workMap[book.book_id] ?: return restroeTaskInfo(book)
    }

    @Synchronized
    fun freshBook(book_id: String, needRefreshProgress: Boolean) {
        checkService()
        val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book_id)
        if (book != null) {
            val bookTask = workMap[book.book_id]

            if (bookTask == null) {
                workMap.put(book_id, restroeTaskInfo(book))
            } else if (bookTask.book.book_source_id != book.book_source_id) {
                if (bookTask.state == DownloadState.DOWNLOADING || bookTask.state == DownloadState.WAITTING) {
                    bookTask.state = DownloadState.PAUSEED
                }
                workMap.put(book_id, restroeTaskInfo(book))
            } else {

                if (needRefreshProgress && bookTask.state != DownloadState.DOWNLOADING && bookTask.state != DownloadState.WAITTING && bookTask.state != DownloadState.WAITTING_WIFI) {

                    workMap.put(book_id, restroeTaskInfo(book))
                } else {
                    //避免其他地方修改书籍信息时不同步的问题
                    //TODO 修复转成kotlin后clone的问题
                    bookTask.book = book
                }
            }
        }
    }

    fun freshBooks(needRefreshProgress: Boolean) {
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBooks()?.forEach {
            freshBook(it.book_id, needRefreshProgress)
        }
    }

    fun freshBooksAsync(needRefreshProgress: Boolean, callback: (() -> Unit)? = null) {
        Observable.create<Boolean> {

            freshBooks(needRefreshProgress)

            it.onNext(true)
            it.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            callback?.invoke()
                        },
                        onError = { it.printStackTrace() }
                )
    }

    @Synchronized
    fun start(book_id: String, start: Int = 0): Boolean {
        if (checkService()) {

            freshBook(book_id, false)

            if (workMap.containsKey(book_id)) {
                val bookTask = workMap[book_id]
                val state = bookTask!!.state
                if (state != DownloadState.WAITTING && state != DownloadState.DOWNLOADING) {
                    bookTask.startSequence = start
                    bookTask.state = DownloadState.WAITTING
                    innerListener.onTaskStatusChange(book_id)
                }
            }

            if(downloadService != null){
                synchronized(downloadService!!.lock) {
                    downloadService!!.lock.notify()
                }
            }

            return true
        }
        return false
    }

    @Synchronized
    fun stop(book_id: String) {
        if (workMap.containsKey(book_id)) {
            val bookTask = workMap[book_id]

            saveTaskInfo(bookTask!!)

            if (bookTask.state == DownloadState.DOWNLOADING || bookTask.state == DownloadState.WAITTING || bookTask.state == DownloadState.WAITTING_WIFI) {
                bookTask.state = DownloadState.PAUSEED
                bookTask.isAutoState = false
                bookTask.isWifiAuto = false
            }
        }

    }

    fun getNextTask(): BookTask? {
        workMap.forEach {
            if (it.value.state == DownloadState.WAITTING) {
                return it.value
            }
        }

        return null
    }

    @Synchronized
    fun remove(book_id: String) {

        resetTask(book_id)

        if (workMap.containsKey(book_id)) {
            val task = workMap.remove(book_id)
            BaseBookHelper.removeChapterCacheFile(task!!.book)
        }

    }

    @Synchronized
    fun resetTask(book_id: String) {
        if (workMap.containsKey(book_id)) {
            val task = workMap[book_id]
            task!!.state = DownloadState.NOSTART
            task.startSequence = 0
            task.progress = 0
            resetTaskInfo(task.book)
        }
    }

    @Synchronized
    fun removeAll() {
        workMap.forEach {
            BaseBookHelper.removeChapterCacheFile(it.value.book)
            resetTask(it.key)
        }

        workMap.clear()
    }

    @Synchronized
    fun pauseAll(state: DownloadState): List<BookTask> {
        val list = mutableListOf<BookTask>()
        workMap.values.forEach {
            if (it.state == DownloadState.DOWNLOADING || it.state == DownloadState.WAITTING) {
                it.state = state
                if (state == DownloadState.WAITTING_WIFI) {
                    it.isWifiAuto = true
                }
                list.add(it)
            }
        }
        return list
    }

    @Synchronized
    fun checkAutoStart() {
        val autoStart = SPUtils.getDefaultSharedBoolean(SPKey.AUTO_UPDATE_CAHCE, true)
        if (checkService()) {

            if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_WIFI) {
                workMap.values.forEach {

                    val count = it.book.chapter_count

                    if (it.state == DownloadState.WAITTING_WIFI) {
                        it.state = DownloadState.WAITTING
                    } else if (autoStart && count > 0 && it.book.sequence + Constants.WIFI_AUTO_CACHE_COUNT > it.startSequence && it.startSequence < count && it.startSequence + Constants.WIFI_AUTO_CACHE_COUNT >= count) {
                        if (it.state != DownloadState.WAITTING && it.state != DownloadState.DOWNLOADING) {
                            it.state = DownloadState.WAITTING
                            it.startSequence -= 1
                            it.isAutoState = true
                        }
                    }
                }
            }

            if(downloadService != null){
                synchronized(downloadService!!.lock) {
                    downloadService!!.lock.notify()
                }
            }
        }

    }

    fun onNetTypeChange() {

        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_MOBILE && (!Constants.isDownloadManagerActivity || !Constants.hadShownMobilNetworkConfirm)) {
            val intent = Intent()
            intent.action = ActionConstants.ACTION_CACHE_WAIT_WIFI
            LocalBroadcastManager.getInstance(BaseBookApplication.getGlobalContext()).sendBroadcast(intent)
        } else if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_WIFI) {
            checkAutoStart()
        }
    }
}