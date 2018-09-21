package net.lzbook.kit.utils.book;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.ding.basic.bean.Book;
import com.ding.basic.repository.RequestRepositoryFactory;

import net.lzbook.kit.R;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.base.BaseBookApplication;
import net.lzbook.kit.data.db.help.ChapterDaoHelper;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.download.CacheManager;
import net.lzbook.kit.utils.toast.ToastUtil;
import net.lzbook.kit.widget.MyDialog;

import java.util.HashMap;
import java.util.Map;

/**
 * 书籍修复
 * Created by yuchao on 2017/11/1 0001.
 */
public class RepairHelp {

    public static void showFixMsg(Activity activity, Book book, FixCallBack fixCallBack) {
        Book book1 = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).checkBookSubscribe(book.getBook_id());
        if (book1 != null && book1.waitingCataFix()) {
            if (NetWorkUtils.isNetworkAvailable(activity)) {
                showFixHintDialog(activity, book1, fixCallBack);
            }
        }

    }


    private static boolean isComfire = false;

    private static void showFixHintDialog(final Activity activity, final Book book, final FixCallBack fixCallBack) {
        if (activity != null && !activity.isFinishing()) {
            isComfire = false;
            final MyDialog myDialog = new MyDialog(activity, R.layout.fixbook_hint_dialog);
            myDialog.setCanceledOnTouchOutside(true);
            TextView dialog_confirm = myDialog.findViewById(R.id.publish_leave);
            dialog_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isComfire = true;
                    myDialog.dismiss();
                    if (NetWorkUtils.isNetworkAvailable(activity)) {
                        fixBook(book, fixCallBack);
                    } else {
                        ToastUtil.INSTANCE.showToastMessage("网络不给力，请检查网络连接");
                    }
                    Map<String, String> data2 = new HashMap<>();
                    data2.put("type", "1");
                    data2.put("bookid",book.getBook_id());
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE,
                            StartLogClickUtil.REPAIRDEDIALOGUE, data2);
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
                    if (!isComfire) {
                        Map<String, String> data2 = new HashMap<>();
                        data2.put("type", "2");
                        data2.put("bookid",book.getBook_id());
                        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE,
                                StartLogClickUtil.REPAIRDEDIALOGUE, data2);
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

    private static void fixBook(final Book book, final FixCallBack fixCallBack) {
        //1.删除缓存
        //2.删除目录
        //3.清除缓存队列中信息
        //4.全本缓存
        //5.更新书籍version
        //6.删除修复状态信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChapterDaoHelper bookChapterDao =
                        ChapterDaoHelper.Companion.loadChapterDataProviderHelper(
                                BaseBookApplication.getGlobalContext(), book.getBook_id());
                BaseBookHelper.removeChapterCacheFile(book);
                CacheManager.INSTANCE.remove(book.getBook_id());
                bookChapterDao.deleteAllChapters();

                if (book.waitingCataFix()) {
                    book.setList_version(book.getList_version_fix());
                    book.setForce_fix(0);
                }

                if (book.getLast_chapter() != null) {
                    book.getLast_chapter().setChapter_id("");
                }

                RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                        BaseBookApplication.getGlobalContext()).updateBook(book);

                CacheManager.INSTANCE.start(book.getBook_id(), 0);

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
        Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book_id);
        if (book != null && book.waitingCataFix()) {
            if (NetWorkUtils.isNetworkAvailable(context)) {
                return true;
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
                Book book1 = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                        BaseBookApplication.getGlobalContext()).checkBookSubscribe(
                        book.getBook_id());
                if (book1 != null && book1.waitingCataFix()) {
                    if (NetWorkUtils.isNetworkAvailable(context)) {
                        ChapterDaoHelper bookChapterDao =
                                ChapterDaoHelper.Companion.loadChapterDataProviderHelper(
                                        BaseBookApplication.getGlobalContext(),
                                        book1.getBook_id());
                        BaseBookHelper.removeChapterCacheFile(book1);
                        CacheManager.INSTANCE.remove(book1.getBook_id());
                        bookChapterDao.deleteAllChapters();

                        if (book1.waitingCataFix()) {
                            book1.setList_version(book1.getList_version_fix());
                            book1.setForce_fix(0);
                        }

                        if (book1.getLast_chapter() != null) {
                            book1.getLast_chapter().setChapter_id("");
                        }

                        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                                BaseBookApplication.getGlobalContext()).updateBook(book1);
                        CacheManager.INSTANCE.start(book1.getBook_id(), 0);
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
                                ToastUtil.INSTANCE.showToastMessage("网络不给力，请检查网络连接");
                            }
                        });
                    }
                }
            }
        }).start();


    }

    public interface FixCallBack {
        void toDownLoadActivity();
    }
}
