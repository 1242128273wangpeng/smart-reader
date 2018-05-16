package com.dingyue.bookshelf

import android.support.v7.widget.GridLayoutManager
import com.dingyue.contract.HolderType

/**
 * Desc 书架item比重
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/16 10:40
 */
class BookShelfSpanSizeLookup(private val bookShelfAdapter: BookShelfAdapter) : GridLayoutManager.SpanSizeLookup() {

    override fun getSpanSize(position: Int): Int {

        val type = bookShelfAdapter.getItemViewType(position)

        return try {
            when (type) {
                HolderType.Type_AD -> 3
                HolderType.Type_Add -> 1
                HolderType.Type_Book -> 1
                else -> 3
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            3
        }
    }
}