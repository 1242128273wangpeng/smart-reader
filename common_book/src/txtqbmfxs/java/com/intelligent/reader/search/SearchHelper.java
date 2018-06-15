package com.intelligent.reader.search;

import com.intelligent.reader.R;
import com.intelligent.reader.activity.CoverPageActivity;
import com.intelligent.reader.activity.FindBookDetail;
import com.intelligent.reader.activity.ReadingActivity;
import com.intelligent.reader.activity.SearchBookActivity;
import com.intelligent.reader.read.help.BookHelper;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.data.search.SearchAutoCompleteBeanYouHua;
import net.lzbook.kit.data.search.SearchCommonBeanYouHua;
import net.lzbook.kit.encrypt.URLBuilderIntterface;
import net.lzbook.kit.net.custom.service.NetService;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.statistic.model.Search;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.FootprintUtils;
import net.lzbook.kit.utils.JSInterfaceHelper;
import net.lzbook.kit.utils.oneclick.AntiShake;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static net.lzbook.kit.statistic.StatisticKt.alilog;
import static net.lzbook.kit.statistic.StatisticUtilKt.buildSearch;

/**
 * Created by yuchao on 2017/8/2 0002.
 */

public class SearchHelper {
    private static final String TAG = SearchHelper.class.getSimpleName();
    private BookDaoHelper bookDaoHelper;
    private Map<String, WordInfo> wordInfoMap = new HashMap<>();
    private SharedPreferences sharedPreferences;

    private String word;
    private String searchType = "0";
    private String filterType = "0";
    private String filterWord = "ALL";
    private String sortType = "0";
    private String mUrl;
    private String fromClass;

    private Context mContext;
    private String url_tag;
    private Disposable disposable;
//    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public SearchHelper(Context context){
        mContext = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (bookDaoHelper == null) {
            bookDaoHelper = BookDaoHelper.getInstance();
        }
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

    public void setJsNoneResultSearchCall(JsNoneResultSearchCall jsNoneResultSearchCall){
        this.jsNoneResultSearchCall = jsNoneResultSearchCall;
    }

    public void setSearchSuggestCallBack(SearchSuggestCallBack ssb){
        searchSuggestCallBack = ssb;
    }

    public interface JsNoneResultSearchCall{
        void onNoneResultSearch(String searchWord);
    }

    public interface SearchSuggestCallBack{
        void onSearchResult(List<Object> suggestList, SearchAutoCompleteBeanYouHua transmitBean);
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

    public void setInitType(Intent intent){
        word = intent.getStringExtra("word");
        fromClass = intent.getStringExtra("from_class");
        searchType = intent.getStringExtra("search_type");
        filterType = intent.getStringExtra("filter_type");
        filterWord = intent.getStringExtra("filter_word");
        sortType = intent.getStringExtra("sort_type");

    }

    private AntiShake shake = new AntiShake();
    public void initJSHelp(JSInterfaceHelper jsInterfaceHelper) {

        if (jsInterfaceHelper == null){
            return;
        }

        jsInterfaceHelper.setOnSearchClick(new JSInterfaceHelper.onSearchClick() {

            @Override
            public void doSearch(String keyWord, String search_type, String filter_type, String filter_word, String sort_type) {

                AppLog.e("aaa", "aaaa");
                word = keyWord;
                searchType = search_type;
                filterType = filter_type;
                filterWord = filter_word;
                sortType = sort_type;

                startLoadData(0);

                if (mJsCallSearchCall != null){
                    mJsCallSearchCall.onJsSearch();
                }
            }
        });

        jsInterfaceHelper.setOnEnterCover(new JSInterfaceHelper.onEnterCover() {

            @Override
            public void doCover(String host, String book_id, String book_source_id, String name, String author, String parameter,
                                String extra_parameter) {

                AppLog.e(TAG, "doCover");
                Map<String, String> data = new HashMap<>();
                data.put("BOOKID", book_id);
                data.put("source", "WEBVIEW");
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ENTER, data);

                RequestItem requestItem = new RequestItem();
                requestItem.book_id = book_id;
                requestItem.book_source_id = book_source_id;
                requestItem.host = host;
                requestItem.name = name;
                requestItem.author = author;

                WordInfo wordInfo = wordInfoMap.get(word);
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

        jsInterfaceHelper.setOnAnotherWebClick(new JSInterfaceHelper.onAnotherWebClick() {

            @Override
            public void doAnotherWeb(String url, String name) {
                if (shake.check()) {
                    return;
                }
                AppLog.e(TAG, "doAnotherWeb");
                try {
                    if(url.contains(URLBuilderIntterface.AUTHOR_V4)){
                        sharedPreferences.edit().putString(Constants.FINDBOOK_SEARCH, "author").apply();//FindBookDetail 返回键时标识
                    }
                    Intent intent = new Intent();
                    intent.setClass(mContext, FindBookDetail.class);
                    intent.putExtra("url", url);
                    intent.putExtra("title", name);
                    mContext.startActivity(intent);
                    AppLog.e(TAG, "EnterAnotherWeb");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        jsInterfaceHelper.setSearchWordClick(new JSInterfaceHelper.onSearchWordClick() {
            @Override
            public void sendSearchWord(String searchWord, String search_type) {
                word = searchWord;
                searchType = search_type;

                startLoadData(0);

                if(jsNoneResultSearchCall != null){
                    jsNoneResultSearchCall.onNoneResultSearch(searchWord);
                }

            }
        });

        jsInterfaceHelper.setOnTurnRead(new JSInterfaceHelper.onTurnRead() {
            @Override
            public void turnRead(String book_id, String book_source_id, String host, String name, String author, String parameter, String extra_parameter, String update_type, String last_chapter_name, int serial_number, String img_url, long update_time, String desc, String label, String status, String bookType) {

                Intent intent = new Intent();
                Bundle bundle = new Bundle();

                bundle.putInt("sequence",0);
                bundle.putInt("offset", 0);

                Book book = new Book();
                book.book_id = book_id;
                book.book_source_id = book_source_id;
                book.site = host;
                book.author = author;
                book.name = name;
                book.parameter = parameter;
                book.extra_parameter = extra_parameter;
                book.last_chapter_name = last_chapter_name;
                book.chapter_count = serial_number;
                book.img_url = img_url;
                book.last_updatetime_native = update_time;
                book.sequence = -1;
                book.desc = desc;
                book.category = label;
                if ("FINISH".equals(status)) {
                    book.status = 2;
                } else {
                    book.status = 1;
                }
//                book.mBookType = Integer.parseInt(bookType);
                //bookType为是否付费书籍标签 除快读外不加

                bundle.putSerializable("book", book);
                FootprintUtils.saveHistoryShelf(book);
                RequestItem requestItem = new RequestItem();
                requestItem.book_id = book_id;
                requestItem.book_source_id = book_source_id;
                requestItem.host = host;
                requestItem.parameter = parameter;
                requestItem.extra_parameter = extra_parameter;
                requestItem.name = name;
                requestItem.author = author;
//                requestItem.mBookType = Integer.parseInt(bookType);
                bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);

                bundle.putSerializable("book", book);

                AppLog.e(TAG, "GotoReading: " + book.site + " : " + requestItem.host);
                intent.setClass(mContext, ReadingActivity.class);
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
                WordInfo wordInfo = wordInfoMap.get(word);
                if(wordInfo != null) {
                    wordInfo.actioned = true;
                    alilog(buildSearch(book, word, Search.OP.BOOKSHELF, wordInfo.computeUseTime()));
                }
                boolean succeed = bookDaoHelper.insertBook(book);
                if (succeed) {
                    Toast.makeText(mContext.getApplicationContext(), R.string.bookshelf_insert_success, Toast.LENGTH_SHORT).show();
                }
            }
        });

        jsInterfaceHelper.setOnDeleteBook(new JSInterfaceHelper.OnDeleteBook() {
            @Override
            public void doDeleteBook(String book_id) {
                AppLog.e(TAG, "doDeleteBook");
                bookDaoHelper.deleteBook(book_id);
                Toast.makeText(mContext.getApplicationContext(), R.string.bookshelf_delete_success, Toast.LENGTH_SHORT).show();
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

    public void startLoadData(int isAuthor) {
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
//
            if(searchType.equals("2") && isAuthor == 1){

                Map<String, String> params = new HashMap<>();
                params.put("author", searchWord);
                mUrl = URLBuilderIntterface.AUTHOR_V4+"?author="+searchWord;
                try {
                    sharedPreferences.edit().putString(Constants.FINDBOOK_SEARCH, "author").apply();//FindBookDetail 返回键时标识
                    SearchBookActivity.Companion.setIsSatyHistory(true);
                    Intent intent = new Intent();
                    intent.setClass(mContext, FindBookDetail.class);
                    intent.putExtra("url", mUrl);
                    intent.putExtra("title", "作者主页");
                    fromClass = "findBookDetail";
                    mContext.startActivity(intent);
                    AppLog.e(TAG, "EnterAnotherWeb");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else{
                Map<String, String> params = new HashMap<>();
                params.put("keyword", searchWord);
                params.put("search_type", searchType);
                params.put("filter_type", filterType);
                params.put("filter_word", filterWord);
                params.put("sort_type", sortType);
                params.put("wordType", searchType);
                params.put("searchEmpty", "1");
                AppLog.e("kk",searchWord+"=="+searchType+"=="+filterType+"=="+filterWord+"==="+sortType);
                mUrl = UrlUtils.buildWebUrl(URLBuilderIntterface.SEARCH_V4, params);
            }

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

    /**
     *根据策略，显示数据的顺序为 两个书名 + 一个间隔 + 两个作者 + 一个间隔 + 两个标签 + 一个间隔 + 剩余书名
     */
    public void packageData(SearchAutoCompleteBeanYouHua bean) {
        List<Object> resultSuggest = new ArrayList<>();
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_search_history_gap, null, false);
        //两个书名
        if(bean.getData().getName()!=null&&bean.getData().getName().size() > 0){
            for(int i=0;i< ((bean.getData().getName().size() >= 2) ? 2 : bean.getData().getName().size()); i++){
                SearchAutoCompleteBeanYouHua.DataBean.NameBean nameBean = bean.getData().getName().get(i);
                if(nameBean!=null){
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
                    searchCommonBean.setBookType(nameBean.getVip()+"");
                    //------------------------------------------------------------

                    resultSuggest.add(searchCommonBean);
                }
            }
            resultSuggest.add(view);
        }

        if(bean.getData().getAuthors()!=null && bean.getData().getAuthors().size() > 0){

            //两个作者
            for(int i=0;i< ((bean.getData().getAuthors().size() >= 2) ? 2 : bean.getData().getAuthors().size());i++){
                SearchAutoCompleteBeanYouHua.DataBean.AuthorsBean authorsBean = bean.getData().getAuthors().get(i);
                if(authorsBean!=null){
                    SearchCommonBeanYouHua searchCommonBean = new SearchCommonBeanYouHua();
                    searchCommonBean.setSuggest(bean.getData().getAuthors().get(i).getSuggest());
                    searchCommonBean.setWordtype(bean.getData().getAuthors().get(i).getWordtype());
                    searchCommonBean.setImage_url("");
                    searchCommonBean.setIsAuthor(bean.getData().getAuthors().get(i).getIsAuthor());
                    resultSuggest.add(searchCommonBean);

                }
            }
            resultSuggest.add(view);
        }

        if(bean.getData().getLabel()!=null && bean.getData().getLabel().size() > 0){

            //两个标签
            for(int i=0;i< ((bean.getData().getLabel().size() >= 2) ? 2 : bean.getData().getLabel().size());i++){
                SearchAutoCompleteBeanYouHua.DataBean.LabelBean labelBean = bean.getData().getLabel().get(i);
                if(labelBean!=null){
                    SearchCommonBeanYouHua searchCommonBean = new SearchCommonBeanYouHua();
                    searchCommonBean.setSuggest(bean.getData().getLabel().get(i).getSuggest());
                    searchCommonBean.setWordtype(bean.getData().getLabel().get(i).getWordtype());
                    searchCommonBean.setImage_url("");
                    resultSuggest.add(searchCommonBean);

                }
            }
            resultSuggest.add(view);
        }else{
            if(bean.getData().getAuthors() ==null || (bean.getData().getAuthors() != null && bean.getData().getAuthors().size() == 0)) {
                resultSuggest.remove(view);
            }
        }

        //其余书名
        if(bean.getData().getName()!=null){
            for(int i = 2; i < bean.getData().getName().size(); i++){
                SearchCommonBeanYouHua searchCommonBean = new SearchCommonBeanYouHua();
                SearchAutoCompleteBeanYouHua.DataBean.NameBean nameBean = bean.getData().getName().get(i);
                if(nameBean!=null){
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
                    searchCommonBean.setBookType(nameBean.getVip()+"");
                    //------------------------------------------------------------

                    resultSuggest.add(searchCommonBean);
                }
            }
        }

        for(Object bean1:resultSuggest){
            AppLog.e("uuu",bean1.toString());
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

    public void onDestroy(){
        Set<String> strings = wordInfoMap.keySet();
        for (String key: strings){
            WordInfo wordInfo = wordInfoMap.get(key);
            if(wordInfo != null && !wordInfo.actioned) {
                alilog(buildSearch(key, Search.OP.CANCEL, wordInfo.computeUseTime()));
            }
        }
        wordInfoMap.clear();
        recycleDisposable();
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

    private void recycleDisposable(){
        if(disposable!=null&&!disposable.isDisposed()){
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
        recycleDisposable();
        disposable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext(finalQuery);
                e.onComplete();
            }
        }).debounce(400, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io()).switchMap(new Function<String, ObservableSource<SearchAutoCompleteBeanYouHua>>() {
            @Override
            public ObservableSource<SearchAutoCompleteBeanYouHua> apply(String s) throws Exception {
                return NetService.INSTANCE.getOwnSearchService().searchAutoCompleteSecond(s);
            }
        }).observeOn(AndroidSchedulers.mainThread()) .subscribe(new Consumer<SearchAutoCompleteBeanYouHua>() {
            @Override
            public void accept(SearchAutoCompleteBeanYouHua bean) throws Exception {
                                if (SearchAutoCompleteBeanYouHua.REQUESR_SUCCESS.equals(bean.getRespCode()) && bean.getData() != null) {
                    packageData(bean);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                AppLog.e("rxjava", "error");
            }
        });
    }
}
