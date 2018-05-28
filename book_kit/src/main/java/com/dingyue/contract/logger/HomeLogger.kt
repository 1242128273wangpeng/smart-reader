package com.dingyue.contract.logger

import android.preference.PreferenceManager
import com.dingyue.contract.util.SharedPreUtil
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.utils.AppUtils
import java.util.HashMap

/**
 * Desc HomeActivity相关打点
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/12 16:36
 */

object HomeLogger {

    /***
     * 上传书架信息
     * **/
    fun uploadHomeBookListInformation() {
        val books = BookDaoHelper.getInstance().getInitBooksOnLineList()

        if (books.isNotEmpty()) {
            val lastTime = SharedPreUtil.getLong(SharedPreUtil.HOME_TODAY_FIRST_POST_BOOKIDS)
            val currentTime = System.currentTimeMillis()

            val isSameDay = AppUtils.isToday(lastTime, currentTime)

            if (!isSameDay) {
                val bookIdList = StringBuilder()
                books.forEachIndexed { index, book ->
                    bookIdList.append(book.book_id)
                    bookIdList.append(if (book.readed == 1) "_1" else "_0")//1已读，0未读
                    bookIdList.append(if (index == books.size-1) "" else "$")
                }
                SharedPreUtil.putLong(SharedPreUtil.HOME_TODAY_FIRST_POST_BOOKIDS, currentTime)

                val data = HashMap<String, String>()
                data["bookid"] = bookIdList.toString()

                StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                        StartLogClickUtil.PAGE_HOME, StartLogClickUtil.ACTION_HOME_BOOK_LIST, data)
            }
        }
    }

    /***
     * HomeActivity点击书架
     * **/
    fun uploadHomeBookShelfSelected() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_HOME, StartLogClickUtil.ACTION_HOME_BOOK_SHELF)
    }

    /***
     * HomeActivity点击推荐
     * **/
    fun uploadHomeRecommendSelected() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_HOME, StartLogClickUtil.ACTION_HOME_RECOMMEND)
    }

    /***
     * HomeActivity点击榜单
     * **/
    fun uploadHomeRankSelected() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_HOME, StartLogClickUtil.ACTION_HOME_TOP)
    }

    /***
     * HomeActivity点击榜单
     * **/
    fun uploadHomeCategorySelected() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_HOME, StartLogClickUtil.ACTION_HOME_CLASS)
    }

    /***
     * HomeActivity点击 设置/个人中心
     * **/
    fun uploadHomePersonal() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_HOME, StartLogClickUtil.ACTION_HOME_PERSONAL)
    }

    /***
     * HomeActivity点击 搜索
     * **/
    fun uploadHomeSearch() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_HOME, StartLogClickUtil.ACTION_HOME_SEARCH)
    }

    /***
     * HomeActivity点击 下载管理
     * **/
    fun uploadHomeCacheManager() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_HOME, StartLogClickUtil.ACTION_HOME_CACHE_MANAGE)
    }
}