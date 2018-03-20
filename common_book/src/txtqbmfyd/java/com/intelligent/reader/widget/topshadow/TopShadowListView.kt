package com.intelligent.reader.widget.topshadow

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.widget.AbsListView
import android.widget.ListView

/**
 * Desc 控制顶部阴影显示的 ListView
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/16 0016 15:09
 */
class TopShadowListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ListView(context, attrs, defStyleAttr) {

    var topShadow: View? = null

    private var distance = 10

    private val recordSp = SparseArray<ItemRecord>(0)
    private var currentFirstVisibleItem = 0

    init {
        val density = resources.displayMetrics.density//屏幕密度
        distance = (distance * density + 0.5f).toInt()
        setOnScrollListener(object : OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}

            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                topShadow?.let {
                    currentFirstVisibleItem = firstVisibleItem
                    val firstView = view?.getChildAt(0)
                    if (firstView != null) {
                        var itemRecord: ItemRecord? = recordSp.get(firstVisibleItem)
                        if (itemRecord == null) {
                            itemRecord = ItemRecord()
                        }
                        itemRecord.height = firstView.height
                        itemRecord.top = firstView.top

                        recordSp.append(firstVisibleItem, itemRecord)

                        val h = getListScrollY()//滚动距离
                        if (h > distance) {
                            it.visibility = View.VISIBLE
                        } else {
                            it.visibility = View.GONE
                        }
                    }
                }
            }


        })
    }

    private fun getListScrollY(): Int {
        var height = 0
        for (i in 0 until currentFirstVisibleItem) {
            val itemRecord = recordSp.get(i)
            height += itemRecord.height
        }
        var itemRecord: ItemRecord? = recordSp.get(currentFirstVisibleItem)
        if (itemRecord == null) {
            itemRecord = ItemRecord()
        }
        return height - itemRecord.top
    }

    inner class ItemRecord {
        var height = 0
        var top = 0
    }

}