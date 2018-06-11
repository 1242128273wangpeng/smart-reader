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

import com.dingyue.contract.router.RouterConfig;
import com.dingyue.contract.router.RouterUtil;
import com.dingyue.contract.util.CommonUtil;
import com.dingyue.contract.util.SharedPreUtil;
import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.SearchBookActivity;
import com.intelligent.reader.activity.SettingActivity;
import com.intelligent.reader.app.BookApplication;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.view.FirstUsePointView;
import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.CustomWebClient;
import net.lzbook.kit.utils.JSInterfaceHelper;
import net.lzbook.kit.utils.NetWorkUtils;

import java.lang.ref.WeakReference;

public class WebViewFragment extends Fragment implements View.OnClickListener {

    private static String TAG = WebViewFragment.class.getSimpleName();
    public String url;
    private String type;
    private WeakReference<Activity> weakReference;
    private Context context;
    private View rootView;
    private RelativeLayout contentLayout;
    private WebView contentView;
    private CustomWebClient customWebClient;
    private JSInterfaceHelper jsInterfaceHelper;
    private FragmentCallback fragmentCallback;
    private LoadingPage loadingpage;
    private Handler handler;
    private ImageView img_head_setting;
    private boolean isPrepared;
    private boolean isVisible;
    private boolean isFirstVisible = true;
    private FirstUsePointView fupv_head_setting;
    private TextView txt_head_title;
    private ImageView img_head_search;
    private ImageView img_head_download_manage;
    private SharedPreUtil sharedPreUtil;

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
        sharedPreUtil = new SharedPreUtil(SharedPreUtil.Companion.getSHARE_DEFAULT());
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
        initView();
//        initRefresh();
        return rootView;
    }

    public void setTitle(String title) {
        txt_head_title.setText(title);
    }

    @SuppressLint("JavascriptInterface")
    private void initView() {
        if (rootView != null) {
            contentLayout = (RelativeLayout) rootView.findViewById(R.id.web_content_layout);
            img_head_setting = (ImageView) rootView.findViewById(R.id.img_head_setting);
            fupv_head_setting = (FirstUsePointView) rootView.findViewById(R.id.fupv_head_setting);
            txt_head_title = (TextView) rootView.findViewById(R.id.txt_head_title);
            img_head_search = (ImageView) rootView.findViewById(R.id.img_head_search);
            img_head_download_manage = (ImageView) rootView.findViewById(
                    R.id.img_head_download_manage);
            contentView = rootView.findViewById(R.id.web_content_view);
//            View title_layout = rootView.findViewById(R.id.txt_head_title);
            if (Build.VERSION.SDK_INT >= 11) {
                contentView.setLayerType(View.LAYER_TYPE_NONE, null);
            }


//            if (type.equals("recommend")) {
//                txt_head_title.setText(R.string.recommend);
//            } else if (type.equals("rank")) {
//                txt_head_title.setText(R.string.rank);
//            } else {
//                txt_head_title.setText(R.string.category);
//            }

            img_head_setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fupv_head_setting.setVisibility(View.GONE);
                    startActivity(new Intent(WebViewFragment.this.context, SettingActivity.class));
                }
            });
            img_head_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String titleType = sharedPreUtil.getString("HOME_FINDBOOK_SEARCH");
                    String logType = StartLogClickUtil.MAIN_PAGE;
                    if (!TextUtils.isEmpty(titleType)) {
                        if (titleType.equals("recommend")) {
                            logType = StartLogClickUtil.RECOMMEND_PAGE;
                        } else if (titleType.equals("top")) {
                            logType = StartLogClickUtil.TOP_PAGE;
                        } else if (titleType.equals("class")) {
                            logType = StartLogClickUtil.CLASS_PAGE;
                        }
                    }

                    StartLogClickUtil.upLoadEventLog(context,
                            logType, StartLogClickUtil.SEARCH);

                    startActivity(
                            new Intent(WebViewFragment.this.context, SearchBookActivity.class));
                }
            });

            img_head_download_manage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RouterUtil.INSTANCE.navigation(getActivity(),
                            RouterConfig.DOWNLOAD_MANAGER_ACTIVITY);
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.MAIN_PAGE,
                            StartLogClickUtil.CACHEMANAGE);
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
                try {
                    RouterUtil.INSTANCE.navigation(getActivity(),
                            RouterConfig.DOWNLOAD_MANAGER_ACTIVITY);
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


    private SuperSwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar head_pb_view;
    private TextView head_text_view;
    private ImageView head_image_view;
//
//    private void initRefresh() {
//
//        // 免费全本小说书城 推荐页添加下拉刷新
//        if ("cc.kdqbxs.reader".equals(AppUtils.getPackageName()) && !TextUtils.isEmpty(url) &&
// rootView != null) {
//            swipeRefreshLayout = (SuperSwipeRefreshLayout) rootView.findViewById(R.id
// .bookshelf_refresh_view);
//            swipeRefreshLayout.setHeaderViewBackgroundColor(0x00000000);
//            swipeRefreshLayout.setHeaderView(createHeaderView());
//            swipeRefreshLayout.setTargetScrollWithLayout(true);
//            swipeRefreshLayout.setOnPullRefreshListener(new SuperSwipeRefreshLayout
// .OnPullRefreshListener() {
//
//                @Override
//                public void onRefresh() {
//                    head_text_view.setText("正在刷新");
//                    head_image_view.setVisibility(View.GONE);
//                    head_pb_view.setVisibility(View.VISIBLE);
//                    checkUpdate();
//                }
//
//                @Override
//                public void onPullDistance(int distance) {
//                    // pull distance
//                }
//
//                @Override
//                public void onPullEnable(boolean enable) {
//                    head_pb_view.setVisibility(View.GONE);
//                    head_text_view.setText(enable ? "松开刷新" : "下拉刷新");
//                    head_image_view.setVisibility(View.VISIBLE);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                        head_image_view.setRotation(enable ? 180 : 0);
//                    }
//                }
//            });
//            if (url.contains("recommend")) {
//                swipeRefreshLayout.setPullToRefreshEnabled(true);
//            } else {
//                swipeRefreshLayout.setPullToRefreshEnabled(false);
//            }
//        }
//    }
//
//    private View createHeaderView() {
//        View headerView = LayoutInflater.from(swipeRefreshLayout.getContext())
//                .inflate(R.layout.layout_head, null);
//        head_pb_view = (ProgressBar) headerView.findViewById(R.id.head_pb_view);
//        head_text_view = (TextView) headerView.findViewById(R.id.head_text_view);
//        head_text_view.setText("下拉刷新");
//        head_image_view = (ImageView) headerView.findViewById(R.id.head_image_view);
//        head_image_view.setVisibility(View.VISIBLE);
//        head_image_view.setImageResource(R.drawable.pulltorefresh_down_arrow);
//        head_pb_view.setVisibility(View.GONE);
//        return headerView;
//    }

    private void checkUpdate() {
        if (swipeRefreshLayout == null) {
            return;
        }

        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            swipeRefreshLayout.setRefreshing(false);
            CommonUtil.showToastMessage("网络不给力");
            return;
        }

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
