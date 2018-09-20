package com.intelligent.reader.activity

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.preference.PreferenceManager
import android.view.View
import android.view.animation.AnimationUtils
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import com.ding.basic.net.Config
import com.ding.basic.net.api.service.RequestService
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.CommonUtil
import com.dingyue.contract.util.SharedPreUtil
import com.dy.reader.setting.ReaderSettings
import com.intelligent.reader.R
import com.intelligent.reader.util.EventBookStore
import com.intelligent.reader.view.ClearCacheDialog
import iyouqu.theme.BaseCacheableActivity
import iyouqu.theme.ThemeMode
import kotlinx.android.synthetic.qbmfkdxs.act_setting_user.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.view.SwitchButton
import net.lzbook.kit.cache.DataCleanManager
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.*
import net.lzbook.kit.utils.update.ApkUpdateUtils
import swipeback.ActivityLifecycleHelper
import java.util.*


@Route(path = RouterConfig.SETTING_ACTIVITY)
class SettingActivity : BaseCacheableActivity(), View.OnClickListener, SwitchButton.OnCheckedChangeListener {

    private var currentThemeMode: String? = null //是否切换了主题
    internal var apkUpdateUtils = ApkUpdateUtils(this)

    private var isActivityPause = false
    private var isStyleChanged = false
    var handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            super.handleMessage(message)
            when (message.what) {

            }
        }
    }

    private val feedbackRunnable = Runnable {
        FeedbackAPI.openFeedbackActivity()
    }


    private var isFromPush = false

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_setting_user)

        // 获取CommunitySDK实例, 参数1为Context类型
        currentThemeMode = mThemeHelper.mode
        isStyleChanged = intent.getBooleanExtra("isStyleChanged", false)
        initView()
        initListener()
        initData()
    }

    private fun initView() {

        if (mThemeHelper.isNight) {
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_day_shift)
            tv_night_shift.setText(R.string.mode_day)
            bt_night_shift.isChecked = true
        } else {
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_night_shift)
            tv_night_shift.setText(R.string.mode_night)
            bt_night_shift.isChecked = false
        }

        bt_wifi_auto.isChecked = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SharedPreUtil.AUTO_UPDATE_CAHCE, true)

        //福利中心动画
        startWelfareCenterAnim()

        showCacheMessage()
    }

    private fun initListener() {

        img_back.setOnClickListener(this)
        tv_qrcode.setOnClickListener(this)
        tv_setting_more.setOnClickListener(this)
        tv_mark.setOnClickListener(this)
        tv_feedback.setOnClickListener(this)
        tv_disclaimer.setOnClickListener(this)

        rl_welfare.setOnClickListener(this)
        rl_check_update.setOnClickListener(this)
        rl_clear_cache.setOnClickListener(this)


        bt_night_shift.setOnCheckedChangeListener(this)
        bt_wifi_auto.setOnCheckedChangeListener(this)

    }

    private fun initData() {
        val versionName = AppUtils.getVersionName()
        tv_check_version.text = ("V$versionName")
        isFromPush = intent.getBooleanExtra(IS_FROM_PUSH, false)
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
        if (img_welfare != null) {
            img_welfare!!.clearAnimation()
        }
        try {
            setContentView(R.layout.empty)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        super.onDestroy()
    }

    /**
     * 福利中心红包动画
     */
    private fun startWelfareCenterAnim() {
        if (img_welfare != null) {
            val animation = AnimationUtils.loadAnimation(this, R.anim.welfare_center_anim)
            img_welfare?.animation = animation
            animation.start()
        }
    }

    override fun onClick(paramView: View) {

        when (paramView.id) {
            R.id.img_back -> {
                val data = HashMap<String, String>()
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.BACK, data)
                goBackToHome()
            }
            R.id.tv_qrcode -> {
                val welfareIntent = Intent()
                val uri = RequestService.QR_CODE.replace("{packageName}", AppUtils.getPackageName())
                welfareIntent.putExtra("url", UrlUtils.buildWebUrl(uri, HashMap()))
                welfareIntent.putExtra("title", "和朋友一起读书")
                welfareIntent.setClass(this@SettingActivity, WelfareCenterActivity::class.java)
                startActivity(welfareIntent)
            }
            R.id.rl_welfare -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.ADPAGE)
                val welfareIntent = Intent()
                welfareIntent.putExtra("url", Config.WelfareHost)
                welfareIntent.putExtra("title", "福利中心")
                welfareIntent.setClass(this@SettingActivity, WelfareCenterActivity::class.java)
                startActivity(welfareIntent)
            }
            R.id.tv_setting_more -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.MORESET)
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_more)
                startActivity(Intent(this@SettingActivity, SettingMoreActivity::class.java))
            }
            R.id.rl_check_update -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.VERSION)
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_ver)
                checkUpdate()
            }
            R.id.tv_feedback -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.HELP)
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_help)
                handler.removeCallbacks(feedbackRunnable)
                handler.postDelayed(feedbackRunnable, 500)
            }
            R.id.tv_mark -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.COMMENT)
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_help)
                try {
                    val uri = Uri.parse("market://details?id=" + getPackageName())
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } catch (e: Exception) {
                    CommonUtil.showToastMessage(R.string.menu_no_market)
                }

            }
            R.id.tv_disclaimer -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE,
                        StartLogClickUtil.PROCTCOL)
                val bundle = Bundle()
                bundle.putBoolean(RouterUtil.FROM_DISCLAIMER_PAGE, true)
                RouterUtil.navigation(this, RouterConfig.DISCLAIMER_ACTIVITY, bundle)
            }
            R.id.rl_clear_cache -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.CACHECLEAR)
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_clear_cache)
                clearCacheDialog.show()
            }
            else -> {
            }
        }
    }

    private fun checkUpdate() {
        try {
            apkUpdateUtils.getApkUpdateInfo(this, handler, "SettingActivity")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private val clearCacheDialog: ClearCacheDialog by lazy {
        val dialog = ClearCacheDialog(this)

        dialog.setOnConfirmListener {
            dialog.showLoading()

            this.doAsync {
                CacheManager.removeAll()
                UIHelper.clearAppCache()
                DataCleanManager.clearAllCache(this.applicationContext)
                Thread.sleep(1000)
                uiThread {
                    dialog.dismiss()
                    tv_check_cache_size.text = applicationContext.getString(R.string.application_cache_size)
                }
            }
        }
        dialog
    }


    /***
     * 获取缓存大小
     * **/
    private fun showCacheMessage() {
        doAsync {
            var result = "0B"
            try {
                result = DataCleanManager.getTotalCacheSize(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            uiThread { tv_check_cache_size?.text = result }
        }
    }

    override fun onBackPressed() {
        goBackToHome()
    }

    private fun goBackToHome() {
        if (currentThemeMode != mThemeHelper.mode || isStyleChanged) {
            if (swipeBackHelper == null || !swipeBackHelper.isSliding) {//滑动返回已结束
                onThemeSwitch()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onSlideFinishAnimEnd() {
        super.onSlideFinishAnimEnd()
        if (currentThemeMode != mThemeHelper.mode || isStyleChanged) {
            onThemeSwitch()
        }
    }

    private fun onThemeSwitch() {
        val themIntent = Intent(this@SettingActivity, HomeActivity::class.java)
        val bundle = Bundle()
        bundle.putInt(EventBookStore.BOOKSTORE, EventBookStore.TYPE_TO_SWITCH_THEME)
        themIntent.putExtras(bundle)
        startActivity(themIntent)
        finish()
    }

    //夜间模式切换按钮的回调
    override fun onCheckedChanged(view: SwitchButton, isChecked: Boolean) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val edit = sharedPreferences.edit()
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
            edit.putBoolean(SharedPreUtil.AUTO_UPDATE_CAHCE, isChecked)
            edit.apply()
            val data = HashMap<String, String>()
            data.put("type", if (isChecked) "1" else "0")
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.WIFI_AUTOCACHE, data)
        }
    }

    override fun finish() {
        super.finish()
        //离线消息 跳转到主页
        val isThemeChange = currentThemeMode != mThemeHelper.mode || isStyleChanged
        if (!isThemeChange && isFromPush && ActivityLifecycleHelper.getActivities().size <= 1) {
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }

    override fun supportSlideBack(): Boolean {
        return ActivityLifecycleHelper.getActivities().size > 1
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

}