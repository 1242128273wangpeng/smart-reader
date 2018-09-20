package com.intelligent.reader.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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

import net.lzbook.kit.utils.router.RouterConfig;
import net.lzbook.kit.utils.router.RouterUtil;
import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.R;
import com.intelligent.reader.app.BookApplication;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.utils.toast.CommonUtil;
import net.lzbook.kit.utils.webview.CustomWebClient;
import net.lzbook.kit.utils.webview.JSInterfaceHelper;
import net.lzbook.kit.widget.LoadingPage;
import net.lzbook.kit.widget.pulllist.SuperSwipeRefreshLayout;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.NetWorkUtils;

import java.lang.ref.WeakReference;

public class WebViewFragment extends Fragment {

    private static String TAG = WebViewFragment.class.getSimpleName();
    public String url = "";
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
    private RelativeLayout rl_head_recommend;
    private RelativeLayout rl_head_other;
    private TextView txt_title;
    private ImageView img_search;

    public static final String TYPE_RECOMM = "recommend";
    public static final String TYPE_RANK = "rank";
    public static final String TYPE_CATEGORY = "category";
    private String mTitle = null;

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
            this.mTitle = bundle.getString("type");
            AppLog.e(TAG, "url---->" + url);
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
        initRefresh();
        return rootView;
    }

    private void setTitle() {
        if (TYPE_RECOMM.equals(mTitle)){
            return;
        }

        if (rl_head_other != null) {
            rl_head_other.setVisibility(View.VISIBLE);
        }
        if (txt_title != null && mTitle != null) {
            if (TYPE_RANK.equals(mTitle)){
                txt_title.setText("榜单");
            } else if(TYPE_CATEGORY.equals(mTitle)){
                txt_title.setText("分类");
            }
        }
        if (rl_head_recommend != null) {
            rl_head_recommend.setVisibility(View.GONE);
        }
    }

    @SuppressLint("JavascriptInterface")
    private void initView() {
        if (rootView != null) {
            contentLayout = rootView.findViewById(R.id.web_content_layout);
            contentView = rootView.findViewById(R.id.web_content_view);
            rl_head_recommend = rootView.findViewById(R.id.rl_head_recommend);
            rl_head_other = rootView.findViewById(R.id.rl_head_other);
            txt_title = rootView.findViewById(R.id.txt_title);
            img_search = rootView.findViewById(R.id.img_search);
            if (Build.VERSION.SDK_INT >= 11) {
                contentView.setLayerType(View.LAYER_TYPE_NONE, null);
            }
            setTitle();
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

        rl_head_recommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RouterUtil.INSTANCE.navigation(requireActivity(),
                        RouterConfig.SEARCH_BOOK_ACTIVITY);
                StartLogClickUtil.upLoadEventLog(requireActivity(),
                        StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.QG_TJY_SEARCH);
            }
        });

        img_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtil.INSTANCE.navigation(requireActivity(),
                        RouterConfig.SEARCH_BOOK_ACTIVITY);
                if ("rank".equals(mTitle)) {
                    StartLogClickUtil.upLoadEventLog(requireActivity(),
                            StartLogClickUtil.TOP_PAGE, StartLogClickUtil.QG_BDY_SEARCH);
                } else if ("category".equals(mTitle)) {
                    StartLogClickUtil.upLoadEventLog(requireActivity(),
                            StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.QG_FL_SEARCH);

                }
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!TextUtils.isEmpty(url)) {
            loadWebData(url);
        }
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
    public void onDestroy() {
        super.onDestroy();
        if (contentView != null) {
            contentView.clearCache(true); //清空缓存
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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


    private SuperSwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar head_pb_view;
    private TextView head_text_view;
    private ImageView head_image_view;

    private void initRefresh() {

        // 免费全本小说书城 推荐页添加下拉刷新
        if (!TextUtils.isEmpty(url) && rootView != null) {
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        head_image_view.setRotation(enable ? 180 : 0);
                    }
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