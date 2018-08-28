package com.dy.reader.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dy.reader.R
import com.dy.reader.adapter.PagerScrollAdapter
import com.dy.reader.setting.ReaderStatus
import com.intelligent.reader.read.mode.NovelPageBean

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
        slogan_tv = itemView.findViewById(R.id.txt_reader_slogan)
        product_name_tv = itemView.findViewById(R.id.txt_reader_product)
    }

    override fun bindHolder(pageLines: NovelPageBean) {
        book_name_tv.text = ReaderStatus.book.name
        book_auth_tv.text = ReaderStatus.book.author
        slogan_tv.setTextView(2f, parent.context.resources.getString(R.string.reader_slogan))
        product_name_tv.setTextView(1f, parent.context.resources.getString(R.string.application_name))

        book_name_tv.setTextColor(textColor)
        book_auth_tv.setTextColor(textColor)
        slogan_tv.setTextColor(textColor)
        product_name_tv.setTextColor(textColor)
    }
}