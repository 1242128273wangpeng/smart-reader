package com.dingyue.downloadmanager.contract

import android.content.Context
import com.dingyue.contract.CommonContract
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
        Collections.sort<Book>(books, CommonContract.MultiComparator(type))
        Collections.sort<Book>(books, CommonContract.CachedComparator())
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
}