package com.intelligent.reader.read

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.LruCache
import android.view.ViewGroup
import com.dycm_adsdk.PlatformSDK
import com.dycm_adsdk.callback.AbstractCallback
import com.dycm_adsdk.callback.ResultCode
import com.dycm_adsdk.constant.AdMarkPostion
import com.intelligent.reader.DisposableAndroidViewModel
import com.intelligent.reader.cover.BookCoverLocalRepository
import com.intelligent.reader.cover.BookCoverOtherRepository
import com.intelligent.reader.cover.BookCoverQGRepository
import com.intelligent.reader.cover.BookCoverRepositoryFactory
import com.intelligent.reader.read.help.ReadSeparateHelper
import com.intelligent.reader.read.mode.NovelChapter
import com.intelligent.reader.read.mode.NovelPageBean
import com.intelligent.reader.read.mode.ReadState
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
import net.lzbook.kit.net.custom.service.NetService
import net.lzbook.kit.user.UserManager
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.OpenUDID
import net.lzbook.kit.utils.runOnMain
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Created by wt on 2017/12/20.
 */
class DataProvider : DisposableAndroidViewModel() {

    companion object {
        fun getInstance() = Provider.INSTANCE
    }

    private object Provider {
        val INSTANCE = DataProvider()
    }

    var countCacheSize: Int = 4
    var chapterKey = arrayListOf<Int>()
    val chapterLruCache: LruCache<Int, NovelChapter> = LruCache(countCacheSize)
    val mBookChapterDao = BookChapterDao(BaseBookApplication.getGlobalContext(), ReadState.book_id)

    //工厂
    var mReaderRepository: ReaderRepository = ReaderRepositoryFactory.getInstance(ReaderOwnRepository.getInstance())

    var mBookCoverRepository: BookCoverRepository = BookCoverRepositoryFactory.getInstance(BookCoverOtherRepository.getInstance(NetService.userService)
            , BookCoverQGRepository.getInstance(OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()))
            , BookCoverLocalRepository.getInstance(BaseBookApplication.getGlobalContext()))

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
                getChapterList(book, requestItem, sequence, type, mReadDataListener)
            }
            ReadViewEnums.PageIndex.next -> {
                getChapterList(book, requestItem, sequence, type, mReadDataListener)
            }
            ReadViewEnums.PageIndex.previous -> {
                getChapterList(book, requestItem, sequence, type, mReadDataListener)
            }
        }
    }

    fun loadAd(context: Context, type: String, callback: OnLoadReaderAdCallback) {

        var adViewHeight = 800

        if (type == AdMarkPostion.READING_MIDDLE_POSITION) {
            adViewHeight = 800
        } else if (type == AdMarkPostion.READING_POSITION) {
            adViewHeight = 1080
        }

        PlatformSDK.adapp().dycmNativeAd(context, type, adViewHeight, 1920, object : AbstractCallback() {
            override fun onResult(adswitch: Boolean, views: List<ViewGroup>, jsonResult: String?) {
                super.onResult(adswitch, views, jsonResult)
                if (!adswitch) {
                    return
                }
                try {
                    val jsonObject = JSONObject(jsonResult)
                    if (jsonObject.has("state_code")) {
                        when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                            ResultCode.AD_REQ_SUCCESS
                            -> {
                                callback.onLoadAd(views[0])
                            }
                            else -> {
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    fun loadAd(context: Context, type: String, w: Int, h: Int, callback: OnLoadReaderAdCallback) {
        PlatformSDK.adapp().dycmNativeAd(context as Activity, type, h, w, object : AbstractCallback() {
            override fun onResult(adswitch: Boolean, views: List<ViewGroup>, jsonResult: String?) {
                super.onResult(adswitch, views, jsonResult)
                if (!adswitch) {
                    return
                }
                try {
                    val jsonObject = JSONObject(jsonResult)
                    if (jsonObject.has("state_code")) {
                        when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                            ResultCode.AD_REQ_SUCCESS
                            -> callback.onLoadAd(views[0])
                            else -> {
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    /**
     * 获取段末广告 6-3
     */
    fun loadChapterLastPageAd(context: Context, callback: OnLoadReaderAdCallback) {
        // 上下滑动模式，横屏无段末广告
        if (ReadConfig.IS_LANDSCAPE) return
//        loadAd(context, AdMarkPostion.LANDSCAPE_SLIDEUP_POPUPAD, callback)

        PlatformSDK.adapp().dycmNativeAd(context, AdMarkPostion.LANDSCAPE_SLIDEUP_POPUPAD, 600, 1080, object : AbstractCallback() {
            override fun onResult(adswitch: Boolean, views: List<ViewGroup>, jsonResult: String?) {
                super.onResult(adswitch, views, jsonResult)
                if (!adswitch) {
                    return
                }
                try {
                    val jsonObject = JSONObject(jsonResult)
                    if (jsonObject.has("state_code")) {
                        when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                            ResultCode.AD_REQ_SUCCESS
                            -> {
                                callback.onLoadAd(views[0])
                            }
                            else -> {
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    /**
     * 获取章节间广告 5-3 6-3
     */
    fun loadChapterBetweenAd(context: Context, callback: OnLoadReaderAdCallback) {
        val adTyep: String
        val AdHeight: Int
        val AdWidth: Int
        if (ReadConfig.IS_LANDSCAPE) {
            adTyep = AdMarkPostion.LANDSCAPE_SLIDEUP_POPUPAD
            AdHeight = 1280
            AdWidth = 1920
        } else {
            adTyep = AdMarkPostion.SLIDEUP_POPUPAD_POSITION
            AdHeight = 1920
            AdWidth = 1280
        }
//        loadAd(context, adTyep, callback)

        PlatformSDK.adapp().dycmNativeAd(context, adTyep, AdHeight, AdWidth, object : AbstractCallback() {
            override fun onResult(adswitch: Boolean, views: List<ViewGroup>, jsonResult: String?) {
                super.onResult(adswitch, views, jsonResult)
                if (!adswitch) {
                    return
                }
                try {
                    val jsonObject = JSONObject(jsonResult)
                    if (jsonObject.has("state_code")) {
                        when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                            ResultCode.AD_REQ_SUCCESS
                            -> {
                                callback.onLoadAd(views[0])
                            }
                            else -> {
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    /**
     * 获取书籍目录 //复用BookCoverRepositoyFactory
     */
    fun getChapterList(book: Book, requestItem: RequestItem, sequence: Int, type: ReadViewEnums.PageIndex, mReadDataListener: ReadDataListener) {
        if (sequence == -1) {//封面页
            chapterLruCache.put(sequence, NovelChapter(Chapter(), arrayListOf(NovelPageBean(arrayListOf(NovelLineBean().apply { lineContent = "txtzsydsq_homepage\n";this.sequence = -1; }), 1, arrayListOf()))))
            chapterKey.add(sequence)
            mReadDataListener.loadDataSuccess(Chapter(), type)
            return
        }
        if (sequence < -1) {
            mReadDataListener.loadDataError("无章节")
            return
        }
        if (ReadState.chapterList.size < 0) {
            val chapterList = mBookChapterDao.queryBookChapter()
            ReadState.chapterList.addAll(chapterList)
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
                                ReadState.chapterList.addAll(chapters)
                                if (ReadState.chapterList.size != 0) {
                                    requestSingleChapter(book, ReadState.chapterList, sequence, type, mReadDataListener)
                                } else {
                                    mReadDataListener.loadDataError("拉取章节时无网络")
                                }
                            }, { e ->
                                mReadDataListener.loadDataError("拉取章节时无网络")
                                e.printStackTrace()
                            }))
        }
    }

    private fun requestSingleChapter(book: Book, chapters: List<Chapter>, sequence: Int, type: ReadViewEnums.PageIndex, mReadDataListener: ReadDataListener) {
        if (sequence < 0 || sequence >= chapters.size) {
            runOnMain {
                mReadDataListener.loadDataError("章节超目录列表")
            }
            return
        }
        val chapter = chapters[sequence]
        addDisposable(mReaderRepository.requestSingleChapter(book.site, chapter)
                .map {
                    mReaderRepository.writeChapterCache(it, false)
                    val separateContent = ReadSeparateHelper.initTextSeparateContent(it.content, it.chapter_name)
                    NovelChapter(it, separateContent)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ mNovelChapter ->
                    val c = mNovelChapter.chapter
                    val separateContent = mNovelChapter.separateList
                    if (!TextUtils.isEmpty(chapter.content)) {
                        c.isSuccess = true
                        // 自动切源需要就更新目录
                        if (c.flag == 1 && !TextUtils.isEmpty(c.content)) {
                            mReaderRepository.updateBookCurrentChapter(c.book_id, c, c.sequence)
                        }
                    }

                    if (c.content != "null" && c.content.isNotEmpty()) {
                        ReadState.chapterId = c.chapter_id
                        chapterLruCache.put(sequence, NovelChapter(c, separateContent))
                        chapterKey.add(sequence)
                        //加章末广告
                        if (!Constants.isHideAD) {
                            loadAd(sequence)
                        }
                        mReadDataListener.loadDataSuccess(c, type)
                    } else {
                        mReadDataListener.loadDataError("章节内容为空")
                    }
                }, { throwable ->
                    mReadDataListener.loadDataError(throwable.message.toString())
                }))
    }

    fun isCacheExistBySequence(sequence: Int): Boolean {
        if (ReadState.chapterList.size > 0) {
            return mReaderRepository.isChapterCacheExist(ReadState.book.site, ReadState.chapterList[sequence])
        } else {
            return false
        }
    }

    private fun loadAd(sequence: Int) {
        PlatformSDK.config().setAd_userid(UserManager.mUserInfo?.uid ?: "")
        PlatformSDK.config().setChannel_code(Constants.CHANNEL_LIMIT)
        var cityCode = if (Constants.cityCode.isEmpty()) {
            0
        } else {
            Constants.cityCode.toInt()
        }
        PlatformSDK.config().setCityCode(cityCode)
        PlatformSDK.config().setCityName(Constants.adCityInfo ?: "")
        PlatformSDK.config().setLatitude(Constants.latitude.toFloat())
        PlatformSDK.config().setLongitude(Constants.longitude.toFloat())

        val within = PlatformSDK.config().getAdSwitch("5-2") and PlatformSDK.config().getAdSwitch("6-2")
        val between = PlatformSDK.config().getAdSwitch("5-1") and PlatformSDK.config().getAdSwitch("5-1")
//            val arrayList = chapterSeparate[sequence]
        val arrayList = chapterLruCache[sequence].separateList
        if (!arrayList.last().isAd && between) {
            val offset = arrayList.last().offset + arrayList.last().lines.sumBy { it.lineContent.length } + 1
            arrayList.add(NovelPageBean(arrayListOf(), offset, arrayListOf()).apply { isAd = true })
        }
        val frequency = PlatformSDK.config().configExpireMinutes
        if (arrayList.size >= frequency * 2 && within) {
            val count = arrayList.size - 2
            for (i in 1 until count) {
                if (i % frequency == 0) {
                    val offset2 = arrayList[i].offset
                    arrayList.add(i, NovelPageBean(arrayListOf(), offset2, arrayListOf()).apply { isAd = true })
                    for (j in i + 1 until arrayList.size - 1) {
                        //其他页offset向后偏移 1 length
                        arrayList[j].offset = arrayList[j].offset + 1
                    }
                }
            }
        }
    }

    fun onReSeparate() {
        for (it in chapterKey) {
            if (chapterLruCache[it] != null) {
                if (it != -1) {
                    chapterLruCache[it].separateList = ReadSeparateHelper.initTextSeparateContent(chapterLruCache[it].chapter.content, chapterLruCache[it].chapter.chapter_name)
                    if (!Constants.isHideAD) {
                        loadAd(it)
                    }
                }
            }
        }
    }

    fun clear() {
        chapterKey.clear()
        chapterLruCache.evictAll()
        unSubscribe()
    }

    abstract class ReadDataListener {
        open fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) {
        }

        open fun loadDataError(message: String) {
        }
    }

    fun findCurrentPageNovelLineBean(): List<NovelLineBean> {
//        var currentNovelLineBean = arrayListOf<NovelLineBean>()
//        var mNovelPageBean = chapterSeparate[ReadState.sequence]
        val mNovelPageBean = chapterLruCache[ReadState.sequence].separateList
        return mNovelPageBean[if (ReadState.currentPage == 0) 0 else ReadState.currentPage - 1].lines
    }

    abstract class OnLoadAdViewCallback(val loadAdBySequence: Int) : OnLoadReaderAdCallback

    interface OnLoadReaderAdCallback {
        fun onLoadAd(adView: ViewGroup)
    }
}