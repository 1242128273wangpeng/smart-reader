package net.lzbook.kit.encrypt;

import java.util.Map;

public interface URLBuilderIntterface {

    // YS搜索提示
    public static final String YS_SEARCH_SUGGEST = "http://api.easou.com/api/bookapp/input_hint.m?word={word}&ch=blp1298_10269_001&appverion=1034&version=002&cid=eef_easou_book";

    // 封面
    @Deprecated
    public static final String COVER = "/v3/book/{book_id}/{book_source_id}/cover";
    // 章节列表
    public static final String CHAPTER_LIST = "/v3/book/{book_id}/{book_source_id}/chapter";
    // 小说源列表
    public static final String BOOK_SOURCE_SINGLE = "/v3/book/source/{book_id}/single";
    public static final String BOOK_SOURCE_BATCH = "/v3/book/source/batch";
    // 检查更新
    public static final String BOOK_CHECK = "/v4/book/check";
    // 默认书籍
    public static final String DEFAULT_BOOK = "/v3/book/default";
    // 搜索
    @Deprecated
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

    @Deprecated
    public static final String AUTHOR_V4 = "/v4/author/homepage/page";
    // 搜索
    @Deprecated
    public static final String SEARCH_V4 = "/v4/search/page";

    // webview精选页面
    public static final String WEB_RECOMMEND = "/h5/{packageName}/recommend";

    // webview排行页面
    public static final String WEB_RANK = "/h5/{packageName}/rank";
    // webview男频分类页面
    public static final String WEB_CATEGORY_MAN = "/h5/{packageName}/categoryBoy";
    // webview女频分类页面
    public static final String WEB_CATEGORY_WOMAN = "/h5/{packageName}/categoryGirl";

    // webview分类页面
    public static final String WEB_CATEGORY = "/{packageName}/v3/category/index.do";


    //获取书籍的下载地址接口
    public static final String GET_DOWN_ADDRESS = "/v3/book/{book_source_id}/downUrl";

    //书籍封面页 推荐书籍接口
    public static final String GET_COVER_RECOMMEND = "/v4/recommend/{book_id}/coverPage";

    // 动态参数
    public static final String DYNAMIC_PARAMAS = "/v3/dynamic/dynamicParameter";

    public String buildUrl(String host, String uriTag, Map<String, String> params);

    public String buildContentUrl(String url, Map<String, String> params);

    /************************** V5接口 **********************/

    // 搜索
    public static final String SEARCH_V5 = "/v5/search/page";

    @Deprecated
    public static final String SEARCH_VUE = "/h5/{packageName}/search";

}
