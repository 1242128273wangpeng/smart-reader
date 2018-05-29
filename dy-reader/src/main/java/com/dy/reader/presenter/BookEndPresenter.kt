package com.dy.reader.presenter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import com.ding.basic.bean.Book
import com.ding.basic.bean.BookSource
import com.ding.basic.bean.Source
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dy.reader.setting.ReaderStatus
import com.orhanobut.logger.Logger
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.book.view.RecommendItemView
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.db.help.ChapterDaoHelper
import net.lzbook.kit.utils.ATManager
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by zhenXiang on 2017\11\21 0021.
 */

class BookEndPresenter(var act: Activity, contract: BookEndContract,
                       var category: String?) {
    var activity: WeakReference<Activity>? = null
    var bookEndContract: WeakReference<BookEndContract>? = null
    var myDialog: MyDialog? = null
    var sourceList = ArrayList<Source>()

    init {
        activity = WeakReference(act)
        bookEndContract = WeakReference(contract)
    }

    //获取书籍来源信息
    fun getBookSource(book: Book) {
        if (Constants.QG_SOURCE == book.host) {
            getEndContract()?.showSource(false, sourceList)
            return
        }
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestBookSources(book.book_id, book.book_source_id, book.book_chapter_id, object : RequestSubscriber<BookSource>() {
            override fun requestResult(result: BookSource?) {
                if (result != null) {
                    val successMessage = Message()
                    successMessage.what = 1
                    successMessage.obj = result
                    handler.sendMessage(successMessage)
                } else {
                    handler.sendEmptyMessage(0)
                }
            }

            override fun requestError(message: String) {
                val failMessage = Message()
                failMessage.what = -144
                failMessage.obj = message
                handler.sendMessage(failMessage)
            }

            override fun requestComplete() {
                Logger.v("获取来源列表完成！")
            }

        })
    }

    //书籍来源列表点击
    fun itemClick(source: Source) {

        if ((RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                        .checkBookSubscribe(ReaderStatus.book.book_id) != null)) {
            if (source.book_source_id != ReaderStatus.book.book_source_id) {
                intoCatalogActivity(source, true)
                return
            }
        }
        intoCatalogActivity(source, false)
    }

    /**
     * 去书城
     */
    fun goToBookStore() {
        val bundle = Bundle()
        bundle.putInt("type_event", 1)
        ATManager.exitReading()
        RouterUtil.navigation(act, RouterConfig.HOME_ACTIVITY, bundle)
    }

    fun goToBookSearchActivity(view: View) {
        if (view is RecommendItemView) {
            val bundle = Bundle()
            bundle.putString("word", view.title)
            bundle.putString("search_type", "0")
            bundle.putString("filter_type", "0")
            bundle.putString("filter_word", "ALL")
            bundle.putString("sort_type", "0")
            RouterUtil.navigation(act, RouterConfig.SEARCH_BOOK_ACTIVITY, bundle)
            return
        }
    }

    /**
     * 去书架
     */
    fun goToShelf() {
        val bundle = Bundle()
        bundle.putInt("type_event", 0)
        ATManager.exitReading()
        RouterUtil.navigation(act, RouterConfig.HOME_ACTIVITY, bundle)
    }

    private fun intoCatalogActivity(source: Source, b: Boolean) {
        if (ReaderStatus.book != null) {
            if ((RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                            .checkBookSubscribe(ReaderStatus.book.book_id) != null)) {
                val iBook = RequestRepositoryFactory
                        .loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                        .loadBook(ReaderStatus.book.book_id)
                if (iBook != null) {
                    iBook.book_source_id = source.book_source_id
                    iBook.book_chapter_id = source.book_chapter_id
                    iBook.host = source.host
                    iBook.last_chapter?.update_time = source.update_time
                    RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                            .updateBook(iBook)
                    ReaderStatus.book = iBook
                }
                if (b) {
                    val bookChapterDao = ChapterDaoHelper
                            .loadChapterDataProviderHelper(BaseBookApplication.getGlobalContext(), ReaderStatus.book.book_id)
                    bookChapterDao.deleteAllChapters()
                }
            } else {
                val iBook = ReaderStatus.book
                iBook.book_source_id = source.book_source_id
                iBook.book_chapter_id = source.book_chapter_id
                iBook.host = source.host
            }
            //dataFactory.chapterList.clear();
            CacheManager.stop(ReaderStatus.book.book_id)
            openCategoryPage()
        }
    }

    //进入阅读页
    private fun openCategoryPage() {
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        val bundle = Bundle()
        bundle.putSerializable("cover", ReaderStatus.book)
        bundle.putString("book_id", ReaderStatus.book.book_id)
        bundle.putInt("sequence", ReaderStatus.book.sequence)
        bundle.putBoolean("fromCover", true)
        bundle.putBoolean("fromEnd", true)

        if (activity != null && activity?.get() != null) {
            RouterUtil.navigation(activity?.get()!!, RouterConfig.CATALOGUES_ACTIVITY, bundle, flags)
        }
    }

    private fun dismissDialog() {
        if (myDialog != null && myDialog!!.isShowing()) {
            myDialog!!.dismiss()
        }
    }


    private fun getEndContract(): BookEndContract? {
        return bookEndContract?.get()
    }


    public var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> {
                    if (msg.obj != null) {
                        var bookSource = msg.obj as BookSource
                        if (bookSource != null && bookSource.items != null) {
                            val count = bookSource.items!!.size
                            if (count != 0) {
                                for (i in 0 until count) {
                                    if (i < 3) {
                                        sourceList!!.add(bookSource.items!![i])
                                    }
                                }
                            }
                        }
                    }
                    getEndContract()?.showSource(true, sourceList)

                }
                0 -> {
                    getEndContract()?.showSource(false, sourceList)
                }
                -144 -> {
                    getEndContract()?.showSource(false, sourceList)
                }
            }
            super.handleMessage(msg)
        }
    }

}
