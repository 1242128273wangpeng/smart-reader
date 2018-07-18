package com.dingyue.bookshelf


import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.qbmfkkydq.item_bookshelf_book.view.*
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.AppUtils
import android.view.ViewGroup
import com.ding.basic.bean.Book
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.repair_books.RepairHelp

/**
 * Desc 书架页item
 * Author zhenxiang
 * 2018\5\15 0015
 */

class BookShelfItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_book, parent, false)) {

    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener,
             contains: Boolean, remove: Boolean) = with(itemView) {

        if (!TextUtils.isEmpty(book.name)) {
            txt_book_name.text = book.name
        }



        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1
        }

        if (book.sequence >= 0) {
//            txt_book_chapter.text = MessageFormat.format("{0}/{1}章", book.sequence + 1, book.chapter_count)
            txt_book_chapter.text = (book.sequence + 1).toString() + "/" + book.chapter_count + "章"
        } else {
            txt_book_chapter.text = "未读"
        }


        /**
         * 书架检测到书籍有修复会在该书籍封面显示更新角标，
         * 并且章节信息变更为：章节已修复至最新（列表书架显示，九宫格书架只显示更新角标）
         * 目录修复：如用户未点击更新弹窗的同步按钮，则书籍封面上的更新角标和更新文案一直存在
         */
        val sp = BaseBookApplication.getGlobalContext().getSharedPreferences(Constants.SHAREDPREFERENCES_KEY, 0)
        txt_book_states_update.visibility = View.GONE
        if (RepairHelp.isShowFixBtn(context, book.book_id) && sp.getBoolean(book.book_id, true)) {
            txt_book_states_update.visibility = View.VISIBLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                txt_book_states_update.background = getLabelBg("#FF0060")
            } else {
                txt_book_states_update.setBackgroundColor(Color.parseColor("#FF0060"))
            }
            txt_book_states_update.text = "更"

        } else {
            // 是否有更新
            if (book.update_status == 1) {
                txt_book_states_update.visibility = View.VISIBLE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    txt_book_states_update.background = getLabelBg("#FF0060")
                } else {
                    txt_book_states_update.setBackgroundColor(Color.parseColor("#FF0060"))
                }
                txt_book_states_update.text = "更"
            }
            if (book.status == "FINISH") {
                txt_book_states_update.visibility = View.VISIBLE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    txt_book_states_update.background = getLabelBg("#2AD1BE")
                } else {
                    txt_book_states_update.setBackgroundColor(Color.parseColor("#2AD1BE"))
                }
                txt_book_states_update.text = "完"
            }

        }

        if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(itemView.context.applicationContext)
                    .load(book.img_url)
                    .placeholder(R.drawable.common_book_cover_default_icon)
                    .error(R.drawable.common_book_cover_default_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(img_book_cover)
        } else {
            Glide.with(itemView.context.applicationContext)
                    .load(R.drawable.common_book_cover_default_icon)
                    .into(img_book_cover)
        }



        if (remove) {
            img_item_select_state.visibility = View.VISIBLE
//            txt_book_states_finish.visibility = View.GONE
            txt_book_states_update.visibility = View.GONE

            if (contains) {
                img_item_select_state.setImageResource(R.drawable.bookshelf_item_book_checked_icon)
            } else {
                img_item_select_state.setImageResource(R.drawable.bookshelf_item_book_check_icon)
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

    private fun getLabelBg(color: String): GradientDrawable {
        var drawable = GradientDrawable()

        drawable.shape = GradientDrawable.RECTANGLE

        drawable.setColor(Color.parseColor(color))


        val corner = AppUtils.dip2px(itemView.context, 2f).toFloat()
        drawable.cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, corner, corner, 0f, 0f)

        return drawable

    }
}