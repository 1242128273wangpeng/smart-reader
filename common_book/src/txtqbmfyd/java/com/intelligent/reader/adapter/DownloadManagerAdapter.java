package com.intelligent.reader.adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.DownloadManagerActivity;
import com.intelligent.reader.read.help.BookHelper;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.adapter.RemoveModeAdapter;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookTask;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.data.db.table.ChapterTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DownloadManagerAdapter extends RemoveModeAdapter implements RemoveModeAdapter.RemoveAdapterChild {
    private static final String TAG = "DownloadManagerAdapter";
    protected ArrayList<Book> book_data;
    private int notCachedCount = Integer.MAX_VALUE;
    private int drawablePause;
    private int drawableDownload;
    private int drawableWait;
    private int drawableFinish;
    private DownloadManagerActivity downloadManagerActivity;
    public BookDaoHelper mBookDaoHelper;
    private Activity mContext;
    private Resources mResources;
    private int progressbarMain;
    private int progressbarSecond;
    private int progressbarThird;

    private static final int TYPE_BOOK = 0;
    private static final int TYPE_NOT_CACHED = 1;
    private static final int TYPE_CACHED = 2;

    class ViewCache extends ViewHolder {
        private TextView txtBookName;
        private ImageView imgDownload;
        private ImageView imgBookCover;
        private TextView txtDownloadState;
        private TextView txtDownloadProgress;
        private ProgressBar pgbarDownload;

        ViewCache() {
        }
    }

    public DownloadManagerAdapter(Activity context, ArrayList<Book> list) {
        super(context, list);
        mContext = context;
        mResources = context.getResources();
        mBookDaoHelper = BookDaoHelper.getInstance();
        book_data = list;
        downloadManagerActivity = (DownloadManagerActivity) context;
        setAdapterChild(this, MODE_EXCEPT_FIRST);

        progressbarMain = R.drawable.down_manager_progressbar_main;
        progressbarSecond = R.drawable.down_manager_progressbar_second;
        progressbarThird = R.drawable.down_manager_progressbar_third;

        drawableDownload = R.drawable.download_manager_download;
        drawablePause = R.drawable.download_manager_pause;
        drawableWait = R.drawable.download_manager_wait;
        drawableFinish = R.drawable.download_manager_finished;
    }

    @Override
    public void notifyDataSetChanged() {
        freshBook();
        super.notifyDataSetChanged();
    }

    private void freshBook() {
        this.notCachedCount = Integer.MAX_VALUE;
        if (this.book_data != null) {
            this.notCachedCount = 0;
            Iterator it = this.book_data.iterator();
            while (it.hasNext()) {
                if (CacheManager.INSTANCE.getBookStatus((Book) it.next()) != DownloadState.FINISH) {
                    this.notCachedCount++;
                } else {
                    return;
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (this.notCachedCount != 0) {
            if (position == 0) {
                return TYPE_NOT_CACHED;
            }
            if (position != this.notCachedCount + 1) {
                return TYPE_BOOK;
            }
            return TYPE_CACHED;
        } else if (position == 0) {
            return TYPE_CACHED;
        } else {
            return TYPE_BOOK;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getCount() {
        int index = this.notCachedCount;
        if (index == 0 || index >= this.book_data.size()) {
            return this.book_data.size() + 1;
        }
        return this.book_data.size() + 2;
    }

    public int getBookCount() {
        return this.book_data.size();
    }

    private int getRealPosition(int position) {
        int index = position - 1;
        if (this.notCachedCount <= 0 || index < this.notCachedCount) {
            return index;
        }
        return index - 1;
    }

    @Override
    public Book getItem(int position) {
        if (getItemViewType(position) != 0) {
            return null;
        }
        position = getRealPosition(position);
        return this.book_data.get(position);
    }

    @Override
    public View setChildView(int position, View convertView, ViewHolder holder) {
        View childView = null;
        switch (getItemViewType(position)) {
            case TYPE_BOOK:
                childView = LayoutInflater.from(this.mContext).inflate(R.layout.download_manager_list_item, null);
                ViewCache cache = (ViewCache) holder;
                cache.txtBookName = (TextView) childView.findViewById(R.id.txt_book_name);
                cache.pgbarDownload = (ProgressBar) childView.findViewById(R.id.pgbar_download);
                cache.txtDownloadProgress = (TextView) childView.findViewById(R.id.txt_download_num);
                cache.txtDownloadState = (TextView) childView.findViewById(R.id.txt_download_state);
                cache.imgDownload = (ImageView) childView.findViewById(R.id.img_download);
                cache.imgBookCover = (ImageView) childView.findViewById(R.id.img_book_cover);
                break;
            case TYPE_NOT_CACHED:
                childView = LayoutInflater.from(this.mContext)
                        .inflate(R.layout.download_manager_list_item_type_not_cached, null);
                if (position == 0) {
                    childView.findViewById(R.id.view_divider).setVisibility(View.GONE);
                }
                break;
            case TYPE_CACHED:
                childView = LayoutInflater.from(this.mContext)
                        .inflate(R.layout.download_manager_list_item_type_cached, null);
                if (position == 0) {
                    childView.findViewById(R.id.view_divider).setVisibility(View.GONE);
                }
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
        switch (getItemViewType(position)) {
            case 0:
                ViewCache cache = (ViewCache) holder;
                final Book book = getItem(position);
                if (!TextUtils.isEmpty(book.img_url)
                        && !book.img_url.equals(ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
                    Glide.with(this.downloadManagerActivity)
                            .load(book.img_url)
                            .placeholder(R.drawable.icon_book_cover_default)
                            .error((R.drawable.icon_book_cover_default))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(cache.imgBookCover);
                } else {
                    Glide.with(this.downloadManagerActivity)
                            .load(R.drawable.icon_book_cover_default)
                            .into(cache.imgBookCover);
                }
                BookTask task = CacheManager.INSTANCE.getBookTask(book);
                int typeColor;
                if (!(cache.txtBookName == null || TextUtils.isEmpty(book.name))) {
                    cache.txtBookName.setText(book.name);
                }
                cache.txtDownloadProgress.setVisibility(View.GONE);
                if (cache.txtDownloadProgress != null && task.progress > 0) {
                    cache.txtDownloadProgress.setVisibility(View.VISIBLE);
                    String progress = ": " + task.progress + "%";
                    cache.txtDownloadProgress.setText(progress);
                }
                cache.pgbarDownload.setMax(100);
                cache.pgbarDownload.setProgress(task.progress);
                DownloadState state = task.state;
                cache.pgbarDownload.setProgressDrawable(this.mResources.getDrawable(this.progressbarSecond));
                cache.imgDownload.setImageResource(drawableDownload);
                cache.txtDownloadState.setTextColor(downloadManagerActivity.getResources().getColor(R.color.download_manager_other_tag_color));
                if (state == DownloadState.DOWNLOADING) {
                    cache.txtDownloadState.setText("正在缓存");
                    cache.imgDownload.setImageResource(drawablePause);
                    cache.pgbarDownload.setProgressDrawable(this.mResources.getDrawable(this.progressbarMain));
                } else if (state == DownloadState.WAITTING) {
                    cache.txtDownloadState.setText("等待缓存");
                    cache.imgDownload.setImageResource(drawableWait);
                    cache.txtDownloadProgress.setVisibility(View.GONE);
                } else if (state == DownloadState.PAUSEED) {
                    cache.txtDownloadState.setText("已暂停");
                } else if (state == DownloadState.NONE_NETWORK) {
                    cache.txtDownloadState.setText("已暂停");
                } else if (state == null || state == DownloadState.NOSTART) {
                    cache.txtDownloadState.setText("未缓存");
                    cache.pgbarDownload.setProgress(0);
                } else if (state == DownloadState.FINISH) {
                    cache.txtDownloadState.setText("已缓存");
                    cache.imgDownload.setImageResource(drawableFinish);
                    cache.pgbarDownload.setProgress(100);
                    cache.txtDownloadProgress.setVisibility(View.GONE);
                    cache.pgbarDownload.setProgressDrawable(this.mResources.getDrawable(this.progressbarThird));
                } else if (state == DownloadState.WAITTING_WIFI) {
                    cache.txtDownloadState.setText("非Wi-Fi状态下已自动暂停");
//                        cache.txtDownloadState.setTextColor(this.downloadManagerActivity.getResources().getColor(R.color.download_manager_wifi_pause_color));
                    cache.txtDownloadProgress.setVisibility(View.GONE);
                } else {
                    task.state = DownloadState.NOSTART;
                    cache.txtDownloadState.setText("未缓存");
                    cache.pgbarDownload.setProgress(0);
                }
                if (this.remove_checked_states.contains(book)) {
                    typeColor = R.drawable.download_manager_selector_selected;
                } else {
                    typeColor = R.drawable.download_manager_selector_unselected;
                }
                cache.check.setBackgroundResource(typeColor);
                final int finalProgress = task.progress;
                if (!isRemoveMode()) {
                    cache.imgDownload.setClickable(true);
                    cache.imgDownload.setOnClickListener(new View.OnClickListener() {
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
                                CacheManager.INSTANCE.stop(book.book_id);
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
                    cache.imgDownload.setClickable(false);
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
