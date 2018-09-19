package com.intelligent.reader.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.ding.basic.net.api.service.RequestService;
import com.intelligent.reader.R;
import com.intelligent.reader.app.BookApplication;
import com.intelligent.reader.util.PagerDesc;

import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.CustomWebClient;
import net.lzbook.kit.utils.JSInterfaceHelper;
import net.lzbook.kit.utils.download.DownloadAPKService;
import net.lzbook.kit.utils.oneclick.AntiShake;

import java.util.ArrayList;

import iyouqu.theme.FrameActivity;

public class WelfareCenterActivity extends FrameActivity implements View.OnClickListener {

    private static String TAG = WelfareCenterActivity.class.getSimpleName();

    private RelativeLayout welfare_center_main;

    private ImageView welfare_center_back;
    private TextView welfare_center_title;
    private ImageView welfare_center_search;
    private WebView welfare_center_content;

    private String currentUrl;
    private String currentTitle;

    private ArrayList<String> urls;
    private ArrayList<String> names;

    private int backClickCount;

    private LoadingPage loadingpage;
    private CustomWebClient customWebClient;
    private JSInterfaceHelper jsInterfaceHelper;

    private Handler handler;

    private PagerDesc mPagerDesc;

    private int h5Margin;

    private boolean isSupport = false;

    private AntiShake shake = new AntiShake();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_welfare_center);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        handler = new Handler();

        urls = new ArrayList<>();
        names = new ArrayList<>();

        Intent intent = getIntent();

        if (intent != null) {
            currentUrl = intent.getStringExtra("url");
            urls.add(currentUrl);

            currentTitle = intent.getStringExtra("title");
            names.add(currentTitle);
        }

        AppUtils.disableAccessibility(this);
        initView();

        initJSHelp();

        if (!TextUtils.isEmpty(currentUrl)) {
            loadWebData(currentUrl, currentTitle);
        }
    }

    private void initView() {
        welfare_center_main =  findViewById(R.id.welfare_center_main);
        welfare_center_back =  findViewById(R.id.welfare_center_back);
        welfare_center_title =  findViewById(R.id.welfare_center_title);
        welfare_center_search =  findViewById(R.id.welfare_center_search);
        welfare_center_content =  findViewById(R.id.welfare_center_content);

        initListener();

        if (currentUrl.contains(RequestService.AUTHOR_V4)) {
            welfare_center_search.setVisibility(View.GONE);
        } else {
            welfare_center_search.setVisibility(View.GONE);
        }

        welfare_center_main.setLayerType(View.LAYER_TYPE_NONE, null);

        loadingpage = new LoadingPage(this, welfare_center_main, LoadingPage.setting_result);

        if (welfare_center_content != null) {
            customWebClient = new CustomWebClient(this, welfare_center_content);
        }

        if (welfare_center_content != null && customWebClient != null) {
            customWebClient.setWebSettings();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                welfare_center_content.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            welfare_center_content.setWebViewClient(customWebClient);
        }

        if (welfare_center_content != null) {
            jsInterfaceHelper = new JSInterfaceHelper(this, welfare_center_content);
        }

        if (jsInterfaceHelper != null && welfare_center_content != null) {
            welfare_center_content.addJavascriptInterface(jsInterfaceHelper, "J_search");
        }
    }

    private void initListener() {
        if (welfare_center_title != null && !TextUtils.isEmpty(currentTitle)) {
            welfare_center_title.setText(currentTitle);
        }

        if (welfare_center_back != null) {
            welfare_center_back.setOnClickListener(this);
        }

        if (welfare_center_search != null) {
            welfare_center_search.setOnClickListener(this);
        }

        addTouchListener();
    }

    private void initJSHelp() {

        jsInterfaceHelper.setOnWebGameClick(new JSInterfaceHelper.onWebGameClick() {

            @Override
            public void openWebGame(String url, String name) {
                AppLog.e("福利中心", "网页游戏: " + name + " : " + url);

                if (shake.check()) {
                    return;
                }

                AppLog.e(TAG, "openWebGame");

                try {
                    currentUrl = url;
                    currentTitle = name;

                    urls.add(currentUrl);
                    names.add(currentTitle);

                    loadWebData(currentUrl, name);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        jsInterfaceHelper.setOnGameAppClick(new JSInterfaceHelper.onGameAppClick() {

            @Override
            public void downloadGame(String url, String name) {
                AppLog.e("福利中心", "下载游戏: " + name + " : " + url);

                Intent intent = new Intent(BookApplication.getGlobalContext(), DownloadAPKService.class);
                intent.putExtra("url", url);
                intent.putExtra("name", name);
                startService(intent);

            }
        });

        if (isNeedInterceptSlide()) {
            jsInterfaceHelper.setOnH5PagerInfo(new JSInterfaceHelper.OnH5PagerInfoListener() {
                @Override
                public void onH5PagerInfo(float x, float y, float width, float height) {
                    mPagerDesc = new PagerDesc(y, x, x + width, y + height);
                }
            });

        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.welfare_center_back:
                clickBackBtn();
                break;
            case R.id.welfare_center_search:
                Intent intent = new Intent();
                intent.setClass(this, SearchBookActivity.class);
                startActivity(intent);
//                SearchBookActivity.Companion.setSatyHistory(false);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (welfare_center_content != null) {
            welfare_center_content.clearCache(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (welfare_center_main != null) {
                    welfare_center_main.removeView(welfare_center_content);
                }
                welfare_center_content.stopLoading();
                welfare_center_content.removeAllViews();
            } else {
                welfare_center_content.stopLoading();
                welfare_center_content.removeAllViews();
                if (welfare_center_main != null) {
                    welfare_center_main.removeView(welfare_center_content);
                }
            }

            welfare_center_content.destroy();

            welfare_center_content = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (urls.size() - backClickCount <= 1) {
//            SearchBookActivity.Companion.setSatyHistory(true);
            super.onBackPressed();
        } else {
            backClickCount++;
            int nowIndex = urls.size() - 1 - backClickCount;

            currentUrl = urls.get(nowIndex);
            currentTitle = names.get(nowIndex);
            loadWebData(currentUrl, currentTitle);
        }

    }

    private void loadWebData(String url, String name) {
        setTitle(name);

        startLoading(handler, url);

        webViewCallback();
    }

    private void startLoading(Handler handler, final String url) {
        if (welfare_center_content == null) {
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

        if (!TextUtils.isEmpty(url) && welfare_center_content != null) {
            try {
                welfare_center_content.loadUrl(url);
            } catch (NullPointerException nullPointerException) {
                nullPointerException.printStackTrace();
                this.finish();
            }
        }
    }

    private void webViewCallback() {
        if (welfare_center_main == null) {
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
                    addCheckSlide(welfare_center_content);
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
                    welfare_center_content.reload();
                }
            });
        }

    }

    private void setTitle(final String name) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                welfare_center_title.setText(name);
            }
        });
    }


    private void clickBackBtn() {
//        SearchBookActivity.Companion.setSatyHistory(true);

        if (urls.size() - backClickCount <= 1) {
            WelfareCenterActivity.this.finish();
        } else {
            backClickCount++;
            int nowIndex = urls.size() - 1 - backClickCount;

            currentUrl = urls.get(nowIndex);
            currentTitle = names.get(nowIndex);
            loadWebData(currentUrl, currentTitle);
        }
    }

    private void addCheckSlide(WebView welfare_center_content) {
        if (welfare_center_content != null && isNeedInterceptSlide()) {
            welfare_center_content.loadUrl("javascript:getViewPagerInfo()");
        }
    }

    private boolean isNeedInterceptSlide() {
        if ("cc.kdqbxs.reader".equals(AppUtils.getPackageName()) && !TextUtils.isEmpty(currentTitle) && (currentTitle.contains("男频") || currentTitle.contains("女频"))) {
            return true;
        }
        return false;
    }

    private void addTouchListener() {
        if (welfare_center_content != null && isNeedInterceptSlide()) {
            welfare_center_content.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float y = event.getRawY();
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (welfare_center_content != null) {
                                int[] loction = new int[2];
                                welfare_center_content.getLocationOnScreen(loction);
                                h5Margin = loction[1];
                            }
                            if (null != mPagerDesc) {
                                float top = mPagerDesc.getTop();
                                float bottom = top + (mPagerDesc.getBottom() - mPagerDesc.getTop());
                                DisplayMetrics metric = getResources().getDisplayMetrics();
                                top = (int) (top * metric.density) + h5Margin;
                                bottom = (int) (bottom * metric.density) + h5Margin;
                                if (y > top && y < bottom) {
                                    isSupport = false;
                                } else {
                                    isSupport = true;
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isSupport = true;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                        default:
                            isSupport = true;
                            break;
                    }
                    return false;
                }
            });
        }
    }


    @Override
    public boolean supportSlideBack() {
        return isSupport;
    }
}
