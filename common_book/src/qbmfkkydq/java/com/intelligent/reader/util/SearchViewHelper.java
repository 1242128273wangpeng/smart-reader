package com.intelligent.reader.util;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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

import com.ding.basic.RequestRepositoryFactory;
import com.ding.basic.bean.Book;
import com.ding.basic.bean.HotWordBean;
import com.ding.basic.bean.Result;
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua;
import com.ding.basic.bean.SearchCommonBeanYouHua;
import com.ding.basic.bean.SearchRecommendBook;
import com.ding.basic.bean.SearchResult;
import com.ding.basic.net.RequestSubscriber;
import com.dingyue.bookshelf.ShelfGridLayoutManager;
import com.dingyue.contract.util.CommonUtil;
import com.google.gson.Gson;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.CoverPageActivity;
import com.intelligent.reader.adapter.RecommendBooksAdapter;
import com.intelligent.reader.adapter.SearchHistoryAdapter;
import com.intelligent.reader.adapter.SearchHotWordAdapter;
import com.intelligent.reader.adapter.SearchSuggestAdapter;
import com.intelligent.reader.search.SearchHelper;
import com.intelligent.reader.view.ScrollForGridView;
import com.intelligent.reader.widget.ConfirmDialog;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.constants.Constants;
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

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class SearchViewHelper implements SearchHelper.SearchSuggestCallBack,
        SearchHistoryAdapter.OnPositionClickListener
        , RecommendBooksAdapter.RecommendItemClickListener {
    private Context mContext;
    private Activity activity;
    private ViewGroup mRootLayout;
    private RelativeLayout mRecommendRLayout;


    /**
     * 热词
     */
    private View mSearchHotTitleLayout;
    private List<HotWordBean> mHotWordList;
    private ScrollForGridView mHotWordGridView;
    private SearchHotWordAdapter mSearchHotWordAdapter;

    /**
     * 推荐模块
     */
    private RecyclerView mRecommendRecycleView;
    private RecommendBooksAdapter mRecommendBooksAdapter;
    private List<SearchRecommendBook.DataBean> mRecommendBooks = new ArrayList<>();
    private List<SearchRecommendBook.DataBean> mRecommendFinalBooks = new ArrayList<>();


    /**
     * 自动补全
     */
    private String suggest;
    private EditText mSearchEditText;
    private ListView mSuggestListView;
    private SearchSuggestAdapter mSuggestAdapter;
    private List<Object> mSuggestList = new ArrayList<>();

    /**
     * 历史记录
     */
    private View mHistoryDeleteView;
    private ListView mHistoryListView;
    private SearchHistoryAdapter mHistoryAdapter;
    private static ArrayList<String> mHistoryList = new ArrayList<>();
    private static RelativeLayout mHistoryHeadersTitle;


    public OnHotWordClickListener onHotWordClickListener;
    private OnHistoryClickListener mOnHistoryClickListener;

    private Resources mResources;

    private boolean mShouldShowHint = true;

    public Context context;
    private SearchHelper mSearchHelper;


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


    private int count = 0;//用于标识换一换次数
    private List<Book> books = new ArrayList<>();
    private LoadingPage loadingPage;
    private TextView tv_search_title;


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
        initRecommendView();
        showRecommendView();
    }

    private void showRecommendView() {

        if (mSearchHotTitleLayout != null) {
            mSearchHotTitleLayout.setVisibility(View.VISIBLE);
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

        //当搜索次为空是显示搜索历史界面
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
        if (mSearchHotTitleLayout != null) {
            mSearchHotTitleLayout.setVisibility(View.GONE);
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
        if (mHistoryListView != null && mResources != null) {
            mHistoryListView.setCacheColorHint(mResources.getColor(R.color.transparent));
            mHistoryListView.setDivider(mResources.getDrawable(R.color.transparent));
            mHistoryListView.setSelector(R.drawable.item_selector_white);
        }
    }

    private void initRecommendView() {

        setHotTagList();
        if (mRootLayout != null) {
            mRootLayout.addView(mSearchHotTitleLayout);
        }
    }


    /**
     * 对外提供一个操作mRecommendListView隐藏显示的方法
     */
    public void hideRecommendListView() {
        if (mSearchHotTitleLayout != null
                && mSearchHotTitleLayout.getVisibility() == View.VISIBLE) {
            mSearchHotTitleLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 搜索历史单独抽离成一个页面
     */
    public void showSearchHistory() {

        if (mSearchHotTitleLayout != null
                && mSearchHotTitleLayout.getVisibility() == View.VISIBLE) {
            mSearchHotTitleLayout.setVisibility(View.GONE);
        }

        //初始化搜索历史的ListView
        initHistoryMain(mContext);

        mHistoryList = Tools.getHistoryWord(mContext);
        mHistoryAdapter = new SearchHistoryAdapter(mContext, mHistoryList);
        if (mHistoryAdapter != null) {
            mHistoryAdapter.setPositionClickListener(this);
        }

        if (mHistoryListView != null) {

            mHistoryListView.setAdapter(mHistoryAdapter);
            mHistoryListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long position) {
                    StatServiceUtils.statAppBtnClick(context,
                            StatServiceUtils.b_search_click_his_word);
                    if (mHistoryList != null && !mHistoryList.isEmpty() && position > -1 &&
                            position < mHistoryList.size()) {
                        String history = mHistoryList.get((int) position);
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

        mHistoryDeleteView = View.inflate(context, R.layout.item_search_history_delete_layout,
                null);
        mHistoryDeleteView.setVisibility(mHistoryList.size() > 0 ? View.VISIBLE : View.GONE);
        mHistoryDeleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatServiceUtils.statAppBtnClick(context,
                        StatServiceUtils.b_search_click_his_clear);
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SEARCH,
                        StartLogClickUtil.BARCLEAR);
                showClearHistoryDialog();

            }
        });
        mHistoryListView.addFooterView(mHistoryDeleteView);


        setHistoryHeadersTitleView();
        if (mRootLayout != null) {
            mRootLayout.addView(mHistoryListView);
        }

    }


    private void initHotTagView() {
        mSearchHotTitleLayout = View.inflate(mContext, R.layout.search_hot_title_layout, null);
        tv_search_title = mSearchHotTitleLayout.findViewById(R.id.tv_search_title);
        linear_root = mSearchHotTitleLayout.findViewById(R.id.linear_root);

        mHotWordGridView = mSearchHotTitleLayout.findViewById(R.id.grid_hot_word);

        mHotWordGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StatServiceUtils.statAppBtnClick(context,
                        StatServiceUtils.b_search_click_allhotword);
                HotWordBean hotWord = mHotWordList.get(position);
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
                            mHotWordList.get(position).getKeywordType() + "");
                }
            }
        });

    }


    private void setHotTagList() {
        initHotTagView();

        mRecommendRecycleView = mSearchHotTitleLayout.findViewById(R.id.list_recommed);
        mRecommendRLayout = mSearchHotTitleLayout.findViewById(R.id.rl_recommend);
        TextView tv_change = mSearchHotTitleLayout.findViewById(R.id.tv_change);
        tv_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count += 8;
                if (count >= 24) {
                    count = 0;
                }
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE,
                        StartLogClickUtil.HOTREADCHANGE);
                initRecycleView(count);
            }
        });


        if (mRecommendRecycleView != null) {
            mRecommendRecycleView.setVisibility(View.VISIBLE);

            mRecommendRecycleView.getRecycledViewPool().setMaxRecycledViews(0, 12);
            ShelfGridLayoutManager layoutManager = new ShelfGridLayoutManager(mContext, 4);

            mRecommendRecycleView.setLayoutManager(layoutManager);
            mRecommendRecycleView.setNestedScrollingEnabled(false);
            mRecommendRecycleView.getItemAnimator().setAddDuration(0);
            mRecommendRecycleView.getItemAnimator().setChangeDuration(0);
            mRecommendRecycleView.getItemAnimator().setMoveDuration(0);
            mRecommendRecycleView.getItemAnimator().setRemoveDuration(0);
            ((SimpleItemAnimator) mRecommendRecycleView.getItemAnimator())
                    .setSupportsChangeAnimations(
                            false);


        }

        if (NetWorkUtils.getNetWorkTypeNew(mContext).equals("无")) {
//            getCacheDataFromShare(false);
//            getRecommendBooksFromCache();
            linear_root.setVisibility(View.GONE);
        } else {
            if (loadingPage == null && mRootLayout != null && !activity.isFinishing()) {
                loadingPage = new LoadingPage(activity, mRootLayout, LoadingPage.setting_result);
            }
            getHotWords();
            getRecommendData();
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
                            linear_root.setVisibility(View.VISIBLE);
                            SearchResult result = value.getData();
                            sharedPreferencesUtils.putString(Constants.SERARCH_HOT_WORD_YOUHUA,
                                    gson.toJson(result, SearchResult.class));
                            setHotWordList(result);

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

    //获取推荐书籍
    private void getRecommendData() {


        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchRecommend(

                getBookOnLineIds(), new RequestSubscriber<SearchRecommendBook>() {
                    @Override
                    public void requestResult(@Nullable SearchRecommendBook value) {
                        if (value != null && value.getData() != null) {
                            mRecommendBooks.clear();
                            mRecommendBooks = value.getData();
                            mRecommendRLayout.setVisibility(View.VISIBLE);
                            initRecycleView(count);

//                            if (mBookDaoHelper == null) {
//                                mBookDaoHelper = BookDaoHelper.getInstance();
//                            }
//                            mBookDaoHelper.insertSearchBook(mRecommendBooks);


                        } else {
//                            getRecommendBooksFromCache();
                        }
                    }

                    @Override
                    public void requestError(@NotNull String message) {
//                        getRecommendBooksFromCache();
                    }
                });


    }

    /**
     * 获取书架上的书Id
     */

    public String getBookOnLineIds() {
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


//    //获取本地存储的推荐书籍
//    public void getRecommendBooksFromCache() {
//        count = 0;
//        mRecommendBooks.clear();
//        if (mBookDaoHelper == null) {
//            mBookDaoHelper = BookDaoHelper.getInstance();
//        }
//        mRecommendBooks = mBookDaoHelper.getSearchBooks();
//        if (mRecommendBooks != null && mRecommendBooks.size() > 0) {
//            mRecommendRLayout.setVisibility(View.VISIBLE);
//            initRecycleView(count);
//        } else {
//            mRecommendRLayout.setVisibility(View.GONE);
//        }
//
//    }

    public synchronized void initRecycleView(int bookCount) {
        mRecommendFinalBooks.clear();
        for (int i = bookCount; i < bookCount + 8; i++) {
            if (i < mRecommendBooks.size()) {
                mRecommendFinalBooks.add(mRecommendBooks.get(i));
            }
        }
        if (mRecommendBooksAdapter == null) {
            mRecommendBooksAdapter = new RecommendBooksAdapter(mContext, SearchViewHelper.this,
                    mRecommendFinalBooks);
            mRecommendRecycleView.setAdapter(mRecommendBooksAdapter);
        } else {
            mRecommendBooksAdapter.notifyDataSetChanged();
        }

    }

    /**
     * if hasn't net getHotWord from sharepreferenecs cache
     */
    public void getCacheDataFromShare(boolean hasNet) {
        if (!TextUtils.isEmpty(
                sharedPreferencesUtils.getString(Constants.SERARCH_HOT_WORD_YOUHUA))) {
            String cacheHotWords = sharedPreferencesUtils.getString(
                    Constants.SERARCH_HOT_WORD_YOUHUA);
            SearchResult searchResult = gson.fromJson(cacheHotWords, SearchResult.class);
            if (searchResult != null) {
                linear_root.setVisibility(View.VISIBLE);
                setHotWordList(searchResult);
            } else {
                tv_search_title.setVisibility(View.GONE);
                linear_root.setVisibility(View.GONE);
            }

        } else {
            if (!hasNet) {
                CommonUtil.showToastMessage("网络不给力哦");
            }
            tv_search_title.setVisibility(View.GONE);
            linear_root.setVisibility(View.GONE);
        }
    }


    /**
     * 展示热词列表
     *
     * @param result 搜索结果
     */
    private void setHotWordList(SearchResult result) {
        mHotWordList = result.getHotWords();

        if (mSearchHotWordAdapter == null) {
            mSearchHotWordAdapter = new SearchHotWordAdapter(activity, mHotWordList);
            mHotWordGridView.setAdapter(mSearchHotWordAdapter);
        } else {
            mSearchHotWordAdapter.setData(mHotWordList);
            mSearchHotWordAdapter.notifyDataSetChanged();
        }


    }

    /**
     * 展示自动补全列表
     */
    private void setSuggestList() {

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

    }

    private void initSuggestListView() {

        if (mSearchHotTitleLayout != null
                && mSearchHotTitleLayout.getVisibility() == View.VISIBLE) {
            mSearchHotTitleLayout.setVisibility(View.GONE);
        }

        if (mSearchHelper != null) {
            mSearchHelper.setSearchSuggestCallBack(this);
        }

        mSuggestListView = new ListView(activity);
        if (mSuggestListView == null) {
            return;
        }
        mSuggestListView.setCacheColorHint(mResources.getColor(R.color.transparent));
        mSuggestListView.setDivider(mResources.getDrawable(R.color.color_divider));
//        mSuggestListView.setDividerHeight(AppUtils.dip2px(mContext, 0.5f));
        mSuggestListView.setSelector(R.drawable.item_selector_white);
        mSuggestListView.setVisibility(View.GONE);
        if (mRootLayout != null) {
            mRootLayout.addView(mSuggestListView);
        }

        setSuggestList();

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
                    SearchCommonBeanYouHua searchCommonBeanYouHua =
                            (SearchCommonBeanYouHua) mSuggestList.get(arg2);
                    data.put("bookid", searchCommonBeanYouHua.getBook_id());

                    //统计进入到书籍封面页
                    Map<String, String> data1 = new HashMap<>();
                    data1.put("BOOKID", searchCommonBeanYouHua.getBook_id());
                    data1.put("source", "SEARCH");
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE,
                            StartLogClickUtil.ENTER, data1);


                    Intent intent = new Intent();
                    intent.setClass(activity, CoverPageActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("book_id", searchCommonBeanYouHua.getBook_id());
                    bundle.putString("book_source_id", searchCommonBeanYouHua.getBook_source_id());
//                    bundle.putString("book_chapter_id", searchCommonBeanYouHua.get)

                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                    addHistoryWord(suggest);
                } else {
                    searchType = "0";
                    isAuthor = 0;
                }
                if (!TextUtils.isEmpty(suggest) && mSearchEditText != null) {

                    data.put("keyword", suggest);
                    data.put("type", searchType);
                    data.put("enterword", mSearchEditText.getText().toString().trim());
                    if ((arg2 + 1) > 0 && (arg2 + 1) <= 2) {
                        data.put("rank", (arg2 + 1) + "");
                    } else if ((arg2 + 1) > 3 && (arg2 + 1) <= 5) {
                        data.put("rank", arg2 + "");
                    } else if ((arg2 + 1) > 6 && (arg2 + 1) <= 8) {
                        data.put("rank", (arg2 - 1) + "");
                    } else if ((arg2 + 1) > 9) {
                        data.put("rank", (arg2 - 2) + "");
                    }
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE,
                            StartLogClickUtil.TIPLISTCLICK, data);
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
        if (mSearchHotTitleLayout != null) {
            mSearchHotTitleLayout.setVisibility(View.GONE);
        }
        if (mHistoryListView != null) {
            mHistoryListView.setVisibility(View.VISIBLE);
        }

        if (mHistoryList != null && mContext != null) {
            mHistoryList.clear();
            ArrayList<String> historyWord = Tools.getHistoryWord(mContext);
            if (historyWord != null) {
                mHistoryList.addAll(historyWord);
            }
            if (mHistoryAdapter != null) {
                mHistoryAdapter.notifyDataSetChanged();
            }
        }
    }


    public void setSearchWord(String word) {
        if (mSearchEditText != null) {
            mSearchEditText.setText(word);
//            mSearchEditText.setSelection(mSearchEditText.length());
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
        if (mHistoryList == null) {
            mHistoryList = new ArrayList<>();
        }

        if (keyword == null || keyword.equals("")) {
            return;
        }
        if (mHistoryList.contains(keyword)) {
            mHistoryList.remove(keyword);
        }

        if (!mHistoryList.contains(keyword)) {
            int size = mHistoryList.size();
            if (size >= 30) {
                mHistoryList.remove(size - 1);
            }
            mHistoryList.add(0, keyword);
            Tools.saveHistoryWord(mContext, mHistoryList);
        }
        if (mHistoryAdapter != null) {
            mHistoryAdapter.notifyDataSetChanged();
        }
        setHistoryHeadersTitleView();
    }


    private void clearHistory() {
        if (mHistoryList != null) {
            mHistoryList.clear();
        }
        setHistoryHeadersTitleView();
        if (mHistoryAdapter != null) {
            mHistoryAdapter.notifyDataSetChanged();
        }
        Tools.saveHistoryWord(mContext, mHistoryList);
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
        mSuggestList.addAll(suggestList);
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
                    mSuggestAdapter.setEditInput(inputString);
                    mSuggestAdapter.notifyDataSetChanged();
                }

            }
        });
    }

    @Override
    public void onItemClickListener(int position) {


    }

    @Override
    public void onItemClick(@NonNull View view, int position) {


        SearchRecommendBook.DataBean dataBean = mRecommendFinalBooks.get(position);
        Map<String, String> data = new HashMap<>();
        data.put("rank", position + 1 + "");
        data.put("type", "1");
        data.put("bookid", dataBean.getBookId());
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE,
                StartLogClickUtil.HOTREADCLICK, data);

        CoverPageActivity.Companion.launcher(mContext, dataBean.getHost(), dataBean.getBookId(),
                dataBean.getId(), dataBean.getBookName(),
                dataBean.getAuthorName(), "", "");

        isBackSearch = true;
        isFocus = true;
    }

    public void setHistoryHeadersTitleView() {
        if (mHistoryHeadersTitle == null) {
            return;
        }
        if (mHistoryList != null && mHistoryList.size() != 0) {
            mHistoryHeadersTitle.setVisibility(View.VISIBLE);
        } else {
            mHistoryHeadersTitle.setVisibility(View.INVISIBLE);
        }

    }

    private final Handler mSearchHandler = new SearchHandler(this);

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
                    helper.clearHistory();
                    helper.setHistoryHeadersTitleView();
                    break;

                case 20:
                    helper.result((ArrayList<SearchCommonBeanYouHua>) msg.obj);
                    break;

                default:
                    break;
            }
        }
    }


    public void setOnHistoryClickListener(OnHistoryClickListener listener) {
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

        if (mHistoryAdapter != null) {
            mHistoryAdapter = null;
        }
        if (loadingPage != null) {
            loadingPage = null;
        }

        if (mSuggestAdapter != null) {
            mSuggestAdapter.clear();
            mSuggestAdapter = null;
        }

        if (mHistoryList != null) {
            mHistoryList.clear();
            mHistoryList = null;
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


    /**
     * 清除搜索历史记录
     */
    private void showClearHistoryDialog() {

        final ConfirmDialog dialog = new ConfirmDialog(activity);
        dialog.setTitle(activity.getString(R.string.prompt));
        dialog.setContent(activity.getString(R.string.determine_clear_serach_history));
        dialog.setOnConfirmListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                HashMap<String, String> data = new HashMap<>();
                data.put("type", "1");
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH,
                        StartLogClickUtil.HISTORYCLEAR, data);
                mSearchHandler.sendEmptyMessage(10);
                dialog.dismiss();

                return null;
            }
        });


        dialog.setOnCancelListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                HashMap<String, String> data = new HashMap<>();
                data.put("type", "0");
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE,
                        StartLogClickUtil.HISTORYCLEAR, data);
                dialog.dismiss();
                return null;
            }
        });

        dialog.show();

    }
}
