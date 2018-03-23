package net.lzbook.kit.utils;

import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.UpdateCallBack;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookUpdateTaskData;
import net.lzbook.kit.data.bean.BookUpdateTaskData.UpdateTaskFrom;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.Chapter.Status;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.request.DataCache;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public abstract class BaseBookHelper {
    public static final int CHAPTER_CACHE_COUNT = 5;
    private static final String DOWN_INDEX = "down_index";
    private static final String DOWN_PROGRESS = "progress";
    private static final String DOWN_START = "start";
    private static WifiWarningDialog warningDialog;
    private static ChangeSourceDialog changeSourceDialog;
    static String TAG = "BaseBookHelper";

    public static RequestItem getRequestItem(Book iBook) {
        RequestItem requestItem = new RequestItem();
        requestItem.book_id = iBook.book_id;
        requestItem.book_source_id = iBook.book_source_id;
        requestItem.name = iBook.name;
        requestItem.author = iBook.author;
        requestItem.host = iBook.site;
        requestItem.parameter = iBook.parameter;
        requestItem.extra_parameter = iBook.extra_parameter;
        return requestItem;
    }

    public static void setChapterStatus(Chapter chapter) {
        if (chapter == null) {
            new Chapter().status = Status.CONTENT_ERROR;
        } else if (chapter.status != Status.CONTENT_NORMAL) {
        } else {
            if (TextUtils.isEmpty(chapter.content) || chapter.content.equals("null")) {
                chapter.status = Status.CONTENT_EMPTY;
            }
        }
    }

    public static boolean isChapterExist(Chapter chapter) {
        if (chapter == null) {
            return false;
        }
        return DataCache.isChapterExists(chapter);
    }

    public static int getQGCacheCount(String book_id) {
        String[] filelist = new File(com.quduquxie.Constants.APP_PATH_BOOK + book_id + "/").list();
        if (filelist != null) {
            return filelist.length;
        }
        return 0;
    }

    public static int getCacheCount(String book_id) {
        String[] filelist = new File(ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book_id + "/").list();
        if (filelist != null) {
            return filelist.length;
        }
        return 0;
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
        data.mCallBack = callBack;
        return data;
    }


    private static void removeQGChaptersCacheFile(String book_id) {
        FileUtils.deleteDir(new File(net.lzbook.kit.constants.Constants.SDCARD_PATH + "/quanben/book" + File.separator + book_id));
    }

    public static void removeChapterCacheFile(Book book) {
        if (Constants.QG_SOURCE.equals(book.site)) {
            removeQGChaptersCacheFile(book.book_id);
        } else {
            String newPath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book.book_id;
            FileUtils.deleteDir(new File(newPath));
        }
    }

    public static void startDownBookTask(final Context context, final Book book, final int startDownIndex) {

        if (!BookDaoHelper.getInstance().isBookSubed(book.book_id) && !BookDaoHelper.getInstance().insertBook(book)) {
            Toast.makeText(context, "书架已满", Toast.LENGTH_LONG).show();
            return;
        }

        DownloadState bookStatus = CacheManager.INSTANCE.getBookStatus(book);
        if (bookStatus == DownloadState.FINISH) {
            Toast.makeText(context, "书籍已缓存", Toast.LENGTH_LONG).show();
        } else if (bookStatus == DownloadState.DOWNLOADING || bookStatus == DownloadState.WAITTING) {
            Toast.makeText(context, "拼命缓存中...", Toast.LENGTH_LONG).show();
            if (!CacheManager.INSTANCE.getBookTask(book).isFullCache) {
                CacheManager.INSTANCE.stop(book.book_id);
                CacheManager.INSTANCE.start(book.book_id, startDownIndex);
            }
        } else if (NetWorkUtils.getNetWorkType(context) != NetWorkUtils.NETWORK_MOBILE || (net.lzbook.kit.constants.Constants.isDownloadManagerActivity && net.lzbook.kit.constants.Constants.hadShownMobilNetworkConfirm)) {
            startDownload(context, book, startDownIndex);
        } else {
//            final MyDialog myDialog = new MyDialog((Activity) context, R.layout.publish_hint_dialog);
//            myDialog.setCanceledOnTouchOutside(false);
//            myDialog.setCancelable(false);
//            Button btn_cancle_clear_cache = (Button) myDialog.findViewById(R.id.publish_stay);
//            Button btn_confirm_clear_cache = (Button) myDialog.findViewById(R.id.publish_leave);
//            TextView publish_content = (TextView) myDialog.findViewById(R.id.publish_content);
//            ((TextView) myDialog.findViewById(R.id.dialog_title)).setText(R.string.prompt);
//            publish_content.setText(R.string.tip_network_mobile);
//            btn_cancle_clear_cache.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    myDialog.dismiss();
//                }
//            });
//            btn_confirm_clear_cache.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    myDialog.dismiss();
//                    if (net.lzbook.kit.constants.Constants.isDownloadManagerActivity) {
//                        net.lzbook.kit.constants.Constants.hadShownMobilNetworkConfirm = true;
//                    }
//                    BaseBookHelper.startDownload(context, book, startDownIndex);
//                }
//            });
//            myDialog.show();
            if (warningDialog == null) {
                warningDialog = new WifiWarningDialog((Activity) context);
                warningDialog.setOnConfirmListener(new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        if (net.lzbook.kit.constants.Constants.isDownloadManagerActivity) {
                            net.lzbook.kit.constants.Constants.hadShownMobilNetworkConfirm = true;
                        }
                        BaseBookHelper.startDownload(context, book, startDownIndex);
                        return null;
                    }
                });
            }
            warningDialog.show();
        }
    }


    private static void startDownload(final Context context, final Book book, final int startDownIndex) {
        if (CacheManager.INSTANCE.hasOtherSourceStatus(book)) {
//            final MyDialog myDialog = new MyDialog((Activity) context, R.layout.publish_hint_dialog);
//            myDialog.setCanceledOnTouchOutside(false);
//            myDialog.setCancelable(false);
//            Button btn_cancle_clear_cache = (Button) myDialog.findViewById(R.id.publish_stay);
//            Button btn_confirm_clear_cache = (Button) myDialog.findViewById(R.id.publish_leave);
//            final TextView publish_content = (TextView) myDialog.findViewById(R.id.publish_content);
//            final TextView dialog_title = (TextView) myDialog.findViewById(R.id.dialog_title);
//            publish_content.setText(R.string.tip_clear_other_source_cache);
//            btn_cancle_clear_cache.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    myDialog.dismiss();
//                }
//            });
//            btn_confirm_clear_cache.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    publish_content.setVisibility(View.GONE);
//                    dialog_title.setText(R.string.tip_cleaning_cache);
//                    myDialog.findViewById(R.id.change_source_bottom).setVisibility(View.GONE);
//                    myDialog.findViewById(R.id.progress_del).setVisibility(View.VISIBLE);
//                    new Thread() {
//                        public void run() {
//                            super.run();
//                            context.getSharedPreferences(BaseBookHelper.DOWN_INDEX + book.book_id, 0).edit().clear().apply();
//                            DataCache.deleteOtherSourceCache(book);
//                            ExtensionsKt.msMainLooperHandler.post(new Runnable() {
//                                public void run() {
//                                    myDialog.dismiss();
//                                    if (!CacheManager.INSTANCE.start(book.book_id, startDownIndex)) {
//                                        Toast.makeText(context, "启动缓存服务失败", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            });
//                        }
//                    }.start();
//                }
//            });
//            myDialog.show();
            if (changeSourceDialog == null) {
                changeSourceDialog = new ChangeSourceDialog((Activity) context);
                changeSourceDialog.setOnConfirmListener(new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        changeSourceDialog.showLoading();
                        context.getSharedPreferences(BaseBookHelper.DOWN_INDEX + book.book_id, 0).edit().clear().apply();
                            DataCache.deleteOtherSourceCache(book);
                            ExtensionsKt.msMainLooperHandler.post(new Runnable() {
                                public void run() {
                                    changeSourceDialog.dismiss();
                                    if (!CacheManager.INSTANCE.start(book.book_id, startDownIndex)) {
                                        Toast.makeText(context, "启动缓存服务失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        return null;
                    }
                });
            }
            changeSourceDialog.show();
        } else if (!CacheManager.INSTANCE.start(book.book_id, startDownIndex)) {
            Toast.makeText(context, "启动缓存服务失败", Toast.LENGTH_SHORT).show();
        }
    }
}
