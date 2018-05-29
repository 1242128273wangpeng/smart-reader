package net.lzbook.kit.request;

import android.text.TextUtils;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.input.MultiInputStreamHelper;
import net.lzbook.kit.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataCache {

    public static boolean isChapterContentAvailable(String content) {
        if (TextUtils.isEmpty(content) || content.trim().length() < 50) {
            return false;
        }

        return true;
    }


    public static boolean saveChapter(String content, Chapter chapter) {
        String filePath = getCacheFilePath(chapter);
//        if (isChapterContentAvailable(content)) {
        if (isChapterExists(chapter)) {
            return true;
        }
        if(TextUtils.isEmpty(content)){
            content = "null";
        }
        return FileUtils.writeByteFile(filePath, MultiInputStreamHelper.encrypt(content.getBytes()));
//        } else {
//            return false;
//        }
    }

    public static boolean saveEncryptedChapter(byte[] content, Chapter chapter) {
        return FileUtils.writeByteFile(getCacheFilePath(chapter), content);
    }

    public static boolean isRangeCached(Book book, List<Chapter> chapterList) {
        for (int i = 0; i < chapterList.size(); i++) {
            if (!new File(getCacheFilePath(chapterList.get(i))).exists()) {
                return false;
            }
        }
        return true;
    }


    public static String getCacheFilePath(Chapter chapter) {
        return ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + chapter.book_id + "/" + chapter.book_source_id + "/" + chapter.chapter_id;
    }

    public static String getOldCacheFilePath(Chapter chapter) {
        return ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + chapter.book_id + "/" + chapter.sequence + ".text";
    }

    public static boolean fixChapter(String content, Chapter chapter) {
        String filePath = getCacheFilePath(chapter);
        File file = new File(filePath);
        boolean isDeleteSuc = true;
        if (file.exists()) {
            isDeleteSuc = file.delete();
        }
        if (isDeleteSuc && !TextUtils.isEmpty(content) && FileUtils.writeByteFile(filePath, MultiInputStreamHelper.encrypt(content.getBytes()))) {
            return true;
        }

        return false;
    }

    public static String getChapterFromCache(Chapter chapter) {
        String filePath = getCacheFilePath(chapter);
        String oldFilePath = getOldCacheFilePath(chapter);
        String content = null;
        byte[] b;
        if (new File(filePath).exists()) {
            b = FileUtils.readBytes(filePath);
            try {
                content = new String(MultiInputStreamHelper.encrypt(b));
            } catch (Throwable e) {
                e.printStackTrace();
                System.gc();
                content = new String(MultiInputStreamHelper.encrypt(b));
            }
        } else if (new File(oldFilePath).exists()) {
            b = FileUtils.readBytes(oldFilePath);
            try {
                content = new String(MultiInputStreamHelper.encrypt(b));
            } catch (Throwable e2) {
                e2.printStackTrace();
                System.gc();
                content = new String(MultiInputStreamHelper.encrypt(b));
            }
        }
        if (TextUtils.isEmpty(content)) {
            return content;
        }
        return content.replace("\\n", "\n").replace("\\n\\n", "\n").replace("\\n \\n", "\n").replace("\\", "");
    }

    public static boolean isChapterExists(Chapter chapter) {
        if(chapter == null){
            return false;
        }
        if (Constants.QG_SOURCE.equals(chapter.site)) {
            return com.quduquxie.network.DataCache.isChapterExists(chapter.chapter_id, chapter.book_id);
        } else {
            String filePath = getCacheFilePath(chapter);
            String oldFilePath = getOldCacheFilePath(chapter);
            if (new File(filePath).exists()) {
                return true;
            }
            return new File(oldFilePath).exists();
        }
    }

    public static boolean isNewCacheExists(Chapter chapter) {
        if(chapter == null){
            return false;
        }

        String filePath = getCacheFilePath(chapter);
        return new File(filePath).exists();
    }

    public static List<String> getCacheChapterIDs(Book book) {
        String[] listNames;
        List<String> list = new ArrayList();
        if (Constants.QG_SOURCE.equals(book.site)) {
            listNames = new File(com.quduquxie.Constants.APP_PATH_BOOK + book.book_id).list();
        } else {
            listNames = new File(ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book.book_id + "/" + book.book_source_id).list();
        }
        if (listNames != null) {
            list.addAll(Arrays.asList(listNames));
        }
        return list;
    }

    public static void deleteOtherSourceCache(Book book) {
        File[] files = new File(ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book.book_id).listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.isDirectory() || f.getName().equals(book.book_source_id)) {
                    f.delete();
                } else {
                    FileUtils.deleteDir(f);
                }
            }
        }
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

    public static boolean saveChapterFromPackage(String content, Chapter chapter) {
        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + chapter.book_id + "/" + chapter.sequence + ".text";
        if (DataCache.isChapterExists(chapter)) {
            return true;
        } else {
            return !TextUtils.isEmpty(content) && FileUtils.writeByteFile(filePath, MultiInputStreamHelper.encrypt(content.getBytes()));
        }
    }
}
