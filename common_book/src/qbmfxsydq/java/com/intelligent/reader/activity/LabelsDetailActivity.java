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
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.ding.basic.Config;
import com.ding.basic.bean.Book;
import com.ding.basic.bean.Chapter;
import com.ding.basic.repository.RequestRepositoryFactory;
import com.ding.basic.request.RequestService;
import com.dingyue.contract.CommonContract;
import com.dingyue.contract.router.RouterConfig;
import com.dingyue.contract.router.RouterUtil;
import com.dingyue.contract.util.SharedPreUtil;
import com.intelligent.reader.R;
import com.intelligent.reader.util.PagerDesc;
import com.intelligent.reader.view.SelectSexDialog;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.CustomWebClient;
import net.lzbook.kit.utils.EnterUtilKt;
import net.lzbook.kit.utils.JSInterfaceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import iyouqu.theme.FrameActivity;
import swipeback.ActivityLifecycleHelper;

import static net.lzbook.kit.utils.PushExtKt.IS_FROM_PUSH;

/**
 * WebView二级页面
 */
public class LabelsDetailActivity extends FrameActivity implements View.OnClickListener,SelectSexDialog.onAniFinishedCallback {

    private static String TAG = LabelsDetailActivity.class.getSimpleName();
    String rankType;
    private RelativeLayout find_book_detail_main;
    private ImageView find_book_detail_back;
    private TextView find_book_detail_title;
    private ImageView find_book_detail_search;
    private ImageView img_sex;
    private WebView find_detail_content;
    private String currentUrl;
    private String currentTitle;
    private ArrayList<String> urls;
    private ArrayList<String> names;
    private int backClickCount;
    private LoadingPage loadingpage;
    private CustomWebClient customWebClient;
    private JSInterfaceHelper jsInterfaceHelper;
    private Handler handler;
    private SharedPreUtil sharedPreUtil;
    private String fromType = "";
    private PagerDesc mPagerDesc;
    private int h5Margin;
    private boolean isSupport = true;
    private boolean isFromPush = false;
    private SelectSexDialog selectSexDialog;
    private boolean isMale;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.act_find_detail);
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
            isFromPush = intent.getBooleanExtra(IS_FROM_PUSH, false);
        }
        if (currentUrl == null || currentTitle == null) {
            onBackPressed();
            return;
        }
        sharedPreUtil = new SharedPreUtil(SharedPreUtil.SHARE_DEFAULT);
        fromType = sharedPreUtil.getString(SharedPreUtil.HOME_FINDBOOK_SEARCH,
                "other");
        AppUtils.disableAccessibility(this);
        initView();
        initJSHelp();

        if (!TextUtils.isEmpty(currentUrl)) {
            loadWebData(currentUrl, currentTitle);
        }
    }

    private void initView() {
        find_book_detail_main = findViewById(R.id.find_book_detail_main);
        find_book_detail_back = findViewById(R.id.find_book_detail_back);
        find_book_detail_title = findViewById(R.id.find_book_detail_title);
        find_book_detail_search = findViewById(R.id.find_book_detail_search);
        img_sex = findViewById(R.id.img_sex);

        find_detail_content = findViewById(R.id.rank_content);
        initListener();
        //判断是否是作者主页
        if (currentUrl.contains(RequestService.AUTHOR_V4) || currentUrl.contains(
                RequestService.AUTHOR_h5.replace("{packageName}", AppUtils.getPackageName()))) {
            find_book_detail_search.setVisibility(View.GONE);
        } else {
            find_book_detail_search.setVisibility(View.VISIBLE);
        }

        //精选页 榜单进入需要选男女
        if(currentUrl.contains("/h5/cc.quanben.novel/rank")){
            img_sex.setVisibility(View.VISIBLE);
            find_book_detail_search.setVisibility(View.GONE);
        }else {
            img_sex.setVisibility(View.GONE);
            find_book_detail_search.setVisibility(View.VISIBLE);
        }

        if (currentUrl.contains("/h5/cc.quanben.novel/rankBoy")) {
            isMale = true;
            img_sex.setImageResource(R.drawable.rank_boy_icon);
        } else{
            isMale = false;
            img_sex.setImageResource(R.drawable.rank_gril_icon);
        }

        if (Build.VERSION.SDK_INT >= 11) {
            find_book_detail_main.setLayerType(View.LAYER_TYPE_NONE, null);
        }


        loadingpage = new LoadingPage(this, find_book_detail_main, LoadingPage.setting_result);

        if (find_detail_content != null) {
            customWebClient = new CustomWebClient(this, find_detail_content);
        }

        if (find_detail_content != null) {
            customWebClient.setWebSettings();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                find_detail_content.getSettings().setMixedContentMode(
                        WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
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
        img_sex.setOnClickListener(this);
        addTouchListener();
    }

    @Override
    public boolean shouldLightStatusBase() {
        if ("cc.quanben.novel".equals(AppUtils.getPackageName())) {
            return true;
        }
        return super.shouldLightStatusBase();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.find_book_detail_back:
                Map<String, String> data = new HashMap<>();
                data.put("type", "1");
                if (fromType.equals("class")) {
                    data.put("firstclass", currentTitle);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTCLASS_PAGE,
                            StartLogClickUtil.BACK, data);
                } else if (fromType.equals("top")) {
                    data.put("firsttop", currentTitle);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTTOP_PAGE,
                            StartLogClickUtil.BACK, data);
                } else if (fromType.equals("recommend")) {
                    data.put("firstrecommend", currentTitle);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTRECOMMEND_PAGE,
                            StartLogClickUtil.BACK, data);
                } else if (fromType.equals("authorType")) {
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.AUTHORPAGE_PAGE,
                            StartLogClickUtil.BACK, data);
                }
                clickBackBtn();
                break;
            case R.id.find_book_detail_search:
                Map<String, String> postData = new HashMap<>();

                if (fromType.equals("class")) {
                    postData.put("firstclass", currentTitle);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTCLASS_PAGE,
                            StartLogClickUtil.SEARCH, postData);
                } else if (fromType.equals("top")) {
                    postData.put("firsttop", currentTitle);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTTOP_PAGE,
                            StartLogClickUtil.SEARCH, postData);
                } else if (fromType.equals("recommend")) {
                    postData.put("firstrecommend", currentTitle);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTRECOMMEND_PAGE,
                            StartLogClickUtil.SEARCH, postData);
                }

                RouterUtil.INSTANCE.navigation(this, RouterConfig.SEARCH_BOOK_ACTIVITY);

                break;
            case R.id.img_sex:
                if(selectSexDialog == null){
                    selectSexDialog = new SelectSexDialog(this);
                    selectSexDialog.setAniFinishedAction(this);
                }
                //0 表示男  1 表示女
                if(isMale){
                    isMale = false;
                    img_sex.setImageResource(R.drawable.rank_gril_icon);
                    selectSexDialog.show(false);
                    currentUrl = RequestService.WEB_RANK_H5_Girl.replace("{packageName}", AppUtils.getPackageName());
                    loadWebData(currentUrl,currentTitle);
                }else{
                    isMale = true;
                    selectSexDialog.show(true);
                    img_sex.setImageResource(R.drawable.rank_boy_icon);
                    currentUrl = RequestService.WEB_RANK_H5_BOY.replace("{packageName}", AppUtils.getPackageName());
                    loadWebData(currentUrl,currentTitle);
                }
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
        if(selectSexDialog != null){
            selectSexDialog = null;
        }
        if (find_detail_content != null) {
            find_detail_content.clearCache(false); //清空缓存
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (find_detail_content.getParent() != null) {
                    ((ViewGroup) find_detail_content.getParent()).removeView(find_detail_content);
                }
                find_detail_content.stopLoading();
                // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
                find_detail_content.getSettings().setJavaScriptEnabled(false);
                find_detail_content.clearHistory();
                find_detail_content.removeAllViews();
                find_detail_content.destroy();
            } else {
                find_detail_content.stopLoading();
                // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
                find_detail_content.getSettings().setJavaScriptEnabled(false);
                find_detail_content.clearHistory();
                find_detail_content.removeAllViews();
                find_detail_content.destroy();
                if (find_detail_content.getParent() != null) {
                    ((ViewGroup) find_detail_content.getParent()).removeView(find_detail_content);
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
            if (array.length
                    == 2) {//如果传递过来的url带参数   /cn.kkqbtxtxs.reader/v3/rank/more
                // .do?type=100&rankType=0
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
            public void doSearch(String keyWord, String search_type, String filter_type,
                    String filter_word, String sort_type) {
                if (CommonContract.INSTANCE.isDoubleClick(System.currentTimeMillis())) {
                    return;
                }
                try {
                    Map<String, String> data = new HashMap<>();
                    data.put("keyword", keyWord);
                    data.put("type", "1");//0 代表从分类过来 1 代表从FindBookDetail
                    StartLogClickUtil.upLoadEventLog(LabelsDetailActivity.this,
                            StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.SYSTEM_SEARCHRESULT,
                            data);

                    EnterUtilKt.enterSearch(LabelsDetailActivity.this,
                            keyWord, search_type, filter_type, filter_word, sort_type,
                            "findBookDetail");

                    AppLog.i(TAG, "enterSearch success");
                } catch (Exception e) {
                    AppLog.e(TAG, "Search failed");
                    e.printStackTrace();
                }
            }
        });

        jsInterfaceHelper.setOnEnterCover(new JSInterfaceHelper.onEnterCover() {

            @Override
            public void doCover(final String host, final String book_id,
                    final String book_source_id, final String name, final String author,
                    final String parameter, final String extra_parameter) {
                AppLog.e(TAG, "doCover");

                if (CommonContract.INSTANCE.isDoubleClick(System.currentTimeMillis())) {
                    return;
                }
                Map<String, String> data = new HashMap<>();
                data.put("BOOKID", book_id);
                data.put("source", "WEBVIEW");
                StartLogClickUtil.upLoadEventLog(LabelsDetailActivity.this,
                        StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ENTER, data);


                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CoverPageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("author", author);
                bundle.putString("book_id", book_id);
                bundle.putString("book_source_id", book_source_id);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        jsInterfaceHelper.setOnAnotherWebClick(new JSInterfaceHelper.onAnotherWebClick() {

            @Override
            public void doAnotherWeb(String url, String name) {
                AppLog.e(TAG, "doAnotherWeb");
                String packageName = AppUtils.getPackageName();
                if (CommonContract.INSTANCE.isDoubleClick(System.currentTimeMillis())) {
                    return;
                }
                if ("cc.kdqbxs.reader".equals(packageName)
                        || "cn.txtkdxsdq.reader".equals(packageName)) {
                    try {
                        Intent intent = new Intent();
                        intent.setClass(LabelsDetailActivity.this, LabelsDetailActivity.class);
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
                public void onH5PagerInfo(float x, float y, float width, float height) {
                    mPagerDesc = new PagerDesc(y, x, x + width, y + height);
                }
            });

        }

        jsInterfaceHelper.setOnInsertBook(new JSInterfaceHelper.OnInsertBook() {
            @Override
            public void doInsertBook(final String host, final String book_id,
                    final String book_source_id, final String name, final String author,
                    final String status, final String category, final String imgUrl,
                    final String last_chapter, final String
                    chapter_count, final long updateTime, final String parameter,
                    final String extra_parameter, final int
                    dex) {
                AppLog.e(TAG, "doInsertBook");
                Book book = genCoverBook(host, book_id, book_source_id, name, author, status,
                        category, imgUrl, last_chapter, chapter_count, updateTime, parameter,
                        extra_parameter, dex);
                boolean succeed = (RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                        BaseBookApplication.getGlobalContext()).insertBook(book) > 0);
                if (succeed) {
                    Toast.makeText(LabelsDetailActivity.this.getApplicationContext(),
                            R.string.bookshelf_insert_success, Toast.LENGTH_SHORT).show();
                }
            }
        });

        jsInterfaceHelper.setOnDeleteBook(new JSInterfaceHelper.OnDeleteBook() {
            @Override
            public void doDeleteBook(String book_id) {
                AppLog.e(TAG, "doDeleteBook");
                RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                        BaseBookApplication.getGlobalContext()).deleteBook(book_id);
                CacheManager.INSTANCE.stop(book_id);
                CacheManager.INSTANCE.resetTask(book_id);
                Toast.makeText(LabelsDetailActivity.this.getApplicationContext(),
                        R.string.bookshelf_delete_success, Toast.LENGTH_SHORT).show();
            }
        });

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

    }

    private void setSearchBtnVisibel(boolean visibel) {
        if (visibel) {
            find_book_detail_search.setVisibility(View.VISIBLE);
        } else {
            find_book_detail_search.setVisibility(View.GONE);
        }
    }

    private Map<String, String> analysisUrl(String ulr) {
        Map<String, String> map = new HashMap<>();
        if (ulr != null) {
            String[] array = ulr.split("\\?");
            if (array.length
                    == 2) {//如果传递过来的url带参数   /cn.kkqbtxtxs.reader/v3/rank/more
                // .do?type=100&rankType=0
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
            if (array.length
                    == 2) {//如果传递过来的url带参数   /cn.kkqbtxtxs.reader/v3/rank/more
                // .do?type=100&rankType=0
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
        if (url.contains(Config.INSTANCE.loadRequestAPIHost())) {
            int start = url.lastIndexOf(Config.INSTANCE.loadRequestAPIHost())
                    + Config.INSTANCE.loadRequestAPIHost().length();
            this.currentUrl = url.substring(start, url.length());
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

    private void clickBackBtn() {
        if (urls.size() - backClickCount <= 1) {
            LabelsDetailActivity.this.finish();
        } else {
            backClickCount++;
            int nowIndex = urls.size() - 1 - backClickCount;

            currentUrl = urls.get(nowIndex);
            currentTitle = names.get(nowIndex);
            loadWebData(currentUrl, currentTitle);
        }
    }

    private void addCheckSlide(WebView find_detail_content) {
        if (find_detail_content != null && isNeedInterceptSlide()) {
            find_detail_content.loadUrl("javascript:getViewPagerInfo()");
        }
    }

    private boolean isNeedInterceptSlide() {
        String packageName = AppUtils.getPackageName();
        if (("cc.kdqbxs.reader".equals(packageName) || "cc.quanbennovel".equals(packageName)
                || "cn.txtkdxsdq.reader".equals(packageName)) && !TextUtils.isEmpty(currentTitle)
                && (currentTitle.contains("男频") || currentTitle.contains("女频"))) {
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
        return ActivityLifecycleHelper.getActivities().size() > 1 && isSupport;
    }

    protected Book genCoverBook(String host, String book_id, String book_source_id, String name,
            String author, String status, String category,
            String imgUrl, String last_chapter, String chapter_count, long update_time,
            String parameter, String
            extra_parameter, int dex) {
        Book book = new Book();
        book.setStatus(status);
        book.setUpdate_date_fusion(0);
        book.setBook_id(book_id);
        book.setBook_source_id(book_source_id);
        book.setName(name);
        book.setLabel(category);
        book.setAuthor(author);
        book.setImg_url(imgUrl);
        book.setHost(host);
        book.setChapter_count(Integer.valueOf(chapter_count));

        Chapter lastChapter = new Chapter();
        lastChapter.setName(last_chapter);
        lastChapter.setUpdate_time(update_time);
        book.setLast_update_success_time(System.currentTimeMillis());
        return book;

    }

    @Override
    public void finish() {
        super.finish();
        //离线消息 跳转到主页
        if (isFromPush && ActivityLifecycleHelper.getActivities().size() <= 1) {
            startActivity(new Intent(this, SplashActivity.class));
        }
    }

    @Override
    public void onAniFinished() {
        if (selectSexDialog != null) {
            if (selectSexDialog.isShow()) {
                selectSexDialog.dismiss();
            }
        }
    }
}
