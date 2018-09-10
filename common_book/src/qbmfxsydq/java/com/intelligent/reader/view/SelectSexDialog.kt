package com.intelligent.reader.view

import android.app.Activity
import android.view.Gravity
import android.widget.FrameLayout
import com.intelligent.reader.R
import net.lzbook.kit.book.view.MyDialog
import kotlinx.android.synthetic.qbmfxsydq.dialog_select_sex.*
/**
 * Desc 封面页转码阅读
 * Author zhenxiang
 * Mail zhenxiang_lin@dingyuegroup.cn
 * Date 2018\6\13 0013 16:06
 */
class SelectSexDialog(val activity: Activity) {
    private val dialog = MyDialog(activity, R.layout.dialog_select_sex, Gravity.CENTER)

    init {

        val window = dialog.window
        val layoutParams = window.attributes

        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT

        window.attributes = layoutParams

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

    }

    /**
     * sexType 0 男 1 是女
     */
    fun setSex(sexType:Int){
        if(sexType == 0){

            dialog.img_backgroud.setImageResource(R.drawable.select_boy)
            dialog.txt_title.text = activity.getText(R.string.select_boy)

        }else{
            dialog.img_backgroud.setImageResource(R.drawable.select_girl)
            dialog.txt_title.text = activity.getText(R.string.select_girl)

        }
    }


    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
    fun isShow(): Boolean{
        return dialog.isShowing
    }
}