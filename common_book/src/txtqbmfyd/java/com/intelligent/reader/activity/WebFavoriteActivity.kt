package com.intelligent.reader.activity

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.ding.basic.bean.WebPageFavorite
import com.dingyue.statistics.DyStatService
import com.intelligent.reader.R
import com.intelligent.reader.adapter.WebFavoriteAdapter
import com.intelligent.reader.presenter.WebFavoritePresenter
import com.intelligent.reader.view.WebFavoriteView
import kotlinx.android.synthetic.txtqbmfyd.act_web_favorite.*
import kotlinx.android.synthetic.txtqbmfyd.in_bottom_edit.*
import net.lzbook.kit.bean.WebFavoriteUpdateBean
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

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
    private var uploadFirst = true
    private var isReload = false

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
        EventBus.getDefault().register(this)
        initView()
        presenter.initData()
    }

    override fun onResume() {
        super.onResume()
        if (isReload) {
            isReload = false
            presenter.initData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun initView() {
        img_back.setOnClickListener {
            DyStatService.onEvent(EventPoint.WEBCOLLECT_BACK, mapOf("type" to "1"))
            finish()
        }
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
            if (uploadFirst) {
                DyStatService.onEvent(EventPoint.WEBCOLLECT_LINKLIST, mapOf("number" to it.size.toString()))
                uploadFirst = false
            }
        }
    }

    /**
     * 列表点击事件
     */
    private fun onItemClick(position: Int) {
        if (adapter?.remove == false) {
            if (favoriteList != null && !favoriteList!![position].webLink.isBlank()) {
                val data = HashMap<String, String>()
                data["title"] = favoriteList!![position].webTitle
                data["link"] = favoriteList!![position].webLink
                data["rank"] = (position + 1).toString()
                DyStatService.onEvent(EventPoint.WEBCOLLECT_LINKCLICK, data)
                val bundle = Bundle()
                bundle.putString("url", favoriteList!![position].webLink)
                RouterUtil.navigation(this, RouterConfig.WEB_VIEW_ACTIVITY, bundle)
            }
            // 页面跳转
            return
        }
        favoriteList?.let {
            if (it[position].selected && adapter?.selectAll == true) {
                // 取消单项选中时,重置全选状态
                adapter?.selectAll = false
                btn_select_all.text = "全选"
            }
            it[position].selected = it[position].selected.not()
            // 判断是否全部选中
            if (favoriteList?.filter { item -> !item.selected }?.size == 0) {
                adapter?.selectAll = true
                btn_select_all.text = "取消全选"
            }
            refreshUI()
            refreshDeleteButtonState()
        }
    }

    /**
     * 右上角操作按钮 点击事件
     */
    private fun rightClick() {
        if (adapter?.remove == true) {
            DyStatService.onEvent(EventPoint.WEBCHCHEEDIT_CANCLE)
            hideEdit()
        } else {
            DyStatService.onEvent(EventPoint.WEBCOLLECT_CACHEEDIT)
            showEdit()
        }
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
            refreshDeleteButtonState()
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
            // 重置所有选中项
            favoriteList?.map { item -> item.selected = false }
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
        uploadDeleteLog(selectedList)
        presenter.deleteFavorite(selectedList)
    }

    /**
     * 上传删除点位 日志
     */
    private fun uploadDeleteLog(list: List<WebPageFavorite>) {
        val data = HashMap<String, String>()
        data["number"] = list.size.toString()
        val title = StringBuilder()
        list.forEach { title.append(it.webTitle).append("_") }
        title.deleteCharAt(title.length - 1)
        data["title"] = title.toString()
        DyStatService.onEvent(EventPoint.WEBCHCHEEDIT_DELETE, data)
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
            DyStatService.onEvent(EventPoint.WEBCHCHEEDIT_SELECTALL, mapOf("type" to if (it.selectAll) "2" else "1"))
            if (!it.selectAll) {
                // 全选
                btn_select_all.text = "取消全选"
                it.selectAll = true
                btn_delete.isEnabled = true
                btn_delete.text = "删除(${favoriteList!!.size})"
                favoriteList?.map { item -> item.selected = true }
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

    /**
     * 刷新界面通知
     */
    @Subscribe
    fun reLoadDataEvent(bean: WebFavoriteUpdateBean) {
        isReload = true
    }

    override fun onBackPressed() {
        if (adapter?.remove == true) {
            hideEdit()
            return
        }
        super.onBackPressed()
    }
}