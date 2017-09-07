package net.lzbook.kit.request;

import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.net.volley.request.VolleyDataService;

import android.content.Context;
import android.os.Handler;

import java.util.Map;

public interface RequestExecutor {

    int REQUEST_COVER_SUCCESS = 10;
    int REQUEST_COVER_QG_SUCCESS = 20;
    int REQUEST_COVER_QG_ERROR = 21;
    int REQUEST_COVER_ERROR = 11;
    int REQUEST_CATALOG_SUCCESS = 12;
    int REQUEST_QG_CATALOG_SUCCESS = 22;
    int REQUEST_CATALOG_ERROR = 13;
    int REQUEST_QG_CATALOG_ERROR = 23;
    int REQUEST_BOOK_SOURCE_SUCCESS = 14;
    int REQUEST_BOOK_SOURCE_ERROR = 15;
    int REQUEST_CONFIG_SUCCESS = 16;
    int REQUEST_CONFIG_ERROR = 17;

    void requestBookCover(Handler handler, RequestItem requestItem);

    void requestCatalogList(Context context, Handler handler, RequestItem requestItem);

    void requestChapterList(Context context, RequestItem requestItem, VolleyDataService.DataServiceCallBack dataServiceCallBack) throws Exception;

    void requestUpdate(Context context, RequestItem requestItem, int update_count, long update_time,
                       VolleyDataService.DataServiceCallBack dataServiceCallBack) throws Exception;

    Chapter requestSingleChapter(int dex, BookDaoHelper bookDaoHelper, BookChapterDao bookChapterDao, Chapter
            chapter) throws Exception;

    void requestBatchChapter(int dex, BookDaoHelper bookDaoHelper, BookChapterDao bookChapterDao, boolean
            downloadFlag, Map<String, Chapter> chapterMap) throws Exception;

    Chapter requestSingleChapter(int dex, Chapter chapter) throws Exception;

    void requestBatchChapter(int dex, boolean downloadFlag, Map<String, Chapter> chapterMap) throws Exception;


}
