package net.lzbook.kit.utils;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.Constants;

import android.content.Context;

/**
 * 屏蔽管理类
 */
public class ShieldManager {

    private static final String TAG = ShieldManager.class.getSimpleName();
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    public SharedPreferencesUtils sharedPreferencesUtils;
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
                    AppLog.e(TAG, "城市信息：" + aMapLocation.getCity() + "城市编码：" + aMapLocation.getCityCode() + " 经度：" + aMapLocation.getLongitude() + " 纬度：" + aMapLocation.getLatitude());
                    AppLog.e(TAG, "城区信息：" + aMapLocation.getDistrict() + " 街道信息：" + aMapLocation.getStreet() + " 门牌号：" + aMapLocation.getStreetNum() + " 地区编码：" + aMapLocation.getAdCode());
                    stopAchieveUserLocation();
                    //为InMobi广告sdk设置Location
//                    LogUtils.e(TAG,"location:"+aMapLocation.toString());
                    InMobiAdapter.setLocation(aMapLocation);
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    AppLog.e("AmapError", "location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:" + aMapLocation.getErrorInfo());
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

    public ShieldManager(Context context, SharedPreferencesUtils sharedPreferencesUtils) {
        this.context = context;
        this.sharedPreferencesUtils = sharedPreferencesUtils;
    }

    private void initBook() {
        LoadDataManager loadDataManager = new LoadDataManager(context);
        if (!sharedPreferencesUtils.getBoolean(Constants.ADD_DEFAULT_BOOKS)) {
            // 首次安装新用户添加默认书籍
            loadDataManager.addDefaultBooks();
        }
    }

//    //获取书籍屏蔽、阅读页屏蔽
//    public void loadSensitiveWords(String type) {
//
//        String filePath = "";
//        String digest = BookApplication.getUdid();
//
//        switch (type) {
//            case "read":
//                filePath = Constants.APP_PATH_CACHE + "read";
//                break;
//            case "gid":
//                filePath = Constants.APP_PATH_CACHE + "gid";
//                break;
//            case "location":
//                filePath = Constants.APP_PATH_CACHE + "location";
//                break;
//            case "adCode":
//                filePath = Constants.APP_PATH_CACHE + "adCode";
//        }
//
//        File file = new File(filePath);
//        AppLog.e(TAG, "File : " + file.exists());
//        SensitiveWords sensitiveWords;
//        if (file.exists() && file.canRead()) {
//            sensitiveWords = (SensitiveWords) FileUtils.deserialize(filePath);
//            if (sensitiveWords != null && !TextUtils.isEmpty(sensitiveWords.getDigest())) {
//                digest = sensitiveWords.getDigest();
//                switch (type) {
//                    case "read":
//                        BookApplication.setReadSensitiveWords(sensitiveWords);
//                        break;
//                    case "gid":
//                        BookApplication.setBookSensitiveWords(sensitiveWords);
//                        break;
//                    case "location":
//                        BookApplication.setLocationSensitiveWords(sensitiveWords);
//                        break;
//                    case "adCode":
//                        BookApplication.setAdcodeSensitiveWords(sensitiveWords);
//                    default:
//
//                }
//            }
//        }
//
//        String url = DataUtil.buildUrlNew(DataUtil.GET_SHIELD_INFORMATION, false, type, digest);
//        OtherRequestService.getSensitiveWords(sensitiveWordsCallBack, url, digest, filePath);
//    }

//    final OtherRequestService.DataServiceTagCallBack sensitiveWordsCallBack = new OtherRequestService.DataServiceTagCallBack() {
//        @Override
//        public void onSuccess(Object result, Object tag) {
//        }
//
//        @Override
//        public void onError(Exception error, Object tag) {
//        }
//    };

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

//        Constants.isShielding = judgeShield();
//        AppLog.e(TAG, "是否需要屏蔽：" + Constants.isShielding);
    }

//    //判断是否开启屏蔽
//    public boolean judgeShield() {
//        //判断地域编码
//        List<String> adCode = BookApplication.getAdcodeSensitiveWords().getList();
//        for (int i = 0; i < adCode.size(); i++) {
//            if (Constants.adCode.startsWith(adCode.get(i))) {
//                return true;
//            }
//        }
//
//        //判断经纬度
//        double longitudeStart = 0;
//        double longitudeEnd = 0;
//        double latitudeStart = 0;
//        double latitudeEnd = 0;
//
//        List<String> location = BookApplication.getLocationSensitiveWords().getList();
//        for (int i = 0; i < location.size(); i++) {
//            AppLog.e(TAG, "JudgeShield: " + location.get(i));
//            if (!TextUtils.isEmpty(location.get(i))) {
//                String[] message = location.get(i).split("#");
//                if (message.length > 0) {
//                    latitudeStart = Double.valueOf(message[0]);
//                }
//                if (message.length > 1) {
//                    longitudeStart = Double.valueOf(message[1]);
//                }
//                if (message.length > 2) {
//                    latitudeEnd = Double.valueOf(message[2]);
//                }
//                if (message.length > 3) {
//                    longitudeEnd = Double.valueOf(message[3]);
//                }
//            }
//            if (Constants.longitude >= longitudeStart && Constants.longitude <= longitudeEnd && Constants.latitude >= latitudeStart && Constants.latitude <= latitudeEnd) {
//                return true;
//            }
//        }
//        return false;
//    }
}