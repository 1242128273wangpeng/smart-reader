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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ding.basic.request.RequestService;
import com.dingyue.contract.util.SharedPreUtil;
import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.R;
import com.intelligent.reader.app.BookApplication;
import com.intelligent.reader.util.PagerDesc;
import com.intelligent.reader.view.SelectSexDialog;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.CustomWebClient;
import net.lzbook.kit.utils.JSInterfaceHelper;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class WebViewFragment extends Fragment implements SelectSexDialog.onAniFinishedCallback {

    private static String TAG = WebViewFragment.class.getSimpleName();
    public String url = "";
    private String type = "";
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
    private TextView txt_head_title;
    private int bottomType;//青果打点搜索 2 推荐  3 榜单
    private RelativeLayout rl_head;
    private View img_shadow;
    private int count = 0;
    private SelectSexDialog selectSexDialog;
    private SharedPreUtil sharedPreUtil;
    private ImageView img_sex;
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
        sharedPreUtil = new SharedPreUtil(SharedPreUtil.SHARE_DEFAULT);
        initView();
        return rootView;
    }

    public void setTitle(String title, int logBottomType) {
        if (txt_head_title != null) {
            txt_head_title.setText(title);
        }
        bottomType = logBottomType;
    }

    @SuppressLint("JavascriptInterface")
    private void initView() {
        if (rootView != null) {
            contentLayout = (RelativeLayout) rootView.findViewById(R.id.web_content_layout);
            contentView = (WebView) rootView.findViewById(R.id.web_content_view);
            txt_head_title = rootView.findViewById(R.id.txt_head_title);
            rl_head = (RelativeLayout) rootView.findViewById(R.id.rl_head);
            img_sex = rootView.findViewById(R.id.img_sex);
            img_shadow = rootView.findViewById(R.id.img_shadow);

            if (contentView != null) {
                contentView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            if(img_sex != null){
                img_sex.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(weakReference.get() != null){
                            if (selectSexDialog == null) {
                                selectSexDialog = new SelectSexDialog(weakReference.get());
                                selectSexDialog.setAniFinishedAction(WebViewFragment.this);
                            }
                            Map<String,String> data = new HashMap<>();
                            //0 表示男  1 表示女
                            if (sharedPreUtil.getInt(SharedPreUtil.RANK_SELECT_SEX, 0) == 0) {
                                data.put("type","2");
                                sharedPreUtil.putInt(SharedPreUtil.RANK_SELECT_SEX, 1);
                                img_sex.setImageResource(R.drawable.rank_gril_icon);
                                selectSexDialog.show(false);
                                String uri = RequestService.WEB_RANK_H5_Girl.replace("{packageName}",
                                        AppUtils.getPackageName());
                                url = UrlUtils.buildWebUrl(uri, new HashMap());
                                loadingData(url);
                            } else {
                                data.put("type","1");
                                sharedPreUtil.putInt(SharedPreUtil.RANK_SELECT_SEX, 0);
                                selectSexDialog.show(true);
                                img_sex.setImageResource(R.drawable.rank_boy_icon);
                                String uri = RequestService.WEB_RANK_H5_BOY.replace("{packageName}",
                                        AppUtils.getPackageName());
                                url = UrlUtils.buildWebUrl(uri, new HashMap());
                                loadingData(url);
                            }
                            StartLogClickUtil.upLoadEventLog(weakReference.get(),StartLogClickUtil.TOP_PAGE,StartLogClickUtil.QG_SWITCHTAB,data);
                        }

                    }
                });
            }

        }

        if (type.equals("recommend_male") || type.equals("recommend_female")) {
            if (rl_head != null) {
                rl_head.setVisibility(View.GONE);
            }
            if (img_shadow != null) {
                img_shadow.setVisibility(View.GONE);
            }
        }
        if(img_sex != null){
            if ("rankBoy".equals(type)) {
                img_sex.setVisibility(View.VISIBLE);
                img_sex.setImageResource(R.drawable.rank_boy_icon);
            } else if ("rankGirl".equals(type)) {
                img_sex.setVisibility(View.VISIBLE);
                img_sex.setImageResource(R.drawable.rank_gril_icon);
            } else {
                img_sex.setVisibility(View.GONE);
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
        addTouchListener();
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
        if (fragmentCallback != null && contentView != null) {
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

    public boolean isNeedInterceptSlide() {
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
                    if(contentView != null){
                        contentView.loadUrl(url);
                    }

                }
            });
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (selectSexDialog != null) {
            selectSexDialog = null;
        }
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

    @Override
    public void onAniFinished() {
        if (weakReference.get() != null && selectSexDialog != null) {
            if (selectSexDialog.isShow()) {
                selectSexDialog.dismiss();
            }
        }
    }

    public interface FragmentCallback {
        void webJsCallback(JSInterfaceHelper jsInterfaceHelper);

        String startLoad(WebView webView, String url);

    }
}