package com.dingyue.bookshelf

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.dy.media.IMediaControl
import com.dy.media.MediaControl

/**
 * Desc 请描述这个文件
 * Author Xian
 * Mail dongxian_zhang@dingyuegroup.cn
 * Date 2018/8/16 06:47
 */
class BookShelfADView : FrameLayout {
    val activity: Activity

    constructor(act: Activity) : super(act) {
        this.activity = act
        // 修复广告拉取失败时导致无法下拉刷新的问题
        this.minimumHeight = 1
    }

    val preDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            loadAD()
            viewTreeObserver.removeOnPreDrawListener(this)
            return true
        }
    }

    private fun loadAD() {
        MediaControl.loadBookShelMedia2(activity, object : IMediaControl.MediaCallback {
            override fun requestMediaRepairSuccess(views: List<ViewGroup>) {

            }

            override fun requestMediaSuccess(views: List<ViewGroup>) {
                if (isShown) {
                    removeAllViews()
                    addView(views[0], LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
                }
            }
        })
    }


    init {
        viewTreeObserver.addOnPreDrawListener(preDrawListener)
    }
}
