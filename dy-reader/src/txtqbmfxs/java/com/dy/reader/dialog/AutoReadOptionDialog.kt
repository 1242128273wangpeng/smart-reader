package com.dy.reader.dialog

import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import com.dingyue.statistics.DyStatService
import com.dy.reader.R
import com.dy.reader.event.EventReaderConfig
import com.dy.reader.setting.ReaderSettings
import kotlinx.android.synthetic.txtqbmfxs.dialog_reader_auto_read_option.*
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.ui.activity.base.FrameActivity
import net.lzbook.kit.utils.StatServiceUtils
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

            dialog.ll_exit.setOnClickListener(this)

            dialog.skbar_speed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    dialog?.txt_speed?.text = (progress + 10).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    readerSettings.autoReadSpeed = seekBar?.progress ?: 0 + 10
                }
            })

            dialog.skbar_speed.progress = readerSettings.autoReadSpeed

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
        if (readerSettings.isAutoReading) {
            EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.AUTO_RESUME))
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.txt_speed?.text = readerSettings.autoReadSpeed.toString()
    }

    override fun onClick(view: View) {
        val i = view.id
        when (i) {
            R.id.ll_exit -> {
                DyStatService.onEvent(EventPoint.READPAGESET_AUTOREAD, mapOf("type" to "2"))
                StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.rb_click_auto_read_cancel)
                readerSettings.isAutoReading = false
                dismiss()
            }
        }
    }
}