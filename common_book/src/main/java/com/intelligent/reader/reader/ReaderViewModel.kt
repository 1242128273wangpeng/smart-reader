package com.intelligent.reader.reader

import android.text.TextUtils
import com.intelligent.reader.DisposableAndroidViewModel
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.repository.BookCoverRepository
import com.intelligent.reader.repository.ReaderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.*
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.purchase.SingleChapterBean
import net.lzbook.kit.request.DataCache
import net.lzbook.kit.user.bean.RecommendBooksEndResp
import net.lzbook.kit.utils.NetWorkUtils
import java.util.*

/**
 * @desc 阅读器 View Model
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/20 16:49
 */
class ReaderViewModel : DisposableAndroidViewModel {
    //===============================================ReadDataFactory===================================================
    var toChapterStart: Boolean = false
    //阅读状态
    var readStatus: ReadStatus? = null
    //
    var tempCurrentPage: Int = 0
    var tempPageCount: Int = 0
    var tempSequence: Int = 0
    var tempOffset: Int = 0
    var tempChapterName: String? = null
    //
    var chapterList: ArrayList<Chapter>? = null
    var nextChapter: Chapter? = null
    var preChapter: Chapter? = null
    var currentChapter: Chapter? = null
    //
    var tempChapterNameList: ArrayList<NovelLineBean>? = null
    var tempNextChapter: Chapter? = null
    var tempPreviousChapter: Chapter? = null
    var tempCurrentChapter: Chapter? = null
    var tempLineList: ArrayList<ArrayList<NovelLineBean>>? = null
    //接口回调
    var mReadDataListener: ReadDataListener? = null

    fun saveData() {
        tempCurrentPage = readStatus?.currentPage!!
        tempPageCount = readStatus?.pageCount!!
        tempSequence = readStatus?.sequence!!
        tempOffset = readStatus?.offset!!
        tempChapterName = ReadState.chapterName
        tempChapterNameList = readStatus?.chapterNameList
        tempNextChapter = nextChapter
        tempCurrentChapter = currentChapter
        tempPreviousChapter = preChapter
        tempLineList = readStatus?.mLineList
    }

    fun restore() {
        readStatus?.currentPage = tempCurrentPage
        readStatus?.pageCount = tempPageCount
        readStatus?.sequence = tempSequence
        readStatus?.offset = tempOffset
        ReadState.chapterName = tempChapterName
//        readStatus?.chapterName = tempChapterName
//        readStatus?.mCurrentChapter = currentChapter
        readStatus?.chapterNameList = tempChapterNameList
        nextChapter = tempNextChapter
        currentChapter = tempCurrentChapter
        preChapter = tempPreviousChapter
        readStatus?.mLineList = tempLineList
    }
//===============================================Repository Factory===================================================

    var mReaderRepository: ReaderRepository? = null
    var mBookCoverRepository: BookCoverRepository? = null

    constructor()
    constructor(readerRepository: ReaderRepository) : this() {
        mReaderRepository = readerRepository
    }

    constructor(readerRepository: ReaderRepository, bookCoverRepository: BookCoverRepository) : this() {
        mReaderRepository = readerRepository
        mBookCoverRepository = bookCoverRepository
    }

    private var mReaderRecommendViewCallback: ReaderRecommendViewCallback? = null

    private var mReaderBookSourceViewCallback: ReaderBookSourceViewCallback? = null

    private var mBookChapterViewCallback: BookChapterViewCallback? = null

    private var mBookChapterPayCallback: BookChapterPayCallback? = null


    /**
     * 阅读完结书籍推荐
     */
    fun getBookEndRecommendBook(recommanded: String, bookId: String) {
        addDisposable(mReaderRepository!!.getBookEndRecommendBook(recommanded, bookId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ mReaderRecommendViewCallback?.onRecommendBook(it) },
                        { mReaderRecommendViewCallback?.onRecommendBookFail(it.message) }))
    }

    /**
     * 换源集合
     */
    fun getBookSource(bookId: String) {
        addDisposable(mReaderRepository!!.getBookSource(bookId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ mReaderBookSourceViewCallback?.onBookSource(it) },
                        { mReaderBookSourceViewCallback?.onBookSourceFail(it.message) }))
    }


    /**
     * 获取书籍目录 //复用BookCoverRepositoyFactory
     */
    fun getChapterList(requestItem: RequestItem) {
        if (NetWorkUtils.getNetWorkType(BaseBookApplication.getGlobalContext()) == NetWorkUtils.NETWORK_NONE) {
            val bookChapterDao = BookChapterDao(BaseBookApplication.getGlobalContext(), requestItem.book_id)
            val chapterList = bookChapterDao.queryBookChapter()
            if (chapterList.size != 0) {
                if (mBookChapterViewCallback != null) {
                    mBookChapterViewCallback?.onChapterList(chapterList)
                }
                return
            } else {
                if (mBookChapterViewCallback != null) {
                    mBookChapterViewCallback?.onFail("拉取章节时无网络")
                }
                return
            }
        }

        addDisposable(
                mBookCoverRepository!!.getChapterList(requestItem)
                .doOnNext { chapters ->
                    // 已被订阅则加入数据库
                    mBookCoverRepository?.saveBookChapterList(chapters, requestItem)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ chapters ->
                    if (mBookChapterViewCallback != null) {
                        mBookChapterViewCallback?.onChapterList(chapters)
                    }
                }, { throwable ->
                    if (mBookChapterViewCallback != null) {
                        mBookChapterViewCallback?.onFail(throwable.message.toString())
                    }
                }))
    }

    /**
     * 购买单章
     */
    fun paySingleChapter(sourceId: String?, chapterId: String?, chapterName: String?, uid: String?) {
        addDisposable(mReaderRepository!!.paySingleChapter(sourceId, chapterId, chapterName, uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ chapters ->
                    if (mBookChapterViewCallback != null) {
                        mBookChapterPayCallback?.onPayChapter(chapters)
                    }
                }, { throwable ->
                    if (mBookChapterViewCallback != null) {
                        mBookChapterViewCallback?.onFail(throwable.message.toString())
                    }
                }))

    }

    /**
     * 请求单章
     */
    fun requestSingleChapter(host: String, chapter: Chapter, mBookSingleChapterCallback: BookSingleChapterCallback) {
        addDisposable(mReaderRepository!!.requestSingleChapter(host, chapter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ chapters ->
                    if (!TextUtils.isEmpty(chapters.content)) {
                        chapters.isSuccess = true
                        // 自动切源需要就更新目录
                        if (chapters.flag == 1 && !TextUtils.isEmpty(chapters.content)) {
                            mReaderRepository?.updateBookCurrentChapter(chapters.book_id, chapters, chapters.sequence)
                        }
                    }
                    mReaderRepository?.writeChapterCache(chapters, false)
                    mBookSingleChapterCallback.onPayChapter(chapters)
                }, { throwable ->
                    mBookSingleChapterCallback.onFail(throwable.message.toString())
                }))

    }

    fun getChapterByAuto(what: Int, sequence: Int): Chapter? {
        var sequence = sequence
        if (chapterList == null || chapterList?.isEmpty()!!) {
            chapterList = BookChapterDao(BaseBookApplication.getGlobalContext(), readStatus?.book_id).queryBookChapter()
            return null
        }
        if (sequence < 0) {
            sequence = 0
        } else if (sequence >= chapterList?.size!!) {
            sequence = chapterList?.size!! - 1
        }
        var chapter: Chapter? = chapterList?.get(sequence)
        try {
            val content = DataCache.getChapterFromCache(chapter!!.sequence, chapter.book_id)
            if (!TextUtils.isEmpty(content) && !("null" == content || "isChapterExists" == content)) {
                chapter.content = content
                chapter.isSuccess = true
            } else {
                chapter = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return chapter
    }

    fun getChapter(what: Int, sequence: Int): Chapter? {
        var sq = sequence
        if (chapterList == null || chapterList!!.isEmpty()) {
            chapterList = BookChapterDao(BaseBookApplication.getGlobalContext(), readStatus?.book_id).queryBookChapter()
            return null
        }
        if (sequence < 0) {
            sq = 0
        } else if (sequence >= chapterList!!.size) {
            sq = chapterList!!.size - 1
        }
        var chapter: Chapter? = chapterList!!.get(sq)
        try {
            val content = DataCache.getChapterFromCache(chapter!!.sequence, chapter.book_id)
            if (!TextUtils.isEmpty(content) && !("null" == content || "isChapterExists" == content)) {
                chapter.content = content
                chapter.isSuccess = true
            } else {
                chapter = null
                mReadDataListener?.getChapter(what, sequence)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return chapter
    }

    fun getPreviousChapter(): Chapter? {
        return preChapter
    }

    fun getNextsChapter(): Chapter? {
        return nextChapter
    }

    operator fun next(): Boolean {
        saveData()
        var isPrepared = false
        return isPrepared
    }

    private fun sendPVData() {
        Constants.endReadTime = System.currentTimeMillis() / 1000L
        val params = HashMap<String, String>()
        params.put("book_id", readStatus?.book_id!!)
        val book_source_id: String
        if (Constants.QG_SOURCE == readStatus?.book?.site) {
            book_source_id = readStatus?.book?.book_id!!
        } else {
            book_source_id = readStatus?.book?.book_source_id!!
        }
        params.put("book_source_id", book_source_id)
        if (currentChapter != null) {
            params.put("chapter_id", currentChapter!!.chapter_id)
        }
        val channelCode: String
        if (Constants.QG_SOURCE == readStatus?.book?.site) {
            channelCode = "1"
        } else {
            channelCode = "2"
        }
        params.put("channel_code", channelCode)
        params.put("chapter_read", "1")
        params.put("chapter_pages", readStatus?.pageCount.toString())
        params.put("start_time", Constants.startReadTime.toString())
        params.put("end_time", Constants.endReadTime.toString())

//        StatisticManager.getStatisticManager().sendReadPvData(params)
    }

    fun nextByAutoRead(): Boolean {
        saveData()
        var isPrepared = false

        return isPrepared
    }

    fun previous(): Boolean {
        saveData()
        var isPrepared = false
        return isPrepared
    }


    interface ReaderRecommendViewCallback {

        fun onRecommendBook(recommanded: RecommendBooksEndResp)

        fun onRecommendBookFail(msg: String?)
    }

    interface ReaderBookSourceViewCallback {

        fun onBookSource(sourceItem: SourceItem)

        fun onBookSourceFail(msg: String?)
    }

    interface BookChapterViewCallback {

        fun onChapterList(chapters: List<Chapter>)

        fun onFail(msg: String)
    }

    interface BookSingleChapterCallback {

        fun onPayChapter(chapter: Chapter)

        fun onFail(msg: String)
    }

    interface BookChapterPayCallback {

        fun onPayChapter(mSingleChapterBean: SingleChapterBean)

        fun onFail(msg: String)
    }

    interface ReadDataListener {
//        fun freshPage()
        fun gotoOver()
        fun showToast(str: Int)
        fun downLoadNovelMore()
        fun initBookStateDeal()
        fun changeChapter()
        fun getChapter(what: Int, sequence: Int)
        fun nextChapterCallBack(b: Boolean)
        fun preChapterCallBack(b: Boolean)
        fun showChangeNetDialog()
    }


    fun setReaderBookSourceViewCallback(readerBookSourceViewCallback: ReaderBookSourceViewCallback) {
        this.mReaderBookSourceViewCallback = readerBookSourceViewCallback
    }

    fun setBookChapterViewCallback(mBookChapterViewCallback: BookChapterViewCallback) {
        this.mBookChapterViewCallback = mBookChapterViewCallback
    }
}