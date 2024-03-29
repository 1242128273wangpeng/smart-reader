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
import android.view.animation.AnimationUtils
import android.widget.*
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import com.dy.reader.setting.ReaderSettings
import com.intelligent.reader.R
import kotlinx.android.synthetic.main.publish_hint_dialog.*
import net.lzbook.kit.bean.EventBookStore
import net.lzbook.kit.ui.activity.WelfareCenterActivity
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.ui.widget.MyDialog
import net.lzbook.kit.ui.widget.SwitchButton
import net.lzbook.kit.utils.ApkUpdateUtils
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.IS_FROM_PUSH
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.cache.DataCleanManager
import net.lzbook.kit.utils.cache.UIHelper
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.statistics.DyStatService
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.logger.PersonalLogger
import net.lzbook.kit.utils.swipeback.ActivityLifecycleHelper
import net.lzbook.kit.utils.theme.StatusBarCompat
import net.lzbook.kit.utils.theme.ThemeMode
import net.lzbook.kit.utils.toast.ToastUtil


@Route(path = RouterConfig.SETTING_ACTIVITY)
class SettingActivity : BaseCacheableActivity(), View.OnClickListener, SwitchButton.OnCheckedChangeListener {

    var TAG = SettingActivity::class.java!!.getSimpleName()
    private var btnBack: ImageView? = null
    private var top_setting_back: ImageView? = null

    protected var currentThemeMode: String? = null //是否切换了主题

    private var myDialog: MyDialog? = null//清除缓存对话框
    private var user_login_layout: RelativeLayout? = null
    private var is_show_drawable: View? = null

    private val mRelativeLayoutList: List<RelativeLayout>? = null
    private val mTextViewList: List<TextView>? = null
    private val mDivider: List<View>? = null
    private val mGap: List<View>? = null

    private var tv_readpage_bbs: TextView? = null
    private var tv_style_change: TextView? = null
    private var tv_night_shift: TextView? = null
    private var tv_readpage_setting: TextView? = null
    private var tv_setting_more: TextView? = null
    private var tv_feedback: TextView? = null
    private var tv_mark: TextView? = null
    private var text_check_update: TextView? = null
    private var text_clear_cache: TextView? = null
    private var text_disclaimer_statement: TextView? = null

    //第二种布局 登录在左侧
    private var top_navigation_bg: RelativeLayout? = null
    private var icon_more_left: ImageView? = null
    private var tv_login_info_detail_left: TextView? = null
    private var tv_login_info_left: TextView? = null
    private var top_navigation_title: TextView? = null
    private var iv_mine_image_left: ImageView? = null
    private var user_login_layout_left: RelativeLayout? = null

    private var rl_setting_layout: LinearLayout? = null//背景
    private var rl_readpage_bbs: RelativeLayout? = null//论坛
    private var rl_style_change: RelativeLayout? = null//主题切换
    private var iv_mine_image: ImageView? = null
    private var tv_login_info: TextView? = null
    private var bt_night_shift: SwitchButton? = null//夜间模式切换按钮
    private var bt_wifi_auto: SwitchButton? = null//wifi下自动缓存
    private var rl_readpage_setting: RelativeLayout? = null//阅读页设置
    private var rl_setting_more: RelativeLayout? = null//更多设置
    private var rl_feedback: RelativeLayout? = null//意见反馈
    private var rl_mark: RelativeLayout? = null//评分
    private var checkUpdateGuideRL: RelativeLayout? = null // 检查更新
    private var clear_cache_rl: RelativeLayout? = null//清除缓存
    private var disclaimer_statement_rl: RelativeLayout? = null//免责声明
    private var check_update_message: TextView? = null //版本号
    private var clear_cache_size: TextView? = null//缓存
    private var theme_name: TextView? = null//主题名
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

    private var isFromPush = false

    private val feedbackRunnable = Runnable({
        FeedbackAPI.openFeedbackActivity()
    })

    internal var themeName = TypedValue()//分割块颜色


    // 福利中心
    private var rl_welfare: RelativeLayout? = null
    private var img_welfare: ImageView? = null

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        try {
            setContentView(R.layout.act_setting_user)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        // 获取CommunitySDK实例, 参数1为Context类型
        currentThemeMode = mThemeHelper.getMode()
        isStyleChanged = getIntent().getBooleanExtra("isStyleChanged", false)
        initView()
        initListener()
        initData()
    }

    protected fun initView() {

        //用于判断是否显示Textview的Drawable
        is_show_drawable = findViewById(R.id.is_show_drawable)
        top_navigation_bg = findViewById(R.id.top_navigation_bg)
        icon_more_left = findViewById(R.id.icon_more_left)
        btnBack = findViewById(R.id.setting_back)
        top_setting_back = findViewById(R.id.top_setting_back)
        user_login_layout = findViewById(R.id.user_login_layout)
        iv_mine_image = findViewById(R.id.iv_mine_image)
        tv_login_info = findViewById(R.id.tv_login_info)
        iv_mine_image = findViewById(R.id.iv_mine_image)
        tv_login_info = findViewById(R.id.tv_login_info)
        iv_mine_image_left = findViewById(R.id.iv_mine_image_left)
        user_login_layout_left = findViewById(R.id.user_login_layout_left)
        // 福利中心
        rl_welfare = findViewById(R.id.rl_welfare)
        img_welfare = findViewById(R.id.img_welfare)

        rl_readpage_bbs = findViewById(R.id.rl_readpage_bbs)
        rl_style_change = findViewById(R.id.rl_style_change)
        bt_night_shift = findViewById(R.id.bt_night_shift)
        bt_wifi_auto = findViewById(R.id.bt_wifi_auto)
        rl_readpage_setting = findViewById(R.id.rl_readpage_setting)
        rl_setting_more = findViewById(R.id.rl_setting_more)
        rl_feedback = findViewById(R.id.rl_feedback)
        rl_mark = findViewById(R.id.rl_mark)
        checkUpdateGuideRL = findViewById(R.id.check_update_rl)
        clear_cache_rl = findViewById(R.id.clear_cache_rl)
        disclaimer_statement_rl = findViewById(R.id.disclaimer_statement_rl)
        rl_setting_layout = findViewById(R.id.rl_setting_layout)

        theme_name = findViewById(R.id.theme_name)
        clear_cache_size = findViewById(R.id.check_cache_size)
        check_update_message = findViewById(R.id.check_update_message)

        //条目字
        tv_readpage_bbs = findViewById(R.id.tv_readpage_bbs)
        tv_style_change = findViewById(R.id.tv_style_change)
        tv_night_shift = findViewById(R.id.tv_night_shift)
        tv_readpage_setting = findViewById(R.id.tv_readpage_setting)
        tv_setting_more = findViewById(R.id.tv_setting_more)
        tv_feedback = findViewById(R.id.tv_feedback)
        tv_mark = findViewById(R.id.tv_mark)
        text_check_update = findViewById(R.id.text_check_update)
        text_clear_cache = findViewById(R.id.text_clear_cache)
        text_disclaimer_statement = findViewById(R.id.text_disclaimer_statement)

        tv_login_info_left = findViewById(R.id.tv_login_info_left)
        tv_login_info_detail_left = findViewById(R.id.tv_login_info_detail_left)
        top_navigation_title = findViewById(R.id.top_navigation_title)


        if (mThemeHelper.isNight()) {
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_day_shift)
            tv_night_shift!!.setText(R.string.mode_day)
            bt_night_shift!!.setChecked(true)
        } else {
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_night_shift)
            tv_night_shift!!.setText(R.string.mode_night)
            bt_night_shift!!.setChecked(false)
        }

        bt_wifi_auto!!.setChecked(SPUtils.getDefaultSharedBoolean(SPKey.AUTO_UPDATE_CAHCE, true))

        //福利中心动画
        startWelfareCenterAnim()
    }

    protected fun initListener() {
        if (btnBack != null) {
            btnBack!!.setOnClickListener(this)
        }
        if (top_setting_back != null) {
            top_setting_back!!.setOnClickListener(this)
        }
        if (user_login_layout != null) {
            user_login_layout!!.setOnClickListener(this)
        }
        if (iv_mine_image != null) {
            iv_mine_image!!.setOnClickListener(this)
        }
        if (tv_login_info != null) {
            tv_login_info!!.setOnClickListener(this)
        }
        if (rl_style_change != null) {
            rl_style_change!!.setOnClickListener(this)
        }
        if (rl_welfare != null) {
            rl_welfare!!.setOnClickListener(this)
        }
        if (rl_readpage_bbs != null) {
            rl_readpage_bbs!!.setOnClickListener(this)
        }
        if (rl_readpage_setting != null) {
            rl_readpage_setting!!.setOnClickListener(this)
        }
        if (rl_setting_more != null) {
            rl_setting_more!!.setOnClickListener(this)
        }
        if (rl_feedback != null) {
            rl_feedback!!.setOnClickListener(this)
        }
        if (rl_mark != null) {
            rl_mark!!.setOnClickListener(this)
        }
        if (checkUpdateGuideRL != null) {
            checkUpdateGuideRL!!.setOnClickListener(this)
        }
        if (clear_cache_rl != null) {
            clear_cache_rl!!.setOnClickListener(this)
        }
        if (disclaimer_statement_rl != null) {
            disclaimer_statement_rl!!.setOnClickListener(this)
        }
        if (bt_night_shift != null) {
            bt_night_shift!!.setOnCheckedChangeListener(this)
        }
        if (bt_wifi_auto != null) {
            bt_wifi_auto!!.setOnCheckedChangeListener(this)
        }
        if (tv_login_info != null) {
            tv_login_info!!.setOnClickListener(this)
        }
        if (user_login_layout_left != null) {
            user_login_layout_left!!.setOnClickListener(this)
        }
    }

    private fun initData() {
        CancelTask()
        cacheAsyncTask = CacheAsyncTask()
        cacheAsyncTask!!.execute()
        val versionName = AppUtils.getVersionName()
        check_update_message!!.text = "V$versionName"
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
        CancelTask()
    }

    /**
     * 福利中心红包动画
     */
    private fun startWelfareCenterAnim() {
        if (img_welfare != null) {
            val animation = AnimationUtils.loadAnimation(this, R.anim.welfare_center_anim)
            img_welfare!!.setAnimation(animation)
            animation.start()
        }
    }

    override fun onClick(paramView: View) {

        when (paramView.getId()) {
            R.id.rl_welfare// 福利中心
            -> {
                PersonalLogger.uploadPersonalADPage()
                val welfareIntent = Intent()
                welfareIntent.putExtra("url", "https://st.quanbennovel.com/static/welfareCenter/welfareCenter.html")
                welfareIntent.putExtra("title", "福利中心")
                welfareIntent.setClass(this@SettingActivity, WelfareCenterActivity::class.java)
                startActivity(welfareIntent)
            }
            R.id.rl_setting_more -> {
                PersonalLogger.uploadPersonalPushSetting()
                startActivity(Intent(this@SettingActivity, SettingMoreActivity::class.java))
            }
//            R.id.rl_style_change -> {
//                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_theme_change)
//                startActivity(Intent(this@SettingActivity, StyleChangeActivity::class.java))
//            }
            R.id.tv_login_info -> Toast.makeText(getApplicationContext(), R.string.enter_community, Toast.LENGTH_SHORT).show()
            R.id.iv_mine_image, R.id.user_login_layout_left -> Toast.makeText(getApplicationContext(), R.string.enter_community, Toast.LENGTH_SHORT).show()
            R.id.check_update_rl -> {
                PersonalLogger.uploadPersonalCheckUpdate()
                checkUpdate()
            }
            R.id.rl_feedback -> {
                PersonalLogger.uploadPersonalFeedback()
                handler.removeCallbacks(feedbackRunnable)
                handler.postDelayed(feedbackRunnable, 500)
            }
            R.id.rl_mark -> {
                PersonalLogger.uploadPersonalMark()
                try {
                    val uri = Uri.parse("market://details?id=" + getPackageName())
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } catch (e: Exception) {
                    ToastUtil.showToastMessage(R.string.menu_no_market)
                }

            }

            R.id.disclaimer_statement_rl -> {
                PersonalLogger.uploadPersonalDisclaimer()
                val bundle = Bundle()
                bundle.putBoolean(RouterUtil.FROM_DISCLAIMER_PAGE, true)
                RouterUtil.navigation(this, RouterConfig.DISCLAIMER_ACTIVITY, bundle) }
//            R.id.rl_readpage_setting -> {
//                //阅读页设置
//                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_read)
//                startActivity(Intent(this@SettingActivity, ReadingSettingActivity::class.java))
//            }
            R.id.rl_readpage_bbs -> Toast.makeText(getApplicationContext(), R.string.enter_community, Toast.LENGTH_SHORT).show()
            R.id.clear_cache_rl//清除缓存的处理
            -> {
                PersonalLogger.uploadPersonalClearCache()
                clearCacheDialog()
            }

            R.id.top_setting_back, R.id.setting_back -> {
                DyStatService.onEvent(EventPoint.PERSONAL_BACK, mapOf("type" to "1"))
                goBackToHome()
            }
            else -> {
            }
        }//                finish();
    }

    private fun checkUpdate() {
        try {
            apkUpdateUtils.getApkUpdateInfo(this, handler, "SettingActivity")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun clearCacheDialog() {
        if (!isFinishing()) {
            myDialog = MyDialog(this, R.layout.publish_hint_dialog)
            myDialog!!.setCanceledOnTouchOutside(false)
            myDialog!!.setCancelable(false)
            myDialog!!.setCanceledOnTouchOutside(true)//设置点击dialog外面对话框消失
            myDialog!!.publish_content.setText(R.string.tip_clear_cache)
            myDialog!!.publish_stay.setOnClickListener({
                dismissDialog()
            })
            myDialog!!.publish_leave.setOnClickListener({
                myDialog!!.publish_content.setVisibility(View.GONE)
                myDialog!!.dialog_title.setText(R.string.tip_cleaning_cache)
                myDialog!!.change_source_bottom.setVisibility(View.GONE)

                myDialog!!.progress_del.setVisibility(View.VISIBLE)
                //添加清除缓存的处理
                object : Thread() {
                    override fun run() {
                        super.run()


                        CacheManager.removeAll()

                        UIHelper.clearAppCache()
                        DataCleanManager.clearAllCache(getApplicationContext())
                        runOnUiThread({
                            dismissDialog()
                            clear_cache_size!!.setText("0B")
                        })
                    }
                }.start()
            })

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
        DyStatService.onEvent(EventPoint.PERSONAL_BACK, mapOf("type" to "2"))
        goBackToHome()
    }

    fun goBackToHome() {
        if (!currentThemeMode!!.equals(mThemeHelper.getMode()) || isStyleChanged) {
            if (getSwipeBackHelper() == null || !getSwipeBackHelper().isSliding()) {//滑动返回已结束
                onThemeSwitch()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onSlideFinishAnimEnd() {
        super.onSlideFinishAnimEnd()
        if (!currentThemeMode!!.equals(mThemeHelper.getMode()) || isStyleChanged) {
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
            PersonalLogger.uploadPersonalNightModeChange()
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
            PersonalLogger.uploadPersonalAutoCache(isChecked)
        }
    }

    private inner class CacheAsyncTask : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void): String {
            var result = "0B"
            try {
                result = DataCleanManager.getTotalCacheSize(getApplicationContext())
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

    override fun finish() {
        super.finish()
        //离线消息 跳转到主页
        if (isFromPush && ActivityLifecycleHelper.getActivities().size <= 1) {
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }

    override fun supportSlideBack(): Boolean {
        return ActivityLifecycleHelper.getActivities().size > 1
    }

    companion object {

        private val PUSH_TIME_SETTING = 1

        var cacheSize: Long = 0
        private val LOGIN_SUCCESS = 0x20
    }

}