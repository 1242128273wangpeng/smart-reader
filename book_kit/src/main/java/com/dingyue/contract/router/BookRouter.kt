package com.dingyue.contract.router

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.ding.basic.bean.Book
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.util.DataCache
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.db.help.ChapterDaoHelper
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
    const val NAVIGATE_TYPE_RECOMMEND = 2 // 书籍封面页下方推荐模块
    const val NAVIGATE_TYPE_READING = 3
    const val NAVIGATE_TYPE_BOOKEND = 5

    private val shake = AntiShake()

    fun navigateCoverOrRead(activity: Activity, book: Book, type: Int): Any? {

        val bundle = Bundle()

        val localBook = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book.book_id)

        if (localBook != null) {
            book.update_status = 0
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).updateBook(book)
        }

        if (book.readed == -2) {
            bundle.putSerializable("cover", book)
            bundle.putInt("sequence", book.sequence)
            bundle.putBoolean("fromCover", true)
            bundle.putBoolean("is_last_chapter", false)
            val path = RouterConfig.CATALOGUES_ACTIVITY
            return RouterUtil.navigation(activity, path, bundle)

        } else if ((book.sequence > -1 || book.readed == 1 || NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE && isCached(book)) && RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book.book_id) != null) {
            FootprintUtils.saveHistoryShelf(book)

            book.fromType = 0

            bundle.putInt("sequence", book.sequence)
            bundle.putInt("offset", book.offset)

            if (Constants.QG_SOURCE == book.host) {
                book.channel_code = 1
            } else {
                book.channel_code = 2
            }

            bundle.putSerializable("book", book)

            val path = RouterConfig.READER_ACTIVITY
            val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.bs_click_one_book)

            return RouterUtil.navigation(activity, path, bundle, flags)
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
                NAVIGATE_TYPE_BOOKEND -> data["source"] = "BOOKENDPAGE"
            }

            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ENTER, data)

            bundle.putString("book_id", book.book_id)
            bundle.putString("book_source_id", book.book_source_id)
            bundle.putString("book_chapter_id", book.book_chapter_id)


            val path = RouterConfig.COVER_PAGE_ACTIVITY
            return RouterUtil.navigation(activity, path, bundle)
        }
    }

    private fun isCached(book: Book): Boolean {
        val index = Math.max(0, book.sequence)
        val bookChapterDao = ChapterDaoHelper.loadChapterDataProviderHelper(BaseBookApplication.getGlobalContext(), book.book_id)
        val chapter = bookChapterDao.queryChapterBySequence(index) ?: return false

        return DataCache.isChapterCached(chapter)
    }

    /**
     * 针对非book类型的实体类，new一个book对象，传递三个值
     *     val book = Book()
     *     book.book_id = recommendBooks.bookId
     *     book.book_source_id = recommendBooks.id
     *     book.book_chapter_id = recommendBooks.bookChapterId
    BookRouter.navigateCover(this, book)
     */
    fun navigateCover(activity: Activity, book: Book) {
        val bundle = Bundle()
        bundle.putString("book_id", book.book_id)
        bundle.putString("book_source_id", book.book_source_id)
        bundle.putString("book_chapter_id", book.book_chapter_id)

        val path = RouterConfig.COVER_PAGE_ACTIVITY
        return RouterUtil.navigation(activity, path, bundle)
    }

}