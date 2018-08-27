package com.dy.reader.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dy.reader.R
import com.dy.reader.adapter.PagerScrollAdapter
import com.dy.reader.setting.ReaderStatus
import com.dy.reader.mode.NovelPageBean

/**
 * Desc 书籍封面Holder
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/22 11:43
 */
class HomePagerHolder(var parent: ViewGroup, private val textColor: Int) :
        PagerScrollAdapter.ReaderPagerHolder(itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reader_cover, parent, false)) {

    init {
        book_name_tv = itemView.findViewById(R.id.txt_reader_book)
        book_auth_tv = itemView.findViewById(R.id.txt_reader_author)
    }

    override fun bindHolder(pageLines: NovelPageBean) {
        book_name_tv.text = ReaderStatus.book.name
        book_auth_tv.text = ReaderStatus.book.author

        book_name_tv.setTextColor(textColor)
        book_auth_tv.setTextColor(textColor)
    }
}