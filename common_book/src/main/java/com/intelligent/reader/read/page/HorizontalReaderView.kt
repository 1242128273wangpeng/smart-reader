package com.intelligent.reader.read.page

import android.content.Context
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.*
import com.intelligent.reader.R
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.adapter.HorizontalAdapter
import com.intelligent.reader.read.animation.ShiftTransformer
import com.intelligent.reader.read.animation.SlideTransformer
import com.intelligent.reader.read.help.HorizontalEvent
import com.intelligent.reader.read.help.IReadPageChange
import com.intelligent.reader.read.help.IReadView
import com.intelligent.reader.read.mode.ReadCursor
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.view.ViewPager
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_custom_dialog.view.*
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.data.bean.ReadViewEnums
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.ToastUtils

/**
 * 水平滑动PageView容器
 * Created by wt on 2017/12/2.
 */
class HorizontalReaderView : ViewPager, IReadView, HorizontalPage.NoticePageListener {

    private val SHADOW_WIDTH = 30

    //记录上一次滑动x坐标
    private var beforeX: Float = 0.toFloat()
    private var shouldGiveUpAction: Boolean = false

    private var disallowIntercept = false
    private val touchSlop by lazy {
        ViewConfiguration.get(context).scaledTouchSlop
    }
    //当前游标
    var curCursor: ReadCursor? = null
    //当前坐标
    private var index: Int = Int.MAX_VALUE.div(2)
    private var curViewState: ReadViewEnums.ViewState = ReadViewEnums.ViewState.loading

    //滑动监听
    private var mListener: OnPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {

        override fun onPageSelected(position: Int) {
            //判断方向
            when {
            //1、向上翻页/向下翻页
            //2、获取游标
            //3、改变adapter游标
                index > position -> {
                    checkViewState("Pre", ReadViewEnums.NotifyStateState.left)
                    checkChapterCache("Pre")
                }
                index < position -> {
                    //log埋点
                    if (ReadState.sequence >= 0) mReadPageChange?.addLog()
                    checkViewState("Next", ReadViewEnums.NotifyStateState.right)
                    checkChapterCache("Next")
                }
            }
            index = position


            isClickable = true
        }
    }

    //构造
    constructor(context: Context) : this(context, null)


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        if (ReadConfig.animation == ReadViewEnums.Animation.slide) {
            setShadowDrawable(R.drawable.page_shadow)
            setShadowWidth(SHADOW_WIDTH)
            setPageTransformer(true, ShiftTransformer())
        } else {
            setPageTransformer(true, SlideTransformer())
        }


        adapter = HorizontalAdapter(this)
        setCurrentItem(Int.MAX_VALUE.div(2), false)
        addOnPageChangeListener(mListener)
    }

    /**
     * 检查ItemView状态
     * @param whichView
     * @param notify
     */
    fun checkViewState(whichView: String, notify: ReadViewEnums.NotifyStateState) {
        //1、获取View
        val view = when (whichView) {
            "Pre" -> findViewWithTag(ReadViewEnums.PageIndex.previous)
            "Next" -> findViewWithTag(ReadViewEnums.PageIndex.next)
            else -> findViewWithTag(ReadViewEnums.PageIndex.current)
        }
        //2、判断View状态
        when ((view as HorizontalPage).viewState) {
            ReadViewEnums.ViewState.loading, ReadViewEnums.ViewState.other -> {
                //改变View的NotifyStateState，
                // success后，通知其他页updata
                view.viewNotify = notify
                if (notify == ReadViewEnums.NotifyStateState.all) {
                    view.setCursor(curCursor!!)
                }
            }
            ReadViewEnums.ViewState.success -> {//
                //获取旧游标
                val mCousor = view.mCursor!!
                //章节顺序
                val newSequence: Int = getNewSequence(notify, mCousor)
                //offset
                val newOffset: Int = getNewOffset(notify, mCousor)
                //设置新游标
                val newCursor = ReadCursor(curCursor!!.curBook, newSequence, newOffset, ReadViewEnums.PageIndex.previous)
                (adapter as HorizontalAdapter).cursor = newCursor
            }
        }
    }

    /**
     *  检查缓存
     */
    private fun checkChapterCache(whichOrientation: String) {
        val threadObserve = Observable.create<Int>({
            it.onNext(1)
            it.onComplete()
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()).subscribe({
            val provider = DataProvider.getInstance()
            var sequence: Int = ReadState.sequence

            if ("Pre" == whichOrientation) {
                sequence -= 1
                if (sequence > -1) {
                    provider.loadChapter(ReadState.book, sequence, ReadViewEnums.PageIndex.current, object : DataProvider.ReadDataListener() {
                        override fun loadDataSuccess(c: Chapter?, type: ReadViewEnums.PageIndex) = Unit
                        override fun loadDataError(message: String) = Unit
                        override fun loadDataInvalid(message: String) {
                            ToastUtils.showToastNoRepeat(message)
                            if (context is ReadingActivity) {
                                (context as ReadingActivity).showChangeSourceDialog()
                            }
                        }
                    })
                }
            } else {
                sequence += 1
                if (sequence < ReadState.chapterCount) {
                    provider.loadChapter(ReadState.book, sequence, ReadViewEnums.PageIndex.current, object : DataProvider.ReadDataListener() {
                        override fun loadDataSuccess(c: Chapter?, type: ReadViewEnums.PageIndex) = Unit
                        override fun loadDataError(message: String) = Unit
                        override fun loadDataInvalid(message: String) {
                            ToastUtils.showToastNoRepeat(message)
                            if (context is ReadingActivity) {
                                (context as ReadingActivity).showChangeSourceDialog()
                            }
                        }
                    })
                }

                if (ReadState.pageCount == ReadState.currentPage) {
                    provider.preLoad(sequence + 1, sequence + 6)
                }

            }
        })
        DataProvider.getInstance().addDisposable(threadObserve)
    }

    /**
     * 通知更所有页
     */
    private fun allViewUpdata(cursor: ReadCursor) {
        preViewUpdata(cursor)
        nextViewUpdata(cursor)
    }

    /**
     * 通知更新下页
     */
    private fun nextViewUpdata(cursor: ReadCursor) {
        val nextView = findViewWithTag(ReadViewEnums.PageIndex.next)
        if (nextView != null) {
            val newNextSequence: Int
            val newOffset = when (cursor.offset) {
                cursor.lastOffset -> {//如果当前页是最后页：加载下一章1页
                    newNextSequence = cursor.sequence.plus(1)
                    0
                }
                else -> {//其他情况： +1页
                    newNextSequence = cursor.sequence
                    cursor.nextOffset
                }
            }
            val nextCursor = ReadCursor(curCursor!!.curBook, newNextSequence, newOffset, ReadViewEnums.PageIndex.next)
            (nextView as HorizontalPage).viewNotify = ReadViewEnums.NotifyStateState.none
            nextView.setCursor(nextCursor)
        }
    }

    /**
     * 通知更新前页
     */
    private fun preViewUpdata(cursor: ReadCursor) {
        val preView = findViewWithTag(ReadViewEnums.PageIndex.previous)
        if (preView != null) {
            val newPreSequence: Int
            val newOffset = when (cursor.offset) {
                0 -> {//如果当前页是1：加载上一章最后页
                    newPreSequence = cursor.sequence.minus(1)
                    Int.MAX_VALUE
                }
                else -> {//其他情况： -1页
                    newPreSequence = cursor.sequence
                    cursor.offset.minus(1)
                }
            }
            val preCursor = ReadCursor(curCursor!!.curBook, newPreSequence, newOffset, ReadViewEnums.PageIndex.previous)
            (preView as HorizontalPage).viewNotify = ReadViewEnums.NotifyStateState.none
            preView.setCursor(preCursor)
        }
    }


    private fun getNewOffset(notify: ReadViewEnums.NotifyStateState, mCousor: ReadCursor): Int {
        return when (notify) {
            ReadViewEnums.NotifyStateState.left -> {
                when (mCousor.offset) {
                    0 -> {//如果当前页是1：加载上一章最后页
                        Int.MAX_VALUE
                    }
                    else -> {//其他情况： -1页
                        mCousor.offset.minus(1)
                    }
                }
            }
            ReadViewEnums.NotifyStateState.right -> {
                when (mCousor.offset) {
                    mCousor.lastOffset -> {//如果当前页是最后页：加载下一章1页
                        0
                    }
                    else -> {//其他情况： +1页
                        mCousor.nextOffset
                    }
                }
            }
            else -> 1
        }
    }

    private fun getNewSequence(notify: ReadViewEnums.NotifyStateState, mCousor: ReadCursor): Int {
        return when (notify) {
            ReadViewEnums.NotifyStateState.left -> {
                when (mCousor.offset) {
                    0 -> {//如果当前页是1：加载上一章最后页
                        mCousor.sequence.minus(1)
                    }
                    else -> {//其他情况： -1页
                        mCousor.sequence
                    }
                }
            }
            ReadViewEnums.NotifyStateState.right -> {
                when (mCousor.offset) {
                    mCousor.lastOffset -> {//如果当前页是最后页：加载下一章1页
                        mCousor.sequence.plus(1)
                    }
                    else -> {//其他情况： +1页
                        mCousor.sequence
                    }
                }
            }
            else -> 0
        }
    }
//================================================NoticePageListener================================
    /**
     * 通知其他页更新数据
     */
    override fun pageChangSuccess(cursor: ReadCursor, notify: ReadViewEnums.NotifyStateState) {
        when (notify) {
            ReadViewEnums.NotifyStateState.all -> {
                allViewUpdata(cursor)
            }
            ReadViewEnums.NotifyStateState.left -> {
                preViewUpdata(cursor)
            }
            ReadViewEnums.NotifyStateState.right -> {
                nextViewUpdata(cursor)
            }
            ReadViewEnums.NotifyStateState.none -> {
                val viewState = (findViewWithTag(ReadViewEnums.PageIndex.current) as HorizontalPage).viewState
                if (viewState == ReadViewEnums.ViewState.start) {
                    if (viewState.Tag == 1) {
                        viewState.Tag = -1
                        nextViewUpdata(cursor)
                        AppLog.e("none", "start")
                    }
                }
            }
        }
    }

    /**
     * 点击屏幕左边前翻页
     */
    override fun onClickLeft(smoothScroll: Boolean) {
        //当前页是封面页禁止点击
        if ((findViewWithTag(ReadViewEnums.PageIndex.current) != null) and ((findViewWithTag(ReadViewEnums.PageIndex.current) as HorizontalPage).viewState == ReadViewEnums.ViewState.start)) return
        checkViewState("Pre", ReadViewEnums.NotifyStateState.left)
        setCurrentItem(index.minus(1), smoothScroll)
    }

    /**
     * 点击屏幕右边后翻页
     */
    override fun onClickRight(smoothScroll: Boolean) {
        //当前页是最后一页禁止点击
        if ((findViewWithTag(ReadViewEnums.PageIndex.current) != null)
                and ((findViewWithTag(ReadViewEnums.PageIndex.current) as HorizontalPage).viewState == ReadViewEnums.ViewState.end)) {
            mReadPageChange?.goToBookOver()
            return
        }
        checkViewState("Next", ReadViewEnums.NotifyStateState.right)
        setCurrentItem(index.plus(1), smoothScroll)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (event.action == KeyEvent.ACTION_UP) {
                    onClickLeft(true)
                }
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event.action == KeyEvent.ACTION_UP) {
                    onClickRight(true)
                }
                return true
            }
        }
        return false
    }

    /**
     * 点击屏幕中间区域显示菜单
     */
    override fun onClickMenu(isShow: Boolean) = mReadPageChange?.showMenu(isShow) ?: Unit

    /**
     * 点击原网页
     */
    override fun loadOrigin() = mReadPageChange?.onOriginClick() ?: Unit

    /**
     * 点击源码声明
     */
    override fun loadTransCoding() = mReadPageChange?.onTransCodingClick() ?: Unit

    override fun currentViewSuccess() {
        val curView = findViewWithTag(ReadViewEnums.PageIndex.current)
        curView as HorizontalPage
        val mCursor = curView.mCursor
        curViewState = curView.viewState
        if (mCursor != null) {
            ReadState.sequence = mCursor.sequence
//            ReadState.offset = mCursor.offset.plus(curView.mCursorOffset)
            ReadState.offset = mCursor.offset
            ReadState.currentPage = curView.pageIndex
            ReadState.pageCount = curView.pageSum
            ReadState.contentLength = curView.contentLength
        }
    }

    //跳章
    override fun onJumpChapter() {
        entrance()
    }

    //==================================================IReadPageChange=========================================
    private var mReadPageChange: IReadPageChange? = null

    override fun setIReadPageChange(mReadPageChange: IReadPageChange) {
        this.mReadPageChange = mReadPageChange
    }

//==================================================IReadView重写方法=========================================

    //入口
    override fun entrance() {
        ReadState.book.let {
            val sequence = ReadState.sequence
            val offset = ReadState.offset
            DataProvider.getInstance().clear()
            DataProvider.getInstance().preLoad(sequence, sequence + 6)

            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    //更改当前view状态
                    var curView: View?

                    curView = findViewWithTag(ReadViewEnums.PageIndex.current)

                    if (curView != null) {

                        viewTreeObserver.removeOnPreDrawListener(this)

                        (curView as HorizontalPage).viewState = ReadViewEnums.ViewState.loading
                        curCursor = ReadCursor(it, sequence, offset, ReadViewEnums.PageIndex.current)
                        checkViewState("Cur", ReadViewEnums.NotifyStateState.all)
                    }
                    return true
                }
            })

        }
    }

    override fun onAnimationChange(animation: ReadViewEnums.Animation) {
        if (ReadConfig.animation == ReadViewEnums.Animation.slide) {
            setShadowDrawable(R.drawable.page_shadow)
            setShadowWidth(SHADOW_WIDTH)
            setPageTransformer(true, ShiftTransformer())
        } else {
            setShadowDrawable(null)
            setPageTransformer(true, SlideTransformer())
        }
    }

    //==================================================TouchEvent=========================================

    private var mHorizontalEvent: HorizontalEvent? = null

    override fun setHorizontalEventListener(mHorizontalEvent: HorizontalEvent?) {
        this.mHorizontalEvent = mHorizontalEvent
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
        this.disallowIntercept = disallowIntercept
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return false
    }

    //-----禁止左滑-------左滑：上一次坐标 > 当前坐标
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (ReadState.isMenuShow) {
            mReadPageChange?.showMenu(false)
        }

        return if (ReadConfig.animation == ReadViewEnums.Animation.curl) {
            mHorizontalEvent!!.myDispatchTouchEvent(event)
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {

        if (!disallowIntercept && ReadConfig.animation == ReadViewEnums.Animation.curl
                && MotionEvent.ACTION_MOVE == ev?.actionMasked) {
            //仿真是要先判断出滑动方向的，防止ViewPager先滑动touchSlop长度
            return touchSlop <= Math.abs(ev.x - mLastMotionX)
        }
//        if(!mScroller.isFinished()){
//            mScroller.abortAnimation()
//            computeScroll()
//            post {
//                ViewCompat.postInvalidateOnAnimation(this)
//            }
//            return false
//        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return if (ReadConfig.animation == ReadViewEnums.Animation.curl) {
            super.dispatchTouchEvent(event)
        } else {
            prohibitionOfSlidingTouchEvent(event)
        }
    }

    /**
     * 禁止单方向滑动事件
     */
    private fun prohibitionOfSlidingTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN//按下
            -> {
                shouldGiveUpAction = false
                beforeX = ev.x
                return super.dispatchTouchEvent(ev)
            }
            MotionEvent.ACTION_MOVE -> {//移动
                val motionValue = ev.x - beforeX
                if (!disallowIntercept && motionValue < -touchSlop && (findViewWithTag(ReadViewEnums.PageIndex.current) as HorizontalPage).orientationLimit == ReadViewEnums.ScrollLimitOrientation.RIGHT) {
                    mReadPageChange?.goToBookOver()//跳bookend
                    shouldGiveUpAction = true
                    return false
                } else {
                    return super.dispatchTouchEvent(ev)
                }
            }
        }

        if (shouldGiveUpAction) {
            return false
        }
        return super.dispatchTouchEvent(ev)
    }
}