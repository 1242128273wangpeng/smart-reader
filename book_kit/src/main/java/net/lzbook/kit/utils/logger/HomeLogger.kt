package net.lzbook.kit.utils.logger

import com.ding.basic.RequestRepositoryFactory
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.utils.AppUtils
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.statistics.DyStatService
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.pointpage.EventPoint

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
                    bookIdList.append(if (index == books.size - 1) "" else "$")
                }
                SPUtils.putDefaultSharedLong(SPKey.HOME_TODAY_FIRST_POST_BOOKIDS, currentTime)

                DyStatService.onEvent(EventPoint.MAIN_BOOKLIST, mapOf("bookid" to bookIdList.toString()))
            }
        }
    }

    /***
     * HomeActivity点击书城
     * **/
    fun uploadHomeStoreSelected() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_HOME, StartLogClickUtil.ACTION_HOME_BOOKSTORE)
    }


    /***
     * HomeActivity点击书架
     * **/
    fun uploadHomeBookShelfSelected() {
        DyStatService.onEvent(EventPoint.MAIN_BOOKSHELF)
    }

    /***
     * HomeActivity点击推荐
     * **/
    fun uploadHomeRecommendSelected() {
        DyStatService.onEvent(EventPoint.MAIN_RECOMMEND)
    }

    /***
     * HomeActivity点击榜单
     * **/
    fun uploadHomeRankSelected() {
        DyStatService.onEvent(EventPoint.MAIN_TOP)
    }

    /***
     * HomeActivity点击分类
     * **/
    fun uploadHomeCategorySelected() {
        DyStatService.onEvent(EventPoint.MAIN_CLASS)
    }

    /***
     * HomeActivity点击 设置/个人中心
     * **/
    fun uploadHomePersonal() {
        DyStatService.onEvent(EventPoint.MAIN_PERSONAL)
    }

    fun uploadHomeSearch(currPageType: Int) {
        when (currPageType) {
            2 -> DyStatService.onEvent(EventPoint.RECOMMEND_SEARCH)
            3 -> DyStatService.onEvent(EventPoint.TOP_SEARCH)
            4 -> DyStatService.onEvent(EventPoint.CLASS_SEARCH)
            else -> DyStatService.onEvent(EventPoint.MAIN_SEARCH)
        }
    }

    /***
     * HomeActivity点击 下载管理
     * **/
    fun uploadHomeCacheManager() {
        DyStatService.onEvent(EventPoint.MAIN_CACHEMANAGE)
    }
}