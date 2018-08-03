package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.mfqbxssc.item_bookshelf_book.view.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.repair_books.RepairHelp

/**
 * Desc 书架页Item
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/12 0002 14:19
 */
class BookShelfItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_book, parent, false)) {

    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener,
             contains: Boolean, remove: Boolean) = with(itemView) {

        if (book.name.isNotEmpty()) txt_book_name.text = book.name
        val sp = BaseBookApplication.getGlobalContext().getSharedPreferences(Constants.SHAREDPREFERENCES_KEY, 0)
        if (RepairHelp.isShowFixBtn(context, book.book_id) && sp.getBoolean(book.book_id, true)) {
            img_book_status.visibility = View.VISIBLE
            img_book_status.setImageResource(R.drawable.bookshelf_item_book_update_icon)
        }else{
            when {
                book.update_status == 1 -> { //更新
                    img_book_status.visibility = View.VISIBLE
                    img_book_status.setImageResource(R.drawable.bookshelf_item_book_update_icon)
                }
                book.status == 2 -> { //完结
                    img_book_status.visibility = View.VISIBLE
                    img_book_status.setImageResource(R.drawable.bookshelf_item_book_finish_icon)
                }
                else -> img_book_status.visibility = View.GONE
            }
        }


        if (book.img_url.isNotEmpty() && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(itemView.context)
                    .load(book.img_url)
                    .placeholder(R.drawable.common_book_cover_default_icon)
                    .error(R.drawable.common_book_cover_default_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(img_book_cover)
        } else {
            Glide.with(itemView.context)
                    .load(R.drawable.common_book_cover_default_icon)
                    .into(img_book_cover)
        }

        if (remove) {
            img_book_select_state.visibility = View.VISIBLE
            if (contains) {
                img_book_select_state.setImageResource(R.drawable.bookshelf_item_checked_icon)
            } else {
                img_book_select_state.setImageResource(R.drawable.bookshelf_item_check_icon)
            }
        } else {
            img_book_select_state.visibility = View.GONE
        }

        rl_book_content.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }

        rl_book_content.setOnLongClickListener {
            bookshelfItemListener.longClickedBookShelfItem()
        }

    }
}
