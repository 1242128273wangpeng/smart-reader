package net.lzbook.kit.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.BookUpdate;
import com.ding.basic.bean.Chapter;
import com.ding.basic.bean.CheckItem;
import com.ding.basic.RequestRepositoryFactory;
import com.ding.basic.net.RequestSubscriber;
import com.ding.basic.net.api.ContentAPI;
import com.ding.basic.net.api.MicroAPI;
import com.ding.basic.net.api.RequestAPI;
import com.ding.basic.util.DataCache;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import net.lzbook.kit.R;
import net.lzbook.kit.constants.ActionConstants;
import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.utils.download.CacheManager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.bean.UpdateCallBack;
import net.lzbook.kit.bean.BookUpdateResult;
import net.lzbook.kit.bean.BookUpdateTaskData;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.book.CheckNovelUpdHelper;
import net.lzbook.kit.utils.ResourceUtil;
import net.lzbook.kit.utils.Tools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class CheckNovelUpdateService extends Service {
    public static final String ACTION_CHKUPDATE =
            AppUtils.getPackageName() + ".action_check_update";
    public static final String CLICK_ACTION = "cn.txtzsydsq.reader.receiver.CLICK_BOOK_UPDATE";
    public static final int novel_upd_notify_id = ResourceUtil.getStringById(
            R.string.app_name).hashCode();
    public static final int novel_update_notify_id = (ResourceUtil.getStringById(R.string.app_name)
            + "Update").hashCode();
    private final static String mFormat = "k:mm";
    private static final long FINISH_BOOK_REFRESHTIME = 24 * 60 * 60 * 1000;
    private static final long NOT_READ_END_REFRESHTIME = 60 * 60 * 1000;
    public static ArrayList<CheckNovelUpdHelper.MyBook> cache_list;
    public static boolean UPDATE_OWN_SUCCESS = true;
    int updateTotalCount = 0;//需要更新的总书籍数量
    int hasUpdatedCount = 0;//已经更新了的书籍数量
    ArrayList<BookUpdate> mUpdateBooks = new ArrayList<>();
    private String TAG = CheckNovelUpdateService.class.getSimpleName();
    private Handler h = new Handler();
    private CheckUpdateBinder binder;
    private NotificationManager nftmgr = null;
    private Random random;
    private int startTimeOffSet = -1;

    Handler timerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0:
                    checkInterval();
                    timerHandler.sendEmptyMessageDelayed(0, Constants.refreshTime);
                    break;
                case 1:
                    checkAuthAccess();
                    timerHandler.sendEmptyMessageDelayed(1, Constants.authAccessRefreshTime);
                    break;
            }
        }
    };

    private WeakReference<OnBookUpdateListener> onBookUpdateListenerWef;
    private boolean isFirst = false;
    private SelfCallBack selfCallBack;

    public static void startChkUpdService(Context ctt) {
        try {
            Intent intent = new Intent();
            intent.setClass(ctt, CheckNovelUpdateService.class);
            ctt.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        AppLog.e(TAG, "CheckNovelUpdateService : onCreate");
        super.onCreate();
        isFirst = true;
        if (startTimeOffSet == -1) {
            random = new Random();
            startTimeOffSet = random.nextInt(60);
        }
        binder = new CheckUpdateBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ACTION_CHKUPDATE.equals(intent.getAction())) {
                checkInterval();
                checkAuthAccess();
                AppUtils.appendLog(ACTION_CHKUPDATE, AppUtils.LOG_TYPE_BAIDUPUSH);
            }
        }

        if (isFirst) {
            timerHandler.sendEmptyMessageDelayed(1, Constants.authAccessRefreshTime);
            timerHandler.sendEmptyMessageDelayed(0, Constants.refreshTime);
            isFirst = false;
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        AppLog.e(TAG, "CheckNovelUpdateService : onBind");
        return binder;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        AppLog.e(TAG, "CheckNovelUpdateService : onStart");
        super.onStart(intent, startId);
    }

    public void onDestroy() {
        AppLog.e(TAG, "CheckNovelUpdateService : onDestroy");
        Intent restartIntent = new Intent(this, CheckNovelUpdateService.class);
        startService(restartIntent);
        super.onDestroy();
    }

    private void innerOnSuccess(BookUpdateResult result) {
        if (result != null) {
            ArrayList<BookUpdate> list = result.items;
            if (list == null) {
                return;
            }
            ArrayList<CheckNovelUpdHelper.MyBook> books = new ArrayList<>();
            String last_chapter_name = null;
            for (int i = 0; i < list.size(); i++) {
                BookUpdate item = list.get(i);
                if (!TextUtils.isEmpty(item.getBook_id()) && item.getUpdate_count() != 0) {
                    last_chapter_name = item.getLast_chapter_name();
                    if (!TextUtils.isEmpty(item.getBook_name())) {
                        CheckNovelUpdHelper.MyBook myBook = new CheckNovelUpdHelper.MyBook(
                                item.getBook_name(), item.getBook_id(), item.getUpdate_count());
                        books.add(myBook);
                    }
                }
            }

            books = CheckNovelUpdHelper.combain(this, books);
            if (books != null) {
                cache_list = books;
                int n = books.size();
                try {
                    Intent intent = new Intent(ActionConstants.ACTION_CHECK_UPDATE_FINISH);
                    sendBroadcast(intent);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                if (n > 0) {
                    if (n == 1) {
                        CheckNovelUpdHelper.MyBook myBook = books.get(0);
                        showNotification(getBookNameString2(books) + getString(
                                R.string.notification_update_catalog), last_chapter_name,
                                myBook.book_id);
                    } else {
                        String txt = "《" + books.get(0).name + "》" + getString(
                                R.string.notification_update_end) + n + getString(
                                R.string.notification_update_book_count);
                        showNotification(txt, getBookNameString2(books), "");
                    }
                }
            }
        }
    }

    private String getBookNameString2(ArrayList<CheckNovelUpdHelper.MyBook> names) {
        if (names == null || names.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (CheckNovelUpdHelper.MyBook b : names) {
            if (!TextUtils.isEmpty(b.name)) {
                sb.append("《").append(b.name).append("》");
            }
        }
        return sb.toString();
    }

    private void checkAuthAccess() {
        MicroAPI.INSTANCE.requestAuthAccess();
        ContentAPI.INSTANCE.requestAuthAccess();
    }

    private void checkInterval() {
        if (isUpdateTime()) {

            List<Book> books = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                    BaseBookApplication.getGlobalContext()).loadReadBooks();

            if (books != null) {
                BookUpdateTaskData bookUpdateTaskData = new BookUpdateTaskData();

                bookUpdateTaskData.books = checkBookUpdate((ArrayList<Book>) books);
                bookUpdateTaskData.from = BookUpdateTaskData.UpdateTaskFrom.FROM_SELF;
                selfCallBack = new SelfCallBack();
                bookUpdateTaskData.mCallBack = new WeakReference<UpdateCallBack>(selfCallBack);

                Logger.i("检查更新服务: 添加检查更新任务！");

                checkUpdate(bookUpdateTaskData);
            }
        }
    }

    private ArrayList<Book> checkBookUpdate(ArrayList<Book> books) {
        ArrayList<Book> doUpdateBooks = new ArrayList<>();
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            long new_time = System.currentTimeMillis();
            long old_time = book.getLast_check_update_time();
            long intervalTime = Math.abs(new_time - old_time);
            if (Book.STATUS_FINISH.equals(book.getStatus())) {
                if (intervalTime > FINISH_BOOK_REFRESHTIME - 1000) {
                    doUpdateBooks.add(book);
                }
            } else if (book.getSequence() + 1 < book.getChapter_count()) {
                if (intervalTime > NOT_READ_END_REFRESHTIME - 1000) {
                    doUpdateBooks.add(book);
                }
            } else {
                doUpdateBooks.add(book);
            }
        }
        return doUpdateBooks;
    }

    public void checkUpdate(final BookUpdateTaskData bookUpdateTaskData) {
        if (bookUpdateTaskData == null) {
            Logger.e("检查更新服务: 检查更新任务为空！");
            return;
        }

        mUpdateBooks.clear();
        hasUpdatedCount = 0;
        updateTotalCount = 0;

        ArrayList<Book> books = bookUpdateTaskData.books;
        ArrayList<Book> checkUpdateBooks = new ArrayList<Book>();

        for (Book book : books) {
            if (book != null && !TextUtils.isEmpty(book.getBook_id()) && !book.waitingCataFix()) {
                book.setLast_check_update_time(System.currentTimeMillis());
                checkUpdateBooks.add(book);
            }
        }

        final BookUpdateResult updateResult = new BookUpdateResult();

        if (checkUpdateBooks == null || checkUpdateBooks.size() == 0) {
            checkOnSuccess(bookUpdateTaskData, updateResult);
            return;
        }

        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).updateBooks(checkUpdateBooks);

        if (checkUpdateBooks.size() > 0) {
            if (UPDATE_OWN_SUCCESS) {
                UPDATE_OWN_SUCCESS = false;
            } else {
                checkOnCancel(bookUpdateTaskData, updateResult);
                return;
            }
            //部分4.2 手机报 retrofit 动态代理问题 java.lang.reflect.UndeclaredThrowableException at
            // $Proxy2.a(Native Method)
            try {
                handleCheckBookUpdate(checkUpdateBooks, bookUpdateTaskData, updateResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            UPDATE_OWN_SUCCESS = true;
        }
    }

    private void handleCheckBookUpdate(final ArrayList<Book> checkUpdateBooks,
            final BookUpdateTaskData data, final BookUpdateResult updateResult) {

        final ArrayList<Book> bookClone = (ArrayList<Book>) checkUpdateBooks.clone();

        final HashMap<String, Book> bookItems = getBookItems(bookClone);

        RequestBody checkBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                enclosureUpdateParameters(bookClone));

        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestBookUpdate(checkBody, bookItems,
                new RequestSubscriber<List<BookUpdate>>() {
                    @Override
                    public void requestResult(@Nullable List<BookUpdate> result) {
                        Logger.i("检查更新服务: 请求已返回");

                        ArrayList<BookUpdate> bookUpdates;
                        try {
                            if (result != null && result.size() > 0) {
                                bookUpdates = new ArrayList<>();

                                for (int i = 0; i < result.size(); i++) {
                                    BookUpdate bookUpdate = changeChapters(result.get(i));

                                    if (bookUpdate != null) {
                                        bookUpdates.add(bookUpdate);
                                    }
                                }
                                if (bookUpdates.size() > 0) {
                                    updateTotalCount = updateTotalCount + bookUpdates.size();
                                    hasUpdatedCount = hasUpdatedCount + bookUpdates.size();
                                    if (bookUpdates.size() > 0) {
                                        mUpdateBooks.addAll(bookUpdates);
                                    }
                                }
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }

                        UPDATE_OWN_SUCCESS = true;
                    }

                    @Override
                    public void requestError(@NotNull String message) {
                        Logger.e("检查更新服务: 检查书籍更新异常！");

                        updateResult.items = null;

                        UPDATE_OWN_SUCCESS = true;

                        if (data.mCallBack != null) {
                            checkOnSuccess(data, updateResult);
                        }
                    }

                    @Override
                    public void requestComplete() {
                        Logger.i("检查更新服务: 检查书籍更新完成！");
                        checkOnSuccess(data, updateResult);
                    }
                });
    }

    private boolean isUpdateTime() {
        if (startTimeOffSet == -1) {
            random = new Random();
            startTimeOffSet = random.nextInt(60);
        }
        int startHour = 6;
        int startMinute = startTimeOffSet;
        AppLog.d(TAG, "startTimeOffSet = " + startMinute);
        int stopHour = 1;
        int stopMinute = 0;
        Calendar c = Calendar.getInstance();
        int curHour = c.get(Calendar.HOUR_OF_DAY);
        int curMinute = c.get(Calendar.MINUTE);
        int cur = curHour * 60 + curMinute;
        return (cur >= startHour * 60 + startMinute || cur <= stopHour * 60 + stopMinute);
    }

    private void checkOnSuccess(final BookUpdateTaskData data, final BookUpdateResult result) {

        if (UPDATE_OWN_SUCCESS) {
            if (mUpdateBooks != null && mUpdateBooks.size() > 0) {
                //更新缓存任务状态
                BookUpdate[] arr = new BookUpdate[mUpdateBooks.size()];
                io.reactivex.Observable.fromArray(mUpdateBooks.toArray(arr))
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                CacheManager.INSTANCE.checkAutoStart();
                            }
                        })
                        .subscribe(new Consumer<BookUpdate>() {
                            @Override
                            public void accept(BookUpdate myBook) throws Exception {
                                CacheManager.INSTANCE.freshBook(myBook.getBook_id(), true);
                            }
                        });
            } else {
                //检测一次是否有符合标准的
                CacheManager.INSTANCE.checkAutoStart();
            }

            if (hasUpdatedCount == updateTotalCount) {
                h.post(new Runnable() {

                    @Override
                    public void run() {
                        if (data.mCallBack.get() != null) {
                            result.items = mUpdateBooks;
                            data.mCallBack.get().onSuccess(result);
                        }
                    }
                });
            } else {
                h.post(new Runnable() {

                    @Override
                    public void run() {
                        if (data.mCallBack.get() != null) {
                            data.mCallBack.get().onException(new Exception("update failed!"));
                        }
                    }
                });
            }
        }
    }

    private void checkOnCancel(final BookUpdateTaskData data, final BookUpdateResult result) {
        h.post(new Runnable() {

            @Override
            public void run() {
                if (data.mCallBack.get() != null) {
                    result.items = mUpdateBooks;
                    data.mCallBack.get().onSuccess(result);
                }
            }
        });
    }

    private void showNotification(String tickerText, String content, String book_id) {
        if (shouldAlarm() && isPushOn() && !TextUtils.isEmpty(content)) {
            if (nftmgr == null) {
                nftmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }

            /**
             * bug异常：
             * android.app.RemoteServiceException:
             *      Bad notification posted from package cc.lianzainovel:
             *          Couldn't create icon: StatusBarIcon(pkg=cc.lianzainoveluser=0
             *          id=0x7f020176 level=0 visible=true num=0 )
             *
             * 这个问题多数集中在setSmallIcon(R.drawable.icon)这句代码上，
             * 在某些情况下，比如开启重启动系统就要发送通知，R.drawable.icon这个资源尚未准备好，导致了App异常
             * android5.0的bug，在android4.4和6.0中都正常
             * 解决方式：.setSmallIcon(getApplicationContext().getApplicationInfo().icon)
             */
            try {
                Notification preNTF = new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(getApplicationContext().getApplicationInfo().icon)
//                    .setSmallIcon(R.drawable.icon)
                        .setContentTitle(tickerText)
                        .setContentText(content).build();
                if (shouldSound()) {
                    preNTF.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
                }

                preNTF.when = System.currentTimeMillis();
                preNTF.flags = Notification.FLAG_AUTO_CANCEL;
                if (onBookUpdateListenerWef != null && onBookUpdateListenerWef.get() != null) {
                    onBookUpdateListenerWef.get().receiveUpdateCallBack(preNTF);
                } else {
                    preNTF.contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                            new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
                }
                nftmgr.notify(novel_upd_notify_id, preNTF);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private String getCurTime() {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        AppLog.d(TAG, "System.currentTimeMillis() " + System.currentTimeMillis());
        CharSequence s = DateFormat.format(mFormat, mCalendar);
        AppLog.i(TAG, "更新结果通知时间  " + s);
        return (String) s;
    }

    private boolean shouldAlarm() {
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!sp.getBoolean("push_time", true)) {
            return true;
        }
        int startHour = sp.getInt("push_time_start_hour", 7);
        int startMinute = sp.getInt("push_time_start_minute", 0);
        int stopHour = sp.getInt("push_time_stop_hour", 23);
        int stopMinute = sp.getInt("push_time_stop_minute", 0);
        Calendar c = Calendar.getInstance();
        int curHour = c.get(Calendar.HOUR_OF_DAY);
        int curMinute = c.get(Calendar.MINUTE);
        int cur = curHour * 60 + curMinute;
        if (startHour * 60 + startMinute >= stopHour * 60 + stopMinute) {
            return true;
        }
        return (cur >= startHour * 60 + startMinute && cur <= stopHour * 60 + stopMinute);
    }

    private boolean shouldSound() {
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sp.getBoolean("push_sound", true);
    }

    private boolean isPushOn() {
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sp.getBoolean("settings_push", true);
    }

    public void setBookUpdateListener(OnBookUpdateListener l) {
        onBookUpdateListenerWef = new WeakReference<>(l);
    }

    public interface OnBookUpdateListener {
        void receiveUpdateCallBack(Notification preNTF);
    }

    public class CheckUpdateBinder extends Binder {
        public CheckNovelUpdateService getService() {
            return CheckNovelUpdateService.this;
        }
    }

    class SelfCallBack implements UpdateCallBack {

        @Override
        public void onSuccess(BookUpdateResult result) {
            AppLog.e(TAG, "CheckNovelUpdateService OnSuccess!");
            innerOnSuccess(result);
        }

        @Override
        public void onException(Exception e) {

        }
    }

    private HashMap<String, Book> getBookItems(ArrayList<Book> books) {
        HashMap<String, Book> map = new HashMap<>();
        for (Book book : books) {
            map.put(book.getBook_id(), book);
        }
        return map;
    }


    public String enclosureUpdateParameters(ArrayList<Book> books) {
        String result;

        List<CheckItem> checkItems = new ArrayList<>();

        CheckItem checkItem;

        for (Book book : books) {
            if (book != null && !TextUtils.isEmpty(book.getBook_id())) {

                Chapter lastChapter = book.getLast_chapter();

                if (lastChapter == null || TextUtils.isEmpty(lastChapter.getChapter_id())) {
                    Logger.e("检查更新服务: 书籍章节目录为空！");
                    continue;
                }

                checkItem = new CheckItem();
                checkItem.setBook_id(book.getBook_id());
                checkItem.setBook_source_id(book.getBook_source_id());
                checkItem.setBook_chapter_id(book.getBook_chapter_id());
                checkItem.setLast_chapter_id(lastChapter.getChapter_id());
                checkItem.setList_version(book.getList_version());
                checkItem.setC_version(book.getC_version());
                checkItem.setAdd_bookshelf_time(book.getInsert_time());

                checkItems.add(checkItem);
            }
        }

        result = new Gson().toJson(checkItems);

        Logger.i("检查更新服务: 请求更新的书籍信息为: " + result);

        return result;
    }

    public BookUpdate changeChapters(BookUpdate bookUpdate) {
        RequestRepositoryFactory repositoryFactory =
                RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                        BaseBookApplication.getGlobalContext());
        if (bookUpdate != null && !TextUtils.isEmpty(bookUpdate.getBook_id())) {
            Book book = repositoryFactory.loadBook(bookUpdate.getBook_id());

            BookUpdate resUpdate = null;

            if (book != null && bookUpdate.getChapterList() != null
                    && bookUpdate.getChapterList().size() > 0) {
                // 增加更新章节
                repositoryFactory.insertOrUpdateChapter(book.getBook_id(),
                        bookUpdate.getChapterList());
                // 更新书架信息
                Chapter lastChapter = bookUpdate.getChapterList().get(
                        bookUpdate.getChapterList().size() - 1);

                book.setChapter_count(repositoryFactory.getChapterCount(book.getBook_id()));
                book.setLast_chapter(lastChapter);
                book.setUpdate_status(1);

                // 没有返回更新章节的书籍更新book.last_updateUpdateTime, 有更新的书籍更新对应信息
                repositoryFactory.updateBook(book);

                // 返回bookUpdate
                resUpdate = new BookUpdate();
                resUpdate.setBook_name(book.getName());
                resUpdate.setBook_id(book.getBook_id());
                resUpdate.setLast_chapter_name(lastChapter.getName());
                resUpdate.setUpdate_count(bookUpdate.getChapterList().size());

                if (Constants.DEVELOPER_MODE) {
                    StringBuilder update_log = new StringBuilder();
                    update_log.append("book_id : ").append(book.getBook_id()).append(" \\\n");
                    update_log.append("book_source_id : ").append(book.getBook_source_id()).append(
                            " \\\n");
                    update_log.append("book_name : ").append(bookUpdate.getBook_name()).append(
                            " \\\n");
                    update_log.append("update_count_service : ").append(
                            bookUpdate.getChapterList().size()).append(" \\\n");
                    update_log.append("update_count_local : ").append(
                            book.getChapter_count()).append(
                            " \\\n");
                    update_log.append("last_chapter_name_service : ").append(
                            lastChapter.getName()).append(" \\\n");
                    update_log.append("last_chapter_name_local : ").append(book.getName()).append(
                            " \\\n");
                    update_log.append("update_time : ").append(
                            Tools.logTime(AppUtils.log_formatter,
                                    lastChapter.getUpdate_time())).append(
                            " \\\n");
                    update_log.append("system_time : ").append(
                            Tools.logTime(AppUtils.log_formatter,
                                    System.currentTimeMillis())).append(
                            " \\\n");
                    DataCache.saveUpdateLog(update_log.toString());
                }
            }

            return resUpdate;
        }

        return null;
    }
}