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
import com.kyview.InitConfiguration
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.data.bean.*
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.net.custom.service.NetService
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.OpenUDID
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

    //上下文
    var context: Context? = null
    //是否显示广告
    var isShowAd: Boolean = true
    var countCacheSize: Int = 4
//    //分页前缓存容器
//    val chapterMap: HashMap<Int, Chapter> = HashMap()
//    //分页后缓存容器
//    val chapterSeparate: HashMap<Int, ArrayList<NovelPageBean>> = HashMap()
    var chapterKey = arrayListOf<Int>()
    val chapterLruCache: LruCache<Int, NovelChapter> = LruCache(countCacheSize)

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
        if (sequence <= -1) {//封面页
            chapterLruCache.put(sequence, NovelChapter(Chapter(),arrayListOf(NovelPageBean(arrayListOf(NovelLineBean().apply { lineContent = "txtzsydsq_homepage\n";this.sequence = -1; }), 1, arrayListOf()))))
//            chapterSeparate.put(sequence, arrayListOf(NovelPageBean(arrayListOf(NovelLineBean().apply { lineContent = "txtzsydsq_homepage\n";this.sequence = -1; }), 1, arrayListOf())))
//            chapterMap.put(-1, Chapter())
            chapterKey.add(sequence)
            mReadDataListener.loadDataSuccess(Chapter(), type)
            return
        }
        if (NetWorkUtils.getNetWorkType(BaseBookApplication.getGlobalContext()) == NetWorkUtils.NETWORK_NONE) {
            val bookChapterDao = BookChapterDao(BaseBookApplication.getGlobalContext(), requestItem.book_id)
            val chapterList = bookChapterDao.queryBookChapter()
            ReadState.chapterList = chapterList
            if (chapterList.size != 0) {
                requestSingleChapter(book, chapterList, sequence, type, mReadDataListener)
                return
            } else {
                mReadDataListener.loadDataError("拉取章节时无网络")
                return
            }
        }
        addDisposable(
                mBookCoverRepository.getChapterList(requestItem)
                        .doOnNext { chapters ->
                            // 已被订阅则加入数据库
                            mBookCoverRepository.saveBookChapterList(chapters, requestItem)
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe({ chapters ->
                            ReadState.chapterList = chapters as ArrayList<Chapter>
                            if (chapters.size != 0) {
                                requestSingleChapter(book, ReadState.chapterList, sequence, type, mReadDataListener)
                            } else {
                                mReadDataListener.loadDataError("拉取章节时无网络")
                            }
                        }, { e ->
                            e.printStackTrace()
                            mReadDataListener.loadDataError("拉取章节时无网络")
                        }))
    }

    private fun requestSingleChapter(book: Book, chapters: List<Chapter>, sequence: Int, type: ReadViewEnums.PageIndex, mReadDataListener: ReadDataListener) {
        val chapter = chapters[sequence]
        addDisposable(mReaderRepository.requestSingleChapter(book.site, chapter)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ c ->
                    if (!TextUtils.isEmpty(chapter.content)) {
                        c.isSuccess = true
                        // 自动切源需要就更新目录
                        if (c.flag == 1 && !TextUtils.isEmpty(c.content)) {
                            mReaderRepository.updateBookCurrentChapter(c.book_id, c, c.sequence)
                        }
                    }
                    mReaderRepository.writeChapterCache(c, false)
                    if (c.content != "null" && c.content.isNotEmpty()) {
                        ReadState.chapterId = c.chapter_id
                        chapterLruCache.put(sequence, NovelChapter(c,ReadSeparateHelper.initTextSeparateContent(c.content, c.chapter_name)))
                        chapterKey.add(sequence)
                        //加章末广告
                        if (isShowAd) {
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

    private fun loadAd(sequence: Int) {
        var isShowAd = PlatformSDK.config().getAdSwitch("5-1") and PlatformSDK.config().getAdSwitch("5-2") and PlatformSDK.config().getAdSwitch("6-1") and PlatformSDK.config().getAdSwitch("6-2")
        if (isShowAd) {
//            val arrayList = chapterSeparate[sequence]
            val arrayList = chapterLruCache[sequence].separateList
            if (!arrayList.last().isAd) {
                val offset = arrayList.last().offset + arrayList.last().lines.sumBy { it.lineContent.length } + 1
                arrayList.add(NovelPageBean(arrayListOf(), offset, arrayListOf()).apply { isAd = true })
            }
            if (arrayList.size >= 16) {
                val offset2 = arrayList[7].offset + arrayList[7].lines.sumBy { it.lineContent.length } + 1
                arrayList.add(8, NovelPageBean(arrayListOf(), offset2, arrayListOf()).apply { isAd = true; })
                for (i in 9 until arrayList.size - 1) {
                    //其他页offset向后偏移 1 length
                    arrayList[i].offset = arrayList[i - 1].offset + 1
                }
            }
        }
    }

    fun onReSeparate() {
        for (it in chapterKey){
            if (chapterLruCache[it] != null) {
                if (it != -1) {
                    chapterLruCache[it].separateList =  ReadSeparateHelper.initTextSeparateContent(chapterLruCache[it].chapter.content, chapterLruCache[it].chapter.chapter_name)
                    loadAd(it)
                }else {
                    chapterLruCache.put(-1, NovelChapter(Chapter(),arrayListOf(NovelPageBean(arrayListOf(NovelLineBean().apply { lineContent = "txtzsydsq_homepage\n";this.sequence = -1; }), 1, arrayListOf()))))
                    chapterKey.add(-1)
                }
            }
        }
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
        return mNovelPageBean[ReadState.currentPage - 1].lines
    }

    abstract class OnLoadAdViewCallback(val loadAdBySequence: Int) : OnLoadReaderAdCallback

    interface OnLoadReaderAdCallback {
        fun onLoadAd(adView: ViewGroup)
    }

    fun relase() {
        chapterKey.forEach {
            chapterLruCache.remove(it)
        }
        chapterKey.clear()
//        chapterMap.clear()
//        val iter = chapterSeparate.entries.iterator()
//        while (iter.hasNext()) {
//            val chapterList = iter.next().value
//            for (page in chapterList) {
//                if (page.adView != null && page.adView!!.tag != null) {
//                    page.adView!!.tag = null
//                }
//                page.adView = null
//            }
//        }
//        chapterSeparate.clear()
    }
}