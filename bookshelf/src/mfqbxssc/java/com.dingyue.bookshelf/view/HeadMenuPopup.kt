package com.dingyue.bookshelf.view

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.dingyue.bookshelf.R
import kotlinx.android.synthetic.mfqbxssc.popup_head_menu.view.*
import net.lzbook.kit.base.BasePopup
import net.lzbook.kit.utils.sp.SPKey
import net.lzbook.kit.utils.sp.SPUtils

/**
 * Desc 顶部菜单弹窗
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/11 0011 16:27
 */
class HeadMenuPopup(context: Context, layout: Int = R.layout.popup_head_menu,
                    width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
                    height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height){

    var onDownloadManagerClickListener: (() -> Unit)? = null

    var onBookSortingClickListener: (() -> Unit)? = null

    var onApplicationShareClickListener: (() -> Unit)? = null

    init {
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = false

        contentView.ll_download_manager.setOnClickListener {
            dismiss()
            onDownloadManagerClickListener?.invoke()
        }
        contentView.ll_book_sort.setOnClickListener {
            dismiss()
            onBookSortingClickListener?.invoke()
        }

        contentView.ll_app_share.setOnClickListener {
            dismiss()
            onApplicationShareClickListener?.invoke()
        }
    }

    fun show(view: View) {
        showAsDropDown(view, 0, -(view.height + 30))

        val share = SPUtils.getDefaultSharedBoolean(SPKey.APPLICATION_SHARE_ACTION)

        if (share) {
            contentView.view_app_share.visibility = View.GONE
        } else {
            contentView.view_app_share.visibility = View.VISIBLE
        }
    }
}