package com.intelligent.reader.read.page

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import com.intelligent.reader.util.StatServiceUtils
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.data.bean.ReadViewEnums
import net.lzbook.kit.utils.AppLog
import java.util.*

/**
 * 阅读容器
 * Created by wt on 2017/12/13.
 */
class ReaderViewWidget : FrameLayout, IReadWidget {

    private var mReaderViewFactory: ReaderViewFactory? = null

    private var mReaderView: IReadView? = null

    private var mTextureView: PageFlipView? = null

    private var animaEnums: ReadViewEnums.Animation? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var num = Integer.MAX_VALUE / 2 - 1

    private var mLoadBitmaplistener = SinglePageRender.LoadBitmapListener {
        if ((mTextureView != null) and (mTextureView!!.isFangzhen) and (mReaderView is HorizontalReaderView)) {
            when {
                num <  it-> {
                    num = it
                    return@LoadBitmapListener (mReaderView as HorizontalReaderView).findViewWithTag(ReadViewEnums.PageIndex.next).drawingCache
                }
                num > it -> {
                    num = it
                    return@LoadBitmapListener (mReaderView as HorizontalReaderView).findViewWithTag(ReadViewEnums.PageIndex.previous).drawingCache
                }
            }
        }
        BitmapManager.getInstance().createBitmap()
    }
    var isFlow:Boolean? = null
    private var mBeginLisenter = object : PageFlip.BeginListener {
        override fun beginNext(){
            if (mReaderView is HorizontalReaderView){
                if (isFlow == false){
                    mTextureView?.onDrawNextFrame(true)
                }else {
                    (mReaderView as HorizontalReaderView).onClickRight(false)
                }
                isFlow = true
            }
        }
        override fun beginPre(){
            if (mReaderView is HorizontalReaderView){
                if (isFlow == true){
                    mTextureView?.onDrawNextFrame(false)
                }else {
                    (mReaderView as HorizontalReaderView).onClickLeft(false)
                }
                isFlow = false
            }
        }
    }

    private var mPageFlipStateListener = object : SinglePageRender.PageFlipStateListener {
        override fun gone(){
            if (mTextureView!!.isFangzhen) mTextureView!!.alpha = 0f
        }
        override fun backward(mPageNo: Int){
            if (mTextureView!!.isFangzhen) mTextureView!!.alpha = 0f
        }
        override fun forward(mPageNo: Int){
            if(num < mPageNo)  {//重置
                num = mPageNo
                (mReaderView as HorizontalReaderView).onClickRight(false)
            }
            if (mTextureView!!.isFangzhen) mTextureView!!.alpha = 0f
        }
        override fun restore(mPageNo: Int){
            (mReaderView as HorizontalReaderView).onClickLeft(false)
            if (mTextureView!!.isFangzhen) mTextureView!!.alpha = 0f
        }
    }

    /**
     * 初始化GLSufaceView
     */
    private fun initGLSufaceView() {
        removeView(mTextureView)
        if (mTextureView == null) mTextureView = PageFlipView(context)
        mTextureView?.isOpaque = false
        mTextureView?.alpha = 0f
        mTextureView?.isFangzhen = when (animaEnums) {
            ReadViewEnums.Animation.curl -> true
            else -> false
        }
        //加载Bitmap数据监听
        (mTextureView?.getmPageRender() as SinglePageRender).setListener(mLoadBitmaplistener)
        //翻页动画结束监听
        (mTextureView?.getmPageRender() as SinglePageRender).setPageFlipStateListenerListener(mPageFlipStateListener)
        mTextureView?.setBeginLisenter(mBeginLisenter)
        addView(mTextureView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        mTextureView?.createGLThread()
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
        mTextureView?.createGLThread()
        mTextureView?.onResume()
    }

    override fun onPause() = mTextureView?.onPause() ?: Unit

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return when (mTextureView?.isFangzhen ?: false) {
            true -> {
                val x = event.x.toInt()
                val y = event.y.toInt()
                val h4 = height / 4
                val w3 = width / 3
                if ((event.action == MotionEvent.ACTION_DOWN) and !((x <= w3) or (x >= width - w3) or (y >= height - h4 && x >= w3))) {
                    //Menu
                    super.dispatchTouchEvent(event)
                }else {
                    //仿真+
                    if (event.action == MotionEvent.ACTION_UP) {
                        mTextureView?.onFingerUp(event.x, event.y)
                        true
                    } else {
                        mGestureDetector.onTouchEvent(event)
                    }
                }
            }
            false -> super.dispatchTouchEvent(event)
        }
    }
    private var mGestureDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
        override fun onDown(e: MotionEvent): Boolean {
            mTextureView?.onFingerDown(e.x, e.y)
            AppLog.e("mGestureDetector",e.action.toString())
            //翻页显示
            if (mTextureView!!.isFangzhen) mTextureView!!.alpha = 1f
            //Menu隐藏
            if (context is ReadingActivity) (context as ReadingActivity).showMenu(false)
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            mTextureView?.onFingerMove(e2.x, e2.y)
            return true
        }

        override fun onLongPress(e: MotionEvent) = Unit

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = false

        override fun onShowPress(e: MotionEvent) = Unit

        override fun onSingleTapUp(e: MotionEvent): Boolean = false
    })
}