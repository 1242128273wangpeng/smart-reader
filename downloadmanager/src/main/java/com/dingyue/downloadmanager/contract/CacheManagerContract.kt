package com.dingyue.downloadmanager.contract

import com.ding.basic.bean.Book
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.CallBackDownload
import net.lzbook.kit.book.download.DownloadState

/**
 * Desc 抽象 CacheManager 方法的调用
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/2 0002 20:22
 */
object CacheManagerContract {

    fun freshBookTasks() {
        CacheManager.freshBooks(false)
    }

    fun removeBookTask(id: String) {
        CacheManager.remove(id)
    }

    fun loadBookDownloadState(book: Book): DownloadState {
        return CacheManager.getBookStatus(book)
    }

    fun insertDownloadCallBack(callBackDownload: CallBackDownload) {
        CacheManager.listeners.add(callBackDownload)
    }

    fun removeDownloadCallBack(callBackDownload: CallBackDownload) {
        CacheManager.listeners.remove(callBackDownload)
    }
}