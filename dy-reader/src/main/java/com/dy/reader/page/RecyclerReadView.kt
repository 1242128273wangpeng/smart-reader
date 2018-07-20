package com.dy.reader.page

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI.activity
import com.ding.basic.bean.Chapter
import com.dingyue.contract.util.showToastMessage
import com.dy.reader.R
import com.dy.reader.adapter.PagerScrollAdapter
import com.dy.reader.data.DataProvider
import com.dy.reader.event.EventLoading
import com.dy.reader.event.EventReaderConfig
import com.dy.reader.event.EventSetting
import com.dy.reader.helper.AppHelper
import com.dy.reader.helper.ReadSeparateHelper
import com.dy.reader.mode.NovelLineBean
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import com.dy.reader.util.ThemeUtil
import com.intelligent.reader.read.mode.NovelPageBean
import iyouqu.theme.FrameActivity
import kotlinx.android.synthetic.main.reader_loading.view.*
import kotlinx.android.synthetic.main.reader_vertical_pager.view.*
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.ReadViewEnums
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class RecyclerReadView @JvmOverloads constructor(context: Context?, attrs: AttributeSet?) : FrameLayout(context, attrs), PagerScrollAdapter.OnLoadViewClickListener {

    private var mAdapter: PagerScrollAdapter

    private var mLayoutManager: WrapContentLinearLayoutManager

    private var mOriginDataList: CopyOnWriteArrayList<NovelPageBean>

    private val CHAPTER_WAITING: Int = 0

    private val CHAPTER_LOADING: Int = 1

    /**
     * 数据加载状态
     */
    private var mChapterLoadStat = CHAPTER_WAITING

    private var mCanScrollVertically = true

    private var mFirstRead = false

    private var mStartTouchX: Float = 0f

    private var mStartTouchY: Float = 0f

    private var mLastY: Float = 0f

    /**
     * 当前可见视图位置
     */
    private var mLastVisiblePosition = -1

    /**
     * 当前可见第一视图位置
     */
    private var mFirstVisiblePosition = -1

    /**
     * 上翻页章节阅读比例
     */
    private val PRE_LOAD_CHAPTER_SCROLL_SCALE = 0.3

    /**
     * 下翻页章节阅读比例
     */
    private val NEXT_LOAD_CHAPTER_SCROLL_SCALE = 0.6

    private var mGestureDetector: GestureDetector

    private var mIsJumpChapter = false

    init {
        LayoutInflater.from(context).inflate(R.layout.reader_vertical_pager, this)
        mLayoutManager = WrapContentLinearLayoutManager(context)
        recl_reader_content.layoutManager = mLayoutManager
        mOriginDataList = CopyOnWriteArrayList()

        mAdapter = PagerScrollAdapter(context!!)

//        loading_error_reload.setOnClickListener {
//            entrance()
//        }

        recl_reader_content.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager

                val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
                if(firstVisibleItemPosition != mFirstVisiblePosition){
                    mFirstVisiblePosition = firstVisibleItemPosition
                    setCurrentChapterInfo(firstVisibleItemPosition)
                }

                if (mLastVisiblePosition != linearLayoutManager.findLastVisibleItemPosition()) {
                    mLastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition()

                    if (mLastVisiblePosition < (mOriginDataList.size * PRE_LOAD_CHAPTER_SCROLL_SCALE)) {
                        loadPreChapter(ReaderStatus.position.group - 1)
                    } else if (mLastVisiblePosition > (mOriginDataList.size * NEXT_LOAD_CHAPTER_SCROLL_SCALE)) {
                        loadNextChapter(ReaderStatus.position.group + 1)
                    }
//                } else if (mOriginDataList.size > 0 && mOriginDataList[linearLayoutManager.findFirstVisibleItemPosition()].lines[0].sequenceType == PagerScrollAdapter.HEADER_ITEM_TYPE) {
                } else if (mOriginDataList.size > 0 &&
                        mOriginDataList[firstVisibleItemPosition].lines.size > 0 &&
                        mOriginDataList[firstVisibleItemPosition].lines[0].sequenceType == PagerScrollAdapter.HEADER_ITEM_TYPE) {
                    loadPreChapter(ReaderStatus.position.group - 1)
                }
                mCanScrollVertically = recyclerView.canScrollVertically(1)
            }
        })

        mGestureDetector = GestureDetector(this.context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val childe = recl_reader_content.findChildViewUnder(e.x, e.y)
                if (childe != null) {
                    val viewHolder = recl_reader_content.getChildViewHolder(childe)
                    if (viewHolder != null) {

                        if (viewHolder is PagerScrollAdapter.AdViewHolder) {
                            this@RecyclerReadView.parent.requestDisallowInterceptTouchEvent(true)
                            return true

                        } else if (viewHolder is PagerScrollAdapter.PagerHolder) {
                            val groupLocation = IntArray(2)
                            recl_reader_content.getLocationOnScreen(groupLocation)
                            val evX = (e.x + groupLocation[0]).toInt()
                            val evY = (e.y + groupLocation[1]).toInt()

                            if (viewHolder.singleTapUpIsInside(evX, evY)) {
                                this@RecyclerReadView.parent.requestDisallowInterceptTouchEvent(true)
                                return true
                            }
                        }
                    }
                }
                return super.onSingleTapUp(e)
            }

        })

        recl_reader_content.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {

            override fun onTouchEvent(rv: RecyclerView?, e: MotionEvent?) {
                mGestureDetector.onTouchEvent(e)
            }

            override fun onInterceptTouchEvent(rv: RecyclerView?, e: MotionEvent?): Boolean {
                mGestureDetector.onTouchEvent(e)
                return false
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) = Unit
        })

//        loading_error_setting.visibility = View.GONE

        recl_reader_content.setOnTouchListener { _, ev ->
            var flag = false
            when (ev?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downPointF.set(ev.x, ev.y)
                    shouldShowMenu = menuRect.contains(ev.x.toInt(), ev.y.toInt())
                }
                MotionEvent.ACTION_MOVE -> {
                    if (ReaderStatus.isMenuShow) {
                        flag = false
                    }
                    if (Math.abs(ev.x - downPointF.x) >= AppHelper.touchSlop
                            || Math.abs(ev.y - downPointF.y) >= AppHelper.touchSlop) {
                        shouldShowMenu = false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (shouldShowMenu || ReaderStatus.isMenuShow) {
                        EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE))
                        flag = false
                    }
                }
            }

            flag
        }
    }

    val menuRect by lazy {
        Rect(width / 3, height / 5, width / 3 * 2, height / 5 * 4)
    }

    var shouldShowMenu = false

    val downPointF = PointF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        menuRect.set(Rect(width / 3, height / 5, width / 3 * 2, height / 5 * 4))
    }

    fun entrance() {

        showLoadPage(ReaderStatus.position.group)

        if (mOriginDataList.size > 0) {
            mOriginDataList.clear()
        }

        mAdapter = PagerScrollAdapter(context)
        mAdapter.setOnLoadViewClickListener(this)
        recl_reader_content.adapter = mAdapter

        if (ReaderStatus.position.group == -1) {
            ReaderStatus.position.group = 0
            mFirstRead = true
        }
        DataProvider.loadPre(ReaderStatus.position.group + 2, ReaderStatus.position.group + 6)

        getChapterData(ReaderStatus.position.group, ReadViewEnums.PageIndex.current, false)

        setBackground()
    }

    /**
     * 上翻页
     */
    private fun loadPreChapter(sequence: Int) {
//        if (mIsJumpChapter) return
        if ((!checkLoadChapterValid(sequence)) || sequence < 0) return
        if (mChapterLoadStat == CHAPTER_WAITING) {
            mChapterLoadStat = CHAPTER_LOADING
            loadChapterState {
                getChapterData(sequence, ReadViewEnums.PageIndex.previous, false)
                if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
                    mAdapter.setLoadViewState(PagerScrollAdapter.LOAD_VIEW_FAIL_STATE)
                } else {
                    mAdapter.setLoadViewState(PagerScrollAdapter.LOAD_VIEW_LOADING_STATE)
                }
            }
        }
    }

    /**
     * 下翻页
     */
    private fun loadNextChapter(sequence: Int) {
//        if (mIsJumpChapter) return
        if ((!checkLoadChapterValid(sequence)) || sequence > ReaderStatus.chapterCount - 1) return
        if (mChapterLoadStat == CHAPTER_WAITING) {
            mChapterLoadStat = CHAPTER_LOADING
            loadChapterState {
                getChapterData(sequence, ReadViewEnums.PageIndex.next, false)
                if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
                    mAdapter.setLoadViewState(PagerScrollAdapter.LOAD_VIEW_FAIL_STATE)
                } else {
                    mAdapter.setLoadViewState(PagerScrollAdapter.LOAD_VIEW_LOADING_STATE)
                }
            }
        }
    }

    private fun loadChapterState(operation: (() -> Unit)) {
        Handler().postDelayed(operation, 300)
    }

    private fun getChapterData(sequence: Int, index: ReadViewEnums.PageIndex, reLoad: Boolean) {
        DataProvider.loadGroupWithVertical(sequence, { success: Boolean, chapter: Chapter? ->
            if (success) {
                chapter?.let {
                    handleChapter(chapter, index, reLoad)
                }
                dismissLoadPage()
//                if (ReaderStatus.isJumpMenuShow) {
//                    mReadPageChange?.showMenu(true)
//                    ReadState.isJumpMenuShow = false
//                }
            } else {
                mChapterLoadStat = CHAPTER_WAITING
                mAdapter.setLoadViewState(PagerScrollAdapter.LOAD_VIEW_FAIL_STATE)
                if (mOriginDataList.size == 0) {
                    showErrorPage()
                }
                dismissLoadPage()
            }
        })
    }

    private fun handleChapter(chapter: Chapter, index: ReadViewEnums.PageIndex, reLoad: Boolean) {
        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            mAdapter.setLoadViewState(PagerScrollAdapter.LOAD_VIEW_FAIL_STATE)
        } else {
            mAdapter.setLoadViewState(PagerScrollAdapter.LOAD_VIEW_LOADING_STATE)
        }
        if (TextUtils.isEmpty(chapter.content)) {
            recl_reader_content.isEnabled = true
            mChapterLoadStat = CHAPTER_WAITING
            mAdapter.setLoadViewState(PagerScrollAdapter.LOAD_VIEW_FAIL_STATE)
            return
        }

        when (index) {
        //上翻页
            ReadViewEnums.PageIndex.previous -> {
                if (!checkLoadChapterValid(chapter.sequence)) return
                val preChapterContent = DataProvider.chapterCache.get(chapter.sequence)
                preChapterContent?.apply {
                    setChapterPagePosition(chapter.sequence, chapter.name ?: "", preChapterContent.separateList)

                    val scrollIndex = mAdapter.addPreChapter(chapter.sequence, preChapterContent.separateList)
                    // 加载上一章的操作是否来自加载视图，防止加载数据时列表视图往上跳转
                    val firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition()
                    if (firstVisibleItemPosition != -1 && mOriginDataList.size != 0 && firstVisibleItemPosition < mOriginDataList.size) {
                        val currentItemSequence = if (mOriginDataList[firstVisibleItemPosition].lines.size > 0) {
                            mOriginDataList[firstVisibleItemPosition].lines[0].sequence
                        } else {
                            PagerScrollAdapter.AD_ITEM_TYPE
                        }
                        if (scrollIndex != -1) {
                            if (currentItemSequence == PagerScrollAdapter.HEADER_ITEM_TYPE) {
                                recl_reader_content.scrollToPosition(scrollIndex + 1)
                            } else if (currentItemSequence == PagerScrollAdapter.AD_ITEM_TYPE) {
                                recl_reader_content.scrollToPosition(scrollIndex + 2)
                            }
                        }
                    }
                    addBookHomePage(chapter)

                    loadAdViewToChapterBetween(chapter.sequence, ReadViewEnums.PageIndex.previous)

                    mChapterLoadStat = CHAPTER_WAITING

                    // 在获取到上一章数据时，可能存在章节字数过少，造成无法运算上一章拉取逻辑
                    if (preChapterContent.separateList.size < 2) {
                        loadPreChapter(chapter.sequence - 1)
                    }
                }
            }
        //当前阅读页
            ReadViewEnums.PageIndex.current -> {
                if (reLoad && mAdapter.getAllData().size > 0) {
                    mAdapter.clearData()
                }
                mAdapter.setChapterCatalog(ReaderStatus.chapterList)
                if (!checkLoadChapterValid(chapter.sequence)) return
                val currentChapterContent = DataProvider.chapterCache.get(chapter.sequence)
                currentChapterContent?.apply {
                    ReaderStatus.position.index = ReadSeparateHelper.getCurrentPage(ReaderStatus.position.offset, currentChapterContent.separateList)
                    mOriginDataList.addAll(currentChapterContent.separateList)
                    setChapterPagePosition(chapter.sequence, chapter.name ?: "", mOriginDataList)
                    mAdapter.setChapter(chapter.sequence, mOriginDataList)

                    loadAdViewToChapterBetween(chapter.sequence, ReadViewEnums.PageIndex.current)

                    addBookHomePage(chapter)
                    if (ReaderStatus.position.group == 0 && ReaderStatus.position.index == 0) {
                        if (mFirstRead) {
                            recl_reader_content.scrollToPosition(0)
                        } else {
                            recl_reader_content.scrollToPosition(1)
                        }
                    } else {
                        val position = ReaderStatus.position.index
                        if (ReaderStatus.position.index > 0) {
                            recl_reader_content.scrollToPosition(position)
                        } else if (ReaderStatus.position.index == 0) {
                            recl_reader_content.scrollToPosition(1)
                            ReaderStatus.position.index = 0
                        }
                    }
                    if (chapter.sequence == ReaderStatus.chapterCount - 1) {
                        mAdapter.showFootView(false)
                    }
                    mChapterLoadStat = CHAPTER_WAITING
                }
            }
        //下翻页
            ReadViewEnums.PageIndex.next -> {
                if (!checkLoadChapterValid(chapter.sequence)) return
                if (ReaderStatus.position.group == -1) {
                    ReaderStatus.position.group = 0
                }
                val nextChapterContent = DataProvider.chapterCache.get(chapter.sequence)
                nextChapterContent?.apply {
                    setChapterPagePosition(chapter.sequence, chapter.name ?: "", nextChapterContent.separateList)

                    mAdapter.addNextChapter(chapter.sequence, nextChapterContent.separateList)

                    loadAdViewToChapterBetween(chapter.sequence, ReadViewEnums.PageIndex.next)

                    addBookHomePage(chapter)
                    if (chapter.sequence == ReaderStatus.chapterCount - 1) {
                        mAdapter.showFootView(false)
                    }
                    mChapterLoadStat = CHAPTER_WAITING
                    // 在获取到下一章数据时，可能存在章节字数过少，造成无法运算下一章拉取逻辑
                    if (nextChapterContent.separateList.size < 2) {
                        loadNextChapter(chapter.sequence + 1)
                    }
                }
            }
        }
    }

    private fun addBookHomePage(chapter: Chapter) {
        if (chapter.sequence == 0 && checkLoadChapterValid(-1)) {
            mAdapter.addPreChapter(-1, arrayListOf(NovelPageBean(arrayListOf(NovelLineBean().apply { lineContent = "txtzsydsq_homepage\n";sequence = -1;sequenceType = -1 }), 0, ArrayList())))
            mAdapter.showHeaderView(false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setCurrentChapterInfo(position: Int) {

        if (position > -1 && mOriginDataList.size > 0 && mOriginDataList.size > position && mOriginDataList[position].lines.size > 0) {

            if (mOriginDataList[position].lines[0].sequence != PagerScrollAdapter.HEADER_ITEM_TYPE
                    && mOriginDataList[position].lines[0].sequence != PagerScrollAdapter.FOOTER_ITEM_TYPE
                    && mOriginDataList[position].lines[0].sequence != PagerScrollAdapter.AD_ITEM_TYPE) {

//                ReadState.chapterName = mOriginDataList[position].lines[0].chapterName
                ReaderStatus.position.index = getCurrentChapterPage(position)
                ReaderStatus.position.offset = mOriginDataList[position].offset
                ReaderStatus.position.group = mOriginDataList[position].lines[0].sequence
                ReaderStatus.position.groupChildCount = getCurrentChapterPageCount(mOriginDataList[position].lines[0].sequence)
                EventBus.getDefault().post(EventLoading(EventLoading.Type.PROGRESS_CHANGE))
//                ReaderStatus.contentLength = mOriginDataList[position].contentLength todo
            }
            //todo 打点统计
        }
    }

    /**
     * 章节是否重复
     */
    @Synchronized
    private fun checkLoadChapterValid(sequence: Int): Boolean {
        var valid = true
        if (mOriginDataList.size > 0) {
            foo@ for (pages in mOriginDataList) {
                for (content in pages.lines) {
                    if (content.sequenceType == sequence) {
                        valid = false
                        break@foo
                    }
                }
            }
        }
        return valid
    }

    /**
     * 章节内广告是否重复
     */
    @Synchronized
    private fun checkLoadAdValid(sequence: Int): Int =
            mAdapter.getAllData().filter { it.lines[0].sequenceType == sequence }.filter { it.adType != "" }.size

    /**
     * 当前显示位置章节页数
     */
    private fun getCurrentChapterPage(position: Int): Int {
        return if (mOriginDataList.size > 0 && mOriginDataList[position].lines.size > 0) {
            // 只需从任意位置获取位置
            mOriginDataList[position].lines[0].position
        } else 0
    }

    /**
     * 当前显示章节页数总和
     */
    private fun getCurrentChapterPageCount(sequence: Int): Int {
        var chapterPageCount = 0
        if (mOriginDataList.size > 0) {
            for (chapterPageList in mOriginDataList) {
                if (chapterPageList.lines.size > 0) {
                    if (chapterPageList.lines[0].sequence == sequence) {
                        chapterPageCount++
                    }
                }
            }
        }
        return chapterPageCount
    }

    /**
     * 计算章节页数下标
     */
    private fun setChapterPagePosition(sequence: Int, chapterName: String, chapterContent: List<NovelPageBean>?) {
        if (chapterContent == null || chapterContent.isEmpty()) {
            return
        }
        for (i in chapterContent.indices) {
            val pageContent = chapterContent[i].lines
            for (content in pageContent) {
                content.position = i
                content.chapterName = chapterName
                content.sequence = sequence
                content.sequenceType = sequence
            }

            val offset = if (ReaderSettings.instance.isLandscape) 1 else 2
            if (chapterContent.size - offset >= 0 && i == chapterContent.size - offset) {
                chapterContent[i].isLastPage = true
            }
        }
    }

    /**
     * 添加广告 章节间  5-3   chapterContent: ArrayList<NovelPageBean>,
     */
    private fun loadAdViewToChapterBetween(sequence: Int, index: ReadViewEnums.PageIndex) {
        if (Constants.isHideAD) return
        if (sequence == -1 && checkLoadAdValid(sequence) != 0) return
//
//        val adData = NovelPageBean(arrayListOf(NovelLineBean().apply { this.sequence = PagerScrollAdapter.AD_ITEM_TYPE; this.sequenceType = sequence }), 0,
//                arrayListOf())
//        mAdapter.addAdViewToTheChapterLast(sequence, adData)
        mAdapter.clearUselessChapter(index)
//        mDataProvider.loadChapterBetweenAd(context, object : DataProvider.OnLoadAdViewCallback(sequence) {
//
//            override fun onFail() {
//                mAdapter.removeViewToTheChapterLast(loadAdBySequence)
//            }
//
//            override fun onLoadAd(adView: ViewGroup) {
//                adData.apply { isAd = true;this.adBigView = adView }
//            }
//        })
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {

        val tmpX = event.x
        val tmpY = event.y
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                mStartTouchX = tmpX
                mStartTouchY = tmpY
                mLastY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                // 底部
                if (mLastY - event.y > 20) {
                    if (!mCanScrollVertically) {
                        //跳转书末页
                        EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.GO_TO_BOOKEND))
                    }
                    // 顶部
                } else if (mLastY - event.y < -20) {
                    if (ReaderStatus.position.group == -1) {
                        context.applicationContext.showToastMessage(R.string.is_first_chapter)
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                smoothScrollUp(event)
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                smoothScrollDown(event)
                return true
            }
        }
        return false
    }

    private fun smoothScrollUp(event: KeyEvent) {
        if (event.action == KeyEvent.ACTION_UP) {
            if (mLastVisiblePosition == 0) {
                context.applicationContext.showToastMessage(R.string.is_first_chapter)
                return
            }
            recl_reader_content.smoothScrollBy(0, -AppUtils.dp2px(resources, 300f).toInt())
        }
    }

    private fun smoothScrollDown(event: KeyEvent) {
        if (event.action == KeyEvent.ACTION_UP) {
            recl_reader_content.smoothScrollBy(0, AppUtils.dp2px(resources, 300f).toInt())
        }
    }

    override fun onLoadViewClick(type: Int) {
        when (type) {
            PagerScrollAdapter.HEADER_ITEM_TYPE ->  loadPreChapter(ReaderStatus.position.group - 1)
            PagerScrollAdapter.FOOTER_ITEM_TYPE ->  loadNextChapter(ReaderStatus.position.group + 1)
        }
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if(visibility == View.VISIBLE && ReaderSettings.instance.animation == GLReaderView.AnimationType.LIST){
            if(!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this)
            }
        }else if(visibility != View.VISIBLE && ReaderSettings.instance.animation != GLReaderView.AnimationType.LIST){
            mOriginDataList?.clear()
            mAdapter?.clearData()
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    private fun onRedrawPage() {
        DataProvider.clearPreLoad()
        entrance()
    }

    private fun onJumpChapter(sequence: Int) {
        DataProvider.clearPreLoad()
        ReaderStatus.position.group = sequence
        ReaderStatus.position.offset = 0
        entrance()
    }

    private fun showLoadPage(sequence: Int) {
        if (DataProvider.isCacheExistBySequence(sequence)) {
            return
        }
        recl_reader_content?.visibility = GONE
//        view_reader_error.visibility = GONE
        view_reader_loading?.visibility = View.VISIBLE
    }

    private fun showErrorPage() {
        recl_reader_content?.visibility = GONE
//        view_reader_error.visibility = View.VISIBLE
    }

    private fun dismissLoadPage() {
        recl_reader_content?.visibility = VISIBLE
        view_reader_loading?.visibility = GONE
//        view_reader_error.visibility = GONE
    }

    private fun setBackground() {
        ThemeUtil.getModePrimaryBackground(resources, this)
        mAdapter.setTextColor(resources.getColor(ThemeUtil.modeLoadTextColor))
        ThemeUtil.getModePrimaryBackground(resources, view_reader_loading)
        txt_reader_loading_message?.setTextColor(resources.getColor(ThemeUtil.modeLoadTextColor))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecieveEvent(event: EventSetting) {
        when (event.type) {
            EventSetting.Type.CHANGE_SCREEN_MODE -> onRedrawPage()//横竖屏切换
            EventSetting.Type.REFRESH_MODE -> setBackground()
            else -> Unit
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecieveEvent(event: EventReaderConfig) {
        when (event.type) {
            ReaderSettings.ConfigType.CHAPTER_REFRESH -> if (event.obj is Position) onJumpChapter(event.obj.group) //跳章
            ReaderSettings.ConfigType.PAGE_REFRESH -> setBackground()//背景
            ReaderSettings.ConfigType.FONT_REFRESH -> {
                if(event.obj != null){
                    val position = event.obj as Position
                    ReaderStatus.position.group = position.group
                    ReaderStatus.position.offset = position.offset
                }
                onRedrawPage()
            } //字体大小、字体间距
            else -> Unit
        }
    }

    fun onKeyEvent(event: KeyEvent): Boolean {
        return when {
            event.keyCode == KeyEvent.KEYCODE_VOLUME_UP && this.visibility == View.VISIBLE -> {
                smoothScrollUp(event)
                true
            }
            this.visibility == View.VISIBLE && event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && this.visibility == View.VISIBLE -> {
                smoothScrollDown(event)
                true
            }
            else -> false
        }
    }
}