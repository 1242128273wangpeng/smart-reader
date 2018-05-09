package net.lzbook.kit.utils;

import net.lzbook.kit.R;
import net.lzbook.kit.book.component.service.CheckNovelUpdateService;
import net.lzbook.kit.book.download.CacheManager;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

public class FrameBookHelper {
    private Context context;

    private CheckNovelUpdateService updateService;

    BookChanged bookChanged;
    NotificationCallback notification;
    BookUpdateService updateBookService;
    static DownLoadNotify downLoadNotify;
    static DownLoadStateCallback downLoadState;


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

        registDownloadReceiver();

        CheckNovelUpdHelper.delLocalNotify(context);
        DeletebookHelper helper = new DeletebookHelper(context);
        helper.startPendingService();
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
            if (intent.getBooleanExtra("click_push", false)) {
                String book_id = intent.getStringExtra("book_id");
                if (notification != null) {
                    notification.notification(book_id);
                }
            }
            if (intent.getBooleanExtra("cancel_finish_ntf", false)) {
                NotificationManager nftmgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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
        filter.addAction(DownloadFinishReceiver.ACTION_UPDATE_NOTIFY);
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
        public static final String ACTION_UPDATE_NOTIFY = ACTION_PACKAGENAME + ".update_notify";

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ACTION_UPDATE_NOTIFY)) {

                if (downLoadNotify != null) {
                    downLoadNotify.doNotifyDownload();
                }
            }
        }
    }
}