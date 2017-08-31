package net.lzbook.kit.request.own;

import android.content.Context;
import android.os.Handler;

import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.net.volley.request.VolleyDataService;
import net.lzbook.kit.request.RequestExecutorDefault;
import net.lzbook.kit.utils.NetWorkUtils;

import java.util.ArrayList;
import java.util.Map;

public class OtherRequestExecutor extends RequestExecutorDefault {

    @Override
    public void requestBookCover(Handler handler, RequestItem requestItem) {
        OtherRequestService.requestBookCover(handler, REQUEST_COVER_SUCCESS, REQUEST_COVER_ERROR, requestItem);
    }

    @Override
    public void requestCatalogList(Context context, Handler handler, RequestItem requestItem) {
        OtherRequestService.requestOwnCatalogList(handler, context, REQUEST_CATALOG_SUCCESS, REQUEST_CATALOG_ERROR,
                requestItem);
    }

    @Override
    public void requestChapterList(Context context, RequestItem requestItem,VolleyDataService.DataServiceCallBack requestChaptersCallBack) throws Exception {
        mContext = context;
        if (NetWorkUtils.getNetWorkType(mContext) == NetWorkUtils.NETWORK_NONE){
            BookChapterDao bookChapterDao = new BookChapterDao(context,requestItem.book_id);
            ArrayList<Chapter> chapterList = bookChapterDao.queryBookChapter();
            if (chapterList.size() != 0){
                requestChaptersCallBack.onSuccess(chapterList);
                return;
            }else {
                mRquestChaptersListener.requestFailed(RequestChaptersListener.ERROR_TYPE_NETWORK_NONE,"拉取章节时无网络",0);
                return;
            }
        }
        OtherRequestService.requestOwnChapterList(context, requestItem,requestChaptersCallBack);
    }

    /*@Override
    public void requestUpdate(Context context, RequestItem requestItem, int update_count, long update_time,
                              VolleyDataService.DataServiceCallBack dataServiceCallBack) throws Exception {
        OtherRequestService.requestOwnUpdate(context, requestItem, update_count, update_time, dataServiceCallBack);
    }*/

    @Override
    public Chapter requestSingleChapter(int dex,BookDaoHelper bookDaoHelper, BookChapterDao bookChapterDao, Chapter chapter)
            throws Exception {
        return new OtherRequestChapterExecutor(bookDaoHelper, bookChapterDao).requestSingleChapter(dex,chapter);
    }

    @Override
    public void requestBatchChapter(int dex,BookDaoHelper bookDaoHelper, BookChapterDao bookChapterDao, boolean downloadFlag,
                                    Map<String, Chapter> chapterMap) throws Exception {
        new OtherRequestChapterExecutor(bookDaoHelper, bookChapterDao).requestBatchChapter(dex,downloadFlag, chapterMap);
    }
}
