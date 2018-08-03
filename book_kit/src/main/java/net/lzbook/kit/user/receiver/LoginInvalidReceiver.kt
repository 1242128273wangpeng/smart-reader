package com.intelligent.reader.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.intelligent.reader.activity.LoginActivity
import com.intelligent.reader.view.login.LoginInvalidDialog
import net.lzbook.kit.utils.loge

/**
 * Desc 登录失效
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/16 0016 11:30
 */
class LoginInvalidReceiver(val activity: Activity, private val cancelListener: () -> Unit) : BroadcastReceiver() {

    private var dialog: LoginInvalidDialog? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        loge("LoginInvalidReceiver", "context: $context", "intent.action: ${intent?.action}")
        if (activity.isFinishing) {
            return
        }
        if (dialog?.isShowing() == true) {
            dialog?.dismiss()
            return
        }
        dialog = LoginInvalidDialog(activity)
        dialog?.setOnConfirmListener {
            activity.startActivity(Intent(context, LoginActivity::class.java))
        }
        dialog?.setOnCancelListener {
            cancelListener.invoke()
        }
        dialog?.show()

    }

}