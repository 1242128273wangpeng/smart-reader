package net.lzbook.kit.request.own;

import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.encrypt.URLBuilderIntterface;
import net.lzbook.kit.net.volley.request.Parser;
import net.lzbook.kit.net.volley.request.VolleyDataService;
import net.lzbook.kit.request.UrlUtils;

import org.json.JSONException;

import android.content.Context;
import android.os.Handler;

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
                        chapterDao.deleteBookChapters(0);
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
                            chapterDao.deleteBookChapters(0);
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


}
