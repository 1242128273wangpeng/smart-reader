package net.lzbook.kit.appender_loghub;

import net.lzbook.kit.appender_loghub.appender.AndroidLogClient;
import net.lzbook.kit.appender_loghub.common.PLItemKey;
import net.lzbook.kit.appender_loghub.util.FormatUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.user.UserManager;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017\8\14 0014.
 */

public class StartLogClickUtil {

    //页面编码
    public static final String SYSTEM_PAGE = "SYSTEM";//APP通用
    public static final String MAIN_PAGE = "MAIN";//主页
    public static final String SEARCH_PAGE = "SEARCH";//搜索页
    public static final String SEARCHRESULT_PAGE = "SEARCHRESULT";//搜索结果页
    public static final String SHELF_PAGE = "SHELF";//书架页
    public static final String SHELFEDIT_PAGE = "SHELFEDIT";//书架编辑页
    public static final String CHCHEEDIT_PAGE = "CHCHEEDIT";//缓存编辑页
    public static final String BOOOKDETAIL_PAGE = "BOOOKDETAIL";//书籍详情页
    public static final String PEASONAL_PAGE = "PEASONAL";//个人中心页
    public static final String MORESET_PAGE = "MORESET";//更多设置
    public static final String READPAGE_PAGE = "READPAGE";//阅读页
    public static final String READPAGESET_PAGE = "READPAGESET";//阅读页设置
    public static final String READPAGEMORE_PAGE = "READPAGEMORE";//阅读页更多

    //APP通用
    public static final String APPINIT = "APPINIT";//客户端启动
    public static final String HOME = "HOME";//切换至后台
    public static final String ACTIVATE = "ACTIVATE";//切换至前台
    public static final String BACK = "BACK";//返回
    public static final String SCREENSCROLL = "SCREENSCROLL";//屏幕滑动
    public static final String CASHERESULT = "CASHERESULT";//缓存结果

    //主页
    public static final String BOOKSHELF = "BOOKSHELF";
    public static final String RECOMMEND = "RECOMMEND";
    public static final String TOP = "TOP";
    public static final String CLASS = "CLASS";
    public static final String PERSONAL = "PERSONAL";
    public static final String SEARCH = "SEARCH";


    //书架页
    public static final String MORE = "MORE";//点击书架上方更多
    public static final String CACHEMANAGE = "CACHEMANAGE";//点击书架上方更多内缓存管理
    public static final String CACHEEDIT = "CACHEEDIT";//点击缓存管理内缓存编辑(UI优化免费全本小说书城没有此选项)
    public static final String BOOKSORT = "BOOKSORT";//点击书架上方更多内书籍排序
    public static final String BOOKCLICK = "BOOKCLICK";//书籍点击
    public static final String TOBOOKCITY = "TOBOOKCITY";//空白页点击跳转书城
    public static final String LONGTIMEBOOKSHELFEDIT = "LONGTIMEBOOKSHELFEDIT";//长按编辑书架

    //缓存管理页
    public static final String SELECTALL = "SELECTALL";//全选

    //搜索页
    public static final String BAR = "BAR";//点击搜索框
    public static final String BARCLEAR = "BARCLEAR";//搜索词清空
    public static final String TOPIC = "TOPIC";//大家都在搜-点击搜索热词
    public static final String TOPICCHANGE = "TOPICCHANGE";//大家都在搜-换一换
    public static final String HISTORY = "HISTORY";//搜索历史-点击某一条搜索历史
    public static final String HISTORYCLEAR = "HISTORYCLEAR";//搜索历史-历史记录清空
    public static final String TIPLISTCLICK = "TIPLISTCLICK";//自动补全结果点击
    public static final String SEARCHBUTTON = "SEARCHBUTTON";//自动补全点击“搜索”按钮
    public static final String SHELFADD = "SHELFADD";//点击加入书架
    public static final String CLEAR = "CLEAR";//点击清空，重回搜索页

    //书籍详情页
    public static final String SOURCECHANGE = "SOURCECHANGE";//点击切源弹出
    public static final String LATESTCHAPTER = "LATESTCHAPTER";//点击最新章节（目录）
    public static final String CATALOG = "CATALOG";//点击查看目录
    public static final String CASHEALL = "CASHEALL";//点击全本缓存
    public static final String SHELFEDIT = "SHELFEDIT";//点击加入书架
    public static final String TRANSCODEREAD = "TRANSCODEREAD";//点击转码阅读


    //更多设置
    public static final String PUSHSET = "PUSHSET";//	消息推送开启与关闭
    public static final String PUSHAUDIO = "PUSHAUDIO";//推送声音

    //个人中心
    public static final String LOGIN = "LOGIN";//	点击登录
    public static final String NIGHTMODE = "NIGHTMODE";//点击夜间模式
    public static final String PERSON_HISTORY = "HISTORY";//	点击浏览足迹
    public static final String HISTORYLOGIN = "HISTORYLOGIN";//点击浏览足迹内的登录
    public static final String MORESET = "MORESET";//点击更多设置
    public static final String HELP = "HELP";//点击帮助与反馈
    public static final String COMMENT = "COMMENT";//点击去评分
    public static final String VERSION = "VERSION";//点击当前版本
    public static final String VERSIONUPDATE = "VERSIONUPDATE";//点击版本更新
    public static final String CACHECLEAR = "CACHECLEAR";//点击清除缓存
    public static final String PROCTCOL = "PROCTCOL";//点击使用协议
    public static final String LOGOUT = "LOGOUT";//点击退出登录


    //阅读页更多
    public static final String READ_SOURCECHANGE = "SOURCECHANGE";//换源
    public static final String READ_SOURCECHANGECONFIRM = "SOURCECHANGECONFIRM";//确认换源
    public static final String BOOKMARKEDIT = "BOOKMARKEDIT";//添加书签
    public static final String BOOKDETAIL = "BOOKDETAIL";//书籍详情


    private static List<ServerLog> linkList = new LinkedList<ServerLog>();

    //上传普通的点击事件
    public static void upLoadEventLog(Context context, String pageCode, String identify) {
        if (!Constants.dy_ad_new_statistics_switch) {
            return;
        }
        final ServerLog log = new ServerLog(PLItemKey.ZN_APP_EVENT);

        log.PutContent("project", PLItemKey.ZN_APP_EVENT.getProject());
        log.PutContent("logstore", PLItemKey.ZN_APP_EVENT.getLogstore());
        log.PutContent("code", identify);//点击事件唯一标识
        log.PutContent("page_code", pageCode);

        if (UserManager.INSTANCE.isUserLogin()) {
            log.PutContent("uid", UserManager.INSTANCE.getMUserInfo().getUid());//用户中心唯一标识
        } else {
            log.PutContent("uid", "");
        }

        log.PutContent("os", "android");//手机操作系统
        log.PutContent("log_time", System.currentTimeMillis() + "");//日志产生时间（毫秒数）
        log.PutContent("network", AppUtils.getNetState(context));//网络状况
        log.PutContent("longitude", Constants.longitude + "");//经度
        log.PutContent("latitude", Constants.latitude + "");//纬度
        log.PutContent("city_info", Constants.adCityInfo);//城市
        log.PutContent("location_detail", Constants.adLocationDetail);//具体位置信息

        //事件对应的额外的参数部分
//        Map<String, String> data = new HashMap<>();
//        data.put("start_time", System.currentTimeMillis()+"");
//        data.put("end_time", System.currentTimeMillis()+1000+"");
//        log.PutContent("data", FormatUtil.forMatMap(data));
        new Thread() {
            @Override
            public void run() {
                AppLog.e("log", log.GetContent().toString());
                AndroidLogClient.putLog(log);
            }
        }.start();


    }

    //上传普通的点击事件,带事件参数
    public static void upLoadEventLog(Context context, String pageCode, String identify, Map<String, String> extraParam) {
        if (!Constants.dy_ad_new_statistics_switch) {
            return;
        }
        final ServerLog log = new ServerLog(PLItemKey.ZN_APP_EVENT);

        log.PutContent("project", PLItemKey.ZN_APP_EVENT.getProject());
        log.PutContent("logstore", PLItemKey.ZN_APP_EVENT.getLogstore());
        log.PutContent("code", identify);//点击事件唯一标识
        log.PutContent("page_code", pageCode);

        if (UserManager.INSTANCE.isUserLogin()) {
            log.PutContent("uid", UserManager.INSTANCE.getMUserInfo().getUid());//用户中心唯一标识
        } else {
            log.PutContent("uid", "");
        }

        log.PutContent("os", "android");//手机操作系统
        log.PutContent("log_time", System.currentTimeMillis() + "");//日志产生时间（毫秒数）
        log.PutContent("network", AppUtils.getNetState(context));//网络状况
        log.PutContent("longitude", Constants.longitude + "");//经度
        log.PutContent("latitude", Constants.latitude + "");//纬度
        log.PutContent("city_info", Constants.adCityInfo);//城市
        log.PutContent("location_detail", Constants.adLocationDetail);//具体位置信息

        //事件对应的额外的参数部分

        if (extraParam != null) {
            log.PutContent("data", FormatUtil.forMatMap(extraParam));
        }


        new Thread() {
            @Override
            public void run() {
                AppLog.e("log", log.GetContent().toString());
                AndroidLogClient.putLog(log);
            }
        }.start();


    }

    //上传用户App列表
    public static void upLoadApps(Context context, String applist) {
        if (!Constants.dy_ad_new_statistics_switch) {
            return;
        }
        final ServerLog log = new ServerLog(PLItemKey.ZN_APP_APPSTORE);
        if (UserManager.INSTANCE.isUserLogin()) {
            log.PutContent("uid", UserManager.INSTANCE.getMUserInfo().getUid());//用户中心唯一标识
        } else {
            log.PutContent("uid", "");
        }
        log.PutContent("apps", applist);
        log.PutContent("time", System.currentTimeMillis() + "");

        new Thread() {
            @Override
            public void run() {
                AppLog.e("log", log.GetContent().toString());
                AndroidLogClient.putLog(log);
            }
        }.start();


    }


    //上传用户阅读内容(传参按此格式顺序)
    public static void upLoadReadContent(String... params) {
        if (!Constants.dy_ad_new_statistics_switch || !Constants.dy_readPage_statistics_switch) {
            return;
        }
        ServerLog log = new ServerLog(PLItemKey.ZN_APP_READ_CONTENT);
        if (UserManager.INSTANCE.isUserLogin()) {
            log.PutContent("uid", UserManager.INSTANCE.getMUserInfo().getUid());//用户中心唯一标识
        } else {
            log.PutContent("uid", "");
        }
        if (params != null) {
            log.PutContent("book_id", params[0]);//书籍唯一字符串
            log.PutContent("chapter_id", params[1]);//阅读章节唯一字符串
            log.PutContent("source_ids", params[2]);//使用书籍源，中间有切换源则多个源使用分隔符"`"进行连接，尽量准确获取（不丢数据）
            log.PutContent("page_num", params[3]);//当前阅读章节被切分的总页数
            log.PutContent("pager", params[4]);//章节页数索引，即当前为第几页
            log.PutContent("page_size", params[5]);//当前页尺寸，可以是byte或总字数（包括所有字符，需要知道当前页内容）
            log.PutContent("from", params[6]);//当前页面来源，所有可能来源的映射唯一字符串。书籍封面/书架/上一页翻页等等（不包括退出App后在进入来源）
            log.PutContent("begin_time", params[7]);//进入当前页时间戳（秒数）
            log.PutContent("end_time", params[8]);//退出当前页时间戳（秒数）（不包括用户退出App在进来，即该时间表示为用户主动翻页和主动退出阅读）
            log.PutContent("read_time", params[9]);//总阅读时长秒数（考虑中间退出App的时长不应该包括进来，即排除打电话等时间）
            log.PutContent("has_exit", params[10]);//是否有阅读中间退出行为
            log.PutContent("channel_code", params[11]);//书籍来源1为青果，2为智能
            log.PutContent("lon", Constants.longitude + "");//经度
            log.PutContent("lat", Constants.latitude + "");//纬度

        }
        linkList.add(log);
        AppLog.e("log", log.GetContent().toString());
        if (linkList != null && linkList.size() > 10) {
            new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < linkList.size(); i++) {
                        AndroidLogClient.putLog(linkList.get(i));
                    }
                    linkList.clear();
                }
            }.start();

        }

    }


}
