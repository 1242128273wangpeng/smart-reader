package com.ding.basic.bean

import java.io.Serializable

/**
 * Desc 动态参数
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/8/7 18:10
 */

data class Parameter(
        var success: Boolean?,
        var error_log: String?,
        var params: Any?,
        var map: Map?
) : Serializable

data class Map(
        var DY_in_chapter_frequence: String?,
        var new_app_ad_switch: String?,
        var NONET_READHOUR: String?,
        var Dy_ad_readPage_slide_switch: String?,
        var ad_interstitial_count: String?,
        var limit_versioncode: String?,
        var DY_page_in_chapter_ad_switch: String?,
        var P2P_4G_ENABLE: String?,
        var Dy_ad_new_statistics_switch: String?,
        var user_transfer_first: String?,
        var recommend_bookcover: String?,
        var webView_host: String?,
        var DY_switch_ad_sec: String?,
        var shenzhen_log: String?,
        var DY_mid_page_frequence: String?,
        var shanghai_log: String?,
        var noNetReadNumber: String?,
        var hot_words: String?,
        var DY_adfree_new_user: String?,
        var ad_limit_time_day: String?,
        var DY_ad_click_freq: String?,
        var book_shelf_state: String?,
        var JX_ad_version: String?,
        var P2P_WIFI_ENABLE: String?,
        var download_limit: String?,
        var DY_rest_ad_switch: String?,
        var DY_shelf_ad_switch: String?,
        var show_ad_version: String?,
        var DY_ad_click_action_type: String?,
        var Dy_ad_readPage_slide_switch_new: String?,
        var day_limit: String?,
        var DY_shelf_boundary_switch: String?,
        var DY_rest_ad_sec: String?,
        var baidu_examine: String?,
        var DY_book_end_ad_switch: String?,
        var channel_limit: String?,
        var httpsWebView_host: String?,
        var push_key: String?,
        var novel_host: String?,
        var union_host: String?,
        var content_host: String?,
        var DY_page_end_ad_freq: String?,
        var Dy_readPage_statistics_switch: String?,
        var DY_ad_new_request_domain_name: String?,
        var DY_shelf_ad_freq: String?,
        var user_transfer_second: String?,
        var DY_page_in_chapter_limit: String?,
        var DY_splash_ad_switch: String?,
        var limit_channelid: String?,
        var DY_page_end_ad_switch: String?,
        var DY_ad_switch: String?,
        var DY_is_new_reading_end: String?,
        var DY_activited_switch_ad: String?,
        var DY_switch_ad_close_sec: String?,
        var P2P_ENABLE: String?,
        var baidu_stat_id: String?,
        var DY_page_middle_ad_switch: String?,
        var DY_ad_old_request_switch: String?
) : Serializable