package com.intelligent.reader.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.SettingActivity;
import com.intelligent.reader.app.BookApplication;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.router.RouterConfig;
import net.lzbook.kit.utils.router.RouterUtil;
import net.lzbook.kit.utils.toast.ToastUtil;
import net.lzbook.kit.utils.webview.CustomWebClient;
import net.lzbook.kit.utils.webview.JSInterfaceHelper;
import net.lzbook.kit.ui.widget.FirstUsePointView;
import net.lzbook.kit.ui.widget.LoadingPage;
import net.lzbook.kit.ui.widget.pulllist.SuperSwipeRefreshLayout;

import java.util.HashMap;

public class WebViewFragment extends Fragment implements View.OnClickListener {

    public String url = "";
    private String type;

    private View rootView;
    private RelativeLayout rl_web_content;
    private CustomWebView web_content_view;
    private CustomWebClient customWebClient;
    private JSInterfaceHelper jsInterfaceHelper;
    private FragmentCallback fragmentCallback;
    private LoadingPage loadingpage;
    private Handler handler;
    private boolean isPrepared;
    private boolean isVisible;
    private boolean isFirstVisible = true;
    private FirstUsePointView fp_header_point;
    private TextView txt_header_title;
    private int bottomType;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.fragmentCallback = (FragmentCallback) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        AppLog.e(TAG, "----------->start");
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            this.url = bundle.getString("url");
            this.type = bundle.getString("type");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.webview_layout, container, false);

        AppUtils.disableAccessibility(requireActivity());

        initView();

        return rootView;
    }

    public void setTitle(String title, int logBottomType) {
        if (txt_header_title != null) {
            txt_header_title.setText(title);
        }
        bottomType = logBottomType;
    }

    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    private void initView() {
        if (rootView != null) {
            View rl_home_header = rootView.findViewById(R.id.rl_home_header);

            rl_web_content = rootView.findViewById(R.id.rl_web_content);
            fp_header_point = rootView.findViewById(R.id.fp_header_point);
            txt_header_title = rootView.findViewById(R.id.txt_header_title);
            web_content_view = rootView.findViewById(R.id.web_content_view);

            ImageView img_header_setting = rootView.findViewById(R.id.img_header_setting);
            ImageView img_header_search = rootView.findViewById(R.id.img_header_search);
            ImageView img_header_cache = rootView.findViewById(R.id.img_header_cache);

            web_content_view.setLayerType(View.LAYER_TYPE_NONE, null);

            if ("recommend".equals(type) || "recommendMan".equals(type) || "recommendWoman".equals(
                    type) || "recommendFinish".equals(type)) {
                rl_home_header.setVisibility(View.GONE);
            } else {
                rl_home_header.setVisibility(View.VISIBLE);
            }

            img_header_setting.setOnClickListener(view -> {
                fp_header_point.setVisibility(View.GONE);
                HashMap<String, String> parameter = new HashMap<>();
                if ("rank".equals(type)) {
                    parameter.put("pk", "榜单");
                } else if ("category".equals(type)) {
                    parameter.put("pk", "分类");
                }
                StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                        StartLogClickUtil.PAGE_HOME, StartLogClickUtil.ACTION_HOME_PERSONAL, parameter);

                startActivity(new Intent(requireActivity(), SettingActivity.class));
            });
            img_header_search.setOnClickListener(v -> {

                RouterUtil.INSTANCE.navigation(requireActivity(),
                        RouterConfig.SEARCH_BOOK_ACTIVITY);
                if (bottomType == 2) {
                    StartLogClickUtil.upLoadEventLog(requireActivity(),
                            StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.QG_TJY_SEARCH);
                } else if (bottomType == 3) {
                    StartLogClickUtil.upLoadEventLog(requireActivity(), StartLogClickUtil.TOP_PAGE,
                            StartLogClickUtil.QG_BDY_SEARCH);
                } else if (bottomType == 4) {
                    StartLogClickUtil.upLoadEventLog(requireActivity(),
                            StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.QG_FL_SEARCH);
                } else {
                    StartLogClickUtil.upLoadEventLog(requireActivity(), StartLogClickUtil.MAIN_PAGE,
                            StartLogClickUtil.SEARCH);
                }
            });

            img_header_cache.setOnClickListener(v -> {
                RouterUtil.INSTANCE.navigation(requireActivity(),
                        RouterConfig.DOWNLOAD_MANAGER_ACTIVITY);

                HashMap<String, String> parameter = new HashMap<>();
                if ("rank".equals(type)) {
                    parameter.put("pk", "榜单");
                } else if ("category".equals(type)) {
                    parameter.put("pk", "分类");
                }
                StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                        StartLogClickUtil.PAGE_HOME, StartLogClickUtil.CACHEMANAGE, parameter);
            });
        }

        loadingpage = new LoadingPage(requireActivity(), rl_web_content);

        if (web_content_view != null) {
            customWebClient = new CustomWebClient(requireContext(), web_content_view);
        }

        if (web_content_view != null && customWebClient != null) {
            customWebClient.setWebSettings();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                web_content_view.getSettings().setMixedContentMode(
                        WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            web_content_view.setWebViewClient(customWebClient);
        }

        if (web_content_view != null) {
            jsInterfaceHelper = new JSInterfaceHelper(requireContext(), web_content_view);
        }

        if (jsInterfaceHelper != null && web_content_view != null) {
            web_content_view.addJavascriptInterface(jsInterfaceHelper, "J_search");
            web_content_view.addJavascriptInterface(new JsPositionInterface(), "J_position");
        }

        if (fragmentCallback != null && jsInterfaceHelper != null) {
            fragmentCallback.webJsCallback(jsInterfaceHelper);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isPrepared = true;
        if (type != null && (type.equals("recommendMan") || type.equals("recommendWoman")
                || type.equals("recommendFinish"))) {
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
            if (jsInterfaceHelper == null && web_content_view != null) {
                jsInterfaceHelper = new JSInterfaceHelper(requireContext(), web_content_view);
            }
            if (type.equals("rank")) {//榜单
                notifyWebLog();//通知 H5 打点
            }
            if (type.equals("recommend")) {//推荐
                notifyWebLog();
            }
            if (type.equals("recommendMan") || type.equals("recommendWoman")
                    || type.equals("recommendFinish")) {//分类-女频
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
        if (fragmentCallback != null) {
            url = fragmentCallback.startLoad(web_content_view, url);
        }
        startLoading(handler, url);
        webViewCallback();
    }

    private void startLoading(Handler handler, final String url) {
        if (web_content_view == null) {
            return;
        }

        if (handler != null) {
            handler.post(() -> loadingData(url));
        } else {
            loadingData(url);
        }
    }

    private void loadingData(String url) {
        if (customWebClient != null) {
            customWebClient.doClear();
        }
        if (!TextUtils.isEmpty(url) && web_content_view != null) {
            try {
                web_content_view.loadUrl(url);
            } catch (NullPointerException exception) {
                exception.printStackTrace();
                requireActivity().finish();
            }
        }
    }

    private void webViewCallback() {
        if (rootView == null) {
            return;
        }

        if (customWebClient != null) {
            customWebClient.setStartedAction(url -> {
                Logger.e("开始加载WebView: " + url);
            });

            customWebClient.setErrorAction(() -> {
                if (loadingpage != null) {
                    loadingpage.onErrorVisable();
                }
            });

            customWebClient.setFinishedAction(() -> {
                if (loadingpage != null) {
                    loadingpage.onSuccessGone();
                }
            });
        }

        if (loadingpage != null) {
            loadingpage.setReloadAction(() -> {
                if (customWebClient != null) {
                    customWebClient.doClear();
                }
                web_content_view.reload();
            });
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.content_head_search:
                RouterUtil.INSTANCE.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY);
                break;
            case R.id.content_download_manage:
                try {
                    RouterUtil.INSTANCE.navigation(requireActivity(),
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
        if (web_content_view != null) {
            web_content_view.clearCache(true);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                if (rl_web_content != null) {
                    rl_web_content.removeView(web_content_view);
                }
                web_content_view.stopLoading();
                web_content_view.getSettings().setJavaScriptEnabled(false);
                web_content_view.clearHistory();
                web_content_view.removeAllViews();
                web_content_view.destroy();
            } else {
                web_content_view.stopLoading();
                web_content_view.getSettings().setJavaScriptEnabled(false);
                web_content_view.clearHistory();
                web_content_view.removeAllViews();
                web_content_view.destroy();
                if (rl_web_content != null) {
                    rl_web_content.removeView(web_content_view);
                }
            }
            web_content_view = null;
        }
        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this);
        }
    }

    public interface FragmentCallback {
        void webJsCallback(JSInterfaceHelper jsInterfaceHelper);

        String startLoad(WebView webView, String url);
    }

    /**
     * 获取web中banner的位置js回调
     */
    public class JsPositionInterface {

        @JavascriptInterface
        public void getH5ViewPagerInfo(String x, String y, String width, String height) {
            try {
                float viewWidth = Float.parseFloat(width);
                float viewHeight = Float.parseFloat(height);
                float scale = web_content_view.getResources().getDisplayMetrics().widthPixels / (viewWidth);

                web_content_view.insertProhibitSlideArea(new RectF(
                        Float.parseFloat(x) * scale
                        , Float.parseFloat(y) * scale
                        , (Float.parseFloat(x) + viewWidth) * scale,
                        (Float.parseFloat(y) + viewHeight) * scale));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}