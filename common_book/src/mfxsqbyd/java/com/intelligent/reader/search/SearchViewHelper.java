package com.intelligent.reader.search;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.HotWordBean;
import com.ding.basic.bean.Result;
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua;
import com.ding.basic.bean.SearchCommonBeanYouHua;
import com.ding.basic.bean.SearchRecommendBook;
import com.ding.basic.bean.SearchResult;
import com.ding.basic.repository.RequestRepositoryFactory;
import com.ding.basic.request.RequestSubscriber;
import com.dingyue.bookshelf.ShelfGridLayoutManager;
import com.dingyue.contract.util.CommonUtil;
import com.google.gson.Gson;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.CoverPageActivity;
import com.intelligent.reader.view.ScrollForGridView;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.SharedPreferencesUtils;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.Tools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Function：搜索辅助类
 *
 * Created by JoannChen on 2018/5/30 0030 09:57
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
public class SearchViewHelper implements SearchHelper.SearchSuggestCallBack,
        SearchHistoryAdapter.onPositionClickListener,
        RecommendBooksAdapter.RecommendItemClickListener {

    private Context mContext;
    private Activity activity;
    private ViewGroup mRootLayout;
    private EditText mSearchEditText;

    //    private TextView tv_recommend;
    private static RelativeLayout mHistoryHeadersTitle;


    private Resources mResources;

    public OnHotWordClickListener onHotWordClickListener;
    private OnHistoryClickListener mOnHistoryClickListener;
    public Context context;
    private SearchHelper mSearchHelper;
    private String suggest;
    private String searchType;
    private SharedPreferencesUtils sharedPreferencesUtils;
    private Gson gson;

    private SearchCommonBeanYouHua searchCommonBean;
    private LinearLayout linear_root;

    //运营模块返回标识
    private boolean isBackSearch = false;
    //从标签和作者的webView页面返回是否保留焦点
    public boolean isFocus = true;
    private int isAuthor = 0;

    /**
     * 热词
     */
    private View searchHotTitleLayout;
    private ScrollForGridView mGridView;
    private List<HotWordBean> mHotWords = new ArrayList<>();
    private SearchHotWordAdapter searchHotWordAdapter;


    /**
     * 推荐
     */
    private RecyclerView mRecommendRecycleView;
    private RecommendBooksAdapter mRecommendBooksAdapter;
    private List<SearchRecommendBook.DataBean> recommendBooks = new ArrayList<>();
    private List<SearchRecommendBook.DataBean> finalRecommendBooks = new ArrayList<>();


    /**
     * 自动补全
     */
    private ListView mSuggestListView;
    private SearchSuggestAdapter mSuggestAdapter;
    private List<Object> mSuggestList = new ArrayList<>();


    /**
     * 历史记录
     */
    private ListView mHistoryListView;
    private SearchHistoryAdapter historyAdapter;
    private static ArrayList<String> historyData = new ArrayList<>();
    /**
     *
     */
    private List<Book> books = new ArrayList<>();
    private int itemGapViewCount = 0; // 用于打点 记录自动补全的type不为 书籍，作者，标签

    public SearchViewHelper(Activity activity, ViewGroup rootLayout, EditText
            searchEditText, SearchHelper searchHelper) {
        context = activity;
        mSearchHelper = searchHelper;
        init(context, activity, rootLayout, searchEditText);
    }

    private void init(Context context, Activity activity, ViewGroup rootLayout, EditText
            searchEditText) {
        gson = new Gson();
        sharedPreferencesUtils = new SharedPreferencesUtils(
                PreferenceManager.getDefaultSharedPreferences(context));
        mContext = context;
        this.activity = activity;
        mRootLayout = rootLayout;
        mSearchEditText = searchEditText;
        if (mContext != null) {
            mResources = mContext.getResources();
        }

        showSearchHistory();
        initSuggestListView();
        initHotTagView();
        initRecommendRecycleView();
        showRecommendView();
    }

    private void showRecommendView() {

        if (searchHotTitleLayout != null) {
            searchHotTitleLayout.setVisibility(View.VISIBLE);
        }
        if (mSuggestListView != null) {
            mSuggestListView.setVisibility(View.GONE);
        }
        if (mHistoryListView != null) {
            mHistoryListView.setVisibility(View.GONE);
        }

    }

    public void hideHintList() {
        if (mRootLayout != null) {
            mRootLayout.setVisibility(View.GONE);
        }
    }

    public void showHintList() {
        if (mRootLayout != null && mRootLayout.getVisibility() == View.GONE) {
            mRootLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 返回isFocus 和 isBackSearch 的值，以此来确定searchBookActivity页面显示的模块
     */
    public boolean getShowStatus() {
        if (!isBackSearch && isFocus) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 对searchBookActivity提供控制隐藏显示搜索框下面的内容
     */
    public void showRemainWords(String searchWord) {

        //当搜索词为空是显示搜索历史界面
        if (searchWord != null && "".equals(searchWord) || TextUtils.isEmpty(searchWord.trim())) {
            showHistoryList();
        } else {
            showSuggestList(searchWord);
        }
    }

    private void showSuggestList(String searchWord) {
        if (mRootLayout.getVisibility() == View.GONE) {
            mRootLayout.setVisibility(View.VISIBLE);
        }
        if (mSuggestListView != null) {
            mSuggestListView.setVisibility(View.VISIBLE);
        }
        if (mHistoryListView != null) {
            mHistoryListView.setVisibility(View.GONE);
        }
        if (searchHotTitleLayout != null) {
            searchHotTitleLayout.setVisibility(View.GONE);
        }

        // 清空上一个词的联想词结果
        if (mSuggestList != null) {
            mSuggestList.clear();
        }
        if (mSuggestAdapter != null) {
            mSuggestAdapter.notifyDataSetChanged();
        }

        if (TextUtils.isEmpty(searchWord)) {
            showHistoryList();
        }

        if (mSearchHelper != null) {
            mSearchHelper.startSearch(searchWord);
        }

    }

    private void initHistoryMain(Context context) {
        mHistoryListView = new ListView(context);
        if (mResources != null) {
            mHistoryListView.setCacheColorHint(mResources.getColor(R.color.transparent));
            mHistoryListView.setDivider(mResources.getDrawable(R.color.transparent));
            mHistoryListView.setSelector(R.drawable.item_selector_white);
            TextView historyText = new TextView(context);
            historyText.setText("搜索历史");
            historyText.setTextSize(14);
            historyText.setTextColor(ContextCompat.getColor(context, R.color.font_gray));
            historyText.setPadding(AppUtils.dip2px(context, 16), AppUtils.dip2px(context, 16), 0,
                    0);
            mHistoryListView.addHeaderView(historyText);
        }
    }


    /**
     * 对外提供一个操作mRecommendListView隐藏显示的方法
     */
    public void hideRecommendListView() {
        if (searchHotTitleLayout != null && searchHotTitleLayout.getVisibility() == View.VISIBLE) {
            searchHotTitleLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 搜索历史单独抽离成一个页面
     */
    public void showSearchHistory() {

        if (searchHotTitleLayout != null && searchHotTitleLayout.getVisibility() == View.VISIBLE) {
            searchHotTitleLayout.setVisibility(View.GONE);
        }

        //初始化搜索历史的ListView
        initHistoryMain(mContext);

        historyData = Tools.getHistoryWord(mContext);
        historyAdapter = new SearchHistoryAdapter(mContext, historyData);
        if (historyAdapter != null) {
            historyAdapter.setPositionClickListener(this);
        }

        if (mHistoryListView != null) {

            mHistoryListView.setAdapter(historyAdapter);
            mHistoryListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long position) {
                    StatServiceUtils.statAppBtnClick(context,
                            StatServiceUtils.b_search_click_his_word);
                    if (historyData != null && !historyData.isEmpty() && position > -1 &&
                            position < historyData.size()) {
                        String history = historyData.get((int) position);
                        if (history != null && mSearchEditText != null) {
                            mSearchEditText.setText(history);
//                            mSearchEditText.setSelection(history.length());
                            isFocus = false;
                            startSearch(history, "0", 0);

                            Map<String, String> data = new HashMap<>();
                            data.put("keyword", history);
                            data.put("rank", position + "");
                            StartLogClickUtil.upLoadEventLog(activity,
                                    StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BARLIST, data);
                        }
                    }
                }
            });
        }

        setHistoryHeadersTitleView();
        if (mRootLayout != null) {
            mRootLayout.addView(mHistoryListView);
        }

    }

    private void initHotTagView() {
        searchHotTitleLayout = View.inflate(mContext, R.layout.search_hot_title_layout, null);

        linear_root = (LinearLayout) searchHotTitleLayout.findViewById(R.id.linear_root);
        mGridView = (ScrollForGridView) searchHotTitleLayout.findViewById(R.id.grid);

        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StatServiceUtils.statAppBtnClick(context,
                        StatServiceUtils.b_search_click_allhotword);
                HotWordBean hotWord = mHotWords.get(position);
                Map<String, String> data = new HashMap<>();
                data.put("topicword", hotWord.getKeyword());
                data.put("rank", String.valueOf(hotWord.getSort()));
                data.put("type", hotWord.getSuperscript());
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE,
                        StartLogClickUtil.TOPIC, data);

                if (mSearchEditText != null) {
                    mSearchEditText.setText(hotWord.getKeyword());
                }

                isFocus = false;
                if (onHotWordClickListener != null) {
                    onHotWordClickListener.hotWordClick(hotWord.getKeyword(),
                            mHotWords.get(position).getKeywordType() + "");
                }
            }
        });
//        return searchHotTitleLayout;
    }


    private void initRecommendRecycleView() {

        mRecommendRecycleView = (RecyclerView) searchHotTitleLayout.findViewById(
                R.id.list_recommed);
        mRecommendRecycleView.getRecycledViewPool().setMaxRecycledViews(0, 12);

        ShelfGridLayoutManager layoutManager = new ShelfGridLayoutManager(mContext, 4);

        mRecommendRecycleView.setLayoutManager(layoutManager);
        mRecommendRecycleView.getItemAnimator().setAddDuration(0);
        mRecommendRecycleView.getItemAnimator().setChangeDuration(0);
        mRecommendRecycleView.getItemAnimator().setMoveDuration(0);
        mRecommendRecycleView.getItemAnimator().setRemoveDuration(0);
        ((SimpleItemAnimator) mRecommendRecycleView.getItemAnimator()).setSupportsChangeAnimations(
                false);

        if (NetWorkUtils.getNetWorkTypeNew(context).equals("无")) {
//            getCacheDataFromShare(false);
//            getRecommendBooksFromCache();
            linear_root.setVisibility(View.GONE);
        } else {
            getHotWords();
            getRecommendData();
        }

        if (mRootLayout != null) {
            mRootLayout.addView(searchHotTitleLayout);
        }

    }

    /**
     * 【解析】获取热词
     */
    private void getHotWords() {
        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchOperationV4(

                new RequestSubscriber<Result<SearchResult>>() {
                    @Override
                    public void requestResult(@Nullable Result<SearchResult> value) {
                        if (value != null && value.getData() != null) {
                            linear_root.setVisibility(View.VISIBLE);
                            SearchResult result = value.getData();
                            sharedPreferencesUtils.putString(Constants.SERARCH_HOT_WORD_YOUHUA,
                                    gson.toJson(result, SearchResult.class));
                            initHotWordList(result);

                        } else {
                            getCacheDataFromShare(true);
                        }
                    }

                    @Override
                    public void requestError(@NotNull String message) {
                        getCacheDataFromShare(true);
                    }
                });

    }

    /**
     * 【解析】获取推荐书籍
     */
    private void getRecommendData() {

        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchRecommend(
                getBookOnLineIds(), new RequestSubscriber<SearchRecommendBook>() {
                    @Override
                    public void requestResult(@Nullable SearchRecommendBook value) {
                        if (value != null && value.getData() != null) {
                            recommendBooks.clear();
                            recommendBooks = value.getData();
                            initRecommendList();
                        }
                    }

                    @Override
                    public void requestError(@NotNull String message) {
                    }
                });

    }

    /**
     * 获取书架上的书Id
     */
    private String getBookOnLineIds() {
        books.clear();
        books = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).loadBooks();
        StringBuilder sb = new StringBuilder();
        if (books != null && books.size() > 0) {
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                sb.append(book.getBook_id());
                sb.append((i == books.size() - 1) ? "" : ",");
            }
            return sb.toString();
        }
        return "";
    }



    private void getCacheDataFromShare(boolean hasNet) {
        if (!TextUtils.isEmpty(
                sharedPreferencesUtils.getString(Constants.SERARCH_HOT_WORD_YOUHUA))) {
            String cacheHotWords = sharedPreferencesUtils.getString(
                    Constants.SERARCH_HOT_WORD_YOUHUA);
            SearchResult searchResult = gson.fromJson(cacheHotWords, SearchResult.class);
            if (searchResult != null) {
                linear_root.setVisibility(View.VISIBLE);
                initHotWordList(searchResult);
            } else {
                linear_root.setVisibility(View.GONE);
            }

        } else {
            if (!hasNet) {
                CommonUtil.showToastMessage("网络不给力哦");
            }
            linear_root.setVisibility(View.GONE);
        }
    }


    private void initHotWordList(SearchResult value) {
        mHotWords.clear();
        mHotWords = value.getHotWords();

        if (searchHotWordAdapter == null) {
            searchHotWordAdapter = new SearchHotWordAdapter(activity, mHotWords);
            mGridView.setAdapter(searchHotWordAdapter);
        } else {
            searchHotWordAdapter.setData(mHotWords);
            searchHotWordAdapter.notifyDataSetChanged();
        }
    }

    private synchronized void initRecommendList() {

        finalRecommendBooks.clear();
        for (int i = 0; i < 8; i++) {
            if (i < recommendBooks.size()) {
                finalRecommendBooks.add(recommendBooks.get(i));
            }
        }
        if (mRecommendBooksAdapter == null) {
            mRecommendBooksAdapter = new RecommendBooksAdapter(mContext, SearchViewHelper.this,
                    finalRecommendBooks);
            mRecommendRecycleView.setAdapter(mRecommendBooksAdapter);
        } else {
            mRecommendBooksAdapter.notifyDataSetChanged();
        }


    }

    private void initSuggestListView() {

        if (searchHotTitleLayout != null && searchHotTitleLayout.getVisibility() == View.VISIBLE) {
            searchHotTitleLayout.setVisibility(View.GONE);
        }

        if (mSearchHelper != null) {
            mSearchHelper.setSearchSuggestCallBack(this);
        }

        mSuggestListView = new ListView(activity);

        mSuggestListView.setCacheColorHint(mResources.getColor(R.color.transparent));
        mSuggestListView.setDivider(mResources.getDrawable(R.color.transparent));
        mSuggestListView.setDividerHeight(AppUtils.dip2px(mContext, 0.5f));
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
            if (mSearchEditText != null) {
                Editable editable = mSearchEditText.getText();
                if (editable != null && editable.length() > 0) {
                    inputString = editable.toString();
                }
            }
            mSuggestAdapter = new SearchSuggestAdapter(activity, mSuggestList, inputString);
        }
        mSuggestListView.setAdapter(mSuggestAdapter);
        mSuggestListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Object obj = mSuggestList.get(arg2);
                if (obj instanceof SearchCommonBeanYouHua) {
                    searchCommonBean = (SearchCommonBeanYouHua) obj;
                } else {
                    return;
                }
                suggest = searchCommonBean.getSuggest();
                searchType = "0";
                isAuthor = 0;
                Map<String, String> data = new HashMap<>();

                if (!TextUtils.isEmpty(suggest) && mSearchEditText != null && mSuggestList != null) {
                    itemGapViewCount = 0;
                    data.put("keyword", suggest);
                    data.put("type", searchType);
                    data.put("enterword", mSearchEditText.getText().toString().trim());
                    for(int i = 0; i< arg2 ; i++){
                        if(!(mSuggestList.get(i) instanceof SearchCommonBeanYouHua)){
                            itemGapViewCount++;
                        }
                    }
                    data.put("rank", (arg2 + 1 - itemGapViewCount)+"");
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE,
                            StartLogClickUtil.TIPLISTCLICK, data);
                }

                if (searchCommonBean.getWordtype().equals("label")) {
                    searchType = "1";
                    isAuthor = 0;
                    isFocus = false;
                } else if (searchCommonBean.getWordtype().equals("author")) {
                    searchType = "2";
                    isAuthor = searchCommonBean.getIsAuthor();
                    isBackSearch = false;
                    isFocus = true;
                    addHistoryWord(suggest);
                } else if (searchCommonBean.getWordtype().equals("name")) {
                    searchType = "3";

                    isFocus = true;
                    isBackSearch = false;
                    isAuthor = 0;
                    data.put("bookid", ((SearchCommonBeanYouHua) mSuggestList.get(
                            arg2)).getBook_id());

                    //统计进入到书籍封面页
                    Map<String, String> data1 = new HashMap<>();
                    data1.put("BOOKID", ((SearchCommonBeanYouHua) mSuggestList.get(
                            arg2)).getBook_id());
                    data1.put("source", "SEARCH");
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE,
                            StartLogClickUtil.ENTER, data1);

                    Intent intent = new Intent();
                    intent.setClass(context, CoverPageActivity.class);
                    Bundle bundle = new Bundle();

                    bundle.putString("author", searchCommonBean.getAuthor());
                    bundle.putString("book_id", searchCommonBean.getBook_id());
                    bundle.putString("book_source_id", searchCommonBean.getBook_source_id());

                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                    addHistoryWord(suggest);
                } else {
                    searchType = "0";
                    isAuthor = 0;
                }


                if (mSearchEditText != null && !searchType.equals("3")) {
                    startSearch(suggest, searchType, isAuthor);


                }
            }
        });
        mSuggestListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                hideInputMethod(view);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {

            }
        });
    }


    public void showHistoryList() {

        if (mSuggestListView != null) {
            mSuggestListView.setVisibility(View.GONE);
        }
        if (searchHotTitleLayout != null) {
            searchHotTitleLayout.setVisibility(View.GONE);
        }
        if (mHistoryListView != null) {
            mHistoryListView.setVisibility(View.VISIBLE);
        }

        if (historyData != null && mContext != null) {
            historyData.clear();
            ArrayList<String> historyWord = Tools.getHistoryWord(mContext);
            if (historyWord != null) {
                historyData.addAll(historyWord);
            }
            if (historyAdapter != null) {
                historyAdapter.notifyDataSetChanged();
            }
        }
    }

    private static void setHistoryHeadersTitleView() {
        if (mHistoryHeadersTitle == null) {
            return;
        }
        if (historyData != null && historyData.size() != 0) {
            mHistoryHeadersTitle.setVisibility(View.VISIBLE);
        } else {
            mHistoryHeadersTitle.setVisibility(View.INVISIBLE);
        }
    }

    public void setSearchWord(String word) {
        if (mSearchEditText != null) {
            mSearchEditText.setText(word);
        }
        addHistoryWord(word);
    }

    private void startSearch(String searchWord, String searchType, int isAuthor) {
        if (searchWord != null && !searchWord.equals("")) {
            addHistoryWord(searchWord);

            if (mOnHistoryClickListener != null) {
                mOnHistoryClickListener.OnHistoryClick(searchWord, searchType, isAuthor);
            }
        }
    }

    public void addHistoryWord(String keyword) {
        if (historyData == null) {
            historyData = new ArrayList<>();
        }

        if (keyword == null || keyword.equals("")) {
            return;
        }
        if (historyData.contains(keyword)) {
            historyData.remove(keyword);
        }

        if (!historyData.contains(keyword)) {
            int size = historyData.size();
            if (size >= 30) {
                historyData.remove(size - 1);
            }
            historyData.add(0, keyword);
            Tools.saveHistoryWord(mContext, historyData);
        }
        if (historyAdapter != null) {
            historyAdapter.notifyDataSetChanged();
        }
        setHistoryHeadersTitleView();
    }


    private void clearHistory(int index) {
        if (historyData != null && index < historyData.size()) {
            historyData.remove(index);
        }
        setHistoryHeadersTitleView();
        if (historyAdapter != null) {
            historyAdapter.notifyDataSetChanged();
        }
        Tools.saveHistoryWord(mContext, historyData);
    }

    private void result(List<SearchCommonBeanYouHua> result) {
        if (mSuggestList == null) {
            return;
        }
        mSuggestList.clear();
        int index = 0;
        for (SearchCommonBeanYouHua item : result) {
            if (index > 4) // 只显示5个
            {
                break;
            }

            mSuggestList.add(item);
            index++;
        }
        String inputString = "";
        if (mSearchEditText != null) {
            Editable editable = mSearchEditText.getText();
            if (editable != null && editable.length() > 0) {
                inputString = editable.toString();
            }
        }
        if (mSuggestAdapter != null) {
            if (inputString != null) {
                mSuggestAdapter.setEditInput(inputString);
            }
            mSuggestAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onSearchResult(List<Object> suggestList,
            SearchAutoCompleteBeanYouHua transmitBean) {
        if (mSuggestList == null) {
            return;
        }
        mSuggestList.clear();
        for (Object item : suggestList) {
            mSuggestList.add(item);
        }
        if (mSearchHandler == null) {
            return;
        }
        mSearchHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSuggestAdapter != null) {
                    String inputString = "";
                    if (mSearchEditText != null) {
                        Editable editable = mSearchEditText.getText();
                        if (editable != null && editable.length() > 0) {
                            inputString = editable.toString();
                        }
                    }
                    if (inputString != null) {
                        mSuggestAdapter.setEditInput(inputString);
                    }
                    mSuggestAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onItemClickListener(int position) {
//        ToastUtils.showToastNoRepeat(""+position);
        if (mSearchHandler != null) {
            Message message = mSearchHandler.obtainMessage();
            message.arg1 = position;
            message.what = 10;
            mSearchHandler.handleMessage(message);
        }
    }

    @Override
    public void onItemClick(View view, int position) {


        SearchRecommendBook.DataBean dataBean = recommendBooks.get(position);
        Map<String, String> data = new HashMap<>();
        data.put("rank", position + 1 + "");
        data.put("type", "1");
        data.put("bookid", dataBean.getBookId());
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE,
                StartLogClickUtil.HOTREADCLICK, data);

        Intent intent = new Intent();
        intent.setClass(context, CoverPageActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString("author", dataBean.getAuthorName());
        bundle.putString("book_id", dataBean.getBookId());
        bundle.putString("book_source_id", "");
        bundle.putString("book_chapter_id", dataBean.getBookChapterId());
        intent.putExtras(bundle);
        mContext.startActivity(intent);

        isBackSearch = true;
        isFocus = true;
    }

    static class SearchHandler extends Handler {
        private WeakReference<SearchViewHelper> reference;

        SearchHandler(SearchViewHelper helper) {
            reference = new WeakReference<>(helper);
        }

        public void handleMessage(Message msg) {
            SearchViewHelper helper = reference.get();
            if (helper == null) {
                return;
            }
            switch (msg.what) {
                case 10:
                    helper.clearHistory(msg.arg1);
                    break;

                case 20:
                    helper.result((ArrayList<SearchCommonBeanYouHua>) msg.obj);
                    break;

                default:
                    break;
            }
        }
    }

    private final Handler mSearchHandler = new SearchHandler(this);


    public void setOnHistoryClickListener(
            SearchViewHelper.OnHistoryClickListener listener) {
        mOnHistoryClickListener = listener;
    }

    public interface OnHotWordClickListener {
        void hotWordClick(String tag, String searchType);
    }

    public interface OnHistoryClickListener {
        void OnHistoryClick(String history, String searchType, int isAuthor);
    }

    public void clear() {
        if (mSearchEditText != null) {
            mSearchEditText.getText().clear();
            mSearchEditText.getText().clearSpans();
            mSearchEditText.getEditableText().clearSpans();
            mSearchEditText.setText(null);
            mSearchEditText.getEditableText().clear();
            mSearchEditText.clearFocus();
        }
        if (mSuggestList != null) {
            mSuggestList.clear();
            mSuggestList = null;
        }

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

        if (mHistoryHeadersTitle != null) {
            mHistoryHeadersTitle.removeAllViews();
            mHistoryHeadersTitle = null;
        }

        if (historyAdapter != null) {
            historyAdapter = null;
        }

        if (mSuggestAdapter != null) {
            mSuggestAdapter.clear();
            mSuggestAdapter = null;
        }

        if (historyData != null) {
            historyData.clear();
            historyData = null;
        }

        if (mSuggestList != null) {
            mSuggestList.clear();
            mSuggestList = null;
        }
        clear();
    }

    public void hideInputMethod(final View paramView) {
        if (paramView == null || paramView.getContext() == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) paramView.getContext().getSystemService(
                INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(paramView.getApplicationWindowToken(), 0);
        }
    }
}
