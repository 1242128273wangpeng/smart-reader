package net.lzbook.kit.utils;

import net.lzbook.kit.app.ActionConstants;
import net.lzbook.kit.book.view.RecommendItemView;
import com.ding.basic.bean.Book;
import com.ding.basic.bean.HistoryInfo;

import net.lzbook.kit.data.bean.RecommendItem;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 */
public class BookCoverUtil {
    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_AUTHOR = 1;
    final int LABEL_EACH_ROW_UNIT = 4;//标签分行基数（单行最多数量）
    Activity activity;
    Context context;
    Context applicationContext;
    View.OnClickListener onClickListener;
    StateReceiver stateReceiver;
    OnDownloadState onDownloadState;
    OnDownLoadService onDownLoadService;

    public BookCoverUtil(Context context, View.OnClickListener onClickListener) {
        this.context = context;
        this.onClickListener = onClickListener;
        applicationContext = context.getApplicationContext();
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }


    /**
     * 推荐 :用户书籍推荐\本书作者的其他作品\该类别下的其他书籍
     */
    public void setRecommendViewData(ViewGroup parent, View layout, ArrayList<?> datas, int type) {
        if (parent != null) {
            parent.removeAllViews();
        }
        if (layout != null) {
            layout.setVisibility(View.GONE);
        }

        if (datas != null && datas.size() > 0) {
            if (layout != null)
                layout.setVisibility(View.VISIBLE);
            int size = datas.size();

            RecommendItem recommend;
            if (size > 0) {
                recommend = (RecommendItem) datas.get(0);
                setRecommendItems(parent, recommend, type);
            }
            if (size > 1) {
                recommend = (RecommendItem) datas.get(1);
                setRecommendItems(parent, recommend, type);
            }
            if (size > 2) {
                recommend = (RecommendItem) datas.get(2);
                setRecommendItems(parent, recommend, type);
            }
        } else {
            if (layout != null) {
                layout.setVisibility(View.GONE);
            }
        }
    }

    protected void setRecommendItems(ViewGroup parent, RecommendItem recommend, int type) {
        if (recommend != null) {
            RecommendItemView item = new RecommendItemView(context, type);
            if (item == null) {
                return;
            }
            if (type == TYPE_AUTHOR) {
                item.setArgs(recommend.cover_url, recommend.novel_name, recommend.last_chapter_name, recommend.sub_count);
            } else if (type == TYPE_CATEGORY) {
                item.setArgs(recommend.cover_url, recommend.novel_name, recommend.author, recommend.sub_count);
            }

            if (type == TYPE_CATEGORY) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams
                        .WRAP_CONTENT);
                params.weight = 1.0f;
                item.setLayoutParams(params);
            } else {
                item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            item.setOnClickListener(onClickListener);
            if (parent != null) {
                parent.addView(item);
            }
        }
    }

    public void setOnDownloadState(OnDownloadState onDownloadState) {
        this.onDownloadState = onDownloadState;
    }

    public void setOnDownLoadService(OnDownLoadService onDownLoadService) {
        this.onDownLoadService = onDownLoadService;
    }

    public void registReceiver() {
        if (stateReceiver == null) {
            stateReceiver = new StateReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ActionConstants.ACTION_DOWNLOAD_BOOK_FINISH);
        filter.addAction(ActionConstants.ACTION_DOWNLOAD_BOOK_LOCKED);
        if (context != null) {
            try {
                unRegisterReceiver();
            }catch (Exception e){}
            context.registerReceiver(stateReceiver, filter);
        }
    }

    public void unRegisterReceiver() {
        if (stateReceiver != null && context != null) {
            context.unregisterReceiver(stateReceiver);
        }
    }


    public boolean saveHistory(Book book) {
        HistoryInfo info = new HistoryInfo();
        info.setName(book.getName());
        info.setBook_id(book.getBook_id());
        info.setBook_source_id(book.getBook_source_id());
        info.setLabel(book.getLabel());
        info.setAuthor(book.getAuthor());
        info.setChapter_count(book.getChapter_count());
        info.setImg_url(book.getImg_url());
        info.setHost(book.getHost());
        info.setStatus(book.getStatus());
        info.setDesc(book.getDesc());
        info.setBrowse_time(System.currentTimeMillis());

        return FootprintUtils.saveHistoryData(info);
    }



    public interface OnDownloadState {
        void changeState();
    }

    public interface OnDownLoadService {
        void downLoadService();
    }

    class StateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            AppLog.d("BookCoverUtil", "DownloadFinishReceiver action : " + intent.getAction());
            if (intent.getAction().equals(ActionConstants.ACTION_DOWNLOAD_BOOK_FINISH)
                    || intent.getAction().equals(ActionConstants.ACTION_DOWNLOAD_BOOK_LOCKED)) {

                if (onDownloadState != null) {
                    onDownloadState.changeState();
                }
            }
        }
    }
}
