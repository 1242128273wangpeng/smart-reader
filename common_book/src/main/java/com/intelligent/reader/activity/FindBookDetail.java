package com.intelligent.reader.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.intelligent.reader.R;
import com.intelligent.reader.util.PagerDesc;
import com.intelligent.reader.widget.topshadow.TopShadowWebView;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.CustomWebClient;
import net.lzbook.kit.utils.JSInterfaceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import iyouqu.theme.FrameActivity;


public class FindBookDetail extends FrameActivity implements View.OnClickListener {

    private static String TAG = FindBookDetail.class.getSimpleName();
    String rankType;
    private RelativeLayout find_book_detail_main;
    private ImageView find_book_detail_back;
    //    private ImageView bangdan_pull;
    private TextView find_book_detail_title;
    private ImageView find_book_detail_search;
    private PopupWindow popupWindow;
    private RelativeLayout bangdanWeek;
    private RelativeLayout bangdanMonth;
    private RelativeLayout bangdanTotal;
    private TextView tv_bd_total;
    private TextView tv_bd_week;
    private TextView tv_bd_month;
    private ImageView weekSelect;
    private ImageView monthSelect;
    private ImageView totalSelect;
    private TopShadowWebView find_detail_content;
    private String currentUrl;
    private String currentTitle;
    private ArrayList<String> urls;
    private ArrayList<String> names;
    private int backClickCount;
    private LoadingPage loadingpage;
    private CustomWebClient customWebClient;
    private JSInterfaceHelper jsInterfaceHelper;
    private Handler handler;
    private SharedPreferences mSharedPreferences;
    private String fromType = "";
    private PagerDesc mPagerDesc;
    private int h5Margin;
    private boolean isSupport = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.act_find_detail);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        handler = new Handler();
        urls = new ArrayList<String>();
        names = new ArrayList<String>();

        Intent intent = getIntent();
        if (intent != null) {
            currentUrl = intent.getStringExtra("url");
            urls.add(currentUrl);
            currentTitle = intent.getStringExtra("title");
            names.add(currentTitle);
        }
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        fromType = mSharedPreferences.getString(Constants.FINDBOOK_SEARCH, "other");
        initView();

        initJSHelp();

        if (!TextUtils.isEmpty(currentUrl)) {
            loadWebData(currentUrl, currentTitle);
        }
    }

    private void initView() {
        find_book_detail_main = (RelativeLayout) findViewById(R.id.find_book_detail_main);
//        bangdan_pull = (ImageView) findViewById(R.id.iv_banddan_pull);
//        view_zhehzao = findViewById(R.id.view_zhezhao);
        find_book_detail_back = (ImageView) findViewById(R.id.find_book_detail_back);
        find_book_detail_title = (TextView) findViewById(R.id.find_book_detail_title);
        find_book_detail_search = (ImageView) findViewById(R.id.find_book_detail_search);
        find_detail_content = (TopShadowWebView) findViewById(R.id.rank_content);
//        find_detail_content.setTopShadow(findViewById(R.id.img_head_shadow));
        initListener();

        if (Build.VERSION.SDK_INT >= 11) {
            find_book_detail_main.setLayerType(View.LAYER_TYPE_NONE, null);
        }


        loadingpage = new LoadingPage(this, find_book_detail_main, LoadingPage.setting_result);

        if (find_detail_content != null) {
            customWebClient = new CustomWebClient(this, find_detail_content);
        }

        if (find_detail_content != null && customWebClient != null) {
            customWebClient.setWebSettings();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                find_detail_content.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            find_detail_content.setWebViewClient(customWebClient);
        }

        if (find_detail_content != null) {
            jsInterfaceHelper = new JSInterfaceHelper(this, find_detail_content);
        }

        if (jsInterfaceHelper != null && find_detail_content != null) {
            find_detail_content.addJavascriptInterface(jsInterfaceHelper, "J_search");
        }
    }

    private void initListener() {
        if (find_book_detail_title != null && !TextUtils.isEmpty(currentTitle)) {
            find_book_detail_title.setText(currentTitle);
        }
        find_book_detail_title.setOnClickListener(this);
        if (find_book_detail_back != null) {
            find_book_detail_back.setOnClickListener(this);
        }
        find_book_detail_search.setOnClickListener(this);
        addTouchListener();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.find_book_detail_back:
                Map<String, String> data = new HashMap<>();
                data.put("type", "1");
                if (fromType.equals("class")) {
                    data.put("firstclass", currentTitle);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTCLASS_PAGE, StartLogClickUtil.BACK, data);
                } else if (fromType.equals("top")) {
                    data.put("firsttop", currentTitle);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTTOP_PAGE, StartLogClickUtil.BACK, data);
                } else if (fromType.equals("recommend")) {
                    data.put("firstrecommend", currentTitle);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTRECOMMEND_PAGE, StartLogClickUtil.BACK, data);
                }
                clickBackBtn();
                break;
            case R.id.find_book_detail_search:
                Map<String, String> postData = new HashMap<>();

                if (fromType.equals("class")) {
                    postData.put("firstclass", currentTitle);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTCLASS_PAGE, StartLogClickUtil.SEARCH, postData);
                } else if (fromType.equals("top")) {
                    postData.put("firsttop", currentTitle);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTTOP_PAGE, StartLogClickUtil.SEARCH, postData);
                } else if (fromType.equals("recommend")) {
                    postData.put("firstrecommend", currentTitle);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTRECOMMEND_PAGE, StartLogClickUtil.SEARCH, postData);
                }

                Intent intent = new Intent();
                intent.setClass(this, SearchBookActivity.class);
                startActivity(intent);
                break;
//            case R.id.iv_banddan_pull:
//            case R.id.find_book_detail_title:
//                showPopupWindow();
//                break;
        }
    }

//    View view_zhehzao;
//    private void showPopupWindow() {
//        if (popupWindow != null && !popupWindow.isShowing()) {
////            View view = findViewById(R.id.find_book_detail_head);
////            popupWindow.showAtLocation(view, Gravity.TOP, 0, view.getHeight());
//            popupWindow.showAsDropDown(findViewById(R.id.find_book_detail_head),0,0);
//            view_zhehzao.setVisibility(View.VISIBLE);
//        }
//    }

//    private void dissmissPop(){
//        if (popupWindow != null && popupWindow.isShowing()) {
//            popupWindow.dismiss();
//            view_zhehzao.setVisibility(View.GONE);
//        }
//    }

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
        if (find_detail_content != null) {
            find_detail_content.clearCache(true); //清空缓存
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (find_book_detail_main != null) {
                    find_book_detail_main.removeView(find_detail_content);
                }
                find_detail_content.stopLoading();
                find_detail_content.removeAllViews();
                //find_detail_content.destroy();
            } else {
                find_detail_content.stopLoading();
                find_detail_content.removeAllViews();
                //find_detail_content.destroy();
                if (find_book_detail_main != null) {
                    find_book_detail_main.removeView(find_detail_content);
                }
            }
            find_detail_content = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (urls.size() - backClickCount <= 1) {
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
        Map<String, String> map = null;
        if (url != null) {
            String[] array = url.split("\\?");
            url = array[0];
            if (array.length == 2) {//如果传递过来的url带参数   /cn.kkqbtxtxs.reader/v3/rank/more.do?type=100&rankType=0
                map = UrlUtils.getUrlParams(array[1]);
            } else if (array.length == 1) {//如果传递过来的url不带参数   /cn.kkqbtxtxs.reader/v3/rank/index.do
                map = new HashMap<String, String>();
            }
            url = UrlUtils.buildWebUrl(url, map);
        }

        //如果可以切换周榜和月榜总榜
        if (map != null && map.get("qh") != null && map.get("qh").equals("true")) {
            setPullImageVisible(true);
            //刷新popwindow的UI
            rankType = analysisUrl(currentUrl).get("rankType");
//            updatePopView();
        } else {//如果不可以切换周榜和月榜总榜
            setPullImageVisible(false);
            setTitle(name);
        }

        startLoading(handler, url);
        webViewCallback();

    }

    private void startLoading(Handler handler, final String url) {
        if (find_detail_content == null) {
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
        AppLog.e(TAG, "LoadingData ==> " + url);
        if (!TextUtils.isEmpty(url) && find_detail_content != null) {
            try {
                find_detail_content.loadUrl(url);
            } catch (NullPointerException e) {
                e.printStackTrace();
                this.finish();
            }
        }
    }

    private void webViewCallback() {
        if (find_book_detail_main == null) {
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
                    addCheckSlide(find_detail_content);
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
                    find_detail_content.reload();
                }
            });
        }

    }

    private void initJSHelp() {
        jsInterfaceHelper.setOnSearchClick(new JSInterfaceHelper.onSearchClick() {
            @Override
            public void doSearch(String keyWord, String search_type, String filter_type, String filter_word, String sort_type) {
                try {
                    Map<String, String> data = new HashMap<>();
                    data.put("keyword", keyWord);
                    data.put("type", "1");//0 代表从分类过来 1 代表从FindBookDetail
                    StartLogClickUtil.upLoadEventLog(FindBookDetail.this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.SYSTEM_SEARCHRESULT, data);

                    Intent intent = new Intent();
                    intent.setClass(FindBookDetail.this, SearchBookActivity.class);
                    intent.putExtra("word", keyWord);
                    intent.putExtra("search_type", search_type);
                    intent.putExtra("filter_type", filter_type);
                    intent.putExtra("filter_word", filter_word);
                    intent.putExtra("sort_type", sort_type);
                    intent.putExtra("from_class", "findBookDetail");
                    startActivity(intent);
                    AppLog.i(TAG, "enterSearch success");
                } catch (Exception e) {
                    AppLog.e(TAG, "Search failed");
                    e.printStackTrace();
                }
            }
        });

        jsInterfaceHelper.setOnEnterCover(new JSInterfaceHelper.onEnterCover() {

            @Override
            public void doCover(final String host, final String book_id, final String book_source_id, final String name, final String author, final String parameter, final String extra_parameter) {
                AppLog.e(TAG, "doCover");

                RequestItem requestItem = new RequestItem();
                requestItem.book_id = book_id;
                requestItem.book_source_id = book_source_id;
                requestItem.host = host;
                requestItem.name = name;
                requestItem.author = author;
                requestItem.parameter = parameter;
                requestItem.extra_parameter = extra_parameter;

                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CoverPageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        jsInterfaceHelper.setOnAnotherWebClick(new JSInterfaceHelper.onAnotherWebClick() {

            @Override
            public void doAnotherWeb(String url, String name) {
                AppLog.e(TAG, "doAnotherWeb");
                String packageName = AppUtils.getPackageName();

                if ("cc.kdqbxs.reader".equals(packageName) || "cc.quanbennovel".equals(packageName) || "cn.txtkdxsdq.reader".equals(packageName)) {
                    try {
                        Intent intent = new Intent();
                        intent.setClass(FindBookDetail.this, FindBookDetail.class);
                        intent.putExtra("url", url);
                        intent.putExtra("title", name);
                        startActivity(intent);
                        AppLog.e(TAG, "EnterAnotherWeb");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        currentUrl = url;
                        currentTitle = name;
                        urls.add(currentUrl);
                        names.add(currentTitle);
                        loadWebData(currentUrl, name);
//                    setTitle(name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        if (isNeedInterceptSlide()) {
            jsInterfaceHelper.setOnH5PagerInfo(new JSInterfaceHelper.OnH5PagerInfoListener() {
                @Override
                public void onH5PagerInfo(int x, int y, int width, int height) {
                    mPagerDesc = new PagerDesc(y, x, x + width, y + height);
                }
            });

        }
    }

    private void setTitle(final String name) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                find_book_detail_title.setText(name);
            }
        });
    }

    private void setPullImageVisible(boolean visible) {
        if (visible) {
//            bangdan_pull.setVisibility(View.VISIBLE);
        } else {
//            bangdan_pull.setVisibility(View.GONE);
        }
    }

    private void setSearchBtnVisibel(boolean visibel) {
        if (visibel) {
            find_book_detail_search.setVisibility(View.VISIBLE);
        } else {
            find_book_detail_search.setVisibility(View.GONE);
        }
    }

//    private void updatePopView(){
//        tv_bd_total.setText(currentTitle+"—总榜");
//        tv_bd_week.setText(currentTitle+"—周榜");
//        tv_bd_month.setText(currentTitle+"—月榜");
//        if ("week".equals(rankType)){
//            find_book_detail_title.setText(currentTitle+"—周榜");
//            weekSelect.setVisibility(View.VISIBLE);
//            monthSelect.setVisibility(View.GONE);
//            totalSelect.setVisibility(View.GONE);
//        }else if ("month".equals(rankType)){
//            find_book_detail_title.setText(currentTitle+"—月榜");
//            monthSelect.setVisibility(View.VISIBLE);
//            weekSelect.setVisibility(View.GONE);
//            totalSelect.setVisibility(View.GONE);
//        }else if ("total".equals(rankType)){
//            find_book_detail_title.setText(currentTitle+"—总榜");
//            totalSelect.setVisibility(View.VISIBLE);
//            weekSelect.setVisibility(View.GONE);
//            monthSelect.setVisibility(View.GONE);
//        }
//    }

//    private void initPopupWindow() {
//        PopupClickListener popupClickListener = new PopupClickListener();
//        View popupView = getLayoutInflater().inflate(R.layout.bookstore_bangdan_select, null);
//        bangdanWeek = (RelativeLayout) popupView.findViewById(R.id.rl_bangdan_week);
//        bangdanMonth = (RelativeLayout) popupView.findViewById(R.id.rl_bangdan_month);
//        bangdanTotal = (RelativeLayout) popupView.findViewById(R.id.rl_bangdan_total);
//        tv_bd_total = (TextView) popupView.findViewById(R.id.tv_bd_total);
//        tv_bd_week = (TextView) popupView.findViewById(R.id.tv_bd_week);
//        tv_bd_month = (TextView) popupView.findViewById(R.id.tv_bd_month);
//        weekSelect = (ImageView) popupView.findViewById(R.id.iv_bangdan_week);
//        monthSelect = (ImageView) popupView.findViewById(R.id.iv_bangdan_month);
//        totalSelect = (ImageView) popupView.findViewById(R.id.iv_bangdan_total);
//
//        bangdanWeek.setOnClickListener(popupClickListener);
//        bangdanMonth.setOnClickListener(popupClickListener);
//        bangdanTotal.setOnClickListener(popupClickListener);
//
//        popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT,true);
//        popupWindow.setTouchable(true);
//        popupWindow.setOutsideTouchable(true);
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
//
//        popupWindow.getContentView().setFocusableInTouchMode(true);
//        popupWindow.getContentView().setFocusable(true);
//        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                view_zhehzao.setVisibility(View.GONE);
//            }
//        });
//        popupWindow.getContentView().setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0
//                        && event.getAction() == KeyEvent.ACTION_DOWN) {
//                    if (popupWindow != null && popupWindow.isShowing()) {
//                        dissmissPop();
//                    }
//                    return true;
//                }
//                return false;
//            }
//        });
//    }

    private Map<String, String> analysisUrl(String ulr) {
        Map<String, String> map = new HashMap<>();
        if (ulr != null) {
            String[] array = ulr.split("\\?");
            if (array.length == 2) {//如果传递过来的url带参数   /cn.kkqbtxtxs.reader/v3/rank/more.do?type=100&rankType=0
                ulr = array[0];
                map = UrlUtils.getUrlParams(array[1]);
            }
        }
        return map;
    }

    private void reLoadWebData(String currentUrl, int type) {
        String url = "";
        Map<String, String> map = new HashMap<>();
        if (currentUrl != null) {
            String[] array = currentUrl.split("\\?");
            if (array.length == 2) {//如果传递过来的url带参数   /cn.kkqbtxtxs.reader/v3/rank/more.do?type=100&rankType=0
                url = array[0];
                map = UrlUtils.getUrlParams(array[1]);
            }
        }
        switch (type) {
            case 0:
                map.put("rankType", "week");
                break;
            case 1:
                map.put("rankType", "month");
                break;
            case 2:
                map.put("rankType", "total");
                break;
        }

        url = UrlUtils.buildUrl(url, map);
        if (url.contains(UrlUtils.getBookNovelDeployHost())) {
            int start = url.lastIndexOf(UrlUtils.getBookNovelDeployHost()) + UrlUtils.getBookNovelDeployHost().length();
            String tempUrl = url.substring(start, url.length());
            this.currentUrl = tempUrl;
        }

        //如果可以切换周榜和月榜总榜
        if (map != null && map.get("qh") != null && map.get("qh").equals("true")) {
            setPullImageVisible(true);
        } else {//如果不可以切换周榜和月榜总榜
            setPullImageVisible(false);
        }

        startLoading(handler, url);
        webViewCallback();
    }


//    class PopupClickListener implements View.OnClickListener{
//
//        @Override
//        public void onClick(View v) {
//            //设置UI改变
//            int type = 0;
//            switch (v.getId()){
//                case R.id.rl_bangdan_week:
//                    weekSelect.setVisibility(View.VISIBLE);
//                    monthSelect.setVisibility(View.GONE);
//                    totalSelect.setVisibility(View.GONE);
//                    setTitle(currentTitle+"—周榜");
//                    type = 0;
//                    break;
//                case R.id.rl_bangdan_month:
//                    weekSelect.setVisibility(View.GONE);
//                    monthSelect.setVisibility(View.VISIBLE);
//                    totalSelect.setVisibility(View.GONE);
//                    setTitle(currentTitle+"—月榜");
//                    type = 1;
//                    break;
//                case R.id.rl_bangdan_total:
//                    weekSelect.setVisibility(View.GONE);
//                    monthSelect.setVisibility(View.GONE);
//                    totalSelect.setVisibility(View.VISIBLE);
//                    setTitle(currentTitle+"—总榜");
//                    type = 2;
//                    break;
//            }
//            dissmissPop();
//            //重新加载webview
//            reLoadWebData(currentUrl,type);
//        }
//    }

    private void clickBackBtn() {
        if (urls.size() - backClickCount <= 1) {
            FindBookDetail.this.finish();
        } else {
            backClickCount++;
            int nowIndex = urls.size() - 1 - backClickCount;

            currentUrl = urls.get(nowIndex);
            currentTitle = names.get(nowIndex);
            loadWebData(currentUrl, currentTitle);
        }
        return;
    }

    private void addCheckSlide(WebView find_detail_content) {
        if (find_detail_content != null && isNeedInterceptSlide()) {
            find_detail_content.loadUrl("javascript:getViewPagerInfo()");
        }
    }

    private boolean isNeedInterceptSlide() {
        String packageName = AppUtils.getPackageName();
        if (("cc.kdqbxs.reader".equals(packageName) || "cc.quanbennovel".equals(packageName) || "cn.txtkdxsdq.reader".equals(packageName)) && !TextUtils.isEmpty(currentTitle) && (currentTitle.contains("男频") || currentTitle.contains("女频"))) {
            return true;
        }
        return false;
    }

    private void addTouchListener() {
        if (find_detail_content != null && isNeedInterceptSlide()) {
            find_detail_content.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float y = event.getRawY();
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (find_detail_content != null) {
                                int[] loction = new int[2];
                                find_detail_content.getLocationOnScreen(loction);
                                h5Margin = loction[1];
                            }
                            if (null != mPagerDesc) {
                                int top = mPagerDesc.getTop();
                                int bottom = top + (mPagerDesc.getBottom() - mPagerDesc.getTop());
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
