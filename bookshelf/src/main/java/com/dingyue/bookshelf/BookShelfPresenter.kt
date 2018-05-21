package com.dingyue.bookshelf

import android.app.Activity
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.text.TextUtils
import android.view.ViewGroup
import com.dingyue.bookshelf.contract.BookHelperContract
import com.dingyue.bookshelf.contract.BookShelfADContract
import com.dingyue.contract.CommonContract
import com.dingyue.contract.IPresenter
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.UpdateCallBack
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.BookUpdate
import net.lzbook.kit.data.bean.BookUpdateResult
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.utils.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by qiantao on 2017/11/14 0014
 */
class BookShelfPresenter(override var view: BookShelfView?) : IPresenter<BookShelfView> {

    private var bookDaoHelper: BookDaoHelper = BookDaoHelper.getInstance()

    var iBookList: ArrayList<Book> = ArrayList()

    var updateService: CheckNovelUpdateService? = null

    val updateConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {}

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            try {
                updateService = (service as CheckNovelUpdateService.CheckUpdateBinder).service
                if (updateService != null) {
                    view?.doUpdateBook(updateService!!)
                }
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }
        }
    }

    fun addUpdateTask(updateCallBack: UpdateCallBack) {
        if (bookDaoHelper.booksCount > 0 && updateService != null) {
            val list = bookDaoHelper.booksList
            updateService?.checkUpdate(BookHelperContract.loadBookUpdateTaskData(list, updateCallBack))
        }
    }

    /**
     * 查询书籍列表
     */
    fun queryBookListAndAd(activity: Activity, isShowAD: Boolean, isList: Boolean) {
        val adCount = calculationShelfADCount(isShowAD)

        if (isShowAD && iBookList.isNotEmpty()) {

            if (isList) {
                if (adCount > 0) {
                    requestShelfADs(activity, adCount, true)
                }
            } else {
                when {
                    Constants.book_shelf_state == 1 -> {
                        requestShelfHeaderAD(activity)
                    }

                    Constants.book_shelf_state == 2 -> {
                        if (adCount > 0) {
                            requestShelfADs(activity, adCount, false)
                        }
                    }

                    Constants.book_shelf_state == 3 -> {
                        requestShelfHeaderAD(activity)

                        if (adCount > 0) {
                            requestShelfADs(activity, adCount, false)
                        }
                    }
                }
            }
        }
    }

    /***
     * 刷新书籍列表，并计算广告数量
     * **/
    private fun calculationShelfADCount(isShowAD: Boolean): Int {
        val bookList = bookDaoHelper.booksOnLineList

        iBookList.removeAll {
            it.item_type != 2
        }

        if (bookList.isEmpty()) {
            uiThread {
                view?.onBookListQuery(bookList)
            }
            return 0
        } else {
            Collections.sort(bookList, CommonContract.MultiComparator(Constants.book_list_sort_type))
            iBookList.addAll(bookList)

            uiThread {
                view?.onBookListQuery(bookList)
            }

            return if (isShowAD) {
                val interval = BookShelfADContract.loadBookShelfADInterval()

                if (interval == 0) {
                    0
                } else {
                    bookList.size / interval
                }
            } else {
                0
            }
        }
    }

    /***
     * 请求书架页广告
     * **/
    private fun requestShelfADs(activity: Activity, count: Int, isList: Boolean) {
        BookShelfADContract.loadBookShelAD(activity, count, object : BookShelfADContract.ADCallback {
            override fun requestADSuccess(views: List<ViewGroup>) {

                if (iBookList.isEmpty()) {
                    return
                }

                val interval = BookShelfADContract.loadBookShelfADInterval() + 1

                val range = if (isList) {
                    views.indices
                } else {
                    1 until views.size + 1
                }

                for (i in range) {
                    if (i * interval < iBookList.size) {
                        if (iBookList[i * interval].item_type == 1) {
                            iBookList[i * interval].item_view = views[if (isList) i else i - 1]
                        } else if (iBookList[i * interval].item_type == 0) {
                            val adBook = Book()
                            adBook.item_type = 1
                            adBook.item_view = views[if (isList) i else i - 1]
                            iBookList.add(i * interval, adBook)
                        }
                    }
                }

                view?.onAdRefresh()
            }

            override fun requestADRepairSuccess(views: List<ViewGroup>) {

                var last = 0

                for (i in iBookList.indices) {
                    if (iBookList[i].item_type == 1) {
                        last = i
                    }
                }

                val interval = BookShelfADContract.loadBookShelfADInterval() + 1

                for (i in 1 until views.size + 1) {
                    if (i * interval < iBookList.size) {
                        if (iBookList[last + (i * interval)].item_type == 1) {
                            iBookList[last + (i * interval)].item_view = views[i - 1]
                        } else if (iBookList[last + (i * interval)].item_type == 0) {
                            val adBook = Book()
                            adBook.item_type = 1
                            adBook.item_view = views[i - 1]
                            iBookList.add(last + (i * interval), adBook)
                        }
                    }
                }

                view?.onAdRefresh()
            }
        })
    }

    /***
     * 获取九宫格顶部广告
     * **/
    private fun requestShelfHeaderAD(activity: Activity) {
        BookShelfADContract.loadBookShelfHeaderAD(activity, object : BookShelfADContract.HeaderADCallback {
            override fun requestADSuccess(viewGroup: ViewGroup?) {
                if (viewGroup != null) {
                    if (iBookList.size > 0 && iBookList[0].item_type == 2) {
                        iBookList[0].item_view = viewGroup
                    } else {
                        val adBook = Book()
                        adBook.item_type = 2
                        adBook.item_view = viewGroup
                        iBookList.add(0, adBook)
                    }

                    view?.onAdRefresh()
                }
            }
        })
    }

    fun requestFloatAD(activity: Activity, viewGroup: ViewGroup) {
        BookShelfADContract.loadBookShelfFloatAD(activity, viewGroup)
    }

    fun deleteBooks(deleteBooks: java.util.ArrayList<Book>, onlyDeleteCache: Boolean) {
        val size = deleteBooks.size
        doAsync {
            val stringBuilder = StringBuilder()
            for (i in 0 until size) {
                val book = deleteBooks[i]
                stringBuilder.append(book.book_id)
                stringBuilder.append(if (book.readed == 1) "_1" else "_0")
                stringBuilder.append(if (i == size - 1) "" else "$")
            }
            // 删除书架数据库和章节数据库
            if (onlyDeleteCache) {
                deleteBooks.forEach {
                    CacheManager.remove(it.book_id)
                    BaseBookHelper.removeChapterCacheFile(it)
                }
            } else {
                bookDaoHelper.deleteBook(deleteBooks)
            }

            Thread.sleep(1000)
            uiThread {
                view?.onBookDelete()
            }
            BookShelfLogger.uploadBookShelfEditDelete(size, stringBuilder, onlyDeleteCache)
        }
    }

    fun handleSuccessUpdate(result: BookUpdateResult) {
        val hasUpdateList = ArrayList<BookUpdate>()
        if (result.items != null && result.items.isNotEmpty()) {
            val bookUpdates = result.items
            val size = bookUpdates.size
            (0 until size).map { bookUpdates[it] }
                    .filterTo(hasUpdateList) { !TextUtils.isEmpty(it.book_id) && it.update_count != 0 }
            if (hasUpdateList.isNotEmpty()) {
                view?.onSuccessUpdateHandle(hasUpdateList.size, hasUpdateList[0])
            }
        } else {
            view?.onSuccessUpdateHandle()
        }
    }

    fun removeAd() {
        iBookList.removeAll {
            it.item_type == 1
        }
    }

    fun clear() {
        view = null
    }
}