package com.intelligent.reader.activity

import android.os.Bundle
import com.baidu.mobstat.StatService
import com.dingyue.statistics.DyStatService
import com.intelligent.reader.R
import com.intelligent.reader.widget.PushTimeDialog
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import kotlinx.android.synthetic.txtqbmfyd.act_setting_more.*
import net.lzbook.kit.bean.SettingItems
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.SettingItemsHelper
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.logger.AppLog

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
        txt_push_time_setting.isEnabled = btn_push_time.isChecked
        if (settingItems.isSetPushTime) {
            showPushTime(settingItems.pushTimeStartH, settingItems.pushTimeStartMin,
                    settingItems.pushTimeStopH, settingItems.pushTimeStopMin)
        } else {
            txt_push_time_setting.text = "全天推送"
        }

    }

    override fun onResume() {
        super.onResume()
        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
    }

    private fun initListener() {

        img_head_back.setOnClickListener {
            DyStatService.onEvent(EventPoint.MORESET_BACK, mapOf("type" to "1"))
            finish()
        }

        btn_push.setOnCheckedChangeListener { _, isChecked ->
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_more_push)
            DyStatService.onEvent(EventPoint.MORESET_PUSHSET, mapOf("type" to if (isChecked) "1" else "2"))

            changePushStatus(isChecked)
        }


        btn_push_sound.setOnCheckedChangeListener { _, isChecked ->
            DyStatService.onEvent(EventPoint.MORESET_PUSHAUDIO, mapOf("type" to if (isChecked) "1" else "2"))
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_more_push_voi)

            setPushSound(isChecked, isChecked)
        }


        btn_push_time.setOnCheckedChangeListener { _, isChecked ->
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_more_push_time)

            setPushTime(btn_push.isChecked, isChecked)
        }

        txt_push_time_setting.setOnClickListener {
            AppLog.e(tag, "showDialog")
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

        settingItemsHelper.putBoolean(settingItemsHelper.openBookPush, status)

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
            } else {
                txt_push_time_setting.text = "全天推送"
            }
            settingItemsHelper.putBoolean(settingItemsHelper.setPushTime, isStartPush)
            txt_push_time_setting.isEnabled = isStartPush
        } else {
            settingItemsHelper.putBoolean(settingItemsHelper.setPushTime, isStartPush)
            txt_push_time_setting.isEnabled = false
            txt_push_time_setting.text = "无推送"
        }

    }

    private fun showPushTime(startHour: Int, startMinute: Int, stopHour: Int, stopMinute: Int) {
        val timeStr = "推送时间：${startHour.format()}:${startMinute.format()}" +
                "-${stopHour.format()}:${stopMinute.format()}"
        txt_push_time_setting.text = timeStr
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
