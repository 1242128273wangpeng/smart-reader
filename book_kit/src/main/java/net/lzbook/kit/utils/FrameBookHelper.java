package net.lzbook.kit.utils;

import net.lzbook.kit.R;
import net.lzbook.kit.book.component.service.CheckNovelUpdateService;
import net.lzbook.kit.book.download.CacheManager;
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
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.io.Serializable;
import java.util.Comparator;

public class FrameBookHelper {
    static DownLoadNotify downLoadNotify;
    static DownLoadStateCallback downLoadState;
    public SharedPreferencesUtils su;
    public SharedPreferences preferences;
    String TAG = "FrameBookHelper";
    BookUpdateService updateBookService;
    NotificationCallback notification;
    BookChanged bookChanged;
    private Context context;
    private Activity activity;
    private CheckNovelUpdateService updateService;
    private BookDaoHelper bookHelper;
    private DownloadFinishReceiver downloadFinishReceiver;

    private ServiceConnection updateConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                updateService = ((CheckNovelUpdateService.CheckUpdateBinder) service).getService();
                if (updateService != null && updateBookService != null) {
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

        CacheManager.INSTANCE.checkService();

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

    public void setBookUpdate(BookUpdateService update) {
        this.updateBookService = update;
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

        if (updateService != null && updateConnection != null) {
            context.unbindService(updateConnection);
        }

        unregistDownloadReceiver();
    }

    private void unregistDownloadReceiver() {
        if (downloadFinishReceiver != null) {
            context.unregisterReceiver(downloadFinishReceiver);
        }
    }

    private void registDownloadReceiver() {
        downloadFinishReceiver = new DownloadFinishReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadFinishReceiver.ACTION_DOWN_ALL_FINISH);
        filter.addAction(DownloadFinishReceiver.ACTION_UPDATE_NOTIFY);
        filter.addAction(DownloadFinishReceiver.ACTION_DOWNLOAD_FINISH);
        context.registerReceiver(downloadFinishReceiver, filter);
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

    public interface NotificationCallback {
        void notification(String gid);
    }

    public interface BookChanged {
        void updateBook();
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

        @Override
        public void onReceive(Context context, Intent intent) {

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
}
