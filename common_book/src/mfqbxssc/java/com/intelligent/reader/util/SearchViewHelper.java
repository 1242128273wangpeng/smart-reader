package com.intelligent.reader.util;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.SearchHisAdapter;
import com.intelligent.reader.adapter.SearchSuggestAdapter;
import com.intelligent.reader.search.SearchHelper;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.Tools;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchViewHelper implements SearchHelper.SearchSuggestCallBack, SearchHisAdapter.SearchClearCallBack {
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
    private ArrayAdapter<String> mHotAdapter;
    private SearchSuggestAdapter mSuggestAdapter;
    private SearchHisAdapter mHisAdapter;
    private ArrayList<String> mSuggestList = new ArrayList<String>();
    private Resources mResources;
    private boolean mShouldShowHint = true;
    private OnHistoryClickListener mOnHistoryClickListener;
    private String[] hotWords;
    private SearchHelper mSearchHelper;
    private boolean isFromEditClick = false;

    public SearchViewHelper(Context context, Activity activity, ViewGroup rootLayout, EditText
            searchEditText, SearchHelper searchHelper) {
        context = activity;
        mSearchHelper = searchHelper;
        init(context, activity, rootLayout, searchEditText);
    }

    private void init(Context context, Activity activity, ViewGroup rootLayout, EditText
            searchEditText) {
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

        if (mSearchHelper != null) {
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
        if (historyWord != null) {
            hisDatas.addAll(historyWord);
        }
        if (mHisAdapter != null) {
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

        hotWords = mContext.getResources().getStringArray(R.array.hot_word_list);

        // 确定随机七个热词下标
        ArrayList<Integer> indexes = SearchHelper.getRandomInt(hotWords.length, 7);
        hotDatas = new ArrayList<>();
        for (Integer i : indexes) {
            hotDatas.add(hotWords[i].replaceAll(" ", ""));
        }

        mHotAdapter = new ArrayAdapter<String>(activity, R.layout.item_hot_search_view,
                hotDatas);
        if (mHotListView != null) {

            mHotListView.setAdapter(mHotAdapter);
            mHotListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long position) {
                    StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.b_search_click_his_word);
                    if (hotDatas != null && !hotDatas.isEmpty() && position > -1 &&
                            position < hotDatas.size()) {
                        String hotWord = hotDatas.get((int) position);
                        if (hotWord != null && mSearchEditText != null) {
                            mShouldShowHint = false;
                            mSearchEditText.setText(hotWord);
                            startSearch(hotWord);

                            Map<String, String> data = new HashMap<>();
                            data.put("topicword", hotWord);
                            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TOPIC, data);
                        }
                    }
                }
            });
        }

        if (mRootLayout != null) {
            mRootLayout.addView(mHotListView);
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
                            startSearch(history);

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
                    .layout_hotword_search_view, null);
        } catch (InflateException e) {
            e.printStackTrace();
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
                String suggest = mSuggestList.get(arg2);
                if (!TextUtils.isEmpty(suggest) && mSearchEditText != null) {
                    mShouldShowHint = false;
                    mSearchEditText.setText(suggest);
                    startSearch(suggest);

                    Map<String, String> data = new HashMap<>();
                    data.put("keyword", suggest);
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TIPLISTCLICK, data);
                }
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

    private void startSearch(String searchWord) {
        if (searchWord != null && !searchWord.equals("")) {
            addHistoryWord(searchWord);

            if (mOnHistoryClickListener != null) {
                mOnHistoryClickListener.OnHistoryClick(searchWord);
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
    public void onSearchResult(ArrayList<String> suggestList) {
        if (mSuggestList == null) {
            return;
        }
        mSuggestList.clear();
        int index = 0;
        for (String item : suggestList) {
            if (index > 9) // 只显示10个
                break;

            mSuggestList.add(item);
            index++;
        }
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (mSuggestAdapter != null) {
                    String inputString = null;
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

    public interface OnHotWordClickListener {
        void hotWordClick(String tag);
    }

    public interface OnHistoryClickListener {
        void OnHistoryClick(String history);
    }
}
