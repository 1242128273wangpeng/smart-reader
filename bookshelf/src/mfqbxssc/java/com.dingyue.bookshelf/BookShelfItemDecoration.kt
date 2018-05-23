package com.dingyue.bookshelf

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import com.dingyue.contract.HolderType

/**
 * Desc 书架item装饰
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/16 10:40
 */
class BookShelfItemDecoration(private var bookShelfAdapter: BookShelfAdapter) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, recyclerView: RecyclerView, state: RecyclerView.State?) {
        val count = bookShelfAdapter.itemCount
        val position = recyclerView.getChildAdapterPosition(view)

        if (position > -1 && position < count) {
            val type = bookShelfAdapter.getItemViewType(position)

            when (type) {
                HolderType.Type_Book -> {
                    outRect.left = 0
                    outRect.right = 0
                }
                else -> {
                    super.getItemOffsets(outRect, view, recyclerView, state)
                }
            }
        } else {
            super.getItemOffsets(outRect, view, recyclerView, state)
        }
    }
}