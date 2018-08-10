package com.intelligent.reader.receiver;

import com.dingyue.contract.util.SharedPreUtil;
import com.intelligent.reader.util.DynamicParameter;
import com.umeng.message.PushAgent;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.book.component.service.CheckNovelUpdateService;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.ExtensionsKt;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.OpenUDID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ConnectionChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectionChangeReceiver";
    private static final int G0 = 0;
    private static final int G2 = 2;
    private static final int G3 = 3;
    private static final int G4 = 4;
    private static final int WIFI = 5;
    public static int mobileType;
    private static boolean canReload = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            AppLog.d(TAG, "ConnectivityReceiver.onReceive()...");

            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                int nType = networkInfo.getType();
                if (nType == ConnectivityManager.TYPE_MOBILE) {
                    NetWorkUtils.NETWORK_TYPE = NetWorkUtils.NETWORK_MOBILE;
                    mobileType = getMobileType(networkInfo.getSubtype());

                    CacheManager.INSTANCE.onNetTypeChange();

                } else if (nType == ConnectivityManager.TYPE_WIFI) {
                    NetWorkUtils.NETWORK_TYPE = NetWorkUtils.NETWORK_WIFI;
                    mobileType = WIFI;

                    CacheManager.INSTANCE.onNetTypeChange();

                } else {
                    NetWorkUtils.NETWORK_TYPE = NetWorkUtils.NETWORK_NONE;
                    mobileType = G0;
                }
                AppLog.d(TAG, "Network Type  = " + networkInfo.getTypeName());
                AppLog.d(TAG, "Network State = " + networkInfo.getState());
                if (networkInfo.isConnected()) {
                    //更新标签
                    updatePushTags(context);
                }
                if (networkInfo.isConnected() && canReload) {
                    canReload = false;
                    AppLog.i(TAG, "Network connected");
                    Intent intent_service = new Intent();
                    intent_service.setClass(context, CheckNovelUpdateService.class);
                    intent_service.setAction(CheckNovelUpdateService.ACTION_CHKUPDATE);
                    context.startService(intent_service);
                }

                //当连接网络时，重新加载动态参数，如果成功过一次后续网络发生变化时不再加载
                if (nType == ConnectivityManager.TYPE_WIFI || nType == ConnectivityManager.TYPE_MOBILE) {
                    if (DynamicParameter.isReloadDynamic()) {
                        DynamicParameter.setReloadDynamic(false);
                        DynamicParameter dynamicParameter = new DynamicParameter(context);
                        dynamicParameter.setDynamicParameter();
                    }
                }
            } else {
                canReload = true;
                AppLog.e(TAG, "Network unavailable");
                NetWorkUtils.NETWORK_TYPE = NetWorkUtils.NETWORK_NONE;
                mobileType = G0;
            }
        }
    }

    private void updatePushTags(Context context) {
        String udid = OpenUDID.getOpenUDIDInContext(context);
        final SharedPreUtil share = new SharedPreUtil(SharedPreUtil.Companion.getSHARE_DEFAULT());
        Long latestUpdateTime = share.getLong(SharedPreUtil.PUSH_TAG_LATEST_UPDATE_TIME, 0);
        final Long currentTime = System.currentTimeMillis();
        boolean isSameDay = AppUtils.isToday(latestUpdateTime, currentTime);
        if (!isSameDay) {
            ExtensionsKt.updateTags(PushAgent.getInstance(context), context, udid,
                    new Function1<Boolean, Unit>() {
                        @Override
                        public Unit invoke(Boolean isSuccess) {
                            if (isSuccess) {
                                share.putLong(SharedPreUtil.PUSH_TAG_LATEST_UPDATE_TIME,
                                        currentTime);
                            }
                            return null;
                        }
                    });
        }
    }

    private int getMobileType(int subType) {
        switch (subType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return G2; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return G2; // ~ 14-64 kbps
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return G2; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return G3; // ~ 400-1000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return G3; // ~ 600-1400 kbps
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return G2; // ~ 100 kbps
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return G3; // ~ 2-14 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return G3; // ~ 700-1700 kbps
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return G3; // ~ 1-23 Mbps
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return G3; // ~ 400-7000 kbps
            // NOT AVAILABLE YET IN API LEVEL 7
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return G3; // ~ 1-2 Mbps
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return G3; // ~ 5 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return G3; // ~ 10-20 Mbps
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return G2; // ~25 kbps
            case TelephonyManager.NETWORK_TYPE_LTE:
                return G4; // ~ 10+ Mbps
            // Unknown
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return G2;
            default:
                return G2;
        }
    }
}