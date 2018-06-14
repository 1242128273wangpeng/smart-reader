package com.intelligent.reader.presenter.catalogues

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import com.ding.basic.bean.Book
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter
import com.ding.basic.database.helper.BookDataProviderHelper
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import com.ding.basic.rx.SchedulerHelper
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.cover.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.repair_books.RepairHelp
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.utils.BookCoverUtil
import net.lzbook.kit.utils.NetWorkUtils
import java.util.HashMap

class CataloguesPresenter(var activity: Activity, var book: Book, var cataloguesContract: CataloguesContract,
                          onClickListener: View.OnClickListener, private val fromCover: Boolean)
    : BookCoverUtil.OnDownloadState, BookCoverViewModel.BookChapterViewCallback {

    var chapterList: ArrayList<Chapter> = ArrayList()
    var bookmarkList: ArrayList<Bookmark> = ArrayList()
    val MESSAGE_FETCH_CATALOG = 0
    val MESSAGE_FETCH_BOOKMARK = MESSAGE_FETCH_CATALOG + 1
    val MESSAGE_FETCH_ERROR = MESSAGE_FETCH_BOOKMARK + 1
    private val DELAY_OVERLAY = MESSAGE_FETCH_ERROR + 1

    private var bookCoverUtil: BookCoverUtil? = null
    private var bookCoverViewModel: BookCoverViewModel? = null

    init {
        bookCoverViewModel = BookCoverViewModel()
        bookCoverViewModel?.setBookChapterViewCallback(this)

        bookCoverUtil = BookCoverUtil(activity, onClickListener)
        bookCoverUtil?.registReceiver()
        bookCoverUtil?.setOnDownloadState(this)
    }

    fun requestCatalogList() {
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .requestCatalog(book.book_id, book.book_source_id, book.book_chapter_id, object : RequestSubscriber<List<Chapter>>() {
                    override fun requestResult(result: List<Chapter>?) {
                        if (result != null) {
                            chapterList.clear()
                            chapterList.addAll(result)
                            cataloguesContract.requestCatalogSuccess(chapterList)
                        } else {
                            cataloguesContract.requestCatalogError()
                        }

                        Observable.create<Boolean> {
                            Logger.e("Refresh CacheManager")
                            CacheManager.freshBook(book.book_id, false)
                            it.onNext(true)
                            it.onComplete()
                        }.subscribeOn(Schedulers.io())
                    }

                    override fun requestError(message: String) {
                        cataloguesContract.requestCatalogError()
                    }
                }, SchedulerHelper.Type_Main)
    }

    fun loadBookMark() {
        bookCoverViewModel?.getBookMarkList(book.book_id)
    }

    override fun changeState() {
        cataloguesContract.changeDownloadButtonStatus()
    }

    /**
     * 点击item 进入阅读页
     * isCatalog true 目录点击  false 书签点击
     */
    fun catalogToReading(position: Int, isCatalog: Boolean) {
        val intent = Intent()
        val bundle = Bundle()
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        book.fromType = 1
        if (isCatalog) {
            if (!chapterList.isEmpty()) {
                val tempChapter = chapterList[position]
                val loadFactory = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                if (book.host == Constants.QG_SOURCE) {
                    book.channel_code = 1
                } else {
                    book.channel_code = 2
                }
                if (!loadFactory.isChapterCacheExist(tempChapter) && NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
                    activity.applicationContext.showToastMessage("网络不给力，请稍后重试！")
                    return
                }

                book.sequence = tempChapter.sequence
                book.offset = 0

                val logData = HashMap<String, String>()
                logData["bookid"] = book.book_id
                logData["chapterid"] = tempChapter.chapter_id
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.CATALOG_CATALOGCHAPTER, logData)
            }
        } else {
            val bookmark = bookmarkList[position]
            book.sequence = bookmark.sequence
            book.offset = bookmark.offset
        }
        bundle.putSerializable("book", book)
        bundle.putInt("sequence", book.sequence)
        intent.putExtras(bundle)
        if (fromCover) {
            RouterUtil.navigation(activity, RouterConfig.READER_ACTIVITY, bundle, flags)
        } else {
            activity.setResult(Activity.RESULT_OK, intent)
        }
        activity.finish()

    }

    fun activityResult(sequence: Int) {
        val intent = Intent()
        val bundle = Bundle()
        bundle.putInt("sequence", sequence)
        bundle.putSerializable("book", book)
        intent.putExtras(bundle)
        activity.setResult(Activity.RESULT_OK, intent)
    }

//    fun doDeleteBookmarks(list: ArrayList<Int>) {
//
//        val bookHelper = BookDataProviderHelper.loadBookDataProviderHelper(BaseBookApplication.getGlobalContext())
//        bookHelper.deleteBookMark(list)
//        val marks = bookHelper.getBookMarks(book.book_id!!)
//        if (bookmarkList != null)
//            bookmarkList.clear()
//        if (marks != null && bookmarkList != null) {
//            for (bookmark in marks) {
//                bookmarkList.add(bookmark)
//            }
//        }
//        cataloguesContract.notifyDataChange(true, bookmarkList)
//
//    }

    fun removeHandler() {
        myHandler.removeCallbacksAndMessages(null)
    }

    fun unRegisterRec() {
        if (bookCoverUtil != null) {
            bookCoverUtil!!.unRegisterReceiver()
            bookCoverUtil = null
        }
    }

    fun delayOverLayHandler() {
        cataloguesContract.handOverLay()
    }

    fun onEventReceive(bookmark: Bookmark) {
        val deleteList = ArrayList<Int>()
        deleteList.add(bookmark.id)
        cataloguesContract.deleteBookmarks(deleteList)

    }


    //删除标签

    fun doDeleteBookmarks(list: ArrayList<Int>) {

        val bookHelper = BookDataProviderHelper.loadBookDataProviderHelper(BaseBookApplication.getGlobalContext())
        bookHelper.deleteBookMark(list)
        val marks = bookHelper.getBookMarks(book.book_id!!)
        if (bookmarkList != null)
            bookmarkList.clear()
        if (marks != null && bookmarkList != null) {
            for (bookmark in marks) {
                bookmarkList.add(bookmark)
            }
        }
        cataloguesContract.notifyDataChange(true, bookmarkList)

    }


    //修复
    fun fixBook() {
        RepairHelp.fixBook(activity, book, {
            try {
                RouterUtil.navigation(activity, RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
                activity.finish()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        })
    }


    private val myHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                DELAY_OVERLAY -> cataloguesContract.handOverLay()
            }
        }
    }


    override fun requestBookmarkList(bookmarks: ArrayList<Bookmark>?) {
        if (bookmarks != null) {
            bookmarkList = bookmarks
            cataloguesContract.notifyDataChange(false, bookmarkList)
        }
    }
}