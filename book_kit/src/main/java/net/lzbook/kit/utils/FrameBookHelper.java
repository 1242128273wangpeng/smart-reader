package net.lzbook.kit.utils;

import net.lzbook.kit.R;
import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.book.component.service.CheckNovelUpdateService;
import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.db.BookDaoHelper;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;

import java.io.Serializable;
import java.util.Comparator;

/**
 * <书架页工具类>
 */
public class FrameBookHelper {
    public final static String ACTION_SEARCH_UPDATE_BOOK = AppUtils.getPackageName() + ".action.search_update_book";
    static DownLoadNotify downLoadNotify;
    static DownLoadStateCallback downLoadState;
    public SharedPreferencesUtils su;
    public SharedPreferences preferences;
    String TAG = "FrameBookHelper";
    CancleUpdateCallback cancleUpdate;
    BookUpdateService updateBookService;
    NotificationCallback notification;
    SearchUpdateBook searchUpdateBook;
    BookChanged bookChanged;
    private Context context;
    private Activity activity;
    private DownloadService downloadService;
    private CheckNovelUpdateService updateService;
    private boolean isActivityPause = false;
    private BookDaoHelper bookHelper;
    private DownloadFinishReceiver downloadFinishReceiver;
    private SearchUpdateBookReceiver searchUpdateReceiver;
    private ServiceConnection downLoadConnection = new ServiceConnection() {

        private Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (downloadService.isOffLineDowning()) {
                    if (downLoadState != null) {
                        downLoadState.changeDownLoadBtn(true);
                    }
                } else {
                    if (downLoadState != null) {
                        downLoadState.changeDownLoadBtn(false);
                    }
                }
            }

        };

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                downloadService = ((DownloadService.MyBinder) service).getService();
                if (downloadService != null) {
                    if (Constants.is_wifi_auto_download && NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_WIFI) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                downloadService.autoStartDownLoad();
                                handler.sendEmptyMessage(0);
                            }
                        }).start();

                    }

                }
                BaseBookApplication.setDownloadService(downloadService);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    };

    // =======================================================
    // 服务
    // ======================================================
    // 我的消息服务
    private ServiceConnection updateConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                updateService = ((CheckNovelUpdateService.CheckUpdateBinder) service).getService();
                AppLog.d(TAG, "auto-updateService" + updateService);
                if (updateService != null && updateBookService != null) {
                    AppLog.d(TAG, "updateData " + updateBookService);
                    updateBookService.doUpdateBook(updateService);
                }
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    };


    public FrameBookHelper(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;

        registDownloadReceiver();
//		initCheckVersion();

        CheckNovelUpdHelper.delLocalNotify(context);
        DeletebookHelper helper = new DeletebookHelper(context);
        helper.startPendingService();

        if (bookHelper == null) {
            bookHelper = BookDaoHelper.getInstance();
        }
        if (preferences == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        if (su == null) {
            su = new SharedPreferencesUtils(preferences);
        }
    }

    /**
     * 初始化service
     */
    public void initDownUpdateService() {
        // 离线下载服务
        Intent intent;
        if (downloadService == null) {
            try {
                intent = new Intent();
                intent.setClass(context, DownloadService.class);
                context.startService(intent);
                context.bindService(intent, downLoadConnection, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 检查更新服务
        if (updateService == null) {
            try {
                intent = new Intent();
                intent.setClass(context, CheckNovelUpdateService.class);
                context.startService(intent);
                context.bindService(intent, updateConnection, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // =========================================================
    // interface
    // ==================================================

    public void setNotification(NotificationCallback notify) {
        this.notification = notify;
    }

    /**
     * <更新数据操作>
     * <p>
     * updateAction
     * void
     */
    public void setDownNotify(DownLoadNotify updateAction) {
        this.downLoadNotify = updateAction;
    }

    public void setDownLoadState(DownLoadStateCallback btnstate) {
        this.downLoadState = btnstate;
    }

    public void setCancleUpdate(CancleUpdateCallback exitapp) {
        this.cancleUpdate = exitapp;
    }

    public void setBookUpdate(BookUpdateService update) {
        this.updateBookService = update;
    }

    public void setSearchUpdateUI(SearchUpdateBook searchUpdate) {
        this.searchUpdateBook = searchUpdate;
    }

    public void setBookChanged(BookChanged bookChanged) {
        this.bookChanged = bookChanged;
    }

    /**
     * <获取updateService对象>
     * <p>
     * CheckNovelUpdateService
     */
    public CheckNovelUpdateService getUpdateService() {
        return updateService;
    }

    /**
     * <从通知栏点击进来后执行>
     * <p>
     * intent
     * void
     */
    public void clickNotification(Intent intent) {

        if (intent != null) {
            AppLog.d(TAG, "click_push: " + intent.getBooleanExtra("click_push", false));
            if (intent.getBooleanExtra("click_push", false)) {
                String book_id = intent.getStringExtra("book_id");
                AppLog.d(TAG, "gid: " + book_id);
                AppLog.d(TAG, "notify: " + notification);
                if (notification != null) {
                    notification.notification(book_id);
                }
            }
            if (intent.getBooleanExtra("cancel_finish_ntf", false)) {
                NotificationManager nftmgr = (NotificationManager) context.getSystemService(Context
                        .NOTIFICATION_SERVICE);
                if (nftmgr != null) {
                    nftmgr.cancel(context.getResources().getString(R.string.main_nftmgr_id).hashCode());
                }
            }
        }
    }

    /**
     * <显式调用释放内存等操作>
     * <p>
     * void
     */
    public void restoreState() {

        if (downloadService != null && downLoadConnection != null) {
            context.unbindService(downLoadConnection);
            if (!downloadService.isOffLineDowning()) {
                downloadService.stopSelf();
                BaseBookApplication.setDownloadService(null);
            }
        }
        if (updateService != null && updateConnection != null) {
            context.unbindService(updateConnection);
        }


        unregistDownloadReceiver();
        unregistSearchUpdateReceiver();

    }

    private void unregistDownloadReceiver() {
        if (downloadFinishReceiver != null) {
            context.unregisterReceiver(downloadFinishReceiver);
        }
    }

    public void onPauseAction() {
        isActivityPause = true;
    }

    private void cancleUpdateExitApp() {
        // 恢复显示参数
        if (cancleUpdate != null) {
            cancleUpdate.restoreSystemState();
        }
        if (activity != null) {
            activity.finish();
            ATManager.exitClient();
        }
    }

    // ================================================
    // 广播
    // ================================================
    private void registDownloadReceiver() {
        downloadFinishReceiver = new DownloadFinishReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadFinishReceiver.ACTION_DOWN_ALL_FINISH);
        filter.addAction(DownloadFinishReceiver.ACTION_UPDATE_NOTIFY);
        filter.addAction(DownloadFinishReceiver.ACTION_DOWNLOAD_FINISH);
        context.registerReceiver(downloadFinishReceiver, filter);
    }

    /**
     * <接收搜索订阅及刷新数据的广播>
     * <p>
     * void
     */
    public void registSearchUpdateReceiver() {
        if (searchUpdateReceiver == null) {
            searchUpdateReceiver = new SearchUpdateBookReceiver();
        }
        if (searchUpdateReceiver != null && context != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_SEARCH_UPDATE_BOOK);
            context.registerReceiver(searchUpdateReceiver, filter);
            AppLog.d(TAG, "searchUpdateReceiver" + searchUpdateReceiver);
        }
    }

    // =============================================
    // activity相关
    // ==========================================

    public void unregistSearchUpdateReceiver() {
        if (searchUpdateReceiver != null && context != null) {
            AppLog.d(TAG, "unregistSearchUpdateReceiver");

            try {
                context.unregisterReceiver(searchUpdateReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

        }
    }

    public void recycleCallback() {
        if (downLoadState != null) {
            downLoadState = null;
        }

        if (updateBookService != null) {
            updateBookService = null;
        }

        if (downLoadNotify != null) {
            downLoadNotify = null;
        }

        if (notification != null) {
            notification = null;
        }

        if (bookChanged != null) {
            bookChanged = null;
        }
    }

    public interface BookUpdateService {
        void doUpdateBook(CheckNovelUpdateService updateService);
    }

    public interface DownLoadNotify {
        /**
         * <更新数据>
         * <p>
         * void
         */
        void doNotifyDownload();
    }

    public interface DownLoadStateCallback {
        /**
         * <修改下载按钮状态>
         * <p>
         * isDownLoading
         * void
         */
        void changeDownLoadBtn(boolean isDownLoading);
    }

    public interface CancleUpdateCallback {
        /**
         * <还原系统显示>
         * <p>
         * void
         */
        void restoreSystemState();
    }

    public interface NotificationCallback {
        void notification(String gid);
    }

    public interface BookChanged {
        void updateBook();
    }

    public interface SearchUpdateBook {
        void searchUpdateBook();
    }

    /**
     * 全部下载完成和检查更新监听器
     */
    public static class DownloadFinishReceiver extends BroadcastReceiver {
        public static final String ACTION_PACKAGENAME = AppUtils.getPackageName();
        public static final String ACTION_DOWN_ALL_FINISH = ACTION_PACKAGENAME + ".download_all_finish";
        public static final String ACTION_UPDATE_NOTIFY = ACTION_PACKAGENAME + ".update_notify";
        public static final String ACTION_DOWNLOAD_FINISH = ACTION_PACKAGENAME + ".download_finish";
        public static final String ACTION_DOWNLOAD_LOCKED = ACTION_PACKAGENAME + ".download_locked";
        private String TAG = "FrameBookHelper";

        @Override
        public void onReceive(Context context, Intent intent) {

            AppLog.d(TAG, "DownloadFinishReceiver action : " + intent.getAction());
            if (intent.getAction().equals(ACTION_DOWN_ALL_FINISH)) {
                if (downLoadState != null) {
                    downLoadState.changeDownLoadBtn(false);
                }
            } else if (intent.getAction().equals(ACTION_UPDATE_NOTIFY) || intent.getAction().equals
                    (ACTION_DOWNLOAD_FINISH)) {

                if (downLoadNotify != null) {
                    downLoadNotify.doNotifyDownload();
                }
            }
        }
    }

    /**
     * 对booklist进行多类型排序
     */
    public static class MultiComparator implements Comparator<Object>, Serializable {

        @Override
        public int compare(Object o1, Object o2) {
            if (Constants.book_list_sort_type == 1) {

                return ((Book) o1).last_updatetime_native == ((Book) o2).last_updatetime_native ? 0 : (((Book) o1)
                        .last_updatetime_native < ((Book) o2).last_updatetime_native ? 1 : -1);
            } else if (Constants.book_list_sort_type == 2) {
                return 0;
            } else {
                return ((Book) o1).sequence_time == ((Book) o2).sequence_time ? 0 : (((Book) o1).sequence_time < (
                        (Book) o2).sequence_time ? 1 : -1);
            }
        }
    }

    /**
     * 对booklist按照阅读时间排序
     */
    public static class ReadTimeComparator implements Comparator<Object> {

        @Override
        public int compare(Object o1, Object o2) {
            return ((Book) o1).sequence_time == ((Book) o2).sequence_time ? 0 : (((Book) o1).sequence_time < ((Book) o2).sequence_time ? 1 : -1);
        }
    }

    private class SearchUpdateBookReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            AppLog.d(TAG, "SearchUpdateBookReceiver action : " + intent.getAction());
            if (intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_SEARCH_UPDATE_BOOK) && searchUpdateBook != null) {
                    searchUpdateBook.searchUpdateBook();
                }

            }
        }
    }
}
