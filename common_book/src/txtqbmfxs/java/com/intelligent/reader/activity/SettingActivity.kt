package com.intelligent.reader.activity

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
import android.view.animation.AnimationUtils
import android.widget.*
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import com.bumptech.glide.Glide
import com.dingyue.contract.router.RouterConfig
import com.intelligent.reader.util.EventBookStore
import de.greenrobot.event.EventBus
import iyouqu.theme.BaseCacheableActivity
import iyouqu.theme.ThemeMode
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.view.ConsumeEvent
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.book.view.SwitchButton
import net.lzbook.kit.cache.DataCleanManager
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.SPKeys
import net.lzbook.kit.user.UserManager
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.UIHelper
import net.lzbook.kit.utils.update.ApkUpdateUtils
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
    private var myDialog: MyDialog? = null//清除缓存对话框


    //第二种布局 登录在左侧


    private var cacheAsyncTask: CacheAsyncTask? = null
    private var isActivityPause = false
    private var isStyleChanged = false
    private val feedbackRunnable = Runnable { FeedbackAPI.openFeedbackActivity() }


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

    private fun initView() {

        //用于判断是否显示Textview的Drawable


        //条目字

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


        //字体颜色
        mTextViewList = ArrayList()
        val tvNum = arrayOf<TextView>(tv_readpage_bbs, tv_style_change, tv_night_shift, tv_history_setting, tv_readpage_setting, tv_setting_more, tv_feedback, tv_mark, text_check_update, text_clear_cache, text_disclaimer_statement, tv_login_info_left)

        for (textView in tvNum) {
            mTextViewList!!.add(textView)
        }

        if (txt_login_des != null)
            mTextViewList!!.add(txt_login_des)


        //条目背景
        mRelativeLayoutList = ArrayList()
        val rlNum = arrayOf<RelativeLayout>(rl_readpage_bbs, rl_style_change, rl_history_setting, rl_readpage_setting, rl_setting_more, rl_feedback, rl_mark, checkUpdateGuideRL, clear_cache_rl, disclaimer_statement_rl)

        for (relativeLayout in rlNum) {
            mRelativeLayoutList!!.add(relativeLayout)
        }


        //15条分割线 和 3个gap
        mDivider = ArrayList()
        val viewNum = arrayOf<View>(findViewById(R.id.v_divider), findViewById(R.id.v_divider1), findViewById(R.id.v_divider2), findViewById(R.id.v_divider3), findViewById(R.id.v_divider4), findViewById(R.id.v_divider5), findViewById(R.id.v_divider6), findViewById(R.id.v_divider7), findViewById(R.id.v_divider8), findViewById(R.id.v_divider9), findViewById(R.id.v_divider10), findViewById(R.id.v_divider11), findViewById(R.id.v_divider12), findViewById(R.id.v_divider13), findViewById(R.id.v_divider14), findViewById(R.id.v_divider15), findViewById(R.id.v_divider16), findViewById(R.id.v_divider17), findViewById(R.id.v_divider18))

        for (view in viewNum) {
            mDivider!!.add(view)
        }

        val mGap = ArrayList<String>()
        mGap.add(findViewById(R.id.v_gap1))
        mGap.add(findViewById(R.id.v_gap2))
        mGap.add(findViewById(R.id.v_gap3))
        mGap.add(findViewById(R.id.v_gap4))
        mGap.add(findViewById(R.id.v_gap5))

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

    private fun initListener() {
        fun initListener() {
            btnBack.antiShakeClick(this)
            top_setting_back.antiShakeClick(this)
            user_login_layout.antiShakeClick(this)
            iv_mine_image.antiShakeClick(this)
            tv_login_info.setOnClickListener(this)
            rl_style_change.setOnClickListener(this)
            rl_readpage_bbs.setOnClickListener(this)
            rl_history_setting.setOnClickListener(this)
            rl_welfare.setOnClickListener(this)
            rl_readpage_setting.setOnClickListener(this)
            rl_setting_more.setOnClickListener(this)
            rl_feedback.setOnClickListener(this)
            rl_mark.setOnClickListener(this)
            checkUpdateGuideRL.setOnClickListener(this)
            clear_cache_rl.setOnClickListener(this)
            disclaimer_statement_rl.setOnClickListener(this)
            bt_night_shift.setOnCheckedChangeListener(this)
            bt_wifi_auto.setOnCheckedChangeListener(this)
            tv_login_info.setOnClickListener(this)
            user_login_layout_left.setOnClickListener(this)
            btn_login.setOnClickListener(this)
            rl_logout.setOnClickListener(this)
            btn_logout.setOnClickListener(this)
            img_head.setOnClickListener(this)

        }

    }

    private fun initData() {
        CancelTask()
        cacheAsyncTask = CacheAsyncTask()
        cacheAsyncTask!!.execute()
        val versionName = AppUtils.getVersionName()
        check_update_message!!.text = "V" + versionName
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
            img_head!!.setImageResource(R.mipmap.my_top_pic)
            rl_logout.visibility = View.GONE

            if (txt_login_des != null) {
                txt_login_des!!.visibility = View.VISIBLE
            }
        }

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
        sInstance = null
    }

    override fun onClick(paramView: View) {

        when (paramView.id) {
            R.id.rl_setting_more -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.MORESET)
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_more)
                startActivity(Intent(this@SettingActivity, SettingMoreActivity::class.java))
            }
            R.id.rl_style_change -> {
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_theme_change)
                startActivity(Intent(this@SettingActivity, StyleChangeActivity::class.java))
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
                    val uri = Uri.parse("market://details?id=" + packageName)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } catch (e: Exception) {
                    showToastShort(R.string.menu_no_market)
                }

            }

            R.id.disclaimer_statement_rl -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.PROCTCOL)
                IntentUtils.INSTANCE.start(this, DisclaimerActivity::class.java, IntentUtils.INSTANCE.isFormDisclaimerPage(), true, false)
            }
            R.id.rl_history_setting -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.PERSON_HISTORY)
                EventBus.getDefault().post(ConsumeEvent(R.id.redpoint_setting_history))
                startActivity(Intent(this@SettingActivity, FootprintActivity::class.java))
            }
            R.id.rl_welfare -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.ADPAGE)
                val welfareIntent = Intent()
                welfareIntent.putExtra("url", "https://st.quanbennovel.com/static/welfareCenter/welfareCenter.html")
                welfareIntent.putExtra("title", "福利中心")
                welfareIntent.setClass(this@SettingActivity, WelfareCenterActivity::class.java)
                startActivity(welfareIntent)
            }
            R.id.rl_readpage_setting -> {
                //阅读页设置
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_read)
                startActivity(Intent(this@SettingActivity, ReadingSettingActivity::class.java))
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
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.BACK, data)
                goBackToHome()
            }
            R.id.img_head, R.id.btn_login -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.LOGIN)
                btn_login!!.isClickable = false
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivityForResult(loginIntent, CODE_REQ_LOGIN)
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
        val sure = myDialog!!.findViewById(R.id.publish_stay) as Button
        val cancel = myDialog!!.findViewById(R.id.publish_leave) as Button
        val publish_content = myDialog!!.findViewById(R.id.publish_content) as TextView

        publish_content.setText(R.string.tips_logout)
        sure.setOnClickListener(object : Button.OnClickListener {
            override fun onClick(v: View) {
                dismissDialog()
            }
        })
        cancel.setOnClickListener(object : Button.OnClickListener {
            override fun onClick(v: View) {
                dismissDialog()
                if (UserManager.isUserLogin) {
                    UserManager.logout(null)

                    hideUserInfo()
                }
            }
        })
        myDialog!!.show()
    }

    private fun clearCacheDialog() {
        if (!isFinishing) {
            myDialog = MyDialog(this, R.layout.publish_hint_dialog)
            myDialog!!.setCanceledOnTouchOutside(false)
            myDialog!!.setCancelable(false)
            myDialog!!.setCanceledOnTouchOutside(true)//设置点击dialog外面对话框消失
            val btn_cancle_clear_cache = myDialog!!.findViewById(R.id.publish_stay) as Button
            val btn_confirm_clear_cache = myDialog!!.findViewById(R.id.publish_leave) as Button
            val publish_content = myDialog!!.findViewById(R.id.publish_content) as TextView
            val dialog_title = myDialog!!.findViewById(R.id.dialog_title) as TextView
            publish_content.setText(R.string.tip_clear_cache)
            btn_cancle_clear_cache.setOnClickListener(object : Button.OnClickListener {
                override fun onClick(v: View) {
                    dismissDialog()
                }
            })
            btn_confirm_clear_cache.setOnClickListener(object : Button.OnClickListener {
                override fun onClick(v: View) {
                    publish_content.visibility = View.GONE
                    dialog_title.setText(R.string.tip_cleaning_cache)
                    myDialog!!.findViewById(R.id.change_source_bottom).setVisibility(View.GONE)

                    myDialog!!.findViewById(R.id.progress_del).setVisibility(View.VISIBLE)
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
            if (isChecked) {
                tv_night_shift!!.setText(R.string.mode_day)
                edit.putInt("current_light_mode", Constants.MODE)
                Constants.MODE = 61
                mThemeHelper.setMode(ThemeMode.NIGHT)
            } else {
                tv_night_shift!!.setText(R.string.mode_night)
                edit.putInt("current_night_mode", Constants.MODE)
                Constants.MODE = sharedPreferences.getInt("current_light_mode", 51)
                mThemeHelper.setMode(ThemeMode.THEME1)
            }
            edit.putInt("content_mode", Constants.MODE)
            edit.apply()
            nightShift(isChecked, true)
        } else if (view.id == R.id.bt_wifi_auto) {
            edit.putBoolean(SPKeys.Setting.AUTO_UPDATE_CAHCE, isChecked)
            edit.apply()
            val data = HashMap<String, String>()
            data.put("type", if (isChecked) "1" else "0")
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.WIFI_AUTOCACHE, data)
        }
    }

    private fun CancelTask() {
        if (cacheAsyncTask != null) {
            cacheAsyncTask!!.cancel(true)
            cacheAsyncTask = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
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