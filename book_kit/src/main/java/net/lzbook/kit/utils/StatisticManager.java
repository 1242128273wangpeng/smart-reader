package net.lzbook.kit.utils;

import com.dingyueads.sdk.Bean.AdSceneData;
import com.dingyueads.sdk.Bean.Novel;
import com.dingyueads.sdk.Bean.Ration;
import com.dingyueads.sdk.Native.YQNativeAdInfo;
import com.dingyueads.sdk.NativeInit;
import com.dingyueads.sdk.manager.ADStatisticManager;
import com.logcat.sdk.LogEncapManager;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.Constants;

import android.app.Activity;
import android.os.Build;
import android.view.View;

import java.util.HashMap;

public class StatisticManager {

    public static final String TAG = StatisticManager.class.getSimpleName();
    public static final int TYPE_SHOW = 0x20;
    public static final int TYPE_CLICK = 0x21;
    public static final int TYPE_END = 0x22;
    private static volatile StatisticManager statisticManager = null;
    private HashMap<String, YQNativeAdInfo> nativeAD = new HashMap<>();

    public static StatisticManager getStatisticManager() {

        if (statisticManager == null) {
            synchronized (StatisticManager.class) {
                if (statisticManager == null) {
                    statisticManager = new StatisticManager();
                }
            }
        }
        return statisticManager;
    }

    public void schedulingRequest(Activity activity, View view, YQNativeAdInfo nativeAdInfo, Novel novel, int type, String position) {

        if (nativeAdInfo != null) {
            nativeAdInfo.setCreateTime(System.currentTimeMillis());
        }

        //广告位置判断
        if (NativeInit.ad_position[0].equals(position) || NativeInit.ad_position[7].equals(position) || NativeInit.ad_position[8].equals(position)
                || NativeInit.ad_position[1].equals(position)
                || (nativeAdInfo != null && nativeAdInfo.getAdvertisement() != null && nativeAdInfo.getAdvertisement().platformId == com.dingyueads.sdk.Constants.AD_TYPE_YINCHENG)
                || (nativeAdInfo != null && nativeAdInfo.getAdvertisement() != null && nativeAdInfo.getAdvertisement().platformId == com.dingyueads.sdk.Constants.AD_TYPE_OWNAD)) {
            switch (type) {
                //书架展现
                case TYPE_SHOW:
                    if (nativeAdInfo != null && nativeAdInfo.getAdvertisement() != null && !nativeAdInfo.getAdvertisement().isShowed) {
                        nativeAdInfo.showedDefaultAD(BaseBookApplication.getGlobalContext(), view, OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()), novel, position, Constants.dy_ad_old_request_switch);
                        AdSceneData adSceneData = nativeAdInfo.getAdSceneData();
                        if (adSceneData != null) {
                            adSceneData.ad_showSuccessTime = String.valueOf(System.currentTimeMillis() / 1000L);
                            adSceneData.ad_show = 1;
                            if (novel != null) {
                                adSceneData.ad_chapterId = novel.adChapterId;
                                adSceneData.ad_author = novel.author;
                                adSceneData.book_id = novel.novelId;
                                adSceneData.book_source_id = novel.book_source_id;
                                adSceneData.channel_code = novel.channelCode;
                            }
                            sendAdSceneData(adSceneData);
                        }

                    }
                    break;
                //书架点击
                case TYPE_CLICK:
                    if (nativeAdInfo != null && nativeAdInfo.getAdvertisement() != null && !nativeAdInfo.getAdvertisement().isClicked) {
                        nativeAdInfo.clickedDefaultAD(activity, BaseBookApplication.getGlobalContext(), view, OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()), novel, position, Constants.dy_ad_old_request_switch);
                        AdSceneData adSceneData = nativeAdInfo.getAdSceneData();
                        if (adSceneData != null) {
                            adSceneData.ad_clickSuccessTime = String.valueOf(System.currentTimeMillis() / 1000L);
                            adSceneData.ad_click = 1;
                            if (novel != null) {
                                adSceneData.ad_chapterId = novel.adChapterId;
                                adSceneData.ad_author = novel.author;
                                adSceneData.book_id = novel.novelId;
                                adSceneData.book_source_id = novel.book_source_id;
                                adSceneData.channel_code = novel.channelCode;
                            }
                            sendAdSceneData(adSceneData);
                        }
                    }
                    break;
            }
        } else {
            switch (type) {
                case TYPE_SHOW:
                    if (!nativeAD.containsKey(position)) {
                        if (nativeAdInfo != null && nativeAdInfo.getAdvertisement() != null && !nativeAdInfo.getAdvertisement().isShowed && !nativeAdInfo.getAdvertisement().isClicked) {
                            nativeAD.put(position, nativeAdInfo);
                            AdSceneData adSceneData = nativeAdInfo.getAdSceneData();

                            if (adSceneData != null && novel != null) {
                                adSceneData.ad_show = 1;
                                adSceneData.ad_showSuccessTime = String.valueOf(System.currentTimeMillis() / 1000L);
                                adSceneData.ad_author = novel.author;
                                adSceneData.book_id = novel.novelId;
                                adSceneData.book_source_id = novel.book_source_id;
                                adSceneData.channel_code = novel.channelCode;
                            }
                        }
                    }
                    break;
                case TYPE_CLICK:
                    if (nativeAdInfo != null && nativeAdInfo.getAdvertisement() != null && !nativeAdInfo.getAdvertisement().isClicked) {
                        nativeAdInfo.clickedDefaultAD(activity, BaseBookApplication.getGlobalContext(), view, OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()), novel, position, Constants.dy_ad_old_request_switch);
                        if (nativeAD.containsKey(position)) {
                            nativeAD.remove(position);
                        }
                        AdSceneData adSceneData = nativeAdInfo.getAdSceneData();
                        if (adSceneData != null) {
                            adSceneData.ad_clickSuccessTime = String.valueOf(System.currentTimeMillis() / 1000L);
                            adSceneData.ad_click = 1;
                            if (novel != null) {
                                adSceneData.ad_chapterId = novel.adChapterId;
                                adSceneData.ad_author = novel.author;
                                adSceneData.book_id = novel.novelId;
                                adSceneData.book_source_id = novel.book_source_id;
                                adSceneData.channel_code = novel.channelCode;
                            }
                            sendAdSceneData(adSceneData);
                        }
                    }
                    break;
                case TYPE_END:
                    if (nativeAdInfo != null) {
                        if (nativeAD.containsKey(position)) {
                            if (nativeAdInfo.getAdvertisement() != null && !nativeAdInfo.getAdvertisement().isShowed) {
                                nativeAdInfo.showedDefaultAD(BaseBookApplication.getGlobalContext(), view, OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()), novel, position, Constants.dy_ad_old_request_switch);
                            }
                            nativeAD.remove(position);
                            AdSceneData adSceneData = nativeAdInfo.getAdSceneData();
                            if (adSceneData != null) {
                                adSceneData.ad_showFinishTime = String.valueOf(System.currentTimeMillis() / 1000L);
                                if (novel != null) {
                                    adSceneData.ad_chapterId = novel.adChapterId;
                                    adSceneData.ad_author = novel.author;
                                    adSceneData.book_id = novel.novelId;
                                    adSceneData.book_source_id = novel.book_source_id;
                                    adSceneData.channel_code = novel.channelCode;
                                }
                                sendAdSceneData(adSceneData);
                            }
                        }
                    } else {
                        ADStatisticManager adStatisticManager = ADStatisticManager.getADStatisticManager();
                        adStatisticManager.onADShowed(OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()), null, null, novel, position, Constants.dy_ad_old_request_switch);

                    }
                    break;
            }
        }
    }

    //发送用户基础数据
    public void sendUserData() {
        if (!Constants.dy_ad_new_statistics_switch) {
            return;
        }
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("udid", OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));
        parameters.put("app_package", AppUtils.getPackageName());
        parameters.put("app_version", AppUtils.getVersionName());
        parameters.put("app_version_code", String.valueOf(AppUtils.getVersionCode()));
        parameters.put("app_channel_id", AppUtils.getChannelId());
        parameters.put("phone_identity", AppUtils.getIMEI(BaseBookApplication.getGlobalContext()));
        parameters.put("vendor", Build.MODEL);
        parameters.put("os", Constants.APP_SYSTEM_PLATFORM + android.os.Build.VERSION.RELEASE);
        parameters.put("operator", AppUtils.getProvidersName(BaseBookApplication.getGlobalContext()));
        parameters.put("network", NetWorkUtils.getNetWorkTypeNew(BaseBookApplication.getGlobalContext()));
        if (null != BaseBookApplication.getDisplayMetrics()) {
            String resolution_ratio = BaseBookApplication.getDisplayMetrics().widthPixels + "*" +
                    BaseBookApplication.getDisplayMetrics().heightPixels;
            parameters.put("resolution_ratio", resolution_ratio);
        }
        parameters.put("longitude", String.valueOf(Constants.longitude));
        parameters.put("latitude", String.valueOf(Constants.latitude));
        parameters.put("city_info", Constants.adCityInfo);
        parameters.put("location_detail", Constants.adLocationDetail);

        LogEncapManager.getInstance().sendLog(parameters, "zn_user");
    }

    //发送广告场景数据
    public void sendAdSceneData(AdSceneData adSceneData) {
        if (!Constants.dy_ad_new_statistics_switch) {
            return;
        }
        if (adSceneData == null) return;

        HashMap<String, String> params = new HashMap<>();
        params.put("udid", OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));
        params.put("app_package", AppUtils.getPackageName());
        params.put("app_version", AppUtils.getVersionName());
        params.put("app_version_code", String.valueOf(AppUtils.getVersionCode()));
        params.put("app_channel_id", AppUtils.getChannelId());
        params.put("mark_id", adSceneData.ad_markId);
        params.put("platform_id", adSceneData.ad_platformId);
        params.put("platform_app_id", adSceneData.ad_platformAppId);
        params.put("platform_position_id", adSceneData.ad_positionId);
        params.put("material_id", adSceneData.ad_creativeId);
        params.put("material_title", adSceneData.ad_creativeTitle);
        params.put("material_decs", adSceneData.ad_creativeDesc);
        params.put("material_icon_url", adSceneData.ad_creativeIconUrl);
        params.put("material_img_url", adSceneData.ad_creativeImgUrl);
        params.put("material_action", adSceneData.ad_isApp);
        params.put("request_time", adSceneData.ad_requestTime);
        params.put("request_platform_success_time", adSceneData.ad_requestSuccessTime);
        params.put("show_success_time", adSceneData.ad_showSuccessTime);
        params.put("show_finish_time", adSceneData.ad_showFinishTime);
        params.put("show_num", String.valueOf(adSceneData.ad_show));
        params.put("click_time", adSceneData.ad_clickSuccessTime);
        params.put("click_location", adSceneData.ad_clickLocation);
        params.put("click_num", String.valueOf(adSceneData.ad_click));
        params.put("book_id", adSceneData.book_id);
        params.put("book_source_id", adSceneData.book_source_id);
        params.put("chapter_id", adSceneData.ad_chapterId);
        if ("A001".equals(adSceneData.channel_code)) {
            params.put("channel_code", String.valueOf(1));
        } else if ("A002".equals(adSceneData.channel_code)) {
            params.put("channel_code", String.valueOf(2));
        }

        LogEncapManager.getInstance().sendLog(params, "ad_action");
    }

    //发送广告失败数据
    public void sendAdFailData(AdSceneData adSceneData) {
        if (!Constants.dy_ad_new_statistics_switch) {
            return;
        }
        if (adSceneData == null) return;

        HashMap<String, String> params = new HashMap<>();
        params.put("udid", OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));
        params.put("app_package", AppUtils.getPackageName());
        params.put("app_version", AppUtils.getVersionName());
        params.put("app_version_code", String.valueOf(AppUtils.getVersionCode()));
        params.put("app_channel_id", AppUtils.getChannelId());
        params.put("mark_id", adSceneData.ad_markId);
        params.put("platform_id", adSceneData.ad_platformId);
        params.put("platform_app_id", adSceneData.ad_platformAppId);
        params.put("platform_position_id", adSceneData.ad_positionId);
        params.put("request_time", adSceneData.ad_requestTime);
        params.put("request_fail_time", adSceneData.ad_requestFailureTime);
        params.put("request_fail_reason", adSceneData.ad_requestFailureReason);

        LogEncapManager.getInstance().sendLog(params, "ad_fail");
    }

    //发送阅读信息pv数据
    public void sendReadPvData(HashMap<String, String> params) {
        if (!Constants.dy_ad_new_statistics_switch) {
            return;
        }
        if (params == null) return;
        params.put("udid", OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));
        params.put("app_package", AppUtils.getPackageName());
        params.put("app_version", AppUtils.getVersionName());
        params.put("app_version_code", String.valueOf(AppUtils.getVersionCode()));
        params.put("app_channel_id", AppUtils.getChannelId());

        LogEncapManager.getInstance().sendLog(params, "zn_pv");
    }

    //发送请求广告平台信息成功后的时间、物料数目
    public void sendRequestAdBackInfo(Ration ration) {
        if (!Constants.dy_ad_new_statistics_switch) {
            return;
        }
        if (ration == null) return;
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("udid", OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));
        parameters.put("app_package", AppUtils.getPackageName());
        parameters.put("app_version", AppUtils.getVersionName());
        parameters.put("app_version_code", String.valueOf(AppUtils.getVersionCode()));
        parameters.put("app_channel_id", AppUtils.getChannelId());
        parameters.put("mark_id", ration.getMarkId());
        parameters.put("platform_id", String.valueOf(ration.getPlatformId()));
        parameters.put("platform_app_id", ration.getPlatformAppId());
        parameters.put("platform_position_id", ration.getPositionId());
        parameters.put("request_platform_time", String.valueOf(ration.getRequestPlatTime()));
        parameters.put("request_platform_success_time", String.valueOf(ration.getRequestPlatSuccessTime()));
        parameters.put("request_success_material", String.valueOf(ration.getRequestMaterialCount()));

        LogEncapManager.getInstance().sendLog(parameters, "ad_request");
    }
}
