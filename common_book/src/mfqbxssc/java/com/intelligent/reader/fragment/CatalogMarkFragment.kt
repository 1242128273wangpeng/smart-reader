package com.intelligent.reader.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.intelligent.reader.R
import com.intelligent.reader.activity.DownloadManagerActivity
import com.intelligent.reader.adapter.BaseRecyclerHolder
import com.intelligent.reader.adapter.ListRecyclerAdapter
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.presenter.read.CatalogMark
import com.intelligent.reader.read.help.BookHelper
import com.quduquxie.network.DataCache
import kotlinx.android.synthetic.mfqbxssc.item_read_bookmark.view.*
import kotlinx.android.synthetic.mfqbxssc.pop_catalog_mark_delete.view.*
import kotlinx.android.synthetic.mfqbxssc.read_catalog_mark_layout.view.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Bookmark
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.repair_books.RepairHelp
import net.lzbook.kit.utils.StatServiceUtils
import java.text.SimpleDateFormat
import java.util.concurrent.Callable

/**
 * Created by xian on 2017/8/17.
 */
class CatalogMarkFragment : Fragment(), CatalogMark.View, DrawerLayout.DrawerListener {

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

    }

    override fun onDrawerOpened(drawerView: View) {
        if (view!!.catalog_main.visibility == View.VISIBLE) {
            presenter?.loadCatalog(reverse)
        } else {
            presenter?.loadBookMark(activity, 2)
        }
    }

    override fun onDrawerClosed(drawerView: View) {

    }

    override fun onDrawerStateChanged(newState: Int) {

    }

    class ChapterHolder(itemView: View?) : BaseRecyclerHolder<Chapter>(itemView) {
        override fun onBindData(position: Int, chapter: Chapter, editMode: Boolean) {
            if (itemView != null) {
                itemView.tag = chapter
                itemView.isClickable = true
                itemView.setOnClickListener { v ->
                    onItemClick?.onClick(v)
                }

                val txt = (itemView as TextView)

                txt.text = "${chapter.chapter_name}"

                var chapterExist = false
                if (Constants.QG_SOURCE.equals(BaseBookApplication.getGlobalContext().readStatus.book.site)) {
                    chapterExist = DataCache.isChapterExists(chapter.chapter_id, chapter.book_id)
                } else {
                    chapterExist = BookHelper.isChapterExist(chapter.sequence, chapter.book_id)
                }

                var txtColor = 0
                if (chapterExist) {
                    txtColor = R.color.read_item_catalog_chapter_text_color

                } else {
                    txtColor = R.color.read_item_catalog_uncached_chapter_text_color
                }
//
                if (chapter.chapter_name?.equals(BaseBookApplication.getGlobalContext().readStatus?.chapterName) ?: false) {
                    txtColor = R.color.read_item_catalog_current_chapter_text_color
                }

                txt.setTextColor(itemView.context.resources.getColor(txtColor))

            }

        }
    }

    class BookMarkHolder(itemView: View?) : BaseRecyclerHolder<Bookmark>(itemView) {

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

                (itemView.item_bookmark_title as TextView).text = "${data.chapter_name}"
                (itemView.item_bookmark_desc as TextView).text = "${data.chapter_content}"
                (itemView.item_bookmark_time as TextView).text = dateFormat.format(data.last_time)
            }
        }
    }

    override var presenter: CatalogMark.Presenter? = null

    var chapterList = mutableListOf<Chapter>()
    var bookMarkList = mutableListOf<Bookmark>()
    var reverse = false

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.read_catalog_mark_layout, container, false)
        val dividerCatalog = ShapeItemDecoration(activity, DividerItemDecoration.VERTICAL)
        val dividerBookmark = ShapeItemDecoration(activity, DividerItemDecoration.VERTICAL)

        dividerCatalog.setDrawable(ColorDrawable(Color.parseColor("#0c000000")))
        dividerBookmark.setDrawable(ColorDrawable(Color.parseColor("#0c000000")))

//        view!!.catalog_main.layoutManager = LinearLayoutManager(activity)
        view!!.catalog_main.addItemDecoration(dividerCatalog)
//        view.bookmark_main.layoutManager = LinearLayoutManager(activity)
        view!!.bookmark_main.addItemDecoration(dividerBookmark)

        if (RepairHelp.isShowFixBtn(activity, (BookApplication.getGlobalContext())?.readStatus?.book_id)) {
            view!!.iv_fixbook.visibility = View.VISIBLE
        } else {
            view!!.iv_fixbook.visibility = View.GONE
        }

        view!!.iv_fixbook.setOnClickListener { v ->
            RepairHelp.fixBook(activity, presenter?.getBook(), RepairHelp.FixCallBack {
                val intent_download = Intent(activity, DownloadManagerActivity::class.java)
                try {
                    activity.startActivity(intent_download)
                    activity.finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })
            presenter?.onClickFixBook(activity)
        }

        val catalogAdapter = ListRecyclerAdapter(chapterList, R.layout.item_read_catalog, ChapterHolder::class.java)
        catalogAdapter.itemClick = View.OnClickListener { v ->

            presenter?.gotoChapter(activity, v.tag as Chapter)

        }
        view.catalog_main.adapter = catalogAdapter
        view.catalog_main.setLayoutManager(object : LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false) {
            override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
                super.onLayoutChildren(recycler, state)
                //TODO if the items are filtered, considered hiding the fast scroller here
                val firstVisibleItemPosition = findFirstVisibleItemPosition()
                if (firstVisibleItemPosition != 0) {
                    if (firstVisibleItemPosition == -1)
                        view.catalog_fastscroller.setVisibility(View.GONE)
                    return
                }
                val lastVisibleItemPosition = findLastVisibleItemPosition()
                val itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1
                view.catalog_fastscroller.setVisibility(if (catalogAdapter.getItemCount() > itemsShown) View.VISIBLE else View.GONE)
            }
        })
        view.catalog_fastscroller.setRecyclerView(view.catalog_main)
        view.catalog_fastscroller.setViewsToUse(R.layout.read_recyclerview_fast_scroller, R.id.fastscroller_handle)

        val bookmarkAdapter = ListRecyclerAdapter(bookMarkList, R.layout.item_read_bookmark, BookMarkHolder::class.java)
        bookmarkAdapter.itemClick = View.OnClickListener { v ->
            presenter?.gotoBookMark(activity, v.tag as Bookmark)
        }

        bookmarkAdapter.itemLongClick = View.OnLongClickListener { v: View ->
            view.rl_left_pop_bg.visibility = View.VISIBLE
            val transX = activity.window.decorView.width - view.rl_catalog_novel.width
            val inflate = LayoutInflater.from(context).inflate(R.layout.pop_catalog_mark_delete, null)

            val popupWindow = PopupWindow(inflate, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000));
            popupWindow.isFocusable = true
            popupWindow.isOutsideTouchable = false
            popupWindow.showAtLocation(view.rl_left_pop_bg, Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL, -transX / 2, 0)
            inflate.txt_delete_mark.tag = v.tag
            inflate.txt_delete_mark.setOnClickListener { v ->
                presenter?.deleteBookMark(activity, v.tag as Bookmark)
                popupWindow.dismiss()
            }
            inflate.txt_clear_mark.setOnClickListener { v ->
                presenter?.deleteAllBookMark(activity)
                popupWindow.dismiss()
            }

            popupWindow.setOnDismissListener {
                view.rl_left_pop_bg.visibility = View.GONE
            }


            true
        }
        view.bookmark_main.adapter = bookmarkAdapter
        view.bookmark_main.setLayoutManager(object : LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false) {
            override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
                super.onLayoutChildren(recycler, state)
                //TODO if the items are filtered, considered hiding the fast scroller here
                val firstVisibleItemPosition = findFirstVisibleItemPosition()
                if (firstVisibleItemPosition != 0) {
                    if (firstVisibleItemPosition == -1)
                        view.bookmark_fastscroller.setVisibility(View.GONE)
                    return
                }
                val lastVisibleItemPosition = findLastVisibleItemPosition()
                val itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1
                view.bookmark_fastscroller.setVisibility(if (bookmarkAdapter.getItemCount() > itemsShown) View.VISIBLE else View.GONE)
            }
        })
        view.bookmark_fastscroller.setRecyclerView(view.bookmark_main)
        view.bookmark_fastscroller.setViewsToUse(R.layout.read_recyclerview_fast_scroller, R.id.fastscroller_handle)

        view.read_rg_catlog_mark.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.tab_catalog -> {
                    view.tv_catalog_novel_sort.visibility = View.VISIBLE
                    presenter?.loadCatalog(reverse)
                }
                R.id.tab_bookmark -> {
                    view.tv_catalog_novel_sort.visibility = View.GONE
                    presenter?.loadBookMark(activity, 1)//用于标识只有为1的时候才打点书签
                }
                else -> {

                }
            }
        }
        view.tv_catalog_novel_sort.setOnClickListener {
            reverse = !reverse
//            var sortIcon = TypedValue()//背景色
            if (reverse) {
//                activity.iyouqu.theme.resolveAttribute(R.attr.directory_sort_positive, sortIcon, true)
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_catalog_click_dx_btn)
            } else {
//                activity.iyouqu.theme.resolveAttribute(R.attr.directory_sort_negative, sortIcon, true)
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_catalog_click_zx_btn)
            }
//            view.iv_catalog_novel_sort.setImageResource(sortIcon.resourceId)
            view.tv_catalog_novel_sort.setText(if (!reverse) R.string.catalog_negative else R.string.catalog_positive)
            presenter?.loadCatalog(reverse)
        }


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private var dataLoaded: Boolean = false

    override fun showCatalog(chapters: List<Chapter>, sequence: Int) {
        if (view == null) {
            //monkey
            return
        }
        view!!.catalog_main.visibility = View.VISIBLE
        view!!.catalog_fastscroller.visibility = View.VISIBLE
        view!!.bookmark_main.visibility = View.GONE
        view!!.bookmark_fastscroller.visibility = View.GONE
        view!!.rl_layout_empty_online.visibility = View.GONE
        loadingPage?.onSuccess()

        view!!.catalog_novel_name.setText(BaseBookApplication.getGlobalContext().readStatus.book.name)

        dataLoaded = true

        chapterList.clear()
        chapterList.addAll(chapters)

        view!!.catalog_chapter_count.text = "共${chapterList.size}章"

        view!!.catalog_main.adapter.notifyDataSetChanged()
        view!!.catalog_main.scrollToPosition(sequence)
    }

    override fun showMark(marks: List<Bookmark>) {
        view!!.catalog_main.visibility = View.GONE
        view!!.catalog_fastscroller.visibility = View.GONE
        view!!.bookmark_main.visibility = View.VISIBLE
        view!!.bookmark_fastscroller.visibility = View.VISIBLE
        loadingPage?.onSuccess()
        if (marks.isEmpty()) {
            view!!.rl_layout_empty_online.visibility = View.VISIBLE
        } else {
            view!!.rl_layout_empty_online.visibility = View.GONE
        }
        bookMarkList.clear()
        bookMarkList.addAll(marks)
        view!!.bookmark_main.adapter.notifyDataSetChanged()

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
                presenter?.loadCatalog(false)
                null
            })

        }
    }

    override fun onNetError() {
        loadingPage?.onError()
    }
}