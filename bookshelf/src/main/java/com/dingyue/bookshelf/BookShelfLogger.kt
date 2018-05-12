package com.dingyue.bookshelf

import android.content.SharedPreferences
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.StatServiceUtils
import java.util.*

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/11 10:33
 */
object BookShelfLogger {

    fun uploadShelfEditCancelLog() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.CANCLE1)
    }

    fun uploadEditorSelectAllLog(all: Boolean) {
        val data = HashMap<String, String>()
        data["type"] = if (all) "2" else "1"
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.SELECTALL1, data)
    }

    /***
     * 书架排序日志
     * **/
    fun uploadSortingLog(type: Int) {
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.me_set_cli_shelf_rak_time)
    }

    fun uploadFirstOpenLog(books: ArrayList<Book>, sp: SharedPreferences) {
        //判断用户是否是当日首次打开应用,并上传书架的id
        val lastTime = sp.getLong(Constants.TODAY_FIRST_POST_BOOKIDS, 0)
        val currentTime = System.currentTimeMillis()

        val isSameDay = AppUtils.isToday(lastTime, currentTime)
        if (!isSameDay) {
            val bookIdList = StringBuilder()
            books.forEachIndexed { index, book ->
                bookIdList.append(book.book_id)
                bookIdList.append(if (book.readed == 1) "_1" else "_0")//1已读，0未读
                bookIdList.append(if (index == books.size) "" else "$")
            }
            val data = HashMap<String, String>()
            data.put("bookid", bookIdList.toString())
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                    StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.BOOKLIST, data)
            sp.edit().putLong(Constants.TODAY_FIRST_POST_BOOKIDS, currentTime).apply()
        }
    }

    fun uploadItemClickLog(books: ArrayList<Book>,position: Int) {
        val data = HashMap<String, String>()
        data.put("bookid", books[position].book_id)
        data.put("rank", (position + 1).toString())
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.BOOKCLICK, data)
    }

    fun uploadItemLongClickLog() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.LONGTIMEBOOKSHELFEDIT)
    }

    fun uploadBookCacheDeleteLog(sb: StringBuffer, size: Int, isOnlyDeleteCache: Boolean) {
        val data = HashMap<String, String>()
        data["type"] = "1"
        data["number"] = size.toString()
        data["bookids"] = sb.toString()
        data["status"] = if (isOnlyDeleteCache) "1" else "2"
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.DELETE1, data)
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(),
                StatServiceUtils.bs_click_delete_ok_btn)
    }

    fun uploadBookDeleteCancelLog() {
        val data = HashMap<String, String>()
        data["type"] = "2"
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELFEDIT, StartLogClickUtil.DELETE1, data)
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(),
                StatServiceUtils.bs_click_delete_cancel_btn)
    }


    fun uploadHeadSettingLog() {
        val context = BaseBookApplication.getGlobalContext()
        StartLogClickUtil.upLoadEventLog(context,
                StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.PERSONAL)
        net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(context,
                net.lzbook.kit.utils.StatServiceUtils.bs_click_mine_menu)
    }


    fun uploadHeadSearchLog(bottomType: Int) {
        val context = BaseBookApplication.getGlobalContext()
        when (bottomType) {
            2 -> StartLogClickUtil.upLoadEventLog(context,
                    StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.QG_TJY_SEARCH)
            3 -> StartLogClickUtil.upLoadEventLog(context,
                    StartLogClickUtil.TOP_PAGE, StartLogClickUtil.QG_BDY_SEARCH)
            4 -> StartLogClickUtil.upLoadEventLog(context,
                    StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.QG_FL_SEARCH)
            else -> StartLogClickUtil.upLoadEventLog(context,
                    StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.SEARCH)
        }
        net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(context,
                net.lzbook.kit.utils.StatServiceUtils.bs_click_search_btn)
    }

    fun uploadDownloadManagerLog() {
        val context = BaseBookApplication.getGlobalContext()
        net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(context,
                net.lzbook.kit.utils.StatServiceUtils.bs_click_download_btn)
        StartLogClickUtil.upLoadEventLog(context,
                StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.CACHEMANAGE)
    }


    fun uploadBookSortingLog() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.BOOKSORT)
    }

}