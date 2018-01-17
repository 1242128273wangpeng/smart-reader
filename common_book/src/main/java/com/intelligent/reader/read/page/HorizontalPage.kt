package com.intelligent.reader.read.page

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.intelligent.reader.R
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.exception.ReadCustomException
import com.intelligent.reader.read.help.DrawTextHelper
import com.intelligent.reader.read.mode.NovelPageBean
import com.intelligent.reader.read.mode.ReadCursor
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.read.util.ReadQueryUtil
import net.lzbook.kit.data.bean.ReadViewEnums
import com.intelligent.reader.util.ThemeUtil
import kotlinx.android.synthetic.main.book_home_page_layout.view.*
import kotlinx.android.synthetic.main.error_page2.view.*
import kotlinx.android.synthetic.main.read_bottom.view.*
import kotlinx.android.synthetic.main.read_top.view.*
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.AppUtils
import java.util.*
import kotlin.collections.ArrayList


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
class HorizontalPage : FrameLayout, Observer {

    private var mDrawTextHelper: DrawTextHelper? = null
    private lateinit var loadView: View
    private lateinit var errorView: View
    private lateinit var pageView: HorizontalItemPage
    private lateinit var readTop: View
    private lateinit var readBottom: View
    private lateinit var homePage: View

    var mCursorOffset = 0
    var mCursor: ReadCursor? = null
    var viewState: ReadViewEnums.ViewState = ReadViewEnums.ViewState.loading
    var viewNotify: ReadViewEnums.NotifyStateState = ReadViewEnums.NotifyStateState.none
    var pageIndex: Int = 0
    var pageSum: Int = 0
    var contentLength: Int = 0
    var noticePageListener: NoticePageListener? = null
    var mNovelPageBean: NovelPageBean? = null
    var hasAd = false
    var hasBigAd = false

    constructor(context: Context, noticePageListener: NoticePageListener) : this(context, null, noticePageListener)

    constructor(context: Context, attrs: AttributeSet?, noticePageListener: NoticePageListener) : this(context, attrs, 0, noticePageListener)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, noticePageListener: NoticePageListener) : super(context, attrs, defStyleAttr) {
        this.noticePageListener = noticePageListener
        isDrawingCacheEnabled = true
        drawingCacheQuality = View.DRAWING_CACHE_QUALITY_LOW
        isChildrenDrawnWithCacheEnabled = true
        isChildrenDrawnWithCacheEnabled = true
        init()
    }

    private fun init() {
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
        var colorInt = when (ReadConfig.MODE) {
            51 -> R.color.reading_operation_text_color_first
            52 -> R.color.reading_text_color_second
            53 -> R.color.reading_text_color_third
            54 -> R.color.reading_text_color_fourth
            55 -> R.color.reading_text_color_fifth
            56 -> R.color.reading_text_color_sixth
            61 -> R.color.reading_text_color_night
            else -> R.color.reading_operation_text_color_first
        }
        novel_time.setTextColor(resources.getColor(colorInt))
        origin_tv.setTextColor(resources.getColor(colorInt))
        trans_coding_tv.setTextColor(resources.getColor(colorInt))
        novel_page.setTextColor(resources.getColor(colorInt))
        novel_chapter.setTextColor(resources.getColor(colorInt))
        novel_title.setTextColor(resources.getColor(colorInt))
        //pageView
        ThemeUtil.getModePrimaryBackground(resources, pageView)
        //电池背景
        ThemeUtil.getModePrimaryBackground(resources, novel_content_battery_view)
        //封面页
        ThemeUtil.getModePrimaryBackground(resources, homePage)
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

    fun setCursor(cursor: ReadCursor) = pageView.setCursor(cursor)

    private fun onReSeparate() = DataProvider.getInstance().onReSeparate()

    fun onRedrawPage() {
        if (tag == ReadViewEnums.PageIndex.current) {
            onReSeparate()
            viewNotify = when (mCursor!!.sequence) {
                -1 -> ReadViewEnums.NotifyStateState.right
                else -> ReadViewEnums.NotifyStateState.all
            }
            setCursor(mCursor!!)
        }
    }

    private fun onScreenChange() {
        onRedrawPage()
        checkAdBiggerView()
    }

    private fun onJumpChapter() = if (tag == ReadViewEnums.PageIndex.current) noticePageListener?.onJumpChapter() ?: Unit else Unit

    //段末广告 8-1
    private fun checkAdBanner(topMargins: Float) {
        DataProvider.getInstance().loadAd(context, "8-1", mCursor!!.readStatus.screenWidth, mCursor!!.readStatus.screenHeight - topMargins.toInt(), object : DataProvider.OnLoadReaderAdCallback {
            override fun onLoadAd(adView: ViewGroup) {
                val param = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                val margin = AppUtils.dip2px(context, 10f)
                param.setMargins(margin, topMargins.toInt(), margin, margin)
                addView(adView, param)
            }
        })
    }

    //广告页 5-1 5-2 6-1 6-2
    private fun checkAdBiggerView() {
        if ((mNovelPageBean != null) and hasBigAd) {
            val adType = if (ReadConfig.IS_LANDSCAPE) {
                if (pageIndex == pageSum) "6-1" else "6-2"
            } else {
                if (pageIndex == pageSum) "5-1" else "5-2"
            }
            DataProvider.getInstance().loadAd(context, adType, object : DataProvider.OnLoadReaderAdCallback {
                override fun onLoadAd(adView: ViewGroup) {
                    if (mNovelPageBean!!.adView != null && mNovelPageBean!!.adView!!.parent != null) {
                        removeView(mNovelPageBean!!.adView)
                    } else {
                        mNovelPageBean!!.adView = adView
                        val param = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                        val leftMargin = AppUtils.dip2px(context, 10f)
                        val rightMargin = AppUtils.dip2px(context, 10f)
                        val topMargin = AppUtils.dip2px(context, 40f)
                        val bottomMargin = AppUtils.dip2px(context, 30f)
                        param.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
                        addView(mNovelPageBean!!.adView, param)
                    }
                }
            })
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

    private fun showErrorView(cursor: ReadCursor) {
        //Error
        addView(errorView)
        viewState = ReadViewEnums.ViewState.error
        loadView.visibility = View.GONE
        errorView.loading_error_reload.setOnClickListener({
            pageView.entrance(cursor)
            loadView.visibility = View.VISIBLE
            viewState = ReadViewEnums.ViewState.loading
            removeView(errorView)
        })
        errorView.loading_error_setting.visibility = FrameLayout.GONE
    }

    override fun update(o: Observable, arg: Any) {
        when (arg as String) {
            "READ_INTERLINEAR_SPACE" -> onRedrawPage()
            "FONT_SIZE" -> onRedrawPage()
            "SCREEN" -> onScreenChange()
            "MODE" -> setupView()
            "JUMP" -> onJumpChapter()
        }
    }

    interface NoticePageListener {
        fun pageChangSuccess(cursor: ReadCursor, notify: ReadViewEnums.NotifyStateState)
        fun onClickLeft(smoothScroll: Boolean)
        fun onClickRight(smoothScroll: Boolean)
        fun onClickMenu(isShow: Boolean)
        fun loadOrigin()
        fun loadTransCoding()
        fun currentViewSuccess()
        fun onJumpChapter()
    }

    inner class HorizontalItemPage : View {

        constructor(context: Context) : this(context, null)
        constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (mNovelPageBean != null) {
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
                    showErrorView(cursor)
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
                preDrawPage(cursor, chapter)
            } else {//无缓存数据
                entrance(cursor)
            }
        }

        /**
         * 画页面前准备
         */
        private fun preDrawPage(cursor: ReadCursor, chapter: Chapter) {
            //获取数据
            cursor.readStatus.chapterName = chapter.chapter_name
            val chapterList = DataProvider.getInstance().chapterSeparate[cursor.sequence]!!
            try {
                pageIndex = ReadQueryUtil.findPageIndexByOffset(cursor.offset, chapterList)
            } catch (e: ReadCustomException.PageIndexException) {
                showErrorView(mCursor!!)
                return
            }
            pageSum = chapterList.size
            if (pageIndex <= pageSum) {
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
                    noticePageListener?.pageChangSuccess(mCursor!!, ReadViewEnums.NotifyStateState.none)//游标通知回调
                } else {
                    try {
                        mNovelPageBean = ReadQueryUtil.findNovelPageBeanByOffset(cursor.offset, chapterList)
                    } catch (e: ReadCustomException.PageOffsetException) {
                        showErrorView(mCursor!!)
                        return
                    }
                    hasAd = mNovelPageBean!!.isAd
                    hasBigAd = mNovelPageBean!!.isAd
                    contentLength = mNovelPageBean!!.contentLength
                    if (mNovelPageBean!!.isAd) {//广告页
                        checkAdBiggerView()
                    } else {//普通页
                        //画之前清空内容
                        removeView(homePage)
                        postInvalidate()
                        //判断展示Banner广告
                        val topMargin = if (mNovelPageBean?.lines?.isNotEmpty() == true) mNovelPageBean!!.height else ReadConfig.screenHeight.toFloat()
                        if (cursor.readStatus.screenHeight - topMargin > cursor.readStatus.screenHeight / 5) {
                            hasAd = true
                            checkAdBanner(topMargin)
                        }
                    }
                    changeCursorState(chapterList)

                    //设置top and bottom
                    val chapterProgress = "" + (cursor.sequence.plus(1)) + "/" + cursor.readStatus.chapterCount + "章"
                    val pageProgress = "本章第$pageIndex/$pageSum"
                    setTopAndBottomViewContext(cursor.readStatus.chapterName, chapterProgress, pageProgress)
                    noticePageListener?.currentViewSuccess()
                }
            }

        }

        private fun changeCursorState(chapterList: ArrayList<NovelPageBean>) {
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
            mCursorOffset = when {
                (pageSum >= 16) and (pageIndex >= 9) and (pageIndex != pageSum) -> -1
                (pageSum >= 16) and (pageIndex >= 9) and (pageIndex == pageSum) -> -2
                (pageSum < 16) and (pageIndex == pageSum) -> -1
                else -> 0
            }
        }

        private var isShowMenu: Boolean = false

        override fun onTouchEvent(event: MotionEvent): Boolean {
            return mGestureDetector.onTouchEvent(event)
        }

        private val mGestureDetector by lazy {
            val detector = GestureDetector(context, object : GestureDetector.OnGestureListener {
                private var time: Long = 0

                override fun onDown(event: MotionEvent): Boolean {
                    if (System.currentTimeMillis() - time < 500) {
                        return false
                    }
                    time = System.currentTimeMillis()

                    if (ReadConfig.animation == ReadViewEnums.Animation.curl) {
                        return isTouchMenuArea(event)
                    } else {
                        return true
                    }
                }

                override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                    return true
                }

                override fun onLongPress(e: MotionEvent) = Unit

                override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = false

                override fun onShowPress(e: MotionEvent) = Unit

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    if (isShowMenu) {
                        noticePageListener?.onClickMenu(false)
                        isShowMenu = false
                    } else {
                        if (isTouchMenuArea(e)) {
                            noticePageListener?.onClickMenu(true)
                            isShowMenu = true
                        } else {
                            if (e.x < width / 2) {
                                //left
                                noticePageListener?.onClickLeft(true)
                            } else {
                                noticePageListener?.onClickRight(true)
                            }
                        }
                    }
                    return true
                }
            })
            detector.setIsLongpressEnabled(false)
            detector
        }

        private fun isTouchMenuArea(event: MotionEvent): Boolean {
            val x = event.x.toInt()
            val y = event.y.toInt()
            val h4 = height / 4
            val w3 = width / 3
            return (x > w3) && !(x >= width.minus(w3) || y >= height.minus(h4) && x >= w3)
        }
    }
}