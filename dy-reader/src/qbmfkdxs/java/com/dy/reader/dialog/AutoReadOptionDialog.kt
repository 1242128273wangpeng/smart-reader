package com.dy.reader.dialog

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
import kotlinx.android.synthetic.qbmfkdxs.dialog_reader_auto_read_option.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.StatServiceUtils
import java.util.HashMap
import org.greenrobot.eventbus.EventBus

class AutoReadOptionDialog : DialogFragment(), View.OnClickListener {

    private val readerSettings = ReaderSettings.instance
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        
        dialog.setContentView(R.layout.dialog_reader_auto_read_option)
        
        val window = dialog.window

        window.setGravity(Gravity.BOTTOM)
        
        window.decorView.setPadding(0, 0, 0, 0)
        val layoutParams = window.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = layoutParams
        
        dialog.setCanceledOnTouchOutside(true)

        dialog.setOnShowListener {
            activity?.window?.decorView?.systemUiVisibility = FrameActivity.UI_OPTIONS_NORMAL

            dialog.txt_speed_decelerate.setOnClickListener(this)
            dialog.txt_speed_accelerate.setOnClickListener(this)
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
        val i = view.id
        when (i) {
            R.id.txt_speed_decelerate -> {
                StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.rb_click_auto_read_speed_down)
                readerSettings.autoReadSpeed = Math.max(10, readerSettings.autoReadSpeed - 1)
                setRateValue()
            }
            R.id.txt_speed_accelerate -> {
                StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.rb_click_auto_read_speed_up)
                readerSettings.autoReadSpeed = Math.min(20, readerSettings.autoReadSpeed + 1)
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

    private fun setRateValue() {
        dialog?.txt_auto_read_speed?.text = readerSettings.autoReadSpeed.toString()
    }
}