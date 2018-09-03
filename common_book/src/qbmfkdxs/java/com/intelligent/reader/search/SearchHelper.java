package com.intelligent.reader.search;

import static net.lzbook.kit.statistic.StatisticKt.alilog;
import static net.lzbook.kit.statistic.StatisticUtilKt.buildSearch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.Chapter;
import com.ding.basic.bean.SearchAutoCompleteBean;
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua;
import com.ding.basic.bean.SearchCommonBeanYouHua;
import com.ding.basic.repository.RequestRepositoryFactory;
import com.ding.basic.request.RequestService;
import com.ding.basic.request.RequestSubscriber;
import com.dingyue.contract.router.RouterConfig;
import com.dingyue.contract.router.RouterUtil;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.CoverPageActivity;
import com.intelligent.reader.activity.FindBookDetail;
import com.intelligent.reader.activity.SearchBookActivity;
import com.orhanobut.logger.Logger;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.encrypt.URLBuilderIntterface;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.statistic.model.Search;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.FootprintUtils;
import net.lzbook.kit.utils.JSInterfaceHelper;
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
        void onSearchResult(ArrayList<SearchCommonBeanYouHua> suggestList, SearchAutoCompleteBean transmitBean);
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

    public void initJSHelp(JSInterfaceHelper jsInterfaceHelper) {

        if (jsInterfaceHelper == null) {
            return;
        }

        jsInterfaceHelper.setOnSearchClick(new JSInterfaceHelper.onSearchClick() {

            @Override
            public void doSearch(String keyWord, String search_type, String filter_type,
                    String filter_word, String sort_type) {

                AppLog.e("aaa", "aaaa");
                word = keyWord;
                searchType = search_type;
                filterType = filter_type;
                filterWord = filter_word;
                sortType = sort_type;

                startLoadData(0);

                if (mJsCallSearchCall != null) {
                    mJsCallSearchCall.onJsSearch();
                }
            }
        });

        jsInterfaceHelper.setOnEnterCover(new JSInterfaceHelper.onEnterCover() {

            @Override
            public void doCover(String host, String book_id, String book_source_id, String name,
                    String author, String parameter,
                    String extra_parameter) {

                AppLog.e(TAG, "doCover");
                Map<String, String> data = new HashMap<>();
                data.put("BOOKID", book_id);
                data.put("source", "WEBVIEW");
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.BOOOKDETAIL_PAGE,
                        StartLogClickUtil.ENTER, data);

                Book book = new Book();
                book.setBook_id(book_id);
                book.setBook_source_id(book_source_id);
                book.setHost(host);
                book.setName(name);
                book.setAuthor(author);

                WordInfo wordInfo = wordInfoMap.get(word);
                if (wordInfo != null) {
                    wordInfo.actioned = true;
                    alilog(buildSearch(book, word, Search.OP.COVER,
                            wordInfo.computeUseTime()));
                }
                Intent intent = new Intent();
                intent.setClass(mContext, CoverPageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("book_id", book_id);
                bundle.putString("book_source_id", book_source_id);
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
                    if (url.contains(RequestService.AUTHOR_V4)) {
                        sharedPreferences.edit().putString(Constants.FINDBOOK_SEARCH,
                                "author").apply();//FindBookDetail 返回键时标识
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

                if (jsNoneResultSearchCall != null) {
                    jsNoneResultSearchCall.onNoneResultSearch(searchWord);
                }

            }
        });

        jsInterfaceHelper.setOnTurnRead(new JSInterfaceHelper.onTurnRead() {
            @Override
            public void turnRead(String book_id, String book_source_id, String host, String name,
                    String author, String parameter, String extra_parameter, String update_type,
                    String last_chapter_name, int serial_number, String img_url, long update_time,
                    String desc, String label, String status, String bookType) {


                Book book = new Book();
                book.setBook_id(book_id);
                book.setBook_source_id(book_source_id);
                book.setHost(host);
                book.setAuthor(author);
                book.setName(name);
                Chapter chapter = new Chapter();
                chapter.setName(last_chapter_name);
                book.setLast_chapter(chapter);
                book.setChapter_count(serial_number);
                book.setImg_url(img_url);
                book.setLast_update_success_time(update_time);
                book.setSequence(-1);
                book.setDesc(desc);
                book.setLabel(label);
                book.setStatus(status);

//                book.mBookType = Integer.parseInt(bookType);
                //bookType为是否付费书籍标签 除快读外不加

                FootprintUtils.saveHistoryShelf(book);

                Bundle bundle = new Bundle();

                bundle.putInt("sequence", 0);
                bundle.putInt("offset", 0);
                bundle.putSerializable("book", book);
                int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP;
                RouterUtil.INSTANCE.navigation(mContext, RouterConfig.READER_ACTIVITY, bundle,
                        flags);
            }
        });

        jsInterfaceHelper.setOnEnterRead(new JSInterfaceHelper.onEnterRead() {
            @Override
            public void doRead(final String host, final String book_id, final String book_source_id,
                    final String name, final String author, final
            String status, final String category, final String imgUrl, final String last_chapter,
                    final String chapter_count, final long
                    updateTime, final String parameter, final String extra_parameter,
                    final int dex) {
                AppLog.e(TAG, "doRead");
                Book coverBook = genCoverBook(host, book_id, book_source_id, name, author, status,
                        category, imgUrl, last_chapter, chapter_count,
                        updateTime, parameter, extra_parameter, dex);
                AppLog.e(TAG, "DoRead : " + coverBook.getSequence());

                Bundle bundle = new Bundle();
                bundle.putInt("sequence", coverBook.getSequence());
                bundle.putInt("offset", coverBook.getOffset());
                bundle.putSerializable("book", coverBook);
                int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP;
                RouterUtil.INSTANCE.navigation(mContext, RouterConfig.READER_ACTIVITY, bundle,
                        flags);

            }
        });

        List<Book> booksOnLine = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).loadBooks();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (int i = 0; i < booksOnLine.size(); i++) {
            stringBuilder.append("{'id':'").append(booksOnLine.get(i).getBook_id()).append("'}");
            if (i != booksOnLine.size() - 1) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("]");
        AppLog.e(TAG, "StringBuilder : " + stringBuilder.toString());
        jsInterfaceHelper.setBookString(stringBuilder.toString());

        jsInterfaceHelper.setOnInsertBook(new JSInterfaceHelper.OnInsertBook() {
            @Override
            public void doInsertBook(final String host, final String book_id,
                    final String book_source_id, final String name, final String author,
                    final String status, final String category, final String imgUrl,
                    final String last_chapter, final String
                    chapter_count, final long updateTime, final String parameter,
                    final String extra_parameter, final int
                    dex) {
                AppLog.e(TAG, "doInsertBook");
                Book book = genCoverBook(host, book_id, book_source_id, name, author, status,
                        category, imgUrl, last_chapter, chapter_count,
                        updateTime, parameter, extra_parameter, dex);
                WordInfo wordInfo = wordInfoMap.get(word);
                if (wordInfo != null) {
                    wordInfo.actioned = true;
                    alilog(buildSearch(book, word, Search.OP.BOOKSHELF, wordInfo.computeUseTime()));
                }
                long succeed = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                        BaseBookApplication.getGlobalContext()).insertBook(book);
                if (succeed > 0) {
                    Toast.makeText(mContext.getApplicationContext(),
                            R.string.bookshelf_insert_success, Toast.LENGTH_SHORT).show();
                }
            }
        });

        jsInterfaceHelper.setOnDeleteBook(new JSInterfaceHelper.OnDeleteBook() {
            @Override
            public void doDeleteBook(String book_id) {
                AppLog.e(TAG, "doDeleteBook");
                RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                        BaseBookApplication.getGlobalContext()).deleteBook(book_id);
                CacheManager.INSTANCE.stop(book_id);
                CacheManager.INSTANCE.resetTask(book_id);
                Toast.makeText(mContext.getApplicationContext(), R.string.bookshelf_delete_success,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

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
            AppLog.e("kk",searchWord+"=="+searchType+"=="+filterType+"=="+filterWord+"==="+sortType);
            mUrl = UrlUtils.buildWebUrl(RequestService.SEARCH_V4, params);
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

        if (finalQuery != null && !TextUtils.isEmpty(finalQuery)) {
            RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestAutoComplete(finalQuery, new RequestSubscriber<SearchAutoCompleteBean>() {
                @Override
                public void requestResult(@Nullable SearchAutoCompleteBean result) {
                    ArrayList<SearchCommonBeanYouHua> resultSuggest = new ArrayList<SearchCommonBeanYouHua>();
                    resultSuggest.clear();
                    AppLog.e("bean", result.toString());
                    if (result != null && "200".equals(result.getSuc())&& result.getData() != null) {
                        for (int i = 0; i < result.getData().getAuthors().size(); i++) {
                            SearchCommonBeanYouHua searchCommonBean = new SearchCommonBeanYouHua();
                            searchCommonBean.setSuggest(result.getData().getAuthors().get(i).getSuggest());
                            searchCommonBean.setWordtype(result.getData().getAuthors().get(i).getWordtype());
                            resultSuggest.add(searchCommonBean);
                        }
                        for (int i = 0; i < result.getData().getLabel().size(); i++) {
                            SearchCommonBeanYouHua searchCommonBean = new SearchCommonBeanYouHua();
                            searchCommonBean.setSuggest(result.getData().getLabel().get(i).getSuggest());
                            searchCommonBean.setWordtype(result.getData().getLabel().get(i).getWordtype());
                            resultSuggest.add(searchCommonBean);
                        }
                        for (int i = 0; i < result.getData().getName().size(); i++) {
                            SearchCommonBeanYouHua searchCommonBean = new SearchCommonBeanYouHua();
                            searchCommonBean.setSuggest(result.getData().getName().get(i).getSuggest());
                            searchCommonBean.setWordtype(result.getData().getName().get(i).getWordtype());
                            resultSuggest.add(searchCommonBean);
                        }


                        if (searchSuggestCallBack != null && result != null) {

                            searchSuggestCallBack.onSearchResult(resultSuggest, result);
                        }
                    }
                }

                @Override
                public void requestError(@NotNull String message) {

                }
            });
        }

    }
}
