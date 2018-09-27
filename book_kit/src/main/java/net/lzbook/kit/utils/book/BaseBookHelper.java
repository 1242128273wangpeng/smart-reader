package net.lzbook.kit.utils.book;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.Chapter;
import com.ding.basic.bean.ChapterState;
import com.ding.basic.RequestRepositoryFactory;
import com.ding.basic.util.DataCache;

import net.lzbook.kit.R;
import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.utils.ExtensionsKt;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.download.CacheManager;
import net.lzbook.kit.utils.download.DownloadState;
import net.lzbook.kit.utils.file.FileUtils;
import net.lzbook.kit.ui.widget.MyDialog;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.bean.UpdateCallBack;
import net.lzbook.kit.bean.BookUpdateTaskData;
import net.lzbook.kit.bean.BookUpdateTaskData.UpdateTaskFrom;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public abstract class BaseBookHelper {
    public static final int CHAPTER_CACHE_COUNT = 5;
    private static final String DOWN_INDEX = "down_index";
    private static final String DOWN_PROGRESS = "progress";
    private static final String DOWN_START = "start";
    static String TAG = "BaseBookHelper";

    public static void setChapterStatus(Chapter chapter) {
        if (chapter == null) {
        } else if (chapter.getStatus() != ChapterState.CONTENT_NORMAL) {
        } else {
            if (TextUtils.isEmpty(chapter.getContent()) || chapter.getContent().equals("null")) {
                chapter.setStatus(ChapterState.CONTENT_EMPTY);
            }
        }
    }

    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (File delete : childFiles) {
                delete(delete);
            }
            file.delete();
        }
    }

    public static BookUpdateTaskData getBookUpdateTaskData(ArrayList<Book> list, UpdateCallBack callBack) {
        BookUpdateTaskData data = new BookUpdateTaskData();
        data.books = list;
        data.from = UpdateTaskFrom.FROM_BOOK_SHELF;
        data.mCallBack = new WeakReference<UpdateCallBack>(callBack);
        return data;
    }


    private static void removeQGChaptersCacheFile(String book_id) {
        FileUtils.deleteDir(new File(net.lzbook.kit.constants.Constants.SDCARD_PATH + "/quanben/book" + File.separator + book_id));
    }

    public static void removeChapterCacheFile(Book book) {
        String newPath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book.getBook_id();
        FileUtils.deleteDir(new File(newPath));

        if (book.fromQingoo()) {
            removeQGChaptersCacheFile(book.getBook_id());
        }
    }

    public static void startDownBookTask(final Context context, final Book book, final int startDownIndex) {

        //TODO .提供数据库查询大小的方法
        if (RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book.getBook_id()) == null) {
            if (RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(book) <= 0) {
                return;
            }
        }

        DownloadState bookStatus = CacheManager.INSTANCE.getBookStatus(book);
        if (bookStatus == DownloadState.FINISH) {
            Toast.makeText(context, "书籍已缓存", Toast.LENGTH_LONG).show();
        } else if (bookStatus == DownloadState.DOWNLOADING || bookStatus == DownloadState.WAITTING) {
            Toast.makeText(context, "拼命缓存中...", Toast.LENGTH_LONG).show();
            if (!CacheManager.INSTANCE.getBookTask(book).isFullCache) {
                CacheManager.INSTANCE.stop(book.getBook_id());
                CacheManager.INSTANCE.start(book.getBook_id(), startDownIndex);
            }
        } else if (NetWorkUtils.getNetWorkType(context) != NetWorkUtils.NETWORK_MOBILE || (net.lzbook.kit.constants.Constants.isDownloadManagerActivity && net.lzbook.kit.constants.Constants.hadShownMobilNetworkConfirm)) {
            startDownload(context, book, startDownIndex);
        } else {
            final MyDialog myDialog = new MyDialog((Activity) context, R.layout.publish_hint_dialog);
            myDialog.setCanceledOnTouchOutside(false);
            myDialog.setCancelable(false);
            Button btn_cancle_clear_cache = (Button) myDialog.findViewById(R.id.publish_stay);
            Button btn_confirm_clear_cache = (Button) myDialog.findViewById(R.id.publish_leave);
            TextView publish_content = (TextView) myDialog.findViewById(R.id.publish_content);
            ((TextView) myDialog.findViewById(R.id.dialog_title)).setText(R.string.prompt);
            publish_content.setText(R.string.tip_network_mobile);
            btn_cancle_clear_cache.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    myDialog.dismiss();
                }
            });
            btn_confirm_clear_cache.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    myDialog.dismiss();
                    if (net.lzbook.kit.constants.Constants.isDownloadManagerActivity) {
                        net.lzbook.kit.constants.Constants.hadShownMobilNetworkConfirm = true;
                    }
                    BaseBookHelper.startDownload(context, book, startDownIndex);
                }
            });
            myDialog.show();
        }
    }

    private static void startDownload(final Context context, final Book book, final int startDownIndex) {
        if (CacheManager.INSTANCE.hasOtherSourceStatus(book)) {
            final MyDialog myDialog = new MyDialog((Activity) context, R.layout.publish_hint_dialog);
            myDialog.setCanceledOnTouchOutside(false);
            myDialog.setCancelable(false);
            Button btn_cancle_clear_cache = (Button) myDialog.findViewById(R.id.publish_stay);
            Button btn_confirm_clear_cache = (Button) myDialog.findViewById(R.id.publish_leave);
            final TextView publish_content = (TextView) myDialog.findViewById(R.id.publish_content);
            final TextView dialog_title = (TextView) myDialog.findViewById(R.id.dialog_title);
            publish_content.setText(R.string.tip_clear_other_source_cache);
            btn_cancle_clear_cache.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    myDialog.dismiss();
                }
            });
            btn_confirm_clear_cache.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    publish_content.setVisibility(View.GONE);
                    dialog_title.setText(R.string.tip_cleaning_cache);
                    myDialog.findViewById(R.id.change_source_bottom).setVisibility(View.GONE);
                    myDialog.findViewById(R.id.progress_del).setVisibility(View.VISIBLE);
                    new Thread() {
                        public void run() {
                            super.run();
                            context.getSharedPreferences(BaseBookHelper.DOWN_INDEX + book.getBook_id(), 0).edit().clear().apply();
                            DataCache.deleteOtherSourceCache(book);
                            ExtensionsKt.msMainLooperHandler.post(new Runnable() {
                                public void run() {
                                    myDialog.dismiss();
                                    if (!CacheManager.INSTANCE.start(book.getBook_id(), startDownIndex)) {
                                        Toast.makeText(context, "启动缓存服务失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }.start();
                }
            });
            myDialog.show();
        } else if (!CacheManager.INSTANCE.start(book.getBook_id(), startDownIndex)) {
            Toast.makeText(context, "启动缓存服务失败", Toast.LENGTH_SHORT).show();
        }
    }
}
