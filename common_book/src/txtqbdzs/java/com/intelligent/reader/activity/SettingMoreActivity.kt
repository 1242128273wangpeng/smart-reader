package com.intelligent.reader.activity

import android.os.Bundle
import com.dingyue.statistics.DyStatService
import com.intelligent.reader.R
import com.intelligent.reader.view.PushTimeDialog
import com.umeng.message.IUmengCallback
import com.umeng.message.MsgConstant
import com.umeng.message.PushAgent
import kotlinx.android.synthetic.txtqbdzs.act_setting_more.*
import net.lzbook.kit.bean.SettingItems
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.utils.SettingItemsHelper
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.logger.AppLog


class SettingMoreActivity : BaseCacheableActivity() {

    private val tag = SettingMoreActivity::class.java.simpleName

    private lateinit var settingItemsHelper: SettingItemsHelper
    private lateinit var settingItems: SettingItems

    private val pushAgent by lazy {
        PushAgent.getInstance(this)
    }

    private val pushTimeDialog: PushTimeDialog by lazy {
        val dialog = PushTimeDialog(this)
        dialog.setOnConfirmListener { startHour, startMinute, endHour, endMinute ->
            showPushTime(startHour, startMinute, endHour, endMinute)
            saveTime(startHour, startMinute, endHour, endMinute)
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
        btn_book_update_push.isChecked = settingItems.isBookUpdatePush
        btn_umeng_push.isChecked = settingItems.isUmengPush
        btn_push_sound.isChecked = settingItems.isSoundOpen
        btn_push_time.isChecked = settingItems.isSetPushTime
        setBookShelfSort(settingItems.booklist_sort_type)
        txt_push_time_setting.isEnabled = btn_push_time.isChecked
        if (settingItems.isSetPushTime) {
            showPushTime(settingItems.pushTimeStartH, settingItems.pushTimeStartMin,
                    settingItems.pushTimeStopH, settingItems.pushTimeStopMin)
        } else {
            txt_push_time_setting.text = "全天推送"
        }

    }

    private fun initListener() {

        img_head_back.setOnClickListener {
            DyStatService.onEvent(EventPoint.MORESET_BACK, mapOf("type" to "1"))
            finish()
        }

        btn_book_update_push.setOnCheckedChangeListener { _, isChecked ->
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_more_push)
            DyStatService.onEvent(EventPoint.MORESET_PUSHSET, mapOf("type" to if (isChecked) "1" else "2"))

            settingItemsHelper.putBoolean(settingItemsHelper.openBookPush, isChecked)
            changePushStatus()
        }

        btn_umeng_push.setOnCheckedChangeListener { _, isChecked ->
            settingItemsHelper.putBoolean(settingItemsHelper.openUmengPush, isChecked)
            changeUmengStatus(isChecked)
            changePushStatus()
        }

        btn_push_sound.setOnCheckedChangeListener { _, isChecked ->
            DyStatService.onEvent(EventPoint.MORESET_PUSHAUDIO, mapOf("type" to if (isChecked) "1" else "2"))
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_more_push_voi)

            setPushSound(isChecked, isChecked)
        }


        btn_push_time.setOnCheckedChangeListener { _, isChecked ->
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_more_push_time)

            setPushTime(btn_book_update_push.isChecked, isChecked)
        }

        txt_push_time_setting.setOnClickListener {
            AppLog.e(tag, "showDialog")
            pushTimeDialog.show(settingItems)
        }

        rl_book_sort_read.setOnClickListener {
            StatServiceUtils.statAppBtnClick(this,
                    StatServiceUtils.me_set_cli_shelf_rak_time)
            settingItemsHelper.putInt(settingItemsHelper.booklistSortType, 0)
            Constants.book_list_sort_type = 0
            setBookShelfSort(0)
        }

        rl_book_sort_update.setOnClickListener {
            StatServiceUtils.statAppBtnClick(this,
                    StatServiceUtils.me_set_cli_shelf_rank_up);
            settingItemsHelper.putInt(settingItemsHelper.booklistSortType, 1)
            Constants.book_list_sort_type = 1
            setBookShelfSort(1)
        }

    }

    private fun changeUmengStatus(checked: Boolean) {
        if (checked) {
            pushAgent.enable(object : IUmengCallback {
                override fun onSuccess() {
                    this@SettingMoreActivity.loge("成功打开友盟推送")
                }

                override fun onFailure(p0: String?, p1: String?) {
                    this@SettingMoreActivity.loge("打开友盟推送失败: $p0 | $p1")
                }

            })
        } else {
            pushAgent.disable(object : IUmengCallback {
                override fun onSuccess() {
                    this@SettingMoreActivity.loge("成功关闭友盟推送")
                }

                override fun onFailure(p0: String?, p1: String?) {
                    this@SettingMoreActivity.loge("关闭友盟推送失败: $p0 | $p1")
                }
            })

        }
    }

    private fun changePushStatus() {
        if (!btn_book_update_push.isChecked && !btn_umeng_push.isChecked) {
            //先改变 check，后关闭 enable
            btn_push_sound.isChecked = false
            btn_push_sound.isEnabled = false

            btn_push_time.isChecked = false
            btn_push_time.isEnabled = false

            setPushSound(false, false)
            setPushTime(false, false)

        } else {
            //因为 enable 为 false，所以先打开 enable，后改变 check
            btn_push_sound.isEnabled = true
            btn_push_sound.isChecked = true

            btn_push_time.isEnabled = true
            btn_push_time.isChecked = true

            setPushSound(true, true)
            setPushTime(true, true)

        }
    }

    private fun setPushSound(isChecked: Boolean, isStartPush: Boolean) {
        if (isChecked) {
            if (isStartPush) {
                settingItemsHelper.putBoolean(settingItemsHelper.openPushSound, true)
            } else {
                settingItemsHelper.putBoolean(settingItemsHelper.openPushSound, false)
            }
            pushAgent.notificationPlaySound = MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE
        } else {
            settingItemsHelper.putBoolean(settingItemsHelper.openPushSound, isStartPush)
            pushAgent.notificationPlaySound = MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE
        }
    }

    private fun setPushTime(isChecked: Boolean, isStartPush: Boolean) {
        if (isChecked) {
            if (isStartPush) {
                val startHour = settingItems.pushTimeStartH
                val startMin = settingItems.pushTimeStartMin
                val endHour = settingItems.pushTimeStopH
                val endMin = settingItems.pushTimeStopMin
                showPushTime(startHour, startMin, endHour, endMin)
                pushAgent.setNoDisturbMode(startHour, startMin, endHour, endMin)
            } else {
                txt_push_time_setting.text = "全天推送"
                pushAgent.setNoDisturbMode(0, 0, 0, 0)
            }
            settingItemsHelper.putBoolean(settingItemsHelper.setPushTime, isStartPush)
            txt_push_time_setting.isEnabled = isStartPush

        } else {
            settingItemsHelper.putBoolean(settingItemsHelper.setPushTime, isStartPush)
            txt_push_time_setting.isEnabled = false
            txt_push_time_setting.text = "无推送"
        }

    }

    private fun showPushTime(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        val timeStr = "推送时间：${startHour.format()}:${startMinute.format()}" +
                "-${endHour.format()}:${endMinute.format()}"
        txt_push_time_setting.text = timeStr
    }

    private fun saveTime(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        settingItemsHelper.putInt(settingItemsHelper.pushTimeStartH, startHour)
        settingItemsHelper.putInt(settingItemsHelper.pushTimeStartMin, startMinute)
        settingItemsHelper.putInt(settingItemsHelper.pushTimeStopH, endHour)
        settingItemsHelper.putInt(settingItemsHelper.pushTimeStopMin, endMinute)
        pushAgent.setNoDisturbMode(startHour, startMinute, endHour, endMinute)
    }

    private fun Int.format(): String {
        return if (this < 10) {
            "0$this"
        } else {
            this.toString()
        }
    }

    private fun setBookShelfSort(type: Int) {
        img_book_sort_read.setImageResource(if (type != 1) R.drawable.bookshelf_delete_checked else R.drawable.bookshelf_delete_unchecked)
        img_book_sort_update.setImageResource(if (type == 1) R.drawable.bookshelf_delete_checked else R.drawable.bookshelf_delete_unchecked)
    }

    override fun onBackPressed() {
        DyStatService.onEvent(EventPoint.MORESET_BACK, mapOf("type" to "2"))
        super.onBackPressed()
    }

}