package com.dingyue.bookshelf.view

import android.app.Activity
import android.support.v4.view.ViewPager
import android.view.View
import android.view.WindowManager
import com.ding.basic.bean.Book
import com.dingyue.bookshelf.BookShelfDetailAdapter
import com.dingyue.bookshelf.R
import com.dingyue.contract.BasePopup
import kotlinx.android.synthetic.mfqbxssc.popup_book_detail.view.*

/**
 * Desc 书籍详情弹窗
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/12 0012 15:49
 */
class BookShelfDetailPopup(private val activity: Activity, layout: Int = R.layout.popup_book_detail,
                           width: Int = WindowManager.LayoutParams.MATCH_PARENT,
                           height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(activity, layout, width, height) {

    private val bookShelfDetailAdapter = BookShelfDetailAdapter(activity)

    init {

        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.animationStyle = R.style.remove_menu_anim_style

        contentView.txt_detail_current.text = "1"
        contentView.vp_detail_content.adapter = bookShelfDetailAdapter
        contentView.vp_detail_content.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                contentView.txt_detail_current.text = (position + 1).toString()
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
        showAtLocation(view)
        bookShelfDetailAdapter.update(books)
        contentView.vp_detail_content.currentItem = 0
        contentView.txt_detail_total.text = books.size.toString()
    }

    private fun setBackgroundAlpha(bgAlpha: Float) {
        val attributes = activity.window.attributes
        attributes.alpha = bgAlpha
        activity.window.attributes = attributes
    }
}