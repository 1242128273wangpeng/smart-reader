package com.dingyue.bookshelf

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import net.lzbook.kit.data.bean.Book
import java.util.*

class BookShelfAdapter(private val context: Context,
                       private val bookShelfItemListener: BookShelfAdapter.BookShelfItemListener,
                       private var books: ArrayList<Book>, private val adViews: List<ViewGroup>?,
                       private val hasAddView: Boolean = false)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var selectedBooks: ArrayList<Book> = ArrayList()

    var isRemove = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return when (viewType) {
            TYPE_AD -> {
                BookShelfADHolder(LayoutInflater.from(context).inflate(R.layout.layout_bookshelf_item_list_ad, parent, false))
            }
            TYPE_ADD -> {
                BookShelfADDHolder(LayoutInflater.from(context).inflate(R.layout.item_bookshelf_add, parent, false))
            }
            else -> {
                BookShelfItemHolder(LayoutInflater.from(context).inflate(R.layout.layout_bookshelf_item_grid, parent, false))
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (position >= books.size) {
            if (viewHolder is BookShelfADDHolder) {
                viewHolder.bind(null, isRemove, bookShelfItemListener)
            }
            return
        }

        val book: Book? = books[position]

        if (book == null) {
            viewHolder.itemView.visibility = View.GONE
            return
        }

        viewHolder.itemView.visibility = View.VISIBLE

        if (viewHolder is BookShelfItemHolder) {
            viewHolder.bind(book, bookShelfItemListener, selectedBooks.contains(book), isRemove)
        } else if (viewHolder is BookShelfADHolder) {
            val view = getAdView(book)
            if (view != null) {

                val parent = view.parent

                if (parent != null && parent is RelativeLayout) {
                    parent.removeAllViews()
                }

                viewHolder.bind(view)
            }
        }
    }

    override fun getItemCount(): Int {
        if (hasAddView) {
            if (books.size > 0) {
                if (books.size >= 50) {
                    return 50
                }
                return books.size + 1
            }
        }
        return books.size
    }


    private fun getAdView(book: Book?): View? {
        if (adViews == null || adViews.isEmpty() || book == null) {
            return null
        }

        return if (book.sequence < adViews.size) {
            adViews[book.sequence]
        } else null
    }

    override fun getItemViewType(position: Int): Int {
        if (position >= 0) {
            if (position < books.size) {
                val book = books[position]
                if (book.book_type == 0) {
                    return TYPE_BOOK
                } else if (book.book_type == -2) {
                    return TYPE_AD
                }
            } else if (position == books.size) {
                return TYPE_ADD
            }
        }
        return -1
    }

    companion object {
        private val TYPE_BOOK = 0
        private val TYPE_AD = 1
        private val TYPE_ADD = 2
    }


    interface BookShelfItemListener {

        fun clickedBookShelfItem(book: Book?, position: Int)

        fun longClickedBookShelfItem(): Boolean
    }

    fun insertRemoveState(isRemove: Boolean) {
        this.isRemove = isRemove

        if (!isRemove) {
            selectedBooks.clear()
        }

        notifyDataSetChanged()
    }

    fun insertSelectedPosition(position: Int) {
        if (position > -1 && position < books.size) {
            val book = books[position]

            if (selectedBooks.contains(book)) {
                selectedBooks.remove(book)
            } else {
                selectedBooks.add(book)
            }

            notifyItemChanged(position)
        }
    }

    fun isSelectedAll(): Boolean {
        return books.size == selectedBooks.size
    }

    fun insertSelectAllState(all: Boolean) {
        if (all) {
            books.forEach {
                if (!this.selectedBooks.contains(it)) {
                    this.selectedBooks.add(it)
                }
            }
        } else {
            selectedBooks.clear()
        }
        notifyDataSetChanged()
    }
}