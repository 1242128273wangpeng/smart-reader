package com.dingyue.bookshelf

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout

import net.lzbook.kit.data.bean.Book

import java.util.ArrayList

class BookShelfAdapter(private val context: Context, private val bookShelfItemListener: BookShelfAdapter.BookShelfItemListener, private var books: ArrayList<Book>, private val adViews: List<ViewGroup>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var checkedBooks: ArrayList<Book> = ArrayList()

    var remove = false

    private var updateBooks: ArrayList<String> = ArrayList()

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
                viewHolder.bind(null, remove, bookShelfItemListener)
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
            viewHolder.bind(book, bookShelfItemListener, checkedBooks.contains(book), remove, updateBooks.contains(book.book_id))
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
        return if (books.size > 0) {
            if (books.size >= 50) {
                books.size
            } else {
                books.size + 1
            }
        } else 0
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

    fun setUpdate_table(update_table: ArrayList<String>) {
        this.updateBooks = update_table
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

    fun insertRemoveState(remove: Boolean) {
        this.remove = remove

        if (!remove) {
            checkedBooks.clear()
        }

        notifyDataSetChanged()
    }

    fun insertCheckedPosition(position: Int) {
        if (position > -1 && position < books.size) {
            val book = books[position]

            if (checkedBooks.contains(book)) {
                checkedBooks.remove(book)
            } else {
                checkedBooks.add(book)
            }

            notifyItemChanged(position)
        }
    }

    fun isCheckAll(): Boolean {
        return books.size == checkedBooks.size
    }

    fun insertSelectAllState(all: Boolean) {
        if (all) {
            books.forEach {
                if (!this.checkedBooks.contains(it)) {
                    this.checkedBooks.add(it)
                }
            }
        } else {
            checkedBooks.clear()
        }
        notifyDataSetChanged()
    }
}