package com.dingyue.bookshelf

import android.app.Activity
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.text.TextUtils
import android.view.ViewGroup
import com.dingyue.bookshelf.contract.BookHelperContract
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
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.BaseBookHelper
import net.lzbook.kit.utils.doAsync
import net.lzbook.kit.utils.uiThread
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by qiantao on 2017/11/14 0014
 */
class BookShelfPresenter(override var view: BookShelfView?) : IPresenter<BookShelfView> {

    private val tag = "BookShelfPresenter"

    private var bookDaoHelper: BookDaoHelper = BookDaoHelper.getInstance()

    var iBookList: ArrayList<Book> = ArrayList()

    var aDViews: ArrayList<ViewGroup> = ArrayList()

    private val updateTableList: ArrayList<String> = ArrayList()

    var updateService: CheckNovelUpdateService? = null

    val updateConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {}

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            try {
                updateService = (service as CheckNovelUpdateService.CheckUpdateBinder).service
                AppLog.d(tag, "auto-updateService $updateService")
                if (updateService != null) {
                    AppLog.d(tag, "updateData ")
                    view?.doUpdateBook(updateService!!)
                }
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }

        }
    }

    fun addUpdateTask(updateCallBack: UpdateCallBack) {
        AppLog.e(tag, "updateService: $updateService")
        if (bookDaoHelper.booksCount > 0 && updateService != null) {
            val list = bookDaoHelper.booksList
            AppLog.e(tag, "BookUpdateCount: " + list.size)
            updateService?.checkUpdate(BookHelperContract.loadBookUpdateTaskData(list, updateCallBack))
        }
    }

    /**
     * 查询书籍列表
     */
    fun queryBookListAndAd(activity: Activity, isShowAd: Boolean) {
        val adNum = updateBookList()
        if (isShowAd && iBookList.isNotEmpty()) {
            updateAd(activity, adNum)
        }
    }

    fun updateBookList(): Int {
        val bookList = bookDaoHelper.booksOnLineList
        iBookList.clear()
        if (bookList.isEmpty()) {
            uiThread {
                view?.onBookListQuery(bookList)
            }
            return 0
        } else {
            Collections.sort(bookList, CommonContract.MultiComparator(Constants.book_list_sort_type))
            iBookList.addAll(bookList)
//            val adCount = PlatformSDK.config().adCount
            val adCount = 0
            if (aDViews.isNotEmpty()) {
                val size = iBookList.size
                var index = 0
                var book1 = Book()
                book1.book_type = -2
                book1.sequence = index++
                iBookList.add(0, book1)
                var i: Int = 1
                while (size > adCount * i) {
                    book1 = Book()
                    book1.book_type = -2
                    book1.sequence = index++
                    iBookList.add(adCount * i, book1)
                    i++
                }
            }
            uiThread {
                view?.onBookListQuery(bookList)
            }
//            return bookList.size / adCount + 1
            return 0
        }
    }

    fun updateAd(activity: Activity, num: Int) {
//        PlatformSDK.adapp().dycmNativeAd(activity, "1-1", RelativeLayout(activity), object : AbstractCallback() {
//            override fun onResult(adswitch: Boolean, views: List<ViewGroup>?, jsonResult: String?) {
//                DyLogUtils.dd("NativeActivity:" + jsonResult!!)
//                if (!adswitch) return
//                try {
//                    val jsonObject = JSONObject(jsonResult)
//                    DyLogUtils.e("ADSDK", "执行NativeActivity 回调")
//                    if (jsonObject.has("state_code")) {
//                        when (ResultCode.parser(jsonObject.getInt("state_code"))) {
//                            ResultCode.AD_REQ_SUCCESS//请求成功
//                            -> {
//                                uiThread {
//                                    if (views != null) {
//                                        aDViews.clear()
//                                        updateBookList()
//                                        aDViews.addAll(views)
//                                        if (iBookList.isEmpty()) {
//                                            return@uiThread
//                                        }
//                                        val size = iBookList.size
//                                        var index = 0
//                                        var book1 = Book()
//                                        book1.book_type = -2
//                                        book1.sequence = index++
//                                        iBookList.add(0, book1)
//                                        var adCount = PlatformSDK.config().getAdCount()
//                                        var i: Int = 1
//                                        while (size > adCount * i) {
//                                            book1 = Book()
//                                            book1.book_type = -2
//                                            book1.sequence = index++
//                                            iBookList.add(adCount * i, book1)
//                                            i++
//                                        }
//
//                                        view?.onAdRefresh()
//
//                                    }
//                                    DyLogUtils.e("ADSDK", "请求成功")
//                                }
//
//                            }
//                            ResultCode.AD_REPAIR_SUCCESS//补充
//                            -> {
//                                if (views != null) {
//                                    aDViews.addAll(views)
//                                    uiThread {
//                                        view?.onAdRefresh()
//                                    }
//                                }
//                            }
//                            ResultCode.AD_REQ_FAILED//请示失败
//                            -> {
//                            }
//                        }
//                    }
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                }
//
//            }
//        }, num)
    }

    fun deleteBooks(deleteBooks: java.util.ArrayList<Book>, isOnlyDeleteCache: Boolean) {
        val size = deleteBooks.size
        doAsync {
            val sb = StringBuffer()
            for (i in 0 until size) {
                val book = deleteBooks[i]
                sb.append(book.book_id)
                sb.append(if (book.readed == 1) "_1" else "_0")
                sb.append(if (i == size - 1) "" else "$")
            }
            // 删除书架数据库和章节数据库
            if (isOnlyDeleteCache) {
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
            BookShelfLogger.uploadBookCacheDeleteLog(sb, size, isOnlyDeleteCache)
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
        iBookList.filter {
            //若当前的书籍是广告
            it.book_type == -2
        }.forEach { book ->
            iBookList.remove(book)
        }
    }

//    /**
//     * 过滤出更新状态的表
//     */
//    fun filterUpdateTableList(): ArrayList<String> {
//        iBookList.asSequence().forEach { book: Book? ->
//            if (book?.update_status == 1) {
//                if (!updateTableList.contains(book.book_id)) {
//                    updateTableList.add(book.book_id)
//                }
//            } else {
//                if (updateTableList.contains(book?.book_id)) {
//                    updateTableList.remove(book?.book_id)
//                }
//            }
//        }
//        return updateTableList
//    }

    /**
     * 取消数据库中更新状态
     */
    fun resetUpdateStatus(book_id: String) {
        val book = Book()
        book.book_id = book_id
        book.update_status = 0
        if (updateTableList.contains(book.book_id)) {
            updateTableList.remove(book_id)
            bookDaoHelper.updateBook(book)
        }
    }

}