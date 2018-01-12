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
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.runOnMain



/**
 * 阅读容器
 * Created by wt on 2017/12/13.
 */
class ReaderViewWidget : FrameLayout, IReadWidget, HorizontalEvent {

    companion object {
        val tag = "PageFlipView"
    }

    private var mReaderViewFactory: ReaderViewFactory? = null

    private var mReaderView: IReadView? = null

    private var mTextureView: PageFlipView? = null

    private var animaEnums: ReadViewEnums.Animation? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var mLoadBitmaplistener = SinglePageRender.LoadBitmapListener {
        index ->
        if ((mTextureView != null) and (mTextureView!!.isFangzhen) and (mReaderView is HorizontalReaderView)) {

            var bitmap: Bitmap? = null
            var isFinishCache = false

            runOnMain {
                println("${ReaderViewWidget.tag} load ${index.name}")
                val view = (mReaderView as HorizontalReaderView).findViewWithTag(index) as HorizontalPage
//                view.destroyDrawingCache()
                bitmap = view.drawingCache

                synchronized((this@ReaderViewWidget as Object)) {
                    println("this@ReaderViewWidget as Object).notify")
                    (this@ReaderViewWidget as Object).notify()
                }
                isFinishCache = true
            }


            synchronized((this@ReaderViewWidget as Object)) {
                if (!isFinishCache) {
                    try {
                        println("this@ReaderViewWidget as Object).wait ${Thread.currentThread().name}")
                        (this@ReaderViewWidget as Object).wait()
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
            AppLog.e(ReaderViewWidget.tag, "gone")
//            if (mTextureView!!.isFangzhen) mTextureView!!.visibility = View.INVISIBLE
        }

        private fun invisibelSurface(){
            //等待ViewPager切换完页面再隐藏
            runOnMain {
                var curView = (mReaderView as HorizontalReaderView).findViewWithTag(ReadViewEnums.PageIndex.current) as HorizontalPage
                if (curView.mNovelPageBean!!.isAd) {
                    if(mTextureView!!.visibility == View.VISIBLE && mTextureView!!.surfaceAviable) {
                        mTextureView!!.visibility = View.INVISIBLE
                    }
                }else {
                    if(mTextureView!!.visibility == View.INVISIBLE && mTextureView!!.surfaceAviable) {
                        mTextureView!!.visibility = View.VISIBLE
                    }
                }
            }
        }

        override fun backward() {
            AppLog.e(ReaderViewWidget.tag, "backward")
            (mReaderView as HorizontalReaderView).onClickLeft(false)

            invisibelSurface()
        }

        override fun forward() {
            AppLog.e(ReaderViewWidget.tag, "forward")
            (mReaderView as HorizontalReaderView).onClickRight(false)
            invisibelSurface()
        }

        override fun restore() {
            AppLog.e(ReaderViewWidget.tag, "restore")
            invisibelSurface()
        }
    }

    /**
     * 初始化GLSufaceView
     */
    private fun initGLSufaceView() {
        removeView(mTextureView)
        if (mTextureView == null) {
            mTextureView = PageFlipView(context)

            mTextureView!!.visibility = View.INVISIBLE
        }
        //加载Bitmap数据监听
        (mTextureView?.getmPageRender() as SinglePageRender).setListener(mLoadBitmaplistener)
        //翻页动画结束监听
        (mTextureView?.getmPageRender() as SinglePageRender).setPageFlipStateListenerListener(mPageFlipStateListener)
        mTextureView?.setBeginLisenter(mBeginLisenter)
        addView(mTextureView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
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
            (mReaderView as View).isClickable = false
            if (mReaderView != null) addView(mReaderView as View)//添加
            animaEnums = mReadInfo.animaEnums//记录动画模式
            mReaderView?.setHorizontalEventListener(this)

            initGLSufaceView()

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


        mReaderView?.onAnimationChange(ReadConfig.animation)
    }

    override fun onResume() {
//        if(animaEnums == ReadViewEnums.Animation.curl) {
//            mTextureView?.visibility = View.VISIBLE
//        }
    }

    override fun onPause() {
//        if(animaEnums == ReadViewEnums.Animation.curl) {
//            mTextureView?.visibility = View.GONE
//        }
    }

    private var isDownActioned = false

    override fun myDispatchTouchEvent(event: MotionEvent):Boolean {
//        AppLog.e("event",event.action.toString())
        if (event.actionMasked == MotionEvent.ACTION_UP  || event.actionMasked == MotionEvent.ACTION_CANCEL) {
//            mTextureView!!.visibility = View.INVISIBLE
            mTextureView?.post {
                mTextureView?.onFingerUp(event.x, event.y)
            }

//            if (mTextureView!!.visibility != View.VISIBLE){
//                //翻页显示
//                mTextureView!!.visibility = View.VISIBLE
//            }
            isDownActioned = false
        }
        return mGestureDetector.onTouchEvent(event)
    }

    private var mGestureDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {

        override fun onDown(e: MotionEvent): Boolean {
            AppLog.e(ReaderViewWidget.tag, "mGestureDetector onDown")
            //Menu隐藏
            if (context is ReadingActivity) (context as ReadingActivity).showMenu(false)


            if (mTextureView!!.visibility != View.VISIBLE && !mTextureView!!.surfaceAviable){

                //翻页显示
                mTextureView!!.visibility = View.VISIBLE

            }
            mTextureView!!.post{
                mTextureView?.onFingerDown(e.x, e.y)
            }
            isDownActioned = true
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if(isDownActioned) {
                mTextureView?.post {
                    mTextureView?.onFingerMove(e2.x, e2.y)
                }
            }else{
                onDown(e2)
            }

//            if (mTextureView!!.visibility != View.VISIBLE){
//                //翻页显示
//                mTextureView!!.visibility = View.VISIBLE
//            }
            return true
        }

        override fun onLongPress(e: MotionEvent) = Unit

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = false

        override fun onShowPress(e: MotionEvent) = Unit

        override fun onSingleTapUp(e: MotionEvent): Boolean = false
    })
}