package com.intelligent.reader.activity

import com.intelligent.reader.R

import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.CustomDialog
import net.lzbook.kit.book.view.SwitchButton
import net.lzbook.kit.book.view.TimePicker
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.SettingItems
import net.lzbook.kit.utils.SettingItemsHelper
import net.lzbook.kit.utils.StatServiceUtils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

import java.util.HashMap

import iyouqu.theme.BaseCacheableActivity

class SettingMoreActivity : BaseCacheableActivity(), View.OnClickListener, SwitchButton.OnCheckedChangeListener {
    var TAG = SettingMoreActivity::class.java.simpleName
    private var btnBack: ImageView? = null
    private var title: TextView? = null
    private val setting_more_back: TextView? = null
    private var push: RelativeLayout? = null
    private var push_checkbox: SwitchButton? = null
    private var push_sound: RelativeLayout? = null
    private var push_sound_checkbox: SwitchButton? = null
    private var push_time: RelativeLayout? = null
    private var push_time_checkbox: SwitchButton? = null
    private var push_time_setting_text: TextView? = null
    private var bookshelf_sort_time: RelativeLayout? = null
    private var bookshelf_sort_time_checkbox: ImageView? = null
    private var bookshelf_sort_update_time: RelativeLayout? = null
    private var bookshelf_sort_update_time_checkbox: ImageView? = null
    private var linear_book_sort: LinearLayout? = null
    //设置帮助类
    private var settingItemsHelper: SettingItemsHelper? = null
    private var settingItems: SettingItems? = null
    private var time_dialog: View? = null
    private var time_picker: TimePicker? = null
    private var mContext: Context? = null

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_setting_more)
        mContext = this
        initView()
        initData()
        initListener()
    }

    protected fun initView() {

        btnBack = findViewById(R.id.btn_left_setting) as ImageView
        title = findViewById(R.id.tv_title_name) as TextView

        //推送
        push = findViewById(R.id.push) as RelativeLayout
        push_checkbox = findViewById(R.id.push_message_checkbox) as SwitchButton
        //推送声音
        push_sound = findViewById(R.id.push_sound) as RelativeLayout
        push_sound_checkbox = findViewById(R.id.push_sound_checkbox) as SwitchButton
        //推送时间
        push_time = findViewById(R.id.push_time) as RelativeLayout
        push_time_checkbox = findViewById(R.id.push_time_checkbox) as SwitchButton
        //推送时间设置
        push_time_setting_text = findViewById(R.id.push_time_setting_text) as TextView
        //书架按时间排序
        bookshelf_sort_time = findViewById(R.id.bookshelf_sort_time) as RelativeLayout
        bookshelf_sort_time_checkbox = findViewById(R.id.bookshelf_sort_time_checkbox) as ImageView
        //书架按更新时间排序
        bookshelf_sort_update_time = findViewById(R.id.bookshelf_sort_update_time) as RelativeLayout
        bookshelf_sort_update_time_checkbox = findViewById(R.id.bookshelf_sort_update_time_checkbox) as ImageView
        //        tv_feedback = findViewById(R.id.tv_feedback);
        time_dialog = LayoutInflater.from(this).inflate(R.layout.view_custom_dialog_push_time_setting, null)
        time_picker = time_dialog!!.findViewById(R.id.timepicker) as TimePicker
        linear_book_sort = findViewById(R.id.linear_book_sort) as LinearLayout


    }

    private fun initData() {
        settingItemsHelper = SettingItemsHelper.getSettingHelper(applicationContext)
        settingItems = settingItemsHelper!!.values
        push_checkbox!!.isChecked = settingItems!!.isPush
        push_sound_checkbox!!.isChecked = settingItems!!.isSoundOpen
        push_time_checkbox!!.isChecked = settingItems!!.isSetPushTime
        if (push_time_checkbox!!.isChecked) {
            push_time_setting_text!!.isEnabled = true
            initPushTime()
            push_time_setting_text!!.visibility = View.VISIBLE
            setPushTime2Show()
        } else {
            push_time_setting_text!!.isEnabled = false
            push_time_setting_text!!.visibility = View.VISIBLE
        }

        initBookShelfSort(settingItems!!.booklist_sort_type)
    }

    protected fun initListener() {

        setting_more_back?.setOnClickListener(this)

        if (push != null) {
            push!!.setOnClickListener(this)
        }

        if (push_checkbox != null) {
            push_checkbox!!.setOnCheckedChangeListener(this)
        }

        if (push_sound != null) {
            push_sound!!.setOnClickListener(this)
        }

        if (push_sound_checkbox != null) {
            push_sound_checkbox!!.setOnCheckedChangeListener(this)
        }

        if (push_time != null) {
            push_time!!.setOnClickListener(this)
        }

        if (push_time_checkbox != null) {
            push_time_checkbox!!.setOnCheckedChangeListener(this)
        }

        if (push_time_setting_text != null) {
            push_time_setting_text!!.setOnClickListener(this)
        }

        if (bookshelf_sort_time != null) {
            bookshelf_sort_time!!.setOnClickListener(this)
        }

        if (bookshelf_sort_time_checkbox != null) {
            bookshelf_sort_time_checkbox!!.setOnClickListener(this)
        }

        if (bookshelf_sort_update_time != null) {
            bookshelf_sort_update_time!!.setOnClickListener(this)
        }
        if (btnBack != null) {
            btnBack!!.setOnClickListener(this)
        }
        if (bookshelf_sort_update_time_checkbox != null) {
            bookshelf_sort_update_time_checkbox!!.setOnClickListener(this)
        }
        //        if(tv_feedback !=null){
        //            tv_feedback.setOnClickListener(this);
        //        }
    }

    override fun onClick(paramView: View) {

        when (paramView.id) {

            R.id.btn_left_setting -> {
                val data = HashMap<String, String>()
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.MORESET_PAGE, StartLogClickUtil.BACK, data)
                finish()
            }

            R.id.push_time_setting_text -> showDialog(PUSH_TIME_SETTING)

            R.id.bookshelf_sort_time, R.id.bookshelf_sort_time_checkbox -> {
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.me_set_cli_shelf_rak_time)
                settingItemsHelper!!.putInt(settingItemsHelper!!.booklistSortType, 0)
                Constants.book_list_sort_type = 0
                initBookShelfSort(0)
            }

            R.id.bookshelf_sort_update_time, R.id.bookshelf_sort_update_time_checkbox -> {
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.me_set_cli_shelf_rank_up)
                settingItemsHelper!!.putInt(settingItemsHelper!!.booklistSortType, 1)
                Constants.book_list_sort_type = 1
                initBookShelfSort(1)
            }
            R.id.push_message_checkbox -> {

                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.me_set_click_more_push)
                changePushStatus(push_checkbox!!.isChecked)
            }

            else -> {
            }
        }
    }

    private fun changePushStatus(status: Boolean) {
        push_checkbox!!.isChecked = status

        push_sound!!.isEnabled = status
        if (status) {
            push_sound_checkbox!!.isEnabled = status
            push_sound_checkbox!!.isChecked = status
        } else {
            push_sound_checkbox!!.isChecked = status
            push_sound_checkbox!!.isEnabled = status
        }

        push_time!!.isEnabled = status
        if (status) {
            push_time_checkbox!!.isEnabled = status
            push_time_checkbox!!.isChecked = status
        } else {
            push_time_checkbox!!.isChecked = status
            push_time_checkbox!!.isEnabled = status
        }

        settingItemsHelper!!.putBoolean(settingItemsHelper!!.openPush, status)
        setPushSound(status, status)
        setPushTime(status, status)
    }

    private fun changePushSoundStatus(status: Boolean) {
        push_sound_checkbox!!.isChecked = status
    }

    private fun changePushTimeStatus(status: Boolean) {
        push_time_checkbox!!.isChecked = status
    }

    private fun setPushSound(isChecked: Boolean, isStartPush: Boolean) {
        if (isChecked) {
            if (isStartPush) {
                settingItemsHelper!!.putBoolean(settingItemsHelper!!.openPushSound, true)
            } else {
                settingItemsHelper!!.putBoolean(settingItemsHelper!!.openPushSound, false)
            }

        } else {
            settingItemsHelper!!.putBoolean(settingItemsHelper!!.openPushSound, isStartPush)
        }
    }

    private fun setPushTime(isChecked: Boolean, isStartPush: Boolean) {
        if (isChecked) {
            if (isStartPush) {
                settingItemsHelper!!.putBoolean(settingItemsHelper!!.setPushTime, true)
                push_time_setting_text!!.isEnabled = true
                push_time_setting_text!!.visibility = View.VISIBLE
                initPushTime()
                setPushTime2Show()
            } else {
                settingItemsHelper!!.putBoolean(settingItemsHelper!!.setPushTime, false)
                push_time_setting_text!!.isEnabled = false
                push_time_setting_text!!.visibility = View.VISIBLE
                push_time_setting_text!!.text = "全天推送"
            }

        } else {
            settingItemsHelper!!.putBoolean(settingItemsHelper!!.setPushTime, isStartPush)
            push_time_setting_text!!.isEnabled = false
            push_time_setting_text!!.visibility = View.VISIBLE
            push_time_setting_text!!.text = "无推送"
        }

    }

    private fun initBookShelfSort(type: Int) {

        var checkedColor = 0
        var uncheckedColor = 0
        val theme = mContext!!.theme
        checkedColor = R.mipmap.bookshelf_delete_checked
        uncheckedColor = R.mipmap.bookshelf_delete_unchecked
        bookshelf_sort_time_checkbox!!.setImageResource(if (type != 1) checkedColor else uncheckedColor)
        bookshelf_sort_update_time_checkbox!!.setImageResource(if (type == 1) checkedColor else uncheckedColor)
    }

    private fun initPushTime() {
        if (time_picker != null) {
            time_picker!!.currentStartHour = settingItems!!.pushTimeStartH
            time_picker!!.currentStartMinute = settingItems!!.pushTimeStartMin
            time_picker!!.currentStopHour = settingItems!!.pushTimeStopH
            time_picker!!.currentStopMinute = settingItems!!.pushTimeStopMin
        }
    }

    private fun setPushTime2Show() {
        if (settingItems!!.pushTimeStartMin >= 10) {
            if (settingItems!!.pushTimeStopMin >= 10) {
                val stringBuilder = StringBuilder()
                stringBuilder.append("推送时间：")
                        .append(settingItems!!.pushTimeStartH).append(":")
                        .append(settingItems!!.pushTimeStartMin).append("-")
                        .append(settingItems!!.pushTimeStopH).append(":")
                        .append(settingItems!!.pushTimeStopMin)
                push_time_setting_text!!.text = stringBuilder.toString()

            } else {
                val stringBuilder = StringBuilder()
                stringBuilder.append("推送时间：")
                        .append(settingItems!!.pushTimeStartH).append(":")
                        .append(settingItems!!.pushTimeStartMin).append("-")
                        .append(settingItems!!.pushTimeStopH).append(":0")
                        .append(settingItems!!.pushTimeStopMin)
                push_time_setting_text!!.text = stringBuilder.toString()
            }
        } else {
            if (settingItems!!.pushTimeStopMin >= 10) {
                val stringBuilder = StringBuilder()
                stringBuilder.append("推送时间：")
                        .append(settingItems!!.pushTimeStartH).append(":0")
                        .append(settingItems!!.pushTimeStartMin).append("-")
                        .append(settingItems!!.pushTimeStopH).append(":")
                        .append(settingItems!!.pushTimeStopMin)
                push_time_setting_text!!.text = stringBuilder.toString()
            } else {
                val stringBuilder = StringBuilder()
                stringBuilder.append("推送时间：")
                        .append(settingItems!!.pushTimeStartH).append(":0")
                        .append(settingItems!!.pushTimeStartMin).append("-")
                        .append(settingItems!!.pushTimeStopH).append(":0")
                        .append(settingItems!!.pushTimeStopMin)
                push_time_setting_text!!.text = stringBuilder.toString()
            }
        }
    }

    override fun onCreateDialog(id: Int): Dialog? {
        var dialog: Dialog? = null
        when (id) {
            PUSH_TIME_SETTING -> {
                val customBuilder = CustomDialog.Builder(this)
                customBuilder.setTitle("时间设置")
                customBuilder
                        .setContentView(this.time_dialog)
                        .setNegativeButton("取消",
                                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() }
                        )
                        .setPositiveButton("设置",
                                DialogInterface.OnClickListener { dialog, which ->
                                    dialog.dismiss()
                                    // 获取时间，写文件
                                    val startHour = time_picker!!
                                            .currentStartHour
                                    val startMin = time_picker!!
                                            .currentStartMinute
                                    val stopHour = time_picker!!
                                            .currentStopHour
                                    val stopMin = time_picker!!
                                            .currentStopMinute

                                    settingItemsHelper!!.putInt(
                                            settingItemsHelper!!.pushTimeStartH,
                                            startHour)
                                    settingItemsHelper!!.putInt(
                                            settingItemsHelper!!.pushTimeStartMin,
                                            startMin)
                                    settingItemsHelper!!.putInt(
                                            settingItemsHelper!!.pushTimeStopH,
                                            stopHour)
                                    settingItemsHelper!!.putInt(
                                            settingItemsHelper!!.pushTimeStopMin,
                                            stopMin)

                                    setPushTime2Show()
                                }
                        )
                dialog = customBuilder.create()

                dialog!!.setOnDismissListener {
                    time_picker!!.currentStartHour = settingItems!!.pushTimeStartH
                    time_picker!!.currentStartMinute = settingItems!!.pushTimeStartMin

                    time_picker!!.currentStopHour = settingItems!!.pushTimeStopH
                    time_picker!!.currentStopMinute = settingItems!!.pushTimeStopMin
                }
            }
        }
        return dialog

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                setResult(67)
                this.finish()
            }
            else -> {
            }
        }
    }

    fun setPushStatus(pushStatus: Boolean) {
        push_checkbox!!.isChecked = pushStatus

        push_sound!!.isEnabled = pushStatus
        push_sound_checkbox!!.isChecked = pushStatus
        push_sound_checkbox!!.isEnabled = pushStatus

        push_time!!.isEnabled = pushStatus
        push_time_checkbox!!.isChecked = pushStatus
        push_time_checkbox!!.isEnabled = pushStatus

        settingItemsHelper!!.putBoolean(settingItemsHelper!!.openPush, pushStatus)
        if (pushStatus) {
            setPushSound(settingItems!!.isSoundOpen, pushStatus)
            setPushTime(settingItems!!.isSetPushTime, pushStatus)
        } else {
            setPushSound(pushStatus, pushStatus)
            setPushTime(pushStatus, pushStatus)
        }
    }

    override fun onDestroy() {

        if (time_picker != null) {
            time_picker = null
        }

        if (time_dialog != null) {
            time_dialog = null
        }

        try {
            setContentView(R.layout.empty)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        super.onDestroy()
    }

    override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {
        if (view == null) {
            return
        }
        when (view.id) {
            R.id.push_message_checkbox -> {
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.me_set_click_more_push)
                changePushStatus(push_checkbox!!.isChecked)
                val params1 = HashMap<String, String>()
                params1.put("type", if (push_checkbox!!.isChecked) "1" else "2")
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.MORESET_PAGE, StartLogClickUtil.PUSHSET, params1)
            }

            R.id.push_sound_checkbox -> {
                val params = HashMap<String, String>()
                params.put("type", if (push_sound_checkbox!!.isChecked) "1" else "2")
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.MORESET_PAGE, StartLogClickUtil.PUSHAUDIO, params)
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.me_set_cli_more_push_voi)
                changePushSoundStatus(push_sound_checkbox!!.isChecked)
                setPushSound(push_checkbox!!.isChecked, push_sound_checkbox!!.isChecked)
            }

            R.id.push_time_checkbox -> {
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.me_set_cli_more_push_time)
                changePushTimeStatus(push_time_checkbox!!.isChecked)
                setPushTime(push_checkbox!!.isChecked, push_time_checkbox!!.isChecked)
            }
        }
    }

    companion object {

        private val PUSH_TIME_SETTING = 1
    }
}
