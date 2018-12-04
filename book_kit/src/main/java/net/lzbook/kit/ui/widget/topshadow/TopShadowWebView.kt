package net.lzbook.kit.ui.widget.topshadow

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView

/**
 * Desc 控制顶部阴影显示的 WebView
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/16 0016 15:09
 */
class TopShadowWebView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : WebView(context, attrs, defStyleAttr) {

    var topShadow: View? = null

    private var distance = 10

    init {
        val density = resources.displayMetrics.density//屏幕密度
        distance = (distance * density + 0.5f).toInt()

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(true)
        }*/
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        topShadow?.let {
            if (t > distance) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }
}