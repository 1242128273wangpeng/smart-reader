package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.Book
import kotlinx.android.synthetic.qbmfxsydq.item_bookshelf_book.view.*
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools

/**
 * Created by Administrator on 2017/4/13 0013
 */
class BookShelfItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_book, parent, false)) {

    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener,
             contains: Boolean, isRemove: Boolean) = with(itemView) {

        if (!TextUtils.isEmpty(book.name)) {
            book_shelf_name.setText(book.name)
        }

        if (!TextUtils.isEmpty(book.author)) {
            book_shelf_author.setText(book.author)
        }

        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1
        }

        if (book.sequence >= 0) {
            book_shelf_unread.setText((book.sequence + 1).toString() + "/" + book.chapter_count + "章")
        } else {
            book_shelf_unread.setText("未读")
        }


        // 是否连载
        if (!book.status.equals("FINISH")) {
            book_shelf_status_finish.setVisibility(View.VISIBLE)
        } else {
            book_shelf_status_finish.setVisibility(View.GONE)
        }
        // 是否有更新
        if (book.update_status!=1) {
            book_shelf_status_update.setVisibility(View.GONE)
        } else {
           book_shelf_status_update.setVisibility(View.VISIBLE)
            book_shelf_status_finish.setVisibility(View.GONE)
        }
        if (book_shelf_update_time != null) {
            book_shelf_update_time.setText(
                    Tools.compareTime(AppUtils.formatter, book.last_check_update_time) + "更新: ")
        }

        if (!TextUtils.isEmpty(book.img_url) && !book.img_url.equals(
                ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
            Glide.with(itemView.context.applicationContext)
                    .load(book.img_url).placeholder(R.drawable.icon_book_cover_default)
                    .error(R.drawable.icon_book_cover_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(book_shelf_image)
        } else {
            Glide.with(itemView.context.applicationContext)
                    .load(R.drawable.icon_book_cover_default)
                    .into(book_shelf_image)
        }


        //显示最后一个章节
        book_shelf_last_chapter.text = book.last_chapter?.name ?: ""


        if (isRemove) {
            check_delete.setVisibility(View.VISIBLE)
            book_shelf_status_finish.setVisibility(View.GONE)
            book_shelf_status_update.setVisibility(View.GONE)
            if (contains) {
                check_delete.setBackgroundResource(R.drawable.readsetting_check)
            } else {
                check_delete.setBackgroundResource(R.drawable.readsetting_uncheck)
            }
        } else {
            check_delete.setVisibility(View.GONE)

        }

        itemView.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }

        itemView.setOnLongClickListener {
            bookshelfItemListener.longClickedBookShelfItem()
        }

    }
}
