package com.dy.reader.dialog

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import com.ding.basic.bean.Source
import com.dy.reader.R
import com.dy.reader.adapter.SourceAdapter
import com.dy.reader.listener.SourceClickListener
import kotlinx.android.synthetic.main.dialog_book_end_change_source.*
import net.lzbook.kit.widget.MyDialog

/**
 * Desc 请描述这个文件
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/18 09:53
 */
class BookEndChangeSourceDialog(activity: Activity, sourceClickListener: SourceClickListener) {

    private val dialog = MyDialog(activity, R.layout.dialog_book_end_change_source)

    private var sourceList: ArrayList<Source> = ArrayList()

    private var sourceAdapter: SourceAdapter = SourceAdapter(sourceList, sourceClickListener)

    init {
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.recl_change_source.adapter = sourceAdapter
        dialog.recl_change_source.layoutManager = LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false)

        dialog.btn_cancel.setOnClickListener {
            dismiss()
        }
        dialog.btn_confirm.setOnClickListener {
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