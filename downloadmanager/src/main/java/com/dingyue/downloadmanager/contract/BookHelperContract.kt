package com.dingyue.downloadmanager.contract

import android.content.Context
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.utils.BaseBookHelper
import net.lzbook.kit.utils.SettingItemsHelper
import java.io.Serializable
import java.util.*

/**
 * Desc 抽象 BookHelper 方法的调用
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/20 0020 14:05
 */
object BookHelperContract {

    private var lastClickTime: Long = 0

    fun startDownBookTask(context: Context, book: Book, startDownloadIndex: Int) {
        BaseBookHelper.startDownBookTask(context, book, startDownloadIndex)
    }

    fun querySortedBookList(type: Int): List<Book> {
        val books = BookDaoHelper.getInstance().booksOnLineList
        Collections.sort<Book>(books, MultiComparator(type))
        Collections.sort<Book>(books, CachedComparator())
        return books
    }

    fun removeChapterCacheFile(book: Book) {
        BaseBookHelper.removeChapterCacheFile(book)
    }

    fun loadLocalBook(id: String?): Book {
        return BookDaoHelper.getInstance().getBook(id, 0)
    }

    fun insertShelfSortType(type: Int) {
        Constants.book_list_sort_type = type

        val settingItemsHelper = SettingItemsHelper.getSettingHelper(BaseBookApplication.getGlobalContext())
        settingItemsHelper.putInt(settingItemsHelper.booklistSortType, type)
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

    class CachedComparator : Comparator<Book> {
        override fun compare(book1: Book, book2: Book): Int {
            val status1 = CacheManager.getBookStatus(book1)
            val status2 = CacheManager.getBookStatus(book2)

            if (status1 == status2) {
                return 0
            }

            if (status1 == DownloadState.FINISH && status2 == DownloadState.FINISH) {
                return 0
            }

            if (status1 == DownloadState.FINISH && status2 != DownloadState.FINISH) {
                return 1
            }

            return if (status1 != DownloadState.FINISH && status2 == DownloadState.FINISH) {
                -1
            } else 0
        }
    }
}