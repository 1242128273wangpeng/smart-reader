package com.intelligent.reader.reader

import com.intelligent.reader.DisposableAndroidViewModel
import com.intelligent.reader.repository.ReaderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.SourceItem
import net.lzbook.kit.purchase.SingleChapterBean
import net.lzbook.kit.user.bean.RecommendBooksEndResp
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

    private lateinit var mReaderRepository: ReaderRepository
    private lateinit var mBookCoverRepository: BookCoverRepository

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


    /**
     * 阅读完结书籍推荐
     */
//    fun getBookEndRecommendBook(recommanded: String, bookId: String) {
//        addDisposable(mReaderRepository.getBookEndRecommendBook(recommanded, bookId)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ mReaderRecommendViewCallback?.onRecommendBook(it) },
//                        { mReaderRecommendViewCallback?.onRecommendBookFail(it.message) }))
//    }

    /**
     * 换源集合
     */
    fun getBookSource(bookId: String) {
        addDisposable(mReaderRepository.getBookSource(bookId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mReaderBookSourceViewCallback?.onBookSource(it)
                },
                        {
                            mReaderBookSourceViewCallback?.onBookSourceFail(it.message)
                        }))
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