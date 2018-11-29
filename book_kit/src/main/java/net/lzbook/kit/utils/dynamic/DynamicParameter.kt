package net.lzbook.kit.utils.dynamic

import android.content.Context
import com.baidu.mobstat.StatService
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.AdControlByChannelBean
import com.ding.basic.bean.BasicResult
import com.ding.basic.bean.Map
import com.ding.basic.bean.Parameter
import com.ding.basic.net.Config
import com.ding.basic.net.RequestSubscriber
import com.ding.basic.net.api.ContentAPI
import com.ding.basic.net.api.MicroAPI
import com.ding.basic.net.api.RequestAPI
import com.ding.basic.net.api.service.RequestService
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.orhanobut.logger.Logger
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.service.DynamicService
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.isNumeric
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.logger.AppLog
import net.lzbook.kit.utils.web.WebResourceCache
import okhttp3.HttpUrl
import java.util.*

class DynamicParameter(private val context: Context) {

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


    private var mCurVersion: Int = -1
    private var mReqVersion: Int = -1

    fun setDynamicParameter() {
        prepareCheck()

        installParams()

        requestAdControl()

        requestCheck()

        DynamicService.startDynaService(BaseBookApplication.getGlobalContext())

    }

    /**
     * 为了覆盖升级的用户能够强制拉取一次动态参数
     */
    private fun prepareCheck() {
        val versionCode = AppUtils.getVersionCode().toString()
        val isShouldCheck = SPUtils.getDefaultSharedBoolean(versionCode + SPKey.CHECK_DYNAMIC, true)
        if (isShouldCheck) {
            SPUtils.editDefaultShared {
                putInt(SPKey.DYNAMIC_VERSION, -1)
                putBoolean(versionCode + SPKey.CHECK_DYNAMIC, false)
            }
        }
    }


    fun requestAdControl() {
        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestAdControlDynamic(
                object : RequestSubscriber<AdControlByChannelBean>() {
                    override fun requestResult(result: AdControlByChannelBean?) {
                        if (result != null && result.respCode == "20000" && result.data != null && result.data!!.isNotEmpty()) {
                            checkAdControlResult(result)
                        } else {
                            saveAdControlParams(false, null)
                        }
                    }

                    override fun requestError(message: String) {
                        Logger.e("请求广告动态参数异常！")
                        saveAdControlParams(false, null)
                    }

                    override fun requestComplete() {

                    }
                })

    }

    /**
     * 进行动态参数版本校验
     */
    fun requestCheck() {
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .requestDynamicCheck(object : RequestSubscriber<BasicResult<Int>>() {
                    override fun requestResult(result: BasicResult<Int>?) {
                        if (result?.data != null) {
                            mReqVersion = result.data!!
                            mCurVersion = SPUtils.getDefaultSharedInt(SPKey.DYNAMIC_VERSION, -1)
                            AppLog.d("requestDynamicCheck", "mReqVersion = $mReqVersion\nmCurVersion = $mCurVersion")
                            if (mCurVersion != mReqVersion) {
                                requestContent()
                            }
                        }
                    }

                    override fun requestError(message: String) {
                        Logger.e("请求动态参数校验接口异常！")
                    }

                })
    }


    /**
     * 请求动态参数
     */
    fun requestContent() {
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

    private fun checkAdControlResult(adControlBean: AdControlByChannelBean) {
        var hasAdContol = false //
        adControlBean.data?.forEach {
            if (it.channelId?.toLowerCase() == AppUtils.getChannelId().toLowerCase() && it.packageName == AppUtils.getPackageName() && it.version == AppUtils.getVersionName()) {
                hasAdContol = true
                saveAdControlParams(true, it)
            }
        }
        if (!hasAdContol) {
            saveAdControlParams(false, null)
        }
    }

    private fun saveAdControlParams(hasAdContol: Boolean, bean: AdControlByChannelBean.DataBean?) {

        if (hasAdContol) {
            putDynamicString(SPKey.AD_CONTROL_STATUS, bean?.status)
            putDynamicString(SPKey.AD_CONTROL_PGK, bean?.packageName)
            putDynamicString(SPKey.AD_CONTROL_CHANNELID, bean?.channelId)
            putDynamicString(SPKey.AD_CONTROL_VERSION, bean?.version)
            putDynamicString(SPKey.AD_CONTROL_ADTYPE, bean?.adSpaceType)
        } else {
            putDynamicString(SPKey.AD_CONTROL_STATUS, "0")
            putDynamicString(SPKey.AD_CONTROL_PGK, "")
            putDynamicString(SPKey.AD_CONTROL_CHANNELID, "")
            putDynamicString(SPKey.AD_CONTROL_VERSION, "")
            putDynamicString(SPKey.AD_CONTROL_ADTYPE, "")
        }
        setAdControl()
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

        putDynamicString(SPKey.CHANNEL_LIMIT, map.channel_limit)
        putDynamicString(SPKey.RECOMMEND_BOOKCOVER, map.recommend_bookcover)
        putDynamicString(SPKey.DAY_LIMIT, map.day_limit)
        putDynamicString(SPKey.DY_SHELF_BOUNDARY_SWITCH, map.DY_shelf_boundary_switch)
        putDynamicString(SPKey.BAIDU_STAT_ID, map.baidu_stat_id)
        putDynamicString(SPKey.DY_AD_SWITCH, if (isShowAd) "true" else map.DY_ad_switch)
        putDynamicString(SPKey.DY_AD_NEW_STATISTICS_SWITCH, map.Dy_ad_new_statistics_switch)
        putDynamicString(SPKey.DY_READPAGE_STATISTICS_SWITCH, map.Dy_readPage_statistics_switch)
        putDynamicString(SPKey.DY_AD_READPAGE_SLIDE_SWITCH_NEW, map.Dy_ad_readPage_slide_switch_new)
        putDynamicString(SPKey.DY_AD_OLD_REQUEST_SWITCH, map.DY_ad_old_request_switch)
        putDynamicString(SPKey.DY_ADFREE_NEW_USER, map.DY_adfree_new_user)
        putDynamicString(SPKey.DY_SPLASH_AD_SWITCH, map.DY_splash_ad_switch)
        putDynamicString(SPKey.DY_SHELF_AD_SWITCH, map.DY_shelf_ad_switch)
        putDynamicString(SPKey.BOOK_SHELF_STATE, map.book_shelf_state)
        putDynamicString(SPKey.DY_SHELF_AD_FREQ, map.DY_shelf_ad_freq)
        putDynamicString(SPKey.DY_PAGE_END_AD_SWITCH, map.DY_page_end_ad_switch)
        putDynamicString(SPKey.DY_PAGE_END_AD_FREQ, map.DY_page_end_ad_freq)
        putDynamicString(SPKey.DY_BOOK_END_AD_SWITCH, map.DY_book_end_ad_switch)
        putDynamicString(SPKey.DY_REST_AD_SWITCH, map.DY_rest_ad_switch)
        putDynamicString(SPKey.DY_REST_AD_SEC, map.DY_rest_ad_sec)
        putDynamicString(SPKey.DY_PAGE_MIDDLE_AD_SWITCH, map.DY_page_middle_ad_switch)
        putDynamicString(SPKey.DY_IS_NEW_READING_END, map.DY_is_new_reading_end)
        putDynamicString(SPKey.DY_SWITCH_AD_SEC, map.DY_switch_ad_sec)
        putDynamicString(SPKey.DY_ACTIVITED_SWITCH_AD, map.DY_activited_switch_ad)
        putDynamicString(SPKey.DY_SWITCH_AD_CLOSE_SEC, map.DY_switch_ad_close_sec)
        putDynamicString(SPKey.PUSH_KEY, map.push_key)
        putDynamicString(SPKey.AD_LIMIT_TIME_DAY, map.ad_limit_time_day)
        putDynamicString(SPKey.BAIDU_EXAMINE, map.baidu_examine)
        putDynamicString(SPKey.USER_TRANSFER_FIRST, map.user_transfer_first)
        putDynamicString(SPKey.USER_TRANSFER_SECOND, map.user_transfer_second)
        putDynamicString(SPKey.DY_AD_NEW_REQUEST_DOMAIN_NAME, map.DY_ad_new_request_domain_name)
        putDynamicString(SPKey.NO_NET_READ_NUMBER, map.noNetReadNumber)

        putDynamicString(SPKey.NEW_APP_AD_SWITCH, if (isShowAd) "true" else map.new_app_ad_switch)

        if (SPUtils.getOnlineConfigSharedBoolean(SPKey.DEBUG_DYNAMICA_STATE, true)) {

            SPUtils.insertPrivateSharedString(SPKey.MICRO_AUTH_HOST, map.DY_micro_host)
            SPUtils.insertPrivateSharedString(SPKey.CONTENT_AUTH_HOST, map.DY_micro_chapter_host)

            putDynamicString(SPKey.NOVEL_HOST, map.novel_host?.parseUrl())
            putDynamicString(SPKey.WEBVIEW_HOST, map.httpsWebView_host?.parseUrl())
        }

        putDynamicString(SPKey.USER_TAG_HOST, map.user_tag_host?.parseUrl())

        putDynamicString(SPKey.DY_STATIC_RESOURCE_RULE, map.DY_static_resource_rule)

        putDynamicString(SPKey.DY_WEB_STATIC_RESOURCES, map.DY_web_static_resources)

        putDynamicString(SPKey.SHARE_SWITCH, map.share_switch_enable)

        // 保存动态参数校验版本号
        if (mCurVersion < mReqVersion) {
            SPUtils.putDefaultSharedInt(SPKey.DYNAMIC_VERSION, mReqVersion)
            AppLog.d("requestDynamicCheck", "mReqVersion = " + mReqVersion)
        }

    }

    private fun putDynamicString(key: String, value: String?) {
        if (value == null) return
        SPUtils.putOnlineConfigSharedString(key, value)
    }

    private fun startRequestCDNDynamic() {
        var url = dynamicUrl
        if (url.isEmpty()) {
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

        setBaidu()

        setAdControl()

        setAd()

        setUserTransfer()

        setNoADTime()

        setNetWorkLimit()

        setShareSwitch()

        AppLog.d("um_param", " real param ==> " + this.toString())
    }

    private fun insertRequestParams() {
        MicroAPI.microHost = SPUtils.loadPrivateSharedString(SPKey.MICRO_AUTH_HOST)
        ContentAPI.contentHost = SPUtils.loadPrivateSharedString(SPKey.CONTENT_AUTH_HOST)

        Config.insertRequestAPIHost(SPUtils.getOnlineConfigSharedString(SPKey.NOVEL_HOST))
        Config.insertWebViewHost(SPUtils.getOnlineConfigSharedString(SPKey.WEBVIEW_HOST))
        Config.insertUserTagHost(SPUtils.getOnlineConfigSharedString(SPKey.USER_TAG_HOST))
    }

    private fun initApi() {
        MicroAPI.initMicroService()
        ContentAPI.initContentService()
        RequestAPI.initializeDataRequestService()
    }

    private fun setBaidu() {
        // 设置百度统计信息
        val baiduStatId = SPUtils.getOnlineConfigSharedString(SPKey.BAIDU_STAT_ID)
        if (baiduStatId.isNotEmpty()) {
            ReplaceConstants.getReplaceConstants().BAIDU_STAT_ID = baiduStatId
        }
        try {
            StatService.setAppKey(ReplaceConstants.getReplaceConstants().BAIDU_STAT_ID)
            StatService.setAppChannel(AppUtils.getChannelId())
            StatService.start(context)
            StatService.setOn(context, StatService.EXCEPTION_LOG)
            StatService.setDebugOn(Constants.DEVELOPER_MODE)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 设置百度广告计费id
        val pushKey = SPUtils.getOnlineConfigSharedString(SPKey.PUSH_KEY)
        if (pushKey.isNotEmpty()) {
            ReplaceConstants.getReplaceConstants().PUSH_KEY = pushKey
        }

        val baiduExamine = SPUtils.getOnlineConfigSharedString(SPKey.BAIDU_EXAMINE)
        if (baiduExamine.isNotEmpty()) {
            val message = baiduExamine.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (message.isNotEmpty()) {
                if (message.isNotEmpty() && message[0].isNotEmpty()) {
                    val value = Integer.parseInt(message[0])
                    Constants.isBaiduExamine = value != 0
                    loge("baiduExamine: " + message[0])
                    loge("baiduExamine: " + Constants.isBaiduExamine)
                }

                if (message.size > 1 && message[1].isNotEmpty()) {
                    Constants.versionCode = Integer.valueOf(message[1])
                    loge("baiduExamine: " + message[1])
                    loge("baiduExamine: " + Constants.versionCode)
                }

                if (message.size > 2 && message[2].isNotEmpty()) {
                    val value = Integer.valueOf(message[2])
                    Constants.isHuaweiExamine = value != 0
                }
            }
        }
    }

    private fun setNetWorkLimit() {
        val networkLimit = SPUtils.getOnlineConfigSharedString(Constants.NETWORK_LIMIT)
        if (networkLimit.isNumeric()) {
            Constants.is_reading_network_limit = networkLimit.toInt() == 0
        }

    }

    private fun setUserTransfer() {
        val userTransferFirst = SPUtils.getOnlineConfigSharedString(SPKey.USER_TRANSFER_FIRST)
        if (userTransferFirst.isNumeric()) {
            val firstLevel = userTransferFirst.toInt()
            Constants.is_user_transfer_first = firstLevel != 0
        } else {
            Constants.is_user_transfer_first = true
        }

        val userTransferSecond = SPUtils.getOnlineConfigSharedString(SPKey.USER_TRANSFER_SECOND)
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

        val channelLimit = SPUtils.getOnlineConfigSharedString(SPKey.CHANNEL_LIMIT)
        if (channelLimit.isNotEmpty()) {
            val message = channelLimit.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            channelLimitList.addAll(Arrays.asList(*message))
        }

        val dayLimit = SPUtils.getOnlineConfigSharedString(SPKey.DAY_LIMIT)
        if (dayLimit.isNotEmpty()) {
            val message = dayLimit.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            message.filter { it.isNumeric() }
                    .mapTo(dayLimitList) { it.toInt() }
        }

        // 隐藏广告期限
        var adLimitTimeDay = SPUtils.getOnlineConfigSharedString(SPKey.AD_LIMIT_TIME_DAY)
        if (adLimitTimeDay.isNotEmpty()) {
            val channelID = AppUtils.getChannelId()
            loge("channelLimitList: $channelLimit")
            loge("dayLimitList: $dayLimit")

            val allChannelIndex = channelLimitList.indexOf("AllChannel")
            if (allChannelIndex != -1) {
                val allChannelLimit = dayLimitList[allChannelIndex]
                if (allChannelLimit != 0) {
                    loge("all_channel_limit: $allChannelLimit")
                    Constants.ad_limit_time_day = allChannelLimit
                } else {
                    if (channelLimitList.contains(channelID)) {
                        loge("channelLimitList.contains: $channelID")
                        val index = channelLimitList.indexOf(channelID)
                        loge("dayLimitList: ${dayLimitList[index]}")
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
                loge(" ad_limit_time_day:${Constants.ad_limit_time_day}")
            }
        }
    }

    private fun setAdControl() {
        //广告渠道控制 总开关
        val adConstrolStatus = SPUtils.getOnlineConfigSharedString(SPKey.AD_CONTROL_STATUS, "0")
        if (adConstrolStatus.isNotEmpty()) {
            Constants.ad_control_status = adConstrolStatus
        }

        //广告渠道控制 包名
        val adConstrolPkg = SPUtils.getOnlineConfigSharedString(SPKey.AD_CONTROL_PGK)
        if (adConstrolPkg.isNotEmpty()) {
            Constants.ad_control_pkg = adConstrolPkg
        }

        //广告渠道控制 渠道号
        val adConstrolChannelId = SPUtils.getOnlineConfigSharedString(SPKey.AD_CONTROL_CHANNELID)
        if (adConstrolChannelId.isNotEmpty()) {
            Constants.ad_control_channelId = adConstrolChannelId
        }

        //广告渠道控制 版本号
        val adConstrolVersion = SPUtils.getOnlineConfigSharedString(SPKey.AD_CONTROL_VERSION)
        if (adConstrolVersion.isNotEmpty()) {
            Constants.ad_control_version = adConstrolVersion
        }

        //广告渠道控制 广告位
        val adConstrolAdType = SPUtils.getOnlineConfigSharedString(SPKey.AD_CONTROL_ADTYPE)
        if (adConstrolAdType.isNotEmpty()) {
            Constants.ad_control_adTpye = adConstrolAdType
        }
    }

    private fun setAd() {

        //广告总开关
        val dyAdSwitch = SPUtils.getOnlineConfigSharedString(SPKey.DY_AD_SWITCH)
        if (dyAdSwitch.isNotEmpty()) {
            Constants.dy_ad_switch = dyAdSwitch.toBoolean()
        }

        //新的统计开关
        val dyAdNewStatisticsSwitch = SPUtils.getOnlineConfigSharedString(SPKey.DY_AD_NEW_STATISTICS_SWITCH)
        if (dyAdNewStatisticsSwitch.isNotEmpty()) {
            Constants.dy_ad_new_statistics_switch = dyAdNewStatisticsSwitch.toBoolean()
        }

        //阅读页翻页统计开关
        val dyReadPageStatisticsSwitch = SPUtils.getOnlineConfigSharedString(SPKey.DY_READPAGE_STATISTICS_SWITCH)
        if (dyReadPageStatisticsSwitch.isNotEmpty()) {
            Constants.dy_readPage_statistics_switch = dyReadPageStatisticsSwitch.toBoolean()
        }

        //阅读页上下翻页展示广告开关
        val dyAdReadPageSlideSwitchNew = SPUtils.getOnlineConfigSharedString(SPKey.DY_AD_READPAGE_SLIDE_SWITCH_NEW)
        if (dyAdReadPageSlideSwitchNew.isNotEmpty()) {
            Constants.dy_ad_readPage_slide_switch_new = dyAdReadPageSlideSwitchNew.toBoolean()
        }

        //老的广告统计开关
        val dyAdOldRequestSwitch = SPUtils.getOnlineConfigSharedString(SPKey.DY_AD_OLD_REQUEST_SWITCH)
        if (dyAdOldRequestSwitch.isNotEmpty()) {
            Constants.dy_ad_old_request_switch = dyAdOldRequestSwitch.toBoolean()
        }

        //新的用户广告请求接口
        val dyAdNewRequestDomainName = SPUtils.getOnlineConfigSharedString(SPKey.DY_AD_NEW_REQUEST_DOMAIN_NAME)
        if (dyAdNewRequestDomainName.isNotEmpty()) {
            Constants.AD_DATA_Collect = dyAdNewRequestDomainName
        }

        //X小时内新用户不显示广告设置
        val dyAdFreeNewUser = SPUtils.getOnlineConfigSharedString(SPKey.DY_ADFREE_NEW_USER)
        if (dyAdFreeNewUser.isNumeric()) {
            Constants.ad_limit_time_day = dyAdFreeNewUser.toInt()
        }

        //开屏页开关
        val dySplashAdSwitch = SPUtils.getOnlineConfigSharedString(SPKey.DY_SPLASH_AD_SWITCH)
        if (dySplashAdSwitch.isNotEmpty()) {
            Constants.dy_splash_ad_switch = dySplashAdSwitch.toBoolean()
        }

        //书架页开关
        val dyShelfAdSwitch = SPUtils.getOnlineConfigSharedString(SPKey.DY_SHELF_AD_SWITCH)
        if (dyShelfAdSwitch.isNotEmpty()) {
            Constants.dy_shelf_ad_switch = dyShelfAdSwitch.toBoolean()
        }

        //书架悬浮广告开关
        val dyShelfBoundarySwitch = SPUtils.getOnlineConfigSharedString(SPKey.DY_SHELF_BOUNDARY_SWITCH)
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
        val bookShelfState = SPUtils.getOnlineConfigSharedString(SPKey.BOOK_SHELF_STATE)
        if (bookShelfState.isNumeric()) {
            Constants.book_shelf_state = bookShelfState.toInt()
        }

        //书架页广告间隔频率设置
        val dyShelfAdFreq = SPUtils.getOnlineConfigSharedString(SPKey.DY_SHELF_AD_FREQ)
        if (dyShelfAdFreq.isNumeric()) {
            Constants.dy_shelf_ad_freq = dyShelfAdFreq.toInt()
        }

        //章节末开关
        val dyPageEndAdSwitch = SPUtils.getOnlineConfigSharedString(SPKey.DY_PAGE_END_AD_SWITCH)
        if (dyPageEndAdSwitch.isNotEmpty()) {
            Constants.dy_page_end_ad_switch = dyPageEndAdSwitch.toBoolean()
        }

        //章节末广告间隔频率设置
        val dyPageEndAdFreq = SPUtils.getOnlineConfigSharedString(SPKey.DY_PAGE_END_AD_FREQ)
        if (dyPageEndAdFreq.isNumeric()) {
            Constants.dy_page_end_ad_freq = dyPageEndAdFreq.toInt()
        }

        //书末广告开关
        val dyBookEndAdSwitch = SPUtils.getOnlineConfigSharedString(SPKey.DY_BOOK_END_AD_SWITCH)
        if (dyBookEndAdSwitch.isNotEmpty()) {
            Constants.dy_book_end_ad_switch = dyBookEndAdSwitch.toBoolean()
        }

        //休息页广告开关
        val dyRestAdSwitch = SPUtils.getOnlineConfigSharedString(SPKey.DY_REST_AD_SWITCH)
        if (dyRestAdSwitch.isNotEmpty()) {
            Constants.dy_rest_ad_switch = dyRestAdSwitch.toBoolean()
        }

        //休息页广告休息时间设置
        val dyRestAdSec = SPUtils.getOnlineConfigSharedString(SPKey.DY_REST_AD_SEC)
        if (dyRestAdSec.isNumeric()) {
            Constants.read_rest_time = dyRestAdSec.toInt() * 60 * 1000
        }

        //章节间开关
        val dyPageMiddleAdSwitch = SPUtils.getOnlineConfigSharedString(SPKey.DY_PAGE_MIDDLE_AD_SWITCH)
        if (dyPageMiddleAdSwitch.isNotEmpty()) {
            Constants.dy_page_middle_ad_switch = dyPageMiddleAdSwitch.toBoolean()
        }

        //切屏广告的开关
        val isShowSwitchSplashAd = SPUtils.getOnlineConfigSharedString(SPKey.DY_ACTIVITED_SWITCH_AD)
        if (isShowSwitchSplashAd.isNotEmpty()) {
            Constants.isShowSwitchSplashAd = isShowSwitchSplashAd.toBoolean()
        }

        //切屏广告的间隔秒数
        val switchSplashAdSec = SPUtils.getOnlineConfigSharedString(SPKey.DY_SWITCH_AD_SEC)
        if (switchSplashAdSec.isNumeric()) {
            Constants.switchSplash_ad_sec = switchSplashAdSec.toInt()
        }

        //切屏广告关闭按钮出现的时间
        val switchSplashAdCloseSec = SPUtils.getOnlineConfigSharedString(SPKey.DY_SWITCH_AD_CLOSE_SEC)
        if (switchSplashAdCloseSec.isNotEmpty()) {
            Constants.show_switchSplash_ad_close = switchSplashAdCloseSec.toInt()
        }
        //章节末广告是否新版
        val dyIsNewReadingEnd = SPUtils.getOnlineConfigSharedString(SPKey.DY_IS_NEW_READING_END)
        if (dyIsNewReadingEnd.isNotEmpty()) {
            Constants.dy_is_new_reading_end = dyIsNewReadingEnd.toBoolean()
        }

        //新app广告开关
        val newAppAdSwitch = SPUtils.getOnlineConfigSharedString(SPKey.NEW_APP_AD_SWITCH)
        if (newAppAdSwitch.isNotEmpty()) {
            Constants.new_app_ad_switch = newAppAdSwitch.toBoolean()
        }

    }

    private fun String.parseUrl(): String? {
        val httpUrl = HttpUrl.parse(this)
        return if (httpUrl != null) {
            this
        } else {
            null
        }
    }

    private fun setShareSwitch() {
        val shareSwitchEnable = SPUtils.getOnlineConfigSharedString(SPKey.SHARE_SWITCH)
        if (shareSwitchEnable.isNotEmpty()) {
            Constants.SHARE_SWITCH_ENABLE = shareSwitchEnable.toBoolean()
        }
    }

    companion object {

        var TAG = DynamicParameter::class.java.simpleName

    }
}
