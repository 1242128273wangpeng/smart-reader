package net.lzbook.kit.utils;

import net.lzbook.kit.app.BaseBookApplication;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.androidquery.util.Common;
import com.dingyue.contract.util.CommonUtil;

public class CustomWebClient extends WebViewClient {
    private final static String TAG = "CustomWebClient";
    public WebSettings webSetting;
    public int ERROR_CODE = 0;
    public int pageStartCount = 0;
    Context context;
    WebView webview;
    onErrorCallback onError;
    onStartedCallback onLoad;
    onFinishedCallback onFinished;
    onOverrideCallback override;
    int pageFinishCount = 0;

    public CustomWebClient(Context context, WebView webview) {
        super();
        this.context = context;
        this.webview = webview;
    }

    /**
     * return true , application handle url itself, do not use current WebView
     * return false,current WebView handle url, defult: false
     */

    @Override
    public boolean shouldOverrideUrlLoading(final WebView view, String url) {
        // 如下方案可在非微信内部WebView的H5页面中调出微信支付
        if(url.startsWith("weixin://wap/pay?")) {
            try {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
                return true;
            } catch (Exception exception) {
                if (exception instanceof ActivityNotFoundException) {
                    CommonUtil.showToastMessage("未找到微信客户端，请先安装微信！");
                }
                exception.printStackTrace();
                return true;
            }
        }

        if (url.startsWith("alipays://platformapi/startApp?")) {
            try {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
                return true;
            } catch (Exception exception) {
                if (exception instanceof ActivityNotFoundException) {
                    CommonUtil.showToastMessage("未找到支付宝客户端，请先安装支付宝！");
                }
                exception.printStackTrace();
                return  super.shouldOverrideUrlLoading(view, url);
            }
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        pageStartCount++;
        AppLog.d(TAG, "pageStartCount  " + pageStartCount + " ERROR_CODE : " + ERROR_CODE + " onError : " + onError);
        if (onLoad != null && pageStartCount == 1) {
            onLoad.onLoadStarted(url);// FIXME 保证回调只执行一次
        }
        if (webview != null && pageStartCount == 1) {
            webview.setVisibility(View.GONE);
        } else if (ERROR_CODE != 0 && onError != null) {
            if (webview != null) {
                webview.stopLoading();
                webview.clearView();
                webview.setVisibility(View.GONE);// 出错时不显示Webview的错误页面，由app处理
            }
            onError.onErrorReceived();
        }
        AppLog.e(TAG, "onPageStarted view " + " ERROR_CODE : " + ERROR_CODE + " onError : " + onError + view + " webview " + webview);//初始化webview与加载webview要一致
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        pageFinishCount++;
        AppLog.d(TAG, "onPageFinished ERROR_CODE " + ERROR_CODE + onFinished);
        if (ERROR_CODE == 0 && onFinished != null) {// FIXME
            onFinished.onLoadFinished();
            if (webview != null) {

                webview.setVisibility(View.VISIBLE);
                // FIXME 网页显示之前，空白一段时间
            }
            if (webSetting != null) {
                webSetting.setBlockNetworkImage(false);// 通过图片的延迟载入，让网页能更快地显示
            }
        } else if (ERROR_CODE != 0 && onError != null) {
            if (webview != null) {
                webview.clearView();
                webview.stopLoading();
                webview.setVisibility(View.GONE);// 出错时不显示Webview的错误页面，由app处理
            }
            onError.onErrorReceived();
        }
        AppLog.d(TAG, "onPageFinished view" + view + " webview " + webview + " ERROR_CODE : " + ERROR_CODE + " onError : " + onError);
        super.onPageFinished(view, url);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        ERROR_CODE = errorCode;// FIXME 标识是否出错
        AppLog.e(TAG, "error[" + errorCode + " --- " + failingUrl + "] " + description);
        if (webview != null) {
            webview.clearView();
            webview.stopLoading();
        }
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        super.onReceivedHttpAuthRequest(view, handler, host, realm);
    }

    /**
     * 处理https请求
     */
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }

    // =================================================
    // activity 回调
    // ============================================
    public void doClear() {
        if (ERROR_CODE != 0) {// 解决出现过error情况后重新加载ERROR_CODE 全局变量不变
            ERROR_CODE = 0;
        }
        AppLog.d(TAG, "doClear ERROR_CODE" + ERROR_CODE);
    }

    public void setErrorAction(onErrorCallback onError) {
        this.onError = onError;
    }

    public void setStartedAction(onStartedCallback onStarted) {
        this.onLoad = onStarted;
    }

    public void setFinishedAction(onFinishedCallback onFinished) {
        this.onFinished = onFinished;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void setWebSettings() {
        // webview.requestFocus();
        String appCachePath = null;
        String dbPath = null;
        if (webview != null && webSetting == null) {
            webview.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
            webSetting = webview.getSettings();
        }

        if (context == null) {
            context = BaseBookApplication.getGlobalContext();
        }
        if (context != null && context.getCacheDir() != null && context.getDir("databases", 0) != null) {

            try {
                appCachePath = context.getCacheDir().getAbsolutePath();
                dbPath = context.getDir("databases", 0).getPath();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            AppLog.d(TAG, "appCachePath " + appCachePath);
            AppLog.d(TAG, "dbPath " + dbPath);
        }
        if (webSetting != null && appCachePath != null)
            webSetting.setAppCachePath(appCachePath);
        if (webSetting != null && dbPath != null) {
            webSetting.setDatabasePath(dbPath);// TODO
        }
        if (webSetting != null) {

            if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
                webSetting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            } else {
                webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
            }
            // HTML5 API flags
            webSetting.setAppCacheEnabled(true);
            webSetting.setDatabaseEnabled(true);
            webSetting.setDomStorageEnabled(true);

            // HTML5 configuration parametersettings.

            webSetting.setAllowFileAccess(true);
            webSetting.setAppCacheMaxSize(1024 * 1024 * 8);// TODO

            // WebView inside Browser doesn't want initial focus to be set.
            webSetting.setNeedInitialFocus(false);
            // Browser supports multiple windows
            webSetting.setSupportMultipleWindows(true);

            try {
                webSetting.setJavaScriptEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            webSetting.setJavaScriptCanOpenWindowsAutomatically(false);
            webSetting.setLoadsImagesAutomatically(true);
            webSetting.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
            webSetting.setRenderPriority(RenderPriority.HIGH);// 提高渲染的优先级

            webSetting.setBlockNetworkImage(false); // 图片下载阻塞
            webSetting.setUseWideViewPort(true);//FIXME
        }

    }

    public interface onErrorCallback {
        public void onErrorReceived();
    }

    public interface onStartedCallback {
        public void onLoadStarted(String url);
    }

    public interface onFinishedCallback {
        public void onLoadFinished();
    }

    public interface onOverrideCallback {
        public boolean onOverride(WebView view, String url);
    }
}
