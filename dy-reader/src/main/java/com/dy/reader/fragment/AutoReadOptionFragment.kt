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
import kotlinx.android.synthetic.txtqbmfyd.read_option_autoread.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.StatServiceUtils
import java.util.HashMap
import kotlinx.android.synthetic.txtqbmfyd.read_option_autoread.view.*
import org.greenrobot.eventbus.EventBus

class AutoReadOptionFragment : DialogFragment(), View.OnClickListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.read_option_autoread)
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

            dialog.autoread_up.setOnClickListener(this)
            dialog.autoread_down.setOnClickListener(this)
            dialog.autoread_stop.setOnClickListener(this)

            EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.AUTO_PAUSE))
        }
        dialog.setOnKeyListener { dialog, keyCode, event ->

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

    val readerSettings = ReaderSettings.instance

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        activity?.window?.decorView?.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY
        if(readerSettings.isAutoReading){
            EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.AUTO_RESUME))
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.autoread_rate?.setText(readerSettings.autoReadSpeed.toString())
    }

    override fun onClick(view: View) {
        val i = view.id
        if (i == R.id.autoread_up) {
            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.rb_click_auto_read_speed_up)
            readerSettings.autoReadSpeed = Math.min(20, readerSettings.autoReadSpeed + 1)
            setRateValue()

        } else if (i == R.id.autoread_down) {
            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.rb_click_auto_read_speed_down)
            readerSettings.autoReadSpeed = Math.max(10, readerSettings.autoReadSpeed - 1)
            setRateValue()

        } else if (i == R.id.autoread_stop) {
            val data = HashMap<String, String>()
            data["type"] = "2"
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.AUTOREAD, data)
            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.rb_click_auto_read_cancel)
            readerSettings.isAutoReading = false
            dismiss()
        }
    }

    fun setRateValue() {
        dialog?.autoread_rate?.setText(readerSettings.autoReadSpeed.toString())
    }
}