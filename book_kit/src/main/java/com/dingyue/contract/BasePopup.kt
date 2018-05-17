package com.dingyue.contract

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow

/**
 * Desc popupWindow 基类
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/25 0025 16:46
 */
open class BasePopup(var context: Context, var layout: Int, var width: Int, var height: Int) {

    protected val contentView: View = LayoutInflater.from(context).inflate(layout, null)

    protected val popupWindow = PopupWindow(contentView)

    init {
        popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000))
        popupWindow.width = width
        popupWindow.height = height
    }

    fun showAsDropDown(view: View, x: Int = 0, y: Int = 0) {
        popupWindow.showAsDropDown(view, x, y)
    }

    fun showAtLocation(parent: View, gravity: Int = Gravity.BOTTOM, x: Int = 0, y: Int = 0) {
        popupWindow.showAtLocation(parent, gravity, x, y)
    }

    open fun dismiss() {
        popupWindow.dismiss()
    }
}