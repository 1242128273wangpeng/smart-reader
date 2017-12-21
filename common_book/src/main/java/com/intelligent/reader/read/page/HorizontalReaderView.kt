package com.intelligent.reader.read.page

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.MotionEvent
import com.intelligent.reader.read.adapter.HorizontalAdapter
import com.intelligent.reader.read.help.*
import com.intelligent.reader.read.mode.ReadCursor
import com.intelligent.reader.read.mode.ReadInfo
import com.intelligent.reader.read.mode.ReadViewEnums
import com.intelligent.reader.view.ViewPager
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.utils.AppLog
import java.util.ArrayList

/**
 * 水平滑动PageView容器
 * Created by wt on 2017/12/2.
 */
class HorizontalReaderView : ViewPager, IReadView, HorizontalPage.NoticePageListener {

    //记录上一次x坐标
    private var beforeX: Float = 0.toFloat()
    //ViewPager是否能滑动
    val isCanScroll: Boolean = true
    //禁止滑动方向 true:禁止左滑 false:禁止右滑
    val isLeftSlip: Boolean = true
    //宽、高、时间、电量、颜色
    private var w: Int = 0
    private var h: Int = 0
    private var percent: Float = 0f
    private var time: String = ""
    private var color: Int = 0
    var mReadInfo: ReadInfo? = null
    //当前游标
    var curCursor:ReadCursor? = null
    //滑动方向
    private var direction = ReadViewEnums.Direction.leftToRight
    //滑动监听
    private var mListener: OnPageChangeListener = object : ViewPager.OnPageChangeListener {
        private var lastValue: Float = 0.toFloat()
        private var index: Int = 0
        override fun onPageScrollStateChanged(state: Int) = Unit

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            if (positionOffset == 0.0f) return
            if (lastValue > positionOffset) {
                direction = ReadViewEnums.Direction.rightToLeft
            } else if (lastValue < positionOffset) {
                direction = ReadViewEnums.Direction.leftToRight
            }
            lastValue = positionOffset
        }

        override fun onPageSelected(position: Int) {
            //建立新的游标
            var newSequence = 0
            var newPageIndex = 0
            //判断方向
            when {
                //向上翻页
                //获取上页游标，因为上页即将成为当页
                //改变adapter游标
                index > position -> {
                    val preView = findViewWithTag(ReadViewEnums.PageIndex.previous)
                    if(preView != null){
                        val preCousor = (preView as HorizontalPage).cursor!!
                        when(preCousor.pageIdex){
                            1->{//如果当前页是1：加载上一章最后页
                                newSequence = preCousor.sequence -1
                                newPageIndex = -1
                            }else ->{//其他情况： -1页
                                newSequence = preCousor.sequence
                                newPageIndex = preCousor.pageIdex -1
                            }
                        }
                    }
                    //设置游标
                    val newCursor = ReadCursor(curCursor!!.curBook,newSequence,newPageIndex,ReadViewEnums.PageIndex.previous,mReadInfo!!.mReadStatus)
                    (adapter as HorizontalAdapter).cursor = newCursor
                    index = position
                }
                //向下翻页
                //获取下页游标，因为下页即将成为当页
                //改变adapter游标
                index < position -> {
                    val nextView = findViewWithTag(ReadViewEnums.PageIndex.next)
                    if(nextView != null){
                        val nextCousor = (nextView as HorizontalPage).cursor!!
                        when(nextCousor.pageIdex){
                            nextCousor.pageIdexSum ->{//如果当前页是最后页：加载下一章1页
                                newSequence = nextCousor.sequence +1
                                newPageIndex = 1
                            }else ->{//其他情况： +1页
                                newSequence = nextCousor.sequence
                                newPageIndex  = nextCousor.pageIdex +1
                            }
                        }
                    }
                    //设置游标
                    val newCursor = ReadCursor(curCursor!!.curBook,newSequence,newPageIndex,ReadViewEnums.PageIndex.next,mReadInfo!!.mReadStatus)
                    (adapter as HorizontalAdapter).cursor = newCursor
                    index = position
                }
            }
        }
    }

    //构造
    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        adapter = HorizontalAdapter(this)
        setCurrentItem(Int.MAX_VALUE / 2, false)
        addOnPageChangeListener(mListener)
    }


//==================================================IReadPageChange=========================================
    private var mReadPageChange: IReadPageChange? = null

    override fun setIReadPageChange(mReadPageChange: IReadPageChange) {
        this.mReadPageChange = mReadPageChange
    }

//==================================================IReadView重写方法=========================================

    //入口
    override fun entrance(mReadInfo: ReadInfo) {
        this.mReadInfo = mReadInfo
        Handler().postDelayed({
            //通知当前item游标
            var curPage = mReadInfo.mReadStatus.currentPage +1
            curCursor = ReadCursor(mReadInfo.curBook,mReadInfo.mReadStatus.sequence,curPage,ReadViewEnums.PageIndex.current,mReadInfo.mReadStatus)
            val curView = (adapter as HorizontalAdapter).getPrimaryItem()
            (curView as HorizontalPage).entrance(curCursor!!)
        },200)

        //设置字体颜色
        mReadPageChange?.onLoadChapter(ReadViewEnums.MsgType.MSG_LOAD_CUR_CHAPTER, mReadInfo.mReadStatus.currentPage - 1, false, ReadViewEnums.PageIndex.current)
    }
    /**
     * 入口章节加载成功，通知其他页更新数据
     *
     */
    override fun curPageChangSuccess(pageIndex:Int,pageSum:Int) {
        val preView = findViewWithTag(ReadViewEnums.PageIndex.previous)
        if(preView != null){
            var newPreSequence = curCursor?.sequence?:0
            var newPrePageIndex = pageIndex
            when(pageIndex){
                1->{//如果当前页是1：加载上一章最后页
                    newPreSequence--
                    newPrePageIndex = -1
                }else ->{//其他情况： -1页
                    newPrePageIndex--
                }
            }
            val preCursor = ReadCursor(curCursor!!.curBook,newPreSequence,newPrePageIndex,ReadViewEnums.PageIndex.previous,mReadInfo!!.mReadStatus)
            (preView as HorizontalPage).setCursor(preCursor,false)
        }
        val nextView = findViewWithTag(ReadViewEnums.PageIndex.next)
        if(nextView != null){
            var newNextSequence = curCursor?.sequence?:0
            var newNextPageIndex = pageIndex
            when(pageIndex){
                pageSum ->{//如果当前页是最后页：加载下一章1页
                    newNextSequence++
                    newNextPageIndex = 1
                }else ->{//其他情况： +1页
                    newNextPageIndex++
                }
            }
            val nextCursor = ReadCursor(curCursor!!.curBook,newNextSequence,newNextPageIndex,ReadViewEnums.PageIndex.previous,mReadInfo!!.mReadStatus)
            (nextView as HorizontalPage).setCursor(nextCursor,false)
        }
    }


    //布局发生变化
    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)
        w = width
        h = height
    }

    //时间
    override fun freshTime(time: CharSequence?) {
        this.time = time.toString()
        adapter.notifyDataSetChanged()
    }

    //电量
    override fun freshBattery(percent: Float) {
        this.percent = percent
        adapter.notifyDataSetChanged()
    }

    //设置背景颜色
    override fun setBackground(background: Int) {
        this.color = color
        adapter.notifyDataSetChanged()
    }

    //设置阅读信息
    override fun setReadInfo(mReadInfo: ReadInfo?) {
        this.mReadInfo = mReadInfo
    }

    //章节回调
    override fun setLoadChapter(msg: Int, chapter: Chapter, chapterList: ArrayList<ArrayList<NovelLineBean>>?) {
        AppLog.w("chapter", "" + (chapterList?.size ?: -1))
    }

    //广告回调
    override fun setLoadAd(view: View?) {

    }

    //==================================================TouchEvent=========================================
    //-----禁止左滑-------左滑：上一次坐标 > 当前坐标
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (isCanScroll) {
            return super.dispatchTouchEvent(ev)
        } else {
            return prohibitionOfSlidingTouchEvent(ev)
        }
    }

    /**
     * 禁止单方向滑动事件
     */
    private fun prohibitionOfSlidingTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN//按下
            -> beforeX = ev.x
            MotionEvent.ACTION_MOVE -> {//移动
                val motionValue = ev.x - beforeX
                if (isLeftSlip) {
                    if (motionValue < 0) {//禁止左滑
                        return true
                    }
                } else {
                    if (motionValue > 0) {//禁止右滑动
                        return true
                    }
                }
                beforeX = ev.x//手指移动时，再把当前的坐标作为下一次的‘上次坐标’，解决上述问题
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}