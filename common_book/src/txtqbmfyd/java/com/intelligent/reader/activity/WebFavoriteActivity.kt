package com.intelligent.reader.activity

import android.os.Bundle
import android.view.View
import com.ding.basic.bean.WebPageFavorite
import com.intelligent.reader.R
import com.intelligent.reader.presenter.WebFavoritePresenter
import com.intelligent.reader.view.WebFavoriteView
import kotlinx.android.synthetic.txtqbmfyd.act_web_favorite.*
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity

/**
 * Desc 网页收藏Activity
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/30 16:32
 */
class WebFavoriteActivity : BaseCacheableActivity(), WebFavoriteView {

    private val presenter by lazy { WebFavoritePresenter(this, this) }

    override fun showEmptyView() {
        rv_favorite_list.visibility = View.GONE
        ll_empty_view.visibility = View.VISIBLE
    }

    override fun showFavoriteList(list: List<WebPageFavorite>) {
        ll_empty_view.visibility = View.GONE
        rv_favorite_list.visibility = View.VISIBLE

    }

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_web_favorite)
        img_back.setOnClickListener { finish() }
        txt_right_handle.setOnClickListener { rightClick() }
        presenter.initData()
    }

    private fun rightClick() {


    }
}