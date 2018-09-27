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
import android.util.DisplayMetrics;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.intelligent.reader.app.BookApplication;
import net.lzbook.kit.bean.PagerDesc;

import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.toast.ToastUtil;
import net.lzbook.kit.utils.webview.CustomWebClient;
import net.lzbook.kit.utils.webview.JSInterfaceHelper;
import net.lzbook.kit.ui.widget.LoadingPage;
import net.lzbook.kit.ui.widget.pulllist.SuperSwipeRefreshLayout;

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
    public PagerDesc mPagerDesc;
    private int h5Margin;

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
        }
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
        if(weakReference != null){
            AppUtils.disableAccessibility(weakReference.get());
        }
        initView();
//        initRefresh();
        return rootView;
    }

    @SuppressLint("JavascriptInterface")
    private void initView() {
        if (rootView != null) {
            contentLayout =  rootView.findViewById(R.id.web_content_layout);
            contentView =  rootView.findViewById(R.id.web_content_view);
            if (Build.VERSION.SDK_INT >= 11) {
                contentView.setLayerType(View.LAYER_TYPE_NONE, null);
            }
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

        addTouchListener();
    }


    public boolean isNeedInterceptSlide() {
        String packageName = AppUtils.getPackageName();
        if (this.url.contains("recommend")) {
            return true;
        }
        return false;
    }

    private void addTouchListener() {
        if (contentView != null && isNeedInterceptSlide()) {
            contentView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float y = event.getRawY();
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (contentView != null) {
                                int[] loction = new int[2];
                                contentView.getLocationOnScreen(loction);
                                h5Margin = loction[1];
                            }
//                            if (y > 100 && y < 800) {
//                                contentView.requestDisallowInterceptTouchEvent(true);
//                                isSupport = false;
//                            } else {
//                                contentView.requestDisallowInterceptTouchEvent(false);
//                                isSupport = true;
//                            }
                            if (null != mPagerDesc) {
                                float top = mPagerDesc.getTop();
                                float bottom = top + (mPagerDesc.getBottom() - mPagerDesc.getTop());
                                DisplayMetrics metric = getResources().getDisplayMetrics();
                                top = (float) (top * metric.density) + h5Margin;
                                bottom = (float) (bottom * metric.density) + h5Margin;
                                if (y > top && y < bottom) {
                                    contentView.requestDisallowInterceptTouchEvent(true);
                                } else {
                                    contentView.requestDisallowInterceptTouchEvent(false);
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });
        }
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

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.content_head_search:
//                Intent intent = new Intent(getActivity(), SearchBookActivity.class);
//                try {
//                    startActivity(intent);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case R.id.content_download_manage:
//                try {
//                    RouterUtil.INSTANCE.navigation(getActivity(),
//                            RouterConfig.DOWNLOAD_MANAGER_ACTIVITY);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//        }
//    }

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
            ToastUtil.INSTANCE.showToastMessage("网络不给力");
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
