package com.dingyue.downloadmanager.recl

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dingyue.downloadmanager.DownloadManagerTaskHolder
import com.dingyue.downloadmanager.R
import net.lzbook.kit.data.bean.Book

/**
 * Created on 2018/4/24.
 * Created by crazylei.
 **/
class DownloadManagerAdapter constructor(private val context: Context, private var downloadManagerItemListener: DownloadManagerItemListener, private var books: ArrayList<Book>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var remove = false

    var checkedBooks = ArrayList<Book>()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        return DownloadManagerTaskHolder(LayoutInflater.from(context).inflate(R.layout.item_download_manager_task, parent, false))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val book: Book? = books[position]

        if (book == null) {
            viewHolder.itemView.visibility = View.GONE
            return
        }

        viewHolder.itemView.visibility = View.VISIBLE

        when (viewHolder) {
            is DownloadManagerTaskHolder -> viewHolder.bind(book, downloadManagerItemListener, checkedBooks.contains(book), remove)
        }
    }

    override fun getItemCount(): Int {
        return if (books.size == 0) {
            0
        } else {
            books.size
        }
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

    fun insertRemoveState(state: Boolean) {
        remove = state

        if (!state) {
            checkedBooks.clear()
        }

        notifyDataSetChanged()
    }

    fun insertSelectAllState(state: Boolean) {
        if (state) {
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

    interface DownloadManagerItemListener {

        fun clickedDownloadItem(book: Book?, position: Int)

        fun longClickedDownloadItem(): Boolean
    }
}