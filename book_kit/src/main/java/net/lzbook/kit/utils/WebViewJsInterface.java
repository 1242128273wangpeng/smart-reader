package net.lzbook.kit.utils;

/**
 * 供js调用的对象方法
 */
public interface WebViewJsInterface {


    void enterApp(final String name);//进入app

    void openAd(final String url);

    void showToast(String str);

    void openWebView(final String url, final String name);// 打开webView

    void closeWebview();

    String buildAjaxUrl(String url);

    void enterCover(final String host, final String book_id, final String book_source_id, final String name, final String author, final String parameter, final String extra_parameter);// 进入封面页面

    void enterRead(final String host, final String book_id, final String book_source_id, final String name, final String author, final String status, final String category, final String imgUrl, final String last_chapter, final String chapter_count, final String updateTime, final String parameter, final String extra_parameter, final String dex);// 进入阅读页面

    void enterCategory(final String gid, final String nid, final String name, final String lastSort);// 进入目录页面

    void enterSearch(final String keyWord, final String search_type, final String filter_type, final String filter_word, final String sort_type);// 进入搜索结果页面


    void doInsertBook(String host, String book_id, String book_source_id, String name, String author, String status, String category, String imgUrl, String last_chapter, String chapter_count, String updateTime, String parameter, String extra_parameter, String dex);

    void doDeleteBook(String gid);

    String returnBooks();

    //统计信息
    void collectInfo(String urlData);

    //获得H5页面ViewPager边界
    void getH5ViewPagerInfo(String x, String y, String width, String height);
}
