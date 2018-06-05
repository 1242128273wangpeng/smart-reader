package com.dy.reader.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.ding.basic.bean.Chapter
import com.dy.reader.R
import com.dy.reader.ReadMediaManager
import com.dy.reader.helper.ReadSeparateHelper
import com.dy.reader.mode.NovelLineBean
import com.dy.reader.page.PageContentView
import com.dy.reader.page.SpacingTextView
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import com.dycm_adsdk.PlatformSDK
import com.intelligent.reader.read.mode.NovelPageBean
import net.lzbook.kit.constants.Constants

import net.lzbook.kit.data.bean.ReadViewEnums
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.ArrayList

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

    private val mLoadedChapter = LinkedList<ArrayList<NovelPageBean>>()

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
                HEADER_ITEM_TYPE -> LoadViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_reader_loading, parent, false), HEADER_ITEM_TYPE)
                FOOTER_ITEM_TYPE -> LoadViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_reader_loading, parent, false), FOOTER_ITEM_TYPE)
                BOOK_HOME_ITEM_TYPE -> HomePagerHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_reader_cover, parent, false))
                AD_ITEM_TYPE -> AdViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_reader_ad, parent, false))
                else -> PagerHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_reader_content, parent, false))
            }

    override fun onBindViewHolder(holder: PagerScrollAdapter.ReaderPagerHolder, position: Int) = holder.bindHolder(chapterList[position])

    override fun getItemCount() = chapterList.size

    override fun getItemViewType(position: Int): Int = if (!TextUtils.isEmpty(chapterList[position].adType) && if (ReaderSettings.instance.isLandscape) true else !chapterList[position].isLastPage) {
        AD_ITEM_TYPE
    } else if (chapterList[position].lines != null && chapterList[position].lines.size != 0) {
        chapterList[position].lines[0].sequence
    } else 0


    fun setChapter(sequence: Int, data: CopyOnWriteArrayList<NovelPageBean>) {
        mLoadedChapter.clear()
        chapterList = data
        mLoadedChapter.add(ArrayList(data))
        showHeaderView(true)
        showFootView(true)
        notifyDataSetChanged()
    }

    private fun addAllChapter(location: Int, data: ArrayList<NovelPageBean>): Int {
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
            mLoadedChapter.add(0, data)
        }
        return addAllChapter(headerViewList.size, data)
    }

    fun addNextChapter(sequence: Int, data: java.util.ArrayList<NovelPageBean>): Int {
        mLoadedChapter.add(data)
        return addAllChapter(chapterList.size - footViewList.size, data)
    }

    /**
     * 清理超过阅读范围的数据，缓解内存过高
     */
    fun clearUselessChapter(type: ReadViewEnums.PageIndex) {
        if (mLoadedChapter.size > CLEAR_USERLESS_MAX_SIZE) {

            if (type == ReadViewEnums.PageIndex.previous) {
//                val lastData = getAllData().filter { (it.lines.size == 0 || it.lines.size > 0 && it.lines[0].sequenceType != HEADER_ITEM_TYPE && it.lines[0].sequenceType != FOOTER_ITEM_TYPE) }
//                        .filter { it.lines.size == 0 || it.lines.size > 0 && it.lines[0].sequenceType == mLoadedChapter.last() }
                mLoadedChapter.removeLast().apply {
                    Log.d(this.javaClass.simpleName, "AllData size remove : " + this.size)
                    getAllData().removeAll(this)

                    notifyItemRangeRemoved(chapterList.size - footViewList.size, this.size)
                }
            } else {
//                val firstData = getAllData().filter { (it.lines.size > 0 && it.lines[0].sequenceType != HEADER_ITEM_TYPE && it.lines[0].sequenceType != FOOTER_ITEM_TYPE) }
//                        .filter { it.lines.size > 0 && it.lines[0].sequenceType == mLoadedChapter.first() }
                mLoadedChapter.removeFirst().apply {

                    Log.d(this.javaClass.simpleName, "AllData size remove : " + this.size)
                    getAllData().removeAll(this)

                    notifyItemRangeRemoved(headerViewList.size, this.size)
                }
            }
            Log.d(this.javaClass.simpleName, "AllData size mLoadedChapter : " + mLoadedChapter.size)
            Log.d(this.javaClass.simpleName, "AllData size chapterList : " + getAllData().size)
        }
    }

    private fun addAllChapter(data: ArrayList<NovelPageBean>) {
        val lastIndex = chapterList.size
        chapterList.addAll(data)
        notifyItemRangeInserted(lastIndex, data.size)
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
        mLoadedChapter.clear()
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
            text = itemView.findViewById(R.id.pcv_reader_content) as PageContentView
            fl_reader_content_ad = itemView.findViewById(R.id.fl_reader_content_ad) as FrameLayout
        }

        override fun bindHolder(pageLines: NovelPageBean) {
            addAdView(pageLines)
            text.setContent(pageLines)
        }

        private fun addAdView(page: NovelPageBean) {
            if (Constants.isHideAD) {
                fl_reader_content_ad.visibility = View.GONE
                itemView.layoutParams.height = page.height.toInt()
                return
            }

            fl_reader_content_ad.removeAllViews()
            if (page.isLastPage) {//6-3
                val adView = ReadMediaManager.adCache.get(page.adType)

                adView?.view?.apply {

                    val map = HashMap<String, String>()
                    map.put("book_id", ReaderStatus.book.book_id)
                    map.put("book_source_id",ReaderStatus.book.book_source_id)
                    map.put("chapter_id", ReaderStatus.chapterId)

                    if ("api.qingoo.cn".equals(ReaderStatus.book.host, true)
                            || "open.qingoo.cn".equals(ReaderStatus.book.host, true)) {
                        map.put("channel_code", "A001")
                    } else {
                        map.put("channel_code", "A002")
                    }

                    PlatformSDK.config().setExpandInfo(map)

                    fl_reader_content_ad.visibility = View.VISIBLE
                    val adViewLayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    if (this.parent != null) {
                        (this.tag as ViewGroup).removeAllViews()
                    }
                    this.tag = fl_reader_content_ad
                    fl_reader_content_ad.alpha = if (ReaderSettings.instance.readThemeMode == 61) 0.5f else 1f
                    fl_reader_content_ad.addView(this, adViewLayoutParams)
                    itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            } else {
                fl_reader_content_ad.visibility = View.GONE
                itemView.layoutParams.height = page.height.toInt()
            }
        }

        private fun getAdView(): View = fl_reader_content_ad

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
            book_name_tv = itemView.findViewById(R.id.txt_reader_book) as TextView
            book_auth_tv = itemView.findViewById(R.id.txt_reader_author) as TextView
            slogan_tv = itemView.findViewById(R.id.txt_reader_slogan) as SpacingTextView
            product_name_tv = itemView.findViewById(R.id.txt_reader_product) as SpacingTextView
        }

        override fun bindHolder(pageLines: NovelPageBean) {
            book_name_tv.text = ReaderStatus.book.name
            book_auth_tv.text = ReaderStatus.book.author
            slogan_tv.setTextView(2f, context.resources.getString(R.string.reader_slogan))
            product_name_tv.setTextView(1f, context.resources.getString(R.string.application_name))

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
            txt_reader_loading_sequence = itemView.findViewById(R.id.txt_reader_loading_sequence) as TextView
            txt_reader_loading_chapter = itemView.findViewById(R.id.txt_reader_loading_chapter) as TextView
            txt_reader_loading_prompt = itemView.findViewById(R.id.txt_reader_loading_prompt) as TextView
            btn_reader_loading_refresh = itemView.findViewById(R.id.btn_reader_loading_refresh) as Button
            pgbar_reader_loading_progress = itemView.findViewById(R.id.pgbar_reader_loading_progress) as ProgressBar
        }

        override fun bindHolder(pageLines: NovelPageBean) {
            txt_reader_loading_prompt.setTextColor(textColor)
            txt_reader_loading_chapter.setTextColor(textColor)
            txt_reader_loading_sequence.setTextColor(textColor)

            val loadSequence = if (type == HEADER_ITEM_TYPE) {
                ReaderStatus.position.group - 1
            } else {
                ReaderStatus.position.group + 1
            }

            if (allChapterList?.size ?: 0 > loadSequence) {
                allChapterList?.let {
                    ReadSeparateHelper.getChapterNameList(it[loadSequence].name ?: "").forEachIndexed { index, novelLineBean ->
                        if (index == 0) {
                            txt_reader_loading_sequence.text = novelLineBean.lineContent
                        } else {
                            txt_reader_loading_chapter.text = novelLineBean.lineContent
                        }
                    }
                }
            }

            btn_reader_loading_refresh.setOnClickListener {
                if (pageLines.lines.size > 0) {
                    mOnLoadViewClickListener?.onLoadViewClick(pageLines.lines[0].sequence)
                    setLoadingState()
                }
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
            pgbar_reader_loading_progress.visibility = View.VISIBLE
            btn_reader_loading_refresh.visibility = View.GONE
            txt_reader_loading_prompt.text = context.resources.getString(R.string.loading_read_page)
        }

        private fun setErrorState() {
            pgbar_reader_loading_progress.visibility = View.GONE
            btn_reader_loading_refresh.visibility = View.VISIBLE
            txt_reader_loading_prompt.text = context.resources.getString(R.string.loading_fail_load_page)
        }
    }

    /**
     * AD
     */
    internal inner class AdViewHolder(itemView: View) : PagerScrollAdapter.ReaderPagerHolder(itemView) {

        init {
            fl_reader_content_ad = itemView.findViewById(R.id.fl_reader_content_ad) as FrameLayout
        }

        override fun bindHolder(page: NovelPageBean) {
            fl_reader_content_ad.removeAllViews()
            if (!Constants.isHideAD && !TextUtils.isEmpty(page.adType)) {

                val map = HashMap<String, String>()
                map.put("book_id", ReaderStatus.book.book_id)
                map.put("book_source_id",ReaderStatus.book.book_source_id)
                map.put("chapter_id", ReaderStatus.chapterId)

                if ("api.qingoo.cn".equals(ReaderStatus.book.host, true)
                        || "open.qingoo.cn".equals(ReaderStatus.book.host, true)) {
                    map.put("channel_code", "A001")
                } else {
                    map.put("channel_code", "A002")
                }

                PlatformSDK.config().setExpandInfo(map)

                val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val adView = ReadMediaManager.adCache.get(page.adType)
                adView?.view?.apply {
                    if (this.parent != null) {
                        (this.tag as ViewGroup).removeAllViews()
                    }
                    if (this.parent == null) {
                        this.tag = fl_reader_content_ad
                        fl_reader_content_ad.alpha = if (ReaderSettings.instance.readThemeMode == 61) 0.5f else 1f
                    }
                    fl_reader_content_ad.addView(this, layoutParams)
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

        lateinit var pgbar_reader_loading_progress: ProgressBar
        lateinit var txt_reader_loading_prompt: TextView
        lateinit var txt_reader_loading_sequence: TextView
        lateinit var txt_reader_loading_chapter: TextView
        lateinit var btn_reader_loading_refresh: Button

        lateinit var fl_reader_content_ad: FrameLayout

//        init {
//            itemView.layoutParams = RecyclerView.LayoutParams(AppHelper.screenWidth, AppHelper.screenHeight)
//        }
        abstract fun bindHolder(pageLines: NovelPageBean)
    }

    interface OnLoadViewClickListener {
        fun onLoadViewClick(type: Int)
    }
}

