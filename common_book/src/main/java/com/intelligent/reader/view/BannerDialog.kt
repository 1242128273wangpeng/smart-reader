package com.intelligent.reader.view

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.ding.basic.bean.push.BannerInfo
import com.ding.basic.util.editShared
import com.ding.basic.util.getSharedObject
import com.ding.basic.util.putObject
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.intelligent.reader.R
import kotlinx.android.synthetic.main.dialog_banner.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.user.UserManager


/**
 * Desc 活动弹窗
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/9/5 15:29
 */
class BannerDialog(val activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_banner, Gravity.CENTER)

    private var bannerWebTitle = "推荐书单"
    private var bannerWebUrl = "/v4/cn.dingyueWeb.reader/activity/banner"

    init {

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(true)

        dialog.img_banner.setOnClickListener {
//            val intent = Intent()
//            intent.setClass(activity, FindBookDetail::class.java)
//            intent.putExtra("url", bannerWebUrl)
//            intent.putExtra("title", "推荐书单")
//            activity.startActivity(intent)

            val bundle = Bundle()
            bundle.putString("url", bannerWebUrl)
            bundle.putString("title", bannerWebTitle)
            bundle.putString("from", "banner_dialog")
            RouterUtil.navigation(activity, RouterConfig.TABULATION_ACTIVITY, bundle)

            dialog.dismiss()

            // 弹窗点击，status记录登录状态：1未登录、2已登录
            val data = HashMap<String, String>()
            data.put("status", if (UserManager.isUserLogin) "2" else "1")
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.PAGE_SHELF,
                    StartLogClickUtil.BANNER_POPUP_CLICK, data)
        }

        dialog.img_close.setOnClickListener {
            dialog.dismiss()
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.PAGE_SHELF,
                    StartLogClickUtil.BANNER_POPUP_CLOSE)
        }

        dialog.setOnShowListener {
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.PAGE_SHELF,
                    StartLogClickUtil.BANNER_POPUP_SHOW)
        }

    }

    fun show(bannerInfo: BannerInfo) {
        val imgUrl = bannerInfo.url
        if (imgUrl?.isEmpty() == true) {
            updateBannerInfo()
        }

        val activityUrl = bannerInfo.nativeActivityUrl
        if (imgUrl?.isNotEmpty() == true && activityUrl?.isNotEmpty() == true) {

            val strArr = activityUrl.split(",")[0]
            bannerWebTitle = strArr[0].toString()
            bannerWebUrl = strArr[1].toString()

            Glide.with(activity)
                    .load(imgUrl)
                    .into(simpleTarget)
        }

    }


    private val simpleTarget = object : SimpleTarget<GlideDrawable>() {
        override fun onResourceReady(resource: GlideDrawable?, glideAnimation: GlideAnimation<in GlideDrawable>?) {
            resource?.let {
                if (activity.isFinishing) return
                dialog.show()
                dialog.img_banner.setImageDrawable(it)
                updateBannerInfo()
            }
        }
    }

    private fun updateBannerInfo() {
        val bannerInfo = activity.getSharedObject(BannerInfo.KEY, BannerInfo::class.java)
                ?: return
        bannerInfo.hasShowed = true
        activity.editShared {
            putObject(BannerInfo.KEY, bannerInfo)
        }
    }

}