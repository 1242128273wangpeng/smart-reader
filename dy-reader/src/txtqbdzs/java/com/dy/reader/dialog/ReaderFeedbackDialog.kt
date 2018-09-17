package com.dy.reader.dialog

import android.widget.CheckBox
import android.widget.FrameLayout
import com.dingyue.contract.util.showToastMessage
import com.dy.reader.R
import com.dy.reader.activity.ReaderActivity
import com.dy.reader.setting.ReaderSettings
import kotlinx.android.synthetic.txtqbdzs.dialog_reader_feedback.*
import net.lzbook.kit.book.view.MyDialog

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/2 16:14
 */

class ReaderFeedbackDialog(readerActivity: ReaderActivity) {

    private val dialog = MyDialog(readerActivity, R.layout.dialog_reader_feedback)

    private var submitListener: ((type: Int) -> Unit)? = null
    private var cancelListener: (() -> Unit)? = null

    private var checkedView: CheckBox? = null
    private var checkedPosition: Int = -1

    init {
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.cb_feedback_chapter_empty.setOnClickListener {
            checkedPosition = 1

            if (checkedView != null) {
                checkedView?.isChecked = false
            }

            checkedView = it as CheckBox?
        }

        dialog.cb_feedback_reading_cache_error.setOnClickListener {
            checkedPosition = 2

            if (checkedView != null) {
                checkedView?.isChecked = false
            }

            checkedView = it as CheckBox?
        }

        dialog.cb_feedback_chapter_content_repeat.setOnClickListener {
            checkedPosition = 3

            if (checkedView != null) {
                checkedView?.isChecked = false
            }

            checkedView = it as CheckBox?
        }

        dialog.cb_feedback_chapter_sequence_error.setOnClickListener {
            checkedPosition = 4

            if (checkedView != null) {
                checkedView?.isChecked = false
            }

            checkedView = it as CheckBox?
        }

        dialog.cb_feedback_chapter_content_error.setOnClickListener {
            checkedPosition = 5

            if (checkedView != null) {
                checkedView?.isChecked = false
            }

            checkedView = it as CheckBox?
        }

        dialog.cb_feedback_typesetting_error.setOnClickListener {
            checkedPosition = 6

            if (checkedView != null) {
                checkedView?.isChecked = false
            }

            checkedView = it as CheckBox?
        }

        dialog.cb_feedback_cache_fail.setOnClickListener {
            checkedPosition = 7

            if (checkedView != null) {
                checkedView?.isChecked = false
            }

            checkedView = it as CheckBox?
        }


        dialog.btn_feedback_submit.setOnClickListener {
            if (checkedPosition == -1) {
                readerActivity.applicationContext.showToastMessage("请选择错误类型！")
            } else {
                submitListener?.invoke(checkedPosition)
            }

            dismiss()
        }

        dialog.btn_feedback_cancel.setOnClickListener {

            cancelListener?.invoke()

            checkedView = null
            checkedPosition = -1

            dismiss()
        }
    }

    fun insertSubmitListener(submitListener: (type: Int) -> Unit) {
        this.submitListener = submitListener
    }

    fun insertCancelListener(cancelListener: () -> Unit) {
        this.cancelListener = cancelListener
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}