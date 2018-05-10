package net.lzbook.kit.router

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.request.DataCache
import net.lzbook.kit.utils.FootprintUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.oneclick.AntiShake
import java.util.*

/**
 * Desc 关于 Book 操作的路由跳转
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/20 0020 14:18
 */
object BookRouter {

    const val NAVIGATE_TYPE_BOOKSHELF = 0
    const val NAVIGATE_TYPE_DOWNLOAD = 1
    const val NAVIGATE_TYPE_RECOMMEND = 2
    const val NAVIGATE_TYPE_READING = 3

    private val shake = AntiShake()

    fun navigateCoverOrRead(activity: Activity, book: Book, type: Int): Any? {

        if (book.book_type != Book.TYPE_ONLINE) return null
        val bundle = Bundle()

        // 逻辑已经改 这里修改更新状态为false
        val updateBook = Book()
        updateBook.book_id = book.book_id
        updateBook.book_source_id = book.book_source_id
        updateBook.update_status = 0
        updateBook.book_type = 0
        updateBook.dex = book.dex
        updateBook.initialization_status = book.initialization_status
        BookDaoHelper.getInstance().updateBook(updateBook)
        val requestItem = RequestItem()
        requestItem.book_id = book.book_id
        requestItem.book_source_id = book.book_source_id
        requestItem.host = book.site
        requestItem.name = book.name
        requestItem.author = book.author
        requestItem.dex = book.dex
        if (book.readed == -2 || book.initialization_status == 5) {
            val bookDaoHelper = BookDaoHelper.getInstance()
            book.initialization_status = 0
            bookDaoHelper.updateBookNew(book)

            //跳转到目录页
            bundle.putSerializable("cover", book)
            bundle.putInt("sequence", book.sequence)
            bundle.putBoolean("fromCover", true)
            bundle.putBoolean("is_last_chapter", false)
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
            val path = RouterConfig.CATALOGUES_ACTIVITY
            return RouterUtil.navigation(path, bundle)
        } else if ((book.sequence > -1 || book.readed == 1 || NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE
                        && isCached(book)) && BookDaoHelper.getInstance().isBookSubed(book.book_id)) {

            requestItem.fromType = 0
            FootprintUtils.saveHistoryShelf(book)
            bundle.putInt("sequence", book.sequence)
            bundle.putInt("offset", book.offset)
            bundle.putSerializable("book", book)
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
            if (Constants.QG_SOURCE == book.site) {
                requestItem.channel_code = 1
            } else {
                requestItem.channel_code = 2
            }
            val path = RouterConfig.READING_ACTIVITY
            val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.bs_click_one_book)

            return RouterUtil.navigation(path, bundle, flags)
        } else {
            if (shake.check()) {
                return null
            }

            val data = HashMap<String, String>()
            data["BOOKID"] = book.book_id
            when (type) {
                NAVIGATE_TYPE_BOOKSHELF -> data["source"] = "SHELF"
                NAVIGATE_TYPE_DOWNLOAD -> data["source"] = "CACHEMANAGE"
                NAVIGATE_TYPE_RECOMMEND -> data["source"] = "BOOOKDETAIL"
                NAVIGATE_TYPE_READING -> data["source"] = "READPAGE"
            }
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ENTER, data)

            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
            val path = RouterConfig.COVER_PAGE_ACTIVITY
            return RouterUtil.navigation(path, bundle)
        }
    }

    private fun isCached(book: Book): Boolean {
        val index = Math.max(0, book.sequence)
        val bookChapterDao = BookChapterDao(BaseBookApplication.getGlobalContext(), book.book_id)
        val chapterBySequence = bookChapterDao.getChapterBySequence(index) ?: return false
        return if (Constants.QG_SOURCE == book.site) {
            com.quduquxie.network.DataCache.isChapterExists(chapterBySequence.chapter_id, book.book_id)
        } else {
            DataCache.isChapterExists(chapterBySequence)
        }
    }

}