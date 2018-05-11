package com.dingyue.bookshelf

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools

/**
 * Created by Administrator on 2017/4/13 0013.
 */

class BookShelfItemHolder(itemView: View,
                          shelfItemClickListener: BookShelfReAdapter.ShelfItemClickListener,
                          shelfItemLongClickListener: BookShelfReAdapter.ShelfItemLongClickListener)
    : AbsRecyclerViewHolder<Book>(itemView, shelfItemClickListener, shelfItemLongClickListener) {

    private val mCheck: ImageView
    private val mImage: ImageView
    private val mName: TextView
    private val mUpdateTime: TextView?
    private val mFinish: View
    private val mUpdate: View

    init {

        mCheck = itemView.findViewById(R.id.check_delete) as ImageView
        mImage = itemView.findViewById(R.id.book_shelf_image) as ImageView

        mFinish = itemView.findViewById(R.id.book_shelf_status_finish)
        mUpdate = itemView.findViewById(R.id.book_shelf_status_update)
        mName = itemView.findViewById(R.id.book_shelf_name) as TextView
        mUpdateTime = itemView.findViewById(R.id.book_shelf_update_time) as TextView
        //        mUnread = (TextView) itemView.findViewById(R.id.book_shelf_unread);

    }

    override fun onBindData(position: Int, book: Book, update: Boolean, isRemoveMode: Boolean, removeMark: Boolean) {
        if (!TextUtils.isEmpty(book.name))
            this.mName.text = book.name

        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1
        }
        val update_count = book.chapter_count - (book.sequence + 1)
        //        if(update_count == 0){
        //            this.mUnread.setVisibility(View.GONE);
        //        }else{
        //            this.mUnread.setVisibility(View.VISIBLE);
        //            this.mUnread.setText(update_count + "章未读");
        //        }

        // 是否连载
        if (book.status == 2) {
            this.mFinish.visibility = View.VISIBLE
        } else {
            this.mFinish.visibility = View.GONE
        }
        // 是否有更新
        if (!update) {
            this.mUpdate.visibility = View.GONE
        } else {
            this.mUpdate.visibility = View.VISIBLE
            this.mFinish.visibility = View.GONE
        }
        if (this.mUpdateTime != null) {
            this.mUpdateTime.text = Tools.compareTime(AppUtils.formatter, book
                    .last_updatetime_native) + "更新"
        }

        if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(itemView.context).load(book.img_url).placeholder(R.drawable.icon_book_cover_default).error(R.drawable.icon_book_cover_default).diskCacheStrategy(DiskCacheStrategy.ALL).into(this.mImage)
        } else {
            Glide.with(itemView.context).load(R.drawable.icon_book_cover_default).into(this.mImage)
        }

        if (isRemoveMode) {
            this.mCheck.visibility = View.VISIBLE
            this.mFinish.visibility = View.GONE
            this.mUpdate.visibility = View.GONE
            var typeColor = 0
            val theme = itemView.context.theme
            if (removeMark) {
                typeColor = R.drawable.edit_bookshelf_selected
            } else {
                typeColor = R.drawable.edit_bookshelf_unselected
            }
            this.mCheck.setBackgroundResource(typeColor)
        } else {
            this.mCheck.visibility = View.GONE
        }
    }
}
