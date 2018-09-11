package net.lzbook.kit.utils;

/**
 * 供js调用的对象方法
 */
public interface WebViewJsInterface {


    /**
     * 进度app, 目前没有任何实现
     * @param name
     */
    void enterApp(final String name);

    /**
     * 打开广告, 目前没有任何实现
     * @param url
     */
    void openAd(final String url);

    /**
     * 弹出toast, 没有看到使用
     * @param str
     */
    void showToast(String str);

    /**
     * 进入 FindBookDetail 界面
     * @param url
     * @param name title
     */
    void openWebView(final String url, final String name);

    /**
     * 打开网页游戏
     * @param url
     * @param name
     */
    void openWebGame(final String url, final String name);

    /**
     * 下载应用, 调用到DownloadAPKService
     * @param url
     * @param name
     */
    void downloadGame(final String url, final String name);

    /**
     * 关闭页面, 目前空实现
     */
    void closeWebview();

    /**
     * 生成带有token的请求
     * @param url
     * @return
     */
    String buildAjaxUrl(String url);

    /**
     * 进入 CoverPageActivity
     * @param host 未使用参数
     * @param book_id
     * @param book_source_id
     * @param name 未使用参数
     * @param author 未使用参数
     * @param parameter 未使用参数
     * @param extra_parameter 未使用参数
     */
    void enterCover(final String host, final String book_id, final String book_source_id, final String name, final String author, final String parameter, final String extra_parameter);// 进入封面页面

    /**
     * 进入阅读
     * @param host
     * @param book_id
     * @param book_source_id
     * @param name
     * @param author
     * @param status
     * @param category
     * @param imgUrl
     * @param last_chapter
     * @param chapter_count
     * @param updateTime
     * @param parameter 无用参数
     * @param extra_parameter 无用参数
     * @param dex 无用参数
     */
    void enterRead(final String host, final String book_id, final String book_source_id, final String name, final String author, final String status, final String category, final String imgUrl, final String last_chapter, final String chapter_count, final String updateTime, final String parameter, final String extra_parameter, final String dex);// 进入阅读页面

    /**
     * 进入目录页, 目前没有使用
     * @param gid
     * @param nid
     * @param name
     * @param lastSort
     */
    void enterCategory(final String gid, final String nid, final String name, final String lastSort);// 进入目录页面

    /**
     * 进入搜索
     * @param keyWord
     * @param search_type
     * @param filter_type
     * @param filter_word
     * @param sort_type
     */
    void enterSearch(final String keyWord, final String search_type, final String filter_type, final String filter_word, final String sort_type);// 进入搜索结果页面


    /**
     * 添加书架
     * @param host
     * @param book_id
     * @param book_source_id
     * @param name
     * @param author
     * @param status
     * @param category
     * @param imgUrl
     * @param last_chapter
     * @param chapter_count
     * @param updateTime
     * @param parameter 无用参数
     * @param extra_parameter 无用参数
     * @param dex 无用参数
     */
    void doInsertBook(String host, String book_id, String book_source_id, String name, String author, String status, String category, String imgUrl, String last_chapter, String chapter_count, String updateTime, String parameter, String extra_parameter, String dex);

    /**
     * 移除书架
     * @param gid
     */
    void doDeleteBook(String gid);

    /**
     * 获得书架上的所有书
     * @return json结构
     */
    String returnBooks();


    /**
     * 统计信息
     * @param urlData, page_code=value#func_code=value
     */
    void collectInfo(String urlData);

    /**
     * 获得H5页面ViewPager边界, 用于手势回退时, 规避掉h5横向滑动区域
     * @param x
     * @param y
     * @param width
     * @param height
     */
    void getH5ViewPagerInfo(String x, String y, String width, String height);

    /**
     * 搜索优化, 未使用接口
     * @param book_id
     * @param book_source_id
     * @param host
     * @param name
     * @param author
     * @param parameter
     * @param extra_parameter
     * @param update_type
     * @param last_chapter_name
     * @param serial_number
     * @param img_url
     * @param update_time
     * @param desc
     * @param label
     * @param status
     * @param bookType
     */
    void turnToRead(final String book_id, final String book_source_id, final String host, final String name, final String author, final String parameter, final String extra_parameter, final String update_type, final String last_chapter_name, final String serial_number, final String img_url, final String update_time,final String desc,final String label,final String status,final String bookType);

    /**
     * 点击搜索辞
     * @param searchWord
     * @param searchType
     */
    void sendSearchWord(final String searchWord, final String searchType);

    /**
     * 搜索去重, 获得书架上所有的书
     * @return 逗号分隔的所有书的id
     */
    String uploadBookShelfList();

    /**
     * 搜索无结果  点击订阅
     */
    void showSubBookDialog(final String word);

    /**
     * 搜索结果回调
     *
     * @param result 1表示有结果，2表示搜索无结果
     */
    void onSearchResult(int result);

}
