package com.dy.reader.activity

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.mobstat.StatService
import com.ding.basic.net.Config
import com.dy.reader.R
import kotlinx.android.synthetic.main.act_disclaimer.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.ui.activity.base.FrameActivity
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.ui.widget.MyDialog
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.toast.ToastUtil
import java.util.*

/**
 * Function：使用协议 / 转码声明
 *
 * Created by JoannChen on 2018/7/11 0011 17:25
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
@Route(path = RouterConfig.DISCLAIMER_ACTIVITY)
class DisclaimerActivity : FrameActivity() {

    var loadingPage: LoadingPage? = null

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_disclaimer)


        // 阅读页转码声明
        val isFromReadingPage = intent.getBooleanExtra(RouterUtil.FROM_READING_PAGE, false)
        if (isFromReadingPage) {
            txt_title.text = resources.getString(R.string.translate_code)
            txt_content.text = resources.getString(R.string.translate_code_description)
        }


        // 登录页服务条款
        val isServicePolicy = intent.getBooleanExtra(RouterUtil.SERVICE_POLICY, false)
        if (isServicePolicy) {
            txt_title.text = resources.getString(R.string.login_service_policy)
            txt_content.text = resources.getString(R.string.service_policy_description)

        }

        // 登录页隐私条款
        val isPrivacyPolicy = intent.getBooleanExtra(RouterUtil.PRIVACY_POLICY, false)
        if (isPrivacyPolicy) {
            txt_title.text = resources.getString(R.string.login_privacy_policy)
            txt_content.text = resources.getString(R.string.privacy_policy_description)

        }

        // 使用协议页面
        val isFormDisclaimerPage = intent.getBooleanExtra(RouterUtil.FROM_DISCLAIMER_PAGE, false)
        if (isFormDisclaimerPage) {
            txt_title.text = resources.getString(R.string.disclaimer_statement)
            web_disclaimer.visibility = View.VISIBLE

            loadingPage = LoadingPage(this, rl_disclaimer_main, LoadingPage.setting_result)

            web_disclaimer.webViewClient = object : WebViewClient() {
                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?,
                                                error: SslError?) {
                    handler?.proceed()
                }


                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    loadingPage?.onErrorVisable()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    loadingPage?.onSuccess()

                }
            }

            if (NetWorkUtils.isNetworkAvailable(this)) {
                // 使用协议转H5时，根据包名拼接地址时，将包名中.替换为-，新壳2特殊处理，直接使用包名
                web_disclaimer.loadUrl("${Config.cdnHost}/${AppUtils.getPackageNameFor_()}/protocol/protocol.html")
            } else {
                loadingPage?.onErrorVisable()
            }

            loadingPage?.setReloadAction(LoadingPage.reloadCallback {
                web_disclaimer.loadUrl("about:blank")
                web_disclaimer?.loadUrl("${Config.cdnHost}/${AppUtils.getPackageNameFor_()}/protocol/protocol.html")
            })

            // 修改字体大小
            val fontSize = resources.getDimension(R.dimen.text_size_small)
            web_disclaimer.settings.defaultFontSize = fontSize.toInt()

            //可以打开调试模式
            rl_disclaimer.setOnClickListener {
                displayEggs()
            }
        }


        img_back.setOnClickListener {
            val data = HashMap<String, String>()
            data["type"] = "1"
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PROCTCOL_PAGE, StartLogClickUtil.BACK, data)
            finish()
        }

    }

    /**
     * 存放点击事件次数
     */
    private var mHits: LongArray? = null

    /**
     * 测试彩蛋
     */
    private fun displayEggs() {

        if (mHits == null) {
            mHits = LongArray(5) // 需要点击几次 就设置几
        }

        mHits?.let {
            //把从第二位至最后一位之间的数字复制到第一位至倒数第一位
            System.arraycopy(mHits, 1, mHits, 0, it.size - 1)

            //记录一个时间
            it[it.size - 1] = SystemClock.uptimeMillis()
            if (SystemClock.uptimeMillis() - it[0] <= 5000) {//5秒内连续点击。
                mHits = null    //这里说明一下，我们在进来以后需要还原状态，否则如果点击过快，第六次，第七次 都会不断进来触发该效果。重新开始计数即可

                if ("DEBUG" == AppUtils.getChannelId()) {
                    RouterUtil.navigation(this, RouterConfig.DEBUG_ACTIVITY)
                } else {
                    showAdminDialog()
                }
            }
        }
    }

    /**
     * 展示管理员权限对话框
     */
    private fun showAdminDialog() {
        val dialog = MyDialog(this, R.layout.layout_debug, Gravity.CENTER)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(true)
        dialog.show()

        val cancelBtn = dialog.findViewById<Button>(R.id.btn_cancel)
        val confirmBtn = dialog.findViewById<Button>(R.id.btn_confirm)
        val adminEditText = dialog.findViewById<EditText>(R.id.edit_admin)

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        confirmBtn.setOnClickListener {
            if ("鼎阅集团" == adminEditText.text.toString()) {
                RouterUtil.navigation(this, RouterConfig.DEBUG_ACTIVITY)
            } else {
                ToastUtil.showToastMessage("身份验证失败")
            }
            dialog.dismiss()
        }


    }

    override fun onResume() {
        super.onResume()
        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
    }
}