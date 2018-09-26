package net.lzbook.kit.utils.logger

import com.ding.basic.repository.RequestRepositoryFactory
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.sp.SPKey
import net.lzbook.kit.utils.sp.SPUtils
import java.util.*

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
        val books = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBooks()

        if (books != null && books.isNotEmpty()) {
            val lastTime = SPUtils.getDefaultSharedLong(SPKey.HOME_TODAY_FIRST_POST_BOOKIDS)
            val currentTime = System.currentTimeMillis()

            val isSameDay = AppUtils.isToday(lastTime, currentTime)

            if (!isSameDay) {
                val bookIdList = StringBuilder()
                books.forEachIndexed { index, book ->
                    bookIdList.append(book.book_id)
                    bookIdList.append(if (book.readed == 1) "_1" else "_0")//1已读，0未读
                    bookIdList.append(if (index == books.size-1) "" else "$")
                }
                SPUtils.putDefaultSharedLong(SPKey.HOME_TODAY_FIRST_POST_BOOKIDS, currentTime)

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

    fun uploadHomeSearch(currPageType: Int) {
        if (currPageType == 2) {
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                    StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.QG_TJY_SEARCH)
        } else if (currPageType == 3) {
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                    StartLogClickUtil.TOP_PAGE, StartLogClickUtil.QG_BDY_SEARCH)
        } else if (currPageType == 4) {
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                    StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.QG_FL_SEARCH)
        } else {
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                    StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.SEARCH)
        }
    }

    /***
     * HomeActivity点击 下载管理
     * **/
    fun uploadHomeCacheManager() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_HOME, StartLogClickUtil.ACTION_HOME_CACHE_MANAGE)
    }
}