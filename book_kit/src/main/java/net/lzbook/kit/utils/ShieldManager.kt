package net.lzbook.kit.utils

import android.content.Context
import android.text.TextUtils
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.ding.basic.bean.Interest
import com.ding.basic.config.ParameterConfig
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.statistics.DyStatService
import com.dy.media.MediaConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.book.LoadDataManager
import net.lzbook.kit.utils.logger.AppLog

/**
 * Desc 屏蔽管理类
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/11/20 0019 15:53
 */
class ShieldManager(private val context: Context) {

    //声明mLocationOption对象
    var mLocationOption: AMapLocationClientOption? = null

    //声明AMapLocationClient类对象
    var mLocationClient: AMapLocationClient? = null

    private var mLocationListener: AMapLocationListener = AMapLocationListener { aMapLocation ->
        if (aMapLocation != null) {
            if (aMapLocation.errorCode == 0) {
                //定位成功回调信息，设置相关消息
                Constants.latitude = aMapLocation.latitude
                ParameterConfig.latitude = aMapLocation.altitude.toString()

                Constants.longitude = aMapLocation.longitude
                ParameterConfig.longitude = aMapLocation.longitude.toString()

                Constants.adCode = aMapLocation.adCode
                ParameterConfig.areaCode = aMapLocation.adCode

                Constants.adCityInfo = aMapLocation.city
                ParameterConfig.city = aMapLocation.city

                Constants.cityCode = aMapLocation.cityCode
                ParameterConfig.cityCode = aMapLocation.cityCode

                Constants.adLocationDetail = (aMapLocation.district + " "
                        + aMapLocation.street + " "
                        + aMapLocation.streetNum + " "
                        + "(" + Constants.longitude + ", " + Constants.latitude + ")")

                ParameterConfig.locationDetail = (aMapLocation.district + " "
                        + aMapLocation.street + " "
                        + aMapLocation.streetNum + " "
                        + "(" + Constants.longitude + ", " + Constants.latitude + ")")


                val latitude = Constants.latitude.toString()
                val longitude = Constants.longitude.toString()

                if (!Constants.isHideAD && MediaConfig.getConfig() != null) {
                    if (!TextUtils.isEmpty(Constants.cityCode)) {
                        MediaConfig.setCityCode(Constants.cityCode)
                    }

                    MediaConfig.setCityName(Constants.adCityInfo)

                    if (!TextUtils.isEmpty(latitude)) {
                        MediaConfig.setLatitude(java.lang.Float.valueOf(latitude))
                    }

                    if (!TextUtils.isEmpty(longitude)) {
                        MediaConfig.setLongitude(java.lang.Float.valueOf(longitude))
                    }
                    MediaConfig.setAd_userid(OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()))
                    MediaConfig.setChannel_code(AppUtils.getChannelId())
                }

                stopAchieveUserLocation()
                // 统计SDK初始化位置信息
                DyStatService.setLocationInfo(Constants.longitude, Constants.latitude, Constants.adCityInfo, Constants.cityCode, Constants.adLocationDetail)
            } else {
                AppLog.e("AmapError", "location Error, ErrCode:" + aMapLocation.errorCode + ", errInfo:" + aMapLocation.errorInfo)
            }
        }
        try {
            if (NetWorkUtils.getNetWorkType(BaseBookApplication.getGlobalContext()) != NetWorkUtils.NETWORK_NONE) {
                initBook()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initBook() {
        val loadDataManager = LoadDataManager(context)
        if (!SPUtils.getDefaultSharedBoolean(SPKey.ADD_DEFAULT_BOOKS, false)) {
            // 首次安装新用户添加默认书籍
            val hasSelectInterest = SPUtils.getDefaultSharedInt(SPKey.HAS_SELECT_INTEREST, 0)
            when (hasSelectInterest) {
                -1 -> // 选择兴趣时，选择跳过
                    loadDataManager.addDefaultBooksWithInterest("", "")
                0 -> // 无选兴趣功能，执行原有逻辑
                    loadDataManager.addDefaultBooks(ParameterConfig.GENDER_TYPE)
                1 -> {
                    // 获取用户选择的兴趣
                    val list = Gson().fromJson<List<Interest>>(
                            SPUtils.getDefaultSharedString(SPKey.SELECTED_INTEREST_DATA, ""),
                            object : TypeToken<List<Interest>>() {

                            }.type)
                    val labelOne = StringBuilder()
                    val labelTwo = StringBuilder()
                    for (item in list) {
                        if (item.type == 1) {
                            labelOne.append(item.name).append(",")
                        } else {
                            labelTwo.append(item.name).append(",")
                        }
                    }
                    // 移除最后 逗号
                    if (labelOne.isNotEmpty() && labelOne.lastIndexOf(",") == labelOne.length - 1) {
                        labelOne.deleteCharAt(labelOne.length - 1)
                    }
                    if (labelTwo.isNotEmpty() && labelTwo.lastIndexOf(",") == labelTwo.length - 1) {
                        labelTwo.deleteCharAt(labelTwo.length - 1)
                    }

                    loadDataManager.addDefaultBooksWithInterest(labelOne.toString(),
                            labelTwo.toString())
                }
            }
        }
    }

    fun startAchieveUserLocation() {
        //初始化定位
        mLocationClient = AMapLocationClient(context)
        //设置定位回调监听
        mLocationClient?.setLocationListener(mLocationListener)

        //初始化定位参数
        mLocationOption = AMapLocationClientOption()
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption?.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption?.isNeedAddress = true
        //设置是否只定位一次,默认为false
        mLocationOption?.isOnceLocation = false
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption?.isWifiActiveScan = true
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption?.isMockEnable = true
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption?.interval = (30 * 60 * 1000).toLong()
        //给定位客户端对象设置定位参数
        mLocationClient?.setLocationOption(mLocationOption)
        //启动定位
        mLocationClient?.startLocation()
    }

    private fun stopAchieveUserLocation() {
        mLocationClient?.onDestroy()
    }

}