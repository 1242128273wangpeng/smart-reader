package com.dingyue.contract

import com.ding.basic.bean.Book
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.SettingItemsHelper
import java.io.Serializable
import java.util.*

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
            return if (type == 0) {//阅读时间
                when {
                    book1.last_read_time == book2.last_read_time -> 0
                    book1.last_read_time < book2.last_read_time -> 1
                    else -> -1
                }
            } else if (type == 2) {//添加时间
                when {
                    book1.insert_time == book2.insert_time -> 0
                    book1.insert_time < book2.insert_time -> 1
                    else -> -1
                }
            } else {//更新时间
                val lastChapter1 = book1.last_chapter
                val lastChapter2 = book2.last_chapter

                if (lastChapter1 != null && lastChapter2 != null) {
                    when {
                        lastChapter1.update_time == lastChapter2.update_time -> 0
                        lastChapter1.update_time < lastChapter2.update_time -> 1
                        else -> -1
                    }
                } else 0
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
     * 防止阅读页音量键按钮两次点击
     * **/
    fun isVolumDoubleClick(time: Long): Boolean {
        val interval = time - lastClickTime
        return if (interval > 300) {
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

    fun queryBookSortingType(): Int {
        val settingItemsHelper = SettingItemsHelper.getSettingHelper(BaseBookApplication.getGlobalContext())
        return settingItemsHelper.values.booklist_sort_type
    }

}