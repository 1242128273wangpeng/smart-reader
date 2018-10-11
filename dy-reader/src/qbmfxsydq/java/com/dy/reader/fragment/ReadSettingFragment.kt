package com.dy.reader.fragment

import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import com.dy.reader.R
import com.dy.reader.activity.ReaderActivity
import com.dy.reader.event.EventLoading
import com.dy.reader.event.EventReaderConfig
import com.dy.reader.event.EventSetting
import com.dy.reader.page.GLReaderView
import com.dy.reader.presenter.ReadSettingPresenter
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import kotlinx.android.synthetic.qbmfxsydq.frag_read_setting.*
import net.lzbook.kit.ui.activity.base.FrameActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Created by yuchao on 2018/4/29 0029.
 */
class ReadSettingFragment : DialogFragment() {

    companion object {
        const val TAG = "menu"
    }

    private var readSettingPresenter: ReadSettingPresenter? = null

    var fm: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readSettingPresenter = ReadSettingPresenter(act = activity as ReaderActivity)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity, R.style.dialog_menu)

        dialog.setContentView(R.layout.frag_read_setting)
        val window = dialog.window

        window.setGravity(Gravity.CENTER)
        val layoutParams = window.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = layoutParams

        dialog.setCancelable(false)
        dialog.setOnShowListener {
            activity?.window?.decorView?.systemUiVisibility = FrameActivity.UI_OPTIONS_NORMAL
            dialog.rsh_option_header.showMenu(true)
            dialog.rsbd_option_bottom_detail.showMenu(true)

            dialog.rl_read_setting_content.canTouchCallbak = {
                canTouch
            }
        }
        dialog.setOnKeyListener { _, keyCode, event ->

            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (event.action == MotionEvent.ACTION_UP) {
                        activity?.onBackPressed()
                    }
                    true
                }
                KeyEvent.KEYCODE_MENU -> {
                    show(false)
                    ReaderStatus.isMenuShow = false
                    true
                }
                else -> false
            }
        }

        return dialog
    }

    private var canTouch = true

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecieveEvent(event: EventReaderConfig) {
        if(ReaderSettings.instance.animation != GLReaderView.AnimationType.LIST) {
            when (event.type) {
                ReaderSettings.ConfigType.CHAPTER_REFRESH -> {
                    canTouch = false
                }
                ReaderSettings.ConfigType.FONT_REFRESH -> {
                    canTouch = false
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecieveEvent(event: EventLoading) {
        if (event.type == EventLoading.Type.SUCCESS
                || event.type == EventLoading.Type.RETRY) {
            canTouch = true
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.rsbd_option_bottom_detail?.readPresenter = (activity as ReaderActivity).mReadPresenter
        dialog?.rsh_option_header?.presenter = readSettingPresenter
        dialog?.rsbd_option_bottom_detail?.presenter = readSettingPresenter
        dialog?.rsbd_option_bottom_detail?.currentThemeMode = themeMode
        dialog?.rsbd_option_bottom_detail?.setNovelMode(ReaderSettings.instance.readThemeMode)
        dialog?.rl_read_setting_content?.setOnClickListener {
            if(dialog?.isShowing == true){
                dialog?.dismiss()
            }
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        canTouch = true
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecieveEvent(event: EventSetting) {
        if (event.type == EventSetting.Type.REFRESH_MODE) {
            dialog?.rsbd_option_bottom_detail?.setMode()
        } else if (event.type == EventSetting.Type.DISMISS_TOP_MENU) {
            dialog?.rsh_option_header?.showMenu(false)
        }
    }

    fun show(flag: Boolean) {
        try {
            if (flag && !this.isAdded && null == fm?.findFragmentByTag(TAG)) {
                fm?.beginTransaction()?.remove(this)?.commit()
                this.show(fm, TAG)
            } else {
                dismiss()
            }
        }catch (e:Exception){
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        ReaderStatus.isMenuShow = false
        activity?.window?.decorView?.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY
    }

    var themeMode: String? = ""


    fun setCurrentThemeMode(mode: String?) {
        themeMode = mode
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        readSettingPresenter?.clear()
    }
}