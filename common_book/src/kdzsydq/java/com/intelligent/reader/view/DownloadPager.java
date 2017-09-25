package com.intelligent.reader.view;

import com.intelligent.reader.R;
import com.intelligent.reader.activity.DownloadManagerActivity;
import com.intelligent.reader.adapter.DownloadManagerAdapter;
import com.intelligent.reader.read.help.BookHelper;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.book.download.CallBackDownload;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.RemoveAdapterHelper;
import net.lzbook.kit.utils.StatServiceUtils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    protected DownloadService downService;
    long lastShowTime;
    DownloadManagerActivity downloadManagerActivity;
    long time = System.currentTimeMillis();
    BookChapterDao bookChapterDao;
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


    }

    public void setDeleteItemListener(DeleteItemListener deleteItemListener) {
        this.deleteItemListener = deleteItemListener;
    }

    @Override
    public void onSelectAll(boolean checkedAll) {
        StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.bs_down_m_click_select_all);
        Map<String, String> data = new HashMap<>();
        data.put("type", checkedAll ? "1" : "0");
        StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.CHCHEEDIT_PAGE, StartLogClickUtil.SELECTALL,data);
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
                replaceCallBack();//FIXME
                downloadAdapter.notifyDataSetChanged();
            }
        }
    }

    public DownloadManagerAdapter getAdapter() {
        return downloadAdapter;
    }

    public void stopAllTAsk() {
        if (BaseBookApplication.getDownloadService() != null) {
            BaseBookApplication.getDownloadService().cancelAll();
        }
    }

    @Override
    public void onMenuDelete(HashSet<Integer> checked_state) {
        StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.bs_down_m_click_delete);
        deleteItems(checked_state);
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
    public void deleteItems(HashSet<Integer> checked_state) {
        deleteBooks = new ArrayList<Book>();
        deleteBooks.clear();
        int size = booksData.size();
        isDeleteBookrack = false;
//		AppLog.d("test", "deleteItems   size = " + size);
//		for (Map.Entry<Integer, Boolean> entry : checked_state.entrySet()) {
//			AppLog.d("test", "key = " + entry.getKey() + " , value = " + entry.getValue());
//		}
        for (int i = 0; i < size; i++) {
            Book book = booksData.get(i);
            if (checked_state.contains(i)) {
//				AppLog.d("test", "deleteItems for   book.gid = " + book.gid + " , name = " + book.name);

                deleteBooks.add(book);
            }
        }
        if (deleteBooks.size() == 0) {
            Toast.makeText(mContext, net.lzbook.kit.R.string.mian_delete_cache_no_choose, Toast.LENGTH_SHORT).show();
        } else if (deleteBooks.size() > 0) {

            final MyDialog myDialog = new MyDialog(activity, R.layout.layout_addshelf_dialog);
            TextView dialog_title = (TextView) myDialog.findViewById(R.id.dialog_title);
            dialog_title.setText(R.string.prompt);
            TextView tv_update_info_dialog = (TextView) myDialog.findViewById(R.id.tv_update_info_dialog);
            tv_update_info_dialog.setText("你确定要删除缓存吗？");
            tv_update_info_dialog.setGravity(Gravity.CENTER);
            final CheckBox cb_hint = (CheckBox) myDialog.findViewById(R.id.cb_hint);
            cb_hint.setGravity(Gravity.CENTER);
            cb_hint.setText("同时从书架中删除");
            cb_hint.setOnClickListener(new OnClickListener() {
                boolean isChecked = false;

                @Override
                public void onClick(View v) {
                    isChecked = !isChecked;
                    isDeleteBookrack = isChecked;
                    StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_flip_auto_not_tip);
                }
            });
            TextView bt_cancel = (TextView) myDialog.findViewById(R.id.bt_cancel);
            bt_cancel.setText(R.string.cancel);
            TextView bt_ok = (TextView) myDialog.findViewById(R.id.bt_ok);
            bt_ok.setText(R.string.confirm);
            bt_cancel.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (myDialog != null) {
                        try {
                            myDialog.dismiss();
                            Map<String, String> data = new HashMap<>();
                            data.put("type", "0");
                            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.CHCHEEDIT_PAGE, StartLogClickUtil.DELETE, data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            bt_ok.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (myDialog != null) {
                        try {
                            myDialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (isDeleteBookrack) {
                        if (deleteBooks != null) {
                            String[] gids = new String[deleteBooks.size()];
                            for (int i = 0; i < gids.length; i++) {
                                AppLog.e(TAG, "DownloadPage: " + deleteBooks.get(i).toString());
                                gids[i] = deleteBooks.get(i).book_id;
                            }
                            mBookDaoHelper.deleteBook(gids);
                        }

                    }
                    if (deleteBooks != null){
                        Map<String, String> data = new HashMap<>();
                        data.put("type", "1");
                        data.put("number", String.valueOf(deleteBooks.size()));
                        StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.CHCHEEDIT_PAGE, StartLogClickUtil.DELETE, data);
                    }
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            DownloadService downloadService = BaseBookApplication.getDownloadService();
                            for (int i = 0; i < deleteBooks.size(); i++) {
                                Book book = deleteBooks.get(i);
                                if (downloadService != null) {
                                    if (isDeleteBookrack)
                                        downloadService.dellTask(book.book_id);
                                    else
                                        downloadService.resetTask(book.book_id, true);
                                }
                                if (getService().getDownBookTask(book.book_id) != null) {
                                    getService().getDownBookTask(book.book_id).state = DownloadState
                                            .NOSTART;
                                    getService().getDownBookTask(book.book_id).startSequence = 0;
                                }
                                BookHelper.delDownIndex(mContext, book.book_id);
                                BookHelper.removeChapterCacheFile(book.book_id);
                            }
                            deleteBooks.clear();
                            Message message = handler.obtainMessage(0);
                            message.sendToTarget();
                        }
                    }).start();
                }
            });
            if (myDialog != null && !myDialog.isShowing() && !activity.isFinishing()) {
                try {
                    myDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

//            Dialog dialog = customBuilder.create();
//            dialog.show();

        }
    }

    private void dealHandler() {
        if (isDeleteBookrack) {
            ((DownloadManagerActivity) activity).freshBooks(true);// FIXME
        } else {
            downloadAdapter.notifyDataSetChanged();
        }
        dissmissremoveMenu();
        deleteItemListener.onSuccess();
    }

    //===================================
//downloadservice callback
//=====================================
    @Override
    public void onTaskStart(String book_id) {
        downloadAdapter.notifyDataSetChanged();
//		AppLog.d(TAG, "onTaskStart =" + gid);
    }

    @Override
    public void onChapterDownStart(String book_id, int sequence) {
//		AppLog.d(TAG, "onChapterDownStart =");
    }

    @Override
    public void onChapterDownFinish(String book_id, int sequence) {

        System.out.println("onChapterDownFinish : " + sequence);

        if (System.currentTimeMillis() - time > 1000) {
            time = System.currentTimeMillis();
            if (downloadAdapter != null) {
                downloadAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onChapterDownFailed(String book_id, int sequence, String msg) {
        long time = System.currentTimeMillis();
        if (time - lastShowTime > 4000) {
            lastShowTime = time;
            Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        }
        downloadAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskFinish(String book_id) {
        Book book = mBookDaoHelper.getBook(book_id, 0);
        if (null != getService().getDownBookTask(book_id)
                && getService().getDownBookTask(book_id).state == DownloadState.FINISH) {
            ArrayList<Book> data = booksData;
            int size = data.size();
            for (int i = 0; i < size; i++) {
                Book b = data.get(i);
                if (b.book_id.equals(book.book_id)) {
                    data.remove(i);
                    break;
                }
            }
            Toast.makeText(mContext, book.name + "缓存完成", Toast.LENGTH_SHORT).show();
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                ((DownloadManagerActivity) activity).freshBooks(true);
            }
        });
    }

    @Override
    public void onProgressUpdate(String book_id, final int progress) {
        if (System.currentTimeMillis() - time > 500) {

            time = System.currentTimeMillis();
            if (downloadAdapter != null) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("onProgressUpdate : " + progress);
                        downloadAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    @Override
    public void onOffLineFinish() {
        AppLog.d(TAG, "onOffLineFinish =");
    }

    public DownloadService getService() {
        if (downService == null) {
            downService = BaseBookApplication.getDownloadService();
        }
        return downService;
    }

    public void replaceCallBack() {
        if (getService() != null) {
            getService().replaceOffLineCallBack(this);// TODO 替换初始化时的空回调
        }
    }

    @Override
    public void getMenuShownState(boolean isShown) {
        isShowing = isShown;
    }

    @Override
    public void getAllCheckedState(boolean isAll) {
    }

    @Override
    public void onChapterDownFailedNeedLogin() {
    }

    @Override
    public void onChapterDownFailedNeedPay(String book_id, int nid, int sequence) {

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
