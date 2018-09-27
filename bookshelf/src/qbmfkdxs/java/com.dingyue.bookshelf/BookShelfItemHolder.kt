package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.Book
import kotlinx.android.synthetic.qbmfkdxs.item_bookshelf_book.view.*
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools
import net.lzbook.kit.utils.book.RepairHelp

/**
 * Created by Administrator on 2017/4/13 0013
 */

class BookShelfItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_book, parent, false)) {

    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener,
             contains: Boolean, isRemove: Boolean) = with(itemView) {

        if (book.img_url?.isNotEmpty() == true
                && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(itemView.context.applicationContext)
                    .load(book.img_url)
                    .placeholder(R.drawable.icon_book_cover_default)
                    .error(R.drawable.icon_book_cover_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(img_book_cover)
        } else {
            Glide.with(itemView.context.applicationContext)
                    .load(R.drawable.icon_book_cover_default)
                    .into(img_book_cover)
        }

        if (book.name?.isNotEmpty() == true) txt_book_name.text = book.name


        /**
         * 书架检测到书籍有修复会在该书籍封面显示更新角标，
         * 并且章节信息变更为：章节已修复至最新（列表书架显示，九宫格书架只显示更新角标）
         * 目录修复：如用户未点击更新弹窗的同步按钮，则书籍封面上的更新角标和更新文案一直存在
         */
        val sp = BaseBookApplication.getGlobalContext().getSharedPreferences(Constants.SHAREDPREFERENCES_KEY, 0)
        if (RepairHelp.isShowFixBtn(context, book.book_id) && sp.getBoolean(book.book_id, true)) {
            img_book_status.visibility = View.VISIBLE
            img_book_status.setImageResource(R.drawable.bookshelf_book_update_icon)
            txt_book_latest_chapter.text = "章节已修复至最新"
        } else {
            when {
                book.update_status == 1 -> { //更新
                    img_book_status.visibility = View.VISIBLE
                    img_book_status.setImageResource(R.drawable.bookshelf_book_update_icon)
                }
                book.status == "FINISH" -> { //完结
                    img_book_status.visibility = View.VISIBLE
                    img_book_status.setImageResource(R.drawable.bookshelf_item_book_finish_icon)
                }
                else -> img_book_status.visibility = View.GONE
            }

            book.last_chapter?.name?.let {
                val latestChapter = "更新至：" + it
                txt_book_latest_chapter.text = latestChapter
            }
        }

        val updateTime = Tools.compareTime(AppUtils.formatter, book.last_chapter!!.update_time) + "更新"
        txt_book_last_update_time.text = updateTime

        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1
        }


        val readPercent = if (book.sequence >= 0) {
            val firstMark = book.chapter_count * 0.01
            if (book.sequence + 1 < firstMark) {
                "已读 1%"
            } else {
                "已读 ${Math.round(((book.sequence + 1) * 100 / book.chapter_count).toFloat())}%"
            }
        } else {
            "未读"
        }
        txt_book_unread_chapters.text = readPercent

        if (isRemove) {
            img_book_select_state.visibility = View.VISIBLE
            img_book_status.visibility = View.GONE
            if (contains) {
                img_book_select_state.setBackgroundResource(R.drawable.bookshelf_item_selected_icon)
            } else {
                img_book_select_state.setBackgroundResource(R.drawable.bookshelf_item_unselected_icon)
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
