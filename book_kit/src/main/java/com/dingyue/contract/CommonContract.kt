package com.dingyue.contract

import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.SettingItemsHelper
import java.io.Serializable
import java.util.Comparator

/**
 * Desc 公共方法
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/9 10:10
 */
object CommonContract {

    private var lastClickTime: Long = 0

    /***
     * 书籍列表多类型排序
     * **/
    class MultiComparator constructor(val type: Int) : Comparator<Book>, Serializable {
        override fun compare(book1: Book, book2: Book): Int {
            return if (type != 1) {
                when {
                    book1.sequence_time == book2.sequence_time -> 0
                    book1.sequence_time < book2.sequence_time -> 1
                    else -> -1
                }
            } else {
                when {
                    book1.last_updatetime_native == book2.last_updatetime_native -> 0
                    book1.last_updatetime_native < book2.last_updatetime_native -> 1
                    else -> -1
                }
            }
        }
    }

    /***
     * 书籍列表缓存排序
     * **/
    class CachedComparator : Comparator<Book> {
        override fun compare(book1: Book, book2: Book): Int {
            val downloadState1 = CacheManager.getBookStatus(book1)
            val downloadState2 = CacheManager.getBookStatus(book2)

            if (downloadState1 == downloadState2) {
                return 0
            }

            if (downloadState1 == DownloadState.FINISH && downloadState2 == DownloadState.FINISH) {
                return 0
            }

            if (downloadState1 == DownloadState.FINISH && downloadState2 != DownloadState.FINISH) {
                return 1
            }

            return if (downloadState1 != DownloadState.FINISH && downloadState2 == DownloadState.FINISH) {
                -1
            } else 0
        }
    }

    /***
     * 防止按钮两次点击
     * **/
    fun isDoubleClick(time: Long): Boolean {
        val interval = time - lastClickTime
        return if (interval > 800) {
            lastClickTime = time
            false
        } else {
            true
        }
    }

    /***
     * 书架排序
     * **/
    fun insertShelfSortType(type: Int) {
        Constants.book_list_sort_type = type
        val settingItemsHelper = SettingItemsHelper.getSettingHelper(BaseBookApplication.getGlobalContext())
        settingItemsHelper.putInt(settingItemsHelper.booklistSortType, type)
    }
}