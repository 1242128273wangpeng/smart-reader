package com.dy.reader.dialog

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.ding.basic.bean.Source
import com.dy.reader.R
import com.dy.reader.adapter.SourceAdapter
import com.dy.reader.listener.SourceClickListener
import com.dy.reader.setting.ReaderSettings
import kotlinx.android.synthetic.qbmfxsydq.dialog_reader_chang_source.*
import net.lzbook.kit.ui.widget.MyDialog
import java.util.ArrayList

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/4 17:31
 */

class ReaderChangeSourceDialog(activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_reader_chang_source)

    private var continueListener: (() -> Unit)? = null
    private var cancelListener: (() -> Unit)? = null

    private var sourceAdapter: SourceAdapter? = null

    init {

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.txt_change_source_continue.setOnClickListener {
            continueListener?.invoke()
        }

        dialog.txt_change_source_cancel.setOnClickListener {
            cancelListener?.invoke()
        }
    }

    fun insertContinueListener(continueListener: () -> Unit) {
        this.continueListener = continueListener
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

    fun insertChangeSourcePrompt(prompt: String) {
        dialog.txt_change_source_prompt.text = prompt
        dialog.txt_change_source_cancel.visibility = View.INVISIBLE
        dialog.txt_change_source_continue.setText(R.string.skip_chapter)
    }

    fun showSourceList(activity: Activity, sourceList: ArrayList<Source>, sourceClickListener: SourceClickListener) {

        if (sourceList.isNotEmpty()) {

            if (ReaderSettings.instance.isLandscape) {
                if (sourceList.size > 1) {
                    dialog.recl_change_source_content.layoutParams.height = activity.resources.getDimensionPixelOffset(R.dimen.dimen_view_height_80)
                }
            } else {
                if (sourceList.size > 3) {
                    dialog.recl_change_source_content.layoutParams.height = activity.resources.getDimensionPixelOffset(R.dimen.dimen_view_height_135)
                }
            }

            sourceAdapter = SourceAdapter(sourceList, sourceClickListener)

            val linearLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

            dialog.recl_change_source_content.adapter = sourceAdapter

            dialog.recl_change_source_content.layoutManager = linearLayoutManager
        }
    }
}