package com.dingyue.bookshelf.childmvp

import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import com.ding.basic.rx.SchedulerHelper
import com.dingyue.bookshelf.BookShelfPresenter
import com.dingyue.bookshelf.BookShelfView
import com.dingyue.contract.CommonContract
import net.lzbook.kit.app.BaseBookApplication
import java.util.*

/**
 * Date: 2018/7/19 19:54
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 书架逻辑层，添加查询当前看的书籍
 */
class ChildBookShelfPresenter(view:ChildBookShelfView) : BookShelfPresenter(view) {
    var currentReadBook: Book? = null
    var currentTitle: String? = ""
    /**
     * 获取最近阅读的书
     */
    fun queryCurrentReadBook() {
        val readBooks = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadReadBooks()
        if (readBooks == null || readBooks.size < 1) {
            return
        }
        Collections.sort(readBooks, CommonContract.MultiComparator(0))
        currentReadBook = readBooks[0]





        val requestSubscriber = object : RequestSubscriber<List<Chapter>>() {
            override fun requestResult(result: List<Chapter>?) {

                if(currentReadBook?.sequence!! < result?.size!!){
                    currentTitle = result?.get(currentReadBook!!.sequence)?.name
                    if (view is ChildBookShelfView){
                        (view as ChildBookShelfView).onCurrentBookCommplete(currentReadBook!!,currentTitle)
                    }
                }

            }

            override fun requestError(message: String) {

            }
        }
//
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .requestCatalog(currentReadBook!!.book_id,
                        currentReadBook!!.book_source_id,
                        currentReadBook!!.book_chapter_id,
                        requestSubscriber, SchedulerHelper.Type_Main)
    }


}