package com.intelligent.reader.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.DownloadManagerActivity;
import com.intelligent.reader.activity.SearchBookActivity;
import com.intelligent.reader.app.BookApplication;
import com.intelligent.reader.widget.topshadow.TopShadowWebView;

import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.CustomWebClient;
import net.lzbook.kit.utils.ExtensionsKt;
import net.lzbook.kit.utils.JSInterfaceHelper;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.ToastUtils;

import java.lang.ref.WeakReference;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class WebViewFragment extends Fragment implements View.OnClickListener {

    private static String TAG = WebViewFragment.class.getSimpleName();
    public String url;
    private String type;
    private WeakReference<Activity> weakReference;
    private Context context;
    private View rootView;
    private RelativeLayout contentLayout;
    private TopShadowWebView contentView;
    private CustomWebClient customWebClient;
    private JSInterfaceHelper jsInterfaceHelper;
    private FragmentCallback fragmentCallback;
    private LoadingPage loadingpage;
    private Handler handler;

    private boolean isPrepared;
    private boolean isVisible;

    private boolean isFirstVisible = true;

    private SuperSwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar head_pb_view;
    private TextView head_text_view;
    private ImageView head_image_view;

    private ImageView topShadowView;

    private boolean hasRefreshHead = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.weakReference = new WeakReference<>(activity);
        this.fragmentCallback = (FragmentCallback) activity;
        this.context = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        AppLog.e(TAG, "----------->start");
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            this.url = bundle.getString("url");
            AppLog.e(TAG, "url---->" + url);
            type = bundle.getString("type");
        }

        String packageName = AppUtils.getPackageName();
        hasRefreshHead = "cc.kdqbxs.reader".equals(packageName)
                || "cn.txtqbmfyd.reader".equals(packageName);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.e(TAG, "onCreateView");
        try {
            rootView = inflater.inflate(R.layout.webview_layout, container, false);
        } catch (InflateException e) {
            e.printStackTrace();
        }
        initView();
        initRefresh();
        return rootView;
    }

    @SuppressLint("JavascriptInterface")
    private void initView() {
        if (rootView != null) {
            contentLayout = (RelativeLayout) rootView.findViewById(R.id.web_content_layout);
            topShadowView = (ImageView) rootView.findViewById(R.id.img_head_shadow);
            View title_layout = rootView.findViewById(R.id.title_layout);
            contentView = (TopShadowWebView) rootView.findViewById(R.id.web_content_view);
            contentView.setTopShadow(topShadowView);
            if (Build.VERSION.SDK_INT >= 11) {
                contentView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }

//            if (!TextUtils.isEmpty(getArguments().getString("type")) && getArguments().getString("type").equals("category")) {
            title_layout.setVisibility(View.GONE);
//            } else {
//                title_layout.setVisibility(View.VISIBLE);
//            }
        }

        if (weakReference != null) {
            loadingpage = new LoadingPage(weakReference.get(), contentLayout);
        }

        if (contentView != null && context != null) {
            customWebClient = new CustomWebClient(context, contentView);
        }

        if (contentView != null && customWebClient != null) {
            customWebClient.setWebSettings();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                contentView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            contentView.setWebViewClient(customWebClient);
        }

        if (contentView != null && context != null) {
            jsInterfaceHelper = new JSInterfaceHelper(context, contentView);
        }

        if (jsInterfaceHelper != null && contentView != null) {
            contentView.addJavascriptInterface(jsInterfaceHelper, "J_search");
        }

        if (fragmentCallback != null && jsInterfaceHelper != null) {
            fragmentCallback.webJsCallback(jsInterfaceHelper);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isPrepared = true;
        if (type != null && type.equals("category_female")) {//女频
            lazyLoad();
        } else {
            if (!TextUtils.isEmpty(url)) {
                loadWebData(url);
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }

    protected void onVisible() {
        if (type != null) {
            if (type.equals("rank")) {//榜单
                jsInterfaceHelper.setRankingWebVisible();
                notifyWebLog();//通知 H5 打点
            }
            if (type.equals("recommend")) {//推荐
                jsInterfaceHelper.setRecommendVisible();
                notifyWebLog();
            }
            if (type.equals("category_female")) {//分类-女频
                lazyLoad();
            }
        }
    }

    protected void onInvisible() {
    }

    private void notifyWebLog() {
        if (!isVisible || !isPrepared) {
            return;
        }
        contentView.loadUrl("javascript:startEventFunc()");
    }

    protected void lazyLoad() {
        if (!isVisible || !isPrepared || !isFirstVisible) {
            return;
        }
        if (!TextUtils.isEmpty(url)) {
            loadWebData(url);
        }
        isFirstVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void loadWebData(String url) {
        AppLog.e(TAG, "loadWebData url: " + url);
        if (fragmentCallback != null) {
            url = fragmentCallback.startLoad(contentView, url);
        }
        AppLog.e(TAG, "loadWebData url: " + url);
        startLoading(handler, url);
        webViewCallback();
    }

    private void startLoading(Handler handler, final String url) {
        if (contentView == null) {
            return;
        }

        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    loadingData(url);
                }
            });
        } else {
            loadingData(url);
        }
    }

    private void loadingData(String url) {
        if (customWebClient != null) {
            customWebClient.doClear();
        }
        if (!TextUtils.isEmpty(url) && contentView != null) {
            try {
                AppLog.e(TAG, "WebViewFragment LoadingData ==> " + url);
                contentView.loadUrl(url);
            } catch (NullPointerException e) {
                e.printStackTrace();
                weakReference.get().finish();
            }
        }
    }

    private void webViewCallback() {
        if (rootView == null) {
            return;
        }

        if (customWebClient != null) {
            customWebClient.setStartedAction(new CustomWebClient.onStartedCallback() {
                @Override
                public void onLoadStarted(String url) {
                    AppLog.e(TAG, "onLoadStarted: " + url);
                }
            });

            customWebClient.setErrorAction(new CustomWebClient.onErrorCallback() {
                @Override
                public void onErrorReceived() {
                    AppLog.e(TAG, "onErrorReceived");
                    if (loadingpage != null) {
                        loadingpage.onErrorVisable();
                    }
                }
            });

            customWebClient.setFinishedAction(new CustomWebClient.onFinishedCallback() {
                @Override
                public void onLoadFinished() {
                    AppLog.e(TAG, "onLoadFinished");
                    if (loadingpage != null) {
                        loadingpage.onSuccessGone();
                    }
                }
            });
        }

        if (loadingpage != null) {
            loadingpage.setReloadAction(new LoadingPage.reloadCallback() {
                @Override
                public void doReload() {
                    AppLog.e(TAG, "doReload");
                    if (customWebClient != null) {
                        customWebClient.doClear();
                    }
                    contentView.reload();
                }
            });
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.content_head_search:
                Intent intent = new Intent(getActivity(), SearchBookActivity.class);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.content_download_manage:
                Intent intent_download = new Intent(getActivity(), DownloadManagerActivity.class);
                try {
                    startActivity(intent_download);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (contentView != null) {
            contentView.clearCache(true); //清空缓存
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                if (contentLayout != null) {
                    contentLayout.removeView(contentView);

                }
                contentView.stopLoading();
                contentView.removeAllViews();
                contentView.destroy();
            } else {
                contentView.stopLoading();
                contentView.removeAllViews();
                contentView.destroy();
                if (contentLayout != null) {
                    contentLayout.removeView(contentView);
                }
            }
            contentView = null;
        }
        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this);
        }

    }

    public interface FragmentCallback {
        void webJsCallback(JSInterfaceHelper jsInterfaceHelper);

        String startLoad(WebView webView, String url);

    }

    private void initRefresh() {

        // 免费全本小说书城 推荐页添加下拉刷新
        if (hasRefreshHead && !TextUtils.isEmpty(url) && rootView != null) {
            swipeRefreshLayout = (SuperSwipeRefreshLayout) rootView.findViewById(R.id.bookshelf_refresh_view);
            swipeRefreshLayout.setHeaderViewBackgroundColor(0x00000000);
            swipeRefreshLayout.setHeaderView(createHeaderView());
            swipeRefreshLayout.setTargetScrollWithLayout(true);
            swipeRefreshLayout.setOnPullRefreshListener(new SuperSwipeRefreshLayout.OnPullRefreshListener() {

                @Override
                public void onRefresh() {
                    head_text_view.setText("正在刷新");
                    head_image_view.setVisibility(View.GONE);
                    head_pb_view.setVisibility(View.VISIBLE);
                    checkUpdate();
                }

                @Override
                public void onPullDistance(int distance) {
                    // pull distance
                }

                @Override
                public void onPullEnable(boolean enable) {
                    head_pb_view.setVisibility(View.GONE);
                    head_text_view.setText(enable ? "松开刷新" : "下拉刷新");
                    head_image_view.setVisibility(View.VISIBLE);
                    head_image_view.setRotation(enable ? 180 : 0);
                }
            });
            if (url.contains("recommend")) {
                swipeRefreshLayout.setPullToRefreshEnabled(true);
            } else {
                swipeRefreshLayout.setPullToRefreshEnabled(false);
            }
        }
    }

    private View createHeaderView() {
        View headerView = LayoutInflater.from(swipeRefreshLayout.getContext())
                .inflate(R.layout.layout_head, null);
        head_pb_view = (ProgressBar) headerView.findViewById(R.id.head_pb_view);
        head_text_view = (TextView) headerView.findViewById(R.id.head_text_view);
        head_text_view.setText("下拉刷新");
        head_image_view = (ImageView) headerView.findViewById(R.id.head_image_view);
        head_image_view.setVisibility(View.VISIBLE);
        head_image_view.setImageResource(R.drawable.pulltorefresh_down_arrow);
        head_pb_view.setVisibility(View.GONE);
        return headerView;
    }

    private void checkUpdate() {

        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            swipeRefreshLayout.setRefreshing(false);
            ToastUtils.showToastNoRepeat("网络不给力");
            return;
        }

        swipeRefreshLayout.onRefreshComplete();
        ExtensionsKt.uiThread(this, new Function1<Object, Unit>() {
            @Override
            public Unit invoke(Object o) {
                AppLog.e(TAG, "call back jsMethod s: javascript:refreshNew()");
                contentView.loadUrl("javascript:refreshNew()");
                return null;
            }
        });
    }
}
