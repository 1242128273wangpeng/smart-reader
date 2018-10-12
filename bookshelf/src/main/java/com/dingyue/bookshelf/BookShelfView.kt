package com.dingyue.bookshelf

import com.ding.basic.bean.Book
import com.ding.basic.bean.BookUpdate
import net.lzbook.kit.service.CheckNovelUpdateService

/**
 * Created by qiantao on 2017/11/14 0014
 */
interface BookShelfView {

    fun doUpdateBook(updateService: CheckNovelUpdateService)

    fun onBookListQuery(books: List<Book>?)

    fun onBookDelete(onlyDeleteCache: Boolean)

    fun onSuccessUpdateHandle(updateCount: Int = 0, firstBook: BookUpdate? = null)

    fun onAdRefresh()

}