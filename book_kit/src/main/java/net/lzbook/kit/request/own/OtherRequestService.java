package net.lzbook.kit.request.own;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookUpdate;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.ChapterErrorBean;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.encrypt.URLBuilderIntterface;
import net.lzbook.kit.net.volley.request.Parser;
import net.lzbook.kit.net.volley.request.VolleyDataService;
import net.lzbook.kit.request.DataCache;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.MurmurHash;
import net.lzbook.kit.utils.Tools;

import org.json.JSONException;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class OtherRequestService extends VolleyDataService {

    private static final String BOOK_ID_PARAM = "{book_id}";
    private static final String BOOK_SOURCE_ID_PARAM = "{book_source_id}";

    public static String TAG = OtherRequestService.class.getSimpleName();

    public static void requestBookCover(Handler handler, int resultCode, int errorCode, RequestItem requestItem) {

        String uri = URLBuilderIntterface.COVER.replace(BOOK_ID_PARAM, requestItem.book_id).replace(BOOK_SOURCE_ID_PARAM, requestItem.book_source_id);
        String url = UrlUtils.buildUrl(uri, new HashMap<String, String>());

        publicCode(url, null, handler, resultCode, errorCode, new Parser() {
            @Override
            public Object parserMethod(String response) throws Exception {
                return OWNParser.parserOwnCoverInfo(response);
            }
        });
    }

    public static void requestOwnCatalogList(Handler handler, final Context context, int resultCode, int errorCode,
                                             final RequestItem requestItem) {

        final BookChapterDao chapterDao = new BookChapterDao(context, requestItem.book_id);
        ArrayList<Chapter> chapterList = chapterDao.queryBookChapter();
        if (chapterList == null || chapterList.size() == 0) {

            String uri = URLBuilderIntterface.CHAPTER_LIST.replace(BOOK_ID_PARAM, requestItem.book_id).replace(BOOK_SOURCE_ID_PARAM, requestItem
                    .book_source_id);
            String url = UrlUtils.buildUrl(uri, new HashMap<String, String>());

            publicCode(url, null, handler, resultCode, errorCode, new Parser() {

                @Override
                public Object parserMethod(String response) throws Exception {

                    ArrayList<Chapter> chapterList = OWNParser.parserOwnChapterList(response, requestItem);
                    if (chapterList != null && !chapterList.isEmpty() && BookDaoHelper.getInstance()
                            .isBookSubed(requestItem.book_id)) {
                        chapterDao.insertBookChapter(chapterList);
                        Chapter lastChapter = chapterList.get(chapterList.size() - 1);
                        Book book = new Book();
                        book.book_id = requestItem.book_id;
                        book.book_source_id = requestItem.book_source_id;
                        book.site = requestItem.host;
                        book.chapter_count = chapterDao.getCount();
                        book.last_updatetime_native = lastChapter.time;
                        book.last_chapter_name = lastChapter.chapter_name;
                        book.last_chapter_md5 = lastChapter.book_chapter_md5;
                        book.last_updateSucessTime = System.currentTimeMillis();
                        BookDaoHelper.getInstance().updateBook(book);
                    }
                    return chapterList;
                }
            });

        }
    }

    // 源迁移时请求目录的方法,重新请求切源后的目录
    public static void requestOwnCatalogListNew(final Context context, final Book bookVo, DataServiceCallBack dataServiceCallBack) {

        String uri = URLBuilderIntterface.CHAPTER_LIST.replace(BOOK_ID_PARAM, bookVo.book_id).replace(BOOK_SOURCE_ID_PARAM, bookVo.book_source_id);
        String url = UrlUtils.buildUrl(uri, new HashMap<String, String>());

        publicCode(url, null, dataServiceCallBack, new Parser() {

            @Override
            public Object parserMethod(String response) throws Exception {

                ArrayList<Chapter> chapterList = OWNParser.parserOwnChapterListNew(response, bookVo);
                return chapterList;
            }
        });

    }

    public static void requestOwnChapterList(final Context context, final RequestItem requestItem, final VolleyDataService.DataServiceCallBack
            callback)
            throws Exception {

        final BookChapterDao chapterDao = new BookChapterDao(context, requestItem.book_id);
        ArrayList<Chapter> chapterList = chapterDao.queryBookChapter();
        if (chapterList == null || chapterList.size() == 0) {

            String uri = URLBuilderIntterface.CHAPTER_LIST.replace(BOOK_ID_PARAM, requestItem.book_id).replace(BOOK_SOURCE_ID_PARAM, requestItem
                    .book_source_id);
            String url = UrlUtils.buildUrl(uri, new HashMap<String, String>());

            publicCode(url, null, callback, new Parser() {
                @Override
                public Object parserMethod(String response) throws JSONException, Exception {
                    try {
                        ArrayList<Chapter> chapters = OWNParser.parserOwnChapterList(response, requestItem);
                        if (chapters != null && !chapters.isEmpty() && BookDaoHelper.getInstance().isBookSubed
                                (requestItem.book_id)) {
                            chapterDao.insertBookChapter(chapters);
                            Chapter lastChapter = chapters.get(chapters.size() - 1);
                            Book book = new Book();
                            book.book_id = requestItem.book_id;
                            book.book_source_id = requestItem.book_source_id;
                            book.parameter = requestItem.parameter;
                            book.extra_parameter = requestItem.extra_parameter;
                            book.site = requestItem.host;
                            book.chapter_count = chapterDao.getCount();
                            book.last_updatetime_native = lastChapter.time;
                            book.last_chapter_name = lastChapter.chapter_name;
                            book.last_sort = lastChapter.sort;
                            book.gsort = lastChapter.gsort;
                            book.last_chapter_md5 = lastChapter.book_chapter_md5;
                            book.last_updateSucessTime = System.currentTimeMillis();
                            BookDaoHelper.getInstance().updateBook(book);

                        }
                        return chapters;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });

        } else {
            callback.onSuccess(chapterList);
        }
    }

    // 书架做一键切换时请求每本书的源列表post方式
    public static void requestBooksSource(final HashMap<String, String> parameter, DataServiceCallBack dataServiceCallBack) {

        String uri = URLBuilderIntterface.BOOK_SOURCE_BATCH;
        String url = UrlUtils.buildUrl(uri, parameter);

        publicCode(url, null, dataServiceCallBack, true, new Parser() {
            @Override
            public Object parserMethod(String response) throws JSONException, Exception {
                return OWNParser.parserBooksSource(response, parameter.get("bookIds"));
            }
        });
    }

    public static void requestBookSourceChange(final Handler handler, final int successCode, int erroCode, final String book_id) throws Exception {

        String uri = URLBuilderIntterface.BOOK_SOURCE_SINGLE.replace(BOOK_ID_PARAM, book_id);
        String url = UrlUtils.buildUrl(uri, new HashMap<String, String>());

        publicCode(url, null, handler, successCode, erroCode, new Parser() {
            @Override
            public Object parserMethod(String response) throws JSONException, Exception {
                return OWNParser.parserBookSource(response, book_id);
            }
        });
        return;
    }


    public static void doBookUpdate(final Context context, HashMap<String, String> parameter, final HashMap<String, Book>
            bookItems, DataServiceCallBack dataServiceCallBack) {

        String uri = URLBuilderIntterface.BOOK_CHECK;
        HashMap<String, String> map = new HashMap<String, String>();
        int hash = MurmurHash.hash32(parameter.get("data"));
        map.put("hash", String.valueOf(hash));
        String url = UrlUtils.buildUrl(uri, map);

        publicCode(url, parameter, dataServiceCallBack, new Parser() {
            @Override
            public Object parserMethod(String response) throws Exception {
                ArrayList<BookUpdate> resultLists = OWNParser.parserBookUpdateInfo(response, bookItems);
                ArrayList<BookUpdate> bookUpdateLists = null;
                if (resultLists != null && resultLists.size() > 0) {
                    bookUpdateLists = new ArrayList<BookUpdate>();
                    for (int i = 0; i < resultLists.size(); i++) {
                        BookUpdate bookUpdate = changeChapters(resultLists.get(i));
                        if (bookUpdate != null) {
                            bookUpdateLists.add(bookUpdate);
                        }
                    }
                }
                return bookUpdateLists;
            }


            private synchronized BookUpdate changeChapters(BookUpdate bookUpdate) {
                Book book = bookItems.get(bookUpdate.book_id);
                BookUpdate resUpdate = null;
                if (!bookUpdate.chapterList.isEmpty()) {
                    BookChapterDao bookChapterDao = new BookChapterDao(context, book.book_id);
                    // 增加更新章节
                    bookChapterDao.insertBookChapter(bookUpdate.chapterList);
                    // 更新书架信息
                    Chapter lastchapter = bookUpdate.chapterList.get(bookUpdate.chapterList.size() - 1);
                    book.chapter_count = bookChapterDao.getCount();
                    book.last_updatetime_native = lastchapter.time;
                    book.last_chapter_name = lastchapter.chapter_name;
                    book.last_sort = lastchapter.sort;
                    book.gsort = lastchapter.gsort;
                    book.update_status = 1;

                    // 返回bookUpdate
                    resUpdate = new BookUpdate();
                    resUpdate.book_name = book.name;
                    resUpdate.book_id = book.book_id;
                    resUpdate.last_chapter_name = lastchapter.chapter_name;
                    resUpdate.update_count = bookUpdate.chapterList.size();

                    if (Constants.DEVELOPER_MODE) {
                        StringBuilder update_log = new StringBuilder();
                        update_log.append("book_id : ").append(book.book_id).append(" \\\n");
                        update_log.append("book_source_id : ").append(book.book_source_id).append(" \\\n");
                        update_log.append("book_name : ").append(bookUpdate.book_name).append(" \\\n");
                        update_log.append("update_count_service : ").append(bookUpdate.chapterList.size()).append(" \\\n");
                        update_log.append("update_count_local : ").append(book.chapter_count).append(" \\\n");
                        update_log.append("last_chapter_name_service : ").append(lastchapter.chapter_name).append(" \\\n");
                        update_log.append("last_chapter_name_local : ").append(book.name).append(" \\\n");
                        update_log.append("update_time : ").append(Tools.logTime(AppUtils.log_formatter, lastchapter.time)).append(" \\\n");
                        update_log.append("system_time : ").append(Tools.logTime(AppUtils.log_formatter, System.currentTimeMillis())).append(" \\\n");
                        DataCache.saveUpdateLog(update_log.toString());
                    }
                }
                // 没有返回更新章节的书籍更新book.last_updateUpdateTime, 有更新的书籍更新对应信息
                BookDaoHelper.getInstance().updateBook(book);

                return resUpdate;
            }
        }, 60 * 1000, 0, true);
    }

    public static void getDefaultBook(final Context context, DataServiceCallBack dataServiceCallBack)
            throws IOException, JSONException {

        String uriTag = URLBuilderIntterface.DEFAULT_BOOK;
        String url = UrlUtils.buildUrl(uriTag, new HashMap<String, String>());

        final BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();
        publicCode(url, null, dataServiceCallBack, new Parser() {
            @Override
            public Object parserMethod(String response) throws JSONException, Exception {
                AppLog.e(TAG, "response is :" + response);
                ArrayList<Book> books = OWNParser.parserOwnDefaultBook(response, context);
                for (Book iBook : books) {
                    if (!bookDaoHelper.isBookSubed(iBook.book_id)) {
                        if (bookDaoHelper.insertBook(iBook)) {
                            AppLog.i(TAG, "iBook.last_updateSucessTime = " + iBook.last_updateSucessTime);
                        }
                    }
                }
                return books;
            }
        });
    }

    public static void updateShelfBooks(final Context context, DataServiceCallBack dataServiceCallBack)
            throws IOException, JSONException {
        final BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();
        ArrayList<Book> books = bookDaoHelper.getOwnBooksList();
        if (books == null || books.isEmpty()) {
            return;
        }

        HashMap<String, String> parameter = new HashMap<>();
        StringBuffer idBuffer = new StringBuffer();
        StringBuffer sourceBuffer = new StringBuffer();
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            if (!TextUtils.isEmpty(book.book_id) && !TextUtils.isEmpty(book.book_source_id)) {
                if (i == books.size() - 1) {
                    idBuffer.append(book.book_id);
                    sourceBuffer.append(book.book_source_id);
                } else {
                    idBuffer.append(book.book_id + "$$");
                    sourceBuffer.append(book.book_source_id + "$$");
                }
            }
        }
        parameter.put("book_ids", idBuffer.toString());
        parameter.put("book_source_ids", sourceBuffer.toString());

        String uriTag = URLBuilderIntterface.UPDATE_SHELF_BOOKS;
        String url = UrlUtils.buildUrl(uriTag, parameter);

        publicCode(url, null, dataServiceCallBack, new Parser() {
            @Override
            public Object parserMethod(String response) throws JSONException, Exception {
                AppLog.e(TAG, "response is :" + response);
                ArrayList<Book> books = OWNParser.parserOwnUpdateShelfBooks(response);
                if (books == null || books.isEmpty()) {
                    return null;
                }

                for (Book iBook : books) {
                    if (bookDaoHelper.isBookSubed(iBook.book_id)) {
                        Book book = bookDaoHelper.getBook(iBook.book_id, 0);
                        if (iBook.status == 1 || iBook.status == 2) {
                            book.status = iBook.status;
                        }
                        if (iBook.dex == 0 || iBook.dex == 1) {
                            book.dex = iBook.dex;
                        }
                        if (bookDaoHelper.updateBook(book)) {
                            AppLog.i(TAG, "书架书籍: " + book.name + "dex值,完结/连载状态已更新!");
                        }
                    }
                }
                return books;
            }
        });
    }

    public static void requestDynamicPar(DataServiceCallBack dataServiceCallBack)
            throws IOException, JSONException {

        String uriTag = URLBuilderIntterface.DYNAMIC_PARAMAS;
        String url = UrlUtils.buildDynamicParamasUrl(uriTag, new HashMap<String, String>());

        publicCode(url, null, dataServiceCallBack, new Parser() {
            @Override
            public Object parserMethod(String response) throws JSONException, Exception {
                return OWNParser.parserDynamic(response);
            }
        });
    }

    /**
     * 发送异常日志
     */
    public static void sendLogData(String jsonResult) {
        HashMap<String, String> data = new HashMap<>();
        data.put("data", jsonResult);

        HashMap<String, String> map = new HashMap<String, String>();
        int hash = MurmurHash.hash32(jsonResult);
        map.put("hash", String.valueOf(hash));

        String uri = URLBuilderIntterface.LOG;
        String url = UrlUtils.buildUrl(uri, map);

        publicCode(url, data, null, null);
    }

    /**
     * 发送阅读页反馈信息(章节错误报告)
     */
    public static void sendChapterErrorData(ChapterErrorBean chapterErrorBean) {
        HashMap<String, String> data = new HashMap<>();
        data.put("bookSourceId", chapterErrorBean.bookSourceId);
        data.put("bookName", chapterErrorBean.bookName);
        data.put("author", chapterErrorBean.author);
        data.put("bookChapterId", chapterErrorBean.bookChapterId);
        data.put("chapterId", chapterErrorBean.chapterId);
        data.put("chapterName", chapterErrorBean.chapterName);
        data.put("serial", String.valueOf(chapterErrorBean.serial));
        data.put("host", chapterErrorBean.host);
        data.put("type", String.valueOf(chapterErrorBean.type));

        String uri = URLBuilderIntterface.CHAPTER_ERROR_FEEDBACK;
        String url = UrlUtils.buildUrl(uri, data);
        publicCode(url, null, null, null);
        AppLog.d(TAG, "url = " + url);
    }
}
