package com.dy.reader.dialog

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.view.ViewGroup
import com.ding.basic.bean.Source
import com.dy.reader.R
import com.dy.reader.adapter.SourceAdapter
import com.dy.reader.listener.SourceClickListener
import kotlinx.android.synthetic.qbmfrmxs.dialog_reader_chang_source.*
import net.lzbook.kit.book.view.MyDialog

/**
 * Desc 换源 dialog
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/18 09:53
 */
class BookEndChangeSourceDialog(activity: Activity, sourceClickListener: SourceClickListener) {

    private val dialog = MyDialog(activity, R.layout.dialog_reader_chang_source)

    private var sourceList: ArrayList<Source> = ArrayList()

    private var sourceAdapter: SourceAdapter = SourceAdapter(sourceList, sourceClickListener)

    init {
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        val params = dialog.window.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog.window.attributes = params

        dialog.recl_change_source_content.adapter = sourceAdapter
        dialog.recl_change_source_content.layoutManager = LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false)

        dialog.txt_change_source_cancel.setOnClickListener {
            dismiss()
        }
        dialog.txt_change_source_continue.setOnClickListener {
            dismiss()
        }

    }

    fun show(list: ArrayList<Source>) {
        sourceList.clear()
        sourceList.addAll(list)
        sourceAdapter.notifyDataSetChanged()
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

}