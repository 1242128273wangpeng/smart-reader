package net.lzbook.kit.repair_books;

import net.lzbook.kit.R;
import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.data.NullCallBack;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.data.update.UpdateBean;
import net.lzbook.kit.repair_books.bean.BookFix;
import net.lzbook.kit.repair_books.bean.FixContentState;
import net.lzbook.kit.request.DataCache;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.BaseBookHelper;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.ToastUtils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuchao on 2017/11/1 0001.
 */

public class RepairHelp {
    private static final String TAG = RepairHelp.class.getSimpleName();

    public static synchronized void parserData(UpdateBean.DataBean repairData) {
        if (repairData == null) {
            return;
        }

        final List<UpdateBean.DataBean.FixBookBean> fix_books = repairData.getFix_book();
        final List<UpdateBean.DataBean.FixContentBean> fix_contents = repairData.getFix_content();

        if (fix_books != null && !fix_books.isEmpty()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    saveFixBook(fix_books);
                }
            }).start();
        }

        if (fix_contents != null && !fix_contents.isEmpty()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    fixChapterContent(fix_contents);
                }
            }).start();
        }
    }

    private static void saveFixBook(List<UpdateBean.DataBean.FixBookBean> fix_books) {
        BookDaoHelper instance = BookDaoHelper.getInstance();
        if (instance == null) {
            return;
        }
        for (UpdateBean.DataBean.FixBookBean fixBookBean : fix_books) {
            Book book = instance.getBook(fixBookBean.getBook_id(), 0);
            if (!TextUtils.isEmpty(book.book_id)) {
                if (book.list_version == -1 || book.c_version == -1) {
                    book.list_version = fixBookBean.getList_version();
                    book.c_version = fixBookBean.getC_version();
                    boolean isupdateBookSucess = instance.updateBook(book);
                    AppLog.d(TAG, "saveFixBook -- book.name = " + book.name + "isupdateBookSucess = " + isupdateBookSucess);
                } else {
                    BookFix bookFix = new BookFix();
                    bookFix.book_id = fixBookBean.getBook_id();
                    bookFix.list_version = fixBookBean.getList_version();
                    bookFix.c_version = fixBookBean.getC_version();
                    bookFix.fix_type = 2;
                    boolean isinsertBookFixSucess = instance.insertBookFix(bookFix);
                    AppLog.d(TAG, "saveFixBook -- book.name = " + book.name + "isinsertBookFixSucess = " + isinsertBookFixSucess);
                }
            }
        }
    }

    private static void fixChapterContent(List<UpdateBean.DataBean.FixContentBean> fix_contents) {
        for (UpdateBean.DataBean.FixContentBean fixContentBook : fix_contents) {
            if (fixContentBook.getChapters() != null && !fixContentBook.getChapters().isEmpty()) {
                if (TextUtils.isEmpty(fixContentBook.getBook_id())) {
                    continue;
                }
                BookChapterDao chapterDao = new BookChapterDao(BaseBookApplication.getGlobalContext(), fixContentBook.getBook_id());
                FixContentState fixState = new FixContentState();
                boolean isNoChapterID = false;
                for (UpdateBean.DataBean.FixContentBean.ChaptersBean c : fixContentBook.getChapters()) {
                    //1.修复章节表
                    if (TextUtils.isEmpty(c.getId())) {
                        fixState.addMsgState(false);
                        continue;
                    }
                    Chapter chapter = chapterDao.getChapterById(c.getId());
                    if (chapter == null) {
                        // 根据章节id找不到该章节,这种情况可能是2016年数据流改版之前缓存的书籍
                        // 处理方式: 进入fix_book逻辑
                        fixState.addMsgState(false);
                        isNoChapterID = true;
                        continue;
                    }
                    chapter.book_source_id = c.getBook_souce_id();
                    chapter.chapter_name = c.getName();
                    chapter.sort = c.getSerial_number();
                    chapter.site = c.getHost();
                    chapter.curl = c.getUrl();
                    chapter.chapter_status = c.getStatus();
                    chapter.time = c.getUpdate_time();
                    chapter.word_count = c.getWord_count();
                    boolean isUpdateChapterByIdSucess = chapterDao.updateChapterById(chapter);
                    fixState.addMsgState(isUpdateChapterByIdSucess);
                    AppLog.d(TAG, "fixChapterContent --- chapter.chapter_name = " + chapter.chapter_name + "isUpdateChapterByIdSucess = " + isUpdateChapterByIdSucess);
                    //2.修复章节缓存内容
                    fixChapterContent(chapter, fixState);

                }

                BookDaoHelper instance = BookDaoHelper.getInstance();
                if (fixState.getFixState()) {
                    if (instance != null) {
                        Book book = instance.getBook(fixContentBook.getBook_id(), 0);
                        if (!TextUtils.isEmpty(book.book_id)) {
                            book.list_version = fixContentBook.getList_version();
                            book.c_version = fixContentBook.getC_version();
                            instance.updateBook(book);
                            if (fixState.getSaveFixState()) {
                                BookFix bookFix = new BookFix();
                                bookFix.book_id = book.book_id;
                                bookFix.fix_type = 1;
                                boolean isinsertBookFixSucess = instance.insertBookFix(bookFix);
                                AppLog.d(TAG, "fixChapterContent --- book.name = " + book.name + "isinsertBookFixSucess = " + isinsertBookFixSucess);
                            }
                        }
                    }
                }

                if (isNoChapterID) {
                    BookFix bookFix = new BookFix();
                    bookFix.book_id = fixContentBook.getBook_id();
                    bookFix.list_version = fixContentBook.getList_version();
                    bookFix.c_version = fixContentBook.getC_version();
                    bookFix.fix_type = 2;
                    boolean isinsertBookSucess = instance.insertBookFix(bookFix);
                    AppLog.d(TAG, "isNoChapterID -- isinsertBookSucess = " + isinsertBookSucess);
                }

            }
        }
    }

    private static void fixChapterContent(Chapter chapter, FixContentState fixState) {
        if (chapter != null && !TextUtils.isEmpty(chapter.curl) && DataCache.isChapterExists(chapter.sequence, chapter.book_id)) {

            try {
                String url = UrlUtils.buildContentUrl(chapter.curl);
                chapter.content = BaseBookApplication.getGlobalContext().getMainExtractorInterface().extract(url);
                if (!TextUtils.isEmpty(chapter.content)) {
                    chapter.content = chapter.content.replace("\\n", "\n");
                    chapter.content = chapter.content.replace("\\n\\n", "\n");
                    chapter.content = chapter.content.replace("\\n \\n", "\n");
                    chapter.content = chapter.content.replace("\\", "");
                }

                String content = chapter.content;
                if (TextUtils.isEmpty(content)) {
                    content = "null";
                }

                fixState.addContState(DataCache.fixChapter(content, chapter.sequence, chapter.book_id));
            } catch (Exception e) {
                fixState.addContState(false);
                e.printStackTrace();
            }
        }
    }

    public static void showFixMsg(Activity activity, Book book, FixCallBack fixCallBack) {

        BookDaoHelper instance = BookDaoHelper.getInstance();
        if (instance != null) {
            if (instance.isBookSubed(book.book_id)) {
                BookFix bookFix = instance.getBookFix(book.book_id);
                if (!TextUtils.isEmpty(bookFix.book_id)) {
                    if (bookFix.fix_type == 1) {
                        ToastUtils.showToastNoRepeat("本书问题章节已精修完成");
                        instance.deleteBookFix(bookFix.book_id);
                    } else if (bookFix.fix_type == 2) {
                        if (NetWorkUtils.isNetworkAvailable(activity) && bookFix.dialog_flag != 1) {
                            showFixHintDialog(activity, instance, book, bookFix, fixCallBack);
                        }
                    }
                }
            }
        }

    }


    private static boolean isComfire = false;
    private static void showFixHintDialog(final Activity activity, final BookDaoHelper instance, final Book book, final BookFix bookFix, final FixCallBack fixCallBack) {
        if (activity != null && !activity.isFinishing()) {
            isComfire = false;
            final MyDialog myDialog = new MyDialog(activity, R.layout.fixbook_hint_dialog);
            myDialog.setCanceledOnTouchOutside(true);
            TextView dialog_comfire = (TextView) myDialog.findViewById(R.id.publish_leave);
            dialog_comfire.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isComfire = true;
                    myDialog.dismiss();
                    if (NetWorkUtils.isNetworkAvailable(activity)) {
                        fixBook(instance, book, bookFix, fixCallBack);
                    } else {
                        ToastUtils.showToastNoRepeat("网络不给力，请检查网络连接");
                    }
                    Map<String, String> data2 = new HashMap<>();
                    data2.put("type", "1");
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.REPAIRDEDIALOGUE, data2);
                }
            });
            TextView dialog_cancle = (TextView) myDialog.findViewById(R.id.publish_stay);
            dialog_cancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myDialog.dismiss();
                }
            });
            myDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    bookFix.dialog_flag = 1;
                    instance.updateBookFix(bookFix);
                    if (!isComfire) {
                        Map<String, String> data2 = new HashMap<>();
                        data2.put("type", "2");
                        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.REPAIRDEDIALOGUE, data2);
                    }
                }
            });
            if (!myDialog.isShowing()) {
                try {
                    myDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void fixBook(final BookDaoHelper instance, final Book book, final BookFix bookFix, final FixCallBack fixCallBack) {
        //1.删除缓存
        //2.删除目录
        //3.清除缓存队列中信息
        //4.全本缓存
        //5.更新书籍version
        //6.删除修复状态信息

        new Thread(new Runnable() {
            @Override
            public void run() {
                BookChapterDao bookChapterDao = new BookChapterDao(BaseBookApplication.getGlobalContext(), book.book_id);
                BaseBookHelper.deleteAllChapterCache(book.book_id, 0, bookChapterDao.getCount());
                DownloadService.clearTask(book.book_id);
                BaseBookHelper.delDownIndex(BaseBookApplication.getGlobalContext(), book.book_id);
                bookChapterDao.deleteBookChapters(0);

                DownloadService downloadService = BaseBookApplication.getDownloadService();

                if (downloadService != null) {
                    book.list_version = bookFix.list_version;
                    book.c_version = bookFix.c_version;
                    instance.updateBook(book);
                    downloadService.dellTask(book.book_id);
                    BaseBookHelper.writeDownIndex(BaseBookApplication.getGlobalContext(), book.book_id, false, 0);
                    downloadService.addTask(BaseBookHelper.getBookTask(BaseBookApplication.getGlobalContext(), book, DownloadState.NOSTART, new NullCallBack(), true));
                    downloadService.addRequestItem(book);
                    downloadService.startTask(book.book_id);
                    boolean isdeleteBookFixSucess = instance.deleteBookFix(book.book_id);
                    AppLog.d(TAG, "删除修复状态信息 -- isdeleteBookFixSucess = " + isdeleteBookFixSucess);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (fixCallBack != null) {
                                fixCallBack.toDownLoadActivity();
                            }
                        }
                    });
                }
            }
        }).start();

    }

    public static boolean isShowFixBtn(Context context, String book_id) {
        BookDaoHelper instance = BookDaoHelper.getInstance();
        if (instance != null) {
            if (instance.isBookSubed(book_id)) {
                BookFix bookFix = instance.getBookFix(book_id);
                if (!TextUtils.isEmpty(bookFix.book_id)) {
                    if (bookFix.fix_type == 2) {
                        if (NetWorkUtils.isNetworkAvailable(context)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void fixBook(final Context context, final Book book, final FixCallBack fixCallBack) {
        //1.删除缓存
        //2.删除目录
        //3.清除缓存队列中信息
        //4.全本缓存
        //5.更新书籍version
        //6.删除修复状态信息
        if (book == null) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                BookDaoHelper instance = BookDaoHelper.getInstance();
                if (instance != null) {
                    if (instance.isBookSubed(book.book_id)) {
                        BookFix bookFix = instance.getBookFix(book.book_id);
                        if (!TextUtils.isEmpty(bookFix.book_id)) {
                            if (bookFix.fix_type == 2) {
                                if (NetWorkUtils.isNetworkAvailable(context)) {
                                    BookChapterDao bookChapterDao = new BookChapterDao(BaseBookApplication.getGlobalContext(), book.book_id);
                                    BaseBookHelper.deleteAllChapterCache(book.book_id, 0, bookChapterDao.getCount());
                                    DownloadService.clearTask(book.book_id);
                                    BaseBookHelper.delDownIndex(BaseBookApplication.getGlobalContext(), book.book_id);
                                    bookChapterDao.deleteBookChapters(0);

                                    DownloadService downloadService = BaseBookApplication.getDownloadService();

                                    if (downloadService != null) {
                                        book.list_version = bookFix.list_version;
                                        book.c_version = bookFix.c_version;
                                        instance.updateBook(book);
                                        downloadService.dellTask(book.book_id);
                                        BaseBookHelper.writeDownIndex(BaseBookApplication.getGlobalContext(), book.book_id, false, 0);
                                        downloadService.addTask(BaseBookHelper.getBookTask(BaseBookApplication.getGlobalContext(), book, DownloadState.NOSTART, new NullCallBack(), true));
                                        downloadService.addRequestItem(book);
                                        downloadService.startTask(book.book_id);
                                        boolean isdeleteBookFixSucess = instance.deleteBookFix(book.book_id);
                                        AppLog.d(TAG, "删除修复状态信息 -- isdeleteBookFixSucess = " + isdeleteBookFixSucess);
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (fixCallBack != null) {
                                                    fixCallBack.toDownLoadActivity();
                                                }
                                            }
                                        });

                                    }

                                } else {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtils.showToastNoRepeat("网络不给力，请检查网络连接");
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }
        }).start();


    }

    public interface FixCallBack {
        void toDownLoadActivity();
    }
}
