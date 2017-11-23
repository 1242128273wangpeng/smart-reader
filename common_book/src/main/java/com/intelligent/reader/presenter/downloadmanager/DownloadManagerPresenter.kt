package com.intelligent.reader.presenter.downloadmanager

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.intelligent.reader.activity.DownloadManagerActivity
import com.intelligent.reader.presenter.IPresenter
import com.intelligent.reader.read.help.BookHelper
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.component.service.DownloadService
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

    var downloadService: DownloadService? = BaseBookApplication.getDownloadService()
    val serviceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {}

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            downloadService = (service as DownloadService.MyBinder).service
            BaseBookApplication.setDownloadService(downloadService)
            setDownloadService()
            queryBooks(false)
        }
    }

    fun setDownloadService() {
        downloadService?.setUiContext(BaseBookApplication.getGlobalContext())
        downloadService?.setOnDownloadListener { preNTF, bookId ->
            val pending = (view as DownloadManagerActivity).pendingIntent(bookId)
            preNTF.contentIntent = pending
        }
    }

    fun queryDownloadBooks(hasDeleted: Boolean) {
        if (downloadService == null) {
            (view as DownloadManagerActivity).startDownloadService()
        } else {
            queryBooks(hasDeleted)
        }
    }

    private fun queryBooks(hasDeleted: Boolean) {
        val books = bookDaoHelper.booksOnLineList
        (view as DownloadManagerActivity).addBookToService(books)
        // TODO 从service获取books列表 FIXME
        if (downloadService != null) {
            downloadBooks.clear()
            downloadBooks.addAll(books)
            Collections.sort<Book>(downloadBooks)
            Collections.sort<Book>(downloadBooks, FrameBookHelper.MultiComparator())

            view?.onDownloadBookQuery(downloadBooks, hasDeleted)
        } else {
            AppLog.e(tag, "downloadService == null")
        }
    }

    fun getDeleteBooks(checkStates: HashSet<Int>): ArrayList<Book> {
        val deleteBooks = ArrayList<Book>()
        deleteBooks.clear()
        for (i in 0 until downloadBooks.size) {
            val book = downloadBooks[i]
            if (checkStates.contains(i)) {
                deleteBooks.add(book)
            }
        }
        return deleteBooks
    }

    fun deleteBooksOfShelf(books: ArrayList<Book>?) {
        if (books == null) return
        val bookIds = arrayOfNulls<String>(books.size)
        for (i in bookIds.indices) {
            AppLog.e(tag, "DownloadPage: " + books[i].toString())
            bookIds[i] = books[i].book_id
        }
        bookDaoHelper.deleteBook(*bookIds)
    }

    fun deleteDownload(books: ArrayList<Book>?, isDeleteOfShelf: Boolean) {
        if (books == null) return
        doAsync {
            books.forEach { book ->
                if (downloadService != null) {
                    if (isDeleteOfShelf) downloadService?.dellTask(book.book_id)
                    else downloadService?.resetTask(book.book_id, true)
                }
                if (downloadService?.getDownBookTask(book.book_id) != null) {
                    downloadService?.getDownBookTask(book.book_id)?.state = DownloadState
                            .NOSTART
                    downloadService?.getDownBookTask(book.book_id)?.startSequence = 0
                }
                BookHelper.delDownIndex(BaseBookApplication.getGlobalContext(), book.book_id)
                BookHelper.removeChapterCacheFile(book.book_id)
            }
            runOnMain {
                view?.onDownloadDelete(isDeleteOfShelf)
            }
        }
    }

    fun uploadBackLog() {
        val data = HashMap<String, String>()
        data.put("type", "1")
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data)
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
        data.put("type", "0")
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
        data.put("type", if (checkedAll) "1" else "0")
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.CHCHEEDIT_PAGE, StartLogClickUtil.SELECTALL, data)
    }

    fun uploadBookClickLog(b: Book) {
        val data = HashMap<String, String>()
        data.put("STATUS", if (BookHelper.isDownFnish(BaseBookApplication.getGlobalContext(), b)) "1" else "0")
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

}