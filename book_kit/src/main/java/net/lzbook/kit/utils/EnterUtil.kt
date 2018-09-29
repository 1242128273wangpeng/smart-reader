package net.lzbook.kit.utils

import android.app.Activity
import android.os.Bundle
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil


/**
 * Desc：进入封面页
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/27 0027 15:01
 */
fun Activity.enterCover(author: String = "", book_id: String = "", book_source_id: String = "", book_chapter_id: String = "") {
    val bundle = Bundle()
    bundle.putString("author", author)
    bundle.putString("book_id", book_id)
    bundle.putString("book_source_id", book_source_id)
    bundle.putString("book_chapter_id", book_chapter_id)
    RouterUtil.navigation(this, RouterConfig.COVER_PAGE_ACTIVITY, bundle)
}

/**
 * 进入搜索页
 */
fun Activity.enterSearch(word: String = "", search_type: String = "", filter_type: String = "", filter_word: String = "", sort_type: String = "", from_class: String = "") {
    val bundle = Bundle()
    bundle.putString("word", word)
    bundle.putString("search_type", search_type)
    bundle.putString("filter_type", filter_type)
    bundle.putString("filter_word", filter_word)
    bundle.putString("sort_type", sort_type)
    bundle.putString("from_class", from_class)
    RouterUtil.navigation(this, RouterConfig.SEARCH_BOOK_ACTIVITY, bundle)
}

fun Activity.enterSearch() {
    RouterUtil.navigation(this, RouterConfig.SEARCH_BOOK_ACTIVITY)
}



