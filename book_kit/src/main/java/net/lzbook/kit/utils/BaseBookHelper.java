package net.lzbook.kit.utils;

import net.lzbook.kit.R;
import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.book.download.CallBackDownload;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.UpdateCallBack;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookTask;
import net.lzbook.kit.data.bean.BookUpdateTaskData;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.RequestItem;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public abstract class BaseBookHelper {

    public static final int CHAPTER_CACHE_COUNT = 5;
    private static final String DOWN_INDEX = "down_index";

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


    public static void reStartDownloadService() {
        try {
            Context context = BaseBookApplication.getGlobalContext().getApplicationContext();
            Intent intent = new Intent();
            intent.setClass(context, DownloadService.class);
            context.startService(intent);
            context.bindService(intent, sc, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ServiceConnection sc = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService downloadService = ((DownloadService.MyBinder) service).getService();
            BaseBookApplication.setDownloadService(downloadService);
        }
    };

    /**
     * 判断章节状态
     * <p/>
     * chapter
     */
    public static void setChapterStatus(Chapter chapter) {
        if (chapter == null) {
            chapter = new Chapter();
            chapter.status = Chapter.Status.CONTENT_ERROR;
        } else if (chapter.status == Chapter.Status.CONTENT_NORMAL) {
            if (TextUtils.isEmpty(chapter.content) || chapter.content.equals("null")) {
                chapter.status = Chapter.Status.CONTENT_EMPTY;
            }
        }
    }


    /**
     * 判断gid的书下sequence章节是否在本地存在
     * <p/>
     * sequence
     * gid
     */

    public static boolean isChapterExist(int sequence, String book_id) {
        // FIXME ??
        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book_id + "/" + sequence + ".text";
        File file = new File(filePath);
        if (file != null && file.exists()) {
            return true;
        }
        return false;
    }

    public static int getQGCacheCount(String book_id) {
        String filePath = com.quduquxie.Constants.APP_PATH_BOOK + book_id + "/";
        String[] filelist = new File(filePath).list();
        if (filelist != null) {
            return filelist.length;
        } else {
            return 0;
        }
    }

    public static int getCacheCount(String book_id) {
        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book_id + "/";
        String[] filelist = new File(filePath).list();
        if (filelist != null) {
            return filelist.length;
        } else {
            return 0;
        }
    }

    /*
     * 获取本次下载的文件数目
     */
//    public static int getCacheCount(String book_id, int fromIndex, int end) {
//        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book_id + "/";
//        int cacheCount = 0;
//        String[] filelist = new File(filePath).list();
////        if (filelist != null) {
////            for (String filename : filelist) {
////                int name = 0;
////                try {
////                    name = Integer.parseInt(filename.substring(0, filename.lastIndexOf(".")));
////                } catch (Exception e) {
////                    continue;
////                }
////                if (name >= fromIndex && name < end) {
////                    cacheCount++;
////                }
////            }
////        }
//        if (filelist != null) {
//            cacheCount = filelist.length;
//        }else{
//            filelist = new File(com.quduquxie.Constants.APP_PATH_BOOK + book_id + "/").list();
//            if (filelist != null) {
//                cacheCount = filelist.length;
//            }
//        }
//        cacheCount -= fromIndex;
//        return cacheCount >= 0 ? cacheCount : 0;
//    }

    public static int getStartDownIndex(Context ctt, Book book) {
        SharedPreferences prefer = ctt.getSharedPreferences(DOWN_INDEX, Context.MODE_PRIVATE);
        return prefer.getInt(book.book_id, -1);
    }

    public static void writeDownIndex(Context ctt, String book_id, boolean fromMark, int downIndex) {
        AppLog.d(TAG, "writeDownIndex -->  downIndex = " + downIndex);
        SharedPreferences prefer = ctt.getSharedPreferences(DOWN_INDEX, Context.MODE_PRIVATE);
        if (downIndex < 0) {
            downIndex = 0;
        }
        prefer.edit().putInt(book_id, downIndex).apply();

    }

    /**
     * 删除gid为此的缓存章节
     * <p>
     * gid
     */
    public static void deleteAllChapterCache(String gid, int seq, int cout) {
        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + gid + "/";
        for (int i = seq; i < cout; i++) {
            File file = new File(filePath + i + ".text");
            if (file.exists()) {
                try {
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void deleteAllChapterCacheNew(Context context, String bookId) {
        //重置缓存任务的状态
        DownloadService.clearTask(bookId);
        BaseBookHelper.delDownIndex(context, bookId);
        //删除缓存下来的文件
        final String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + bookId;
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(filePath);
                File file1 = new File(filePath + ".delete");
                boolean bool = file.renameTo(file1);
                File deleteFile = null;
                if (bool) {
                    deleteFile = file1;
                } else {
                    deleteFile = file;
                }
                delete(deleteFile);
            }
        }).start();
    }

    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    /**
     * 获取更新任务所需结构
     * <p>
     * books
     * callBack
     */
    public static BookUpdateTaskData getBookUpdateTaskData(ArrayList<Book> list, UpdateCallBack callBack) {
        BookUpdateTaskData data = new BookUpdateTaskData();
        data.books = list;
        data.from = BookUpdateTaskData.UpdateTaskFrom.FROM_BOOK_SHELF;
        data.mCallBack = callBack;
        return data;

    }

    /**
     * 获取离线下载单本书籍结构
     * <p>
     * books
     */
    public static BookTask getBookTask(Context context, Book book, DownloadState state, CallBackDownload callBack, boolean fromStart) {
        // 从0开始
        int endSequence = book.chapter_count;
        int startSequence = 0;
        if (!fromStart) {
            startSequence = book.sequence > -1 ? book.sequence : 0;
        }
        BookTask data = new BookTask(book, state, startSequence, endSequence, callBack);
        return data;
    }

    /*
     * 获取下载初始状态
     */
    public static DownloadState getInitDownstate(Context context, Book book, int count) {

        DownloadService downloadService = BaseBookApplication.getDownloadService();
        if (downloadService != null) {
            BookTask bookTask = downloadService.getDownBookTask(book.book_id);
            if(bookTask != null) {
                return bookTask.state;
            }
        }

        if (count > -1) {
            // 判断是否下载成功
            if (count == book.chapter_count) {
                return DownloadState.FINISH;
            }
            return DownloadState.PAUSEED;
        } else {
            return DownloadState.NOSTART;
        }
    }

    /*
     * 获取已经下载的章节数
     */
//    public static int getDownCount(Context context, Book book) {
//        int index = getStartDownIndex(context, book.book_id);
//        if (index > -1) {
//            int count = 0;
//            if (Constants.QG_SOURCE.equals(book.site)) {
//                count = getQGCacheCount(book.book_id);
//            } else {
//                count = getCacheCount(book.book_id);
//            }
//            if (count < index){
//                //缓存已经无效了
//                index = -1;
//            }
//            return index;
////            int count = getCacheCount(book.book_id, index, book.chapter_count);
//        } else {
//            return -1;
//        }
//    }

//    public static int getDownCount(Context context, Book book, int index) {
//        if (index > -1) {
//            int count;
//            if (Constants.QG_SOURCE.equals(book.site)) {
//                count = getQGCacheCount(book.book_id);
//                return count;
//            } else {
//                count = getCacheCount(book.book_id, index, book.chapter_count);
//                return (count + index);
//            }
////            int count = getCacheCount(book.book_id, index, book.chapter_count);
//        } else {
//            return -1;
//        }
//    }

    /**
     * 下载单本书
     * <p>
     * context
     * book
     * callBack
     */
    public static void addDownBookTask(Context context, Book book, CallBackDownload callBack, boolean fromStartIndex) {
        DownloadService downloadService = BaseBookApplication.getDownloadService();
        if (downloadService != null) {
            int firstDownIndex = getStartDownIndex(context, book);
            DownloadState state = getInitDownstate(context, book, firstDownIndex);
            BookTask bookTask = getBookTask(context, book, state, callBack, fromStartIndex);
            String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_DOWNLOAD + book.book_id;
            if (firstDownIndex > 0) {
                bookTask.startSequence = firstDownIndex;
            }
            if (FileUtils.fileIsExist(filePath)) {
                File file = new File(filePath);
                Properties properties = new Properties();
                BufferedInputStream bis = null;
                try {
                    bis = new BufferedInputStream(new FileInputStream(file));
                    properties.load(bis);
                    int index = Integer.parseInt(properties.getProperty("index", "0"));
                    int fileLength = Integer.parseInt(properties.getProperty("length", "0"));
                    bookTask.progress = (index == 0 || fileLength == 0) ? 0 : (index * 100) / fileLength;

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            downloadService.addTask(bookTask);
            downloadService.addRequestItem(book);
        } else {
            Toast.makeText(context, "启动缓存服务失败", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 获取当前书籍的下载状态
     * <p>
     * context
     * book
     */
    public static DownloadState getDownloadState(Context context, Book book) {
        DownloadState state;
        DownloadService downloadService = BaseBookApplication.getDownloadService();
        if (downloadService != null) {
            BookTask bookTask = downloadService.getDownBookTask(book.book_id);
            if (bookTask != null) {
                state = bookTask.state;
            } else {
                int count = getStartDownIndex(context, book);
                state = getInitDownstate(context, book, count);
            }
        } else {
            int count = getStartDownIndex(context, book);
            state = getInitDownstate(context, book, count);
        }

        return state;
    }

    /*
     * 启动book下载任务
     */
    public static void startDownBookTask(Context context, String book_id) {
        if (NetWorkUtils.getNetWorkType(context) == NetWorkUtils.NETWORK_NONE) {
            Toast.makeText(context, context.getText(R.string.game_network_none), Toast.LENGTH_LONG).show();
            return;
        }
        DownloadService downloadService = BaseBookApplication.getDownloadService();
        if (downloadService != null) {
            downloadService.startTask(book_id);
        } else {
            Toast.makeText(context, "启动缓存服务失败", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * 启动book下载任务
     */
    public static BookTask getDownBookTask(Context context, String book_id) {
        DownloadService downloadService = BaseBookApplication.getDownloadService();
        if (downloadService != null) {
            return downloadService.getDownBookTask(book_id);
        } else {
            Toast.makeText(context, "启动缓存服务失败", Toast.LENGTH_SHORT).show();
        }
        return null;
    }


    /*********************
     * 重构的方法
     **********************/

    public static void delDownIndex(Context ctt, int gid) {
        SharedPreferences prefer = ctt.getSharedPreferences(DOWN_INDEX, Context.MODE_PRIVATE);
        prefer.edit().remove(String.valueOf(gid)).apply();
    }

    public static void delDownIndex(Context ctt, String book_id) {
        SharedPreferences prefer = ctt.getSharedPreferences(DOWN_INDEX, Context.MODE_PRIVATE);
        prefer.edit().remove(book_id).apply();
    }

    public static boolean removeChapterCacheFile(int gid) {
        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + gid;
        String newFilePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + gid + ".delete";
        File downloadFile = new File(filePath);
        int count = 0;
        if (downloadFile != null && downloadFile.exists()) {
            File newFile = new File(newFilePath);
            try {
                if (downloadFile.listFiles() != null && downloadFile.listFiles().length > 0) {// 灰度修改非空判断
                    File file = downloadFile.listFiles()[0];
                    if (file != null) {
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.close();
                    }
                }
                while (true) {
                    if (newFile.exists()) {
                        newFile = new File(ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + gid + "." + count++ + ".delete");
                    } else {
                        break;
                    }
                }
                // 重命名不成功 执行删除
                if (!downloadFile.renameTo(newFile)) {
                    if (!downloadFile.delete()) {
                        AppLog.e(TAG, newFile + " delete failure");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                AppLog.e(TAG, newFile + " delete failure");
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }
        return false;
    }

    public static boolean removeQGChaptersCacheFile(String book_id) {
        String filePath = Constants.SDCARD_PATH + "/quanben/book" + File.separator + book_id;
        String newFilePath = Constants.SDCARD_PATH + "/quanben/book" + File.separator + book_id + ".delete";
        File downloadFile = new File(filePath);
        int count = 0;
        if (downloadFile != null && downloadFile.exists()) {
            File newFile = new File(newFilePath);
            try {
                if (downloadFile.listFiles() != null && downloadFile.listFiles().length > 0) {// 灰度修改非空判断
                    File file = downloadFile.listFiles()[0];
                    if (file != null) {
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.close();
                    }
                }
                while (true) {
                    if (newFile.exists()) {
                        newFile = new File(Constants.SDCARD_PATH + "/quanben/book" + File.separator + book_id + "." + count++ + ".delete");
                    } else {
                        break;
                    }
                }
                // 重命名不成功 执行删除
                if (!downloadFile.renameTo(newFile)) {
                    if (!downloadFile.delete()) {
                        AppLog.e(TAG, newFile + " delete failure");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                AppLog.e(TAG, newFile + " delete failure");
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }
        return false;
    }

    public static boolean removeChapterCacheFile(String book_id) {
        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book_id;
        String newFilePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book_id + ".delete";
        File downloadFile = new File(filePath);
        int count = 0;
        if (downloadFile != null && downloadFile.exists()) {
            File newFile = new File(newFilePath);
            try {
                if (downloadFile.listFiles() != null && downloadFile.listFiles().length > 0) {// 灰度修改非空判断
                    File file = downloadFile.listFiles()[0];
                    if (file != null) {
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.close();
                    }
                }
                while (true) {
                    if (newFile.exists()) {
                        newFile = new File(ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book_id + "." + count++ + ".delete");
                    } else {
                        break;
                    }
                }
                // 重命名不成功 执行删除
                if (!downloadFile.renameTo(newFile)) {
                    if (!downloadFile.delete()) {
                        AppLog.e(TAG, newFile + " delete failure");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                AppLog.e(TAG, newFile + " delete failure");
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }
        return false;
    }

    /*
     * 启动book下载任务
     */
    public static void startDownBookTask(Context context, String book_id, int startDownIndex) {
        if (NetWorkUtils.getNetWorkType(context) == NetWorkUtils.NETWORK_NONE) {
            Toast.makeText(context, context.getText(R.string.game_network_none), Toast.LENGTH_LONG).show();
            return;
        }
        DownloadService downloadService = BaseBookApplication.getDownloadService();
        if (downloadService != null) {
            downloadService.startTask(book_id, startDownIndex);
        } else {
            Toast.makeText(context, "启动缓存服务失败", Toast.LENGTH_SHORT).show();
        }
    }
}
