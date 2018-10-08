package com.dy.reader.fragment

import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import com.dy.reader.R
import com.dy.reader.helper.AppHelper
import kotlinx.android.synthetic.main.frag_loading_dialog.*

class LoadingDialogFragment : DialogFragment() {

    enum class DialogType {
        LOADING, ERROR
    }

    private var dialogType = DialogType.LOADING

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        if (fm?.findFragmentByTag(ReadSettingFragment.TAG) == null) {
            dialog.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        dialog.setContentView(R.layout.frag_loading_dialog)

        val window = dialog.window
        window.setGravity(Gravity.CENTER) //可设置dialog的位置
//        window.decorView.setPadding(0, 0, 0, 0) //消除边距
        val lp = window.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = lp
        dialog.setCanceledOnTouchOutside(true)


        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                activity?.onBackPressed()
                return@OnKeyListener true
            }
            false
        })
        dialog.setOnShowListener {
            if (DialogType.LOADING == dialogType) {
                showLoading()
            } else {
                showReload()
            }
        }

        return dialog
    }

    var callback: (() -> Unit)? = null

    @Volatile
    var isShowing = false

    var fm: FragmentManager? = null

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
                    showLoading()
                } else {
                    showReload()
                }
            }
        } catch (e: Exception) {
        }
    }

    /**
     * 显示加载布局
     */
    fun showLoading() {
        dialog?.ll_loading_content?.visibility = View.VISIBLE
        val loading = dialog?.img_anim?.drawable
        if (loading is AnimationDrawable) {
            loading.start()
        }
        dialog?.ll_reload_content?.visibility = View.GONE
    }

    /**
     * 显示重新加载布局
     */
    fun showReload() {
        dialog?.ll_loading_content?.visibility = View.GONE
        val loading = dialog?.img_anim?.drawable
        if (loading is AnimationDrawable) {
            loading.stop()
        }
        dialog?.ll_reload_content?.visibility = View.VISIBLE
        dialog?.ll_reload_content?.setOnClickListener {
            showLoading()
            AppHelper.mainHandler.postDelayed({
                callback?.invoke()
            }, 500)
        }


    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        val loading = this.dialog?.img_anim?.drawable
        if (loading is AnimationDrawable) {
            loading.stop()
        }
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