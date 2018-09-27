package com.intelligent.reader.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.ding.basic.RequestRepositoryFactory;
import com.ding.basic.bean.HotWordBean;
import com.ding.basic.bean.Result;
import com.ding.basic.bean.SearchAutoCompleteBean;
import com.ding.basic.bean.SearchCommonBeanYouHua;
import com.ding.basic.bean.SearchResult;
import com.ding.basic.net.RequestSubscriber;
import com.google.gson.Gson;
import com.intelligent.reader.R;
import com.intelligent.reader.adapter.SearchHisAdapter;
import com.intelligent.reader.adapter.SearchHotWordAdapter;
import com.intelligent.reader.adapter.SearchSuggestAdapter;
import com.intelligent.reader.search.SearchHelper;

import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.ui.widget.LoadingPage;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.Tools;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.sp.SPUtils;
import net.lzbook.kit.utils.toast.ToastUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SearchViewHelper implements SearchHelper.SearchSuggestCallBack ,SearchHisAdapter.SearchClearCallBack{
    private static String TAG = SearchViewHelper.class.getSimpleName();
    private static ArrayList<String> hotDatas = new ArrayList<String>();
    private static ArrayList<String> hisDatas = new ArrayList<String>();
    public OnHotWordClickListener onHotWordClickListener;
    private Context mContext;
    private Activity activity;
    private ViewGroup mRootLayout;
    private EditText mSearchEditText;
    private ListView mHotListView;
    private ListView mHisListView;
    private ListView mSuggestListView;
    private SearchHotWordAdapter mHotAdapter;
    private SearchSuggestAdapter mSuggestAdapter;
    private SearchHisAdapter mHisAdapter;
    private List<SearchCommonBeanYouHua> mSuggestList = new ArrayList<SearchCommonBeanYouHua>();
    private Resources mResources;
    private boolean mShouldShowHint = true;
    private OnHistoryClickListener mOnHistoryClickListener;
    private SearchHelper mSearchHelper;
    private boolean isFromEditClick = false;
    private Gson gson;
    private List<HotWordBean> hotWords = new ArrayList<>();
    private RelativeLayout relative_parent;
    private String searchType;
    private String suggest;

    private LoadingPage loadingPage;

    public SearchViewHelper(Context context, Activity activity, ViewGroup rootLayout, EditText
            searchEditText, SearchHelper searchHelper) {
        context = activity;
        mSearchHelper = searchHelper;
        init(context, activity, rootLayout, searchEditText);
    }

    private void init(Context context, Activity activity, ViewGroup rootLayout, EditText
            searchEditText) {
        gson = new Gson();
        mContext = context;
        this.activity = activity;
        mRootLayout = rootLayout;
        mSearchEditText = searchEditText;
        if (mContext != null)
            mResources = mContext.getResources();

        initHotListView();
        initHisListView();
        initSuggestListView();
        showHotList();
    }


    public void setShowHintEnabled(boolean showHint) {
        mShouldShowHint = showHint;
    }

    public void showHintList(String searchWord) {
        if (!mShouldShowHint) {
            hideHintList();
            return;
        }

        if (mRootLayout != null)
            mRootLayout.setVisibility(View.VISIBLE);

        //保证开始输入且续输入空格时显示搜索历史
        if (searchWord == null || TextUtils.isEmpty(searchWord.trim()) || isFromEditClick) {
            showHisList();
            isFromEditClick = false;
        } else {

            showSuggestList(searchWord);
        }
    }

    public void hideHintList() {
        if (mRootLayout != null)
            mRootLayout.setVisibility(View.GONE);
    }

    private void showSuggestList(String searchWord) {
        if (mHotListView != null)
            mHotListView.setVisibility(View.GONE);
        if (mHisListView != null)
            mHisListView.setVisibility(View.GONE);
        if (mSuggestListView != null)
            mSuggestListView.setVisibility(View.VISIBLE);

        // 清空上一个词的联想词结果
        if (mSuggestList != null) {
            mSuggestList.clear();
        }
        if (mSuggestAdapter != null) {
            mSuggestAdapter.notifyDataSetChanged();
        }

        if (mSearchHelper != null){
            mSearchHelper.startSearch(searchWord);
        }

    }

    private void showHisList() {
        if (mSuggestListView != null)
            mSuggestListView.setVisibility(View.GONE);
        if (mHotListView != null)
            mHotListView.setVisibility(View.GONE);
        if (mHisListView != null)
            mHisListView.setVisibility(View.VISIBLE);

        if (hisDatas != null && mContext != null)
            hisDatas.clear();
        ArrayList<String> historyWord = Tools.getHistoryWord(mContext);
        if (historyWord != null){
            hisDatas.addAll(historyWord);
        }
        if (mHisAdapter != null){
            mHisAdapter.notifyDataSetChanged();
        }
    }

    private void initHotListView() {
        mHotListView = new ListView(activity);
        if (mHotListView != null && mResources != null) {
            mHotListView.setCacheColorHint(mResources.getColor(R.color.transparent));
            mHotListView.setDivider(mResources.getDrawable(R.color.transparent));
            mHotListView.setSelector(mResources.getDrawable(R.color.transparent));
        }

        setHotHeader();

        if (NetWorkUtils.getNetWorkTypeNew(mContext).equals("无")) {
            getCacheDataFromShare(false);
        } else {

            if (loadingPage == null && mRootLayout != null && !activity.isFinishing() ){
                loadingPage = new LoadingPage(activity, mRootLayout, LoadingPage.setting_result);
            }

            getHotWords();

        }


        if (mRootLayout != null) {
            mRootLayout.addView(mHotListView);
        }
    }

    //获取热词
    public void getHotWords() {

        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchOperationV4(

                new RequestSubscriber<Result<SearchResult>>() {
                    @Override
                    public void requestResult(@Nullable Result<SearchResult> value) {
                        if (value != null && value.getData() != null) {
                            SearchResult result = value.getData();
                            SPUtils.INSTANCE.putDefaultSharedString(Constants.SERARCH_HOT_WORD_YOUHUA,
                                    gson.toJson(result, SearchResult.class));
                            parseResult(result);

                        } else {
                            getCacheDataFromShare(true);
                        }
                        if (loadingPage != null) {
                            loadingPage.onSuccess();
                        }
                    }

                    @Override
                    public void requestError(@NotNull String message) {
                        if (loadingPage != null) {
                            loadingPage.onSuccess();
                        }
                    }
                });

    }

    /**
     * if hasn't net getData from sharepreferenecs cache
     */
    public void getCacheDataFromShare(boolean hasNet){
        if (!TextUtils.isEmpty(SPUtils.INSTANCE.getDefaultSharedString(Constants.SERARCH_HOT_WORD_YOUHUA,""))) {
            String cacheHotWords = SPUtils.INSTANCE.getDefaultSharedString(Constants.SERARCH_HOT_WORD_YOUHUA,"");
            SearchResult searchResult = gson.fromJson(cacheHotWords, SearchResult.class);
            relative_parent.setVisibility(View.VISIBLE);
            parseResult(searchResult);
            AppLog.e("urlbean", cacheHotWords);
        } else {
            if(!hasNet){
                ToastUtil.INSTANCE.showToastMessage("网络不给力哦");
            }
            relative_parent.setVisibility(View.GONE);
        }
    }

    /**
     * parse result data
     */
    public void parseResult(SearchResult value) {
        hotWords.clear();
        if (value != null && value.getHotWords() != null) {
            hotWords = value.getHotWords();
            if (hotWords != null && hotWords.size() >= 0) {
                relative_parent.setVisibility(View.VISIBLE);
                mHotAdapter = new SearchHotWordAdapter(activity, hotWords);
                if (mHotListView != null) {

                    mHotListView.setAdapter(mHotAdapter);
                    mHotListView.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long position) {
                            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.b_search_click_his_word);
                            if (hotWords != null && !hotWords.isEmpty() && position > -1 && position < hotWords.size()) {
                                String hotWord = hotWords.get(arg2 - 1).getKeyword();
                                if (hotWord != null && mSearchEditText != null) {
                                    mShouldShowHint = false;
                                    mSearchEditText.setText(hotWord);
                                    startSearch(hotWord,hotWords.get(arg2-1).getKeywordType()+"");

                                    Map<String, String> data = new HashMap<>();
                                    data.put("topicword", hotWord);
                                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TOPIC, data);
                                }
                            }
                        }
                    });
                }
            } else {
                SPUtils.INSTANCE.putDefaultSharedString(Constants.SERARCH_HOT_WORD, "");
                relative_parent.setVisibility(View.GONE);
            }
        }
    }

    private void initHisListView() {
        mHisListView = new ListView(activity);
        if (mHisListView != null && mResources != null) {
            mHisListView.setCacheColorHint(mResources.getColor(R.color.transparent));
            mHisListView.setDivider(mResources.getDrawable(R.color.transparent));
            mHisListView.setSelector(R.drawable.item_selector_white);
        }

        hisDatas = Tools.getHistoryWord(mContext);
        mHisAdapter = new SearchHisAdapter(activity, hisDatas);
        mHisAdapter.setSearchClearCallBack(this);
        if (mHisListView != null) {

            mHisListView.setAdapter(mHisAdapter);
            mHisListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long position) {
                    StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.b_search_click_his_word);
                    if (hisDatas != null && !hisDatas.isEmpty() && position > -1 &&
                            position < hisDatas.size()) {
                        String history = hisDatas.get((int) position);
                        if (history != null && mSearchEditText != null) {
                            mShouldShowHint = false;
                            mSearchEditText.setText(history);
                            startSearch(history,"0");

                            Map<String, String> data = new HashMap<>();
                            data.put("keyword", history);
                            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.HISTORY, data);
                        }
                    }
                }
            });
        }

        if (mRootLayout != null) {
            mRootLayout.addView(mHisListView);
        }
    }

    @Override
    public void onSearchClear(String content) {
        StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.b_search_click_his_clear);
        clearHistoryItem(content);
    }

    private void setHotHeader() {
        View hotword_view = initHotWordView();
        if (mHotListView != null)
            mHotListView.addHeaderView(hotword_view);
    }

    private View initHotWordView() {
        View listHeader = null;
        try {
            listHeader = LayoutInflater.from(activity).inflate(R.layout
                    .search_hotword_view, null);
            relative_parent = (RelativeLayout) listHeader.findViewById(R.id.relative_parent);
        } catch (InflateException e) {
            e.printStackTrace();
        }
        return listHeader;
    }


    private void initSuggestListView() {
        if (mSearchHelper != null){
            mSearchHelper.setSearchSuggestCallBack(this);
        }

        mSuggestListView = new ListView(activity);
        if (mSuggestListView == null)
            return;
        mSuggestListView.setCacheColorHint(mResources.getColor(R.color.transparent));
        mSuggestListView.setDivider(mResources.getDrawable(R.color.transparent));
        mSuggestListView.setSelector(R.drawable.item_selector_white);
        mSuggestListView.setVisibility(View.GONE);
        if (mRootLayout != null) {
            mRootLayout.addView(mSuggestListView);
        }
        if (mSuggestList != null) {
            mSuggestList.clear();
        }
        if (mSuggestAdapter == null) {
            String inputString = "";
            if (mSearchEditText != null){
                Editable editable = mSearchEditText.getText();
                if (editable != null && editable.length() > 0){
                    inputString = editable.toString();
                }
            }
            mSuggestAdapter = new SearchSuggestAdapter(activity, mSuggestList, inputString);
        }
        mSuggestListView.setAdapter(mSuggestAdapter);
        mSuggestListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                SearchCommonBeanYouHua bean = mSuggestList.get(arg2);
                suggest = bean.getSuggest();
                searchType = "0";
                if (bean.getWordtype().equals("label")) {
                    searchType = "1";
                } else if (bean.getWordtype().equals("author")) {
                    searchType = "2";
                } else if (bean.getWordtype().equals("name")) {
                    searchType = "3";
                } else {
                    searchType = "0";
                }
                if (mSearchHelper != null) {
                    mSearchHelper.setSearchType(searchType);
                    mSearchHelper.setWord(suggest);
                    AppLog.e("typesearc", mSearchHelper.getSearchType() + suggest);
                }
                if (!TextUtils.isEmpty(suggest) && mSearchEditText != null) {
                    Map<String, String> data = new HashMap<>();
                    data.put("keyword", suggest);
                    data.put("type", searchType);
                    data.put("enterword", mSearchEditText.getText().toString().trim());
                    data.put("rank", arg2 + 1 + "");
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TIPLISTCLICK, data);

                    mShouldShowHint = false;
                    mSearchEditText.setText(suggest);
                    startSearch(suggest, searchType);
                }
            }
        });
        mSuggestListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                hideInputMethod(view);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }


    private void showHotList() {
        if (mHotListView != null)
            mHotListView.setVisibility(View.VISIBLE);
        if (mSuggestListView != null)
            mSuggestListView.setVisibility(View.GONE);
        if (mHisListView != null)
            mHisListView.setVisibility(View.GONE);
    }

    public void notifyListChanged() {
        if (mHotAdapter != null)
            mHotAdapter.notifyDataSetChanged();
    }

    public void setSearchWord(String word) {
        mShouldShowHint = false;
        if (mSearchEditText != null) {
            mSearchEditText.setText(word);
        }
        addHistoryWord(word);
    }

    private void startSearch(String searchWord,String searchType) {
        if (searchWord != null && !searchWord.equals("")) {
            addHistoryWord(searchWord);

            if (mOnHistoryClickListener != null) {
                mOnHistoryClickListener.OnHistoryClick(searchWord,searchType);
            }

        }
    }

    public void addHistoryWord(String keyword) {
        if (hisDatas == null) {
            hisDatas = new ArrayList<String>();
        }

        if (keyword == null || keyword.equals("")) {
            return;
        }

        if (hisDatas.contains(keyword)) {
            hisDatas.remove(keyword);
        }

        if (!hisDatas.contains(keyword)) {
            int size = hisDatas.size();
            if (size >= 10) {
                hisDatas.remove(size - 1);
            }
            hisDatas.add(0, keyword);
            Tools.saveHistoryWord(mContext, hisDatas);
        }
        if (mHisAdapter != null) {
            mHisAdapter.notifyDataSetChanged();
        }
    }


    private void clearHistoryItem(String item) {
        if (hisDatas != null && hisDatas.contains(item))
            hisDatas.remove(item);
        if (mHisAdapter != null)
            mHisAdapter.notifyDataSetChanged();
        Tools.saveHistoryWord(mContext, hisDatas);
    }

    @Override
    public void onSearchResult(ArrayList<SearchCommonBeanYouHua> suggestList, SearchAutoCompleteBean transmitBean) {
        if (mSuggestList == null) {
            return;
        }
        mSuggestList.clear();
        for (SearchCommonBeanYouHua item : suggestList) {
            mSuggestList.add(item);
        }
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (mSuggestAdapter != null){
                    String inputString = "";
                    if (mSearchEditText != null){
                        Editable editable = mSearchEditText.getText();
                        if (editable != null && editable.length() > 0){
                            inputString = editable.toString();
                        }
                    }
                    if (inputString != null){
                        mSuggestAdapter.setEditInput(inputString);
                    }
                    mSuggestAdapter.notifyDataSetChanged();
                }

            }
        });
    }

    public boolean isFromEditClick() {
        return isFromEditClick;
    }

    public void setFromEditClick(boolean fromEditClick) {
        isFromEditClick = fromEditClick;
    }

    public void setOnHistoryClickListener(OnHistoryClickListener listener) {
        mOnHistoryClickListener = listener;
    }

    public void onDestroy() {
        if (mContext != null) {
            mContext = null;
        }

        if (activity != null) {
            activity = null;
        }

        if (mRootLayout != null) {
            mRootLayout.removeAllViews();
            mRootLayout = null;
        }

        if (mOnHistoryClickListener != null) {
            mOnHistoryClickListener = null;
        }

        if (onHotWordClickListener != null) {
            onHotWordClickListener = null;
        }

        if (mSuggestAdapter != null) {
            mSuggestAdapter.clear();
            mSuggestAdapter = null;
        }

        if (hotDatas != null) {
            hotDatas.clear();
            hotDatas = null;
        }
        if(loadingPage != null){
            loadingPage = null;
        }

        if (mSuggestList != null) {
            mSuggestList.clear();
            mSuggestList = null;
        }
    }

    public void hideInputMethod(final View paramView) {
        if (paramView == null || paramView.getContext() == null)
            return;
        InputMethodManager imm = (InputMethodManager) paramView.getContext().getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(paramView.getApplicationWindowToken(), 0);
        }
    }

    public interface OnHotWordClickListener {
        void hotWordClick(String tag, String searchType);
    }

    public interface OnHistoryClickListener {
        void OnHistoryClick(String history, String searchType);
    }
}
