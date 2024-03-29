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
        val list = requestRepository.getAllWebFavorite()
        if (list.isEmpty()) {
            // 显示无数据页面
            view.showEmptyView()
        } else {
            // 展示数据
            view.showFavoriteList(list)
        }
    }

    /**
     * 删除收藏
     */
    fun deleteFavorite(list: List<WebPageFavorite>) {
        list.forEach { requestRepository.deleteWebFavoriteById(it.id) }
        initData()
    }

    fun initTempData() {
        (0..15).map {
            val item = WebPageFavorite()
            item.webTitle = "百度地址:xxxxxx_$it"
            item.webLink = "https://wwww.baiduxxxxxx_$it.com"
            item.createTime = System.currentTimeMillis()
            requestRepository.addWebFavorite(item)
        }
    }

}