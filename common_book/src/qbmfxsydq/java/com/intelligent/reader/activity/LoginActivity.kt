package com.intelligent.reader.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.dingyue.contract.util.debugToastShort
import com.dingyue.contract.util.showToastMessage
import com.dy.reader.activity.DisclaimerActivity
import com.intelligent.reader.R
import com.intelligent.reader.view.login.LoadingDialog
import com.intelligent.reader.view.login.MobileNumberEditText
import iyouqu.theme.FrameActivity
import iyouqu.theme.statusbar.impl.FlymeHelper
import iyouqu.theme.statusbar.impl.MIUIHelper
import kotlinx.android.synthetic.qbmfxsydq.act_login.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.user.Platform
import net.lzbook.kit.user.UserManager
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.logi

class LoginActivity : FrameActivity() {

    private var flagLoginEnd = true

    private val loadingDialog: LoadingDialog by lazy {
        LoadingDialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDarkStatusBar()

        setContentView(R.layout.act_login)
        UserManager.initPlatform(this)

        ll_wechat.setOnClickListener {
            val data = HashMap<String, String>()
            data["type"] = "1"
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.LOGIN,
                    StartLogClickUtil.OTHERLOGIN, data)
            if (!UserManager.isPlatformEnable(Platform.WECHAT)) {
                showToastMessage("请安装微信后重试")
                return@setOnClickListener
            }
            if (flagLoginEnd) {
                flagLoginEnd = false

                loadingDialog.show()

                UserManager.login(this,
                        Platform.WECHAT,
                        onSuccess = { ret ->
                            loadingDialog.dismiss()
                            flagLoginEnd = true
                            this.debugToastShort(ret.toString())
                            setLoginResult()
                            finish()

                        }, onFailure = { t ->
                    loadingDialog.dismiss()
                    flagLoginEnd = true
                    this.debugToastShort(t)
                })


            }
        }

        ll_qq.setOnClickListener {
            val data = HashMap<String, String>()
            data["type"] = "2"
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.LOGIN,
                    StartLogClickUtil.OTHERLOGIN, data)
            if (flagLoginEnd) {
                flagLoginEnd = false

                loadingDialog.show()

                UserManager.login(this,
                        Platform.QQ,
                        onSuccess = { ret ->
                            loadingDialog.dismiss()
                            flagLoginEnd = true
                            this.debugToastShort(ret.toString())
                            setLoginResult()
                            finish()

                        }, onFailure = { t ->
                    loadingDialog.dismiss()
                    flagLoginEnd = true
                    this.debugToastShort(t)
                })

            }
        }

        img_close.setOnClickListener {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.LOGIN,
                    StartLogClickUtil.BACK)
            if (flagLoginEnd) {
                setLoginResult()
                finish()
            }
        }

        etxt_mobile_number.setOnClickListener {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.LOGIN,
                    StartLogClickUtil.PHONELOGIN)
        }

        etxt_mobile_number.setStateListener { stateList ->
            stateList.forEach {
                when (it) {
                    MobileNumberEditText.State.CORRECT -> {
                        txt_number_error.visibility = View.GONE
                        img_number_error.visibility = View.GONE
                    }
                    MobileNumberEditText.State.INCORRECT -> {
                        txt_number_error.visibility = View.VISIBLE
                        img_number_error.visibility = View.VISIBLE
                    }
                    MobileNumberEditText.State.EMPTY -> {
                        img_clear_number.visibility = View.GONE
                    }
                    MobileNumberEditText.State.NOT_EMPTY -> {
                        img_clear_number.visibility = View.VISIBLE
                    }
                    MobileNumberEditText.State.COMPLETE -> {
                        if (etxt_mobile_number.isCorrect()) {
                            txt_fetch_code.isEnabled = true
                            if (etxt_verify_code.isComplete) {
                                txt_login.isEnabled = true
                            }
                        }
                    }
                    MobileNumberEditText.State.NOT_COMPLETE -> {
                        txt_fetch_code.isEnabled = false
                        txt_login.isEnabled = false
                    }
                }
            }
        }

        img_clear_number.setOnClickListener {
            etxt_mobile_number.setText("")
            etxt_mobile_number.showKeyboard()
        }

        etxt_verify_code.setOnCompleteListener { isComplete ->
            val isReady = etxt_mobile_number.isComplete() && etxt_mobile_number.isCorrect() && isComplete
            txt_login.isEnabled = isReady
            if (isReady) {
                etxt_verify_code.hideKeyboard()
                smsLogin()
            }
        }

        txt_fetch_code.setOnClickListener {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.LOGIN,
                    StartLogClickUtil.PIN)
            val number = etxt_mobile_number.getMobileNumber()
            txt_fetch_code.startCountdown()
            etxt_verify_code.showKeyboard()

            UserManager.requestSmsCode(number) { b, s ->
                if (b) {
                    showToastMessage(getString(R.string.fetch_sms_code_success))
                } else {
                    showToastMessage(s)
                }

            }
        }

        txt_login.setOnClickListener {
            smsLogin()
        }

        txt_service_policy.setOnClickListener {
            val intent = Intent(this, DisclaimerActivity::class.java)
            intent.putExtra(IS_SERVICE_POLICY, true)
            startActivity(intent)
        }

        txt_privacy_policy.setOnClickListener {
            val intent = Intent(this, DisclaimerActivity::class.java)
            intent.putExtra(IS_PRIVACY_POLICY, true)
            startActivity(intent)
        }

//        // 上次登录的方式
//        val lastLogin = UserManager.sharedPreferences?.getString(UserManager.LOGIN_METHOD, null)
//        logi("lastLogin: $lastLogin")
//        if (lastLogin == CHANNEL_WX) {
//            txt_wx_login.text = getString(R.string.login_weixin_latest_used)
//        } else if (lastLogin == CHANNEL_QQ) {
//            txt_qq_login.text = getString(R.string.login_qq_latest_used)
//        }
    }

    private fun smsLogin() {
//        val number = etxt_mobile_number.getMobileNumber()
//        val code = etxt_verify_code.text.toString()
//        loadingDialog.show()
//        val data = HashMap<String, String>()
//        UserManager.smsLogin(number, code, {
//            onSuccess {
//                UserManager.keepReadInfo { state, msg ->
//                    uploadLoginSuccessLog("3")
//                    showToastMessage(getString(R.string.login_success))
//                    loadingDialog.dismiss()
//                    data["status"] = "1"
//                    StartLogClickUtil.upLoadEventLog(this@LoginActivity, StartLogClickUtil.LOGIN,
//                            StartLogClickUtil.LOGIN, data)
//                    finish()
//                }
//            }
//            onFailed {
//                uploadLoginErrorLog("3", it.message.toString())
//
//                data["status"] = "2"
//                data["reason"] = it.message.toString()
//                StartLogClickUtil.upLoadEventLog(this@LoginActivity, StartLogClickUtil.LOGIN,
//                        StartLogClickUtil.LOGIN, data)
//
//                loadingDialog.dismiss()
//                if (it is LoginError) {
//                    showToastMessage(it.message.toString())
//                } else {
//                    showToastMessage("网络不给力哦，请稍后再试")
//                }
//            }
//        })
    }

    private fun EditText.showKeyboard() {
        isFocusable = true
        requestFocus()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager).showSoftInput(this, 0)
    }

    private fun EditText.hideKeyboard() {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager).hideSoftInputFromWindow(this.windowToken, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UserManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        val data = HashMap<String, String>()
        data.put("type", "2")
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data)
        if (flagLoginEnd) {
            setLoginResult()
            finish()
        }
    }

    private fun setLoginResult() {
        if (UserManager.isUserLogin) {
            setResult(Activity.RESULT_OK)
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.user_login_succeed)
        } else
            setResult(Activity.RESULT_CANCELED)
    }

    private fun setDarkStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            UI_OPTIONS_IMMERSIVE_STICKY = window.decorView.systemUiVisibility
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            val isMIUISupport = MIUIHelper().setStatusBarLightMode(this, true)
            val isFlymeSupport = FlymeHelper().setStatusBarLightMode(this, true)
            if (isMIUISupport || isFlymeSupport) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        txt_fetch_code.stopCountDown()
    }

    private fun uploadLoginSuccessLog(type: String) {
        val data = java.util.HashMap<String, String>()
        data.put("status", "1")
        data.put("type", type)
        StartLogClickUtil.upLoadEventLog(this@LoginActivity,
                StartLogClickUtil.LOGIN, StartLogClickUtil.LOGINRESULT, data)
    }

    private fun uploadLoginErrorLog(type: String, error: String) {
        val data = java.util.HashMap<String, String>()
        data.put("status", "2")
        data.put("reason", error)
        data.put("type", type)
        StartLogClickUtil.upLoadEventLog(this@LoginActivity,
                StartLogClickUtil.LOGIN, StartLogClickUtil.LOGINRESULT, data)
    }

    companion object {
        const val IS_SERVICE_POLICY = "is_service_policy"
        const val IS_PRIVACY_POLICY = "is_privacy_policy"
    }

    override fun supportSlideBack(): Boolean {
        return false
    }

}
