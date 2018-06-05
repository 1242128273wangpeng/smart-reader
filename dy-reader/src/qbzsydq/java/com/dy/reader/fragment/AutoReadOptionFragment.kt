package com.dy.reader.fragment

import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import com.dy.reader.R
import com.dy.reader.event.EventReaderConfig
import com.dy.reader.setting.ReaderSettings
import iyouqu.theme.FrameActivity
import kotlinx.android.synthetic.qbzsydq.reader_option_autoread.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.StatServiceUtils
import java.util.HashMap
import org.greenrobot.eventbus.EventBus

class AutoReadOptionFragment : DialogFragment(), View.OnClickListener {

    private val readerSettings = ReaderSettings.instance

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.reader_option_autoread)
        val window = dialog.window

        window.setGravity(Gravity.BOTTOM) //可设置dialog的位置
        window.decorView.setPadding(0, 0, 0, 0) //消除边距
        val lp = window.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = lp
        dialog.setCanceledOnTouchOutside(true)

        dialog.setOnShowListener {
            activity?.window?.decorView?.systemUiVisibility = FrameActivity.UI_OPTIONS_NORMAL

            dialog.txt_speed_accelerate.setOnClickListener(this)
            dialog.txt_speed_decelerate.setOnClickListener(this)
            dialog.txt_auto_read_stop.setOnClickListener(this)

            EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.AUTO_PAUSE))
        }
        dialog.setOnKeyListener { _, keyCode, event ->

            if (KeyEvent.KEYCODE_BACK == keyCode) {
                if (event.action == MotionEvent.ACTION_UP) {
                    dismiss()
                }
                true
            } else {
                false
            }
        }
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        activity?.window?.decorView?.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY
        if(readerSettings.isAutoReading){
            EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.AUTO_RESUME))
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.txt_auto_read_speed?.text = readerSettings.autoReadSpeed.toString()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.txt_speed_accelerate -> {
                StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.rb_click_auto_read_speed_up)
                readerSettings.autoReadSpeed = Math.min(20, readerSettings.autoReadSpeed + 1)
                setRateValue()

            }
            R.id.txt_speed_decelerate -> {
                StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.rb_click_auto_read_speed_down)
                readerSettings.autoReadSpeed = Math.max(10, readerSettings.autoReadSpeed - 1)
                setRateValue()

            }
            R.id.txt_auto_read_stop -> {
                val data = HashMap<String, String>()
                data["type"] = "2"
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.AUTOREAD, data)
                StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.rb_click_auto_read_cancel)
                readerSettings.isAutoReading = false
                dismiss()
            }
        }
    }

    fun setRateValue() {
        dialog?.txt_auto_read_speed?.text = readerSettings.autoReadSpeed.toString()
    }
}