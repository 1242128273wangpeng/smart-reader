package com.dingyue.bookshelf

import net.lzbook.kit.data.bean.Book

/**
 * Desc 菜单管理器
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/25 0025 11:29
 */
interface BookShelfMenuManager {

    fun showBookShelfRemovePopup()

    fun dismissBookShelfRemovePopup()

    fun handleCheckAllAction(all: Boolean)

    fun handleSortBooksAction(type: Int)

    fun handleRemoveShelfAction(books: ArrayList<Book>)

}