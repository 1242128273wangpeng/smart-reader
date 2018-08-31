package com.dingyue.bookshelf

import com.ding.basic.bean.Book
import com.dingyue.contract.util.SharedPreUtil
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
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
    var shareUtil = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT)
    /***
     * 书架点击更多
     * **/
    fun uploadBookShelfMore() {
        val context = BaseBookApplication.getGlobalContext()

        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PAGE_SHELF,
                StartLogClickUtil.ACTION_SHELF_MORE)

        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.bs_click_mine_menu)
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

    /***
     * 书架点击书架排序
     * **/
    fun uploadBookShelfBookSort() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_SHELF, StartLogClickUtil.ACTION_SHELF_BOOK_SORT)
    }

    /***
     * 书架点击书籍
     * **/
    fun uploadBookShelfBookClick(book: Book, position: Int) {
        val data = HashMap<String, String>()
        data["bookid"] = book.book_id
        data["rank"] = (position + 1).toString()

        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_SHELF, StartLogClickUtil.ACTION_SHELF_BOOK_CLICK, data)
    }

    /***
     * 书架点击去书城
     * **/
    fun uploadBookShelfToBookCity() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_SHELF, StartLogClickUtil.ACTION_SHELF_TO_BOOK_CITY)
    }

    /***
     * 书架点击下载管理
     * **/
    fun uploadBookShelfCacheManager() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_SHELF, StartLogClickUtil.ACTION_SHELF_CACHE_MANAGE)
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(),
                StatServiceUtils.bs_click_download_btn)
    }

    /***
     * 书架长按进入书架编辑状态
     * **/
    fun uploadBookShelfLongClickBookShelfEdit() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_SHELF, StartLogClickUtil.ACTION_SHELF_LONG_TIME_BOOK_SHELF_EDIT)
    }




    /***
     * 书架排序弹窗点击 按最近阅读排序、按更新时间排序
     * **/
    fun uploadBookShelfSortCancel() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_SHELF_SORT, StartLogClickUtil.ACTION_SHELF_SORT_CANCEL)
    }

    /***
     * 书架排序弹窗点击 按最近阅读排序、按更新时间排序
     * **/
    fun uploadBookShelfSortType(type: Int) {
        val data = HashMap<String, String>()
        data["type"] = type.toString()
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_SHELF_SORT, StartLogClickUtil.ACTION_SHELF_SORT_BOOK_SORT, data)
    }




    /***
     * 书架编辑点击返回
     * **/
    fun uploadBookShelfEditBack() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_SHELF_EDIT, StartLogClickUtil.ACTION_SHELF_EDIT_BACK)
    }

    /***
     * 书架编辑点击取消
     * **/
    fun uploadBookShelfEditCancel() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_SHELF_EDIT, StartLogClickUtil.ACTION_SHELF_EDIT_CANCEL)
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

        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PAGE_SHELF_EDIT,
                StartLogClickUtil.ACTION_SHELF_EDIT_DELETE, data)
    }

    /***
     * 书架编辑点击全选
     * **/
    fun uploadBookShelfEditSelectAll(all: Boolean) {
        val data = HashMap<String, String>()
        data["type"] = if (all) "1" else "2"

        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_SHELF_EDIT, StartLogClickUtil.ACTION_SHELF_EDIT_SELECT_ALL, data)
    }

    fun uploadFirstOpenBooks(iBookList:List<Book>) {

        //判断用户是否是当日首次打开应用,并上传书架的id
        val lastTime = shareUtil.getLong(Constants.TODAY_FIRST_POST_BOOKIDS, 0)
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
            shareUtil.putLong(Constants.TODAY_FIRST_POST_BOOKIDS, currentTime)
        }
    }

}