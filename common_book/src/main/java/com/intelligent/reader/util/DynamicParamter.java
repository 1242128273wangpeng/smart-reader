package com.intelligent.reader.util;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.mobstat.SendStrategyEnum;
import com.baidu.mobstat.StatService;
import com.umeng.onlineconfig.OnlineConfigAgent;
import com.umeng.onlineconfig.UmengOnlineConfigureListener;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.LoadDataManager;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class DynamicParamter {

    public static String TAG = DynamicParamter.class.getSimpleName();
    public static boolean isReloadDynamic = false;

    public String baidu_stat_id;
    public String native_ad_page_interstitial_count;
    public String native_ad_page_gap_in_chapter;
    public String native_ad_page_in_chapter_limit;
    public String dy_is_new_reading_end;
    public String push_key;
    public String ban_gids;
    //广告总开关
    public String new_app_ad_switch;
    //广告总开关
    public String dy_ad_switch;
    //新的用户广告请求开关
    public String dy_ad_new_request_switch;
    //新的统计开关
    public String dy_ad_new_statistics_switch;
    //阅读页翻页统计开关
    public String dy_readPage_statistics_switch;
    //老的广告统计开关
    public String dy_ad_old_request_switch;
    //X小时内新用户不显示广告设置
    public String dy_adfree_new_user;
    //开屏页开关
    public String dy_splash_ad_switch;
    //书架页开关
    public String dy_shelf_ad_switch;
    //书架页广告间隔频率设置
    public String dy_shelf_ad_freq;
    //章节末开关
    public String dy_page_end_ad_switch;
    //章节末广告间隔频率设置
    public String dy_page_end_ad_freq;
    //书末广告开关
    public String dy_book_end_ad_switch;
    //休息页广告
    public String dy_rest_ad_switch;
    //休息页广告休息时间设置
    public String dy_rest_ad_sec;
    //章节间开关
    public String dy_page_middle_ad_switch;
    //章节内开关
    public String dy_page_in_chapter_ad_switch;

    public String ad_limit_time_day;
    public String baidu_examine;
    public String user_transfer_first;
    public String user_transfer_second;
    public String switchSplashAdSec;
    public String isShowSwitchSplashAd;
    public String switchSplashAdCloseSec;
    public String channel_limit;
    public String day_limit;
    public String network_limit;
    public String dy_ad_new_request_domain_name;
    public String novel_host;
    public String webView_host;
    public String download_limit;
    //    public String nonet_readhour;
    public String noNetReadNumber;
    private ArrayList<String> channelLimit = new ArrayList<>();
    private ArrayList<Integer> dayLimit = new ArrayList<>();
    private SharedPreferences sp;
    private Context context;

    public DynamicParamter(Context context) {
        this.context = context;
        sp = context.getSharedPreferences("onlineconfig_agent_online_setting_" + AppUtils.getPackageName(), 0);
    }


    public void setDynamicParamter() {
        LoadDataManager loadDataManager = new LoadDataManager(context);
        loadDataManager.startRequestDynamic(new LoadDataManager.DynamicServiceCallBack() {
            @Override
            public void onDynamicReceived(JSONObject result) {
                isReloadDynamic = false;
                parserJSONObject(result, true);
                AppLog.d("own_patamater", "onDataReceived value :" + result.toString());
            }

            @Override
            public void onError(Exception error) {
                isReloadDynamic = true;
                setUMDynamicParamter();
            }
        });

        baidu_stat_id = getConfigParams("baidu_stat_id");
        push_key = getConfigParams("push_key");
        ban_gids = getConfigParams("ban_gids");
        channel_limit = getConfigParams(Constants.CHANNEL_LIMIT);
        day_limit = getConfigParams(Constants.DAY_LIMIT);
        ad_limit_time_day = getConfigParams(Constants.AD_LIMIT_TIME_DAY);
        baidu_examine = getConfigParams("baidu_examine");
        network_limit = getConfigParams("network_limit");
        dy_ad_new_request_domain_name = getConfigParams(Constants.DY_AD_NEW_REQUEST_DOMAIN_NAME);
        user_transfer_first = getConfigParams("user_transfer_first");
        user_transfer_second = getConfigParams("user_transfer_second");

        //新壳广告开关
        new_app_ad_switch = getConfigParams(Constants.NEW_APP_AD_SWITCH);
        //广告总开关
        dy_ad_switch = getConfigParams(Constants.DY_AD_SWITCH);
        //广告，新的请求开关
        dy_ad_new_request_switch = getConfigParams(Constants.DY_AD_NEW_REQUEST_SWITCH);
        //新的统计开关
        dy_ad_new_statistics_switch = getConfigParams(Constants.DY_AD_NEW_STATISTICS_SWITCH);
        //阅读页翻页统计开关
        dy_readPage_statistics_switch = getConfigParams(Constants.DY_READPAGE_STATISTICS_SWITCH);
        //老的广告统计开关
        dy_ad_old_request_switch = getConfigParams(Constants.DY_AD_OLD_REQUEST_SWITCH);
        //X小时内新用户不显示广告设置
        dy_adfree_new_user = getConfigParams(Constants.DY_ADFREE_NEW_USER);
        //开屏页开关
        dy_splash_ad_switch = getConfigParams(Constants.DY_SPLASH_AD_SWITCH);
        //书架页开关
        dy_shelf_ad_switch = getConfigParams(Constants.DY_SHELF_AD_SWITCH);
        //书架页广告间隔频率设置
        dy_shelf_ad_freq = getConfigParams(Constants.DY_SHELF_AD_FREQ);
        //章节末开关
        dy_page_end_ad_switch = getConfigParams(Constants.DY_PAGE_END_AD_SWITCH);
        //章节末广告间隔频率设置
        dy_page_end_ad_freq = getConfigParams(Constants.DY_PAGE_END_AD_FREQ);
        //书末广告开关
        dy_book_end_ad_switch = getConfigParams(Constants.DY_BOOK_END_AD_SWITCH);
        //休息页广告
        dy_rest_ad_switch = getConfigParams(Constants.DY_REST_AD_SWITCH);
        //休息页广告休息时间设置
        dy_rest_ad_sec = getConfigParams(Constants.DY_REST_AD_SEC);
        //切屏广告开关
        isShowSwitchSplashAd = getConfigParams("DY_activited_switch_ad");
        //切屏广告显示间隔
        switchSplashAdSec = getConfigParams("DY_switch_ad_sec");
        //切屏广告关闭按钮出现的时间
        switchSplashAdCloseSec = getConfigParams("DY_switch_ad_close_sec");
        //章节间开关
        dy_page_middle_ad_switch = getConfigParams("DY_page_middle_ad_switch");
        //章节内开关
        dy_page_in_chapter_ad_switch = getConfigParams("DY_page_in_chapter_ad_switch");
        //章节间广告展示频率
        native_ad_page_interstitial_count = getConfigParams("DY_mid_page_frequence");
        //章节内广告展示频率(隔章)
        native_ad_page_gap_in_chapter = getConfigParams("DY_in_chapter_frequence");
        //章节内展现广告要求的最小数量
        native_ad_page_in_chapter_limit = getConfigParams("DY_page_in_chapter_limit");
        //是否启用新版书末UI
        dy_is_new_reading_end = getConfigParams(Constants.DY_IS_NEW_READING_END);
        //api的host
        novel_host = getConfigParams(Constants.NOVEL_HOST);
        //webView的host
        webView_host = getConfigParams(Constants.WEBVIEW_HOST);

        //每天下载书籍量限制
        download_limit = getConfigParams(Constants.DOWNLOAD_LIMIT);

        //每日无网络阅读限制
//        nonet_readhour = getConfigParams(Constants.NONET_READTIME);


        //无网限制开关
        noNetReadNumber = getConfigParams(Constants.noNetReadNumber);


        installParam();

    }


    private String getConfigParams(String key) {
        if (sp != null) {
            return sp.getString(key, "");
        }
        return null;
    }

    private void putConfigParams(String key, String value) {
        if (sp != null) {
            sp.edit().putString(key, value).apply();
        }
    }

    private void setUMDynamicParamter() {

        // 更新在线参数
        OnlineConfigAgent.getInstance().updateOnlineConfig(context);
        OnlineConfigAgent.getInstance().setOnlineConfigListener(new UmengOnlineConfigureListener() {
            @Override
            public void onDataReceived(JSONObject data) {
                if (data != null) {
                    parserJSONObject(data, false);
                    isReloadDynamic = false;
                    AppLog.d("um_param", "onDataReceived value :" + data.toString());
                }
            }

        });
        baidu_stat_id = OnlineConfigAgent.getInstance().getConfigParams(context, "baidu_stat_id");
        push_key = OnlineConfigAgent.getInstance().getConfigParams(context, "push_key");
        ban_gids = OnlineConfigAgent.getInstance().getConfigParams(context, "ban_gids");
//        splash_loading_time = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.SPLASH_LOADING_TIME);
//        splash_ad_type = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.SPLASH_AD_TYPE);
        channel_limit = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.CHANNEL_LIMIT);
        day_limit = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DAY_LIMIT);
        ad_limit_time_day = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.AD_LIMIT_TIME_DAY);
        baidu_examine = OnlineConfigAgent.getInstance().getConfigParams(context, "baidu_examine");
        network_limit = OnlineConfigAgent.getInstance().getConfigParams(context, "network_limit");
        dy_ad_new_request_domain_name = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_AD_NEW_REQUEST_DOMAIN_NAME);
        user_transfer_first = OnlineConfigAgent.getInstance().getConfigParams(context, "user_transfer_first");
        user_transfer_second = OnlineConfigAgent.getInstance().getConfigParams(context, "user_transfer_second");

        //广告总开关
        dy_ad_switch = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_AD_SWITCH);
        //广告，新的请求开关
        dy_ad_new_request_switch = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_AD_NEW_REQUEST_SWITCH);
        //新的统计开关
        dy_ad_new_statistics_switch = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_AD_NEW_STATISTICS_SWITCH);
        //阅读页翻页统计开关
        dy_readPage_statistics_switch = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_READPAGE_STATISTICS_SWITCH);
        //老的广告统计开关
        dy_ad_old_request_switch = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_AD_OLD_REQUEST_SWITCH);
        //X小时内新用户不显示广告设置
        dy_adfree_new_user = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_ADFREE_NEW_USER);
        //开屏页开关
        dy_splash_ad_switch = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_SPLASH_AD_SWITCH);
        //书架页开关
        dy_shelf_ad_switch = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_SHELF_AD_SWITCH);
        //书架页广告间隔频率设置
        dy_shelf_ad_freq = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_SHELF_AD_FREQ);
        //章节末开关
        dy_page_end_ad_switch = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_PAGE_END_AD_SWITCH);
        //章节末广告间隔频率设置
        dy_page_end_ad_freq = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_PAGE_END_AD_FREQ);
        //书末广告开关
        dy_book_end_ad_switch = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_BOOK_END_AD_SWITCH);
        //休息页广告
        dy_rest_ad_switch = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_REST_AD_SWITCH);
        //休息页广告休息时间设置
        dy_rest_ad_sec = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_REST_AD_SEC);
        //切屏广告开关
        isShowSwitchSplashAd = OnlineConfigAgent.getInstance().getConfigParams(context, "DY_activited_switch_ad");
        //切屏广告显示间隔
        switchSplashAdSec = OnlineConfigAgent.getInstance().getConfigParams(context, "DY_switch_ad_sec");
        //切屏广告关闭按钮出现的时间
        switchSplashAdCloseSec = OnlineConfigAgent.getInstance().getConfigParams(context, "DY_switch_ad_close_sec");
        //章节间开关
        dy_page_middle_ad_switch = OnlineConfigAgent.getInstance().getConfigParams(context, "DY_page_middle_ad_switch");
        //章节内开关
        dy_page_in_chapter_ad_switch = OnlineConfigAgent.getInstance().getConfigParams(context, "DY_page_in_chapter_ad_switch");
        //章节间广告展示频率
        native_ad_page_interstitial_count = OnlineConfigAgent.getInstance().getConfigParams(context, "DY_mid_page_frequence");
        //章节内广告展示频率(隔章)
        native_ad_page_gap_in_chapter = OnlineConfigAgent.getInstance().getConfigParams(context, "DY_in_chapter_frequence");
        //章节内展现广告要求的最小数量
        native_ad_page_in_chapter_limit = OnlineConfigAgent.getInstance().getConfigParams(context, "DY_page_in_chapter_limit");
        //是否启用新版书末UI
        dy_is_new_reading_end = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DY_IS_NEW_READING_END);

        //api的host
        novel_host = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.NOVEL_HOST);

        //webView的host
        webView_host = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.WEBVIEW_HOST);

        //每天下载书籍量限制
        download_limit = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.DOWNLOAD_LIMIT);

        //每日无网络阅读限制
//        nonet_readhour = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.NONET_READTIME);
        //无网络限制开关
        noNetReadNumber = OnlineConfigAgent.getInstance().getConfigParams(context, Constants.noNetReadNumber);

        installParam();
    }

    private void parserJSONObject(JSONObject data, boolean isOwn) {
        try {
            if (!data.isNull(Constants.CHANNEL_LIMIT)) {
                channel_limit = data.getString(Constants.CHANNEL_LIMIT);
                if (isOwn) {
                    putConfigParams(Constants.CHANNEL_LIMIT, channel_limit);
                }
            }

            if (!data.isNull(Constants.DAY_LIMIT)) {
                day_limit = data.getString(Constants.DAY_LIMIT);
                if (isOwn) {
                    putConfigParams(Constants.DAY_LIMIT, day_limit);
                }
            }

            if (!data.isNull(Constants.BAIDU_STAT_ID)) {
                baidu_stat_id = data.getString(Constants.BAIDU_STAT_ID);
                if (isOwn) {
                    putConfigParams(Constants.BAIDU_STAT_ID, baidu_stat_id);
                }
            }

            if (!data.isNull(Constants.DY_AD_SWITCH)) {
                dy_ad_switch = data.getString(Constants.DY_AD_SWITCH);
                if (isOwn) {
                    putConfigParams(Constants.DY_AD_SWITCH, dy_ad_switch);
                }
            }
            if (!data.isNull(Constants.DY_AD_NEW_REQUEST_SWITCH)) {
                dy_ad_new_request_switch = data.getString(Constants.DY_AD_NEW_REQUEST_SWITCH);
                if (isOwn) {
                    putConfigParams(Constants.DY_AD_NEW_REQUEST_SWITCH, dy_ad_new_request_switch);
                }
            }
            if (!data.isNull(Constants.DY_AD_NEW_STATISTICS_SWITCH)) {
                dy_ad_new_statistics_switch = data.getString(Constants.DY_AD_NEW_STATISTICS_SWITCH);
                if (isOwn) {
                    putConfigParams(Constants.DY_AD_NEW_STATISTICS_SWITCH, dy_ad_new_statistics_switch);
                }
            }
            if (!data.isNull(Constants.DY_READPAGE_STATISTICS_SWITCH)) {
                dy_readPage_statistics_switch = data.getString(Constants.DY_READPAGE_STATISTICS_SWITCH);
                if (isOwn) {
                    putConfigParams(Constants.DY_READPAGE_STATISTICS_SWITCH, dy_readPage_statistics_switch);
                }
            }
            if (!data.isNull(Constants.DY_AD_OLD_REQUEST_SWITCH)) {
                dy_ad_old_request_switch = data.getString(Constants.DY_AD_OLD_REQUEST_SWITCH);
                if (isOwn) {
                    putConfigParams(Constants.DY_AD_OLD_REQUEST_SWITCH, dy_ad_old_request_switch);
                }
            }
            if (!data.isNull(Constants.DY_ADFREE_NEW_USER)) {
                dy_adfree_new_user = data.getString(Constants.DY_ADFREE_NEW_USER);
                if (isOwn) {
                    putConfigParams(Constants.DY_ADFREE_NEW_USER, dy_adfree_new_user);
                }
            }
            if (!data.isNull(Constants.DY_SPLASH_AD_SWITCH)) {
                dy_splash_ad_switch = data.getString(Constants.DY_SPLASH_AD_SWITCH);
                if (isOwn) {
                    putConfigParams(Constants.DY_SPLASH_AD_SWITCH, dy_splash_ad_switch);
                }
            }
            if (!data.isNull(Constants.DY_SHELF_AD_SWITCH)) {
                dy_shelf_ad_switch = data.getString(Constants.DY_SHELF_AD_SWITCH);
                if (isOwn) {
                    putConfigParams(Constants.DY_SHELF_AD_SWITCH, dy_shelf_ad_switch);
                }
            }

            if (!data.isNull(Constants.DY_SHELF_AD_FREQ)) {
                dy_shelf_ad_freq = data.getString(Constants.DY_SHELF_AD_FREQ);
                if (isOwn) {
                    putConfigParams(Constants.DY_SHELF_AD_FREQ, dy_shelf_ad_freq);
                }
            }

            if (!data.isNull(Constants.DY_PAGE_END_AD_SWITCH)) {
                dy_page_end_ad_switch = data.getString(Constants.DY_PAGE_END_AD_SWITCH);
                if (isOwn) {
                    putConfigParams(Constants.DY_PAGE_END_AD_SWITCH, dy_page_end_ad_switch);
                }
            }

            if (!data.isNull(Constants.DY_PAGE_END_AD_FREQ)) {
                dy_page_end_ad_freq = data.getString(Constants.DY_PAGE_END_AD_FREQ);
                if (isOwn) {
                    putConfigParams(Constants.DY_PAGE_END_AD_FREQ, dy_page_end_ad_freq);
                }
            }

            if (!data.isNull(Constants.DY_BOOK_END_AD_SWITCH)) {
                dy_book_end_ad_switch = data.getString(Constants.DY_BOOK_END_AD_SWITCH);
                if (isOwn) {
                    putConfigParams(Constants.DY_BOOK_END_AD_SWITCH, dy_book_end_ad_switch);
                }
            }

            if (!data.isNull(Constants.DY_REST_AD_SWITCH)) {
                dy_rest_ad_switch = data.getString(Constants.DY_REST_AD_SWITCH);
                if (isOwn) {
                    putConfigParams(Constants.DY_REST_AD_SWITCH, dy_rest_ad_switch);
                }
            }

            if (!data.isNull(Constants.DY_REST_AD_SEC)) {
                dy_rest_ad_sec = data.getString(Constants.DY_REST_AD_SEC);
                if (isOwn) {
                    putConfigParams(Constants.DY_REST_AD_SEC, dy_rest_ad_sec);
                }
            }

            if (!data.isNull(Constants.NATIVE_AD_PAGE_INTERSTITIAL_COUNT)) {
                native_ad_page_interstitial_count = data.getString(Constants.NATIVE_AD_PAGE_INTERSTITIAL_COUNT);
                if (isOwn) {
                    putConfigParams(Constants.NATIVE_AD_PAGE_INTERSTITIAL_COUNT, native_ad_page_interstitial_count);
                }
            }
            if (!data.isNull(Constants.NATIVE_AD_PAGE_GAP_IN_CHAPTER)) {
                native_ad_page_gap_in_chapter = data.getString(Constants.NATIVE_AD_PAGE_GAP_IN_CHAPTER);
                if (isOwn) {
                    putConfigParams(Constants.NATIVE_AD_PAGE_GAP_IN_CHAPTER, native_ad_page_gap_in_chapter);
                }
            }

            if (!data.isNull(Constants.DY_PAGE_MIDDLE_AD_SWITCH)) {
                dy_page_middle_ad_switch = data.getString(Constants.DY_PAGE_MIDDLE_AD_SWITCH);
                if (isOwn) {
                    putConfigParams(Constants.DY_PAGE_MIDDLE_AD_SWITCH, dy_page_middle_ad_switch);
                }
            }

            if (!data.isNull(Constants.NATIVE_AD_PAGE_IN_CHAPTER_LIMIT)) {
                native_ad_page_in_chapter_limit = data.getString(Constants.NATIVE_AD_PAGE_IN_CHAPTER_LIMIT);
                if (isOwn) {
                    putConfigParams(Constants.NATIVE_AD_PAGE_IN_CHAPTER_LIMIT, native_ad_page_in_chapter_limit);
                }
            }

            if (!data.isNull(Constants.DY_IS_NEW_READING_END)) {
                dy_is_new_reading_end = data.getString(Constants.DY_IS_NEW_READING_END);
                if (isOwn) {
                    putConfigParams(Constants.DY_IS_NEW_READING_END, dy_is_new_reading_end);
                }
            }

            if (!data.isNull(Constants.DY_SWITCH_AD_SEC)) {
                switchSplashAdSec = data.getString(Constants.DY_SWITCH_AD_SEC);
                if (isOwn) {
                    putConfigParams(Constants.DY_SWITCH_AD_SEC, switchSplashAdSec);
                }
            }

            if (!data.isNull(Constants.DY_ACTIVITED_SWITCH_AD)) {
                isShowSwitchSplashAd = data.getString(Constants.DY_ACTIVITED_SWITCH_AD);
                if (isOwn) {
                    putConfigParams(Constants.DY_ACTIVITED_SWITCH_AD, isShowSwitchSplashAd);
                }
            }

            if (!data.isNull(Constants.DY_SWITCH_AD_CLOSE_SEC)) {
                switchSplashAdCloseSec = data.getString(Constants.DY_SWITCH_AD_CLOSE_SEC);
                if (isOwn) {
                    putConfigParams(Constants.DY_SWITCH_AD_CLOSE_SEC, switchSplashAdCloseSec);
                }
            }

            if (!data.isNull(Constants.PUSH_KEY)) {
                push_key = data.getString(Constants.PUSH_KEY);
                if (isOwn) {
                    putConfigParams(Constants.PUSH_KEY, push_key);
                }
            }
            if (!data.isNull(Constants.BAN_GIDS)) {
                ban_gids = data.getString(Constants.BAN_GIDS);
                if (isOwn) {
                    putConfigParams(Constants.BAN_GIDS, ban_gids);
                }
            }
//            if (!data.isNull(Constants.SPLASH_LOADING_TIME)) {
//                splash_loading_time = data.getString(Constants.SPLASH_LOADING_TIME);
//            }
//            if (!data.isNull(Constants.SPLASH_AD_TYPE)) {
//                splash_ad_type = data.getString(Constants.SPLASH_AD_TYPE);
//            }
            if (!data.isNull(Constants.AD_LIMIT_TIME_DAY)) {
                ad_limit_time_day = data.getString(Constants.AD_LIMIT_TIME_DAY);
                if (isOwn) {
                    putConfigParams(Constants.AD_LIMIT_TIME_DAY, ad_limit_time_day);
                }
            }
            if (!data.isNull(Constants.BAIDU_EXAMINE)) {
                baidu_examine = data.getString(Constants.BAIDU_EXAMINE);
                if (isOwn) {
                    putConfigParams(Constants.BAIDU_EXAMINE, baidu_examine);
                }
            }
            if (!data.isNull(Constants.USER_TRANSFER_FIRST)) {
                user_transfer_first = data.getString(Constants.USER_TRANSFER_FIRST);
                if (isOwn) {
                    putConfigParams(Constants.USER_TRANSFER_FIRST, user_transfer_first);
                }
            }
            if (!data.isNull(Constants.USER_TRANSFER_SECOND)) {
                user_transfer_second = data.getString(Constants.USER_TRANSFER_SECOND);
                if (isOwn) {
                    putConfigParams(Constants.USER_TRANSFER_SECOND, user_transfer_second);
                }
            }
            if (!data.isNull(Constants.NETWORK_LIMIT)) {
                network_limit = data.getString(Constants.NETWORK_LIMIT);
                if (isOwn) {
                    putConfigParams(Constants.NETWORK_LIMIT, network_limit);
                }
            }
            if (!data.isNull(Constants.DY_AD_NEW_REQUEST_DOMAIN_NAME)) {
                dy_ad_new_request_domain_name = data.getString(Constants.DY_AD_NEW_REQUEST_DOMAIN_NAME);
                if (isOwn) {
                    putConfigParams(Constants.DY_AD_NEW_REQUEST_DOMAIN_NAME, dy_ad_new_request_domain_name);
                }
            }
            if (!data.isNull(Constants.NOVEL_HOST)) {
                novel_host = data.getString(Constants.NOVEL_HOST);
                if (isOwn) {
                    putConfigParams(Constants.NOVEL_HOST, novel_host);
                }
            }
            if (!data.isNull(Constants.WEBVIEW_HOST)) {
                webView_host = data.getString(Constants.WEBVIEW_HOST);
                if (isOwn) {
                    putConfigParams(Constants.WEBVIEW_HOST, webView_host);
                }
            }
            if (!data.isNull(Constants.noNetReadNumber)) {
                noNetReadNumber = data.getString(Constants.noNetReadNumber);
                if (isOwn) {
                    putConfigParams(Constants.noNetReadNumber, noNetReadNumber);
                }
            }

            if (!data.isNull(Constants.DOWNLOAD_LIMIT)) {
                download_limit = data.getString(Constants.DOWNLOAD_LIMIT);
                if (isOwn) {
                    putConfigParams(Constants.DOWNLOAD_LIMIT, download_limit);
                }
            }
//            if (!data.isNull(Constants.NONET_READTIME)) {
//                nonet_readhour = data.getString(Constants.NONET_READTIME);
//                if (isOwn) {
//                    putConfigParams(Constants.NONET_READTIME, nonet_readhour);
//                }
//            }
            //新壳的广告开关
            if (!data.isNull(Constants.NEW_APP_AD_SWITCH)) {
                new_app_ad_switch = data.getString(Constants.NEW_APP_AD_SWITCH);
                if (isOwn) {
                    putConfigParams(Constants.NEW_APP_AD_SWITCH, new_app_ad_switch);
                }
            }
            installParam();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void installParam() {
//        setBaiduAppId();
//        setMogoAppId();
        setHost();

        setDownLoadLimitNumber();

        setNoNetReadTime();

        setBaiduStat();
        setPush();
        setBanGids();
        setNativeAdIntervalCount();
//        setSplashLoadingTime();
//        setSplashADType();
        setBaiduExamine();
        setUserTransfer();

        setChannelLimit();
        setDayLimit();

        setNoADTime();

        setNetWorkLimit();

        AppLog.d("um_param", " real param ==> " + this.toString());
    }

    private void setNoNetReadTime() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Editor editor = sp.edit();
//        if (!TextUtils.isEmpty(nonet_readhour)) {
//            int nonet_read = Integer.parseInt(nonet_readhour);
//            Constants.NONET_READHOUR = nonet_read;
//            editor.putInt(Constants.NONET_READTIME, nonet_read).apply();
//        }
//        if (!TextUtils.isEmpty(noNetReadNumber)) {
//            Constants.isNoNetRead = Integer.parseInt(noNetReadNumber);
//        }
    }

    private void setDownLoadLimitNumber() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Editor editor = sp.edit();
        if (!TextUtils.isEmpty(download_limit)) {
            int download_limit_number = Integer.parseInt(download_limit);
            Constants.DOWNLOAD_LIMIT_NUMBER = download_limit_number;
            editor.putInt(Constants.DOWNLOAD_LIMIT, download_limit_number).apply();
        }
    }

    private void setHost() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Editor editor = sp.edit();
        if (!TextUtils.isEmpty(novel_host)) {
            editor.putString(Constants.NOVEL_HOST, novel_host);
        }
        if (!TextUtils.isEmpty(webView_host)) {
            editor.putString(Constants.WEBVIEW_HOST, webView_host);
        }
        editor.apply();
    }

    private void setNetWorkLimit() {
        if (!TextUtils.isEmpty(network_limit)) {
            int limit = Integer.parseInt(network_limit);
            if (limit == 0) {
                Constants.is_reading_network_limit = true;
            } else {
                Constants.is_reading_network_limit = false;
            }
        }

    }

    private void setUserTransfer() {
        if (!TextUtils.isEmpty(user_transfer_first)) {
            int first_level = Integer.parseInt(user_transfer_first);
            if (first_level == 0) {
                Constants.is_user_transfer_first = false;
            } else {
                Constants.is_user_transfer_first = true;
            }
        } else {
            Constants.is_user_transfer_first = true;
        }
        if (!TextUtils.isEmpty(user_transfer_second)) {
            int second_level = Integer.parseInt(user_transfer_second);
            if (second_level == 0) {
                Constants.is_user_transfer_second = false;
            } else {
                Constants.is_user_transfer_second = true;
            }
        } else {
            Constants.is_user_transfer_second = false;
        }
    }

    private void setChannelLimit() {
        if (!TextUtils.isEmpty(channel_limit)) {
            String[] message = channel_limit.split(",");
            for (int i = 0; i < message.length; i++) {
                channelLimit.add(i, message[i]);
            }
        }
    }

    private void setDayLimit() {
        if (!TextUtils.isEmpty(day_limit)) {
            String[] message = day_limit.split(",");
            for (int i = 0; i < message.length; i++) {
                dayLimit.add(i, Integer.valueOf(message[i]));
            }
        }
    }

    private void setBaiduExamine() {

        if (!TextUtils.isEmpty(baidu_examine)) {
            String[] message = baidu_examine.split(",");
            if (message.length > 0) {
                if (message.length > 0 && !TextUtils.isEmpty(message[0])) {
                    int value = Integer.parseInt(message[0]);
                    if (value == 0) {
                        Constants.isBaiduExamine = false;
                    } else {
                        Constants.isBaiduExamine = true;
                    }
                    AppLog.e(TAG, "baiduExamine: " + message[0]);
                    AppLog.e(TAG, "baiduExamine: " + Constants.isBaiduExamine);
                }

                if (message.length > 1 && !TextUtils.isEmpty(message[1])) {
                    Constants.versionCode = Integer.valueOf(message[1]);
                    AppLog.e(TAG, "baiduExamine: " + message[1]);
                    AppLog.e(TAG, "baiduExamine: " + Constants.versionCode);
                }

                if (message.length > 2 && !TextUtils.isEmpty(message[2])) {
                    int value = Integer.valueOf(message[2]);
                    if (value == 0) {
                        Constants.isHuaweiExamine = false;
                    } else {
                        Constants.isHuaweiExamine = true;
                    }
                }
            }
        }
    }

    private void setNoADTime() {
        // 隐藏广告期限
        if (!TextUtils.isEmpty(ad_limit_time_day)) {
            String channelID = "";
            channelID = AppUtils.getChannelId();

            AppLog.e(TAG, "channelLimit: " + channel_limit);
            AppLog.e(TAG, "dayLimit: " + day_limit);
            int all_channel_index = channelLimit.indexOf("AllChannel");
            int all_channel_limit = dayLimit.get(all_channel_index);
            if (all_channel_limit != 0) {
                AppLog.e(TAG, "all_channel_limit: " + all_channel_limit);
                Constants.ad_limit_time_day = all_channel_limit;
            } else {
                if (channelLimit.contains(channelID)) {
                    AppLog.e(TAG, "channelLimit.contains: " + channelID);
                    int index = channelLimit.indexOf(channelID);
                    AppLog.e(TAG, "dayLimit: " + dayLimit.get(index));
                    Constants.ad_limit_time_day = dayLimit.get(index);
                } else {
                    try {
                        Constants.ad_limit_time_day = Integer.parseInt(ad_limit_time_day);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (Constants.DEVELOPER_MODE) {
                    AppLog.e(TAG, " ad_limit_time_day:" + Constants.ad_limit_time_day);
                }
            }
//            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.user_new_ad_limit_day,   Constants.ad_limit_time_day).apply();;

        }
    }

//    private void setSplashADType() {
//        // 开屏广告显示类型
//        if (!TextUtils.isEmpty(splash_ad_type)) {
//            try {
//                Constants.splash_ad_type = Integer.parseInt(splash_ad_type);
//            } catch (Exception e) {
//            }
//        }
//    }

//    private void setSplashLoadingTime() {
//        // 开屏时间
//        if (!TextUtils.isEmpty(splash_loading_time)) {
//            try {
//                Constants.splash_loading_time = Integer.parseInt(splash_loading_time);
//            } catch (Exception e) {
//            }
//        }
//    }

    private void setNativeAdIntervalCount() {

        //广告总开关
        if (!TextUtils.isEmpty(dy_ad_switch)) {
            try {
                Constants.dy_ad_switch = Boolean.parseBoolean(dy_ad_switch);
            } catch (Exception e) {
            }
        }

        //新的用户广告请求开关
        if (!TextUtils.isEmpty(dy_ad_new_request_switch)) {
            try {
                Constants.dy_ad_new_request_switch = Boolean.parseBoolean(dy_ad_new_request_switch);
            } catch (Exception e) {
            }
        }
        //新的统计开关
        if (!TextUtils.isEmpty(dy_ad_new_statistics_switch)) {
            try {
                Constants.dy_ad_new_statistics_switch = Boolean.parseBoolean(dy_ad_new_statistics_switch);
            } catch (Exception e) {
            }
        }
        //阅读页翻页统计开关
        if (!TextUtils.isEmpty(dy_readPage_statistics_switch)) {
            try {
                Constants.dy_readPage_statistics_switch = Boolean.parseBoolean(dy_readPage_statistics_switch);
            } catch (Exception e) {
            }
        }
        //老的广告统计开关
        if (!TextUtils.isEmpty(dy_ad_old_request_switch)) {
            try {
                Constants.dy_ad_old_request_switch = Boolean.parseBoolean(dy_ad_old_request_switch);
            } catch (Exception e) {
            }
        }
        //新的用户广告请求接口
        if (!TextUtils.isEmpty(dy_ad_new_request_domain_name)) {
            try {
                Constants.AD_DATA_Collect = dy_ad_new_request_domain_name;
            } catch (Exception e) {
            }
        }

        //X小时内新用户不显示广告设置
        if (!TextUtils.isEmpty(dy_adfree_new_user)) {
            try {
                Constants.ad_limit_time_day = Integer.parseInt(dy_adfree_new_user);
            } catch (Exception e) {
            }
        }

        //开屏页开关
        if (!TextUtils.isEmpty(dy_splash_ad_switch)) {
            try {
                Constants.dy_splash_ad_switch = Boolean.parseBoolean(dy_splash_ad_switch);
            } catch (Exception e) {
            }
        }

        //书架页开关
        if (!TextUtils.isEmpty(dy_shelf_ad_switch)) {
            try {
                Constants.dy_shelf_ad_switch = Boolean.parseBoolean(dy_shelf_ad_switch);
            } catch (Exception e) {
            }
        }

        //书架页广告间隔频率设置
        if (!TextUtils.isEmpty(dy_shelf_ad_freq)) {
            try {
                Constants.dy_shelf_ad_freq = Integer.parseInt(dy_shelf_ad_freq);
            } catch (Exception e) {
            }
        }

        //章节末开关
        if (!TextUtils.isEmpty(dy_page_end_ad_switch)) {
            try {
                Constants.dy_page_end_ad_switch = Boolean.parseBoolean(dy_page_end_ad_switch);
            } catch (Exception e) {
            }
        }

        //章节末广告间隔频率设置
        if (!TextUtils.isEmpty(dy_page_end_ad_freq)) {
            try {
                Constants.dy_page_end_ad_freq = Integer.parseInt(dy_page_end_ad_freq);
            } catch (Exception e) {
            }
        }

        //书末广告开关
        if (!TextUtils.isEmpty(dy_book_end_ad_switch)) {
            try {
                Constants.dy_book_end_ad_switch = Boolean.parseBoolean(dy_book_end_ad_switch);
            } catch (Exception e) {
            }
        }

        //休息页广告开关
        if (!TextUtils.isEmpty(dy_rest_ad_switch)) {
            try {
                Constants.dy_rest_ad_switch = Boolean.parseBoolean(dy_rest_ad_switch);
            } catch (Exception e) {
            }
        }

        //休息页广告休息时间设置
        if (!TextUtils.isEmpty(dy_rest_ad_sec)) {
            try {
                Constants.read_rest_time = Integer.parseInt(dy_rest_ad_sec) * 60 * 1000;
            } catch (Exception e) {
            }
        }

        //章节间开关
        if (!TextUtils.isEmpty(dy_page_middle_ad_switch)) {
            try {
                Constants.dy_page_middle_ad_switch = Boolean.parseBoolean(dy_page_middle_ad_switch);
            } catch (Exception e) {
            }
        }
        //章节内开关
        if (!TextUtils.isEmpty(dy_page_in_chapter_ad_switch)) {
            try {
                Constants.dy_page_in_chapter_ad_switch = Boolean.parseBoolean(dy_page_in_chapter_ad_switch);
            } catch (Exception e) {
            }
        }
        // 原生广告间隔的章节数
        if (!TextUtils.isEmpty(native_ad_page_interstitial_count)) {
            try {
                Constants.native_ad_page_interstitial_count = Integer.parseInt(native_ad_page_interstitial_count);
            } catch (Exception e) {
            }
        }

        //章节内广告间隔的章节数
        if (!TextUtils.isEmpty(native_ad_page_gap_in_chapter)) {
            try {
                Constants.native_ad_page_gap_in_chapter = Integer.parseInt(native_ad_page_gap_in_chapter);
            } catch (Exception e) {
            }
        }

        // 章节内广告显示要求的最小页数
        if (!TextUtils.isEmpty(native_ad_page_in_chapter_limit)) {
            try {
                Constants.native_ad_page_in_chapter_limit = Integer.parseInt(native_ad_page_in_chapter_limit);
            } catch (Exception e) {
            }
        }

        //切屏广告的开关
        if (!TextUtils.isEmpty(isShowSwitchSplashAd)) {
            try {
                Constants.isShowSwitchSplashAd = Boolean.parseBoolean(isShowSwitchSplashAd);
            } catch (Exception e) {
            }
        }


        //切屏广告的间隔秒数
        if (!TextUtils.isEmpty(switchSplashAdSec)) {
            try {
                Constants.switchSplash_ad_sec = Integer.parseInt(switchSplashAdSec);
            } catch (Exception e) {
            }
        }
        //切屏广告关闭按钮出现的时间
        if (!TextUtils.isEmpty(switchSplashAdCloseSec)) {
            try {
                Constants.show_switchSplash_ad_close = Integer.parseInt(switchSplashAdCloseSec);
            } catch (Exception e) {
            }
        }
        //章节末广告是否新版
        if (!TextUtils.isEmpty(dy_is_new_reading_end)) {
            try {
                Constants.dy_is_new_reading_end = Boolean.parseBoolean(dy_is_new_reading_end);
            } catch (Exception e) {
            }
        }
        //新app广告开关
        if (!TextUtils.isEmpty(new_app_ad_switch)) {
            try {
                Constants.new_app_ad_switch = Boolean.parseBoolean(new_app_ad_switch);
            } catch (Exception e) {
            }
        }

    }

//    private void setBaiduAppId() {
//        // 设置百度广告计费id
//        if (!TextUtils.isEmpty(baidu_app_id)) {
//            AppConstants.baidu_app_id = baidu_app_id;
//        }
//
//        if (!TextUtils.isEmpty(baidu_app_sec)) {
//            AppConstants.baidu_app_sec = baidu_app_sec;
//        }
//    }

//    private void setMogoAppId() {
//        // 设置芒果计费id
//        if (!TextUtils.isEmpty(mogo_app_id)) {
//            AppConstants.mogo_app_id = mogo_app_id;
//        }
//    }


    private void setBaiduStat() {
        // 设置百度统计信息
        if (!TextUtils.isEmpty(baidu_stat_id)) {
            ReplaceConstants.getReplaceConstants().BAIDU_STAT_ID = baidu_stat_id;
        }
        try {
            StatService.setAppKey(ReplaceConstants.getReplaceConstants().BAIDU_STAT_ID);
            StatService.setSendLogStrategy(context, SendStrategyEnum.APP_START, 1, false);
            StatService.setOn(context, StatService.EXCEPTION_LOG);
            StatService.setDebugOn(Constants.DEVELOPER_MODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPush() {
        // 设置百度广告计费id
        if (!TextUtils.isEmpty(push_key)) {
            ReplaceConstants.getReplaceConstants().PUSH_KEY = push_key;
        }
        try {
            PushManager.startWork(context, PushConstants.LOGIN_TYPE_API_KEY, ReplaceConstants.getReplaceConstants().PUSH_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setBanGids() {
        if (!TextUtils.isEmpty(ban_gids)) {
            String[] gids = ban_gids.split(",");
            List<Integer> index = new ArrayList<>();
            for (int i = 0; i < gids.length; i++) {
                index.add(Integer.valueOf(gids[i]));
            }
        }
    }

    @Override
    public String toString() {
        return "DynamicParamter{" +
                "baidu_stat_id='" + baidu_stat_id + '\'' +
                ", native_ad_page_interstitial_count='" + native_ad_page_interstitial_count + '\'' +
                ", native_ad_page_in_chapter_limit='" + native_ad_page_in_chapter_limit + '\'' +
//                ", splash_loading_time='" + splash_loading_time + '\'' +
                ", ad_limit_time_day='" + ad_limit_time_day + '\'' +
//                ", splash_ad_type='" + splash_ad_type + '\'' +
                ", baidu_examine='" + baidu_examine + '\'' +
                ", user_transfer_first='" + user_transfer_first + '\'' +
                ", user_transfer_second='" + user_transfer_second + '\'' +
                ", push_key='" + push_key + '\'' +
                ", reading_network_limit='" + network_limit + '\'' +
                ", context=" + context +
                '}';
    }
}
