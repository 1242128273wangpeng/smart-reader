package com.intelligent.reader.presenter

import android.content.Context
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.WebPageFavorite
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
        var list = requestRepository.getAllWebFavorite()
        list = ArrayList()
        // TODO 模拟数据
        (0..5).map {
            val item = WebPageFavorite()
            item.id = it
            item.webTitle = "百度地址:xxxxxx_$it"
            item.webLink = "https://wwww.baiduxxxxxx_$it.com"
            item.createTime = System.currentTimeMillis()
            list.add(item)
        }
        if (list.isEmpty()) {
            // 显示无数据页面
            view.showEmptyView()
        } else {
            // 展示数据
            view.showFavoriteList(list)
        }
    }


}