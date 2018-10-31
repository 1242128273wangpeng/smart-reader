package com.intelligent.reader.presenter

import android.content.Context
import com.ding.basic.RequestRepositoryFactory
import com.intelligent.reader.view.WebFavoriteView

/**
 * Desc
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/30 17:51
 */
class WebFavoritePresenter(private val view: WebFavoriteView, private val context: Context) {

    private val requestRepository by lazy { RequestRepositoryFactory.loadRequestRepositoryFactory(context) }

    /**
     * 获取收藏列表
     */
    fun initData() {
        val list = requestRepository.getAllWebFavorite()
        if (list.isEmpty()) {
            // 显示无数据页面
            view.showEmptyView()
        } else {
            // 展示数据
            view.showFavoriteList(list)
        }
    }



}