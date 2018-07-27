package com.intelligent.reader.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import com.bumptech.glide.Glide
import com.ding.basic.Config
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.showToastMessage
import com.dy.reader.setting.ReaderSettings
import com.intelligent.reader.R
import com.intelligent.reader.util.EventBookStore

import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.view.ConsumeEvent
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.book.view.SwitchButton
import net.lzbook.kit.cache.DataCleanManager
import net.lzbook.kit.constants.SPKeys
import net.lzbook.kit.user.UserManager
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.*
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.UIHelper
import net.lzbook.kit.utils.update.ApkUpdateUtils

import java.util.HashMap

import de.greenrobot.event.EventBus
import iyouqu.theme.BaseCacheableActivity
import iyouqu.theme.ThemeMode
import kotlinx.android.synthetic.qbmfxsydq.act_setting_user.*


@Route(path = RouterConfig.SETTING_ACTIVITY)
class SettingActivity : BaseCacheableActivity(), View.OnClickListener, SwitchButton.OnCheckedChangeListener {
    var TAG = SettingActivity::class.java.simpleName
    var handler: Handler = object : Handler() {
        override fun handleMessage(message: Message) {
            super.handleMessage(message)
            when (message.what) {

            }
        }
    }
    protected lateinit var currentThemeMode: String //是否切换了主题
    internal var apkUpdateUtils = ApkUpdateUtils(this)
    internal var themeName = TypedValue()//分割块颜色
    private var btnBack: ImageView? = null
    private var top_setting_back: ImageView? = null
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
    private var cacheAsyncTask: CacheAsyncTask? = null
    private var isActivityPause = false
    private var isStyleChanged = false
    private val feedbackRunnable = Runnable { FeedbackAPI.openFeedbackActivity() }
    private var rl_history_setting: RelativeLayout? = null
    private var tv_history_setting: TextView? = null
    private var txt_nickname: TextView? = null
    private var txt_userid: TextView? = null
    private var btn_login: Button? = null
    private var img_head: ImageView? = null
    private var btn_logout: Button? = null
    private var img_head_background: ImageView? = null
    private var txt_login_des: TextView? = null
    private var img_welfare: ImageView? = null
    private var rl_welfare: RelativeLayout? = null

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
        initListener()
        initData()
        sInstance = this
        UserManager.initPlatform(this, null)
    }

    protected fun initView() {

        //用于判断是否显示Textview的Drawable
        is_show_drawable = findViewById(R.id.is_show_drawable)
        top_navigation_bg = findViewById(R.id.top_navigation_bg) as RelativeLayout
        icon_more_left = findViewById(R.id.icon_more_left) as ImageView
        btnBack = findViewById(R.id.setting_back) as ImageView
        top_setting_back = findViewById(R.id.top_setting_back) as ImageView
        user_login_layout = findViewById(R.id.user_login_layout) as RelativeLayout
        iv_mine_image = findViewById(R.id.iv_mine_image) as ImageView
        tv_login_info = findViewById(R.id.tv_login_info) as TextView
        iv_mine_image = findViewById(R.id.iv_mine_image) as ImageView
        tv_login_info = findViewById(R.id.tv_login_info) as TextView
        iv_mine_image_left = findViewById(R.id.iv_mine_image_left) as ImageView
        user_login_layout_left = findViewById(R.id.user_login_layout_left) as RelativeLayout

        rl_readpage_bbs = findViewById(R.id.rl_readpage_bbs) as RelativeLayout
        rl_style_change = findViewById(R.id.rl_style_change) as RelativeLayout
        bt_night_shift = findViewById(R.id.bt_night_shift) as SwitchButton
        bt_wifi_auto = findViewById(R.id.bt_wifi_auto) as SwitchButton
        rl_history_setting = findViewById(R.id.rl_history_setting) as RelativeLayout
        rl_welfare = findViewById(R.id.rl_welfare) as RelativeLayout
        img_welfare = findViewById(R.id.img_welfare) as ImageView
        rl_setting_more = findViewById(R.id.rl_setting_more) as RelativeLayout
        rl_feedback = findViewById(R.id.rl_feedback) as RelativeLayout
        rl_mark = findViewById(R.id.rl_mark) as RelativeLayout
        checkUpdateGuideRL = findViewById(R.id.check_update_rl) as RelativeLayout
        clear_cache_rl = findViewById(R.id.clear_cache_rl) as RelativeLayout
        disclaimer_statement_rl = findViewById(R.id.disclaimer_statement_rl) as RelativeLayout
        rl_setting_layout = findViewById(R.id.rl_setting_layout) as LinearLayout

        theme_name = findViewById(R.id.theme_name) as TextView
        clear_cache_size = findViewById(R.id.check_cache_size) as TextView
        check_update_message = findViewById(R.id.check_update_message) as TextView
//
//        //条目字
        tv_readpage_bbs = findViewById(R.id.tv_readpage_bbs) as TextView
        tv_style_change = findViewById(R.id.tv_style_change) as TextView
        tv_night_shift = findViewById(R.id.tv_night_shift) as TextView
        tv_history_setting = findViewById(R.id.tv_history_setting) as TextView
        tv_setting_more = findViewById(R.id.tv_setting_more) as TextView
        tv_feedback = findViewById(R.id.tv_feedback) as TextView
        tv_mark = findViewById(R.id.tv_mark) as TextView
        text_check_update = findViewById(R.id.text_check_update) as TextView
        text_clear_cache = findViewById(R.id.text_clear_cache) as TextView
        text_disclaimer_statement = findViewById(R.id.text_disclaimer_statement) as TextView

        tv_login_info_left = findViewById(R.id.tv_login_info_left) as TextView
        tv_login_info_detail_left = findViewById(R.id.tv_login_info_detail_left) as TextView
        top_navigation_title = findViewById(R.id.top_navigation_title) as TextView

        txt_nickname = findViewById(R.id.txt_nickname) as TextView
        txt_userid = findViewById(R.id.txt_userid) as TextView
        btn_login = findViewById(R.id.btn_login) as Button
        btn_logout = findViewById(R.id.btn_logout) as Button
        img_head = findViewById(R.id.img_head) as ImageView
        img_head_background = findViewById(R.id.img_head_background) as ImageView
        val desid = resources.getIdentifier("txt_login_des", "id", packageName)

        if (desid != 0) {
            txt_login_des = findViewById(desid) as TextView
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

        bt_wifi_auto!!.isChecked = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SPKeys.Setting.AUTO_UPDATE_CAHCE, true)

        startWelfareCenterAnim()
    }

    private fun startWelfareCenterAnim() {
        img_welfare?.let {
            val animation = AnimationUtils.loadAnimation(this, R.anim.welfare_center_anim)
            img_welfare?.setAnimation(animation)
            animation.start()
        }
    }


    private fun showUserInfo() {
        if (btn_login != null) {
            img_head!!.isClickable = false
            btn_login!!.visibility = View.GONE
            txt_nickname!!.visibility = View.VISIBLE
            txt_userid!!.visibility = View.VISIBLE
            val userInfo = UserManager.mUserInfo
            txt_nickname!!.text = userInfo!!.nickname
            txt_userid!!.text = "ID:" + userInfo.uid
            Glide.with(this).load(userInfo.head_portrait).into(img_head!!)
            rl_logout.visibility = View.VISIBLE

            if (txt_login_des != null) {
                txt_login_des!!.visibility = View.GONE
            }
        }
    }

    private fun hideUserInfo() {
        if (btn_login != null) {
            img_head!!.isClickable = true
            img_head!!.antiShakeClick(this)
            btn_login!!.antiShakeClick(this)

            btn_login!!.visibility = View.VISIBLE
            txt_nickname!!.visibility = View.GONE
            txt_userid!!.visibility = View.GONE
            img_head!!.setImageResource(R.drawable.default_head)
            rl_logout.visibility = View.GONE

            if (txt_login_des != null) {
                txt_login_des!!.visibility = View.VISIBLE
            }
        }
    }

    protected fun initListener() {
        if (btnBack != null) {
            btnBack!!.antiShakeClick(this)
        }
        if (top_setting_back != null) {
            top_setting_back!!.antiShakeClick(this)
        }
        if (user_login_layout != null) {
            user_login_layout!!.antiShakeClick(this)
        }
        if (iv_mine_image != null) {
            iv_mine_image!!.antiShakeClick(this)
        }
        rl_welfare?.setOnClickListener(this)
        if (tv_login_info != null) {
            tv_login_info!!.antiShakeClick(this)
        }
        if (rl_style_change != null) {
            rl_style_change!!.antiShakeClick(this)
        }
        if (rl_readpage_bbs != null) {
            rl_readpage_bbs!!.antiShakeClick(this)
        }
        if (rl_history_setting != null) {
            rl_history_setting!!.antiShakeClick(this)
        }
        if (rl_readpage_setting != null) {
            rl_readpage_setting!!.antiShakeClick(this)
        }
        if (rl_setting_more != null) {
            rl_setting_more!!.antiShakeClick(this)
        }
        if (rl_feedback != null) {
            rl_feedback!!.antiShakeClick(this)
        }
        if (rl_mark != null) {
            rl_mark!!.antiShakeClick(this)
        }
        if (checkUpdateGuideRL != null) {
            checkUpdateGuideRL!!.antiShakeClick(this)
        }
        if (clear_cache_rl != null) {
            clear_cache_rl!!.antiShakeClick(this)
        }
        if (disclaimer_statement_rl != null) {
            disclaimer_statement_rl!!.antiShakeClick(this)
        }
        if (bt_night_shift != null) {
            bt_night_shift!!.setOnCheckedChangeListener(this)
        }
        if (tv_login_info != null) {
            tv_login_info!!.antiShakeClick(this)
        }
        if (user_login_layout_left != null) {
            user_login_layout_left!!.antiShakeClick(this)
        }
        if (btn_login != null) {
            btn_login!!.antiShakeClick(this)
        }
        if (btn_logout != null) {
            rl_logout.antiShakeClick(this)
            btn_logout!!.antiShakeClick(this)
        }
        if (img_head != null) {
            img_head!!.antiShakeClick(this)
        }
        if (bt_wifi_auto != null) {
            bt_wifi_auto!!.setOnCheckedChangeListener(this)
        }
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
        if (UserManager.isUserLogin) {
            showUserInfo()
        } else {
            hideUserInfo()
        }
    }

    override fun onPause() {
        super.onPause()
        isActivityPause = true
    }

    override fun onDestroy() {

        img_welfare?.clearAnimation()

        try {
            setContentView(R.layout.empty)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        super.onDestroy()
        CancelTask()
        sInstance = null
    }

    override fun onClick(paramView: View) {

        when (paramView.id) {
            R.id.rl_setting_more -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.MORESET)
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_more)
                startActivity(Intent(this@SettingActivity, SettingMoreActivity::class.java))
            }
            R.id.tv_login_info -> Toast.makeText(applicationContext, R.string.enter_community, Toast.LENGTH_SHORT).show()
            R.id.iv_mine_image, R.id.user_login_layout_left -> Toast.makeText(applicationContext, R.string.enter_community, Toast.LENGTH_SHORT).show()
            R.id.check_update_rl -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.VERSION)
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_ver)
                checkUpdate()
            }
            R.id.rl_feedback -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.HELP)
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_help)
                handler.removeCallbacks(feedbackRunnable)
                handler.postDelayed(feedbackRunnable, 500)
            }
            R.id.rl_mark -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.COMMENT)
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_help)
                try {
                    val uri = Uri.parse("market://details?id=$packageName")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } catch (e: Exception) {
                    this.applicationContext.showToastMessage(R.string.menu_no_market)
                }

            }

            R.id.disclaimer_statement_rl -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.PROCTCOL)
                RouterUtil.navigation(this, RouterConfig.DISCLAIMER_ACTIVITY)
            }
            R.id.rl_history_setting -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.PERSON_HISTORY)
                EventBus.getDefault().post(ConsumeEvent(R.id.redpoint_setting_history))
                startActivity(Intent(this@SettingActivity, FootprintActivity::class.java))
            }
            R.id.rl_welfare -> {
                val welfareIntent = Intent()
                welfareIntent.putExtra("url", Config.WelfareHost)
                welfareIntent.putExtra("title", "福利中心")
                welfareIntent.setClass(this@SettingActivity, WelfareCenterActivity::class.java)
                startActivity(welfareIntent)
            }

            R.id.rl_readpage_bbs -> Toast.makeText(applicationContext, R.string.enter_community, Toast.LENGTH_SHORT).show()
            R.id.clear_cache_rl//清除缓存的处理
            -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.CACHECLEAR)
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_clear_cache)
                clearCacheDialog()
            }

            R.id.top_setting_back, R.id.setting_back -> {
                val data = HashMap<String, String>()
                data["type"] = "1"
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data)
                goBackToHome()
            }
            R.id.img_head, R.id.btn_login -> {
                if (UserManager.isUserLogin) {

                } else {
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.LOGIN)
                    btn_login!!.isClickable = false
                    val loginIntent = Intent(this, LoginActivity::class.java)
                    startActivityForResult(loginIntent, CODE_REQ_LOGIN)
                }

            }
            R.id.btn_logout, R.id.rl_logout -> {
                logoutDialog()
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.LOGOUT)
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

    private fun logoutDialog() {
        if (myDialog != null && myDialog!!.isShowing) {
            myDialog!!.dismiss()
        }
        myDialog = MyDialog(this, R.layout.publish_hint_dialog)
        myDialog!!.setCanceledOnTouchOutside(false)
        myDialog!!.setCancelable(false)
        myDialog!!.setCanceledOnTouchOutside(true)//设置点击dialog外面对话框消失
        val sure = myDialog!!.findViewById<View>(R.id.publish_stay) as Button
        val cancel = myDialog!!.findViewById<View>(R.id.publish_leave) as Button
        val publish_content = myDialog!!.findViewById<View>(R.id.publish_content) as TextView

        publish_content.setText(R.string.tips_logout)
        sure.setOnClickListener {
            dismissDialog()
        }
        cancel.setOnClickListener {
            dismissDialog()
            if (UserManager.isUserLogin) {
                UserManager.logout(null)

                hideUserInfo()
            }
        }
        myDialog!!.show()
    }

    private fun clearCacheDialog() {
        if (!isFinishing) {
            myDialog = MyDialog(this, R.layout.publish_hint_dialog)
            myDialog!!.setCanceledOnTouchOutside(false)
            myDialog!!.setCancelable(false)
            myDialog!!.setCanceledOnTouchOutside(true)//设置点击dialog外面对话框消失
            val btn_cancle_clear_cache = myDialog!!.findViewById<View>(R.id.publish_stay) as Button
            val btn_confirm_clear_cache = myDialog!!.findViewById<View>(R.id.publish_leave) as Button
            val publish_content = myDialog!!.findViewById<View>(R.id.publish_content) as TextView
            val dialog_title = myDialog!!.findViewById<View>(R.id.dialog_title) as TextView
            publish_content.setText(R.string.tip_clear_cache)
            btn_cancle_clear_cache.antiShakeClick {
                dismissDialog()
            }
            btn_confirm_clear_cache.antiShakeClick {
                publish_content.visibility = View.GONE
                dialog_title.setText(R.string.tip_cleaning_cache)
                myDialog!!.setCanceledOnTouchOutside(false)//设置点击dialog外面对话框消失
                myDialog!!.findViewById<View>(R.id.change_source_bottom).visibility = View.GONE
                myDialog!!.findViewById<View>(R.id.progress_del).visibility = View.VISIBLE
                //添加清除缓存的处理
                object : Thread() {
                    override fun run() {
                        super.run()

                        CacheManager.removeAll()

                        UIHelper.clearAppCache()
                        DataCleanManager.clearAllCache(applicationContext)
                        runOnUiThread {
                            dismissDialog()
                            clear_cache_size!!.text = "0B"
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

    fun goBackToHome() {
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
    }

    private fun dismissDialog() {
        if (myDialog != null && myDialog!!.isShowing) {
            myDialog!!.dismiss()
        }
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
            edit.putBoolean(SPKeys.Setting.AUTO_UPDATE_CAHCE, isChecked)
            edit.apply()
            val data = HashMap<String, String>()
            data["type"] = if (isChecked) "1" else "0"
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.WIFI_AUTOCACHE, data)
        }
    }

    private fun CancelTask() {
        if (cacheAsyncTask != null) {
            cacheAsyncTask!!.cancel(true)
            cacheAsyncTask = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CODE_REQ_LOGIN) {
            btn_login!!.isClickable = true
            if (resultCode == Activity.RESULT_OK && UserManager.isUserLogin) {
                showUserInfo()
            }
            return
        }
        when (resultCode) {
            Activity.RESULT_OK -> {
                setResult(67)
                this.finish()
            }
            else -> {
            }
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

    companion object {

        private val CODE_REQ_LOGIN = 100
        private val PUSH_TIME_SETTING = 1
        private val LOGIN_SUCCESS = 0x20
        var sInstance: SettingActivity? = null
        var cacheSize: Long = 0
    }
}