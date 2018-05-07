package com.dingyue.downloadmanager

import net.lzbook.kit.data.bean.Book

/**
 * Desc 菜单管理器
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/25 0025 11:29
 */
interface MenuManager {

    fun showMenu()

    fun dismissMenu()

    fun checkAll(isAll: Boolean)

    fun sortBooks(type: Int)

    fun deleteCache(books: ArrayList<Book>)

}