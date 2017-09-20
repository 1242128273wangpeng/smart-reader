package com.intelligent.reader.read.page

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.intelligent.reader.R
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.read.animation.BitmapManager
import com.intelligent.reader.read.help.CallBack
import com.intelligent.reader.read.help.DrawTextHelper
import com.intelligent.reader.read.help.IReadDataFactory
import com.intelligent.reader.read.help.NovelHelper
import kotlinx.android.synthetic.main.vertical_pager_layout.view.*
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.data.bean.ReadStatus
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.toastLong


/**
 * Desc 上下翻页视图
 * Author lijun Lee
 * Mail jun_li@dingyuegroup.cn
 * Data 2017/9/12 15:39
 */
class VerticalPagerView : FrameLayout, PageInterface {

    private lateinit var mAdapter: PagerScrollAdapter

    private lateinit var mPageBitmap: Bitmap

    private lateinit var mPageCanvas: Canvas

    private lateinit var mBitmapManager: BitmapManager

    private lateinit var mOriginActivity: Activity

    /**
     * 翻页操作
     */
    private lateinit var mReadDataFactory: IReadDataFactory

    /**
     * 菜单回调
     */
    private var mMenuCallBack: CallBack? = null

    /**
     * 绘制操作
     */
    private lateinit var mDrawTextHelper: DrawTextHelper

    private lateinit var mNovelHelper: NovelHelper

    /**
     * 阅读状态
     */
    private lateinit var mReadStatus: ReadStatus

    /**
     * 翻页布局参数
     */
    private lateinit var mPagerLayoutParams: FrameLayout.LayoutParams

    /**
     * 章节数据
     */
    private lateinit var mCurChapter: Chapter

    private var mTotalItemCount: Int = 0

    private var mLastVisiblePosition = -1

    /**
     * Touch
     */
    private var mStartTouchTime: Long = 0

    private var mStartTouchX: Int = 0

    private var mStartTouchY: Int = 0

    private var mWidth: Int = 0

    private var mHeight: Int = 0


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mReadStatus.screenWidth = w
        mReadStatus.screenHeight = h
        mWidth = mReadStatus.screenWidth
        mHeight = mReadStatus.screenHeight

        mBitmapManager = BitmapManager(mReadStatus.screenWidth, mReadStatus.screenHeight)
        mPageBitmap = mBitmapManager.bitmap
        mPageCanvas = Canvas(mPageBitmap)

        if (mMenuCallBack != null && Math.abs(oldh - h) > AppUtils.dip2px(context, 26f)) {
            mMenuCallBack?.onResize()
            if (android.os.Build.VERSION.SDK_INT < 11 && Constants.isFullWindowRead) {
                mHeight = mReadStatus.screenHeight - AppUtils.dip2px(context, 20f)
            } else {
                mWidth = mReadStatus.screenHeight - AppUtils.dip2px(context, 40f)
            }

            mPagerLayoutParams = FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, mHeight)
            setBackground()

            getChapter(true)
        }
    }

    override fun init(activity: Activity, readStatus: ReadStatus, novelHelper: NovelHelper) {
        LayoutInflater.from(context).inflate(R.layout.vertical_pager_layout, this)
        page_rv.layoutManager = LinearLayoutManager(context)
        mWidth = readStatus.screenWidth
        mHeight = readStatus.screenHeight - AppUtils.dip2px(activity, 40f)
        mPagerLayoutParams = FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, mHeight)
        mOriginActivity = activity

        mNovelHelper = novelHelper
        mReadStatus = readStatus
        mDrawTextHelper = DrawTextHelper(resources, this, activity)

        addScroll()
    }

    private fun addScroll() {
        page_rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 已在顶部
                if (!recyclerView.canScrollVertically(-1)) {
                    // 已在底部
                } else if (!recyclerView.canScrollVertically(1)) {

                }

                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                mTotalItemCount = linearLayoutManager.childCount
                mLastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition()
                mReadStatus.currentPage = mLastVisiblePosition + 1
                setCurrentChapterInfo(mReadStatus.currentPage)

                Log.e("Scroll", "lastVisible: " + mLastVisiblePosition)
            }
        })
    }

    override fun getChapter(needSavePage: Boolean) {

        mAdapter = PagerScrollAdapter(mPageBitmap, mPageCanvas)
        page_rv.adapter = mAdapter

        if (mReadStatus.mLineList == null) {
            return
        }
        if (!needSavePage) {
            mReadStatus.currentPage = 1
        } else {
            mAdapter.clearData()
        }

        mCurChapter = mReadDataFactory.currentChapter

        mAdapter.setChapter(mReadStatus.mLineList)
    }

    fun setCurrentChapterInfo(position: Int) {
        if (mOriginActivity is ReadingActivity) {
            (mOriginActivity as ReadingActivity).freshPage()
        }

        mReadStatus.sequence = mCurChapter.sequence
        mReadStatus.chapterName = mCurChapter.chapter_name

        if (!mReadStatus.chapterName.isNullOrEmpty()) {
            val chapterName = mReadStatus.chapterName.replace("\n", "", false)
            if (chapterName.length > 18) {
                novel_title.text = "${chapterName.substring(0, 18)}..."
            } else {
                novel_title.text = chapterName
            }
        }
        novel_chapter.text = "${mReadStatus.sequence + 1}/${mReadStatus.chapterCount}"
        novel_page.text = "$position/${mReadStatus.pageCount}"
        mNovelHelper.getPageContentScroll()
    }

    /**
     * PagerScrollAdapter
     */
    internal inner class PagerScrollAdapter(private val pageBitmap: Bitmap,
                                            private val pageCanvas: Canvas) : RecyclerView.Adapter<PagerScrollAdapter.PagerHolder>() {

        private var chapterList: ArrayList<ArrayList<NovelLineBean>>

        init {
            chapterList = ArrayList()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PagerHolder(LayoutInflater.from(parent.context).inflate(R.layout.page_item2, parent, false))


        override fun onBindViewHolder(holder: PagerHolder, position: Int) {
            holder.text.layoutParams = mPagerLayoutParams
//
//            mDrawTextHelper.drawText(pageCanvas, chapterList[position], mCurChapter.chapterNameList)
//            holder.page.drawPage(pageBitmap)
//            holder.text.text = "特工教师"
        }

        override fun getItemCount() = chapterList.size

        fun setChapter(data: ArrayList<ArrayList<NovelLineBean>>) {
            chapterList = data
            notifyDataSetChanged()
        }

        fun addAllChapter(location: Int, data: ArrayList<ArrayList<NovelLineBean>>): Boolean {
            return if (chapterList.addAll(location, data)) {
                notifyItemRangeInserted(location, data.size)
                true
            } else {
                false
            }
        }

        fun addAllChapter(data: ArrayList<ArrayList<NovelLineBean>>): Boolean {
            val lastIndex = chapterList.size
            return if (chapterList.addAll(data)) {
                notifyItemRangeInserted(lastIndex, data.size)
                true
            } else {
                false
            }
        }

        fun clearData() {
            if (chapterList.size > 0) chapterList.clear()
            notifyDataSetChanged()
        }

        internal inner class PagerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            //            var page: PagerHolder = itemView.findViewById(R.id.page_item) as Page
            var text: TextView = itemView.findViewById(R.id.text) as TextView
        }

    }

    private fun drawBackground() {
        if (Constants.MODE == 51) {// 牛皮纸
            scroll_page.setBackgroundResource(R.drawable.read_page_bg_default)
            novel_title.setBackgroundResource(R.drawable.read_page_bg_default_patch)
            novel_bottom.setBackgroundResource(R.drawable.read_page_bg_default_patch)
//            page_list.setFootViewBackground(R.drawable.read_page_bg_default_patch)
        } else {
            // 通过新的画布，将矩形画新的bitmap上去
            var color_int = R.color.reading_backdrop_first
            when {
                Constants.MODE == 52 -> // day
                    color_int = R.color.reading_backdrop_second
                Constants.MODE == 53 -> // eye
                    color_int = R.color.reading_backdrop_third
                Constants.MODE == 54 -> // powersave
                    color_int = R.color.reading_backdrop_fourth
                Constants.MODE == 55 -> // color -4
                    color_int = R.color.reading_backdrop_fifth
                Constants.MODE == 56 -> // color -5
                    color_int = R.color.reading_backdrop_sixth
                Constants.MODE == 61 -> //night3
                    color_int = R.color.reading_backdrop_night
            }

            novel_title.setBackgroundColor(resources.getColor(color_int))
            novel_bottom.setBackgroundColor(resources.getColor(color_int))
//            page_list.setFootViewBackgroundColor(resources.getColor(color_int))
        }
    }

    private fun drawHeadFootText() {
        var color_int = R.color.reading_text_color_first
        when {
            Constants.MODE == 51 -> // night1
                color_int = R.color.reading_text_color_first
            Constants.MODE == 52 -> // day
                color_int = R.color.reading_text_color_second
            Constants.MODE == 53 -> // eye
                color_int = R.color.reading_text_color_third
            Constants.MODE == 54 -> // powersave
                color_int = R.color.reading_text_color_fourth
            Constants.MODE == 55 -> // color -4
                color_int = R.color.reading_text_color_fifth
            Constants.MODE == 56 -> // color -5
                color_int = R.color.reading_text_color_sixth
            Constants.MODE == 61 -> // night2
                color_int = R.color.reading_text_color_night
        }

        novel_time.setTextColor(resources.getColor(color_int))
        novel_page.setTextColor(resources.getColor(color_int))
        novel_chapter.setTextColor(resources.getColor(color_int))
        novel_title.setTextColor(resources.getColor(color_int))
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (mReadStatus.isMenuShow) {
            mMenuCallBack?.onShowMenu(false)
            return false
        }

        val tmpX = event.x.toInt()
        val tmpY = event.y.toInt()

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                mStartTouchTime = System.currentTimeMillis()
                mStartTouchX = tmpX
                mStartTouchY = tmpY
            }

            MotionEvent.ACTION_UP -> {
                val touchTime = System.currentTimeMillis() - mStartTouchTime
                val distance = Math.sqrt(Math.pow((mStartTouchX - tmpX).toDouble(), 2.0) + Math.pow((mStartTouchY - tmpY).toDouble(), 2.0)).toInt()
                if (touchTime < 100 && distance < 30 || distance < 10) {
                    showMenuClick(event)
                }
                mStartTouchTime = 0
            }
        }

        return super.onInterceptTouchEvent(event)
    }

    /**
     * 显示菜单
     */
    private fun showMenuClick(event: MotionEvent) {
        val x = event.x.toInt()
        val y = event.y.toInt()

        val h4 = height / 4
        val w3 = width / 3
        if (x <= w3) {

        } else if (x >= width - w3 || y >= height - h4 && x >= w3) {

        } else {
            mMenuCallBack?.onShowMenu(true)
        }
    }

    override fun freshTime(time: CharSequence?) {
    }

    override fun freshBattery(percent: Float) {
        novel_content_battery_view.setBattery(percent)
    }

    override fun drawNextPage() {
    }

    override fun drawCurrentPage() {
    }

    override fun setTextColor(color: Int) {
    }

    override fun changeBatteryBg(res: Int) {
    }

    override fun setBackground() {
        mDrawTextHelper.resetBackBitmap()
        drawBackground()
        drawHeadFootText()
    }

    override fun setPageBackColor(color: Int) {
    }

    override fun refreshCurrentPage() {
    }

    override fun tryTurnPrePage() {
    }

    override fun onAnimationFinish() {
    }

    override fun setCallBack(callBack: CallBack?) {
        this.mMenuCallBack = callBack
    }

    override fun clear() {
    }

    override fun isAutoReadMode(): Boolean = false

    override fun startAutoRead() {
    }

    override fun exitAutoRead() {
    }

    override fun exitAutoReadNoCancel() {
    }

    override fun tryResumeAutoRead() {
    }

    override fun resumeAutoRead() {
    }

    override fun pauseAutoRead() {
    }

    override fun getPreChapter() {
    }

    override fun getNextChapter() {
    }

    override fun setReadFactory(factory: IReadDataFactory) {
        this.mReadDataFactory = factory
    }

    override fun setFirstPage(firstPage: Boolean) {
    }

    override fun setisAutoMenuShowing(isShowing: Boolean) {
    }

    override fun setKeyEvent(event: KeyEvent?): Boolean {
        return false
    }

    override fun loadNatvieAd() {
    }

    override fun getCurrentNovel() = mReadDataFactory.transformation()


}