package net.lzbook.kit.utils;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReplaceConstants;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.TELEPHONY_SERVICE;

public class AppUtils {
    public static final int LOG_TYPE_BAIDUPUSH = 0;
    public static final int LOG_TYPE_ESCARD_PAY = LOG_TYPE_BAIDUPUSH + 1;
    public static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat min_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat log_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
    private static Context mContext;
    private static PackageInfo packageInfo;
    private static ApplicationInfo appInfo;

    public static void setContext(Context context) {
        mContext = context;
    }

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
     * 获取手机号码
     */
    public static String getPhoneNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        String _PhoneNumbber = tm.getLine1Number();
        if (_PhoneNumbber != null && _PhoneNumbber.length() > 0) {
            _PhoneNumbber = _PhoneNumbber.replace("#", "");
            _PhoneNumbber = _PhoneNumbber.replace("*", "");
        }
        return _PhoneNumbber;
    }

    public static String getProvidersName(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        String providersName = "";
        String subscriberId = telephonyManager.getSubscriberId();

        if (subscriberId != null) {
            if (subscriberId.startsWith("46000") || subscriberId.startsWith("46002") || subscriberId.startsWith("46007")) {
                providersName = "中国移动";
            } else if (subscriberId.startsWith("46001")) {
                providersName = "中国联通";
            } else if (subscriberId.startsWith("46003")) {
                providersName = "中国电信";
            }
        }
        return providersName;
    }

    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    /**
     * 获取版本号
     * <p/>
     * context
     */
    public static int getVersionCode() {
        int versionCode = -1;
        try {
            versionCode = getPackageInfo().versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * getVersionName
     * 取得当前VersionName
     * context
     * String 返回类型
     */
    public static String getVersionName() {

        String versionName = "";
        try {
            versionName = getPackageInfo().versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static PackageInfo getPackageInfo()
            throws NameNotFoundException {
        if (packageInfo == null) {
            synchronized (sLock) {
                if (packageInfo == null) {
                    PackageManager packageManager = BaseBookApplication.getGlobalContext().getPackageManager();
                    packageInfo = packageManager.getPackageInfo(
                            BaseBookApplication.getGlobalContext().getPackageName(), 0);
                }
            }
        }

        return packageInfo;
    }

    /**
     * 获取包名
     */
    public static String getPackageName() {
        String packageName = "";
        try {
            packageName = getPackageInfo().packageName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageName;
    }

    // 获取渠道号
    public static String getChannelId() {
        return getAppMetadata("BaiduMobAd_CHANNEL");
    }

    // 获取application节点下某个meta-data节点的值
    private static String getAppMetadata(String metaKey) {
        if (mContext != null && !TextUtils.isEmpty(metaKey)) {
            String packageName = mContext.getPackageName();
            if (packageName != null) {

                try {
                    if (appInfo == null) {
                        appInfo = mContext.getPackageManager().getApplicationInfo(packageName,
                                PackageManager.GET_META_DATA);

                    }
                    if (appInfo != null && appInfo.metaData != null)
                        return appInfo.metaData.getString(metaKey);
                } catch (NameNotFoundException e) {
                    // 忽略该异常
                }

                return "";
            }
        }

        return "";
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
        TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        String IMEI = TelephonyMgr.getDeviceId();

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

    /**
     * 获取屏幕分辨率
     */
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

    /**
     * 获取网络状态
     */
    public static String getNetState(Context context) {
        //结果返回值
        String netType = "无网络";
        //获取手机所有连接管理对象
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //4G
            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
                    && !telephonyManager.isNetworkRoaming()) {
                netType = "4G";
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0 && !telephonyManager.isNetworkRoaming()) {
                netType = "3G";
                //2G 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS || nSubType == TelephonyManager.NETWORK_TYPE_EDGE || nSubType == TelephonyManager.NETWORK_TYPE_CDMA && !telephonyManager.isNetworkRoaming()) {
                netType = "2G";
            } else {
                netType = "2G";
            }
        }
        return netType;
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


}
