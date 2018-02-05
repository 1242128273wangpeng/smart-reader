package com.intelligent.reader.read.page

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.dycm_adsdk.PlatformSDK
import com.intelligent.reader.R
import com.intelligent.reader.read.help.ReadSeparateHelper
import com.intelligent.reader.read.mode.NovelPageBean
import com.intelligent.reader.read.mode.ReadState
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.data.bean.ReadViewEnums
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @desc 阅读页展示
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/2 14:35
 */
class PagerScrollAdapter(val context: Context) : RecyclerView.Adapter<PagerScrollAdapter.ReaderPagerHolder>() {

    private var chapterList: CopyOnWriteArrayList<NovelPageBean>

    private var headerViewList: ArrayList<NovelPageBean>

    private var footViewList: ArrayList<NovelPageBean>

    private var allChapterList: ArrayList<Chapter>? = null

    private val mLoadedChapter = CopyOnWriteArrayList<Int>()

    private var textColor: Int = 0

    private val LAST_PAGE_EXTEND_HEIGHT = 300

    private val LAST_PAGE_AD_EXTEND_HEIGHT = 50

    private val AD_PORTRAIT_VIEW_HEIGHT = 600

    private val AD_LANDSCAPE_VIEW_HEIGHT = 800

    private val CLEAR_USERLESS_MAX_SIZE = 6

    // 书籍封面页
    private val BOOK_HOME_ITEM_TYPE = -1

    // 加载条状态
    companion object {
        val LOAD_VIEW_LOADING_STATE = 0
        val LOAD_VIEW_FAIL_STATE = 1

        val HEADER_ITEM_TYPE = 1000000
        val FOOTER_ITEM_TYPE = 2000000
        val AD_ITEM_TYPE = 3000000
    }

    private var loadViewStatus = LOAD_VIEW_LOADING_STATE

    private var headerViewIsShow = true

    private var footViewIsShow = true

    private var mOnLoadViewClickListener: OnLoadViewClickListener? = null

    init {
        chapterList = CopyOnWriteArrayList()
        headerViewList = arrayListOf(NovelPageBean(arrayListOf(NovelLineBean().apply { sequence = HEADER_ITEM_TYPE;sequenceType = HEADER_ITEM_TYPE }), 0, ArrayList()))
        footViewList = arrayListOf(NovelPageBean(arrayListOf(NovelLineBean().apply { sequence = FOOTER_ITEM_TYPE;sequenceType = FOOTER_ITEM_TYPE }), 0, ArrayList()))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                HEADER_ITEM_TYPE -> LoadViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.read_load_item_layout, parent, false), HEADER_ITEM_TYPE)
                FOOTER_ITEM_TYPE -> LoadViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.read_load_item_layout, parent, false), FOOTER_ITEM_TYPE)
                BOOK_HOME_ITEM_TYPE -> HomePagerHolder(LayoutInflater.from(parent.context).inflate(R.layout.book_home_page_layout, parent, false))
                AD_ITEM_TYPE -> AdViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.ad_page_item_layout, parent, false))
                else ->
                    PagerHolder(LayoutInflater.from(parent.context).inflate(R.layout.page_content_item_layout, parent, false))
            }

    override fun onBindViewHolder(holder: PagerScrollAdapter.ReaderPagerHolder, position: Int) {
        holder.bindHolder(chapterList[position])
    }

    override fun getItemCount() = chapterList.size

    override fun getItemViewType(position: Int) = chapterList[position].lines[0].sequence


    fun setChapter(sequence: Int, data: CopyOnWriteArrayList<NovelPageBean>) {
        chapterList = data
        mLoadedChapter.add(sequence)
        showHeaderView(true)
        showFootView(true)
        notifyDataSetChanged()
    }

    fun addAllChapter(location: Int, data: ArrayList<NovelPageBean>): Int {
        if (chapterList.size == 0) return -1
        (chapterList.addAll(location, data))
        notifyItemRangeInserted(location, data.size)
        return data.size
    }

    private fun removeChapter(location: Int, data: ArrayList<NovelPageBean>) {
        chapterList.removeAll(data)
        notifyItemRangeRemoved(location, data.size)
    }

    fun addPreChapter(sequence: Int, data: java.util.ArrayList<NovelPageBean>): Int {
        if (sequence != -1) {
            mLoadedChapter.add(0, sequence)
        }
        return addAllChapter(headerViewList.size, data)
    }


    fun addNextChapter(sequence: Int, data: java.util.ArrayList<NovelPageBean>): Int {
        mLoadedChapter.add(sequence)
        return addAllChapter(chapterList.size - footViewList.size, data)
    }

    /**
     * 清理超过阅读范围的数据，缓解内存过高
     */
    fun clearUselessChapter(type: ReadViewEnums.PageIndex) {
        if (mLoadedChapter.size > CLEAR_USERLESS_MAX_SIZE) {
            val lastData = getAllData().filter { (it.lines[0].sequenceType != HEADER_ITEM_TYPE && it.lines[0].sequenceType != FOOTER_ITEM_TYPE) }
                    .filter { it.lines[0].sequenceType == mLoadedChapter.last() }
            if (type == ReadViewEnums.PageIndex.previous) {
                getAllData().removeAll(lastData)
                clearUserlessAdView(lastData)
                notifyItemRangeRemoved(chapterList.size - footViewList.size, lastData.size)
                mLoadedChapter.remove(mLoadedChapter.last())
            } else {
                val firstData = getAllData().filter { (it.lines[0].sequenceType != HEADER_ITEM_TYPE && it.lines[0].sequenceType != FOOTER_ITEM_TYPE) }
                        .filter { it.lines[0].sequenceType == mLoadedChapter.first() }
                getAllData().removeAll(firstData)
                clearUserlessAdView(firstData)
                notifyItemRangeRemoved(headerViewList.size, firstData.size)
                mLoadedChapter.remove(mLoadedChapter.first())
            }
        }
    }

    private fun clearUserlessAdView(userlessChapter: List<NovelPageBean>) {
        for (i in userlessChapter.indices) {
            val pageContent = userlessChapter[i]
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
    }

    private fun addAllChapter(data: ArrayList<NovelPageBean>) {
        val lastIndex = chapterList.size
        chapterList.addAll(data)
        notifyItemRangeInserted(lastIndex, data.size)
    }

    fun addAdViewToTheChapterLast(sequence: Int, adData: NovelPageBean) {
        var sequenceIndex = 0
        foo@ for (i in getAllData().indices) {
            if (getAllData()[i].lines[0].sequenceType == sequence) {
                sequenceIndex = i
                break@foo
            }
        }
        val addAdViewIndex = sequenceIndex + getAllData().filter { it.lines[0].sequenceType == sequence }.size
        getAllData().add(addAdViewIndex, adData)
        notifyItemRangeInserted(addAdViewIndex, 1)
    }

    fun removeViewToTheChapterLast(sequence: Int) {
        var sequenceIndex = 0
        foo@ for (i in getAllData().indices) {
            if (getAllData()[i].lines[0].sequenceType == sequence) {
                sequenceIndex = i
                break@foo
            }
        }
        val removeAdViewIndex = sequenceIndex + getAllData().filter { it.lines[0].sequenceType == sequence }.size - 1
        if (getAllData()[removeAdViewIndex].lines[0].sequence == AD_ITEM_TYPE) {
            getAllData().removeAt(removeAdViewIndex)
            notifyItemRangeRemoved(removeAdViewIndex, 1)
        }
    }

    fun showHeaderView(show: Boolean) {
        if (show) {
            addAllChapter(0, headerViewList)
        } else {
            removeChapter(0, headerViewList)
        }
        headerViewIsShow = show
    }

    fun showFootView(show: Boolean) {
        if (show) {
            addAllChapter(footViewList)
        } else {
            removeChapter(chapterList.size, footViewList)
        }
        footViewIsShow = show
    }

    fun clearData() {
        if (chapterList.size > 0) chapterList.clear()
        notifyDataSetChanged()
    }

    fun getAllData(): CopyOnWriteArrayList<NovelPageBean> = chapterList

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
        notifyDataSetChanged()
    }

    fun setLoadViewState(state: Int) {
        loadViewStatus = state
        if (headerViewIsShow) notifyItemRangeChanged(0, headerViewList.size)
        if (footViewIsShow) notifyItemRangeChanged(chapterList.size - footViewList.size, footViewList.size)
    }

    fun setOnLoadViewClickListener(onLoadViewClickListener: OnLoadViewClickListener) {
        this.mOnLoadViewClickListener = onLoadViewClickListener
    }

    fun setChapterCatalog(chapterListL: java.util.ArrayList<Chapter>?) {
        allChapterList = chapterListL
    }

    /**
     * 正文Holder
     */
    internal inner class PagerHolder(itemView: View) : PagerScrollAdapter.ReaderPagerHolder(itemView) {
        init {
            text = itemView.findViewById(R.id.read_content_text) as PageContentView
            ad_fl = itemView.findViewById(R.id.ad_fl) as FrameLayout
        }

        override fun bindHolder(pageLines: NovelPageBean) {
            addAdView(pageLines)
            text.setContent(pageLines)
        }

        private fun addAdView(pageLines: NovelPageBean) {

            val lineData = pageLines.lines[pageLines.lines.size - 1]
            ad_fl.removeAllViews()

            if (pageLines.isLastPage) {
                if (lineData.adView != null) {
                    ad_fl.visibility = View.VISIBLE
                    val adViewLayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            if (ReadConfig.IS_LANDSCAPE) AD_LANDSCAPE_VIEW_HEIGHT else AD_PORTRAIT_VIEW_HEIGHT)

                    if (lineData.adView.parent != null) {
                        (lineData.adView.tag as ViewGroup).removeAllViews()
                    }
                    lineData.adView.tag = ad_fl
                    ad_fl.addView(lineData.adView, adViewLayoutParams)
                    itemView.layoutParams.height = (pageLines.height + adViewLayoutParams.height).toInt() + LAST_PAGE_AD_EXTEND_HEIGHT
                } else {
                    itemView.layoutParams.height = pageLines.height.toInt() + LAST_PAGE_EXTEND_HEIGHT
                }

            } else {
                ad_fl.visibility = View.GONE
                itemView.layoutParams.height = pageLines.height.toInt()
            }
        }

        private fun getAdView(): View = ad_fl

        /**
         * 事件触发是否在广告视图范围内
         */
        fun singleTapUpIsInside(evX: Int, evY: Int): Boolean {
            val location = IntArray(2)
            getAdView().getLocationOnScreen(location)
            val left = location[0]
            val right = left + getAdView().width
            val top = location[1]
            val bottom = top + getAdView().height
            return evX in left..right && top <= evY && bottom >= evY
        }
    }

    /**
     * 书籍封面Holder
     */
    internal inner class HomePagerHolder(itemView: View) : PagerScrollAdapter.ReaderPagerHolder(itemView) {

        init {
            book_name_tv = itemView.findViewById(R.id.book_name_tv) as TextView
            book_auth_tv = itemView.findViewById(R.id.book_auth_tv) as TextView
            slogan_tv = itemView.findViewById(R.id.slogan_tv) as SpacingTextView
            product_name_tv = itemView.findViewById(R.id.product_name_tv) as SpacingTextView
        }

        override fun bindHolder(pageLines: NovelPageBean) {
            book_name_tv.text = ReadState.book?.name
            book_auth_tv.text = ReadState.book?.author
            slogan_tv.setTextView(2f, context.resources.getString(R.string.slogan))
            product_name_tv.setTextView(1f, context.resources.getString(R.string.app_name))

            book_name_tv.setTextColor(textColor)
            book_auth_tv.setTextColor(textColor)
            slogan_tv.setTextColor(textColor)
            product_name_tv.setTextColor(textColor)
        }
    }

    /**
     * Load View
     */
    @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
    internal inner class LoadViewHolder(itemView: View, val type: Int) : PagerScrollAdapter.ReaderPagerHolder(itemView) {
        init {
            loading_progressbar = itemView.findViewById(R.id.loading_progressbar) as ProgressBar
            tv_loading_progress = itemView.findViewById(R.id.tv_loading_progress) as TextView
            load_chapter_name_tv = itemView.findViewById(R.id.load_chapter_name_tv) as TextView
            load_chapter_num_tv = itemView.findViewById(R.id.load_chapter_num_tv) as TextView
            loading_error_reload = itemView.findViewById(R.id.loading_error_reload) as Button
        }

        override fun bindHolder(pageLines: NovelPageBean) {
            tv_loading_progress.setTextColor(textColor)
            load_chapter_name_tv.setTextColor(textColor)
            load_chapter_num_tv.setTextColor(textColor)

            var loadSequence = 0
            if (type == HEADER_ITEM_TYPE) {
                loadSequence = ReadState.sequence - 1
            } else {
                loadSequence = ReadState.sequence + 1
            }

            if (allChapterList?.size ?: 0 > loadSequence) {
                allChapterList?.let {
                    ReadSeparateHelper.getChapterNameList(it[loadSequence].chapter_name).forEachIndexed { index, novelLineBean ->
                        if (index == 0) {
                            load_chapter_num_tv.text = novelLineBean.lineContent
                        } else {
                            load_chapter_name_tv.text = novelLineBean.lineContent
                        }
                    }
                }
            }

            loading_error_reload.setOnClickListener {
                mOnLoadViewClickListener?.onLoadViewClick(pageLines.lines[0].sequence)
                setLoadingState()
            }
            when (loadViewStatus) {
                LOAD_VIEW_LOADING_STATE -> {
                    setLoadingState()
                }
                LOAD_VIEW_FAIL_STATE -> {
                    setErrorState()
                }
            }
        }

        private fun setLoadingState() {
            loading_progressbar.visibility = View.VISIBLE
            loading_error_reload.visibility = View.GONE
            tv_loading_progress.text = context.resources.getString(R.string.loading_read_page)
        }

        private fun setErrorState() {
            loading_progressbar.visibility = View.GONE
            loading_error_reload.visibility = View.VISIBLE
            tv_loading_progress.text = context.resources.getString(R.string.loading_fail_load_page)
        }
    }

    /**
     * AD
     */
    internal inner class AdViewHolder(itemView: View) : PagerScrollAdapter.ReaderPagerHolder(itemView) {

        init {
            ad_fl = itemView.findViewById(R.id.ad_fl) as FrameLayout
        }

        override fun bindHolder(pageLines: NovelPageBean) {
            ad_fl.removeAllViews()

            if (pageLines.isAd && pageLines.adBigView != null) {
                val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                if (pageLines.adBigView!!.parent != null) {
                    (pageLines.adBigView!!.tag as ViewGroup).removeAllViews()
                }
                if (pageLines.adBigView!!.parent == null) {
                    pageLines.adBigView!!.tag = ad_fl
                    ad_fl.addView(pageLines.adBigView, layoutParams)
                }
            }
        }

    }

    abstract class ReaderPagerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var text: PageContentView

        lateinit var book_name_tv: TextView
        lateinit var book_auth_tv: TextView
        lateinit var slogan_tv: SpacingTextView
        lateinit var product_name_tv: SpacingTextView


        lateinit var loading_progressbar: ProgressBar
        lateinit var tv_loading_progress: TextView
        lateinit var load_chapter_num_tv: TextView
        lateinit var load_chapter_name_tv: TextView
        lateinit var loading_error_reload: Button


        lateinit var ad_fl: FrameLayout
        abstract fun bindHolder(pageLines: NovelPageBean)
    }

    interface OnLoadViewClickListener {
        fun onLoadViewClick(type: Int)
    }
}

