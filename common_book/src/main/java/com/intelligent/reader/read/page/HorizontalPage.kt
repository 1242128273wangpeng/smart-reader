package com.intelligent.reader.read.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.animation.BitmapManager
import com.intelligent.reader.read.help.DrawTextHelper
import com.intelligent.reader.read.help.ReadSeparateHelper
import com.intelligent.reader.read.mode.ReadCursor
import com.intelligent.reader.read.mode.ReadViewEnums
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.R
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.utils.ToastUtils
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
class HorizontalPage : FrameLayout {

    private val paint: Paint = Paint()
    private var mDrawTextHelper: DrawTextHelper? = null
    private lateinit var loadView:View
    private lateinit var errorView:View
    private lateinit var pageView:HorizontalItemPage

    var mCurPageBitmap: Bitmap? = null
    var mCurrentCanvas: Canvas? = null
    var mCursor: ReadCursor? = null
    var viewState:ReadViewEnums.ViewState = ReadViewEnums.ViewState.loading
    var viewNotify:ReadViewEnums.NotifyStateState = ReadViewEnums.NotifyStateState.none

    constructor(context: Context, noticePageListener: NoticePageListener) : this(context, null, noticePageListener)

    constructor(context: Context, attrs: AttributeSet?, noticePageListener: NoticePageListener) : this(context, attrs, 0, noticePageListener)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, noticePageListener: NoticePageListener) : super(context, attrs, defStyleAttr) {
        this.noticePageListener = noticePageListener
        init()
    }

    private fun init() {
        mDrawTextHelper = DrawTextHelper(context.resources)
        loadView = LayoutInflater.from(context).inflate(R.layout.loading_page_reading, null)
        errorView = LayoutInflater.from(context).inflate(R.layout.error_page2, null)
        pageView = HorizontalItemPage(context)
        addView(loadView)
        addView(errorView)
        addView(pageView)
        errorView.visibility = View.GONE
    }

    fun setCursor(cursor: ReadCursor){
        pageView.setCursor(cursor)
    }

    var noticePageListener: NoticePageListener? = null

    interface NoticePageListener {
        fun pageChangSuccess(cursor:ReadCursor,notify:ReadViewEnums.NotifyStateState)
        fun onClickLeft()
        fun onClickRight()
        fun onClickMenu(isShow: Boolean)
    }

    inner class HorizontalItemPage:View{

        constructor(context: Context) : this(context, null)
        constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (mCurPageBitmap != null) {
                if (!mCurPageBitmap!!.isRecycled) canvas.drawBitmap(mCurPageBitmap, 0f, 0f, paint)
            }
        }
        /**
         * 入口模式
         * 加载3章至内存
         */
        fun entrance(cursor: ReadCursor) {
            DataProvider.getInstance().loadChapter(cursor.curBook, cursor.sequence, ReadViewEnums.PageIndex.current, object : DataProvider.ReadDataListener {
                override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) {
                    //当前章拉去成功 执行一般方法
                    //true 通知其他页面加载
                    setCursor(cursor)
                }
                override fun loadDataError(message: String){
                    //Error
                    viewState = ReadViewEnums.ViewState.error
                    loadView.visibility = View.GONE
                    errorView.visibility = View.VISIBLE
                    errorView.findViewById(R.id.loading_error_reload).setOnClickListener({
                        entrance(cursor)
                        loadView.visibility = View.VISIBLE
                        errorView.visibility = View.GONE
                        viewState = ReadViewEnums.ViewState.loading
                    })
                }
            })
            DataProvider.getInstance().loadChapter(cursor.curBook, cursor.sequence, ReadViewEnums.PageIndex.previous, object : DataProvider.ReadDataListener {
                override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) = Unit
                override fun loadDataError(message: String) = Unit
            })
            DataProvider.getInstance().loadChapter(cursor.curBook, cursor.sequence, ReadViewEnums.PageIndex.next, object : DataProvider.ReadDataListener {
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
            val provider = DataProvider.getInstance()
            //判断item 需要的章节是否在缓存
            val chapter = provider.chapterMap[cursor.sequence]
            if (chapter != null) {//加载数据
                drawPage(cursor, chapter)
            } else {//无缓存数据
                entrance(cursor)
            }
        }

        /**
         * 画页面
         */
        private fun drawPage(cursor: ReadCursor, chapter: Chapter) {
            cursor.readStatus.chapterName = chapter.chapter_name
            val chapterList = ReadSeparateHelper.getInstance(cursor.readStatus).initTextSeparateContent(chapter.content)//分页
            if (!chapterList.isEmpty() and (cursor.pageIndex <= chapterList.size)) {//集合不为空，角标小于集合长度
                mCursor!!.pageIdexSum = chapterList.size//确定总长度
                noticePageListener?.pageChangSuccess(mCursor!!,viewNotify)//游标通知回调
                cursor.readStatus.currentPage = cursor.pageIndex//画页码
                val pageList = if (cursor.pageIndex == -1) {//分页前不知道上一章长度
                    mCursor!!.pageIndex = chapterList.size//确定长度
                    chapterList[chapterList.size - 1]
                } else {
                    chapterList[cursor.pageIndex - 1]
                }
                mCurPageBitmap = BitmapManager.getInstance().createBitmap()
                mCurrentCanvas = Canvas(mCurPageBitmap)
                mDrawTextHelper?.drawText(mCurrentCanvas, pageList)
                postInvalidate()
                viewState = ReadViewEnums.ViewState.success
                loadView.visibility = View.GONE
            }
        }

        private var lastTouchY: Int = 0
        private var startTouchTime: Long = 0
        private var startTouchX: Int = 0
        private var startTouchY: Int = 0
        private var isShowMenu:Boolean = true

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            val tmpX = event.x.toInt()
            val tmpY = event.y.toInt()
            when(event.action) {
                MotionEvent.ACTION_DOWN->{
                    lastTouchY = tmpY
                    startTouchTime = System.currentTimeMillis()
                    startTouchX = tmpX
                    startTouchY = tmpY
                    return true
                }
                MotionEvent.ACTION_CANCEL ->{
                    startTouchTime = 0
                    return true
                }
                MotionEvent.ACTION_UP ->{
                    val touchTime = System.currentTimeMillis() - startTouchTime
                    val distance = Math.sqrt(Math.pow((startTouchX - tmpX).toDouble(), 2.0) + Math.pow((startTouchY - tmpY).toDouble(), 2.0)).toInt()
                    if (touchTime < 100 && distance < 30 || distance < 10) {
                        if(onClick(event)) return true
                    }
                    startTouchTime = 0
                }
                MotionEvent.ACTION_MOVE ->{
                    if (!isShowMenu){
                        noticePageListener?.onClickMenu(isShowMenu)
                        isShowMenu = true
                    }
                }
            }
            return super.dispatchTouchEvent(event)
        }

        private var time: Long = 0
        private fun onClick(event: MotionEvent) : Boolean{
            if (System.currentTimeMillis() - time < 500) {//动画时间
                return false
            }
            time = System.currentTimeMillis()
            val x = event.x.toInt()
            val y = event.y.toInt()
            val h4 = height / 4
            val w3 = width / 3
            return if (x <= w3) {
                noticePageListener?.onClickLeft()
                true
            } else if (x >= width - w3 || y >= height - h4 && x >= w3) {
                noticePageListener?.onClickRight()
                true
            } else {
                noticePageListener?.onClickMenu(isShowMenu)
                isShowMenu = !isShowMenu
                true
            }
        }
    }
}