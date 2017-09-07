package net.lzbook.kit.utils;

import net.lzbook.kit.R;
import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.book.view.RecommendItemView;
import net.lzbook.kit.data.NullCallBack;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.CoverPage;
import net.lzbook.kit.data.bean.RecommendItem;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.data.ormlite.bean.HistoryInfo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

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
        filter.addAction(FrameBookHelper.DownloadFinishReceiver.ACTION_DOWNLOAD_FINISH);
        filter.addAction(FrameBookHelper.DownloadFinishReceiver.ACTION_DOWNLOAD_LOCKED);
        if (context != null)
            context.registerReceiver(stateReceiver, filter);
    }

    public void unRegistReceiver() {
        if (stateReceiver != null && context != null) {
            context.unregisterReceiver(stateReceiver);
        }
    }

    //新的获取封面书籍的方法
    public Book getCoverBook(BookDaoHelper bookDaoHelper, CoverPage.BookVoBean coverResult) {

        Book book;
        if (bookDaoHelper != null && coverResult != null && bookDaoHelper.isBookSubed(coverResult.book_id)) {
            book = (Book) bookDaoHelper.getBook(coverResult.book_id, 0);
        } else {
            book = new Book();
            if (coverResult != null) {
                book.book_id = coverResult.book_id;
                book.book_source_id = coverResult.book_source_id;
                book.name = coverResult.name;
                book.category = coverResult.labels;
                book.author = coverResult.author;
                book.chapter_count = coverResult.serial_number;
                book.last_chapter_name = coverResult.last_chapter_name;
                book.img_url = coverResult.img_url;
                book.status = coverResult.status;
                book.site = coverResult.host;
                book.dex = coverResult.dex;
                book.last_updatetime_native = coverResult.update_time;
                book.parameter = coverResult.parameter;
                book.extra_parameter = coverResult.extra_parameter;
            }
        }
        return book;
    }

    /**
     * 保存浏览足迹
     *
     * @return 保存是否成功
     */
    public boolean saveHistory(CoverPage.BookVoBean book) {

        HistoryInfo info = new HistoryInfo();
        info.setName(book.name);
        info.setBook_id(book.book_id);
        info.setBook_source_id(book.book_source_id);
        info.setCategory(book.labels);
        info.setAuthor(book.author);
        info.setChapter_count(book.serial_number);
        info.setLast_chapter_name(book.last_chapter_name);
        info.setImg_url(book.img_url);
        info.setSite(book.host);
        info.setStatus(book.status);
        info.setDesc(book.desc);
        info.setLast_brow_time(System.currentTimeMillis());

        return FootprintUtils.saveHistoryData(info);
    }

    //新的开始下载的方法
    public void startDownLoad(BookDaoHelper bookDaoHelper, CoverPage.BookVoBean coverResult) {
        if (context == null)
            return;
        if (NetWorkUtils.getNetWorkType(context) == NetWorkUtils.NETWORK_NONE) {
            Toast.makeText(context, context.getText(R.string.game_network_none), Toast.LENGTH_LONG).show();
            return;
        }
        Book book = getCoverBook(bookDaoHelper, coverResult);
        DownloadService downloadService = BaseBookApplication.getDownloadService();

        if (downloadService != null && book != null) {
            if (downloadService.containTask(book.book_id)) {
                downloadService.startTask(book.book_id);
            } else {
                downloadService.addTask(BaseBookHelper.getBookTask(context, book, DownloadState.NOSTART, new NullCallBack(), true));
                downloadService.addRequestItem(book);
                downloadService.startTask(book.book_id);
            }
            BaseBookHelper.writeDownIndex(context, book.book_id, false, 0);

            if (onDownLoadService != null) {
                onDownLoadService.downLoadService();
            }

        }
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
            if (intent.getAction().equals(FrameBookHelper.DownloadFinishReceiver.ACTION_DOWNLOAD_FINISH)
                    || intent.getAction().equals(FrameBookHelper.DownloadFinishReceiver.ACTION_DOWNLOAD_LOCKED)) {

                if (onDownloadState != null) {
                    onDownloadState.changeState();
                }
            }
        }
    }
}
