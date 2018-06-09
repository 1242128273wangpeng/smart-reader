package com.dy.media

import com.dycm_adsdk.PlatformSDK
import com.dycm_adsdk.inter.ConfigInterface
import com.dycm_adsdk.view.NativeView

/**
 * Desc 请描述这个文件
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/8 17:01
 */
interface IMediaConfig {

    fun getConfig(): ConfigInterface?
    fun getAdCount(): Int? //获取广告间隔数
    fun getConfigExpireMinutes(): Int? //获取广告刷新频率
    fun getChapter_limit(): Int? //章节内 出现5-2 的频率
    fun getRestAd_sec(): Int? //休息页间隔时间
    fun getSwitch_sec(): Int? //切屏间隔时间
    fun getAdfree_new_user(): Int? // 新用户 多久显示广告

    fun setChannel_code(channel_code: String) // 设置渠道 ID
    fun setAd_userid(user_id: String) // 设置userId
    fun setCityName(cityName: String) // 设置城市名称
    fun setCityCode(cityCode: Int) // 设置城市编号
    fun setLatitude(latitude: Float) // 设置城市纬度
    fun setLongitude(longitude: Float) // 设置城市经度
    fun setBookShelfGrid(grid: Boolean) // 设置九宫格标识符
    fun setCusTomParam(param: String) // 自定义参数设置
    fun getAdSwitch(ad_mark_id: String): Boolean? // 根据markid 获取 开关状态
    fun ExposureToPlugin(nativeView: NativeView) //曝光回调接口
    fun setExpandInfo(bookmap: Map<String, String>) //设置booK info 信息
}