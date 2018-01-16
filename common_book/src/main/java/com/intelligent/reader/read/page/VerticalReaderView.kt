package com.intelligent.reader.read.page

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.intelligent.reader.R
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.help.*
import com.intelligent.reader.read.mode.NovelPageBean
import com.intelligent.reader.read.mode.ReadInfo
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.util.ThemeUtil
import kotlinx.android.synthetic.main.vertical_pager_layout.view.*
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.data.bean.ReadViewEnums
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.ToastUtils
import java.util.concurrent.CopyOnWriteArrayList

class VerticalReaderView : FrameLayout, IReadView, PagerScrollAdapter.OnLoadViewClickListener {

    private val TAG: String = "VerticalReaderView"

    private lateinit var mReadInfo: ReadInfo

    private lateinit var mAdapter: PagerScrollAdapter

    private lateinit var mDataProvider: DataProvider

    private lateinit var mLayoutManager: WrapContentLinearLayoutManager

    private var mCatalogList: ArrayList<Chapter>? = null

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
        page_rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (mLastVisiblePosition != linearLayoutManager.findLastVisibleItemPosition()) {
                    mLastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition()
                    setCurrentChapterInfo(mLastVisiblePosition)
                }
                if (mLastVisiblePosition < (mOriginDataList.size * PRE_LOAD_CHAPTER_SCROLL_SCALE)) {
                    loadPreChapter(ReadState.sequence - 1)
                } else if (mLastVisiblePosition > (mOriginDataList.size * NEXT_LOAD_CHAPTER_SCROLL_SCALE)) {
                    loadNextChapter(ReadState.sequence + 1)
                }
                mCanScrollVertically = recyclerView.canScrollVertically(1)
            }
        })
    }

    override fun entrance(readInfo: ReadInfo) {
        if (mOriginDataList.size > 0) {
            mOriginDataList.clear()
        }

        mReadInfo = readInfo
        mAdapter = PagerScrollAdapter(context, mReadInfo.mReadStatus)
        mAdapter.setOnLoadViewClickListener(this)
        page_rv.adapter = mAdapter

        if (ReadState.sequence == -1) {
            ReadState.sequence = 0
            mFirstRead = true
        }

        mCatalogList?.apply {
            mReadInfo.mReadStatus.chapterCount = size
        }

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
        if (mChapterLoadStat == CHAPTER_WAITING && checkLoadChapterValid(sequence)) {
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
        if (mChapterLoadStat == CHAPTER_WAITING && checkLoadChapterValid(sequence)) {
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
        mDataProvider.loadChapter2(mReadInfo.curBook, sequence, index, object : DataProvider.ReadDataListener() {

            override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) {
                handleChapter(c, type, reLoad)
            }

            override fun loadDataError(message: String) {
                mChapterLoadStat = CHAPTER_WAITING
                mAdapter.setLoadViewState(PagerScrollAdapter.LOAD_VIEW_FAIL_STATE)
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
                val scrollIndex = mAdapter.addPreChapter(preChapterContent)
                loadAdViewToChapterBetween(preChapterContent, ReadViewEnums.PageIndex.previous)

                // 加载上一章的操作是否来自加载视图，防止加载数据时列表视图往上跳转
                val firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition()
                if (mOriginDataList[firstVisibleItemPosition].lines[0].sequence == PagerScrollAdapter.HEADER_ITEM_TYPE && scrollIndex != -1) {
                    page_rv.scrollToPosition(scrollIndex + 1)
                }
                addBookHomePage(chapter)
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
                    mOriginDataList.clear()
                    mAdapter.clearData()
                }
                mCatalogList = ReadState.chapterList
                mAdapter.setChapterCatalog(mCatalogList)

                if (!checkLoadChapterValid(chapter.sequence)) return
                val currentChapterContent = ReadSeparateHelper.initTextSeparateContent(chapter.content, chapter.chapter_name)
                ReadState.currentPage = ReadSeparateHelper.getCurrentPage(ReadState.offset, currentChapterContent)
                mOriginDataList.addAll(currentChapterContent)
                setChapterPagePosition(chapter.sequence, chapter.chapter_name, mOriginDataList)
                loadAdViewToChapterLastPage(mOriginDataList)
                mAdapter.setChapter(mOriginDataList)
                loadAdViewToChapterBetween(currentChapterContent, ReadViewEnums.PageIndex.current)
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
                if (chapter.sequence == mReadInfo.mReadStatus.chapterCount - 1) {
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
                mAdapter.addNextChapter(nextChapterContent)
                loadAdViewToChapterBetween(nextChapterContent, ReadViewEnums.PageIndex.next)
                addBookHomePage(chapter)
                if (chapter.sequence == mReadInfo.mReadStatus.chapterCount - 1) {
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
            mAdapter.addPreChapter(arrayListOf(NovelPageBean(arrayListOf(NovelLineBean().apply { lineContent = "txtzsydsq_homepage\n";sequence = -1 }), 0, ArrayList())))
            mAdapter.showHeaderView(false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setCurrentChapterInfo(position: Int) {

        if (mOriginDataList.size > 0 && mOriginDataList.size > position && mOriginDataList[position].lines.size > 0) {

            if (mOriginDataList[position].lines[0].sequence != PagerScrollAdapter.HEADER_ITEM_TYPE
                    && mOriginDataList[position].lines[0].sequence != PagerScrollAdapter.FOOTER_ITEM_TYPE
                    && mOriginDataList[position].lines[0].sequence != PagerScrollAdapter.AD_ITEM_TYPE) {

                mReadInfo.mReadStatus.chapterName = mOriginDataList[position].lines[0].chapterName
                novel_title.text = mReadInfo.mReadStatus.chapterName
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
                    if (content.sequence == sequence) {
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
    private fun checkLoadAdValid(chapterContent: ArrayList<NovelPageBean>): Boolean {
        var valid = true
        if (chapterContent.size > 0) {
            foo@ for (pages in chapterContent) {
                for (content in pages.lines) {
                    if (content.sequence == PagerScrollAdapter.AD_ITEM_TYPE) {
                        valid = false
                        break@foo
                    }
                }
            }
        }
        return valid
    }

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
            }
            if (i == chapterContent.size - 1) {
                chapterContent[i].isLastPage = true
            }
        }
    }

    /**
     * 添加广告 段尾  8-1
     */
    private fun loadAdViewToChapterLastPage(chapterContent: List<NovelPageBean>) {
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
            override fun onLoadAd(adView: ViewGroup) {
                lineData?.adView = adView
            }
        })
    }

    /**
     * 添加广告 章节间  5-1
     */
    private fun loadAdViewToChapterBetween(chapterContent: ArrayList<NovelPageBean>, index: ReadViewEnums.PageIndex) {
        mDataProvider.loadChapterBetweenAd(context, object : DataProvider.OnLoadReaderAdCallback {
            override fun onLoadAd(adView: ViewGroup) {
                if (!checkLoadAdValid(chapterContent)) return
                val adData = arrayListOf(NovelPageBean(arrayListOf(NovelLineBean().apply { sequence = PagerScrollAdapter.AD_ITEM_TYPE; }), 0,
                        arrayListOf()).apply { isAd = true;this.adView = adView })
                chapterContent.addAll(adData)
                mAdapter.addAllChapter(mAdapter.getNotifyIndexByLoadChapter(index, adData), adData)
            }
        })
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (isShowMenu) {
            mReadPageChange?.showMenu(false)
            isShowMenu = false
            return false
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
                val distance = Math.sqrt(Math.pow((mStartTouchX - tmpX).toDouble(), 2.0) + Math.pow((mStartTouchY - tmpY).toDouble(), 2.0)).toInt()
                if (distance < 30 || distance < 10) {
                    showMenuClick(event)
                }
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        for (i in mOriginDataList.indices) {
            val pageContent = mOriginDataList[i]
            for (content in pageContent.lines) {
                if (content.adView != null && content.adView.tag != null) {
                    content.adView.tag = null
                }
                content.adView = null
            }
            if (pageContent.adView != null && pageContent.adView?.tag != null) {
                pageContent.adView?.tag = null
            }
            pageContent.adView = null
        }
        mOriginDataList.clear()
    }

    override fun onRedrawPage() {
        getChapterData(ReadState.sequence, ReadViewEnums.PageIndex.current, true)
    }

    override fun onJumpChapter(sequence: Int) {
        getChapterData(sequence, ReadViewEnums.PageIndex.current, true)
    }

    override fun onAnimationChange(animation: ReadViewEnums.Animation) {

    }

    override fun setBackground() {
        ThemeUtil.getModePrimaryBackground(resources, this)
        origin_tv.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        trans_coding_tv.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        novel_page.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        novel_chapter.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        novel_title.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        mAdapter.setTextColor(resources.getColor(ThemeUtil.modeLoadTextColor))
    }

    private var mReadPageChange: IReadPageChange? = null

    override fun setIReadPageChange(mReadPageChange: IReadPageChange?) {
        this.mReadPageChange = mReadPageChange
    }

    override fun setHorizontalEventListener(mHorizontalEvent: HorizontalEvent?) {
    }

}