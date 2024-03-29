package com.intelligent.reader.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
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
import android.widget.RelativeLayout;

import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.R;
import com.intelligent.reader.app.BookApplication;
import com.intelligent.reader.widget.topshadow.TopShadowWebView;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.ui.widget.LoadingPage;
import net.lzbook.kit.ui.widget.pulllist.SuperSwipeRefreshLayout;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.router.RouterConfig;
import net.lzbook.kit.utils.router.RouterUtil;
import net.lzbook.kit.utils.toast.ToastUtil;
import net.lzbook.kit.utils.webview.CustomWebClient;
import net.lzbook.kit.utils.webview.JSInterfaceHelper;

import java.lang.ref.WeakReference;

public class WebViewFragment extends Fragment implements View.OnClickListener {

    private static String TAG = WebViewFragment.class.getSimpleName();
    public String url = "";
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
    private ImageView img_head;
    private ImageView img_anim;

    private ImageView topShadowView;


    private RelativeLayout rl_recommend_head;
    private RelativeLayout rl_recommend_search;
    private RelativeLayout rl_head_ranking;
    private ImageView img_ranking_search;

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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        AppLog.e(TAG, "onCreateView");
        try {
            rootView = inflater.inflate(R.layout.webview_layout, container, false);
        } catch (InflateException e) {
            e.printStackTrace();
        }
        if (weakReference != null) {
            AppUtils.disableAccessibility(weakReference.get());
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
            rl_recommend_head = (RelativeLayout) rootView.findViewById(R.id.rl_recommend_head);
            rl_recommend_search = (RelativeLayout) rootView.findViewById(R.id.rl_recommend_search);
            rl_head_ranking = (RelativeLayout) rootView.findViewById(R.id.rl_head_ranking);
            img_ranking_search = (ImageView) rootView.findViewById(R.id.img_ranking_search);
            View title_layout = rootView.findViewById(R.id.title_layout);
            contentView = (TopShadowWebView) rootView.findViewById(R.id.web_content_view);
            contentView.setTopShadow(topShadowView);
            if (Build.VERSION.SDK_INT >= 11) {
                contentView.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            if (type.equals("recommend")) {
                rl_recommend_head.setVisibility(View.VISIBLE);
                rl_head_ranking.setVisibility(View.GONE);
                title_layout.setVisibility(View.VISIBLE);
            } else if (type.equals("rank")) {
                rl_recommend_head.setVisibility(View.GONE);
                rl_head_ranking.setVisibility(View.VISIBLE);
                title_layout.setVisibility(View.VISIBLE);
            } else {
                title_layout.setVisibility(View.GONE);
            }

            rl_recommend_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    RouterUtil.INSTANCE.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY);

                    StartLogClickUtil.upLoadEventLog(context,
                            StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.QG_TJY_SEARCH);
                }
            });

            img_ranking_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    RouterUtil.INSTANCE.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY);
                    StartLogClickUtil.upLoadEventLog(context,
                            StartLogClickUtil.TOP_PAGE, StartLogClickUtil.QG_BDY_SEARCH);
                }
            });
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
                contentView.getSettings().setMixedContentMode(
                        WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
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
            if (jsInterfaceHelper == null && contentView != null && context != null) {
                jsInterfaceHelper = new JSInterfaceHelper(context, contentView);
            }
            if (type.equals("rank")) {//榜单
                notifyWebLog();//通知 H5 打点
            }
            if (type.equals("recommend")) {//推荐
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
                    contentView.loadUrl(url);
                }
            });
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.content_head_search:
                RouterUtil.INSTANCE.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY);
                break;
//            case R.id.content_download_manage:
//                try {
//                    RouterUtil.INSTANCE.navigation(RouterConfig.DOWNLOAD_MANAGER_ACTIVITY);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppLog.e("webviewFrag", "exit");
        if (contentView != null) {
            contentView.clearCache(false); //清空缓存
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (contentLayout != null) {
                    contentLayout.removeView(contentView);
                }
                contentView.stopLoading();
                // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
                contentView.getSettings().setJavaScriptEnabled(false);
                contentView.clearHistory();
                contentView.removeAllViews();
                contentView.destroy();
            } else {
                contentView.stopLoading();
                // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
                contentView.getSettings().setJavaScriptEnabled(false);
                contentView.clearHistory();
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
        if (!TextUtils.isEmpty(url) && rootView != null) {
            swipeRefreshLayout = (SuperSwipeRefreshLayout) rootView.findViewById(
                    R.id.bookshelf_refresh_view);
            swipeRefreshLayout.setHeaderViewBackgroundColor(0x00000000);
            swipeRefreshLayout.setHeaderView(createHeaderView());
            swipeRefreshLayout.setTargetScrollWithLayout(true);
            swipeRefreshLayout.setOnPullRefreshListener(
                    new SuperSwipeRefreshLayout.OnPullRefreshListener() {

                        @Override
                        public void onRefresh() {
                            img_head.setVisibility(View.GONE);
                            img_anim.setVisibility(View.VISIBLE);
                            ((AnimationDrawable) img_anim.getDrawable()).start();
                            checkUpdate();
                        }

                        @Override
                        public void onPullDistance(int distance) {}

                        @Override
                        public void onPullEnable(boolean enable) {
                            img_anim.setVisibility(View.GONE);
                            img_head.setVisibility(View.VISIBLE);
                            img_head.setImageResource((enable ? R.drawable.refresh_head_pull_light_1
                                    : R.drawable.refresh_head_pull_light_0));
                        }
                    });
            if (type != null && type.equals("recommend")) {
                swipeRefreshLayout.setPullToRefreshEnabled(true);
            } else {
                swipeRefreshLayout.setPullToRefreshEnabled(false);
            }
        }
    }

    private View createHeaderView() {
        View headerView = LayoutInflater.from(swipeRefreshLayout.getContext()).inflate(
                R.layout.bookshelf_refresh_header, null);
        img_head = headerView.findViewById(R.id.img_head);
        img_anim = headerView.findViewById(R.id.img_anim);
        img_head.setVisibility(View.VISIBLE);
        img_anim.setVisibility(View.GONE);
        img_head.setImageResource(R.drawable.refresh_head_pull_light_0);
        return headerView;
    }

    private void checkUpdate() {

        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            swipeRefreshLayout.setRefreshing(false);
            ToastUtil.INSTANCE.showToastMessage("网络不给力！");
            return;
        }

        StartLogClickUtil.upLoadEventLog(requireContext(),StartLogClickUtil.RECOMMEND_PAGE,StartLogClickUtil.DROPDOWN);
        swipeRefreshLayout.onRefreshComplete();
        loadData("javascript:refreshNew()");
    }

    private void loadData(final String s) {
        if (!TextUtils.isEmpty(s) && contentView != null) {
            contentView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        AppLog.e(TAG, "call back jsMethod s: " + s);
                        contentView.loadUrl(s);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        weakReference.get().finish();
                    }
                }
            });
        }
    }
}