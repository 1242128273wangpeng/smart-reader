package net.lzbook.kit.repair_books;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.BookFix;
import com.ding.basic.repository.RequestRepositoryFactory;
import com.dingyue.contract.util.CommonUtil;
import com.dingyue.contract.util.SharedPreUtil;
import com.dingyue.statistics.DyStatService;

import net.lzbook.kit.R;
import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.data.db.help.ChapterDaoHelper;
import net.lzbook.kit.pointpage.EventPoint;
import net.lzbook.kit.utils.BaseBookHelper;
import net.lzbook.kit.utils.NetWorkUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 书籍修复
 * Created by yuchao on 2017/11/1 0001.
 */
public class RepairHelp {

    private static SharedPreUtil sp =
            new SharedPreUtil(SharedPreUtil.SHARE_ONLINE_CONFIG);

    public static void showFixMsg(Activity activity, Book book, FixCallBack fixCallBack) {

        if ((RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).checkBookSubscribe(book.getBook_id())
                != null)) {
            BookFix bookFix = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                    BaseBookApplication.getGlobalContext()).loadBookFix(book.getBook_id());
            if (bookFix != null && !TextUtils.isEmpty(bookFix.getBook_id())) {
                if (bookFix.getFix_type() == 1) {
                    CommonUtil.showToastMessage("本书问题章节已精修完成");
                    RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                            BaseBookApplication.getGlobalContext()).deleteBookFix(
                            bookFix.getBook_id());
                } else if (bookFix.getFix_type() == 2) {
                    if (NetWorkUtils.isNetworkAvailable(activity)) {
                        showFixHintDialog(activity, book, bookFix, fixCallBack);
                    }
                }
            }
        }

    }


    private static boolean isComfire = false;

    private static void showFixHintDialog(final Activity activity, final Book book,
            final BookFix bookFix, final FixCallBack fixCallBack) {
        if (activity != null && !activity.isFinishing()) {
            isComfire = false;
            final MyDialog myDialog = new MyDialog(activity, R.layout.fixbook_hint_dialog);
            myDialog.setCanceledOnTouchOutside(true);
            TextView dialog_confirm = myDialog.findViewById(R.id.publish_leave);
            dialog_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 更新修复状态为已修复
                    sp.putBoolean(book.getBook_id(), false);
                    isComfire = true;
                    myDialog.dismiss();
                    if (NetWorkUtils.isNetworkAvailable(activity)) {
                        fixBook(book, bookFix, fixCallBack);
                    } else {
                        CommonUtil.showToastMessage("网络不给力，请检查网络连接");
                    }
                    Map<String, String> data2 = new HashMap<>();
                    data2.put("type", "1");
                    data2.put("bookid", book.getBook_id());
                    DyStatService.onEvent(EventPoint.READPAGE_REPAIRDEDIALOGUE, data2);
                }
            });
            TextView dialog_cancel = myDialog.findViewById(R.id.publish_stay);
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
                    RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                            BaseBookApplication.getGlobalContext()).updateBookFix(bookFix);
                    if (!isComfire) {
                        Map<String, String> data2 = new HashMap<>();
                        data2.put("type", "2");
                        data2.put("bookid", book.getBook_id());
                        DyStatService.onEvent(EventPoint.READPAGE_REPAIRDEDIALOGUE, data2);
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

    private static void fixBook(final Book book, final BookFix bookFix,
            final FixCallBack fixCallBack) {
        //1.删除缓存
        //2.删除目录
        //3.清除缓存队列中信息
        //4.全本缓存
        //5.更新书籍version
        //6.删除修复状态信息
        new Thread(new Runnable() {
            @Override
            public void run() {

                Book interimBook = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                        BaseBookApplication.getGlobalContext()).loadBook(book.getBook_id());

                if (interimBook != null) {

                    ChapterDaoHelper bookChapterDao =
                            ChapterDaoHelper.Companion.loadChapterDataProviderHelper(
                                    BaseBookApplication.getGlobalContext(),
                                    interimBook.getBook_id());
                    BaseBookHelper.removeChapterCacheFile(interimBook);
                    CacheManager.INSTANCE.remove(interimBook.getBook_id());
                    bookChapterDao.deleteAllChapters();

                    interimBook.setList_version(bookFix.getList_version());
                    interimBook.setC_version(bookFix.getC_version());

                    if (interimBook.getLast_chapter() != null) {
                        interimBook.getLast_chapter().setChapter_id("");
                    }

                    RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                            BaseBookApplication.getGlobalContext()).updateBook(interimBook);

                    CacheManager.INSTANCE.start(interimBook.getBook_id(), 0);

                    RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                            BaseBookApplication.getGlobalContext()).deleteBookFix(
                            interimBook.getBook_id());

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
        if ((RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).checkBookSubscribe(book_id) != null)) {
            BookFix bookFix = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                    BaseBookApplication.getGlobalContext()).loadBookFix(book_id);
            if (bookFix != null && !TextUtils.isEmpty(bookFix.getBook_id())) {
                if (bookFix.getFix_type() == 2) {
                    if (NetWorkUtils.isNetworkAvailable(context)) {
                        sp.putBoolean(book_id, true);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void fixBook(final Context context, final Book book,
            final FixCallBack fixCallBack) {
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
                Book interimBook = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                        BaseBookApplication.getGlobalContext()).checkBookSubscribe(
                        book.getBook_id());
                if (interimBook != null) {
                    BookFix bookFix =
                            RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                                    BaseBookApplication.getGlobalContext()).loadBookFix(
                                    interimBook.getBook_id());
                    if (bookFix != null && !TextUtils.isEmpty(bookFix.getBook_id())) {
                        if (bookFix.getFix_type() == 2) {
                            if (NetWorkUtils.isNetworkAvailable(context)) {
                                ChapterDaoHelper bookChapterDao =
                                        ChapterDaoHelper.Companion.loadChapterDataProviderHelper(
                                                BaseBookApplication.getGlobalContext(),
                                                interimBook.getBook_id());
                                BaseBookHelper.removeChapterCacheFile(interimBook);
                                CacheManager.INSTANCE.remove(interimBook.getBook_id());
                                bookChapterDao.deleteAllChapters();

                                interimBook.setList_version(bookFix.getList_version());
                                interimBook.setC_version(bookFix.getC_version());

                                if (interimBook.getLast_chapter() != null) {
                                    interimBook.getLast_chapter().setChapter_id("");
                                }

                                RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                                        BaseBookApplication.getGlobalContext()).updateBook(
                                        interimBook);
                                CacheManager.INSTANCE.start(interimBook.getBook_id(), 0);
                                RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                                        BaseBookApplication.getGlobalContext()).deleteBookFix(
                                        interimBook.getBook_id());
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
