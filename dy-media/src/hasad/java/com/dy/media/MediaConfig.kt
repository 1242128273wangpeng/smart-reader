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
object MediaConfig {

    fun getConfig(): ConfigInterface? = PlatformSDK.config()

    fun getAdCount(): Int? = PlatformSDK.config()?.adCount

    fun getConfigExpireMinutes(): Int? = PlatformSDK.config()?.configExpireMinutes

    fun getChapter_limit(): Int? = PlatformSDK.config()?.chapter_limit

    fun getRestAd_sec(): Int? = PlatformSDK.config()?.restAd_sec

    fun getSwitch_sec(): Int? = PlatformSDK.config()?.switch_sec

    fun getAdfree_new_user(): Int? = PlatformSDK.config()?.adfree_new_user

    fun setChannel_code(channel_code: String) {
        PlatformSDK.config().setChannel_code(channel_code)
    }

    fun setAd_userid(user_id: String) {
        PlatformSDK.config().setAd_userid(user_id)
    }

    fun setCityName(cityName: String) {
        PlatformSDK.config().setCityName(cityName)
    }

    fun setCityCode(cityCode: Int) {
        PlatformSDK.config().setCityCode(cityCode)
    }

    fun setLatitude(latitude: Float) {
        PlatformSDK.config().setLatitude(latitude)
    }

    fun setLongitude(longitude: Float) {
        PlatformSDK.config().setLongitude(longitude)
    }

    fun setBookShelfGrid(grid: Boolean) {
        PlatformSDK.config().setBookShelfGrid(grid)
    }

    fun setCusTomParam(param: String) {
        PlatformSDK.config().setCusTomParam(param)
    }

    fun getAdSwitch(ad_mark_id: String): Boolean? = PlatformSDK.config()?.getAdSwitch(ad_mark_id)

    fun ExposureToPlugin(nativeView: NativeView) {
        PlatformSDK.config().ExposureToPlugin(nativeView)
    }

    fun setExpandInfo(bookmap: Map<String, String>) {
        PlatformSDK.config().setExpandInfo(bookmap)
    }
}