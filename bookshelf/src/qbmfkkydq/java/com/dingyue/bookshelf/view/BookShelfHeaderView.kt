package com.dingyue.bookshelf.view

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.Book
import com.dingyue.bookshelf.R
import com.dingyue.contract.router.BookRouter
import kotlinx.android.synthetic.qbmfkkydq.bookshelf_header_view.view.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.repair_books.RepairHelp
import java.text.MessageFormat

/**
 * Date: 2018/7/17 19:55
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 书架也头部视图，最近阅读书籍
 */
class BookShelfHeaderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.bookshelf_header_view, this)
        var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.layoutParams = layoutParams
        initView()
    }

    private fun initView() {
        txt_continue_read.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                BookRouter.navigateCoverOrRead(mContext!!, mBook!!, BookRouter.NAVIGATE_TYPE_BOOKSHELF)
            }

        })

    }

    private var mBook: Book? = null
    private var mContext: Activity? = null
    fun setData(book: Book, context: Activity) {
        mBook = book
        mContext = context
        if (!TextUtils.isEmpty(book.name)) {
            txt_book_name.text = book.name
        }



        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1
        }


        txt_book_chapter_info.text = "第" + (book.sequence + 1) + "章 "

        if (book.sequence >= 0) {
            txt_book_chapter.text = (book.sequence + 1).toString() + "/" + book.chapter_count + "章"
//            txt_book_chapter.text = MessageFormat.format("{0}/{1}章", book.sequence + 1, book.chapter_count)
        } else {
            txt_book_chapter.text = "未读"
        }

        /**
         * 书架检测到书籍有修复会在该书籍封面显示更新角标，
         * 并且章节信息变更为：章节已修复至最新（列表书架显示，九宫格书架只显示更新角标）
         * 目录修复：如用户未点击更新弹窗的同步按钮，则书籍封面上的更新角标和更新文案一直存在
         */
        val sp = BaseBookApplication.getGlobalContext().getSharedPreferences(Constants.SHAREDPREFERENCES_KEY, 0)
        if (RepairHelp.isShowFixBtn(context, book.book_id) && sp.getBoolean(book.book_id, true)) {
            txt_book_states_update.visibility = View.VISIBLE
            txt_book_states_update.setBackgroundColor(Color.parseColor("#FF0060"))
            txt_book_states_update.text = "更"

        } else {
            // 是否有更新
            txt_book_states_update.visibility = if (book.update_status == 1) View.VISIBLE else View.GONE

//            // 是否连载
            txt_book_states_finish.visibility = if (book.status == "FINISH") View.VISIBLE else View.GONE
//

        }



        if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(iv_book_icon.context)
                    .load(book.img_url)
                    .placeholder(R.drawable.common_book_cover_default_icon)
                    .error(R.drawable.common_book_cover_default_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(iv_book_icon)
        } else {
            Glide.with(iv_book_icon.context)
                    .load(R.drawable.common_book_cover_default_icon)
                    .into(iv_book_icon)
        }


    }
}


