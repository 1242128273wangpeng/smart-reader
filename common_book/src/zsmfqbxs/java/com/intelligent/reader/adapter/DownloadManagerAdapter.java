package com.intelligent.reader.adapter;

import com.intelligent.reader.R;
import com.intelligent.reader.activity.DownloadManagerActivity;
import com.intelligent.reader.read.help.BookHelper;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.adapter.RemoveModeAdapter;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookTask;
import net.lzbook.kit.data.db.BookDaoHelper;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DownloadManagerAdapter extends RemoveModeAdapter implements RemoveModeAdapter.RemoveAdapterChild {
    private static final String TAG = "DownloadManagerAdapter";
    public BookDaoHelper mBookDaoHelper;
    protected ArrayList<Book> book_data;
    FrameLayout frameLayout;
    Resources.Theme theme;
    Resources resources;
    int progressbarMain;//点亮的进度条
    int progressbarSecond;//次要进度条
    int downBtnNoStart;//次要进度条
    int downBtnDowning;//次要进度条
    int downBtnWaitting;//次要进度条
    private DownloadManagerActivity downloadManagerActivity;
    private Resources mResources;
    private Context mContext;

    public DownloadManagerAdapter(Activity context, ArrayList<Book> list, FrameLayout frameLayout) {
        super(context, list);
        this.mContext = context;
        mResources = context.getResources();
        mBookDaoHelper = BookDaoHelper.getInstance();
        book_data = list;
        this.frameLayout = frameLayout;
        downloadManagerActivity = (DownloadManagerActivity) context;
        setAdapterChild(this, MODE_EXCEPT_FIRST);

        theme = mContext.getTheme();
        resources = mContext.getResources();
        progressbarMain = R.drawable.down_manager_progressbar_main;
        progressbarSecond = R.drawable.down_manager_progressbar_second;
        downBtnNoStart = R.mipmap.icon_download_nostart;
        downBtnDowning = R.mipmap.icon_downloading;
        downBtnWaitting = R.mipmap.icon_download_watting;

    }

    @Override
    public int getItemViewType(int position) {
        return book_data.get(position).book_type;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getCount() {
        return book_data.size();
    }

    @Override
    public Object getItem(int position) {
        return book_data.get(position);
    }

    @Override
    public View setChildView(int position, View convertView, ViewHolder holder) {
        final ViewCache cache;
        View childView = null;
        switch (getItemViewType(position)) {
            case 0:
                childView = LayoutInflater.from(mContext).inflate(R.layout.download_manager_list_item, null);
                cache = (ViewCache) holder;
                cache.book_name = (TextView) childView.findViewById(R.id.download_manager_bookname);
                cache.download_progress = (ProgressBar) childView.findViewById(R.id.download_progress);
                cache.download_count = (TextView) childView.findViewById(R.id.download_manager_chapter_num);
                cache.download_state = (TextView) childView.findViewById(R.id.download_manager_download_state);
                cache.download_btn = (ImageView) childView.findViewById(R.id.download_manager_download_btn);
                break;
            default:
                break;
        }
        return childView;
    }

    @Override
    public ViewHolder wholeHolder() {
        return new ViewCache();
    }

    @Override
    public void setChildAdapterData(int position, ViewHolder holder, View childView) {
        final ViewCache cache = (ViewCache) holder;
        final Book book = book_data.get(position);
        switch (getItemViewType(position)) {
            case 0:
                final BookTask task = downloadManagerActivity.views.getService().getDownBookTask(book.book_id);
                if (task == null || book == null) {
                    return;
                }
                if (cache.book_name != null && !TextUtils.isEmpty(book.name)) {
                    cache.book_name.setText(book.name);
                }
                int start = task.startSequence == task.endSequence ? task.startSequence : task.startSequence;

                if (start > task.endSequence) {
                    start = task.endSequence;
                }
                final String speed = start + "/" + (task.endSequence);
                if (cache.download_count != null) {
                    cache.download_count.setText(speed + mResources.getString(R.string.chapter));
                }
                int num = task.endSequence == 0 ? task.book.chapter_count : task.endSequence;
                int progress = task.progress;
                if (task.startSequence > 0) {
                    try {
                        progress = task.startSequence * 100 / num;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (task.startSequence > 0) {
                    cache.download_count.setVisibility(View.VISIBLE);
                } else {
                    cache.download_count.setVisibility(View.GONE);
                }
                cache.download_progress.setMax(100);
                cache.download_progress.setProgress(progress);
                DownloadState state = task.state;

                cache.download_progress.setProgressDrawable(mResources.getDrawable(progressbarSecond));
                cache.download_btn.setImageResource(downBtnNoStart);

                if ((state == DownloadState.DOWNLOADING)) {
                    String stateText = "";
                    if (task.startSequence > 0) {
                        stateText = "内容加载中 " + progress + "%";
                    } else {
                        stateText = "下载中。。。。";
                    }

                    cache.download_state.setText(stateText);
                    cache.download_progress.setProgressDrawable(mResources.getDrawable(progressbarMain));
                    cache.download_btn.setImageResource(downBtnDowning);
                } else if ((state == DownloadState.WAITTING)) {
                    cache.download_state.setText("等待缓存");
                    cache.download_btn.setImageResource(downBtnWaitting);
                } else if ((state == DownloadState.PAUSEED)) {
                    cache.download_state.setText("已暂停");
                } else if ((state == DownloadState.NONE_NETWORK)) {
                    cache.download_state.setText("已暂停");
                } else if ((state == DownloadState.REFRESH)) {
                    cache.download_state.setText("已暂停");
                } else if (state == null || (state == DownloadState.NOSTART)) {
                    cache.download_state.setText("无缓存");
                    cache.download_progress.setProgress(0);
                } else if (state == DownloadState.FINISH) {
                    cache.download_state.setText("缓存完成");
                    cache.download_btn.setImageResource(R.drawable.icon_download_complete);
                    cache.download_progress.setProgress(100);
                } else if (state == DownloadState.LOCKED) {
                    cache.download_state.setText("已暂停");
                } else {
                    task.state = DownloadState.NOSTART;
                    cache.download_state.setText("无缓存");
                    cache.download_progress.setProgress(0);
                }

                int typeColor = 0;

                if (remove_checked_states.contains(position)) {
                    typeColor = R.mipmap.bookshelf_delete_checked;
                } else {
                    typeColor = R.mipmap.bookshelf_delete_unchecked;
                }
                cache.check.setBackgroundResource(typeColor);

                cache.download_btn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        System.err.println("download : " + book.book_id);
                        DownloadState state = downloadManagerActivity.views.getService().getDownBookTask(book.book_id) == null ? null
                                : downloadManagerActivity.views.getService().getDownBookTask(book.book_id).state;
                        Map<String, String> data = new HashMap<>();
                        if (state == null || state == DownloadState.NOSTART) {
                            BookHelper.startDownBookTask(mContext, book.book_id, 0);
                            BookHelper.writeDownIndex(mContext, book.book_id, false, 0);
                            data.put("type", "1");
                            data.put("bookid", book.book_id);
                            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.CACHEBUTTON, data);
                            return;
                        } else if (state == DownloadState.DOWNLOADING || state == DownloadState.WAITTING) {
                            downloadManagerActivity.stopDownloadbook(book.book_id);
                            data.put("type", "2");
                            data.put("speed", speed);
                        } else if (state == DownloadState.LOCKED) {
                            BookHelper.startDownBookTask(mContext, book.book_id);
                            data.put("type", "1");
                        } else if (state == DownloadState.NONE_NETWORK) {
                            BookHelper.startDownBookTask(mContext, book.book_id);
                            data.put("type", "1");
                        } else if (state == DownloadState.PAUSEED || state == DownloadState.REFRESH) {
                            BookHelper.startDownBookTask(mContext, book.book_id);
                            data.put("type", "1");
                        } else if (state == DownloadState.FINISH) {
                            Toast.makeText(mContext, "缓存已完成", Toast.LENGTH_SHORT).show();
                            data.put("type", "1");
                        }
                        notifyDataSetChanged();
                        data.put("bookid", book.book_id);
                        StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.CACHEBUTTON, data);

                    }
                });
                break;
            default:
                break;
        }
    }

    public void recycleResource() {

        if (this.downloadManagerActivity != null) {
            this.downloadManagerActivity = null;
        }

        if (this.mContext != null) {
            this.mContext = null;
        }

        if (this.book_data != null) {
            this.book_data.clear();
            this.book_data = null;
        }
    }

    class ViewCache extends ViewHolder {

        private TextView book_name;
        private TextView download_count;
        private ProgressBar download_progress;
        private TextView download_state;
        private ImageView download_btn;

    }

}
