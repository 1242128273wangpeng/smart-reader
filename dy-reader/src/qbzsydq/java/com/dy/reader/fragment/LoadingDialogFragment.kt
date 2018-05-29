package com.dy.reader.fragment

import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.graphics.drawable.ColorDrawable
import android.view.*
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI.activity
import com.dy.reader.R
import com.dy.reader.Reader
import com.dy.reader.helper.AppHelper
import com.dy.reader.util.ThemeUtil
import kotlinx.android.synthetic.main.read_error_page3.*


class LoadingDialogFragment : DialogFragment() {

    enum class DialogType {
        LOADING, ERROR
    }

    var dialogType = DialogType.LOADING

//    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val window = dialog.window
//        val view = inflater?.inflate(R.layout.read_error_page3, window!!.findViewById(android.R.id.content) as ViewGroup, false)
//        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        window.setLayout(-1, -2)
//        return view
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        if (fm.findFragmentByTag(ReadSettingFragment.TAG) == null) {
            dialog.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        dialog.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        dialog.setContentView(R.layout.read_error_page3)
        val window = dialog.window
        window.setGravity(Gravity.CENTER) //可设置dialog的位置
//        window.decorView.setPadding(0, 0, 0, 0) //消除边距
        val lp = window.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = lp
        dialog.setCanceledOnTouchOutside(true)


        dialog.tv_loading_progress?.setTextColor(Reader.context.resources.getColor(ThemeUtil.modeLoadTextColor))

        ThemeUtil.getModePrimaryBackground(activity.resources, dialog.loading_load)
        ThemeUtil.getModePrimaryBackground(activity.resources, dialog.loading_error)


        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnKeyListener(object : DialogInterface.OnKeyListener {
            override
            fun onKey(dialog: DialogInterface, keyCode: Int, event: KeyEvent): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    activity?.onBackPressed()
                    return true
                }
                return false
            }
        })
        dialog.setOnShowListener({
            if (DialogType.LOADING == dialogType) {
                dialog.loading_error?.visibility = View.GONE
                dialog.loading_load?.visibility = View.VISIBLE
            } else {
                dialog.loading_error?.visibility = View.VISIBLE
                dialog.loading_load?.visibility = View.GONE
                dialog.loading_error_reload?.setOnClickListener({
                    dialog.loading_error?.visibility = View.GONE
                    dialog.loading_load?.visibility = View.VISIBLE
                    AppHelper.mainHandler.postDelayed({
                        callback?.invoke()
                    }, 500)
                })
            }
        })

        return dialog
    }

    var callback: (() -> Unit)? = null

    @Volatile
    var isShowing = false

    lateinit var fm: FragmentManager

    fun show(type: DialogType, retry: (() -> Unit)? = null) {
        try {
            this.dialogType = type
            callback = retry
            if (!isShowing) {
                isShowing = true

                if (fm != null) {
                    super.show(fm, "loading")
                }
            } else if (isDialogShowing()) {
                if (DialogType.LOADING == dialogType) {
                    dialog?.loading_error?.visibility = View.GONE
                    dialog?.loading_load?.visibility = View.VISIBLE
                } else {
                    dialog?.loading_error?.visibility = View.VISIBLE
                    dialog?.loading_load?.visibility = View.GONE
                    dialog?.loading_error_reload?.setOnClickListener({
                        dialog?.loading_error?.visibility = View.GONE
                        dialog?.loading_load?.visibility = View.VISIBLE
                        AppHelper.mainHandler.postDelayed({
                            retry?.invoke()
                        }, 500)
                    })
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        isShowing = false
    }

    fun dismissDiaslog(isResumed: Boolean) {
        if (isResumed) {
            if (activity != null && !activity.isFinishing && activity.fragmentManager != null) {
                super.dismiss()
            }
        } else {
            dismissAllowingStateLoss()
        }

    }

    override fun dismissAllowingStateLoss() {
        if (isDialogShowing()) {
            super.dismissAllowingStateLoss()
        }
    }


    /**
     * 判断弹窗是否显示
     * @return
     */
    fun isDialogShowing(): Boolean {
        return dialog != null && dialog.isShowing
    }
}