package net.lzbook.kit.request;


import com.quduquxie.network.DataCache;
import com.quduquxie.network.DataService;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.net.volley.request.VolleyDataService;
import net.lzbook.kit.utils.BeanParser;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.OpenUDID;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Map;

public class QGRequestExecutor extends RequestExecutorDefault {

    @Override
    public void requestBookCover(Handler handler, RequestItem requestItem) {

    }

    @Override
    public void requestCatalogList(final Context context, final Handler handler, final RequestItem requestItem) {
        mContext = context;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<com.quduquxie.bean.Chapter> chapters = null;
                try {
                    String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
                    chapters = DataService.getChapterList(mContext, requestItem.book_id, 1, Integer.MAX_VALUE - 1, udid);
                    ArrayList<Chapter> list = BeanParser.buildOWNChapterList(chapters, 0, chapters.size());
                    if (list != null && list.size() > 0) {
                        Message.obtain(handler, RequestExecutor.REQUEST_QG_CATALOG_SUCCESS, list).sendToTarget();
                    } else {
                        Message.obtain(handler, RequestExecutor.REQUEST_QG_CATALOG_ERROR, list).sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void requestChapterList(final Context context, final RequestItem requestItem, final VolleyDataService.DataServiceCallBack requestChaptersCallBack) throws Exception {
        mContext = context;
        if (NetWorkUtils.getNetWorkType(mContext) == NetWorkUtils.NETWORK_NONE) {
            BookChapterDao bookChapterDao = new BookChapterDao(context, requestItem.book_id);
            ArrayList<Chapter> chapterList = bookChapterDao.queryBookChapter();
            if (chapterList.size() != 0) {
                requestChaptersCallBack.onSuccess(chapterList);
                return;
            } else {
                mRquestChaptersListener.requestFailed(RequestChaptersListener.ERROR_TYPE_NETWORK_NONE, "拉取章节时无网络", 0);
                return;
            }
        }
        final BookChapterDao chapterDao = new BookChapterDao(context, requestItem.book_id);

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Chapter> list = chapterDao.queryBookChapter();
                if (list == null || list.size() == 0) {
                    ArrayList<com.quduquxie.bean.Chapter> chapters = null;
                    try {
                        String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
                        chapters = DataService.getChapterList(context, requestItem.book_id, 1, Integer.MAX_VALUE - 1, udid);
                        ArrayList<Chapter> chapterList = BeanParser.buildOWNChapterList(chapters, 0, chapters.size());
                        //数据库操作
                        if (chapterList != null && !chapterList.isEmpty() && BookDaoHelper.getInstance(context).isBookSubed
                                (requestItem.book_id)) {
                            chapterDao.insertBookChapter(chapterList);
                            Chapter lastChapter = chapterList.get(chapterList.size() - 1);
                            Book book = new Book();
                            book.book_id = requestItem.book_id;
                            book.book_source_id = requestItem.book_source_id;
                            book.parameter = requestItem.parameter;
                            book.extra_parameter = requestItem.extra_parameter;
                            book.chapter_count = chapterDao.getCount();
                            book.last_updatetime_native = lastChapter.time;
                            book.last_chapter_name = lastChapter.chapter_name;
                            book.last_updateSucessTime = System.currentTimeMillis();
                            book.last_sort = lastChapter.sort;
                            book.gsort = lastChapter.gsort;
                            book.last_chapter_md5 = lastChapter.book_chapter_md5;
                            BookDaoHelper.getInstance(context).updateBook(book);

                        }
                        requestChaptersCallBack.onSuccess(chapterList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    requestChaptersCallBack.onSuccess(list);
                }
            }

        }).start();

    }

    @Override
    public void requestUpdate(Context context, RequestItem requestItem, int update_count, long update_time, VolleyDataService.DataServiceCallBack dataServiceCallBack) throws Exception {

    }

    /**
     * 获取单章节内容
     */
    @Override
    public Chapter requestSingleChapter(int dex, BookDaoHelper bookDaoHelper, BookChapterDao bookChapterDao, Chapter chapter) throws Exception {
        boolean isChapterExist = com.quduquxie.network.DataCache.isChapterExists(chapter.chapter_id, chapter.book_id);
        if (isChapterExist) {
            chapter.content = DataCache.getChapterFromCache(chapter.chapter_id, chapter.book_id);
            chapter.isSuccess = true;
            return chapter;
        } else {
            String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
            com.quduquxie.bean.Chapter qgChapter = DataService.getChapterFromNet(mContext, BeanParser.parseToQGBean(chapter), udid);
            return BeanParser.parseToOWNBean(qgChapter);
        }
    }

    @Override
    public void requestBatchChapter(int dex, BookDaoHelper bookDaoHelper, BookChapterDao bookChapterDao, boolean downloadFlag, Map<String, Chapter> chapterMap) throws Exception {

    }

    @Override
    public Chapter requestSingleChapter(int dex, Chapter chapter) throws Exception {
        return null;
    }

    @Override
    public void requestBatchChapter(int dex, boolean downloadFlag, Map<String, Chapter> chapterMap) throws Exception {

    }


}
