package com.intelligent.reader.read.page

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.view.MotionEvent
import com.intelligent.reader.read.adapter.HorizontalAdapter
import com.intelligent.reader.read.help.*
import com.intelligent.reader.read.mode.ReadInfo
import com.intelligent.reader.read.mode.ReadViewEnums
import com.intelligent.reader.view.PagerAdapter
import com.intelligent.reader.view.ViewPager
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.utils.AppLog
import java.util.ArrayList

/**
 * 水平滑动PageView容器
 * Created by wt on 2017/12/2.
 */
class HorizontalReaderView : ViewPager, IReadView {

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
            when {
                index > position -> {
                    //改变游标

                }
                index < position -> {
                    //改变游标

                }
            }
            index = position
        }
    }

    //构造
    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        adapter = HorizontalAdapter(resources)
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
        //通知各个item游标

        //设置字体颜色
        mReadPageChange?.onLoadChapter(ReadViewEnums.MsgType.MSG_LOAD_CUR_CHAPTER, mReadInfo.curChapterIndex - 1, false, ReadViewEnums.PageIndex.current)

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

    //
    private var isInitCount: Int = 0
    private var preSize: Int = 0
    private var nextSize: Int = 0
    private var curSize: Int = 0

    //章节回调
    override fun setLoadChapter(msg: Int, chapter: Chapter, chapterList: ArrayList<ArrayList<NovelLineBean>>?) {
        isInitCount++
        val sequence = chapter.sequence
        when {
            sequence == mReadInfo!!.curChapterIndex -> {
                mReadInfo?.curOriginList?.addAll(chapterList!!)
                curSize = chapterList?.size ?: 0
            }
            sequence > mReadInfo!!.curChapterIndex -> {
                mReadInfo?.curOriginList?.addAll(chapterList!!)
                nextSize = chapterList?.size ?: 0
            }
            sequence < mReadInfo!!.curChapterIndex -> {
                mReadInfo?.curOriginList?.addAll(0, chapterList!!)
                preSize = chapterList?.size ?: 0
            }
        }
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