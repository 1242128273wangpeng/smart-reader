package com.dy.reader.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter
import com.ding.basic.util.DataCache

import com.dy.reader.R
import com.dy.reader.adapter.BaseRecyclerHolder
import com.dy.reader.adapter.ListRecyclerAdapter
import com.dy.reader.event.EventSetting
import com.dy.reader.presenter.CatalogMark
import com.dy.reader.presenter.CatalogMarkPresenter
import com.dy.reader.setting.ReaderStatus

import kotlinx.android.synthetic.txtqbdzs.frag_catalog_mark.*
import kotlinx.android.synthetic.txtqbdzs.item_reader_bookmark.view.*
import kotlinx.android.synthetic.txtqbdzs.item_reader_catalog.view.*

import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.book.RepairHelp
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.theme.ThemeHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable

class CatalogMarkFragment : Fragment(), CatalogMark.View {

    val presenter: CatalogMark.Presenter by lazy {
        CatalogMarkPresenter(this)
    }

    var chapterList = mutableListOf<Chapter>()

    private var bookmarkList = mutableListOf<Bookmark>()

    private var reverse = false

    private lateinit var catalogAdapter: ListRecyclerAdapter<Chapter, ChapterHolder>
    private lateinit var bookmarkAdapter: ListRecyclerAdapter<Bookmark, BookMarkHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.frag_catalog_mark, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rl_container.setBackgroundColor(Color.WHITE)
        view_line.setBackgroundColor(Color.parseColor("#f4f4f4"))
        catalogAdapter = ListRecyclerAdapter(chapterList, R.layout.item_reader_catalog, ChapterHolder::class.java)
        catalogAdapter.itemClick = View.OnClickListener { v ->
            presenter.gotoChapter(requireActivity(), v.tag as Chapter)
        }
        recl_catalog_content.adapter = catalogAdapter
        recl_catalog_content.layoutManager = object : LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false) {
            override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
                super.onLayoutChildren(recycler, state)
                val firstVisibleItemPosition = findFirstVisibleItemPosition()
                if (firstVisibleItemPosition != 0) {
                    if (firstVisibleItemPosition == -1) {
                        ckb_catalog_order.visibility = View.VISIBLE
                    } else {
                        ckb_catalog_order.visibility = View.VISIBLE
                    }
                    return
                }
                val lastVisibleItemPosition = findLastVisibleItemPosition()
                val itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1
                rfs_catalog_scroller.visibility = if (catalogAdapter.itemCount > itemsShown) View.VISIBLE else View.GONE
            }
        }
        rfs_catalog_scroller.setRecyclerView(recl_catalog_content)
        rfs_catalog_scroller.setViewsToUse(R.layout.common_recycler_view_scroller, R.id.img_scroller)

        bookmarkAdapter = ListRecyclerAdapter(bookmarkList, R.layout.item_reader_bookmark, BookMarkHolder::class.java)
        bookmarkAdapter.itemClick = View.OnClickListener { v ->
            presenter.gotoBookMark(requireActivity(), v.tag as Bookmark)
        }

        bookmarkAdapter.itemLongClick = View.OnLongClickListener { v ->
            presenter.deleteBookMark(requireActivity(), v.tag as Bookmark)
            true
        }

        checkHead(true)

        recl_mark_content.adapter = bookmarkAdapter
        recl_mark_content.layoutManager = object : LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false) {
            override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
                super.onLayoutChildren(recycler, state)
                val firstVisibleItemPosition = findFirstVisibleItemPosition()
                if (firstVisibleItemPosition != 0) {
                    if (firstVisibleItemPosition == -1)
                        rfs_mark_scroller.visibility = View.GONE
                    return
                }
                val lastVisibleItemPosition = findLastVisibleItemPosition()
                val itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1
                rfs_mark_scroller.visibility = if (bookmarkAdapter.itemCount > itemsShown) View.VISIBLE else View.GONE
            }
        }
        rfs_mark_scroller.setRecyclerView(recl_mark_content)
        rfs_mark_scroller.setViewsToUse(R.layout.common_recycler_view_scroller, R.id.img_scroller)

        initListener()
    }

    private fun initListener() {
        img_fix_book.setOnClickListener {
            RepairHelp.fixBook(requireActivity(), presenter.getBook()) {
                if (!requireActivity().isFinishing) {
                    RouterUtil.navigation(requireActivity(), RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
                }
            }
            presenter.onClickFixBook(requireActivity())
        }

        rl_catalog.setOnClickListener {
            checkHead(true)
            ckb_catalog_order.visibility = View.VISIBLE
            rl_book_content.visibility = View.VISIBLE
            presenter.loadCatalog(reverse)
        }

        rl_bookmark.setOnClickListener {
            checkHead(false)
            ckb_catalog_order.visibility = View.GONE
            rl_book_content.visibility = View.GONE
            presenter.loadBookMark(requireActivity(), 1)//用于标识只有为1的时候才打点书签
        }


        ckb_catalog_order.setOnCheckedChangeListener { buttonView, isChecked ->
            reverse = isChecked
            if (isChecked) {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_catalog_click_dx_btn)
            } else {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_catalog_click_zx_btn)
            }
            presenter.loadCatalog(isChecked)
        }

    }

    private fun checkHead(isCheckCatalog: Boolean) {
        if (isCheckCatalog) {
            txt_catalog.isSelected = true
            view_catalog.isSelected = true
            txt_bookmark.isSelected = false
            view_bookmark.isSelected = false
        } else {
            txt_catalog.isSelected = false
            view_catalog.isSelected = false
            txt_bookmark.isSelected = true
            view_bookmark.isSelected = true
        }
    }

    fun loadData() {
        if (recl_catalog_content.visibility == View.VISIBLE) {
            presenter.loadCatalog(reverse)
        } else {
            presenter.loadBookMark(requireActivity(), 2)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveEvent(event: EventSetting) {
        if (event.type == EventSetting.Type.OPEN_CATALOG) {
            changeBgColorWithNightModel()
        }
    }

    private fun changeBgColorWithNightModel() {
        try {
            // 替换背景
            if (ThemeHelper.getInstance(context).isNight) {
                rl_container.setBackgroundColor(Color.parseColor("#181818"))
                view_line.setBackgroundColor(Color.parseColor("#282828"))
                img_empty_book_mark.setImageResource(R.drawable.reader_catalog_mark_empty_icon_night)
                txt_empty_book_mark_tip.setTextColor(Color.parseColor("#989898"))
                rfs_catalog_scroller.setHandleImage(R.drawable.recycler_view_scroller_icon_night)
                rfs_mark_scroller.setHandleImage(R.drawable.recycler_view_scroller_icon_night)
                catalogAdapter.isEditMode = true
                bookmarkAdapter.isEditMode = true
                catalogAdapter.notifyDataSetChanged()
                bookmarkAdapter.notifyDataSetChanged()
            } else {
                rl_container.setBackgroundColor(Color.WHITE)
                view_line.setBackgroundColor(Color.parseColor("#f4f4f4"))
                img_empty_book_mark.setImageResource(R.drawable.reader_catalog_mark_empty_icon)
                txt_empty_book_mark_tip.setTextColor(Color.parseColor("#B9B9B9"))
                rfs_catalog_scroller.setHandleImage(R.drawable.recycler_view_scroller_icon)
                rfs_mark_scroller.setHandleImage(R.drawable.recycler_view_scroller_icon)
                catalogAdapter.isEditMode = false
                bookmarkAdapter.isEditMode = false
                catalogAdapter.notifyDataSetChanged()
                bookmarkAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    fun fixBook() {
        if (RepairHelp.isShowFixBtn(requireActivity(), ReaderStatus.book.book_id)) {
            img_fix_book.visibility = View.VISIBLE
        } else {
            img_fix_book.visibility = View.GONE
        }
    }

    private var dataLoaded: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun showCatalog(chapters: List<Chapter>, sequence: Int) {

        recl_catalog_content.visibility = View.VISIBLE
        rfs_catalog_scroller.visibility = View.VISIBLE
        recl_mark_content.visibility = View.GONE
        rfs_mark_scroller.visibility = View.GONE
        rl_mark_empty.visibility = View.GONE
        loadingPage?.onSuccess()

        txt_book_name.text = ReaderStatus.book.name

        dataLoaded = true

        chapterList.clear()
        chapterList.addAll(chapters)

        txt_chapter_count.text = "共${chapterList.size}章"

        recl_catalog_content.adapter.notifyDataSetChanged()
        recl_catalog_content.scrollToPosition(sequence)
    }

    override fun showMark(marks: List<Bookmark>) {
        recl_catalog_content.visibility = View.GONE
        rfs_catalog_scroller.visibility = View.GONE
        recl_mark_content.visibility = View.VISIBLE
        rfs_mark_scroller.visibility = View.VISIBLE
        loadingPage?.onSuccess()
        if (marks.isEmpty()) {
            rl_mark_empty.visibility = View.VISIBLE
        } else {
            rl_mark_empty.visibility = View.GONE
        }
        bookmarkList.clear()
        bookmarkList.addAll(marks)
        recl_mark_content.adapter.notifyDataSetChanged()

    }

    private var loadingPage: LoadingPage? = null

    override fun setChangeAble(enable: Boolean) {
        rl_bookmark.isClickable = enable
        rl_catalog.isClickable = enable
    }

    override fun onLoading() {
        loadingPage?.onSuccess()
        if (!dataLoaded) {
            loadingPage = LoadingPage(requireActivity(), view as RelativeLayout)
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
        override fun onBindData(position: Int, data: Chapter, editMode: Boolean) {
            if (itemView != null) {
                itemView.tag = data
                itemView.isClickable = true
                itemView.setOnClickListener { v ->
                    onItemClick?.onClick(v)
                }
                itemView.catalog_divider_line.setBackgroundColor(if (editMode) Color.parseColor("#282828") else Color.parseColor("#f4f4f4"))

                itemView.txt_chapter_name.text = "${data.name}"

                val chapterExist = DataCache.isChapterCached(data)

                var color = when {
                    chapterExist -> if (editMode) Color.parseColor("#989898") else Color.parseColor("#616161") // 已缓存
                    else -> if (editMode) Color.parseColor("#66989898") else Color.parseColor("#B9B9B9") // 未缓存
                }

                if (data.name?.equals(ReaderStatus.chapterName) == true) {
                    color = Color.parseColor("#3ECC96")
                }

                itemView.txt_chapter_name.setTextColor(color)
            }
        }
    }

    /**
     * 书签Holder
     */
    class BookMarkHolder(itemView: View?) : BaseRecyclerHolder<Bookmark>(itemView) {

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.CHINA)

        override fun onBindData(position: Int, data: Bookmark, editMode: Boolean) {
            if (itemView != null) {
                itemView.tag = data
                itemView.txt_delete_mark.tag = data

                itemView.isClickable = true
                itemView.setOnClickListener {
                    onItemClick?.onClick(it)
                }
                itemView.txt_delete_mark.setOnClickListener { v ->
                    onItemLongClick?.onLongClick(v)
                }
                itemView.book_mark_divider_line.setBackgroundColor(if (editMode) Color.parseColor("#282828") else Color.parseColor("#f4f4f4"))


                (itemView.txt_bookmark_title as TextView).text = "${data.chapter_name}"
                (itemView.txt_bookmark_content as TextView).text = "${data.chapter_content}"
                (itemView.txt_bookmark_time as TextView).text = dateFormat.format(data.insert_time)
            }
        }
    }
}