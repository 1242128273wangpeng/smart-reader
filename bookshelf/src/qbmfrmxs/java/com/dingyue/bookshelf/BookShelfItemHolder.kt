package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.Book
import kotlinx.android.synthetic.qbmfrmxs.item_bookshelf_book.view.*

import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools
import java.math.BigDecimal


/**
 * Desc 书架页Item
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/5 0002 14:19
 */
class BookShelfItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_book, parent, false)) {

    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener,
             contains: Boolean, remove: Boolean) = with(itemView) {

        if (!TextUtils.isEmpty(book.name)) {
            this.txt_book_name.text = book.name
        }

        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1
        }

        // 更新时间+更新章节
        val updateTime = "${Tools.compareTime(AppUtils.formatter, book.last_chapter!!.update_time)}更新"
        val updateChapter = book.last_chapter?.name
        txt_book_update.text = (updateTime + "：" + updateChapter)

        // 阅读进度
        if (book.last_chapter != null) {
            val count: Int = if (book.sequence <= 0) {
                0
            } else {
                val readChapter = BigDecimal(book.sequence + 1)
                val chapterCount = BigDecimal(book.chapter_count)
                (readChapter.divide(chapterCount, 2, BigDecimal.ROUND_HALF_UP).toDouble() * 100).toInt()
            }
            txt_read_status.text = ("已读$count%")
        }


        //更新
        img_book_status_update.visibility = if (book.update_status == 1) View.VISIBLE else View.GONE

        when {
            book.status == "FINISH" -> { //完结
                img_book_status.visibility = View.VISIBLE
                img_book_status.setImageResource(R.drawable.bookshelf_item_book_finish_icon)
            }
            else -> {
                img_book_status.visibility = View.GONE
            }
        }

        if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(itemView.context.applicationContext)
                    .load(book.img_url)
                    .placeholder(R.drawable.common_book_cover_default_icon)
                    .error(R.drawable.common_book_cover_default_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(this.img_book_cover)
        } else {
            Glide.with(itemView.context.applicationContext)
                    .load(R.drawable.common_book_cover_default_icon)
                    .into(this.img_book_cover)
        }

        if (remove) {
            this.img_book_select_state.visibility = View.VISIBLE
            if (contains) {
                this.img_book_select_state.setBackgroundResource(R.drawable.bookshelf_item_checked_icon)
            } else {
                this.img_book_select_state.setBackgroundResource(R.drawable.bookshelf_item_check_icon)
            }
        } else {
            this.img_book_select_state.visibility = View.GONE
        }

        rl_book_content.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }

        rl_book_content.setOnLongClickListener {
            bookshelfItemListener.longClickedBookShelfItem()
        }
    }
}