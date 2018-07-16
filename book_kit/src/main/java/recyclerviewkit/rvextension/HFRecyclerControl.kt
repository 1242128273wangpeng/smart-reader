package recyclerviewkit.rvextension

import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Date: 2018/7/16 11:43
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: RecyclerView 添加Header和Footer
 * @see HFRecyclerAdapter 的外部控制类
 */
class HFRecyclerControl {
    private var recyclerView: RecyclerView? = null

    private val hfAdapter: HFRecyclerAdapter by lazy{
        HFRecyclerAdapter()
    }



    fun setAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        this.recyclerView = recyclerView
        hfAdapter.setAdapter(adapter)
        recyclerView.adapter = hfAdapter

    }

    fun addHeaderView(header: View) {
        hfAdapter.addHeaderView(header)
    }

    fun addFooterView(footer: View) {
        hfAdapter.addFooterView(footer)
    }

    fun getHeaderCount(): Int {
        return hfAdapter.getHeaderViewsCount()
    }

    fun getFooterCount(): Int {
        return hfAdapter.getFooterViewsCount()
    }

    fun getRecyclerView(): RecyclerView? {
        return recyclerView
    }
}
