package com.intelligent.reader.read.adapter

import android.view.View
import android.view.ViewGroup
import com.intelligent.reader.read.mode.ReadCursor
import com.intelligent.reader.read.mode.ReadViewEnums
import com.intelligent.reader.read.page.HorizontalPage
import com.intelligent.reader.view.PagerAdapter
import com.intelligent.reader.view.ViewPager
import net.lzbook.kit.utils.AppLog

/**
 * 水平阅读 adapter
 * Created by wt on 2017/12/20.
 */
class HorizontalAdapter(private var noticePageListener: HorizontalPage.NoticePageListener): PagerAdapter() {

    var mCurrentView: View? = null
    var index: Int = -1
    var cursor:ReadCursor? = null

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
        (view as HorizontalPage).mCurPageBitmap?.recycle()
        view.mCurPageBitmap = null
        container.removeView(view)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = HorizontalPage(container.context,noticePageListener)
        //添加view
        itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        if (cursor!= null) {
            AppLog.e("cursor: ",cursor.toString())
            itemView.setCursor(cursor!!,false)
        }
        addPageTag(container as ViewPager,itemView, position)
        container.addView(itemView)
        return itemView
    }

    /**
     * instantiateItem fun 设置tag
     */
    private fun addPageTag(vp: ViewPager,itemView: View, position: Int) {
        when{
            index>position->{
                val preView = vp.findViewWithTag(ReadViewEnums.PageIndex.previous)
                val curView = vp.findViewWithTag(ReadViewEnums.PageIndex.current)
                preView?.tag = (preView?.tag as ReadViewEnums.PageIndex).next//previous -> current
                curView?.tag = (curView?.tag as ReadViewEnums.PageIndex).next//current -> next
                itemView.tag = ReadViewEnums.PageIndex.previous//add -> previous
            }
            index<position->{
                val nextView = vp.findViewWithTag(ReadViewEnums.PageIndex.next)
                val curView = vp.findViewWithTag(ReadViewEnums.PageIndex.current)
                nextView?.tag = (nextView?.tag as ReadViewEnums.PageIndex).previous//next -> current
                curView?.tag = (curView?.tag as ReadViewEnums.PageIndex).previous//current -> previous
                itemView.tag = ReadViewEnums.PageIndex.next//add -> next
            }
        }
        index = position
    }
}