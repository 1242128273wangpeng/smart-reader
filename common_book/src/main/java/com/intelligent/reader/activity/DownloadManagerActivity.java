package com.intelligent.reader.activity;

import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.R;
import com.intelligent.reader.app.BookApplication;
import com.intelligent.reader.read.help.BookHelper;
import com.intelligent.reader.receiver.DownBookClickReceiver;
import com.intelligent.reader.view.DownloadPager;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.FrameBookHelper;
import net.lzbook.kit.utils.FrameBookHelper.MultiComparator;
import net.lzbook.kit.utils.StatServiceUtils;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 下载管理
 */
public class DownloadManagerActivity extends BaseCacheableActivity implements OnClickListener, OnItemClickListener, OnItemLongClickListener, DownloadService.OnDownloadListener {
    private static final String TAG = "DownloadManagerActivity";
    public DownloadPager views;
    public FrameBookHelper.MultiComparator multiComparator;
    public long lastClickTime;
    private ImageView back_btn;
    private TextView title_name_btn;
    private TextView editBtn;
    private ViewGroup content_layout;
    private ArrayList<Book> downloadingBooks;
    private DownloadService downloadService;
    private BookDaoHelper mBookDaoHelper;
    private Book adpBook;
    private MyDialog alertDialog;
    //private ProgressDialog dialog;
    private ServiceConnection sc = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
//            if (dialog != null) {
//                dialog.dismiss();
//            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadService = ((DownloadService.MyBinder) service).getService();
            BaseBookApplication.setDownloadService(downloadService);
            downloadService.setUiContext(getApplicationContext());
            downloadService.setOnDownloadListener(DownloadManagerActivity.this);
//            if (dialog != null) {
//                dialog.dismiss();
            getDownLoadBookList(false);
//            }

        }
    };

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        try {
            setContentView(R.layout.download_manager);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        downloadService = BaseBookApplication.getDownloadService();
        multiComparator = new MultiComparator();
        initView();
        initData();
        initListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initService();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        if (alertDialog != null) {
            alertDialog = null;
        }

        try {
            setContentView(R.layout.empty);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        super.onDestroy();
        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this);
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            freshBooks(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 初始化离线下载服务
     */
    private void initService() {
    }

    public void stopDownloadbook(String book_id) {
        if (downloadService != null) {
            downloadService.cancelTask(book_id);
        }
    }

    private void initView() {
        // title初始化
        back_btn = (ImageView) findViewById(R.id.title_back_btn);
        editBtn = (TextView) findViewById(R.id.btn_edit);
        content_layout = (ViewGroup) findViewById(R.id.content_layout);
        title_name_btn = (TextView) findViewById(R.id.title_name_btn);
    }

    private void initListener() {
        back_btn.setOnClickListener(this);
        editBtn.setOnClickListener(this);

        title_name_btn.setOnClickListener(this);
        views.setDeleteItemListener(new DownloadPager.DeleteItemListener() {
            @Override
            public void onSuccess() {
                if (views.booksData.size() == 0) {
                    editBtn.setVisibility(View.GONE);
                } else {
                    editBtn.setText("编辑");
                }
            }

            @Override
            public void onFailed() {
                editBtn.setText("编辑");
            }
        });
        views.getListView().setOnItemClickListener(this);
        views.getListView().setOnItemLongClickListener(this);
        if (downloadService == null) {
            reStartDownloadService(this);
            downloadService = BaseBookApplication.getDownloadService();
        } else {
            downloadService.setOnDownloadListener(this);
            downloadService.setUiContext(getApplicationContext());
        }
    }

    private void initData() {
        mBookDaoHelper = BookDaoHelper.getInstance();
        downloadingBooks = new ArrayList<>();
        views = new DownloadPager(getApplicationContext(), this, downloadingBooks);
        content_layout.addView(views);
    }

    /**
     * 检测书籍下载状态分类，并刷新ui
     */
    public void freshBooks(boolean hasDeleted) {
        if (downloadService == null) {
            reStartDownloadService(this);
            return;
        }

        getDownLoadBookList(hasDeleted);
    }

    private void getDownLoadBookList(boolean hasDeleted) {
        if (mBookDaoHelper == null) {
            mBookDaoHelper = BookDaoHelper.getInstance();
        }
        ArrayList<Book> books = mBookDaoHelper.getBooksOnLineList();
//        Log.e(TAG, "addBookToService:"+books);
        addBookToService(books);
        // TODO 从service获取books列表 FIXME
        if (downloadService != null) {
//            Log.e(TAG, "getDownloadTaskNoFinish:"+downBooks);
            if (downloadingBooks != null && views != null) {
                downloadingBooks.clear();
                downloadingBooks.addAll(books);
                Collections.sort(downloadingBooks);
                Collections.sort(downloadingBooks, multiComparator);

                if (downloadingBooks.size() == 0) {
                    editBtn.setVisibility(View.GONE);
                } else {
                    if (AppUtils.getPackageName().equals("cc.kdqbxs.reader")) {
                        editBtn.setVisibility(View.GONE);
                    } else {
                        editBtn.setVisibility(View.VISIBLE);
                        if (hasDeleted) {
                            editBtn.setText("编辑");
                        }
                    }
                }
                views.freshBookList(downloadingBooks);
            }
        } else {
            Log.e(TAG, "downloadService == null");
        }
    }

    private void reStartDownloadService(Activity context) {
        //dialog = ProgressDialog.show(context, "", "重启服务中....", true, true);
        Intent intent = new Intent();
        intent.setClass(context, DownloadService.class);
        context.startService(intent);
        context.bindService(intent, sc, BIND_AUTO_CREATE);
    }

    private void addBookToService(ArrayList<Book> books) {
        int count = books.size();
        for (int i = 0; i < count; i++) {
            Book book = books.get(i);
            BookHelper.addDownBookTask(DownloadManagerActivity.this, book, views, true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back_btn:
                Map<String, String> data = new HashMap<>();
                data.put("type", "1");
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data);
                finish();
                break;
            case R.id.title_name_btn:
                finish();
                break;
            case R.id.btn_edit:
                if (isDoubleClick(System.currentTimeMillis())) {
                    return;
                }
                if ("编辑".equals(editBtn.getText())) {
                    views.showRemoveMenu(views);
                    editBtn.setText("取消");
                    StatServiceUtils.statAppBtnClick(this, StatServiceUtils.bs_down_m_click_edit);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.CACHEEDIT1);
                } else {
                    views.dissmissremoveMenu();
                    editBtn.setText("编辑");
                    StatServiceUtils.statAppBtnClick(this, StatServiceUtils.bs_down_m_click_cancel);
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.CHCHEEDIT_PAGE, StartLogClickUtil.CANCLE);
                }

                break;
            default:
                break;
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        views.showRemoveMenu(views);
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.cancel();

                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (!AppUtils.getPackageName().equals("cc.kdqbxs.reader")) {
            if (!views.isRemoveMode()) {
                views.showRemoveMenu(findViewById(R.id.root));
                editBtn.setText("取消");
            }
        }

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (downloadingBooks == null || position < 0 || position > downloadingBooks.size()) {
            return;
        }
        if (position > downloadingBooks.size()) {
            return;
        }
        if (!views.isRemoveMode() && !views.isShowing) {
            Book book = downloadingBooks.get(position);
            Book b = (Book) mBookDaoHelper.getBook(book.book_id, 0);
            Map<String, String> data = new HashMap<>();
            data.put("STATUS", BookHelper.isDownFnish(this, b) ? "1" : "0");
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.BOOKCLICK1, data);
            BookHelper.goToCoverOrRead(getApplicationContext(), DownloadManagerActivity.this, b);
        } else {
            views.setRemoveChecked(position);
        }
    }

    @Override
    public void onBackPressed() {
        if (views.removehelper != null && views.removehelper.isRemoveMode()) {
            views.removehelper.dismissRemoveMenu();
            editBtn.setText("编辑");
        } else {
            //如果是从通知栏过来, 且已经退出到home了, 要回到应用中
            if (isTaskRoot()) {
                Intent intent = new Intent(this, SplashActivity.class);
                startActivity(intent);
            }
            super.onBackPressed();
        }
    }

    @Override
    public void notificationCallBack(Notification preNTF, String book_id) {
        PendingIntent pending;
        Intent intent;
        if (!book_id.equals(-1 + "")) {
            intent = new Intent(this, DownBookClickReceiver.class);
            intent.setAction(DownBookClickReceiver.action);
            intent.putExtra("book_id", book_id);
            pending = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            intent = new Intent(this, DownloadManagerActivity.class);
            pending = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        preNTF.contentIntent = pending;
    }

    public boolean isDoubleClick(long time) {
        long length = time - lastClickTime;
        if (length > 800) {
            lastClickTime = time;
            return false;
        } else {
            return true;
        }
    }

}
