package com.dingyue.bookshelf

import android.app.Activity
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.text.TextUtils
import android.view.ViewGroup
import com.ding.basic.bean.Book
import com.ding.basic.bean.BookUpdate
import com.ding.basic.RequestRepositoryFactory
import com.dingyue.bookshelf.contract.BookHelperContract
import com.dy.media.IMediaControl
import com.dy.media.MediaControl
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.presenter.base.IPresenter
import net.lzbook.kit.bean.BookUpdateResult
import net.lzbook.kit.bean.UpdateCallBack
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.service.CheckNovelUpdateService
import net.lzbook.kit.utils.book.BaseBookHelper
import net.lzbook.kit.utils.book.CommonContract
import net.lzbook.kit.utils.doAsync
import net.lzbook.kit.utils.download.CacheManager
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import net.lzbook.kit.utils.uiThread
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

/**
 * Created by qiantao on 2017/11/14 0014
 */
open class BookShelfPresenter(override var view: BookShelfView?) : IPresenter<BookShelfView> {

    var iBookList: ArrayList<Book> = ArrayList()


    private val adBookMap = LinkedHashMap<Int, Book>()

    var updateService: CheckNovelUpdateService? = null

    /***
     * 检查更新服务Connection, 当Service启动后调用书架检查更新方法
     * **/
    val updateConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {}

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            try {
                updateService = (service as CheckNovelUpdateService.CheckUpdateBinder).service
                if (updateService != null) {
                    view?.doUpdateBook(updateService!!)
                }
            } catch (exception: ClassCastException) {
                exception.printStackTrace()
            }
        }
    }

    /***
     * 添加检查更新任务，调用Service进行检查更新
     * **/
    fun addUpdateTask(updateCallBack: UpdateCallBack) {

        val count = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBookCount()

        if (count > 0 && updateService != null) {
            val books = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadReadBooks()
            updateService?.checkUpdate(BookHelperContract.loadBookUpdateTaskData(books, updateCallBack))
        }
    }

    /***
     * 更新书架书籍列表，并请求书架广告
     * **/
    fun queryBookListAndAd(activity: Activity, isShowAD: Boolean, isList: Boolean) {
        val adCount = calculationShelfADCount(isShowAD, isList)
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
     * 刷新书籍列表，并计算请求广告数量。 注：将adBookMap插入到列表中，主要是为了解决刷新列表时，广告抖动的问题。
     * **/
    private fun calculationShelfADCount(isShowAD: Boolean, isList: Boolean): Int {
        val books = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBooks()

        if (books != null) {
            if(isList){
                iBookList.removeAll {
                    it.item_type != 2
                }
            }else{
                iBookList.clear()
            }

            if (books.isEmpty()) {
                uiThread {
                    view?.onBookListQuery(books)
                }
                return 0
            } else {
                Collections.sort(books, CommonContract.MultiComparator(Constants.book_list_sort_type))
                iBookList.addAll(books)

                //将之前请求到广告的item，添加到列表中，防止刷新列表是，广告抖动问题
                if (adBookMap.isNotEmpty()) {
                    val iterator = adBookMap.entries.iterator()

                    while (iterator.hasNext()) {
                        val entry = iterator.next()
                        if (entry.key < iBookList.size && entry.value.item_view != null) {
                            iBookList.add(entry.key, entry.value)
                        }
                    }
                }

                uiThread {
                    view?.onBookListQuery(books)
                }

                return if (isShowAD) {
                    val interval = MediaControl.loadBookShelfMediaInterval()

                    if (interval == 0) {
                        0
                    } else {
                        //如果当前书架展示为列表时，请求广告数量加1。主要是因为列表形式的广告，position为0需要插入一条多余的广告
                        books.size / interval + (if (isList) 1 else 0)
                    }
                } else {
                    0
                }
            }
        } else {
            return 0
        }
    }

    /***
     * 请求书架列表中广告。注：列表中广告请求，返回结果中不一定包含所有广告，剩余广告将由补余策略回传
     * **/
    private fun requestShelfADs(activity: Activity, count: Int, isList: Boolean) {

        val interval = MediaControl.loadBookShelfMediaInterval() + 1

        var adBook: Book

        //创建相应广告位置的Bean
        if (isList) {
            adBook = Book()
            adBook.item_type = 1
            adBook.item_view = null
            adBookMap[0] = adBook
        }

        for (i in 1 until count + 1) {
            val key = i * interval
            if (key < iBookList.size) {
                adBook = Book()
                adBook.item_type = 1
                adBook.item_view = null
                adBookMap[key] = adBook
            }
        }

        val views = mutableListOf<ViewGroup>()

        for (i in 0 until count){
            views.add(BookShelfADView(activity))
        }

        handleADResult(views)
        view?.onAdRefresh()

//        doAsync {
//            MediaControl.loadBookShelMedia(activity, count, object : IMediaControl.MediaCallback {
//                override fun requestMediaSuccess(views: List<ViewGroup>) {
//                    runOnMain {
//                        handleADResult(views)
//                        view?.onAdRefresh()
//                    }
//                }
//
//                override fun requestMediaRepairSuccess(views: List<ViewGroup>) {
//                    runOnMain {
//                        handleADResult(views)
//                        view?.onAdRefresh()
//                    }
//
//                }
//            })
//        }

    }


    /***
     * 处理书架列表中广告请求结果。返回结果先保存到adBookMap中，再添加或更新到列表中
     * **/
    private fun handleADResult(views: List<ViewGroup>) {
        val adViews = ArrayList<ViewGroup>()
        val iBooks = ArrayList<Book>()
        adViews.addAll(views)
        iBooks.addAll(iBookList)

        var index = 0

        val iterator = adBookMap.entries.iterator()

        val size = iBooks.size

        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key < size && entry.value.item_view == null) {
                if (index < adViews.size) {
                    entry.value.item_view = adViews[index]

                    if (iBooks[entry.key].item_type == 1) {
                        iBooks[entry.key].item_view = entry.value.item_view
                    } else {
                        iBooks.add(entry.key, entry.value)
                    }

                    index += 1
                } else {
                    break
                }
            }
        }
    }

    /***
     * 获取九宫格顶部广告
     * **/
    private fun requestShelfHeaderAD(activity: Activity) {
        MediaControl.insertBookShelfMediaType(false)
        MediaControl.loadBookShelfHeaderMedia(activity, object : IMediaControl.HeaderMediaCallback {
            override fun requestMediaSuccess(viewGroup: ViewGroup?) {
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

    /***
     * 请求书架悬浮广告
     * **/
    fun requestFloatAD(activity: Activity, viewGroup: ViewGroup) {
        MediaControl.loadBookShelfFloatMedia(activity, viewGroup)
    }

    /***
     * 删除书籍
     * **/
    fun deleteBooks(deleteBooks: java.util.ArrayList<Book>, onlyDeleteCache: Boolean) {
        val size = deleteBooks.size

        // 书架上书籍数量
        val bookCount = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBookCount()

        // 清除当前阅读书籍状态
        if (bookCount.toInt() == size) {
            SPUtils.putDefaultSharedString(SPKey.CURRENT_READ_BOOK, "")
        }

        doAsync {
            val stringBuilder = StringBuilder()
            for (i in 0 until size) {
                val book = deleteBooks[i]
                stringBuilder.append(book.book_id)
                stringBuilder.append(if (book.readed == 1) "_1" else "_0")
                stringBuilder.append(if (i == size - 1) "" else "$")
            }

            //删除数据库和章节缓存。可只清除章节缓存
            if (onlyDeleteCache) {
                deleteBooks.forEach {
                    CacheManager.remove(it.book_id)
                    BaseBookHelper.removeChapterCacheFile(it)
                }
            } else {
                deleteBooks.forEach {
                    CacheManager.remove(it.book_id)
                    BaseBookHelper.removeChapterCacheFile(it)
                }
                RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).deleteBooks(deleteBooks)
            }

            Thread.sleep(1000)
            uiThread {
                view?.onBookDelete()
            }

            BookShelfLogger.uploadBookShelfEditDelete(size, stringBuilder, onlyDeleteCache)

        }
    }

    /***
     * 处理书籍检查更新结果
     * **/
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

    /***
     * 移除列表中的广告Bean，九宫格形式的书架，在书架编辑状态下，顶部广告位不隐藏，所以只隐藏item_type为1的广告Bean
     * **/
    fun removeAd() {
        iBookList.removeAll {
            it.item_type == 1
        }
    }

    /***
     * 清除缓存，主要清除Presenter中的View引用，以及List和Map中，每个Bean中的View
     * **/
    fun clear() {
        view = null

        iBookList.forEach {
            if (it.item_type == 1 || it.item_type == 2) {
                if (it.item_view != null) {
                    it.item_view?.removeAllViews()
                    it.item_view = null
                }
            }
        }

        if (adBookMap.isNotEmpty()) {
            val iterator = adBookMap.entries.iterator()

            while (iterator.hasNext()) {
                val entry = iterator.next()

                if (entry.value.item_view != null) {
                    entry.value.item_view?.removeAllViews()
                    entry.value.item_view = null
                }
            }
        }

        iBookList.clear()

        adBookMap.clear()
    }
}