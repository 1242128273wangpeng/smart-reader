package net.lzbook.kit.encrypt;

import java.util.Map;

public interface URLBuilderIntterface {

    // YS搜索提示
    public static final String YS_SEARCH_SUGGEST = "http://api.easou.com/api/bookapp/input_hint.m?word={word}&ch=blp1298_10269_001&appverion=1034&version=002&cid=eef_easou_book";

    // 封面
    public static final String COVER = "/v3/book/{book_id}/{book_source_id}/cover";
    // 章节列表
    public static final String CHAPTER_LIST = "/v3/book/{book_id}/{book_source_id}/chapter";
    // 小说源列表
    public static final String BOOK_SOURCE_SINGLE = "/v3/book/source/{book_id}/single";
    public static final String BOOK_SOURCE_BATCH = "/v3/book/source/batch";
    // 检查更新
    public static final String BOOK_CHECK = "/v3/book/check";
    // 默认书籍
    public static final String DEFAULT_BOOK = "/v3/book/default";
    // 搜索
    public static final String SEARCH = "/v3/search";
    // 日志
    public static final String LOG = "/v3/log";
    // 章节错误反馈
    public static final String CHAPTER_ERROR_FEEDBACK = "/v3/log/fb";
    // APP版本检查更新
    public static final String APP_CHECK = "/v3/app/check";
    // 更新书架dex值和书本的连载完结状态
    public static final String UPDATE_SHELF_BOOKS = "/v3/book/covers";
    // dex
    public static final String DEX_CHECK = "/v3/dynamic/check/encryption/{premVersion}";

    // webview精选页面
    public static final String WEB_RECOMMEND = "/{packageName}/v3/recommend/index.do";
    // webview排行页面
    public static final String WEB_RANK = "/{packageName}/v3/rank/index.do";
    // webview分类页面
    public static final String WEB_CATEGORY = "/{packageName}/v3/category/index.do";
    //获取书籍的下载地址接口
    public static final String GET_DOWN_ADDRESS = "/v3/book/{book_source_id}/downUrl";

    //书籍封面页 推荐书籍接口
    public static final String GET_COVER_RECOMMEND = "/v4/recommend/{book_id}/coverPage";
    // webview分类页面
    public static final String DYNAMIC_PARAMAS = "/v3/dynamic/dynamicParameter";

    public String buildUrl(String host, String uriTag, Map<String, String> params);

    public String buildContentUrl(String url, Map<String, String> params);

}
