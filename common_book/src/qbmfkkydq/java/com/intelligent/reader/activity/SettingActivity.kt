package com.intelligent.reader.activity


import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.TypedValue
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import com.dy.reader.setting.ReaderSettings
import com.intelligent.reader.R
import com.intelligent.reader.widget.SwitchButton
import kotlinx.android.synthetic.main.publish_hint_dialog.*
import kotlinx.android.synthetic.qbmfkkydq.act_setting_user.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.bean.EventBookStore
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.ui.widget.MyDialog
import net.lzbook.kit.utils.ApkUpdateUtils
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.cache.DataCleanManager
import net.lzbook.kit.utils.cache.UIHelper
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import net.lzbook.kit.utils.theme.StatusBarCompat
import net.lzbook.kit.utils.theme.ThemeMode
import net.lzbook.kit.utils.toast.ToastUtil
import java.util.*


@Route(path = RouterConfig.SETTING_ACTIVITY)
open class SettingActivity : BaseCacheableActivity(), SwitchButton.OnCheckedChangeListener {

    var TAG = SettingActivity::class.java.simpleName
    protected var currentThemeMode: String? = null //是否切换了主题

    private var myDialog: MyDialog? = null//清除缓存对话框

    internal var apkUpdateUtils = ApkUpdateUtils(this)
    private var cacheAsyncTask: CacheAsyncTask? = null

    private var isActivityPause = false
    private var isStyleChanged = false
    var handler: Handler = object : Handler() {
        override fun handleMessage(message: Message) {
            super.handleMessage(message)
            when (message.what) {

            }
        }
    }

    private val feedbackRunnable = Runnable {
        FeedbackAPI.openFeedbackActivity()
    }

    internal var themeName = TypedValue()//分割块颜色


    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        try {
            setContentView(R.layout.act_setting_user)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        // 获取CommunitySDK实例, 参数1为Context类型
        currentThemeMode = mThemeHelper.mode
        isStyleChanged = intent.getBooleanExtra("isStyleChanged", false)
        initView()
        initData()
    }

    protected fun initView() {

        // 返回箭头
        top_setting_back.setOnClickListener {
            goBackToHome()
        }

        // 夜间模式切换
        bt_night_shift.setOnCheckedChangeListener(this)

        // wifi自动下载
        bt_wifi_auto.setOnCheckedChangeListener(this)

        //推送设置
        rl_setting_more.setOnClickListener {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.MORESET)
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_more)
            startActivity(Intent(this@SettingActivity, SettingMoreActivity::class.java))

        }

        // 意见反馈
        rl_feedback.setOnClickListener {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.HELP)
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_help)
            handler.removeCallbacks(feedbackRunnable)
            handler.postDelayed(feedbackRunnable, 500)
        }

        // 去评分
        rl_mark.setOnClickListener {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.COMMENT)
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_help)
            try {
                val uri = Uri.parse("market://details?id=" + packageName)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e: Exception) {
                ToastUtil.showToastMessage(R.string.menu_no_market)
            }

        }

        // 法律声明
        disclaimer_statement_rl.setOnClickListener {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE,
                    StartLogClickUtil.PROCTCOL)
            val bundle = Bundle()
            bundle.putBoolean(Constants.FROM_DISCLAIMER_PAGE, true)
            RouterUtil.navigation(this, RouterConfig.DISCLAIMER_ACTIVITY, bundle)
        }

        // 当前版本
        check_update_rl.setOnClickListener {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.VERSION)
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_ver)
            checkUpdate()
        }

        // 清除缓存
        clear_cache_rl.setOnClickListener {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.CACHECLEAR)
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_clear_cache)
            if (clear_cache_size.text == "0B") {
                ToastUtil.showToastMessage("缓存已清除")
            } else {
                clearCacheDialog()
            }
        }


        if (mThemeHelper.isNight) {
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_day_shift)
            tv_night_shift!!.setText(R.string.mode_day)
            bt_night_shift!!.isChecked = true
        } else {
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_night_shift)
            tv_night_shift!!.setText(R.string.mode_night)
            bt_night_shift!!.isChecked = false
        }

        bt_wifi_auto!!.isChecked = SPUtils.getDefaultSharedBoolean(SPKey.AUTO_UPDATE_CAHCE, true)

    }


    private fun initData() {
        CancelTask()
        cacheAsyncTask = CacheAsyncTask()
        cacheAsyncTask!!.execute()
        val versionName = AppUtils.getVersionName()
        check_update_message!!.text = "V$versionName"
    }

    override fun onResume() {
        super.onResume()
        isActivityPause = false
    }

    override fun onPause() {
        super.onPause()
        isActivityPause = true
    }

    override fun onDestroy() {

        try {
            setContentView(R.layout.empty)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        super.onDestroy()
        CancelTask()
    }


    private fun checkUpdate() {
        try {
            apkUpdateUtils.getApkUpdateInfo(this, handler, "SettingActivity")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun clearCacheDialog() {
        if (!isFinishing) {
            myDialog = MyDialog(this, R.layout.publish_hint_dialog)
            myDialog!!.setCanceledOnTouchOutside(false)
            myDialog!!.setCancelable(false)
            myDialog!!.setCanceledOnTouchOutside(true)//设置点击dialog外面对话框消失
            myDialog!!.publish_content.text = "清除包括下载书籍在内的所有缓存"
            myDialog!!.publish_stay.setOnClickListener {
                dismissDialog()
            }
            myDialog!!.publish_leave.setOnClickListener {
                myDialog!!.publish_content.visibility = View.GONE
                myDialog!!.dialog_title.setText(R.string.tip_cleaning_cache)
                myDialog!!.change_source_bottom.visibility = View.GONE

                myDialog!!.progress_del.visibility = View.VISIBLE
                //添加清除缓存的处理
                object : Thread() {
                    override fun run() {
                        super.run()

                        CacheManager.removeAll()

                        UIHelper.clearAppCache()

//                        DataCleanManager.clearAllCache(applicationContext)

                        runOnUiThread {
                            dismissDialog()
                            clear_cache_size!!.text = "0B"
                            ToastUtil.showToastMessage("缓存已清除")
                        }
                    }
                }.start()
            }

            myDialog!!.setOnCancelListener { myDialog!!.dismiss() }
            if (!myDialog!!.isShowing) {
                try {
                    myDialog!!.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    override fun onBackPressed() {
        goBackToHome()
    }

    private fun goBackToHome() {
        if (currentThemeMode!! != mThemeHelper.mode || isStyleChanged) {
            if (swipeBackHelper == null || !swipeBackHelper.isSliding) {//滑动返回已结束
                onThemeSwitch()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onSlideFinishAnimEnd() {
        super.onSlideFinishAnimEnd()
        if (currentThemeMode!! != mThemeHelper.mode || isStyleChanged) {
            onThemeSwitch()
        }
    }

    private fun onThemeSwitch() {
        val themIntent = Intent(this@SettingActivity, HomeActivity::class.java)
        val bundle = Bundle()
        bundle.putInt(EventBookStore.BOOKSTORE, EventBookStore.TYPE_TO_SWITCH_THEME)
        themIntent.putExtras(bundle)
        startActivity(themIntent)
    }

    private fun dismissDialog() {
        if (myDialog != null && myDialog!!.isShowing) {
            myDialog!!.dismiss()
        }
    }


    private fun nightShift() {
        mThemeHelper.showAnimation(this)
        StatusBarCompat.compat(this)
    }

    //夜间模式切换按钮的回调
    override fun onCheckedChanged(view: SwitchButton, isChecked: Boolean) {
        if (view.id == R.id.bt_night_shift) {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.NIGHTMODE)
            ReaderSettings.instance.initValues()
            if (isChecked) {
                tv_night_shift!!.setText(R.string.mode_day)
                ReaderSettings.instance.readLightThemeMode = ReaderSettings.instance.readThemeMode
                ReaderSettings.instance.readThemeMode = 61
                mThemeHelper.setMode(ThemeMode.NIGHT)
            } else {
                tv_night_shift!!.setText(R.string.mode_night)
                ReaderSettings.instance.readThemeMode = ReaderSettings.instance.readLightThemeMode
                mThemeHelper.setMode(ThemeMode.THEME1)
            }
            ReaderSettings.instance.save()
            nightShift(isChecked, true)
        } else if (view.id == R.id.bt_wifi_auto) {
            SPUtils.putDefaultSharedBoolean(SPKey.AUTO_UPDATE_CAHCE, isChecked)
            val data = HashMap<String, String>()
            data.put("type", if (isChecked) "1" else "0")
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.WIFI_AUTOCACHE, data)
        }
    }

    private inner class CacheAsyncTask : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void): String {
            var result = "0B"
            try {
                result = DataCleanManager.getTotalCacheSize(applicationContext)
                SettingActivity.cacheSize = DataCleanManager.internalCacheSize
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            clear_cache_size!!.text = result
        }

    }

    private fun CancelTask() {
        if (cacheAsyncTask != null) {
            cacheAsyncTask!!.cancel(true)
            cacheAsyncTask = null
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (resultCode) {
            RESULT_OK -> {
                setResult(67)
                this.finish()
            }
            else -> {
            }
        }
    }

    companion object {

        private val PUSH_TIME_SETTING = 1

        var cacheSize: Long = 0
        private val LOGIN_SUCCESS = 0x20
    }

}