package com.intelligent.reader.read.page

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.read.animation.BitmapManager
import com.intelligent.reader.read.help.DrawTextHelper
import com.intelligent.reader.read.help.IReadView
import com.intelligent.reader.read.help.NovelHelper
import com.intelligent.reader.reader.ReaderViewModel
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.db.BookChapterDao
import java.util.ArrayList
import android.view.MotionEvent
import android.widget.TextView
import com.intelligent.reader.read.help.ReadViewEnums
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.utils.AppLog


/**
 * 水平滑动PageView容器
 * Created by wt on 2017/12/2.
 */
class HorizontalReaderView : ViewPager, IReadView {
    //记录上一次x坐标
    private var beforeX: Float = 0.toFloat()
    //ViewPager是否能滑动
    val isCanScroll:Boolean = true
    //禁止滑动方向 true:禁止左滑 false:禁止右滑
    val isLeftSlip:Boolean = true
    //宽、高、时间、电量、颜色
    private var w: Int = 0
    private var h: Int = 0
    private var percent: Float = 0f
    private var time: String = ""
    private var manager: BitmapManager? = null
    private var color: Int = 0
    //Activity
    private lateinit var mOriginActivity: Activity
    //章节列表
    private lateinit var mChapterList: ArrayList<Chapter>
    //章节内容
    private lateinit var mOriginDataList: ArrayList<ArrayList<NovelLineBean>>
    private lateinit var preChaperConent: ArrayList<ArrayList<NovelLineBean>>
    private lateinit  var currentChaperConent: ArrayList<ArrayList<NovelLineBean>>
    private lateinit var nextChaperContent: ArrayList<ArrayList<NovelLineBean>>
    //章节
    private lateinit var preChapter: Chapter
    private lateinit var curChapter: Chapter
    private lateinit var nextChapter: Chapter
    lateinit var tempChapter: Chapter
    //页数
    private var preSize: Int = 0
    private var currentSize: Int = 0
    private var nextSize: Int = 0
    //helper
    private lateinit var novelHelper: NovelHelper
    private lateinit var drawTextHelper: DrawTextHelper
    //ReadViewModel
    private lateinit var mReadViewModel: ReaderViewModel
    //当前页数
    var curNumber:Int = 1
    //记录当前状态
    var pageIndex = ReadViewEnums.PageIndex.previous
    //记录章节
    var chapterNumber : Int = 0
    //记录章节page数
    var pageNumber : Int = 0
    /**
     * 用户画像
     */
    private var isFirstCome = true
    private var markPosition: Int = 0

    //构造
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    //初始化
    override fun init(activity: Activity, novelHelper: NovelHelper) {
        this.mOriginActivity = activity
        this.novelHelper = novelHelper
        mChapterList = BookChapterDao(context, mReadViewModel.readStatus?.book_id).queryBookChapter()
        adapter = MyAdapter()
        mOriginDataList = ArrayList()
        isFirstCome = true
        drawTextHelper = DrawTextHelper(resources, this.mOriginActivity)
    }

    //布局发生变化
    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)
        w = width
        h = height
        manager = BitmapManager.getInstance();
    }

    //设置Params
    fun setLayoutParams() {
        layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
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

    //字体颜色
    override fun setTextColor(color: Int) {
        this.color = color
        adapter.notifyDataSetChanged()
    }

    //设置电池背景
    override fun changeBatteryBg(res: Int) {

    }

    //设置背景

    override fun setBackground() {
//        ThemeUtil.getModePrimaryBackground(resources, this)
//        novel_time.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        adapter.notifyDataSetChanged()
    }

    //设置ViewModel
    override fun setReadViewModel(mReadViewModel: ReaderViewModel) {
        this.mReadViewModel = mReadViewModel
    }

    fun getPrePageBitmap():Bitmap {
        var index = 0
        return this.getChildAt(index).drawingCache
    }

    fun getNextPageBitmap():Bitmap {
        var index = 0
        for (i in 0..childCount){
            index = if((getChildAt(index).tag as Int)>curNumber) 1 else 2
        }
        return this.getChildAt(index).drawingCache
    }

    //拉取数据
    override fun getChapter(needSavePage: Boolean) {
        // 拉取当前章节
//        mReadViewModel.getChapter(ReadingActivity.MSG_LOAD_NEXT_CHAPTER, mReadViewModel.readStatus?.sequence ?: 1 + 1)
//        val chapter = mReadViewModel.getChapter(ReadingActivity.MSG_LOAD_NEXT_CHAPTER, mReadViewModel.readStatus?.sequence ?: 1 + 1)
//        onLoadChapter(ReadingActivity.MSG_LOAD_CUR_CHAPTER,chapter)
//        var bitmap = BitmapManager.getInstance().getBitmap(0)
//        var mNextPageCanvas = Canvas(bitmap)
//        if (currentChaperConent!=null) {
//            var drawText = drawTextHelper.drawText(mNextPageCanvas, currentChaperConent.get(0), mOriginActivity)
//        }
//        AppLog.w("","")
    }

    //拉取数据结束回调
    override fun onLoadChapter(what: Int, chapter: Chapter?) {
        when (what) {
        //本页
            ReadingActivity.MSG_LOAD_CUR_CHAPTER ->{
                novelHelper.getChapterContent(mOriginActivity,chapter,mReadViewModel.readStatus?.book,false)
                mOriginDataList.addAll(mReadViewModel.readStatus?.mLineList?:ArrayList())
                currentChaperConent = mReadViewModel.readStatus?.mLineList?:ArrayList()
            }
        //上页
            ReadingActivity.MSG_LOAD_PRE_CHAPTER ->{
                novelHelper.getChapterContent(mOriginActivity,chapter,mReadViewModel.readStatus?.book,false)
                mOriginDataList.addAll(mReadViewModel.readStatus?.mLineList?:ArrayList())
                preChaperConent = mReadViewModel.readStatus?.mLineList?:ArrayList()
            }
        //下页
            ReadingActivity.MSG_LOAD_NEXT_CHAPTER ->{
                novelHelper.getChapterContent(mOriginActivity,chapter,mReadViewModel.readStatus?.book,false)
                mOriginDataList.addAll(mReadViewModel.readStatus?.mLineList?:ArrayList())
                nextChaperContent = mReadViewModel.readStatus?.mLineList?:ArrayList()
            }
        }
    }

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
    private fun prohibitionOfSlidingTouchEvent(ev: MotionEvent):Boolean{
        when (ev.action) {
            MotionEvent.ACTION_DOWN//按下
            -> beforeX = ev.x
            MotionEvent.ACTION_MOVE -> {//移动
                val motionValue = ev.x - beforeX
                if (isLeftSlip) {
                    if (motionValue < 0) {//禁止左滑
                        return true
                    }
                }else{
                    if (motionValue > 0){//禁止右滑动
                        return true
                    }
                }
                beforeX = ev.x//手指移动时，再把当前的坐标作为下一次的‘上次坐标’，解决上述问题
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    //Adapter
    inner class MyAdapter : PagerAdapter() {

        var mCurrentView:View ?= null

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
            reAddPageTag(view.tag)
            container.removeView(view)
        }

        override fun instantiateItem(container: ViewGroup?, position: Int): Any {
            val rootView = TextView(container?.context)
            //添加view
            rootView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
            rootView.text = "position:"+position
            //添加tag
            addPageTag(rootView)
            container?.addView(rootView)
            return rootView
        }
        /**
         * instantiateItem fun 设置tag
         */
        private fun addPageTag(rootView:View) {
            //1、判断新章和记录章
            when {
                (mReadViewModel.readStatus?.sequence ?: 0) > chapterNumber -> {
                    rootView.tag = ReadViewEnums.PageIndex.next
                }
                (mReadViewModel.readStatus?.sequence ?: 0) < chapterNumber -> {
                    rootView.tag = ReadViewEnums.PageIndex.previous
                }
            //2、比较页数
                (mReadViewModel.readStatus?.sequence ?: 0) == chapterNumber -> {
                    when {
                        (mReadViewModel.readStatus?.currentPage?:1)>pageNumber->{
                            rootView.tag = ReadViewEnums.PageIndex.next
                        }
                        (mReadViewModel.readStatus?.currentPage?:1)<pageNumber->{
                            rootView.tag = ReadViewEnums.PageIndex.previous
                        }
                        (mReadViewModel.readStatus?.currentPage?:1)==pageNumber->{
                            rootView.tag = ReadViewEnums.PageIndex.current
                        }
                    }
                }
            }
            //记录
            chapterNumber = mReadViewModel.readStatus?.sequence?:0
            pageNumber = mReadViewModel.readStatus?.currentPage?:1
        }

        /**
         * destroyItem fun 重新设置tag
         */
        private fun reAddPageTag(tag: Any?) {
            when (tag) {
                ReadViewEnums.PageIndex.next -> {//如果移除的是下页 -> 往上翻 设置新tag
                    val preView = findViewWithTag(ReadViewEnums.PageIndex.previous)
                    preView?.tag = (preView?.tag as ReadViewEnums.PageIndex).previous
                    val curView = findViewWithTag(ReadViewEnums.PageIndex.current)
                    curView?.tag = (curView?.tag as ReadViewEnums.PageIndex).previous
                }
                ReadViewEnums.PageIndex.previous -> {//如果移除的是上页 -> 往下翻 设置新tag
                    val nextView = findViewWithTag(ReadViewEnums.PageIndex.next)
                    nextView?.tag = (nextView?.tag as ReadViewEnums.PageIndex).next
                    val curView = findViewWithTag(ReadViewEnums.PageIndex.current)
                    curView?.tag = (curView?.tag as ReadViewEnums.PageIndex).next
                }
            }
        }
    }

    /**
     * 用户画像打点
     *
     * @param endTime   阅读结束时间
     * @param position  当前第几页
     * @param pagecount 当前章的总页数
     * @param sequence  当前第几张
     */
    private fun addLog(endTime: Long, position: Int, pageCount: Int, sequence: Int) {
        //判断章节的最后一页
        if (sequence > mReadViewModel.readStatus?.lastSequenceRemark!! && !isFirstCome && mReadViewModel.readStatus?.requestItem != null) {
            //按照此顺序传值 当前的book_id，阅读章节，书籍源，章节总页数，当前阅读页，当前页总字数，当前页面来自，开始阅读时间,结束时间,阅读时间,是否有阅读中间退出行为,书籍来源1为青果，2为智能
            StartLogClickUtil.upLoadReadContent(mReadViewModel.readStatus?.book_id, mReadViewModel.readStatus?.lastChapterId, mReadViewModel.readStatus?.source_ids, "${mReadViewModel.readStatus?.lastPageCount}",
                    "${mReadViewModel.readStatus?.lastCurrentPageRemark}", "${mReadViewModel.readStatus?.currentPageConentLength}", "${mReadViewModel.readStatus?.requestItem?.fromType}",
                    "${mReadViewModel.readStatus?.startReadTime}", endTime.toString() + "", "${endTime - mReadViewModel.readStatus?.startReadTime!!}", "false", "${mReadViewModel.readStatus?.requestItem?.channel_code}")
        } else {
            if (mReadViewModel.readStatus?.requestItem != null && mReadViewModel.currentChapter != null && markPosition < position) {
                //按照此顺序传值 当前的book_id，阅读章节，书籍源，章节总页数，当前阅读页，当前页总字数，当前页面来自，开始阅读时间,结束时间,阅读时间,是否有阅读中间退出行为,书籍来源1为青果，2为智能
                StartLogClickUtil.upLoadReadContent(mReadViewModel.readStatus?.book_id, mReadViewModel.currentChapter?.chapter_id, mReadViewModel.readStatus?.source_ids, "${mReadViewModel.readStatus?.pageCount}",
                        (position - 1).toString(), "${mReadViewModel.readStatus?.currentPageConentLength}", "${mReadViewModel.readStatus?.requestItem?.fromType}",
                        "${mReadViewModel.readStatus?.startReadTime}", endTime.toString(), "${endTime - mReadViewModel.readStatus?.startReadTime!!}", "false", "${mReadViewModel.readStatus?.requestItem?.channel_code}")
                mReadViewModel.readStatus?.lastChapterId = mReadViewModel.currentChapter?.chapter_id
                mReadViewModel.readStatus?.requestItem?.fromType = 2
            }
        }
        mReadViewModel.readStatus?.startReadTime = endTime
        mReadViewModel.readStatus?.lastSequenceRemark = sequence
        mReadViewModel.readStatus?.lastCurrentPageRemark = position
        mReadViewModel.readStatus?.lastPageCount = pageCount
        markPosition = position
        isFirstCome = false
    }

}