package com.dingyue.bookshelf


import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout
import java.util.*

class BookShelfReAdapter(protected var mContext: Activity, list: List<Book>, private val mAdViews: List<ViewGroup>?, private val shelfItemClickListener: ShelfItemClickListener, private val shelfItemLongClickListener: ShelfItemLongClickListener, isList: Boolean) : RecyclerView.Adapter<AbsRecyclerViewHolder<Book>>() {
    var remove_checked_states: HashSet<Int>? = null
    internal var distanceY = -1
    var isRemoveMode = false
    var book_list: ArrayList<Book>? = null
    private var update_table: ArrayList<String>? = null
    private var down_table: ArrayList<String>? = null
    private val parentView: ViewGroup? = null

    val checkedSize: Int
        get() = if (remove_checked_states != null) {
            remove_checked_states!!.size
        } else 0

    init {
        book_list = list as ArrayList<Book>
        update_table = ArrayList()
        down_table = ArrayList()
        remove_checked_states = HashSet()
        resetRemovedState()
        distanceY = mContext.resources.getDimension(net.lzbook.kit.R.dimen.dimen_view_height_default).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsRecyclerViewHolder<Book>? {
        val view: View
        var holder: AbsRecyclerViewHolder<Book>? = null
        when (viewType) {
            0 -> {
                if (isList) {
                    view = LayoutInflater.from(mContext).inflate(R.layout
                            .layout_bookshelf_item_list,
                            parent, false)
                } else {
                    view = LayoutInflater.from(mContext).inflate(R.layout.layout_bookshelf_item_grid, parent, false)
                }

                holder = BookShelfItemHolder(view, shelfItemClickListener,
                        shelfItemLongClickListener)
            }
            1 -> {
                //              view = LayoutInflater.from(mContext).inflate(R.layout.ad_item_small_layout, parent, false);
                //修改广告显示样式为九宫格
                view = LayoutInflater.from(mContext).inflate(R.layout.layout_bookshelf_item_list_ad, parent, false)
                holder = ADViewHolder(view)
            }
        }//                parentView = parent;
        return holder
    }

    override fun onBindViewHolder(holder: AbsRecyclerViewHolder<Book>?, position: Int) {
        if (holder == null && book_list == null) return
        val book = book_list!![position]
        when (getItemViewType(position)) {
            0 ->

                holder!!.onBindData(position, book, update_table!!.contains(book.book_id),
                        isRemoveMode, remove_checked_states!!.contains(position))
            1 -> if (holder is ADViewHolder) {
                val adView = getAdView(book)
                if (adView != null) {
                    val parent = adView.parent
                    if (parent != null && parent is RelativeLayout) {
                        parent.removeAllViews()
                    }
                    holder.item_ad_layout.removeAllViews()
                    holder.item_ad_layout.addView(adView)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (book_list != null) {
            book_list!!.size
        } else 0
    }

    private fun getAdView(book: Book?): View? {
        if (mAdViews == null || mAdViews.isEmpty() || book == null) {
            return null
        }
        return if (book.sequence < mAdViews.size) {
            mAdViews[book.sequence]
        } else null
    }

    override fun getItemViewType(position: Int): Int {
        if (book_list != null && position >= 0 && position <= book_list!!.size - 1) {
            val book = book_list!![position]
            if (book != null) {
                if (book.book_type == 0) {
                    return 0
                } else if (book.book_type == -2) {
                    return 1
                }
            }
        }
        return -1
    }

    fun setUpdate_table(update_table: ArrayList<String>) {
        this.update_table = update_table
    }

    fun setBookDownLoad(download_table: ArrayList<String>) {
        this.down_table = download_table
    }

    fun resetRemovedState() {
        remove_checked_states!!.clear()
    }

    fun setChecked(position: Int) {
        if (!remove_checked_states!!.contains(position)) {
            remove_checked_states!!.add(position)
        } else {
            remove_checked_states!!.remove(position)
        }
    }

    fun setAllChecked(checkedAll: Boolean) {
        if (checkedAll) {
            for (position in book_list!!.indices) {
                if (!remove_checked_states!!.contains(position)) {
                    remove_checked_states!!.add(position)
                }
            }
        } else {
            for (position in book_list!!.indices) {
                if (remove_checked_states!!.contains(position)) {
                    remove_checked_states!!.remove(position)
                }
            }
        }
    }

    fun setListPadding(Layout: SuperSwipeRefreshLayout, isShowing: Boolean) {
        if (isShowing) {
            val height = distanceY
            Layout.setPadding(0, 0, 0, height)
        } else {
            Layout.setPadding(0, 0, 0, 0)
        }
    }

    interface ShelfItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    interface ShelfItemLongClickListener {
        fun onItemLongClick(view: View, position: Int)
    }

    internal inner class ADViewHolder(itemView: View) : AbsRecyclerViewHolder<Book>(itemView, null, null) {
        var item_ad_layout: RelativeLayout


        init {
            item_ad_layout = itemView.findViewById(R.id.book_shelf_item_ad) as RelativeLayout
        }

        override fun onBindData(position: Int, data: Book, update: Boolean, isRemoveMode: Boolean, removeMark: Boolean) {

        }
    }

    companion object {

        private val isList: Boolean = false
    }
}
