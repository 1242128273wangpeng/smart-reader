package com.intelligent.reader.activity.usercenter

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.intelligent.reader.R
import com.intelligent.reader.view.login.LoadingDialog
import com.intelligent.reader.view.login.MobileNumberEditText
import kotlinx.android.synthetic.qbmfxsydq.act_bind_phone.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.base.activity.BaseCacheableActivity
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.user.UserManagerV4

/**
 * Date: 2018/7/31 18:21
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 绑定手机号
 */
class BindPhoneActivity : BaseCacheableActivity() {

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_bind_phone)
        initView()
    }

    private val loadingDialog: LoadingDialog by lazy {
        LoadingDialog(this)
    }

    override fun onResume() {
        super.onResume()
        if (!UserManagerV4.isUserLogin) {
            finish()
            return
        }
    }

    private fun initView() {
        img_back.setOnClickListener {
            finish()
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
                                txt_bind.isEnabled = true
                            }
                        }
                    }
                    MobileNumberEditText.State.NOT_COMPLETE -> {
                        txt_fetch_code.isEnabled = false
                        txt_bind.isEnabled = false
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
            txt_bind.isEnabled = isReady
            if (isReady) {
                etxt_verify_code.hideKeyboard()
                smsBind()
            }
        }

        txt_fetch_code.setOnClickListener {
            val number = etxt_mobile_number.getMobileNumber()
            txt_fetch_code.startCountdown()
            etxt_verify_code.showKeyboard()
            UserManagerV4.requestSmsCode(number) { success, result ->
                if (success) {
                    ToastUtil.showToastMessage(getString(R.string.fetch_sms_code_success))

                } else {
                    ToastUtil.showToastMessage(result)

                }

            }

        }

        txt_bind.setOnClickListener {
            smsBind()
        }
    }

    private fun smsBind() {
        val number = etxt_mobile_number.getMobileNumber()
        val code = etxt_verify_code.text.toString()
        loadingDialog.show(getString(R.string.loading_dialog_title_binding))
        val data = java.util.HashMap<String, String>()
        UserManagerV4.bindPhoneNumber(number, code) { success, result ->
            if (success){
                data["status"] = "1"
                StartLogClickUtil.upLoadEventLog(this@BindPhoneActivity,
                        StartLogClickUtil.PROFILE, StartLogClickUtil.BINDPHONE, data)
                UserManagerV4.user?.let {
                    it.phone_number = result?.data?.phone_number
                    UserManagerV4.updateUser(it)
                }
                ToastUtil.showToastMessage(getString(R.string.bind_success))
                loadingDialog.dismiss()
                finish()
            }else{
                data["status"] = "2"
                StartLogClickUtil.upLoadEventLog(this@BindPhoneActivity,
                        StartLogClickUtil.PROFILE, StartLogClickUtil.BINDPHONE, data)
                loadingDialog.dismiss()
                if (result!=null){
                    ToastUtil.showToastMessage(result.message.toString())
                }else{
                    ToastUtil.showToastMessage(resources.getString(R.string.net_work_error))
                }

            }
        }

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

    override fun onBackPressed() {
        super.onBackPressed()
        val data = java.util.HashMap<String, String>()
        data.put("status", "2")
        StartLogClickUtil.upLoadEventLog(this@BindPhoneActivity,
                StartLogClickUtil.PROFILE, StartLogClickUtil.BINDPHONE, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        txt_fetch_code.stopCountDown()
    }
}
