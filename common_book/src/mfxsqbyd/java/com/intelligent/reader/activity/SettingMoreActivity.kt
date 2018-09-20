package com.intelligent.reader.activity

import android.os.Bundle
import android.view.View
import com.intelligent.reader.R
import com.intelligent.reader.R.id.*
import com.intelligent.reader.widget.PushTimeDialog

import kotlinx.android.synthetic.mfxsqbyd.act_setting_more.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.base.activity.BaseCacheableActivity
import net.lzbook.kit.bean.SettingItems

import net.lzbook.kit.utils.SettingItemsHelper
import net.lzbook.kit.utils.StatServiceUtils
import java.util.*

class SettingMoreActivity : BaseCacheableActivity() {

    private val tag = SettingMoreActivity::class.java.simpleName

    private lateinit var settingItemsHelper: SettingItemsHelper
    private lateinit var settingItems: SettingItems

    private val pushTimeDialog: PushTimeDialog by lazy {
        val dialog = PushTimeDialog(this)
        dialog.setOnConfirmListener { startHour, startMinute, stopHour, stopMinute ->
            showPushTime(startHour, startMinute, stopHour, stopMinute)
            saveTime(startHour, startMinute, stopHour, stopMinute)
        }
        dialog
    }

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_setting_more)
        initData()
        initListener()
    }

    private fun initData() {
        settingItemsHelper = SettingItemsHelper.getSettingHelper(applicationContext)
        settingItems = settingItemsHelper.values
        btn_push.isChecked = settingItems.isUmengPush
        btn_push_sound.isChecked = settingItems.isSoundOpen
        btn_push_time.isChecked = settingItems.isSetPushTime

        rl_choice_time.isEnabled = btn_push_time.isChecked
        if (settingItems.isSetPushTime) {
            showPushTime(settingItems.pushTimeStartH, settingItems.pushTimeStartMin,
                    settingItems.pushTimeStopH, settingItems.pushTimeStopMin)
            rl_choice_time.visibility = View.VISIBLE
        } else {
            rl_choice_time.visibility = View.GONE
        }

    }

    private fun initListener() {

        img_head_back.setOnClickListener {
            val data = HashMap<String, String>()
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.MORESET_PAGE, StartLogClickUtil.BACK, data)
            finish()
        }

        btn_push.setOnCheckedChangeListener { _, isChecked ->
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_more_push)
            val params1 = HashMap<String, String>()
            params1.put("type", if (isChecked) "1" else "2")
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.MORESET_PAGE, StartLogClickUtil.PUSHSET, params1)

            changePushStatus(isChecked)
        }


        btn_push_sound.setOnCheckedChangeListener { _, isChecked ->
            val params = HashMap<String, String>()
            params.put("type", if (isChecked) "1" else "2")
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.MORESET_PAGE, StartLogClickUtil.PUSHAUDIO, params)
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_more_push_voi)

            setPushSound(isChecked, isChecked)
        }


        btn_push_time.setOnCheckedChangeListener { _, isChecked ->
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_more_push_time)

            setPushTime(btn_push.isChecked, isChecked)
        }

        rl_choice_time.setOnClickListener {
            pushTimeDialog.show(settingItems)
        }

    }

    private fun changePushStatus(status: Boolean) {
        if (status) {
            //因为 enable 为 false，所以先打开 enable，后改变 check
            btn_push_sound.isEnabled = status
            btn_push_sound.isChecked = status

            btn_push_time.isEnabled = status
            btn_push_time.isChecked = status
        } else {
            //先改变 check，后关闭 enable
            btn_push_sound.isChecked = status
            btn_push_sound.isEnabled = status

            btn_push_time.isChecked = status
            btn_push_time.isEnabled = status
        }
        setPushSound(status, status)
        setPushTime(status, status)
    }

    private fun setPushSound(isChecked: Boolean, isStartPush: Boolean) {
        if (isChecked) {
            if (isStartPush) {
                settingItemsHelper.putBoolean(settingItemsHelper.openPushSound, true)
            } else {
                settingItemsHelper.putBoolean(settingItemsHelper.openPushSound, false)
            }

        } else {
            settingItemsHelper.putBoolean(settingItemsHelper.openPushSound, isStartPush)
        }
    }

    private fun setPushTime(isChecked: Boolean, isStartPush: Boolean) {
        if (isChecked) {
            if (isStartPush) {
                showPushTime(settingItems.pushTimeStartH, settingItems.pushTimeStartMin,
                        settingItems.pushTimeStopH, settingItems.pushTimeStopMin)
                rl_choice_time.visibility = View.VISIBLE
            } else {
                rl_choice_time.visibility = View.GONE
            }
            settingItemsHelper.putBoolean(settingItemsHelper.setPushTime, isStartPush)
            rl_choice_time.isEnabled = isStartPush
        } else {
            settingItemsHelper.putBoolean(settingItemsHelper.setPushTime, isStartPush)
            rl_choice_time.isEnabled = false
            rl_choice_time.visibility = View.GONE
        }

    }

    private fun showPushTime(startHour: Int, startMinute: Int, stopHour: Int, stopMinute: Int) {
        val startTime = "${startHour.format()}:${startMinute.format()}"
        val stopTime = "${stopHour.format()}:${stopMinute.format()}"

        tv_start_time.text = startTime
        tv_stop_time.text = stopTime
    }


    private fun saveTime(startHour: Int, startMinute: Int, stopHour: Int, stopMinute: Int) {
        settingItemsHelper.putInt(settingItemsHelper.pushTimeStartH, startHour)
        settingItemsHelper.putInt(settingItemsHelper.pushTimeStartMin, startMinute)
        settingItemsHelper.putInt(settingItemsHelper.pushTimeStopH, stopHour)
        settingItemsHelper.putInt(settingItemsHelper.pushTimeStopMin, stopMinute)
    }

    private fun Int.format(): String {
        return if (this < 10) {
            "0$this"
        } else {
            this.toString()
        }
    }

}
