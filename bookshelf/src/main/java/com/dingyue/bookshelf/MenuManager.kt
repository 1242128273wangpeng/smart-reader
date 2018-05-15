package com.dingyue.bookshelf

import net.lzbook.kit.data.bean.Book

/**
 * Desc 菜单管理器
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/25 0025 11:29
 */
interface MenuManager {

    fun showRemoveMenu()

    fun dismissRemoveMenu()

    fun isRemoveMenuShow(): Boolean

    fun selectAll(isAll: Boolean)

    fun sortBooks(type: Int)

    fun deleteBooks(books: ArrayList<Book>, isDeleteCacheOnly: Boolean)

}