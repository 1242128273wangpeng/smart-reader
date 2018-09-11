package com.dy.reader.view

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.PopupWindow
import com.dy.reader.R
import com.dy.reader.adapter.FontAdapter
import com.dy.reader.model.FontData
import com.dy.reader.service.FontDownLoadService
import kotlinx.android.synthetic.txtqbdzs.reader_option_font_layout.view.*


/**
 * Function：字体下载弹框
 *
 * Created by JoannChen on 2018/9/10 0010 22:02
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
@SuppressLint("InflateParams")
class FontPopupWindow(context: Context?) : PopupWindow(context) {

    init {
        contentView = LayoutInflater.from(context)
                .inflate(R.layout.reader_option_font_layout,null,false)

        contentView.recyclerView.layoutManager = LinearLayoutManager(context)

        val list = arrayListOf<FontData>()
        list.add(FontData("中文",""))
        list.add(FontData("英文",""))
        list.add(FontData("韩文",""))
        list.add(FontData("日文",""))

        val fontAdapter = FontAdapter(list)
        contentView.recyclerView.adapter = fontAdapter
        fontAdapter.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            FontDownLoadService().downLoad("我的字体啊","http://gyxz.hwm6b6.cn/hk/rj_hq1/koudaiyoushu.apk")
        }
    }
}