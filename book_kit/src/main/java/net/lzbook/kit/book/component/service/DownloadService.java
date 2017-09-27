package net.lzbook.kit.book.component.service;

import com.quduquxie.network.DataService;

import net.lzbook.kit.R;
import net.lzbook.kit.app.ActionConstants;
import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.download.BreakPointFileLoader;
import net.lzbook.kit.book.download.CacheInfo;
import net.lzbook.kit.book.download.CallBackDownload;
import net.lzbook.kit.book.download.DesUtils;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.book.download.GZipUtils;
import net.lzbook.kit.book.download.NumberUtil;
import net.lzbook.kit.book.download.PackChapterProto;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.NullCallBack;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookTask;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.encrypt.URLBuilderIntterface;
import net.lzbook.kit.net.volley.request.VolleyDataService;
import net.lzbook.kit.request.RequestExecutorDefault;
import net.lzbook.kit.request.RequestFactory;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.request.own.OWNParser;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.BaseBookHelper;
import net.lzbook.kit.utils.BeanParser;
import net.lzbook.kit.utils.BlockingLinkedHashMap;
import net.lzbook.kit.utils.FrameBookHelper;
import net.lzbook.kit.utils.MD5Utils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.OpenUDID;
import net.lzbook.kit.utils.StatServiceUtils;

import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static net.lzbook.kit.book.download.HttpUtilKt.getHttpData;
import static net.lzbook.kit.book.download.HttpUtilKt.getHttpDataString;
import static net.lzbook.kit.book.download.KTFileUtilsKt.delFile;
import static net.lzbook.kit.book.download.ParseCacheHelperKt.getChapterFromPackage;
import static net.lzbook.kit.request.RequestExecutorDefault.RequestChaptersListener.ERROR_TYPE_NETWORK_NONE;

public class DownloadService extends Service {

    private static final String BOOK_ID_PARAM = "{book_id}";
    private static final String BOOK_SOURCE_ID_PARAM = "{book_source_id}";
    private static final int ntfId = "小说离线缓存".hashCode();
    // 单次章节下载量
    private static final int DOWN_SIZE = 10;
    private static final int MAX_SIZE = 1000;
    private static BlockingLinkedHashMap<String, BookTask> mTaskQueue;
    boolean isShown = false;
    Handler uiHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Toast.makeText(getApplicationContext(), "空间不足", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    Context uiContext;
    private String TAG = "DownloadService";
    private ExecutorService executor;
    private Handler handler = new Handler();
    private MyBinder binder;
    private NotificationManager nftmgr = null;
    private NotificationManager notificationManager;
    private BookTask curTask;
    private int progress = 0;
    private int down_num = 0;
    private long notifyTime = 0;
    private String curBookName;
    private BookDaoHelper mBookDaoHelper;
    private RequestFactory requestFactory;
    private HashMap<String, RequestItem> downloadRequestItem = new HashMap<>();
    private WeakReference<OnDownloadListener> downloadListenerRef;
    private RequestExecutorDefault.RequestChaptersListener onRequestChaptersListener;
    private int downPosition = 0;

    /**
     * 清除缓存之后，将下载任务的状态置为未启动。
     */
    public static void clearTask(String book_id) {
        //        BaseBookHelper.delDownIndex(this,book_id);
        if (mTaskQueue == null)
            return;
        BookTask bookTask = mTaskQueue.get(book_id);
        if (bookTask != null) {
            bookTask.state = DownloadState.NOSTART;
            bookTask.isAutoState = false;
            bookTask.startSequence = 0;
            if (bookTask != null && bookTask.cacheLoader != null) {
                bookTask.cacheLoader.delete();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new MyBinder();
        if (mBookDaoHelper == null) {
            mBookDaoHelper = BookDaoHelper.getInstance();
        }
        if (mTaskQueue == null) {
            mTaskQueue = new BlockingLinkedHashMap(MAX_SIZE);
        }
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor(new BackgroundThreadFactory());
        }

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void onDestroy() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
        super.onDestroy();
    }

    public void replaceOffLineCallBack(CallBackDownload cb) {
        Collection<Map.Entry<String, BookTask>> list = mTaskQueue.getAll();
        for (Map.Entry<String, BookTask> entry : list) {
            BookTask bookTask = entry.getValue();
            if (bookTask != null) {
                bookTask.mCallBack = cb;
            }
        }
    }

    // 自动缓存
    public void autoStartDownLoad() {
        ArrayList<Book> books = mBookDaoHelper.getBooksOnLineList();
        int size = books.size();
        for (int i = 0; i < size && i < 10; i++) {//最多10本
            Book book = books.get(i);
            BookTask bookTask = mTaskQueue.get(book.book_id);
            if (bookTask != null && BaseBookHelper.getStartDownIndex(this, book) > -1) {
                Log.e("autoStartDownLoad", "has");
                bookTask.isAutoState = true;
                //请耐心等待，已存在下载队列
                if (bookTask.state == DownloadState.DOWNLOADING) {
                } else if (bookTask.state == DownloadState.WAITTING || bookTask.state == DownloadState.NOSTART
                        || bookTask.state == DownloadState.PAUSEED || bookTask.state == DownloadState.REFRESH) {
                    // 已添加到下载管理页面
                    startTask(book.book_id);
                }
            } else {
                Log.e("autoStartDownLoad", "nohas");
                int count = BaseBookHelper.getStartDownIndex(this, book);
                DownloadState state = BaseBookHelper.getInitDownstate(this, book, count);
                // 从0开始
                int endSequence = book.chapter_count - 1;
                int startSequence = 0;
                startSequence = book.sequence > -1 ? book.sequence : 0;
                BookTask data = new BookTask(book, state, startSequence, endSequence, new NullCallBack());
                data.isAutoState = true;
                if (count > 0) {
                    data.startSequence = count;
                }
                mTaskQueue.remove(data.book.book_id);
                addTask(data);
                addRequestItem(book);
                startTask(book.book_id, startSequence);
                BaseBookHelper.writeDownIndex(this, book.book_id, true, startSequence);
            }
        }
    }

    public void restoreStartDownLoad(ArrayList<BookTask> bookTasks) {
        int size = 0;
        if (bookTasks != null) {
            size = bookTasks.size();
        }
        for (int i = 0; i < size; i++) {
            BookTask bookTask = bookTasks.get(i);
            if (bookTask != null && BaseBookHelper.getStartDownIndex(this, bookTask.book) > -1) {
                if (bookTask.state == DownloadState.DOWNLOADING) {
                    return;
                } else if (bookTask.state == DownloadState.WAITTING || bookTask.state == DownloadState.PAUSEED
                        || bookTask.state == DownloadState.REFRESH) {
                    startTask(bookTask.book.book_id);
                    return;
                }
            }
        }
    }

    public void showAlert() {
        if (isShown || !isOffLineDowning()) {
            return;
        }
        final ArrayList<BookTask> bookTasks = cancelAll();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("当前处于移动网络数据,是否继续缓存?");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                restoreStartDownLoad(bookTasks);
                isShown = false;
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                isShown = false;
                stopSelf();
                BaseBookApplication.setDownloadService(null);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                isShown = false;
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
        isShown = true;
    }

    /*
     * 根据gid获取下载任务信息
     */
    public BookTask getDownBookTask(String book_id) {
        return mTaskQueue.get(book_id);
    }

    /*
     * 获取非完成的任务信息
     */
    public ArrayList<Book> getDownloadTaskNoFinish() {
        ArrayList<Book> retList = new ArrayList<Book>();
        if (mTaskQueue != null) {
            Collection<Map.Entry<String, BookTask>> list = mTaskQueue.getAll();
            for (Map.Entry<String, BookTask> entry : list) {
                BookTask bookTask = entry.getValue();
                if (bookTask != null && bookTask.state != DownloadState.FINISH) {
                    bookTask.book.chapter_count = mBookDaoHelper.getBook(bookTask.book.book_id, 0).chapter_count;
                    retList.add(bookTask.book);
                }
            }
        }
        return retList;
    }

    /*
     * 是否包含gid下载任务
     */
    public boolean containTask(String book_id) {
        return mTaskQueue.containsKey(book_id);
    }

    /*
     * 取消所有任务
     */
    public ArrayList<BookTask> cancelAll() {
        if (mTaskQueue != null) {
            Collection<Map.Entry<String, BookTask>> list = mTaskQueue.getAll();
            ArrayList<BookTask> bookTasks = new ArrayList<BookTask>();

            for (Map.Entry<String, BookTask> entry : list) {
                BookTask bookTask = entry.getValue();
                if (bookTask != null
                        && (bookTask.state == DownloadState.WAITTING || bookTask.state == DownloadState.DOWNLOADING)) {
                    bookTask.state = DownloadState.PAUSEED;
                    bookTask.isAutoState = false;
                    bookTasks.add(bookTask);
                }
            }
            notificationManager.cancel(ntfId);
            return bookTasks;
        }
        return null;
    }

    /*
     * 取消当前gid任务
     */
    public void cancelTask(final String book_id) {

        executor.execute(new Runnable() {
            @Override
            public void run() {

                final BookTask bookTask = mTaskQueue.get(book_id);
                if (bookTask != null) {
                    BaseBookHelper.writeDownIndex(DownloadService.this, bookTask.book.book_id, true, bookTask.startSequence);
                    bookTask.state = DownloadState.PAUSEED;
                    bookTask.isAutoState = false;
                    if (bookTask.cacheLoader != null) {
                        bookTask.cacheLoader.pause();
                    }
                }

                //遍历队列，如果当前有任务正处在下载中的话，就不执行updateTask操作。
                Collection<Map.Entry<String, BookTask>> list = mTaskQueue.getAll();
                for (Map.Entry<String, BookTask> entry : list) {
                    BookTask bt = entry.getValue();
                    if (bt != null && bt.state == DownloadState.DOWNLOADING) {
                        return;
                    }
                }
                updateTask();
            }
        });
    }

    /*
     * 重置任务
     */
    public void resetTask(String book_id, boolean clearSequence) {
        BookTask bookTask = mTaskQueue.get(book_id);
        if (bookTask != null && bookTask.state != DownloadState.NOSTART) {
            bookTask.state = DownloadState.PAUSEED;
            bookTask.isAutoState = false;
            if (clearSequence) {
                bookTask.startSequence = 0;
            } else {
                bookTask.startSequence = BaseBookHelper.getStartDownIndex(DownloadService.this, bookTask.book);
            }
        }
        updateTask();
    }

    /*
     * 添加一个小说下载任务
     */
    public void addTask(BookTask task) {
        if (task != null) {
            if (mTaskQueue.containsKey(task.book.book_id)) {
                BookTask bookTask = mTaskQueue.get(task.book.book_id);
                if (bookTask.state != DownloadState.DOWNLOADING) {
                    bookTask.startSequence = task.startSequence;
                    bookTask.endSequence = task.endSequence;

                }

                if (bookTask.state == DownloadState.FINISH && bookTask.startSequence != bookTask.endSequence) {
                    bookTask.state = DownloadState.PAUSEED;
                }

            } else {
                mTaskQueue.put(task.book.book_id, task);
            }
        }
    }

    public void addRequestItem(Book book) {
        if (book != null) {
            RequestItem requestItem = new RequestItem();
            requestItem.book_id = book.book_id;
            requestItem.book_source_id = book.book_source_id;
            requestItem.host = book.site;
            requestItem.name = book.name;
            requestItem.author = book.author;
            requestItem.parameter = book.parameter;
            requestItem.extra_parameter = book.extra_parameter;
            AppLog.e("ADDRequestItem", "ADDRequestItem: " + requestItem.toString());
            downloadRequestItem.put(book.book_id, requestItem);
        }
    }

    /*
     * 运行当前任务
     */
    private void runCurrTask() {
        if (curTask != null) {
            if (executor == null) {
                executor = Executors.newSingleThreadExecutor(new BackgroundThreadFactory());
            }
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    downBook();
                }
            });
        } else {
            updateTask();
        }
    }

    /*
     * 是否离线下载任务在运行
     */
    public boolean isOffLineDowning() {
        if (curTask != null && curTask.state == DownloadState.DOWNLOADING) {
            return true;
        }
        Collection<Map.Entry<String, BookTask>> list = mTaskQueue.getAll();
        for (Map.Entry<String, BookTask> entry : list) {
            BookTask bookTask = entry.getValue();
            if (bookTask.state == DownloadState.DOWNLOADING) {
                return true;
            }
        }
        return false;
    }

    private void onOffLineFinish(final BookTask task) {
        if (isOffLineDowning()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (task != null) {
                        task.isAutoState = false;
                        task.mCallBack.onOffLineFinish();
                        Intent intent = new Intent(FrameBookHelper.DownloadFinishReceiver.ACTION_DOWN_ALL_FINISH);
                        sendBroadcast(intent);
                    }
                }
            });
        } else {
            updateTask();
        }
    }

    private void onTaskStart(final BookTask task) {
        Collection<Map.Entry<String, BookTask>> entrys = mTaskQueue.getAll();
        for (Map.Entry<String, BookTask> entry : entrys) {
            if (entry != null && entry.getValue() != null && entry.getValue().state == DownloadState.DOWNLOADING) {
                entry.getValue().state = DownloadState.PAUSEED;
            }
        }
        if (task != null) {
            task.state = DownloadState.DOWNLOADING;// FIXME
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (task != null) {
                    task.mCallBack.onTaskStart(task.book.book_id);
                }
            }
        });
    }

    private void onTaskFail(final BookTask task) {
        onTaskFail(task, "请检查您的网络连接");
    }

    private void onTaskFail(final BookTask task, final String msg) {
        if (task != null) {
            Map<String, String> data = new HashMap<>();
            data.put("status", "2");
            data.put("reason", msg);
            data.put("bookId", task.book.book_id);
            StartLogClickUtil.upLoadEventLog(DownloadService.this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.CASHERESULT, data);
            task.state = DownloadState.REFRESH;
            task.isAutoState = false;

//            task.startSequence = BaseBookHelper.getCacheCount(task.book.book_id);// FIXME
            BaseBookHelper.writeDownIndex(DownloadService.this, task.book.book_id, true, task.startSequence);
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (task != null) {
                    //  TODO 回调任务下载失败
                    task.mCallBack.onChapterDownFailed(task.book.book_id, task.startSequence, msg);
                }
            }
        });
        curTask = null;
        updateTask();
    }

    private void onTaskFailByLocked(final BookTask task) {
        if (task != null) {
            task.state = DownloadState.LOCKED;
            task.isAutoState = false;

//            task.startSequence = start + BaseBookHelper.getCacheCount(task.book.book_id, start, task.book.chapter_count);// FIXME
            BaseBookHelper.writeDownIndex(DownloadService.this, task.book.book_id, true, task.startSequence);
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (task != null) {
                    // TODO 回调任务下载失败
                    task.mCallBack.onChapterDownFailed(task.book.book_id, task.startSequence, "请检查您的网络连接");
                    task.mCallBack.onChapterDownFailedNeedPay(task.book.book_id, task.book.nid, task.startSequence);
                    Intent intent = new Intent(FrameBookHelper.DownloadFinishReceiver.ACTION_DOWNLOAD_FINISH);
                    sendBroadcast(intent);
                }
            }
        });
        curTask = null;
        updateTask();
    }

    private void onTaskFailByLockedAndLogin(final BookTask task) {
        if (task != null) {
            task.state = DownloadState.LOCKED;
            task.isAutoState = false;

//            task.startSequence = start + BaseBookHelper.getCacheCount(task.book.book_id, start, task.book.chapter_count);// FIXME
            BaseBookHelper.writeDownIndex(DownloadService.this, task.book.book_id, true, task.startSequence);
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (task != null) {
                    // TODO 回调任务下载失败
                    task.mCallBack.onChapterDownFailed(task.book.book_id, task.startSequence, "请检查您的网络连接");
                    task.mCallBack.onChapterDownFailedNeedLogin();
                    Intent intent = new Intent(FrameBookHelper.DownloadFinishReceiver.ACTION_DOWNLOAD_LOCKED);
                    sendBroadcast(intent);
                }
            }
        });
        curTask = null;
        updateTask();
    }

    private void onTaskFilsh(final BookTask task) {
        if (task != null) {

            BaseBookHelper.writeDownIndex(this, task.book_id, true, task.endSequence);

            task.book = BookDaoHelper.getInstance().getBook(task.book.book_id, 0);

            task.isAutoState = false;
//            task.startSequence = start + BaseBookHelper.getCacheCount(task.book.book_id, start, task.book.chapter_count);// FIXME

            //            if (count >= task.endSequence + 1) {
            //                task.state = DownloadState.FINISH;
            //            } else {
            //                task.state = DownloadState.REFRESH;
            //            }
            task.state = DownloadState.FINISH;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    task.mCallBack.onTaskFinish(task.book.book_id);
                    Intent intent = new Intent(FrameBookHelper.DownloadFinishReceiver.ACTION_DOWNLOAD_FINISH);
                    sendBroadcast(intent);

                    Intent broakCastIntent = new Intent();
                    broakCastIntent.putExtra(Constants.REQUEST_ITEM, task.book);
                    broakCastIntent.setAction(ActionConstants.ACTION_CACHE_COMPLETE);
                    LocalBroadcastManager.getInstance(BaseBookApplication.getGlobalContext()).sendBroadcast(broakCastIntent);

                    notificationManager.cancel(ntfId);

                    showFinishNotify(task.book, BaseBookApplication.getGlobalContext());

                    Map<String, String> data = new HashMap<>();
                    data.put("status", "1");
                    data.put("bookId", task.book.book_id);
                    StartLogClickUtil.upLoadEventLog(DownloadService.this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.CASHERESULT, data);

                }
            });
        }
    }

    private void showFinishNotify(Book book, Context context) {

        Intent notifyIntent = null;
        try {
            notifyIntent = new Intent(context, Class.forName("com.intelligent.reader.activity.GoToCoverOrReadActivity"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //notifyIntent.setAction(getPackageName() + ".GoToCoverOrReadActivity");
        notifyIntent.putExtra(Constants.REQUEST_ITEM, book);
        notifyIntent.putExtra(Constants.NOTIFY_ID, Constants.DOWNLOAD);
//        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Constants.DOWNLOAD, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification build = builder.setSmallIcon(R.drawable.icon)

                .setAutoCancel(true)
                .setContentTitle(book.name)
                .setContentText("缓存已经完成，点击查看")
                .setContentIntent(pendingIntent)
                .build();

        NotificationManagerCompat.from(context).notify(Constants.DOWNLOAD, build);
        Constants.DOWNLOAD++;

    }

    private void showFinishNotification(Book book, Context context) {
        Notification mNotification;
        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotification = new Notification();
        mNotification.icon = R.drawable.icon;
        mNotification.tickerText = book.name;
        mNotification.when = System.currentTimeMillis();
        mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        String packageName = context.getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            packageName = AppUtils.getPackageName();
        }
        mNotification.contentView = new RemoteViews(packageName, R.layout.notify_cache);
        mNotification.contentView.setViewVisibility(R.id.notify_text_tv, View.GONE);
        mNotification.contentView.setViewVisibility(R.id.notify_text_progress, View.GONE);
        mNotification.contentView.setViewVisibility(R.id.notify_progress, View.GONE);
        mNotification.contentView.setViewVisibility(R.id.notify_button_reset, View.GONE);
        mNotification.contentView.setViewVisibility(R.id.notify_txt_reset, View.GONE);

        mNotification.contentView.setViewVisibility(R.id.notify_txt_complete, View.VISIBLE);
        mNotification.contentView.setViewVisibility(R.id.notify_txt_gotolook, View.VISIBLE);

        mNotification.contentView.setTextViewText(R.id.notify_title_tv, book.name);

        Intent notifyIntent = null;
        try {
            notifyIntent = new Intent(context, Class.forName("com.intelligent.reader.activity.GoToCoverOrReadActivity"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //notifyIntent.setAction(getPackageName() + ".GoToCoverOrReadActivity");
        notifyIntent.putExtra(Constants.REQUEST_ITEM, book);
        notifyIntent.putExtra(Constants.NOTIFY_ID, Constants.DOWNLOAD);
//        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Constants.DOWNLOAD, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotification.contentView.setOnClickPendingIntent(R.id.notify_txt_gotolook, pendingIntent);

        mNotificationManager.notify(Constants.DOWNLOAD, mNotification);
        Constants.DOWNLOAD++;
    }

    private void onAutoStateTaskFinish(final BookTask task) {
        if (task != null) {
            task.book = BookDaoHelper.getInstance().getBook(task.book.book_id, 0);
            int count = BaseBookHelper.getStartDownIndex(this, task.book);
            task.startSequence = downPosition;// FIXME

            BaseBookHelper.writeDownIndex(DownloadService.this, task.book.book_id, true, task.startSequence);
            if (count >= task.endSequence + 1) {
                task.state = DownloadState.FINISH;
            } else {
                task.state = DownloadState.PAUSEED;
            }
            task.isAutoState = false;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    task.mCallBack.onTaskFinish(task.book.book_id);
                    Intent intent = new Intent(FrameBookHelper.DownloadFinishReceiver.ACTION_DOWNLOAD_FINISH);
                    sendBroadcast(intent);
                }
            });
        }
    }

    private void onTaskProgress(final BookTask task, final int index, final int total) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                int l = 0;
                long cur_time = System.currentTimeMillis();
                try {
                    l = (index + 1) * 100 / total;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (l > progress) {
                    progress = l;
                }
                if (!task.isAutoState && (index - 10 > down_num && (cur_time - notifyTime > 1000 * 2)) || l == 100) {
                    if (task.state == DownloadState.DOWNLOADING) {
                        showNotification(task.book.name, l, index, total, l == 100 ? task.book.book_id : -1 + "");
                    }
                    down_num = index;
                    notifyTime = cur_time;// FIXME
                }

                if (task != null && task.state == DownloadState.DOWNLOADING// FIXME
                        ) {
                    task.mCallBack.onChapterDownFinish(task.book.book_id, index);
                }
            }
        });
    }

    private HashMap<String, Chapter> buildChapterMap(ArrayList<Chapter> chapterList, int start, int size) {
        HashMap<String, Chapter> chapterMap = new HashMap<String, Chapter>();
        for (int i = start; i < start + size; i++) {
            if (i > -1 && i < chapterList.size()) {
                Chapter chapter = chapterList.get(i);
                String key = chapter.book_id + "_" + chapter.sequence;
                chapterMap.put(key, chapter);
            } else {
                break;
            }
        }
        return chapterMap;
    }

    private void downChapter(final ArrayList<Chapter> chapterList, final BookTask task, final BookChapterDao bookChapterDao) {
        // 如果目录下载失败，直接return
        if (chapterList != null && chapterList.size() > 0) {
            onRequestChaptersListener = null;
            RequestExecutorDefault.mRquestChaptersListener = null;
            onRequestChaptersListener = new MyRequestChaptersListener(task);
            int size = chapterList.size();
            AppLog.e("downChapter下载章节结束点", "获取到的chapterList的size=" + size);
            //            task.endSequence = (task.endSequence > size - 1) ? size - 1 : task.endSequence;
            task.endSequence = size;//不管哪个大，都应该按照本地数据库中存储的章节列表来下载
            int endPosition = 0;

            //正本缓存
            if (task.startSequence <= 0 && !task.isAutoState && !Constants.QG_SOURCE.equals(task.book.site)) {

                String checkCacheUrl = URLBuilderIntterface.GET_DOWN_ADDRESS.replace("{book_source_id}", task.book.book_source_id);
                Map<String, String> map = new HashMap<>();
                map.put("downType", "0");
                checkCacheUrl = UrlUtils.buildDownBookUrl(checkCacheUrl, map);

                System.err.println("checkCacheUrl : " + checkCacheUrl);

                final CacheInfo cacheInfo = getHttpData(checkCacheUrl, CacheInfo.class);


                System.err.println("cacheInfo : " + cacheInfo);

                if (task.state != DownloadState.DOWNLOADING) {
                    curTask = null;
                    return;
                }

                if (cacheInfo.getSuccess() && cacheInfo.getDownUrl() != null) {


                    final boolean isSameSource = cacheInfo.getHost().equalsIgnoreCase(task.book.site);

                    BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();
                    if (bookDaoHelper.isBookSubed(task.book.book_id)) {
                        Book iBook = bookDaoHelper.getBook(task.book.book_id, 0);
                        iBook.book_source_id = cacheInfo.getBookSourceId();
                        iBook.site = cacheInfo.getHost();
                        bookDaoHelper.updateBook(iBook);
                    } else {
                        Book iBook = task.book;
                        iBook.book_source_id = cacheInfo.getBookSourceId();
                        iBook.site = cacheInfo.getHost();
                        iBook.dex = task.book.dex;
                        iBook.parameter = task.book.parameter;
                        iBook.extra_parameter = task.book.extra_parameter;
                        bookDaoHelper.insertBook(iBook);
                    }


                    String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_DOWNLOAD + cacheInfo.getFileName();
                    final File targetCacheFile = new File(filePath);

                    System.err.println("targetCacheFile : " + targetCacheFile.getAbsolutePath());

                    task.book.book_source_id = cacheInfo.getBookSourceId();
                    task.book.site = cacheInfo.getHost();

                    task.cacheLoader = new BreakPointFileLoader(task.book_id, cacheInfo.getDownUrl(), targetCacheFile,
                            //onError
                            new Function2<String, String, Unit>() {
                                @Override
                                public Unit invoke(String s, String s2) {
                                    onTaskProgress(task, 0, cacheInfo.getChapterCount());
                                    onRequestChaptersListener.requestFailed(ERROR_TYPE_NETWORK_NONE, s2, 0);
                                    StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.download_stop);
                                    curTask = null;
                                    return null;
                                }
                            },
                            //onProgress
                            new Function2<String, Integer, Boolean>() {
                                @Override
                                public Boolean invoke(String s, Integer integer) {
                                    task.progress = integer;
                                    task.mCallBack.onProgressUpdate(s, integer);
                                    return task.state == DownloadState.DOWNLOADING;
                                }
                            },
                            //onComplete
                            new Function1<String, Unit>() {
                                @Override
                                public Unit invoke(String s) {

//                                            targetCacheFile


                                    String fileMD5 = MD5Utils.getFileMD5(targetCacheFile);
                                    if (!fileMD5.equalsIgnoreCase(cacheInfo.getMd5())) {
                                        task.cacheLoader.delete();
                                        onTaskFail(task);
                                        return null;
                                    }


                                    if (!isSameSource) {
                                        //异源都要 清除缓存目录内容
                                        delFile(new File(ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + task.book.book_id));
                                        task.startSequence = 0;
                                    }

                                    ArrayList<Chapter> chapterArrayList = null;

                                    RequestItem requestItem = downloadRequestItem.get(task.book_id);
                                    requestItem.book_source_id = cacheInfo.getBookSourceId();
                                    requestItem.host = cacheInfo.getHost();

                                    String uri = URLBuilderIntterface.CHAPTER_LIST.replace(BOOK_ID_PARAM, requestItem.book_id).replace(BOOK_SOURCE_ID_PARAM, requestItem
                                            .book_source_id);
                                    String url = UrlUtils.buildUrl(uri, new HashMap<String, String>());

                                    if (task.state != DownloadState.DOWNLOADING) {
                                        notificationManager.cancel(ntfId);
                                        curTask = null;
                                        return null;
                                    }

                                    String response = getHttpDataString(url);

                                    if (task.state != DownloadState.DOWNLOADING) {
                                        notificationManager.cancel(ntfId);
                                        curTask = null;
                                        return null;
                                    }

                                    if (response != null) {

                                        bookChapterDao.deleteBookChapters(0);

                                        try {
                                            ArrayList<Chapter> chapters = OWNParser.parserOwnChapterList(response, requestItem);

                                            if (chapters != null && !chapters.isEmpty() && BookDaoHelper.getInstance().isBookSubed
                                                    (requestItem.book_id)) {

                                                bookChapterDao.insertBookChapter(chapters);
                                                Chapter lastChapter = chapters.get(chapters.size() - 1);

                                                task.book.book_id = requestItem.book_id;
                                                task.book.book_source_id = requestItem.book_source_id;
                                                task.book.parameter = requestItem.parameter;
                                                task.book.extra_parameter = requestItem.extra_parameter;
                                                task.book.site = requestItem.host;
                                                task.book.chapter_count = chapters.size();
                                                task.book.last_updatetime_native = lastChapter.time;
                                                task.book.last_chapter_name = lastChapter.chapter_name;
                                                task.book.last_sort = lastChapter.sort;
                                                task.book.gsort = lastChapter.gsort;
                                                task.book.last_chapter_md5 = lastChapter.book_chapter_md5;
                                                task.book.last_updateSucessTime = System.currentTimeMillis();
                                                BookDaoHelper.getInstance().updateBook(task.book);

                                            }
                                            chapterArrayList = chapters;


                                        } catch (JSONException e) {
                                            e.printStackTrace();

                                            onTaskFail(task);
                                            return null;
                                        }

                                    } else {

                                        onTaskFail(task);

                                        return null;
                                    }


                                    ArrayList<String> chapterIdList = new ArrayList<String>();
                                    for (int i = 0; i < chapterArrayList.size(); i++) {
                                        chapterIdList.add(chapterArrayList.get(i).chapter_id);
                                    }

                                    //fix progress 100%+
                                    task.endSequence = chapterArrayList.size();

                                    long startParse = System.currentTimeMillis();
                                    try {
                                        BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();

                                        DesUtils des = new DesUtils();//自定义密钥
                                        File packageFile = targetCacheFile;

                                        int chapterCount = chapterArrayList.size();

                                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(packageFile));
                                        byte[] bbuf = new byte[4];
                                        //使用循环来重复读取数据
                                        int index = 0;

                                        final int skip = task.startSequence;

                                        long lastUpdateTime = System.currentTimeMillis();

                                        while (bis.read(bbuf, 0, 4) > 0) {
                                            if (task.state != DownloadState.DOWNLOADING) {
                                                bis.close();
                                                notificationManager.cancel(ntfId);
                                                curTask = null;
                                                return null;
                                            }

                                            // 将字节数组转换为字符串输出
                                            int a = NumberUtil.byte4ToInt(bbuf);
                                            byte[] bbufs = new byte[a];
                                            if (bis.read(bbufs, 0, a) > 0) {

                                                if (index < chapterCount) {

                                                    index++;

                                                    if (index < skip) {
                                                        continue;
                                                    }


                                                    byte[] bookbyte = des.decrypt(bbufs);
                                                    bookbyte = GZipUtils.decompress(bookbyte);
                                                    //模拟接收Byte[]，反序列化成Person类
                                                    PackChapterProto.PackChapter p2 = PackChapterProto.PackChapter.parseFrom(bookbyte);
                                                    //Chapter chapter = chapterList.get(index);
                                                    Chapter ret = getChapterFromPackage(chapterIdList, chapterArrayList, bookChapterDao, bookDaoHelper, p2);

                                                    task.startSequence = index;

                                                    if (System.currentTimeMillis() - lastUpdateTime > 200) {
                                                        lastUpdateTime = System.currentTimeMillis();

                                                        int progress = (int) (100 * (index * 1.0f / chapterArrayList.size()));
                                                        System.err.println("parse chapter : " + progress);

                                                        onTaskProgress(task, index, chapterCount);
                                                    }
                                                }
                                            }
                                        }
                                        System.err.println("parse end user time : " + (System.currentTimeMillis() - startParse));
                                        packageFile.delete();

                                        onTaskProgress(task, index - 1, index);// 修正一下通知栏进度

                                        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.download_parse_success);

                                        if (task.startSequence == chapterCount) {
                                            onTaskFilsh(task);
                                            curTask = null;
                                        } else {

                                            if (task.cacheLoader != null) {
                                                task.cacheLoader.delete();
                                            }

                                            task.endSequence = chapterCount;
                                            downChapter(chapterArrayList, task, bookChapterDao);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();

                                        task.cacheLoader.getFile().delete();
                                        task.cacheLoader.getStatusFile().delete();
                                        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.download_parse_error);
                                        onTaskFail(task);
                                    }

                                    curTask = null;
                                    return null;
                                }

                            });

                    task.cacheLoader.load();


                } else {

                    StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.download_no_address);

                    //正本缓存中还未打包
                    onTaskFail(task, "正在打包，请稍后再试。。");
                }

                return;
            }


            Log.e("autoStartDownLoad", "task.isAutoState:" + task.isAutoState);
            if (task.isAutoState) {
                endPosition = task.startSequence + 20;
                endPosition = endPosition > task.endSequence ? task.endSequence : endPosition;
            } else {
                endPosition = task.endSequence;
            }


            AppLog.e("downChapter下载章节结束点", "task.endSequence=" + task.endSequence + ";;endPosition=" + endPosition);


            for (; task.startSequence <= endPosition; ) {
                if (task.state != DownloadState.DOWNLOADING) {
                    notificationManager.cancel(ntfId);
                    curTask = null;
                    break;
                }


                for (int i = 0; i < 10; i++) {
                    try {
                        //青果
                        if (Constants.QG_SOURCE.equals(task.book.site)) {
                            if (NetWorkUtils.getNetWorkType(this) == NetWorkUtils.NETWORK_NONE) {
                                onNetworkNone(task, downPosition);
                            }
                            String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
                            ArrayList<com.quduquxie.bean.Chapter> list = BeanParser.buildQGChapterList(chapterList, task.startSequence, DOWN_SIZE);
                            if (list.size() > 0) {
                                DataService.getChapterBatch(uiContext, list, list.get(0), DOWN_SIZE, true, udid);
                            }
                        } else {
                            HashMap<String, Chapter> chapterMap = buildChapterMap(chapterList, task.startSequence, DOWN_SIZE);
                            if (requestFactory == null) {
                                requestFactory = new RequestFactory();
                            }
                            ((RequestExecutorDefault) requestFactory.requestExecutor(downloadRequestItem.get(task.book_id)))
                                    .setRequestChaptersListener(getApplicationContext(), onRequestChaptersListener)
                                    .requestBatchChapter(task.book.dex, mBookDaoHelper, bookChapterDao, true, chapterMap);
                            if (chapterMap != null) {
                                chapterMap.clear();
                                chapterMap = null;
                            }

                        }
                        downPosition = Math.min(task.startSequence + DOWN_SIZE, task.endSequence);
                        task.startSequence += DOWN_SIZE;
                        break;
                    } catch (Exception e1) {
                        AppLog.e("DownloadChapter", "DownloadChapter-DownloadChapter: " + e1.toString());
                        e1.printStackTrace();
                        if (e1 instanceof FileNotFoundException) {
                            uiHandler.sendMessage(uiHandler.obtainMessage(0));
                            onTaskFail(task);
                            break;
                        } else if (!TextUtils.isEmpty(e1.getMessage())) {
                            AppLog.e("DownloadChapter", "DownloadChapter-DownloadChapter: " + e1.getMessage());
                            if (e1.getMessage().equals("3003")) {
                                onTaskFailByLocked(task);
                            } else if (e1.getMessage().equals("2014")) {
                                onTaskFailByLockedAndLogin(task);
                            } else if (e1.getMessage().equals("2016")) {
                                onTaskFailByLocked(task);
                            }
                            break;
                        } else {
                            if (i == 9) {
                                AppLog.e("DownloadChapter", "DownloadChapter-DownloadChapter: i == 9");
                                onTaskFail(task);
                                break;
                            } else {
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                    }
                }
                if (task.startSequence > task.endSequence) {
                    task.startSequence = task.endSequence;
                    onTaskProgress(task, size - 1, size);
                    break;
                } else {
                    if (task.state == DownloadState.DOWNLOADING) {
                        onTaskProgress(task, task.startSequence, size);
                    }
                }

            }
            if (task.state == DownloadState.DOWNLOADING) {
                if (task.isAutoState) {
                    onAutoStateTaskFinish(task);
                } else {
                    onTaskProgress(task, size - 1, size);// 修正一下通知栏进度
                    onTaskFilsh(task);
                }
            }
            curTask = null;
        }
    }

    private void onNetworkNone(final BookTask task, int downIndex) {
        if (task != null) {
            //暂停当前下载任务
            task.state = DownloadState.NONE_NETWORK;
            //保存下载到的章节序号
//            task.startSequence = start + BaseBookHelper.getCacheCount(task.book.book_id, start, task.book.chapter_count);// FIXME
            BaseBookHelper.writeDownIndex(DownloadService.this, task.book.book_id, true, task.startSequence);
        }
        //通知Activity界面更新UI
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (task != null) {
                    // TODO 回调任务下载失败
                    task.mCallBack.onChapterDownFailed(task.book.book_id, task.startSequence, "请检查您的网络连接");
                }
            }
        });

        //开始下一个任务
        curTask = null;
        updateTask();
    }

    /**
     * 下载某本小说
     * <p/>
     * data
     */
    private void downBook() {
        if (curTask == null) {
            updateTask();
            return;
        }
        final BookTask task = curTask;
        progress = 0;
        down_num = 0;
        curBookName = task.book.name;
        onTaskStart(task);
        RequestItem currentRequestItem = downloadRequestItem.get(task.book_id);
        AppLog.d(TAG, "downBook : bookName = " + curBookName + " , start = " + task.startSequence);
        final BookChapterDao bookChapterDao = new BookChapterDao(DownloadService.this, task.book_id);
        // 下载目录，判断目录是否存在，不存在从网络下载目录，存储到数据库
        ArrayList<Chapter> chapterList = bookChapterDao.queryBookChapter();
        AppLog.e("downChapter下载章节结束点", "获取到的chapterList的size=" + task.book.book_source_id);
        if (currentRequestItem != null && !Constants.SG_SOURCE.equals(currentRequestItem.host)) {

            if (chapterList == null || chapterList.size() == 0) {
                try {
                    if (requestFactory == null) {
                        requestFactory = new RequestFactory();
                    }
                    onRequestChaptersListener = null;
                    RequestExecutorDefault.mRquestChaptersListener = null;
                    onRequestChaptersListener = new MyRequestChaptersListener(task);
                    ((RequestExecutorDefault) requestFactory.requestExecutor(currentRequestItem))
                            .setRequestChaptersListener(getApplicationContext(), onRequestChaptersListener)
                            .requestChapterList(getApplicationContext(), currentRequestItem, new VolleyDataService.DataServiceCallBack() {
                                @Override
                                public void onSuccess(Object result) {
                                    ArrayList<Chapter> chapterList = (ArrayList<Chapter>) result;
                                    startDownLoadChapter(chapterList, task, bookChapterDao);
                                }

                                @Override
                                public void onError(Exception error) {
                                    onTaskFail(task);
                                }

                            });
                    if (chapterList != null && chapterList.size() > 0) {
                        task.endSequence = chapterList.size();
                    }
                } catch (Exception e) {
                    onTaskFail(task);
                    e.printStackTrace();
                }
            } else {
                startDownLoadChapter(chapterList, task, bookChapterDao);
            }
        }
    }

    public void startDownLoadChapter(final ArrayList<Chapter> chapterList, final BookTask task, final BookChapterDao bookChapterDao) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                downChapter(chapterList, task, bookChapterDao);
                if (chapterList != null) {
                    chapterList.clear();
                }
                onOffLineFinish(task);
            }
        }).start();

    }

    public void setUiContext(Context context) {
        uiContext = context;
    }

    public void setOnDownloadListener(OnDownloadListener downloadListener) {
        downloadListenerRef = new WeakReference<>(downloadListener);
    }

    private void showNotification(String bookName, int progress, int index, int total, String book_id) {
        if (nftmgr == null) {
            nftmgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        if (index > total) {
            index = total;
        }
        int x = progress == 100 ? index + 1 : index;
        String content = "内容解析中。。 " + x + "/" + total + "  " + progress + "%";
        String tickerText = getString(R.string.downloadservice_nofify_ticker) + bookName + "》";
        Notification preNTF = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(tickerText)
                .setContentText(content).build();
        preNTF.when = System.currentTimeMillis();
        preNTF.flags = Notification.FLAG_AUTO_CANCEL;
        OnDownloadListener downloadListener = downloadListenerRef.get();
        if (downloadListener != null) {
            downloadListener.notificationCallBack(preNTF, book_id);
        }
        if (nftmgr != null) {
            nftmgr.notify(ntfId, preNTF);
        }
    }

    /********************
     * 重构的方法
     ********************/
    /*
     * 删除book_id任务
	 */
    public void dellTask(String book_id) {
        if (mTaskQueue != null && mTaskQueue.containsKey(book_id)) {
            BookTask remove = mTaskQueue.remove(book_id);
            if (remove != null && remove.cacheLoader != null) {
                remove.cacheLoader.delete();
            }
            downloadRequestItem.remove(book_id);
            if (null != curTask && curTask.book != null && book_id.equals(curTask.book.book_id)) {
                curTask.state = DownloadState.PAUSEED;

                if (curTask.cacheLoader != null) {
                    curTask.cacheLoader.delete();
                }

                if (notificationManager != null) {
                    notificationManager.cancel(ntfId);
                }
                updateTask();
            }
        }
    }

    /*
     * 删除gid任务
     */
    public void dellTask(int gid) {
        if (mTaskQueue != null && mTaskQueue.containsKey(gid)) {
            BookTask remove = mTaskQueue.remove(gid);
            if (remove != null && remove.cacheLoader != null) {
                curTask.cacheLoader.delete();
            }
            if (null != curTask && curTask.book != null && gid == curTask.book.gid) {
                curTask.state = DownloadState.PAUSEED;

                if (curTask.cacheLoader != null) {
                    curTask.cacheLoader.delete();
                }
                if (notificationManager != null) {
                    notificationManager.cancel(ntfId);
                }
                updateTask();
            }
        }
    }

    /*
     * 启动任务
     */
    public void startTask(String book_id) {
        BookTask bookTask = mTaskQueue.get(book_id);

        //更新书籍信息
        bookTask.book = mBookDaoHelper.getBook(book_id, 0);

        if (bookTask.state == DownloadState.DOWNLOADING || bookTask.state == DownloadState.WAITTING) {
            return;
        }
        if (bookTask.state == DownloadState.FINISH) {
            Toast.makeText(BaseBookApplication.getGlobalContext(), "离线缓存已完成", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bookTask != null) {
            if (bookTask.state == DownloadState.REFRESH) {
                bookTask.startSequence = BaseBookHelper.getStartDownIndex(DownloadService.this, bookTask.book);
            }

            bookTask.state = DownloadState.WAITTING;

            //遍历队列，如果当前有任务正处在下载中的话，就不执行updateTask操作。
            Collection<Map.Entry<String, BookTask>> list = mTaskQueue.getAll();
            for (Map.Entry<String, BookTask> entry : list) {
                BookTask bt = entry.getValue();
                if (bt != null && bt.state == DownloadState.DOWNLOADING) {
                    return;
                }
            }
            updateTask();
        }
    }

    /*
     * 启动任务
     */
    public void startTask(String book_id, int startDownIndex) {
        BookTask bookTask = mTaskQueue.get(book_id);
        if (bookTask.state == DownloadState.DOWNLOADING || bookTask.state == DownloadState.WAITTING) {
            return;
        }
        if (bookTask != null) {
            bookTask.state = DownloadState.WAITTING;
            bookTask.startSequence = Math.max(0, startDownIndex);
        }

        //遍历队列，如果当前有任务正处在下载中的话，就不执行updateTask操作。
        Collection<Map.Entry<String, BookTask>> list = mTaskQueue.getAll();
        for (Map.Entry<String, BookTask> entry : list) {
            BookTask bt = entry.getValue();
            if (bt != null && bt.state == DownloadState.DOWNLOADING) {
                return;
            }
        }
        updateTask();
    }

    /*
     * 更新任务，如果当前任务为空或者非正在下载状态，则运行队列里下一个等待任务
     */
    private void updateTask() {

        if ((curTask == null || curTask.state != DownloadState.DOWNLOADING) && !mTaskQueue.isEmpty()) {
            Collection<Map.Entry<String, BookTask>> list = mTaskQueue.getAll();
            for (Map.Entry<String, BookTask> entry : list) {
                BookTask bookTask = entry.getValue();
                if (bookTask != null && bookTask.state == DownloadState.WAITTING) {
                    curTask = bookTask;
                    runCurrTask();
                    break;
                }
            }
        }
    }

    public interface OnDownloadListener {
        void notificationCallBack(Notification preNTF, String book_id);
    }

    private class BackgroundThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            return t;
        }
    }

    public class MyBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    private class MyRequestChaptersListener implements RequestExecutorDefault.RequestChaptersListener {

        BookTask bookTask;

        public MyRequestChaptersListener(BookTask bookTask) {
            this.bookTask = bookTask;
        }

        @Override
        public void requestSuccess(ArrayList<Chapter> chapterList) {

        }

        @Override
        public void requestFailed(int errorType, String errorMessage, int downIndex) {
            AppLog.e(TAG, "downChapters Failed:" + errorMessage);
            if (errorType == ERROR_TYPE_NETWORK_NONE) {
                onNetworkNone(bookTask, downIndex);
            }
        }
    }
}