package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import android.view.ViewGroup
import com.ding.basic.bean.Book
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools
import kotlinx.android.synthetic.zsmfqbxs.item_bookshelf_book.view.*
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.book.RepairHelp

/**
 * Desc 书架Item
 * Created by zhenxiang
 * on 2018/5/12 0013.
 */

class BookShelfItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_book, parent, false)) {

    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener,
             contains: Boolean, remove: Boolean) = with(itemView) {
        if (!TextUtils.isEmpty(book.name)) {
            txt_book_name.text = book.name
        }

        if(book.last_chapter != null){
            if (book.sequence + 1 > book.last_chapter!!.serial_number) {
                book.sequence = book.last_chapter!!.serial_number - 1
            }

            if (book.sequence + 1 == book.last_chapter!!.serial_number) {
                txt_book_unread_chapters.visibility = View.GONE
            } else {
                txt_book_unread_chapters.visibility = View.VISIBLE
                if(book.sequence >= 0){
                    txt_book_unread_chapters.text = ((book.sequence + 1).toString() + "/" + book.last_chapter!!.serial_number + "章")
                }else{
                    txt_book_unread_chapters.text = ("0" + "/" + book.last_chapter!!.serial_number + "章")
                }
            }
        }

        /**
         * 书架检测到书籍有修复会在该书籍封面显示更新角标，
         * 并且章节信息变更为：章节已修复至最新（列表书架显示，九宫格书架只显示更新角标）
         * 目录修复：如用户未点击更新弹窗的同步按钮，则书籍封面上的更新角标和更新文案一直存在
         */
        val sp = BaseBookApplication.getGlobalContext().getSharedPreferences(Constants.SHAREDPREFERENCES_KEY, 0)
        if (RepairHelp.isShowFixBtn(context, book.book_id) && sp.getBoolean(book.book_id, true)) {
            img_book_status_update.visibility = View.VISIBLE
        }else{
            when {
                book.update_status == 1 -> { //更新
                    img_book_status_update.visibility = View.VISIBLE
                }
                book.status == "FINISH" -> { //完结
                    img_book_status.visibility = View.VISIBLE
                    img_book_status.setImageResource(R.drawable.bookshelf_item_book_status_finish_icon)
                }
                else -> {
                    img_book_status.visibility = View.GONE
                    img_book_status_update.visibility = View.GONE
                }
            }

        }

        if (txt_book_last_update_time != null && book.last_chapter != null) {
            txt_book_last_update_time.text = (Tools.compareTime(AppUtils.formatter, book
                    .last_chapter!!.update_time) + "更新")
        }

        if ((!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
            Glide.with(itemView.context.applicationContext)
                    .load(book.img_url)
                    .placeholder(R.drawable.book_cover_default)
                    .error(R.drawable.book_cover_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(img_book_cover)
        } else {
            Glide.with(itemView.context.applicationContext)
                    .load(R.drawable.book_cover_default)
                    .into(img_book_cover)
        }


        if (remove) {
            img_item_select_state.visibility = View.VISIBLE
            img_book_status.visibility = View.GONE
            img_book_status_update.visibility = View.GONE

            if (contains) {
                img_item_select_state.setImageResource(R.drawable.bookshelf_delete_checked)
            } else {
                img_item_select_state.setImageResource(R.drawable.bookshelf_delete_unchecked)
            }
        } else {
            img_item_select_state.visibility = View.GONE
        }

        rl_main.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }

        rl_main.setOnLongClickListener {
            bookshelfItemListener.longClickedBookShelfItem()
        }
    }
}
