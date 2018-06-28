package com.dy.reader.presenter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.ding.basic.bean.*
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.SharedPreUtil
import com.dy.reader.setting.ReaderStatus
import com.orhanobut.logger.Logger
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.db.help.ChapterDaoHelper
import net.lzbook.kit.utils.ATManager
import java.lang.ref.WeakReference
import java.util.*

class BookEndPresenter(var activity: Activity, val contract: BookEndContract) {

    private var bookEndContractReference: WeakReference<BookEndContract>? = null

    var sourceList = ArrayList<Source>()

    private var recommendList = ArrayList<RecommendBean>()

    private var recommendBookList = ArrayList<RecommendBean>()

    private var recommendIndex = 0

    private val sharePreUtil: SharedPreUtil by lazy {
        SharedPreUtil(SharedPreUtil.SHARE_ONLINE_CONFIG)
    }

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

    /***
     * 获取封面页推荐书籍
     * **/
    fun requestRecommend(book_id: String) {
        val bookIDs: String = loadBookShelfID()
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestBookRecommend(book_id, bookIDs, object : RequestSubscriber<RecommendBooks>() {
            override fun requestResult(result: RecommendBooks?) {
                if (result != null) {
                    handleRecommendBooks(result)
                    changeRecommendBooks()
                } else {
                    contract.showRecommend(null)
                }
            }

            override fun requestError(message: String) {
                Logger.e("获取封面推荐异常！")
                contract.showRecommend(null)
            }
        })
    }

    fun handleRecommendBooks(recommendBooks: RecommendBooks?) {
        if (recommendBooks != null) {

            recommendBookList.clear()

            val scale = sharePreUtil.getString(SharedPreUtil.RECOMMEND_BOOKCOVER, "3,3,0").split(",")

            var znScale = 0
            var qgScale = 0
            var feeScale = 0

            if (scale.isNotEmpty()) {
                znScale = Integer.parseInt(scale[0])
            }

            if (scale.size > 1) {
                qgScale = Integer.parseInt(scale[1])
            }

            if (scale.size > 2) {
                feeScale = Integer.parseInt(scale[2])
            }

            var znList: ArrayList<List<RecommendBean>>? = null
            if (znScale > 0 && recommendBooks.znList != null && recommendBooks.znList!!.size > 0) {
                znList = subRecommendList(recommendBooks.znList!!, znScale)
            }

            var qgList: ArrayList<List<RecommendBean>>? = null
            if (qgScale > 0 && recommendBooks.qgList != null && recommendBooks.qgList!!.size > 0) {
                qgList = subRecommendList(recommendBooks.qgList!!, qgScale)
            }

            var feeList: ArrayList<List<RecommendBean>>? = null
            if (feeScale > 0 && recommendBooks.feeList != null && recommendBooks.feeList!!.size > 0) {
                feeList = subRecommendList(recommendBooks.feeList!!, feeScale)
            }

            var count = 0

            if (znList != null && znList.size > 0 && qgList != null && qgList.size > 0 && feeList != null && feeList.size > 0) {
                count = Math.min(znList.size, Math.min(qgList.size, feeList.size))
            } else if (qgList != null && qgList.size > 0 && feeList != null && feeList.size > 0) {
                count = Math.min(qgList.size, feeList.size)
            } else if (znList != null && znList.size > 0 && qgList != null && qgList.size > 0) {
                count = Math.min(znList.size, qgList.size)
            } else if (znList != null && znList.size > 0 && feeList != null && feeList.size > 0) {
                count = Math.min(znList.size, feeList.size)
            } else if (znList != null && znList.size > 0) {
                count = znList.size
            } else if (qgList != null && qgList.size > 0) {
                count = qgList.size
            } else if (feeList != null && feeList.size > 0) {
                count = feeList.size
            }

            for (i in 0 until count) {
                if (znList != null && i < znList.size) {
                    recommendBookList.addAll(znList[i])
                }

                if (qgList != null && i < qgList.size) {
                    recommendBookList.addAll(qgList[i])
                }

                if (feeList != null && i < feeList.size) {
                    recommendBookList.addAll(feeList[i])
                }
            }
            znList?.clear()
            qgList?.clear()
            feeList?.clear()
        }
    }

    private fun subRecommendList(recommends: ArrayList<RecommendBean>, scale: Int): ArrayList<List<RecommendBean>> {

        val result = ArrayList<List<RecommendBean>>()
        var list = ArrayList<RecommendBean>()

        for (i in 0 until recommends.size) {
            list.add(recommends[i])

            if (list.size == scale) {
                result.add(list)
                list = ArrayList()
            }
        }

        return result
    }

    fun changeRecommendBooks() {

        if (recommendIndex + 6 > recommendBookList.size) {
            recommendIndex = 0
        }

        recommendList.clear()

        for (i in recommendIndex until recommendIndex + 6) {
            recommendList.add(recommendBookList[i])
        }

        recommendIndex += 6

        contract.showRecommend(recommendList)
    }

    /***
     * 获取书架书籍ID
     * **/
    private fun loadBookShelfID(): String {
        val books = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBooks()

        if (books != null && books.isNotEmpty()) {
            val stringBuilder = StringBuilder()
            for (i in books.indices) {
                val book = books[i]
                stringBuilder.append(book.book_id)
                stringBuilder.append(if (i == books.size - 1) "" else ",")
            }
            return stringBuilder.toString()
        }
        return ""
    }
}