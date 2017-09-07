package net.lzbook.kit.cache;

import net.lzbook.kit.constants.ReplaceConstants;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.math.BigDecimal;

/**
 * 数据删除工具类
 */
public class DataCleanManager {


    public static long internalCacheSize = 0;

    /**
     * 清除本应用内部缓存
     * (/data/data/com.xxx.xxx/cache)
     */
    public static void cleanInternalCache(Context context) {
        deleteFilesByDirectory(context.getCacheDir());
        deleteFilesByDirectory(context.getFilesDir());
    }

    /**
     * 删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理
     */
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File child : directory.listFiles()) {
                if (child.isDirectory()) {
                    deleteFilesByDirectory(child);
                }
                child.delete();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 本应用数据清除管理器
    ///////////////////////////////////////////////////////////////////////////

    public static String getTotalCacheSize(Context context) throws Exception {

        File cache;
        long cacheSize = 0;
        cache = new File(ReplaceConstants.getReplaceConstants().APP_PATH);
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED) && cache.exists()) {
            cacheSize = getFolderSize(cache);
        } else {
            cache = new File(context.getCacheDir(), ReplaceConstants.getReplaceConstants().APP_PATH);
            cacheSize += getFolderSize(cache);
        }
        DataCleanManager.internalCacheSize = cacheSize;
        return getFormatSize(cacheSize);
    }

    public static void clearAllCache(final Context context) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        File cache;

        cache = new File(ReplaceConstants.getReplaceConstants().APP_PATH);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && cache.exists()) {
            deleteDir(cache);
        } else {
            cache = new File(context.getCacheDir(), ReplaceConstants.getReplaceConstants().APP_PATH);
            if (cache.exists()) {
                deleteDir(cache);
            }
        }
//            }
//        }).start();

    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    // 获取文件
    public static long getFolderSize(File dir) throws Exception {
        long size = 0;

        if (!dir.isDirectory()) {
            return 0;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            } else if (file.isDirectory()) {
                size += file.length();
                size += getFolderSize(file); // 递归调用继续统计
            }
        }
        return size;
    }

    /**
     * 格式化单位
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            // return size + "Byte";
            return "0K";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }
}
