package com.intelligent.reader.read.page

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.intelligent.reader.R
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.animation.BitmapManager
import com.intelligent.reader.read.help.DrawTextHelper
import com.intelligent.reader.read.mode.NovelPageBean
import com.intelligent.reader.read.mode.ReadCursor
import com.intelligent.reader.read.mode.ReadState
import net.lzbook.kit.data.bean.ReadViewEnums
import com.intelligent.reader.util.ThemeUtil
import kotlinx.android.synthetic.main.book_home_page_layout.view.*
import kotlinx.android.synthetic.main.error_page2.view.*
import kotlinx.android.synthetic.main.read_bottom.view.*
import kotlinx.android.synthetic.main.read_top.view.*
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.AppUtils


/**
 * 水平滑动item
 * 子类逻辑
 * 1、首先viewpager 取出游标 通知子类展示哪章哪页
 * 2、子类去取数据
 * 3、子类要维护页面状态 loading success error
 * 4、状态改变通知父类 （ 页面状态 章节状态 ） ？父类游标
 * 5、父类检查 三个item 的状态 tag 做出整体的判断
 * 页面状态逻辑
 * 默认loding
 * 如果当前页为loading 禁止vp滑动
 * error
 * 重新加载数据
 *
 * Created by wt on 2017/12/14.
 */
class HorizontalPage : FrameLayout {

    private var mDrawTextHelper: DrawTextHelper? = null
    private lateinit var loadView: View
    private lateinit var errorView: View
    private lateinit var pageView: HorizontalItemPage
    private lateinit var readTop: View
    private lateinit var readBottom: View
    private lateinit var homePage: View

    var mCursorOffset = 0
    var percent = 0.0f
    var time = ""
    var mCursor: ReadCursor? = null
    var viewState: ReadViewEnums.ViewState = ReadViewEnums.ViewState.loading
    var viewNotify: ReadViewEnums.NotifyStateState = ReadViewEnums.NotifyStateState.none
    var pageIndex:Int = 0
    var pageSum:Int = 0
    var contentLength:Int = 0
    var noticePageListener: NoticePageListener? = null

    constructor(context: Context, noticePageListener: NoticePageListener) : this(context, null, noticePageListener)

    constructor(context: Context, attrs: AttributeSet?, noticePageListener: NoticePageListener) : this(context, attrs, 0, noticePageListener)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, noticePageListener: NoticePageListener) : super(context, attrs, defStyleAttr) {
        this.noticePageListener = noticePageListener
        time = this.noticePageListener!!.getCurTime()
        percent = this.noticePageListener!!.getCurPercent()
        isDrawingCacheEnabled = true
        drawingCacheQuality = DRAWING_CACHE_QUALITY_LOW
        isChildrenDrawnWithCacheEnabled = true
        isChildrenDrawnWithCacheEnabled = true
        init()
    }

    private fun init(){
        mDrawTextHelper = DrawTextHelper(context.resources)
        loadView = inflate(context, R.layout.loading_page_reading, null)
        errorView = inflate(context, R.layout.error_page2, null)
        readTop = inflate(context, R.layout.read_top, null)
        readBottom = inflate(context, R.layout.read_bottom, null)
        homePage = inflate(context, R.layout.book_home_page_layout, null)
        pageView = HorizontalItemPage(context)
        addView(pageView)
        addView(readTop)
        addView(readBottom)
        addView(loadView)
        setupView()
    }

    private fun setupView() {
        //原网页
        origin_tv.setOnClickListener {
            noticePageListener?.loadOrigin()
        }
        //转码声明
        trans_coding_tv.setOnClickListener {
            noticePageListener?.loadTransCoding()
        }
        //设置TextColor
        var colorInt = com.intelligent.reader.R.color.reading_operation_text_color_first
        when {
            Constants.MODE == 51 -> // night1
                colorInt = com.intelligent.reader.R.color.reading_operation_text_color_first
            Constants.MODE == 52 -> // day
                colorInt = com.intelligent.reader.R.color.reading_operation_text_color_second
            Constants.MODE == 53 -> // eye
                colorInt = com.intelligent.reader.R.color.reading_operation_text_color_third
            Constants.MODE == 54 -> // powersave
                colorInt = com.intelligent.reader.R.color.reading_operation_text_color_fourth
            Constants.MODE == 55 -> // color -4
                colorInt = com.intelligent.reader.R.color.reading_operation_text_color_fifth
            Constants.MODE == 56 -> // color -5
                colorInt = com.intelligent.reader.R.color.reading_operation_text_color_sixth
            Constants.MODE == 61 -> // night2
                colorInt = com.intelligent.reader.R.color.reading_operation_text_color_night
        }
        novel_time.setTextColor(resources.getColor(colorInt))
        origin_tv.setTextColor(resources.getColor(colorInt))
        trans_coding_tv.setTextColor(resources.getColor(colorInt))
        novel_page.setTextColor(resources.getColor(colorInt))
        novel_chapter.setTextColor(resources.getColor(colorInt))
        novel_title.setTextColor(resources.getColor(colorInt))
        setBackGroud()
        setTimes(time)
        setBattery(percent)
    }

    /**
     * @param title 章节标题
     * @param chapterProgress 章节进度
     * @param pageProgress 页进度
     */
    fun setTopAndBottomViewContext(title: String, chapterProgress: String, pageProgress: String) {
        novel_title.text = title
        novel_chapter.text = chapterProgress
        novel_page.text = pageProgress
    }

    fun setBackGroud() {
        //pageView
        ThemeUtil.getModePrimaryBackground(resources, pageView)
        //电池背景
        ThemeUtil.getModePrimaryBackground(resources, novel_content_battery_view)
    }

    fun setTimes(time: String) {
        this.time = time
        //时间
        novel_time.text = time
    }

    fun setBattery(percent: Float) {
        this.percent = percent
        //电池
        novel_content_battery_view.setBattery(this.percent)
    }

    fun setCursor(cursor: ReadCursor) = pageView.setCursor(cursor)

    interface NoticePageListener {
        fun pageChangSuccess(cursor: ReadCursor, notify: ReadViewEnums.NotifyStateState)
        fun onClickLeft(smoothScroll: Boolean)
        fun onClickRight(smoothScroll: Boolean)
        fun onClickMenu(isShow: Boolean)
        fun loadOrigin()
        fun loadTransCoding()
        fun getCurPercent(): Float
        fun getCurTime(): String
        fun currentViewSuccess()
        fun myDispatchTouchEvent(event:MotionEvent)
    }

    inner class HorizontalItemPage : View {

        constructor(context: Context) : this(context, null)
        constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if(mNovelPageBean != null) {
                mDrawTextHelper?.drawText(canvas, mNovelPageBean!!)
            }
        }

        /**
         * 入口模式
         * 加载3章至内存
         */
        fun entrance(cursor: ReadCursor) {
            cursor.curBook.sequence = cursor.sequence
            DataProvider.getInstance().loadChapter(cursor.curBook, cursor.sequence, ReadViewEnums.PageIndex.current, object : DataProvider.ReadDataListener() {
                override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) {
                    //当前章拉去成功 执行一般方法
                    //true 通知其他页面加载
                    setCursor(cursor)
                }

                override fun loadDataError(message: String) {
                    //Error
                    addView(errorView)
                    viewState = ReadViewEnums.ViewState.error
                    loadView.visibility = View.GONE
                    errorView.loading_error_reload.setOnClickListener({
                        entrance(cursor)
                        loadView.visibility = View.VISIBLE
                        viewState = ReadViewEnums.ViewState.loading
                        removeView(errorView)
                    })
                    errorView.loading_error_setting.visibility = GONE
                    noticePageListener?.pageChangSuccess(mCursor!!, viewNotify)//游标通知回调
                }
            })
            DataProvider.getInstance().loadChapter(cursor.curBook, cursor.sequence, ReadViewEnums.PageIndex.previous, object : DataProvider.ReadDataListener() {
                override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) = Unit
                override fun loadDataError(message: String) = Unit
            })
            DataProvider.getInstance().loadChapter(cursor.curBook, cursor.sequence, ReadViewEnums.PageIndex.next, object : DataProvider.ReadDataListener() {
                override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) = Unit
                override fun loadDataError(message: String) = Unit
            })
        }

        /**
         * 一般模式
         * 1、判断缓存
         * 2、预加载数据
         * 3、展示数据
         * @param cursor
         */
        fun setCursor(cursor: ReadCursor) {
            mCursor = cursor
            //判断超过章节数
            if ((ReadState.chapterList.isNotEmpty()) and (mCursor!!.sequence > ReadState.chapterList.size - 1)) {
                viewState = ReadViewEnums.ViewState.end
                return
            }
            //判断item 需要的章节是否在缓存
            val chapter = DataProvider.getInstance().chapterMap[cursor.sequence]
            if (chapter != null) {//加载数据
                drawPage(cursor, chapter)
            } else {//无缓存数据
                entrance(cursor)
            }
        }

        private var mNovelPageBean: NovelPageBean? = null

        /**
         * 画页面
         */
        private fun drawPage(cursor: ReadCursor, chapter: Chapter) {
            //获取数据
            cursor.readStatus.chapterName = chapter.chapter_name
            val chapterList = DataProvider.getInstance().chapterSeparate[cursor.sequence]!!
            if (!chapterList.isEmpty()) {//集合不为空，角标小于集合长度
                pageIndex = findPageIndexByOffset(cursor.offset,chapterList)
                pageSum = chapterList.size
                if (pageIndex <= pageSum){
                    //过滤其他页内容
                    if (cursor.sequence == -1) {//封面页
                        setupHomePage(cursor)
                        val start = ReadViewEnums.ViewState.start
                        start.Tag = when (viewNotify) {
                            ReadViewEnums.NotifyStateState.all -> 1
                            else -> 0
                        }
                        mCursor!!.offset = 1
                        mCursor!!.lastOffset = 1
                        mCursorOffset = -1
                        viewState = start
                        noticePageListener?.pageChangSuccess(mCursor!!,ReadViewEnums.NotifyStateState.none)//游标通知回调
                    }else {
                        mNovelPageBean = findNovelPageBeanByOffset(cursor.offset,chapterList)
                        contentLength = mNovelPageBean!!.contentLength
                        if (mNovelPageBean!!.isAd) {//广告页
                            showAdBigger(mNovelPageBean!!)
                        } else {//普通页
                            //画之前清空内容
                            removeView(homePage)
                            //画本页内容
                            BitmapManager.getInstance().setSize(cursor.readStatus.screenWidth, cursor.readStatus.screenHeight)

                            val topMargin = if (mNovelPageBean?.lines?.isEmpty() == true) mNovelPageBean!!.height else ReadConfig.screenHeight.toFloat()

                            postInvalidate()
                            //判断展示Banner广告
                            if (cursor.readStatus.screenHeight - topMargin > cursor.readStatus.screenHeight / 5) {
                                showAdBanner(topMargin)
                            }
                        }
                        //改状态、游标状态
                        loadView.visibility = View.GONE
                        mCursor!!.lastOffset = chapterList.last().offset
                        mCursor!!.offset = mNovelPageBean!!.offset
                        mCursor!!.nextOffset = if (pageIndex < chapterList.size) chapterList[pageIndex].offset else 0
                        viewState = if ((mCursor!!.sequence == ReadState.chapterList.size - 1) and (pageIndex == pageSum)) {//判断这本书的最后一页
                            ReadViewEnums.ViewState.end
                        } else {
                            noticePageListener?.pageChangSuccess(mCursor!!, viewNotify)//游标通知回调
                            ReadViewEnums.ViewState.success
                        }
                        //游标添加广告后的偏移量
                        when {
                            (pageSum >= 16) and (pageIndex >= 9) and (pageIndex != pageSum) -> mCursorOffset == -1
                            (pageSum >= 16) and (pageIndex >= 9) and (pageIndex == pageSum) -> mCursorOffset == -2
                            (pageSum < 16) and (pageIndex == pageSum) -> mCursorOffset == -1
                            else -> mCursorOffset = 0
                        }
                        //设置top and bottom
                        val chapterProgress = "" + (cursor.sequence.plus(1)) + "/" + cursor.readStatus.chapterCount + "章"
                        val pageProgress = "本章第$pageIndex/$pageSum"
                        setTopAndBottomViewContext(cursor.readStatus.chapterName,chapterProgress,pageProgress)
                        noticePageListener?.currentViewSuccess()
                    }
                }
            }
        }

        //封面页
        private fun setupHomePage(cursor: ReadCursor) {
            removeView(homePage)
            addView(homePage)
            //封面页
            homePage.book_name_tv.text = cursor.curBook.name
            homePage.book_auth_tv.text = cursor.curBook.author
            homePage.slogan_tv.setTextView(2f, context.resources.getString(R.string.slogan))
            homePage.product_name_tv.setTextView(1f, context.resources.getString(R.string.app_name))
            //封面字颜色
            homePage.book_name_tv.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
            homePage.book_auth_tv.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
            homePage.slogan_tv.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
            homePage.product_name_tv.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
            postInvalidate()
            //改变状态
            mCursor!!.sequence = -1
            viewState = ReadViewEnums.ViewState.start
            loadView.visibility = View.GONE
            readTop.visibility = View.GONE
            readBottom.visibility = View.GONE
        }

        //通过偏移量获取章节
        private fun findNovelPageBeanByOffset(offset: Int, chapterSeparate: ArrayList<NovelPageBean>): NovelPageBean {
            //过滤集合 小于offset的最后一个元素
            val filter = chapterSeparate.filter {
                it.offset <= offset
            }
            return filter.last()
        }

        //通过偏移量获取页码Index
        private fun findPageIndexByOffset(offset: Int, chapterSeparate: ArrayList<NovelPageBean>): Int {
            val filter = chapterSeparate.filter {
                it.offset <= offset
            }
            return filter.size
        }

        //展示Banner广告
        private fun showAdBanner(topMargins: Float) {
            DataProvider.getInstance().loadAd(context, "8-1", mCursor!!.readStatus.screenWidth, mCursor!!.readStatus.screenHeight - topMargins.toInt(), object : DataProvider.OnLoadReaderAdCallback {
                override fun onLoadAd(adView: ViewGroup) {
                    val param = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    val margin = AppUtils.dip2px(context, 10f)
                    param.setMargins(margin, topMargins.toInt(), margin, margin)
                    addView(adView, param)
                }
            })
        }

        //展示Bigger广告
        private fun showAdBigger(mNovelPageBean: NovelPageBean) {
            removeView(mNovelPageBean.adView)
            if (mNovelPageBean.adView != null
                    && mNovelPageBean.adView!!.parent != null
                    && mNovelPageBean.adView!!.parent is ViewGroup) {
                (mNovelPageBean.adView!!.parent as ViewGroup).removeView(mNovelPageBean.adView)
            }
            val param = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            val margin = AppUtils.dip2px(context, 10f)
            val topMargin = AppUtils.dip2px(context, 40f)
            param.setMargins(margin, topMargin, margin, margin)
            addView(mNovelPageBean.adView, param)
        }

        //点击事件
        private var isShowMenu: Boolean = true

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (ReadConfig.animation == ReadViewEnums.Animation.curl){
                        if (onClick(event)) return true
                    }
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    if (ReadConfig.animation != ReadViewEnums.Animation.curl){
                        if (onClick(event)) return true
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isShowMenu) {
                        noticePageListener?.onClickMenu(isShowMenu)
                        isShowMenu = true
                    }
                }
                MotionEvent.ACTION_POINTER_DOWN-> return true
                MotionEvent.ACTION_POINTER_UP-> return true
            }
            return super.dispatchTouchEvent(event)
        }

        private var time: Long = 0
        private fun onClick(event: MotionEvent): Boolean {
            if (System.currentTimeMillis() - time < 500) {//动画时间
                return false
            }
            time = System.currentTimeMillis()
            val x = event.x.toInt()
            val y = event.y.toInt()
            val h4 = height / 4
            val w3 = width / 3
            return if (x <= w3) {
                noticePageListener?.onClickLeft(true)
                noticePageListener?.onClickMenu(false)
                true
            } else if (x >= width.minus(w3)|| y >= height.minus(h4) && x >= w3) {
                noticePageListener?.onClickRight(true)
                noticePageListener?.onClickMenu(false)
                true
            } else {
                noticePageListener?.onClickMenu(isShowMenu)
                isShowMenu = !isShowMenu
                true
            }
        }
    }

    fun onReSeparate() = DataProvider.getInstance().onReSeparate()
}