package net.lzbook.kit.repair_books;

import net.lzbook.kit.R;
import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.book.view.MyDialog;
import com.ding.basic.bean.Book;
import com.ding.basic.bean.BookFix;
import com.ding.basic.bean.Chapter;
import com.ding.basic.bean.ContextFixState;
import com.ding.basic.bean.FixContent;
import com.ding.basic.bean.UpdateBean;
import com.ding.basic.repository.RequestRepositoryFactory;
import com.ding.basic.util.DataCache;
import com.dingyue.contract.util.CommonUtil;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.db.help.ChapterDaoHelper;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.BaseBookHelper;
import net.lzbook.kit.utils.NetWorkUtils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 书籍修复
 * Created by yuchao on 2017/11/1 0001.
 */
public class RepairHelp {
    private static final String TAG = RepairHelp.class.getSimpleName();

    private static SharedPreferences  sp = BaseBookApplication.getGlobalContext().getSharedPreferences(Constants.SHAREDPREFERENCES_KEY, 0);

    public static synchronized void parserData(UpdateBean repairData) {
        if (repairData == null) {
            return;
        }

        final List<BookFix> fix_books = repairData.getFix_books();
        final List<FixContent> fix_contents = repairData.getFix_contents();

        if (fix_books != null && !fix_books.isEmpty()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    saveBookFix(fix_books);
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

    private static void saveBookFix(List<BookFix> fix_books) {
        for (BookFix BookFixBean : fix_books) {
            Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(BookFixBean.getBook_id());
            if (book != null && !TextUtils.isEmpty(book.getBook_id())) {
                if (book.getList_version() == -1 || book.getC_version() == -1) {
                    book.setList_version(BookFixBean.getList_version());
                    book.setC_version(BookFixBean.getC_version());
                    RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).updateBook(book);
                } else {
                    BookFix bookFix = new BookFix();
                    bookFix.setBook_id(BookFixBean.getBook_id());
                    bookFix.setList_version(BookFixBean.getList_version());
                    bookFix.setC_version(BookFixBean.getC_version());
                    bookFix.setFix_type(2);

                    RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBookFix(bookFix);
                }
            }
        }
    }

    private static void fixChapterContent(List<FixContent> fix_contents) {
        for (FixContent fixContentBook : fix_contents) {
            if (fixContentBook.getChapters() != null && !fixContentBook.getChapters().isEmpty()) {
                if (TextUtils.isEmpty(fixContentBook.getBook_id())) {
                    continue;
                }
                ChapterDaoHelper chapterDao = ChapterDaoHelper.Companion.loadChapterDataProviderHelper(BaseBookApplication.getGlobalContext(), fixContentBook.getBook_id());
                ContextFixState fixState = new ContextFixState();

                boolean isNoChapterID = false;
                for (Chapter c : fixContentBook.getChapters()) {
                    //1.修复章节表
                    if (TextUtils.isEmpty(c.getChapter_id())) {
                        fixState.addMsgState(false);
                        continue;
                    }
                    Chapter chapter = chapterDao.getChapterById(c.getChapter_id());
                    if (chapter == null) {
                        // 根据章节id找不到该章节,这种情况可能是2016年数据流改版之前缓存的书籍
                        // 处理方式: 进入fix_book逻辑
                        fixState.addMsgState(false);
                        isNoChapterID = true;
                        continue;
                    }
                    chapter.setBook_source_id(c.getBook_source_id());
                    chapter.setName(c.getName());
                    chapter.setHost(c.getHost());
                    chapter.setUrl(c.getUrl());
                    chapter.setChapter_status(c.getChapter_status());
                    chapter.setUpdate_time(c.getUpdate_time());
                    chapter.setWord_count(c.getWord_count());
                    boolean isUpdateChapterByIdSucess = chapterDao.updateChapter(chapter);
                    fixState.addMsgState(isUpdateChapterByIdSucess);
                    AppLog.d(TAG, "fixChapterContent --- chapter.chapter_name = " + chapter.getName() + "isUpdateChapterByIdSucess = " + isUpdateChapterByIdSucess);
                    //2.修复章节缓存内容
                    fixChapterContent(chapter, fixState);

                }

                if (fixState.getFixState()) {
                    Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(fixContentBook.getBook_id());
                    if (book != null && !TextUtils.isEmpty(book.getBook_id())) {
                        book.setList_version(fixContentBook.getList_version());
                        book.setC_version(fixContentBook.getC_version());
                        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).updateBook(book);
                        if (fixState.getSaveFixState()) {
                            BookFix bookFix = new BookFix();
                            bookFix.setBook_id(book.getBook_id());
                            bookFix.setFix_type(1);
                            RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBookFix(bookFix);
                        }
                    }
                }

                if (isNoChapterID) {
                    BookFix bookFix = new BookFix();
                    bookFix.setBook_id(fixContentBook.getBook_id());
                    bookFix.setList_version(fixContentBook.getList_version());
                    bookFix.setC_version(fixContentBook.getC_version());
                    bookFix.setFix_type(2);
                    RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBookFix(bookFix);
                }

            }
        }
    }

    private static void fixChapterContent(Chapter chapter, ContextFixState fixState) {
        if (chapter != null && DataCache.isNewCacheExists(chapter)) {

            try {
                chapter.setContent(RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestChapterContentSync(chapter));

                String content = chapter.getContent();
                if (TextUtils.isEmpty(content)) {
                    content = "null";
                }

                fixState.addContState(DataCache.fixChapter(content, chapter));
            } catch (Exception e) {
                fixState.addContState(false);
                e.printStackTrace();
            }
        }
    }

    public static void showFixMsg(Activity activity, Book book, FixCallBack fixCallBack) {

        if ((RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book.getBook_id()) != null)) {
            BookFix bookFix = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBookFix(book.getBook_id());
            if (bookFix != null && !TextUtils.isEmpty(bookFix.getBook_id())) {
                if (bookFix.getFix_type() == 1) {
                    CommonUtil.showToastMessage("本书问题章节已精修完成");
                    RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).deleteBookFix(bookFix.getBook_id());
                } else if (bookFix.getFix_type() == 2) {
                    /*if (NetWorkUtils.isNetworkAvailable(activity) && bookFix.getDialog_flag() != 1) {*/
                    if (NetWorkUtils.isNetworkAvailable(activity)) {
                        showFixHintDialog(activity, book, bookFix, fixCallBack);
                    }
                }
            }
        }

    }

    /**
     * 判断是否修复书籍页
     */
    public static boolean showFixMsg(Book book) {

        if ((RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book.getBook_id()) != null)) {
            BookFix bookFix = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                    BaseBookApplication.getGlobalContext()).loadBookFix(book.getBook_id());
            if (bookFix != null && !TextUtils.isEmpty(bookFix.getBook_id())) {
                if (bookFix.getFix_type() == 1) {//书籍已修复
                    return true;

                }
            }
        }
        return false;
    }


    private static boolean isComfire = false;
    private static void showFixHintDialog(final Activity activity, final Book book, final BookFix bookFix, final FixCallBack fixCallBack) {
        if (activity != null && !activity.isFinishing()) {
            isComfire = false;
            final MyDialog myDialog = new MyDialog(activity, R.layout.fixbook_hint_dialog);
            myDialog.setCanceledOnTouchOutside(true);
            TextView dialog_confirm =  myDialog.findViewById(R.id.publish_leave);
            dialog_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 更新修复状态为已修复
                    sp.edit().putBoolean(Constants.IS_FIX_CATALOG, true).apply();
                    isComfire = true;
                    myDialog.dismiss();
                    if (NetWorkUtils.isNetworkAvailable(activity)) {
                        fixBook(book, bookFix, fixCallBack);
                    } else {
                        CommonUtil.showToastMessage("网络不给力，请检查网络连接");
                    }
                    Map<String, String> data2 = new HashMap<>();
                    data2.put("type", "1");
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.REPAIRDEDIALOGUE, data2);
                }
            });
            TextView dialog_cancel =  myDialog.findViewById(R.id.publish_stay);
            dialog_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myDialog.dismiss();
                }
            });
            myDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    bookFix.setDialog_flag(1);
                    RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).updateBookFix(bookFix);
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

    private static void fixBook(final Book book, final BookFix bookFix, final FixCallBack fixCallBack) {
        //1.删除缓存
        //2.删除目录
        //3.清除缓存队列中信息
        //4.全本缓存
        //5.更新书籍version
        //6.删除修复状态信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChapterDaoHelper bookChapterDao = ChapterDaoHelper.Companion.loadChapterDataProviderHelper(BaseBookApplication.getGlobalContext(), book.getBook_id());
                BaseBookHelper.removeChapterCacheFile(book);
                CacheManager.INSTANCE.remove(book.getBook_id());
                bookChapterDao.deleteAllChapters();

                book.setList_version(bookFix.getList_version());
                book.setC_version(bookFix.getC_version());

                if (book.getLast_chapter() != null) {
                    book.getLast_chapter().setChapter_id("");
                }

                RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).updateBook(book);

                CacheManager.INSTANCE.start(book.getBook_id(), 0);

                RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).deleteBookFix(book.getBook_id());

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (fixCallBack != null) {
                            fixCallBack.toDownLoadActivity();
                        }
                    }
                });

            }
        }).start();

    }

    public static boolean isShowFixBtn(Context context, String book_id) {
        if ((RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book_id) != null)) {
            BookFix bookFix = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBookFix(book_id);
            if (bookFix != null && !TextUtils.isEmpty(bookFix.getBook_id())) {
                if (bookFix.getFix_type() == 2) {
                    if (NetWorkUtils.isNetworkAvailable(context)) {
                        return true;
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
                if ((RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book.getBook_id()) != null)) {
                    BookFix bookFix = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBookFix(book.getBook_id());
                    if (bookFix != null && !TextUtils.isEmpty(bookFix.getBook_id())) {
                        if (bookFix.getFix_type() == 2) {
                            if (NetWorkUtils.isNetworkAvailable(context)) {
                                ChapterDaoHelper bookChapterDao = ChapterDaoHelper.Companion.loadChapterDataProviderHelper(BaseBookApplication.getGlobalContext(), book.getBook_id());
                                BaseBookHelper.removeChapterCacheFile(book);
                                CacheManager.INSTANCE.remove(book.getBook_id());
                                bookChapterDao.deleteAllChapters();

                                book.setList_version(bookFix.getList_version());
                                book.setC_version(bookFix.getC_version());

                                if (book.getLast_chapter() != null) {
                                    book.getLast_chapter().setChapter_id("");
                                }

                                RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).updateBook(book);
                                CacheManager.INSTANCE.start(book.getBook_id(), 0);
                                RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).deleteBookFix(book.getBook_id());
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (fixCallBack != null) {
                                            fixCallBack.toDownLoadActivity();
                                        }
                                    }
                                });

                            } else {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        CommonUtil.showToastMessage("网络不给力，请检查网络连接");
                                    }
                                });
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
