package com.dingyue.downloadmanager

/**
 * Desc 菜单管理器
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/25 0025 11:29
 */
interface MenuManager {

    fun showMenu()

    fun dismissMenu()

    fun checkAll(all: Boolean)

    fun sortBooks(type: Int)

    fun deleteCache(books: ArrayList<Book>)

}