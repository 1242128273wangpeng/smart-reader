package com.dy.reader.data

import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.net.RequestSubscriber
import com.ding.basic.net.rx.SchedulerHelper
import com.dy.reader.ReadMediaManager
import com.dy.reader.Reader
import com.dy.reader.event.EventLoading
import com.dy.reader.event.EventReaderConfig
import com.dy.reader.helper.ReadSeparateHelper
import com.dy.reader.mode.NovelChapter
import com.dy.reader.mode.NovelLineBean
import com.dy.reader.mode.NovelPageBean
import com.dy.reader.page.GLReaderView
import com.dy.reader.page.PageManager
import com.dy.reader.page.Position
import com.dy.reader.repository.ReaderRepository
import com.dy.reader.repository.ReaderRepositoryFactory
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import com.orhanobut.logger.Logger
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.utils.logger.AppLog
import net.lzbook.kit.utils.runOnMain
import net.lzbook.kit.utils.toast.ToastUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by xian on 18-3-21
 */
object DataProvider {
    private var loadFailList = ArrayList<String>()

    init {
        EventBus.getDefault().register(this)
    }

    private val mDisposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onNeedRefresh(event: EventReaderConfig) {
        if (ReaderStatus.isReady()) {
            if (event.type == ReaderSettings.ConfigType.PAGE_REFRESH) {
                loadGroup(ReaderStatus.position.group, false) {
                    groupListeners.forEach {
                        it.onPageRefreshed()
                    }
                }
            } else if (event.type == ReaderSettings.ConfigType.CHAPTER_REFRESH || event.type == ReaderSettings.ConfigType.FONT_REFRESH) {
                if (event.obj == null || ReaderSettings.instance.animation == GLReaderView.AnimationType.LIST) {
                    AppLog.e("DataProvider", "CHAPTER_REFRESH, but obj == null or is list animation")
                    return
                }

                val position = event.obj as Position

                if (isGroupAvalable(position.group)) {

                    chapterCache.removeOther(position.group)

                    val forceReload = event.type == ReaderSettings.ConfigType.FONT_REFRESH
                    if (forceReload) {
                        chapterCache.clear()
                    }

                    if (event.type == ReaderSettings.ConfigType.CHAPTER_REFRESH) {
                        if (forceReload || Math.abs(position.group - ReaderStatus.position.group) > 1 || chapterCache.get(position.group) == null) {
                            EventBus.getDefault().post(EventLoading(EventLoading.Type.START))
                        }
                        loadPre(position.group + 2, position.group + 6)
                    }


                    AppLog.e("DataProvider", "forceReload = " + forceReload)
                    val mediaToken = ReadMediaManager.tonken
                    loadGroup(position.group, forceReload, mediaToken) {
                        if (it) {

                            loadGroup(Math.max(position.group - 1, 0), forceReload, mediaToken) {
                                if (it) {
                                    groupListeners.forEach {
                                        if (event.type == ReaderSettings.ConfigType.CHAPTER_REFRESH) {
                                            it.onGroupRefreshed(position) {
                                                EventBus.getDefault().post(EventLoading(EventLoading.Type.SUCCESS))
                                            }
                                        } else {
                                            it.onFontRefreshed(position) {
                                                EventBus.getDefault().post(EventLoading(EventLoading.Type.SUCCESS))
                                            }
                                        }
                                    }
                                } else {
                                    EventBus.getDefault().post(EventLoading(EventLoading.Type.RETRY) {
                                        onNeedRefresh(event)
                                    })
                                }
                            }

                            loadGroup(Math.min(position.group + 1, ReaderStatus.chapterCount), forceReload, mediaToken)
                        } else {
                            EventBus.getDefault().post(EventLoading(EventLoading.Type.RETRY) {
                                onNeedRefresh(event)
                            })
                        }
                    }
                } else {
                    EventBus.getDefault().post(EventLoading(EventLoading.Type.SUCCESS))
                }
            }
        }
    }

    var countCacheSize: Int = 3

    val chapterCache = DataCache(countCacheSize)

    class DataCache(val maxSize: Int) {
        val map: TreeMap<Int, NovelChapter> = TreeMap()

        fun put(key: Int, novelChapter: NovelChapter) {
            if (map.size >= maxSize) {
                val firstKey = map.firstKey()
                if (firstKey < key) {
                    map.remove(firstKey)
                } else {
                    map.remove(map.lastKey())
                }
            }

            map.put(key, novelChapter)
        }

        fun get(key: Int): NovelChapter? {
            return map[key]
        }

        fun removeOther(key: Int) {
            val oldKeys = map.keys.filter { Math.abs(key - it) > 1 }
            oldKeys.forEach { map.remove(it) }
        }

        fun clear() {
            map.clear()
        }
    }


    interface GroupRefreshListener {
        fun onGroupRefreshed(position: Position, callback: (() -> Unit)? = null)

        fun onFontRefreshed(position: Position, callback: (() -> Unit)? = null)

        fun onPageRefreshed(callback: (() -> Unit)? = null)
    }

    val requesetFactory = RequestRepositoryFactory.loadRequestRepositoryFactory(Reader.context)

    val readerRepository: ReaderRepository = ReaderRepositoryFactory.getInstance()

    val groupListeners = mutableSetOf<GroupRefreshListener>()

    fun prepare(book: Book, start: Int, callback: ((Boolean) -> Unit)?) {
        val index = Math.max(start, 0)
        requesetFactory.requestCatalog(book.book_id, book.book_source_id, book.book_chapter_id, object : RequestSubscriber<List<Chapter>>() {
            override fun requestResult(result: List<Chapter>?) {
                if (result != null) {
                    ReaderStatus.chapterList.clear()
                    ReaderStatus.chapterList.addAll(result)

//                    loadPre(ReaderStatus.position.group + 2, ReaderStatus.position.group + 6)
                    val sourceArr = mutableListOf<Flowable<Chapter>>()
                    for (i in index until Math.min(index + 3, ReaderStatus.chapterList.size)) {
                        sourceArr.add(readerRepository.requestSingleChapter(ReaderStatus.chapterList[i]))
                    }

                    mDisposable.add(Flowable.zipArray<Chapter, List<Chapter>>(Function { it ->
                        val list = ArrayList<Chapter>((it.toList() as List<Chapter>))
                        Collections.sort(list, { first, second ->
                            (first.sequence - second.sequence)
                        })

                        list
                    }, true, Flowable.bufferSize(),
                            *sourceArr.toTypedArray())
                            .subscribeBy(
                                    onNext = {

                                        ReadMediaManager.tonken++
                                        ReadMediaManager.clearAllAd()

                                        it.forEach {
                                            var separateContent = ReadSeparateHelper.initTextSeparateContent(it, it.name
                                                    ?: "")
                                            separateContent = ReadMediaManager.insertChapterAd(it.sequence, ReadMediaManager.tonken, separateContent)
                                            chapterCache.put(it.sequence, NovelChapter(it, separateContent))
                                        }

                                        callback?.invoke(true)
                                    },
                                    onError = {
                                        it.printStackTrace()

                                        callback?.invoke(false)
                                        EventBus.getDefault().post(EventLoading(EventLoading.Type.RETRY, {
                                            prepare(book, start, callback)
                                        }))
                                    }
                            ))
                }
            }

            override fun requestError(message: String) {
                Logger.e("请求目录异常： $message")
                if (index < ReaderStatus.chapterList.size && chapterCache.get(ReaderStatus.chapterList[index].sequence) != null) {
                    callback?.invoke(true)
                } else {
                    callback?.invoke(false)
                    EventBus.getDefault().post(EventLoading(EventLoading.Type.RETRY, {
                        prepare(book, start, callback)
                    }))
                }
            }
        }, SchedulerHelper.Type_IO)
    }

    @Synchronized
    fun clear() {
        groupListeners.clear()
        chapterCache.clear()
        ReaderStatus.chapterList.clear()
        mDisposable.clear()
    }

    @Synchronized
    fun clearPreLoad() {
        chapterCache.clear()
        mDisposable.clear()
    }

    fun isGroupExist(group: Int): Boolean {

        var flag = getPageData(group) != null

        println("isGroupExist $group $flag")

        return flag
    }

    private fun getPageData(group: Int): NovelChapter? {
        if (isGroupAvalable(group)) {
            if (group == -1) {
                return NovelChapter(Chapter(),
                        arrayListOf(NovelPageBean(arrayListOf(NovelLineBean().apply { lineContent = "txtzsydsq_homepage\n";this.sequence = -1; }), 1, arrayListOf())))
            }
//            return lruCache.get(ReaderStatus.chapterList[group].sequence.toLong())
            if(group < ReaderStatus.chapterList.size && ReaderStatus.chapterList[group] != null){
                return chapterCache.get(ReaderStatus.chapterList[group].sequence)
            }else{
                return null
            }

        } else {
            return null
        }
    }

    fun queryPosition(book_id: String, group: Int, offset: Int): Position {
        var realGroup = group
        if (!isGroupAvalable(realGroup))
            realGroup = -1

        val position = Position(book_id, realGroup, 0, 1)

        if (realGroup >= 0) {
            val novelChapter = chapterCache.get(ReaderStatus.chapterList[realGroup].sequence)
            if (novelChapter != null && novelChapter.separateList != null) {

                val list = novelChapter.separateList.clone() as ArrayList<NovelPageBean>
                position.groupChildCount = list.size

                position.index = findPageIndexByOffset(offset, list)

                position.offset = offset
            }
        }

        return position
    }

    fun findPageIndexByOffset(offset: Int, separateList: ArrayList<NovelPageBean>): Int {
        return (0 until separateList.size).firstOrNull { isOffsetInPage(offset, it, separateList) }
                ?: 0
    }

    private fun isOffsetInPage(offset: Int, index: Int, separateList: ArrayList<NovelPageBean>): Boolean {
        if (offset == 0) {
            return true
        }
        if (separateList.size > index + 1) {
            return if (separateList[index].offset == separateList[index + 1].offset) {
                separateList[index].offset == offset
            } else {
                (separateList[index].offset <= offset) && (separateList[index + 1].offset > offset)
            }
        } else if (separateList.size > index) {
            return separateList[index].offset <= offset
        }
        return false
    }

    @Synchronized
    fun revisePosition(position: Position) {

        if (position.group == -1) {
            position.groupChildCount = 1
            position.index = 0
        }

        val page = getPageData(position.group)

        if (page != null) {

            if (position.index == -1 || position.index >= page!!.separateList.size) {
                position.index = page!!.separateList.size - 1
            }

            position.groupChildCount = page!!.separateList.size

        }
    }

    fun loadGroupWithBusyUI(book_id: String, group: Int, callback: ((Boolean) -> Unit)? = null) {
        EventBus.getDefault().post(EventLoading(EventLoading.Type.START))
        loadGroup(group, true) {
            if (it) {
                EventBus.getDefault().post(EventLoading(EventLoading.Type.SUCCESS))

                callback?.invoke(true)
            } else {
                EventBus.getDefault().post(EventLoading(EventLoading.Type.RETRY) {
                    loadGroupWithBusyUI(book_id, group)
                })

                callback?.invoke(false)
            }
        }
    }

    fun loadGroupWithVertical(group: Int, callback: ((Boolean, Chapter?) -> Unit)? = null) {
        loadGroupForVertical(group) { success: Boolean, chapter: Chapter? ->
            if (success) {
                callback?.invoke(true, chapter)
            } else {
                callback?.invoke(false, null)
            }
        }
    }

    fun getPage(position: Position): NovelPageBean {
        val curPosition = PageManager.currentPage.position

        if (position.index == 0 && position.group >= 0) {
            loadPre(position.group + 2, position.group + 6)
            checkReadChapter(position.group - 6, position.group - 1)
        }

        //在加载下一页
        if (position.group > curPosition.group ||
                (position.group == curPosition.group && position.index > curPosition.index)) {
            if (curPosition.index == 0 && getPageData(curPosition.group + 1) == null) {
                loadGroup(curPosition.group + 1, false)
            }
        }
        //在加载上一页
        if (position.group < curPosition.group ||
                (position.group == curPosition.group && position.index < curPosition.index)) {
            if (curPosition.index == curPosition.groupChildCount - 1 && getPageData(curPosition.group - 1) == null) {
                loadGroup(curPosition.group - 1, false)
            }
        }

        //这里不做效验, 在其他的位置一定是验证过的
        val novelPageBean = getPageData(position.group)!!.separateList.get(position.index)
        position.offset = novelPageBean.offset
        return novelPageBean
    }

    fun checkReadChapter(start: Int, end: Int) {
        val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(ReaderStatus.book.book_id)
        if (book != null && !ReaderStatus.chapterList.isEmpty()) {
            val startIndex = Math.max(start, 0)
            val endIndex = Math.min(end, ReaderStatus.chapterCount)
            for (i in startIndex until endIndex) {
                if (i < ReaderStatus.chapterCount) {
                    val requestChapter: Chapter? = ReaderStatus.chapterList[i]
                    if (requestChapter != null && requestChapter.defaultCode == 1) {
                        Logger.e("向前检查章节异常: ${requestChapter.name} : ${requestChapter.defaultCode}")
                        mDisposable.add(readerRepository.requestSingleChapter(requestChapter)
                                .subscribeOn(Schedulers.io())
                                .subscribe({
                                    if (it.defaultCode == 0) {
                                        ToastUtil.showToastMessage(it.name + "内容已修复！")
                                    }
                                }))
                    }
                }
            }
        }
    }

    fun loadPre(start: Int, end: Int) {
        if ((RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(ReaderStatus.book.book_id) != null) && !ReaderStatus.chapterList.isEmpty()) {
            val startIndex = Math.max(start, 0)
            val endIndex = Math.min(end, ReaderStatus.chapterCount)
            for (i in startIndex until endIndex) {
                if (i < ReaderStatus.chapterCount) {
                    val requestChapter: Chapter? = ReaderStatus.chapterList[i]
                    if (requestChapter != null) {
                        mDisposable.add(readerRepository.requestSingleChapter(requestChapter)
                                .subscribeOn(Schedulers.io())
                                .subscribe())
                    }
                }
            }
        }
    }

    private fun loadGroup(group: Int, force: Boolean = true, mediaToken: Long = ReadMediaManager.tonken, callback: ((Boolean) -> Unit)? = null) {
        if (isGroupAvalable(group)) {
            if (group < 0 || (!force && chapterCache.get(group) != null)) {
                AppLog.e("DataProvider", "loadGroup group loaded")
                callback?.invoke(true)
                return
            }
            mDisposable.add(readerRepository.requestSingleChapter(ReaderStatus.chapterList[group])
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(
                            onNext = {

                                AppLog.e("DataProvider", "onNext = ")
                                var separateContent = ReadSeparateHelper.initTextSeparateContent(it, it.name
                                        ?: "")
                                separateContent = ReadMediaManager.insertChapterAd(group, mediaToken, separateContent)
                                chapterCache.put(it.sequence, NovelChapter(it, separateContent))
                                if (loadFailList.contains(ReaderStatus.chapterList[group].chapter_id)) {
                                    PageManager.refreshLeftAndRightPage()
                                    loadFailList.remove(ReaderStatus.chapterList[group].chapter_id)
                                }

                                callback?.invoke(true)
                            },
                            onError = {
                                AppLog.e("DataProvider", "onError = ")
                                it.printStackTrace()
                                if (!loadFailList.contains(ReaderStatus.chapterList[group].chapter_id)) {
                                    loadFailList.add(ReaderStatus.chapterList[group].chapter_id)
                                }
                                callback?.invoke(false)
                            }
                    ))
        } else {
            AppLog.e("DataProvider", "isGroupAvalable = false")
            callback?.invoke(false)
        }

    }

    private fun loadGroupForVertical(group: Int, callback: ((Boolean, Chapter?) -> Unit)? = null) {
        if (isGroupAvalable(group)) {
            val index = if (group < 0) 0 else group
            mDisposable.add(readerRepository.requestSingleChapter(ReaderStatus.chapterList[group])
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(
                            onNext = {
                                var separateContent = ReadSeparateHelper.initTextSeparateContent(it, it.name
                                        ?: "")
                                separateContent = ReadMediaManager.insertChapterAd(index, ReadMediaManager.tonken, separateContent)
                                chapterCache.put(it.sequence, NovelChapter(it, separateContent))
                                runOnMain {
                                    callback?.invoke(true, it)
                                }
                            },
                            onError = {
                                it.printStackTrace()
                                runOnMain {
                                    callback?.invoke(false, null)
                                }
                            }
                    ))
        } else {
            callback?.invoke(false, null)
        }

    }

    fun isGroupAvalable(group: Int) =
            group >= -1 && group < ReaderStatus.chapterList.size

    @Synchronized
    fun isCacheExistBySequence(sequence: Int): Boolean {
        if (sequence == -1 || sequence == -2) {
            return false
        }
        if (ReaderStatus.chapterList.size > 0 && sequence <= ReaderStatus.chapterList.size - 1) {
            return requesetFactory.isChapterCacheExist(ReaderStatus.chapterList[sequence])
        } else {
            return false
        }
    }

    fun findCurrentPageNovelLineBean(): List<NovelLineBean>? {
        val novelChapter = chapterCache.get(ReaderStatus.position.group)
        return if (novelChapter != null && ReaderStatus.position.index < novelChapter.separateList.size) {
            novelChapter.separateList[ReaderStatus.position.index].lines
        } else {
            null
        }
    }
}