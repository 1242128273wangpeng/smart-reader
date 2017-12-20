package com.intelligent.reader.read.adapter

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import com.intelligent.reader.read.help.DrawTextHelper
import com.intelligent.reader.read.mode.ReadViewEnums
import com.intelligent.reader.read.page.HorizontalPage
import com.intelligent.reader.view.PagerAdapter
import com.intelligent.reader.view.ViewPager

/**
 * 水平阅读 adapter
 * Created by wt on 2017/12/20.
 */
class HorizontalAdapter(resources: Resources) :PagerAdapter(){

    var mCurrentView: View? = null
    var drawTextHelper: DrawTextHelper = DrawTextHelper(resources)
    var index: Int = -1

    fun getPrimaryItem(): View? {
        return mCurrentView
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        mCurrentView = `object` as View
        mCurrentView?.tag = ReadViewEnums.PageIndex.current
    }

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean = view == `object`
    override fun getCount(): Int = Int.MAX_VALUE
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any?) {
        val view = `object` as View
        reAddPageTag(container as ViewPager,view.tag)
        (view as HorizontalPage).mCurPageBitmap?.recycle()
        view.mCurPageBitmap = null
        container.removeView(view)
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        val itemView = HorizontalPage(container!!.context)
        //添加view
        itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        itemView.drawPage(drawTextHelper)
        addPageTag(itemView, position)
        container.addView(itemView)
        return itemView
    }

    /**
     * instantiateItem fun 设置tag
     */
    private fun addPageTag(itemView: View, position: Int) {
        when{
            index>position->{
                itemView.tag = ReadViewEnums.PageIndex.previous
            }
            index<position->{
                itemView.tag = ReadViewEnums.PageIndex.next
            }
        }
        index = position
    }
    /**
     * destroyItem fun 重新设置tag
     */
    private fun reAddPageTag(vp: ViewPager,tag: Any?) {
        when (tag) {
            ReadViewEnums.PageIndex.next -> {//如果移除的是下页 -> 往上翻 设置新tag
                val preView = vp.findViewWithTag(ReadViewEnums.PageIndex.previous)
                preView?.tag = (preView?.tag as ReadViewEnums.PageIndex).previous
                val curView = vp.findViewWithTag(ReadViewEnums.PageIndex.current)
                curView?.tag = (curView?.tag as ReadViewEnums.PageIndex).previous
            }
            ReadViewEnums.PageIndex.previous -> {//如果移除的是上页 -> 往下翻 设置新tag
                val nextView = vp.findViewWithTag(ReadViewEnums.PageIndex.next)
                nextView?.tag = (nextView?.tag as ReadViewEnums.PageIndex).next
                val curView = vp.findViewWithTag(ReadViewEnums.PageIndex.current)
                curView?.tag = (curView?.tag as ReadViewEnums.PageIndex).next
            }
        }
    }

}