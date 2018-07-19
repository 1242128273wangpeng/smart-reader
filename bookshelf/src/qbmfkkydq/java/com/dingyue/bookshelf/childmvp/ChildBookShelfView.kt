package com.dingyue.bookshelf.childmvp

import com.ding.basic.bean.Book
import com.dingyue.bookshelf.BookShelfView

/**
 * Date: 2018/7/19 19:59
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 书架view层，添加当前阅读书籍成功回调
 */
interface ChildBookShelfView : BookShelfView {

    fun onCurrentBookCommplete(book: Book, title: String?)
}