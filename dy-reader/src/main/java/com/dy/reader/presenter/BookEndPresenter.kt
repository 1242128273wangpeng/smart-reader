package com.dy.reader.presenter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.db.help.ChapterDaoHelper
import net.lzbook.kit.utils.ATManager
import java.lang.ref.WeakReference
import java.util.*

class BookEndPresenter(var activity: Activity, contract: BookEndContract) {

    private var bookEndContractReference: WeakReference<BookEndContract>? = null

    var sourceList = ArrayList<Source>()

    init {
        bookEndContractReference = WeakReference(contract)
    }

    /***
     * 获取书籍来源
     * **/
    fun requestBookSource(book: Book) {
        if (Constants.QG_SOURCE == book.host) {
            requestBookEndContract()?.showSourceList(sourceList)
            return
        }

        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestBookSources(book.book_id, book.book_source_id, book.book_chapter_id, object : RequestSubscriber<BookSource>() {
            override fun requestResult(result: BookSource?) {
                if (result != null) {
                    if (result.items != null) {

                        result.items?.forEach {
                            sourceList.add(it)
                        }
                    }
                    requestBookEndContract()?.showSourceList(sourceList)
                } else {
                    requestBookEndContract()?.showSourceList(sourceList)
                }
            }

            override fun requestError(message: String) {
                requestBookEndContract()?.showSourceList(sourceList)
            }

            override fun requestComplete() {
                Logger.v("获取来源列表完成！")
            }
        })
    }

    /***
     * 点击书籍来源
     * **/
    fun clickedBookSource(source: Source) {
        if ((RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(ReaderStatus.book.book_id) != null)) {
            if (source.book_source_id != ReaderStatus.book.book_source_id) {
                startCatalogActivity(source, true)
                return
            }
        }
        startCatalogActivity(source, false)
    }

    /***
     * 跳转书城页面
     * **/
    fun startBookStore() {
        val bundle = Bundle()
        bundle.putInt("position", 1)
        ATManager.exitReading()
        RouterUtil.navigation(activity, RouterConfig.HOME_ACTIVITY, bundle)
    }

    /***
     * 跳转书架页面
     * **/
    fun startBookShelf() {
        val bundle = Bundle()
        bundle.putInt("position", 0)
        ATManager.exitReading()
        RouterUtil.navigation(activity, RouterConfig.HOME_ACTIVITY, bundle)
    }

    /***
     * 跳转进入目录页
     * **/
    private fun startCatalogActivity(source: Source, changeSource: Boolean) {
        var book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(ReaderStatus.book.book_id)

        if (book != null) {
            book.host = source.host
            book.book_source_id = source.book_source_id
            book.book_chapter_id = source.book_chapter_id
        } else {
            book = ReaderStatus.book
            book.host = source.host
            book.book_source_id = source.book_source_id
            book.book_chapter_id = source.book_chapter_id
        }

        CacheManager.stop(ReaderStatus.book.book_id)

        openCategoryPage(book, changeSource)
    }

    /***
     * 进入目录页
     * **/
    private fun openCategoryPage(book: Book, changeSource: Boolean) {
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val bundle = Bundle()
        bundle.putSerializable("cover", book)
        bundle.putString("book_id", book.book_id)
        bundle.putInt("sequence", ReaderStatus.book.sequence)
        bundle.putBoolean("fromCover", true)
        bundle.putBoolean("fromEnd", true)
        bundle.putBoolean("changeSource", changeSource)

        RouterUtil.navigation(activity, RouterConfig.CATALOGUES_ACTIVITY, bundle, flags)
    }

    private fun requestBookEndContract(): BookEndContract? {
        return bookEndContractReference?.get()
    }
}