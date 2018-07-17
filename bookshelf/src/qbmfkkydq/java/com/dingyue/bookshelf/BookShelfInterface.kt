package com.dingyue.bookshelf

/**
 * Desc 提供给 HomeActivity
 * Author zhenxiang
 * 2018\5\15 0015
 */

interface BookShelfInterface {

    fun changeHomeNavigationState(state: Boolean)

    fun changeHomePagerIndex(index: Int)

    fun checkShowShelfGuide()
}