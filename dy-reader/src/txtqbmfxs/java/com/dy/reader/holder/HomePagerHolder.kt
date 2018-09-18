package com.dy.reader.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dy.reader.R
import com.dy.reader.adapter.PagerScrollAdapter
import com.dy.reader.mode.NovelPageBean
import com.dy.reader.setting.ReaderStatus
import kotlinx.android.synthetic.txtqbmfxs.item_reader_cover.view.*

/**
 * Desc 书籍封面Holder
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/22 11:43
 */
class HomePagerHolder(private val parent: ViewGroup, private val textColor: Int) :
        PagerScrollAdapter.ReaderPagerHolder(itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reader_cover, parent, false)) {

    override fun bindHolder(pageLines: NovelPageBean) {
        itemView.txt_reader_book.text = ReaderStatus.book.name
        itemView.txt_reader_book.setTextColor(textColor)

        itemView.txt_reader_author.text = ReaderStatus.book.author
        itemView.txt_reader_author.setTextColor(textColor)

        itemView.txt_reader_product.text = parent.context.resources.getString(R.string.application_name)
        itemView.txt_reader_product.setTextColor(textColor)
    }
}