package com.dy.media

import com.dycm_adsdk.PlatformSDK
import com.dycm_adsdk.inter.ConfigInterface
import com.dycm_adsdk.view.NativeView

/**
 * Desc 广告配置 有
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/8 17:03
 */
object MediaConfig : IMediaConfig {

    override fun getConfig(): ConfigInterface? = PlatformSDK.config()

    override fun getAdCount(): Int? = PlatformSDK.config()?.adCount

    override fun getConfigExpireMinutes(): Int? = PlatformSDK.config()?.configExpireMinutes

    override fun getChapter_limit(): Int? = PlatformSDK.config()?.chapter_limit

    override fun getRestAd_sec(): Int? = PlatformSDK.config()?.restAd_sec

    override fun getSwitch_sec(): Int? = PlatformSDK.config()?.switch_sec

    override fun getAdfree_new_user(): Int? = PlatformSDK.config()?.adfree_new_user

    override fun setChannel_code(channel_code: String) {
        PlatformSDK.config().setChannel_code(channel_code)
    }

    override fun setAd_userid(user_id: String) {
        PlatformSDK.config().setAd_userid(user_id)
    }

    override fun setCityName(cityName: String) {
        PlatformSDK.config().setCityName(cityName)
    }

    override fun setCityCode(cityCode: Int) {
        PlatformSDK.config().setCityCode(cityCode)
    }

    override fun setLatitude(latitude: Float) {
        PlatformSDK.config().setLatitude(latitude)
    }

    override fun setLongitude(longitude: Float) {
        PlatformSDK.config().setLongitude(longitude)
    }

    override fun setBookShelfGrid(grid: Boolean) {
        PlatformSDK.config().setBookShelfGrid(grid)
    }

    override fun setCusTomParam(param: String) {
        PlatformSDK.config().setCusTomParam(param)
    }

    override fun getAdSwitch(ad_mark_id: String): Boolean? = PlatformSDK.config()?.getAdSwitch(ad_mark_id)

    override fun ExposureToPlugin(nativeView: NativeView) {
        PlatformSDK.config().ExposureToPlugin(nativeView)
    }

    override fun setExpandInfo(bookmap: Map<String, String>) {
        PlatformSDK.config().setExpandInfo(bookmap)
    }
}