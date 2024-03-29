package com.dingyue.bookshelf.childmvp

import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.net.RequestSubscriber
import com.ding.basic.net.rx.SchedulerHelper
import com.dingyue.bookshelf.BookShelfPresenter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.lzbook.kit.app.base.BaseBookApplication
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils

/**
 * Date: 2018/7/19 19:54
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 书架逻辑层，添加查询当前看的书籍
 */
class ChildBookShelfPresenter(view: ChildBookShelfView) : BookShelfPresenter(view) {

    /**
     * 获取最近阅读的书
     */
    fun queryCurrentReadBook() {

        val currentReadBook: Book?
        var currentTitle: String? = "灵魂跟书籍一起跳舞"
        val json = SPUtils.getDefaultSharedString(SPKey.CURRENT_READ_BOOK)
        currentReadBook = if (json.isEmpty()) {
            null
        } else {
            Gson().fromJson<Book>(json, object : TypeToken<Book>() {}.type)
        }

        if (currentReadBook == null) {
            (view as ChildBookShelfView).onCurrentBookComplete(currentReadBook, currentTitle)
            return
        }
        /**
         * 如果正在阅读书籍不为空这先显示书籍内容，章节标题查询成功后再刷新显示
         */
//        (view as ChildBookShelfView).onCurrentBookComplete(currentReadBook, currentTitle)
        val requestSubscriber = object : RequestSubscriber<List<Chapter>>() {
            override fun requestResult(result: List<Chapter>?) {

                if (currentReadBook.sequence == -1) {
                    currentTitle = "灵魂跟书籍一起跳舞"
                    if (view is ChildBookShelfView) {
                        (view as ChildBookShelfView).onCurrentBookComplete(currentReadBook, currentTitle)
                    }
                } else if (currentReadBook.sequence < result?.size!!) {
                    currentTitle = result[currentReadBook.sequence].name

                    if (view is ChildBookShelfView) {
                        (view as ChildBookShelfView).onCurrentBookComplete(currentReadBook, currentTitle)
                    }
                }

            }

            override fun requestError(message: String) {

            }
        }

        // 查询当前书籍的目录
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .requestCatalog(currentReadBook.book_id,
                        currentReadBook.book_source_id,
                        currentReadBook.book_chapter_id,
                        requestSubscriber, SchedulerHelper.Type_Main)
    }


}