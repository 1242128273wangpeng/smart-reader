package com.intelligent.reader.util

import android.content.Context
import com.baidu.android.pushservice.PushConstants
import com.baidu.android.pushservice.PushManager
import com.baidu.mobstat.SendStrategyEnum
import com.baidu.mobstat.StatService
import com.ding.basic.Config
import com.ding.basic.bean.Map
import com.ding.basic.bean.Parameter
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.*
import com.dingyue.contract.util.SharedPreUtil
import com.orhanobut.logger.Logger
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.isNumeric
import net.lzbook.kit.utils.loge
import java.util.*

class DynamicParameter(private val context: Context) {

    private val shareUtil = SharedPreUtil(SharedPreUtil.SHARE_ONLINE_CONFIG)

    private var dynamicUrl: String = RequestService.DYNAMIC_PARAMETERS
        @Synchronized get() {
            field = when (field) {
                RequestService.DYNAMIC_PARAMETERS -> RequestService.DYNAMIC_ZN
                RequestService.DYNAMIC_ZN -> RequestService.DYNAMIC_CM
                RequestService.DYNAMIC_CM -> RequestService.DYNAMIC_YC
                else -> ""
            }
            return field
        }

    fun setDynamicParameter() {

        installParams()

        AppLog.d("startRequestCDNDynamic", "/v3/dynamic/dynamicParameter")

        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestDynamicParameters(
                object : RequestSubscriber<Parameter>() {
                    override fun requestResult(result: Parameter?) {
                        checkResult(result)
                    }

                    override fun requestError(message: String) {
                        Logger.e("请求动态参数异常！")
                        startRequestCDNDynamic()
                    }

                    override fun requestComplete() {

                    }
                })

    }

    private fun checkResult(parameter: Parameter?) {
        val isSuccess = parameter?.success ?: false
        val map = parameter?.map
        if (parameter == null) {
            startRequestCDNDynamic()
        } else {
            if (isSuccess) {
                if (map != null) {
                    isReloadDynamic = false
                    saveParams(map)
                    installParams()
                } else {
                    startRequestCDNDynamic()
                }
            } else {
                startRequestCDNDynamic()
            }
        }
    }

    private fun saveParams(map: Map) {
        loge("handleParams: $map")

        //广告开关
        var isShowAd = false
        if (map.show_ad_version?.isNumeric() == true) {
            val version = Integer.parseInt(map.show_ad_version)
            if (AppUtils.getVersionCode() >= version) {
                isShowAd = true
            }
        }

        shareUtil.putString(Constants.CHANNEL_LIMIT, map.channel_limit)
        shareUtil.putString(Constants.RECOMMEND_BOOKCOVER, map.recommend_bookcover)
        shareUtil.putString(Constants.DAY_LIMIT, map.day_limit)
        shareUtil.putString(Constants.DY_SHELF_BOUNDARY_SWITCH, map.DY_shelf_boundary_switch)
        shareUtil.putString(Constants.BAIDU_STAT_ID, map.baidu_stat_id)
        shareUtil.putString(Constants.DY_AD_SWITCH, if (isShowAd) "true" else "false")
        shareUtil.putString(Constants.DY_AD_NEW_STATISTICS_SWITCH, map.Dy_ad_new_statistics_switch)
        shareUtil.putString(Constants.DY_READPAGE_STATISTICS_SWITCH, map.Dy_readPage_statistics_switch)
        shareUtil.putString(Constants.DY_AD_READPAGE_SLIDE_SWITCH_NEW, map.Dy_ad_readPage_slide_switch_new)
        shareUtil.putString(Constants.DY_AD_OLD_REQUEST_SWITCH, map.DY_ad_old_request_switch)
        shareUtil.putString(Constants.DY_ADFREE_NEW_USER, map.DY_adfree_new_user)
        shareUtil.putString(Constants.DY_SPLASH_AD_SWITCH, map.DY_splash_ad_switch)
        shareUtil.putString(Constants.DY_SHELF_AD_SWITCH, map.DY_shelf_ad_switch)
        shareUtil.putString(Constants.BOOK_SHELF_STATE, map.book_shelf_state)
        shareUtil.putString(Constants.DY_SHELF_AD_FREQ, map.DY_shelf_ad_freq)
        shareUtil.putString(Constants.DY_PAGE_END_AD_SWITCH, map.DY_page_end_ad_switch)
        shareUtil.putString(Constants.DY_PAGE_END_AD_FREQ, map.DY_page_end_ad_freq)
        shareUtil.putString(Constants.DY_BOOK_END_AD_SWITCH, map.DY_book_end_ad_switch)
        shareUtil.putString(Constants.DY_REST_AD_SWITCH, map.DY_rest_ad_switch)
        shareUtil.putString(Constants.DY_REST_AD_SEC, map.DY_rest_ad_sec)
        shareUtil.putString(Constants.DY_PAGE_MIDDLE_AD_SWITCH, map.DY_page_middle_ad_switch)
        shareUtil.putString(Constants.DY_IS_NEW_READING_END, map.DY_is_new_reading_end)
        shareUtil.putString(Constants.DY_SWITCH_AD_SEC, map.DY_switch_ad_sec)
        shareUtil.putString(Constants.DY_ACTIVITED_SWITCH_AD, map.DY_activited_switch_ad)
        shareUtil.putString(Constants.DY_SWITCH_AD_CLOSE_SEC, map.DY_switch_ad_close_sec)
        shareUtil.putString(Constants.PUSH_KEY, map.push_key)
        shareUtil.putString(Constants.AD_LIMIT_TIME_DAY, map.ad_limit_time_day)
        shareUtil.putString(Constants.BAIDU_EXAMINE, map.baidu_examine)
        shareUtil.putString(Constants.USER_TRANSFER_FIRST, map.user_transfer_first)
        shareUtil.putString(Constants.USER_TRANSFER_SECOND, map.user_transfer_second)
        shareUtil.putString(Constants.DY_AD_NEW_REQUEST_DOMAIN_NAME, map.DY_ad_new_request_domain_name)
        shareUtil.putString(Constants.noNetReadNumber, map.noNetReadNumber)

        var downloadLimit = 0
        if (map.download_limit?.isNumeric() == true) {
            downloadLimit = Integer.parseInt(map.download_limit)
        }
        shareUtil.putInt(Constants.DOWNLOAD_LIMIT, downloadLimit)

        shareUtil.putString(Constants.NEW_APP_AD_SWITCH, if (isShowAd) "true" else map.new_app_ad_switch)

        if (shareUtil.getBoolean(Constants.START_PARAMS)) {
            shareUtil.putString(Constants.NOVEL_HOST, map.novel_host)
            shareUtil.putString(Constants.WEBVIEW_HOST, map.httpsWebView_host)
            shareUtil.putString(Constants.UNION_HOST, map.union_host)
            shareUtil.putString(Constants.CONTENT_HOST, map.content_host)
        }

    }

    private fun startRequestCDNDynamic() {
        var url = dynamicUrl
        if (url.isEmpty()) {
            isReloadDynamic = false
        } else {
            url = url.replace("{packageName}", AppUtils.getPackageName())
            AppLog.d("startRequestCDNDynamic", url)
            RequestAPI.requestCDNDynamicPar(url)
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(onNext = { parameter ->
                        checkResult(parameter)
                    }, onError = { throwable ->
                        throwable.printStackTrace()
                        startRequestCDNDynamic()
                    })
        }
    }

    private fun installParams() {

        insertRequestParams()

        initApi()

        Constants.DOWNLOAD_LIMIT_NUMBER = shareUtil.getInt(Constants.DOWNLOAD_LIMIT)

        setBaidu()

        setAd()

        setUserTransfer()

        setNoADTime()

        setNetWorkLimit()

        AppLog.d("um_param", " real param ==> " + this.toString())
    }


    private fun insertRequestParams() {
        Config.insertRequestAPIHost(shareUtil.getString(Constants.NOVEL_HOST))
        Config.insertWebViewHost(shareUtil.getString(Constants.WEBVIEW_HOST))
        Config.insertMicroAPIHost(shareUtil.getString(Constants.UNION_HOST))
        Config.insertContentAPIHost(shareUtil.getString(Constants.CONTENT_HOST))
    }

    private fun initApi() {
        ContentAPI.initMicroService()
        MicroAPI.initMicroService()
        RequestAPI.initializeDataRequestService()
    }

    private fun setBaidu() {
        // 设置百度统计信息
        val baiduStatId = shareUtil.getString(Constants.BAIDU_STAT_ID)
        if (baiduStatId.isNotEmpty()) {
            ReplaceConstants.getReplaceConstants().BAIDU_STAT_ID = baiduStatId
        }
        try {
            StatService.setAppKey(ReplaceConstants.getReplaceConstants().BAIDU_STAT_ID)
            StatService.setAppChannel(AppUtils.getChannelId())
            StatService.setSendLogStrategy(context, SendStrategyEnum.APP_START, 1, false)
            StatService.setOn(context, StatService.EXCEPTION_LOG)
            StatService.setDebugOn(Constants.DEVELOPER_MODE)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 设置百度广告计费id
        val pushKey = shareUtil.getString(Constants.PUSH_KEY)
        if (pushKey.isNotEmpty()) {
            ReplaceConstants.getReplaceConstants().PUSH_KEY = pushKey
        }
        try {
            PushManager.startWork(context, PushConstants.LOGIN_TYPE_API_KEY,
                    ReplaceConstants.getReplaceConstants().PUSH_KEY)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val baiduExamine = shareUtil.getString(Constants.BAIDU_EXAMINE)
        if (baiduExamine.isNotEmpty()) {
            val message = baiduExamine.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (message.isNotEmpty()) {
                if (message.isNotEmpty() && message[0].isNotEmpty()) {
                    val value = Integer.parseInt(message[0])
                    Constants.isBaiduExamine = value != 0
                    AppLog.e(TAG, "baiduExamine: " + message[0])
                    AppLog.e(TAG, "baiduExamine: " + Constants.isBaiduExamine)
                }

                if (message.size > 1 && message[1].isNotEmpty()) {
                    Constants.versionCode = Integer.valueOf(message[1])
                    AppLog.e(TAG, "baiduExamine: " + message[1])
                    AppLog.e(TAG, "baiduExamine: " + Constants.versionCode)
                }

                if (message.size > 2 && message[2].isNotEmpty()) {
                    val value = Integer.valueOf(message[2])
                    Constants.isHuaweiExamine = value != 0
                }
            }
        }
    }

    private fun setNetWorkLimit() {
        val networkLimit = shareUtil.getString(Constants.NETWORK_LIMIT)
        if (networkLimit.isNumeric()) {
            Constants.is_reading_network_limit = networkLimit.toInt() == 0
        }

    }

    private fun setUserTransfer() {
        val userTransferFirst = shareUtil.getString(Constants.USER_TRANSFER_FIRST)
        if (userTransferFirst.isNumeric()) {
            val firstLevel = userTransferFirst.toInt()
            Constants.is_user_transfer_first = firstLevel != 0
        } else {
            Constants.is_user_transfer_first = true
        }

        val userTransferSecond = shareUtil.getString(Constants.USER_TRANSFER_SECOND)
        if (userTransferSecond.isNumeric()) {
            val secondLevel = userTransferSecond.toInt()
            Constants.is_user_transfer_second = secondLevel != 0
        } else {
            Constants.is_user_transfer_second = false
        }
    }

    private fun setNoADTime() {

        val channelLimitList = ArrayList<String>()
        val dayLimitList = ArrayList<Int>()

        val channelLimit = shareUtil.getString(Constants.CHANNEL_LIMIT)
        if (channelLimit.isNotEmpty()) {
            val message = channelLimit.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            channelLimitList.addAll(Arrays.asList(*message))
        }

        val dayLimit = shareUtil.getString(Constants.DAY_LIMIT)
        if (dayLimit.isNotEmpty()) {
            val message = dayLimit.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            message.filter { it.isNumeric() }
                    .mapTo(dayLimitList) { it.toInt() }
        }

        // 隐藏广告期限
        var adLimitTimeDay = shareUtil.getString(Constants.AD_LIMIT_TIME_DAY)
        if (adLimitTimeDay.isNotEmpty()) {
            val channelID = AppUtils.getChannelId()
            AppLog.e(TAG, "channelLimitList: " + channelLimit)
            AppLog.e(TAG, "dayLimitList: " + dayLimit)

            val allChannelIndex = channelLimitList.indexOf("AllChannel")
            if (allChannelIndex != -1) {
                val allChannelLimit = dayLimitList[allChannelIndex]
                if (allChannelLimit != 0) {
                    AppLog.e(TAG, "all_channel_limit: " + allChannelLimit)
                    Constants.ad_limit_time_day = allChannelLimit
                } else {
                    if (channelLimitList.contains(channelID)) {
                        AppLog.e(TAG, "channelLimitList.contains: " + channelID)
                        val index = channelLimitList.indexOf(channelID)
                        AppLog.e(TAG, "dayLimitList: " + dayLimitList[index])
                        Constants.ad_limit_time_day = dayLimitList[index]
                    } else {
                        if (adLimitTimeDay.isEmpty()) {
                            adLimitTimeDay = "2"
                        }
                        if (adLimitTimeDay.isNumeric()) {
                            Constants.ad_limit_time_day = adLimitTimeDay.toInt()
                        }
                    }
                }
            }
            if (Constants.DEVELOPER_MODE) {
                AppLog.e(TAG, " ad_limit_time_day:" + Constants.ad_limit_time_day)
            }
        }
    }

    private fun setAd() {

        //广告总开关
        val dyAdSwitch = shareUtil.getString(Constants.DY_AD_SWITCH)
        if (dyAdSwitch.isNotEmpty()) {
            Constants.dy_ad_switch = dyAdSwitch.toBoolean()
        }

        //新的统计开关
        val dyAdNewStatisticsSwitch = shareUtil.getString(Constants.DY_AD_NEW_STATISTICS_SWITCH)
        if (dyAdNewStatisticsSwitch.isNotEmpty()) {
            Constants.dy_ad_new_statistics_switch = dyAdNewStatisticsSwitch.toBoolean()
        }

        //阅读页翻页统计开关
        val dyReadPageStatisticsSwitch = shareUtil.getString(Constants.DY_READPAGE_STATISTICS_SWITCH)
        if (dyReadPageStatisticsSwitch.isNotEmpty()) {
            Constants.dy_readPage_statistics_switch = dyReadPageStatisticsSwitch.toBoolean()
        }

        //阅读页上下翻页展示广告开关
        val dyAdReadPageSlideSwitchNew = shareUtil.getString(Constants.DY_AD_READPAGE_SLIDE_SWITCH_NEW)
        if (dyAdReadPageSlideSwitchNew.isNotEmpty()) {
            Constants.dy_ad_readPage_slide_switch_new = dyAdReadPageSlideSwitchNew.toBoolean()
        }

        //老的广告统计开关
        val dyAdOldRequestSwitch = shareUtil.getString(Constants.DY_AD_OLD_REQUEST_SWITCH)
        if (dyAdOldRequestSwitch.isNotEmpty()) {
            Constants.dy_ad_old_request_switch = dyAdOldRequestSwitch.toBoolean()
        }

        //新的用户广告请求接口
        val dyAdNewRequestDomainName = shareUtil.getString(Constants.DY_AD_NEW_REQUEST_DOMAIN_NAME)
        if (dyAdNewRequestDomainName.isNotEmpty()) {
            Constants.AD_DATA_Collect = dyAdNewRequestDomainName
        }

        //X小时内新用户不显示广告设置
        val dyAdFreeNewUser = shareUtil.getString(Constants.DY_ADFREE_NEW_USER)
        if (dyAdFreeNewUser.isNumeric()) {
            Constants.ad_limit_time_day = dyAdFreeNewUser.toInt()
        }

        //开屏页开关
        val dySplashAdSwitch = shareUtil.getString(Constants.DY_SPLASH_AD_SWITCH)
        if (dySplashAdSwitch.isNotEmpty()) {
            Constants.dy_splash_ad_switch = dySplashAdSwitch.toBoolean()
        }

        //书架页开关
        val dyShelfAdSwitch = shareUtil.getString(Constants.DY_SHELF_AD_SWITCH)
        if (dyShelfAdSwitch.isNotEmpty()) {
            Constants.dy_shelf_ad_switch = dyShelfAdSwitch.toBoolean()
        }

        //书架悬浮广告开关
        val dyShelfBoundarySwitch = shareUtil.getString(Constants.DY_SHELF_BOUNDARY_SWITCH)
        if (dyShelfBoundarySwitch.isNotEmpty()) {
            Constants.dy_shelf_boundary_switch = dyShelfBoundarySwitch.toBoolean()
        }

        /*
          0-关闭书架页广告位；两种形式都不开启
          1-开启书架页广告位A样式:顶部横幅书架页广告
          2-开启书架页广告位B样式：九宫格原生书架页广告
          3-开启书架页广告位两种样式
           九宫格书架页广告显示类型切换开关
         */
        val bookShelfState = shareUtil.getString(Constants.BOOK_SHELF_STATE)
        if (bookShelfState.isNumeric()) {
            Constants.book_shelf_state = bookShelfState.toInt()
        }

        //书架页广告间隔频率设置
        val dyShelfAdFreq = shareUtil.getString(Constants.DY_SHELF_AD_FREQ)
        if (dyShelfAdFreq.isNumeric()) {
            Constants.dy_shelf_ad_freq = dyShelfAdFreq.toInt()
        }

        //章节末开关
        val dyPageEndAdSwitch = shareUtil.getString(Constants.DY_PAGE_END_AD_SWITCH)
        if (dyPageEndAdSwitch.isNotEmpty()) {
            Constants.dy_page_end_ad_switch = dyPageEndAdSwitch.toBoolean()
        }

        //章节末广告间隔频率设置
        val dyPageEndAdFreq = shareUtil.getString(Constants.DY_PAGE_END_AD_FREQ)
        if (dyPageEndAdFreq.isNumeric()) {
            Constants.dy_page_end_ad_freq = dyPageEndAdFreq.toInt()
        }

        //书末广告开关
        val dyBookEndAdSwitch = shareUtil.getString(Constants.DY_BOOK_END_AD_SWITCH)
        if (dyBookEndAdSwitch.isNotEmpty()) {
            Constants.dy_book_end_ad_switch = dyBookEndAdSwitch.toBoolean()
        }

        //休息页广告开关
        val dyRestAdSwitch = shareUtil.getString(Constants.DY_REST_AD_SWITCH)
        if (dyRestAdSwitch.isNotEmpty()) {
            Constants.dy_rest_ad_switch = dyRestAdSwitch.toBoolean()
        }

        //休息页广告休息时间设置
        val dyRestAdSec = shareUtil.getString(Constants.DY_REST_AD_SEC)
        if (dyRestAdSec.isNumeric()) {
            Constants.read_rest_time = dyRestAdSec.toInt() * 60 * 1000
        }

        //章节间开关
        val dyPageMiddleAdSwitch = shareUtil.getString(Constants.DY_PAGE_MIDDLE_AD_SWITCH)
        if (dyPageMiddleAdSwitch.isNotEmpty()) {
            Constants.dy_page_middle_ad_switch = dyPageMiddleAdSwitch.toBoolean()
        }

        //切屏广告的开关
        val isShowSwitchSplashAd = shareUtil.getString(Constants.DY_ACTIVITED_SWITCH_AD)
        if (isShowSwitchSplashAd.isNotEmpty()) {
            Constants.isShowSwitchSplashAd = isShowSwitchSplashAd.toBoolean()
        }

        //切屏广告的间隔秒数
        val switchSplashAdSec = shareUtil.getString(Constants.DY_SWITCH_AD_SEC)
        if (switchSplashAdSec.isNumeric()) {
            Constants.switchSplash_ad_sec = switchSplashAdSec.toInt()
        }

        //切屏广告关闭按钮出现的时间
        val switchSplashAdCloseSec = shareUtil.getString(Constants.DY_SWITCH_AD_CLOSE_SEC)
        if (switchSplashAdCloseSec.isNotEmpty()) {
            Constants.show_switchSplash_ad_close = switchSplashAdCloseSec.toInt()
        }
        //章节末广告是否新版
        val dyIsNewReadingEnd = shareUtil.getString(Constants.DY_IS_NEW_READING_END)
        if (dyIsNewReadingEnd.isNotEmpty()) {
            Constants.dy_is_new_reading_end = dyIsNewReadingEnd.toBoolean()
        }

        //新app广告开关
        val newAppAdSwitch = shareUtil.getString(Constants.NEW_APP_AD_SWITCH)
        if (newAppAdSwitch.isNotEmpty()) {
            Constants.new_app_ad_switch = newAppAdSwitch.toBoolean()
        }

    }

    companion object {

        var TAG = DynamicParameter::class.java.simpleName

        @JvmStatic
        var isReloadDynamic = false

        private var mDynamicUrl = RequestService.DYNAMIC_PARAMETERS
    }
}
