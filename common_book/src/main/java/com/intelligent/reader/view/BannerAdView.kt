package com.intelligent.reader.view

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageLoader
import com.dingyueads.sdk.Native.YQNativeAdInfo
import com.dingyueads.sdk.NativeInit
import com.intelligent.reader.R
import de.greenrobot.event.EventBus
import kotlinx.android.synthetic.main.ad_item_small_layout_header.view.*
import net.lzbook.kit.cache.imagecache.ImageCacheManager
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.EventBookshelfAd
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.StatisticManager
import net.lzbook.kit.utils.toastShort

/**
 * Created by qiantao on 2017/11/18 0018
 */
class BannerAdView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : RelativeLayout(context, attrs) {

    private val tag = "BannerAdView"

    private var statisticManager: StatisticManager = StatisticManager.getStatisticManager() //AD 位

    private var yqNativeAdInfo: YQNativeAdInfo? = null

    init {
        View.inflate(context, R.layout.ad_item_small_layout_header, this)
        setOnClickListener {
            try {
                statisticManager.schedulingRequest(context as Activity?, this, yqNativeAdInfo,
                        null, StatisticManager.TYPE_CLICK, NativeInit.ad_position[0])
                if (yqNativeAdInfo != null
                        && com.dingyueads.sdk.Constants.AD_TYPE_360 == yqNativeAdInfo?.advertisement?.platformId) {
                    val eventBookshelfAd = EventBookshelfAd("bookshelfclick_360",
                            0 / Constants.dy_shelf_ad_freq, yqNativeAdInfo)
                    EventBus.getDefault().post(eventBookshelfAd)
                }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            StatServiceUtils.statBookEventClick(context, StatServiceUtils.type_ad_shelf)
            if (Constants.DEVELOPER_MODE) context.toastShort("你点击了广告")
        }
    }

    fun showBannerAd(adInfo: YQNativeAdInfo) {
        AppLog.e(tag, "showBannerAd adInfo: $adInfo")
        yqNativeAdInfo = adInfo
        val advertisement = adInfo.advertisement ?: return
        AppLog.e(tag, "showBannerAd adInfo.advertisement: ${adInfo.advertisement}")
        if (advertisement.platformId == com.dingyueads.sdk.Constants.AD_TYPE_INMOBI && adInfo.inMobiNative != null) {
            val inMobiView = adInfo.inMobiNative.getPrimaryViewOfWidth(this, null, item_ad_image_rl.measuredWidth)
            if (inMobiView != null) {
                item_ad_image_rl.removeAllViews()
                item_ad_image_rl.addView(inMobiView)
                item_ad_image_rl.visibility = View.VISIBLE
                item_ad_image.visibility = View.GONE
                AppLog.e(tag, "showBannerAd inMobiView")
            } else {
                this.visibility = View.GONE
            }
        } else if (!TextUtils.isEmpty(advertisement.iconUrl)
                || com.dingyueads.sdk.Constants.AD_TYPE_KDXF == advertisement.platformId
                && !TextUtils.isEmpty(advertisement.imageUrl)) {
            val url = if (advertisement.iconUrl == null) advertisement.imageUrl else advertisement.iconUrl
            ImageCacheManager.getInstance().imageLoader.get(url, object : ImageLoader.ImageListener {
                override fun onResponse(imageContainer: ImageLoader.ImageContainer?, b: Boolean) {
                    if (imageContainer != null) {
                        val bitmap = imageContainer.bitmap
                        if (bitmap != null) {
                            item_ad_image_rl.visibility = View.INVISIBLE
                            item_ad_image.setImageBitmap(bitmap)
                            item_ad_image.visibility = View.VISIBLE
                            AppLog.e(tag, "showBannerAd ivAd")
                        }
                    }
                }

                override fun onErrorResponse(volleyError: VolleyError) {
                    this@BannerAdView.visibility = View.GONE
                }
            })
        }
        item_ad_title.text = if (TextUtils.isEmpty(advertisement.title)) "" else advertisement.title
        item_ad_desc.text = if (TextUtils.isEmpty(advertisement.description)) "" else advertisement.description
        when {
            "广点通" == advertisement.rationName -> item_ad_right_down.setImageResource(R.drawable.zhuishu_ad)
            "百度" == advertisement.rationName -> item_ad_right_down.setImageResource(R.drawable.icon_ad_bd)
            "360" == advertisement.rationName -> item_ad_right_down.setImageResource(R.drawable.icon_ad_360)
            else -> item_ad_right_down.setImageResource(R.drawable.icon_ad_default)
        }
        try {
            val adSceneData = adInfo.adSceneData
            adSceneData?.ad_showSuccessTime = (System.currentTimeMillis() / 1000L).toString()
            statisticManager.schedulingRequest(context as Activity?, this, adInfo,
                    null, StatisticManager.TYPE_SHOW, NativeInit.ad_position[0])
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        this.visibility = View.VISIBLE//显示bannerAd
    }
}