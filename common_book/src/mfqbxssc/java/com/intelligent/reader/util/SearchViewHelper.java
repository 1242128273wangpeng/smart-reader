package com.intelligent.reader.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.intelligent.reader.R;
import com.intelligent.reader.adapter.SearchHisAdapter;
import com.intelligent.reader.adapter.SearchSuggestAdapter;
import com.intelligent.reader.net.NetOwnSearch;
import com.intelligent.reader.net.OwnSearchService;
import com.intelligent.reader.search.SearchHelper;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.search.SearchCommonBean;
import net.lzbook.kit.data.search.SearchHotBean;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.SharedPreferencesUtils;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.ToastUtils;
import net.lzbook.kit.utils.Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SearchViewHelper implements SearchHelper.SearchSuggestCallBack ,SearchHisAdapter.SearchClearCallBack{
    private static String TAG = SearchViewHelper.class.getSimpleName();
    private Context mContext;
    private Activity activity;
    private ViewGroup mRootLayout;
    private EditText mSearchEditText;
    private ListView mHotListView;
    private ListView mHisListView;
    private ListView mSuggestListView;

    private ArrayAdapter<String> mHotAdapter;
    private SearchSuggestAdapter mSuggestAdapter;
    private SearchHisAdapter mHisAdapter;
    private static ArrayList<String> hotDatas = new ArrayList<String>();
    private static ArrayList<String> hisDatas = new ArrayList<String>();
    private List<SearchCommonBean> mSuggestList = new ArrayList<SearchCommonBean>();

    private Resources mResources;

    private boolean mShouldShowHint = true;

    public OnHotWordClickListener onHotWordClickListener;
    private OnHistoryClickListener mOnHistoryClickListener;
    private SearchHelper mSearchHelper;
    private boolean isFromEditClick = false;
    private Gson gson;
    private List<SearchHotBean.DataBean> hotWords = new ArrayList<>();
    private SharedPreferencesUtils sharedPreferencesUtils;
    private RelativeLayout relative_parent;
    private String searchType;
    private String suggest;

    public SearchViewHelper(Context context, Activity activity, ViewGroup rootLayout, EditText
            searchEditText, SearchHelper searchHelper) {
        context = activity;
        mSearchHelper = searchHelper;
        init(context, activity, rootLayout, searchEditText);
    }

    private void init(Context context, Activity activity, ViewGroup rootLayout, EditText
            searchEditText) {
        gson = new Gson();
        sharedPreferencesUtils = new SharedPreferencesUtils(PreferenceManager.getDefaultSharedPreferences(context));
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
            mSearchHelper.startSearchSuggestData(searchWord);
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
            AppLog.e("url", UrlUtils.BOOK_NOVEL_DEPLOY_HOST + "===" + NetWorkUtils.getNetWorkTypeNew(mContext));
            OwnSearchService searchService = NetOwnSearch.INSTANCE.getOwnSearchService();
            searchService.getHotWord()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<SearchHotBean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }
                        @Override
                        public void onNext(SearchHotBean value) {
                            AppLog.e("result", value.toString());

                            parseResult(value,true);
                        }

                        @Override
                        public void onError(Throwable e) {
                            getCacheDataFromShare(true);
                            AppLog.e("error", e.toString());
                        }

                        @Override
                        public void onComplete() {
                            AppLog.e("complete", "complete");
                        }
                    });

            AppLog.e("url", UrlUtils.BOOK_NOVEL_DEPLOY_HOST + "===" + NetWorkUtils.getNetWorkTypeNew(mContext));
        }


        if (mRootLayout != null) {
            mRootLayout.addView(mHotListView);
        }
    }

    /**
     * if hasn't net getData from sharepreferenecs cache
     */
    public void getCacheDataFromShare(boolean hasNet){
        if (!TextUtils.isEmpty(sharedPreferencesUtils.getString(Constants.SERARCH_HOT_WORD))) {
            relative_parent.setVisibility(View.VISIBLE);
            hotWords.clear();
            String cacheHotWords = sharedPreferencesUtils.getString(Constants.SERARCH_HOT_WORD);
            SearchHotBean searchHotBean = gson.fromJson(cacheHotWords, SearchHotBean.class);
            parseResult(searchHotBean,false);
            AppLog.e("urlbean", cacheHotWords);
        } else {
            if(!hasNet){
                ToastUtils.showToastNoRepeat("网络不给力哦");
            }
            relative_parent.setVisibility(View.GONE);
        }
    }

    /**
     * parse result data
     */
    public void parseResult(SearchHotBean value, boolean hasNet) {
        hotWords.clear();
        if (value != null && value.getData() != null) {
            hotWords = value.getData();
            if (hotWords != null && hotWords.size() >= 0) {
                relative_parent.setVisibility(View.VISIBLE);
                if(hasNet){
                    sharedPreferencesUtils.putString(Constants.SERARCH_HOT_WORD, gson.toJson(value, SearchHotBean.class));
                }

                hotDatas = new ArrayList<>();
                hotDatas.clear();
               for(SearchHotBean.DataBean bean:hotWords){
                   if(hotDatas.size()<9){
                       hotDatas.add(bean.getWord());
                   }
               }
                mHotAdapter = new ArrayAdapter<String>(activity, R.layout.item_hot_search_view,
                        hotDatas);
                if (mHotListView != null) {

                    mHotListView.setAdapter(mHotAdapter);
                    mHotListView.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long position) {
                            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.b_search_click_his_word);
                            if (hotDatas != null && !hotDatas.isEmpty() && position > -1 &&position < hotDatas.size()) {
                                String hotWord = hotDatas.get(arg2-1);
                                if (hotWord != null && mSearchEditText != null) {
                                    mShouldShowHint = false;
                                    mSearchEditText.setText(hotWord);
                                    startSearch(hotWord,hotWords.get(arg2-1).getWordType()+"");

                                    Map<String, String> data = new HashMap<>();
                                    data.put("topicword", hotWord);
                                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TOPIC, data);
                                }
                            }
                        }
                    });
                }
            } else {
                sharedPreferencesUtils.putString(Constants.SERARCH_HOT_WORD, "");
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
            String inputString = null;
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
                suggest = mSuggestList.get(arg2).getSuggest();
                searchType = "0";
                if (mSuggestList.get(arg2).getWordtype().equals("label")) {
                    searchType = "1";
                } else if (mSuggestList.get(arg2).getWordtype().equals("author")) {
                    searchType = "2";
                } else if (mSuggestList.get(arg2).getWordtype().equals("name")) {
                    searchType = "3";
                } else {
                    searchType = "0";
                }
                if (!TextUtils.isEmpty(suggest) && mSearchEditText != null) {
                    mShouldShowHint = false;
                    mSearchEditText.setText(suggest);

//                    mSearchEditText.setSelection(suggest.length());
                    startSearch(suggest, searchType);

                    Map<String, String> data = new HashMap<>();
                    data.put("keyword", suggest);
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TIPLISTCLICK, data);
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
    public void onSearchResult(List<SearchCommonBean>  suggestList) {
        if (mSuggestList == null){
            return;
        }
        mSuggestList.clear();
        for (SearchCommonBean item : suggestList) {

            mSuggestList.add(item);
        }
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (mSuggestAdapter != null){
                    String inputString = null;
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

    public void setFromEditClick(boolean fromEditClick) {
        isFromEditClick = fromEditClick;
    }

    public boolean isFromEditClick() {
        return isFromEditClick;
    }

    public void setOnHistoryClickListener(OnHistoryClickListener listener) {
        mOnHistoryClickListener = listener;
    }



    public interface OnHotWordClickListener {
        void hotWordClick(String tag, String searchType);
    }

    public interface OnHistoryClickListener {
        void OnHistoryClick(String history, String searchType);
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

        if (mHotAdapter != null) {
            mHotAdapter.clear();
            mHotAdapter = null;
        }

        if (mSuggestAdapter != null) {
            mSuggestAdapter.clear();
            mSuggestAdapter = null;
        }

        if (hotDatas != null) {
            hotDatas.clear();
            hotDatas = null;
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
}
