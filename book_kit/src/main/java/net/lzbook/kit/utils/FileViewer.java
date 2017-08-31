package net.lzbook.kit.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.db.BookDaoHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileViewer {
    static String TAG = "FileViewer";
    // ==================================================================
    // fields
    // ====================================================================
    final long maxSize = 100 * 1024 * 1024; // TODO 100MB
    String rootPath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK;
    private List<String> delFailPath = new ArrayList<>();// 删除失败的目录
    private List<String> delSuccessPath = new ArrayList<>();// 删除失败的目录

    public final static String FILE_EXTENSION_SEPARATOR = ".";
    public final static String FILE_EXTENSION_SUFFIX = "delete";
    Context context;
    Handler mHandler = null;

    FileDeleteFailedCallback deleteFailedCallback;
    FileDeleteSuccessCallback deleteSuccessCallback;

    public FileViewer(Context context) {
        this.context = context;
    }

    /**
     * 删除
     */
    public void doDelete() {
        HandlerThread thread = new HandlerThread("doDelete");
        // thread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        AppLog.d(TAG, "doDeleteThread.start " + thread.getName());
        thread.start();

        if (mHandler != null) {
            mHandler.post(deleteFileTask);
        } else {
            mHandler = new Handler(thread.getLooper());
            mHandler.post(deleteFileTask);
        }
    }

    public void setDeleteFailedCallback(FileDeleteFailedCallback callback) {
        this.deleteFailedCallback = callback;
    }

    public void removeDeleteCall() {
        if (mHandler != null && null != deleteFileTask) {
            mHandler.removeCallbacks(deleteFileTask);
        }
    }

    public interface FileDeleteFailedCallback {
        public void getDeleteFailedPath(ArrayList<String> pathList);

    }

    public interface FileDeleteSuccessCallback {
        public void getDeleteSuccessPath(ArrayList<String> pathList);
    }

    Runnable deleteFileTask = new Runnable() {

        @Override
        public void run() {
            String failedPath = null;
            String successPath = null;
            ArrayList<String> faileds = new ArrayList<>();
            faileds.addAll(delFailPath);
            ArrayList<String> successs = new ArrayList<>();
            successs.addAll(delSuccessPath);
            AppLog.d(TAG, "deleteFileTask " + deleteFileTask.hashCode());
            doDeleteFile(rootPath);// 删除目录文件

            if (faileds.size() > 0) {
                for (String path : faileds) {
                    doDeleteFile(path);
                    failedPath = path;
                    AppLog.d(TAG, "delete failed file path" + failedPath);
                }
            }
            if (deleteFailedCallback != null && faileds.size() > 0) {
                deleteFailedCallback.getDeleteFailedPath(faileds);
            }

            if (successs.size() > 0) {
                for (String path : successs) {
                    successPath = path;
                    AppLog.d(TAG, "delete success file path" + successPath);
                }
            }
            if (deleteSuccessCallback != null && successs.size() > 0) {
                deleteSuccessCallback.getDeleteSuccessPath(successs);
            }

        }
    };

    private void doDeleteFile(String root) {
        File f = new File(root);// 一级目录
        if (f != null && f.exists()) {

            File[] files = f.listFiles(); // 子目录文件
            if (files != null) {

                for (int i = 0; i < files.length; i++) {
                    String singlePath = files[i].getAbsolutePath();
                    AppLog.d(TAG, " 删除时，检查的路径 " + files[i].getAbsolutePath());
                    if (singlePath != null && isContented(singlePath)) {
                        AppLog.d(TAG, " 删除的路径 " + files[i].getAbsolutePath());
                        deleteFiles(files[i]);// 执行删除
                    }
                }
            }
        }
    }

    protected boolean isContented(String singlePath) { // 最终路径是否满足条件
        if (getFileExtension(singlePath).equals(FILE_EXTENSION_SUFFIX)) {
            return true;
        }
        return false;
    }


    protected Book getBook(String gid) {
        Book isbook = (Book) BookDaoHelper.getInstance(context).getBook(gid, 0);
        return isbook;
    }


    /**
     * 递归删除文件
     * <p/>
     * singlePath
     */
    private void deleteFiles(File singleFile) {
        String path = null;

        if (singleFile == null || !singleFile.exists()) {
            return;
        }

//		if (!singleFile.isDirectory()) {//FIXME nullpointer
//			return;
//		}
        if (singleFile.listFiles() == null) {
            return;
        }
        try {
            for (File f : singleFile.listFiles()) {
                if (f != null && f.isFile()) {
                    path = f.getAbsolutePath();
                    if (path == null) {
                        return;
                    }
                    if (f.delete()) {
                        delSuccessPath.add(path);
                    } else {

                        delFailPath.add(path);
                        AppLog.e(TAG, "file.delete() failed ");
                    }
                } else if (f != null && f.isDirectory()) {
                    deleteFiles(f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        path = singleFile.getAbsolutePath();
        if (path == null) {
            return;
        }
        if (singleFile.delete()) {
            delSuccessPath.add(path);

        } else {
            delFailPath.add(path);
            AppLog.e(TAG, "file.delete() failed ");

        }
    }

    /**
     * get file name extension from path, not include suffix
     * <p/>
     * filePath
     *
     * @return
     */
    public static String getFileExtension(String filePath) {
        if (isBlank(filePath)) {
            return filePath;
        }

        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (extenPosi == -1) {
            return "";
        }
        return (filePosi >= extenPosi) ? "" : filePath.substring(extenPosi + 1);
    }

    public static boolean isBlank(String str) {
        return (str == null || str.trim().length() == 0);
    }
}
