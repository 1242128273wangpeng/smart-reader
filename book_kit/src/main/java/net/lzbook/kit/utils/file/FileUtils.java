package net.lzbook.kit.utils.file;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.StatFs;
import android.text.TextUtils;

import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.utils.logger.AppLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Desc   File操作工具
 * Author yangweining
 * Mail   weining_yang@dingyuegroup.cn
 * Date   2018/9/21 16:25
 */
public class FileUtils {

    private static final String TAG = "FileUtils";
    private static final int BUFFER_SIZE = 8192;


    public final static String FILE_EXTENSION_SEPARATOR = ".";
    public final static String FILE_EXTENSION_SUFFIX = "delete";
    private static Handler mHandler = null;
    private static FileDeleteFailedCallback deleteFailedCallback;
    private static FileDeleteSuccessCallback deleteSuccessCallback;
    private static List<String> delFailPath = new ArrayList<>();// 删除失败的目录
    private static List<String> delSuccessPath = new ArrayList<>();// 删除失败的目录
    private static HandlerThread thread = new HandlerThread("doDelete");
    private static Runnable deleteFileTask = new Runnable() {

        @Override
        public void run() {
            String failedPath = null;
            String successPath = null;
            AppLog.d(TAG, "deleteFileTask " + deleteFileTask.hashCode());
            doDeleteFile(ReplaceConstants.getReplaceConstants().APP_PATH_BOOK);// 删除目录文件
            ArrayList<String> faileds = new ArrayList<>();
            faileds.addAll(delFailPath);
            delFailPath.clear();
            ArrayList<String> successs = new ArrayList<>();
            successs.addAll(delSuccessPath);
            delSuccessPath.clear();
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

    /**
     * 判断文件是否存在
     */
    public static boolean fileIsExist(String filePath) {
        if (filePath == null || filePath.length() < 1) {
            return false;
        }

        File f = new File(filePath);
        if (!f.exists()) {
            return false;
        }
        return true;
    }

    /**
     * 创建文件
     */
    public static boolean createFolderIfNotExist(String folderPath) {
        if (!fileIsExist(folderPath)) {
            File file = new File(folderPath);
            return file.mkdirs();
        } else {
            return true;
        }
    }

    /**
     * 读取文件
     */
    public static InputStream readFile(String filePath) {
        InputStream is = null;
        if (fileIsExist(filePath)) {
            File f = new File(filePath);
            try {
                is = new FileInputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }
        return is;
    }

    /**
     * 输入流转化为字节
     */
    public static byte[] readBytes(InputStream inputstream) {
        if (inputstream == null) {
            return null;
        }
        BufferedInputStream in = new BufferedInputStream(inputstream);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len = 0;
        byte[] data = null;
        try {
            while ((len = in.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            data = outStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                    in = null;
                } catch (IOException e) {
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                    outStream = null;
                } catch (IOException e) {
                }
            }
        }
        // 把outStream里的数据写入内存
        return data;
    }


    /**
     * 读取文件为字节
     */
    public static byte[] readBytes(String filePath) {
        InputStream inputstream = readFile(filePath);
        if (inputstream == null) {
            return null;
        }
        BufferedInputStream in = new BufferedInputStream(inputstream);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len = 0;
        byte[] data = null;
        try {
            while ((len = in.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            data = outStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                    in = null;
                } catch (IOException e) {
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                    outStream = null;
                } catch (IOException e) {
                }
            }
        }
        // 把outStream里的数据写入内存
        return data;
    }

    /**
     * 将字节写入文件
     */
    public static boolean writeByteFile(String filePath, byte[] bytes) {
        boolean success = true;
        File distFile = new File(filePath);
        if (!distFile.getParentFile().exists()) {
            try {
                distFile.getParentFile().mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filePath), BUFFER_SIZE);
            bos.write(bytes);
        } catch (Exception e) {
            AppLog.e(TAG, "save " + filePath + " failed!");
            success = false;
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                    bos = null;
                } catch (IOException e) {
                }
            }

        }

        return success;
    }

    /**
     * 将对象序列化成文件
     */
    public static void serialize(String filePath, Object obj) {
        File distFile = new File(filePath);
        if (!distFile.getParentFile().exists()) {
            try {
                distFile.getParentFile().mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(filePath));
            out.writeObject(obj);
        } catch (Exception e) {
            AppLog.e(TAG, "serialize " + filePath + " failed!");
            e.printStackTrace();
            if (distFile.exists()) {
                try {
                    distFile.delete();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将文件反序列化为对象
     */
    public static Object deserialize(String filePath) {

        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(filePath));
            return in.readObject();
        } catch (Exception e) {
            AppLog.e(TAG, "deserialize " + filePath + " failed!");
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 删除文件
     */
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String file : children) {
                    if (!deleteDir(new File(dir, file))) {
                        return false;
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        return dir.delete();
    }


    /**
     * 检查书籍存储空间
     */
    public static boolean checkLeftSpace() {
        File file = new File(ReplaceConstants.getReplaceConstants().APP_PATH_BOOK);

        if (!file.exists()) {
            file.mkdirs();
        }

        if (!file.exists()) {
            return false;
        }
        StatFs statFs = new StatFs(ReplaceConstants.getReplaceConstants().APP_PATH_BOOK);
        return new Long((long) statFs.getAvailableBlocks()).longValue()
                * ((long) statFs.getBlockSize()) > 20971520;
    }


    /**
     * 获取文件扩展名
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

    /**
     * 添加删除书籍存储文件任务
     */
    public static void doDelete() {
        if (mHandler != null) {
            mHandler.post(deleteFileTask);
        } else {
            thread.start();
            mHandler = new Handler(thread.getLooper());
            mHandler.post(deleteFileTask);
        }
    }

    /**
     * 设置书籍文件删除失败回调
     */
    public static void setDeleteFailedCallback(FileDeleteFailedCallback callback) {
        deleteFailedCallback = callback;
    }

    /**
     * 移除删除书籍存储文件任务
     */
    public static void removeDeleteCall() {
        if (mHandler != null && null != deleteFileTask) {
            mHandler.removeCallbacks(deleteFileTask);
        }
    }

    /**
     * 执行删除书籍存储文件
     */
    private static void doDeleteFile(String root) {
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

    /**
     * 判断文件是否满足删除条件
     */
    protected static boolean isContented(String singlePath) { // 最终路径是否满足条件
        if (getFileExtension(singlePath).equals(FILE_EXTENSION_SUFFIX)) {
            return true;
        }
        return false;
    }


    /**
     * 递归删除文件
     */
    private static void deleteFiles(File singleFile) {
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
     * 删除文件夹、文件
     *
     * @param filePath       文件路径
     * @param deleteThisPath 删除这个路径下的文件
     */
    public static void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) { //目录
                    File files[] = file.listFiles();
                    for (File file1 : files) {
                        deleteFolderFile(file1.getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) { //如果是文件，删除
                        file.delete();
                    } else { //目录
                        if (file.listFiles().length == 0) { //目录下没有文件或者目录，删除
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public interface FileDeleteFailedCallback {
        public void getDeleteFailedPath(ArrayList<String> pathList);

    }

    public interface FileDeleteSuccessCallback {
        public void getDeleteSuccessPath(ArrayList<String> pathList);
    }
}
