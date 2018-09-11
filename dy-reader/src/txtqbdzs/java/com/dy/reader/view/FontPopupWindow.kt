package com.dy.reader.view

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.PopupWindow
import com.dingyue.contract.BasePopup
import com.dy.reader.R
import com.dy.reader.adapter.FontAdapter
import com.dy.reader.helper.AppHelper
import com.dy.reader.model.FontData
import com.dy.reader.service.FontDownLoadService
import com.dy.reader.setting.ReaderSettings
import kotlinx.android.synthetic.txtqbdzs.reader_option_font_layout.view.*


/**
 * Function：字体下载弹框
 *
 * Created by JoannChen on 2018/9/10 0010 22:02
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
@SuppressLint("InflateParams")
class FontPopupWindow(context: Context, layout: Int = R.layout.reader_option_font_layout,
                      width: Int = WindowManager.LayoutParams.MATCH_PARENT,
                      height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height) {

    private val list = arrayListOf<FontData>()
    private val fontAdapter = FontAdapter(list)

    init {

        list.add(FontData("默认字体", ""))
        list.add(FontData("思源宋体", ""))
        list.add(FontData("全字库正楷体", ""))
        list.add(FontData("杨任东竹石体", ""))
        list.add(FontData("思源黑体", ""))
        contentView.recyclerView.adapter = fontAdapter
        fontAdapter.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            FontDownLoadService().downLoad("我的字体啊", "http://gyxz.hwm6b6.cn/hk/rj_hq1/koudaiyoushu.apk")
        }
    }

    fun show(parent: View) {
        if (ReaderSettings.instance.isLandscape) {
            contentView.recyclerView.layoutManager = GridLayoutManager(context, 2)
        } else {
            contentView.recyclerView.layoutManager = LinearLayoutManager(context)
        }
        showAtLocation(parent, Gravity.BOTTOM)
    }
}