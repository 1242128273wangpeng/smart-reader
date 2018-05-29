package com.dingyue.bookshelf

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.ding.basic.bean.Book
import com.dingyue.contract.HolderType.Type_AD
import com.dingyue.contract.HolderType.Type_Add
import com.dingyue.contract.HolderType.Type_Book
import com.dingyue.contract.HolderType.Type_Header_AD
import java.util.*

class BookShelfAdapter(private val context: Context,
                       private val bookShelfItemListener: BookShelfAdapter.BookShelfItemListener,
                       private var books: ArrayList<Book>,
                       private val hasAddView: Boolean = false)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var selectedBooks: ArrayList<Book> = ArrayList()

    var isRemove = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return when (viewType) {
            Type_AD -> {
                BookShelfADHolder(parent, false)
            }
            Type_Add -> {
                BookShelfADDHolder(LayoutInflater.from(context).inflate(R.layout.item_bookshelf_add, parent, false))
            }
            Type_Header_AD -> {
                BookShelfADHolder(parent, true)
            }
            else -> {
                BookShelfItemHolder(parent)
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
            val view = book.item_view

            if (view != null) {
                val parent = view.parent
                if (parent != null && parent is RelativeLayout) {
                    parent.removeAllViews()
                }

                viewHolder.itemView.visibility = View.VISIBLE
                viewHolder.bind(view)
            } else {
                viewHolder.itemView.visibility = View.GONE
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

    override fun getItemViewType(position: Int): Int {
        if (position >= 0) {
            if (position < books.size) {
                val book = books[position]
                when {
                    book.item_type == 0 -> return Type_Book
                    book.item_type == 1 -> return Type_AD
                    book.item_type == 2 -> return Type_Header_AD
                    else -> {

                    }
                }
            } else if (position == books.size) {
                return Type_Add
            }
        }
        return -1
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