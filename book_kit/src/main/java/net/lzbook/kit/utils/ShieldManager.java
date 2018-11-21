package net.lzbook.kit.utils;

import android.content.Context;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.ding.basic.config.ParameterConfig;
import com.ding.basic.net.Config;
import com.dy.media.MediaConfig;

import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.utils.book.LoadDataManager;
import net.lzbook.kit.utils.logger.AppLog;
import com.ding.basic.util.sp.SPKey;
import com.ding.basic.util.sp.SPUtils;
import com.orhanobut.logger.Logger;

/**
 * 屏蔽管理类
 */
public class ShieldManager {

    private static final String TAG = ShieldManager.class.getSimpleName();
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    private Context context;
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    Constants.longitude = aMapLocation.getLongitude();
                    Constants.latitude = aMapLocation.getLatitude();
                    Constants.adCode = aMapLocation.getAdCode();
                    Constants.adCityInfo = aMapLocation.getCity();
                    Constants.cityCode = aMapLocation.getCityCode();
                    Constants.adLocationDetail = aMapLocation.getDistrict() + " "
                            + aMapLocation.getStreet() + " "
                            + aMapLocation.getStreetNum() + " "
                            + "(" + Constants.longitude + ", " + Constants.latitude + ")";


                    String longitude = String.valueOf(aMapLocation.getLongitude());
                    Config.INSTANCE.insertRequestParameter("longitude", longitude);

                    String latitude = String.valueOf(aMapLocation.getLatitude());
                    Config.INSTANCE.insertRequestParameter("latitude", latitude);

                    String cityCode = aMapLocation.getCityCode();
                    Config.INSTANCE.insertRequestParameter("cityCode", cityCode);



                    Constants.latitude = aMapLocation.getLatitude();
                    ParameterConfig.INSTANCE.setLatitude(String.valueOf(aMapLocation.getLatitude()));

                    Constants.longitude = aMapLocation.getLongitude();
                    ParameterConfig.INSTANCE.setLongitude(String.valueOf(aMapLocation.getLongitude()));

                    Constants.adCode = aMapLocation.getAdCode();
                    ParameterConfig.INSTANCE.setAreaCode(aMapLocation.getAdCode());

                    Constants.adCityInfo = aMapLocation.getCity();
                    ParameterConfig.INSTANCE.setCity(aMapLocation.getCity());

                    Constants.cityCode = aMapLocation.getCityCode();
                    ParameterConfig.INSTANCE.setCityCode(aMapLocation.getCityCode());

                    Constants.adLocationDetail = (aMapLocation.getDistrict() + " "
                            + aMapLocation.getStreet() + " "
                            + aMapLocation.getStreetNum() + " "
                            + "(" + Constants.longitude + ", " + Constants.latitude + ")");

                    ParameterConfig.INSTANCE.setLocationDetail(aMapLocation.getDistrict() + " "
                            + aMapLocation.getStreet() + " "
                            + aMapLocation.getStreetNum() + " "
                            + "(" + Constants.longitude + ", " + Constants.latitude + ")");




                    if (!Constants.isHideAD && MediaConfig.INSTANCE.getConfig() != null) {
                        if (!TextUtils.isEmpty(cityCode)) {
                            MediaConfig.INSTANCE.setCityCode(cityCode);
                        }

                        MediaConfig.INSTANCE.setCityName(Constants.adCityInfo);

                        if (!TextUtils.isEmpty(latitude)) {
                            MediaConfig.INSTANCE.setLatitude(Float.valueOf(latitude));
                        }

                        if (!TextUtils.isEmpty(longitude)) {
                            MediaConfig.INSTANCE.setLongitude(Float.valueOf(longitude));
                        }
                        MediaConfig.INSTANCE.setAd_userid(OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));
                        MediaConfig.INSTANCE.setChannel_code(AppUtils.getChannelId());
                    }

                    AppLog.e(TAG, "城市信息：" + aMapLocation.getCity() + "城市编码：" + aMapLocation.getCityCode() + " 经度：" + aMapLocation.getLongitude() + " 纬度：" + aMapLocation.getLatitude());
                    Logger.e("城区信息：" + aMapLocation.getDistrict() + " 街道信息：" + aMapLocation.getStreet() + " 门牌号：" + aMapLocation.getStreetNum() + " 地区编码：" + aMapLocation.getAdCode());
                    stopAchieveUserLocation();
                    //为InMobi广告sdk设置Location
//                    LogUtils.e(TAG,"location:"+aMapLocation.toString());
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Logger.e("AmapError location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:" + aMapLocation.getErrorInfo());
                }
            }
            try {
                if (NetWorkUtils.getNetWorkType(BaseBookApplication.getGlobalContext()) != NetWorkUtils.NETWORK_NONE) {
                    initBook();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public ShieldManager(Context context) {
        this.context = context;
    }

    private void initBook() {
        LoadDataManager loadDataManager = new LoadDataManager(context);
        if (!SPUtils.INSTANCE.getDefaultSharedBoolean(SPKey.ADD_DEFAULT_BOOKS,false)) {
            // 首次安装新用户添加默认书籍
            loadDataManager.addDefaultBooks(Constants.SGENDER);
        }
    }


    public void startAchieveUserLocation() {
        //初始化定位
        mLocationClient = new AMapLocationClient(context);
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(true);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(30 * 60 * 1000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    public void stopAchieveUserLocation() {
        mLocationClient.onDestroy();
    }

}