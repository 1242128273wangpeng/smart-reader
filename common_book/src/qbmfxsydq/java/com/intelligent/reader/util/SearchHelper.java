package com.intelligent.reader.util;

import static net.lzbook.kit.utils.statistic.StatisticKt.alilog;
import static net.lzbook.kit.utils.statistic.StatisticUtilKt.buildSearch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.Chapter;
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua;
import com.ding.basic.bean.SearchCommonBeanYouHua;
import com.ding.basic.repository.RequestRepositoryFactory;
import com.ding.basic.request.RequestService;
import com.ding.basic.request.RequestSubscriber;

import net.lzbook.kit.base.BaseBookApplication;
import net.lzbook.kit.utils.book.FootprintUtils;
import net.lzbook.kit.utils.download.CacheManager;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.router.RouterConfig;
import net.lzbook.kit.utils.router.RouterUtil;
import com.google.gson.JsonObject;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.CoverPageActivity;
import com.intelligent.reader.activity.FindBookDetail;
import com.intelligent.reader.activity.SearchBookActivity;
import com.intelligent.reader.view.SearchSubBookDialog;
import com.orhanobut.logger.Logger;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.utils.statistic.model.Search;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.oneclick.AntiShake;
import net.lzbook.kit.utils.toast.CommonUtil;
import net.lzbook.kit.utils.webview.JSInterfaceHelper;
import net.lzbook.kit.utils.webview.UrlUtils;

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

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;


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
    private SearchSubBookDialog subBookDialog;

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
                bundle.putString("author", author);
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

        jsInterfaceHelper.setSubSearchBook(new JSInterfaceHelper.OnSubSearchBook() {
            @Override
            public void showSubSearchBook(String word) {
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.NORESULT_PAGE, StartLogClickUtil.FEEDBACK);
                subBookDialog = new SearchSubBookDialog(mContext);
                try {
                    if(!subBookDialog.isShow()){
                        subBookDialog.show();
                    }
                    subBookDialog.setBookName(word);
                    subBookDialog.setOnConfirmListener(new Function2<String, String, Unit>() {
                        @Override
                        public Unit invoke(String bookName, String bookAuthor) {
                            Map<String,String> data = new HashMap<>();
                            data.put("type",2+"");
                            data.put("name",bookName+"");
                            data.put("author",bookAuthor+"");
                            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.FEEDBACK_PAGE, StartLogClickUtil.SUBMIT,data);
                            submitSubBook(bookName,bookAuthor);
                            subBookDialog.dismiss();
                            return null;
                        }
                    });
                    subBookDialog.setOnCancelListener(new Function0<Unit>() {
                        @Override
                        public Unit invoke() {
                            Map<String,String> data = new HashMap<>();
                            data.put("type",1+"");
                            data.put("name","");
                            data.put("author","");
                            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.FEEDBACK_PAGE, StartLogClickUtil.SUBMIT,data);
                            return null;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }




            }
        });
    }

    public void submitSubBook(String bookName,String bookAuthor){
        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(mContext).requestSubBook(
                bookName, bookAuthor, new RequestSubscriber<JsonObject>() {
                    @Override
                    public void requestResult(@Nullable JsonObject result) {
                        if(result != null && "20000".equals(result.get("respCode").getAsString()) && subBookDialog != null){
                            subBookDialog.showResult();
                        }
                    }

                    @Override
                    public void requestError(@NotNull String message) {
                        CommonUtil.showToastMessage("订阅失败");
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
                params.put("author", searchWord);
                mUrl = RequestService.AUTHOR_h5 + "?author=" + searchWord;
                try {
                    sharedPreferences.edit().putString(Constants.FINDBOOK_SEARCH,
                            "author").apply();//FindBookDetail 返回键时标识
                    SearchBookActivity.Companion.setStayHistory(true);
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

            } else {
                Map<String, String> params = new HashMap<>();
                params.put("keyword", searchWord);
                params.put("search_type", searchType);
                params.put("filter_type", filterType);
                params.put("filter_word", filterWord);
                params.put("sort_type", sortType);
                params.put("wordType", searchType);
                params.put("searchEmpty", "1");
                AppLog.e("kk",
                        searchWord + "==" + searchType + "==" + filterType + "==" + filterWord
                                + "===" + sortType);
                mUrl = UrlUtils.buildWebUrl(RequestService.SEARCH_V4, params);
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
    private void packageData(SearchAutoCompleteBeanYouHua bean) {
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

    private String getReplaceWord() {
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

    public void startSearch(String query) {
        if (query != null && !TextUtils.isEmpty(query)) {
            try {
                query = URLDecoder.decode(query, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        final String finalQuery = query;

        if (!TextUtils.isEmpty(finalQuery)) {
            RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                    BaseBookApplication.getGlobalContext()).requestAutoCompleteV5(

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
}
