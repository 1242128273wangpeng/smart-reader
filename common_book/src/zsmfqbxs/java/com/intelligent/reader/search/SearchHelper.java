package com.intelligent.reader.search;

import static net.lzbook.kit.statistic.StatisticKt.alilog;
import static net.lzbook.kit.statistic.StatisticUtilKt.buildSearch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.Chapter;
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua;
import com.ding.basic.bean.SearchCommonBeanYouHua;
import com.ding.basic.repository.RequestRepositoryFactory;
import com.ding.basic.request.RequestService;
import com.ding.basic.request.RequestSubscriber;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.SearchBookActivity;
import com.intelligent.reader.activity.TabulationActivity;
import com.orhanobut.logger.Logger;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.statistic.model.Search;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.oneclick.AntiShake;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.reactivex.disposables.Disposable;

/**
 * SearchHelper
 * Created by yuchao on 2017/8/2 0002.
 */
public class SearchHelper {
    private static final String TAG = SearchHelper.class.getSimpleName();
    private Map<String, WordInfo> wordInfoMap = new HashMap<>();
    private SharedPreferences sharedPreferences;

    private String word;
    private String searchType = "0";
    private String filterType = "0";
    private String filterWord = "ALL";
    private String sortType = "0";
    private String mUrl;
    private String fromClass;

    private Activity mContext;
    private String url_tag;
    private Disposable disposable;
//    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public SearchHelper(Activity context) {
        mContext = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    private class WordInfo {
        boolean actioned = false;
        private long startTime = System.currentTimeMillis();
        private long useTime = 0;

        public long computeUseTime() {
            if (useTime == 0) {
                useTime = System.currentTimeMillis() - startTime;
            }
            return useTime;
        }

    }

    private SearchSuggestCallBack searchSuggestCallBack;
    private JsNoneResultSearchCall jsNoneResultSearchCall;

    public void setJsNoneResultSearchCall(JsNoneResultSearchCall jsNoneResultSearchCall) {
        this.jsNoneResultSearchCall = jsNoneResultSearchCall;
    }

    public void setSearchSuggestCallBack(SearchSuggestCallBack ssb) {
        searchSuggestCallBack = ssb;
    }

    public interface JsNoneResultSearchCall {
        void onNoneResultSearch(String searchWord);
    }

    public interface SearchSuggestCallBack {
        void onSearchResult(List<Object> suggestList, SearchAutoCompleteBeanYouHua transmitBean);
    }

    public void setStartedAction() {
        wordInfoMap.put(word, new WordInfo());
    }

    public void onLoadFinished() {
        WordInfo wordInfo = wordInfoMap.get(word);
        if (wordInfo != null) {
            wordInfo.computeUseTime();
        }
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getFromClass() {
        return fromClass;
    }

    public void setFromClass(String fromClass) {
        this.fromClass = fromClass;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public void setHotWordType(String word, String type) {
        this.word = word;
        searchType = type;
        filterType = "0";
        filterWord = "ALL";
        sortType = "0";
    }

    public void setInitType(Intent intent) {
        word = intent.getStringExtra("word");
        fromClass = intent.getStringExtra("from_class");
        searchType = intent.getStringExtra("search_type");
        filterType = intent.getStringExtra("filter_type");
        filterWord = intent.getStringExtra("filter_word");
        sortType = intent.getStringExtra("sort_type");

    }

    private AntiShake shake = new AntiShake();



    protected Book genCoverBook(String host, String book_id, String book_source_id, String name,
            String author, String status, String category,
            String imgUrl, String last_chapter, String chapter_count, long update_time,
            String parameter, String
            extra_parameter, int dex) {

        Book book = new Book();
        book.setStatus(status);
        book.setBook_id(book_id);
        book.setBook_source_id(book_source_id);
        book.setName(name);
        book.setLabel(category);
        book.setAuthor(author);
        book.setImg_url(imgUrl);
        book.setHost(host);
        Chapter chapter = new Chapter();
        chapter.setName(last_chapter);
        chapter.setUpdate_time(update_time);
        chapter.setSerial_number(Integer.valueOf(chapter_count));
        book.setLast_chapter(chapter);
        book.setChapter_count(Integer.valueOf(chapter_count));
        book.setLast_update_success_time(System.currentTimeMillis());
        return book;
    }

    public void startLoadData(int isAuthor) {
        String searchWord;
        if (word != null) {
            searchWord = word;
            String channelID = AppUtils.getChannelId();
            if (channelID.equals("blp1298_10882_001") || channelID.equals("blp1298_10883_001")
                    || channelID.equals("blp1298_10699_001")) {
                if (Constants.isBaiduExamine
                        && Constants.versionCode == AppUtils.getVersionCode()) {
                    searchWord = getReplaceWord();
                    AppLog.e(TAG, searchWord);
                }
            }
            if (searchType.equals("2") && isAuthor == 1) {

                Map<String, String> params = new HashMap<>();
                params.put("authorType", searchWord);
                mUrl = RequestService.WEB_AUTHOR.replace("{packageName}", AppUtils.getPackageName()) + "?author=" + searchWord;

                try {
                    sharedPreferences.edit().putString(Constants.FINDBOOK_SEARCH,
                            "authorType").apply();
                    SearchBookActivity.isSatyHistory = true;
                    Intent intent = new Intent();
                    intent.setClass(mContext, TabulationActivity.class);
                    intent.putExtra("url", mUrl);
                    intent.putExtra("title", "作者主页");
                    fromClass = "findBookDetail";
                    mContext.startActivity(intent);
                    AppLog.e(TAG, "EnterAnotherWeb");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Map<String, String> params = new HashMap<>();
                params.put("keyword", searchWord);
                params.put("searchType", searchType);
                params.put("filter_type", filterType);
                params.put("filter_word", filterWord);
                params.put("sort_type", sortType);
                params.put("wordType", searchType);
                params.put("searchEmpty", "1");
                AppLog.e("kk",
                        searchWord + "==" + searchType + "==" + filterType + "==" + filterWord
                                + "===" + sortType);
                String uri = RequestService.WEB_SEARCH.replace("{packageName}", AppUtils.getPackageName());
                mUrl = UrlUtils.buildWebUrl(uri, params);
            }

        }

        if (mStartLoadCall != null) {
            mStartLoadCall.onStartLoad(mUrl);
        }

    }

    // 生成 [0-n) 个不重复的随机数
    public static ArrayList<Integer> getRandomInt(int range, int count) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        Random rand = new Random();
        boolean[] bool = new boolean[range];
        int num = 0;

        for (int i = 0; i < count; i++) {
            do {
                // 如果产生的数相同继续循环
                num = rand.nextInt(range);
            } while (bool[num]);

            bool[num] = true;
            list.add(num);
        }

        return list;
    }

    /*
     *
     *根据策略，显示数据的顺序为 两个书名 +一个间隔 +两个作者 +一个间隔 +两个标签 +一个间隔 +剩余书名
     */


    public void packageData(SearchAutoCompleteBeanYouHua bean) {
        List<Object> resultSuggest = new ArrayList<>();
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_search_history_gap, null,
                false);
        //两个书名
        if (bean.getData().getName() != null && bean.getData().getName().size() > 0) {
            for (int i = 0; i < ((bean.getData().getName().size() >= 2) ? 2
                    : bean.getData().getName().size()); i++) {
                SearchAutoCompleteBeanYouHua.DataBean.NameBean nameBean =
                        bean.getData().getName().get(i);
                if (nameBean != null) {
                    SearchCommonBeanYouHua searchCommonBean = new SearchCommonBeanYouHua();
                    searchCommonBean.setSuggest(nameBean.getSuggest());
                    searchCommonBean.setWordtype(nameBean.getWordtype());
                    searchCommonBean.setImage_url(nameBean.getImgUrl());

                    //-----------------书名特有字段-------------------------------
                    searchCommonBean.setHost(nameBean.getHost());
                    searchCommonBean.setBook_id(nameBean.getBookid());
                    searchCommonBean.setBook_source_id(nameBean.getBookSourceId());
                    searchCommonBean.setName(nameBean.getBookName());
                    searchCommonBean.setAuthor(nameBean.getAuthor());
                    searchCommonBean.setParameter(nameBean.getParameter());
                    searchCommonBean.setExtra_parameter(nameBean.getExtraParameter());
                    searchCommonBean.setBookType(nameBean.getVip() + "");
                    //------------------------------------------------------------

                    resultSuggest.add(searchCommonBean);
                }
            }
            resultSuggest.add(view);
        }

        if (bean.getData().getAuthors() != null && bean.getData().getAuthors().size() > 0) {

            //两个作者
            for (int i = 0; i < ((bean.getData().getAuthors().size() >= 2) ? 2
                    : bean.getData().getAuthors().size()); i++) {
                SearchAutoCompleteBeanYouHua.DataBean.AuthorsBean authorsBean =
                        bean.getData().getAuthors().get(i);
                if (authorsBean != null) {
                    SearchCommonBeanYouHua searchCommonBean = new SearchCommonBeanYouHua();
                    searchCommonBean.setSuggest(bean.getData().getAuthors().get(i).getSuggest());
                    searchCommonBean.setWordtype(bean.getData().getAuthors().get(i).getWordtype());
                    searchCommonBean.setImage_url("");
                    searchCommonBean.setIsAuthor(bean.getData().getAuthors().get(i).isAuthor());
                    resultSuggest.add(searchCommonBean);

                }
            }
            resultSuggest.add(view);
        }

        if (bean.getData().getLabel() != null && bean.getData().getLabel().size() > 0) {

            //两个标签
            for (int i = 0; i < ((bean.getData().getLabel().size() >= 2) ? 2
                    : bean.getData().getLabel().size()); i++) {
                SearchAutoCompleteBeanYouHua.DataBean.LabelBean labelBean =
                        bean.getData().getLabel().get(i);
                if (labelBean != null) {
                    SearchCommonBeanYouHua searchCommonBean = new SearchCommonBeanYouHua();
                    searchCommonBean.setSuggest(bean.getData().getLabel().get(i).getSuggest());
                    searchCommonBean.setWordtype(bean.getData().getLabel().get(i).getWordtype());
                    searchCommonBean.setImage_url("");
                    resultSuggest.add(searchCommonBean);

                }
            }
            resultSuggest.add(view);
        } else {
            if (bean.getData().getAuthors() == null || (bean.getData().getAuthors() != null
                    && bean.getData().getAuthors().size() == 0)) {
                resultSuggest.remove(view);
            }
        }


        //其余书名
        if (bean.getData().getName() != null) {
            for (int i = 2; i < bean.getData().getName().size(); i++) {
                SearchCommonBeanYouHua searchCommonBean = new SearchCommonBeanYouHua();
                SearchAutoCompleteBeanYouHua.DataBean.NameBean nameBean =
                        bean.getData().getName().get(i);
                if (nameBean != null) {
                    searchCommonBean.setSuggest(nameBean.getSuggest());
                    searchCommonBean.setWordtype(nameBean.getWordtype());
                    searchCommonBean.setImage_url(nameBean.getImgUrl());

                    //-----------------书名特有字段-------------------------------
                    searchCommonBean.setHost(nameBean.getHost());
                    searchCommonBean.setBook_id(nameBean.getBookid());
                    searchCommonBean.setBook_source_id(nameBean.getBookSourceId());
                    searchCommonBean.setName(nameBean.getBookName());
                    searchCommonBean.setAuthor(nameBean.getAuthor());
                    searchCommonBean.setParameter(nameBean.getParameter());
                    searchCommonBean.setExtra_parameter(nameBean.getExtraParameter());
                    searchCommonBean.setBookType(nameBean.getVip() + "");
                    //------------------------------------------------------------

                    resultSuggest.add(searchCommonBean);
                }
            }
        }

        for (Object bean1 : resultSuggest) {
            AppLog.e("uuu", bean1.toString());
        }
        if (searchSuggestCallBack != null) {
            searchSuggestCallBack.onSearchResult(resultSuggest, bean);
        }

    }

    public String getReplaceWord() {
        String[] words = {"品质随时购", "春节不打烊", "轻松过大年", "便携无屏电视", "游戏笔记本电脑", "全自动洗衣机", "家团圆礼盒"};
        Random random = new Random();
        int index = random.nextInt(7);
        return words[index];
    }

    public void onDestroy() {
        Set<String> strings = wordInfoMap.keySet();
        for (String key : strings) {
            WordInfo wordInfo = wordInfoMap.get(key);
            if (wordInfo != null && !wordInfo.actioned) {
                alilog(buildSearch(key, Search.OP.CANCEL, wordInfo.computeUseTime()));
            }
        }
        wordInfoMap.clear();
        recycleDisposable();
    }


    private JsCallSearchCall mJsCallSearchCall;

    public void setJsCallSearchCall(JsCallSearchCall jsCallSearchCall) {
        mJsCallSearchCall = jsCallSearchCall;
    }

    public interface JsCallSearchCall {
        void onJsSearch();
    }

    private StartLoadCall mStartLoadCall;

    public void setStartLoadCall(StartLoadCall startLoadCall) {
        mStartLoadCall = startLoadCall;
    }

    public interface StartLoadCall {
        void onStartLoad(String url);
    }

    private void recycleDisposable() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public void startSearch(String query) {
        if (query != null && !TextUtils.isEmpty(query)) {
            try {
                query = URLDecoder.decode(query, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        final String finalQuery = query;


        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestAutoCompleteV4(

                finalQuery, new RequestSubscriber<SearchAutoCompleteBeanYouHua>() {
                    @Override
                    public void requestResult(@Nullable SearchAutoCompleteBeanYouHua bean) {
                        if (bean != null
                                && SearchAutoCompleteBeanYouHua.Companion.getREQUESR_SUCCESS()
                                .equals(
                                bean.getRespCode())
                                && bean.getData() != null) {
                            packageData(bean);
                        }
                    }

                    @Override
                    public void requestError(@NotNull String message) {
                        Logger.e("请求自动补全失败！");
                    }
                });

    }
}
