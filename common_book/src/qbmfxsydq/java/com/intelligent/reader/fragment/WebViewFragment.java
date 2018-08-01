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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dingyue.bookshelf.BookShelfLogger;
import com.dingyue.contract.router.RouterConfig;
import com.dingyue.contract.router.RouterUtil;
import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.SearchBookActivity;
import com.intelligent.reader.app.BookApplication;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.view.ConsumeEvent;
import net.lzbook.kit.book.view.FirstUsePointView;
import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.CustomWebClient;
import net.lzbook.kit.utils.JSInterfaceHelper;

import java.lang.ref.WeakReference;

import de.greenrobot.event.EventBus;

public class WebViewFragment extends Fragment implements View.OnClickListener {

    private static String TAG = WebViewFragment.class.getSimpleName();
    public String url;
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
    private ImageView img_head_search;
    private ImageView img_download_manager;
    private TextView txt_head_title;
    private int bottomType;//青果打点搜索 2 推荐  3 榜单

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
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
        return rootView;
    }
    public void setTitle(String title, int logBottomType) {
        if(txt_head_title != null){
            txt_head_title.setText(title);
        }
        bottomType = logBottomType;
    }

    @SuppressLint("JavascriptInterface")
    private void initView() {
        if (rootView != null) {
            contentLayout = (RelativeLayout) rootView.findViewById(R.id.web_content_layout);
            contentView = (WebView) rootView.findViewById(R.id.web_content_view);
            img_head_setting = rootView.findViewById(R.id.img_head_setting);
            img_download_manager = rootView.findViewById(R.id.img_download_manager);
            img_head_search = rootView.findViewById(R.id.img_head_search);
            txt_head_title = rootView.findViewById(R.id.txt_head_title);
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

        img_head_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    BookShelfLogger.INSTANCE.uploadBookShelfPersonal();
                    EventBus.getDefault().post(new ConsumeEvent(R.id.fup_head_personal));
                    RouterUtil.INSTANCE.navigation(requireActivity(), RouterConfig.SETTING_ACTIVITY);
            }
        });

        img_head_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RouterUtil.INSTANCE.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY);
                if(bottomType ==2){
                    StartLogClickUtil.upLoadEventLog(requireActivity(), StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.QG_TJY_SEARCH);
                }else if(bottomType==3){
                    StartLogClickUtil.upLoadEventLog(requireActivity(), StartLogClickUtil.TOP_PAGE, StartLogClickUtil.QG_BDY_SEARCH);
                }else if(bottomType == 4){
                    StartLogClickUtil.upLoadEventLog(requireActivity(), StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.QG_FL_SEARCH);
                }else{
                    StartLogClickUtil.upLoadEventLog(requireActivity(), StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.SEARCH);
                }
            }
        });

        img_download_manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RouterUtil.INSTANCE.navigation(requireActivity(), RouterConfig.DOWNLOAD_MANAGER_ACTIVITY);
                BookShelfLogger.INSTANCE.uploadBookShelfCacheManager();
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

    public interface FragmentCallback {
        void webJsCallback(JSInterfaceHelper jsInterfaceHelper);

        String startLoad(WebView webView, String url);

    }
}