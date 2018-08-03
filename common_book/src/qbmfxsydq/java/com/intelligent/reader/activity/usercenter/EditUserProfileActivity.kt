package com.intelligent.reader.activity.usercenter

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import com.intelligent.reader.view.login.LoadingDialog
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.qbmfxsydq.act_edit_profile.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.user.UserManagerV4
import java.util.regex.Pattern

/**
 * Date: 2018/7/31 17:48
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 修改用户昵称Activity
 */
class EditUserProfileActivity : BaseCacheableActivity() {
    private var mUserName: String? = null

    private val loadingDialog: LoadingDialog by lazy {
        LoadingDialog(this)
    }

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_edit_profile)
        setListener()
        getData()
    }

    private fun setListener() {
        img_back.setOnClickListener { finish() }
        save_bt.setOnClickListener {
            val name = user_name_et.text.toString()
            if (name == mUserName) {
                finish()
                return@setOnClickListener
            }
            loadingDialog.show(getString(R.string.loading_dialog_title_saving))

            val data = java.util.HashMap<String, String>()
            UserManagerV4.uploadUserName(name) { success, result ->
                if (success) {
                    data["status"] = "1"
                    StartLogClickUtil.upLoadEventLog(this@EditUserProfileActivity,
                            StartLogClickUtil.PROFILE, StartLogClickUtil.NICKNAME, data)
                    UserManagerV4.user?.let {
                        it.name = result?.data?.name
                        UserManagerV4.updateUser(it)
                    }
                    loadingDialog.dismiss()
                    showToastMessage(getString(R.string.edit_success))
                    finish()

                } else {
                    data["status"] = "2"
                    StartLogClickUtil.upLoadEventLog(this@EditUserProfileActivity,
                            StartLogClickUtil.PROFILE, StartLogClickUtil.NICKNAME, data)
                    loadingDialog.dismiss()
                    if (result != null) {
                        showToastMessage(result.message.toString())
                    } else {
                        showToastMessage(resources.getString(R.string.net_work_error))
                    }
                }


            }


        }
        content_delete_iv.setOnClickListener {
            user_name_et.setText("")
            user_name_et.showKeyboard()
        }

        user_name_et.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isNotEmpty()) {
                    content_delete_iv.visibility = View.VISIBLE
                    checkDataValid(s.toString())
                } else {
                    content_delete_iv.visibility = View.GONE
                    save_bt.isEnabled = false
                    user_name_fail_tv.visibility = View.INVISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
    }

    private fun checkDataValid(data: String) {
        if (data.length in 2..10) {
            if (isNotLetterOrDigit(data)) {
                save_bt.isEnabled = false
                user_name_fail_tv.visibility = View.VISIBLE
                user_name_fail_tv.text = getString(R.string.edit_user_info_standard_fail_tip1)
                user_name_et.setTextColor(resources.getColor(R.color.edir_user_profile_fail_text_color))
            } else {
                save_bt.isEnabled = true
                user_name_fail_tv.visibility = View.INVISIBLE
                user_name_et.setTextColor(resources.getColor(R.color.color_black))
            }
        } else {
            save_bt.isEnabled = false
            user_name_fail_tv.visibility = View.VISIBLE
            user_name_fail_tv.text = getString(R.string.edit_user_info_standard_fail_tip2)
            user_name_et.setTextColor(resources.getColor(R.color.edir_user_profile_fail_text_color))
        }
    }


    fun isNotLetterOrDigit(str: String): Boolean {
        var LETTER_OR_DIGIT = "[^a-zA-Z0-9\\u4E00-\\u9FA5]"
        return Pattern.compile(LETTER_OR_DIGIT).matcher(str).find()
    }

    private fun getData() {
        mUserName = intent.getStringExtra("userName")
        mUserName?.let {
            user_name_et.setText(it)
//            user_name_et.setSelection(it.length)
        }
    }

    private fun EditText.showKeyboard() {
        isFocusable = true
        requestFocus()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager).showSoftInput(this, 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val data = java.util.HashMap<String, String>()
        data.put("status", "2")
        StartLogClickUtil.upLoadEventLog(this@EditUserProfileActivity,
                StartLogClickUtil.PROFILE, StartLogClickUtil.NICKNAME)
    }


}