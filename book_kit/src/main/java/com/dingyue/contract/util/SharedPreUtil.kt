package com.dingyue.contract.util

import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils
import com.google.gson.Gson
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.utils.AppUtils


/**
 * Desc sharepre 工具类
 * Author zhenxiang
 * Mail zhenxiang_lin@dingyuegroup.cn
 * Date 2018\5\26 0026 10:12
 */
class SharedPreUtil(val type: Int) {

    companion object {

        @JvmField
        val SHARE_DEFAULT = 0  // 保存的是默认地址
        @JvmField
        val SHARE_ONLINE_CONFIG = 1  // 保存的是 onlineconfig_agent_online_setting_ + AppUtils.getPackageName()
        val SHAREDPREFERENCES_KEY = "onlineconfig_agent_online_setting_" + AppUtils.getPackageName()

        // 当前阅读的书籍
        const val CURRENT_READ_BOOK = "current_read_book"


        /**
         * DebugActivity
         */
        @Deprecated("")
        val API_URL = "api_url"// api地址
        @Deprecated("")
        val WEB_URL = "web_url"// web地址
        const val START_PARAMS = "start_params"           //启动动态参数,默认是开启
        const val PRE_SHOW_AD = "pre_show_ad"             //提前展示广告
        const val RESET_BOOK_SHELF = "reset_book_shelf"   //重置书架
        const val UPDATE_CHAPTER = "update_chapter"       //更新章节
        const val SHOW_TOAST_LOG = "show_toast_log"       //打点Toast显示，方便h5查看打点

        const val HOST_LIST = "host_list"//host列表

        /**
         * HOST分类
         */
        const val NOVEL_HOST = "novel_host"           //智能API接口
        const val WEBVIEW_HOST = "httpsWebView_host"  // WebView
        const val UNION_HOST = "union_host"           //微服务API接口
        const val CONTENT_HOST = "content_host"       //微服务内容接口
        const val USER_TAG_HOST = "user_tag_host"       //用户标签接口

        /**
         * 禁用动态参数前保留一份动态HOST
         */
        const val NOVEL_PRE_HOST = "novel_pre_host"
        const val WEBVIEW_PRE_HOST = "httpsWebView_pre_host"
        const val UNION_PRE_HOST = "union_pre_host"
        const val CONTENT_PRE_HOST = "content_pre_host"


        /**
         * DynamicParameter
         */
        const val DYNAMIC_VERSION = "dynamic_version"
        const val CHANNEL_LIMIT = "channel_limit"
        const val DAY_LIMIT = "day_limit"
        const val RECOMMEND_BOOKCOVER = "recommend_bookcover" //书籍封面页推荐位智能，青果书籍配比

        const val NO_NET_READ_NUMBER = "noNetReadNumber"

        const val PUSH_KEY = "push_key"

        const val BAIDU_EXAMINE = "baidu_examine"
        const val BAIDU_STAT_ID = "baidu_stat_id"

        const val USER_TRANSFER_FIRST = "user_transfer_first"
        const val USER_TRANSFER_SECOND = "user_transfer_second"

        const val AD_LIMIT_TIME_DAY = "ad_limit_time_day"

        const val NATIVE_AD_PAGE_INTERSTITIAL_COUNT = "DY_mid_page_frequence"//章节末广告间隔
        const val NATIVE_AD_PAGE_GAP_IN_CHAPTER = "DY_in_chapter_frequence"//章节内广告间隔（章数）
        const val NATIVE_AD_PAGE_IN_CHAPTER_LIMIT = "DY_page_in_chapter_limit"//章节内要展现广告限制的最小页数

        const val DY_AD_NEW_REQUEST_DOMAIN_NAME = "DY_ad_new_request_domain_name"//新的用户广告数据搜集接口域名获取key
        const val DY_ACTIVITED_SWITCH_AD = "DY_activited_switch_ad"
        const val DY_SWITCH_AD_SEC = "DY_switch_ad_sec"
        const val DY_SWITCH_AD_CLOSE_SEC = "DY_switch_ad_close_sec"
        const val DY_PAGE_MIDDLE_AD_SWITCH = "DY_page_middle_ad_switch"//章节末广告开关
        const val DY_AD_NEW_REQUEST_SWITCH = "DY_ad_new_request_switch"//新的用户广告请求开关
        const val DY_AD_NEW_STATISTICS_SWITCH = "Dy_ad_new_statistics_switch"// 新的统计开关
        const val DY_READPAGE_STATISTICS_SWITCH = "Dy_readPage_statistics_switch"// 阅读页翻页统计开关
        const val DY_AD_READPAGE_SLIDE_SWITCH_NEW = "Dy_ad_readPage_slide_switch_new"// 阅读页上下翻页展示广告开关
        const val DY_AD_OLD_REQUEST_SWITCH = "DY_ad_old_request_switch"//老的广告统计开关key
        const val DY_AD_SWITCH = "DY_ad_switch"//广告总开关
        const val DY_ADFREE_NEW_USER = "DY_adfree_new_user"//新用户不显示广告的时间
        const val DY_SPLASH_AD_SWITCH = "DY_splash_ad_switch"//开屏广告开关
        const val DY_SHELF_AD_SWITCH = "DY_shelf_ad_switch"//书架广告开关
        const val DY_SHELF_BOUNDARY_SWITCH = "DY_shelf_boundary_switch"//书架1-2广告开关
        const val DY_SHELF_AD_FREQ = "DY_shelf_ad_freq"//书架广告频率
        const val DY_PAGE_END_AD_SWITCH = "DY_page_end_ad_switch"//章节末广告开关
        const val DY_PAGE_END_AD_FREQ = "DY_page_end_ad_freq"//章节末广告频率
        const val DY_BOOK_END_AD_SWITCH = "DY_book_end_ad_switch"//书末广告开关
        const val DY_REST_AD_SWITCH = "DY_rest_ad_switch"//休息页广告开关
        const val DY_REST_AD_SEC = "DY_rest_ad_sec"//休息页广告休息时间
        const val DY_IS_NEW_READING_END = "DY_is_new_reading_end"//是否启用新版章节末UI

        const val NEW_APP_AD_SWITCH = "new_app_ad_switch"//新壳广告开关

        const val BOOK_SHELF_STATE = "book_shelf_state"//九宫格书架页广告显示类型切换开关 1表示横向header, 2 表示九宫格列表形式


        /**
         * 广告分版本，分渠道，分广告位开关控制
         */
        const val AD_CONTROL_STATUS = "status" //广告动态参数总开关 0关 1 开
        const val AD_CONTROL_PGK = "packageName" //广告动态参数 包名
        const val AD_CONTROL_CHANNELID = "channelId" //广告动态参数 渠道号
        const val AD_CONTROL_VERSION = "version" //广告动态参数 app版本号
        const val AD_CONTROL_ADTYPE = "advertisingSpace" //广告动态参数  广告位类型  0 福利中心 1书架页1-1  2书架页1-2  3阅读页广告 4全部


        /**
         * webviewFragment
         */

        const val RANK_SELECT_SEX = "rank_select_sex"   // 五步替 榜单选男女
        const val RECOMMEND_SELECT_SEX = "recommend_select_sex"   // 五步替 精选页选男女


        /**
         * SplashActivity
         */
        const val SCREEN_WIDTH = "screen_width"   //屏幕宽度
        const val SCREEN_HEIGHT = "screen_height" //屏幕高度
        const val SETTINGS_PUSH = "settings_push" //是否接受推送消息
        const val CREATE_SHOTCUT = "createshotcut" //是否创建桌面快捷
        const val FIRST_GUIDE = "first_guide"         //是否显示引导页
        const val AUTO_DOWNLOAD_WIFI = "auto_download_wifi" //wifi下是否自动缓存
        const val BOOKLIST_SORT_TYPE = "booklist_sort_type" //书架排序方式
        const val ADD_DEFAULT_BOOKS = "add_default_books" //添加默认书籍
        const val USER_NEW_INDEX = "user_new_index"       // * 0: 新用户：无广告* 1：新用户：两天内无广告 * 2：老用户：显示广告
        const val USER_NEW_AD_LIMIT_DAY = "user_new_ad_limit_day" //新用户前两天不显示广告
        const val UPDATE_CHAPTER_SOURCE_ID = "update_chapter_source_id" //更新当前章节

        val DATABASE_REMARK = "database_remark"
        const val GENDER_TAG = "gender" //开屏选男女

        /**
         * HomeActivity
         */
        const val HOME_FINDBOOK_SEARCH = "findbook_search" //打点搜索按钮来源 recommend  top   class  author（作者主页）
        const val HOME_TODAY_FIRST_OPEN_APP = "today_first_open_app"  //是否每天第一次打开 用于上传APP列表
        const val HOME_IS_UPLOAD = "IS_UPLOAD"  //是否上传了用户信息  zn_user
        const val HOME_TODAY_FIRST_POST_BOOKIDS = "today_first_post_bookids" //每天上传一次书架上的书
        const val CONTENT_MODE = "content_mode"  //当前阅读页背景

        /**
         * SettingActivity
         */
        const val CURRENT_NIGHT_MODE = "current_night_mode" //当前是否是夜间模式
        const val AUTO_UPDATE_CAHCE = "AUTO_UPDATE_CAHCE" //wifi下是否自动追更书籍 开关


        /**
         * Bookshelf
         */
        val BOOKSHELF_GUIDE_TAG = AppUtils.getVersionCode().toString() + "bookshelf_guide_tag" //书架引导  和versionCode拼接
        const val BOOKSHELF_ISSHOW_CHANGE_GUIDE = "isShowChangAnGuide" //判断是否显示长按删除书籍引导  快读替 新版引导页
        const val BOOKSHELF_BOOK_RACKUP_DATETIME = "bookRackUpdateTime"
        const val BOOKSHELF_PERSON_RED = "bookshelf_preson_red" //用于书架页上的红点显示隐藏

        /**
         * SearchBookActivity
         */
        const val SERARCH_HOT_WORD = "search_hot_word" //搜索热词 做缓存用
        const val SERARCH_HOT_WORD_YOUHUA = "search_hot_word_youhua";// 4个替壳 新版搜索热词,防止升级用户首次没网，从缓存拿数据时报错


        /**
         * ReadingActivity
         */
        const val READING_GUIDE_TAG = "reading_guide_tag"       //阅读页引导页
        const val READING_SETING_GUIDE_TAG = "reading_setting_guide_tag" //阅读页设置引导
        const val READ_PAGE_MODE = "page_mode"              //阅读页翻页模式
        const val READ_FULL_SCREEN_READ = "full_screen_read"//阅读页全屏阅读
        const val READ_FULLWINDOW = "read_fullwindow"       //阅读页是否设置全屏
        const val READ_SOUND_TURNOVER = "sound_turnover"    //阅读页音量
        const val READ_SCREEN_MODE = "screen_mode"  //阅读页横竖屏模式
        const val READ_MODE = "mode"    //阅读页白天夜间
        const val READ_LOCK_SCREEN_TIME = "lock_screen_time" //阅读页锁屏时间
        const val READED_CONT = "readed_count"    // 统计阅读章节数
        const val READ_CURRENT_LIGHT_MODE = "current_light_mode" //当前阅读背景
        const val READ_CONTENT_MODE = "content_mode"  //当前阅读页背景
        const val READ_UPLOAD_OLDUSER_READ_SETTING = "upload_olduser_read_setting"; // 老用户每天上传一次阅读页设置
        const val READ_NOVEL_FONT_SIZE = "novel_font_size"  //阅读页文字大小
        const val READ_SCREEN_BRIGHT = "screen_bright"      //阅读页屏幕亮度
        const val READ_AUTO_BRIGHTNESS = "auto_brightness"  //是否自动阅读
        const val READ_INTERLINEAR_SPACE = "read_interlinear_space" //阅读页行间距
        const val READ_PARAGRAPH_SPACE = "read_paragraph_space"     //阅读页段间距
        const val READ_CONTENT_PAGE_TOP_SPACE = "read_content_page_top_space"   //阅读页距顶部的间距
        const val READ_CONTENT_PAGE_LEFT_SPACE = "read_content_page_left_space" //阅读页距左右的间距

        /**
         * push
         */
        @JvmField
        val PUSH_TAG_LATEST_UPDATE_TIME = "push_tag_latest_update_time"
        val PUSH_LATEST_SHOW_SETTING_DIALOG_TIME = "push_latest_show_setting_dialog_time"


        const val APPLICATION_SHARE_ACTION = "application_share_action"
        const val COVER_SHARE_PROMPT = "cover_share_prompt"

        const val READER_TYPE_FACE = "reader_type_face" //阅读页当前使用的字体
    }


    private val shareList = listOf<SharedPreferences>(
            PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext()),
            BaseBookApplication.getGlobalContext().getSharedPreferences(SHAREDPREFERENCES_KEY, 0)

    )


    private lateinit var sp: SharedPreferences


    init {
        when (type) {
            SHARE_DEFAULT -> sp = shareList[0]
            SHARE_ONLINE_CONFIG -> sp = shareList[1]

        }
    }


    fun putBoolean(key: String, value: Boolean) {
        sp.edit().putBoolean(key, value).apply()
    }

    fun putInt(key: String, value: Int) {
        sp.edit().putInt(key, value).apply()
    }

    fun putString(key: String, value: String?) {
        sp.edit().putString(key, value).apply()
    }

    fun putLong(key: String, value: Long) {
        sp.edit().putLong(key, value).apply()
    }

    fun getBoolean(key: String): Boolean {
        return sp.getBoolean(key, false)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sp.getBoolean(key, defaultValue)
    }

    fun getInt(key: String): Int {
        return sp.getInt(key, 0)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return sp.getInt(key, defaultValue)
    }

    fun getString(key: String): String {
        return sp.getString(key, "")
    }

    fun getString(key: String, defaultValue: String): String {
        return sp.getString(key, defaultValue)
    }

    fun getLong(key: String): Long {
        return sp.getLong(key, 0L)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return sp.getLong(key, defaultValue)
    }


    fun <T> getObject(key: String, clazz: Class<T>): T? {
        val json = getString(key, "")
        if (TextUtils.isEmpty(json)) {
            return null
        }
        return try {
            val gson = Gson()
            gson.fromJson<T>(json, clazz)
        } catch (e: Exception) {
            null
        }

    }

    fun putObject(key: String, obj: Any) {
        val gson = Gson()
        val json = gson.toJson(obj)
        putString(key, json)
    }
}