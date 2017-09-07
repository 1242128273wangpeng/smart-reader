package com.intelligent.reader.util;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.SearchSuggestAdapter;
import com.intelligent.reader.search.SearchHelper;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.utils.StatServiceUtils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SearchViewHelper implements SearchHelper.SearchSuggestCallBack {
    private static String TAG = SearchViewHelper.class.getSimpleName();
    private static RelativeLayout mHistoryHeadersTitle;
    private static ArrayAdapter<String> mHistoryAdapter;
    private static ArrayList<String> historyDatas = new ArrayList<String>();
    private final Handler mSearchHandler = new SearchHandler(this);
    public OnHotWordClickListener onHotWordClickListener;
    public Context context;
    TextView tv_clear_history_search_view;
    String url_tag;
    private Context mContext;
    private Activity activity;
    private ViewGroup mRootLayout;
    private EditText mSearchEditText;
    private ListView mHistoryListView;
    private ListView mSuggestListView;
    private LinearLayout hotwordContainer;
    private SearchSuggestAdapter mSuggestAdapter;
    private ArrayList<String> mSuggestList = new ArrayList<String>();
    private Resources mResources;
    private boolean mShouldShowHint = true;
    private OnHistoryClickListener mOnHistoryClickListener;
    private Random random;
    private LinearLayout ll_hotword_change_view;
    private String[] hotWords;
    private int oldType = -1;
    private SearchHelper mSearchHelper;

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
        mContext = context;
        this.activity = activity;
        mRootLayout = rootLayout;
        mSearchEditText = searchEditText;
        if (mContext != null)
            mResources = mContext.getResources();

        random = new Random();
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
            TypedValue typeColor = new TypedValue();
            Resources.Theme theme = activity.getTheme();
            theme.resolveAttribute(R.attr.color_divider, typeColor, true);
            mHistoryListView.setDivider(mResources.getDrawable(typeColor.resourceId));
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
                            startSearch(history);

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
            hotwordContainer = (LinearLayout) listHeader.findViewById(R.id
                    .hotword_container_search_view);
            ll_hotword_change_view = (LinearLayout) listHeader.findViewById(R.id
                    .ll_hotword_change_view);
            ll_hotword_change_view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_ch_hotword);
                    resetHotWordList();
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
        TypedValue typeColor = new TypedValue();
        Resources.Theme theme = activity.getTheme();
        theme.resolveAttribute(R.attr.color_divider, typeColor, true);
        mSuggestListView.setDivider(mResources.getDrawable(typeColor.resourceId));
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
//                    mSearchEditText.setSelection(suggest.length());
                    startSearch(suggest);

                    Map<String, String> data = new HashMap<>();
                    data.put("keyword", suggest);
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TIPLISTCLICK, data);
                }
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

    private void startSearch(String searchWord) {
        if (searchWord != null && !searchWord.equals("")) {
            addHistoryWord(searchWord);

            if (mOnHistoryClickListener != null) {
                mOnHistoryClickListener.OnHistoryClick(searchWord);
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

        if (hotWords == null || hotWords.length == 0) {
            hotWords = mContext.getResources().getStringArray(R.array.hot_word_list);
        }

        // 确定随机七个热词下标
        ArrayList<Integer> indexes = SearchHelper.getRandomInt(hotWords.length, 7);
        if (hotwordContainer == null || indexes == null || indexes.size() < 7) {
            return;
        }
        int index = -1;
        outer:
        for (int i = 0; i < hotwordContainer.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) hotwordContainer.getChildAt(i);
            for (int j = 0; j < linearLayout.getChildCount(); j++) {
                TextView textView = (TextView) linearLayout.getChildAt(j);
                textView.setText(hotWords[indexes.get(++index)]);
                setHotShowType(textView);
                textView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_allhotword);
                        String hotWord = ((TextView) v).getText().toString();
                        if (mSearchEditText != null) {
                            mSearchEditText.setText(hotWord);
                        }
                        mShouldShowHint = false;
                        if (onHotWordClickListener != null) {
                            onHotWordClickListener.hotWordClick(hotWord);
                        }
                    }
                });
                if (index >= 6) {
                    break outer;
                }
            }
        }
    }

    private void setHotShowType(TextView textView) {
        int currType = random.nextInt(7);
        while (oldType == currType) {
            currType = random.nextInt(7);
        }
        oldType = currType;
        ColorStateList csl;
        TypedValue typeColor = new TypedValue();
        Resources.Theme theme = activity.getTheme();
        switch (currType) {
            case 0:
            case 1:
            case 2:
            case 3:
                theme.resolveAttribute(R.attr.search_hot_word_text_bg_1, typeColor, true);
                textView.setBackgroundResource(typeColor.resourceId);
                theme.resolveAttribute(R.attr.search_hot_word_text_color_1, typeColor, true);
                csl = mContext.getResources().getColorStateList(typeColor.resourceId);
                textView.setTextColor(csl);
                break;
            case 4:
                theme.resolveAttribute(R.attr.search_hot_word_text_bg_2, typeColor, true);
                textView.setBackgroundResource(typeColor.resourceId);
                theme.resolveAttribute(R.attr.search_hot_word_text_color_2, typeColor, true);
                csl = mContext.getResources().getColorStateList(typeColor.resourceId);
                textView.setTextColor(csl);
                break;
            case 5:
                theme.resolveAttribute(R.attr.search_hot_word_text_bg_3, typeColor, true);
                textView.setBackgroundResource(typeColor.resourceId);
                theme.resolveAttribute(R.attr.search_hot_word_text_color_3, typeColor, true);
                csl = mContext.getResources().getColorStateList(typeColor.resourceId);
                textView.setTextColor(csl);
                break;
            case 6:
                theme.resolveAttribute(R.attr.search_hot_word_text_bg_4, typeColor, true);
                textView.setBackgroundResource(typeColor.resourceId);
                theme.resolveAttribute(R.attr.search_hot_word_text_color_4, typeColor, true);
                csl = mContext.getResources().getColorStateList(typeColor.resourceId);
                textView.setTextColor(csl);
                break;
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
                    if (mSearchHandler != null)
                        mSearchHandler.sendEmptyMessage(10);
                    myDialog.dismiss();
                }
            });
            TextView dialog_cancle = (TextView) myDialog.findViewById(R.id.publish_stay);
            dialog_cancle.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
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

    private void result(ArrayList<String> result) {
        if (mSuggestList == null)
            return;
        mSuggestList.clear();
        int index = 0;
        for (String item : result) {
            if (index > 4) // 只显示5个
                break;

            mSuggestList.add(item);
            index++;
        }
        String inputString = null;
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
    public void onSearchResult(ArrayList<String> suggestList) {
        if (mSuggestList == null) {
            return;
        }
        mSuggestList.clear();
        int index = 0;
        for (String item : suggestList) {
            if (index > 4) // 只显示5个
                break;

            mSuggestList.add(item);
            index++;
        }
        if (mSearchHandler == null)
            return;
        mSearchHandler.post(new Runnable() {
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

    public interface OnHotWordClickListener {
        void hotWordClick(String tag);
    }

    public interface OnHistoryClickListener {
        void OnHistoryClick(String history);
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
                    helper.result((ArrayList<String>) msg.obj);
                    break;

                default:
                    break;
            }
        }
    }
}
