package com.intelligent.reader.read.page

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import com.intelligent.reader.R
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.help.HorizontalEvent
import com.intelligent.reader.read.help.IReadPageChange
import com.intelligent.reader.read.help.IReadView
import com.intelligent.reader.read.help.ReadSeparateHelper
import com.intelligent.reader.read.mode.NovelPageBean
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.util.ThemeUtil
import kotlinx.android.synthetic.main.error_page2.view.*
import kotlinx.android.synthetic.main.loading_page_reading.view.*
import kotlinx.android.synthetic.main.vertical_pager_layout.view.*
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.data.bean.ReadViewEnums
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.ToastUtils
import net.lzbook.kit.utils.runOnMain
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class VerticalReaderView : FrameLayout, IReadView, PagerScrollAdapter.OnLoadViewClickListener, Observer {

    private lateinit var mAdapter: PagerScrollAdapter

    private lateinit var mDataProvider: DataProvider

    private lateinit var mLayoutManager: WrapContentLinearLayoutManager

    private lateinit var mOriginDataList: CopyOnWriteArrayList<NovelPageBean>

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

    private var isShowMenu: Boolean = false

    /**
     * 上翻页章节阅读比例
     */
    private val PRE_LOAD_CHAPTER_SCROLL_SCALE = 0.3

    /**
     * 下翻页章节阅读比例
     */
    private val NEXT_LOAD_CHAPTER_SCROLL_SCALE = 0.6

    private lateinit var mGestureDetector: GestureDetector

    constructor(context: Context?) : this(context, null) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.vertical_pager_layout, this)
        mLayoutManager = WrapContentLinearLayoutManager(context)
        page_rv.layoutManager = mLayoutManager
        mDataProvider = DataProvider.getInstance()
        mOriginDataList = CopyOnWriteArrayList()
        origin_tv.setOnClickListener {
            mReadPageChange?.onOriginClick()
        }
        trans_coding_tv.setOnClickListener {
            mReadPageChange?.onTransCodingClick()
        }
        loading_error_reload.setOnClickListener {
            entrance()
        }

        page_rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (mLastVisiblePosition != linearLayoutManager.findLastVisibleItemPosition()) {
                    mLastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition()
                    setCurrentChapterInfo(mLastVisiblePosition)
                    if (mLastVisiblePosition < (mOriginDataList.size * PRE_LOAD_CHAPTER_SCROLL_SCALE)) {
                        loadPreChapter(ReadState.sequence - 1)
                    } else if (mLastVisiblePosition > (mOriginDataList.size * NEXT_LOAD_CHAPTER_SCROLL_SCALE)) {
                        loadNextChapter(ReadState.sequence + 1)
                    }
                }
                mCanScrollVertically = recyclerView.canScrollVertically(1)
            }
        })

        mGestureDetector = GestureDetector(this.context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val childe = page_rv.findChildViewUnder(e.x, e.y)
                if (childe != null) {
                    val viewHolder = page_rv.getChildViewHolder(childe)
                    if (viewHolder != null) {

                        if (viewHolder is PagerScrollAdapter.AdViewHolder) {
                            this@VerticalReaderView.parent.requestDisallowInterceptTouchEvent(true)
                            return true

                        } else if (viewHolder is PagerScrollAdapter.PagerHolder) {
                            val groupLocation = IntArray(2)
                            page_rv.getLocationOnScreen(groupLocation)
                            val evX = (e.x + groupLocation[0]).toInt()
                            val evY = (e.y + groupLocation[1]).toInt()

                            if (viewHolder.singleTapUpIsInside(evX, evY)) {
                                this@VerticalReaderView.parent.requestDisallowInterceptTouchEvent(true)
                                return true
                            } else {
                                showMenuClick(e)
                                return super.onSingleTapUp(e)
                            }
                        } else {
                            showMenuClick(e)
                            return super.onSingleTapUp(e)
                        }
                    }
                }
                return super.onSingleTapUp(e)
            }

        })

        page_rv.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {

            override fun onTouchEvent(rv: RecyclerView?, e: MotionEvent?) {
                mGestureDetector.onTouchEvent(e)
            }

            override fun onInterceptTouchEvent(rv: RecyclerView?, e: MotionEvent?): Boolean {
                mGestureDetector.onTouchEvent(e)
                return false
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }

        })
        loading_error_setting.visibility = View.GONE
    }

    override fun entrance() {

        showLoadPage(ReadState.sequence)

        if (mOriginDataList.size > 0) {
            mOriginDataList.clear()
        }

        mAdapter = PagerScrollAdapter(context)
        mAdapter.setOnLoadViewClickListener(this)
        page_rv.adapter = mAdapter

        if (ReadState.sequence == -1) {
            ReadState.sequence = 0
            mFirstRead = true
        }

        DataProvider.getInstance().preLoad(ReadState.sequence, ReadState.sequence + 6)

        getChapterData(ReadState.sequence, ReadViewEnums.PageIndex.current, false)

        loadPreChapter(ReadState.sequence - 1)
        loadNextChapter(ReadState.sequence + 1)
        setBackground()
    }

    /**
     * 上翻页
     */
    private fun loadPreChapter(sequence: Int) {
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
        if ((!checkLoadChapterValid(sequence)) || sequence > ReadState.chapterList.size - 1) return
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
        ReadState.book?.let {
            mDataProvider.loadChapter2(it, sequence, index, object : DataProvider.ReadDataListener() {
                override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) {
                    runOnMain {
                        handleChapter(c, type, reLoad)
                        dismissLoadPage()
                    }
                }

                override fun loadDataError(message: String) {
                    runOnMain {
                        mChapterLoadStat = CHAPTER_WAITING
                        mAdapter.setLoadViewState(PagerScrollAdapter.LOAD_VIEW_FAIL_STATE)
                        if (mOriginDataList.size > 0) {
                            dismissLoadPage()
                        } else {
                            showErrorPage()
                        }

                    }
                }
            })
        }
    }

    private fun handleChapter(chapter: Chapter, index: ReadViewEnums.PageIndex, reLoad: Boolean) {
        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            mAdapter.setLoadViewState(PagerScrollAdapter.LOAD_VIEW_FAIL_STATE)
        } else {
            mAdapter.setLoadViewState(PagerScrollAdapter.LOAD_VIEW_LOADING_STATE)
        }
        if (TextUtils.isEmpty(chapter.content)) {
            page_rv.isEnabled = true
            mChapterLoadStat = CHAPTER_WAITING
            mAdapter.setLoadViewState(PagerScrollAdapter.LOAD_VIEW_FAIL_STATE)
            return
        }

        when (index) {

        /**
         * 上翻页
         */
            ReadViewEnums.PageIndex.previous -> {
                if (!checkLoadChapterValid(chapter.sequence)) return
                val preChapterContent = ReadSeparateHelper.initTextSeparateContent(chapter.content, chapter.chapter_name)
                setChapterPagePosition(chapter.sequence, chapter.chapter_name, preChapterContent)
                loadAdViewToChapterLastPage(preChapterContent)
                val scrollIndex = mAdapter.addPreChapter(chapter.sequence, preChapterContent)
                // 加载上一章的操作是否来自加载视图，防止加载数据时列表视图往上跳转
                val firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItemPosition != -1 && mOriginDataList.size != 0) {
                    val currentItemSequence = mOriginDataList[firstVisibleItemPosition].lines[0].sequence
                    if (scrollIndex != -1) {
                        if (currentItemSequence == PagerScrollAdapter.HEADER_ITEM_TYPE) {
                            page_rv.scrollToPosition(scrollIndex + 1)
                        } else if (currentItemSequence == PagerScrollAdapter.AD_ITEM_TYPE) {
                            page_rv.scrollToPosition(scrollIndex + 2)
                        }
                    }
                }
                addBookHomePage(chapter)
                loadAdViewToChapterBetween(chapter.sequence, ReadViewEnums.PageIndex.previous)
                mChapterLoadStat = CHAPTER_WAITING

                // 在获取到上一章数据时，可能存在章节字数过少，造成无法运算上一章拉取逻辑
                if (preChapterContent.size < 2) {
                    loadPreChapter(chapter.sequence - 1)
                }
            }

        /**
         * 当前阅读页
         */
            ReadViewEnums.PageIndex.current -> {
                if (reLoad && mAdapter.getAllData().size > 0) {
                    mAdapter.clearData()
                }
                mAdapter.setChapterCatalog(ReadState.chapterList)
                if (!checkLoadChapterValid(chapter.sequence)) return
                val currentChapterContent = ReadSeparateHelper.initTextSeparateContent(chapter.content, chapter.chapter_name)
                ReadState.currentPage = ReadSeparateHelper.getCurrentPage(ReadState.offset, currentChapterContent)
                mOriginDataList.addAll(currentChapterContent)
                setChapterPagePosition(chapter.sequence, chapter.chapter_name, mOriginDataList)
                loadAdViewToChapterLastPage(mOriginDataList)
                mAdapter.setChapter(chapter.sequence, mOriginDataList)
                loadAdViewToChapterBetween(chapter.sequence, ReadViewEnums.PageIndex.current)
                addBookHomePage(chapter)
                if (ReadState.sequence == 0 && ReadState.currentPage == 1) {
                    if (mFirstRead) {
                        page_rv.scrollToPosition(0)
                    } else {
                        page_rv.scrollToPosition(1)
                    }
                } else {
                    val position = ReadState.currentPage
                    if (ReadState.currentPage > 1) {
                        page_rv.scrollToPosition(position)
                    } else if (ReadState.currentPage == 1) {
                        page_rv.scrollToPosition(1)
                        ReadState.currentPage = 1
                    }
                }
                if (chapter.sequence == ReadState.chapterList.size - 1) {
                    mAdapter.showFootView(false)
                }
                mChapterLoadStat = CHAPTER_WAITING
            }

        /**
         * 下翻页
         */
            ReadViewEnums.PageIndex.next -> {
                if (!checkLoadChapterValid(chapter.sequence)) return
                if (ReadState.sequence == -1) {
                    ReadState.sequence = 0
                }
                val nextChapterContent = ReadSeparateHelper.initTextSeparateContent(chapter.content, chapter.chapter_name)
                setChapterPagePosition(chapter.sequence, chapter.chapter_name, nextChapterContent)
                loadAdViewToChapterLastPage(nextChapterContent)
                mAdapter.addNextChapter(chapter.sequence, nextChapterContent)
                loadAdViewToChapterBetween(chapter.sequence, ReadViewEnums.PageIndex.next)
                addBookHomePage(chapter)
                if (chapter.sequence == ReadState.chapterList.size - 1) {
                    mAdapter.showFootView(false)
                }
                mChapterLoadStat = CHAPTER_WAITING

                // 在获取到下一章数据时，可能存在章节字数过少，造成无法运算下一章拉取逻辑
                if (nextChapterContent.size < 2) {
                    loadNextChapter(chapter.sequence + 1)
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

        if (mOriginDataList.size > 0 && mOriginDataList.size > position && mOriginDataList[position].lines.size > 0) {

            if (mOriginDataList[position].lines[0].sequence != PagerScrollAdapter.HEADER_ITEM_TYPE
                    && mOriginDataList[position].lines[0].sequence != PagerScrollAdapter.FOOTER_ITEM_TYPE
                    && mOriginDataList[position].lines[0].sequence != PagerScrollAdapter.AD_ITEM_TYPE) {

                ReadState.chapterName = mOriginDataList[position].lines[0].chapterName
                novel_title.text = ReadState.chapterName
                novel_chapter.text = "${mOriginDataList[position].lines[0].sequence + 1} / ${ReadState.chapterList.size} 章"
                novel_page.text = "本章第${(getCurrentChapterPage(position) + 1)} / ${getCurrentChapterPageCount(mOriginDataList[position].lines[0].sequence)}"

                ReadState.currentPage = getCurrentChapterPage(position)
                ReadState.offset = mOriginDataList[position].offset
                ReadState.sequence = mOriginDataList[position].lines[0].sequence
                ReadState.pageCount = getCurrentChapterPageCount(mOriginDataList[position].lines[0].sequence)
                ReadState.contentLength = mOriginDataList[position].contentLength
            }

            mReadPageChange?.addLog()
        }

        if (ReadState.sequence == -1) {
            novel_title_layout.visibility = View.INVISIBLE
            novel_bottom.visibility = View.INVISIBLE
        } else {
            novel_title_layout.visibility = View.VISIBLE
            novel_bottom.visibility = View.VISIBLE
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
            mAdapter.getAllData().filter { it.lines[0].sequenceType == sequence }.filter { it.adBigView != null }.size

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
            if (i == chapterContent.size - 1) {
                chapterContent[i].isLastPage = true
            }
        }
    }

    /**
     * 添加广告 段尾  6-3
     */
    private fun loadAdViewToChapterLastPage(chapterContent: List<NovelPageBean>) {
        if (Constants.isHideAD) return
        var lineData: NovelLineBean? = null
        for (i in chapterContent.indices) {
            val pageContent = chapterContent[i]
            for (j in pageContent.lines.indices) {
                if (j == pageContent.lines.size - 1) {
                    lineData = pageContent.lines[j]
                }
            }
        }

        if (lineData?.adView != null) {
            return
        }

        mDataProvider.loadChapterLastPageAd(context, object : DataProvider.OnLoadReaderAdCallback {

            override fun onFail() {
            }

            override fun onLoadAd(adView: ViewGroup) {
                lineData?.adView = adView
            }
        })
    }

    /**
     * 添加广告 章节间  5-3   chapterContent: ArrayList<NovelPageBean>,
     */
    private fun loadAdViewToChapterBetween(sequence: Int, index: ReadViewEnums.PageIndex) {
        if (Constants.isHideAD) return
        if (sequence == -1 && checkLoadAdValid(sequence) != 0) return

        val adData = NovelPageBean(arrayListOf(NovelLineBean().apply { this.sequence = PagerScrollAdapter.AD_ITEM_TYPE; this.sequenceType = sequence }), 0,
                arrayListOf())
        mAdapter.addAdViewToTheChapterLast(sequence, adData)
        mAdapter.clearUselessChapter(index)

        mDataProvider.loadChapterBetweenAd(context, object : DataProvider.OnLoadAdViewCallback(sequence) {

            override fun onFail() {
                mAdapter.removeViewToTheChapterLast(loadAdBySequence)
            }

            override fun onLoadAd(adView: ViewGroup) {
                adData.apply { isAd = true;this.adBigView = adView }
            }
        })
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (isShowMenu) {
            mReadPageChange?.showMenu(false)
            isShowMenu = false
            return true
        }
        val tmpX = event.x
        val tmpY = event.y
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                mStartTouchX = tmpX
                mStartTouchY = tmpY
                mLastY = event.y
            }

            MotionEvent.ACTION_UP -> {
//                val distance = Math.sqrt(Math.pow((mStartTouchX - tmpX).toDouble(), 2.0) + Math.pow((mStartTouchY - tmpY).toDouble(), 2.0)).toInt()
//                if (distance < 30 || distance < 10) {
//                    showMenuClick(event)
//                }
            }
            MotionEvent.ACTION_MOVE -> {
                // 底部
                if (mLastY - event.y > 20) {
                    if (!mCanScrollVertically) {
                        mReadPageChange?.goToBookOver()
                    }
                    // 顶部
                } else if (mLastY - event.y < -20) {
                    if (ReadState.sequence == -1) {
                        ToastUtils.showToastNoRepeat(resources.getString(R.string.is_first_chapter))
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onLoadViewClick(type: Int) {
        when (type) {
            PagerScrollAdapter.HEADER_ITEM_TYPE -> {
                loadPreChapter(ReadState.sequence - 1)
            }
            PagerScrollAdapter.FOOTER_ITEM_TYPE -> {
                loadNextChapter(ReadState.sequence + 1)
            }
        }
    }

    private fun showMenuClick(event: MotionEvent) {
        val x = event.x.toInt()
        val y = event.y.toInt()
        val h4 = height / 4
        val w3 = width / 3
        if (x <= w3) {
        } else if (x >= width - w3 || y >= height - h4 && x >= w3) {
        } else {
            mReadPageChange?.showMenu(true)
            isShowMenu = true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ReadConfig.registObserver(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ReadConfig.unregistObserver(this)
        for (i in mOriginDataList.indices) {
            val pageContent = mOriginDataList[i]
            for (content in pageContent.lines) {
                if (content.adView != null && content.adView.tag != null) {
                    content.adView.tag = null
                }
                content.adView = null
            }
            if (pageContent.adBigView != null && pageContent.adBigView?.tag != null) {
                pageContent.adBigView?.tag = null
            }
            pageContent.adBigView = null
        }
        mOriginDataList.clear()
    }

    private fun onRedrawPage() {
        entrance()
    }

    private fun onJumpChapter(sequence: Int) {
        if (sequence == 0) {
            mFirstRead = false
        }
        ReadState.sequence = sequence
        entrance()
    }

    override fun onAnimationChange(animation: ReadViewEnums.Animation) {

    }

    private fun showLoadPage(sequence: Int) {
        if (mDataProvider.isCacheExistBySequence(sequence)) {
            return
        }
        page_rv.visibility = GONE
        error_page.visibility = GONE
        load_page.visibility = View.VISIBLE
    }

    private fun showErrorPage() {
        page_rv.visibility = GONE
        error_page.visibility = View.VISIBLE
    }

    private fun dismissLoadPage() {
        page_rv.visibility = VISIBLE
        load_page.visibility = GONE
        error_page.visibility = GONE
    }

    private fun setBackground() {
        ThemeUtil.getModePrimaryBackground(resources, this)
        origin_tv.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        trans_coding_tv.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        novel_page.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        novel_chapter.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        novel_title.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        mAdapter.setTextColor(resources.getColor(ThemeUtil.modeLoadTextColor))

        ThemeUtil.getModePrimaryBackground(resources, load_page)
        tv_loading_progress.setTextColor(resources.getColor(ThemeUtil.modeLoadTextColor))
    }

    override fun update(o: Observable?, arg: Any?) {
        when (arg as String) {
            "READ_INTERLINEAR_SPACE" -> onRedrawPage()
            "FONT_SIZE" -> onRedrawPage()
            "SCREEN" -> onRedrawPage()
            "MODE" -> setBackground()
            "JUMP" -> onJumpChapter(ReadState.sequence)
        }
    }

    private var mReadPageChange: IReadPageChange? = null

    override fun setIReadPageChange(mReadPageChange: IReadPageChange?) {
        this.mReadPageChange = mReadPageChange
    }

    override fun setHorizontalEventListener(mHorizontalEvent: HorizontalEvent?) {
    }

}