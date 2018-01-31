package com.intelligent.reader.read.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.intelligent.reader.R
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.help.DrawTextHelper
import com.intelligent.reader.read.mode.NovelChapter
import com.intelligent.reader.read.mode.NovelPageBean
import com.intelligent.reader.read.mode.ReadCursor
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.read.util.ReadQueryUtil
import com.intelligent.reader.util.ThemeUtil
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.book_home_page_layout.view.*
import kotlinx.android.synthetic.main.error_page2.view.*
import kotlinx.android.synthetic.main.loading_page_reading.view.*
import kotlinx.android.synthetic.main.read_bottom.view.*
import kotlinx.android.synthetic.main.read_top.view.*
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.data.bean.ReadViewEnums
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.runOnMain
import java.util.*


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
    private lateinit var mAdFrameLayout: FrameLayout

    var mCursorOffset = 0
    var mCursor: ReadCursor? = null
    var viewState: ReadViewEnums.ViewState = ReadViewEnums.ViewState.loading
    var viewNotify: ReadViewEnums.NotifyStateState = ReadViewEnums.NotifyStateState.none
    var pageIndex: Int = 0
    var pageSum: Int = 0
    var contentLength: Int = 0
    var hasAd = false
    var hasBigAd = false
    var noticePageListener: NoticePageListener? = null
    var mNovelPageBean: NovelPageBean? = null

    var orientationLimit = ReadViewEnums.ScrollLimitOrientation.NONE

    internal var mDisposable: CompositeDisposable = CompositeDisposable()

//    var drawCacheBitmap:Bitmap? = null
//    var innerCanvas:Canvas? = null

    constructor(context: Context, noticePageListener: NoticePageListener) : this(context, null, noticePageListener)

    constructor(context: Context, attrs: AttributeSet?, noticePageListener: NoticePageListener) : this(context, attrs, 0, noticePageListener)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, noticePageListener: NoticePageListener) : super(context, attrs, defStyleAttr) {
        this.noticePageListener = noticePageListener
        isDrawingCacheEnabled = true
        init()
    }

    private fun init() {
        mDrawTextHelper = DrawTextHelper(context.resources)
        loadView = inflate(context, R.layout.loading_page_reading, null)
        errorView = inflate(context, R.layout.error_page2, null)
        readTop = inflate(context, R.layout.read_top, null)
        readBottom = inflate(context, R.layout.read_bottom, null)
        homePage = inflate(context, R.layout.book_home_page2_layout, null)
        pageView = HorizontalItemPage(context)
        mAdFrameLayout = FrameLayout(context)
        addView(pageView)
        addView(mAdFrameLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(readTop)
        addView(readBottom)
        addView(loadView)

        loadView.setOnTouchListener { v, event ->
            requestDisallowInterceptTouchEvent(true)
            true
        }
        errorView.setOnTouchListener { v, event ->
            requestDisallowInterceptTouchEvent(true)
            true
        }
    }

    override fun getDrawingCache(): Bitmap? {
        if (ReadViewEnums.PageIndex.current.equals(tag) && (hasAd || hasBigAd)) {
            destroyDrawingCache()
        }
        return super.getDrawingCache()
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
        val textColor = ReadQueryUtil.getColor(resources)

        novel_time.setTextColor(textColor)
        origin_tv.setTextColor(textColor)
        trans_coding_tv.setTextColor(textColor)
        novel_page.setTextColor(textColor)
        novel_chapter.setTextColor(textColor)
        novel_title.setTextColor(textColor)
        tv_loading_progress.setTextColor(textColor)
        //pageView
        ThemeUtil.getModePrimaryBackground(resources, pageView)
        //电池背景
        ThemeUtil.getModePrimaryBackground(resources, novel_content_battery_view)
        //封面页
        ThemeUtil.getModePrimaryBackground(resources, homePage)
        //进度条
        ThemeUtil.getModePrimaryBackground(resources, loadView)
    }

    override fun onDetachedFromWindow() {
        ReadConfig.unregistObserver(this)
        mDisposable.clear()
        mAdFrameLayout.removeAllViews()

        removeView(homePage)
        readTop.visibility = View.VISIBLE
        readBottom.visibility = View.VISIBLE

        mCursorOffset = 0
        mCursor = null
        viewState = ReadViewEnums.ViewState.loading
        viewNotify = ReadViewEnums.NotifyStateState.none
        pageIndex = 0
        pageSum = 0
        contentLength = 0
        hasAd = false
        hasBigAd = false
//        mNovelPageBean?.adBigView = null
        mNovelPageBean = null
        super.onDetachedFromWindow()
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

    fun setCursor(cursor: ReadCursor) {
        mDisposable.clear()
        ReadConfig.registObserver(this)
        mAdFrameLayout.removeAllViews()
        removeView(errorView)
        setupView()
        pageView.setCursor(cursor)
    }

    private fun onReSeparate() = DataProvider.getInstance().onReSeparate()

    fun onRedrawPage() {
        mAdFrameLayout.removeAllViews()

        viewState = ReadViewEnums.ViewState.other

        if (tag == ReadViewEnums.PageIndex.current) {
            onReSeparate()
            mCursor?.let {
                viewNotify = when (it.sequence) {
                    -1 -> ReadViewEnums.NotifyStateState.right
                    else -> ReadViewEnums.NotifyStateState.all
                }
                setCursor(it)
            }
        }
    }

    private fun onScreenChange() {
        mAdFrameLayout.removeAllViews()
        onRedrawPage()
//        checkAdBiggerView()
    }

    private fun onJumpChapter() {
        mAdFrameLayout.removeAllViews()
        if (tag == ReadViewEnums.PageIndex.current) {
            noticePageListener?.onJumpChapter()
        }
        viewState = ReadViewEnums.ViewState.other
    }

//    //段末广告 8-1
//    private fun checkAdBanner(topMargins: Float) {
//        if (PlatformSDK.config().getAdSwitch("5-1")) {
//            mDisposable.add(io.reactivex.Observable.create<ViewGroup> {
//                DataProvider.getInstance().loadAd(context, "8-1", ReadConfig.screenWidth, ReadConfig.screenHeight - topMargins.toInt(), object : DataProvider.OnLoadReaderAdCallback {
//                    override fun onFail() {
//                    }
//
//                    override fun onLoadAd(adView: ViewGroup) {
//                        if(!it.isDisposed) {
//                            it.onNext(adView)
//                        }
//                        it.onComplete()
//                    }
//                })
//            }.subscribekt(
//                    onNext = {
//                        val param = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
//                        val margin = AppUtils.dip2px(context, 10f)
//                        param.setMargins(margin, topMargins.toInt(), margin, margin)
//                        mAdFrameLayout.removeAllViews()
//                        mAdFrameLayout.addView(it, param)
//                    },
//                    onError = {
//                        it.printStackTrace()
//                    }
//            ))
//
//        }
//    }

    //广告页 5-1 5-2 6-1 6-2
//    private fun checkAdBiggerView() {
//        if ((mNovelPageBean != null) and hasBigAd) {
//            val adType = if (ReadConfig.IS_LANDSCAPE) {
//                if (pageIndex == pageSum) "6-1" else "6-2"
//            } else {
//                if (pageIndex == pageSum) "5-1" else "5-2"
//            }
//
//            mDisposable.add(io.reactivex.Observable.create<ViewGroup> {
//                DataProvider.getInstance().loadAd(context, adType, object : DataProvider.OnLoadReaderAdCallback {
//                    override fun onFail() {
//                    }
//
//                    override fun onLoadAd(adView: ViewGroup) {
//                        if(!it.isDisposed) {
//                            it.onNext(adView)
//                        }
//                        it.onComplete()
//                    }
//                })
//            }.subscribekt(
//                    onNext = {
//                        if (mNovelPageBean!!.adBigView != null && mNovelPageBean!!.adBigView!!.parent != null) {
//                            mAdFrameLayout.removeView(mNovelPageBean!!.adBigView)
//                        } else {
//                            mNovelPageBean!!.adBigView = it
//                            val param = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
//                            val leftMargin = AppUtils.dip2px(context, 10f)
//                            val rightMargin = AppUtils.dip2px(context, 10f)
//                            val topMargin = AppUtils.dip2px(context, 40f)
//                            val bottomMargin = AppUtils.dip2px(context, 30f)
//                            param.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
//                            mAdFrameLayout.addView(mNovelPageBean!!.adBigView, param)
//                        }
//                    },
//                    onError = {
//                        it.printStackTrace()
//                    }
//            )
//            )
//
//
//        }
//    }

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
        var color = ReadQueryUtil.getHomePageColor(resources)
        homePage.book_name_tv.setTextColor(color)
        homePage.book_auth_tv.setTextColor(color)
        homePage.slogan_tv.setTextColor(color)
        homePage.product_name_tv.setTextColor(color)
        postInvalidate()
        //改变状态
        mCursor!!.sequence = -1
        viewState = ReadViewEnums.ViewState.start
        loadView.visibility = View.GONE
        readTop.visibility = View.GONE
        readBottom.visibility = View.GONE
    }

    private fun showErrorView(cursor: ReadCursor) {
        runOnMain {
            //Error
            removeView(errorView)
            addView(errorView)
            ThemeUtil.getModePrimaryBackground(resources, errorView)

            viewState = ReadViewEnums.ViewState.error
            loadView.visibility = View.GONE
            errorView.loading_error_reload.setOnClickListener({
                pageView.entrance(cursor)
                mAdFrameLayout.removeAllViews()
                loadView.visibility = View.VISIBLE
                viewState = ReadViewEnums.ViewState.loading
                viewNotify = ReadViewEnums.NotifyStateState.all
                removeView(errorView)
                post {
                    destroyDrawingCache()
                }
            })
            errorView.loading_error_setting.visibility = FrameLayout.GONE
            noticePageListener?.pageChangSuccess(cursor, viewNotify)//游标通知回调
        }
    }

    override fun update(o: Observable, arg: Any) {
        destroyDrawingCache()
        if(ReadViewEnums.PageIndex.current == tag) {
            when (arg as String) {
                "READ_INTERLINEAR_SPACE" -> onRedrawPage()
                "FONT_SIZE" -> onRedrawPage()
                "SCREEN" -> onScreenChange()
                "MODE" -> setupView()
                "JUMP" -> onJumpChapter()
            }
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

        var entranceArray = arrayOf(false, false, false)
        /**
         * 入口模式
         * 加载3章至内存
         */
        fun entrance(cursor: ReadCursor) {
            mCursor = cursor
            entranceArray = arrayOf(false, false, false)
            cursor.curBook.sequence = cursor.sequence

            //sequence检测
            if (ReadState.chapterList.size > 0) {
                if (cursor.curBook.sequence >= ReadState.chapterList.size) {
                    cursor.curBook.sequence = ReadState.chapterList.size - 1
                }
            }

            if (!DataProvider.getInstance().isCacheExistBySequence(cursor.curBook.sequence)) {
                loadView.visibility = View.VISIBLE
            }
            DataProvider.getInstance().loadChapter(cursor.curBook, cursor.sequence, ReadViewEnums.PageIndex.current, object : DataProvider.ReadDataListener() {
                override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) = checkEntrance(cursor, 0)
                override fun loadDataError(message: String) = showErrorView(cursor)
            })
            DataProvider.getInstance().loadChapter(cursor.curBook, cursor.sequence, ReadViewEnums.PageIndex.previous, object : DataProvider.ReadDataListener() {
                override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) = checkEntrance(cursor, 1)
                override fun loadDataError(message: String) = checkEntrance(cursor, 1)
            })
            DataProvider.getInstance().loadChapter(cursor.curBook, cursor.sequence, ReadViewEnums.PageIndex.next, object : DataProvider.ReadDataListener() {
                override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) = checkEntrance(cursor, 2)
                override fun loadDataError(message: String) = checkEntrance(cursor, 2)
            })
        }

        fun checkEntrance(cursor: ReadCursor, index: Int) {
            //当前章拉去成功 执行一般方法
            //true 通知其他页面加载
            entranceArray[index] = true
            if (entranceArray.all { it }) {
                setCursor(cursor)
                this@HorizontalPage.destroyDrawingCache()
            }
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
            //判断item 需要的章节是否在缓存
            val novelChapter = DataProvider.getInstance().chapterLruCache[cursor.sequence]
            if (novelChapter != null) {//加载数据
                preDrawPage(cursor, novelChapter)
            } else {//无缓存数据
                entrance(cursor)
            }
        }

        /**
         * 画页面前准备
         */
        private fun preDrawPage(cursor: ReadCursor, novelChapter: NovelChapter) {
            //判断超过章节数
//            val bookChapterDao = BookChapterDao(BaseBookApplication.getGlobalContext(), cursor.curBook.book_id)
//            ReadState.novelChapter = bookChapterDao.queryBookChapter()
            if ((ReadState.chapterList.isNotEmpty()) and (mCursor!!.sequence > ReadState.chapterList.size - 1)) {
                viewState = ReadViewEnums.ViewState.end
                return
            }
            //获取数据
            ReadState.chapterName = novelChapter.chapter.chapter_name
//            cursor.offset = cursor.offset - mCursorOffset
//            cursor.offset = cursor.offset
            try {
                pageIndex = ReadQueryUtil.findPageIndexByOffset(cursor.offset, novelChapter.separateList)
            } catch (e: Exception) {
                showErrorView(mCursor!!)
                return
            }
            pageSum = novelChapter.separateList.size
            if (pageIndex <= pageSum) {
                //过滤其他页内容
                if (cursor.sequence == -1) {//封面页
                    orientationLimit = ReadViewEnums.ScrollLimitOrientation.LEFT

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

                    if (cursor.sequence == ReadState.chapterCount - 1 && pageIndex == pageSum) {
                        orientationLimit = ReadViewEnums.ScrollLimitOrientation.RIGHT
                    } else {
                        orientationLimit = ReadViewEnums.ScrollLimitOrientation.NONE
                    }

                    try {
                        mNovelPageBean = ReadQueryUtil.findNovelPageBeanByOffset(cursor.offset, novelChapter.separateList)
                    } catch (e: Exception) {
                        showErrorView(mCursor!!)
                        return
                    }
                    hasAd = mNovelPageBean!!.isAd
                    hasBigAd = mNovelPageBean!!.isAd
                    contentLength = mNovelPageBean!!.contentLength
                    if (mNovelPageBean!!.isAd) {//广告页
                        //已经曝光过的广告，移除并回收
                        if (mNovelPageBean!!.adBigView != null) {
                            if (mNovelPageBean!!.adBigView!!.parent == null) {
                                mAdFrameLayout.addView(mNovelPageBean!!.adBigView)
                            } else {
                                mNovelPageBean!!.adBigView!!.removeAllViewsInLayout()
                                mNovelPageBean!!.adBigView = null
                            }
                        }
                    } else {//普通页

                        //记录阅读位置
                        if (ReadViewEnums.PageIndex.current == tag) {
                            ReadState.currentPage = pageIndex
                            ReadState.sequence = cursor.sequence
                            ReadState.offset = cursor.offset
                        }

                        //画之前清空内容
                        post {
                            removeView(homePage)
                            postInvalidate()
                        }
                        //判断展示Banner广告

                        val topMargin = if (mNovelPageBean?.lines?.isNotEmpty() == true) mNovelPageBean!!.height else ReadConfig.screenHeight.toFloat()
//                        if (ReadConfig.screenHeight - topMargin > ReadConfig.screenHeight / 5) {
//                            hasAd = false
//                            if (!Constants.isHideAD) {
//                                checkAdBanner(topMargin)
//                                hasAd = true
//                            }
//                        }

                        val param = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                        val margin = AppUtils.dip2px(context, 10f)
                        val marginBottom = AppUtils.dip2px(context, 30f)
                        param.setMargins(margin, topMargin.toInt(), margin, marginBottom)

                        if(mNovelPageBean?.adSmallView != null && mNovelPageBean?.adSmallView?.parent == null){
                            hasAd = true
                            mAdFrameLayout.addView(mNovelPageBean!!.adSmallView, param)
                        }

                    }

                    //设置top and bottom
                    val chapterProgress = "${cursor.sequence.plus(1)} / ${ReadState.chapterList.size} 章"
                    val pageProgress = "本章第$pageIndex/$pageSum"
                    setTopAndBottomViewContext(ReadState.chapterName ?: "", chapterProgress, pageProgress)

                    noticePageListener?.currentViewSuccess()

                    changeCursorState(novelChapter.separateList)

                }
            }

        }

        private fun changeCursorState(chapterList: ArrayList<NovelPageBean>) {
            //改状态、游标状态
            loadView.visibility = View.GONE
            mCursor!!.lastOffset = chapterList.last().offset
            mCursor!!.offset = mNovelPageBean!!.offset
            mCursor!!.nextOffset = if (pageIndex < chapterList.size) chapterList[pageIndex].offset else 0



            if (this@HorizontalPage.tag == ReadViewEnums.PageIndex.current && viewState != ReadViewEnums.ViewState.success) {
                viewNotify = ReadViewEnums.NotifyStateState.all

                noticePageListener?.pageChangSuccess(mCursor!!, viewNotify)//游标通知回调
            }
            viewState = if ((mCursor!!.sequence == ReadState.chapterList.size - 1) and (pageIndex == pageSum)) {//判断这本书的最后一页
                ReadViewEnums.ViewState.end
            } else {
                ReadViewEnums.ViewState.success
            }

            //游标添加广告后的偏移量
//            mCursorOffset = when {
//                (pageSum >= 16) and (pageIndex >= pageSum / 2) and (pageIndex != pageSum) -> {
//                    if (PlatformSDK.config().getAdSwitch("5-1") and (PlatformSDK.config().getAdSwitch("6-1"))) -1 else 0
//                }
//                (pageSum >= 16) and (pageIndex >= pageSum / 2) and (pageIndex == pageSum) -> {
//                    if (PlatformSDK.config().getAdSwitch("5-1") and (PlatformSDK.config().getAdSwitch("6-1"))) {
//                        if (PlatformSDK.config().getAdSwitch("5-2") and (PlatformSDK.config().getAdSwitch("6-2"))) -2 else -1
//                    } else {
//                        if (PlatformSDK.config().getAdSwitch("5-2") and (PlatformSDK.config().getAdSwitch("6-2"))) -1 else 0
//                    }
//                }
//                (pageSum < 16) and (pageIndex == pageSum) -> -1
//                else -> 0
//            }
        }


        private var isShowMenu: Boolean = false

        override fun onTouchEvent(event: MotionEvent): Boolean {
            return if (viewState == ReadViewEnums.ViewState.loading || viewState == ReadViewEnums.ViewState.error) {
                return false
            } else {
                mGestureDetector.onTouchEvent(event)
            }
        }

        private val mGestureDetector by lazy {
            val detector = GestureDetector(context, object : GestureDetector.OnGestureListener {
//                private var time: Long = 0

                override fun onDown(event: MotionEvent): Boolean {
//                    if (System.currentTimeMillis() - time < 500) {
//                        return false
//                    }
//                    time = System.currentTimeMillis()

                    if (ReadConfig.animation == ReadViewEnums.Animation.curl) {
                        return isTouchMenuArea(event)
                    } else {
                        return true
                    }
                }

                override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                    if (isShowMenu) {
                        noticePageListener?.onClickMenu(false)
                        isShowMenu = false
                    }
                    return false
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
                                if (ReadConfig.FULL_SCREEN_READ) {
                                    noticePageListener?.onClickRight(true)
                                } else {
                                    noticePageListener?.onClickLeft(true)
                                }

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