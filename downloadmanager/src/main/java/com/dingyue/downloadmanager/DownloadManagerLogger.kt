package com.dingyue.downloadmanager

import com.ding.basic.bean.Book
import com.dingyue.statistics.DyStatService
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.download.DownloadState
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
        DyStatService.onEvent(EventPoint.CACHEMANAGE_BACK, mapOf("type" to "1"))
    }

    fun uploadCacheManagerBookClick(book: Book) {
        DyStatService.onEvent(EventPoint.CACHEMANAGE_BOOKCLICK, mapOf("STATUS" to if (CacheManager.getBookStatus(book) === DownloadState.FINISH) "1" else "0"))
    }

    fun uploadCacheManagerButtonClick(status: DownloadState, bookId: String, progress: Int) {
        val data = HashMap<String, String>()
        if (status == DownloadState.NOSTART) {
            data["type"] = "1"
        } else if (status == DownloadState.DOWNLOADING || status == DownloadState.WAITTING) {
            data["type"] = "2"
            data["speed"] = "$progress/100"
        } else {
            data["type"] = "1"
        }
        data["bookid"] = bookId
        DyStatService.onEvent(EventPoint.CACHEMANAGE_CACHEBUTTON, data)
    }

    fun uploadCacheManagerEdit() {
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.bs_down_m_click_edit)
        DyStatService.onEvent(EventPoint.CACHEMANAGE_CACHEEDIT)
    }

    fun uploadCacheManagerMore() {
        DyStatService.onEvent(EventPoint.CACHEMANAGE_MORE)
    }

    fun uploadCacheManagerBookCity() {
        DyStatService.onEvent(EventPoint.CACHEMANAGE_TOBOOKCITY)
    }

    fun uploadCacheManagerSort(type: Int) {
        DyStatService.onEvent(EventPoint.CACHEMANAGE_SORT, mapOf("type" to type.toString()))
    }

    fun uploadCacheMangerEditCancel() {
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.bs_down_m_click_cancel)
        DyStatService.onEvent(EventPoint.CHCHEEDIT_CANCLE)
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

        DyStatService.onEvent(EventPoint.CHCHEEDIT_DELETE, data)
    }

    fun uploadCacheManagerEditSelectAll(checkedAll: Boolean) {
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.bs_down_m_click_select_all)
        DyStatService.onEvent(EventPoint.CHCHEEDIT_SELECTALL, mapOf("type" to if (checkedAll) "1" else "2"))
    }

    fun uploadCacheManagerEditDeleteLog() {
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(),
                StatServiceUtils.bs_down_m_click_delete)
    }
}