package com.intelligent.reader.read.page

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.MotionEvent
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.adapter.HorizontalAdapter
import com.intelligent.reader.read.help.*
import com.intelligent.reader.read.mode.ReadCursor
import com.intelligent.reader.read.mode.ReadInfo
import net.lzbook.kit.data.bean.ReadViewEnums
import com.intelligent.reader.view.ViewPager
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.ToastUtils
import java.util.*

/**
 * 水平滑动PageView容器
 * Created by wt on 2017/12/2.
 */
class HorizontalReaderView : ViewPager, IReadView, HorizontalPage.NoticePageListener {

    //记录上一次滑动x坐标
    private var beforeX: Float = 0.toFloat()
    //ViewPager是否能滑动 -1:都不能 0：都能 1 左 2 右
    var isCanScroll: Int = 0
    //禁止滑动方向 true:禁止左滑 false:禁止右滑
    var isLeftSlip: Boolean = true
    //宽、高、时间、电量、颜色
    private var w: Int = 0
    private var h: Int = 0
    private var percent: Float = 0f
    private var time: String = ""

    var mReadInfo: ReadInfo? = null
    //当前游标
    var curCursor:ReadCursor? = null
    //滑动方向
    private var direction = ReadViewEnums.Direction.leftToRight
    //当前坐标
    private var index: Int = Int.MAX_VALUE/2
    //滑动监听
    private var mListener: OnPageChangeListener = object : ViewPager.OnPageChangeListener {
        private var lastValue: Float = 0.toFloat()

        override fun onPageScrollStateChanged(state: Int)=Unit

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            if (positionOffset == 0.0f) return
            if (lastValue > positionOffset) {
                direction = ReadViewEnums.Direction.rightToLeft
            } else if (lastValue < positionOffset) {
                direction = ReadViewEnums.Direction.leftToRight
            }
            lastValue = positionOffset
        }

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
                    checkViewState("Next", ReadViewEnums.NotifyStateState.right)
                    checkChapterCache("Next")
                }
            }
            index = position
        }
    }

    //构造
    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        adapter = HorizontalAdapter(this)
        setCurrentItem(Int.MAX_VALUE / 2, false)
        addOnPageChangeListener(mListener)
    }

    /**
     * 检查ItemView状态
     * @param whichView
     * @param notify
     */
    fun checkViewState(whichView:String,notify: ReadViewEnums.NotifyStateState){
        //1、获取View
        val view = when(whichView) {
            "Pre"-> findViewWithTag(ReadViewEnums.PageIndex.previous)
            "Next" -> findViewWithTag(ReadViewEnums.PageIndex.next)
            else -> findViewWithTag(ReadViewEnums.PageIndex.current)
        }
        //2、判断View状态
        when((view as HorizontalPage).viewState){
            ReadViewEnums.ViewState.loading ->{
                //改变View的NotifyStateState，
                // success后，通知其他页updata
                isCanScroll = -1
                view.viewNotify = notify
                if (notify == ReadViewEnums.NotifyStateState.all){
                    view.setCursor(curCursor!!)
                }
            }
            ReadViewEnums.ViewState.success->{//
                //获取旧游标
                val mCousor = view.mCursor!!
                //章节顺序
                val newSequence: Int = getNewSequence(notify, mCousor)
                //offset
                val newOffset:Int = getNewOffset(notify, mCousor)
                //设置新游标
                val newCursor = ReadCursor(curCursor!!.curBook,newSequence,newOffset, ReadViewEnums.PageIndex.previous,mReadInfo!!.mReadStatus)
                (adapter as HorizontalAdapter).cursor = newCursor
            }
            ReadViewEnums.ViewState.error->{//

            }
            ReadViewEnums.ViewState.start->{//封面开始
                isCanScroll = 1
                isLeftSlip = false
            }
            ReadViewEnums.ViewState.end->{//结束
                isCanScroll = 1
                isLeftSlip = true
            }
        }
    }
    /**
     *  检查缓存
     */
    private fun checkChapterCache(whichOrientation: String) {
        val provider = DataProvider.getInstance()
        val keyList = provider.chapterMap.keys.toList().sorted()
        when(whichOrientation) {
            "Pre"-> {
                if (ReadConfig.sequence <= keyList.first()){
                    //保留前三
                    if(keyList.size>=3){
                        keyList.filter { it > keyList[2] }.forEach { provider.chapterMap.remove(it) }
                    }
                    //加载缓存
                    for (i in 0 ..  keyList.first() - ReadConfig.sequence){
                        DataProvider.getInstance().loadChapter(mReadInfo!!.curBook, ReadConfig.sequence+i, ReadViewEnums.PageIndex.previous, object : DataProvider.ReadDataListener() {
                            override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) = Unit
                            override fun loadDataError(message: String) = Unit
                        })
                    }
                }
            }
            "Next" -> {
                if (ReadConfig.sequence>=keyList.last()){
                    //保留后三
                    if(keyList.size>=3){
                        keyList.filter { it < keyList[keyList.size-3] }.forEach { provider.chapterMap.remove(it) }
                    }
                    for (i in 0 ..ReadConfig.sequence - keyList.first()){
                        DataProvider.getInstance().loadChapter(mReadInfo!!.curBook, ReadConfig.sequence+i, ReadViewEnums.PageIndex.previous, object : DataProvider.ReadDataListener() {
                            override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) = Unit
                            override fun loadDataError(message: String) = Unit
                        })
                    }
                }
            }
            else -> {}
        }
    }
    /**
     * 通知更所有页
     */
    private fun allViewUpdata(cursor: ReadCursor){
        preViewUpdata(cursor)
        nextViewUpdata(cursor)
    }
    /**
     * 通知更新下页
     */
    private fun nextViewUpdata(cursor: ReadCursor) {
        val nextView = findViewWithTag(ReadViewEnums.PageIndex.next)
        if (nextView != null) {
            var newNextSequence = cursor.sequence
            val newOffset = when (cursor.offset) {
                cursor.lastOffset -> {//如果当前页是最后页：加载下一章1页
                    newNextSequence++
                    0
                }
                else -> {//其他情况： +1页
                    cursor.nextOffset
                }
            }
            val nextCursor = ReadCursor(curCursor!!.curBook, newNextSequence, newOffset, ReadViewEnums.PageIndex.previous, mReadInfo!!.mReadStatus)
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
            var newPreSequence = cursor.sequence
            val newOffset =  when (cursor.offset) {
                0 -> {//如果当前页是1：加载上一章最后页
                    newPreSequence--
                    Int.MAX_VALUE
                }
                else -> {//其他情况： -1页
                    cursor.offset -1
                }
            }
            val preCursor = ReadCursor(curCursor!!.curBook, newPreSequence, newOffset, ReadViewEnums.PageIndex.previous, mReadInfo!!.mReadStatus)
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
                        mCousor.offset - 1
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
                        mCousor.sequence - 1
                    }
                    else -> {//其他情况： -1页
                        mCousor.sequence
                    }
                }
            }
            ReadViewEnums.NotifyStateState.right -> {
                when (mCousor.offset) {
                    mCousor.lastOffset -> {//如果当前页是最后页：加载下一章1页
                        mCousor.sequence + 1
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
    override fun pageChangSuccess(cursor: ReadCursor,notify: ReadViewEnums.NotifyStateState) {
        when(notify){
            ReadViewEnums.NotifyStateState.all->{
                allViewUpdata(cursor)
            }
            ReadViewEnums.NotifyStateState.left->{
                preViewUpdata(cursor)
            }
            ReadViewEnums.NotifyStateState.right->{
                nextViewUpdata(cursor)
            }
            ReadViewEnums.NotifyStateState.none->{
                isCanScroll = 0
            }
        }
    }

    /**
     * 点击屏幕左边后翻页
     */
    override fun onClickLeft() {
        //当前页是封面页禁止点击
        if ((findViewWithTag(ReadViewEnums.PageIndex.current) != null) and ((findViewWithTag(ReadViewEnums.PageIndex.current) as HorizontalPage).viewState == ReadViewEnums.ViewState.start)) return
        checkViewState("Pre", ReadViewEnums.NotifyStateState.left)
        setCurrentItem(index-1,true)
    }

    /**
     * 点击屏幕右边后翻页
     */
    override fun onClickRight() {
        //当前页是最后一页禁止点击
        if ((findViewWithTag(ReadViewEnums.PageIndex.current) != null) and ((findViewWithTag(ReadViewEnums.PageIndex.current) as HorizontalPage).viewState == ReadViewEnums.ViewState.end)) return
        checkViewState("Next", ReadViewEnums.NotifyStateState.right)
        setCurrentItem(index+1,true)
    }

    /**
     * 点击屏幕中间区域显示菜单
     */
    override fun onClickMenu(isShow: Boolean) {
        mReadPageChange?.showMenu(isShow)
    }

    /**
     * 点击原网页
     */
    override fun loadOrigin() {
        ToastUtils.showToastNoRepeat("loadOrigin")
    }
    /**
     * 点击源码声明
     */
    override fun loadTransCoding() {
        ToastUtils.showToastNoRepeat("loadTransCoding")
    }

    override fun getCurPercent(): Float = percent

    override fun getCurTime(): String = time

    override fun currentViewSuccess() {
        val curView = findViewWithTag(ReadViewEnums.PageIndex.current)
        curView as HorizontalPage
        if (curView.mCursor!=null){
            ReadConfig.sequence = curView.mCursor!!.sequence
            ReadConfig.offset = curView.mCursor!!.offset + curView.CursorOffset
        }
    }
//==================================================IReadPageChange=========================================
    private var mReadPageChange: IReadPageChange? = null

    override fun setIReadPageChange(mReadPageChange: IReadPageChange) {
        this.mReadPageChange = mReadPageChange
    }

//==================================================IReadView重写方法=========================================

    //入口
    override fun entrance(mReadInfo: ReadInfo) {
        this.mReadInfo = mReadInfo
        val sequence = mReadInfo.mReadStatus.sequence
        val offset = mReadInfo.mReadStatus.offset
        Handler().postDelayed({
            //更改当前view状态
            (findViewWithTag(ReadViewEnums.PageIndex.current) as HorizontalPage).viewState = ReadViewEnums.ViewState.loading
            if(this.mReadInfo!!.mReadStatus.sequence == -1){
                this.mReadInfo!!.mReadStatus.sequence =0
                curCursor = ReadCursor(mReadInfo.curBook,sequence,offset, ReadViewEnums.PageIndex.current,mReadInfo.mReadStatus)
                checkViewState("Cur", ReadViewEnums.NotifyStateState.all)

            }else{
                curCursor = ReadCursor(mReadInfo.curBook,sequence,offset, ReadViewEnums.PageIndex.current,mReadInfo.mReadStatus)
                checkViewState("Cur", ReadViewEnums.NotifyStateState.all)
            }
        },200)
        //设置字体颜色
        mReadPageChange?.onLoadChapter(ReadViewEnums.MsgType.MSG_LOAD_CUR_CHAPTER, mReadInfo.mReadStatus.currentPage - 1, false, ReadViewEnums.PageIndex.current)
    }

    //布局发生变化
    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)
        w = width
        h = height
    }

    //时间
    override fun freshTime(time: CharSequence?) {
        this.time = time.toString()
        for (i in 0 until childCount){
            val childAtView = getChildAt(i) as HorizontalPage
            childAtView.setTimes(this.time)
        }
    }

    //电量
    override fun freshBattery(percent: Float) {
        this.percent = percent
        for (i in 0 until childCount){
            val childAtView = getChildAt(i) as HorizontalPage
            childAtView.setBattery(this.percent)
        }
    }

    //设置背景颜色
    override fun setBackground() {
        for (i in 0 until childCount){
            val childAtView = getChildAt(i) as HorizontalPage
            childAtView.setBackGroud()
        }
    }

    //设置阅读信息
    override fun setReadInfo(mReadInfo: ReadInfo?) {

    }

    //章节回调
    override fun setLoadChapter(msg: Int, chapter: Chapter, chapterList: ArrayList<ArrayList<NovelLineBean>>?) {
        AppLog.w("chapter", "" + (chapterList?.size ?: -1))
    }

    //广告回调
    override fun setLoadAd(view: View?) {

    }
    //重画
    override fun onRedrawPage() {
        val curView = findViewWithTag(ReadViewEnums.PageIndex.current) as HorizontalPage
        curView.viewNotify = ReadViewEnums.NotifyStateState.all
        curView.onReSeparate()
        curView.setCursor(curView.mCursor!!)
    }
    //跳章
    override fun onJumpChapter(sequence: Int) {
        this.mReadInfo!!.mReadStatus.sequence = sequence
        entrance(this.mReadInfo!!)
    }
    //==================================================TouchEvent=========================================
    //-----禁止左滑-------左滑：上一次坐标 > 当前坐标
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return when(isCanScroll){
            -1-> true
            0 -> super.dispatchTouchEvent(event)
            else -> prohibitionOfSlidingTouchEvent(event)
        }
    }

    /**
     * 禁止单方向滑动事件
     */
    private fun prohibitionOfSlidingTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN//按下
            -> {
                beforeX = ev.x
                if (isLeftSlip) mReadPageChange?.goToBookOver()//跳bookend
            }
            MotionEvent.ACTION_MOVE -> {//移动
                val motionValue = ev.x - beforeX
                if (isLeftSlip) {
                    if (motionValue < 0) {//禁止左滑
                        return true
                    }
                } else {
                    if (motionValue > 0) {//禁止右滑动
                        return true
                    }
                }
                beforeX = ev.x//手指移动时，再把当前的坐标作为下一次的‘上次坐标’，解决上述问题
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}