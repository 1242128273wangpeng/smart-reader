package net.lzbook.kit.ui.widget

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.ding.basic.bean.push.BannerInfo
import com.ding.basic.util.sp.SPUtils
import com.dingyue.statistics.DyStatService
import kotlinx.android.synthetic.main.dialog_banner.*
import net.lzbook.kit.R
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.user.UserManager


/**
 * Desc 活动弹窗
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/9/5 15:29
 */
class BannerDialog(val activity: Activity,var intent: Intent) {

    private val dialog = MyDialog(activity, R.layout.dialog_banner, Gravity.CENTER)

    private val bannerWebUrl = "/v4/cn.dingyueWeb.reader/activity/banner"

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
            bundle.putString("title", "推荐书单")
            RouterUtil.navigation(activity, RouterConfig.TABULATION_ACTIVITY, bundle)

            dialog.dismiss()

            // 弹窗点击，status记录登录状态：1未登录、2已登录
            DyStatService.onEvent(EventPoint.SHELF_BANNERPOPUPCLICK, mapOf("status" to if (UserManager.isUserLogin) "2" else "1"))
        }

        dialog.img_close.setOnClickListener {
            dialog.dismiss()
            DyStatService.onEvent(EventPoint.SHELF_BANNERPOPUPCLOSE)
        }

        dialog.setOnShowListener {
            DyStatService.onEvent(EventPoint.SHELF_BANNERPOPUPSHOW)
        }

    }

    fun show(imgUrl: String) {
        if (imgUrl.isEmpty()) {
            updateBannerInfo()
        } else {
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
        val bannerInfo = SPUtils.getDefaultSharedObject(BannerInfo.KEY, BannerInfo::class.java)
                ?: return
        bannerInfo.hasShowed = true
        SPUtils.editDefaultShared  {
            SPUtils.putDefaultSharedObject(BannerInfo.KEY, bannerInfo)
        }
    }

}