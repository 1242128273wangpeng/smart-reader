package com.intelligent.reader.read.page

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.intelligent.reader.R
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.help.IReadPageChange
import com.intelligent.reader.read.help.IReadView
import com.intelligent.reader.read.help.NovelHelper
import com.intelligent.reader.read.help.ReadSeparateHelper
import com.intelligent.reader.read.mode.NovelPageBean
import com.intelligent.reader.read.mode.ReadInfo
import com.intelligent.reader.read.mode.ReadViewEnums
import com.intelligent.reader.util.ThemeUtil
import kotlinx.android.synthetic.main.vertical_pager_layout.view.*
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.ToastUtils
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class VerticalReaderView : FrameLayout, IReadView, PagerScrollAdapter.OnLoadViewClickListener {

    private val TAG: String = "VerticalReaderView"

    private lateinit var mReadInfo: ReadInfo

    private lateinit var mAdapter: PagerScrollAdapter

    private lateinit var mDataProvider: DataProvider

    private lateinit var mLayoutManager: WrapContentLinearLayoutManager

    private lateinit var mNovelHelper: NovelHelper

    private var mCatalogList: ArrayList<Chapter>? = null

    private var mCurrentPage = 0

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

        page_rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (mLastVisiblePosition != linearLayoutManager.findLastVisibleItemPosition()) {
                    mLastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition()
                    setCurrentChapterInfo(mLastVisiblePosition)
                    mReadInfo.mReadStatus.currentPage = mLastVisiblePosition
                }
                if (mLastVisiblePosition < (mOriginDataList.size * PRE_LOAD_CHAPTER_SCROLL_SCALE)) {
                    loadPreChapter(mReadInfo.mReadStatus.sequence - 1)
                } else if (mLastVisiblePosition > (mOriginDataList.size * NEXT_LOAD_CHAPTER_SCROLL_SCALE)) {
                    loadNextChapter(mReadInfo.mReadStatus.sequence + 1)
                }
                mCanScrollVertically = recyclerView.canScrollVertically(1)
            }
        })
    }

    override fun entrance(readInfo: ReadInfo) {
        mReadInfo = readInfo

        mNovelHelper = NovelHelper(context as Activity, mReadInfo.mReadStatus)
        mAdapter = PagerScrollAdapter(context, mReadInfo.mReadStatus, mNovelHelper)
        mAdapter.setOnLoadViewClickListener(this)
        page_rv.adapter = mAdapter

        setCurrentChapterInfo(mReadInfo.mReadStatus.currentPage - 1)

        if (mReadInfo.mReadStatus.sequence == -1) {
            mReadInfo.mReadStatus.sequence = 0
            mFirstRead = true
        }

        mCatalogList?.apply {
            mReadInfo.mReadStatus.chapterCount = size
        }

        getChapterData(mReadInfo.mReadStatus.sequence, ReadViewEnums.PageIndex.current)

        loadPreChapter(mReadInfo.mReadStatus.sequence - 1)
        loadNextChapter(mReadInfo.mReadStatus.sequence + 1)
    }

    /**
     * 上翻页
     */
    private fun loadPreChapter(sequence: Int) {
        if (sequence < 0) return
        if (mChapterLoadStat == CHAPTER_WAITING && checkLoadChapterValid(sequence)) {
            mChapterLoadStat = CHAPTER_LOADING
            loadChapterState {
                getChapterData(sequence, ReadViewEnums.PageIndex.previous)
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
        if (sequence > mReadInfo.mReadStatus.chapterCount - 1) return
        if (mChapterLoadStat == CHAPTER_WAITING && checkLoadChapterValid(sequence)) {
            mChapterLoadStat = CHAPTER_LOADING
            loadChapterState {
                getChapterData(sequence, ReadViewEnums.PageIndex.next)
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

    private fun getChapterData(sequence: Int, index: ReadViewEnums.PageIndex) {
        mDataProvider.loadChapter2(mReadInfo.curBook, sequence, index, object : DataProvider.ReadDataListener() {

            override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) {
                handleChapter(c, type)
            }

            override fun loadDataError(message: String) {
                mChapterLoadStat = CHAPTER_WAITING
                mAdapter.setLoadViewState(PagerScrollAdapter.LOAD_VIEW_FAIL_STATE)
            }
        })
    }

    private fun handleChapter(chapter: Chapter, index: ReadViewEnums.PageIndex) {
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
                mReadInfo.mReadStatus.chapterName = chapter.chapter_name
                val preChapterContent = ReadSeparateHelper.getInstance(mReadInfo.mReadStatus).initTextSeparateContent(chapter.content, chapter.chapter_name)
                setChapterPagePosition(chapter.sequence, chapter.chapter_name, preChapterContent)
                addChapterBetweenAdView(preChapterContent, chapter.sequence, chapter.sequence + 1)
                addAdViewToChapterLastPage(preChapterContent)
                val scrollIndex = mAdapter.addPreChapter(preChapterContent)

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
                if (mAdapter.getAllData().size > 0) {
                    mAdapter.clearData()
                }
                mCatalogList = mDataProvider.chapterList
                mAdapter.setChapterCatalog(mCatalogList)

                if (!checkLoadChapterValid(chapter.sequence)) return
                mReadInfo.mReadStatus.chapterName = chapter.chapter_name
                mOriginDataList.addAll(ReadSeparateHelper.getInstance(mReadInfo.mReadStatus).initTextSeparateContent(chapter.content, chapter.chapter_name))
                setChapterPagePosition(chapter.sequence, chapter.chapter_name, mOriginDataList)
                addAdViewToChapterLastPage(mOriginDataList)
                addChapterBetweenAdView(chapter.sequence, chapter.sequence + 1)
                mAdapter.setChapter(mOriginDataList)
                addBookHomePage(chapter)
                if (mReadInfo.mReadStatus.sequence == 0 && mReadInfo.mReadStatus.currentPage == 1) {
                    if (mFirstRead) {
                        page_rv.scrollToPosition(0)
                    } else {
                        page_rv.scrollToPosition(1)
                    }
                } else {
                    val position = mReadInfo.mReadStatus.currentPage
                    if (mReadInfo.mReadStatus.currentPage > 1) {
                        page_rv.scrollToPosition(position + 1)
                        mReadInfo.mReadStatus.currentPage = position
                    } else if (mReadInfo.mReadStatus.currentPage == 1) {
                        page_rv.scrollToPosition(1)
                        mReadInfo.mReadStatus.currentPage = 1
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
                if (mReadInfo.mReadStatus.sequence == -1) {
                    mReadInfo.mReadStatus.sequence = 0
                }
                mReadInfo.mReadStatus.chapterName = chapter.chapter_name
                val nextChapterContent = ReadSeparateHelper.getInstance(mReadInfo.mReadStatus).initTextSeparateContent(chapter.content, chapter.chapter_name)
                setChapterPagePosition(chapter.sequence, chapter.chapter_name, nextChapterContent)
                addChapterBetweenAdView(nextChapterContent, chapter.sequence, chapter.sequence + 1)
                addAdViewToChapterLastPage(nextChapterContent)
                mAdapter.addNextChapter(nextChapterContent)
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
            mAdapter.addPreChapter(arrayListOf(NovelPageBean(arrayListOf(NovelLineBean().apply { lineContent = "txtzsydsq_homepage\n";sequence = -1 }), 0)))
            mAdapter.showHeaderView(false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setCurrentChapterInfo(position: Int) {
        if (mOriginDataList.size > 0
                && mOriginDataList.size > position
                && mOriginDataList[position].lines.size > 0) {
            if (mOriginDataList[position].lines[0].sequence != PagerScrollAdapter.HEADER_ITEM_TYPE
                    && mOriginDataList[position].lines[0].sequence != PagerScrollAdapter.FOOTER_ITEM_TYPE
                    && mOriginDataList[position].lines[0].sequence != PagerScrollAdapter.AD_ITEM_TYPE) {
                mReadInfo.mReadStatus.chapterName = mOriginDataList[position].lines[0].chapterName
                novel_title.text = mReadInfo.mReadStatus.chapterName
                novel_chapter.text = "${mOriginDataList[position].lines[0].sequence + 1} / ${mReadInfo.mReadStatus.chapterCount} 章"
                novel_page.text = "本章第${(getCurrentChapterPage(position) + 1)} / ${getCurrentChapterPageCount(mOriginDataList[position].lines[0].sequence)}"

                mReadInfo.mReadStatus.currentPage = getCurrentChapterPage(position)
                mReadInfo.mReadStatus.sequence = mOriginDataList[position].lines[0].sequence
                mCurrentPage = mReadInfo.mReadStatus.currentPage
            }

//            mOnReaderViewControlCallback?.onPageChange(mReadInfo.mReadStatus.sequence, mReadInfo.mReadStatus.currentPage)
//
//            // 建议在RecycleView停止滚动时更新视图，目前暂时先放在这 暂无其他来源
//            if (mOriginDataList[position][0].isLastPage) {
//                mAdapter.addAdViewToChapterLastPage(mOnReaderViewControlCallback?.onLoadChapterLastPageAdView(mReadStatus.sequence, mReadStatus.currentPage), position)
//            }
        }
        if (mReadInfo.mReadStatus.sequence == -1) {
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
                if (i == chapterContent.size - 1) {
                    content.isLastPage = true
                }
                content.chapterName = chapterName
                content.sequence = sequence
            }
        }
    }

    /**
     * 添加章节间广告
     */
    private fun addChapterBetweenAdView(chapterContent: ArrayList<NovelPageBean>, sequence: Int, sequence2: Int) {
//        if (mOnReaderViewControlCallback != null &&
//                mOnReaderViewControlCallback?.onLoadChapterBetweenAdView(sequence, sequence2) != null) {
//            chapterContent.addAll(arrayListOf(arrayListOf(NovelLineBean().apply { setSequence(PagerScrollAdapter.AD_ITEM_TYPE) })))
//            mAdapter.addAdViewToChapterBetween(mOnReaderViewControlCallback?.onLoadChapterBetweenAdView(sequence, sequence2))
//        }
    }

    /**
     * 添加章节间广告
     */
    private fun addChapterBetweenAdView(sequence: Int, sequence2: Int) {
//        if (mOnReaderViewControlCallback != null &&
//                mOnReaderViewControlCallback?.onLoadChapterBetweenAdView(sequence, sequence2) != null) {
//            mOriginDataList.addAll(arrayListOf(arrayListOf(NovelLineBean().apply { setSequence(PagerScrollAdapter.AD_ITEM_TYPE) })))
//            mAdapter.addAdViewToChapterBetween(mOnReaderViewControlCallback?.onLoadChapterBetweenAdView(sequence, sequence2))
//        }
    }

    /**
     * 添加段末广告 8-1
     */
    private fun addAdViewToChapterLastPage(chapterContent: List<NovelPageBean>) {
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

        mDataProvider.loadAd(context, object : DataProvider.OnLoadReaderAdCallback {
            override fun onLoadAd(adView: ViewGroup) {
                lineData?.adView = adView
//                mAdapter.addAdViewToChapterLastPage(adView, 0)
            }
        })
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
//        if (mReadInfo.mReadStatus.isMenuShow) {
//            mMenuCallBack?.onShowMenu(false)
//            return false
//        }
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
//                        if (mReadDataFactory is ReadDataFactory) {
//                            (mReadDataFactory as ReadDataFactory).gotoOver()
//                        }
                    }
                    // 顶部
                } else if (mLastY - event.y < -20) {
                    if (mReadInfo.mReadStatus.sequence == -1) {
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
                loadPreChapter(mReadInfo.mReadStatus.sequence - 1)
            }
            PagerScrollAdapter.FOOTER_ITEM_TYPE -> {
                loadNextChapter(mReadInfo.mReadStatus.sequence + 1)
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
//            mMenuCallBack?.onShowMenu(true)
//            mOnReaderViewControlCallback?.onCenterClick()
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
        }
    }


    override fun freshTime(time: CharSequence?) {
        novel_time.text = time
    }

    override fun freshBattery(percent: Float) {
        novel_content_battery_view.setBattery(percent)
    }

    override fun setBackground(background: Int) {
        setBackground()
    }

    override fun setLoadChapter(msg: Int, chapter: Chapter, chapterList: ArrayList<ArrayList<NovelLineBean>>?) {
    }

    override fun setLoadAd(view: View) {

    }


    override fun setReadInfo(mReadInfo: ReadInfo?) {
    }

    private fun setBackground() {
        ThemeUtil.getModePrimaryBackground(resources, this)
        novel_time.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        origin_tv.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        trans_coding_tv.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        novel_time.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        novel_page.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        novel_chapter.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        novel_title.setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
        mAdapter.setTextColor(resources.getColor(ThemeUtil.modeLoadTextColor))
        novel_content_battery_view.invalidate()
    }

    private var mReadPageChange: IReadPageChange? = null

    override fun setIReadPageChange(mReadPageChange: IReadPageChange?) {
        this.mReadPageChange = mReadPageChange
    }
}