package com.ding.basic.util;

import android.os.Environment;
import android.text.TextUtils;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.Chapter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataCache {

    //老版青果缓存路径
    private static String QG_CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/quanben/book/";
    private static String QG_SOURCE = "open.qingoo.cn";

    public static boolean isChapterContentAvailable(String content) {
        if (TextUtils.isEmpty(content) || content.trim().length() < 50) {
            return false;
        }

        return true;
    }


    public static boolean saveChapter(String content, Chapter chapter) {
        String filePath = getCacheFilePath(chapter);
//        if (isChapterContentAvailable(content)) {
        if (isNewCacheExists(chapter)) {
            return true;
        }
        if (TextUtils.isEmpty(content)) {
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
        return ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + chapter.getBook_id() + "/" + chapter.getBook_source_id() + "/" + chapter.getChapter_id();
    }

    public static String getOldCacheFilePath(Chapter chapter) {
        return ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + chapter.getBook_id() + "/" + chapter.getSequence() + ".text";
    }

    public static String getOldQGCacheFilePath(Chapter chapter) {
        return QG_CACHE_PATH + chapter.getBook_id() + "/" + chapter.getChapter_id() + ".text";
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
        String oldQGFilePath = getOldQGCacheFilePath(chapter);
        String content = null;
        byte[] b = null;

        if (new File(filePath).exists()) {
            b = FileUtils.readBytes(filePath);
        } else if (new File(oldFilePath).exists()) {
            b = FileUtils.readBytes(oldFilePath);
        }else if(new File(oldQGFilePath).exists()){
            b = FileUtils.readBytes(oldQGFilePath);
        }

        if(b != null) {
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

    public static boolean isChapterCached(Chapter chapter) {
        return isNewCacheExists(chapter) || isOldChapterExists(chapter);
    }

    public static boolean isOldChapterExists(Chapter chapter) {
        if (chapter == null) {
            return false;
        }
        if (QG_SOURCE.equals(chapter.getHost())) {

            //TODO check qg cache
            return new File(getOldQGCacheFilePath(chapter)).exists();
        } else {
            String oldFilePath = getOldCacheFilePath(chapter);
            return new File(oldFilePath).exists();
        }
    }

    public static boolean isNewCacheExists(Chapter chapter) {
        if (chapter == null) {
            return false;
        }

        String filePath = getCacheFilePath(chapter);
        return new File(filePath).exists();
    }

    public static List<String> getCacheChapterIDs(Book book) {
        String[] listNames;
        List<String> list = new ArrayList();

        listNames = new File(ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book.getBook_id() + "/" + book.getBook_source_id()).list();

        if (listNames != null) {
            list.addAll(Arrays.asList(listNames));
        }
        return list;
    }

    public static void deleteOtherSourceCache(Book book) {
        File[] files = new File(ReplaceConstants.getReplaceConstants().APP_PATH_BOOK + book.getBook_id()).listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.isDirectory() || f.getName().equals(book.getBook_source_id())) {
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
}
