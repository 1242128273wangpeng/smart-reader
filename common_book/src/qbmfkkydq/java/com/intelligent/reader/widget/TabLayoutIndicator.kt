package com.intelligent.reader.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.intelligent.reader.R

/**
 * Date: 2018/7/19 14:12
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: TabLayout 的指示下划线控件
 */
class TabLayoutIndicator : View {

    private var mPaint: Paint? = null
    private var mRect: RectF? = null

    private var mTabLayout: TabLayout? = null
    private var mViewPager: ViewPager? = null


    internal var mIndicatorColor: Int = 0
    internal var mIndicatorWidth: Int = 0

    internal var mTabSelectedListener: TabLayout.OnTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {

        }

        override fun onTabUnselected(tab: TabLayout.Tab) {

        }

        override fun onTabReselected(tab: TabLayout.Tab) {

        }
    }

    internal var mPageChangeListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            computeIndicatorRect(position, positionOffset)
            invalidate()
        }

        override fun onPageSelected(position: Int) {

        }

        override fun onPageScrollStateChanged(state: Int) {

        }
    }

    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        initView(context, attrs, defStyleAttr)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context, attrs, defStyleAttr)
    }

    private fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.TabLayoutIndicator, defStyleAttr, 0)
        mIndicatorWidth = array.getDimensionPixelSize(R.styleable.TabLayoutIndicator_mindicatorWidth, 0)
        mIndicatorColor = array.getColor(R.styleable.TabLayoutIndicator_mindicatorColor, 0)
        array.recycle()


        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.color = mIndicatorColor
        mPaint!!.style = Paint.Style.FILL
    }

    fun setupWithTabLayout(tabLayout: TabLayout) {
        mTabLayout = tabLayout
        tabLayout.addOnTabSelectedListener(mTabSelectedListener)

        tabLayout.viewTreeObserver.addOnScrollChangedListener {
            if (mTabLayout!!.scrollX != scrollX) {
                scrollTo(mTabLayout!!.scrollX, mTabLayout!!.scrollY)
            }
        }
    }

    fun setupWithViewPager(viewPager: ViewPager) {
        mViewPager = viewPager
        mViewPager!!.addOnPageChangeListener(mPageChangeListener)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mRect != null) {
            val saveCount = canvas.save()
            canvas.drawRect(mRect!!, mPaint!!)
            canvas.restoreToCount(saveCount)
        }
    }

    private fun computeIndicatorRect(position: Int, positionOffset: Float) {
        val tab = getTabView(position)

        var left = tab.left.toFloat()
        var right = tab.right.toFloat()
        val top = (tab.top + paddingTop).toFloat()
        val bottom = (tab.bottom + paddingBottom).toFloat()

        if (positionOffset > 0f && position < mTabLayout!!.tabCount - 1) {
            val nextTab = getTabView(position + 1)

            val nextTabLeft = nextTab.left.toFloat()
            val nextTabRight = nextTab.right.toFloat()

            left = nextTabLeft * positionOffset + left * (1f - positionOffset)
            right = nextTabRight * positionOffset + right * (1f - positionOffset)
        }
        if (mIndicatorWidth != 0) {
            val center = (left + right) / 2
            left = center - mIndicatorWidth / 2
            right = center + mIndicatorWidth / 2
        }
        if (mRect == null) {
            mRect = RectF()
        }
        mRect!!.set(left, top, right, bottom)
    }

    private fun getTabView(position: Int): View {
        //TabLayout has only one child of LinearLayout
        val tabStrip = mTabLayout!!.getChildAt(0) as ViewGroup
        return tabStrip.getChildAt(position)
    }
}
