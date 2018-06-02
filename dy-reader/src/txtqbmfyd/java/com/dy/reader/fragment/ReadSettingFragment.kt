package com.dy.reader.fragment

import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
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
import iyouqu.theme.FrameActivity
import kotlinx.android.synthetic.txtqbmfyd.read_setting_layout.*
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.CallBackDownload
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Created by yuchao on 2018/4/29 0029.
 */
class ReadSettingFragment : DialogFragment() , CallBackDownload {
    override fun onTaskStatusChange(book_id: String?) {
        if(dialog != null){
            dialog.readSettingHeader.setBookDownLoadState(book_id)
        }

    }

    override fun onTaskFinish(book_id: String?) {
        if(dialog != null){
            dialog.readSettingHeader.setBookDownLoadState(book_id)
        }

    }

    override fun onTaskFailed(book_id: String?, t: Throwable?) {
    }

    override fun onTaskProgressUpdate(book_id: String?) {

    }

    companion object {
        const val TAG = "menu"
    }

    private var mPresenter: ReadSettingPresenter? = null

    var fm: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter = ReadSettingPresenter(act = activity as ReaderActivity)


    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity, R.style.dialog_menu)

//        dialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE)
//        dialog.window.setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        dialog.setContentView(R.layout.read_setting_layout)
        val window = dialog.window

        window.setGravity(Gravity.CENTER) //可设置dialog的位置
//        window.decorView.setPadding(0, 0, 0, 0) //消除边距
        val lp = window.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = lp

        dialog.setCancelable(false)
        dialog.setOnShowListener {
            activity?.window?.decorView?.systemUiVisibility = FrameActivity.UI_OPTIONS_NORMAL
            dialog.readSettingHeader.showMenu(true)
            dialog.readSettingBottomDetail.showMenu(true)

            //拦截连点
            dialog.read_setting_root.canTouchCallbak = {
                canTouch
            }
        }
        dialog.setOnKeyListener { dialog, keyCode, event ->

            if (KeyEvent.KEYCODE_BACK == keyCode) {
                if (event.action == MotionEvent.ACTION_UP) {
                    activity?.finish()
                }
                true
            } else {
                false
            }
        }
        if (!TextUtils.isEmpty(ReaderStatus.book.book_id)) {
            dialog.readSettingHeader.setBookDownLoadState(ReaderStatus.book.book_id)
            CacheManager.listeners.add(this)
        }
        return dialog
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
//            inflater.inflate(R.layout.read_setting_layout, container, false)

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
        dialog.readSettingBottomDetail.readPresenter = (activity as ReaderActivity).mReadPresenter
        dialog.readSettingHeader.presenter = mPresenter
        dialog.readSettingBottomDetail.presenter = mPresenter
        dialog.readSettingBottomDetail.currentThemeMode = themeMode
        dialog.readSettingBottomDetail.setNovelMode(ReaderSettings.instance.readThemeMode)
        dialog.read_setting_root.setOnClickListener {
            dismiss()
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
            dialog.readSettingBottomDetail.setMode()
        } else if (event.type == EventSetting.Type.DISMISS_TOP_MENU) {
            dialog.readSettingHeader.showMenu(false)
        }
    }

    fun show(flag: Boolean) {
        try {
            if (flag && !this.isAdded && null == fm?.findFragmentByTag(TAG)) {
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
        mPresenter?.clear()
    }

}