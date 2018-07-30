package com.dingyue.bookshelf

/**
 * Desc 提供给 HomeActivity 去实现的接口
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/12 0012 11:39
 */
interface BookShelfInterface {

    fun changeHomeNavigationState(state: Boolean)

    fun changeHomePagerIndex(index: Int)

    fun changeDrawerLayoutState()

}