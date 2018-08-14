package com.intelligent.reader.widget.topshadow

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView

/**
 * Desc 控制顶部阴影显示的 WebView
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/16 0016 15:09
 */
class TopShadowScrollView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ScrollView(context, attrs, defStyleAttr) {

    var topShadow: View? = null

    private var distance = 10

    var scrollChanged : ((y :Int) -> Unit)? = null

    init {
        val density = resources.displayMetrics.density//屏幕密度
        distance = (distance * density + 0.5f).toInt()
    }


    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        scrollChanged?.invoke(t)
        topShadow?.let {
            if (t > distance) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }
}