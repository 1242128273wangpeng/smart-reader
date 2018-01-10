package com.intelligent.reader.read.page

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.flip.PageFlipView
import com.intelligent.reader.flip.base.PageFlip
import com.intelligent.reader.flip.render.SinglePageRender
import com.intelligent.reader.read.animation.BitmapManager
import com.intelligent.reader.read.factory.ReaderViewFactory
import com.intelligent.reader.read.help.IReadPageChange
import com.intelligent.reader.read.help.IReadView
import com.intelligent.reader.read.help.IReadWidget
import com.intelligent.reader.read.mode.ReadInfo
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.data.bean.ReadViewEnums
import java.util.*
import android.view.WindowManager
import com.intelligent.reader.read.help.HorizontalEvent
import kotlinx.android.synthetic.main.layout_custom_dialog.view.*
import net.lzbook.kit.utils.runOnMain
import net.lzbook.kit.utils.AppLog


/**
 * 阅读容器
 * Created by wt on 2017/12/13.
 */
class ReaderViewWidget : FrameLayout, IReadWidget, HorizontalEvent {

    private var mReaderViewFactory: ReaderViewFactory? = null

    private var mReaderView: IReadView? = null

    private var mTextureView: PageFlipView? = null

    private var animaEnums: ReadViewEnums.Animation? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var num = Integer.MAX_VALUE / 2

    private var mLoadBitmaplistener = SinglePageRender.LoadBitmapListener {

        if ((mTextureView != null) and (mTextureView!!.isFangzhen) and (mReaderView is HorizontalReaderView)) {

            var bitmap: Bitmap? = null
            var isFinishCache = false

            runOnMain {
                bitmap = when {
                    num < it -> {
                        val view = (mReaderView as HorizontalReaderView).findViewWithTag(ReadViewEnums.PageIndex.next)
                        view.getDrawingCache(true)
                    }
                    num > it -> {
                        val view = (mReaderView as HorizontalReaderView).findViewWithTag(ReadViewEnums.PageIndex.previous)
                        view.getDrawingCache(true)
                    }
                    else -> {
                        val view = (mReaderView as HorizontalReaderView).findViewWithTag(ReadViewEnums.PageIndex.current)
                        view.getDrawingCache(true)
                    }
                }
                synchronized((this@ReaderViewWidget as Object)) {
                    println("this@ReaderViewWidget as Object).notify")
                    (this@ReaderViewWidget as Object).notify()
                }
                isFinishCache = true
            }


            synchronized((this@ReaderViewWidget as Object)) {
                if (!isFinishCache) {
                    try {
                        println("this@ReaderViewWidget as Object).wait")
                        (this@ReaderViewWidget as Object).wait(100)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            return@LoadBitmapListener bitmap
        }
        BitmapManager.getInstance().createBitmap()
    }
    var isFlow: Boolean? = null
    private var mBeginLisenter = object : PageFlip.BeginListener {
        override fun beginNext() {
            if (mReaderView is HorizontalReaderView) {
                if (isFlow == false) {
                    mTextureView?.onDrawNextFrame(true)
                }
                isFlow = true
            }
        }

        override fun beginPre() {
            if (mReaderView is HorizontalReaderView) {
                if (isFlow == true) {
                    mTextureView?.onDrawNextFrame(false)
                }
                isFlow = false
            }
        }
    }

    private var mPageFlipStateListener = object : SinglePageRender.PageFlipStateListener {
        override fun gone() {
            if (mTextureView!!.isFangzhen) mTextureView!!.alpha = 0f
        }

        override fun backward(mPageNo: Int) {
            num = mPageNo
            (mReaderView as HorizontalReaderView).onClickLeft(false)

            if (mTextureView!!.isFangzhen) mTextureView!!.alpha = 0f
        }

        override fun forward(mPageNo: Int) {

            (mReaderView as HorizontalReaderView).onClickRight(false)
            num = mPageNo
            if (mTextureView!!.isFangzhen) mTextureView!!.alpha = 0f
        }

        override fun restore(mPageNo: Int) {
            num = mPageNo
            if (mTextureView!!.isFangzhen) mTextureView!!.alpha = 0f
        }
    }

    /**
     * 初始化GLSufaceView
     */
    private fun initGLSufaceView() {
        removeView(mTextureView)
        if (mTextureView == null) mTextureView = PageFlipView(context)
        mTextureView?.setZOrderOnTop(true)
        //加载Bitmap数据监听
        (mTextureView?.getmPageRender() as SinglePageRender).setListener(mLoadBitmaplistener)
        //翻页动画结束监听
        (mTextureView?.getmPageRender() as SinglePageRender).setPageFlipStateListenerListener(mPageFlipStateListener)
        mTextureView?.setBeginLisenter(mBeginLisenter)
        addView(mTextureView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        mTextureView?.alpha = 0f
        if (Build.VERSION.SDK_INT < 16) {
            (content as Activity).window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            mTextureView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
        mTextureView?.isFangzhen = when (animaEnums) {
            ReadViewEnums.Animation.curl -> true
            else -> false
        }
    }

    /**
     * 初始化ReaderViewFactory
     */
    fun initReaderViewFactory() = if (mReaderViewFactory == null) mReaderViewFactory = ReaderViewFactory(context) else Unit

    /**
     * 入口
     * @param mReadInfo 阅读信息
     */
    override fun entrance(mReadInfo: ReadInfo) {
        if (animaEnums != mReadInfo.animaEnums) { //如果阅读模式发生变化
            if (mReaderView != null) removeView(mReaderView as View)//移除
            mReaderView = mReaderViewFactory?.getView(mReadInfo.animaEnums)//创建
            if (mReaderView != null) addView(mReaderView as View)//添加
            animaEnums = mReadInfo.animaEnums//记录动画模式
            initGLSufaceView()
            mReaderView?.setHorizontalEventListener(this)
        }
        mReaderView?.entrance(mReadInfo)
    }

    /**
     * 设置时间
     * @param time 时间
     */
    override fun freshTime(time: CharSequence?) = mReaderView?.freshTime(time) ?: Unit


    /**
     * 设置电池
     * @param percent 电量
     */
    override fun freshBattery(percent: Float) = mReaderView?.freshBattery(percent) ?: Unit

    /**
     * 设置背景颜色
     */
    override fun setBackground() = mReaderView?.setBackground() ?: Unit


    /**
     * 设置阅读信息
     * @param mReadInfo 新阅读信息
     */
    override fun setReadInfo(mReadInfo: ReadInfo) = mReaderView?.setReadInfo(mReadInfo) ?: Unit

    /**
     * 设置 IReadView 实现 View 的变化监听
     * @param mReadPageChange 监听对象
     */
    override fun setIReadPageChange(mReadPageChange: IReadPageChange?) = mReaderView?.setIReadPageChange(mReadPageChange) ?: Unit

    /**
     * 返回章节
     * @param chapter 监听对象
     */
    override fun setLoadChapter(msg: Int, chapter: Chapter, chapterList: ArrayList<ArrayList<NovelLineBean>>?) =
            mReaderView?.setLoadChapter(msg, chapter, chapterList) ?: Unit

    /**
     * 返回广告
     * @param view 广告view
     */
    override fun setLoadAd(view: View?) = mReaderView?.setLoadAd(view) ?: Unit

    /**
     * 重画item页面
     */
    override fun onRedrawPage() = mReaderView?.onRedrawPage() ?: Unit

    /**
     * 跳章
     */
    override fun onJumpChapter(sequence: Int) = mReaderView?.onJumpChapter(sequence) ?: Unit

    override fun changeAnimMode(mode: Int) {
        ReadConfig.animation = when (mode) {
            0 -> ReadViewEnums.Animation.slide
            1 -> {
                mTextureView?.isFangzhen = true
                ReadViewEnums.Animation.curl
            }
            2 -> ReadViewEnums.Animation.shift
            else -> ReadViewEnums.Animation.list
        }
    }

    override fun onResume() {
        mTextureView?.visibility = View.VISIBLE
    }

    override fun onPause() {
        mTextureView?.visibility = View.GONE
    }

    override fun myDispatchTouchEvent(event: MotionEvent) {
        AppLog.e("event",event.action.toString())
        mGestureDetector.onTouchEvent(event)
    }

    private var mGestureDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {

        override fun onDown(e: MotionEvent): Boolean {

            mTextureView?.onFingerDown(e.x, e.y)
            //翻页显示
            AppLog.e("down",e.action.toString())
            if (mTextureView!!.isFangzhen) mTextureView!!.alpha = 1.0f

            //Menu隐藏
            if (context is ReadingActivity) (context as ReadingActivity).showMenu(false)
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            mTextureView?.onFingerMove(e2.x, e2.y)
            return true
        }

        override fun onLongPress(e: MotionEvent) = Unit

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = false

        override fun onShowPress(e: MotionEvent) = Unit

        override fun onSingleTapUp(e: MotionEvent): Boolean = false
    })
}