package com.intelligent.reader.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.intelligent.reader.R
import iyouqu.theme.FrameActivity
import kotlinx.android.synthetic.kdqbdzs.act_login.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.user.Platform
import net.lzbook.kit.user.UserManager
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.toastShort

class LoginActivity : FrameActivity() {

    var flagLoginEnd = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_login)
        UserManager.initPlatform(this)

        ibtn_wechat.setOnClickListener {
            if (!UserManager.isPlatformEnable(Platform.WECHAT)) {
                toastShort("请安装微信后重试", false)
                return@setOnClickListener
            }
            if (flagLoginEnd) {
                flagLoginEnd = false

                showProgressDialog()

                UserManager.login(this,
                        Platform.WECHAT,
                        onSuccess = { ret ->
                            dismissProgressDialog()
                            flagLoginEnd = true
                            toastShort(ret.toString())
                            setLoginResult()
                            finish()
                        },
                        onFailure = { t ->
                            dismissProgressDialog()
                            flagLoginEnd = true
                            toastShort(t)
                        })
            }
        }

        ibtn_qq.setOnClickListener {
            if (flagLoginEnd) {
                flagLoginEnd = false

                showProgressDialog()

                UserManager.login(this,
                        Platform.QQ,
                        onSuccess = { ret ->
                            dismissProgressDialog()
                            flagLoginEnd = true
                            toastShort(ret.toString())
                            setLoginResult()
                            finish()
                        },
                        onFailure = { t ->
                            dismissProgressDialog()
                            flagLoginEnd = true
                            toastShort(t)
                        })
            }
        }

        ibtn_back.setOnClickListener {
            val data = HashMap<String, String>()
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data)
            if (flagLoginEnd) {
                setLoginResult()
                finish()
            }
        }
    }

    private var progressDialog: MyDialog? = null

    private fun showProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
        progressDialog = MyDialog(this, R.layout.publish_hint_dialog)
        progressDialog!!.setCanceledOnTouchOutside(false)
        progressDialog!!.setCancelable(true)

        progressDialog!!.findViewById(R.id.publish_content).setVisibility(View.GONE)
        (progressDialog!!.findViewById(R.id.dialog_title) as TextView).setText(R.string.tips_login)
        progressDialog!!.findViewById(R.id.change_source_bottom).setVisibility(View.GONE)
        progressDialog!!.findViewById(R.id.progress_del).setVisibility(View.VISIBLE)
        progressDialog!!.setOnDismissListener {
            flagLoginEnd = true
        }
        progressDialog!!.show()
    }

    fun dismissProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
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

}
