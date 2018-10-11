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
import net.lzbook.kit.ui.activity.base.FrameActivity
import kotlinx.android.synthetic.txtqbmfyd.frag_read_setting.*
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.download.CallBackDownload
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Created by yuchao on 2018/4/29 0029
 */
class ReadSettingFragment : DialogFragment() , CallBackDownload {
    override fun onTaskStatusChange(book_id: String?) {
      /*  if(dialog != null){
            dialog.rsh_option_header.setBookDownLoadState(book_id)
        }*/

    }

    override fun onTaskFinish(book_id: String?) {
       /* if(dialog != null){
            dialog.rsh_option_header.setBookDownLoadState(book_id)
        }
*/
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

        dialog.setContentView(R.layout.frag_read_setting)
        val window = dialog.window

        window.setGravity(Gravity.CENTER) //可设置dialog的位置
        val lp = window.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = lp

        dialog.setCancelable(false)
        dialog.setOnShowListener {
            activity?.window?.decorView?.systemUiVisibility = FrameActivity.UI_OPTIONS_NORMAL
            dialog.rsh_option_header.showMenu(true)
            dialog.rsbd_option_bottom_detail.showMenu(true)

            //拦截连点
            dialog.rl_read_setting_content.canTouchCallbak = {
                canTouch
            }
        }
        dialog.setOnKeyListener { dialog, keyCode, event ->

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
        if (!TextUtils.isEmpty(ReaderStatus.book.book_id)) {
//            dialog.rsh_option_header.setBookDownLoadState(ReaderStatus.book.book_id)
            CacheManager.listeners.add(this)
        }
        return dialog
    }

    private var canTouch = true

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecieveEvent(event: EventReaderConfig) {

        /*if(event.type == ReaderSettings.ConfigType.CHAPTER_SUCCESS ){
            if (ReaderStatus.position.group == -1) {
                if (dialog.novel_hint_chapter != null) {
                    dialog.novel_hint_chapter!!.text = "封面"
                }
            } else {
                if (dialog.novel_hint_chapter != null) {
                    dialog.novel_hint_chapter!!.text = if (TextUtils.isEmpty(ReaderStatus.chapterName)) "" else ReaderStatus.chapterName
                }
            }
        }*/

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
        dialog?.rsh_option_header?.presenter = mPresenter
        dialog?.rsbd_option_bottom_detail?.presenter = mPresenter
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