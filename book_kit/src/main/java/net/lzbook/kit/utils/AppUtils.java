package net.lzbook.kit.utils;

import static android.content.Context.BATTERY_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;

import com.meituan.android.walle.WalleChannelReader;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReplaceConstants;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppUtils {
    public static final int LOG_TYPE_BAIDUPUSH = 0;
    public static final int LOG_TYPE_ESCARD_PAY = LOG_TYPE_BAIDUPUSH + 1;
    public static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat min_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat log_formatter = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    public final static String PERMISSION_GPS = "gps";
    final static String TAG = "AppUtils";
    private static final String text_discard = "（该网页已经技术转换）";
    /**
     * 获取版本信息
     */
    private static final String sLock = "LOCK";
    public static int density;
    public static int width = 0;
    public static long lastClickTime;

    private static String APPLICATION_ID = null;
    private static String VERSION_NAME = null;
    private static int VERSION_CODE = 0;
    private static String CHANNEL_NAME = null;

    public static void initDensity(Context ctt) {
        DisplayMetrics dis = ctt.getResources().getDisplayMetrics();
        density = dis.widthPixels * dis.heightPixels;
        width = dis.widthPixels;
    }

    public static float sp2px(Resources resources, float sp) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    /**
     * <根据手机的分辨率从 dp 的单位 转成为 px(像素)>
     * <p/>
     * context
     * dpValue
     * int
     */
    public static int dip2px(Context context, float dpValue) {
        if (context == null) {
            return -1;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static String deleteTextPoint(String text) {
        text = text.trim();

        int index = text.indexOf(text_discard);
        if (index != -1) {
            text = text.substring(index + text_discard.length()).trim();
        }

        while (text.indexOf("　") == 0 || text.indexOf(" ") == 0
                || text.indexOf("	") == 0) {
            text = text.substring(1);
        }
        try {
            Pattern P = Pattern.compile("^\\p{Punct}", Pattern.UNIX_LINES);
            Matcher M = P.matcher(text);
            while (M.find()) {
                text = text.substring(1);
                M = P.matcher(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (text.indexOf("　") == 0 || text.indexOf(" ") == 0
                || text.indexOf("	") == 0) {
            text = text.substring(1);
        }
        return text.trim();
    }

    public static String timeFormat(long ms) {// 将毫秒数换算成x天x时x分x秒x毫秒
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        long day = ms / dd;
        long hour = (ms - day * dd) / hh;
        long minute = (ms - day * dd - hour * hh) / mi;
        long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        long milliSecond = ms - day * dd - hour * hh - minute * mi - second
                * ss;

        String strDay = day < 10 ? "0" + day : "" + day;
        String strHour = hour < 10 ? "0" + hour : "" + hour;
        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String strSecond = second < 10 ? "0" + second : "" + second;
        String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : ""
                + milliSecond;
        strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : ""
                + strMilliSecond;
        return strDay + " 天" + strHour + " 小时" + strMinute + " 分" + strSecond
                + " 秒" + strMilliSecond + " 毫秒";
    }

    public static boolean isLaterDay(long time) {
        Date date = new Date();
        if (time >= 0) {
            long l = date.getTime() - time;
            long hour = (l / (60 * 60 * 1000));
            if (hour > 24) {
                return true;
            }
            return false;
        }
        AppLog.e("lq", "isLaterDay :" + isLaterDay(time));
        AppLog.d(TAG, "isLaterDay " + isLaterDay(time));
        return false;
    }

    public static void setLongPreferences(Context context, String preName,
            Long value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
                .edit();
        editor.putLong(preName, value);
        editor.apply();
    }

    public static void setBooleanPreferences(Context context, String preName,
            boolean value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
                .edit();
        editor.putBoolean(preName, value);
        editor.apply();
    }

    public static boolean getBooleanPreferences(Context context,
            String preName, boolean defaultValue) {
        SharedPreferences defaultPf = PreferenceManager
                .getDefaultSharedPreferences(context);
        return defaultPf.getBoolean(preName, defaultValue);
    }

    public static long getLongPreferences(Context context, String preName,
            long defaultValue) {
        SharedPreferences defaultPf = PreferenceManager
                .getDefaultSharedPreferences(context);
        return defaultPf.getLong(preName, defaultValue);
    }

    /**
     * 获取当前系统的时间格式
     * <p/>
     * context
     */
    public static String getTimeFormat(Context context) {
        String strTimeFormat = null;
        try {
            ContentResolver cv = context.getContentResolver();
            strTimeFormat = android.provider.Settings.System.getString(cv,
                    android.provider.Settings.System.TIME_12_24);
            if (isStrEmpty(strTimeFormat)) {
                strTimeFormat = nullStrToEmpty("24");// 系统时间格式默认设置为24小时制
            }
            AppLog.d(TAG, "getTimeFormat " + strTimeFormat);
        } catch (Exception e) {
            AppLog.e(TAG, "getTimeFormat error");
            e.printStackTrace();
        }
        return strTimeFormat;
    }

    // 增加或减少天数
    public static Date addDay(Date date, int num) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, num);
        return startDT.getTime();
    }

    public static boolean isStrEmpty(String str) {
        return (str == null || str.length() == 0);
    }

    public static String nullStrToEmpty(String str) {
        return (str == null ? "" : str);
    }

    /**
     * 判断目录数据库是否存在
     * <p/>
     * context
     * gid
     */
    public static boolean isChapterDBexist(Context context, String book_id) {
        return isDBexist(context, "book_chapter_" + book_id);
    }

    /**
     * 判断数据库是否存在
     * <p/>
     * context
     * name
     */
    public static boolean isDBexist(Context context, String name) {
        try {
            File file = context.getDatabasePath(name);
            if (file != null && file.exists()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    //2012-10-03 23:41:31
    private static String getCurTime2() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String s = format1.format(new Date(System.currentTimeMillis()));
        return (String) s;

    }

    /*
     * 测试 日志保存到sdcard
     */
    public static void appendLog(String text, int type) {
        if (!Constants.DEVELOPER_MODE) {
            return;
        }
        try {
            File dir = createSDDir(ReplaceConstants.getReplaceConstants().APP_PATH_DOWNLOAD);// 创建目录
            String path = "";
            if (type == LOG_TYPE_BAIDUPUSH) {
                path = "qbmfydq_baidupush_log.txt";
            } else if (type == LOG_TYPE_ESCARD_PAY) {
                path = "qbmfydqcard_pay_log.txt";
            }
            File file = new File(dir.getPath() + "/" + path);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
            buf.append(getCurTime2());
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File createSDDir(String dirName) {
        File dir;
        if (dirName == null) {
            dirName = "";
        }
        dir = new File(dirName);
        try {
            dir.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dir;
    }

    /**
     * 系统版本号
     */
    public static String getRelease() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取内核版本
     */
    public static String getSystemInnerVersion() {
        String ver;
        if (android.os.Build.DISPLAY.contains(android.os.Build.VERSION.INCREMENTAL)) {
            ver = android.os.Build.DISPLAY;
        } else {
            ver = android.os.Build.VERSION.INCREMENTAL;
        }
        return ver;
    }

    /**
     * 设备厂商
     */
    public static String getPhoneBrand() {
        return Build.BOARD + "" + Build.MANUFACTURER;
    }

    /**
     * 设备名称
     */
    public static String getPhoneModel() {
        return Build.MODEL;
    }

    /**
     * X86架构
     */
    public static String getX86() {
        return Build.CPU_ABI;
    }

    /**
     * X86架构
     */
    public static String getBatteryLevel() {
        int level = 0;
        //API 21 之后用 BATTERY_SERVICE 主动去获取电量
        if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager)BaseBookApplication.getGlobalContext().getSystemService(BATTERY_SERVICE);
            if(batteryManager != null){
                level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            }
        }else{
            Intent batteryInfoIntent = BaseBookApplication.getGlobalContext()
                    .registerReceiver(null,
                            new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            level = batteryInfoIntent != null ? batteryInfoIntent.getIntExtra("level", 0) : 0;
        }
        return level + "%";
    }


    /**
     * 获取手机号码
     */
    public static String getPhoneNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        String _PhoneNumbber = "";
        if (tm != null) {
            _PhoneNumbber = tm.getLine1Number();
        }
        if (_PhoneNumbber != null && _PhoneNumbber.length() > 0) {
            _PhoneNumbber = _PhoneNumbber.replace("#", "");
            _PhoneNumbber = _PhoneNumbber.replace("*", "");
        }
        return _PhoneNumbber;
    }

    /**
     * 获取运营商信息
     */
    public static String getProvidersName(Context context) {
        String providersName = "";
        if (context != null) {
            if (checkPermission(context)) {
                try {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(
                            TELEPHONY_SERVICE);
                    String subscriberId = telephonyManager.getSubscriberId();
                    if (subscriberId != null) {
                        if (subscriberId.startsWith("46000") || subscriberId.startsWith("46002")
                                || subscriberId.startsWith("46007")) {
                            providersName = "中国移动";
                        } else if (subscriberId.startsWith("46001")) {
                            providersName = "中国联通";
                        } else if (subscriberId.startsWith("46003")) {
                            providersName = "中国电信";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        return providersName;
    }

    /**
     * 获取IMEI（设备串号）
     */
    public static String getIMEI(Context context) {
        String deviceId = "";
        try {

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(
                    TELEPHONY_SERVICE);
            deviceId = telephonyManager.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceId;
    }

    /**
     * 判读是否启动VPN
     */
    public static boolean getIsVPNUsed() {
        try {
            Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
            if (niList != null) {
                for (NetworkInterface networkInterface : Collections.list(niList)) {
                    if (!networkInterface.isUp()
                            || networkInterface.getInterfaceAddresses().size() == 0) {
                        continue;
                    }
                    if ("tun0".equals(networkInterface.getName()) || "ppp0".equals(
                            networkInterface.getName())) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 获取无线局域网 WLAN MAC Address
     */
    public static String getWLanMacAddress(Context context) {
        try {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            return wm.getConnectionInfo().getMacAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取MAC地址
     */
    public static String getMacAddress(Context context) {
        try {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            return wm.getConnectionInfo().getMacAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取在WiFi环境下，获取当前连接路由器的Mac地址
     */
    public static String getWifiMacAddress(Context context) {
        try {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            return wm.getConnectionInfo().getBSSID();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 获取IP地址
     */
    public static String getIPAddress(Context context) {
        String ip = "";
        //这里不应该每次都重新获得的, 应该在receiver里 收到网络状态的时候, 获取一次然后做记录, 偷懒了先catch一下吧
        try {
            ConnectivityManager conMann = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobileNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mobileNetworkInfo != null && mobileNetworkInfo.isConnected()) {//移动网络
                ip = getLocalIpAddress();
            } else if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) {//wifi网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                ip = getWifiIPAddress(ipAddress);
            }
        }catch (Throwable t){

        }
        return ip;
    }

    //如果连接的是移动网络
    private static String getLocalIpAddress() {
        try {
            String ipv4;
            ArrayList<NetworkInterface> nilist = Collections.list(
                    NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : nilist) {
                ArrayList<InetAddress> ialist = Collections.list(ni.getInetAddresses());
                for (InetAddress address : ialist) {
                    if (!address.isLoopbackAddress() && InetAddressUtils.isIPv4Address(
                            ipv4 = address.getHostAddress())) {
                        return ipv4;
                    }
                }
            }
        } catch (SocketException e) {

        }
        return null;
    }

    // 如果连接的是WI-FI网络
    private static String getWifiIPAddress(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    /**
     * 获取蓝牙ID
     */
    public static String getBluetoothID() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                return bluetoothAdapter.getAddress();
            }
        }
        return "";

    }

    /**
     * 获得SD卡总大小
     */
    public static String getSDTotalSize(Context context) {
        try {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return Formatter.formatFileSize(context, blockSize * totalBlocks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     */
    public static String getSDAvailableSize(Context context) {
        try {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return Formatter.formatFileSize(context, blockSize * availableBlocks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取CPU型号
     */
    public static String getCpuName() {


        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

//
//        String str1 = "/proc/cpuinfo";//CPU的型号
//        String str2 = "";//CPU的频率
//
//        String[] cpuInfo = {"", ""};
//        String[] arrayOfString;
//        try {
//            FileReader fr = new FileReader(str1);
//            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
//            str2 = localBufferedReader.readLine();
//            arrayOfString = str2.split("\\s+");
//            for (int i = 2; i < arrayOfString.length; i++) {
//                cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
//            }
//            str2 = localBufferedReader.readLine();
//            arrayOfString = str2.split("\\s+");
//            cpuInfo[1] += arrayOfString[2];
//            localBufferedReader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return cpuInfo[0] + cpuInfo[1];


    }

    /**
     * 获取包名
     */
    public static String getPackageName() {
        initValues();
        return APPLICATION_ID;
    }

    public static boolean hasUPush() {
        String packageName = getPackageName();
        return packageName.equals("cc.remennovel") //智胜电子书
                || packageName.equals("cc.kdqbxs.reader") //快读替
                || packageName.equals("cc.quanbennovel") //今日多看
                || packageName.equals("cc.lianzainovel") //鸿雁替
                || packageName.equals("cc.mianfeinovel"); //阅微替
    }

    public static boolean hasReYun() {
        String packageName = getPackageName();
        return packageName.equals("cn.qbmfkkydq.reader");
    }
    /**
     * 获取渠道号
     */
    public static String getChannelId() {
        initValues();
        return CHANNEL_NAME;
    }

    /**
     * 获取当前版本名
     */
    public static String getVersionName() {
        initValues();
        return VERSION_NAME;
    }

    /**
     * 获取版本号
     */
    public static int getVersionCode() {
        initValues();
        return VERSION_CODE;
    }

    /**
     * 获取屏幕分辨率
     */
    public static String getScreenMetrics(Context context) {
        WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(dm);
        }
        int mScreenWidth = dm.widthPixels;
        int mScreenHeight = dm.heightPixels;
        return (mScreenWidth + " * " + mScreenHeight);
    }


    /**
     * 获取网络状态
     */
    public static String getNetState(Context context) {
        //结果返回值
        String netType = "无网络";
        //获取手机所有连接管理对象
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        //获取NetworkInfo对象
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        //NetworkInfo对象为空 则代表没有网络
        if (networkInfo == null) {
            return netType;
        }
        //否则 NetworkInfo对象不为空 则获取该networkInfo的类型
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            //WIFI
            netType = "wifi";
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(
                    Context.TELEPHONY_SERVICE);
            //4G
            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
                    && telephonyManager != null && !telephonyManager.isNetworkRoaming()) {
                netType = "4G";
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    && !telephonyManager.isNetworkRoaming()) {
                netType = "3G";
                //2G 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                    || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                    || nSubType == TelephonyManager.NETWORK_TYPE_CDMA
                    && !telephonyManager.isNetworkRoaming()) {
                netType = "2G";
            } else {
                netType = "2G";
            }
        }
        return netType;
    }


    public static String getMetrics(Context context) {
        String metric = null;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        metric = width + height + "";
        return metric;
    }


    private static void initValues() {
        if (TextUtils.isEmpty(CHANNEL_NAME) || CHANNEL_NAME.equals("DEBUG")) {
            try {
                Class<?> buildConfig = Class.forName("com.intelligent.reader.BuildConfig");
                APPLICATION_ID = getStringField("APPLICATION_ID", buildConfig);
                VERSION_NAME = getStringField("VERSION_NAME", buildConfig);
                VERSION_CODE = getIntField("VERSION_CODE", buildConfig);
//                CHANNEL_NAME = getStringField("CHANNEL_NAME", buildConfig);
                if (!TextUtils.isEmpty(
                        WalleChannelReader.getChannel(BaseBookApplication.getGlobalContext()))) {
                    CHANNEL_NAME = WalleChannelReader.getChannel(
                            BaseBookApplication.getGlobalContext());
                } else {
                    CHANNEL_NAME = "DEBUG";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String getStringField(String fieldName, Object obj)
            throws NoSuchFieldException, IllegalAccessException {
        Class clazz = null;
        if (obj instanceof Class) {
            clazz = (Class) obj;
        } else {
            clazz = obj.getClass();
        }
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(obj);
    }

    private static int getIntField(String fieldName, Object obj)
            throws NoSuchFieldException, IllegalAccessException {
        Class clazz = null;
        if (obj instanceof Class) {
            clazz = (Class) obj;
        } else {
            clazz = obj.getClass();
        }
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getInt(obj);
    }


    public static boolean isToday(long first_time, long currentTime) {
        Calendar pre = Calendar.getInstance();
        Date predate = new Date(currentTime);
        pre.setTime(predate);

        Calendar cal = Calendar.getInstance();
        Date date = new Date(first_time);
        cal.setTime(date);

        if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
            int sameDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR);
            if (sameDay == 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * 删除所有的非法字符
     *
     * @param word 需要进行除非法字符的文本
     */
    public static String deleteAllIllegalChar(String word) {
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？￣ ]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(word);
        word = m.replaceAll("").trim();
        String regEx1 = "\\\\";
        Pattern p1 = Pattern.compile(regEx1);
        Matcher m1 = p1.matcher(word);
        word = m1.replaceAll("").trim();
        return word;
    }

    public static boolean isDoubleClick(long timeMills) {
        if (timeMills - lastClickTime < 800) {
            lastClickTime = timeMills;
            return true;
        } else {
            lastClickTime = timeMills;
            return false;
        }
    }

    public static int getRandomColor() {
        Random random = new Random();
        String[] ranColor = {"#e093a7", "#83b6dd"};
        String color = ranColor[random.nextInt(ranColor.length)];
        return Color.parseColor(color);
    }

    //获取手机唯一标识符
    public static String getUniqueCode(Context context) {

        //获取手机IMEI
        TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(
                TELEPHONY_SERVICE);
        String IMEI = "";
        if (TelephonyMgr != null) {
            IMEI = TelephonyMgr.getDeviceId();
        }

        //获取WLAN MAC Address
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String WLANMAC = wm.getConnectionInfo().getMacAddress();


        //获取蓝牙ID
        BluetoothAdapter mBlueth = BluetoothAdapter.getDefaultAdapter();
        String BluethId = mBlueth.getAddress();

        String m_szLongID = IMEI + WLANMAC + BluethId;
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
        byte p_md5Data[] = m.digest();

        String m_szUniqueID = new String();
        for (int i = 0; i < p_md5Data.length; i++) {
            int b = (0xFF & p_md5Data[i]);
            if (b <= 0xF) {
                m_szUniqueID += "0";
            }
            m_szUniqueID = m_szUniqueID.toUpperCase();
        }
        return m_szUniqueID;
    }


    //获取用户手机安装的所有app列表
    public static String scanLocalInstallAppList(PackageManager packageManager) {
        StringBuilder sb = new StringBuilder();
        try {
            List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
            for (int i = 0; i < packageInfos.size(); i++) {
                PackageInfo packageInfo = packageInfos.get(i);
                //过滤掉系统app
                if ((ApplicationInfo.FLAG_SYSTEM & packageInfo.applicationInfo.flags) <= 0) {
                    if (i == packageInfos.size()) {
                        sb.append(packageInfo.applicationInfo.loadLabel(packageManager));
                    } else {
                        sb.append(packageInfo.applicationInfo.loadLabel(packageManager) + "`");
                    }
                }
            }
        } catch (Exception e) {
        }
        return sb.toString();
    }

    /**
     * 书籍封面页字数显示
     */
    public static String getWordNums(long num) {
        if (num == 0) {
            return "暂无";
        } else if (num < 10000) {
            return num + "字";
        } else {
            return num / 10000 + "." + (num - (num / 10000) * 10000) / 1000 + "万字";
        }

    }

    /**
     * 书籍封面页在读人数显示
     */
    public static String getReadNums(long num) {
        if (num == 0) {
            return "";
        } else if (num < 10000) {
            return num + "人在读";
        } else if (num < 100000000) {
            return num / 10000 + "." + (num - (num / 10000) * 10000) / 1000 + "万人在读";
        } else {
            return "9999+万人在读";
        }

    }

    /**
     * 多少人气显示
     */
    public static String getCommonReadNums(long num) {
        if (num == 0) {
            return "";
        } else if (num < 10000) {
            return num + "人气";
        } else if (num < 100000000) {
            return num / 10000 + "." + (num - (num / 10000) * 10000) / 1000 + "万人气";
        } else {
            return "9999+万人气";
        }

    }

    public static String getProcessName(Context context) {
        try {
            List<ActivityManager.RunningAppProcessInfo> runningApps =
                    ((ActivityManager) context.getSystemService(
                            Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
            if (runningApps == null) {
                return null;
            }
            for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
                if (proInfo.pid == android.os.Process.myPid() && proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isMainProcess(Context context) {
        return getPackageName().equals(getProcessName(context));
    }

    public static String colorHoHex(int color) {
        String red = Integer.toHexString((color & 0xff0000) >> 16);
        String green = Integer.toHexString((color & 0x00ff00) >> 8);
        String blue = Integer.toHexString((color & 0x0000ff));
        return "#" + red + green + blue;
    }


    // 是否含有中文
    public static boolean isContainChinese(String str) {

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    public static boolean checkPermission(Context context) {
        boolean flag = false;
        if (context != null) {
            try {
                PackageManager pm = context.getPackageManager();
                flag = (PackageManager.PERMISSION_GRANTED ==
                        pm.checkPermission("android.permission.READ_PHONE_STATE",
                                "com.intelligent.reader"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public static void fixInputMethodManagerLeak(Context destContext) {
        if (destContext == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) destContext.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        String[] arr = new String[]{"mCurRootView", "mServedView", "mNextServedView"};
        Field f = null;
        Object obj_get = null;
        for (int i = 0; i < arr.length; i++) {
            String param = arr[i];
            try {
                f = imm.getClass().getDeclaredField(param);
                if (f.isAccessible() == false) {
                    f.setAccessible(true);
                } // author: sodino mail:sodino@qq.com
                obj_get = f.get(imm);
                if (obj_get != null && obj_get instanceof View) {
                    View v_get = (View) obj_get;
                    if (v_get.getContext()
                            == destContext) { // 被InputMethodManager持有引用的context是想要目标销毁的
                        f.set(imm, null); // 置空，破坏掉path to gc节点
                    } else {
                        break;
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * 关闭辅助功能，针对4.2.1和4.2.2 崩溃问题  百度移动统计平台上的bug  webview
     *
     * https://blog.csdn.net/qq_22393017/article/details/72782801
     *
     * java.lang.NullPointerException
     * at android.webkit.AccessibilityInjector$TextToSpeechWrapper$1.onInit(AccessibilityInjector
     * .java:753)
     * ... ...
     * at android.webkit.CallbackProxy.handleMessage(CallbackProxy.java:321)
     */
    public static void disableAccessibility(Context context) {
        if (Build.VERSION.SDK_INT == 17/*4.2 (Build.VERSION_CODES.JELLY_BEAN_MR1)*/) {
            if (context != null) {
                try {
                    AccessibilityManager am = (AccessibilityManager) context.getSystemService(
                            Context.ACCESSIBILITY_SERVICE);
                    if (!am.isEnabled()) {
                        //Not need to disable accessibility
                        return;
                    }

                    Method setState = am.getClass().getDeclaredMethod("setState", int.class);
                    setState.setAccessible(true);
                    setState.invoke(am,
                            0);/**{@link AccessibilityManager#STATE_FLAG_ACCESSIBILITY_ENABLED}*/
                } catch (Exception ignored) {

                } catch (Error ignored) {

                }
            }
        }
    }

    /****************

     * 发起添加群流程。群号：正清瑞德(857212322) 的 key 为： 7AVm43OHr7XNKeNSN9bkUW0cnyWpeq5F

     * 调用 joinQQGroup(7AVm43OHr7XNKeNSN9bkUW0cnyWpeq5F) 即可发起手Q客户端申请加群 正清瑞德(857212322)

     * @param key 由官网生成的key

     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败

     ******************/

    public static boolean joinQQGroup(Activity activity,String key) {

        Intent intent = new Intent();
        intent.setData(Uri.parse(
                "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq"
                        + ".com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D"
                        + key));

            // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面 //intent.addFlags(Intent
            // .FLAG_ACTIVITY_NEW_TASK)
        try {
            activity.startActivity(intent);
            return true;

        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;

        }

    }
}
