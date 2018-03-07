package com.intelligent.reader.adapter;

import com.intelligent.reader.R;
import com.intelligent.reader.activity.DownloadManagerActivity;
import com.intelligent.reader.read.help.BookHelper;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.adapter.RemoveModeAdapter;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookTask;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.data.db.table.ChapterTable;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DownloadManagerAdapter extends RemoveModeAdapter implements RemoveModeAdapter.RemoveAdapterChild {
    private static final String TAG = "DownloadManagerAdapter";
    protected ArrayList<Book> book_data;
    private int cachedIndex = Integer.MAX_VALUE;
    int downBtnDowning;
    int downBtnNoStart;
    int downBtnWaitting;
    private DownloadManagerActivity downloadManagerActivity;
    FrameLayout frameLayout;
    public BookDaoHelper mBookDaoHelper;
    private Activity mContext;
    private Resources mResources;
    int progressbarMain;
    int progressbarSecond;
    Resources resources;
    Theme theme;

    class ViewCache extends ViewHolder {
        private TextView book_name;
        private ImageView download_btn;
        private TextView download_count;
        private ProgressBar download_progress;
        private TextView download_state;
        private View divi;
        public int realPosition = 0;

        ViewCache() {
        }
    }

    public DownloadManagerAdapter(Activity context, ArrayList<Book> list, FrameLayout frameLayout) {
        super(context, list);
        mContext = context;
        mResources = context.getResources();
        mBookDaoHelper = BookDaoHelper.getInstance();
        book_data = list;
        frameLayout = frameLayout;
        downloadManagerActivity = (DownloadManagerActivity) context;
        setAdapterChild(this, MODE_EXCEPT_FIRST);

        theme = mContext.getTheme();
        resources = mContext.getResources();
        progressbarMain = R.drawable.down_manager_progressbar_main;
        progressbarSecond = R.drawable.down_manager_progressbar_second;
        downBtnNoStart = R.mipmap.download_icon_download;
        downBtnDowning = R.mipmap.download_icon_downloading;
        downBtnWaitting = R.mipmap.download_icon_wait;
    }

    @Override
    public void notifyDataSetChanged() {
        freshBook();
        super.notifyDataSetChanged();
    }

    private void freshBook() {
        this.cachedIndex = Integer.MAX_VALUE;
        if (this.book_data != null) {
            this.cachedIndex = 0;
            Iterator it = this.book_data.iterator();
            while (it.hasNext()) {
                if (CacheManager.INSTANCE.getBookStatus((Book) it.next()) != DownloadState.FINISH) {
                    this.cachedIndex++;
                } else {
                    return;
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (this.cachedIndex != 0) {
            if (position == 0) {
                return 1;
            }
            if (position != this.cachedIndex + 1) {
                return 0;
            }
            return 2;
        } else if (position != 0) {
            return 0;
        } else {
            return 2;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getCount() {
        int index = this.cachedIndex;
        if (index == 0 || index >= this.book_data.size()) {
            return this.book_data.size() + 1;
        }
        return this.book_data.size() + 2;
    }

    public int getReadPosition(int position) {
        int index = position - 1;
        if (this.cachedIndex <= 0 || index < this.cachedIndex) {
            return index;
        }
        return index - 1;
    }

    @Override
    public Object getItem(int position) {
        if (getItemViewType(position) != 0) {
            return null;
        }
        int i = position;
        i = position - 1;
        if (this.cachedIndex > 0 && i >= this.cachedIndex) {
            i--;
        }
        return this.book_data.get(i);
    }

    @Override
    public View setChildView(int position, View convertView, ViewHolder holder) {
        switch (getItemViewType(position)) {
            case 0:
                View childView = LayoutInflater.from(this.mContext).inflate(R.layout.download_manager_list_item, null);
                ViewCache cache = (ViewCache) holder;
                cache.book_name = (TextView) childView.findViewById(R.id.download_manager_bookname);
                cache.download_progress = (ProgressBar) childView.findViewById(R.id.download_progress);
                cache.download_count = (TextView) childView.findViewById(R.id.download_manager_chapter_num);
                cache.download_state = (TextView) childView.findViewById(R.id.download_manager_download_state);
                cache.download_btn = (ImageView) childView.findViewById(R.id.download_manager_download_btn);
                cache.divi = childView.findViewById(R.id.v_divider);
                return childView;
            case 1:
                return LayoutInflater.from(this.mContext).inflate(R.layout.download_manager_list_item_type_uncache, null);
            case 2:
                View inflate = LayoutInflater.from(this.mContext).inflate(R.layout.download_manager_list_item_type_cached, null);
                if (position == 0) {
                    inflate.findViewById(R.id.ll_divider).setVisibility(View.GONE);
                }
                return inflate;
            default:
                return null;
        }
    }

    @Override
    public ViewHolder wholeHolder() {
        return new ViewCache();
    }

    @Override
    public void setChildAdapterData(int position, ViewHolder holder, View childView) {
        switch (getItemViewType(position)) {
            case 0:
                ViewCache cache = (ViewCache) holder;
                if (position == 1 || (this.cachedIndex > 0 && this.cachedIndex + 2 == position)) {
                    cache.divi.setVisibility(View.GONE);
                } else {
                    cache.divi.setVisibility(View.VISIBLE);
                }
                int i = position - 1;
                if (this.cachedIndex > 0 && i >= this.cachedIndex) {
                    i--;
                }
                cache.realPosition = i;
                final Book book = (Book) this.book_data.get(i);
                BookTask task = CacheManager.INSTANCE.getBookTask(book);
                if (book != null) {
                    int typeColor;
                    if (!(cache.book_name == null || TextUtils.isEmpty(book.name))) {
                        cache.book_name.setText(book.name);
                    }
                    cache.download_count.setVisibility(View.GONE);
                    if (cache.download_count != null && task.progress > 0) {
                        cache.download_count.setVisibility(View.VISIBLE);
                        cache.download_count.setText(" " + task.progress + "%");
                    }
                    cache.download_progress.setMax(100);
                    cache.download_progress.setProgress(task.progress);
                    DownloadState state = task.state;
                    cache.download_progress.setProgressDrawable(this.mResources.getDrawable(this.progressbarSecond));
                    cache.download_btn.setImageResource(this.downBtnNoStart);
                    cache.download_state.setTextColor(this.downloadManagerActivity.getResources().getColor(R.color.download_manager_other_tag_color));
                    if (state == DownloadState.DOWNLOADING) {
                        cache.download_state.setText("正在缓存");
                        cache.download_btn.setImageResource(this.downBtnDowning);
                        cache.download_progress.setProgressDrawable(this.mResources.getDrawable(this.progressbarMain));
                    } else if (state == DownloadState.WAITTING) {
                        cache.download_state.setText("等待缓存");
                        cache.download_btn.setImageResource(this.downBtnWaitting);
                        cache.download_count.setVisibility(View.GONE);
                    } else if (state == DownloadState.PAUSEED) {
                        cache.download_state.setText("已暂停");
                    } else if (state == DownloadState.NONE_NETWORK) {
                        cache.download_state.setText("已暂停");
                    } else if (state == null || state == DownloadState.NOSTART) {
                        cache.download_state.setText("未缓存");
                        cache.download_progress.setProgress(0);
                    } else if (state == DownloadState.FINISH) {
                        cache.download_state.setText("已缓存");
                        cache.download_btn.setImageResource(R.mipmap.download_icon_finish);
                        cache.download_progress.setProgress(100);
                        cache.download_count.setVisibility(View.GONE);
                    } else if (state == DownloadState.WAITTING_WIFI) {
                        cache.download_state.setText("非Wi-Fi状态下已自动暂停");
                        cache.download_state.setTextColor(this.downloadManagerActivity.getResources().getColor(R.color.download_manager_wifi_pause_color));
                        cache.download_count.setVisibility(View.GONE);
                    } else {
                        task.state = DownloadState.NOSTART;
                        cache.download_state.setText("未缓存");
                        cache.download_progress.setProgress(0);
                    }
                    if (this.remove_checked_states.contains(getItem(position))) {
                        typeColor = R.mipmap.bookshelf_delete_checked;
                    } else {
                        typeColor = R.mipmap.bookshelf_delete_unchecked;
                    }
                    cache.check.setBackgroundResource(typeColor);
                    final int finalProgress = task.progress;
                    if (!isRemoveMode()) {
                        cache.download_btn.setClickable(true);
                        cache.download_btn.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                System.err.println("download : " + book.book_id);
                                DownloadState state = CacheManager.INSTANCE.getBookStatus(book);
                                Map<String, String> data = new HashMap();
                                if (state == null || state == DownloadState.NOSTART) {
                                    BookHelper.startDownBookTask(DownloadManagerAdapter.this.mContext, book, 0);
                                    data.put("type", "1");
                                    data.put("bookid", book.book_id);
                                    StartLogClickUtil.upLoadEventLog(DownloadManagerAdapter.this.mContext, "CACHEMANAGE", StartLogClickUtil.CACHEBUTTON, data);
                                    return;
                                }
                                if (state == DownloadState.DOWNLOADING || state == DownloadState.WAITTING) {
                                    DownloadManagerAdapter.this.downloadManagerActivity.stopDownloadbook(book.book_id);
                                    data.put("type", "2");
                                    data.put(ChapterTable.SPEED, finalProgress + "/100");
                                } else {
                                    BookHelper.startDownBookTask(DownloadManagerAdapter.this.mContext, book, 0);
                                    data.put("type", "1");
                                }
                                data.put("bookid", book.book_id);
                                StartLogClickUtil.upLoadEventLog(DownloadManagerAdapter.this.mContext, "CACHEMANAGE", StartLogClickUtil.CACHEBUTTON, data);
                            }
                        });
                    } else {
                        cache.download_btn.setClickable(false);
                    }
                    return;
                }
                return;
            default:
                return;
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
}
