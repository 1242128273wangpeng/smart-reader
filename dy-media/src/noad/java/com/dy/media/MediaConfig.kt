package com.dy.media

/**
 * Desc 广告配置 无
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/8 17:03
 */
object MediaConfig {

    fun getConfig(): Any? = null

    fun getAdCount(): Int? = null

    fun getConfigExpireMinutes(): Int? = null

    fun getChapter_limit(): Int? = null

    fun getRestAd_sec(): Int? = null

    fun getSwitch_sec(): Int? = null

    fun getAdfree_new_user(): Int? = null

    fun setChannel_code(channel_code: String) {}

    fun setAd_userid(user_id: String) {}

    fun setCityName(cityName: String) {}

    fun setCityCode(cityCode: Int) {}

    fun setLatitude(latitude: Float) {}

    fun setLongitude(longitude: Float) {}

    fun setBookShelfGrid(grid: Boolean) {}

    fun setCusTomParam(param: String) {}

    fun getAdSwitch(ad_mark_id: String): Boolean? = null

    fun ExposureToPlugin(nativeView: Any) {}

    fun setExpandInfo(bookmap: Map<String, String>) {}
}