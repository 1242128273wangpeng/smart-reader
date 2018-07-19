package com.dingyue.bookshelf.view

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.Book
import com.dingyue.bookshelf.BookShelfLogger
import com.dingyue.bookshelf.R
import com.dingyue.contract.router.BookRouter
import kotlinx.android.synthetic.qbmfkkydq.bookshelf_header_view.view.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.repair_books.RepairHelp
import net.lzbook.kit.utils.AppUtils
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
        iv_book_icon.setOnClickListener {
            BookRouter.navigateCoverOrRead(mContext!!, mBook!!, BookRouter.NAVIGATE_TYPE_BOOKSHELF)
        }

    }

    private var mBook: Book? = null
    private var mContext: Activity? = null
    fun setData(book: Book, title: String?, context: Activity) {
        mBook = book
        mContext = context
        if (!TextUtils.isEmpty(book.name)) {
            txt_book_name.text = book.name
        }



        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1
        }

        title?.let {
            txt_book_chapter_info.text = title
        }


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


    private fun getLabelBg(color: String): GradientDrawable {
        var drawable = GradientDrawable()

        drawable.shape = GradientDrawable.RECTANGLE

        drawable.setColor(Color.parseColor(color))


        val corner = AppUtils.dip2px(context, 2f).toFloat()
        drawable.cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, corner, corner, 0f, 0f)

        return drawable

    }
}


