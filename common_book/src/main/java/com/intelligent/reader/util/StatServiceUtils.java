package com.intelligent.reader.util;

import android.content.Context;

import com.baidu.mobstat.StatService;

/**
 * 统计工具类
 */
public class StatServiceUtils {

    public static final String TAG = StatServiceUtils.class.getSimpleName();
    public static int number = 100;
    public static final int type_shelf_community = number++;
    public static final int type_setting_community = number++;

    public static int index = 0;
    public static final int type_ad_shelf = index++;
    public static final int type_ad_chapter_end_small = index++;
    public static final int type_ad_chapter_end_medium = index++;
    public static final int type_ad_chapter_space= index++;
    public static final int type_ad_reset_30 = index++;
    public static final int type_ad_book_end = index++;

    /**
     * 进入社区接口打点事件
     */
    public static void StatCommunityClick(Context context, int type) {

        if (type == type_shelf_community) {
            StatService.onEvent(context, "f_more_forumclick", "更多页面讨论区点击");
        } else if (type == type_setting_community) {
            StatService.onEvent(context, "f_me_forumclick", "我的页面讨论区点击");
        }
    }

    /**
     * 广告展现
     * @param mContxt
     * @param type_place
     */
    public static void StatBookEventShow(Context mContxt,int type_place) {
        if (type_place == type_ad_shelf) {
            StatService.onEvent(mContxt, "v_bookshelf_ad_show", "书架页广告展示");
        }else if (type_place == type_ad_chapter_end_small) {
            StatService.onEvent(mContxt, "v_reading_ad_small_show", "章节末尾小图展示");
        }else if (type_place == type_ad_chapter_end_medium) {
            StatService.onEvent(mContxt, "v_reading_ad_medium_show", "章节末尾大图展示");
        }else if (type_place == type_ad_chapter_space) {
            StatService.onEvent(mContxt, "v_reading_space_ad_show", "章间广告展示");
        }else if (type_place == type_ad_reset_30) {
            StatService.onEvent(mContxt, "v_reading_rest_ad_show", "休息30展现");
        }else if (type_place == type_ad_book_end) {
            StatService.onEvent(mContxt, "v_reading_end_ad_show", "完结页展现");
        }
    }

    /**
     * 广告点击
     * @param mContxt
     * @param type_place
     */
    public static void StatBookEventClick(Context mContxt,int type_place) {
        if (type_place == type_ad_shelf) {
            StatService.onEvent(mContxt, "v_bookshelf_ad_click", "书架页广告点击");
        }else if (type_place == type_ad_chapter_end_small) {
            StatService.onEvent(mContxt, "v_reading_ad_small_click", "章节末尾小图点击");
        }else if (type_place == type_ad_chapter_end_medium) {
            StatService.onEvent(mContxt, "v_reading_ad_medium_click", "章节末尾大图点击");
        }else if (type_place == type_ad_chapter_space) {
            StatService.onEvent(mContxt, "v_reading_space_ad_click", "章间广告点击");
        }else if (type_place == type_ad_reset_30) {
            StatService.onEvent(mContxt, "v_reading_rest_ad_click", "休息30点击");
        }else if (type_place == type_ad_book_end) {
            StatService.onEvent(mContxt, "v_reading_end_ad_click", "完结页点击");
        }
    }
}
