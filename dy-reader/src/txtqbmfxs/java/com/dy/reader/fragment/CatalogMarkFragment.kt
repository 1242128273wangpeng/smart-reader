package com.dy.reader.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter
import com.ding.basic.util.DataCache
import com.dy.reader.R
import com.dy.reader.Reader
import com.dy.reader.adapter.BaseRecyclerHolder
import com.dy.reader.adapter.ListRecyclerAdapter
import com.dy.reader.event.EventSetting
import com.dy.reader.presenter.CatalogMark
import com.dy.reader.presenter.CatalogMarkPresenter
import com.dy.reader.setting.ReaderStatus
import com.dy.reader.view.ReaderDeleteBookmarkPopup
import kotlinx.android.synthetic.txtqbmfxs.frag_catalog_mark.*
import kotlinx.android.synthetic.txtqbmfxs.frag_catalog_mark.view.*
import kotlinx.android.synthetic.txtqbmfxs.item_reader_bookmark.view.*
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

    var dividerCatalog: ShapeItemDecoration? = null
    var dividerBookmark: ShapeItemDecoration? = null
    var isNight = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.frag_catalog_mark, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rl_container.setBackgroundColor(Color.WHITE)

        dividerCatalog = ShapeItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL)
        dividerBookmark = ShapeItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL)
        dividerCatalog?.setDrawable(ColorDrawable(Color.parseColor("#0C000000")))
        dividerBookmark?.setDrawable(ColorDrawable(Color.parseColor("#0C000000")))

        recl_catalog_content.addItemDecoration(dividerCatalog)
        recl_mark_content.addItemDecoration(dividerBookmark)

        val catalogAdapter = ListRecyclerAdapter(chapterList, R.layout.item_reader_catalog, ChapterHolder::class.java)
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

        val bookmarkAdapter = ListRecyclerAdapter(bookmarkList, R.layout.item_reader_bookmark, BookMarkHolder::class.java)
        bookmarkAdapter.itemClick = View.OnClickListener { v ->
            presenter.gotoBookMark(requireActivity(), v.tag as Bookmark)
        }

        bookmarkAdapter.itemLongClick = View.OnLongClickListener { v: View ->
            view_content_mask.visibility = View.VISIBLE

            val transX = requireActivity().window.decorView.width - rl_book_content.width

            val deleteBookmarkPopup = ReaderDeleteBookmarkPopup(requireActivity())

            deleteBookmarkPopup.deleteBookmarkListener = {
                presenter.deleteBookMark(requireActivity(), v.tag as Bookmark)
                deleteBookmarkPopup.dismiss()
            }

            deleteBookmarkPopup.clearBookmarkListener = {
                presenter.deleteAllBookMark(requireActivity())
                deleteBookmarkPopup.dismiss()
            }

            deleteBookmarkPopup.dismissList = {
                view_content_mask.visibility = View.GONE
            }

            deleteBookmarkPopup.showAtLocation(view_content_mask, Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL, -transX / 2, 0)

            true
        }


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
            RepairHelp.fixBook(requireActivity(), presenter.getBook(), {
                if (!requireActivity().isFinishing) {
                    RouterUtil.navigation(requireActivity(), RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
                }
            })
            presenter.onClickFixBook(requireActivity())
        }

        rg_catalog_mark.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbtn_catalog -> {
                    ckb_catalog_order.visibility = View.VISIBLE
                    presenter.loadCatalog(reverse)
                }
                R.id.rbtn_bookmark -> {
                    ckb_catalog_order.visibility = View.GONE
                    presenter.loadBookMark(requireActivity(), 1)//用于标识只有为1的时候才打点书签
                }
                else -> {

                }
            }
        }

        ckb_catalog_order.setOnClickListener {
            reverse = !reverse
            if (reverse) {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_catalog_click_dx_btn)
            } else {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_catalog_click_zx_btn)
            }
            ckb_catalog_order.setText(if (!reverse) R.string.catalog_negative else R.string.catalog_positive)
            presenter.loadCatalog(reverse)
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
            if (ThemeHelper.getInstance(context).isNight) {
                rl_container.setBackgroundColor(Color.parseColor("#191C1F"))
                dividerCatalog?.setDrawable(ColorDrawable(Color.parseColor("#282828")))
                dividerBookmark?.setDrawable(ColorDrawable(Color.parseColor("#282828")))
            } else {
                dividerCatalog?.setDrawable(ColorDrawable(Color.parseColor("#0C000000")))
                dividerBookmark?.setDrawable(ColorDrawable(Color.parseColor("#0C000000")))
                rl_container.setBackgroundColor(Color.WHITE)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    fun loadData() {
        if (recl_catalog_content.visibility == View.VISIBLE) {
            presenter.loadCatalog(reverse)
        } else {
            presenter.loadBookMark(requireActivity(), 2)
        }
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
        view?.rbtn_bookmark?.isClickable = enable
        view?.rbtn_catalog?.isClickable = enable
    }

    override fun onLoading() {
        loadingPage?.onSuccess()
        if (!dataLoaded) {
            loadingPage = LoadingPage(requireActivity(), view as FrameLayout)
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

                val textView = (itemView as TextView)

                textView.text = "${data.name}"

                val chapterExist = DataCache.isChapterCached(data)

                var color: Int

                val isNight = ThemeHelper.getInstance(Reader.context).isNight
                color = when {
                    chapterExist ->
                        if (isNight) Color.parseColor("#989898") else Color.parseColor("#E5000000")
                    else ->
                        if (isNight) Color.parseColor("#616161") else Color.parseColor("#4C000000")
                }

                if (data.name?.equals(ReaderStatus.chapterName) == true) {
                    color = Color.parseColor("#87af4c")//主题色
                }

                textView.setTextColor(color)
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

                itemView.isClickable = true
                itemView.setOnClickListener { v ->
                    onItemClick?.onClick(v)
                }
                itemView.setOnLongClickListener { v ->
                    onItemLongClick?.onLongClick(v) ?: false
                }

                val txt_bookmark_title = itemView.txt_bookmark_title as TextView
                val txt_bookmark_content = itemView.txt_bookmark_content as TextView
                val txt_bookmark_time = itemView.txt_bookmark_time as TextView

                txt_bookmark_title.text = "${data.chapter_name}"
                txt_bookmark_content.text = "${data.chapter_content}"
                txt_bookmark_time.text = dateFormat.format(data.insert_time)

                val isNight = ThemeHelper.getInstance(Reader.context).isNight
                val colorTitle = if (isNight) Color.parseColor("#989898") else Color.parseColor("#E5000000")
                val colorcontent = if (isNight) Color.parseColor("#616161") else Color.parseColor("#7F000000")
                val colortime = if (isNight) Color.parseColor("#616161") else Color.parseColor("#7F000000")

                txt_bookmark_title.setTextColor(colorTitle)
                txt_bookmark_content.setTextColor(colorcontent)
                txt_bookmark_time.setTextColor(colortime)
            }
        }
    }
}