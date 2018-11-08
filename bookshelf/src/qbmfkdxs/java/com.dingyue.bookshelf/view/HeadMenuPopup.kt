package com.dingyue.bookshelf.view

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.widget.PopupWindowCompat.showAsDropDown
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import com.dingyue.bookshelf.R

import kotlinx.android.synthetic.qbmfkdxs.popup_head_menu.view.*
import net.lzbook.kit.ui.widget.base.BasePopup

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
    var shareListener: (() -> Unit)? = null
    var promptGoneListener: (() -> Unit)? = null

    init {
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = false

        contentView.ll_download_manager.setOnClickListener {
            popupWindow.dismiss()
            onDownloadManagerClickListener?.invoke()
        }
        contentView.ll_book_sorting.setOnClickListener {
            popupWindow.dismiss()
            onBookSortingClickListener?.invoke()
        }

        contentView.ll_share.setOnClickListener {
            popupWindow.dismiss()
            contentView.view_share.visibility = View.GONE
            context.editShared {
                putBoolean(SharedPreUtil.BOOKSHELF_SHARE_PROMPT, true)
            }
            onPromptClick()
            shareListener?.invoke()
        }
        if (!Constants.SHARE_SWITCH_ENABLE) {
            contentView.ll_share.visibility = View.GONE
        } else {
            contentView.ll_share.visibility = View.VISIBLE
        }
        val isSharePromptGone = context.getSharedBoolean(SharedPreUtil.BOOKSHELF_SHARE_PROMPT)
        if (isSharePromptGone) {
            contentView.view_share.visibility = View.GONE
        }

    }

    fun setOnShareListener(listener: (() -> Unit)) {
        shareListener = listener
    }

    fun setOnGoneListener(listener: () -> Unit) {
        promptGoneListener = listener
    }

    private fun onPromptClick() {
        var isAllPromptGone = false
        val isSharePromptGone = contentView.view_share.visibility == View.GONE
        if (isSharePromptGone) {
            isAllPromptGone = true
        }
        if (isAllPromptGone) {
            promptGoneListener?.invoke()
        }
    }


    fun show(view: View) {
        showAsDropDown(view, 0, -(view.height + 30))
    }

}