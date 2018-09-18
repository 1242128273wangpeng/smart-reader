package com.dingyue.bookshelf

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.dy.media.IMediaControl
import com.dy.media.MediaControl

/**
 * Function：书架顶部广告
 *
 * Created by JoannChen on 2018/9/7 0007 18:28
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class BookShelfADView : FrameLayout {
    val activity: Activity
    var loadedTime = 0L
    constructor(act: Activity) : super(act) {
        this.activity = act
        // 修复广告拉取失败时导致无法下拉刷新的问题
        this.minimumHeight = 1
        //  广告加载出来前默认占位布局
        addView(LayoutInflater.from(context).inflate(R.layout.item_bookshelf_ad_placeholder, null), LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
    }

    val preDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            loadAD()
            viewTreeObserver.removeOnPreDrawListener(this)
            return true
        }
    }

    var needRefreshAD = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (needRefreshAD) {
            needRefreshAD = false
            loadAD()
        }
    }
    private fun loadAD() {
        if (System.currentTimeMillis() - loadedTime < 500) {
            //放在广告刷新时间过短
            return
        }
        loadedTime = System.currentTimeMillis()

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
