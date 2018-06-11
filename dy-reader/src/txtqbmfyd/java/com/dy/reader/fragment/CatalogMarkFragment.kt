package com.dy.reader.fragment
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter
import com.ding.basic.util.DataCache
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dy.reader.R
import com.dy.reader.adapter.BaseRecyclerHolder
import com.dy.reader.adapter.ListRecyclerAdapter
import com.dy.reader.presenter.CatalogMark
import com.dy.reader.presenter.CatalogMarkPresenter
import com.dy.reader.setting.ReaderStatus
import kotlinx.android.synthetic.txtqbmfyd.item_read_bookmark.view.*
import kotlinx.android.synthetic.txtqbmfyd.item_read_catalog.view.*
import kotlinx.android.synthetic.txtqbmfyd.read_catalog_mark_layout.*
import kotlinx.android.synthetic.txtqbmfyd.read_catalog_mark_layout.view.*
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.repair_books.RepairHelp
import net.lzbook.kit.utils.StatServiceUtils
import java.text.SimpleDateFormat
import java.util.concurrent.Callable

/**
 * Created by xian on 2017/8/17
 */
class CatalogMarkFragment : Fragment(), CatalogMark.View {

    val presenter: CatalogMark.Presenter by lazy {
        CatalogMarkPresenter(this)
    }

    var chapterList = mutableListOf<Chapter>()
    var bookMarkList = mutableListOf<Bookmark>()
    var reverse = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.read_catalog_mark_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dividerCatalog = ShapeItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL)
        val dividerBookmark = ShapeItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL)

        dividerCatalog.setDrawable(ColorDrawable(Color.parseColor("#0c000000")))
        dividerBookmark.setDrawable(ColorDrawable(Color.parseColor("#0c000000")))

        catalog_main.addItemDecoration(dividerCatalog)
        bookmark_main.addItemDecoration(dividerBookmark)

        val catalogAdapter = ListRecyclerAdapter(chapterList, R.layout.item_read_catalog, ChapterHolder::class.java)
        catalogAdapter.itemClick = View.OnClickListener { v ->
            presenter.gotoChapter(requireActivity(), v.tag as Chapter)
        }
        catalog_main.adapter = catalogAdapter
        catalog_main.layoutManager = object : LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false) {
            override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
                super.onLayoutChildren(recycler, state)
                val firstVisibleItemPosition = findFirstVisibleItemPosition()
                if (firstVisibleItemPosition != 0) {
                    if (firstVisibleItemPosition == -1)
                        catalog_fastscroller.visibility = View.GONE
                    return
                }
                val lastVisibleItemPosition = findLastVisibleItemPosition()
                val itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1
                catalog_fastscroller.visibility = if (catalogAdapter.itemCount > itemsShown) View.VISIBLE else View.GONE
            }
        }
        catalog_fastscroller.setRecyclerView(catalog_main)
        catalog_fastscroller.setViewsToUse(R.layout.reader_recycler_view_scroller, R.id.img_recycler_view_scroller)

        val bookmarkAdapter = ListRecyclerAdapter(bookMarkList, R.layout.item_read_bookmark, BookMarkHolder::class.java)
        bookmarkAdapter.itemClick = View.OnClickListener { v ->
            when (v.id) {
                R.id.bookmark_content_layout -> {
                    presenter.gotoBookMark(requireActivity(), v.tag as Bookmark)
                }
                R.id.item_bookmark_delete -> {
                    presenter.deleteBookMark(requireActivity(), v.tag as Bookmark)
                }
            }
        }

        bookmark_main.adapter = bookmarkAdapter
        bookmark_main.layoutManager = object : LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false) {
            override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
                super.onLayoutChildren(recycler, state)
                val firstVisibleItemPosition = findFirstVisibleItemPosition()
                if (firstVisibleItemPosition != 0) {
                    if (firstVisibleItemPosition == -1)
                        bookmark_fastscroller.visibility = View.GONE
                    return
                }
                val lastVisibleItemPosition = findLastVisibleItemPosition()
                val itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1
                bookmark_fastscroller.visibility = if (bookmarkAdapter.itemCount > itemsShown) View.VISIBLE else View.GONE
            }
        }
        bookmark_fastscroller.setRecyclerView(bookmark_main)
        bookmark_fastscroller.setViewsToUse(R.layout.reader_recycler_view_scroller, R.id.img_recycler_view_scroller)

        initListener()
    }

    private fun initListener() {
        iv_fixbook.setOnClickListener {
            RepairHelp.fixBook(activity, presenter?.getBook(), {
                if (activity != null && !requireActivity().isFinishing) {
                    try {
                        RouterUtil.navigation(requireActivity(), RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
                        requireActivity().finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
            presenter.onClickFixBook(requireActivity())
        }

        read_rg_catlog_mark.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.tab_catalog -> {
                    tv_catalog_novel_sort.visibility = View.VISIBLE
                    presenter.loadCatalog(reverse)
                }
                R.id.tab_bookmark -> {
                    tv_catalog_novel_sort.visibility = View.GONE
                    presenter.loadBookMark(requireActivity(), 1)//用于标识只有为1的时候才打点书签
                }
                else -> {

                }
            }
        }

        tv_catalog_novel_sort.setOnClickListener {
            reverse = !reverse
//            var sortIcon = TypedValue()//背景色
            if (reverse) {
//                activity.iyouqu.theme.resolveAttribute(R.attr.directory_sort_positive, sortIcon, true)
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_catalog_click_dx_btn)
            } else {
//                activity.iyouqu.theme.resolveAttribute(R.attr.directory_sort_negative, sortIcon, true)
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_catalog_click_zx_btn)
            }
//            iv_catalog_novel_sort.setImageResource(sortIcon.resourceId)
            tv_catalog_novel_sort.setText(if (!reverse) R.string.catalog_negative else R.string.catalog_positive)
            presenter.loadCatalog(reverse)
        }
    }


    fun loadData() {
        if (catalog_main.visibility == View.VISIBLE) {
            presenter.loadCatalog(reverse)
        } else {
            presenter.loadBookMark(requireActivity(), 2)
        }
    }

    fun fixBook() {
        if (RepairHelp.isShowFixBtn(activity, ReaderStatus.book.book_id)) {
            iv_fixbook.visibility = View.VISIBLE
        } else {
            iv_fixbook.visibility = View.GONE
        }
    }

    private var dataLoaded: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun showCatalog(chapters: List<Chapter>, sequence: Int) {
        catalog_main.visibility = View.VISIBLE
        catalog_fastscroller.visibility = View.VISIBLE
        bookmark_main.visibility = View.GONE
        bookmark_fastscroller.visibility = View.GONE
        rl_layout_empty_online.visibility = View.GONE
        loadingPage?.onSuccess()

        catalog_novel_name.setText(ReaderStatus.book.name)

        dataLoaded = true

        chapterList.clear()
        chapterList.addAll(chapters)

        catalog_chapter_count.text = "共${chapterList.size}章"

        catalog_main.adapter.notifyDataSetChanged()
        catalog_main.scrollToPosition(sequence)
    }

    override fun showMark(marks: List<Bookmark>) {
        catalog_main.visibility = View.GONE
        catalog_fastscroller.visibility = View.GONE
        bookmark_main.visibility = View.VISIBLE
        bookmark_fastscroller.visibility = View.VISIBLE
        loadingPage?.onSuccess()
        if (marks.isEmpty()) {
            rl_layout_empty_online.visibility = View.VISIBLE
        } else {
            rl_layout_empty_online.visibility = View.GONE
        }
        bookMarkList.clear()
        bookMarkList.addAll(marks)
        bookmark_main.adapter.notifyDataSetChanged()

    }

    private var loadingPage: LoadingPage? = null

    override fun setChangeAble(enable: Boolean) {
        view?.tab_bookmark?.isClickable = enable
        view?.tab_catalog?.isClickable = enable
    }

    override fun onLoading() {
        loadingPage?.onSuccess()
        if (!dataLoaded) {
            loadingPage = LoadingPage(activity, view as FrameLayout)
            loadingPage!!.setCustomBackgroud()
            loadingPage!!.setReloadAction(Callable<Void> {
                presenter.loadCatalog(false)
                null
            })
        }
    }

    override fun onNetError() {
        loadingPage?.onError()
    }

    /**
     * 章节Holder
     */
    class ChapterHolder(itemView: View?) : BaseRecyclerHolder<Chapter>(itemView) {
        override fun onBindData(position: Int, chapter: Chapter, editMode: Boolean) {
            if (itemView != null) {
                itemView.tag = chapter
                itemView.isClickable = true
                itemView.setOnClickListener { v ->
                    onItemClick?.onClick(v)
                }

//                val txt = (itemView as TextView)
//
//                txt.text = "${chapter.chapter_name}"

                var chapterExist = false

                chapterExist = DataCache.isChapterCached(chapter)


                var txtColor = 0
                if (chapterExist) {
//                    txtColor = R.color.reading_setting_text_color
//                    txtColor = R.color.read_item_catalog_chapter_text_color
                    itemView.chapter_cache_tv.visibility = View.VISIBLE
                } else {
//                    txtColor = R.color.read_item_catalog_uncached_chapter_text_color
//                    txtColor = R.color.reading_setting_text_color
                    itemView.chapter_cache_tv.visibility = View.GONE
                }
                if (chapter.name?.equals(ReaderStatus.chapterName) == true) {
                    txtColor = R.color.dialog_recommend
                } else {
                    txtColor = R.color.reading_setting_text_color
                }

                (itemView.chapter_name_tv as TextView).text = chapter.name
                (itemView.chapter_name_tv as TextView).setTextColor(itemView.context.resources.getColor(txtColor))
            }
        }
    }

    /**
     * 书签Holder
     */
    class BookMarkHolder(itemView: View?) : BaseRecyclerHolder<Bookmark>(itemView) {

        @SuppressLint("SimpleDateFormat")
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd  HH:mm")

        override fun onBindData(position: Int, data: Bookmark, editMode: Boolean) {
            if (itemView != null) {
                itemView.tag = data

                itemView.isClickable = true
                itemView.setOnClickListener { v ->
                    onItemClick?.onClick(v)
                }
                itemView.setOnLongClickListener { v ->
                    onItemLongClick?.onLongClick(v) ?: false
                }

                itemView.item_bookmark_delete.tag = data
                itemView.item_bookmark_delete.setOnClickListener { v ->
                    onItemClick?.onClick(v)
                }

                (itemView.item_bookmark_title as TextView).text = "${data.chapter_name}"
                (itemView.item_bookmark_desc as TextView).text = "${data.chapter_content}"
                (itemView.item_bookmark_time as TextView).text = dateFormat.format(data.insert_time)
            }
        }
    }
}