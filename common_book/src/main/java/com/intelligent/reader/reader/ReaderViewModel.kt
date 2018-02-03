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
    //
    //
    var chapterList: ArrayList<Chapter>? = null
    var nextChapter: Chapter? = null
    var preChapter: Chapter? = null
    //
    //接口回调
    var mReadDataListener: ReadDataListener? = null

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
                .subscribe({
                    mReaderBookSourceViewCallback?.onBookSource(it)
                },
                        {
                            mReaderBookSourceViewCallback?.onBookSourceFail(it.message)
                        }))
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

//    /**
//     * 购买单章
//     */
//    fun paySingleChapter(sourceId: String?, chapterId: String?, chapterName: String?, uid: String?) {
//        addDisposable(mReaderRepository!!.paySingleChapter(sourceId, chapterId, chapterName, uid)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ chapters ->
//                    if (mBookChapterViewCallback != null) {
//                        mBookChapterPayCallback?.onPayChapter(chapters)
//                    }
//                }, { throwable ->
//                    if (mBookChapterViewCallback != null) {
//                        mBookChapterViewCallback?.onFail(throwable.message.toString())
//                    }
//                }))
//
//    }

    /**
     * 请求单章
     */
    fun requestSingleChapter(host: String, chapter: Chapter, mBookSingleChapterCallback: BookSingleChapterCallback) {
        addDisposable(mReaderRepository!!.requestSingleChapter(host, chapter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ chapter ->
                    if (!TextUtils.isEmpty(chapter.content)) {
                        chapter.isSuccess = true
                        // 自动切源需要就更新目录
                        if (chapter.flag == 1 && !TextUtils.isEmpty(chapter.content)) {
                            mReaderRepository?.updateBookCurrentChapter(chapter.book_id, chapter, chapter.sequence)
                        }
                    }
                    mReaderRepository?.writeChapterCache(chapter, false)
                    mBookSingleChapterCallback.onPayChapter(chapter)
                }, { throwable ->
                    mBookSingleChapterCallback.onFail(throwable.message.toString())
                }))

    }


    operator fun next(): Boolean {
        var isPrepared = false
        return isPrepared
    }


    fun previous(): Boolean {
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
        //        fun getChapter(what: Int, sequence: Int)
//        fun nextChapterCallBack(b: Boolean)
//        fun preChapterCallBack(b: Boolean)
        fun showChangeNetDialog()
    }


    fun setReaderBookSourceViewCallback(readerBookSourceViewCallback: ReaderBookSourceViewCallback) {
        this.mReaderBookSourceViewCallback = readerBookSourceViewCallback
    }

    fun setBookChapterViewCallback(mBookChapterViewCallback: BookChapterViewCallback) {
        this.mBookChapterViewCallback = mBookChapterViewCallback
    }
}