package net.lzbook.kit.request;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.input.MultiInputStreamHelper;
import net.lzbook.kit.utils.FileUtils;

import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class DataCache {

    public static boolean saveChapter(String content, int sequence, String book_id) {
        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book_id + "/" + sequence + ".text";
        if ((content.length()) <= Constants.CONTENT_ERROR_COUNT) {
            if (DataCache.isChapterExists(sequence, book_id)) {
                return true;
            } else {
                return !TextUtils.isEmpty(content) && FileUtils.writeByteFile(filePath, MultiInputStreamHelper.encrypt(content.getBytes()));
            }
        } else {
            return !TextUtils.isEmpty(content) && FileUtils.writeByteFile(filePath, MultiInputStreamHelper.encrypt(content.getBytes()));

        }
    }

    public static boolean fixChapter(String content, int sequence, String book_id) {
        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book_id + "/" + sequence + ".text";
        File file = new File(filePath);

        boolean isDeleteSuc = true;
        if (file.exists()) {
            isDeleteSuc = file.delete();
        }

        if (isDeleteSuc) {
            return !TextUtils.isEmpty(content) && FileUtils.writeByteFile(filePath, MultiInputStreamHelper.encrypt(content.getBytes()));
        }

        return false;
    }

    public static String getChapterFromCache(int sequence, String book_id) {
        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book_id + "/" + sequence + ".text";
        File file = new File(filePath);
        String content = null;
        if (file != null && file.exists()) {
            byte[] b = FileUtils.readBytes(filePath);
            try {
                content = new String(MultiInputStreamHelper.encrypt(b));

            } catch (Throwable e) {
                e.printStackTrace();
                // TODO 尝试解决 java.lang.OutOfMemoryError
                System.gc();
                content = new String(MultiInputStreamHelper.encrypt(b));
            }
        }
        return content;
    }

    public static boolean isChapterExists(int sequence, String book_id) {
        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book_id + "/" + sequence + ".text";
        File file = new File(filePath);
        return file.exists();
    }

    public static void saveRequestUpdate(String context) {

        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_LOG + "/" + "request_update_log.text";

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, true)));
            out.write(context);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveResultUpdate(String context) {

        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_LOG + "/" + "result_update_log.text";

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, true)));
            out.write(context);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveUpdateLog(String context) {

        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_LOG + "/" + "update_log.text";

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, true)));
            out.write(context);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean saveChapterFromPackage(String content, int sequence, String book_id) {
        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book_id + "/" + sequence + ".text";
        if (DataCache.isChapterExists(sequence, book_id)) {
            return true;
        } else {
            return !TextUtils.isEmpty(content) && FileUtils.writeByteFile(filePath, MultiInputStreamHelper.encrypt(content.getBytes()));
        }
    }
}
