package net.lzbook.kit.ui.widget

import android.app.Activity
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.net.rx.SchedulerHelper

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import kotlinx.android.synthetic.main.dialog_share.*
import net.lzbook.kit.R
import net.lzbook.kit.app.base.BaseBookApplication
import com.ding.basic.net.Config
import net.lzbook.kit.utils.AppUtils
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.statistics.DyStatService
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.user.UserManager
import net.lzbook.kit.utils.user.UserManagerV4

class ApplicationShareDialog(var activity: Activity?) {

    private val dialog = MyDialog(activity, R.layout.dialog_share, Gravity.BOTTOM)

    init {

        val window = dialog.window
        window.setWindowAnimations(R.style.BottomPopupDialog)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.ll_share_wechat.setOnClickListener {
            requestShareInformation("Wechat")

            DyStatService.onEvent(EventPoint.SHAREPAGE_SHARE, mapOf("type" to "1"))
        }

        dialog.ll_share_wechat_circle.setOnClickListener {
            requestShareInformation("WechatCircle")

            DyStatService.onEvent(EventPoint.SHAREPAGE_SHARE, mapOf("type" to "2"))
        }

        dialog.ll_share_qq.setOnClickListener {
            requestShareInformation("QQ")

            DyStatService.onEvent(EventPoint.SHAREPAGE_SHARE, mapOf("type" to "3"))
        }

        dialog.ll_share_qzone.setOnClickListener {
            requestShareInformation("Qzone")

            DyStatService.onEvent(EventPoint.SHAREPAGE_SHARE, mapOf("type" to "4"))
        }

        dialog.ll_share_url.setOnClickListener {
            requestShareInformation("Url")

            DyStatService.onEvent(EventPoint.SHAREPAGE_SHARE, mapOf("type" to "5"))
        }

        dialog.tv_share_cancel.setOnClickListener {
            dismiss()
            DyStatService.onEvent(EventPoint.SHAREPAGE_CANCEL)
        }
    }

    private fun requestShareInformation(platform: String) {
        val flowable = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestShareInformation()
        if (flowable == null) {
            ToastUtil.showToastMessage("请求分享信息失败，请稍后再试！")
            dismiss()
        } else {
            flowable.compose(SchedulerHelper.schedulerHelper())
                    .compose(SchedulerHelper.schedulerHelper())
                    .subscribe({
                        if (!it.checkResultAvailable()) {
                            ToastUtil.showToastMessage("请求分享信息失败，请稍后再试！")
                        } else {
                            if (TextUtils.isEmpty(it.data?.desc) || TextUtils.isEmpty(it.data?.logo) || TextUtils.isEmpty(it.data?.title) || TextUtils.isEmpty(it.data?.clickUrl)) {
                                ToastUtil.showToastMessage("请求分享信息失败，请稍后再试！")
                            } else {
                                val url = Config.buildRequestUrl(it.data?.clickUrl)

                                if (url == null || TextUtils.isEmpty(url)) {
                                    ToastUtil.showToastMessage("请求分享信息失败，请稍后再试！")
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
                                                ToastUtil.showToastMessage("分享链接已经复制到剪贴板！")
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
                                                ToastUtil.showToastMessage("分享链接已经复制到剪贴板！")
                                                SPUtils.putDefaultSharedBoolean(SPKey.APPLICATION_SHARE_ACTION, true)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        dismiss()
                    }, {
                        ToastUtil.showToastMessage("请求分享信息失败，请稍后再试！")
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