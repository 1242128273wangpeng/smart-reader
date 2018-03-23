package com.intelligent.reader.read

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.intelligent.reader.DisposableAndroidViewModel
import com.intelligent.reader.cover.BookCoverLocalRepository
import com.intelligent.reader.cover.BookCoverOtherRepository
import com.intelligent.reader.cover.BookCoverQGRepository
import com.intelligent.reader.cover.BookCoverRepositoryFactory
import com.intelligent.reader.read.help.ReadSeparateHelper
import com.intelligent.reader.read.mode.NovelChapter
import com.intelligent.reader.read.mode.NovelPageBean
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.read.page.PageAdContainer
import com.intelligent.reader.reader.ReaderOwnRepository
import com.intelligent.reader.reader.ReaderRepositoryFactory
import com.intelligent.reader.repository.BookCoverRepository
import com.intelligent.reader.repository.ReaderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.*
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.net.custom.service.NetService
import net.lzbook.kit.request.RequestFactory
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.OpenUDID
import net.lzbook.kit.utils.runOnMain
import net.lzbook.kit.utils.subscribekt
import java.util.*

/**
 * Created by wt on 2017/12/20.
 */
class DataProvider : DisposableAndroidViewModel(), Observer {

    override fun update(o: Observable?, arg: Any?) {
        if (ReadState.STATE_EVENT.SEQUENCE_CHANGE == arg) {
            val novelChapter = chapterCache.get(ReadState.sequence)
            if (novelChapter != null) {
                novelChapter.separateList.forEach {
                    if (it.adSmallView != null && it.adSmallView is PageAdContainer) {
                        (it.adSmallView as PageAdContainer).load()
                    }
                    if (it.adBigView != null && it.adBigView is PageAdContainer) {
                        (it.adBigView as PageAdContainer).load()
                    }
                }
            }
        }
    }

    var readingActivity: Activity? = null

    inner class DataCache(val maxSize: Int) {
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

        fun clear() {
            map.clear()
        }
    }

    companion object {
        fun getInstance() = Provider.INSTANCE
    }

    private object Provider {
        val INSTANCE = DataProvider()
    }

    var countCacheSize: Int = 3

    val chapterCache = DataCache(countCacheSize)

    //工厂
    var mReaderRepository: ReaderRepository = ReaderRepositoryFactory.getInstance(ReaderOwnRepository.getInstance())

    var mBookCoverRepository: BookCoverRepository = BookCoverRepositoryFactory.getInstance(BookCoverOtherRepository.getInstance(NetService.userService)
            , BookCoverQGRepository.getInstance(OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()))
            , BookCoverLocalRepository.getInstance(BaseBookApplication.getGlobalContext()))


    fun preLoad(start: Int, end: Int) {
        if (BookDaoHelper.getInstance().isBookSubed(ReadState.book_id) && !ReadState.chapterList.isEmpty()) {
            val startIndex = Math.max(start, 0)
            for (i in startIndex until end) {
                if (i < ReadState.chapterCount) {
                    if (!isCacheExistBySequence(i)) {
                        val requestChapter: Chapter = ReadState.chapterList[i]
                        if (RequestFactory.RequestHost.QG.requestHost == ReadState.book.site || !TextUtils.isEmpty(requestChapter.curl)) {
                            addDisposable(mReaderRepository.requestSingleChapter(ReadState.book.site, ReadState.chapterList[i])
                                    .subscribeOn(Schedulers.io())
                                    .subscribekt(onNext = {
                                        mReaderRepository.writeChapterCache(it, ReadState.book)
                                    }, onError = { it.printStackTrace() }))
                        }
                    }
                }
            }
        }
    }


    /**
     * 加载章节
     *  @param mReadDataListener 请求单章后监听
     */
    fun loadChapter(book: Book, sequence: Int, type: ReadViewEnums.PageIndex, mReadDataListener: ReadDataListener) {
        val requestItem = RequestItem.fromBook(book)
        when (type) {
            ReadViewEnums.PageIndex.current -> {
                getChapterList(book, requestItem, sequence, type, mReadDataListener)
            }
            ReadViewEnums.PageIndex.next -> {
                getChapterList(book, requestItem, sequence + 1, type, mReadDataListener)
            }
            ReadViewEnums.PageIndex.previous -> {
                getChapterList(book, requestItem, Math.max(-1, sequence - 1), type, mReadDataListener)
            }
        }
    }

    fun loadChapter2(book: Book, sequence: Int, type: ReadViewEnums.PageIndex, mReadDataListener: ReadDataListener) {
        val requestItem = RequestItem.fromBook(book)
        when (type) {
            ReadViewEnums.PageIndex.current -> {
                preLoad(sequence + 1, sequence + 6)
                getChapterList(book, requestItem, sequence, type, mReadDataListener)
            }
            ReadViewEnums.PageIndex.next -> {
                preLoad(sequence + 1, sequence + 5)
                getChapterList(book, requestItem, sequence, type, mReadDataListener)
            }
            ReadViewEnums.PageIndex.previous -> {
                getChapterList(book, requestItem, sequence, type, mReadDataListener)
            }
        }
    }

    /**
     * 获取段末广告 6-3
     */
    fun loadChapterLastPageAd(context: Context, callback: OnLoadReaderAdCallback) {
//        // 上下滑动模式，横屏无段末广告
//        if (ReadConfig.IS_LANDSCAPE) return
////        loadAd(context, AdMarkPostion.LANDSCAPE_SLIDEUP_POPUPAD, callback)
//
//        PlatformSDK.adapp().dycmNativeAd(context, AdMarkPostion.LANDSCAPE_SLIDEUP_POPUPAD, null, object : AbstractCallback() {
//            override fun onResult(adswitch: Boolean, views: List<ViewGroup>, jsonResult: String?) {
//                super.onResult(adswitch, views, jsonResult)
//                if (!adswitch) {
//                    callback.onFail()
//                    return
//                }
//                try {
//                    val jsonObject = JSONObject(jsonResult)
//                    if (jsonObject.has("state_code")) {
//                        when (ResultCode.parser(jsonObject.getInt("state_code"))) {
//                            ResultCode.AD_REQ_SUCCESS
//                            -> {
//                                callback.onLoadAd(views[0])
//                            }
//                            else -> {
//                                callback.onFail()
//                            }
//                        }
//                    }
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                }
//            }
//        })
    }

    /**
     * 获取章节间广告 5-3 6-3
     */
    fun loadChapterBetweenAd(context: Context, callback: OnLoadReaderAdCallback) {
        val adTyep: String
//        val AdHeight: Int
//        val AdWidth: Int
        if (ReadConfig.IS_LANDSCAPE) {
//            adTyep = AdMarkPostion.LANDSCAPE_SLIDEUP_POPUPAD
//            AdHeight = 1280
//            AdWidth = 1920
        } else {
//            adTyep = AdMarkPostion.SLIDEUP_POPUPAD_POSITION
//            AdHeight = 1920
//            AdWidth = 1280
        }
//        loadAd(context, adTyep, callback)

//        PlatformSDK.adapp().dycmNativeAd(context, adTyep, null, object : AbstractCallback() {
//            override fun onResult(adswitch: Boolean, views: List<ViewGroup>, jsonResult: String?) {
//                super.onResult(adswitch, views, jsonResult)
//                if (!adswitch) {
//                    callback.onFail()
//                    return
//                }
//                try {
//                    val jsonObject = JSONObject(jsonResult)
//                    if (jsonObject.has("state_code")) {
//                        when (ResultCode.parser(jsonObject.getInt("state_code"))) {
//                            ResultCode.AD_REQ_SUCCESS
//                            -> {
//                                callback.onLoadAd(views[0])
//                            }
//                            else -> {
//                                callback.onFail()
//                            }
//                        }
//                    }
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                }
//            }
//        })
    }

    /**
     * 获取书籍目录 //复用BookCoverRepositoyFactory
     */
    fun getChapterList(book: Book, requestItem: RequestItem, sequence: Int, type: ReadViewEnums.PageIndex, mReadDataListener: ReadDataListener) {
        if (ReadState.chapterList.size == 0) {
            val bookChapterDao = BookChapterDao(BaseBookApplication.getGlobalContext(), ReadState.book_id)
            val chapterList = bookChapterDao.queryBookChapter()
            if (ReadState.chapterList.size > 0) {
                ReadState.chapterList.clear()
            }
            ReadState.chapterList.addAll(chapterList)
            preLoad(ReadState.sequence, ReadState.sequence + 6)
        }

        if (ReadState.chapterList.size != 0) {
            requestSingleChapter(book, ReadState.chapterList, sequence, type, mReadDataListener)
        } else {
            addDisposable(
                    mBookCoverRepository.getChapterList(requestItem)
                            .doOnNext { chapters ->
                                // 已被订阅则加入数据库
                                mBookCoverRepository.saveBookChapterList(chapters, requestItem)
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .subscribe({ chapters ->
                                if (ReadState.chapterList.size == 0) {
                                    ReadState.chapterList.addAll(chapters)
                                    preLoad(ReadState.sequence, ReadState.sequence + 6)
                                }
                                if (ReadState.chapterList.size != 0) {
                                    requestSingleChapter(book, ReadState.chapterList, sequence, type, mReadDataListener)
                                } else {
                                    runOnMain {
                                        mReadDataListener.loadDataError("拉取章节时无网络")
                                    }
                                }
                            }, { e ->
                                runOnMain {
                                    mReadDataListener.loadDataError("拉取章节时无网络")
                                }
                                Log.d("DataProvider", "getChapterList e" + e.printStackTrace())
                                e.printStackTrace()
                            }, {
                                Log.d("DataProvider", "getChapterList e" + "onComplete")
                            }, { d ->
                                if (d.isDisposed) {
                                    Log.d("DataProvider", "getChapterList isDisposed" + d.isDisposed)
                                }
                            }))
        }
    }

    private fun requestSingleChapter(book: Book, chapters: List<Chapter>, sequence: Int, type: ReadViewEnums.PageIndex, mReadDataListener: ReadDataListener) {

        val cacheNovelChapter = chapterCache.get(sequence)
        if (cacheNovelChapter != null) {
            runOnMain {
                mReadDataListener.loadDataSuccess(cacheNovelChapter.chapter, type)
            }
            return
        }

        if (sequence < 0) {//封面页
            chapterCache.put(sequence, NovelChapter(Chapter(),
                    arrayListOf(NovelPageBean(arrayListOf(NovelLineBean().apply { lineContent = "txtzsydsq_homepage\n";this.sequence = -1; }), 1, arrayListOf()))))
            runOnMain {
                mReadDataListener.loadDataSuccess(Chapter(), type)
            }
            return
        }


        val chapter = chapters[Math.min(sequence, chapters.size - 1)]

        if(chapter != null){
            addDisposable(mReaderRepository.requestSingleChapter(book.site, chapter)
                    .map {
                        mReaderRepository.writeChapterCache(it, ReadState.book)

                        if (!TextUtils.isEmpty(it.content)) {
                            it.isSuccess = true
                            // 自动切源需要就更新目录
                            if (it.flag == 1 && !TextUtils.isEmpty(it.content)) {
                                mReaderRepository.updateBookCurrentChapter(it.book_id, it, it.sequence)
                            }
                        }

                    if (it.content == "null" || TextUtils.isEmpty(it.content)) {
                        it.content = "文章内容较短，可能非正文，正在抓紧修复中..."
                    }

                        val separateContent = ReadSeparateHelper.initTextSeparateContent(it.content, it.chapter_name)
                        NovelChapter(it, separateContent)
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ novelChapter ->

//                    if (novelChapter.chapter.content != "null" && novelChapter.chapter.content.isNotEmpty()) {
//                        if (ReadState.sequence != -1 && book.site != RequestFactory.RequestHost.QG.requestHost &&
//                                novelChapter.chapter.content.length <= Constants.CONTENT_ERROR_COUNT) {
////                            mReadDataListener.loadDataInvalid("当前章节内容异常，推荐换源。")
//                        }

                    ReadState.chapterId = novelChapter.chapter.chapter_id
                    //加章末广告
                    if (ReadConfig.animation != ReadViewEnums.Animation.list) {
                        loadAd(novelChapter)
                    }
                    chapterCache.put(sequence, novelChapter)
                    mReadDataListener.loadDataSuccess(novelChapter.chapter, type)
//                    } else {
//                        novelChapter.separateList = ReadSeparateHelper.initTextSeparateContent("文章内容较短，可能非正文，正在抓紧修复中...", novelChapter.chapter.chapter_name)
//                        novelChapter.chapter.content = "文章内容较短，可能非正文，正在抓紧修复中..."
//                        mReadDataListener.loadDataSuccess(novelChapter.chapter, type)
////                        mReadDataListener.loadDataError("章节内容为空")
//                    }
                    }, { throwable ->
                        throwable.printStackTrace()
                        mReadDataListener.loadDataError(throwable.message.toString())
                    }))
        }

    }

    @Synchronized
    fun isCacheExistBySequence(sequence: Int): Boolean {

        if (sequence == -1 || sequence == -2) {
            return mBookCoverRepository.isBookSubscribe(ReadState.book_id)
        }

        if (ReadState.chapterList.size > 0 && sequence <= ReadState.chapterList.size - 1) {
            return mReaderRepository.isChapterCacheExist(ReadState.book.site, ReadState.chapterList[sequence])
        } else {
            return false
        }
    }

    private fun getBigAdLayoutParams(): RelativeLayout.LayoutParams {
        val bigAdLayoutParams = RelativeLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
//        val leftMargin = AppUtils.dip2px(ReadState.readingActivity, 10f)
//        val rightMargin = AppUtils.dip2px(ReadState.readingActivity, 10f)t
        val topMargin = AppUtils.dip2px(readingActivity, 30f)
        val bottomMargin = AppUtils.dip2px(readingActivity, 30f)
        bigAdLayoutParams.setMargins(0, topMargin, 0, bottomMargin)
        return bigAdLayoutParams
    }


    private fun loadAd(novelChapter: NovelChapter) {
//        if (readingActivity == null || Constants.isHideAD)
//            return
//
//        PlatformSDK.config().setAd_userid(UserManager.mUserInfo?.uid ?: "")
//        PlatformSDK.config().setChannel_code(Constants.CHANNEL_LIMIT)
//        var cityCode = if (Constants.cityCode.isEmpty()) {
//            0
//        } else {
//            Constants.cityCode.toInt()
//        }
//        PlatformSDK.config().setCityCode(cityCode)
//        PlatformSDK.config().setCityName(Constants.adCityInfo ?: "")
//        PlatformSDK.config().setLatitude(Constants.latitude.toFloat())
//        PlatformSDK.config().setLongitude(Constants.longitude.toFloat())
//
//        val within = PlatformSDK.config().getAdSwitch("5-2") and PlatformSDK.config().getAdSwitch("6-2")
//        val between = PlatformSDK.config().getAdSwitch("5-1") and PlatformSDK.config().getAdSwitch("6-1")
////            val arrayList = chapterSeparate[sequence]
//        val arrayList = novelChapter.separateList
//
//        val last = arrayList.last()
//
//        if (!last.isAd && between) {
//
//            //check small adView
//            val contentHeight = if (last.lines.isNotEmpty()) last.height.toInt() else 0
//            val leftSpace = ReadConfig.screenHeight - contentHeight - (ReadConfig.screenDensity
//                    * ReadConfig.READ_CONTENT_PAGE_TOP_SPACE * 2).toInt() - (ReadConfig.screenDensity * 30).toInt()
//            if (leftSpace >= ReadConfig.screenHeight / 5) {
//
//                last.adSmallView = PageAdContainer(readingActivity!!,
//                        "8-1", ReadConfig.screenWidth
//                        , leftSpace, novelChapter.chapter.sequence == ReadState.sequence)
//            }
//
//            val offset = last.offset + arrayList.last().lines.sumBy { it.lineContent.length } + 1
//
//            val novelPageBean = NovelPageBean(arrayListOf(), offset, arrayListOf()).apply { isAd = true }
//
//            novelPageBean.adBigView = PageAdContainer(readingActivity!!,
//                    if (ReadConfig.IS_LANDSCAPE) "6-1" else "5-1", getBigAdLayoutParams(),
//                    novelChapter.chapter.sequence == ReadState.sequence)
//
//            arrayList.add(novelPageBean)
//        }
//
//        val frequency = PlatformSDK.config().chapter_limit
//
//        if (arrayList.size - 8 > frequency && within) {
//            var count = arrayList.size - 8
//            var index = frequency
//            while (index < count) {
//                if (index % frequency == 0) {
//                    val offset2 = arrayList[index].offset
//
//                    val novelPageBean = NovelPageBean(arrayListOf(), offset2, arrayListOf()).apply { isAd = true }
//
//                    novelPageBean.adBigView = PageAdContainer(readingActivity!!,
//                            if (ReadConfig.IS_LANDSCAPE) "6-2" else "5-2", getBigAdLayoutParams(),
//                            novelChapter.chapter.sequence == ReadState.sequence)
//                    arrayList.add(index, novelPageBean)
//
//                    count++
//                    index++
//
//                    for (j in index until arrayList.size - 1) {
//                        //其他页offset向后偏移 1 length
//                        arrayList[j].offset = arrayList[j].offset + 1
//                    }
//                }
//                index++
//            }
//        }
    }

    fun onReSeparate() {
        val novelChapter = chapterCache.get(ReadState.sequence)
        chapterCache.clear()
        if (ReadState.sequence >= 0 && novelChapter != null) {
            novelChapter.separateList = ReadSeparateHelper.initTextSeparateContent(novelChapter.chapter.content, novelChapter.chapter.chapter_name)
            if (!Constants.isHideAD) {
                loadAd(novelChapter)
            }
            chapterCache.put(ReadState.sequence, novelChapter)
        }
    }

    fun clear() {
        chapterCache.clear()
        unSubscribe()
    }

    abstract class ReadDataListener {

        open fun loadDataSuccess(c: Chapter?, type: ReadViewEnums.PageIndex) {
        }

        open fun loadDataError(message: String) {
        }

        open fun loadDataInvalid(message: String) {

        }
    }

    fun findCurrentPageNovelLineBean(): List<NovelLineBean>? {
        val novelChapter = chapterCache.get(ReadState.sequence)
        if (novelChapter != null) {
            val mNovelPageBean = novelChapter.separateList
            return mNovelPageBean[if (ReadState.currentPage == 0) 0 else ReadState.currentPage - 1].lines
        } else {
            return null
        }
    }

    abstract class OnLoadAdViewCallback(val loadAdBySequence: Int) : OnLoadReaderAdCallback

    interface OnLoadReaderAdCallback {
        fun onLoadAd(adView: ViewGroup)
        fun onFail()
    }
}