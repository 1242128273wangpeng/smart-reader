package net.lzbook.kit.utils;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import net.lzbook.kit.request.UrlUtils;

public class JSInterfaceHelper implements WebViewJsInterface {

    Context context;
    WebView webView;
    String TAG = "JSInterfaceHelper";
    onEnterAppClick enterApp;
    onAnotherWebClick anotherWeb;
    onSearchClick search;
    onEnterCover cover;
    onEnterRead read;
    onEnterCategory mCategory;
    onOpenAd ad;
    OnShowToastListener showToast;
    OnCloseWebviewListener closeWebview;
    Handler handler;
    OnInsertBook insertBook;
    OnDeleteBook deleteBook;
    String strings;

    public JSInterfaceHelper(Context context, WebView webView) {
        super();
        this.context = context;
        this.webView = webView;
        handler = new Handler();

    }

    public void setBookString(String strings) {
        this.strings = strings;
    }

    public interface OnInsertBook {
        void doInsertBook(final String host, final String book_id, final String book_source_id, final String name, final String author, final String status, final String category, final String imgUrl, final String last_chapter, final String chapter_count, final long updateTime, final String parameter, final String extra_parameter, final int dex);
    }

    public interface OnDeleteBook {
        void doDeleteBook(String gid);
    }

    public void setOnInsertBook(OnInsertBook insertBook) {
        this.insertBook = insertBook;
    }

    public void setOnDeleteBook(OnDeleteBook deleteBook) {
        this.deleteBook = deleteBook;
    }


    public interface onEnterAppClick {
        void doEnterApp(final String name);
    }

    public interface onAnotherWebClick {
        void doAnotherWeb(String url, String name);
    }

    public interface onSearchClick {
        void doSearch(final String keyWord, final String search_type, final String filter_type, final String filter_word, final String sort_type);
    }

    public void setOnEnterAppClick(onEnterAppClick enterApp) {
        this.enterApp = enterApp;
    }

    public void setOnAnotherWebClick(onAnotherWebClick another) {
        this.anotherWeb = another;
    }

    public void setOnSearchClick(onSearchClick search) {
        this.search = search;
    }

    public interface onEnterCover {
        void doCover(String host, String book_id, String book_source_id, String name, String author, String parameter, String extra_parameter);
    }

    public void setOnEnterCover(onEnterCover cover) {
        this.cover = cover;
    }

    public interface onEnterRead {
        void doRead(String host, String book_id, String book_source_id, String name, String author, String status, String category, String imgUrl, String last_chapter, String chapter_count, long updateTime, String parameter, String extra_parameter, int dex);
    }

    public void setOnEnterRead(onEnterRead read) {
        this.read = read;
    }

    public interface onEnterCategory {
        void doCategory(final int gid, final int nid, final String name,
                        final int lastSort);
    }

    public void setOnEnterCategory(onEnterCategory category) {
        this.mCategory = category;
    }


    public interface onOpenAd {
        void doOpenAd(String url);
    }

    public void setOnOpenAd(onOpenAd ad) {
        this.ad = ad;
    }

    public interface OnShowToastListener {
        void onShowToast(String str);
    }

    public interface OnCloseWebviewListener {
        void onCloseWebview();

    }


    // ========================================================
    // js调用 java 方法 并传参 ; js-->java :tell what to do
    // ======================================================

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
            }
            url = UrlUtils.buildWebUrl(url, UrlUtils.getUrlParams(array[1]));
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


    // ========================================================
    // 预留
    // ======================================================

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

    private boolean isLogin = false;


}
