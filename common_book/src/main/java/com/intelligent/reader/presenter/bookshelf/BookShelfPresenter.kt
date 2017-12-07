package com.intelligent.reader.presenter.bookshelf

import android.content.SharedPreferences
import android.text.TextUtils
import com.intelligent.reader.presenter.IPresenter
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.BookUpdate
import net.lzbook.kit.data.bean.BookUpdateResult
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.utils.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by qiantao on 2017/11/14 0014
 */
class BookShelfPresenter(override var view: BookShelfView?) : IPresenter<BookShelfView> {

    private val tag = "BookShelfPresenter"

    private var bookDaoHelper: BookDaoHelper = BookDaoHelper.getInstance()

    var iBookList: ArrayList<Book> = ArrayList()

    private val updateTableList: ArrayList<String> = ArrayList()

    /**
     * 查询书籍列表
     */
    fun queryBookListAndAd() {
        val bookList = bookDaoHelper.booksOnLineList
        Collections.sort(bookList, FrameBookHelper.MultiComparator())
        iBookList.clear()
        iBookList.addAll(bookList)
        runOnMain {
            view?.onBookListQuery(bookList)
        }
    }

    fun handleSuccessUpdate(result: BookUpdateResult) {
        val hasUpdateList = ArrayList<BookUpdate>()
        if (result.items != null && result.items.isNotEmpty()) {
            val bookUpdates = result.items
            val size = bookUpdates.size
            (0 until size).map { bookUpdates[it] }
                    .filterTo(hasUpdateList) { !TextUtils.isEmpty(it.book_id) && it.update_count != 0 }
            if (hasUpdateList.isNotEmpty()) {
                view?.onSuccessUpdateHandle(hasUpdateList.size, hasUpdateList[0])
            }
        } else {
            view?.onSuccessUpdateHandle()
        }
    }

    fun removeAd() {
        iBookList.filter {
            //若当前的书籍是广告
            it.book_type == -2
        }.forEach { book ->
            iBookList.remove(book)
        }
    }

    /**
     * 过滤出更新状态的表
     */
    fun filterUpdateTableList(): ArrayList<String> {
        iBookList.asSequence().forEach { book ->
            if (book.update_status == 1) {
                if (!updateTableList.contains(book.book_id)) {
                    updateTableList.add(book.book_id)
                }
            } else {
                if (updateTableList.contains(book.book_id)) {
                    updateTableList.remove(book.book_id)
                }
            }
        }
        return updateTableList
    }

    /**
     * 取消数据库中更新状态
     */
    fun resetUpdateStatus(book_id: String) {
        val book = Book()
        book.book_id = book_id
        book.update_status = 0
        if (updateTableList.contains(book.book_id)) {
            updateTableList.remove(book_id)
            bookDaoHelper.updateBook(book)
        }
    }

    fun uploadFirstOpenLog(sp: SharedPreferences) {
        //判断用户是否是当日首次打开应用,并上传书架的id
        val lastTime = sp.getLong(Constants.TODAY_FIRST_POST_BOOKIDS, 0)
        val currentTime = System.currentTimeMillis()

        val isSameDay = AppUtils.isToday(lastTime, currentTime)
        if (!isSameDay) {
            val bookIdList = StringBuilder()
            iBookList.forEachIndexed { index, book ->
                bookIdList.append(book.book_id)
                bookIdList.append(if (book.readed == 1) "_1" else "_0")//1已读，0未读
                bookIdList.append(if (index == iBookList.size) "" else "$")
            }
            val data = HashMap<String, String>()
            data.put("bookid", bookIdList.toString())
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                    StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.BOOKLIST, data)
            sp.edit().putLong(Constants.TODAY_FIRST_POST_BOOKIDS, currentTime).apply()
        }
    }

    fun uploadItemClickLog(position: Int) {
        val data = HashMap<String, String>()
        data.put("bookid", iBookList[position].book_id)
        data.put("rank", (position + 1).toString())
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.BOOKCLICK, data)
    }

    fun uploadItemLongClickLog() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.LONGTIMEBOOKSHELFEDIT)
    }

    fun deleteBooks(books: ArrayList<Book>) {
        val size = books.size
        doAsync {
            val bookIdArr = arrayOfNulls<String>(size)
            val sb = StringBuffer()
            for (i in 0 until size) {
                val book = books[i]
                bookIdArr[i] = book.book_id

                sb.append(book.book_id)
                sb.append(if (book.readed == 1) "_1" else "_0")
                sb.append(if (i == size - 1) "" else "$")
            }
            // 删除书架数据库和章节数据库
            bookDaoHelper.deleteBook(*bookIdArr)
            runOnMain {
                view?.onBookDelete()
            }

            uploadBookDeleteLog(size, sb)
        }
    }

    private fun uploadBookDeleteLog(size: Int, sb: StringBuffer) {
        val data1 = HashMap<String, String>()
        data1.put("type", "1")
        data1.put("number", size.toString())
        data1.put("bookids", sb.toString())
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.DELETE1, data1)
    }

    fun uploadBookDeleteCancelLog() {
        val data = HashMap<String, String>()
        data.put("type", "2")
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELFEDIT, StartLogClickUtil.DELETE1, data)
    }
}