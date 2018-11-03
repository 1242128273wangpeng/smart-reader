package com.dingyue.statistics.utils

import android.os.Handler
import android.os.Message
import android.support.annotation.StringRes
import android.widget.Toast

import com.dingyue.statistics.common.GlobalContext

/**
 * Desc toast辅助类
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/9/15 10:34
 */
class ToastUtil : Handler() {

    val TOAST_MAG_WHAT = 1

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        if (msg.what == TOAST_MAG_WHAT && msg.obj != null) {
            if (msg.obj is Int) showToastMessage(msg.obj as Int)
            else if (msg.obj is String) showToastMessage(msg.obj as String)
        }
    }

    private var mToast: Toast? = null

    /**
     * 发送消息
     */
    fun postMessage(message: String) {
        sendMessage(obtainMessage(TOAST_MAG_WHAT, message))
    }

    /**
     * 发送消息
     */
    fun postMessage(@StringRes message: Int) {
        sendMessage(obtainMessage(TOAST_MAG_WHAT, message))
    }

    /***
     * 展示Toast
     */
    private fun showToastMessage(@StringRes id: Int) {
        if (mToast == null) {
            mToast = Toast.makeText(GlobalContext.getGlobalContext(), id, Toast.LENGTH_SHORT)
            mToast!!.show()
        } else {
            mToast!!.setText(id)
            mToast!!.duration = Toast.LENGTH_SHORT
            mToast!!.show()
        }
    }

    /***
     * 展示Toast
     */
    private fun showToastMessage(message: String) {
        if (mToast == null) {
            mToast = Toast.makeText(GlobalContext.getGlobalContext(), message, Toast.LENGTH_SHORT)
            mToast!!.show()
        } else {
            mToast!!.setText(message)
            mToast!!.duration = Toast.LENGTH_SHORT
            mToast!!.show()
        }
    }
}