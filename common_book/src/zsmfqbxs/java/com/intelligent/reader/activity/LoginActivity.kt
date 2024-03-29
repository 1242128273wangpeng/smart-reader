package com.intelligent.reader.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.intelligent.reader.R
import kotlinx.android.synthetic.main.publish_hint_dialog.*
import kotlinx.android.synthetic.zsmfqbxs.act_login.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.ui.activity.base.FrameActivity
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.user.Platform
import net.lzbook.kit.utils.user.UserManager
import net.lzbook.kit.ui.widget.MyDialog

@Route(path = RouterConfig.LOGIN_ACTIVITY)
class LoginActivity : FrameActivity() {

    var flagLoginEnd = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_login)
        UserManager.initPlatform(this)

        ibtn_wechat.setOnClickListener {
            if (!UserManager.isPlatformEnable(Platform.WECHAT)) {
                ToastUtil.showToastMessage("请安装微信后重试！")
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
                            ToastUtil.debugToastShort(ret.toString())
                            setLoginResult()
                            finish()
                        },
                        onFailure = { t ->
                            dismissProgressDialog()
                            flagLoginEnd = true
                            ToastUtil.debugToastShort(t)
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
                            ToastUtil.debugToastShort(ret.toString())
                            setLoginResult()
                            finish()
                        },
                        onFailure = { t ->
                            dismissProgressDialog()
                            flagLoginEnd = true
                            ToastUtil.debugToastShort(t)
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

        progressDialog!!.publish_content.visibility = View.GONE
        (progressDialog!!.findViewById(R.id.dialog_title) as TextView).setText(R.string.tips_login)
        progressDialog!!.change_source_bottom.visibility = View.GONE
        progressDialog!!.progress_del.visibility = View.VISIBLE
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
