package com.intelligent.reader.activity

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.ding.basic.bean.WebPageFavorite
import com.intelligent.reader.R
import com.intelligent.reader.adapter.WebFavoriteAdapter
import com.intelligent.reader.presenter.WebFavoritePresenter
import com.intelligent.reader.view.WebFavoriteView
import kotlinx.android.synthetic.txtqbmfyd.act_web_favorite.*
import kotlinx.android.synthetic.txtqbmfyd.in_bottom_edit.*
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil

/**
 * Desc 网页收藏Activity
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/30 16:32
 */
class WebFavoriteActivity : BaseCacheableActivity(), WebFavoriteView {

    private val presenter by lazy { WebFavoritePresenter(this, this) }

    private var favoriteList: List<WebPageFavorite>? = null
    private var adapter: WebFavoriteAdapter? = null

    override fun showEmptyView() {
        rv_favorite_list.visibility = View.GONE
        rl_empty_view.visibility = View.VISIBLE
        txt_right_handle.visibility = View.GONE
        hideEdit()
    }

    override fun showFavoriteList(list: List<WebPageFavorite>) {
        rl_empty_view.visibility = View.GONE
        rv_favorite_list.visibility = View.VISIBLE
        txt_right_handle.visibility = View.VISIBLE
        favoriteList = list
        refreshUI()
        hideEdit()
    }

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_web_favorite)
        initView()
        presenter.initData()
    }

    private fun initView() {
        img_back.setOnClickListener { finish() }
        txt_right_handle.setOnClickListener { rightClick() }
        btn_select_all.setOnClickListener { selectAllOrNot() }
        btn_delete.isEnabled = false
        btn_delete.setOnClickListener { deleteClick() }
        rv_favorite_list.layoutManager = LinearLayoutManager(this)
    }

    /**
     * 刷新UI
     */
    private fun refreshUI() {
        favoriteList?.let {
            if (adapter == null) {
                adapter = WebFavoriteAdapter(this)
                adapter!!.favoriteClick = { position -> onItemClick(position) }
                adapter!!.favoriteLongClick = { showEdit() }
                adapter!!.setData(it)
                rv_favorite_list.adapter = adapter
            } else {
                adapter?.setData(it)
                adapter?.notifyDataSetChanged()
            }
        }
    }

    /**
     * 列表点击事件
     */
    private fun onItemClick(position: Int) {
        if (adapter?.remove == false) {
            if (favoriteList != null && !favoriteList!![position].webLink.isBlank()) {
                val bundle = Bundle()
                bundle.putString("url", favoriteList!![position].webLink)
                RouterUtil.navigation(this, RouterConfig.WEB_VIEW_ACTIVITY, bundle)
                finish()
            }
            // 页面跳转
            return
        }
        favoriteList?.let {
            it[position].selected = it[position].selected.not()
            refreshUI()
            refreshDeleteButtonState()
        }
    }

    /**
     * 右上角操作按钮 点击事件
     */
    private fun rightClick() {
        if (adapter?.remove == true) hideEdit() else showEdit()
    }

    /**
     * 显示编辑功能
     */
    private fun showEdit() {
        adapter?.let {
            if (it.remove) return
            txt_right_handle.text = "完成"
            txt_right_handle.setTextColor(Color.parseColor("#19DD8B"))
            img_back.visibility = View.GONE
            it.remove = true
            it.notifyDataSetChanged()
            in_bottom_menu.visibility = View.VISIBLE
        }
    }

    /**
     * 隐藏编辑功能
     */
    private fun hideEdit() {
        adapter?.let {
            if (!it.remove) return
            txt_right_handle.text = "编辑"
            txt_right_handle.setTextColor(Color.parseColor("#5D646E"))
            img_back.visibility = View.VISIBLE
            it.remove = false
            it.notifyDataSetChanged()
            in_bottom_menu.visibility = View.GONE
        }
    }

    /**
     * 点击删除按钮
     */
    private fun deleteClick() {
        val selectedList = if (adapter?.selectAll == true) favoriteList else favoriteList?.filter { it.selected }
        if (selectedList == null || selectedList.isEmpty()) return
        presenter.deleteFavorite(selectedList)
    }

    /**
     * 刷新删除按钮状态
     */
    private fun refreshDeleteButtonState() {
        val selectedCount = favoriteList?.count { it.selected }
        btn_delete.isEnabled = selectedCount != 0
        btn_delete.text = if (selectedCount == 0) "删除" else "删除($selectedCount)"

    }

    /**
     * 全选 / 取消全选
     */
    private fun selectAllOrNot() {
        adapter?.let {
            if (!it.selectAll) {
                // 全选
                btn_select_all.text = "取消全选"
                it.selectAll = true
                btn_delete.isEnabled = true
                btn_delete.text = "删除(${favoriteList!!.size})"
                refreshUI()
            } else {
                // 取消全选
                btn_select_all.text = "全选"
                it.selectAll = false
                btn_delete.isEnabled = false
                btn_delete.text = "删除"
                favoriteList?.map { item -> item.selected = false }
                refreshUI()
            }
        }
    }

    override fun onBackPressed() {
        if (adapter?.remove == true) {
            hideEdit()
            return
        }
        super.onBackPressed()
    }
}