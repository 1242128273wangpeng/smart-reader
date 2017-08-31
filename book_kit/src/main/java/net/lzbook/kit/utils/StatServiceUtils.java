package net.lzbook.kit.utils;

import com.baidu.mobstat.StatService;

import android.content.Context;

/**
 * 统计工具
 * Created by q on 2015/12/18.
 */
public class StatServiceUtils {
    public static final String TAG = "StatServiceUtils";
    public static int index = 0;
    public static final int type_ad_shelf = index++;
    public static final int type_ad_ranking = index++;
    public static final int type_ad_classify = index++;
    public static final int type_ad_search = index++;
    public static final int type_ad_bookcover = index++;
    public static final int type_ad_chengesource = index++;
    public static final int type_ad_reset_30 = index++;
    public static final int type_ad_download = index++;
    public static final int type_ad_category = index++;
    public static final int type_ad_rank_bannershow = index++;
    public static final int type_ad_changesource_fail = index++;
    public static final int type_ad_reading_lastpage = index++;
    public static final int type_ad_reading_banner = index++;
    public static final int type_ad_reading_middle_lastpage = index++;
    public static final int type_ad_book_end = index++;

    //数据迁移打点
    public static final int user_transfer_first_1 = index++;
    public static final int user_transfer_first_2 = index++;
    public static final int user_transfer_first_3 = index++;
    public static final int user_transfer_first_4 = index++;
    public static final int user_transfer_first_5 = index++;
    public static final int user_transfer_first_6 = index++;
    public static final int user_transfer_second_1 = index++;
    public static final int user_transfer_second_2 = index++;

    //应用中按钮点击打点
    public static final int bs_click_recommend_menu = index++;
    public static final int bs_click_rank_menu = index++;
    public static final int bs_click_category_menu = index++;
    public static final int bs_click_mine_menu = index++;
    public static final int bs_click_download_btn = index++;
    public static final int bs_click_discuss_btn = index++;
    public static final int bs_click_search_btn = index++;
    public static final int bs_click_one_book = index++;
    public static final int bs_click_delete_ok_btn = index++;
    public static final int bs_click_delete_cancel_btn = index++;
    public static final int bs_click_erro_source_change = index++;
    public static final int bs_click_erro_source_remove = index++;
    public static final int bs_down_m_click_edit = index++;
    public static final int bs_down_m_long_click = index++;
    public static final int bs_down_m_click_delete = index++;
    public static final int bs_down_m_click_select_all = index++;
    public static final int bs_down_m_click_cancel = index++;
    public static final int bs_down_m_click_down_all = index++;
    public static final int bs_down_m_click_down_from_now = index++;
    public static final int rb_click_add_book_mark_btn = index++;
    public static final int rb_click_read_head_bookinfo = index++;
    public static final int rb_click_auto_read_btn = index++;
    public static final int rb_click_fullscreen_read_btn = index++;
    public static final int rb_click_auto_read_cancel = index++;
    public static final int rb_click_auto_read_speed_up = index++;
    public static final int rb_click_auto_read_speed_down = index++;
    public static final int rb_click_catalog_btn = index++;
    public static final int rb_click_change_source_btn = index++;
    public static final int rb_click_download_btn = index++;
    public static final int rb_click_read_head_more = index++;
    public static final int rb_click_download_all = index++;
    public static final int rb_click_download_from_now = index++;
    public static final int rb_click_download_cancel = index++;
    public static final int rb_catalog_click_book_mark = index++;
    public static final int rb_catalog_click_zx_btn = index++;
    public static final int rb_catalog_click_dx_btn = index++;
    public static final int rb_click_ld_progress = index++;
    public static final int rb_click_ld_with_system = index++;
    public static final int rb_click_previous_chapter = index++;
    public static final int rb_click_next_chapter = index++;
    public static final int rb_click_back_btn = index++;
    public static final int rb_click_setting_btn = index++;
    public static final int rb_click_background_01 = index++;
    public static final int rb_click_background_02 = index++;
    public static final int rb_click_background_03 = index++;
    public static final int rb_click_background_04 = index++;
    public static final int rb_click_background_05 = index++;
    public static final int rb_click_background_06 = index++;
    public static final int rb_click_background_07 = index++;
    public static final int rb_click_background_08 = index++;
    public static final int rb_click_background_09 = index++;
    public static final int rb_click_background_10 = index++;
    public static final int rb_click_hangju_01 = index++;
    public static final int rb_click_hangju_02 = index++;
    public static final int rb_click_hangju_03 = index++;
    public static final int rb_click_hangju_04 = index++;
    public static final int rb_click_flip_page_01 = index++;
    public static final int rb_click_flip_page_02 = index++;
    public static final int rb_click_flip_page_03 = index++;
    public static final int rb_click_flip_page_04 = index++;
    public static final int rb_click_font_size_bigger = index++;
    public static final int rb_click_font_size_smaller = index++;
    public static final int rb_click_landscape_btn = index++;
    public static final int rb_click_portrait_btn = index++;
    public static final int rb_click_change_source_ok = index++;
    public static final int rb_click_change_source_read = index++;
    public static final int rb_click_flip_auto_ok = index++;
    public static final int rb_click_flip_auto_cancel = index++;
    public static final int rb_click_flip_auto_not_tip = index++;
    public static final int rb_click_feedback_btn = index++;
    public static final int rb_click_feedback_submit = index++;
    public static final int rb_click_night_mode = index++;
    public static final int b_details_click_book_add = index++;
    public static final int b_details_click_book_remove = index++;
    public static final int b_details_click_trans_read = index++;
    public static final int b_details_click_ch_source = index++;
    public static final int b_details_click_all_load = index++;
    public static final int b_details_click_to_catalogue = index++;
    public static final int b_search_click_allhotword = index++;
    public static final int b_search_click_ch_hotword = index++;
    public static final int b_search_click_his_word = index++;
    public static final int b_search_click_his_clear = index++;
    public static final int me_set_click_discussion = index++;
    public static final int me_set_click_login_discussion = index++;
    public static final int me_set_click_read = index++;
    public static final int me_set_click_read_bg_01 = index++;
    public static final int me_set_click_read_bg_02 = index++;
    public static final int me_set_click_read_bg_03 = index++;
    public static final int me_set_click_read_bg_04 = index++;
    public static final int me_set_click_read_bg_05 = index++;
    public static final int me_set_click_read_bg_06 = index++;
    public static final int me_set_click_read_bg_07 = index++;
    public static final int me_set_click_read_bg_08 = index++;
    public static final int me_set_click_read_bg_09 = index++;
    public static final int me_set_click_read_bg_10 = index++;
    public static final int me_set_click_read_row_1 = index++;
    public static final int me_set_click_read_row_2 = index++;
    public static final int me_set_click_read_row_3 = index++;
    public static final int me_set_click_read_row_4 = index++;
    public static final int me_set_click_read_slide = index++;
    public static final int me_set_click_read_simul = index++;
    public static final int me_set_click_read_trans = index++;
    public static final int me_set_click_read_size_add = index++;
    public static final int me_set_click_read_size_dec = index++;
    public static final int me_set_click_read_scr_land = index++;
    public static final int me_set_click_read_scr_ver = index++;
    public static final int me_set_click_read_Bri_sys = index++;
    public static final int me_set_click_read_volu_tur = index++;
    public static final int me_set_click_more = index++;
    public static final int me_set_click_more_push = index++;
    public static final int me_set_cli_more_push_voi = index++;
    public static final int me_set_cli_more_push_time = index++;
    public static final int me_set_cli_shelf_rak_time = index++;
    public static final int me_set_cli_shelf_rank_up = index++;
    public static final int me_set_click_help = index++;
    public static final int me_set_click_grade = index++;
    public static final int me_set_click_ver = index++;
    public static final int me_set_cli_clear_cache = index++;
    public static final int me_set_cli_night_shift = index++;
    public static final int me_set_cli_day_shift = index++;
    public static final int me_set_cli_theme_change = index++;
    public static final int me_set_cli_theme1 = index++;
    public static final int me_set_cli_theme2 = index++;
    public static final int me_set_cli_theme3 = index++;
    public static final int me_set_cli_theme4 = index++;

    public static final int ad_count_remove = index++;
    public static final int ad_count_reselection = index++;
    public static final int ad_count_request = index++;
    public static final int ad_adwin = index++;
    public static final int ad_adwin_no = index++;
    public static final int ad_adwin_resuc = index++;

    public static final int download_stop = index++;
    public static final int download_parse_error = index++;
    public static final int download_parse_success = index++;
    public static final int download_no_address = index++;
    public static final int download_reset = index++;
    public static final int download_read = index++;
    public static final int read_limit = index++;
    public static final int read_limit_continue = index++;
    public static final int read_limit_bookshelf = index++;
    public static final int user_login_succeed = index++;
    public static final int app_start = index++;
    public static final int cover_into = index++;
    public static final int cover_into_his = index++;
    public static final int his_into = index++;

    /**
     * 广告展现
     */
    public static void statBookEventShow(Context mContxt, int type_place) {
        if (type_place == type_ad_shelf) {
            StatService.onEvent(mContxt, "v_bokshelf_bannershow", "书架页展现");
        } else if (type_place == type_ad_ranking) {
            StatService.onEvent(mContxt, "v_bokstore_rank_appshow", "榜单展现");
        } else if (type_place == type_ad_classify) {
            StatService.onEvent(mContxt, "v_bokstore_category_appshow", "分类展现");
        } else if (type_place == type_ad_search) {
            StatService.onEvent(mContxt, "v_search_result_bannershow", "搜索展现");
        } else if (type_place == type_ad_bookcover) {
            StatService.onEvent(mContxt, "v_bokdetails_appshow", "封面页展现");
        } else if (type_place == type_ad_chengesource) {
            StatService.onEvent(mContxt, "v_BR_changesourse_bannershow", "换源页展现");
        } else if (type_place == type_ad_reset_30) {
            StatService.onEvent(mContxt, "v_BR_restremind_bannershow", "休息30展现");
        } else if (type_place == type_ad_download) {
            StatService.onEvent(mContxt, "v_bokdownload_bannershow", "下载展现");
        } else if (type_place == type_ad_category) {
            StatService.onEvent(mContxt, "v_bok_contents_bannershow", "目录页展现");
        } else if (type_place == type_ad_rank_bannershow) {
            StatService.onEvent(mContxt, "v_bokstore_rank_bannershow", "榜单排行展现");
        } else if (type_place == type_ad_changesource_fail) {
            StatService.onEvent(mContxt, "v_BR_nochoice_bannershow", "换源无来源展现");
        } else if (type_place == type_ad_reading_banner) {
            StatService.onEvent(mContxt, "v_BR_lastpage_bannershow", "阅读页banner展现");
        } else if (type_place == type_ad_reading_lastpage) {
            StatService.onEvent(mContxt, "v_BR_BETchapter_bannershow", "阅读页插屏展现");
        } else if (type_place == type_ad_reading_middle_lastpage) {
            StatService.onEvent(mContxt, "v_BR_lastpage_picshow", "阅读页段尾大图展现");
        } else if (type_place == type_ad_book_end) {
            StatService.onEvent(mContxt, "v_bookend_picshow", "完结页展现");
        }
    }

    /**
     * 广告点击
     */
    public static void statBookEventClick(Context mContxt, int type_place) {
        if (type_place == type_ad_shelf) {
            StatService.onEvent(mContxt, "v_bokshelf_bannerclick", "书架页点击");
        } else if (type_place == type_ad_ranking) {
            StatService.onEvent(mContxt, "v_bokstore_rank_appclick", "榜单点击");
        } else if (type_place == type_ad_classify) {
            StatService.onEvent(mContxt, "v_bokstore_category_appclick", "分类点击");
        } else if (type_place == type_ad_search) {
            StatService.onEvent(mContxt, "v_search_result_bannerclick", "搜索点击");
        } else if (type_place == type_ad_bookcover) {
            StatService.onEvent(mContxt, "v_bokdetails_appclick", "封面页点击");
        } else if (type_place == type_ad_chengesource) {
            StatService.onEvent(mContxt, "v_BR_changesourse_bannerclick", "换源页点击");
        } else if (type_place == type_ad_reset_30) {
            StatService.onEvent(mContxt, "v_BR_restremind_bannerclick", "休息30点击");
        } else if (type_place == type_ad_download) {
            StatService.onEvent(mContxt, "v_bokdownload_bannerclick", "下载点击");
        } else if (type_place == type_ad_category) {
            StatService.onEvent(mContxt, "v_bok_contents_bannerclick", "目录页点击");
        } else if (type_place == type_ad_rank_bannershow) {
            StatService.onEvent(mContxt, "v_bokstore_rank_bannerclick", "榜单排行点击");
        } else if (type_place == type_ad_changesource_fail) {
            StatService.onEvent(mContxt, "v_BR_nochoice_bannerclick", "换源无来源点击");
        } else if (type_place == type_ad_reading_banner) {
            StatService.onEvent(mContxt, "v_BR_lastpage_bannerclick", "阅读页banner点击");
        } else if (type_place == type_ad_reading_lastpage) {
            StatService.onEvent(mContxt, "v_BR_BETchapter_bannerclick", "阅读页插屏点击");
        } else if (type_place == type_ad_reading_middle_lastpage) {
            StatService.onEvent(mContxt, "v_BR_lastpage_picclick", "阅读页段尾大图点击");
        } else if (type_place == type_ad_book_end) {
            StatService.onEvent(mContxt, "v_bookend_picclick", "完结页点击");
        }
    }

    public static void statUserTransfer(Context mContext, int type_place) {
        if (type_place == user_transfer_first_1) {
            StatService.onEvent(mContext, "user_transfer_first_1", "第一阶段源，列表为空", 1);
        } else if (type_place == user_transfer_first_2) {
            StatService.onEvent(mContext, "user_transfer_first_2", "宜搜书籍，源列表不为空，追书网未覆盖", 1);
        } else if (type_place == user_transfer_first_3) {
            StatService.onEvent(mContext, "user_transfer_first_3", "书籍目录不存在，未读未缓存，换书籍信息", 1);
        } else if (type_place == user_transfer_first_4) {
            StatService.onEvent(mContext, "user_transfer_first_4", "目录存在，阅读标记对不上，清缓存", 1);
        } else if (type_place == user_transfer_first_5) {
            StatService.onEvent(mContext, "user_transfer_first_5", "目录存在，标记对上，目录不全等，清缓存", 1);
        } else if (type_place == user_transfer_first_6) {
            StatService.onEvent(mContext, "user_transfer_first_6", "目录存在，并且完全一致，不清缓存", 1);
        } else if (type_place == user_transfer_second_1) {
            StatService.onEvent(mContext, "user_transfer_second_1", "第二阶段，宜搜书籍，源列表为空", 1);
        } else if (type_place == user_transfer_second_2) {
            StatService.onEvent(mContext, "user_transfer_second_2", "宜搜迁移到小站", 1);
        }
    }

    /**
     * 应用中点击按钮时，进行打点统计
     */
    public static void statAppBtnClick(Context mContext, int type_place) {
        if (type_place == bs_click_recommend_menu) {
            StatService.onEvent(mContext, "bs_click_recommend_menu", "书架页推荐点击", 1);
        } else if (type_place == bs_click_rank_menu) {
            StatService.onEvent(mContext, "bs_click_rank_menu", "书架页排行点击", 1);
        } else if (type_place == bs_click_category_menu) {
            StatService.onEvent(mContext, "bs_click_category_menu", "书架页分类点击", 1);
        } else if (type_place == bs_click_mine_menu) {
            StatService.onEvent(mContext, "bs_click_mine_menu", "书架页我的点击", 1);
        } else if (type_place == bs_click_download_btn) {
            StatService.onEvent(mContext, "bs_click_download_btn", "书架页缓存管理点击", 1);
        } else if (type_place == bs_click_discuss_btn) {
            StatService.onEvent(mContext, "bs_click_discuss_btn", "书架页讨论区点击", 1);
        } else if (type_place == bs_click_search_btn) {
            StatService.onEvent(mContext, "bs_click_search_btn", "书架页搜索点击", 1);
        } else if (type_place == bs_click_one_book) {
            StatService.onEvent(mContext, "bs_click_one_book", "点击书籍进入阅读页", 1);
        } else if (type_place == bs_click_delete_ok_btn) {
            StatService.onEvent(mContext, "bs_click_delete_ok_btn", "书架页删除点击", 1);
        } else if (type_place == bs_click_delete_cancel_btn) {
            StatService.onEvent(mContext, "bs_click_delete_cancel_btn", "书架页删除取消点击", 1);
        } else if (type_place == bs_click_erro_source_change) {
            StatService.onEvent(mContext, "bs_click_erro_source_change", "来源不可用时更换来源点击", 1);
        } else if (type_place == bs_click_erro_source_remove) {
            StatService.onEvent(mContext, "bs_click_erro_source_remove", "来源不可用时移除书架点击", 1);
        } else if (type_place == bs_down_m_click_edit) {
            StatService.onEvent(mContext, "bs_down_m_click_edit", "缓存管理编辑点击", 1);
        } else if (type_place == bs_down_m_long_click) {
            StatService.onEvent(mContext, "bs_down_m_long_click", "缓存管理长按进行编辑", 1);
        } else if (type_place == bs_down_m_click_delete) {
            StatService.onEvent(mContext, "bs_down_m_click_delete", "缓存管理删除点击", 1);
        } else if (type_place == bs_down_m_click_select_all) {
            StatService.onEvent(mContext, "bs_down_m_click_select_all", "缓存管理全选点击", 1);
        } else if (type_place == bs_down_m_click_cancel) {
            StatService.onEvent(mContext, "bs_down_m_click_cancel", "缓存管理取消点击", 1);
        } else if (type_place == bs_down_m_click_down_all) {
            StatService.onEvent(mContext, "bs_down_m_click_down_all", "缓存管理全部缓存点击", 1);
        } else if (type_place == bs_down_m_click_down_from_now) {
            StatService.onEvent(mContext, "bs_down_m_click_down_from_now", "缓存管理从当前章节缓存点击", 1);
        } else if (type_place == rb_click_add_book_mark_btn) {
            StatService.onEvent(mContext, "rb_click_add_book_mark_btn", "阅读页添加标签点击", 1);
        } else if(type_place == rb_click_read_head_bookinfo){
            StatService.onEvent(mContext, "rb_click_read_head_bookinfo", "阅读页书籍信息点击", 1);
        }else if (type_place == rb_click_auto_read_btn) {
            StatService.onEvent(mContext, "rb_click_auto_read_btn", "阅读页自动阅读点击", 1);
        } else if (type_place == rb_click_fullscreen_read_btn) {
            StatService.onEvent(mContext, "rb_click_fullscreen_read_btn", "阅读页全屏阅读点击", 1);
        } else if (type_place == rb_click_auto_read_cancel) {
            StatService.onEvent(mContext, "rb_click_auto_read_cancel", "阅读页自动翻页取消点击", 1);
        } else if (type_place == rb_click_auto_read_speed_up) {
            StatService.onEvent(mContext, "rb_click_auto_read_speed_up", "阅读页自动翻页加速点击", 1);
        } else if (type_place == rb_click_auto_read_speed_down) {
            StatService.onEvent(mContext, "rb_click_auto_read_speed_down", "阅读页自动翻页减速点击", 1);
        } else if (type_place == rb_click_catalog_btn) {
            StatService.onEvent(mContext, "rb_click_catalog_btn", "阅读页目录点击", 1);
        } else if (type_place == rb_click_change_source_btn) {
            StatService.onEvent(mContext, "rb_click_change_source_btn", "阅读页换源点击", 1);
        } else if (type_place == rb_click_download_btn) {
            StatService.onEvent(mContext, "rb_click_download_btn", "阅读页缓存点击", 1);
        } else if (type_place == rb_click_read_head_more){
            StatService.onEvent(mContext, "rb_click_read_head_more", "阅读更多设置点击", 1);
        }else if (type_place == rb_click_download_all) {
            StatService.onEvent(mContext, "rb_click_download_all", "阅读页全部缓存点击", 1);
        } else if (type_place == rb_click_download_from_now) {
            StatService.onEvent(mContext, "rb_click_download_from_now", "阅读页从当前章节缓存点击", 1);
        } else if (type_place == rb_click_download_cancel) {
            StatService.onEvent(mContext, "rb_click_download_cancel", "阅读页缓存取消点击", 1);
        } else if (type_place == rb_catalog_click_book_mark) {
            StatService.onEvent(mContext, "rb_catalog_click_book_mark", "目录页书签点击", 1);
        } else if (type_place == rb_catalog_click_zx_btn) {
            StatService.onEvent(mContext, "rb_catalog_click_zx_btn", "目录页正序点击", 1);
        } else if (type_place == rb_catalog_click_dx_btn) {
            StatService.onEvent(mContext, "rb_catalog_click_dx_btn", "目录页倒序点击", 1);
        } else if (type_place == rb_click_ld_progress) {
            StatService.onEvent(mContext, "rb_click_ld_progress", "阅读页亮度点击", 1);
        } else if (type_place == rb_click_ld_with_system) {
            StatService.onEvent(mContext, "rb_click_ld_with_system", "阅读页亮度跟随系统点击", 1);
        } else if (type_place == rb_click_previous_chapter) {
            StatService.onEvent(mContext, "rb_click_previous_chapter", "阅读页上一章点击", 1);
        } else if (type_place == rb_click_next_chapter) {
            StatService.onEvent(mContext, "rb_click_next_chapter", "阅读页下一章点击", 1);
        } else if (type_place == rb_click_back_btn) {
            StatService.onEvent(mContext, "rb_click_back_btn", "阅读页返回点击", 1);
        } else if (type_place == rb_click_setting_btn) {
            StatService.onEvent(mContext, "rb_click_setting_btn", "阅读页设置点击", 1);
        } else if (type_place == rb_click_background_01) {
            StatService.onEvent(mContext, "rb_click_background_01", "阅读页背景01点击", 1);
        } else if (type_place == rb_click_background_02) {
            StatService.onEvent(mContext, "rb_click_background_02", "阅读页背景02点击", 1);
        } else if (type_place == rb_click_background_03) {
            StatService.onEvent(mContext, "rb_click_background_03", "阅读页背景03点击", 1);
        } else if (type_place == rb_click_background_04) {
            StatService.onEvent(mContext, "rb_click_background_04", "阅读页背景04点击", 1);
        } else if (type_place == rb_click_background_05) {
            StatService.onEvent(mContext, "rb_click_background_05", "阅读页背景05点击", 1);
        } else if (type_place == rb_click_background_06) {
            StatService.onEvent(mContext, "rb_click_background_06", "阅读页背景06点击", 1);
        } else if (type_place == rb_click_background_07) {
            StatService.onEvent(mContext, "rb_click_background_07", "阅读页背景07点击", 1);
        } else if (type_place == rb_click_background_08) {
            StatService.onEvent(mContext, "rb_click_background_08", "阅读页背景08点击", 1);
        } else if (type_place == rb_click_background_09) {
            StatService.onEvent(mContext, "rb_click_background_09", "阅读页背景09点击", 1);
        } else if (type_place == rb_click_background_10) {
            StatService.onEvent(mContext, "rb_click_background_10", "阅读页背景10点击", 1);
        } else if (type_place == rb_click_hangju_01) {
            StatService.onEvent(mContext, "rb_click_hangju_01", "阅读页行距01点击", 1);
        } else if (type_place == rb_click_hangju_02) {
            StatService.onEvent(mContext, "rb_click_hangju_02", "阅读页行距02点击", 1);
        } else if (type_place == rb_click_hangju_03) {
            StatService.onEvent(mContext, "rb_click_hangju_03", "阅读页行距03点击", 1);
        } else if (type_place == rb_click_hangju_04) {
            StatService.onEvent(mContext, "rb_click_hangju_04", "阅读页行距04点击", 1);
        } else if (type_place == rb_click_flip_page_01) {
            StatService.onEvent(mContext, "rb_click_flip_page_01", "阅读页翻页模式01点击", 1);
        } else if (type_place == rb_click_flip_page_02) {
            StatService.onEvent(mContext, "rb_click_flip_page_02", "阅读页翻页模式02点击", 1);
        } else if (type_place == rb_click_flip_page_03) {
            StatService.onEvent(mContext, "rb_click_flip_page_03", "阅读页翻页模式03点击", 1);
        }else if (type_place == rb_click_flip_page_04) {
            StatService.onEvent(mContext, "rb_click_flip_page_04", "阅读页翻页模式04点击", 1);
        } else if (type_place == rb_click_font_size_bigger) {
            StatService.onEvent(mContext, "rb_click_font_size_bigger", "阅读页加大字号点击", 1);
        } else if (type_place == rb_click_font_size_smaller) {
            StatService.onEvent(mContext, "rb_click_font_size_smaller", "阅读页减小字号点击", 1);
        } else if (type_place == rb_click_landscape_btn) {
            StatService.onEvent(mContext, "rb_click_landscape_btn", "阅读页横屏展示点击", 1);
        } else if (type_place == rb_click_portrait_btn) {
            StatService.onEvent(mContext, "rb_click_portrait_btn", "阅读页竖屏展示点击", 1);
        } else if (type_place == rb_click_change_source_ok) {
            StatService.onEvent(mContext, "rb_click_change_source_ok", "阅读页换源提示确认换源点击", 1);
        } else if (type_place == rb_click_change_source_read) {
            StatService.onEvent(mContext, "rb_click_change_source_read", "阅读页换源提示阅读已缓存内容点击", 1);
        } else if (type_place == rb_click_flip_auto_ok) {
            StatService.onEvent(mContext, "rb_click_flip_auto_ok", "阅读页翻页提示确定点击", 1);
        } else if (type_place == rb_click_flip_auto_cancel) {
            StatService.onEvent(mContext, "rb_click_flip_auto_cancel", "阅读页翻页提示取消点击", 1);
        } else if (type_place == rb_click_flip_auto_not_tip) {
            StatService.onEvent(mContext, "rb_click_flip_auto_not_tip", "阅读页翻页提示再在接收此提示点击", 1);
        } else if (type_place == rb_click_feedback_btn) {
            StatService.onEvent(mContext, "rb_click_feedback_btn", "阅读页反馈/报错点击", 1);
        } else if (type_place == rb_click_feedback_submit) {
            StatService.onEvent(mContext, "rb_click_feedback_submit", "阅读页反馈/报错提交点击", 1);
        } else if (type_place == rb_click_night_mode) {
            StatService.onEvent(mContext, "rb_click_night_mode", "阅读页夜间模式点击", 1);
        } else if (type_place == b_details_click_book_add) {
            StatService.onEvent(mContext, "b_details_click_book_add", "书籍详情页加入书架点击", 1);
        } else if (type_place == b_details_click_book_remove) {
            StatService.onEvent(mContext, "b_details_click_book_remove", "书籍详情页移除书架点击", 1);
        } else if (type_place == b_details_click_trans_read) {
            StatService.onEvent(mContext, "b_details_click_trans_read", "书籍详情页转码阅读点击", 1);
        } else if (type_place == b_details_click_ch_source) {
            StatService.onEvent(mContext, "b_details_click_ch_source", "书籍详情页换源点击", 1);
        } else if (type_place == b_details_click_all_load) {
            StatService.onEvent(mContext, "b_details_click_all_load", "书籍详情页全本缓存点击", 1);
        } else if (type_place == b_details_click_to_catalogue) {
            StatService.onEvent(mContext, "b_details_click_to_catalogue", "书籍详情页查看目录点击", 1);
        } else if (type_place == b_search_click_allhotword) {
            StatService.onEvent(mContext, "b_search_click_allhotword", "搜索页热搜词点击统计：统计所有热搜词的点击量", 1);
        } else if (type_place == b_search_click_ch_hotword) {
            StatService.onEvent(mContext, "b_search_click_ch_hotword", "搜索页换一换点击", 1);
        } else if (type_place == b_search_click_his_word) {
            StatService.onEvent(mContext, "b_search_click_his_word", "搜索页搜索历史点击", 1);
        } else if (type_place == b_search_click_his_clear) {
            StatService.onEvent(mContext, "b_search_click_his_clear", "搜索页搜索历史清空点击", 1);
        } else if (type_place == me_set_click_discussion) {
            StatService.onEvent(mContext, "me_set_click_discussion", "我的模块讨论区点击", 1);
        } else if (type_place == me_set_click_login_discussion) {
            StatService.onEvent(mContext, "me_set_click_login_discussion", "我的模块登录讨论区点击", 1);
        } else if (type_place == me_set_click_read) {
            StatService.onEvent(mContext, "me_set_click_read", "我的模块阅读页设置点击", 1);
        } else if (type_place == me_set_click_read_bg_01) {
            StatService.onEvent(mContext, "me_set_click_read_bg_01", "我的模块点击阅读页背景01", 1);
        } else if (type_place == me_set_click_read_bg_02) {
            StatService.onEvent(mContext, "me_set_click_read_bg_02", "我的模块点击阅读页背景02", 1);
        } else if (type_place == me_set_click_read_bg_03) {
            StatService.onEvent(mContext, "me_set_click_read_bg_03", "我的模块点击阅读页背景03", 1);
        } else if (type_place == me_set_click_read_bg_04) {
            StatService.onEvent(mContext, "me_set_click_read_bg_04", "我的模块点击阅读页背景04", 1);
        } else if (type_place == me_set_click_read_bg_05) {
            StatService.onEvent(mContext, "me_set_click_read_bg_05", "我的模块点击阅读页背景05", 1);
        } else if (type_place == me_set_click_read_bg_06) {
            StatService.onEvent(mContext, "me_set_click_read_bg_06", "我的模块点击阅读页背景06", 1);
        } else if (type_place == me_set_click_read_bg_07) {
            StatService.onEvent(mContext, "me_set_click_read_bg_07", "我的模块点击阅读页背景07", 1);
        } else if (type_place == me_set_click_read_bg_08) {
            StatService.onEvent(mContext, "me_set_click_read_bg_08", "我的模块点击阅读页背景08", 1);
        } else if (type_place == me_set_click_read_bg_09) {
            StatService.onEvent(mContext, "me_set_click_read_bg_09", "我的模块点击阅读页背景09", 1);
        } else if (type_place == me_set_click_read_bg_10) {
            StatService.onEvent(mContext, "me_set_click_read_bg_10", "我的模块点击阅读页背景10", 1);
        } else if (type_place == me_set_click_read_row_1) {
            StatService.onEvent(mContext, "me_set_click_read_row_1", "我的模块点击阅读页行距01", 1);
        } else if (type_place == me_set_click_read_row_2) {
            StatService.onEvent(mContext, "me_set_click_read_row_2", "我的模块点击阅读页行距02", 1);
        } else if (type_place == me_set_click_read_row_3) {
            StatService.onEvent(mContext, "me_set_click_read_row_3", "我的模块点击阅读页行距03", 1);
        } else if (type_place == me_set_click_read_row_4) {
            StatService.onEvent(mContext, "me_set_click_read_row_4", "我的模块点击阅读页行距04", 1);
        } else if (type_place == me_set_click_read_slide) {
            StatService.onEvent(mContext, "me_set_click_read_slide", "我的模块点击阅读页滑动", 1);
        } else if (type_place == me_set_click_read_simul) {
            StatService.onEvent(mContext, "me_set_click_read_simul", "我的模块点击阅读页仿真", 1);
        } else if (type_place == me_set_click_read_trans) {
            StatService.onEvent(mContext, "me_set_click_read_trans", "我的模块点击阅读页平移", 1);
        } else if (type_place == me_set_click_read_size_add) {
            StatService.onEvent(mContext, "me_set_click_read_size_add", "我的模块点击阅读页Aa+", 1);
        } else if (type_place == me_set_click_read_size_dec) {
            StatService.onEvent(mContext, "me_set_click_read_size_dec", "我的模块点击阅读页Aa-", 1);
        } else if (type_place == me_set_click_read_scr_land) {
            StatService.onEvent(mContext, "me_set_click_read_scr_land", "我的模块阅读页设置横屏展示点击", 1);
        } else if (type_place == me_set_click_read_scr_ver) {
            StatService.onEvent(mContext, "me_set_click_read_scr_ver", "我的模块阅读页设置竖屏展示点击", 1);
        } else if (type_place == me_set_click_read_Bri_sys) {
            StatService.onEvent(mContext, "me_set_click_read_Bri_sys", "我的模块阅读页设置亮度跟随系统点击", 1);
        } else if (type_place == me_set_click_read_volu_tur) {
            StatService.onEvent(mContext, "me_set_click_read_volu_tur", "我的模块阅读页设置音量键翻页点击", 1);
        } else if (type_place == me_set_click_more) {
            StatService.onEvent(mContext, "me_set_click_more", "我的模块更多设置点击", 1);
        } else if (type_place == me_set_click_more_push) {
            StatService.onEvent(mContext, "me_set_click_more_push", "我的模块更多设置推送点击", 1);
        } else if (type_place == me_set_cli_more_push_voi) {
            StatService.onEvent(mContext, "me_set_cli_more_push_voi", "我的模块更多设置推送声音点击", 1);
        } else if (type_place == me_set_cli_more_push_time) {
            StatService.onEvent(mContext, "me_set_cli_more_push_time", "我的模块更多设置分时间段推送点击", 1);
        } else if (type_place == me_set_cli_shelf_rak_time) {
            StatService.onEvent(mContext, "me_set_cli_shelf_rak_time", "我的模块书架按阅读时间排序", 1);
        } else if (type_place == me_set_cli_shelf_rank_up) {
            StatService.onEvent(mContext, "me_set_cli_shelf_rank_up", "我的模块书架按更新时间排序", 1);
        } else if (type_place == me_set_click_help) {
            StatService.onEvent(mContext, "me_set_click_help", "我的模块帮助与反馈点击", 1);
        } else if (type_place == me_set_click_grade) {
            StatService.onEvent(mContext, "me_set_click_grade", "我的模块去评分点击", 1);
        } else if (type_place == me_set_click_ver) {
            StatService.onEvent(mContext, "me_set_click_ver", "我的模块当前版本点击", 1);
        } else if (type_place == me_set_cli_clear_cache) {
            StatService.onEvent(mContext, "me_set_cli_clear_cache", "我的模块清除缓存点击", 1);
        } else if (type_place == me_set_cli_night_shift) {
            StatService.onEvent(mContext, "me_set_cli_night_shift", "我的模块夜间模式点击", 1);
        } else if (type_place == me_set_cli_day_shift) {
            StatService.onEvent(mContext, "me_set_cli_day_shift", "我的模块日间模式点击", 1);
        } else if (type_place == me_set_cli_theme_change) {
            StatService.onEvent(mContext, "me_set_cli_theme_change", "我的模块进入主题切换界面的点击", 1);
        } else if (type_place == me_set_cli_theme1) {
            StatService.onEvent(mContext, "me_set_cli_theme1", "主题切换到主题1", 1);
        } else if (type_place == me_set_cli_theme2) {
            StatService.onEvent(mContext, "me_set_cli_theme2", "主题切换到主题2", 1);
        } else if (type_place == me_set_cli_theme3) {
            StatService.onEvent(mContext, "me_set_cli_theme3", "主题切换到主题3", 1);
        } else if (type_place == me_set_cli_theme4) {
            StatService.onEvent(mContext, "me_set_cli_theme4", "主题切换到主题4", 1);
        } else if (type_place == download_stop) {
            StatService.onEvent(mContext, "download_stop", "缓存下载中断", 1);
        } else if (type_place == download_parse_error) {
            StatService.onEvent(mContext, "download_parse_error", "缓存解析失败", 1);
        } else if (type_place == download_parse_success) {
            StatService.onEvent(mContext, "download_parse_success", "缓存解析成功", 1);
        } else if (type_place == download_no_address) {
            StatService.onEvent(mContext, "download_no_address", "缓存返回无地址", 1);
        } else if (type_place == download_reset) {
            StatService.onEvent(mContext, "download_reset", "缓存通知栏点击重试", 1);
        } else if (type_place == download_read) {
            StatService.onEvent(mContext, "download_read", "缓存通知栏点击看书", 1);
        } else if (type_place == read_limit) {
            StatService.onEvent(mContext, "read_limit", "时限弹窗跳出次数", 1);
        } else if (type_place == read_limit_continue) {
            StatService.onEvent(mContext, "read_limit_continue", "时限弹窗点击联网", 1);
        } else if (type_place == read_limit_bookshelf) {
            StatService.onEvent(mContext, "read_limit_bookshelf", "时限弹窗点击返回", 1);
        } else if (type_place == user_login_succeed) {
            StatService.onEvent(mContext, "user_login_succeed", "应用启动时用户已登录记录和登录成功记录", 1);
        } else if (type_place == app_start) {
            StatService.onEvent(mContext, "app_start", "应用启动记录", 1);
        } else if (type_place == cover_into) {
            StatService.onEvent(mContext, "cover_into", "进入封面页总次数", 1);
        } else if (type_place == cover_into_his) {
            StatService.onEvent(mContext, "cover_into_his", "足迹页进入封面页次数", 1);
        } else if (type_place == his_into) {
            StatService.onEvent(mContext, "his_into", "足迹页进入次数", 1);
        }
    }

    public static void statAdEventShow(Context mContext, int type_place) {
        if (type_place == ad_count_remove) {
            StatService.onEvent(mContext, "ADCount_remove", "移除点击广告");
        } else if (type_place == ad_count_reselection) {
            StatService.onEvent(mContext, "ADCount_reselection", "重新请求广告");
        } else if (type_place == ad_count_request) {
            StatService.onEvent(mContext, "ADcount_request", "阅读页广告请求");
        } else if (type_place == ad_adwin) {
            StatService.onEvent(mContext, "Ad_adwin", "直投平台被随机选中的事件");
        } else if (type_place == ad_adwin_no) {
            StatService.onEvent(mContext, "Ad_adwin_no", "直投平台被选中无物料返回的事件");
        } else if (type_place == ad_adwin_resuc) {
            StatService.onEvent(mContext, "Ad_adwin_resuc", "直投无返回，去容器中重选物料成功的事件");
        }
    }

}
