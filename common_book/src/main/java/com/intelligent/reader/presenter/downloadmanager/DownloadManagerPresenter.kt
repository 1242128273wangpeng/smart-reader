package com.intelligent.reader.presenter.downloadmanager

import com.intelligent.reader.presenter.IPresenter
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.utils.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by qiantao on 2017/11/22 0022
 */
class DownloadManagerPresenter(override var view: DownloadManagerView?) : IPresenter<DownloadManagerView> {

    private val tag = "DownloadManager"

    private var lastClickTime: Long = 0

    val bookDaoHelper = BookDaoHelper.getInstance()

    var downloadBooks: ArrayList<Book> = ArrayList()

    fun queryDownloadBooks(hasDeleted: Boolean) {
        CacheManager.freshBooks(false)
        queryBooks(hasDeleted)
    }

    private fun queryBooks(hasDeleted: Boolean) {
        val books = bookDaoHelper.booksOnLineList
        downloadBooks.clear()
        downloadBooks.addAll(books)
        downloadBooks.sort()
        Collections.sort<Book>(downloadBooks, FrameBookHelper.MultiComparator())
        Collections.sort<Book>(downloadBooks, FrameBookHelper.CachedComparator())
        uiThread {
            view?.onDownloadBookQuery(downloadBooks, hasDeleted)
        }
    }

    fun getDeleteBooks(checkStates: MutableList<Book>): ArrayList<Book> {
        val deleteBooks = ArrayList<Book>()
        deleteBooks.clear()
        for (i in 0 until downloadBooks.size) {
            val book = downloadBooks[i]
            if (checkStates.contains(book)) {
                deleteBooks.add(book)
            }
        }
        return deleteBooks
    }

    fun deleteDownload(books: ArrayList<Book>?) {
        if (books == null) return
        doAsync {
            books.forEach { book ->
                CacheManager.remove(book.book_id)
                BaseBookHelper.removeChapterCacheFile(book)
            }
            runOnMain {
                view?.onDownloadDelete()
            }
        }
    }

    fun uploadBackLog() {
        val data = HashMap<String, String>()
        data.put("type", "1")
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                "CACHEMANAGE", StartLogClickUtil.BACK, data)
    }

    fun uploadEditLog() {
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(),
                StatServiceUtils.bs_down_m_click_edit)
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.CACHEEDIT1)
    }

    fun uploadCancelLog() {
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(),
                StatServiceUtils.bs_down_m_click_cancel)
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.CHCHEEDIT_PAGE, StartLogClickUtil.CANCLE)
    }

    fun uploadDeleteLog() {
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(),
                StatServiceUtils.bs_down_m_click_delete)
    }

    fun uploadDialogCheckLog() {
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(),
                StatServiceUtils.rb_click_flip_auto_not_tip)
    }

    fun uploadDialogCancelLog() {
        val data = HashMap<String, String>()
        data.put("type", "2")
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.CHCHEEDIT_PAGE, StartLogClickUtil.DELETE, data)
    }

    fun uploadDialogConfirmLog(size: Int?) {
        if (size == null) return
        val data = HashMap<String, String>()
        data.put("type", "1")
        data.put("number", size.toString())
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.CHCHEEDIT_PAGE, StartLogClickUtil.DELETE, data)
    }

    fun uploadRemoveSelectAllLog(checkedAll: Boolean) {
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(),
                StatServiceUtils.bs_down_m_click_select_all)
        val data = HashMap<String, String>()
        data.put("type", if (checkedAll) "1" else "2")
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.CHCHEEDIT_PAGE, StartLogClickUtil.SELECTALL, data)
    }

    fun uploadBookClickLog(book: Book) {
        val data = HashMap<String, String>()
        data.put("STATUS", if (CacheManager.getBookStatus(book) === DownloadState.FINISH) "1" else "0")
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.BOOKCLICK1, data)
    }

    fun isDoubleClick(time: Long): Boolean {
        val interval = time - lastClickTime
        return if (interval > 800) {
            lastClickTime = time
            false
        } else {
            true
        }
    }

    fun uploadTimeSortingLog() {
        val data = HashMap<String, String>()
        data["type"] = "1"
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.SORT, data)
    }

    fun uploadRecentReadSortingLog() {
        val data = HashMap<String, String>()
        data["type"] = "0"
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.SORT, data)
    }

}