package com.dingyue.downloadmanager

import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.db.table.ChapterTable
import net.lzbook.kit.utils.StatServiceUtils
import java.util.*

/**
 * Desc 下载管理日志管理器
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/2 0002 20:14
 */
object DownloadManagerLogger {

    fun uploadBackLog() {
        val data = HashMap<String, String>()
        data["type"] = "1"
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

    fun uploadDialogConfirmLog(books: List<Book>) {
        val data = HashMap<String, String>()

        val bookIds = StringBuilder()
        val status = StringBuilder()

        for (i in 0 until books.size) {
            bookIds.append(if (i == books.size - 1) books[i].book_id else books[i].book_id + "$")
            val task = CacheManager.getBookTask(books[i])

            val currentStatus: String = if (task.state == DownloadState.DOWNLOADING) {
                "4"
            } else if (task.state == DownloadState.WAITTING
                    || task.state == DownloadState.PAUSEED
                    || task.state == DownloadState.WAITTING_WIFI) {
                "3"
            } else if (task.state == DownloadState.FINISH) {
                "1"
            } else {
                "2"
            }
            status.append(if (i == books.size - 1) currentStatus else "$currentStatus$")

        }
        data["type"] = "1"
        data["status"] = status.toString()
        data["bookid"] = bookIds.toString()
        data["number"] = books.size.toString()
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.CHCHEEDIT_PAGE, StartLogClickUtil.DELETE, data)
    }

    fun uploadRemoveSelectAllLog(checkedAll: Boolean) {
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(),
                StatServiceUtils.bs_down_m_click_select_all)
        val data = HashMap<String, String>()
        data["type"] = if (checkedAll) "1" else "2"
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.CHCHEEDIT_PAGE, StartLogClickUtil.SELECTALL, data)
    }

    fun uploadBookClickLog(book: Book) {
        val data = HashMap<String, String>()
        data["STATUS"] = if (CacheManager.getBookStatus(book) === DownloadState.FINISH) "1" else "0"
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.BOOKCLICK1, data)
    }

    fun uploadSortingLog(type: Int) {
        val data = HashMap<String, String>()
        data["type"] = type.toString()
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.SORT, data)
    }

    fun uploadItemClickLog(status: DownloadState, bookId: String, progress: Int) {
        val context = BaseBookApplication.getGlobalContext()
        val data = HashMap<String, String>()
        if (status == DownloadState.NOSTART) {
            data["type"] = "1"
            data["bookid"] = bookId
        } else if (status == DownloadState.DOWNLOADING || status == DownloadState.WAITTING) {
            data["type"] = "2"
            data[ChapterTable.SPEED] = "$progress/100"
        } else {
            data["type"] = "1"
        }
        data["bookid"] = bookId
        StartLogClickUtil.upLoadEventLog(context, "CACHEMANAGE", StartLogClickUtil.CACHEBUTTON, data)
    }

}