package net.lzbook.kit.pointpage;

/**
 * Desc 所有event类型点位  定义格式  页面编码_点位标识
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/9/7 15:41
 */
public class EventPoint {

    /** ------------------SYSTEM页面 对应的点位编码----------------------**/
    public static final String SYSTEM_APPINIT = "SYSTEM_APPINIT"; // 客户端启动
    public static final String SYSTEM_HOME = "SYSTEM_HOME"; // 切换至后台
    public static final String SYSTEM_ACTIVATE = "SYSTEM_ACTIVATE";// 切换至前台
    public static final String SYSTEM_BACK = "SYSTEM_BACK"; // 返回
    public static final String SYSTEM_CASHERESULT = "SYSTEM_CASHERESULT"; // 缓存结果
    public static final String SYSTEM_SEARCHRESULT = "SYSTEM_SEARCHRESULT"; // 被动搜索进入搜索结果页
    public static final String SYSTEM_PUSHCLICK = "SYSTEM_PUSHCLICK"; // push点击
    public static final String SYSTEM_PUSHRECEIVE = "SYSTEM_PUSHRECEIVE"; // push送达
    public static final String SYSTEM_UPDATE = "SYSTEM_UPDATE"; // 数据库升级
    public static final String SYSTEM_DOWNLOADPACKE = "SYSTEM_DOWNLOADPACKE"; // 下载
    public static final String SYSTEM_RESOLVEPACKE = "SYSTEM_RESOLVEPACKE"; // 解包


    /** ------------------MAIN主页 对应的点位编码-----------------------**/
    public static final String MAIN_BOOKSHELF = "MAIN_BOOKSHELF"; // 点击书架
    public static final String MAIN_RECOMMEND = "MAIN_RECOMMEND"; // 点击推荐
    public static final String MAIN_TOP = "MAIN_TOP"; // 点击榜单
    public static final String MAIN_CLASS = "MAIN_CLASS"; // 点击分类
    public static final String MAIN_BOOK_LIST = "MAIN_BOOK_LIST"; // 点击书单
    public static final String MAIN_PERSONAL = "MAIN_PERSONAL"; // 点击个人中心
    public static final String MAIN_SEARCH = "MAIN_SEARCH"; // 点击搜索框
    public static final String MAIN_CACHEMANAGE = "MAIN_CACHEMANAGE"; // 缓存管理
    public static final String MAIN_BOOKLIST = "MAIN_BOOKLIST";// 获取用户书架上的书
    public static final String MAIN_PUSHEXPOSE = "MAIN_PUSHEXPOSE";// 通知权限弹窗展现
    public static final String MAIN_PUSHCLOSE = "MAIN_PUSHCLOSE";// 通知权限弹窗关闭
    public static final String MAIN_POPUPNOWOPEN = "MAIN_POPUPNOWOPEN";// 通知权限弹窗点击现在开启
    public static final String MAIN_PUSHSET = "MAIN_PUSHSET";// 通知权限弹窗前往系统设置页
    public static final String MAIN_PREFERENCE = "MAIN_PREFERENCE";// 开屏选男女


    /** ------------------SEARCH搜索页 对应的点位编码-------------------**/
    public static final String SEARCH_BAR = "SEARCH_BAR"; // 点击搜索框
    public static final String SEARCH_BARCLEAR = "SEARCH_BARCLEAR"; // 搜索词清空
    public static final String SEARCH_BARLIST = "SEARCH_BARLIST"; // 搜索框下拉历史词点击
    public static final String SEARCH_HISTORY = "SEARCH_HISTORY"; // 搜索历史-点击某一条搜索历史（废弃）
    public static final String SEARCH_TOPIC = "SEARCH_TOPIC"; // 大家都在搜-点击搜索热词
    public static final String SEARCH_HOTREADCLICK = "SEARCH_HOTREADCLICK"; // 推荐书籍点击(热门阅读)
    public static final String SEARCH_HOTREADCHANGE = "SEARCH_HOTREADCHANGE"; // 热门阅读-换一换
    public static final String SEARCH_HISTORYCLEAR = "SEARCH_HISTORYCLEAR"; // 搜索历史-历史记录清空
    public static final String SEARCH_TIPLISTCLICK = "SEARCH_TIPLISTCLICK"; // 自动补全结果点击
    public static final String SEARCH_SEARCHBUTTON = "SEARCH_SEARCHBUTTON"; // 自动补全点击“搜索”按钮
    public static final String SEARCH_BACK = "SEARCH_BACK"; // 屏幕左上方点击返回按钮


    /** ------------------NORESULT搜索 无 结果页 对应的点位编码----------**/
    public static final String NORESULT_FEEDBACK = "NORESULT_FEEDBACK"; // 点击找书反馈
    public static final String NORESULT_BACK = "NORESULT_BACK"; // 返回


    /** ------------------找书反馈页（对话框） 对应的点位编码-------------**/
    public static final String FEEDBACK_SUBMIT = "FEEDBACK_SUBMIT"; // 点击提交按钮


    /** ------------------上架通知 对应的点位编码       ----------------**/
    public static final String SHELFNOTICE_READCLICK = "SHELFNOTICE_READCLICK"; // 点击阅读


    /** ------------------书架页面 对应的点位编码       ----------------**/
    public static final String SHELF_BOOKCLICK = "SHELF_BOOKCLICK"; // 书籍点击
    public static final String SHELF_TOBOOKCITY = "SHELF_TOBOOKCITY"; // 书架无书籍时，点击跳转书城
    public static final String SHELF_LONGTIMEBOOKSHELFEDIT = "SHELF_LONGTIMEBOOKSHELFEDIT"; // 长按编辑书架
    public static final String SHELF_CACHEMANAGE = "SHELF_CACHEMANAGE"; // 点击缓存管理
    public static final String SHELF_MORE = "SHELF_MORE"; // 点击更多
    public static final String SHELF_BOOKSORT = "SHELF_BOOKSORT"; // 点击更多菜单中的排序
    public static final String SHELF_VERSIONUPDATE = "SHELF_VERSIONUPDATE"; // 版本更新
    public static final String SHELF_BANNERPOPUPCLICK = "SHELF_BANNERPOPUPCLICK"; // 活动弹窗点击
    public static final String SHELF_BANNERPOPUPSHOW = "SHELF_BANNERPOPUPSHOW"; // 活动弹窗曝光
    public static final String SHELF_BANNERPOPUPCLOSE = "SHELF_BANNERPOPUPCLOSE"; // 活动弹窗关闭


    /** ------------------书架排序弹窗 对应的点位编码  ------------------**/
    public static final String SHELFSORT_CANCLE = "SHELFSORT_CANCLE"; // 取消
    public static final String SHELFSORT_BOOKSORT = "SHELFSORT_BOOKSORT"; // 排序


    /** ------------------书架编辑页面 对应的点位编码   ----------------**/
    public static final String SHELFEDIT_SELECTALL = "SHELFEDIT_SELECTALL"; // 全选
    public static final String SHELFEDIT_DELETE = "SHELFEDIT_DELETE"; // 删除
    public static final String SHELFEDIT_CANCLE = "SHELFEDIT_CANCLE"; // 右上角取消
    public static final String SHELFEDIT_BACK = "SHELFEDIT_BACK"; // 屏幕左上方点击返回按钮


    /** ------------------书籍详情页面 对应的点位编码   ----------------**/
    public static final String BOOOKDETAIL_ENTER = "BOOOKDETAIL_ENTER"; // 进入书籍详情页
    public static final String BOOOKDETAIL_INTRODUCTION = "BOOOKDETAIL_INTRODUCTION"; // 简介点击展开/收起
    public static final String BOOOKDETAIL_LATESTCHAPTER = "BOOOKDETAIL_LATESTCHAPTER"; // 点击最新章节（目录）
    public static final String BOOOKDETAIL_CASHEALL = "BOOOKDETAIL_CASHEALL"; // 点击全本缓存
    public static final String BOOOKDETAIL_SHELFADD = "BOOOKDETAIL_SHELFADD"; // 点击加入书架
    public static final String BOOOKDETAIL_TRANSCODEREAD = "BOOOKDETAIL_TRANSCODEREAD"; // 点击转码阅读
    public static final String BOOOKDETAIL_TRANSCODEPOPUP = "BOOOKDETAIL_TRANSCODEPOPUP"; // 转码对话框操作
    public static final String BOOOKDETAIL_BACK = "BOOOKDETAIL_BACK"; // 返回
    public static final String BOOOKDETAIL_AUTHORBOOKROCOM = "BOOOKDETAIL_AUTHORBOOKROCOM"; // 点击作者其他作品
    public static final String BOOOKDETAIL_RECOMMENDEDBOOK = "BOOOKDETAIL_RECOMMENDEDBOOK"; // 点击读过这本书的人还读过作品
    public static final String BOOOKDETAIL_LABLECLICK = "BOOOKDETAIL_LABLECLICK"; // 书籍详情页标签点击
    public static final String BOOOKDETAIL_CATALOG = "BOOOKDETAIL_CATALOG"; //书籍详情页查看目录点击


    /** ------------------个人中心页面 对应的点位编码  ----------------**/
    public static final String PERSONAL_NIGHTMODE = "PERSONAL_NIGHTMODE"; // 点击夜间模式
    public static final String PERSONAL_MORESET = "PERSONAL_MORESET"; // 点击更多设置
    public static final String PERSONAL_HELP = "PERSONAL_HELP"; // 点击帮助与反馈
    public static final String PERSONAL_COMMENT = "PERSONAL_COMMENT"; // 点击去评分
    public static final String PERSONAL_VERSION = "PERSONAL_VERSION"; // 点击当前版本
    public static final String PERSONAL_VERSIONUPDATE = "PERSONAL_VERSIONUPDATE"; // 点击版本更新
    public static final String PERSONAL_CACHECLEAR = "PERSONAL_CACHECLEAR"; // 点击清除缓存
    public static final String PERSONAL_PROCTCOL = "PERSONAL_PROCTCOL"; // 点击使用协议
    public static final String PERSONAL_HISTORYLOGIN = "PERSONAL_HISTORYLOGIN"; // 点击浏览足迹内的登录
    public static final String PERSONAL_BACK = "PERSONAL_BACK"; // 屏幕左上方点击返回按钮
    public static final String PERSONAL_ADPAGE = "PERSONAL_ADPAGE"; // 福利中心
    public static final String PERSONAL_WIFI_AUTOCACHE = "PERSONAL_WIFI_AUTOCACHE"; // 点击WIFI自动缓存
    public static final String PERSONAL_WEBCOLLECT = "PERSONAL_WEBCOLLECT"; // 点击网页收藏
    public static final String PERSONAL_LOGIN = "PERSONAL_LOGIN"; // 登录
    public static final String PERSONAL_LOGOUT = "PERSONAL_LOGOUT"; // 退出登录


    /** ------------------个人中心中更多设置页面 对应的点位编码---------**/
    public static final String MORESET_PUSHSET = "MORESET_PUSHSET"; // 消息推送开启与关闭
    public static final String MORESET_PUSHAUDIO = "MORESET_PUSHAUDIO"; // 推送声音
    public static final String MORESET_BACK = "MORESET_BACK"; // 屏幕左上方点击返回按钮


    /** -----------------------阅读 页面 对应的点位编码  -------------**/
    public static final String READPAGE_LABELEDIT = "READPAGE_LABELEDIT"; // 添加书签(其他壳均在使用)
    public static final String READPAGE_ORIGINALLINK = "READPAGE_ORIGINALLINK"; // 点击源网页链接
    public static final String READPAGE_CACHE = "READPAGE_CACHE"; // 点击阅读页内缓存
    public static final String READPAGE_MORE = "READPAGE_MORE"; // 点击阅读页内更多
    public static final String READPAGE_CATALOG = "READPAGE_CATALOG"; // 点击阅读页内目录
    public static final String READPAGE_BOOKMARK = "READPAGE_BOOKMARK"; // 点击阅读页目录内书签
    public static final String READPAGE_NIGHTMODE = "READPAGE_NIGHTMODE"; // 点击阅读页内日/夜间模式
    public static final String READPAGE_CHAPTERTURN = "READPAGE_CHAPTERTURN"; // 点击阅读页内上/下章切换
    public static final String READPAGE_SET = "READPAGE_SET"; // 点击阅读页内设置
    public static final String READPAGE_REPAIRDEDIALOGUE = "READPAGE_REPAIRDEDIALOGUE"; // 弹出修复提示弹窗
    public static final String READPAGE_DIRECTORYREPAIR = "READPAGE_DIRECTORYREPAIR"; // 点击阅读页目录内修复书籍
    public static final String READPAGE_POPUPSHELFADD = "READPAGE_POPUPSHELFADD"; // 阅读页加入书架弹窗加入
    public static final String READPAGE_POPUPSHELFADDCANCLE = "READPAGE_POPUPSHELFADDCANCLE"; // 阅读页加入书架弹窗取消
    public static final String READPAGE_BACK = "READPAGE_BACK"; // 屏幕左上方点击返回按钮
    public static final String READPAGE_PROGRESSCANCLE = "READPAGE_PROGRESSCANCLE"; // 拖动跳章取消
    public static final String READPAGE_DEFAULTSETTINGS = "READPAGE_DEFAULTSETTINGS"; // 用户阅读设置
    public static final String READPAGE_SHARE = "READPAGE_SHARE"; // 分享


    /** -----------------------阅读页设置页面 对应的点位编码-----------**/
    public static final String READPAGESET_LIGHTEDIT = "READPAGESET_LIGHTEDIT"; // 点击亮度调整
    public static final String READPAGESET_SYSFOLLOW = "READPAGESET_SYSFOLLOW"; // 点击跟随系统
    public static final String READPAGESET_WORDSIZE = "READPAGESET_WORDSIZE"; // 点击字号增/减
    public static final String READPAGESET_BACKGROUNDCOLOR = "READPAGESET_BACKGROUNDCOLOR"; // 点击阅读背景色
    public static final String READPAGESET_READGAP = "READPAGESET_READGAP"; // 点击阅读间距
    public static final String READPAGESET_PAGETURN = "READPAGESET_PAGETURN"; // 点击翻页模式
    public static final String READPAGESET_HPMODEL = "READPAGESET_HPMODEL"; // 点击横/竖屏模式
    public static final String READPAGESET_AUTOREAD = "READPAGESET_AUTOREAD"; // 点击自动阅读
    public static final String READPAGESET_FULLSCREENPAGEREAD = "READPAGESET_FULLSCREENPAGEREAD"; // 点击全屏翻页阅读
    public static final String READPAGESET_FONTSETTING = "READPAGESET_FONTSETTING"; // 点击使用字体
    public static final String READPAGESET_FONTDOWNLOAD = "READPAGESET_FONTDOWNLOAD"; // 下载字体
    public static final String READPAGESET_DOWNSTATUS = "READPAGESET_DOWNSTATUS"; // 语音包下载状态
    public static final String READPAGESET_TTSSETTING = "READPAGESET_TTSSETTING"; // 设置听书功能
    public static final String READPAGESET_DOWNLOADDIALOGUE = "READPAGESET_DOWNLOADDIALOGUE"; // 弹出下载语音包提示弹窗
    public static final String READPAGESET_CLOSE = "READPAGESET_CLOSE"; // 退出听书功能
    public static final String READPAGESET_TTS = "READPAGESET_TTS"; // 点击听书按钮


    /** -----------------------阅读页更多菜单中 对应的点位编码---------**/
    public static final String READPAGEMORE_SOURCECHANGE = "READPAGEMORE_SOURCECHANGE"; // 换源
    public static final String READPAGEMORE_SOURCECHANGECONFIRM = "READPAGEMORE_SOURCECHANGECONFIRM"; // 换源确认
    public static final String READPAGEMORE_BOOKDETAIL = "READPAGEMORE_BOOKDETAIL"; // 书籍详情
    public static final String READPAGEMORE_FEEDBACK = "READPAGEMORE_FEEDBACK"; // 反馈


    /** -----------------------缓存管理页面 对应的点位编码-------------**/
    public static final String CACHEMANAGE_BOOKCLICK = "CACHEMANAGE_BOOKCLICK"; // 书籍点击
    public static final String CACHEMANAGE_CACHEBUTTON = "CACHEMANAGE_CACHEBUTTON"; // 缓存按钮点击
    public static final String CACHEMANAGE_CACHEEDIT = "CACHEMANAGE_CACHEEDIT"; // 右上角编辑按钮点击
    public static final String CACHEMANAGE_MORE = "CACHEMANAGE_MORE"; // 右上角更多按钮点击
    public static final String CACHEMANAGE_SORT = "CACHEMANAGE_SORT"; // 右上角菜单中的排序选项
    public static final String CACHEMANAGE_TOBOOKCITY = "CACHEMANAGE_TOBOOKCITY"; // 无缓存，跳转到书城
    public static final String CACHEMANAGE_BACK = "CACHEMANAGE_BACK";


    /** ------------------缓存编辑页面 对应的点位编码   ----------------**/
    public static final String CHCHEEDIT_SELECTALL = "CHCHEEDIT_SELECTALL"; // 全选
    public static final String CHCHEEDIT_DELETE = "CHCHEEDIT_DELETE"; // 删除
    public static final String CHCHEEDIT_CANCLE = "CHCHEEDIT_DELETE"; // 右上角取消
    public static final String CHCHEEDIT_BACK = "CHCHEEDIT_BACK"; // 屏幕左上方点击返回按钮


    /** -----------------------分类页面 对应的点位编码----------------**/
    public static final String CLASS_SEARCH = "CLASS_SEARCH"; // 点击搜索
    public static final String CLASS_SWITCHTAB = "CLASS_SWITCHTAB"; // 切换男女标签


    /** -----------------------点击分类之后的一级页面 对应的点位编码----**/
    public static final String FIRSTCLASS_SEARCH = "FIRSTCLASS_SEARCH"; // 点击搜索
    public static final String FIRSTCLASS_BACK = "FIRSTCLASS_BACK"; // 返回


    /** -----------------------榜单页面 对应的点位编码----------------**/
    public static final String TOP_SEARCH = "TOP_SEARCH"; // 点击搜索


    /** -----------------------点击各排行榜之后的一级页面 对应的点位编码**/
    public static final String FIRSTTOP_SEARCH = "FIRSTTOP_SEARCH"; // 点击搜索
    public static final String FIRSTTOP_BACK = "FIRSTTOP_BACK"; // 屏幕左上方点击返回按钮


    /** -----------------------推荐/精选页面 对应的点位编码-----------**/
    public static final String RECOMMEND_SEARCH = "RECOMMEND_SEARCH"; // 点击搜索
    public static final String RECOMMEND_DROPDOWN = "RECOMMEND_DROPDOWN"; // 下拉刷新


    /** -----------------------一级推荐页面 对应的点位编码-----------**/
    public static final String FIRSTRECOMMEND_SEARCH = "FIRSTRECOMMEND_SEARCH"; // 搜索
    public static final String FIRSTRECOMMEND_BACK = "FIRSTRECOMMEND_BACK"; // 返回


    /** -----------------------使用协议页面 对应的点位编码-----------**/
    public static final String PROCTCOL_BACK = "PROCTCOL_BACK"; // 返回


    /** -----------------------阅读完结页面 对应的点位编码-----------**/
    public static final String READFINISH_REPLACE = "READFINISH_REPLACE"; // 换一换
    public static final String READFINISH_RECOMMENDEDBOOK = "READFINISH_RECOMMENDEDBOOK"; // 点击推荐书籍
    public static final String READFINISH_BACK = "READFINISH_BACK"; // 返回
    public static final String READFINISH_ENTER = "READFINISH_ENTER"; // 进入书籍完结页
    public static final String READFINISH_TOSHELF = "READFINISH_TOSHELF"; // 点击去书架
    public static final String READFINISH_TOBOOKSTORE = "READFINISH_TOBOOKSTORE"; // 点击去书城
    public static final String READFINISH_SOURCECHANGE = "READFINISH_SOURCECHANGE"; // 点击换源


    /** -----------------------登录页面统计 对应的点位编码-----------**/
    public static final String LOGIN_BACK = "LOGIN_BACK"; // 返回
    public static final String LOGIN_UIDDIFFUSER = "LOGIN_UIDDIFFUSER"; // 不同用户在同设备登录


    /** -----------------------作者主页 对应的点位编码--------------**/
    public static final String AUTHORPAGE_BACK = "AUTHORPAGE_BACK"; // 返回
    public static final String AUTHORPAGE_SEARCH = "AUTHORPAGE_SEARCH"; //


    /** -----------------------个人中心浏览历史 对应的点位编码-------**/
    public static final String PERHISTORY_BACK = "PERHISTORY_BACK"; // 返回


    /** -----------------------书籍目录页 对应的点位编码------------**/
    public static final String BOOKCATALOG_CATALOGCHAPTER = "BOOKCATALOG_CATALOGCHAPTER"; // 目录中点击某章节
    public static final String BOOKCATALOG_TRANSCODEREAD = "BOOKCATALOG_TRANSCODEREAD"; // 点击转码阅读
    public static final String BOOKCATALOG_BACK = "BOOKCATALOG_BACK"; // 书籍目录返回


    /** -----------------------书籍列表 对应的点位编码------------**/
    public static final String BOOKLIST_ENTER = "BOOKLIST_ENTER"; // 消息推送点击进入书籍列表

    public static final String SHAREPAGE_SHARE = "SHAREPAGE_SHARE"; // 分享弹窗  分享
    public static final String SHAREPAGE_CANCEL = "SHAREPAGE_CANCEL"; // 分享弹窗  取消


    /** -----------------------网页收藏列表 对应的点位编码------------**/
    public static final String WEBCOLLECT_BACK = "WEBCOLLECT_BACK"; // 返回
    public static final String WEBCOLLECT_CACHEEDIT = "WEBCOLLECT_CACHEEDIT"; // 删除
    public static final String WEBCOLLECT_LINKCLICK = "WEBCOLLECT_LINKCLICK"; // 列表点击
    public static final String WEBCOLLECT_LINKLIST = "WEBCOLLECT_LINKLIST"; // 页面打开上传用户收藏数据


    /** -----------------------网页收藏编辑功能 对应的点位编码------------**/
    public static final String WEBCHCHEEDIT_SELECTALL = "WEBCHCHEEDIT_SELECTALL"; // 全选
    public static final String WEBCHCHEEDIT_DELETE = "WEBCHCHEEDIT_DELETE"; // 删除
    public static final String WEBCHCHEEDIT_CANCLE = "WEBCHCHEEDIT_CANCLE"; // 取消


    /** -----------------------全网搜结果页面 对应的点位编码------------**/
    public static final String WEBSEARCHRESULT_ENTER = "WEBSEARCHRESULT_ENTER"; // 进入页面
    public static final String WEBSEARCHRESULT_LINKCLICK = "WEBSEARCHRESULT_LINKCLICK"; // 列表点击
    public static final String WEBSEARCHRESULT_BACK = "WEBSEARCHRESULT_BACK"; // 返回
    public static final String WEBSEARCHRESULT_BARCLEAR = "WEBSEARCHRESULT_BARCLEAR"; // 清空搜索词
    public static final String WEBSEARCHRESULT_WEBCOLLECT = "WEBSEARCHRESULT_WEBCOLLECT"; // 网页收藏功能
    public static final String WEBSEARCHRESULT_CLOSE = "WEBSEARCHRESULT_CLOSE"; // 网页一键关闭



}
