package com.intelligent.reader.read.page

import android.app.Activity
import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.FrameLayout
import com.intelligent.reader.read.help.IReadView
import com.intelligent.reader.read.help.NovelHelper
import com.intelligent.reader.reader.ReaderViewModel

/**
 * 阅读容器
 * Created by wt on 2017/11/23.
 */
class ReaderViewWidget : FrameLayout {

    private var hrp:IReadView? = null

    var mReadView: IReadView? = null

    constructor(context: Context) : this(context,null)

    constructor(context: Context, attrs: AttributeSet?) : this(context,attrs,0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,attrs,defStyleAttr)

    fun addHorizontalReaderView(activity: Activity,mReaderViewModel:ReaderViewModel,myNovelHelper:NovelHelper){
        if (hrp == null) {
            hrp = HorizontalReaderView(activity)
            addView(hrp as View, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT))
            hrp?.setReadViewModel(mReaderViewModel)
            hrp?.init(activity,myNovelHelper)
            hrp?.getChapter(true)
            (hrp as ViewPager).currentItem = Int.MAX_VALUE/2
        }
    }

    fun removeHorizontalReaderView(){
        if (hrp!=null){
            removeView(hrp as View)
            hrp = null
        }
    }
}