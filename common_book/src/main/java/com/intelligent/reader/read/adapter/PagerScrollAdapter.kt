package com.intelligent.reader.read.page

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.intelligent.reader.R
import com.intelligent.reader.read.help.NovelHelper
import com.intelligent.reader.read.mode.NovelPageBean
import net.lzbook.kit.data.bean.ReadViewEnums
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.data.bean.ReadStatus
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @desc 阅读页展示
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/2 14:35
 */
class PagerScrollAdapter(val context: Context, val mReadStatus: ReadStatus, val mNovelHelper: NovelHelper) : RecyclerView.Adapter<PagerScrollAdapter.ReaderPagerHolder>() {

    private var chapterList: CopyOnWriteArrayList<NovelPageBean>

    private var headerViewList: ArrayList<NovelPageBean>

    private var footViewList: ArrayList<NovelPageBean>

    private var allChapterList: ArrayList<Chapter>? = null

    private var textColor: Int = 0

    // 书籍封面页
    private val BOOK_HOME_ITEM_TYPE = -1

    // 加载条状态
    companion object {
        val LOAD_VIEW_LOADING_STATE = 0
        val LOAD_VIEW_FAIL_STATE = 1

        val HEADER_ITEM_TYPE = 100000
        val FOOTER_ITEM_TYPE = 200000
        val AD_ITEM_TYPE = 300000
    }

    private var loadViewStatus = LOAD_VIEW_LOADING_STATE

    private var headerViewIsShow = true

    private var footViewIsShow = true

    private var mOnLoadViewClickListener: OnLoadViewClickListener? = null

    init {
        chapterList = CopyOnWriteArrayList()
        headerViewList = arrayListOf(NovelPageBean(arrayListOf(NovelLineBean().apply { sequence = HEADER_ITEM_TYPE }), 0, ArrayList()))
        footViewList = arrayListOf(NovelPageBean(arrayListOf(NovelLineBean().apply { sequence = FOOTER_ITEM_TYPE }), 0, ArrayList()))
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
        holder.bindHolder(chapterList[position].lines)
    }

    override fun getItemCount() = chapterList.size

    override fun getItemViewType(position: Int) = chapterList[position].lines[0].sequence


    fun setChapter(data: CopyOnWriteArrayList<NovelPageBean>) {
        chapterList = data
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

    fun addPreChapter(data: java.util.ArrayList<NovelPageBean>): Int =
            addAllChapter(headerViewList.size, data)

    fun addNextChapter(data: java.util.ArrayList<NovelPageBean>): Int =
            addAllChapter(chapterList.size - footViewList.size, data)

    private fun addAllChapter(data: ArrayList<NovelPageBean>) {
        val lastIndex = chapterList.size
        chapterList.addAll(data)
        notifyItemRangeInserted(lastIndex, data.size)
    }

    fun getNotifyIndexByLoadChapter(index: ReadViewEnums.PageIndex, data: ArrayList<NovelPageBean>): Int =
            when (index) {
                ReadViewEnums.PageIndex.previous -> {
                    data.size
                }
                else -> {
                    chapterList.size - footViewList.size
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
            chapter_info_rl = itemView.findViewById(R.id.chapter_info_rl) as RelativeLayout
            chapter_num_tv = itemView.findViewById(R.id.chapter_num_tv) as TextView
            chapter_name_tv = itemView.findViewById(R.id.chapter_name_tv) as TextView
            ad_fl = itemView.findViewById(R.id.ad_fl) as FrameLayout
        }

        override fun bindHolder(pageLines: List<NovelLineBean>) {
            val pageTag = pageLines[0].lineContent
            if (!TextUtils.isEmpty(pageTag) &&
                    PageContentView.CHAPTER_HOME_PAGE == pageTag.trim()) {
                chapter_info_rl.visibility = View.VISIBLE
            } else {
                chapter_info_rl.visibility = View.GONE
            }

            mNovelHelper.getChapterNameList(pageLines[0].chapterName).forEachIndexed { index, novelLineBean ->
                if (index == 0) {
                    chapter_num_tv.text = novelLineBean.lineContent
                } else {
                    chapter_name_tv.text = novelLineBean.lineContent
                }
            }

            addAdView(pageLines)

            text.setReaderStatus(mReadStatus)
            text.setContent(pageLines)
            chapter_num_tv.setTextColor(textColor)
            chapter_name_tv.setTextColor(textColor)
            text.setTextColor(textColor)
        }

        private fun addAdView(pageLines: List<NovelLineBean>) {
            val lineData = pageLines[pageLines.size - 1]
            ad_fl.removeAllViews()
            if (pageLines[0].isLastPage && lineData.adView != null) {
                ad_fl.visibility = View.VISIBLE
                val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600)
                if (lineData.adView.parent != null) {
                    (lineData.adView.tag as ViewGroup).removeAllViews()
                }
                if (lineData.adView.parent == null) {
                    lineData.adView.tag = ad_fl
                    ad_fl.addView(lineData.adView, layoutParams)
                }
                itemView.layoutParams.height = mNovelHelper.getPageHeight(pageLines) + layoutParams.height
            } else {
                ad_fl.visibility = View.GONE
                itemView.layoutParams.height = mNovelHelper.getPageHeight(pageLines)
            }
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

        override fun bindHolder(pageLines: List<NovelLineBean>) {
            book_name_tv.text = mReadStatus.book.name
            book_auth_tv.text = mReadStatus.book.author
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

        override fun bindHolder(pageLines: List<NovelLineBean>) {
            tv_loading_progress.setTextColor(textColor)
            load_chapter_name_tv.setTextColor(textColor)
            load_chapter_num_tv.setTextColor(textColor)

            var loadSequence = 0
            if (type == HEADER_ITEM_TYPE) {
                loadSequence = mReadStatus.sequence - 1
            } else {
                loadSequence = mReadStatus.sequence + 1
            }
            if (allChapterList != null && allChapterList!!.size > 0) {
                mNovelHelper.getChapterNameList(allChapterList!![loadSequence].chapter_name).forEachIndexed { index, novelLineBean ->
                    if (index == 0) {
                        load_chapter_num_tv.text = novelLineBean.lineContent
                    } else {
                        load_chapter_name_tv.text = novelLineBean.lineContent
                    }
                }
            }
            loading_error_reload.setOnClickListener {
                mOnLoadViewClickListener?.onLoadViewClick(pageLines[0].sequence)
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

        override fun bindHolder(pageLines: List<NovelLineBean>) {
//                ad_fl.removeAllViews()
//                ad_fl.addView(mChapterBetweenAdView)
        }

    }

    abstract class ReaderPagerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var text: PageContentView
        lateinit var chapter_info_rl: RelativeLayout
        lateinit var chapter_num_tv: TextView
        lateinit var chapter_name_tv: TextView

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
        abstract fun bindHolder(pageLines: List<NovelLineBean>)
    }

    interface OnLoadViewClickListener {
        fun onLoadViewClick(type: Int)
    }
}