package net.lzbook.kit.widget

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import com.ding.basic.Config
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.rx.SchedulerHelper
import kotlinx.android.synthetic.main.dialog_share.*
import net.lzbook.kit.R
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.base.BaseBookApplication
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.sp.SPKey
import net.lzbook.kit.utils.sp.SPUtils
import net.lzbook.kit.utils.toast.showToastMessage
import net.lzbook.kit.utils.user.UserManager
import net.lzbook.kit.utils.user.UserManagerV4
import java.util.*

class ApplicationShareDialog(var activity: Activity?) {

    private val dialog = MyDialog(activity, R.layout.dialog_share, Gravity.BOTTOM)

    init {

        val window = dialog.window
        window.setWindowAnimations(R.style.BottomPopupDialog)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.ll_share_wechat.setOnClickListener {
            requestShareInformation("Wechat")

            val data = HashMap<String, String>()
            data["type"] = "1"
            StartLogClickUtil.upLoadEventLog(activity?.applicationContext, StartLogClickUtil.PAGE_SHARE, StartLogClickUtil.ACTION_SHARE, data)
        }

        dialog.ll_share_wechat_circle.setOnClickListener {
            requestShareInformation("WechatCircle")

            val data = HashMap<String, String>()
            data["type"] = "2"
            StartLogClickUtil.upLoadEventLog(activity?.applicationContext, StartLogClickUtil.PAGE_SHARE, StartLogClickUtil.ACTION_SHARE, data)
        }

        dialog.ll_share_qq.setOnClickListener {
            requestShareInformation("QQ")

            val data = HashMap<String, String>()
            data["type"] = "3"
            StartLogClickUtil.upLoadEventLog(activity?.applicationContext, StartLogClickUtil.PAGE_SHARE, StartLogClickUtil.ACTION_SHARE, data)
        }

        dialog.ll_share_qzone.setOnClickListener {
            requestShareInformation("Qzone")

            val data = HashMap<String, String>()
            data["type"] = "4"
            StartLogClickUtil.upLoadEventLog(activity?.applicationContext, StartLogClickUtil.PAGE_SHARE, StartLogClickUtil.ACTION_SHARE, data)
        }

        dialog.ll_share_url.setOnClickListener {
            requestShareInformation("Url")

            val data = HashMap<String, String>()
            data["type"] = "5"
            StartLogClickUtil.upLoadEventLog(activity?.applicationContext, StartLogClickUtil.PAGE_SHARE, StartLogClickUtil.ACTION_SHARE, data)
        }

        dialog.tv_share_cancel.setOnClickListener {
            dismiss()
            StartLogClickUtil.upLoadEventLog(activity?.applicationContext, StartLogClickUtil.PAGE_SHARE, StartLogClickUtil.ACTION_CANCEL)
        }
    }

    private fun requestShareInformation(platform: String) {
        val flowable = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestShareInformation()
        if (flowable == null) {
            activity?.showToastMessage("请求分享信息失败，请稍后再试！")
            dismiss()
        } else {
            flowable.compose(SchedulerHelper.schedulerHelper())
                    .compose(SchedulerHelper.schedulerHelper())
                    .subscribe({
                        if (!it.checkResultAvailable()) {
                            activity?.showToastMessage("请求分享信息失败，请稍后再试！")
                        } else {
                            if (TextUtils.isEmpty(it.data?.desc) || TextUtils.isEmpty(it.data?.logo) || TextUtils.isEmpty(it.data?.title) || TextUtils.isEmpty(it.data?.clickUrl)) {
                                activity?.showToastMessage("请求分享信息失败，请稍后再试！")
                            } else {
                                val url = Config.buildRequestUrl(it.data?.clickUrl)

                                if (url == null || TextUtils.isEmpty(url)) {
                                    activity?.showToastMessage("请求分享信息失败，请稍后再试！")
                                } else {
                                    if("cc.quanben.novel" == AppUtils.getPackageName()){
                                        when (platform) {
                                            "Wechat" -> {
                                                UserManagerV4.shareWechat(activity, it.data?.title ?: "", it.data?.desc ?: "", url, it.data?.logo ?: "")
                                            }
                                            "WechatCircle" -> {
                                                UserManagerV4.shareWechatCircle(activity, it.data?.title ?: "", it.data?.desc ?: "", url, it.data?.logo ?: "")
                                            }
                                            "QQ" -> {
                                                UserManagerV4.shareQQ(activity, it.data?.title ?: "", it.data?.desc ?: "", url, it.data?.logo ?: "")
                                            }
                                            "Qzone" -> {
                                                UserManagerV4.shareQzone(activity, it.data?.title ?: "", it.data?.desc ?: "", url, it.data?.logo ?: "")
                                            }
                                            "Url" -> {
                                                val clipboardManager = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clipData = ClipData.newPlainText("Label", url)
                                                clipboardManager.primaryClip = clipData
                                                activity?.showToastMessage("分享链接已经复制到剪贴板！")
                                                SPUtils.putDefaultSharedBoolean(SPKey.APPLICATION_SHARE_ACTION, true)
                                            }
                                        }
                                    }else{
                                        when (platform) {
                                            "Wechat" -> {
                                                UserManager.shareWechat(activity, it.data?.title ?: "", it.data?.desc ?: "", url, it.data?.logo ?: "")
                                            }
                                            "WechatCircle" -> {
                                                UserManager.shareWechatCircle(activity, it.data?.title ?: "", it.data?.desc ?: "", url, it.data?.logo ?: "")
                                            }
                                            "QQ" -> {
                                                UserManager.shareQQ(activity, it.data?.title ?: "", it.data?.desc ?: "", url, it.data?.logo ?: "")
                                            }
                                            "Qzone" -> {
                                                UserManager.shareQzone(activity, it.data?.title ?: "", it.data?.desc ?: "", url, it.data?.logo ?: "")
                                            }
                                            "Url" -> {
                                                val clipboardManager = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clipData = ClipData.newPlainText("Label", url)
                                                clipboardManager.primaryClip = clipData
                                                activity?.showToastMessage("分享链接已经复制到剪贴板！")
                                                SPUtils.putDefaultSharedBoolean(SPKey.APPLICATION_SHARE_ACTION, true)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        dismiss()
                    }, {
                        activity?.showToastMessage("请求分享信息失败，请稍后再试！")
                        dismiss()
                    }, {

                    })
        }
    }

    fun show() {
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.ll_share_content.visibility = View.VISIBLE
        dialog.ll_share_loading.visibility = View.GONE
        dialog.show()
    }

    fun showLoading() {
        dialog.ll_share_content.visibility = View.GONE
        dialog.ll_share_loading.visibility = View.VISIBLE
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
    }

    fun dismiss() {
        dialog.dismiss()
    }
}