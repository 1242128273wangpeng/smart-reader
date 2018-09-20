package com.intelligent.reader.fragment.scroll;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
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
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.SearchBookActivity;
import com.intelligent.reader.app.BookApplication;
import com.intelligent.reader.fragment.WebViewFragment;
import com.intelligent.reader.view.scroll.ScrollWebView;

import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.router.RouterConfig;
import net.lzbook.kit.utils.router.RouterUtil;
import net.lzbook.kit.utils.toast.CommonUtil;
import net.lzbook.kit.utils.webview.CustomWebClient;
import net.lzbook.kit.utils.webview.JSInterfaceHelper;
import net.lzbook.kit.widget.LoadingPage;
import net.lzbook.kit.widget.pulllist.SuperSwipeRefreshLayout;

import java.lang.ref.WeakReference;

public class ScrollWebFragment extends Fragment implements View.OnClickListener {

    private static String TAG = ScrollWebFragment.class.getSimpleName();
    public String url;
    private WeakReference<Activity> weakReference;
    private Context context;
    private View rootView;
    private FrameLayout contentLayout;
    private ScrollWebView contentView;
    private CustomWebClient customWebClient;
    private JSInterfaceHelper jsInterfaceHelper;
    private WebViewFragment.FragmentCallback fragmentCallback;
    private LoadingPage loadingpage;
    private Handler handler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.weakReference = new WeakReference<>(activity);
        this.fragmentCallback = (WebViewFragment.FragmentCallback) activity;
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
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.e(TAG, "onCreateView");
        try {
            rootView = inflater.inflate(R.layout.webview_scroll_layout, container, false);
        } catch (InflateException e) {
            e.printStackTrace();
        }
        if (weakReference != null) {
            AppUtils.disableAccessibility(weakReference.get());
        }

//        initRefresh();
        return rootView;
    }

    @SuppressLint("JavascriptInterface")
    private void initView() {
        if (rootView != null) {
            contentLayout = getView().findViewById(R.id.fl_content_layout);
            contentView = getView().findViewById(R.id.web_content_view);
            if (Build.VERSION.SDK_INT >= 11) {
                contentView.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        }

        if (weakReference != null) {
            loadingpage = new LoadingPage(weakReference.get(), contentLayout);
//            //父布局为scroll时，loading视图高度为包裹内容，这里手动给它赋值，高度按照推荐页内容调整
////            底部导航栏，顶部搜索栏、tab栏等高度和margin
//            loadingpage.getLayoutParams().height =
//                    getContext().getResources().getDisplayMetrics()
//                            .heightPixels - AppUtils.dip2px(context, 36f + 34f + 50f + 13f);

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
            contentView.addJavascriptInterface(new JsPositionInterface(), "J_banner");
        }


        if (fragmentCallback != null && jsInterfaceHelper != null) {
            fragmentCallback.webJsCallback(jsInterfaceHelper);
        }


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
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


    private SuperSwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar head_pb_view;
    private TextView head_text_view;
    private ImageView head_image_view;

//    private void initRefresh() {
//
//        // 免费全本小说书城 推荐页添加下拉刷新
//        if (("cc.kdqbxs.reader".equals(AppUtils.getPackageName()) || "cc.quanbennovel".equals(AppUtils.getPackageName())) && !TextUtils.isEmpty(url) && rootView != null) {
//            swipeRefreshLayout = (SuperSwipeRefreshLayout) rootView.findViewById(R.id.bookshelf_refresh_view);
//            swipeRefreshLayout.setHeaderViewBackgroundColor(0x00000000);
//            swipeRefreshLayout.setHeaderView(createHeaderView());
//            swipeRefreshLayout.setTargetScrollWithLayout(true);
//            swipeRefreshLayout.setOnPullRefreshListener(new SuperSwipeRefreshLayout.OnPullRefreshListener() {
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


    /**
     * 获取web中banner的位置js回调
     */
    public class JsPositionInterface {

        @JavascriptInterface
        public void getH5ViewPagerInfo(String x, String y, String width, String height) {
            AppLog.e("jsPosition" + x + " " + y + " " +
                    width + " " + height + " " + contentView.getScaleX() + "  " + contentView.getScaleY());
            try {
                float bWidht = Float.parseFloat(width);
                float bHeight = Float.parseFloat(height);
                float scale = contentView.getResources().getDisplayMetrics().widthPixels / (bWidht + 1);


                contentView.setBannerRect(new RectF(
                        Float.parseFloat(x)
                        , Float.parseFloat(y)
                        , bWidht * scale,
                        bHeight * scale));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
