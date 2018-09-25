package com.intelligent.reader.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.intelligent.reader.R;
import com.intelligent.reader.search.SearchHelper;
import com.intelligent.reader.util.SearchViewHelper;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.base.activity.FrameActivity;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.Tools;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.router.RouterConfig;
import net.lzbook.kit.utils.toast.ToastUtil;
import net.lzbook.kit.utils.webview.CustomWebClient;
import net.lzbook.kit.utils.webview.JSInterfaceHelper;
import net.lzbook.kit.widget.HWEditText;
import net.lzbook.kit.widget.LoadingPage;

import java.util.HashMap;
import java.util.Map;



@Route(path = RouterConfig.SEARCH_BOOK_ACTIVITY)
public class SearchBookActivity extends FrameActivity implements OnClickListener, OnFocusChangeListener, SearchViewHelper.OnHistoryClickListener,
        TextWatcher, OnEditorActionListener, SearchHelper.JsCallSearchCall, SearchHelper.StartLoadCall, SearchHelper.JsNoneResultSearchCall{

    private ImageView search_result_back;
    private ImageView search_result_button;
    private RelativeLayout search_result_outcome;
    private TextView search_result_count;
    private TextView search_result_keyword;
    private RelativeLayout search_result_default;
    private ImageView search_result_clear;
    private HWEditText search_result_input;
    private RelativeLayout search_result_main;
    private WebView search_result_content;
    private FrameLayout search_result_hint;

    private SearchViewHelper searchViewHelper;
    private Handler handler = new Handler();

    private CustomWebClient customWebClient;
    private JSInterfaceHelper jsInterfaceHelper;

    boolean isSearch = false;
    //记录是否退出当前界面,for:修复退出界面时出现闪影
    boolean isBackPressed = false;

    private LoadingPage loadingPage;

    private SearchHelper mSearchHelper;
    //静态变量定义是否在在进入searchBookActivity中初始化显示上次的搜索界面
    public static  boolean isSatyHistory = false;

    public static final int isNotAuthor = 0;//不是作者
    @Override
    public void onJsSearch() {
        if (search_result_content != null) {
            search_result_content.clearView();
            if (loadingPage == null){
                loadingPage = new LoadingPage(this, search_result_main, LoadingPage.setting_result);
            }
        }
    }

    @Override
    public void onStartLoad(String url) {
        startLoading(handler, url);
        webViewCallback();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_search_book);
        } catch (InflateException e) {
            e.printStackTrace();
            return;
        }
        initData();
        initView();
        if (mSearchHelper != null && !TextUtils.isEmpty(mSearchHelper.getWord())){
            loadDataFromNet(isNotAuthor);
        }
    }

    private void initView() {
        search_result_back = (ImageView) findViewById(R.id.search_result_back);
        search_result_button = (ImageView) findViewById(R.id.search_result_button);
        search_result_outcome = (RelativeLayout) findViewById(R.id.search_result_outcome);
        if (search_result_outcome != null) {
            search_result_outcome.setVisibility(View.VISIBLE);
        }
        search_result_count = (TextView) findViewById(R.id.search_result_count);
        search_result_keyword = (TextView) findViewById(R.id.search_result_keyword);
        search_result_default = (RelativeLayout) findViewById(R.id.search_result_default);
        search_result_clear = (ImageView) findViewById(R.id.search_result_clear);
        if (search_result_clear != null) {
            search_result_clear.setVisibility(View.GONE);
        }
        search_result_input = (HWEditText) findViewById(R.id.search_result_input);
        search_result_main = (RelativeLayout) findViewById(R.id.search_result_main);
        search_result_content = (WebView) findViewById(R.id.search_result_content);

        search_result_hint = (FrameLayout) findViewById(R.id.search_result_hint);

        if (mSearchHelper == null){
            mSearchHelper = new SearchHelper(this);
        }

        if (searchViewHelper == null) {
            searchViewHelper = new SearchViewHelper( this, search_result_hint, search_result_input, mSearchHelper);
        }

        initListener();

        if (Build.VERSION.SDK_INT >= 11) {
            search_result_content.setLayerType(View.LAYER_TYPE_NONE, null);
        }

        if (search_result_content != null) {
            customWebClient = new CustomWebClient(this, search_result_content);
        }

        if (search_result_content != null && customWebClient != null) {
            customWebClient.setWebSettings();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                search_result_content.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            search_result_content.setWebViewClient(customWebClient);
        }

        if (search_result_content != null) {
            jsInterfaceHelper = new JSInterfaceHelper(this, search_result_content);
        }

        if (jsInterfaceHelper != null && search_result_content != null) {
            search_result_content.addJavascriptInterface(jsInterfaceHelper, "J_search");
            mSearchHelper.initJSHelp(jsInterfaceHelper);
        }

    }


    private void initListener() {
        if (mSearchHelper != null){
            mSearchHelper.setJsCallSearchCall(this);
            mSearchHelper.setJsNoneResultSearchCall(this);
            mSearchHelper.setStartLoadCall(this);
        }

        if (search_result_back != null) {
            search_result_back.setOnClickListener(this);
        }

        if (search_result_button != null) {
            search_result_button.setOnClickListener(this);
        }

        if (search_result_outcome != null) {
            search_result_outcome.setOnClickListener(this);
        }

        if (search_result_count != null) {
            search_result_count.setOnClickListener(this);
        }

        if (search_result_default != null) {
            search_result_default.setOnClickListener(this);
        }

        if (search_result_keyword != null) {
            search_result_keyword.setOnClickListener(this);
        }

        if (search_result_clear != null) {
            search_result_clear.setOnClickListener(this);
        }

        if (search_result_input != null) {
            search_result_input.setOnClickListener(this);
            search_result_input.setOnFocusChangeListener(this);
            search_result_input.addTextChangedListener(this);
            search_result_input.setOnEditorActionListener(this);
        }

        if (searchViewHelper != null) {
            searchViewHelper.setOnHistoryClickListener(this);
            searchViewHelper.onHotWordClickListener = new SearchViewHelper.OnHotWordClickListener() {
                @Override
                public void hotWordClick(String tag,String searchType) {
                    if (mSearchHelper != null){
                        mSearchHelper.setHotWordType(tag,searchType);
                    }
                    loadDataFromNet(isNotAuthor);
                }
            };

        }
    }

    private void initData() {
        if (mSearchHelper == null){
            mSearchHelper = new SearchHelper(this);
        }
        Intent intent = getIntent();
        if (intent != null) {
            mSearchHelper.setInitType(intent);
        }
        if (searchViewHelper != null && !TextUtils.isEmpty(mSearchHelper.getWord())) {
            searchViewHelper.setSearchWord(mSearchHelper.getWord());
        }

    }

    private void loadDataFromNet(int isAuthor) {

        if (mSearchHelper == null){
            mSearchHelper = new SearchHelper(this);
        }

        if (search_result_count != null) {
            search_result_count.setText(null);
        }


        if (!TextUtils.isEmpty(mSearchHelper.getWord())) {
            if (search_result_input != null )
                if(isAuthor !=1){
                    search_result_input.setText(mSearchHelper.getWord());
                    search_result_input.setTextColor(getResources().getColor(R.color.search_title_hint));
                }else{
                    mSearchHelper.setSearchType("2");
                    search_result_input.setText(Tools.getKeyWord());
                    search_result_input.setTextColor(getResources().getColor(R.color.search_title_hint));
                }
            if (search_result_keyword != null ) {
                if(isAuthor != 1){
                    search_result_keyword.setText(mSearchHelper.getWord());
                    search_result_keyword.setTextColor(getResources().getColor(R.color.search_title_hint));
                }else{
                    mSearchHelper.setSearchType("2");
                    search_result_keyword.setText(Tools.getKeyWord());
                    search_result_keyword.setTextColor(getResources().getColor(R.color.search_title_hint));
                }
            }

            if (searchViewHelper != null) {
                searchViewHelper.addHistoryWord(mSearchHelper.getWord());
            }

            hideSearchView();

            if (loadingPage == null){
                loadingPage = new LoadingPage(this, search_result_main, LoadingPage.setting_result);
            }

            mSearchHelper.startLoadData(isAuthor);

        } else {
            showSearchViews();
        }
    }


    private void startLoading(Handler handler, final String url) {
        if (search_result_content == null) {
            return;
        }
        search_result_main.setVisibility(View.VISIBLE);
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
        if (!TextUtils.isEmpty(url) && search_result_content != null) {
            try {
                search_result_content.loadUrl(url);
            } catch (NullPointerException e) {
                e.printStackTrace();
                this.finish();
            }
        }
    }

    private void webViewCallback() {

        if (search_result_content == null) {
            return;
        }

        if (customWebClient != null) {
            customWebClient.setStartedAction(new CustomWebClient.onStartedCallback() {
                @Override
                public void onLoadStarted(String url) {
                    AppLog.e(TAG, "onLoadStarted: " + url);
                    if (mSearchHelper == null){
                        mSearchHelper = new SearchHelper(SearchBookActivity.this);
                    }
                    mSearchHelper.setStartedAction();
                }
            });

            customWebClient.setErrorAction(new CustomWebClient.onErrorCallback() {
                @Override
                public void onErrorReceived() {
                    AppLog.e(TAG, "onErrorReceived");
                    if (loadingPage != null) {
                        AppLog.e(TAG, "loadingPage != Null");
                        loadingPage.onErrorVisable();
                    }
                }
            });

            customWebClient.setFinishedAction(new CustomWebClient.onFinishedCallback() {
                @Override
                public void onLoadFinished() {
                    AppLog.e(TAG, "onLoadFinished");
                    if (mSearchHelper == null){
                        mSearchHelper = new SearchHelper(SearchBookActivity.this);
                    }
                    mSearchHelper.onLoadFinished();
                    if (loadingPage != null) {
                        if (isSearch) {
                            hideSearchView();
                        }
                        loadingPage.onSuccessGone();
                    }
                }
            });
        }

        if (loadingPage != null) {
            loadingPage.setReloadAction(new LoadingPage.reloadCallback() {
                @Override
                public void doReload() {
                    AppLog.e(TAG, "doReload");
                    if (customWebClient != null) {
                        customWebClient.doClear();
                    }
                    search_result_content.reload();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isSatyHistory && searchViewHelper != null && searchViewHelper.getShowStatus()){
            if(mSearchHelper != null && mSearchHelper.getFromClass() != null && !mSearchHelper.getFromClass().equals("fromClass")){
                String historyDates = Tools.getKeyWord();

                if(search_result_input != null){
                    search_result_input.requestFocus();
                    search_result_input.setText(historyDates);
                    //设置光标的索引
                    Editable index = search_result_input.getText();
                    search_result_input.setSelection(index.length());
                    showSearchViews();
                }
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        if (mSearchHelper != null) {
            mSearchHelper.onDestroy();
        }
        if (search_result_content != null) {
            search_result_content.clearCache(true); //清空缓存
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (search_result_main != null) {
                    search_result_main.removeView(search_result_content);
                }
                search_result_content.stopLoading();
                search_result_content.removeAllViews();
                search_result_content.destroy();
            }else {
                search_result_content.stopLoading();
                search_result_content.removeAllViews();
                search_result_content.destroy();
                if (search_result_main != null) {
                    search_result_main.removeView(search_result_content);
                }
            }
            search_result_content = null;
        }

        if (loadingPage != null) {
            loadingPage = null;
        }

        if (searchViewHelper != null) {
            searchViewHelper.onDestroy();
            searchViewHelper = null;
        }

        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        isBackPressed = true;
        super.onBackPressed();
    }


    private void showSearchViews() {
        if (NetWorkUtils.getNetWorkType(this) == NetWorkUtils.NETWORK_NONE) {
            return;
        }
        isSearch = true;
        if (search_result_outcome != null && search_result_outcome.getVisibility() != View.GONE) {
            search_result_outcome.setVisibility(View.GONE);
        }

        if (search_result_default != null && search_result_default.getVisibility() != View.VISIBLE) {
            search_result_default.setVisibility(View.VISIBLE);
        }

        if (search_result_content != null && search_result_content.getVisibility() != View.GONE) {
            search_result_content.setVisibility(View.GONE);
        }

        if (mSearchHelper == null){
            mSearchHelper = new SearchHelper(this);
        }
        mSearchHelper.setWord(search_result_input.getText().toString());

        if (!TextUtils.isEmpty(mSearchHelper.getWord())) {

//            search_result_input.setText(word);
//            search_result_input.setSelection(word.length());

            if (search_result_hint != null) {
                search_result_hint.setVisibility(View.GONE);
            }

            if (searchViewHelper != null) {
                searchViewHelper.hideRecommendListView();
            }

            if (searchViewHelper != null) {
//                String finalContent = AppUtils.deleteAllIllegalChar(mSearchHelper.getWord());
                searchViewHelper.showHintList();
//                searchViewHelper.showRemainWords(finalContent);
            }

        } else {
            search_result_input.setText(null);
            search_result_input.getEditableText().clear();
            search_result_input.getText().clear();
        }
        search_result_input.requestFocus();
        dealSoftKeyboard(search_result_input);
    }

    private void hideSearchView() {
        isSearch = false;

        if (search_result_outcome != null && search_result_outcome.getVisibility() != View.VISIBLE && !isBackPressed) {
            search_result_outcome.setVisibility(View.VISIBLE);
        }

        if (search_result_default != null && search_result_default.getVisibility() != View.GONE) {
            search_result_default.setVisibility(View.GONE);
        }

        if (search_result_content != null && search_result_content.getVisibility() != View.VISIBLE && !isBackPressed) {
            search_result_content.setVisibility(View.VISIBLE);
        }

        if (search_result_input != null) {
            search_result_input.clearFocus();
        }

        if (searchViewHelper != null) {
            searchViewHelper.hideHintList();
        }
        if (search_result_hint != null) {
            search_result_hint.setVisibility(View.GONE);
        }
    }

    protected void backAction() {
        isBackPressed = true;
        finish();
    }

    public void dealSoftKeyboard(final View view) {
        if (handler == null) {
            handler = new Handler();
        }
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                // 弹出软键盘
                if (view != null) {
                    imm.showSoftInput(view, 0);
                }
            }
        }, 500);

    }

    public void hideInputMethod(final View paramView) {
        if (paramView == null || paramView.getContext() == null)
            return;
        InputMethodManager imm = (InputMethodManager) paramView.getContext().getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(paramView.getApplicationWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_result_back:
                Map<String, String> data1 = new HashMap<>();
                data1.put("type","1");
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BACK, data1);
                backAction();
                break;

            case R.id.search_result_clear:
                ziyougb = true;
                if (search_result_input != null)
                    search_result_input.setText(null);
                if (search_result_clear != null)
                    search_result_clear.setVisibility(View.GONE);
                dealSoftKeyboard(search_result_input);
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCHRESULT_PAGE, StartLogClickUtil.CLEAR);
                StartLogClickUtil.upLoadEventLog(this,StartLogClickUtil.SEARCH_PAGE,StartLogClickUtil.BARCLEAR);
                break;

            case R.id.search_result_outcome:
            case R.id.search_result_keyword:
            case R.id.search_result_count:
//                if (search_result_input != null)
//                    search_result_input.setSelection(search_result_input.length());
                showSearchViews();
                break;

            case R.id.search_result_default:
            case R.id.search_result_input:
                showSearchViews();
                break;

            case R.id.search_result_button:
                String keyword = null;

                if(searchViewHelper != null){
                    searchViewHelper.isFocus = false;
                }

                if (search_result_input != null) {
                    keyword = search_result_input.getText().toString();
                }
                if (keyword != null && TextUtils.isEmpty(keyword.trim())) {
                    ToastUtil.INSTANCE.showToastMessage(R.string.search_click_check_isright);
                } else {
                    hideInputMethod(search_result_input);
                    if (keyword != null && !TextUtils.isEmpty(keyword.trim()) && searchViewHelper != null) {
                        searchViewHelper.addHistoryWord(keyword);
                        if (mSearchHelper == null){
                            mSearchHelper = new SearchHelper(this);
                        }

                        if(mSearchHelper.getFromClass() != null && !mSearchHelper.getFromClass().equals("other")){
                            if(mSearchHelper.getSearchType() != null){
                                mSearchHelper.setHotWordType(keyword,mSearchHelper.getSearchType());
                                AppLog.e("type14",mSearchHelper.getSearchType()+"===");
                            }
                        }else{

                            if(mSearchHelper.getSearchType() != null && !mSearchHelper.getSearchType().equals("0")){
                                AppLog.e("type12",mSearchHelper.getSearchType()+"===");
                                mSearchHelper.setHotWordType(keyword,mSearchHelper.getSearchType());
                            }else{
                                AppLog.e("type12",0+"===");
                                mSearchHelper.setHotWordType(keyword,"0");
                                mSearchHelper.setSearchType("0");
                            }
                        }
                        loadDataFromNet(isNotAuthor);

                        Map<String, String> data = new HashMap<>();
                        data.put("type","0");
                        data.put("keyword", keyword);
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE,StartLogClickUtil.SEARCHBUTTON, data);
                    } else {
                        showSearchViews();
                    }
                }
                break;

            default:
                break;
        }
    }

    boolean ziyougb;

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (search_result_input == null)
            return;
        if (hasFocus) {
            StartLogClickUtil.upLoadEventLog(this,StartLogClickUtil.SEARCH_PAGE,StartLogClickUtil.BAR);
            dealSoftKeyboard(search_result_input);
            if (search_result_content != null && search_result_content.getVisibility() != View.GONE) {
                search_result_content.setVisibility(View.GONE);
            }

            if (searchViewHelper != null && search_result_input != null) {
                if (mSearchHelper == null) {
                    mSearchHelper = new SearchHelper(this);
                }
                if (TextUtils.isEmpty(mSearchHelper.getWord())) {
                    search_result_input.getText().clear();
                    search_result_input.getEditableText().clear();
                    search_result_input.setText(null);
                } else {
                    if (!ziyougb) {
                        search_result_input.setText(mSearchHelper.getWord());
                        search_result_input.setSelection(mSearchHelper.getWord().length());
                    }
                }
                search_result_keyword.setTextColor(getResources().getColor(R.color.search_input_text_color));
                search_result_input.setTextColor(getResources().getColor(R.color.search_input_text_color));
                //判断当用户没有对editText进行操作时（即编辑框没有内容时），显示搜索历史
                if(search_result_input.getText().toString().equals("")){
                    searchViewHelper.showHistoryList();
                }
            }
            ziyougb = true;
        } else {
            ziyougb = false;
            hideInputMethod(search_result_input);
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }


    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }


    @Override
    public void afterTextChanged(Editable s) {
        if(mSearchHelper != null && mSearchHelper.getWord() != null){
            if(mSearchHelper.getFromClass() != null){
                if(!mSearchHelper.getWord().trim().equals(s.toString().trim())){
                    AppLog.e("typ11","typ111");
                    if(!mSearchHelper.getFromClass().equals("other")){
                        AppLog.e("typ","typ");
                        mSearchHelper.setFromClass("other");
                    }
                    mSearchHelper.setSearchType("0");
                }
            }else{
                if(!mSearchHelper.getWord().trim().equals(s.toString().trim())){
                    mSearchHelper.setSearchType("0");
                }
            }
        }

        if (search_result_clear == null) {
            return;
        }

        if (!TextUtils.isEmpty(s.toString())) {
            if(search_result_input.isFocused() == true){
                search_result_clear.setVisibility(View.VISIBLE);
            }else{
                search_result_clear.setVisibility(View.GONE);
            }
        } else {
            search_result_clear.setVisibility(View.GONE);
            s.clear();
            search_result_main.setVisibility(View.GONE);
        }

        //保存用户搜索词
        Tools.setUserSearchWord(s.toString());

        //网络请求
        if (searchViewHelper != null) {
            String finalContent = AppUtils.deleteAllIllegalChar(s.toString());
            searchViewHelper.showRemainWords(finalContent);
        }
    }

    @Override
    public void OnHistoryClick(String history, String searchType,int isAuthor) {
        if (mSearchHelper == null){
            mSearchHelper = new SearchHelper(this);
        }
        mSearchHelper.setHotWordType(history, searchType);
        if("3".equals(searchType)){

        }else{
            loadDataFromNet(isAuthor);
        }
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_NEXT ||
                actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            String keyword = null;
            if (search_result_input != null) {
                keyword = search_result_input.getText().toString();
            }
            if (keyword != null && keyword.trim().equals("")) {
                ToastUtil.INSTANCE.showToastMessage(R.string.search_click_check_isright);
            } else {

                searchViewHelper.isFocus = false;
                hideInputMethod(v);
                if (keyword != null && !keyword.equals("") && searchViewHelper != null) {
                    searchViewHelper.addHistoryWord(keyword);
                    mSearchHelper.setHotWordType(keyword,"0");
                    loadDataFromNet(isNotAuthor);

                    Map<String, String> data = new HashMap<>();
                    data.put("type","1");
                    data.put("keyword", keyword);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.SEARCHBUTTON, data);
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public void onNoneResultSearch(String searchWord) {

        if (search_result_default != null && search_result_default.getVisibility() != View.VISIBLE) {
            search_result_default.setVisibility(View.VISIBLE);
        }

        if(search_result_input != null){
            search_result_input.setText(searchWord);
        }

        if(searchViewHelper != null){
            searchViewHelper.addHistoryWord(searchWord);
            searchViewHelper.hideHintList();
        }

        if (search_result_content != null) {
            search_result_content.clearView();
            if (loadingPage == null) {
                loadingPage = new LoadingPage(this, search_result_main, LoadingPage.setting_result);
            }
        }
    }
}