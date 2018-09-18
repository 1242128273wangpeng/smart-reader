package net.lzbook.kit.appender_loghub;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ding.basic.bean.LocalLog;
import com.logcat.sdk.LogEncapManager;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.appender.AndroidLogClient;
import net.lzbook.kit.appender_loghub.appender.AndroidLogStorage;
import net.lzbook.kit.appender_loghub.common.PLItemKey;
import net.lzbook.kit.appender_loghub.util.FormatUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.ChapterErrorBean;
import net.lzbook.kit.user.UserManager;
import net.lzbook.kit.user.UserManagerV4;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.OpenUDID;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017\8\14 0014.
 */

public class StartLogClickUtil {


    /***
     * HomeActivity相关点位
     * **/
    //HomeActivity
    public static final String PAGE_HOME = "MAIN";
    //HomeActivity功能
    public static final String ACTION_HOME_BOOK_LIST = "BOOKLIST";
    public static final String ACTION_HOME_BOOK_SHELF = "BOOKSHELF";
    public static final String ACTION_HOME_RECOMMEND = "RECOMMEND";
    public static final String ACTION_HOME_TOP = "TOP";
    public static final String ACTION_HOME_CLASS = "CLASS";
    public static final String ACTION_HOME_PERSONAL = "PERSONAL";
    public static final String ACTION_HOME_SEARCH = "SEARCH";
    public static final String ACTION_HOME_CACHE_MANAGE = "CACHEMANAGE";
    public static final String PREFERENCE = "PREFERENCE";//开屏选男女
    public static final String NORESULT_PAGE = "NORESULT";//搜索无结果页
    public static final String FEEDBACK_PAGE = "FEEDBACK";//搜索无结果页订阅页面



    /***
     * 书架相关点位
     * **/
    //书架页
    public static final String PAGE_SHELF = "SHELF";
    //书架页功能
    public static final String ACTION_SHELF_MORE = "MORE";
    public static final String ACTION_SHELF_SHARE = "SHARE";
    public static final String ACTION_SHELF_SEARCH = "SEARCH";
    public static final String ACTION_SHELF_PERSONAL = "PERSONAL";
    public static final String ACTION_SHELF_BOOK_SORT = "BOOKSORT";
    public static final String ACTION_SHELF_BOOK_CLICK = "BOOKCLICK";
    public static final String ACTION_SHELF_TO_BOOK_CITY = "TOBOOKCITY";
    public static final String ACTION_SHELF_CACHE_MANAGE = "CACHEMANAGE";
    public static final String ACTION_SHELF_LONG_TIME_BOOK_SHELF_EDIT = "LONGTIMEBOOKSHELFEDIT";
    public static final String POPUPMESSAGE = "POPUPMESSAGE";


    //书架编辑页面
    public static final String PAGE_SHELF_EDIT = "SHELFEDIT";
    //书架编辑页功能
    public static final String ACTION_SHELF_EDIT_BACK = "BACK";
    public static final String ACTION_SHELF_EDIT_CANCEL = "CANCLE";
    public static final String ACTION_SHELF_EDIT_DELETE = "DELETE";
    public static final String ACTION_SHELF_EDIT_SELECT_ALL = "SELECTALL";

    //书架排序弹窗
    public static final String PAGE_SHELF_SORT = "SHELFSORT";
    //书架排序弹窗功能
    public static final String ACTION_SHELF_SORT_CANCEL = "CANCLE";
    public static final String ACTION_SHELF_SORT_BOOK_SORT = "BOOKSORT";
    public static final String USERINFO = "USERINFO";//用户信息同步（书架书籍、书签、阅读历史）

    //福利中心
    public static final String ADPAGE = "ADPAGE";   //屏幕左上方点击返回按钮

    /***
     * 个人中心相关点位
     * **/
    //个人中心页
    public static final String PAGE_PERSONAL = "PERSONAL";
    public static final String LOGINRESULT = "LOGINRESULT";//登录结果
    public static final String PHONELOGIN = "PHONELOGIN";//点击输入手机号
    public static final String OTHERLOGIN = "OTHERLOGIN";//点击第三方登录
    public static final String PIN = "PIN";//点击获取验证码
    public static final String PROFILE = "PROFILE";//登录后点击个人信息
    public static final String SEX = "SEX";//点击性别
    public static final String BINDOTHERLOGIN = "BINDOTHERLOGIN";//点击绑定第三方账户
    public static final String UIDDIFFUSER = "UIDDIFFUSER";//不同用户在同设备登录
    public static final String NICKNAME = "NICKNAME";//点击昵称
    //个人中心页功能
    public static final String ACTION_PERSONAL_BACK = "BACK";//屏幕左上方点击返回按钮
    public static final String ACTION_PERSONAL_HELP = "HELP";//点击帮助与反馈
    public static final String ACTION_PERSONAL_LOGIN = "LOGIN";//点击登录
    public static final String ACTION_PERSONAL_LOGOUT = "LOGOUT";//点击退出登录
    public static final String ACTION_PERSONAL_COMMENT = "COMMENT";//点击去评分
    public static final String ACTION_PERSONAL_VERSION = "VERSION";//点击当前版本
    public static final String ACTION_PERSONAL_HISTORY = "HISTORY";//点击浏览足迹
    public static final String ACTION_PERSONAL_PROCTCOL = "PROCTCOL";//点击使用协议
    public static final String ACTION_PERSONAL_MORE_SET = "MORESET";//点击更多设置
    public static final String ACTION_PERSONAL_NIGHT_MODE = "NIGHTMODE";//点击夜间模式
    public static final String ACTION_PERSONAL_CACHE_CLEAR = "CACHECLEAR";//点击清除缓存
    public static final String ACTION_PERSONAL_VERSION_UPDATE = "VERSIONUPDATE";//点击版本更新
    public static final String ACTION_PERSONAL_WIFI_AUTO_CACHE = "WIFI_AUTOCACHE";//点击WIFI自动缓存


    /***
     * 下载管理页相关点位
     * **/
    //下载管理页
    public static final String PAGE_CACHE_MANAGER = "CACHEMANAGE";
    //下载管理页功能
    public static final String ACTION_CACHE_MANAGER_BACK = "BACK";//返回
    public static final String ACTION_CACHE_MANAGER_MORE = "MORE";//更多按钮点击
    public static final String ACTION_CACHE_MANAGER_SORT = "SORT";//排序
    public static final String ACTION_CACHE_MANAGER_BOOK_CLICK = "BOOKCLICK";//书籍点击
    public static final String ACTION_CACHE_MANAGER_CACHE_BUTTON = "CACHEBUTTON";//缓存按钮点击
    public static final String ACTION_CACHE_MANAGER_CACHE_EDIT = "CACHEEDIT";//长按编辑列表
    public static final String ACTION_CACHE_MANAGER_TO_BOOK_CITY = "TOBOOKCITY";//跳转到书城

    //下载管理编辑页面
    public static final String PAGE_CACHE_MANAGER_EDIT = "CHCHEEDIT";
    //下载管理编辑页面功能
    public static final String ACTION_CACHE_MANAGER_EDIT_BACK = "BACK";//返回
    public static final String ACTION_CACHE_MANAGER_EDIT_CANCEL = "CANCLE";//取消
    public static final String ACTION_CACHE_MANAGER_EDIT_DELETE = "DELETE";//删除
    public static final String ACTION_CACHE_MANAGER_EDIT_SELECT_ALL = "SELECTALL";//全选


    //页面编码
    public static final String SYSTEM_PAGE = "SYSTEM";//APP通用
    public static final String MAIN_PAGE = "MAIN";//主页
    public static final String SEARCH_PAGE = "SEARCH";//搜索页
    public static final String SEARCHRESULT_PAGE = "SEARCHRESULT";//搜索结果页
    public static final String SHELF_PAGE = "SHELF";//书架页
    public static final String SHELFEDIT_PAGE = "SHELFEDIT";//书架编辑页
    public static final String CHCHEEDIT_PAGE = "CHCHEEDIT";//缓存编辑页
    public static final String CACHEMANAGE_PAGE = "CACHEMANAGE";//缓存管理页
    public static final String PEASONAL_PAGE = "PERSONAL";//个人中心页
    public static final String BOOOKDETAIL_PAGE = "BOOOKDETAIL";//书籍详情页
    public static final String MORESET_PAGE = "MORESET";//更多设置
    public static final String READPAGE_PAGE = "READPAGE";//阅读页
    public static final String READPAGESET_PAGE = "READPAGESET";//阅读页设置
    public static final String READPAGEMORE_PAGE = "READPAGEMORE";//阅读页更多
    public static final String RECOMMEND_PAGE = "RECOMMEND";//青果推荐页
    public static final String TOP_PAGE = "TOP";//榜单页
    public static final String CLASS_PAGE = "CLASS";//分类页
    public static final String FIRSTCLASS_PAGE = "FIRSTCLASS";//分类一级页面的搜索
    public static final String FIRSTTOP_PAGE = "FIRSTTOP";//榜单一级页面的搜索
    public static final String FIRSTRECOMMEND_PAGE = "FIRSTRECOMMEND";//推荐一级页面的搜索
    public static final String BOOKCATALOG = "BOOKCATALOG";//书籍目录页
    public static final String PROCTCOL_PAGE = "PROCTCOL";//使用协议
    public static final String PERHELP_PAGE = "PERHELP";//帮助与反馈
    public static final String PERHISTORY_PAGE = "PERHISTORY";//浏览足迹
    public static final String AUTHORPAGE_PAGE = "AUTHORPAGE";//作者主页
    public static final String BOOKENDPAGE_PAGE = "BOOKENDPAGE";//书籍完结页
    public static final String READFINISH_PAGE = "READFINISH";//完结页（新壳2新添加）

    public static final String PAGE_SHARE = "SHAREPAGE";//分享弹窗
    public static final String ACTION_SHARE = "SHARE";//分享
    public static final String ACTION_CANCEL = "CANCEL";//分享



    //PUSH
    public static final String PUSHRECEIVE = "PUSHRECEIVE";//通知送达
    public static final String PUSHCLICK = "PUSHCLICK";//通知点击

    //APP通用
    public static final String APPINIT = "APPINIT";//客户端启动
    public static final String HOME = "HOME";//切换至后台
    public static final String ACTIVATE = "ACTIVATE";//切换至前台
    public static final String BACK = "BACK";//返回
    public static final String SCREENSCROLL = "SCREENSCROLL";//屏幕滑动
    public static final String CASHERESULT = "CASHERESULT";//缓存结果
    public static final String SYSTEM_SEARCHRESULT = "SEARCHRESULT";//被动搜索结果
    public static final String UPDATE = "UPDATE";//数据库升级


    //主页
    public static final String BOOKSHELF = "BOOKSHELF";
    public static final String RECOMMEND = "RECOMMEND";
    public static final String TOP = "TOP";
    public static final String CLASS = "CLASS";
    public static final String PERSONAL = "PERSONAL";
    public static final String SEARCH = "SEARCH";
    public static final String BOOKLIST = "BOOKLIST";
    public static final String ENTRYPAGE = "ENTRYPAGE";


    //书架页
    public static final String MORE = "MORE";//点击书架上方更多
    public static final String CACHEMANAGE = "CACHEMANAGE";//点击书架上方更多内缓存管理
    public static final String CACHEEDIT = "CACHEEDIT";//点击缓存管理内缓存编辑(UI优化免费全本小说书城没有此选项)
    public static final String BOOKSORT = "BOOKSORT";//点击书架上方更多内书籍排序
    public static final String BOOKCLICK = "BOOKCLICK";//书籍点击
    public static final String TOBOOKCITY = "TOBOOKCITY";//空白页点击跳转书城
    public static final String LONGTIMEBOOKSHELFEDIT = "LONGTIMEBOOKSHELFEDIT";//长按编辑书架
    public static final String VERSIONUPDATE2 = "VERSIONUPDATE";//点击更新

    //主页
    public static final String POPUPEXPOSE = "POPUPEXPOSE";// 通知权限弹窗展现
    public static final String POPUPCLOSE = "POPUPCLOSE";// 通知权限弹窗关闭
    public static final String POPUPNOWOPEN = "POPUPNOWOPEN";// 通知权限弹窗点击现在开启
    public static final String POPUPSET = "POPUPSET";// 通知权限弹窗前往系统设置页

    public static final String BANNER_POPUP_SHOW = "BANNERPOPUPSHOW"; //活动弹窗曝光
    public static final String BANNER_POPUP_CLICK = "BANNERPOPUPCLICK"; //活动弹窗点击
    public static final String BANNER_POPUP_CLOSE = "BANNERPOPUPCLOSE"; //活动弹窗关闭

    //书架编辑页
    public static final String SELECTALL1 = "SELECTALL";//全选
    public static final String DELETE1 = "DELETE";//删除
    public static final String CANCLE1 = "CANCLE";//右上角取消
    public static final String UPDATEDETAIL = "UPDATEDETAIL";//点击详情

    //缓存管理页
    public static final String BOOKCLICK1 = "BOOKCLICK";//书籍点击
    public static final String CACHEBUTTON = "CACHEBUTTON";//缓存按钮点击
    public static final String CACHEEDIT1 = "CACHEEDIT";//右上角编辑按钮点击
    public static final String SORT = "SORT";//右上角取消

    //缓存编辑页
    public static final String SELECTALL = "SELECTALL";//全选
    public static final String DELETE = "DELETE";//删除
    public static final String CANCLE = "CANCLE";//右上角取消

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
    public static final String HOTREADCHANGE = "HOTREADCHANGE";//点击换一换
    public static final String HOTREADCLICK = "HOTREADCLICK";//热门阅读-书籍点击

    public static final String BARLIST = "BARLIST"; //搜索框下拉历史词点击

    //搜索无结果页
    public static final String FEEDBACK = "FEEDBACK"; //点击找书反馈
    public static final String SUBMIT = "SUBMIT"; //搜索无结果页 点击订阅书籍



    //书籍详情页
    public static final String SOURCECHANGE = "SOURCECHANGE";//点击切源弹出
    public static final String LATESTCHAPTER = "LATESTCHAPTER";//点击最新章节（目录）
    public static final String CATALOG = "CATALOG";//点击查看目录
    public static final String CASHEALL = "CASHEALL";//点击全本缓存
    public static final String SHELFEDIT = "SHELFEDIT";//点击加入书架
    public static final String TRANSCODEREAD = "TRANSCODEREAD";//点击转码阅读
    public static final String ENTER = "ENTER";//进入书籍详情页
    public static final String SOURCECHANGEPOPUP = "SOURCECHANGEPOPUP";//换源弹窗
    public static final String INTRODUCTION = "INTRODUCTION";//简介点击展开/收起
    public static final String TRANSCODEPOPUP = "TRANSCODEPOPUP";//点击转码阅读
    public static final String LABLECLICK = "LABLECLICK";//点击标签
    public static final String RECOMMENDEDBOOK = "RECOMMENDEDBOOK";//点击推荐的书籍
    public static final String AUTHORBOOKROCOM = "AUTHORBOOKROCOM";//点击作者其他作品


    //书籍目录页
    public static final String CATALOG_CASHEALL = "CASHEALL";//点击全本缓存
    public static final String CATALOG_CATALOGCHAPTER = "CATALOGCHAPTER";//目录中点击某章节
    public static final String CATALOG_SHELFEDIT = "SHELFEDIT";//点击加入书架
    public static final String CATALOG_TRANSCODEREAD = "TRANSCODEREAD";//点击转码阅读
    public static final String CATALOG_TRANSCODEPOPUP = "TRANSCODEPOPUP";//转码弹窗


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
    public static final String WIFI_AUTOCACHE = "WIFI_AUTOCACHE";//点击退出登录
    public static final String PHOTO = "PHOTO";//点击头像
    public static final String BINDPHONE = "BINDPHONE";//点击绑定手机号

    //阅读页
    public static final String LABELEDIT = "LABELEDIT";//添加书签
    public static final String ORIGINALLINK = "ORIGINALLINK";//点击源网页链接
    public static final String CACHE = "CACHE";//点击阅读页内缓存
    public static final String MORE1 = "MORE";//点击阅读页内更多
    public static final String CATALOG1 = "CATALOG";//点击阅读页内目录
    public static final String BOOKMARK = "BOOKMARK";//点击阅读页目录内书签
    public static final String NIGHTMODE1 = "NIGHTMODE";//点击阅读页内日/夜间模式
    public static final String CHAPTERTURN = "CHAPTERTURN";//点击阅读页内上/下章切换
    public static final String REPAIRDEDIALOGUE = "REPAIRDEDIALOGUE";//弹出修复提示弹窗
    public static final String DIRECTORYREPAIR = "DIRECTORYREPAIR";//点击阅读页目录内修复书籍
    public static final String POPUPSHELFADD = "POPUPSHELFADD";//阅读页加入书架弹窗加入
    public static final String POPUPSHELFADDCANCLE = "POPUPSHELFADDCANCLE";//阅读页加入书架弹窗取消
    public static final String SET = "SET";//点击阅读页内设置
    public static final String DEFAULTSETTING = "DEFAULTSETTINGS";//点击阅读页内设置
    public static final String FONTSETTING = "FONTSETTING";//字体设置
    public static final String FONTDOWNLOAD = "FONTDOWNLOAD";//字体下载

    //'阅读页设置
    public static final String LIGHTEDIT = "LIGHTEDIT";//点击亮度调整
    public static final String SYSFOLLOW = "SYSFOLLOW";//点击跟随系统
    public static final String WORDSIZE = "WORDSIZE";//点击字号增/减
    public static final String BACKGROUNDCOLOR = "BACKGROUNDCOLOR";//点击阅读背景色
    public static final String READGAP = "READGAP";//点击阅读间距
    public static final String PAGETURN = "PAGETURN";//点击翻页模式
    public static final String HPMODEL = "HPMODEL";//点击横/竖屏模式
    public static final String AUTOREAD = "AUTOREAD";//点击自动阅读
    public static final String FULLSCREENPAGEREAD = "FULLSCREENPAGEREAD";//点击全屏翻页阅读
    public static final String PROGRESSCANCLE = "PROGRESSCANCLE";//拖动跳章取消

    //阅读页更多
    public static final String READ_SOURCECHANGE = "SOURCECHANGE";//换源
    public static final String READ_SOURCECHANGECONFIRM = "SOURCECHANGECONFIRM";//确认换源
    public static final String BOOKMARKEDIT = "BOOKMARKEDIT";//添加书签
    public static final String BOOKDETAIL = "BOOKDETAIL";//书籍详情

    //阅读完结页
    public static final String READFINISH = "READFINISH";//阅读完结页
    public static final String REPLACE = "REPLACE";   //完结页点击换一换
    public static final String TOSHELF = "TOSHELF";   //完结页点击去书架
    public static final String TOBOOKSTORE = "TOBOOKSTORE";   //完结页点击去书城

    //搜索结果页
    public static final String SEARCHRESULT = "SEARCHRESULT";//某本书点击
    public static final String SEARCHRESULT_BOOK = "BOOKCLICK";//某本书点击

    //Crash
    public static final String CRASH = "CRASH";

    //青果推荐页
    public static final String QG_TJY_MODULEEXPOSE = "MODULEEXPOSE";//模块露出
    public static final String QG_TJY_BOOKEXPOSE = "BOOKEXPOSE";//各书籍位置露出
    public static final String QG_TJY_SEARCH = "SEARCH";//点击搜索
    public static final String QG_TJY_BOOKCLICK = "BOOKCLICK";//点击搜索
    public static final String QG_TJY_MORE = "MORE";//点击搜索

    //青果榜单页
    public static final String QG_BDY_MODULEEXPOSE = "MODULEEXPOSE";//模块露出
    public static final String QG_BDY_BOOKEXPOSE = "BOOKEXPOSE";//各书籍位置露出
    public static final String QG_BDY_SEARCH = "SEARCH";//点击搜索
    public static final String QG_BDY_BOOKCLICK = "BOOKCLICK";//点击搜索
    public static final String QG_BDY_MORE = "MORE";//点击搜索


    //青果分类页
    public static final String QG_FL_FIRSTCLASS = "FIRSTCLASS";//点击一级分类
    public static final String QG_FL_BOOKCLICK = "BOOKCLICK";//书籍点击
    public static final String QG_FL_SEARCH = "SEARCH";//点击搜索

    //一级分类页面
    public static final String FIRST_SEARCH = "SEARCH";//点击搜索
    public static final String SWITCHTAB = "SWITCHTAB";//切换tab 男-女

    //下载解包
    public static final String DOWNLOADPACKE = "DOWNLOADPACKE";
    public static final String RESOLVEPACKE = "RESOLVEPACKE";


    public static final String DROPDOWN = "DROPDOWN";//下拉刷新

    private static List<String> prePageList = new ArrayList<>();

    private static Handler handler;

    static{
        HandlerThread handlerThread=new HandlerThread("upload-event-log-thread");
        handlerThread.start();
        handler=new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==0) {
                    String[] data = (String[]) msg.obj;
                    if (data != null && data.length == 2) {
                        String pageCode = data[0];
                        String identify = data[1];
                        final ServerLog log = getCommonLog();
                        log.putContent("code", identify);//点击事件唯一标识
                        log.putContent("page_code", pageCode);
                        log.putContent("pre_page_code", getPrePageCode(pageCode));
                        AppLog.e("log", log.getContent().toString());
                        if (identify.equals(APPINIT)) log.setEventType(LocalLog.getMINORITY());

                        AndroidLogStorage.getInstance().accept(log, BaseBookApplication.getGlobalContext());
                    }
                }else if(msg.what==1){
                    Object[] data=(Object[])msg.obj;
                    if(data!=null&&data.length==3) {
                        String pageCode = (String) data[0];
                        String identify = (String) data[1];
                        Map<String, String> extraParam = (Map<String, String>) data[2];
                        final ServerLog log = new ServerLog(PLItemKey.ZN_APP_EVENT);

                        log.putContent("project", PLItemKey.ZN_APP_EVENT.getProject());
                        log.putContent("logstore", PLItemKey.ZN_APP_EVENT.getLogstore());
                        log.putContent("code", identify);//点击事件唯一标识
                        log.putContent("page_code", pageCode);

                        upLoadUserInfo(log);

                        log.putContent("os", "android");//手机操作系统
                        log.putContent("log_time", System.currentTimeMillis() + "");//日志产生时间（毫秒数）
                        log.putContent("network", NetWorkUtils.NETTYPE);//网络状况
                        log.putContent("longitude", Constants.longitude + "");//经度
                        log.putContent("latitude", Constants.latitude + "");//纬度
                        log.putContent("city_info", Constants.adCityInfo);//城市
                        log.putContent("location_detail", Constants.adLocationDetail);//具体位置信息
                        log.putContent("pre_page_code", getPrePageCode(pageCode));

                        //事件对应的额外的参数部分

                        if (extraParam != null) {
                            log.putContent("data", FormatUtil.forMatMap(extraParam));
                        }
                        AppLog.e("log", log.getContent().toString());
                        AndroidLogStorage.getInstance().accept(log, BaseBookApplication.getGlobalContext());

                    }
                }
            }
        };
    }


    //上传普通的点击事件
    public static void upLoadEventLog(Context context, String pageCode, String identify) {
        if (!Constants.dy_ad_new_statistics_switch || context == null) {
            return;
        }
        if(handler!=null){
            Message message=handler.obtainMessage(0,new String[]{pageCode,identify});
            handler.sendMessage(message);
        }
    }


    public static void sendDirectLog(PLItemKey key, String page, String identify,
            Map<String, String> params) {
        LogGroup logGroup = new LogGroup("", "", key.getProject(),
                PLItemKey.ZN_APP_EVENT.getLogstore());
        ServerLog log = getCommonLog();
        log.putContent("code", identify);//点击事件唯一标识
        log.putContent("page_code", page);

        Set<Map.Entry<String, String>> entries = params.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            log.putContent(entry.getKey(), entry.getValue());
        }

        logGroup.PutLog(log);

        LOGClient logClient = new LOGClient(AndroidLogClient.endPoint, AndroidLogClient.accessKeyId,
                AndroidLogClient.accessKeySecret, key.getProject());
        try {
            long start = System.currentTimeMillis();
            logClient.PostLog(logGroup, key.getLogstore());
            Log.i("upload-Log", "useTime : " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void upLoadUserInfo(ServerLog log){
       if (BaseBookApplication.getGlobalContext().getPackageName().equals("cc.quanben.novel")){//登录二期

           if (UserManagerV4.INSTANCE.isUserLogin()) {
               log.putContent("uid", UserManagerV4.INSTANCE.getUser().getAccount_id());//用户中心唯一标识
           } else {
               log.putContent("uid", "");
           }
       } else {
           if (UserManager.INSTANCE.isUserLogin()) {
               log.putContent("uid", UserManager.INSTANCE.getMUserInfo().getUid());//用户中心唯一标识
           } else {
               log.putContent("uid", "");
           }
       }
    }


    @NonNull
    private static ServerLog getCommonLog() {
        final ServerLog log = new ServerLog(PLItemKey.ZN_APP_EVENT);

        log.putContent("project", PLItemKey.ZN_APP_EVENT.getProject());
        log.putContent("logstore", PLItemKey.ZN_APP_EVENT.getLogstore());

        upLoadUserInfo(log);

        log.putContent("os", "android");//手机操作系统
        log.putContent("log_time", System.currentTimeMillis() + "");//日志产生时间（毫秒数）
        log.putContent("network", NetWorkUtils.NETTYPE);//网络状况
        log.putContent("longitude", Constants.longitude + "");//经度
        log.putContent("latitude", Constants.latitude + "");//纬度
        log.putContent("city_info", Constants.adCityInfo);//城市
        log.putContent("location_detail", Constants.adLocationDetail);//具体位置信息
        return log;
    }

    //上传普通的点击事件,带事件参数
    public static void upLoadEventLog(Context context, String pageCode, String identify,
            Map<String, String> extraParam) {
        if (!Constants.dy_ad_new_statistics_switch || context == null) {
            return;
        }
        if(handler!=null){
            Message message=handler.obtainMessage(1,new Object[]{pageCode,identify,extraParam});
            handler.sendMessage(message);
        }
    }

    //上传用户App列表
    public static void upLoadApps(String applist) {
        if (!Constants.dy_ad_new_statistics_switch) {
            return;
        }
        final ServerLog log = new ServerLog(PLItemKey.ZN_APP_APPSTORE);
        upLoadUserInfo(log);
        log.putContent("apps", applist);
        log.putContent("time", System.currentTimeMillis() + "");
        AppLog.e("app_list", log.getContent().toString());
        AndroidLogStorage.getInstance().accept(log, BaseBookApplication.getGlobalContext());
    }


    public static void sendZnUserLog() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("udid",
                OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));
        parameters.put("app_package", AppUtils.getPackageName());
        parameters.put("app_version", AppUtils.getVersionName());
        parameters.put("app_version_code", String.valueOf(AppUtils.getVersionCode()));
        parameters.put("app_channel_id", AppUtils.getChannelId());
        parameters.put("phone_identity", AppUtils.getIMEI(BaseBookApplication.getGlobalContext()));
        parameters.put("vendor", Build.MODEL);
        parameters.put("os", Constants.APP_SYSTEM_PLATFORM + android.os.Build.VERSION.RELEASE);
        parameters.put("operator",
                AppUtils.getProvidersName(BaseBookApplication.getGlobalContext()));
        parameters.put("network", NetWorkUtils.NETTYPE);
        if (null != BaseBookApplication.getDisplayMetrics()) {
            String resolution_ratio = BaseBookApplication.getDisplayMetrics().widthPixels + "*" +
                    BaseBookApplication.getDisplayMetrics().heightPixels;
            parameters.put("resolution_ratio", resolution_ratio);
        }
        parameters.put("longitude", String.valueOf(Constants.longitude));
        parameters.put("latitude", String.valueOf(Constants.latitude));
        parameters.put("city_info", Constants.adCityInfo);
        parameters.put("location_detail", Constants.adLocationDetail);
        parameters.put("logstore", "zn_user");

        AppLog.e("zn_user", parameters.toString());
        LogEncapManager.getInstance().sendLog(parameters, "zn_user");
    }


    //上传用户阅读内容(传参按此格式顺序)
    public static void upLoadReadContent(String... params) {
        if (!Constants.dy_ad_new_statistics_switch || !Constants.dy_readPage_statistics_switch) {
            return;
        }
        ServerLog log = new ServerLog(PLItemKey.ZN_APP_READ_CONTENT);
        upLoadUserInfo(log);
        if (params != null) {
            log.putContent("book_id", params[0]);//书籍唯一字符串
            log.putContent("chapter_id", params[1]);//阅读章节唯一字符串
            log.putContent("source_ids", params[2]);//使用书籍源，中间有切换源则多个源使用分隔符"`"进行连接，尽量准确获取（不丢数据）
            log.putContent("page_num", params[3]);//当前阅读章节被切分的总页数
            log.putContent("pager", params[4]);//章节页数索引，即当前为第几页
            log.putContent("page_size", params[5]);//当前页尺寸，可以是byte或总字数（包括所有字符，需要知道当前页内容）
            log.putContent("from",
                    params[6]);//当前页面来源，所有可能来源的映射唯一字符串。书籍封面/书架/上一页翻页等等（不包括退出App后在进入来源）
            log.putContent("begin_time", params[7]);//进入当前页时间戳（秒数）
            log.putContent("end_time", params[8]);//退出当前页时间戳（秒数）（不包括用户退出App在进来，即该时间表示为用户主动翻页和主动退出阅读）
            log.putContent("read_time", params[9]);//总阅读时长秒数（考虑中间退出App的时长不应该包括进来，即排除打电话等时间）
            log.putContent("has_exit", params[10]);//是否有阅读中间退出行为
            log.putContent("channel_code", params[11]);//书籍来源1为青果，2为智能
            log.putContent("lon", Constants.longitude + "");//经度
            log.putContent("lat", Constants.latitude + "");//纬度


        }

        AppLog.e("log", log.getContent().toString());
//        AndroidLogStorage.getInstance().accept(log);
    }


    public static void sendPVData(String startReadTime , String bookId, String chapterId,  String sourceIds, String  channelCode,  String pageCount) {
        long endReadTime = System.currentTimeMillis()/1000L ;
        HashMap<String,String> params = new HashMap<>();
        params.put("book_id", bookId);
        params.put("book_source_id", sourceIds);
        params.put("chapter_id", chapterId);
        params.put("channel_code", channelCode);
        params.put("chapter_read", "1");
        params.put("chapter_pages", pageCount);
        params.put("start_time", startReadTime);
        params.put("end_time", endReadTime+"");
        params.put("udid", OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));
        params.put("app_package", AppUtils.getPackageName());
        params.put("app_version", AppUtils.getVersionName());
        params.put("app_version_code", AppUtils.getVersionCode()+"");
        params.put("app_channel_id", AppUtils.getChannelId());

        AppLog.e("zn_pv",params.toString());
        LogEncapManager.getInstance().sendLog(params, "zn_pv");
    }


    public static void upLoadChapterError(ChapterErrorBean bean) {
        if (!Constants.dy_ad_new_statistics_switch) {
            return;
        }
        final ServerLog log = new ServerLog(PLItemKey.ZN_APP_FEEDBACK);
        upLoadUserInfo(log);
        if (bean != null) {
            log.putContent("bookSourceId", bean.bookSourceId);
            log.putContent("bookName", decode(bean.bookName));
            log.putContent("author", decode(bean.author));
            log.putContent("bookChapterId", bean.bookChapterId);
            log.putContent("chapterId", bean.chapterId);
            log.putContent("chapterName", decode(bean.chapterName));
            log.putContent("serial", String.valueOf(bean.serial));
            log.putContent("host", bean.host);
            log.putContent("type", String.valueOf(bean.type));
            log.putContent("channel_code", bean.channelCode);
        }

        String channelId = AppUtils.getChannelId();
        String version = String.valueOf(AppUtils.getVersionName());
        String version_code = String.valueOf(AppUtils.getVersionCode());
        String packageName = AppUtils.getPackageName();
        String os = Constants.APP_SYSTEM_PLATFORM;
        String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
        String longitude = Constants.longitude + "";
        String latitude = Constants.latitude + "";
        String cityCode = Constants.cityCode;

        log.putContent("packageName", packageName);
        log.putContent("version", version);
        log.putContent("version_code", version_code);
        log.putContent("channelId", channelId);
        log.putContent("os", os);
        log.putContent("udid", udid);
        log.putContent("longitude", longitude);
        log.putContent("latitude", latitude);
        log.putContent("cityCode", cityCode);

        log.putContent("os", "android");
        log.putContent("network",
                NetWorkUtils.getNetWorkTypeNew(BaseBookApplication.getGlobalContext()));
        log.putContent("city_info", Constants.adCityInfo);
        log.putContent("location_detail", Constants.adLocationDetail);

        AndroidLogStorage.getInstance().accept(log,BaseBookApplication.getGlobalContext());
    }

    private static String decode(String content) {
        if (content == null || "".equals(content)) {
            return "";
        }
        try {
            return URLDecoder.decode(content, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    //获取prePageCode
    public synchronized static String getPrePageCode(String pageCode) {
        String pre_page_code = "";

        if (prePageList.size() == 0 || (prePageList.size() > 0 && pageCode != null
                && !prePageList.get(prePageList.size() - 1).equals(pageCode))) {
            prePageList.add(pageCode);
            removePre(prePageList);
        }
        if (prePageList != null && prePageList.size() != 0) {

            for (int i = 0; i < prePageList.size(); i++) {
                AppLog.e("loggggg", prePageList.get(i));
            }

            if (prePageList.size() > 1) {
                pre_page_code = prePageList.get(prePageList.size() - 2);
            } else {
                pre_page_code = prePageList.get(prePageList.size() - 1);
            }

        } else {
            pre_page_code = "";
        }
        return pre_page_code;
    }

    public static void removePre(List<String> prePageList) {

        if (prePageList.size() > 6) {
            for (int i = 0; i < 2; i++) {
                prePageList.remove(prePageList.get(i));
            }
        }

    }

}
