package com.intelligent.reader.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.search.SearchAutoCompleteBean;
import net.lzbook.kit.data.search.SearchCommonBean;
import net.lzbook.kit.data.search.SearchHotBean;
import net.lzbook.kit.net.custom.service.NetService;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.utils.*;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.encrypt.URLBuilderIntterface;

import com.google.gson.Gson;
import com.intelligent.reader.R;
import com.intelligent.reader.adapter.SearchHotWordAdapter;
import com.intelligent.reader.adapter.SearchSuggestAdapter;
import net.lzbook.kit.net.custom.service.OwnSearchService;
import com.intelligent.reader.search.SearchHelper;
import com.intelligent.reader.view.ScrollForGridView;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SearchViewHelper implements SearchHelper.SearchSuggestCallBack {
    private static String TAG = SearchViewHelper.class.getSimpleName();
    private static RelativeLayout mHistoryHeadersTitle;
    private static ArrayAdapter<String> mHistoryAdapter;
    private static ArrayList<String> historyDatas = new ArrayList<String>();
    private final Handler mSearchHandler = new SearchHandler(this);
    public OnHotWordClickListener onHotWordClickListener;
    public Context context;
    TextView tv_clear_history_search_view;
    private Context mContext;
    private Activity activity;
    private ViewGroup mRootLayout;
    private EditText mSearchEditText;
    private ListView mHistoryListView;
    private ListView mSuggestListView;
    private ScrollForGridView mGridView;
    private LinearLayout linear_parent;
    private SearchSuggestAdapter mSuggestAdapter;
    private List<SearchCommonBean> mSuggestList = new ArrayList<SearchCommonBean>();
    private Resources mResources;
    private boolean mShouldShowHint = true;
    private OnHistoryClickListener mOnHistoryClickListener;
    private List<SearchHotBean.DataBean> hotWords = new ArrayList<>();
    private SearchHelper mSearchHelper;
    private SearchHotWordAdapter searchHotWordAdapter;
    private String suggest;
    private String searchType;
    private SharedPreferencesUtils sharedPreferencesUtils;
    private Gson gson;
    private List<SearchAutoCompleteBean.DataBean.AuthorsBean> authorsBean = new ArrayList<>();
    private List<SearchAutoCompleteBean.DataBean.LabelBean> labelBean = new ArrayList<>();
    private List<SearchAutoCompleteBean.DataBean.NameBean> bookNameBean = new ArrayList<>();

    public SearchViewHelper(Context context, Activity activity, ViewGroup rootLayout, EditText
            searchEditText, SearchHelper searchHelper) {
        context = activity;
        mSearchHelper = searchHelper;
        init(context, activity, rootLayout, searchEditText);
    }

    private static void setHistoryHeadersTitleView() {
        if (mHistoryHeadersTitle == null) {
            return;
        }
        if (historyDatas != null && historyDatas.size() != 0) {
            mHistoryHeadersTitle.setVisibility(View.VISIBLE);
        } else {
            mHistoryHeadersTitle.setVisibility(View.INVISIBLE);
        }
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

        initHistoryView();
        initSuggestListView();
    }

    public void setShowHintEnabled(boolean showHint) {
        mShouldShowHint = showHint;
    }

    public void showHintList(String searchWord) {
        if (!mShouldShowHint) {
            if (mRootLayout != null)
                mRootLayout.setVisibility(View.GONE);
            return;
        }

        if (mRootLayout != null)
            mRootLayout.setVisibility(View.VISIBLE);

        if (searchWord == null || TextUtils.isEmpty(searchWord)) {
            showHistoryList();
        } else {
            showSuggestList(searchWord);
        }
    }

    public void hideHintList() {
        if (mRootLayout != null)
            mRootLayout.setVisibility(View.GONE);
    }

    private void showSuggestList(String searchWord) {
        if (mSuggestListView != null) {
            mSuggestListView.setVisibility(View.VISIBLE);
        }
        if (mHistoryListView != null)
            mHistoryListView.setVisibility(View.GONE);

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
            mSearchHelper.startSearchSuggestData(searchWord);
        }

    }

    private void initHistoryMain(Context context) {
        mHistoryListView = new ListView(context);
        if (mHistoryListView != null && mResources != null) {
            mHistoryListView.setCacheColorHint(mResources.getColor(R.color.transparent));
            int typeColor = R.color.color_gray_e8e8e8;
            mHistoryListView.setDivider(mResources.getDrawable(typeColor));
            mHistoryListView.setDividerHeight(AppUtils.dip2px(mContext, 0.5f));
            mHistoryListView.setHeaderDividersEnabled(false);
            mHistoryListView.setSelector(R.drawable.item_selector_white);
        }
    }

    private void initHistoryView() {
        initHistoryMain(mContext);

        setHistoryHeaderHotWord();
        initHistoryHeadersTitleView();

        historyDatas = Tools.getHistoryWord(mContext);
        mHistoryAdapter = new ArrayAdapter<String>(activity, R.layout.item_history_search_view,
                historyDatas);
        if (mHistoryListView != null) {

            mHistoryListView.setAdapter(mHistoryAdapter);
            mHistoryListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long position) {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_his_word);
                    if (historyDatas != null && !historyDatas.isEmpty() && position > -1 &&
                            position < historyDatas.size()) {
                        String history = historyDatas.get((int) position);
                        if (history != null && mSearchEditText != null) {
                            mShouldShowHint = false;
                            mSearchEditText.setText(history);
//                            mSearchEditText.setSelection(history.length());
                            startSearch(history, "0");

                            Map<String, String> data = new HashMap<>();
                            data.put("keyword", history);
                            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.HISTORY, data);
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

    private void setHistoryHeaderHotWord() {
        View hotword_view = initHotWordView();
        if (mHistoryListView != null)
            mHistoryListView.addHeaderView(hotword_view);
        resetHotWordList();
    }

    private View initHotWordView() {
        View listHeader = null;
        try {

            listHeader = LayoutInflater.from(activity).inflate(R.layout
                    .layout_hotword_search_view, null);
        } catch (InflateException e) {
            e.printStackTrace();
        }
        if (listHeader != null) {
            mGridView = (ScrollForGridView) listHeader.findViewById(R.id.grid);
            linear_parent = (LinearLayout) listHeader.findViewById(R.id.linear_parent);
            mGridView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_allhotword);
                    String hotWord = hotWords.get(position).getWord();
                    Map<String, String> data = new HashMap<>();
                    data.put("topicword", hotWord);
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TOPIC, data);

                    AppLog.e("wordType",hotWord+hotWords.get(position).getWordType()+"");
                    if (mSearchEditText != null) {
                        mSearchEditText.setText(hotWord);
                    }
                    mShouldShowHint = false;
                    if (onHotWordClickListener != null) {
                        onHotWordClickListener.hotWordClick(hotWord,hotWords.get(position).getWordType()+"");
                    }
                }

            });

        }
        return listHeader;
    }

    private void initSuggestListView() {
        if (mSearchHelper != null) {
            mSearchHelper.setSearchSuggestCallBack(this);
        }

        mSuggestListView = new ListView(activity);
        if (mSuggestListView == null)
            return;
        mSuggestListView.setCacheColorHint(mResources.getColor(R.color.transparent));
        int typeColor =R.color.color_gray_e8e8e8;
        mSuggestListView.setDivider(mResources.getDrawable(typeColor));
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
                if(mSearchHelper != null){
                    mSearchHelper.setSearchType(searchType);
                    mSearchHelper.setWord(suggest);
                    AppLog.e("typesearc",mSearchHelper.getSearchType()+suggest);
                }
                if (!TextUtils.isEmpty(suggest) && mSearchEditText != null) {
                    Map<String, String> data = new HashMap<>();
                    data.put("keyword", suggest);
                    data.put("enterword", mSearchEditText.getText().toString().trim());
                    data.put("rank", String.valueOf(arg2 + 1));
                    data.put("type", searchType);
                    if(arg2+1<=authorsBean.size()){
                        data.put("typerank",arg2+1+"");
                    }else if(arg2+1 <= authorsBean.size()+labelBean.size()){
                        data.put("typerank",arg2+1-authorsBean.size()+"");
                    }else if(arg2+1<=authorsBean.size()+labelBean.size()+bookNameBean.size()){
                        data.put("typerank",arg2+1-authorsBean.size()-labelBean.size()+"");
                    }else{
                        data.put("typerank",arg2+1+"");
                    }
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TIPLISTCLICK, data);
                    mShouldShowHint = false;
                    mSearchEditText.setText(suggest);

                    AppLog.e("type",searchType+"===");
//                    mSearchEditText.setSelection(suggest.length());
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

    private void showHistoryList() {
        if (mHistoryListView != null)
            mHistoryListView.setVisibility(View.VISIBLE);
        if (mSuggestListView != null)
            mSuggestListView.setVisibility(View.GONE);
    }

    public void notifyListChanged() {
        if (mHistoryAdapter != null)
            mHistoryAdapter.notifyDataSetChanged();
    }

    private void initHistoryHeadersTitleView() {
        if (activity == null)
            return;
        try {
            mHistoryHeadersTitle = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout
                    .header_view_history_search_view, null);
        } catch (InflateException e) {
            e.printStackTrace();
        }
        if (mHistoryHeadersTitle != null)
            tv_clear_history_search_view = (TextView) mHistoryHeadersTitle.findViewById(R.id
                    .tv_clear_history_search_view);

        if (mHistoryListView != null) {
            mHistoryListView.addHeaderView(mHistoryHeadersTitle);
        }
        if (tv_clear_history_search_view == null)
            return;
        tv_clear_history_search_view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_his_clear);
                showDialog();
            }
        });
    }

    public void setSearchWord(String word) {
        mShouldShowHint = false;
        if (mSearchEditText != null) {
            mSearchEditText.setText(word);
//            mSearchEditText.setSelection(mSearchEditText.length());
        }
        mShouldShowHint = true;
        addHistoryWord(word);
    }

    private void startSearch(String searchWord, String searchType) {
        if (searchWord != null && !searchWord.equals("")) {
            addHistoryWord(searchWord);

            if (mOnHistoryClickListener != null) {

                mOnHistoryClickListener.OnHistoryClick(searchWord, searchType);
            }
        }
    }

    public void addHistoryWord(String keyword) {
        if (historyDatas == null) {
            historyDatas = new ArrayList<String>();
        }

        if (keyword == null || keyword.equals("")) {
            return;
        }
        if (historyDatas.contains(keyword)) {
            historyDatas.remove(keyword);
        }

        if (!historyDatas.contains(keyword)) {
            int size = historyDatas.size();
            if (size >= 5) {
                historyDatas.remove(size - 1);
            }
            historyDatas.add(0, keyword);
            Tools.saveHistoryWord(mContext, historyDatas);
        }
        if (mHistoryAdapter != null) {
            mHistoryAdapter.notifyDataSetChanged();
        }
        setHistoryHeadersTitleView();
    }

    private void resetHotWordList() {
        if (activity == null)
            return;

        if (NetWorkUtils.getNetWorkTypeNew(mContext).equals("无")) {
            getCacheDataFromShare(false);
        } else {
            AppLog.e("url", UrlUtils.getBookNovelDeployHost() + "===" + NetWorkUtils.getNetWorkTypeNew(mContext));
            OwnSearchService searchService = NetService.INSTANCE.getOwnSearchService();
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

            AppLog.e("url", UrlUtils.getBookNovelDeployHost() + "===" + NetWorkUtils.getNetWorkTypeNew(mContext));
        }
    }

    /**
     * if hasn't net getData from sharepreferenecs cache
     */
    public void getCacheDataFromShare(boolean hasNet){
        if (!TextUtils.isEmpty(sharedPreferencesUtils.getString(Constants.SERARCH_HOT_WORD))) {
            linear_parent.setVisibility(View.VISIBLE);
            hotWords.clear();
            String cacheHotWords = sharedPreferencesUtils.getString(Constants.SERARCH_HOT_WORD);
            SearchHotBean searchHotBean = gson.fromJson(cacheHotWords, SearchHotBean.class);
            parseResult(searchHotBean,false);
            AppLog.e("urlbean", cacheHotWords);
        } else {
            if(!hasNet){
                ToastUtils.showToastNoRepeat("网络不给力哦");
            }
            linear_parent.setVisibility(View.GONE);
        }
    }

    /**
     * parse result data
     */
    public void parseResult(SearchHotBean value,boolean hasNet) {
        hotWords.clear();
        if (value != null && value.getData() != null) {
            hotWords = value.getData();
            if (hotWords != null && hotWords.size() >= 0) {
                linear_parent.setVisibility(View.VISIBLE);
                if(hasNet){
                    sharedPreferencesUtils.putString(Constants.SERARCH_HOT_WORD, gson.toJson(value, SearchHotBean.class));
                }
                if (searchHotWordAdapter == null) {
                    searchHotWordAdapter = new SearchHotWordAdapter(activity, hotWords);
                    mGridView.setAdapter(searchHotWordAdapter);
                } else {
                    searchHotWordAdapter.setDatas(hotWords);
                    searchHotWordAdapter.notifyDataSetChanged();
                }
            } else {
                sharedPreferencesUtils.putString(Constants.SERARCH_HOT_WORD, "");
                linear_parent.setVisibility(View.GONE);
            }
        }
    }

    private void showDialog() {
        if (activity != null && !activity.isFinishing()) {
            final MyDialog myDialog = new MyDialog(activity, R.layout.publish_hint_dialog);
            myDialog.setCanceledOnTouchOutside(true);
            TextView dialog_title = (TextView) myDialog.findViewById(R.id.dialog_title);
            dialog_title.setText(R.string.prompt);
            TextView dialog_content = (TextView) myDialog.findViewById(R.id.publish_content);
            dialog_content.setText(R.string.determine_clear_serach_history);
            TextView dialog_comfire = (TextView) myDialog.findViewById(R.id.publish_leave);

            dialog_comfire.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("type", "1");
                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.HISTORYCLEAR, data);
                    if (mSearchHandler != null)
                        mSearchHandler.sendEmptyMessage(10);
                    myDialog.dismiss();
                }
            });
            TextView dialog_cancle = (TextView) myDialog.findViewById(R.id.publish_stay);
            dialog_cancle.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("type", "0");
                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.HISTORYCLEAR, data);
                    myDialog.dismiss();
                }
            });
            myDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    myDialog.dismiss();
                }
            });
            if (!myDialog.isShowing()) {
                try {
                    myDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void clearHistory() {


        if (historyDatas != null)
            historyDatas.clear();
        setHistoryHeadersTitleView();
        if (mHistoryAdapter != null)
            mHistoryAdapter.notifyDataSetChanged();
        Tools.saveHistoryWord(mContext, historyDatas);
    }

    private void result(List<SearchCommonBean> result) {
        if (mSuggestList == null)
            return;
        mSuggestList.clear();
        int index = 0;
        for (SearchCommonBean item : result) {
            if (index > 4) // 只显示5个
                break;

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
    public void onSearchResult(List<SearchCommonBean>  suggestList,SearchAutoCompleteBean transmitBean) {
        if (mSuggestList == null){
            return;
        }
        mSuggestList.clear();
        authorsBean.clear();
        labelBean.clear();
        bookNameBean.clear();
        if(transmitBean.getData()!=null){
            if(transmitBean.getData().getAuthors() != null){
                authorsBean = transmitBean.getData().getAuthors();
            }
            if(transmitBean.getData().getLabel() != null){
                labelBean = transmitBean.getData().getLabel();
            }
            if(transmitBean.getData().getName() != null){
                bookNameBean = transmitBean.getData().getName();
            }
        }
        for (SearchCommonBean item : suggestList) {
            mSuggestList.add(item);
        }
        if (mSearchHandler == null)
            return;
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

    public void setOnHistoryClickListener(OnHistoryClickListener listener) {
        mOnHistoryClickListener = listener;
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
            mHistoryAdapter.clear();
            mHistoryAdapter = null;
        }

        if (mSuggestAdapter != null) {
            mSuggestAdapter.clear();
            mSuggestAdapter = null;
        }

        if (historyDatas != null) {
            historyDatas.clear();
            historyDatas = null;
        }

        if (mSuggestList != null) {
            mSuggestList.clear();
            mSuggestList = null;
        }
        clear();
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

    static class SearchHandler extends Handler {
        private WeakReference<SearchViewHelper> reference;

        SearchHandler(SearchViewHelper helper) {
            reference = new WeakReference<SearchViewHelper>(helper);
        }

        public void handleMessage(Message msg) {
            SearchViewHelper helper = reference.get();
            if (helper == null) {
                return;
            }
            switch (msg.what) {
                case 10:
                    helper.clearHistory();
                    break;

                case 20:
                    helper.result((ArrayList<SearchCommonBean>) msg.obj);
                    break;

                default:
                    break;
            }
        }
    }
}
