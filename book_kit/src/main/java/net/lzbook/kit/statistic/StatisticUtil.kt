package net.lzbook.kit.statistic

import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.statistic.model.Search

/**
 * Created by xian on 2017/7/3.
 */

fun buildSearch(book: RequestItem, keyword: String, op: Search.OP, useTime: Long): Search {
    val search = Search()

    search.book_id = book.book_id
    search.book_code = if (Constants.QG_SOURCE.equals(book.host)) "1" else "0"
    search.book_source_id = book.book_source_id

    search.cost_time = useTime
    search.keyword = keyword
    search.op = op.name
    search.s_order = -1
    return search
}

fun buildSearch(book: Book, keyword: String, op: Search.OP, useTime: Long): Search {
    val search = Search()

    search.book_id = book.book_id
    search.book_code = if (Constants.QG_SOURCE.equals(book.site)) "1" else "0"
    search.book_source_id = book.book_source_id

    search.cost_time = useTime
    search.keyword = keyword
    search.op = op.name
    search.s_order = -1
    return search
}

fun buildSearch(keyword: String, op: Search.OP, useTime: Long): Search {
    val search = Search()

//    search.book_id = book.book_id
//    search.book_code = if(Constants.QG_SOURCE.equals(book.site)) "1" else "0"
//    search.book_source_id = book.book_source_id

    search.cost_time = useTime
    search.keyword = keyword
    search.op = op.name
    search.s_order = -1
    return search
}