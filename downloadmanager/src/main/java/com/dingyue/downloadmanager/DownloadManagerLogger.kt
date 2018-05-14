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

    fun uploadCacheManagerBack() {
        val data = HashMap<String, String>()
        data["type"] = "1"

        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_CACHE_MANAGER, StartLogClickUtil.ACTION_CACHE_MANAGER_BACK, data)
    }

    fun uploadCacheManagerBookClick(book: Book) {
        val data = HashMap<String, String>()
        data["STATUS"] = if (CacheManager.getBookStatus(book) === DownloadState.FINISH) "1" else "0"

        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_CACHE_MANAGER, StartLogClickUtil.ACTION_CACHE_MANAGER_BOOK_CLICK, data)
    }

    fun uploadCacheManagerButtonClick(status: DownloadState, bookId: String, progress: Int) {
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

        StartLogClickUtil.upLoadEventLog(context,  StartLogClickUtil.PAGE_CACHE_MANAGER,
                StartLogClickUtil.ACTION_CACHE_MANAGER_CACHE_BUTTON, data)
    }

    fun uploadCacheManagerEdit() {
        val context = BaseBookApplication.getGlobalContext()

        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.bs_down_m_click_edit)

        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PAGE_CACHE_MANAGER,
                StartLogClickUtil.ACTION_CACHE_MANAGER_CACHE_EDIT)
    }

    fun uploadCacheManagerMore() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_CACHE_MANAGER, StartLogClickUtil.ACTION_CACHE_MANAGER_MORE)
    }

    fun uploadCacheManagerBookCity() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_CACHE_MANAGER, StartLogClickUtil.ACTION_CACHE_MANAGER_TO_BOOK_CITY)
    }

    fun uploadCacheManagerSort(type: Int) {
        val data = HashMap<String, String>()
        data["type"] = type.toString()

        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_CACHE_MANAGER, StartLogClickUtil.ACTION_CACHE_MANAGER_SORT, data)
    }

    fun uploadCacheMangerEditCancel() {
        val context = BaseBookApplication.getGlobalContext()

        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.bs_down_m_click_cancel)

        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PAGE_CACHE_MANAGER_EDIT,
                StartLogClickUtil.ACTION_CACHE_MANAGER_EDIT_CANCEL)
    }

    fun uploadCacheManagerEditDelete(books: List<Book>) {
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
                StartLogClickUtil.PAGE_CACHE_MANAGER_EDIT, StartLogClickUtil.ACTION_CACHE_MANAGER_EDIT_DELETE, data)
    }

    fun uploadCacheManagerEditSelectAll(checkedAll: Boolean) {
        val context = BaseBookApplication.getGlobalContext()

        val data = HashMap<String, String>()
        data["type"] = if (checkedAll) "1" else "2"

        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.bs_down_m_click_select_all)

        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PAGE_CACHE_MANAGER_EDIT,
                StartLogClickUtil.ACTION_CACHE_MANAGER_EDIT_SELECT_ALL, data)
    }

    fun uploadCacheManagerEditDeleteLog() {
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(),
                StatServiceUtils.bs_down_m_click_delete)
    }
}