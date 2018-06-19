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
import kotlinx.android.synthetic.qbmfkdxs.act_catalog.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.utils.BaseBookHelper
import net.lzbook.kit.utils.BookCoverUtil
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import java.util.HashMap

class CataloguesPresenter(private val activity: Activity, private val book: Book,
                          private val cataloguesContract: CataloguesContract,
                          private val onClickListener: View.OnClickListener?,
                          private val fromCover: Boolean) {

    var chapterList: ArrayList<Chapter> = ArrayList()
    private var bookmarkList: ArrayList<Bookmark> = ArrayList()
    private val DELAY_OVERLAY = 3

    private val bookCoverUtil: BookCoverUtil by lazy {
        val util = BookCoverUtil(activity, onClickListener)
        util.registReceiver()
        util.setOnDownloadState {
            cataloguesContract.changeDownloadButtonStatus()
        }
        util.setOnDownLoadService {
            cataloguesContract.changeDownloadButtonStatus()
        }
        util
    }

    private val bookCoverViewModel: BookCoverViewModel by lazy {
        val viewModel = BookCoverViewModel()
        viewModel.setBookChapterViewCallback { bookmarks ->
            if (bookmarks != null) {
                bookmarkList = bookmarks
                cataloguesContract.notifyDataChange(false, bookmarkList)
            }
        }
        viewModel
    }

    fun requestCatalogList(changeSource: Boolean) {

        val requestSubscriber = object : RequestSubscriber<List<Chapter>>() {
            override fun requestResult(result: List<Chapter>?) {
                if (result != null) {
                    chapterList.clear()
                    chapterList.addAll(result)
                    cataloguesContract.requestCatalogSuccess(chapterList)
                } else {
                    cataloguesContract.requestCatalogError()
                }

                Observable.create<Boolean> {
                    CacheManager.freshBook(book.book_id, false)
                    it.onNext(true)
                    it.onComplete()
                }.subscribeOn(Schedulers.io())
            }

            override fun requestError(message: String) {
                cataloguesContract.requestCatalogError()
            }
        }

        if (changeSource) {
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                    .requestBookCatalog(book.book_id, book.book_source_id, book.book_chapter_id, requestSubscriber, SchedulerHelper.Type_Main)
        } else {
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                    .requestCatalog(book.book_id, book.book_source_id, book.book_chapter_id, requestSubscriber, SchedulerHelper.Type_Main)
        }
    }

    fun loadBookMark() {
        bookCoverViewModel.getBookMarkList(book.book_id)
    }

    fun addToBookShelf() {
        val data = HashMap<String, String>()
        val requestFactory = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
        val subscribedBook = requestFactory.checkBookSubscribe(book.book_id)
        if (subscribedBook != null) {
            cataloguesContract.successAddIntoShelf(false)
        } else {
            val insert = requestFactory.insertBook(book)
            if (insert >= 0) {
                data.put("type", "1")
                //添加书架打点
                StatServiceUtils.statAppBtnClick(activity,
                        StatServiceUtils.b_details_click_book_add)
                cataloguesContract.successAddIntoShelf(true)
            }
        }

        StartLogClickUtil.upLoadEventLog(activity,
                StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.CATALOG_CASHEALL, data)
    }

    /**
     * 点击item 进入阅读页
     * isCatalog true 目录点击  false 书签点击
     */
    fun catalogToReading(position: Int, isCatalog: Boolean) {
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

        navigateToReading(bundle, flags)

    }

    fun continueReading() {

        val requestFactory = RequestRepositoryFactory
                .loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
        val bundle = Bundle()
        val subscribedBook = requestFactory.checkBookSubscribe(book.book_id)
        if (subscribedBook != null && book.sequence != -2) {
            bundle.putInt("sequence", book.sequence ?: -2)
            bundle.putInt("offset", book.offset ?: -1)
        } else {
            bundle.putInt("sequence", -1)
        }

        book.fromType = 1// 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)

        if (book.host == Constants.QG_SOURCE) {
            book.channel_code = 1
        } else {
            book.channel_code = 2
        }
        bundle.putSerializable("book", book)

        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        navigateToReading(bundle, flags)
    }

    private fun navigateToReading(bundle: Bundle, flags: Int) {
        if (fromCover) {
            RouterUtil.navigation(activity, RouterConfig.READER_ACTIVITY, bundle, flags)
        } else {
            val intent = Intent()
            intent.putExtras(bundle)
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

    fun startDownload() {
        //全本缓存的点击统计
        StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.b_details_click_all_load)

        val data = HashMap<String, String>()
        data.put("bookid", book.book_id)
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOKCATALOG,
                StartLogClickUtil.CATALOG_CASHEALL, data)

        val requestFactory = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
        val subscribedBook = requestFactory.checkBookSubscribe(book.book_id)
        if (subscribedBook != null) {
            BaseBookHelper.startDownBookTask(activity, book, 0)
        } else {
            val insert = requestFactory.insertBook(book)
            if (insert >= 0) {
                cataloguesContract.successAddIntoShelf(true)
                BaseBookHelper.startDownBookTask(activity, book, 0)
            }
        }
        cataloguesContract.changeDownloadButtonStatus()
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
        bookCoverUtil.unRegisterReceiver()
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
        val marks = bookHelper.getBookMarks(book.book_id)
        bookmarkList.clear()
        for (bookmark in marks) {
            bookmarkList.add(bookmark)
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

}