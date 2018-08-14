package net.lzbook.kit.utils;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import com.ding.basic.bean.Book;
import com.ding.basic.repository.RequestRepositoryFactory;

import net.lzbook.kit.request.UrlUtils;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSInterfaceHelper implements WebViewJsInterface {

    Context context;
    WebView webView;
    String TAG = "JSInterfaceHelper";
    onEnterAppClick enterApp;
    onAnotherWebClick anotherWeb;
    onGameAppClick gameAppClick;
    onWebGameClick webGameClick;
    onSearchClick search;
    onEnterCover cover;
    onEnterRead read;
    onTurnRead toRead;
    onEnterCategory mCategory;
    onOpenAd ad;
    OnShowToastListener showToast;
    OnCloseWebviewListener closeWebview;
    Handler handler;
    OnInsertBook insertBook;
    OnDeleteBook deleteBook;
    OnH5PagerInfoListener pagerInfo;
    String strings;
    private boolean isLogin = false;

    onSearchWordClick searchWordClick;
    OnSubSearchBook subSearchBook;



    public JSInterfaceHelper(Context context, WebView webView) {
        super();
        this.context = context;
        this.webView = webView;
        handler = new Handler();

    }

    public void setBookString(String strings) {
        this.strings = strings;
    }

    public void setOnInsertBook(OnInsertBook insertBook) {
        this.insertBook = insertBook;
    }

    public void setOnDeleteBook(OnDeleteBook deleteBook) {
        this.deleteBook = deleteBook;
    }

    public void setOnEnterAppClick(onEnterAppClick enterApp) {
        this.enterApp = enterApp;
    }

    public void setOnAnotherWebClick(onAnotherWebClick another) {
        this.anotherWeb = another;
    }

    public void setOnWebGameClick(onWebGameClick webGameClick) {
        this.webGameClick = webGameClick;
    }

    public void setOnGameAppClick(onGameAppClick gameAppClick) {
        this.gameAppClick = gameAppClick;
    }

    public void setOnSearchClick(onSearchClick search) {
        this.search = search;
    }

    public void setOnEnterCover(onEnterCover cover) {
        this.cover = cover;
    }

    public void setOnEnterRead(onEnterRead read) {
        this.read = read;
    }

    public void setOnEnterCategory(onEnterCategory category) {
        this.mCategory = category;
    }

    public void setOnOpenAd(onOpenAd ad) {
        this.ad = ad;
    }

    public void setOnH5PagerInfo(OnH5PagerInfoListener info) {
        this.pagerInfo = info;
    }

    @Override
    @JavascriptInterface
    public void enterSearch(final String keyWord, final String search_type, final String filter_type, final String filter_word, final String sort_type) {
        AppLog.e(TAG, "EnterSearch");
        if (keyWord == null || search_type == null || filter_type == null)
            return;

        handler.post(new Runnable() {

            @Override
            public void run() {
                if (search != null) {
                    search.doSearch(keyWord, search_type, filter_type, filter_word, sort_type);
                }
            }
        });
    }

    @Override
    @JavascriptInterface
    public String buildAjaxUrl(String url) {
        if (url != null) {
            String[] array = url.split("\\?");
            if (array.length == 2) {
                url = array[0];
                url = UrlUtils.buildWebUrl(url, UrlUtils.getUrlParams(array[1]));
            }
        }
        return url;
    }

    @Override
    @JavascriptInterface
    public String returnBooks() {
        AppLog.e(TAG, "String : " + strings);
        return strings;
    }

    @Override
    @JavascriptInterface
    public void doInsertBook(final String host, final String book_id, final String book_source_id, final String name, final String author, final String status, final String category, final String imgUrl, final String last_chapter, final String chapter_count, final String updateTime, final String parameter, final String extra_parameter, final String dex) {
        AppLog.e(TAG, "doInsertBook");
        if (name == null || book_id == null || book_source_id == null || author == null)
            return;

        handler.post(new Runnable() {

            @Override
            public void run() {
                if (insertBook != null) {
                    int dex1 = TextUtils.isEmpty(dex) ? 1 : Integer.parseInt(dex);
                    insertBook.doInsertBook(host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count, Long.valueOf(updateTime), parameter, extra_parameter, dex1);
                }
            }
        });
    }

    @Override
    @JavascriptInterface
    public void doDeleteBook(final String gid) {
        AppLog.e(TAG, "doDeleteBook");
        if (gid == null)
            return;

        handler.post(new Runnable() {

            @Override
            public void run() {
                if (deleteBook != null) {
                    deleteBook.doDeleteBook(gid);
                }
            }
        });
    }

    @Override
    @JavascriptInterface
    public void openWebView(final String url, final String name) {
        if (url == null || name == null)
            return;


        handler.post(new Runnable() {

            @Override
            public void run() {
                if (anotherWeb != null) {
                    anotherWeb.doAnotherWeb(url, name);
                }
            }
        });
    }

    @Override
    @JavascriptInterface
    public void openWebGame(final String url, final String name) {
        if (url == null || name == null)
            return;


        handler.post(new Runnable() {

            @Override
            public void run() {
                if (webGameClick != null) {
                    webGameClick.openWebGame(url, name);
                }
            }
        });

    }

    @Override
    @JavascriptInterface
    public void downloadGame(final String url, final String name) {
        if (url == null || name == null)
            return;


        handler.post(new Runnable() {

            @Override
            public void run() {
                if (gameAppClick != null) {
                    gameAppClick.downloadGame(url, name);
                }
            }
        });

    }

    @Override
    @JavascriptInterface
    public void enterApp(final String name) {
        if (name == null)
            return;

        handler.post(new Runnable() {

            @Override
            public void run() {
                if (enterApp != null) {
                    enterApp.doEnterApp(name);
                }
            }
        });


    }

    @Override
    @JavascriptInterface
    public void openAd(final String url) {
        if (url == null)
            return;

        handler.post(new Runnable() {

            @Override
            public void run() {
                if (ad != null) {
                    ad.doOpenAd(url);
                }
            }
        });


    }

    @Override
    @JavascriptInterface
    public void enterCover(final String host, final String book_id, final String book_source_id, final String name, final String author, final String parameter, final String extra_parameter) {
        if (host == null || book_id == null || book_source_id == null)
            return;

        handler.post(new Runnable() {

            @Override
            public void run() {
                AppLog.d(TAG, "enterNovelInfo " + "book_id " + book_id);
                if (cover != null) {
                    cover.doCover(host, book_id, book_source_id, name, author, parameter, extra_parameter);
                }
            }
        });


    }

    @Override
    @JavascriptInterface
    public void enterRead(final String host, final String book_id, final String book_source_id, final String name, final String author, final String status, final String category, final String imgUrl, final String last_chapter, final String chapter_count, final String updateTime, final String parameter, final String extra_parameter, final String dex) {
        if (book_id == null || name == null || author == null)
            return;

        AppLog.d(TAG, "enterNovelInfo " + "book_id " + book_id);
        handler.post(new Runnable() {

            @Override
            public void run() {
                AppLog.d(TAG, "enterRead " + "book_id " + book_id);

                if (read != null) {
                    int dex1 = TextUtils.isEmpty(dex) ? 1 : Integer.parseInt(dex);
                    read.doRead(host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count, Long.valueOf(updateTime), parameter, extra_parameter, dex1);
                }
            }
        });
    }


    // ========================================================
    // js调用 java 方法 并传参 ; js-->java :tell what to do
    // ======================================================
    //去重书架上的书
    @Override
    @JavascriptInterface

    public String uploadBookShelfList() {

        List<Book> bookShelfList = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBooks();
        StringBuilder bookIdList = new StringBuilder();

        if (bookShelfList != null && bookShelfList.size() > 0) {
            for (int i = 0; i < bookShelfList.size(); i++) {
                Book book = bookShelfList.get(i);
                if(i > 0) bookIdList.append(",");
                bookIdList.append(book.getBook_id());
            }
        }

        return bookIdList.toString();
    }

    //搜索无结果 点击订阅
    @Override
    @JavascriptInterface
    public void showSubBookDialog(final String word) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                if(subSearchBook!=null){
                    subSearchBook.showSubSearchBook(word);
                }
            }
        });
    }

    //收集打点信息,用于统计信息，提供给h5打点数据的通道
    @Override
    @JavascriptInterface
    public void collectInfo(String urlData) {
        AppLog.e("searchResult", "collectInfo");
        if (!urlData.equals("")) {
            Map<String, String> data = UrlUtils.getDataParams(urlData);
            //截取页面编码
            String pageCode = data.get("page_code");
            data.remove("page_code");
            //截取功能编码
            String functionCode = data.get("func_code");
            data.remove("func_code");
            StartLogClickUtil.upLoadEventLog(context, pageCode, functionCode, data);
        }
    }

    @Override
    @JavascriptInterface
    public void getH5ViewPagerInfo(String x, String y, String width, String height) {
        if (this.pagerInfo != null) {
            try {
                this.pagerInfo.onH5PagerInfo(Float.parseFloat(x), Float.parseFloat(y), Float.parseFloat(width), Float.parseFloat(height));
            } catch (Exception e) {
                e.printStackTrace();
                AppLog.e("kk",e.toString());
            }
        }
    }

    @Override
    @JavascriptInterface
    public void enterCategory(final String gid, final String nid,
                              final String name, final String lastSort) {
        if (gid == null || nid == null || lastSort == null || name == null)
            return;

        handler.post(new Runnable() {

            @Override
            public void run() {
                AppLog.d(TAG, "enterCategory " + "gid " + gid);
                if (mCategory != null) {
                    mCategory.doCategory(Integer.parseInt(gid),
                            Integer.parseInt(nid), name,
                            Integer.parseInt(lastSort));
                }
            }
        });

    }

    @Override
    @JavascriptInterface
    public void showToast(final String str) {
        if (str == null)
            return;
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (showToast != null) {
                    showToast.onShowToast(str);
                }
            }
        });
    }

    @Override
    @JavascriptInterface
    public void closeWebview() {

        handler.post(new Runnable() {

            @Override
            public void run() {
                AppLog.d(TAG, "closeWebview " + closeWebview);
                if (closeWebview != null) {
                    closeWebview.onCloseWebview();
                }
            }
        });
    }

    public interface OnInsertBook {
        void doInsertBook(final String host, final String book_id, final String book_source_id, final String name, final String author, final String status, final String category, final String imgUrl, final String last_chapter, final String chapter_count, final long updateTime, final String parameter, final String extra_parameter, final int dex);
    }

    public interface OnDeleteBook {
        void doDeleteBook(String gid);
    }

    public interface onEnterAppClick {
        void doEnterApp(final String name);
    }

    public interface onAnotherWebClick {
        void doAnotherWeb(String url, String name);
    }

    public interface onGameAppClick {
        void downloadGame(String url, String name);
    }

    public interface onWebGameClick {
        void openWebGame(String url, String name);
    }

    public interface onSearchClick {
        void doSearch(final String keyWord, final String search_type, final String filter_type, final String filter_word, final String sort_type);
    }

    //搜索优化新增

    public interface onSearchWordClick{
        void sendSearchWord(final String searchWord, final String search_type);
    }

    public void setSearchWordClick(onSearchWordClick searchWordClick){
        this.searchWordClick = searchWordClick;
    }

    public interface onTurnRead{
        void turnRead(String book_id, String book_source_id, String host, String name, String author, String parameter, String extra_parameter, String update_type, final String last_chapter_name, final int serial_number, final String img_url, final long update_time,final String desc,final String label,final String status,final String bookType);
    }

    public void setOnTurnRead(onTurnRead turnRead){
        this.toRead = turnRead;
    }



    //搜索无结果 点击订阅
    public interface OnSubSearchBook{
        void showSubSearchBook(String word);
    }
    public void setSubSearchBook(OnSubSearchBook subSearchBook){
        this.subSearchBook = subSearchBook;
    }


    @Override
    @JavascriptInterface
    public void turnToRead(final String book_id, final String book_source_id, final String host, final String name, final String author, final String parameter, final String extra_parameter, final String update_type, final String last_chapter_name, final String serial_number, final String img_url, final String update_time, final String desc, final String label, final String status, final String bookType) {
        if(!book_id.equals("") && !book_source_id.equals("")){

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(toRead != null){
                        toRead.turnRead(book_id, book_source_id, host, name, author, parameter, extra_parameter, update_type, last_chapter_name, Integer.valueOf(serial_number), img_url, Long.valueOf(update_time),desc,label,status,bookType);
                    }
                }
            });
        }
    }

    @Override
    @JavascriptInterface
    public void sendSearchWord(final String searchWord, final String search_type) {
        if(!searchWord.equals("")){

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(searchWordClick != null){
                        searchWordClick.sendSearchWord(searchWord, search_type);
                    }
                }
            });
        }
    }

    // ========================================================
    // 预留
    // ======================================================

    public interface onEnterCover {
        void doCover(String host, String book_id, String book_source_id, String name, String author, String parameter, String extra_parameter);
    }

    public interface onEnterRead {
        void doRead(String host, String book_id, String book_source_id, String name, String author, String status, String category, String imgUrl, String last_chapter, String chapter_count, long updateTime, String parameter, String extra_parameter, int dex);
    }

    public interface onEnterCategory {
        void doCategory(final int gid, final int nid, final String name,
                        final int lastSort);
    }


    public interface onOpenAd {
        void doOpenAd(String url);
    }

    public interface OnShowToastListener {
        void onShowToast(String str);
    }

    public interface OnCloseWebviewListener {
        void onCloseWebview();

    }

    public interface OnH5PagerInfoListener {
        void onH5PagerInfo(float x, float y, float width, float height);
    }


}
