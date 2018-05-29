package com.ding.basic.media

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.intelligent.reader.media.MediaControl
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

/**
 * @desc 广告视图
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/4/25 15:51
 */
class MediaView : RelativeLayout {

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    fun loadData(context: Context, adType: String) {
        MediaControl.getInstance().loadMedia(context, adType, BigMediaCallback(this))
    }

    /**
     * 需要具体大小的广告视图
     */
    fun loadData(context: Context, adType: String, height: Int, width: Int) {
        MediaControl.getInstance().loadMedia(context, adType, height, width, SmallMediaCallback(this))
    }

    private class BigMediaCallback(layout: RelativeLayout) : AbsLoadMediaCallback() {

        var mMediaView by Delegates.notNull<WeakReference<RelativeLayout>>()

        init {
            mMediaView = WeakReference(layout)
        }

        override fun onResult(adSwitch: Boolean, view: View) {
            super.onResult(adSwitch, view)
            if (adSwitch && view.parent == null) {
                val adParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                adParams.addRule(RelativeLayout.CENTER_VERTICAL)
                val layout = mMediaView.get()
                layout?.addView(view, adParams)
            }
        }
    }

    private class SmallMediaCallback(layout: RelativeLayout) : AbsLoadMediaCallback() {

        var mMediaView by Delegates.notNull<WeakReference<RelativeLayout>>()

        init {
            mMediaView = WeakReference(layout)
        }

        override fun onResult(adSwitch: Boolean, view: View) {
            super.onResult(adSwitch, view)
            if (adSwitch && view.parent == null) {
                val adParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                adParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                val layout = mMediaView.get()
                layout?.addView(view, adParams)
            }
        }
    }
}