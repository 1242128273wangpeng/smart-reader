package com.dingyue.bookshelf

import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.util.sp.SPUtils
import com.dingyue.statistics.DyStatService
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.StatServiceUtils
import java.util.*

/**
 * Desc 书架相关打点
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/11 10:33
 */
object BookShelfLogger {

    /***
     * 书架点击更多
     * **/
    fun uploadBookShelfMore() {
        DyStatService.onEvent(EventPoint.SHELF_MORE)
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.bs_click_mine_menu)
    }

    /***
     * 书架点击书架排序
     * **/
    fun uploadBookShelfBookSort() {
        DyStatService.onEvent(EventPoint.SHELF_BOOKSORT)
    }

    /***
     * 书架点击书籍
     * **/
    fun uploadBookShelfBookClick(book: Book, position: Int) {
        DyStatService.onEvent(EventPoint.SHELF_BOOKCLICK, mapOf("bookid" to book.book_id, "rank" to (position + 1).toString()))
    }

    /***
     * 书架点击去书城
     * **/
    fun uploadBookShelfToBookCity() {
        DyStatService.onEvent(EventPoint.SHELF_TOBOOKCITY)
    }

    /***
     * 书架点击下载管理
     * **/
    fun uploadBookShelfCacheManager() {
        DyStatService.onEvent(EventPoint.SHELF_CACHEMANAGE)
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(),
                StatServiceUtils.bs_click_download_btn)
    }

    /***
     * 书架点击应用分享
     * **/
    fun uploadBookShelfShare() {
        DyStatService.onEvent(EventPoint.SHELF_CACHEMANAGE)
    }

    /***
     * 书架长按进入书架编辑状态
     * **/
    fun uploadBookShelfLongClickBookShelfEdit() {
        DyStatService.onEvent(EventPoint.SHELF_LONGTIMEBOOKSHELFEDIT)
    }

    /***
     * 书架排序弹窗点击 按最近阅读排序、按更新时间排序
     * **/
    fun uploadBookShelfSortCancel() {
        DyStatService.onEvent(EventPoint.SHELFSORT_CANCLE)
    }

    /***
     * 书架排序弹窗点击 按最近阅读排序、按更新时间排序
     * **/
    fun uploadBookShelfSortType(type: Int) {
        val data = HashMap<String, String>()
        data["type"] = type.toString()
        DyStatService.onEvent(EventPoint.SHELFSORT_BOOKSORT)
    }


    /***
     * 书架编辑点击返回
     * **/
    fun uploadBookShelfEditBack() {
        DyStatService.onEvent(EventPoint.SHELFEDIT_BACK, mapOf("type" to "1"))
    }

    /***
     * 书架编辑点击取消
     * **/
    fun uploadBookShelfEditCancel() {
        DyStatService.onEvent(EventPoint.SHELFEDIT_CANCLE)
    }

    /***
     * 书架编辑点击删除
     * **/
    fun uploadBookShelfEditDelete(size: Int, message: StringBuilder?, onlyDeleteCache: Boolean) {
        val context = BaseBookApplication.getGlobalContext()

        val data = HashMap<String, String>()

        if (size == 0) {
            data["type"] = "2"
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.bs_click_delete_cancel_btn)
        } else {
            data["type"] = "1"
            data["number"] = size.toString()
            data["bookids"] = message.toString()
            data["status"] = if (onlyDeleteCache) "1" else "2"
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.bs_click_delete_ok_btn)
        }

        DyStatService.onEvent(EventPoint.SHELFEDIT_DELETE, data)
    }

    /***
     * 书架编辑点击全选
     * **/
    fun uploadBookShelfEditSelectAll(all: Boolean) {
        DyStatService.onEvent(EventPoint.SHELFEDIT_SELECTALL, mapOf("type" to if (all) "1" else "2"))
    }


    /***
     * 书架点击搜索
     * **/
    fun uploadBookShelfSearch() {
        val context = BaseBookApplication.getGlobalContext()

        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PAGE_SHELF,
                StartLogClickUtil.ACTION_SHELF_SEARCH)

        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.bs_click_search_btn)
    }

    /***
     * 书架点击 设置/个人中心
     * **/
    fun uploadBookShelfPersonal() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_SHELF, StartLogClickUtil.ACTION_SHELF_PERSONAL)
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(),
                StatServiceUtils.bs_click_mine_menu)
    }

    fun uploadFirstOpenBooks() {

        //判断用户是否是当日首次打开应用,并上传书架的id
        val lastTime = SPUtils.getDefaultSharedLong(Constants.TODAY_FIRST_POST_BOOKIDS, 0)
        val currentTime = System.currentTimeMillis()

        val isSameDay = AppUtils.isToday(lastTime, currentTime)
        if (!isSameDay) {
            val books = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBooks()
            val bookIdList = StringBuilder()
            books?.forEachIndexed { index, book ->
                bookIdList.append(book.book_id)
                bookIdList.append(if (book.readed == 1) "_1" else "_0")//1已读，0未读
                bookIdList.append(if (index == books.size) "" else "$")
            }
            val data = HashMap<String, String>()
            data.put("bookid", bookIdList.toString())
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                    StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.BOOKLIST, data)
            SPUtils.putDefaultSharedLong(Constants.TODAY_FIRST_POST_BOOKIDS, currentTime)
        }
    }

    /**
     * 本地导入点位
     */
    fun uploadBookShelfLocalImport() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_SHELF, StartLogClickUtil.SHELF_INSERT)
    }
}