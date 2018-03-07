package com.intelligent.reader.view;

import com.intelligent.reader.R;
import com.intelligent.reader.activity.DownloadManagerActivity;
import com.intelligent.reader.adapter.DownloadManagerAdapter;
import com.intelligent.reader.event.DownLoaderToHome;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.book.download.CallBackDownload;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.utils.BaseBookHelper;
import net.lzbook.kit.utils.RemoveAdapterHelper;
import net.lzbook.kit.utils.StatServiceUtils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;


public class DownloadPager extends LinearLayout implements CallBackDownload, RemoveAdapterHelper
        .OnMenuDeleteClickListener,
        RemoveAdapterHelper.OnMenuStateListener, RemoveAdapterHelper.OnMenuSelectAllListener {

    private static boolean isDeleteBookrack = false;
    private final String TAG = "DownloadPager";
    private final Handler handler = new MHandler(this);
    public ArrayList<Book> booksData;
    public RemoveAdapterHelper removehelper;
    public DeleteItemListener deleteItemListener;
    public boolean isShowing = false;
    DownloadManagerActivity downloadManagerActivity;
    long time = System.currentTimeMillis();
    private DownloadManagerAdapter downloadAdapter;
    private ArrayList<Book> deleteBooks;
    private Context mContext;
    private Activity activity;
    private ListView listView;
    private RelativeLayout bookshelf_empty;
    private BookDaoHelper mBookDaoHelper;
    private TextView btnEmpty;
    private FrameLayout frameLayout;

    public DownloadPager(Context context, Activity activity, ArrayList<Book> books) {
        super(context);
        this.booksData = books;
        initView(context, activity);
    }

    private void initView(Context context, final Activity activity) {
        mBookDaoHelper = BookDaoHelper.getInstance();

        mContext = context;
        this.activity = activity;

        LayoutInflater inflater = LayoutInflater.from(activity);
        frameLayout = (FrameLayout) inflater.inflate(R.layout.download_manager_pager, null);

        this.addView(frameLayout);

        listView = (ListView) findViewById(R.id.download_manager_list);
        bookshelf_empty = (RelativeLayout) findViewById(R.id.empty_bookshelf);
        btnEmpty = (TextView) findViewById(R.id.download_empty_btn);
//		listView.setDivider(null);
        downloadAdapter = new DownloadManagerAdapter(activity, booksData, frameLayout);

        listView.setAdapter(downloadAdapter);
        removehelper = new RemoveAdapterHelper(activity, downloadAdapter, RemoveAdapterHelper.popup_type_download);
        removehelper.setOnMenuDeleteListener(this);
        removehelper.setOnSelectAllListener(this);
        removehelper.setOnMenuStateListener(this);
        removehelper.setListView(listView);
        downloadManagerActivity = (DownloadManagerActivity) activity;

        btnEmpty.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                EventBus.getDefault().post(new DownLoaderToHome(1));
                activity.finish();
            }
        });


    }

    public void setDeleteItemListener(DeleteItemListener deleteItemListener) {
        this.deleteItemListener = deleteItemListener;
    }

    @Override
    public void onSelectAll(boolean checkedAll) {
        StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.bs_down_m_click_select_all);
        Map<String, String> data = new HashMap<>();
        data.put("type", checkedAll ? "1" : "0");
        StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.CHCHEEDIT_PAGE, StartLogClickUtil.SELECTALL);
    }

    public ListView getListView() {
        return listView;
    }

    /**
     * 刷新书籍列表
     *
     * books
     */
    public void freshBookList(ArrayList<Book> books) {
        if (booksData != null && books != null) {
            if (books.size() == 0) {
                listView.setVisibility(GONE);
                bookshelf_empty.setVisibility(VISIBLE);
            } else {
                bookshelf_empty.setVisibility(GONE);
                listView.setVisibility(VISIBLE);
                booksData = books;
                downloadAdapter.notifyDataSetChanged();
            }
        }
    }

    public DownloadManagerAdapter getAdapter() {
        return downloadAdapter;
    }


    @Override
    public void onMenuDelete(List<Book> list) {
        StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.bs_down_m_click_delete);
        deleteItems(list);
    }

    public void showRemoveMenu(View parent) {
        removehelper.showRemoveMenu(parent);
    }

    public void dissmissremoveMenu() {
        removehelper.dismissRemoveMenu();
    }

    public boolean isRemoveMode() {
        return removehelper.isRemoveMode();
    }

    public void setRemoveChecked(int postion) {
        removehelper.setCheckPosition(postion);
    }

    /**
     * 点击删除事件
     *
     * checked_state
     */
    public void deleteItems(List<Book> list) {
        deleteBooks = new ArrayList<Book>();
        deleteBooks.clear();
        int size = booksData.size();
        isDeleteBookrack = false;
        for (int i = 0; i < size; i++) {
            Book book = (Book) this.booksData.get(i);
            if (list.contains(book)) {
                this.deleteBooks.add(book);
            }
        }
        if (this.deleteBooks.size() == 0) {
            Toast.makeText(this.mContext, R.string.mian_delete_cache_no_choose, Toast.LENGTH_LONG).show();
        } else if (this.deleteBooks.size() > 0) {
            final MyDialog cleanDialog = new MyDialog(this.activity, R.layout.dialog_download_clean);
            cleanDialog.setCanceledOnTouchOutside(false);
            cleanDialog.setCancelable(false);
            ((TextView) cleanDialog.findViewById(R.id.dialog_msg)).setText(R.string.tip_cleaning_cache);
            cleanDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (DownloadPager.isDeleteBookrack && DownloadPager.this.deleteBooks != null) {
                        DownloadPager.this.mBookDaoHelper.deleteBook(DownloadPager.this.deleteBooks);
                    }
                    if (DownloadPager.this.deleteBooks != null) {
                        Map<String, String> data = new HashMap();
                        data.put("type", "1");
                        data.put("number", String.valueOf(DownloadPager.this.deleteBooks.size()));
                        StartLogClickUtil.upLoadEventLog(DownloadPager.this.mContext, StartLogClickUtil.CHCHEEDIT_PAGE, "DELETE", data);
                    }
                    for (int i = 0; i < DownloadPager.this.deleteBooks.size(); i++) {
                        Book book = (Book) DownloadPager.this.deleteBooks.get(i);

                        CacheManager.INSTANCE.remove(book.book_id);


                        BaseBookHelper.removeChapterCacheFile(book);

                    }
                    DownloadPager.this.deleteBooks.clear();
                    cleanDialog.dismiss();
                    DownloadPager.this.handler.obtainMessage(0).sendToTarget();
                }
            }).start();
        }
    }

    private void dealHandler() {
        ((DownloadManagerActivity) this.activity).freshBooks(true);
        dissmissremoveMenu();
        deleteItemListener.onSuccess();
    }

    //===================================
//downloadservice callback
//=====================================
    @Override
    public void onTaskStatusChange(String book_id) {
        downloadAdapter.notifyDataSetChanged();
//		AppLog.d(TAG, "onTaskStatusChange =" + gid);
    }

    @Override
    public void onTaskFailed(String book_id, Throwable throwable) {
        Book book = this.mBookDaoHelper.getBook(book_id, 0);
        this.downloadAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskFinish(String book_id) {
        Book book = this.mBookDaoHelper.getBook(book_id, 0);

        if (CacheManager.INSTANCE.getBookStatus(book) == DownloadState.FINISH) {
            ArrayList<Book> data = this.booksData;
            int size = data.size();
            for (int i = 0; i < size; i++) {
                Book b = (Book) data.get(i);
                if (b.book_id != null && book.book_id != null && b.book_id.equals(book.book_id)) {
                    data.remove(i);
                    break;
                }
            }
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                ((DownloadManagerActivity) activity).freshBooks(true);
            }
        });
    }


    @Override
    public void onTaskProgressUpdate(String book_id) {
        if (System.currentTimeMillis() - time > 500) {

            time = System.currentTimeMillis();
            if (downloadAdapter != null) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    public void setSelectAll(boolean selectAll) {
        removehelper.selectAll(selectAll);
    }


    @Override
    public void getMenuShownState(boolean isShown) {
        isShowing = isShown;
    }

    @Override
    public void getAllCheckedState(boolean isAll) {
        ((DownloadManagerActivity) this.activity).checkSelectAll(isAll);
    }


    public void recycleResource() {

        if (booksData != null) {
            booksData.clear();
            booksData = null;
        }

        if (downloadAdapter != null) {
            downloadAdapter.recycleResource();
            downloadAdapter = null;
        }

        if (mBookDaoHelper != null) {
            mBookDaoHelper = null;
        }

        if (this.downloadManagerActivity != null) {
            this.downloadManagerActivity = null;
        }

        if (this.mContext != null) {
            this.mContext = null;
        }

        if (this.activity != null) {
            this.activity = null;
        }
    }

    public interface DeleteItemListener {
        void onSuccess();

        void onFailed();
    }

    private static class MHandler extends Handler {
        private WeakReference<DownloadPager> reference;

        MHandler(DownloadPager pager) {
            reference = new WeakReference<DownloadPager>(pager);
        }

        @Override
        public void handleMessage(Message msg) {
            DownloadPager downloadPager = reference.get();
            if (downloadPager == null) {
                return;
            }

            switch (msg.what) {
                case 0:
                    downloadPager.dealHandler();
                    break;

                default:
                    break;
            }
        }
    }
}
