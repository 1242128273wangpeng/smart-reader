package net.lzbook.kit.book.component.service;

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
import android.widget.RemoteViews;


import com.quduquxie.network.DataService;
import com.quduquxie.network.DataServiceNew;

import net.lzbook.kit.R;
import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.UpdateCallBack;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookUpdate;
import net.lzbook.kit.data.bean.BookUpdateResult;
import net.lzbook.kit.data.bean.BookUpdateTaskData;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.net.volley.request.VolleyDataService;
import net.lzbook.kit.request.own.OtherRequestService;
import net.lzbook.kit.tasks.BaseAsyncTask;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.BeanParser;
import net.lzbook.kit.utils.CheckNovelUpdHelper;
import net.lzbook.kit.utils.FrameBookHelper.DownloadFinishReceiver;
import net.lzbook.kit.utils.OpenUDID;
import net.lzbook.kit.utils.ResourceUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

public class CheckNovelUpdateService extends Service {
    private String TAG = CheckNovelUpdateService.class.getSimpleName();
    public static final String ACTION_CHKUPDATE = AppUtils.getPackageName() + ".action_check_update";
    public static final String CLICK_ACTION ="cn.txtzsydsq.reader.receiver.CLICK_BOOK_UPDATE";
    private Handler h = new Handler();
    private CheckUpdateBinder binder;
    private NotificationManager nftmgr = null;
    public static final int novel_upd_notify_id = ResourceUtil.getStringById(R.string.app_name).hashCode();
    public static final int novel_update_notify_id = (ResourceUtil.getStringById(R.string.app_name) + "Update").hashCode();
    public static ArrayList<CheckNovelUpdHelper.MyBook> cache_list;
    private BookDaoHelper mBookDaoHelper;
    private final static String mFormat = "k:mm";
    private static final long FINISH_BOOK_REFRESHTIME = 24 * 60 * 60 * 1000;
    private static final long NOT_READ_END_REFRESHTIME = 60 * 60 * 1000;
    private Random random;
    private int startTimeOffSet = -1;
    private WeakReference<OnBookUpdateListener> onBookUpdateListenerWef;

    public class CheckUpdateBinder extends Binder {
        public CheckNovelUpdateService getService() {
            return CheckNovelUpdateService.this;
        }
    }

    @Override
    public void onCreate() {
        AppLog.e(TAG, "CheckNovelUpdateService : onCreate");
        super.onCreate();
        isFirst = true;
        if (startTimeOffSet == -1){
            random = new Random();
            startTimeOffSet = random.nextInt(60);
        }
        binder = new CheckUpdateBinder();
        init();
    }

    private void init() {
        if (mBookDaoHelper == null) {
            mBookDaoHelper = BookDaoHelper.getInstance(this);
        }
    }
    private boolean isFirst = false;
    Handler timerHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            checkInterval();
            timerHandler.sendEmptyMessageDelayed(0,Constants.refreshTime);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppLog.e(TAG, "CheckNovelUpdateService : onStartCommand");
        if (intent != null) {
            if (ACTION_CHKUPDATE.equals(intent.getAction())) {
                checkInterval();
                AppLog.e(TAG, "CheckNovelUpdateService : ACTION_CHKUPDATE");
                AppUtils.appendLog(ACTION_CHKUPDATE, AppUtils.LOG_TYPE_BAIDUPUSH);
            }
        }
        if (isFirst){
            timerHandler.sendEmptyMessageDelayed(0,Constants.refreshTime);
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

    private void innerOnSuccess(BookUpdateResult result) {
        if (result != null) {
            ArrayList<BookUpdate> list = result.items;
            if (list == null)
                return;
            ArrayList<CheckNovelUpdHelper.MyBook> books = new ArrayList<>();
            String last_chapter_name = null;
            if (mBookDaoHelper == null) {
                mBookDaoHelper = BookDaoHelper.getInstance(this);
            }
            for (int i = 0; i < list.size(); i++) {
                BookUpdate item = list.get(i);
                if (!TextUtils.isEmpty(item.book_id) && item.update_count != 0) {
                    last_chapter_name = item.last_chapter_name;
                    if (!TextUtils.isEmpty(item.book_name)) {
                        CheckNovelUpdHelper.MyBook myBook = new CheckNovelUpdHelper.MyBook(item.book_name, item.book_id, item.update_count);
                        books.add(myBook);
                    }
                }
            }

            books = CheckNovelUpdHelper.combain(this, books);
            if (books != null) {
                cache_list = books;
                int n = books.size();
                Intent intent = new Intent(DownloadFinishReceiver.ACTION_UPDATE_NOTIFY);
                sendBroadcast(intent);
                if (n > 0) {
                    if (n == 1) {
                        CheckNovelUpdHelper.MyBook myBook = books.get(0);
                        showNotification(getBookNameString2(books) + getString(R.string.notification_update_catalog), last_chapter_name, myBook.book_id);
                    } else {
                        String txt = "《" + books.get(0).name + "》" + getString(R.string.notification_update_end) + n + getString(R.string.notification_update_book_count);
                        showNotification(txt, getBookNameString2(books), "");
                    }
                }
            }
        }
    }

    private String getBookNameString2(ArrayList<CheckNovelUpdHelper.MyBook> names) {
        if (names == null || names.size() == 0)
            return null;
        StringBuilder sb = new StringBuilder();
        for (CheckNovelUpdHelper.MyBook b : names) {
            if (!TextUtils.isEmpty(b.name))
                sb.append("《").append(b.name).append("》");
        }
        return sb.toString();
    }

    private void checkInterval() {
        AppLog.i(TAG, "isUpdateTime() = " + isUpdateTime());
        if (isUpdateTime()) {
            BookUpdateTaskData data = new BookUpdateTaskData();
            if (mBookDaoHelper == null) {
                mBookDaoHelper = BookDaoHelper.getInstance(this);
            }
            ArrayList<Book> books = mBookDaoHelper.getBooksList();
            data.books = checkBookUpdate(books);
            data.from = BookUpdateTaskData.UpdateTaskFrom.FROM_SELF;
            data.mCallBack = new SelfCallBack();
            AppLog.d(TAG, "checkInterval----------> 添加任务");
            checkUpdate(data);
        }
    }

    private ArrayList<Book> checkBookUpdate(ArrayList<Book> books) {
        ArrayList<Book> doUpdateBooks = new ArrayList<>();
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            long new_time = System.currentTimeMillis();
            long old_time = book.last_checkupdatetime;
            long intervalTime = Math.abs(new_time - old_time);
            if (book.status == 2){
                if (intervalTime > FINISH_BOOK_REFRESHTIME - 1000){
                    doUpdateBooks.add(book);
                }
            }else if (book.sequence + 1 < book.chapter_count){
                if (intervalTime > NOT_READ_END_REFRESHTIME - 1000){
                    doUpdateBooks.add(book);
                }
            }else {
                doUpdateBooks.add(book);
            }
        }
        return doUpdateBooks;
    }

    int updateTotalCount = 0;//需要更新的总书籍数量
    int hasUpdatedCount = 0;//已经更新了的书籍数量
    public static boolean UPDATE_QG_SUCCESS = true;
    public static boolean UPDATE_OWN_SUCCESS = true;
    ArrayList<BookUpdate> mUpdateBooks = new ArrayList<>();

    public void checkUpdate(final BookUpdateTaskData data) {
        if (data == null) {
            AppLog.e("checkUpdate","书籍列表为空~");
            return;
        }
        mUpdateBooks.clear();
        hasUpdatedCount = 0;//已经更新了的书籍数量
        updateTotalCount = 0;
        AppLog.e("checkUpdate","checkUpdate");
        ArrayList<Book> books1 = data.books;
        ArrayList<Book> books = new ArrayList<>();
        // 屏蔽搜狗源书籍发送更新请求
        for(Book iBook : books1) {
            if (!Constants.SG_SOURCE.equals(iBook.site)) {
                books.add(iBook);
            }
        }
        final BookUpdateResult updateResult = new BookUpdateResult();
        if (books == null || books.size() == 0) {
            checkOnSuccess(data, updateResult);
            return;
        }
        final ArrayList<com.quduquxie.bean.Book> booksToUpdateOfQG = new ArrayList<com.quduquxie.bean.Book>();
        ArrayList<Book> booksToUpdateOfOWN = new ArrayList<Book>();
        for (Book book: books) {
            book.last_checkupdatetime = System.currentTimeMillis();
            if (mBookDaoHelper == null){
                mBookDaoHelper = BookDaoHelper.getInstance(this);
            }
            mBookDaoHelper.updateBook(book);
            if (Constants.QG_SOURCE.equals(book.site)){
                booksToUpdateOfQG.add(BeanParser.parseToQGBook(book));
            }else {
                booksToUpdateOfOWN.add(book);
            }
        }

        if (booksToUpdateOfOWN.size() > 0) {//自有的更新逻辑
            if (UPDATE_OWN_SUCCESS){
                UPDATE_OWN_SUCCESS = false;
            }else {
                checkOnCancel(data, updateResult);
                return;
            }
            RequestUpdateTask requestUpdateTask = new RequestUpdateTask(getApplicationContext(), updateResult, data, booksToUpdateOfOWN);
            requestUpdateTask.execute2();
        }else {
            UPDATE_OWN_SUCCESS = true;
        }

        if (booksToUpdateOfQG.size() > 0){//青果的更新逻辑
            AppLog.e("checkUpdate","开始更新青果的书籍...");
            if (UPDATE_QG_SUCCESS){
                UPDATE_QG_SUCCESS = false;
            }else {
                checkOnCancel(data, updateResult);
                return;
            }
            //取得需要更新的这些书的最后一章
            final ArrayList<com.quduquxie.bean.Chapter> chapters = new ArrayList<>();
            for (com.quduquxie.bean.Book book : booksToUpdateOfQG){
                com.quduquxie.bean.Chapter chapter = new com.quduquxie.bean.Chapter();
                chapter.id_book = book.id_book;
                BookChapterDao chapterDao = new BookChapterDao(getApplicationContext(), book.id_book);
                Chapter lastChapter = chapterDao.getLastChapter();
                if (lastChapter != null) {
                    chapter.serial_number = lastChapter.sort;
                    chapter.id_chapter = lastChapter.chapter_id;
                }
                chapters.add(chapter);
            }
            //开始更新
            CheckQGUpdateTask qgUpdateTask = new CheckQGUpdateTask(this,updateResult,data,booksToUpdateOfQG,chapters);
            qgUpdateTask.execute2();
        }else {
            UPDATE_QG_SUCCESS = true;
        }
    }

    public class CheckQGUpdateTask extends BaseAsyncTask<Void, Void, Void> {

        private Context context;
        private BookUpdateResult bookUpdateResult;
        private BookUpdateTaskData bookUpdateTaskData;
        private ArrayList<com.quduquxie.bean.Book> books;
        ArrayList<com.quduquxie.bean.Chapter> chapters ;

        public CheckQGUpdateTask(Context context, BookUpdateResult bookUpdateResult, BookUpdateTaskData bookUpdateTaskData, ArrayList<com.quduquxie.bean.Book> books,ArrayList<com.quduquxie.bean.Chapter> chapters) {
            this.context = context;
            this.bookUpdateResult = bookUpdateResult;
            this.bookUpdateTaskData = bookUpdateTaskData;
            this.books = books;
            this.chapters = chapters;
        }

        @Override
        protected Void doInBackground(Void... params) {
            final String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
            DataServiceNew.checkNovelUpdate(context, books, chapters, udid, new DataServiceNew.DataServiceCallBack() {
                @Override
                public void onSuccess(Object result) {
                    AppLog.e("checkUpdate","青果书籍更新信息请求成功"+result);
                    UPDATE_QG_SUCCESS = true;
                    if (result != null) {
                        final ArrayList<com.quduquxie.bean.BookUpdate> list = (ArrayList<com.quduquxie.bean.BookUpdate>) result;
                        updateTotalCount = updateTotalCount + list.size();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //已经获得到了需要更新的书，开始请求新的章节
                                int successBookCount = updateQGChapterLists(list, udid);
                                hasUpdatedCount = hasUpdatedCount + successBookCount;
                                ArrayList<BookUpdate> bookUpdates = BeanParser.buildOwnBookUpdateList(list, books);
                                mUpdateBooks.addAll(bookUpdates);
                                checkOnSuccess(bookUpdateTaskData, bookUpdateResult);
                            }
                        }).start();
                    }else {
                        checkOnSuccess(bookUpdateTaskData, bookUpdateResult);
                    }
                }

                @Override
                public void onError(Exception error) {
                    AppLog.e("checkUpdate","青果书籍更新信息请求失败"+error);
                    //提示用户更新失败
                    UPDATE_QG_SUCCESS = true;
                    checkOnSuccess(bookUpdateTaskData, bookUpdateResult);
                }
            });

            return null;
        }
    }

    /**
     * 根据传入的更新实例，更新这些书籍的章节信息。
     * @param list
     * @return 返回更新成功的书籍的数目。
     */
    public int updateQGChapterLists(ArrayList<com.quduquxie.bean.BookUpdate> list, String udid){
        int successBookCount = 0;
        if (list != null && list.size() > 0){
            for (int i = 0 ; i < list.size() ; i++) {
                com.quduquxie.bean.BookUpdate bookUpdate = list.get(i);
                boolean updateSuccess = getQGChapters(bookUpdate, getApplicationContext(), udid);
                if (updateSuccess) {
                    successBookCount++;
                }
            }
        }
        return successBookCount;
    }

    /**
     *
     * @param bookUpdate
     */
    private boolean getQGChapters(com.quduquxie.bean.BookUpdate bookUpdate,Context context,String udid){
        try {
            AppLog.e("getQGChapters","开始");
            BookChapterDao chapterDao = new BookChapterDao(context, bookUpdate.id_book);
            BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance(context);
            //获取数据
            ArrayList<com.quduquxie.bean.Chapter> list;
            if (bookUpdate.update_index == 1){//异常情况，需要先删除本地的章节，然后再全量更新。
                chapterDao.deleteBookChapters(0);
                list = DataService.getChapterList(CheckNovelUpdateService.this, bookUpdate.id_book,bookUpdate.update_index,Integer.MAX_VALUE-1,udid);
                dealBookMark(bookDaoHelper,bookUpdate.id_book,list.size());
            }else {//正常情况，直接增量更新。
                list = DataService.getChapterList(CheckNovelUpdateService.this, bookUpdate.id_book,bookUpdate.update_index,bookUpdate.update_count,udid);
            }

            //章节数据获取成功后，更新本地数据库
            AppLog.e("getQGChapters","bookUpdate.update_index="+bookUpdate.update_index+":bookUpdate.update_count="+bookUpdate.update_count);
            ArrayList<Chapter> chapterList = BeanParser.buildOWNChapterList(list,0,list.size());
            if (chapterList != null && !chapterList.isEmpty() && BookDaoHelper.getInstance(context).isBookSubed
                    (bookUpdate.id_book)) {

                boolean a = chapterDao.insertBookChapter(chapterList);
                Chapter lastChapter = chapterList.get(chapterList.size() - 1);
                Book book = bookDaoHelper.getBook(bookUpdate.id_book,0);
                book.book_id = bookUpdate.id_book;
                book.book_source_id = Constants.QG_SOURCE;
                book.parameter = "";
                book.extra_parameter = "";
                book.chapter_count = chapterDao.getCount();
                book.last_updatetime_native = lastChapter.time;
                book.last_updateSucessTime = lastChapter.time;
                book.last_chapter_name = lastChapter.chapter_name;
                book.last_sort = lastChapter.sort;
                book.gsort = lastChapter.gsort;
                book.last_chapter_md5 = lastChapter.book_chapter_md5;
                book.chapters_update_index = lastChapter.sequence + 2;//加1是当前章的serial_number，再加1是下一章的serial_number
                book.update_status = 1;
                boolean b = bookDaoHelper.updateBook(book);
                if (a && b){
                    return true;
                }else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void dealBookMark(BookDaoHelper bookDaoHelper,String book_id,int newChapterSize) {
        //修改这本书的sequence属性
        Book book = bookDaoHelper.getBook(book_id,0);
        if (book.sequence > newChapterSize){
            book.sequence = newChapterSize;
            book.offset = 0;
        }
        bookDaoHelper.updateBook(book);
        //删除这本书保存的所有书签
       bookDaoHelper.deleteBookMark(book_id);
    }

    private boolean isUpdateTime(){
        if (startTimeOffSet == -1){
            random = new Random();
            startTimeOffSet = random.nextInt(60);
        }
        int startHour = 6;
        int startMinute = startTimeOffSet;
        AppLog.d(TAG, "startTimeOffSet = "+startMinute );
        int stopHour = 1;
        int stopMinute = 0;
        Calendar c = Calendar.getInstance();
        int curHour = c.get(Calendar.HOUR_OF_DAY);
        int curMinute = c.get(Calendar.MINUTE);
        int cur = curHour * 60 + curMinute;
        return (cur >= startHour * 60 + startMinute || cur <= stopHour * 60 + stopMinute);
    }


    public class RequestUpdateTask extends BaseAsyncTask<Void, Void, Void> {
        private Context context;
        private BookUpdateResult bookUpdateResult;
        private BookUpdateTaskData bookUpdateTaskData;
        private ArrayList<Book> books;

        public RequestUpdateTask(Context context, BookUpdateResult bookUpdateResult, BookUpdateTaskData bookUpdateTaskData, ArrayList<Book> books) {
            this.context = context;
            this.bookUpdateResult = bookUpdateResult;
            this.bookUpdateTaskData = bookUpdateTaskData;
            this.books = books;
        }

        @Override
        public Void doInBackground(Void... params) {
            try {
                AppLog.e("checkUpdate","开始更新自有的书籍...");
                HashMap<String, String> parameter = new HashMap<>();
                HashMap<String, Book> bookItems = getBookItems(books);
                parameter.put("data",arrToJson(books));
                OtherRequestService.doBookUpdate(context,parameter,bookItems, new VolleyDataService.DataServiceCallBack() {

                    @Override
                    public void onSuccess(Object result) {
                        UPDATE_OWN_SUCCESS = true;
                        AppLog.e("checkUpdate", "自有书籍更新成功" + result);

                        ArrayList<BookUpdate> updateBooks = (ArrayList<BookUpdate>) result;
                        if (result != null && updateBooks.size() >0) {
                                updateTotalCount = updateTotalCount + updateBooks.size();
                                hasUpdatedCount = hasUpdatedCount + updateBooks.size();
                                if (result != null && updateBooks.size() > 0) {
                                    AppLog.e(TAG, "RequestUpdateSuccess: " + updateBooks.toString());
                                mUpdateBooks.addAll(updateBooks);
                            }
                            checkOnSuccess(bookUpdateTaskData, bookUpdateResult);
                        } else{
                            checkOnSuccess(bookUpdateTaskData, bookUpdateResult);
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        AppLog.e("checkUpdate","自有书籍更新失败"+error);
                        bookUpdateResult.items = null;
                        UPDATE_OWN_SUCCESS = true;
                        if (bookUpdateTaskData.mCallBack != null) {
                            checkOnSuccess(bookUpdateTaskData, bookUpdateResult);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public String arrToJson(ArrayList<Book> books) {
            String jsonresult = "";
            try {
                JSONArray jsonarray = new JSONArray();
                for (int i = 0; i <books.size() ; i++) {
                    Book book = books.get(i);
                    BookChapterDao bookChapterDao = new BookChapterDao(getApplicationContext(), book.book_id);
                    Chapter lastChapter = bookChapterDao.getLastChapter();
                    if (lastChapter==null){
                        AppLog.e(TAG, "arrToJson lastChapter = null 检测书籍更时发现该书籍的目录为空!!!");
                    }
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("name", book.name);
                    jsonObj.put("book_id", book.book_id);
                    jsonObj.put("book_source_id", book.book_source_id);
                    jsonObj.put("host",lastChapter.site);
                    jsonObj.put("last_update", book.last_updateSucessTime);

                    ArrayList<Chapter> chapters = null;
                    AppLog.e(TAG, "arrToJson lastChapter.sequence = " + lastChapter.sequence);
                    if (lastChapter.sequence >= 9){
                        chapters = bookChapterDao.queryLastChapters(lastChapter.sequence,10);
                    } else {
                        chapters = bookChapterDao.queryBookChapter();
                    }

                    AppLog.e(TAG, "arrToJson chapters.size = " + chapters.size());
                    JSONArray array = new JSONArray();
                    for (int j = 0; j < chapters.size() ; j++) {
                        Chapter chapter = chapters.get(j);
                        JSONObject chapterObject = new JSONObject();

                        chapterObject.put("id",TextUtils.isEmpty(chapter.chapter_id) ? "" : chapter.chapter_id);
                        chapterObject.put("book_souce_id",TextUtils.isEmpty(chapter.book_source_id) ? book.book_source_id : chapter.book_source_id );
                        chapterObject.put("name",chapter.chapter_name);
                        chapterObject.put("serial_number",chapter.sequence);
                        chapterObject.put("host",chapter.site);
                        chapterObject.put("update_time",chapter.time);

                        array.put(chapterObject);
                    }
                    jsonObj.put("chapters",array);
                    jsonarray.put(jsonObj);
                }
                jsonresult = jsonarray.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            AppLog.d(TAG,"arrToJson 生成的json串为:"+jsonresult);
            return jsonresult;
        }

        private HashMap<String,Book> getBookItems(ArrayList<Book> books) {
            HashMap<String,Book> map = new HashMap<>();
            for (int i = 0; i < books.size() ; i++) {
                map.put(books.get(i).book_id, books.get(i));
            }
            return map;
        }
    }

    private void checkOnSuccess(final BookUpdateTaskData data, final BookUpdateResult result) {
        if (UPDATE_OWN_SUCCESS && UPDATE_QG_SUCCESS) {
            if (hasUpdatedCount == updateTotalCount) {
                h.post(new Runnable() {

                    @Override
                    public void run() {
                        if (data.mCallBack != null) {
                            result.items = mUpdateBooks;
                            data.mCallBack.onSuccess(result);
                        }
                    }
                });
            } else {
                h.post(new Runnable() {

                    @Override
                    public void run() {
                        if (data.mCallBack != null) {
                            data.mCallBack.onException(new Exception("update failed!"));
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
                if (data.mCallBack != null) {
                    result.items = mUpdateBooks;
                    data.mCallBack.onSuccess(result);
                }
            }
        });
    }

    private void showNotification(String tickerText, String content, String book_id) {
        if (shouldAlarm() && isPushOn() && !TextUtils.isEmpty(content)) {
            if (nftmgr == null) {
                nftmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }
            Notification preNTF = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle(tickerText)
                    .setContentText(content).build();
            if (shouldSound()) {
                preNTF.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
            }
            preNTF.when = System.currentTimeMillis();
            preNTF.flags = Notification.FLAG_AUTO_CANCEL;
            if (onBookUpdateListenerWef != null && onBookUpdateListenerWef.get() != null){
                onBookUpdateListenerWef.get().receiveUpdateCallBack(preNTF);
            }
            nftmgr.notify(novel_upd_notify_id, preNTF);
        }
    }

    private void showUpdateNotification(String tickerText, String content, String book_id) {
        if (shouldAlarm() && isPushOn() && !TextUtils.isEmpty(content)) {
            if (nftmgr == null) {
                nftmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }
            Notification notification = new Notification();
            notification.icon = R.drawable.icon;
            if (shouldSound()) {
                notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
            }
            notification.tickerText = tickerText;
            notification.when = System.currentTimeMillis();
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            String packageName = getPackageName();
            if (TextUtils.isEmpty(packageName)) {
                packageName = AppUtils.getPackageName();
            }
            notification.contentView = new RemoteViews(packageName, R.layout.notify_chk_novel_upd);
            notification.contentView.setTextViewText(R.id.notify_title_tv, tickerText);
            notification.contentView.setTextViewText(R.id.notify_text_tv, content);
            notification.contentView.setTextViewText(R.id.notify_time, getCurTime());
            try {
                Intent intent = new Intent();
                intent.setAction(CLICK_ACTION);
                intent.putExtra("book_id", book_id);
                intent.setPackage(packageName);
                PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notification.contentIntent = pending;
                nftmgr.notify(novel_update_notify_id, notification);
                AppUtils.appendLog("通知栏提示:" + book_id, AppUtils.LOG_TYPE_BAIDUPUSH);
                AppLog.e(TAG, "通知栏提示:" + book_id);
            } catch (Exception e) {
                e.printStackTrace();
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
        if (!sp.getBoolean("push_time", true))
            return true;
        int startHour = sp.getInt("push_time_start_hour", 7);
        int startMinute = sp.getInt("push_time_start_minute", 0);
        int stopHour = sp.getInt("push_time_stop_hour", 23);
        int stopMinute = sp.getInt("push_time_stop_minute", 0);
        Calendar c = Calendar.getInstance();
        int curHour = c.get(Calendar.HOUR_OF_DAY);
        int curMinute = c.get(Calendar.MINUTE);
        int cur = curHour * 60 + curMinute;
        if (startHour * 60 + startMinute >= stopHour * 60 + stopMinute)
            return true;
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

    public static void startChkUpdService(Context ctt) {
        try {
            Intent intent = new Intent();
            intent.setClass(ctt, CheckNovelUpdateService.class);
            ctt.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnBookUpdateListener {
        void  receiveUpdateCallBack(Notification preNTF);
    }

    public void setBookUpdateListener(OnBookUpdateListener l){
        onBookUpdateListenerWef = new WeakReference<>(l);
    }

}