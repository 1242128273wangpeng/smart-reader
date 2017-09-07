package net.lzbook.kit.request;

import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.net.volley.request.VolleyDataService;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Map;

public class RequestExecutorDefault implements RequestExecutor {

    public static Context mContext;

    public static RequestChaptersListener mRquestChaptersListener;

    @Override
    public void requestBookCover(Handler handler, RequestItem requestItem) {

    }

    @Override
    public void requestCatalogList(Context context, Handler handler, RequestItem requestItem) {

    }

    @Override
    public void requestChapterList(Context context, RequestItem requestItem, VolleyDataService.DataServiceCallBack dataServiceCallBack) throws Exception {
//        return null;
    }

    @Override
    public void requestUpdate(Context context, RequestItem requestItem, int update_count, long update_time,
                              VolleyDataService.DataServiceCallBack dataServiceCallBack) throws Exception {

    }

    @Override
    public Chapter requestSingleChapter(int dex, BookDaoHelper bookDaoHelper, BookChapterDao bookChapterDao, Chapter chapter)
            throws Exception {
        return null;
    }

    @Override
    public void requestBatchChapter(int dex, BookDaoHelper bookDaoHelper, BookChapterDao bookChapterDao, boolean downloadFlag,
                                    Map<String, Chapter> chapterMap) throws Exception {

    }

    @Override
    public Chapter requestSingleChapter(int dex, Chapter chapter) throws Exception {
        return null;
    }

    @Override
    public void requestBatchChapter(int dex, boolean downloadFlag, Map<String, Chapter> chapterMap) throws Exception {

    }

    public RequestExecutorDefault setRequestChaptersListener(Context context, RequestChaptersListener requestChaptersListener) {
        mContext = context;
        if (mRquestChaptersListener == null) {
            mRquestChaptersListener = requestChaptersListener;
        }
        return this;
    }

    public interface RequestChaptersListener {

        int ERROR_TYPE_NETWORK_NONE = 1;
        int ERROR_TYPE_VOLLEY_ERROR = 2;

        void requestSuccess(ArrayList<Chapter> chapterList);

        /**
         * @param downIndex 下载到第x个的时候没有网络了。x从0开始，最大值是chapterMap的size-1；
         */
        void requestFailed(int errorType, String errorMessage, int downIndex);
    }
}
