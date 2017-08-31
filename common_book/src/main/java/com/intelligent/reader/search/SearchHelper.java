package com.intelligent.reader.search;

import com.intelligent.reader.R;
import com.intelligent.reader.activity.CoverPageActivity;
import com.intelligent.reader.read.help.BookHelper;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.request.YSRequestService;
import net.lzbook.kit.statistic.model.Search;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.JSInterfaceHelper;
import net.xxx.yyy.go.spider.URLBuilderIntterface;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static net.lzbook.kit.statistic.StatisticKt.alilog;
import static net.lzbook.kit.statistic.StatisticUtilKt.buildSearch;

/**
 * Created by yuchao on 2017/8/2 0002.
 */

public class SearchHelper {
    private static final String TAG = SearchHelper.class.getSimpleName();
    private BookDaoHelper bookDaoHelper;
    private Map<String, SearchHelper.WordInfo> wordInfoMap = new HashMap<>();

    private String word;
    private String searchType = "0";
    private String filterType = "0";
    private String filterWord = "ALL";
    private String sortType = "0";
    private String mUrl;

    private Context mContext;
    private String url_tag;


    public SearchHelper(Context context){
        mContext = context;
        if (bookDaoHelper == null) {
            bookDaoHelper = BookDaoHelper.getInstance(mContext.getApplicationContext());
        }
    }

    private class WordInfo{
        boolean actioned = false;
        private long startTime = System.currentTimeMillis();
        private long useTime = 0;

        public long computeUseTime(){
            if(useTime == 0) {
                useTime = System.currentTimeMillis() - startTime;
            }
            return useTime;
        }

    }

    public void startSearchSuggestData(String searchWord){
        try {
            if (searchWord != null && !TextUtils.isEmpty(searchWord)) {
                searchWord = URLEncoder.encode(searchWord, "utf-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (searchWord != null && !TextUtils.isEmpty(searchWord)) {
            String url = URLBuilderIntterface.YS_SEARCH_SUGGEST.replace("{word}", searchWord);
            url_tag = url;
            YSRequestService.getSearchSuggestData(new YSRequestService.DataServiceTagCallBack() {
                @Override
                public void onSuccess(Object result, Object tag) {
                    if (result != null && url_tag.equals(tag)) {
                        ArrayList<String> resultSuggest = (ArrayList<String>) result;
                        if (searchSuggestCallBack != null){
                            searchSuggestCallBack.onSearchResult(resultSuggest);
                        }
                    }
                }

                @Override
                public void onError(Exception error, Object tag) {

                }
            }, url);
        }
    }

    private SearchSuggestCallBack searchSuggestCallBack;

    public void setSearchSuggestCallBack(SearchSuggestCallBack ssb){
        searchSuggestCallBack = ssb;
    }

    public interface SearchSuggestCallBack{
        void onSearchResult(ArrayList<String> suggestList);
    }

    public void setStartedAction(){
        wordInfoMap.put(word, new WordInfo());
    }

    public void onLoadFinished(){
        WordInfo wordInfo = wordInfoMap.get(word);
        if(wordInfo != null)
            wordInfo.computeUseTime();
    }

    public String getWord(){
        return word;
    }

    public void setWord(String word){
        this.word = word;
    }

    public void setHotWordType(String word){
        this.word = word;
        searchType = "0";
        filterType = "0";
        filterWord = "ALL";
        sortType = "0";
    }

    public void setInitType(Intent intent){
        word = intent.getStringExtra("word");
        searchType = intent.getStringExtra("search_type");
        filterType = intent.getStringExtra("filter_type");
        filterWord = intent.getStringExtra("filter_word");
        sortType = intent.getStringExtra("sort_type");
    }

    public void initJSHelp(JSInterfaceHelper jsInterfaceHelper) {

        if (jsInterfaceHelper == null){
            return;
        }

        jsInterfaceHelper.setOnSearchClick(new JSInterfaceHelper.onSearchClick() {

            @Override
            public void doSearch(String keyWord, String search_type, String filter_type, String filter_word, String sort_type) {

                word = keyWord;
                searchType = search_type;
                filterType = filter_type;
                filterWord = filter_word;
                sortType = sort_type;

                startLoadData();

                if (mJsCallSearchCall != null){
                    mJsCallSearchCall.onJsSearch();
                }
            }
        });

        jsInterfaceHelper.setOnEnterCover(new JSInterfaceHelper.onEnterCover() {

            @Override
            public void doCover(final String host, final String book_id, final String book_source_id, final String name, final String author, final
            String parameter, final String extra_parameter) {
                AppLog.e(TAG, "doCover");

                RequestItem requestItem = new RequestItem();
                requestItem.book_id = book_id;
                requestItem.book_source_id = book_source_id;
                requestItem.host = host;
                requestItem.name = name;
                requestItem.author = author;
                requestItem.parameter = parameter;
                requestItem.extra_parameter = extra_parameter;

                SearchHelper.WordInfo wordInfo = wordInfoMap.get(word);
                if(wordInfo!= null) {
                    wordInfo.actioned = true;
                    alilog(buildSearch(requestItem, word, Search.OP.COVER, wordInfo.computeUseTime()));
                }
                Intent intent = new Intent();
                intent.setClass(mContext, CoverPageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });

        jsInterfaceHelper.setOnEnterRead(new JSInterfaceHelper.onEnterRead() {
            @Override
            public void doRead(final String host, final String book_id, final String book_source_id, final String name, final String author, final
            String status, final String category, final String imgUrl, final String last_chapter, final String chapter_count, final long
                                       updateTime, final String parameter, final String extra_parameter, final int dex) {
                AppLog.e(TAG, "doRead");
                Book coverBook = genCoverBook(host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count,
                        updateTime, parameter, extra_parameter, dex);
                AppLog.e(TAG, "DoRead : " + coverBook.sequence);

//                alilog(buildSearch(coverBook, word, Search.OP.RETURN));

                BookHelper.goToRead(mContext, coverBook);
            }
        });

        ArrayList<Book> booksOnLine = bookDaoHelper.getBooksOnLineList();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (int i = 0; i < booksOnLine.size(); i++) {
            stringBuilder.append("{'id':'").append(booksOnLine.get(i).book_id).append("'}");
            if (i != booksOnLine.size() - 1) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("]");
        AppLog.e(TAG, "StringBuilder : " + stringBuilder.toString());
        jsInterfaceHelper.setBookString(stringBuilder.toString());

        jsInterfaceHelper.setOnInsertBook(new JSInterfaceHelper.OnInsertBook() {
            @Override
            public void doInsertBook(final String host, final String book_id, final String book_source_id, final String name, final String author,
                                     final String status, final String category, final String imgUrl, final String last_chapter, final String
                                             chapter_count, final long updateTime, final String parameter, final String extra_parameter, final int
                                             dex) {
                AppLog.e(TAG, "doInsertBook");
                Book book = genCoverBook(host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count,
                        updateTime, parameter, extra_parameter, dex);
                SearchHelper.WordInfo wordInfo = wordInfoMap.get(word);
                if(wordInfo != null) {
                    wordInfo.actioned = true;
                    alilog(buildSearch(book, word, Search.OP.BOOKSHELF, wordInfo.computeUseTime()));
                }
                boolean succeed = bookDaoHelper.insertBook(book);
                if (succeed) {
                    Toast.makeText(mContext.getApplicationContext(), R.string.bookshelf_insert_success, Toast.LENGTH_SHORT).show();

                    Map<String, String> data = new HashMap<>();
                    data.put("type","1");
                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SEARCHRESULT_PAGE, StartLogClickUtil.SHELFADD, data);
                }
            }
        });

        jsInterfaceHelper.setOnDeleteBook(new JSInterfaceHelper.OnDeleteBook() {
            @Override
            public void doDeleteBook(String book_id) {
                AppLog.e(TAG, "doDeleteBook");
                bookDaoHelper.deleteBook(book_id);
                Toast.makeText(mContext.getApplicationContext(), R.string.bookshelf_delete_success, Toast.LENGTH_SHORT).show();

                Map<String, String> data = new HashMap<>();
                data.put("type","2");
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SEARCHRESULT_PAGE,StartLogClickUtil.SHELFADD, data);
            }
        });
    }

    protected Book genCoverBook(String host, String book_id, String book_source_id, String name, String author, String status, String category,
                                String imgUrl, String last_chapter, String chapter_count, long update_time, String parameter, String
                                        extra_parameter, int dex) {
        Book book = new Book();

        if (status.equals("FINISH")) {
            book.status = 2;
        } else {
            book.status = 1;
        }

        book.book_id = book_id;
        book.book_source_id = book_source_id;
        book.name = name;
        book.category = category;
        book.author = author;
        book.img_url = imgUrl;
        book.site = host;
        book.last_chapter_name = last_chapter;
        book.chapter_count = Integer.valueOf(chapter_count);
        book.last_updatetime_native = update_time;
        book.parameter = parameter;
        book.extra_parameter = extra_parameter;
        book.dex = dex;
        book.last_updateSucessTime = System.currentTimeMillis();
        AppLog.i(TAG, "book.dex = " + book.dex);
        return book;
    }

    public void startLoadData() {
        String searchWord;
        if (word != null) {
            searchWord = word;
            String channelID = AppUtils.getChannelId();
            if (channelID.equals("blp1298_10882_001") || channelID.equals("blp1298_10883_001") || channelID.equals("blp1298_10699_001")) {
                if (Constants.isBaiduExamine && Constants.versionCode == AppUtils.getVersionCode()) {
                    searchWord = getReplaceWord();
                    AppLog.e(TAG, searchWord);
                }
            }

            Map<String, String> params = new HashMap<>();
            params.put("word", searchWord);
            params.put("search_type", searchType);
            params.put("filter_type", filterType);
            params.put("filter_word", filterWord);
            params.put("sort_type", sortType);
            mUrl = UrlUtils.buildWebUrl(URLBuilderIntterface.SEARCH, params);
        }

        if (mStartLoadCall != null){
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

    public String getReplaceWord() {
        String[] words = {"品质随时购", "春节不打烊", "轻松过大年", "便携无屏电视", "游戏笔记本电脑", "全自动洗衣机", "家团圆礼盒"};
        Random random = new Random();
        int index = random.nextInt(7);
        return words[index];
    }

    public void onDestroy(){
        Set<String> strings = wordInfoMap.keySet();
        for (String key: strings){
            WordInfo wordInfo = wordInfoMap.get(key);
            if(wordInfo != null && !wordInfo.actioned) {
                alilog(buildSearch(key, Search.OP.CANCEL, wordInfo.computeUseTime()));
            }
        }
        wordInfoMap.clear();
    }


    private JsCallSearchCall mJsCallSearchCall;

    public void setJsCallSearchCall(JsCallSearchCall jsCallSearchCall){
        mJsCallSearchCall = jsCallSearchCall;
    }

    public interface JsCallSearchCall{
        void onJsSearch();
    }

    private StartLoadCall mStartLoadCall;

    public void setStartLoadCall(StartLoadCall startLoadCall){
        mStartLoadCall = startLoadCall;
    }

    public interface StartLoadCall{
        void onStartLoad(String url);
    }


}
