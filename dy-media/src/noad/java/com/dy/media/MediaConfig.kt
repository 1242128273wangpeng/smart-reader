package com.dy.media

import com.dycm_adsdk.PlatformSDK
import com.dycm_adsdk.inter.ConfigInterface
import com.dycm_adsdk.view.NativeView

/**
 * Desc 广告配置 无
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/8 17:03
 */
object MediaConfig : IMediaConfig {

    override fun getConfig(): ConfigInterface? = null

    override fun getAdCount(): Int? = null

    override fun getConfigExpireMinutes(): Int? = null

    override fun getChapter_limit(): Int? = null

    override fun getRestAd_sec(): Int? = null

    override fun getSwitch_sec(): Int? = null

    override fun getAdfree_new_user(): Int? = null

    override fun setChannel_code(channel_code: String) {}

    override fun setAd_userid(user_id: String) {}

    override fun setCityName(cityName: String) {}

    override fun setCityCode(cityCode: Int) {}

    override fun setLatitude(latitude: Float) {}

    override fun setLongitude(longitude: Float) {}

    override fun setBookShelfGrid(grid: Boolean) {}

    override fun setCusTomParam(param: String) {}

    override fun getAdSwitch(ad_mark_id: String): Boolean? = null

    override fun ExposureToPlugin(nativeView: NativeView) {}

    override fun setExpandInfo(bookmap: Map<String, String>) {}
}