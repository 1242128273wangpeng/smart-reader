package net.lzbook.kit.utils

import android.content.Context
import android.text.TextUtils

import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.ding.basic.config.ParameterConfig
import com.dy.media.MediaConfig

import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.book.LoadDataManager
import net.lzbook.kit.utils.logger.AppLog
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils

/**
 * 屏蔽管理类
 */
class ShieldManager(//声明定位回调监听器
        private val context: Context) {
    //声明mLocationOption对象
    var mLocationOption: AMapLocationClientOption? = null
    //声明AMapLocationClient类对象
    var mLocationClient: AMapLocationClient? = null
    var mLocationListener: AMapLocationListener = AMapLocationListener { aMapLocation ->
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


                val cityCode = aMapLocation.cityCode
                val latitude = aMapLocation.latitude.toString()
                val longitude = aMapLocation.longitude.toString()

                if (!Constants.isHideAD && MediaConfig.getConfig() != null) {
                    if (!TextUtils.isEmpty(cityCode)) {
                        MediaConfig.setCityCode(cityCode)
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
            loadDataManager.addDefaultBooks(Constants.SGENDER)
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

    fun stopAchieveUserLocation() {
        mLocationClient?.onDestroy()
    }

    companion object {

        private val TAG = ShieldManager::class.java.simpleName
    }

}