package com.dingyue.bookshelf.view

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.bookshelf.R

import kotlinx.android.synthetic.mfxsqbyd.popup_head_menu.view.*
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.ui.widget.base.BasePopup

/**
 * Desc 请描述这个文件
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/6 0006 10:36
 */
class HeadMenuPopup(context: Context, layout: Int = R.layout.popup_head_menu,
                    width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
                    height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height) {

    private var downloadListener: (() -> Unit)? = null
    private var sortingListener: (() -> Unit)? = null
    private var importListener: (() -> Unit)? = null
    private var shareListener: (() -> Unit)? = null
    private var promptGoneListener: (() -> Unit)? = null

    init {
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = false

        contentView.txt_download_manager.setOnClickListener {
            dismiss()
            downloadListener?.invoke()
        }

        contentView.txt_book_sorting.setOnClickListener {
            dismiss()
            sortingListener?.invoke()
        }

        contentView.rl_local_import.setOnClickListener {
            dismiss()
            contentView.view_local_import.visibility = View.GONE
            SPUtils.editDefaultShared {
                putBoolean(SPKey.BOOKSHELF_IMPORT_PROMPT, true)
            }
            onPromptClick()
            importListener?.invoke()
        }
        contentView.rl_share.setOnClickListener {
            dismiss()
            contentView.view_share.visibility = View.GONE
            SPUtils.editDefaultShared  {
                putBoolean(SPKey.BOOKSHELF_SHARE_PROMPT, true)
            }
            onPromptClick()
            shareListener?.invoke()
        }

        val isLocalImportPromptGone = SPUtils.getDefaultSharedBoolean(SPKey.BOOKSHELF_IMPORT_PROMPT)
        if (isLocalImportPromptGone) {
            contentView.view_local_import.visibility = View.GONE
        }
        if (Constants.SHARE_SWITCH_ENABLE) {
            val isSharePromptGone = SPUtils.getDefaultSharedBoolean(SPKey.BOOKSHELF_SHARE_PROMPT)
            if (isSharePromptGone) {
                contentView.view_share.visibility = View.GONE
            }
        } else {
            contentView.rl_share.visibility = View.GONE
            contentView.view_share.visibility = View.GONE
        }


    }

    fun setOnDownloadClickListener(listener: (() -> Unit)) {
        downloadListener = listener
    }

    fun setOnSortingClickListener(listener: (() -> Unit)) {
        sortingListener = listener
    }

    fun setOnImportClickListener(listener: (() -> Unit)) {
        importListener = listener
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
        val isLocalImportPromptGone = contentView.view_local_import.visibility == View.GONE
        if (isSharePromptGone && isLocalImportPromptGone) {
            isAllPromptGone = true
        }
        if (isAllPromptGone) {
            promptGoneListener?.invoke()
        }
    }

    fun show(view: View) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        popupWindow.showAtLocation(view, Gravity.TOP, location[0], location[1])
    }
}