package net.lzbook.kit.widget.topshadow

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

/**
 * Desc 控制顶部阴影显示的 RecyclerView
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/16 0016 15:09
 */
class TopShadowRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {

    var topShadow: View? = null

    private var distance = 10

    init {
        val density = resources.displayMetrics.density//屏幕密度
        distance = (distance * density + 0.5f).toInt()
    }

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        topShadow?.let {
            val layoutManager = layoutManager
            if (layoutManager is LinearLayoutManager) {
                val distance = getScrollDistance(layoutManager)
                if (distance > this.distance) {
                    it.visibility = View.VISIBLE
                } else {
                    it.visibility = View.GONE
                }
            }
        }
    }

    private fun getScrollDistance(layoutManager: LinearLayoutManager): Int {
        val position = layoutManager.findFirstVisibleItemPosition()
        val visibleChildView = layoutManager.findViewByPosition(position) ?: return 0
        //获取Item的宽
        val itemHeight = visibleChildView.height
        //算出该Item还未移出屏幕的高度
        val itemTop = visibleChildView.top
        //position移出屏幕的数量*高度得出移动的距离
        val itemPosition = position * itemHeight
        //因为横着的RecyclerV第一个取到的Item position为零所以计算时需要加一个宽
        return itemPosition - itemTop
    }

}