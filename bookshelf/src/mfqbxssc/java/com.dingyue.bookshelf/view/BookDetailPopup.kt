package com.dingyue.bookshelf.view

import android.app.Activity
import android.support.v4.view.ViewPager
import android.view.View
import android.view.WindowManager
import com.dingyue.bookshelf.BookDetailAdapter
import com.dingyue.bookshelf.R
import com.dingyue.contract.BasePopup
import kotlinx.android.synthetic.mfqbxssc.popup_book_detail.view.*
import net.lzbook.kit.data.bean.Book

/**
 * Desc 书籍详情弹窗
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/12 0012 15:49
 */
class BookDetailPopup(private val activity: Activity, layout: Int = R.layout.popup_book_detail,
                      width: Int = WindowManager.LayoutParams.MATCH_PARENT,
                      height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(activity, layout, width, height) {

    private val bookDetailAdapter = BookDetailAdapter(activity)

    init {

        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.animationStyle = R.style.remove_menu_anim_style

        contentView.current_num.text = "1"
        contentView.viewPager.adapter = bookDetailAdapter
        contentView.viewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                contentView.current_num.text = (position + 1).toString()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        popupWindow.setOnDismissListener {
            setBackgroundAlpha(1f)
        }

    }

    fun show(view: View, books: ArrayList<Book>) {
        if (books.isEmpty()) return
        setBackgroundAlpha(0.6f)
        showAsLocation(view)
        bookDetailAdapter.update(books)
        contentView.viewPager.currentItem = 0
        contentView.total_num.text = books.size.toString()
    }

    private fun setBackgroundAlpha(bgAlpha: Float) {
        val lp = activity.window.attributes
        lp.alpha = bgAlpha
        activity.window.attributes = lp
    }

}