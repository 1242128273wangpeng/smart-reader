package com.intelligent.reader.presenter.bookshelf

import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.BookUpdate

/**
 * Created by qiantao on 2017/11/14 0014
 */
interface BookShelfView {

    fun doUpdateBook(updateService: CheckNovelUpdateService)

    fun notification(gid: String)

    fun onBookListQuery(bookList: ArrayList<Book>)

    fun onBookDelete()

    fun onSuccessUpdateHandle(updateCount: Int = 0, firstBook: BookUpdate? = null)

    fun onAdRefresh()

}