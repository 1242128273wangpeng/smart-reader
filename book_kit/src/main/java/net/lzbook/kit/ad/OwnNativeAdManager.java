package net.lzbook.kit.ad;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.dingyueads.sdk.AdListener;
import com.dingyueads.sdk.Bean.ADPlatform;
import com.dingyueads.sdk.Bean.AdSceneData;
import com.dingyueads.sdk.Bean.Advertisement;
import com.dingyueads.sdk.Bean.Ration;
import com.dingyueads.sdk.Native.YQNativeAdInfo;
import com.dingyueads.sdk.NativeInit;
import com.dingyueads.sdk.RationManager;
import com.dingyueads.sdk.Utils.SDKUtil;
import com.dingyueads.sdk.db.AdDao;

import net.lzbook.kit.R;
import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.cache.imagecache.ImageCacheManager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.EventBookshelfAd;
import net.lzbook.kit.data.bean.EventNativeType;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.ImageUtils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.OpenUDID;
import net.lzbook.kit.utils.ResourceUtil;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.StatisticManager;
import net.lzbook.kit.utils.Tools;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 自有广告平台
 * Created by q on 2016/1/27.
 */
public class OwnNativeAdManager implements AdListener {
    private static final String TAG = "OwnNativeAdManager";
    private static final int default_length = 20;
    private static WeakReference<Activity> mActivityRef;
    private static OwnNativeAdManager mInstance;
    Handler handler = new AdHandler();
    private WeakReference<LayoutInflater> mInflaterRef;
    private ViewGroup viewGroup;
    private NativeInit nativeInit;
    private LinkedList<YQNativeAdInfo> adInfoLinkedList = new LinkedList<>();
    private HashMap<String, LinkedList<YQNativeAdInfo>> infoMap = new HashMap<>();
    private WeakReference<ReadStatus> readStatusWeakReference;
    private int handlerMsgCode = 0;
    private boolean hasInitConfig = true;
    private boolean isLoadSplash = false;
    private boolean isLoadDefaultSplashAD = false;
    private boolean isLoadMore = false;
    private StatisticManager statisticManager;
    private Handler splashHandler;

    public OwnNativeAdManager(Activity activity) {
        this.mActivityRef = new WeakReference<>(activity);
        Activity act = mActivityRef.get();
        if (act != null) {
            mInflaterRef = new WeakReference<>(LayoutInflater.from(act));
        }

        //获取用户uid、定位信息内容
        com.dingyueads.sdk.Constants.uid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
        com.dingyueads.sdk.Constants.cityCode = Constants.cityCode;
        com.dingyueads.sdk.Constants.adCityInfo = Constants.adCityInfo;
        com.dingyueads.sdk.Constants.latitude = (float) Constants.latitude;
        com.dingyueads.sdk.Constants.longitude = (float) Constants.longitude;

        nativeInit = new NativeInit(activity, this);
    }

    public OwnNativeAdManager(Activity activity, ViewGroup viewGroup, Handler splashHandler, int handlerMsgCode, String defaultPostionId, String
            PlatformAppId) {
        this.mActivityRef = new WeakReference<>(activity);
        this.viewGroup = viewGroup;
        this.handlerMsgCode = handlerMsgCode;
        Activity act = mActivityRef.get();
        if (act != null) {
            mInflaterRef = new WeakReference<>(LayoutInflater.from(act));
        }

        //获取用户uid、定位信息内容
        com.dingyueads.sdk.Constants.uid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
        com.dingyueads.sdk.Constants.cityCode = Constants.cityCode;
        com.dingyueads.sdk.Constants.adCityInfo = Constants.adCityInfo;
        com.dingyueads.sdk.Constants.latitude = (float) Constants.latitude;
        com.dingyueads.sdk.Constants.longitude = (float) Constants.longitude;

        setSplashHandler(splashHandler);
        statisticManager = new StatisticManager();
        nativeInit = new NativeInit(activity, this);
        nativeInit.setDfaultPostionId(defaultPostionId, PlatformAppId);
        //如果传入了百度的ID和应用ID则视为 拉取广告失败默认加载百度
        if (defaultPostionId != null && !defaultPostionId.equals("")) {
            isLoadDefaultSplashAD = true;
        }
    }

    public static synchronized OwnNativeAdManager getInstance(Activity mActivity) {
        if (mInstance == null) {
            mInstance = new OwnNativeAdManager(mActivity);
            AppLog.e(TAG, "OwnNativeAdManager mInstance new instance");
        } else {
            if (mActivityRef == null || mActivityRef.get() == null) {
                mActivityRef = new WeakReference<>(mActivity);
            }
            AppLog.e(TAG, "OwnNativeAdManager mInstance already exist");
        }
        return mInstance;
    }

    /**
     * 广告被接收后回调
     *
     * @param viewGroup        广告要展现的容器
     * @param splashHandler    界面跳转的handler对象
     * @param handlerMsgCode   要跳转的消息的编号 默认是0
     * @param defaultPostionId 填充率不足或者自有后台拿不到匹配值时 默认展示百度广告，传入应用对应的百度PostionId 如TXT免费阅读器的就是"2362908"
     *                         传入null则不显示默认广告;
     * @param PlatformAppId    百度应用id
     * @param SplashPostion    开屏位置
     */
    public static synchronized void InitSplashAd(Activity mActivity, ViewGroup viewGroup, Handler splashHandler, int handlerMsgCode, String
            defaultPostionId, String PlatformAppId, NativeInit.CustomPositionName SplashPostion) {
        if (mInstance == null) {
            mInstance = new OwnNativeAdManager(mActivity, viewGroup, splashHandler, handlerMsgCode, defaultPostionId, PlatformAppId);
            AppLog.e(TAG, "OwnNativeAdManager mInstance new instance");
        } else {
            //对象存在时 不初始化网络直接加载开屏广告 设置开屏界面的handler对象
            AppLog.e(TAG, "OwnNativeAdManager mInstance already exist");
            mInstance.loadSplashAd(SplashPostion, viewGroup, false);
            mInstance.setSplashHandler(splashHandler);
        }
    }

    /**
     * 跳到切屏广告
     */
    public static void toSwitchAdActivity(Activity activity) {
        Intent adIntent = new Intent(activity, SwitchSplashAdActivity.class);
        activity.startActivity(adIntent);
    }

    private void setSplashHandler(Handler splashHandler) {
        this.splashHandler = splashHandler;
        isLoadSplash = true;
    }

    public void loadSplashAd(NativeInit.CustomPositionName type, final ViewGroup container, Boolean isDefault) {
        if (Constants.isHideAD) return;
        this.viewGroup = container;
        AppLog.e(TAG, "isLoadDefaultSplashAD !!!!!:" + isLoadDefaultSplashAD);
        if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
            if (nativeInit != null) {
                nativeInit.loadSplahAd(type, container, isDefault, mActivityRef);
            }
        }
    }

    public void loadAdForMiddle(NativeInit.CustomPositionName currentPositionName) {
        if (Constants.isHideAD) return;
        if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
            synchronized (OwnNativeAdManager.class) {
                if (nativeInit != null) {
                    AppLog.e(TAG, "OwnNativeAdManager nativeInit != null");

                    //针对360广告单独写一套逻辑
                    if (readStatusWeakReference == null) return;
                    ReadStatus readStatus = readStatusWeakReference.get();
                    if (readStatus == null) return;
                    //上
                    if (readStatus.currentAdInfo != null && readStatus.currentAdInfo.getAdvertisement() != null
//                            && readStatus.currentAdInfo.getAdvertisement().platformId == com.dingyueads.sdk.Constants.AD_TYPE_360
                            && (System.currentTimeMillis() - readStatus.currentAdInfo.getAvailableTime() < 10 * 60 * 1000)
                            && !readStatus.currentAdInfo.getAdvertisement().isShowed
                            && readStatus.getAd_bitmap_middle() != null
                            && !readStatus.getAd_bitmap_middle().isRecycled()) {
                        //当前360物料可用
                    } else {
                        getAdForMiddle(currentPositionName, 0);
                    }
                    //下
                    if (readStatus.currentAdInfoDown != null && readStatus.currentAdInfoDown.getAdvertisement() != null
//                            && readStatus.currentAdInfoDown.getAdvertisement().platformId == com.dingyueads.sdk.Constants.AD_TYPE_360
                            && (System.currentTimeMillis() - readStatus.currentAdInfoDown.getAvailableTime() < 10 * 60 * 1000)
                            && !readStatus.currentAdInfoDown.getAdvertisement().isShowed
                            && readStatus.getAd_bitmap_middle_down() != null
                            && !readStatus.getAd_bitmap_middle_down().isRecycled()) {
                        //当前360物料可用
                    } else {
                        getAdForMiddle(currentPositionName, 1);
                    }
                }
            }
        }
    }

    private void getAdForMiddle(NativeInit.CustomPositionName currentPositionName, int i) {
        Ration ration = NativeInit.rationManager.getRation(currentPositionName.toString());
        if (ration == null) {
            return;
        }

        if (com.dingyueads.sdk.Constants.AD_TYPE_OWNAD == ration.getPlatformId()) {
            StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_adwin);
        }

        ration.setReadingMiddlePostion(i);

        StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_count_request);

        String keyMd5 = SDKUtil.digest(ration.getMarkId() + ration.getPlatformId());
        LinkedList<YQNativeAdInfo> yqNativeAdInfos = infoMap.get(keyMd5);

        if (yqNativeAdInfos == null || yqNativeAdInfos.isEmpty()) {
            if (!ration.isRequestingMiddleAd()) {
                isLoadMore = true;
                ration.setRequestingMiddleAd(true);
                nativeInit.loadAdNew(RationManager.ad_load_count, ration, mActivityRef);
                AppLog.e(TAG, "loadAdForMiddle yqNativeAdInfos null currentPositionName:" + currentPositionName.toString());
            } else {
                ration.setRequestingMiddleAd(false);
                if (i == 0) {
                    if (readStatusWeakReference != null && readStatusWeakReference.get() != null) {
                        readStatusWeakReference.get().currentAdInfo = null;
                        readStatusWeakReference.get().setAd_bitmap_middle(null);
                    }
                }
                if (i == 1) {
                    if (readStatusWeakReference != null && readStatusWeakReference.get() != null) {
                        readStatusWeakReference.get().currentAdInfoDown = null;
                        readStatusWeakReference.get().setAd_bitmap_middle_down(null);
                    }
                }
            }
        } else {
            YQNativeAdInfo yqNativeAdInfo = null;
            if (currentPositionName == NativeInit.CustomPositionName.READING_MIDDLE_POSITION) {
                if (com.dingyueads.sdk.Constants.AD_TYPE_360 == ration.getPlatformId()) {
                    yqNativeAdInfo = yqNativeAdInfos.removeFirst();
                } else {
                    yqNativeAdInfo = getADInfoNew(yqNativeAdInfos, ration, keyMd5);
                }
                if (yqNativeAdInfo == null) {
                    //获取的方法中已经进行了重新请求，此处只需要返回即可
                    return;
                }
                Message message = handler.obtainMessage();
                if (i == 0) {
                    message.obj = yqNativeAdInfo;
                    message.what = 2;
                    handler.sendMessageDelayed(message, 500);
                } else if (i == 1) {
                    message.obj = yqNativeAdInfo;
                    message.what = 4;
                    handler.sendMessageDelayed(message, 500);
                }
            }
            if (yqNativeAdInfo != null) {
                AppLog.e("getSingleADInfo", "title = " + yqNativeAdInfo.getAdvertisement().title + " isShowed= " + yqNativeAdInfo.getAdvertisement().isShowed + " yqNativeAdInfos size:" + yqNativeAdInfos.size());
            }
        }
    }

    public void loadAd(NativeInit.CustomPositionName currentPositionName) {
        AppLog.e(TAG, "OwnNativeAdManager LoadAd");
        if (Constants.isHideAD) return;
        if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
            synchronized (OwnNativeAdManager.class) {
                if (nativeInit != null) {
                    AppLog.e(TAG, "OwnNativeAdManager nativeInit != null");
                    Ration ration = NativeInit.rationManager.getRation(currentPositionName.toString());
                    if (ration == null) {
                        return;
                    }

                    if (com.dingyueads.sdk.Constants.AD_TYPE_OWNAD == ration.getPlatformId()) {
                        StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_adwin);
                    }

                    if (NativeInit.CustomPositionName.READING_POSITION.toString().equals(ration.getMarkId()) ||
                            NativeInit.CustomPositionName.READING_MIDDLE_POSITION.toString().equals(ration.getMarkId()) ||
                            NativeInit.CustomPositionName.READING_IN_CHAPTER_POSITION.toString().equals(ration.getMarkId())) {
                        StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_count_request);
                    }

                    String keyMd5 = SDKUtil.digest(ration.getMarkId() + ration.getPlatformId());
                    LinkedList<YQNativeAdInfo> yqNativeAdInfos = infoMap.get(keyMd5);

                    if (yqNativeAdInfos == null || yqNativeAdInfos.isEmpty()) {
                        isLoadMore = true;
                        nativeInit.loadAdNew(RationManager.ad_load_count, ration, mActivityRef);
                        AppLog.e(TAG, "loadAd yqNativeAdInfos null currentPositionName:" + currentPositionName.toString());
                    } else {
                        YQNativeAdInfo yqNativeAdInfo = null;
                        if (currentPositionName == NativeInit.CustomPositionName.READING_POSITION) {
                            if (Constants.IS_LANDSCAPE) {
                                yqNativeAdInfo = getSingleADInfo(NativeInit.CustomPositionName.READING_MIDDLE_POSITION);
                            } else {
                                yqNativeAdInfo = getADInfoNew(yqNativeAdInfos, ration, keyMd5);
                            }
                            if (yqNativeAdInfo == null) {
                                //获取的方法中已经进行了重新请求，此处只需要返回即可
                                return;
                            }
                            Message message = handler.obtainMessage();
                            message.obj = yqNativeAdInfo;
                            message.what = 1;
                            handler.sendMessageDelayed(message, 500);
                        } else if (currentPositionName == NativeInit.CustomPositionName.READING_MIDDLE_POSITION) {
                            yqNativeAdInfo = getADInfoNew(yqNativeAdInfos, ration, keyMd5);
                            if (yqNativeAdInfo == null) {
                                //获取的方法中已经进行了重新请求，此处只需要返回即可
                                return;
                            }
                            Message message = handler.obtainMessage();
                            message.obj = yqNativeAdInfo;
                            message.what = 2;
                            handler.sendMessageDelayed(message, 500);
                        } else if (currentPositionName == NativeInit.CustomPositionName.READING_IN_CHAPTER_POSITION) {
                            if (Constants.IS_LANDSCAPE) {
                                yqNativeAdInfo = getSingleADInfo(NativeInit.CustomPositionName.READING_MIDDLE_POSITION);
                            } else {
                                yqNativeAdInfo = getADInfoNew(yqNativeAdInfos, ration, keyMd5);
                            }
                            if (yqNativeAdInfo == null) {
                                //获取的方法中已经进行了重新请求，此处只需要返回即可
                                return;
                            }
                            Message message = handler.obtainMessage();
                            message.obj = yqNativeAdInfo;
                            message.what = 3;
                            handler.sendMessageDelayed(message, 500);
                        } else {
                            EventBus.getDefault().post(new EventNativeType(currentPositionName.toString()));
                        }
                    }
                }
            }
        }
    }

    public void loadAdNew(Ration ration) {
        if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
            if (nativeInit != null) {
                synchronized (OwnNativeAdManager.class) {
                    nativeInit.loadAdNew(RationManager.ad_load_count, ration, mActivityRef);
                }
            }
        }
    }

    @Override
    public void onRequestSuccess(List<YQNativeAdInfo> yqNativeAdInfoList, final Ration ration) {
        isLoadMore = false;
        if (yqNativeAdInfoList != null && !yqNativeAdInfoList.isEmpty()) {

            //记录请求广告平台成功返回的时间
            ration.setRequestPlatSuccessTime(System.currentTimeMillis() / 1000L);
            ration.setRequestMaterialCount(yqNativeAdInfoList.size());
            if (statisticManager == null) {
                statisticManager = StatisticManager.getStatisticManager();
            }
            statisticManager.sendRequestAdBackInfo(ration);

            adInfoLinkedList = null;
            adInfoLinkedList = new LinkedList<>();
            adInfoLinkedList.addAll(yqNativeAdInfoList);

            String keyMd5 = SDKUtil.digest(ration.getMarkId() + ration.getPlatformId());
            AppLog.e(TAG, "onRequestSuccess list size:" + yqNativeAdInfoList.size() + " currentPositionName:" + ration.getMarkId()
                    + " md5:" + keyMd5);

            infoMap.put(keyMd5, adInfoLinkedList);

            LinkedList<YQNativeAdInfo> adInfos = infoMap.get(keyMd5);
            final YQNativeAdInfo yqNativeAdInfo;

            if (NativeInit.CustomPositionName.READING_POSITION.toString().equals(ration.getMarkId())) {
//                AppLog.e("adtest5-1", "onRequestSuccess list size:" + yqNativeAdInfoList.size() + " currentPositionName:" + ration.getMarkId()
//                        + " md5:" + keyMd5 + "\n" + adInfos.toString());
                if (Constants.IS_LANDSCAPE) {
                    yqNativeAdInfo = getSingleADInfo(NativeInit.CustomPositionName.READING_MIDDLE_POSITION);
                } else {
                    yqNativeAdInfo = getADInfoNew(adInfos, ration, keyMd5);
                }
                Message message = handler.obtainMessage();
                message.obj = yqNativeAdInfo;
                message.what = 1;
                handler.sendMessageDelayed(message, 500);
            } else if (NativeInit.CustomPositionName.READING_MIDDLE_POSITION.toString().equals(ration.getMarkId())) {
//                AppLog.e("adtest8-1", "onRequestSuccess list size:" + yqNativeAdInfoList.size() + " currentPositionName:" + ration.getMarkId()
//                        + " md5:" + keyMd5 + "\n" + adInfos.toString());
                if (ration.getPlatformId() == com.dingyueads.sdk.Constants.AD_TYPE_360) {
                    yqNativeAdInfo = adInfos.removeFirst();
                } else {
                    yqNativeAdInfo = getADInfoNew(adInfos, ration, keyMd5);
                }
                ration.setRequestingMiddleAd(false);

                Message message = handler.obtainMessage();
                if (ration.getReadingMiddlePostion() == 0) {
                    message.obj = yqNativeAdInfo;
                    message.what = 2;
                    handler.sendMessageDelayed(message, 500);
                } else if (ration.getReadingMiddlePostion() == 1) {
                    message.obj = yqNativeAdInfo;
                    message.what = 4;
                    handler.sendMessageDelayed(message, 500);
                }
            } else if (NativeInit.CustomPositionName.READING_IN_CHAPTER_POSITION.toString().equals(ration.getMarkId())) {
//                AppLog.e("adtest5-2", "onRequestSuccess list size:" + yqNativeAdInfoList.size() + " currentPositionName:" + ration.getMarkId()
//                        + " md5:" + keyMd5 + "\n" + adInfos.toString());
                if (Constants.IS_LANDSCAPE) {
                    yqNativeAdInfo = getSingleADInfo(NativeInit.CustomPositionName.READING_MIDDLE_POSITION);
                } else {
                    yqNativeAdInfo = getADInfoNew(adInfos, ration, keyMd5);
                }
                //设置章节内大图
                Message message = handler.obtainMessage();
                message.obj = yqNativeAdInfo;
                message.what = 3;
                handler.sendMessageDelayed(message, 500);
            } else if (NativeInit.CustomPositionName.SPLASH_SUPPLY_POSITION.toString().equals(ration.getMarkId())) {
                if (splashHandler != null && viewGroup != null) {
                    yqNativeAdInfo = adInfos.removeFirst();
                    drawBitmapOnSplash(viewGroup, yqNativeAdInfo, NativeInit.ad_position[8]);
                }
                if (com.dingyueads.sdk.Constants.AD_TYPE_OWNAD == ration.getPlatformId()) {
                    StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_adwin_resuc);
                }
            } else if (NativeInit.CustomPositionName.SPLASH_POSITION.toString().equals(ration.getMarkId())) {
                if (splashHandler != null && viewGroup != null) {
                    splashHandler.sendEmptyMessageDelayed(handlerMsgCode, 5000L);
                    yqNativeAdInfo = adInfos.removeFirst();
                    drawBitmapOnSplash(viewGroup, yqNativeAdInfo, NativeInit.ad_position[5]);
                }
            } else if (NativeInit.CustomPositionName.SWITCH_SPLASH_POSITION.toString().equals(ration.getMarkId())) {
                if (splashHandler != null && viewGroup != null) {
                    splashHandler.sendEmptyMessageDelayed(handlerMsgCode, 5000L);
                    yqNativeAdInfo = adInfos.removeFirst();
                    drawBitmapOnSplash(viewGroup, yqNativeAdInfo, NativeInit.ad_position[6]);
                }
            } else {
                if ("1-1".equals(ration.getMarkId())) {
                    ration.setRequestingMiddleAd(false);
                    if (ration.getPlatformId() == com.dingyueads.sdk.Constants.AD_TYPE_360) {
                        yqNativeAdInfo = adInfos.removeFirst();
                    } else {
                        yqNativeAdInfo = getADInfoNew(adInfos, ration, keyMd5);
                    }
                    if (yqNativeAdInfo == null) return;
                    final EventBookshelfAd eventBookshelfAd = new EventBookshelfAd(ration.getMarkId(), ration.getBookShelfAdPosition(),
                            yqNativeAdInfo);
                    if (mActivityRef != null && mActivityRef.get() != null) {
                        mActivityRef.get().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EventBus.getDefault().post(eventBookshelfAd);
                            }
                        });
                    }
                } else {
                    EventBus.getDefault().post(new EventNativeType(ration.getMarkId()));
                }
            }
        } else {
            if (NativeInit.CustomPositionName.SPLASH_POSITION.toString().equals(ration.getMarkId())) {
                if (splashHandler != null) {
                    splashHandler.sendEmptyMessageDelayed(handlerMsgCode, 5000L);
                }
                Ration rationNew = NativeInit.rationManager.getRation(NativeInit.CustomPositionName.SPLASH_SUPPLY_POSITION.toString());
                if (rationNew == null) {
                    return;
                }
                if (nativeInit != null) {
                    nativeInit.loadAdNew(1, rationNew, mActivityRef);
                }
                if (com.dingyueads.sdk.Constants.AD_TYPE_OWNAD == rationNew.getPlatformId()) {
                    StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_adwin);
                }
            }
            //记录请求广告平台成功返回的时间
            ration.setRequestPlatSuccessTime(System.currentTimeMillis() / 1000L);
            ration.setRequestMaterialCount(0);
            if (statisticManager == null) {
                statisticManager = StatisticManager.getStatisticManager();
            }
            statisticManager.sendRequestAdBackInfo(ration);
        }
    }

    private void drawBitmapOnSplash(final ViewGroup view, YQNativeAdInfo yqNativeAdInfo, final String position) {
        if (view == null || yqNativeAdInfo == null) return;

        final YQNativeAdInfo nativeAdInfo = yqNativeAdInfo;
        AdSceneData adSceneData = nativeAdInfo.getAdSceneData();
        if (adSceneData != null) {
            adSceneData.ad_requestTime = String.valueOf(System.currentTimeMillis() / 1000L);
        }
        final Advertisement advertisement = nativeAdInfo.getAdvertisement();
        if (advertisement == null) {
            return;
        }
        AppLog.e(TAG, "Advertisement: " + advertisement.toString());
        if (!TextUtils.isEmpty(advertisement.imageUrl)) {
            final String image_url = advertisement.imageUrl;
            ImageCacheManager.getInstance().getImageLoader().get(image_url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                    AppLog.e(TAG, "onResponse big image");
                    final Activity act = mActivityRef.get();
                    if (act == null || imageContainer == null) {
                        return;
                    }
                    Bitmap bitmap_icon = imageContainer.getBitmap();
                    if (bitmap_icon == null) return;

                    Bitmap logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_default);
                    Bitmap bitmap = toConformBitmap(bitmap_icon, logo_bitmap, true, true);

                    ImageView imageView = new ImageView(act);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    imageView.setImageBitmap(bitmap);
                    view.removeAllViews();
                    view.addView(imageView);

                    if (statisticManager == null) {
                        statisticManager = StatisticManager.getStatisticManager();
                    }

                    statisticManager.schedulingRequest(act, view, nativeAdInfo, null, StatisticManager.TYPE_SHOW, position);

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (statisticManager == null) {
                                statisticManager = StatisticManager.getStatisticManager();
                            }
                            statisticManager.schedulingRequest(act, view, nativeAdInfo, null, StatisticManager.TYPE_CLICK, position);
                        }
                    });
                }

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                }
            });
        }
    }

    @Override
    public void onNoNativeAD(AdSceneData adSceneData, String i, Ration ration) {
        isLoadMore = false;
        if (statisticManager != null && adSceneData != null) {
            adSceneData.ad_requestFailureTime = String.valueOf(System.currentTimeMillis() / 1000L);
            adSceneData.ad_requestFailureReason = i;
            statisticManager.sendAdFailData(adSceneData);
        }

        //信息流广告请求失败后做补充
        if (ration == null && ration.getMarkId() == null) {
            return;
        }
        if ((ration.getMarkId().equals("8-1") || ration.getMarkId().equals("1-1")) && ration.isRequestingMiddleAd()) {
            ration.setRequestingMiddleAd(false);
        }

        if (com.dingyueads.sdk.Constants.AD_TYPE_OWNAD == ration.getPlatformId()) {
            StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_adwin_no);
        }

        //广点通开屏广告请求失败后或者请求自有开屏广告失败后走的逻辑
        if (NativeInit.CustomPositionName.SPLASH_POSITION.toString().equals(ration.getMarkId())) {
            if (splashHandler != null) {
                splashHandler.sendEmptyMessageDelayed(handlerMsgCode, 5000L);
            }
            Ration rationNew = NativeInit.rationManager.getRation(NativeInit.CustomPositionName.SPLASH_SUPPLY_POSITION.toString());
            if (rationNew == null) {
                return;
            }
            if (nativeInit != null) {
                nativeInit.loadAdNew(1, rationNew, mActivityRef);
            }
            if (com.dingyueads.sdk.Constants.AD_TYPE_OWNAD == ration.getPlatformId()) {
                StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_adwin);
            }
        }

        if (NativeInit.CustomPositionName.READING_POSITION.toString().equals(ration.getMarkId()) ||
                NativeInit.CustomPositionName.READING_MIDDLE_POSITION.toString().equals(ration.getMarkId()) ||
                NativeInit.CustomPositionName.READING_IN_CHAPTER_POSITION.toString().equals(ration.getMarkId())) {

            StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_count_reselection);

            ADPlatform adPlatform = NativeInit.rationManager.getPlatForm(ration.getMarkId());
            if (adPlatform == null) return;
            List<Ration> rationList = adPlatform.getRationList();
            if (rationList == null || rationList.isEmpty()) return;

            for (Ration rationSub : rationList) {
                String keyMd5 = SDKUtil.digest(rationSub.getMarkId() + rationSub.getPlatformId());
                LinkedList<YQNativeAdInfo> yqNativeAdInfos = infoMap.get(keyMd5);
                if (yqNativeAdInfos == null || yqNativeAdInfos.isEmpty()) {
                } else {
                    YQNativeAdInfo yqNativeAdInfo = null;
                    if (NativeInit.CustomPositionName.READING_POSITION.toString().equals(rationSub.getMarkId())) {
                        yqNativeAdInfo = getADInfoNew(yqNativeAdInfos, rationSub, keyMd5);
                        if (yqNativeAdInfo == null) {
                            //获取的方法中已经进行了重新请求，此处只需要返回即可
                            continue;
                        }
                        Message message = handler.obtainMessage();
                        message.obj = yqNativeAdInfo;
                        message.what = 1;
                        handler.sendMessageDelayed(message, 500);
                    } else if (NativeInit.CustomPositionName.READING_MIDDLE_POSITION.toString().equals(rationSub.getMarkId())) {
                        rationSub.setReadingMiddlePostion(ration.getReadingMiddlePostion());
                        if (rationSub.getPlatformId() == com.dingyueads.sdk.Constants.AD_TYPE_360) {
                            yqNativeAdInfo = yqNativeAdInfos.removeFirst();
                        } else {
                            yqNativeAdInfo = getADInfoNew(yqNativeAdInfos, rationSub, keyMd5);
                        }
                        if (yqNativeAdInfo == null) {
                            //获取的方法中已经进行了重新请求，此处只需要返回即可
                            continue;
                        }
                        Message message = handler.obtainMessage();
                        if (rationSub.getReadingMiddlePostion() == 0) {
                            message.obj = yqNativeAdInfo;
                            message.what = 2;
                            handler.sendMessageDelayed(message, 500);
                        } else if (rationSub.getReadingMiddlePostion() == 1) {
                            message.obj = yqNativeAdInfo;
                            message.what = 4;
                            handler.sendMessageDelayed(message, 500);
                        }
                    } else if (NativeInit.CustomPositionName.READING_IN_CHAPTER_POSITION.toString().equals(rationSub.getMarkId())) {
                        yqNativeAdInfo = getADInfoNew(yqNativeAdInfos, rationSub, keyMd5);
                        if (yqNativeAdInfo == null) {
                            //获取的方法中已经进行了重新请求，此处只需要返回即可
                            continue;
                        }
                        Message message = handler.obtainMessage();
                        message.obj = yqNativeAdInfo;
                        message.what = 3;
                        handler.sendMessageDelayed(message, 500);
                    }
                    if (yqNativeAdInfo != null) {
                        if (com.dingyueads.sdk.Constants.AD_TYPE_OWNAD == ration.getPlatformId()) {
                            StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_adwin_resuc);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void setReadStatus(ReadStatus readStatus) {
        readStatusWeakReference = new WeakReference<>(readStatus);
    }

    /**
     * 获取 1条 ad info
     */
    public synchronized YQNativeAdInfo getSingleADInfo(NativeInit.CustomPositionName type) {
        if (Constants.isHideAD) return null;
        if (isAdDisable()) {
            return null;
        }

        Ration ration = NativeInit.rationManager.getRation(type.toString());
        if (ration == null) {
            return null;
        }
        if (com.dingyueads.sdk.Constants.AD_TYPE_OWNAD == ration.getPlatformId()) {
            StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_adwin);
        }
        String keyMd5 = SDKUtil.digest(ration.getMarkId() + ration.getPlatformId());
        LinkedList<YQNativeAdInfo> yqNativeAdInfos = infoMap.get(keyMd5);

        if (yqNativeAdInfos != null && !yqNativeAdInfos.isEmpty()) {
            YQNativeAdInfo yqNativeAdInfo = getADInfoNew(yqNativeAdInfos, ration, keyMd5);
            if (yqNativeAdInfo != null && yqNativeAdInfo.getAdSceneData() != null) {
                yqNativeAdInfo.getAdSceneData().ad_requestTime = String.valueOf(System.currentTimeMillis() / 1000L);
            }
            return yqNativeAdInfo;
        } else {
            if (!isLoadMore) {
                isLoadMore = true;
                loadAdNew(ration);
                AppLog.e(TAG, "getSingleADInfo == NULL" + " type:" + type.toString());
            }
            return null;
        }
    }

    public synchronized YQNativeAdInfo getSingleADInfoNew(int positon, NativeInit.CustomPositionName type) {
        if (Constants.isHideAD) return null;
        if (isAdDisable()) {
            return null;
        }

        final Ration ration = NativeInit.rationManager.getRation(type.toString());
        if (ration == null) {
            return null;
        }
        if (com.dingyueads.sdk.Constants.AD_TYPE_OWNAD == ration.getPlatformId()) {
            StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_adwin);
        }
        String keyMd5 = SDKUtil.digest(ration.getMarkId() + ration.getPlatformId());
        LinkedList<YQNativeAdInfo> yqNativeAdInfos = infoMap.get(keyMd5);

        if (yqNativeAdInfos != null && !yqNativeAdInfos.isEmpty()) {
            YQNativeAdInfo yqNativeAdInfo = getADInfoNew(yqNativeAdInfos, ration, keyMd5);
            if (yqNativeAdInfo != null && yqNativeAdInfo.getAdSceneData() != null) {
                yqNativeAdInfo.getAdSceneData().ad_requestTime = String.valueOf(System.currentTimeMillis() / 1000L);
            }
            return yqNativeAdInfo;
        } else {

            if (!ration.isRequestingMiddleAd()) {
                isLoadMore = true;
                ration.setRequestingMiddleAd(true);
                ration.setBookShelfAdPosition(positon);
                loadAdNew(ration);
                AppLog.e(TAG, "getSingleADInfo == NULL" + " type:" + type.toString());
            } else {
                ration.setRequestingMiddleAd(false);
            }
            return null;
        }
    }

    public YQNativeAdInfo getADInfoNew(LinkedList<YQNativeAdInfo> yqNativeAdInfos, Ration ration, String keyMd5) {
        if (ration == null) return null;
        YQNativeAdInfo yqNativeAdInfo = null;
        if (yqNativeAdInfos != null && yqNativeAdInfos.size() > 0) {
            yqNativeAdInfo = yqNativeAdInfos.removeFirst();
        }

        if (yqNativeAdInfo == null) {
            //重新请求广告信息
            if (!isLoadMore) {
                isLoadMore = true;
                Ration rationNew = NativeInit.rationManager.getRation(ration.getMarkId());
                if (rationNew == null) {
                    return null;
                }
                if ("8-1".equals(ration.getMarkId())) {
                    if (ration.isRequestingMiddleAd()) return null;
                    rationNew.setReadingMiddlePostion(ration.getReadingMiddlePostion());
                }
                if ("1-1".equals(ration.getMarkId())) {
                    if (ration.isRequestingMiddleAd()) return null;
                    rationNew.setBookShelfAdPosition(ration.getBookShelfAdPosition());
                }
                loadAdNew(rationNew);
            }
            return null;
        }

        if (ration.getPlatformId() == com.dingyueads.sdk.Constants.AD_TYPE_OWNAD ||
                ration.getPlatformId() == com.dingyueads.sdk.Constants.AD_TYPE_360) {
            return yqNativeAdInfo;
        }

        //判断此广告加载因子对应的广告平台信息是否有效
        if (isADinfoAvailable(yqNativeAdInfo, ration)) {
            //判断此条广告信息是否被点击过
            String md5 = SDKUtil.digest(yqNativeAdInfo.getAdvertisement().title + yqNativeAdInfo.getAdvertisement().description);
            boolean flag;
            int status;
            try {
                status = AdDao.getInstance(BaseBookApplication.getGlobalContext()).queryMaterialStatus(md5);
            } catch (Exception e) {
                status = 0;
                e.printStackTrace();
            }

            if (status == 2) {
                flag = true;
                StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_count_remove);
            } else {
                flag = false;
            }
            if (flag) {
                //此条广告信息点击过，移除当前广告条目，选择下一条
                yqNativeAdInfo = getADInfoNew(yqNativeAdInfos, ration, keyMd5);
            }
            if (yqNativeAdInfo == null) return null;
            //获取到没有被点击过的广告信息，再根据展示顺序进行选择
            try {
                md5 = SDKUtil.digest(yqNativeAdInfo.getAdvertisement().title + yqNativeAdInfo.getAdvertisement().description);
                status = AdDao.getInstance(BaseBookApplication.getGlobalContext()).queryMaterialStatus(md5);
            } catch (Exception e) {
                status = 0;
                e.printStackTrace();
            }
            if (status == 1) {
                flag = true;
            } else {
                flag = false;
            }
            if (flag && yqNativeAdInfo.getSort() == false) {
                //根据优先级选择展示顺序，不删，只是选择最合适的广告信息
                yqNativeAdInfo.setSort(true);
                yqNativeAdInfos.add(yqNativeAdInfo);
                yqNativeAdInfo = getADInfoNew(yqNativeAdInfos, ration, keyMd5);
            } else {
                //没有展示过，直接使用此广告信息
            }
            if (yqNativeAdInfo == null) {
                //移除对应平台广告信息
                recycleResource(SDKUtil.digest(ration.getMarkId() + ration.getPlatformId()));
                //重新请求广告信息
                if (!isLoadMore) {
                    isLoadMore = true;
                    loadAdNew(ration);
                    AppLog.e(TAG, "getSingleADInfo == NULL" + " type:" + ration.getMarkId());
                }
                return null;
            }
            AppLog.e("getSingleADInfo", "title = " + yqNativeAdInfo.getAdvertisement().title + " isShowed= " + yqNativeAdInfo.getAdvertisement().isShowed + " yqNativeAdInfos size:" + yqNativeAdInfos.size());

            if (yqNativeAdInfos.contains(yqNativeAdInfo)) {
                yqNativeAdInfos.remove(yqNativeAdInfo);
            }
            return yqNativeAdInfo;
        } else {
            //移除失效的广告信息
            recycleResource(keyMd5);
            //重新请求广告信息
            if (!isLoadMore) {
                isLoadMore = true;
                loadAdNew(ration);
//                AppLog.e(TAG, "广告信息失效---" + " type:" + ration.getMarkId());
            }
            return null;
        }
    }

    public boolean isADinfoAvailable(YQNativeAdInfo yqNativeAdInfo, Ration ration) {
        switch (ration.getPlatformId()) {
            case com.dingyueads.sdk.Constants.AD_TYPE_BAIDU:
                if ((System.currentTimeMillis() - yqNativeAdInfo.getBackTime()) >= com.dingyueads.sdk.Constants.AD_BAIDU_AVAILABLE_TIME) {
                    return false;
                }
                break;
            case com.dingyueads.sdk.Constants.AD_TYPE_GDT:
                if ((System.currentTimeMillis() - yqNativeAdInfo.getBackTime()) >= com.dingyueads.sdk.Constants.AD_GDT_AVAILABLE_TIME) {
                    return false;
                }
                break;
            case com.dingyueads.sdk.Constants.AD_TYPE_360:
                if ((System.currentTimeMillis() - yqNativeAdInfo.getBackTime()) >= com.dingyueads.sdk.Constants.AD_360_AVAILABLE_TIME) {
                    return false;
                }
                break;
            default:
                break;
        }
        return true;
    }

    public void getAdBitmapLandscape(YQNativeAdInfo yqNativeAdInfo) {

        YQNativeAdInfo nativeAdInfo = yqNativeAdInfo;
        if (nativeAdInfo == null || readStatusWeakReference.get() == null) {
            return;
        }
        NetWorkUtils.NATIVE_AD_TYPE = NetWorkUtils.NATIVE_AD_READY;
        readStatusWeakReference.get().currentAdInfo_image = nativeAdInfo;

        AdSceneData adSceneData = nativeAdInfo.getAdSceneData();
        adSceneData.ad_requestTime = String.valueOf(System.currentTimeMillis() / 1000L);

        Advertisement advertisement = nativeAdInfo.getAdvertisement();
        AppLog.e(TAG, "Advertisement: " + advertisement.toString());
        if (advertisement == null) {
            return;
        }

        if (!TextUtils.isEmpty(advertisement.imageUrl)) {
            final String image_url = advertisement.imageUrl;
            ImageCacheManager.getInstance().getImageLoader().get(image_url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                    AppLog.e(TAG, "getAdBitmapLandscape onResponse big image");
                    if (imageContainer == null) return;
                    final Bitmap bitmap_icon = imageContainer.getBitmap();
                    if (bitmap_icon != null) {
                        readStatusWeakReference.get().setAd_bitmap_big(bitmapScaleHeight(bitmap_icon, readStatusWeakReference.get().screenHeight -
                                45 * readStatusWeakReference.get().screenScaledDensity, 0, false));

                    }
                }

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    if (readStatusWeakReference.get() != null) {
                        readStatusWeakReference.get().setAd_bitmap_big(null);
                    }
                }
            });
        } else {
            if (readStatusWeakReference.get() != null) {
                readStatusWeakReference.get().setAd_bitmap_big(null);
            }
        }
    }

    public void getAdBitmapMiddle(YQNativeAdInfo yqNativeAdInfo, final int flag) {
        NetWorkUtils.NATIVE_AD_TYPE = NetWorkUtils.NATIVE_AD_READY;

        YQNativeAdInfo nativeAdInfo = yqNativeAdInfo;
        if (nativeAdInfo == null || readStatusWeakReference.get() == null)
            return;
        //标记此条物料的时间，暂定为10分钟，用于后期此条物料被使用时，判定是否过期
        nativeAdInfo.setAvailableTime(System.currentTimeMillis());

        if (flag == 0) {
            AppLog.e("adtest8-1", "currentAdInfo= " + nativeAdInfo.toString());
            readStatusWeakReference.get().currentAdInfo = nativeAdInfo;
        } else if (flag == 1) {
            AppLog.e("adtest8-1", "currentAdInfoDown= " + nativeAdInfo.toString());
            readStatusWeakReference.get().currentAdInfoDown = nativeAdInfo;
        } else {
            return;
        }

        AdSceneData adSceneData = nativeAdInfo.getAdSceneData();
        adSceneData.ad_requestTime = String.valueOf(System.currentTimeMillis() / 1000L);
        final Advertisement advertisement = nativeAdInfo.getAdvertisement();
        AppLog.e(TAG, "Advertisement: " + advertisement.toString());
        if (advertisement == null) {
            return;
        }

        if (flag == 1) {
            if (!TextUtils.isEmpty(advertisement.iconUrl)) {
                final String icon_url = advertisement.iconUrl;
                ImageCacheManager.getInstance().getImageLoader().get(icon_url, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        LayoutInflater inflater = mInflaterRef.get();
                        Activity act = mActivityRef.get();
                        if (inflater == null || act == null || imageContainer == null) {
                            return;
                        }
                        final RelativeLayout ad_view = (RelativeLayout) inflater.inflate(R.layout.layout_ad_item_small, null);
                        TextView item_ad_title = (TextView) ad_view.findViewById(R.id.item_ad_title);
                        RatingBar item_ad_extension = (RatingBar) ad_view.findViewById(R.id.item_ad_extension);
                        TextView item_ad_desc = (TextView) ad_view.findViewById(R.id.item_ad_desc);

                        if (item_ad_title != null) {
                            item_ad_title.setText(TextUtils.isEmpty(advertisement.title) ? "" : advertisement.title);
                        }
                        if ("night".equals(ResourceUtil.mode)) {
                            if (act != null) {
                                item_ad_title.setTextColor(act.getResources().getColor(R.color.color_gray_9b9b9b));
                                item_ad_desc.setTextColor(act.getResources().getColor(R.color.color_gray_777777));
                            }
                        }

                        String detail = advertisement.description;

                        int length = TextUtils.isEmpty(detail) ? 0 : detail.length();
                        if (item_ad_desc != null) {
                            if (length < default_length) {
                                item_ad_desc.setText(detail);
                            } else {
                                detail = detail.substring(0, default_length) + "...";
                                item_ad_desc.setText(detail);
                            }
                        }
                        item_ad_extension.setRating(Tools.getIntRandom());

                        Bitmap bitmap_icon = imageContainer.getBitmap();

                        if (bitmap_icon != null) {
                            ImageView item_ad_image = (ImageView) ad_view.findViewById(R.id.item_ad_image);
                            Bitmap roundedCornerBitmap = ImageUtils.getRoundedCornerBitmap(bitmap_icon, 40);
                            if (roundedCornerBitmap != null && !roundedCornerBitmap.isRecycled()) {
                                item_ad_image.setImageBitmap(roundedCornerBitmap);
                            }

                            Bitmap logo_bitmap = null;
                            if ("广点通".equals(advertisement.rationName)) {
                                logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_gdt);
                            } else if ("百度".equals(advertisement.rationName)) {
                                logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_bd);
                            } else if ("360".equals(advertisement.rationName)) {
                                logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_360);
                            } else {
                                logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_default);
                            }
                            bitmap_icon = convertViewToBitmap3(ad_view, false);
                            Bitmap bitmap = toConformBitmap(bitmap_icon, logo_bitmap, true, false);
                            if (readStatusWeakReference.get() != null) {
                                readStatusWeakReference.get().setAd_bitmap(bitmap);
                            }
                            if (roundedCornerBitmap != null) {
                                recycleBitmap(roundedCornerBitmap);
                                item_ad_image.setImageBitmap(null);
                            }
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (readStatusWeakReference.get() != null) {
                            readStatusWeakReference.get().setAd_bitmap(null);
                        }
                    }
                });
            } else {
                if (readStatusWeakReference.get() != null) {
                    readStatusWeakReference.get().setAd_bitmap(null);
                }
            }
        }
        if (!TextUtils.isEmpty(advertisement.imageUrl)) {
            final String image_url = advertisement.imageUrl;
            ImageCacheManager.getInstance().getImageLoader().get(image_url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                    Activity act = mActivityRef.get();
                    if (act == null || imageContainer == null) {
                        return;
                    }

                    Bitmap bitmap_icon = imageContainer.getBitmap();
                    if (bitmap_icon == null) return;

                    Bitmap logo_bitmap;
                    if ("广点通".equals(advertisement.rationName)) {
                        logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_gdt);
                    } else if ("百度".equals(advertisement.rationName)) {
                        logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_bd);
                    } else if ("360".equals(advertisement.rationName)) {
                        logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_360);
                    } else {
                        logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_default);
                    }

                    Bitmap bitmap = null;
                    if (readStatusWeakReference.get() != null) {
                        bitmap = bitmapScale(bitmap_icon, readStatusWeakReference.get().screenWidth, 15, true);
                    }

                    if (bitmap != null) {
                        if (readStatusWeakReference.get() != null) {
                            if (flag == 0) {
                                readStatusWeakReference.get().setAd_bitmap_middle(toConformBitmap(bitmap, logo_bitmap, true, true));
                            } else {
                                readStatusWeakReference.get().setAd_bitmap_middle_down(toConformBitmap(bitmap, logo_bitmap, true, true));
                            }
                        }
                    } else {
                        if (readStatusWeakReference.get() != null) {
                            if (flag == 0) {
                                readStatusWeakReference.get().setAd_bitmap_middle(null);
                            } else {
                                readStatusWeakReference.get().setAd_bitmap_middle_down(null);
                            }
                        }
                    }
                }

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    if (readStatusWeakReference.get() != null) {
                        if (flag == 0) {
                            readStatusWeakReference.get().setAd_bitmap_middle(null);
                        } else {
                            readStatusWeakReference.get().setAd_bitmap_middle_down(null);
                        }
                    }
                }
            });
        } else {
            if (readStatusWeakReference.get() != null) {
                if (flag == 0) {
                    readStatusWeakReference.get().setAd_bitmap_middle(null);
                } else {
                    readStatusWeakReference.get().setAd_bitmap_middle_down(null);
                }
            }
        }
    }

    public void getAdBitmapBig(YQNativeAdInfo yqNativeAdInfo) {
        if (Constants.IS_LANDSCAPE) {
            getAdBitmapLandscape(yqNativeAdInfo);
            return;
        }
        YQNativeAdInfo nativeAdInfo = yqNativeAdInfo;
        if (nativeAdInfo == null || readStatusWeakReference.get() == null) {
            return;
        }
        AppLog.e("adtest5-1", yqNativeAdInfo.toString());
        NetWorkUtils.NATIVE_AD_TYPE = NetWorkUtils.NATIVE_AD_READY;
        readStatusWeakReference.get().currentAdInfo_image = nativeAdInfo;
        AdSceneData adSceneData = nativeAdInfo.getAdSceneData();
        if (adSceneData != null) {
            adSceneData.ad_requestTime = String.valueOf(System.currentTimeMillis() / 1000L);
        }
        final Advertisement advertisement = nativeAdInfo.getAdvertisement();
        AppLog.e(TAG, "Advertisement: " + advertisement.toString());
        if (advertisement == null) {
            return;
        }

        if (!TextUtils.isEmpty(advertisement.imageUrl)) {
            final String image_url = advertisement.imageUrl;
            ImageCacheManager.getInstance().getImageLoader().get(image_url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                    AppLog.e(TAG, "onResponse big image");
                    Activity act = mActivityRef.get();
                    if (act == null || imageContainer == null) {
                        return;
                    }
                    Bitmap bitmap_icon = imageContainer.getBitmap();
                    if (bitmap_icon == null) return;

                    Bitmap logo_bitmap;
                    if ("广点通".equals(advertisement.rationName)) {
                        logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_gdt);
                    } else if ("百度".equals(advertisement.rationName)) {
                        logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_bd);
                    } else if ("360".equals(advertisement.rationName)) {
                        logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_360);
                    } else {
                        logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_default);
                    }
                    Bitmap bitmap = null;
                    if (readStatusWeakReference.get() != null) {
                        bitmap = bitmapScale(bitmap_icon, readStatusWeakReference.get().screenWidth, 0, false);
                    }
                    if (bitmap != null) {
                        if (readStatusWeakReference.get() != null) {
                            readStatusWeakReference.get().setAd_bitmap_big(toConformBitmap(bitmap, logo_bitmap, true, true));
                        }
                    }
                }

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    if (readStatusWeakReference.get() != null) {
                        readStatusWeakReference.get().setAd_bitmap_big(null);
                    }
                }
            });
        } else {
            if (readStatusWeakReference.get() != null) {
                readStatusWeakReference.get().setAd_bitmap_big(null);
            }
        }
    }

    public void getAdBitmapBigInChapter(YQNativeAdInfo yqNativeAdInfo) {
        if (Constants.IS_LANDSCAPE) {
            getAdBitmapLandscapeInChapter(yqNativeAdInfo);
            return;
        }
        final YQNativeAdInfo nativeAdInfo = yqNativeAdInfo;
        if (nativeAdInfo == null || readStatusWeakReference.get() == null) {
            return;
        }
        NetWorkUtils.NATIVE_AD_TYPE = NetWorkUtils.NATIVE_AD_READY;
        AdSceneData adSceneData = nativeAdInfo.getAdSceneData();
        adSceneData.ad_requestTime = String.valueOf(System.currentTimeMillis() / 1000L);

        final Advertisement advertisement = nativeAdInfo.getAdvertisement();
        AppLog.e(TAG, "Advertisement: " + advertisement.toString());
        if (advertisement == null) {
            return;
        }

        if (!TextUtils.isEmpty(advertisement.imageUrl)) {
            final String image_url = advertisement.imageUrl;
            ImageCacheManager.getInstance().getImageLoader().get(image_url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                    AppLog.e(TAG, "onResponse big image");
                    Activity act = mActivityRef.get();
                    if (act == null || imageContainer == null) {
                        return;
                    }
                    Bitmap bitmap_icon = imageContainer.getBitmap();
                    if (bitmap_icon == null) return;

                    Bitmap logo_bitmap;
                    if ("广点通".equals(advertisement.rationName)) {
                        logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_gdt);
                    } else if ("百度".equals(advertisement.rationName)) {
                        logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_bd);
                    } else if ("360".equals(advertisement.rationName)) {
                        logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_360);
                    } else {
                        logo_bitmap = BitmapFactory.decodeResource(act.getResources(), R.drawable.icon_ad_default);
                    }
                    Bitmap bitmap = null;
                    if (readStatusWeakReference.get() != null) {
                        bitmap = bitmapScale(bitmap_icon, readStatusWeakReference.get().screenWidth, 0, false);
                    }
                    if (bitmap != null) {
                        if (readStatusWeakReference.get() != null) {
                            HashMap<YQNativeAdInfo, Bitmap> hashMap = new HashMap<YQNativeAdInfo, Bitmap>();
                            hashMap.put(nativeAdInfo, toConformBitmap(bitmap, logo_bitmap, true, true));
                            readStatusWeakReference.get().containerInChapter.add(hashMap);
                        }
                    }
                }

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    if (readStatusWeakReference.get() != null) {
                        readStatusWeakReference.get().setAd_bitmap_big_inChapter(null);
                    }
                }
            });
        } else {
            if (readStatusWeakReference.get() != null) {
                readStatusWeakReference.get().setAd_bitmap_big_inChapter(null);
            }
        }
    }

    public void getAdBitmapLandscapeInChapter(YQNativeAdInfo yqNativeAdInfo) {

        final YQNativeAdInfo nativeAdInfo = yqNativeAdInfo;
        if (nativeAdInfo == null || readStatusWeakReference.get() == null) {
            return;
        }
        NetWorkUtils.NATIVE_AD_TYPE = NetWorkUtils.NATIVE_AD_READY;

        AdSceneData adSceneData = nativeAdInfo.getAdSceneData();
        adSceneData.ad_requestTime = String.valueOf(System.currentTimeMillis() / 1000L);

        Advertisement advertisement = nativeAdInfo.getAdvertisement();
        AppLog.e(TAG, "Advertisement: " + advertisement.toString());
        if (advertisement == null) {
            return;
        }

        if (!TextUtils.isEmpty(advertisement.imageUrl)) {
            final String image_url = advertisement.imageUrl;
            ImageCacheManager.getInstance().getImageLoader().get(image_url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                    AppLog.e(TAG, "getAdBitmapLandscape onResponse big image");
                    if (imageContainer == null) return;
                    final Bitmap bitmap_icon = imageContainer.getBitmap();
                    if (bitmap_icon != null) {
                        if (readStatusWeakReference.get() != null) {
                            HashMap<YQNativeAdInfo, Bitmap> hashMap = new HashMap<YQNativeAdInfo, Bitmap>();
                            hashMap.put(nativeAdInfo, bitmapScaleHeight(bitmap_icon, readStatusWeakReference.get().screenHeight -
                                    45 * readStatusWeakReference.get().screenScaledDensity, 0, false));
                            readStatusWeakReference.get().containerInChapter.add(hashMap);
                        }
                    }
                }

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    if (readStatusWeakReference.get() != null) {
                        readStatusWeakReference.get().setAd_bitmap_big_inChapter(null);
                    }
                }
            });
        } else {
            if (readStatusWeakReference.get() != null) {
                readStatusWeakReference.get().setAd_bitmap_big_inChapter(null);
            }
        }
    }

    public Bitmap bitmapScaleHeight(Bitmap bitmap, float screenheight, float left, boolean isMiddle) {
        Matrix matrix = new Matrix();
        float bitmapHeight = (screenheight - 2 * left) / bitmap.getHeight();
        matrix.postScale(bitmapHeight, bitmapHeight); // 长和宽放大缩小的比例
        if (bitmap.isRecycled()) {
            return null;
        }
        Bitmap resizeBmp;
        try {
            resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            System.gc();
            return null;
        }
        if (readStatusWeakReference.get() != null) {
            if (isMiddle) {
                readStatusWeakReference.get().height_middle_nativead = resizeBmp.getHeight();
                readStatusWeakReference.get().width_nativead_middle = resizeBmp.getWidth();
            } else {
                readStatusWeakReference.get().width_nativead_big = resizeBmp.getWidth();
                readStatusWeakReference.get().height_nativead_big = resizeBmp.getHeight();
            }
            AppLog.e(TAG, "bitmapScaleHeight resizeBmp.getHeight():" + resizeBmp.getHeight() + " width:" + resizeBmp.getWidth() + " bitmapHeight:" +
                    bitmapHeight
                    + " screenheight:" + screenheight + " width:" + readStatusWeakReference.get().screenWidth);
        }
        if (bitmap != resizeBmp) {
            recycleBitmap(bitmap);
        }
        return resizeBmp;
    }

    public Bitmap bitmapScale(Bitmap bitmap, float screenWidth, float left, boolean isMiddle) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        Matrix matrix = new Matrix();
        float bitmapWidth = (screenWidth - 2 * left) / bitmap.getWidth();
        matrix.postScale(bitmapWidth, bitmapWidth); // 长和宽放大缩小的比例
        Bitmap resizeBmp;
        try {
            resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            System.gc();
            return null;
        }
        if (readStatusWeakReference.get() != null) {
            if (isMiddle) {
                readStatusWeakReference.get().height_middle_nativead = resizeBmp.getHeight();
                readStatusWeakReference.get().width_nativead_middle = resizeBmp.getWidth();
            } else {
                readStatusWeakReference.get().width_nativead_big = resizeBmp.getWidth();
                readStatusWeakReference.get().height_nativead_big = resizeBmp.getHeight();
            }
        }
        if (bitmap != resizeBmp) {
            recycleBitmap(bitmap);
        }
        return resizeBmp;
    }

    public Bitmap convertViewToBitmap3(RelativeLayout view, boolean isBig_ad) {
        Bitmap bitmap = null;
        try {
            if (readStatusWeakReference.get() == null)
                return null;
            /**防止 部分手机measure null 异常*/
            view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec
                    .UNSPECIFIED));
            view.layout(0, 0, readStatusWeakReference.get().screenWidth - 10, view.getMeasuredHeight());
            AppLog.e(TAG, "width:" + view.getMeasuredWidth() + " height:" + view.getMeasuredHeight());
            if (isBig_ad) {
                readStatusWeakReference.get().width_nativead_big = view.getMeasuredWidth();
                readStatusWeakReference.get().height_nativead_big = view.getMeasuredHeight();
            } else {
                readStatusWeakReference.get().width_nativead = view.getMeasuredWidth();
                readStatusWeakReference.get().height_nativead = view.getMeasuredHeight();
            }
            view.buildDrawingCache();
            bitmap = view.getDrawingCache();
        } catch (Exception e) {
            AppLog.e(TAG, "e:" + e.toString());
            e.printStackTrace();
        }
        return bitmap;
    }

    public boolean isAdDisable() {
        return NetWorkUtils.getNetWorkType(BaseBookApplication.getGlobalContext()) == NetWorkUtils.NETWORK_NONE;
    }

    public void release() {
        adInfoLinkedList = null;
        infoMap = null;
    }

    /**
     * 根据广告位置删除映射表里面存储的信息
     */
    public void recycleResourceFromReading(String positionName) {
        if (NativeInit.rationManager != null) {
            ADPlatform platform = NativeInit.rationManager.getPlatForm(positionName);
            if (platform == null) return;

            List<Ration> rationList = platform.getRationList();
            if (rationList == null || rationList.isEmpty()) return;

            String md5;
            for (Ration rationSub : rationList) {
                md5 = SDKUtil.digest(rationSub.getMarkId() + rationSub.getPlatformId());
                if (infoMap != null) {
                    if (infoMap.containsKey(md5)) {
                        AppLog.e("OwnNativeAdManager", "OwnNativeAdManager : recycleBitmap " + positionName);
                        infoMap.get(md5).clear();
                        infoMap.remove(md5);
                    }
                }
            }
        }
    }

    public void recycleResource(String positionName) {
        if (infoMap != null) {
            if (infoMap.containsKey(positionName)) {
                AppLog.e("OwnNativeAdManager", "OwnNativeAdManager : recycleBitmap " + positionName);
                infoMap.get(positionName).clear();
                infoMap.remove(positionName);
            }
        }
    }

    public void removeHandler() {
        if (handler != null) {
            handler.removeMessages(1);
            handler.removeMessages(2);
            handler.removeMessages(3);
            handler.removeMessages(4);
        }
    }

    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            AppLog.e(TAG, "recycleBitmap");
            bitmap.recycle();
            bitmap = null;
        }
    }

    private Bitmap toConformBitmap(Bitmap background, Bitmap prospect, boolean isDrawLogo, boolean isRGB565) {

        if (background != null && prospect != null) {

            //对Bitmap是否被回收进行判断
            if (background.isRecycled() || prospect.isRecycled()) {
                recycleBitmap(background);
                recycleBitmap(prospect);
                return null;
            }

            int backgroundWidth = background.getWidth();
            int backgroundHeight = background.getHeight();

            int prospectWidth = prospect.getWidth();
            int prospectHeight = prospect.getHeight();

            Bitmap bitmap;
            try {
                if (isRGB565) {
                    bitmap = Bitmap.createBitmap(backgroundWidth, backgroundHeight, Bitmap.Config.RGB_565);
                } else {
                    bitmap = Bitmap.createBitmap(backgroundWidth, backgroundHeight, Bitmap.Config.ARGB_8888);
                }
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                System.gc();
                return null;
            }

            if (bitmap.isRecycled()) {
                return null;
            }
            Canvas cv = new Canvas(bitmap);
            cv.drawBitmap(background, 0, 0, null);
            if (isDrawLogo) {
                Activity act = mActivityRef.get();
                float i = 0;
                if (act != null) {
                    i = AppUtils.sp2px(act.getResources(), 55);
                }
                if (isRGB565) {
                    cv.drawBitmap(prospect, backgroundWidth - prospectWidth, backgroundHeight - prospectHeight, null);
                } else {
                    cv.drawBitmap(prospect, backgroundWidth - prospectWidth - 20, backgroundHeight - prospectHeight - i, null);
                }
            }
            cv.save(Canvas.ALL_SAVE_FLAG);
            cv.restore();
            recycleBitmap(background);
            recycleBitmap(prospect);
            return bitmap;
        } else {
            recycleBitmap(background);
            recycleBitmap(prospect);
            return null;
        }
    }

    //开屏广告展示成功时调用 统计展示次数
    @Override
    public void onAdPresent(AdSceneData adSceneData) {
        if (statisticManager != null) {
            // 老的开屏广告展示统计请求
            if (adSceneData != null && !TextUtils.isEmpty(adSceneData.ad_markId)) {
                adSceneData.ad_showSuccessTime = String.valueOf(System.currentTimeMillis() / 1000L);
                adSceneData.ad_show = 1;
            }
        }
    }

    @Override
    public void onAdDismissed(AdSceneData adSceneData) {
        if (splashHandler != null) {
            splashHandler.sendEmptyMessage(handlerMsgCode);
        }
        if (statisticManager != null && adSceneData != null) {
            adSceneData.ad_showFinishTime = String.valueOf(System.currentTimeMillis() / 1000L);
            statisticManager.sendAdSceneData(adSceneData);
        }
    }

    //百度开屏广告展示失败时调用
    @Override
    public void onAdFailed(AdSceneData adSceneData, String s) {
        if (splashHandler != null) {
            splashHandler.sendEmptyMessageDelayed(handlerMsgCode, 5000L);
        }

        if (statisticManager != null && adSceneData != null) {
            adSceneData.ad_requestFailureTime = String.valueOf(System.currentTimeMillis() / 1000L);
            adSceneData.ad_requestFailureReason = s;
            statisticManager.sendAdFailData(adSceneData);
        }

        Ration ration = NativeInit.rationManager.getRation(NativeInit.CustomPositionName.SPLASH_SUPPLY_POSITION.toString());
        if (ration == null) {
            return;
        }
        if (nativeInit != null) {
            nativeInit.loadAdNew(1, ration, mActivityRef);
        }
        if (com.dingyueads.sdk.Constants.AD_TYPE_OWNAD == ration.getPlatformId()) {
            StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_adwin);
        }
    }

    //广点通的点击回调
    @Override
    public void onAdGDTClick(final AdSceneData adSceneData) {
        // 广点通开屏、切屏广告的点击回调
        if (adSceneData != null && !TextUtils.isEmpty(adSceneData.ad_markId)) {
            adSceneData.ad_clickSuccessTime = String.valueOf(System.currentTimeMillis() / 1000L);
            adSceneData.ad_click = 1;
        }
    }

    //百度的点击回调
    @Override
    public void onAdBaiDuClick(AdSceneData adSceneData) {
        if (adSceneData != null && !TextUtils.isEmpty(adSceneData.ad_markId)) {
            adSceneData.ad_clickSuccessTime = String.valueOf(System.currentTimeMillis() / 1000L);
            adSceneData.ad_click = 1;
        }
    }

    // 此处请求广点通开屏和信息流原生广告都调用此方法
    @Override
    public void onNoAD(AdSceneData adSceneData, int a) {
        if (splashHandler != null) {
            splashHandler.sendEmptyMessageDelayed(handlerMsgCode, 1000L);
        }
    }

    @Override
    public void onADTick(long l) {
    }

    @Override
    public void onInitsuccess() {
        //加判断防止广告网络初始化结束后 开屏广告加载两次和 第一次进入时位置覆盖APP书架广告不显示的问题
        if (hasInitConfig && isLoadSplash) {
            hasInitConfig = false;
            isLoadSplash = false;
            loadSplashAd(NativeInit.CustomPositionName.SPLASH_POSITION, viewGroup, false);
        }
    }

    @Override
    public void onInitFail() {
        if (splashHandler != null) {
            splashHandler.sendEmptyMessage(handlerMsgCode);
        }
    }

    @Override
    public void onBaiduClickSplash() {
        StatServiceUtils.statAdEventShow(BaseBookApplication.getGlobalContext(), StatServiceUtils.ad_adwin);
    }

    public class AdHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    getAdBitmapBig((YQNativeAdInfo) msg.obj);
                    break;
                case 2:
                    getAdBitmapMiddle((YQNativeAdInfo) msg.obj, 0);
                    break;
                case 3:
                    getAdBitmapBigInChapter((YQNativeAdInfo) msg.obj);
                    break;
                case 4:
                    getAdBitmapMiddle((YQNativeAdInfo) msg.obj, 1);
                    break;
            }
        }
    }
}

