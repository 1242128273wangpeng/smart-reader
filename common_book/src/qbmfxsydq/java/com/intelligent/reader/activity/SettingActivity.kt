package com.intelligent.reader.activity


import android.app.Activity
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.net.Config
import com.ding.basic.net.api.service.RequestService
import com.dy.reader.setting.ReaderSettings
import com.intelligent.reader.R
import com.intelligent.reader.activity.usercenter.UserProfileActivity
import net.lzbook.kit.bean.EventBookStore
import kotlinx.android.synthetic.qbmfxsydq.act_setting_user.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.ui.activity.WelfareCenterActivity
import net.lzbook.kit.utils.ApkUpdateUtils
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.antiShakeClick
import net.lzbook.kit.utils.cache.DataCleanManager
import net.lzbook.kit.utils.cache.UIHelper
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import net.lzbook.kit.utils.swipeback.ActivityLifecycleHelper
import net.lzbook.kit.utils.theme.ThemeMode
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.user.UserManagerV4
import net.lzbook.kit.utils.webview.UrlUtils
import net.lzbook.kit.ui.widget.ConsumeEvent
import net.lzbook.kit.ui.widget.MyDialog
import net.lzbook.kit.ui.widget.SwitchButton
import org.greenrobot.eventbus.EventBus
import java.util.*



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
    private var isFromPush = false


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
        UserManagerV4.initPlatform(this, null)
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

        rl_readpage_bbs = findViewById(R.id.rl_readpage_bbs)
        rl_style_change = findViewById(R.id.rl_style_change)
        bt_night_shift = findViewById(R.id.bt_night_shift)
        bt_wifi_auto = findViewById(R.id.bt_wifi_auto)
        rl_history_setting = findViewById(R.id.rl_history_setting)
        rl_welfare = findViewById(R.id.rl_welfare)
        img_welfare = findViewById(R.id.img_welfare)
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
//
//        //条目字
        tv_readpage_bbs = findViewById(R.id.tv_readpage_bbs)
        tv_style_change = findViewById(R.id.tv_style_change)
        tv_night_shift = findViewById(R.id.tv_night_shift)
        tv_history_setting = findViewById(R.id.tv_history_setting)
        tv_setting_more = findViewById(R.id.tv_setting_more)
        tv_feedback = findViewById(R.id.tv_feedback)
        tv_mark = findViewById(R.id.tv_mark)
        text_check_update = findViewById(R.id.text_check_update)
        text_clear_cache = findViewById(R.id.text_clear_cache)
        text_disclaimer_statement = findViewById(R.id.text_disclaimer_statement)

        tv_login_info_left = findViewById(R.id.tv_login_info_left)
        tv_login_info_detail_left = findViewById(R.id.tv_login_info_detail_left)
        top_navigation_title = findViewById(R.id.top_navigation_title)

        txt_nickname = findViewById(R.id.txt_nickname)
        txt_userid = findViewById(R.id.txt_userid)
        btn_login = findViewById(R.id.btn_login)
        btn_logout = findViewById(R.id.btn_logout)
        img_head = findViewById(R.id.img_head)
        img_head_background = findViewById(R.id.img_head_background)
        val desid = resources.getIdentifier("txt_login_des", "id", packageName)

        if (desid != 0) {
            txt_login_des = findViewById(desid)
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
        val user = UserManagerV4.user
        if (btn_login != null && user != null) {
            btn_login!!.setVisibility(View.GONE)
            txt_nickname!!.setVisibility(View.VISIBLE)
            txt_userid!!.setVisibility(View.VISIBLE)
            txt_nickname!!.setText(user.name)
            val id = "ID:" + user.global_number
            txt_userid!!.setText(id)
            if (user.avatar_url.isNullOrEmpty()) {
                img_head!!.setImageResource(R.drawable.default_head)
            } else {
                Glide.with(this)
                        .load(user.avatar_url)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .placeholder(R.drawable.default_head)
                        .error(R.drawable.default_head)
                        .dontAnimate()
                        .into(img_head)
            }
            rl_logout.visibility = View.VISIBLE

            if (txt_login_des != null) {
                txt_login_des!!.setVisibility(View.GONE)
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
        rl_qrcode?.antiShakeClick(this)
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
        if (UserManagerV4.isUserLogin) {
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
                    ToastUtil.showToastMessage(R.string.menu_no_market)
                }

            }

            R.id.disclaimer_statement_rl -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.PROCTCOL)
                val bundle = Bundle()
                bundle.putBoolean(Constants.FROM_DISCLAIMER_PAGE, true)
                RouterUtil.navigation(this, RouterConfig.DISCLAIMER_ACTIVITY, bundle)

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
                if (UserManagerV4.isUserLogin) {
                    val userProfileIntent = Intent(this, UserProfileActivity::class.java)
                    startActivity(userProfileIntent)
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE,
                            StartLogClickUtil.PROFILE)

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
            R.id.rl_qrcode -> {
                val welfareIntent = Intent()
                val uri = RequestService.QR_CODE.replace("{packageName}", AppUtils.getPackageName())
                welfareIntent.putExtra("url", UrlUtils.buildWebUrl(uri, HashMap()))
                welfareIntent.putExtra("title", "和朋友一起读书")
                welfareIntent.setClass(this@SettingActivity, WelfareCenterActivity::class.java)
                startActivity(welfareIntent)
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
            if (UserManagerV4.isUserLogin) {
                UserManagerV4.logout {
                    hideUserInfo()
                }


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
        finish()
    }

    private fun dismissDialog() {
        if (myDialog != null && myDialog!!.isShowing) {
            myDialog!!.dismiss()
        }
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
            if (resultCode == Activity.RESULT_OK && UserManagerV4.isUserLogin) {
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

    companion object {

        private val CODE_REQ_LOGIN = 100
        private val PUSH_TIME_SETTING = 1
        private val LOGIN_SUCCESS = 0x20
        var sInstance: SettingActivity? = null
        var cacheSize: Long = 0
    }
}