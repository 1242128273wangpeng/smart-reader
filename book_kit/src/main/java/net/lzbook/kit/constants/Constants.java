package net.lzbook.kit.constants;

import android.content.Context;
import android.os.Environment;

import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.NetWorkUtils;

import org.apache.http.protocol.HTTP;

import java.io.File;

public class Constants {

    /**
     * 阅读页转码声明（RouterUtil）
     */
    @Deprecated
    public static final String FROM_READING_PAGE = "from_reading_page";
    /**
     * 登录页服务条款（RouterUtil）
     */
    @Deprecated
    public static final String SERVICE_POLICY = "service_policy";
    /**
     * 登录页隐秘条款（RouterUtil）
     */
    @Deprecated
    public static final String PRIVACY_POLICY = "privacy_policy";
    /**
     * 仅在使用协议页面进入可以打开调试模式（RouterUtil）
     */
    @Deprecated
    public static final String FROM_DISCLAIMER_PAGE = "from_disclaimer_page";


    /**
     * SharedPreferences的key值
     */
    @Deprecated
    public static String SHAREDPREFERENCES_KEY =
            "onlineconfig_agent_online_setting_" + AppUtils.getPackageName();

    /**
     * HOST分类
     */
    @Deprecated
    public static String NOVEL_HOST = "novel_host";//智能API接口
    @Deprecated
    public static String WEBVIEW_HOST = "httpsWebView_host";// WebView
    @Deprecated
    public static String UNION_HOST = "union_host";//微服务API接口
    @Deprecated
    public static String CONTENT_HOST = "content_host";//微服务内容接口

    /**
     * 禁用动态参数前保留一份动态HOST
     */
    @Deprecated
    public static String NOVEL_PRE_HOST = "novel_pre_host";//
    @Deprecated
    public static String WEBVIEW_PRE_HOST = "httpsWebView_pre_host";//
    @Deprecated
    public static String UNION_PRE_HOST = "union_pre_host";//
    @Deprecated
    public static String CONTENT_PRE_HOST = "content_pre_host";//

    /**
     * SharePre字段名
     */
    @Deprecated
    public static String START_PARAMS = "start_params";// 启动动态参数,默认是开启
    @Deprecated
    public static String PRE_SHOW_AD = "pre_show_ad";// 提前展示广告
    @Deprecated
    public static String RESET_BOOK_SHELF = "reset_book_shelf";//重置书架
    @Deprecated
    public static String UPDATE_CHAPTER = "update_chapter";// 更新章节
    @Deprecated
    public static String HOST_LIST = "host_list";// host列表
    @Deprecated
    public static String SHOW_TOAST_LOG = "show_toast_log";//打点Toast显示，方便h5查看打点

    /**
     * Intent传参
     */
    @Deprecated
    public static String BOOK_ID = "book_id";
    @Deprecated
    public static String BOOK_SOURCE_ID = "book_source_id";
    @Deprecated
    public static String BOOK_CHAPTER_ID = "book_chapter_id";

    // 反射resourceType
    public static final int DRAWABLE = 1;
    public static final int COLOR = 2;
    public static final int STYLE = 3;

    public static final String BAIDU_STAT_ID = "baidu_stat_id";
    //章节末广告开关
    public static final String DY_PAGE_MIDDLE_AD_SWITCH = "DY_page_middle_ad_switch";
    //章节末广告间隔
    public static final String NATIVE_AD_PAGE_INTERSTITIAL_COUNT = "DY_mid_page_frequence";
    //章节内广告间隔（章数）
    public static final String NATIVE_AD_PAGE_GAP_IN_CHAPTER = "DY_in_chapter_frequence";
    //章节内要展现广告限制的最小页数
    public static final String NATIVE_AD_PAGE_IN_CHAPTER_LIMIT = "DY_page_in_chapter_limit";
    ////新的用户广告请求开关
    public static final String DY_AD_NEW_REQUEST_SWITCH = "DY_ad_new_request_switch";
    // 新的统计开关
    public static final String DY_AD_NEW_STATISTICS_SWITCH = "Dy_ad_new_statistics_switch";
    // 阅读页翻页统计开关
    public static final String DY_READPAGE_STATISTICS_SWITCH = "Dy_readPage_statistics_switch";
    // 阅读页上下翻页展示广告开关
    public static final String DY_AD_READPAGE_SLIDE_SWITCH_NEW = "Dy_ad_readPage_slide_switch_new";
    //老的广告统计开关key
    public static final String DY_AD_OLD_REQUEST_SWITCH = "DY_ad_old_request_switch";
    //广告总开关
    public static final String DY_AD_SWITCH = "DY_ad_switch";
    //新用户不显示广告的时间
    public static final String DY_ADFREE_NEW_USER = "DY_adfree_new_user";
    //开屏广告开关
    public static final String DY_SPLASH_AD_SWITCH = "DY_splash_ad_switch";
    //书架广告开关
    public static final String DY_SHELF_AD_SWITCH = "DY_shelf_ad_switch";
    //九宫格书架页广告显示类型切换开关 1表示横向header, 2 表示九宫格列表形式
    public static final String BOOK_SHELF_STATE = "book_shelf_state";
    //书架1-2广告开关
    public static final String DY_SHELF_BOUNDARY_SWITCH = "DY_shelf_boundary_switch";
    //书架广告频率
    public static final String DY_SHELF_AD_FREQ = "DY_shelf_ad_freq";
    //章节末广告开关
    public static final String DY_PAGE_END_AD_SWITCH = "DY_page_end_ad_switch";
    //章节末广告频率
    public static final String DY_PAGE_END_AD_FREQ = "DY_page_end_ad_freq";
    //书末广告开关
    public static final String DY_BOOK_END_AD_SWITCH = "DY_book_end_ad_switch";
    //休息页广告开关
    public static final String DY_REST_AD_SWITCH = "DY_rest_ad_switch";
    //休息页广告休息时间
    public static final String DY_REST_AD_SEC = "DY_rest_ad_sec";
    //是否启用新版章节末UI
    public static final String DY_IS_NEW_READING_END = "DY_is_new_reading_end";
    //新壳广告开关
    public static final String NEW_APP_AD_SWITCH = "new_app_ad_switch";
    //书籍封面页推荐位智能，青果书籍配比
    public static final String RECOMMEND_BOOKCOVER = "recommend_bookcover";

    public static final String DY_ACTIVITED_SWITCH_AD = "DY_activited_switch_ad";
    public static final String DY_SWITCH_AD_SEC = "DY_switch_ad_sec";
    public static final String DY_SWITCH_AD_CLOSE_SEC = "DY_switch_ad_close_sec";
    public static final String AD_LIMIT_TIME_DAY = "ad_limit_time_day";
    public static final String PUSH_KEY = "push_key";
    public static final String BAIDU_EXAMINE = "baidu_examine";
    public static final String USER_TRANSFER_FIRST = "user_transfer_first";
    public static final String USER_TRANSFER_SECOND = "user_transfer_second";

    public static final String CHANNEL_LIMIT = "channel_limit";

    public static final String DAY_LIMIT = "day_limit";

    public static final String BAN_GIDS = "ban_gids";
    public static final String ADD_DEFAULT_BOOKS = "add_default_books";
    public static final String NETWORK_LIMIT = "network_limit";

    //新的用户广告数据搜集接口域名获取key
    public static String DY_AD_NEW_REQUEST_DOMAIN_NAME = "DY_ad_new_request_domain_name";

    public static final String HOME_GUIDE_TAG = "home_guide_tag";
    public static final String BOOKSHELF_GUIDE_TAG = "bookshelf_guide_tag";
    public static final String READING_GUIDE_TAG = "reading_guide_tag";
    public static final String READING_SETING_GUIDE_TAG = "reading_setting_guide_tag";
    public static final String COVER_PAGE_GUIDE_TAG = "cover_page_guide_tag";

    public static final String LAST_READ = "LAST_READ";
    public static final String NONET_READ = "NONET_READ";
    //    public static final String NONET_READTIME = "NONET_READHOUR";
    public static final int NOPACKAGE = 100;
    //根据包名、版本号、渠道号对广告进行限制
    public static final String LIMIT_VERSIONCODE = "limit_versioncode";
    public static final String LIMIT_CHANNELID = "limit_channelid";
    public static final String AD_HUAJIAO_SWITCH = "ad_huajiao_switch";
    public static final String AD_HUAJIAO_ACCESS_ADDRESS = "ad_huajiao_access_address";

    public static final String SHOW_AD_VERSION = "show_ad_version";

    public static final String REQUEST_ITEM = "request_item";
    public static final String NOTIFY_ID = "notify_id";
    public static final String user_new_index = "user_new_index";
    public static final String user_new_ad_limit_day = "user_new_ad_limit_day";
    //用户当日首次打开app的时间
    public static final String TODAY_FIRST_OPEN_APP = "today_first_open_app";
    //用户当天是否上传了用户信息
    public static final String IS_UPLOAD = "IS_UPLOAD";
    //用户平台系统
    public static final String APP_SYSTEM_PLATFORM = "android";
    public static final String UPDATE_CHAPTER_SOURCE_ID = "update_chapter_source_id";
    public static final int CONTENT_ERROR_COUNT = 50;
    //夜间模式蒙板透明度
    public static final float NIGHT_SHADOW_ALPHA = 0.55f;
    public static final String SERARCH_HOT_WORD = "search_hot_word";//搜索热词
    //打点书架页每日首次上传书ID
    public static final String TODAY_FIRST_POST_BOOKIDS = "today_first_post_bookids";
    //打点FindBookDetail的搜索按钮
    public static final String FINDBOOK_SEARCH = "findbook_search";
    //书架页推荐书籍黑名单
    public static final String DISLIKED_BOOK_ID = "disliked_bookId";
    public static final String RECOMMEND_UPDATE_TIME = "recommend_update_time";
    public static int NONET_READHOUR = 24;
    public static int DOWNLOAD = 400;
    // FIXME 上线要改成false
    public static boolean SHOW_LOG = true;
    public static boolean DEVELOPER_MODE = false;
    public static boolean isSdCard = false;
    public static boolean isHideAD = false;//全局广告开关
    public static int limit_versionCode = 0;
    public static String limit_channelIds = "";
    public static boolean ad_huajiao_switch = false;
    public static String ad_huajiao_access_address =
            "http://h.open.huajiao.com?channelid=quanbenzhuishu";
    public static String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    //老版青果缓存路径
    public static String QG_CACHE_PATH = SDCARD_PATH + "/quanben/book/";

    public static String CHARSET = HTTP.UTF_8;
    public static boolean isNetWorkError = false;
    public static int manualReadedCount = 0;
    public static int manualTip = 50;
    // 阅读模式
    public static int MODE = 51;//day
    //翻页模式
    public static final int PAGE_MODE_DELAULT = 0;
    //翻页模式
    public static int PAGE_MODE = 0;
    //全屏阅读
    public static boolean FULL_SCREEN_READ = false;
    // 阅读页默认字体大小
    public static int FONT_SIZE = 18;
    // 阅读页章节首页提示 默认字体大小
    public static int FONT_CHAPTER_DEFAULT = 18;
    //阅读页面章节首页字体
    public static int FONT_CHAPTER_SIZE = 30;
    //阅读页面章节首页首字字体
    public static int FONT_FIRST_CHAPTER_SIZE = 30;
    //landscape 横屏模式
    public static boolean IS_LANDSCAPE = false;
    public static boolean is_wifi_auto_download = false;//默认false


    public static int refreshTime = 3 * 60 * 1000;
    //阅读页行间距
    public static float READ_INTERLINEAR_SPACE = 0.3f;
    //阅读页段间距
    public static float READ_PARAGRAPH_SPACE = 1.0f;
    //阅读页内容页左右边距
    public static int READ_CONTENT_PAGE_LEFT_SPACE = 20;//4-20
    //阅读页内容上下页边距
    public static int READ_CONTENT_PAGE_TOP_SPACE = 45;//20-40
    public static boolean isFullWindowRead = true;
    public static boolean isVolumeTurnover = true;
    public static int screenOffTimeout = 5 * 60 * 1000;// TODO屏幕休眠时间调整为5分钟
    public static boolean isSlideUp = false;
    public static int read_rest_time =
            Constants.DEVELOPER_MODE ? (2 * 60 * 1000) : (30 * 60 * 1000);    //护眼提醒时间
    // (测试用时间2min对应上线时间30min)
    public static int one_day_time = 24 * 60 * 60 * 1000;
    public static int readedCount = 0;
    // 动态参数
    public static int native_ad_page_interstitial_count = 1;
    public static int native_ad_page_gap_in_chapter = 1;
    public static int native_ad_page_in_chapter_limit = 8;
    public static int land_scroll_chapter_frequence = 5;
    public static int switchSplash_ad_sec = 60;
    public static boolean isShowSwitchSplashAd = false;
    public static int show_switchSplash_ad_close = 0;
    public static int dy_adfree_new_user = 48;
    public static boolean dy_splash_ad_switch = true;
    public static boolean dy_shelf_ad_switch = true;


    public static int dy_shelf_ad_freq = 10;
    public static boolean dy_page_end_ad_switch = true;
    public static int dy_page_end_ad_freq = 1;
    public static boolean dy_book_end_ad_switch = true;
    public static boolean dy_rest_ad_switch = true;
    public static boolean dy_page_middle_ad_switch = true;
    public static boolean dy_page_in_chapter_ad_switch = true;
    public static boolean dy_ad_switch = true;
    //新的用户广告请求开关
    public static boolean dy_ad_new_request_switch = true;
    //统计开关
    public static boolean dy_ad_new_statistics_switch = true;
    //阅读页翻页统计开关
    public static boolean dy_readPage_statistics_switch = false;
    //老的广告统计开关
    public static boolean dy_ad_old_request_switch = true;
    //是否启用新版章节末UI
    public static boolean dy_is_new_reading_end = false;
    //新壳广告开关
    public static boolean new_app_ad_switch = false;
    //书架页悬浮广告位
    public static boolean dy_shelf_boundary_switch = true;
    //阅读页上下翻页展示广告开关
    public static boolean dy_ad_readPage_slide_switch_new = true;
    public static int ad_limit_time_day = 2;//新用户前三天不显示ad
    public static boolean isBaiduExamine = false;
    public static boolean isHuaweiExamine = false;
    public static int versionCode = 0;
    public static boolean is_user_transfer_first = true;
    public static boolean is_user_transfer_second = false;
    //是否开启屏蔽
    public static boolean isShielding = false;
    //经度信息
    public static double longitude = 0;
    //纬度信息
    public static double latitude = 0;
    //地区编号
    public static String adCode = "";
    //城市信息
    public static String adCityInfo = "";
    //城市编号
    public static String cityCode = "";
    //详细地址
    public static String adLocationDetail = "";
    public static boolean is_reading_network_limit = false;
    public static boolean USER_TRANSFER_VALUE = false;
    //用户是否是当日首次打开app
    public static boolean is_user_today_first = false;
    public static boolean upload_userinformation = false;
    public static int preVersionCode = 0;
    //用户迁移目标源（域名更替）
    public static String USER_TRANFER_DESTINATION = "www.dushixiaoshuo.cn";
    //新的用户广告数据搜集接口
    public static String AD_DATA_Collect = "http://ad.dingyueads.com:8010/insertData";
    //源相关字段
    public static String QG_SOURCE = "open.qingoo.cn";
    public static String YS_SOURCE = "b.easou.com";
    public static String SG_SOURCE = "k.sogou.com";
    //public static final String CONTENT_ERROR = "文章内容较短,可能非正文,正在抓紧修复中";
    public static String FILTER_WORD = "easou";
    public static String STYLE_CHANGE = "style1";


    public static long startReadTime = 0;
    public static long endReadTime = 0;
    public static String FILE_PATH = "";
    public static boolean is_today_first_read = false;
    public static boolean IS_DOWNLOADING = false;
    public static String DOWNLOAD_LIMIT = "download_limit";
    public static int DOWNLOAD_LIMIT_NUMBER = 20;
    public static String ONE_DOWNLOAD = "oneday_download";
    public static boolean REMOVE_BOOK = false;
    public static String PACKAGE_MD5 = "";
    //1是开启  0是关闭
    public static int isNoNetRead = 0;
    public static String noNetReadNumber = "noNetReadNumber";

    //是否是在下载管理页面
    public static boolean isDownloadManagerActivity = false;
    //是否已经提示过了
    public static boolean hadShownMobilNetworkConfirm = false;

    public static int WIFI_AUTO_CACHE_COUNT = 20;

    public static final String SERARCH_HOT_WORD_YOUHUA = "search_hot_word_youhua";
    // 4个替壳 新版搜索热词,防止升级用户首次没网，从缓存拿数据时报错


    //书架推荐书籍比例 智能：青果
    public static String sRecommendRateForShelf = "1,2";
    //书末推荐书籍比例 智能：青果  智能：青果
    public static String sRecommendRateForBookend = "1,2,0,3";
    public static String UPLOAD_OLDUSER_READ_SETTING = "upload_olduser_read_setting";
    // 老用户每天上传一次阅读页设置


    public static int authAccessRefreshTime = 30 * 60 * 1000;

    public static int checkDynamicTime =  2 * 60 * 1000;

    /**
     * 书架书籍排序
     * 0 阅读时间
     * 1 更新时间
     * 2 添加时间
     */
    public static int book_list_sort_type = 0;


    /**
     * 开屏选男女
     * -1不传sex字段
     * 0全部； 1男； 2女；
     * ParameterConfig类
     */
    //逐步规范全局参数使用，请使用ParameterConfig中的参数
    @Deprecated
    public static int SBOY = 1;
    @Deprecated
    public static int SGIRL = 2;
    @Deprecated
    public static int SDEFAULT = 0;
    @Deprecated
    public static int NONE = -1;
    @Deprecated
    public static int SGENDER = NONE;


    /**
     * 0-关闭书架页广告位；两种形式都不开启
     * 1-开启书架页广告位A样式:顶部横幅书架页广告
     * 2-开启书架页广告位B样式：九宫格原生书架页广告
     * 3-开启书架页广告位两种样式
     * 九宫格书架页广告显示类型切换开关
     */
    public static int book_shelf_state = 1;

    /**
     *广告分渠道，分版本，分广告位控制
     * @param context
     */

    public static String ad_control_status = "0"; //广告分渠道控制  0表示关 1表示开
    public static String ad_control_pkg = ""; //广告分渠道控制   包名
    public static String ad_control_channelId = "";//广告分渠道控制 渠道号
    public static String ad_control_version = "";//广告分渠道控制 app版本号
    public static String ad_control_adTpye = "";//广告分渠道控制  广告位类型  0全部 1 福利中心 2书架页1-1  3书架页1-2  4阅读页广告 5 福利中心和书架页1-2

    public static String ad_control_welfare= "1"; //广告分渠道控制广告位  1福利中心
    public static String ad_control_shelf_normal = "2"; //广告分渠道控制广告位   2书架页1-1
    public static String ad_control_shelf_float = "3";//广告分渠道控制广告位  3书架页1-2
    public static String ad_control_reader = "4";//广告分渠道控制广告位     4阅读页广告
    public static String ad_control_welfare_shelf = "5";//广告分渠道控制广告位     5福利中心和书架页1-2
    public static String ad_control_other = "6";//广告分渠道控制广告位    4其他广告

    public static String BOOK_LOCAL = "local";//本地导入 书籍类型

    public static Boolean SHARE_SWITCH_ENABLE = false; //是否开启分享功能

    public static long INSERT_BOOKSHELF_FULL = -50L;

    /*
     * 初始化
     */
    public static void init(Context context) {
        isSdCard = android.os.Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState());
        if (!isSdCard) {
            SDCARD_PATH = "mnt/sdcard";
        }
        File dir = new File(ReplaceConstants.getReplaceConstants().APP_PATH);
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        dir = new File(ReplaceConstants.getReplaceConstants().APP_PATH_BOOK);
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        dir = new File(ReplaceConstants.getReplaceConstants().APP_PATH_CACHE);
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        dir = new File(ReplaceConstants.getReplaceConstants().APP_PATH_DOWNLOAD);
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        dir = new File(ReplaceConstants.getReplaceConstants().APP_PATH_IMAGE);
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (Constants.DEVELOPER_MODE) {
            dir = new File(ReplaceConstants.getReplaceConstants().APP_PATH_LOG);
            if (!dir.exists()) {
                try {
                    dir.mkdirs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        NetWorkUtils.initNetWorkType(context);
    }
}
