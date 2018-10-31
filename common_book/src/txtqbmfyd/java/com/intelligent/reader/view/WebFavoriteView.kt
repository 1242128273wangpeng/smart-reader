package com.intelligent.reader.view

import com.ding.basic.bean.WebPageFavorite

/**
 * Desc
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/30 17:52
 */
interface WebFavoriteView {

    fun showEmptyView()

    fun showFavoriteList(list: List<WebPageFavorite>)
}