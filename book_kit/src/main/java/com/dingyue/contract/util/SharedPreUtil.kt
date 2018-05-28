package com.dingyue.contract.util

import android.content.Context
import android.content.SharedPreferences
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.utils.AppUtils


/**
 * Desc sharepre 工具类
 * Author zhenxiang
 * Mail zhenxiang_lin@dingyuegroup.cn
 * Date 2018\5\26 0026 10:12
 */
object SharedPreUtil{

    /**
     * SplashActivity
     */
    val SCREEN_WIDTH = "screen_width"  //屏幕宽度
    val SCREEN_HEIGHT = "screen_height" //屏幕高度
    val SETTINGS_PUSH = "settings_push" //是否接受推送消息
    val CREATE_SHOTCUT = "createshotcut" //是否创建桌面快捷
    val FIRST_GUIDE = "first_guide" //是否显示引导页
    val AUTO_DOWNLOAD_WIFI = "auto_download_wifi" //wifi下是否自动缓存
    val BOOKLIST_SORT_TYPE = "booklist_sort_type" //书架排序方式
    val ADD_DEFAULT_BOOKS = "add_default_books" //添加默认书籍
    val USER_NEW_INDEX = "user_new_index" // * 0: 新用户：无广告* 1：新用户：两天内无广告 * 2：老用户：显示广告
    val USER_NEW_AD_LIMIT_DAY = "user_new_ad_limit_day" //新用户前两天不显示广告
    val UPDATE_CHAPTER_SOURCE_ID = "update_chapter_source_id" //更新当前章节


    /**
     * HomeActivity
     */
    val HOME_FINDBOOK_SEARCH = "findbook_search" //打点搜索按钮来源 recommend  top   class  author（作者主页）
    val HOME_TODAY_FIRST_OPEN_APP = "today_first_open_app"  //是否每天第一次打开 用于上传APP列表
    val HOME_IS_UPLOAD = "IS_UPLOAD"  //是否上传了用户信息  zn_user
    val HOME_TODAY_FIRST_POST_BOOKIDS = "today_first_post_bookids" //每天上传一次书架上的书


    /**
     * SettingActivity
     */

    val CURRENT_NIGHT_MODE = "current_night_mode" //当前是否是夜间模式
    val AUTO_UPDATE_CAHCE = "AUTO_UPDATE_CAHCE" //wifi下是否自动追更书籍 开关


    /**
     * Bookshelf
     */
    val BOOKSHELF_GUIDE_TAG = AppUtils.getVersionCode().toString()+"bookshelf_guide_tag" //书架引导  和versionCode拼接
    val BOOKSHELF_ISSHOW_CHANGE_GUIDE = "isShowChangAnGuide" //判断是否显示长按删除书籍引导  快读替 新版引导页
    val BOOKSHELF_BOOK_RACKUP_DATETIME = "bookRackUpdateTime"



    /**
     * CoverPageActivity
     */
    val RECOMMEND_BOOKCOVER = "recommend_bookcover" //书籍封面页 推荐书籍配比


    /**
     * SearchBookActivity
     */
    val SERARCH_HOT_WORD = "search_hot_word" //搜索热词 做缓存用
    val SERARCH_HOT_WORD_YOUHUA = "search_hot_word_youhua";// 4个替壳 新版搜索热词,防止升级用户首次没网，从缓存拿数据时报错



    /**
     * ReadingActivity
     */
    val READING_GUIDE_TAG = "reading_guide_tag" //阅读页引导页
    val READING_SETING_GUIDE_TAG = "reading_setting_guide_tag" //阅读页设置引导
    val READ_PAGE_MODE = "page_mode" //阅读页翻页模式
    val READ_FULL_SCREEN_READ = "full_screen_read" //阅读页全屏阅读
    val READ_FULLWINDOW = "read_fullwindow" //阅读页是否设置全屏
    val READ_SOUND_TURNOVER = "sound_turnover" //阅读页音量
    val READ_SCREEN_MODE = "screen_mode" //阅读页横竖屏模式
    val READ_MODE = "mode" //阅读页白天夜间
    val READ_LOCK_SCREEN_TIME = "lock_screen_time" //阅读页锁屏时间
    val READED_CONT = "readed_count"    // 统计阅读章节数
    val READ_CURRENT_LIGHT_MODE = "current_light_mode" //当前阅读背景
    val READ_CONTENT_MODE = "content_mode"  //当前阅读页背景
    val READ_UPLOAD_OLDUSER_READ_SETTING = "upload_olduser_read_setting"; // 老用户每天上传一次阅读页设置
    val READ_NOVEL_FONT_SIZE = "novel_font_size" //阅读页文字大小
    val READ_SCREEN_BRIGHT = "screen_bright" //阅读页屏幕亮度
    val READ_AUTO_BRIGHTNESS = "auto_brightness" //是否自动阅读
    val READ_INTERLINEAR_SPACE = "read_interlinear_space" //阅读页行间距
    val READ_PARAGRAPH_SPACE = "read_paragraph_space" //阅读页段间距
    val READ_CONTENT_PAGE_TOP_SPACE = "read_content_page_top_space" //阅读页距顶部的间距
    val READ_CONTENT_PAGE_LEFT_SPACE = "read_content_page_left_space" //阅读页距左右的间距



    private val fileName:String = "share_data"

    private val sp:SharedPreferences by lazy {
        BaseBookApplication.getGlobalContext()
                .getSharedPreferences(fileName, Context.MODE_PRIVATE)
    }

    fun putBoolean(key: String, value: Boolean) {
        sp.edit().putBoolean(key, value).apply()
    }

    fun putInt(key: String, value: Int) {
        sp.edit().putInt(key, value).apply()
    }

    fun putString(key: String, value: String) {
        sp.edit().putString(key, value).apply()
    }

    fun putLong(key: String, value: Long) {
        sp.edit().putLong(key, value).apply()
    }

    fun getBoolean(key: String): Boolean {
        return sp.getBoolean(key, false)
    }

    fun getInt(key: String): Int {
        return sp.getInt(key, 0)
    }

    fun getInt(key: String, defValue: Int): Int {
        return sp.getInt(key, defValue)
    }

    fun getString(key: String): String {
        return sp.getString(key, "")
    }

    fun getLong(key: String): Long {
        return sp.getLong(key, 0)
    }
}