package net.lzbook.kit.data.db;

import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.db.table.ChapterTable;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.Tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookChapterDao {
    private static final int version = 7; // 数据库版本
    private static final String TAB_CHAPTER = "chapter";
    private static final String SQL_CREATE_CHAPTER = "" +
            "create table IF NOT EXISTS " + TAB_CHAPTER
            + "(" +
            ChapterTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " +
            ChapterTable.CHAPTER_NAME + " VARCHAR(250), " +
            ChapterTable.NID + " INTEGER, " +
            ChapterTable.SEQUENCE + " INTEGER default -1, " +
            ChapterTable.SORT + " INTEGER, " +
            ChapterTable.CTYPE + " VARCHAR(250)," +
            ChapterTable.SITE + " VARCHAR(250), " +
            ChapterTable.CURL + " VARCHAR ," +
            ChapterTable.GSORT + " INTEGER ," +
            ChapterTable.VIP + " INTEGER , " +
            ChapterTable.PAID + " INTEGER , " +
            ChapterTable.SPEED + " INTEGER , " +
            ChapterTable.BOOK_CHAPTER_MD5 + " INTEGER , " +
            ChapterTable.CMD + " INTEGER , " +
            ChapterTable.BOOK_ID + " VARCHAR(250) , " +
            ChapterTable.PARAMETER + " VARCHAR(250) , " +
            ChapterTable.EXTRA_PARAMETER + " VARCHAR(250) , " +
            ChapterTable.API_URL + " VARCHAR(250) , " +
            ChapterTable.CHAPTER_FORM + " INTEGER ," +
            ChapterTable.CURL1 + " VARCHAR(250) ," +
            ChapterTable.WORD_COUNT + " INTEGER ," +
            ChapterTable.CHAPTER_ID + " VARCHAR(250) ," +
            ChapterTable.BOOK_SOURCE_ID + " VARCHAR(250) ," +
            ChapterTable.CHAPTER_STATUS + " VARCHAR(250) ," +
            ChapterTable.CHAPTER_UPDATE_TIME + " long " +
            ")";
    // version 6 add SPEED
    private SqliteHelper mHelper = null;
    private String DATABASE_NAME;
    // 升级书架书籍
    private String _book_id;

    public BookChapterDao(Context context, String book_id) {
        this._book_id = book_id;
        DATABASE_NAME = "book_chapter_" + book_id;
        this.mHelper = new SqliteHelper(context.getApplicationContext());
    }

    public void setBookId(String book_id) {
        this._book_id = book_id;
    }

    private static boolean checkColumnExist1(SQLiteDatabase db, String tableName, String columnName) {
        boolean result = false;
        Cursor cursor = null;
        try {
            //查询一行
            cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0"
                    , null);
            result = cursor != null && cursor.getColumnIndex(columnName) != -1;
        } catch (Exception e) {
//	         Log.e(TAG,"checkColumnExists1..." + e.getMessage()) ;
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return result;
    }

    public synchronized int getCount() {
        Cursor c = null;
        SQLiteDatabase db = null;
        int count = 0;
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(TAB_CHAPTER, new String[]{"count(sequence)"}, null, null, null, null, null, null);
            if (c.moveToFirst()) {
                count = c.getInt(0);
                AppLog.d("BookChapterDao", "count = " + count + " , col_count = " + c.getColumnCount());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }

        }
        return count;
    }

    public synchronized boolean insertBookChapter(List<Chapter> chapterList) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cur = null;
        try {
            db.beginTransaction();
            if (chapterList != null && chapterList.size() != 0) {

                int count = 0;
                cur = db.rawQuery("select count(_id) from " + TAB_CHAPTER, null);
                boolean bool = cur.moveToNext();
                if (bool) {
                    count = cur.getInt(0);
                }
                for (Chapter c : chapterList) {
                    ContentValues cv = new ContentValues();
                    cv.put(ChapterTable.CHAPTER_NAME, c.chapter_name);
                    cv.put(ChapterTable.NID, c.nid);
                    cv.put(ChapterTable.SORT, c.sort);
                    cv.put(ChapterTable.GSORT, c.gsort);
                    cv.put(ChapterTable.SITE, c.site);
                    cv.put(ChapterTable.CURL, c.curl);
                    cv.put(ChapterTable.CURL1, c.curl1);
                    cv.put(ChapterTable.SEQUENCE, count);
                    cv.put(ChapterTable.GSORT, c.gsort);
                    cv.put(ChapterTable.BOOK_CHAPTER_MD5, c.book_chapter_md5);
                    cv.put(ChapterTable.CMD, c.cmd);
                    cv.put(ChapterTable.BOOK_ID, c.book_id);
                    cv.put(ChapterTable.PARAMETER, c.parameter);
                    cv.put(ChapterTable.EXTRA_PARAMETER, c.extra_parameter);
                    cv.put(ChapterTable.API_URL, c.api_url);
                    cv.put(ChapterTable.CHAPTER_FORM, c.chapter_form);
                    cv.put(ChapterTable.WORD_COUNT, c.word_count);
                    cv.put(ChapterTable.CHAPTER_ID, c.chapter_id);
                    cv.put(ChapterTable.BOOK_SOURCE_ID, c.book_source_id);
                    cv.put(ChapterTable.CHAPTER_STATUS, c.chapter_status);
                    cv.put(ChapterTable.CHAPTER_UPDATE_TIME, c.time);
                    c.sequence = count;
                    db.insert(TAB_CHAPTER, null, cv);
                    count++;
                }
                db.setTransactionSuccessful();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cur != null) {
                cur.close();
            }
            db.endTransaction();
            db.close();
        }
    }

    /*
     * 根据 sort nid name查询章节
     */
    public boolean getChapterByNidSortName(String extra_parameter, int sort, int gsort, String name) {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(TAB_CHAPTER, null, "( extra_parameter=" + extra_parameter + " and sort=" + sort + " and " + ChapterTable.GSORT + "="
                    + gsort + " ) or chapter_name='" + name + "'", null, null, null, null, null);
            if (c.moveToFirst()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                c.close();
                db.close();
            } catch (Exception e2) {
            }
        }
        return false;
    }

    /*
     * 根据 cmd name查询章节
     */
    public boolean getChapterByExtraParameter(String chapter_name, String curl) {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(TAB_CHAPTER, null, "( chapter_name='" + chapter_name + "' and curl='" + curl + "' )", null, null, null, null, null);
            if (c.moveToFirst()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                c.close();
                db.close();
            } catch (Exception e2) {
            }
        }
        return false;
    }

    /*
     * 根据 book_chapter_md5 查询章节
     */
    public boolean getChapterByExtraParameter(String book_chapter_md5) {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(TAB_CHAPTER, null, "( book_chapter_md5='" + book_chapter_md5 + "' )", null, null, null, null, null);
            if (c.moveToFirst()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                c.close();
                db.close();
            } catch (Exception e2) {
            }
        }
        return false;
    }

    /*
     * 根据 章节名查询章节
     */
    public boolean getChapterByName(String name) {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(TAB_CHAPTER, null, "chapter_name='" + name + "'", null, null, null, null, null);
            if (c.moveToFirst()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                c.close();
                db.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return false;
    }

    public Map<String, Chapter> getChapterMap() {
        SQLiteDatabase db = null;
        Cursor c = null;
        Map<String, Chapter> map = new HashMap<String, Chapter>();
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(TAB_CHAPTER, null, null, null, null, null, null, null);
            Chapter chapter = null;
            while (c.moveToNext()) {
                chapter = new Chapter();
                chapter.chapter_name = c.getString(ChapterTable.CHAPTER_NAME_INDEX);
                map.put(Tools.getPatterName(chapter.chapter_name), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                c.close();
                db.close();
            } catch (Exception e2) {
            }
        }
        return map;
    }

    public ArrayList<Chapter> queryBookChapter() {
        ArrayList<Chapter> list = new ArrayList<Chapter>();
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(TAB_CHAPTER, null, null, null, null, null, null, null);
            Chapter chapter = null;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                chapter = new Chapter();
                chapter.book_id = _book_id;
                chapter.chapter_name = c.getString(ChapterTable.CHAPTER_NAME_INDEX);
                chapter.nid = c.getInt(ChapterTable.NID_INDEX);
                chapter.sequence = c.getInt(ChapterTable.SEQUENCE_INDEX);
                chapter.sort = c.getInt(ChapterTable.SORT_INDEX);
                chapter.site = c.getString(ChapterTable.SITE_INDEX);
                chapter.curl = c.getString(ChapterTable.CURL_INDEX);
                chapter.curl1 = c.getString(ChapterTable.CURL1_INDEX);
                chapter.gsort = c.getInt(ChapterTable.GSORT_INDEX);
                chapter.book_chapter_md5 = c.getString(ChapterTable.BOOK_CHAPTER_MD5_INDEX);
                chapter.cmd = c.getString(ChapterTable.CMD_INDEX);
                chapter.book_id = c.getString(ChapterTable.BOOK_ID_INDEX);
                chapter.parameter = c.getString(ChapterTable.PARAMETER_INDEX);
                chapter.extra_parameter = c.getString(ChapterTable.EXTRA_PARAMETER_INDEX);
                chapter.api_url = c.getString(ChapterTable.API_URL_INDEX);
                chapter.chapter_form = c.getInt(ChapterTable.CHAPTER_FORM_INDEX);
                chapter.word_count = c.getInt(ChapterTable.WORD_COUNT_INDEX);
                chapter.chapter_id = c.getString(ChapterTable.CHAPTER_ID_INDEX);
                chapter.book_source_id = c.getString(ChapterTable.BOOK_SOURCE_ID_INDEX);
                chapter.chapter_status = c.getString(ChapterTable.CHAPTER_STATUS_INDEX);
                chapter.time = c.getLong(ChapterTable.CHAPTER_UPDATE_TIME_INDEX);
                list.add(chapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                c.close();
                db.close();
            } catch (Exception e2) {
            }
        }
        return list;
    }

    //在chapter表中从sequence开始向前取count章
    public ArrayList<Chapter> queryLastChapters(int sequence, int count) {
        ArrayList<Chapter> list = new ArrayList<Chapter>();
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = mHelper.getReadableDatabase();
            int m = sequence - count + 1;
            int n = count;
            c = db.query(TAB_CHAPTER, null, null, null, null, null, null, m + "," + n);
            Chapter chapter = null;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                chapter = new Chapter();
                chapter.book_id = _book_id;
                chapter.chapter_name = c.getString(ChapterTable.CHAPTER_NAME_INDEX);
                chapter.nid = c.getInt(ChapterTable.NID_INDEX);
                chapter.sequence = c.getInt(ChapterTable.SEQUENCE_INDEX);
                chapter.sort = c.getInt(ChapterTable.SORT_INDEX);
                chapter.site = c.getString(ChapterTable.SITE_INDEX);
                chapter.curl = c.getString(ChapterTable.CURL_INDEX);
                chapter.curl1 = c.getString(ChapterTable.CURL1_INDEX);
                chapter.gsort = c.getInt(ChapterTable.GSORT_INDEX);
                chapter.book_chapter_md5 = c.getString(ChapterTable.BOOK_CHAPTER_MD5_INDEX);
                chapter.cmd = c.getString(ChapterTable.CMD_INDEX);
                chapter.book_id = c.getString(ChapterTable.BOOK_ID_INDEX);
                chapter.parameter = c.getString(ChapterTable.PARAMETER_INDEX);
                chapter.extra_parameter = c.getString(ChapterTable.EXTRA_PARAMETER_INDEX);
                chapter.api_url = c.getString(ChapterTable.API_URL_INDEX);
                chapter.chapter_form = c.getInt(ChapterTable.CHAPTER_FORM_INDEX);
                chapter.word_count = c.getInt(ChapterTable.WORD_COUNT_INDEX);
                chapter.chapter_id = c.getString(ChapterTable.CHAPTER_ID_INDEX);
                chapter.book_source_id = c.getString(ChapterTable.BOOK_SOURCE_ID_INDEX);
                chapter.chapter_status = c.getString(ChapterTable.CHAPTER_STATUS_INDEX);
                chapter.time = c.getLong(ChapterTable.CHAPTER_UPDATE_TIME_INDEX);
                list.add(chapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                c.close();
                db.close();
            } catch (Exception e2) {
            }
        }
        return list;
    }

    public Chapter getLastChapter() {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(TAB_CHAPTER, null, "sequence=(select max(sequence) from chapter)", null, null, null, null,
                    null);
            Chapter chapter;
            if (c.moveToNext()) {
                chapter = new Chapter();
                chapter.book_id = _book_id;
                chapter.chapter_name = c.getString(ChapterTable.CHAPTER_NAME_INDEX);
                chapter.nid = c.getInt(ChapterTable.NID_INDEX);
                chapter.sequence = c.getInt(ChapterTable.SEQUENCE_INDEX);
                chapter.sort = c.getInt(ChapterTable.SORT_INDEX);
                chapter.site = c.getString(ChapterTable.SITE_INDEX);
                chapter.curl = c.getString(ChapterTable.CURL_INDEX);
                chapter.curl1 = c.getString(ChapterTable.CURL1_INDEX);
                chapter.gsort = c.getInt(ChapterTable.GSORT_INDEX);
                chapter.book_chapter_md5 = c.getString(ChapterTable.BOOK_CHAPTER_MD5_INDEX);
                chapter.cmd = c.getString(ChapterTable.CMD_INDEX);
                chapter.book_id = c.getString(ChapterTable.BOOK_ID_INDEX);
                chapter.parameter = c.getString(ChapterTable.PARAMETER_INDEX);
                chapter.extra_parameter = c.getString(ChapterTable.EXTRA_PARAMETER_INDEX);
                chapter.api_url = c.getString(ChapterTable.API_URL_INDEX);
                chapter.chapter_form = c.getInt(ChapterTable.CHAPTER_FORM_INDEX);
                chapter.word_count = c.getInt(ChapterTable.WORD_COUNT_INDEX);
                chapter.chapter_id = c.getString(ChapterTable.CHAPTER_ID_INDEX);
                chapter.book_source_id = c.getString(ChapterTable.BOOK_SOURCE_ID_INDEX);
                chapter.chapter_status = c.getString(ChapterTable.CHAPTER_STATUS_INDEX);
                chapter.time = c.getLong(ChapterTable.CHAPTER_UPDATE_TIME_INDEX);

                return chapter;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                c.close();
                db.close();
            } catch (Exception e2) {
            }
        }
        return null;
    }

    public Chapter getChapterBySequence(int sequence) {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(TAB_CHAPTER, null, ChapterTable.SEQUENCE + "=" + sequence, null, null, null, null,
                    null);
            Chapter chapter;
            if (c.moveToNext()) {
                chapter = new Chapter();
                chapter.book_id = _book_id;
                chapter.chapter_name = c.getString(ChapterTable.CHAPTER_NAME_INDEX);
                chapter.nid = c.getInt(ChapterTable.NID_INDEX);
                chapter.sequence = c.getInt(ChapterTable.SEQUENCE_INDEX);
                chapter.sort = c.getInt(ChapterTable.SORT_INDEX);
                chapter.site = c.getString(ChapterTable.SITE_INDEX);
                chapter.curl = c.getString(ChapterTable.CURL_INDEX);
                chapter.curl1 = c.getString(ChapterTable.CURL1_INDEX);
                chapter.gsort = c.getInt(ChapterTable.GSORT_INDEX);
                chapter.book_chapter_md5 = c.getString(ChapterTable.BOOK_CHAPTER_MD5_INDEX);
                chapter.cmd = c.getString(ChapterTable.CMD_INDEX);
                chapter.book_id = c.getString(ChapterTable.BOOK_ID_INDEX);
                chapter.parameter = c.getString(ChapterTable.PARAMETER_INDEX);
                chapter.extra_parameter = c.getString(ChapterTable.EXTRA_PARAMETER_INDEX);
                chapter.api_url = c.getString(ChapterTable.API_URL_INDEX);
                chapter.chapter_form = c.getInt(ChapterTable.CHAPTER_FORM_INDEX);
                chapter.word_count = c.getInt(ChapterTable.WORD_COUNT_INDEX);
                chapter.chapter_id = c.getString(ChapterTable.CHAPTER_ID_INDEX);
                chapter.book_source_id = c.getString(ChapterTable.BOOK_SOURCE_ID_INDEX);
                chapter.chapter_status = c.getString(ChapterTable.CHAPTER_STATUS_INDEX);
                chapter.time = c.getLong(ChapterTable.CHAPTER_UPDATE_TIME_INDEX);

                return chapter;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                c.close();
                db.close();
            } catch (Exception e2) {
            }
        }
        return null;
    }

    public Chapter getChapterById(String chapter_id) {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(TAB_CHAPTER, null, ChapterTable.CHAPTER_ID + " = '" + chapter_id + "'", null, null, null, null,
                    null);
            Chapter chapter;
            if (c.moveToNext()) {
                chapter = new Chapter();
                chapter.book_id = _book_id;
                chapter.chapter_name = c.getString(ChapterTable.CHAPTER_NAME_INDEX);
                chapter.nid = c.getInt(ChapterTable.NID_INDEX);
                chapter.sequence = c.getInt(ChapterTable.SEQUENCE_INDEX);
                chapter.sort = c.getInt(ChapterTable.SORT_INDEX);
                chapter.site = c.getString(ChapterTable.SITE_INDEX);
                chapter.curl = c.getString(ChapterTable.CURL_INDEX);
                chapter.curl1 = c.getString(ChapterTable.CURL1_INDEX);
                chapter.gsort = c.getInt(ChapterTable.GSORT_INDEX);
                chapter.book_chapter_md5 = c.getString(ChapterTable.BOOK_CHAPTER_MD5_INDEX);
                chapter.cmd = c.getString(ChapterTable.CMD_INDEX);
                chapter.book_id = c.getString(ChapterTable.BOOK_ID_INDEX);
                chapter.parameter = c.getString(ChapterTable.PARAMETER_INDEX);
                chapter.extra_parameter = c.getString(ChapterTable.EXTRA_PARAMETER_INDEX);
                chapter.api_url = c.getString(ChapterTable.API_URL_INDEX);
                chapter.chapter_form = c.getInt(ChapterTable.CHAPTER_FORM_INDEX);
                chapter.word_count = c.getInt(ChapterTable.WORD_COUNT_INDEX);
                chapter.chapter_id = c.getString(ChapterTable.CHAPTER_ID_INDEX);
                chapter.book_source_id = c.getString(ChapterTable.BOOK_SOURCE_ID_INDEX);
                chapter.chapter_status = c.getString(ChapterTable.CHAPTER_STATUS_INDEX);
                chapter.time = c.getLong(ChapterTable.CHAPTER_UPDATE_TIME_INDEX);

                return chapter;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                c.close();
                db.close();
            } catch (Exception e2) {
            }
        }
        return null;
    }

    /**
     * 单章切源更新当前章节
     * <p/>
     * updateBookCurrentChapter
     * c
     * void
     */
    public void updateBookCurrentChapter(Chapter c, int sequence) {
        SQLiteDatabase db = null;
        try {
            db = mHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            if (!TextUtils.isEmpty(c.chapter_name) && !c.chapter_name.equals("null")) {
                cv.put(ChapterTable.CHAPTER_NAME, c.chapter_name);
            }
            cv.put(ChapterTable.NID, c.nid);
            cv.put(ChapterTable.SORT, c.sort);
            cv.put(ChapterTable.SITE, c.site);
            cv.put(ChapterTable.CURL, c.curl);
            cv.put(ChapterTable.CURL1, c.curl1);
            cv.put(ChapterTable.SEQUENCE, sequence);
            cv.put(ChapterTable.BOOK_CHAPTER_MD5, c.book_chapter_md5);
            cv.put(ChapterTable.CMD, c.cmd);
            cv.put(ChapterTable.BOOK_ID, c.book_id);
            cv.put(ChapterTable.PARAMETER, c.parameter);
            cv.put(ChapterTable.EXTRA_PARAMETER, c.extra_parameter);
            cv.put(ChapterTable.API_URL, c.api_url);
            cv.put(ChapterTable.CHAPTER_FORM, c.chapter_form);
            cv.put(ChapterTable.WORD_COUNT, c.word_count);
            cv.put(ChapterTable.CHAPTER_ID, c.chapter_id);
            cv.put(ChapterTable.BOOK_SOURCE_ID, c.book_source_id);
            cv.put(ChapterTable.CHAPTER_STATUS, c.chapter_status);
            cv.put(ChapterTable.CHAPTER_UPDATE_TIME, c.time);
//			cv.put(ChapterTable.GSORT, c.gsort);
            db.update(TAB_CHAPTER, cv, ChapterTable.SEQUENCE + "=" + sequence, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * 根据章节id更新当前章节
     * <p/>
     * updateBookCurrentChapter
     * c
     * void
     */
    public boolean updateChapterById(Chapter c) {
        SQLiteDatabase db = null;
        int result = 0;
        try {
            db = mHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            if (!TextUtils.isEmpty(c.chapter_name) && !c.chapter_name.equals("null")) {
                cv.put(ChapterTable.CHAPTER_NAME, c.chapter_name);
            }
            cv.put(ChapterTable.NID, c.nid);
            cv.put(ChapterTable.SORT, c.sort);
            cv.put(ChapterTable.SITE, c.site);
            cv.put(ChapterTable.CURL, c.curl);
            cv.put(ChapterTable.CURL1, c.curl1);
            cv.put(ChapterTable.SEQUENCE, c.sequence);
            cv.put(ChapterTable.BOOK_CHAPTER_MD5, c.book_chapter_md5);
            cv.put(ChapterTable.CMD, c.cmd);
            cv.put(ChapterTable.BOOK_ID, c.book_id);
            cv.put(ChapterTable.PARAMETER, c.parameter);
            cv.put(ChapterTable.EXTRA_PARAMETER, c.extra_parameter);
            cv.put(ChapterTable.API_URL, c.api_url);
            cv.put(ChapterTable.CHAPTER_FORM, c.chapter_form);
            cv.put(ChapterTable.WORD_COUNT, c.word_count);
            cv.put(ChapterTable.CHAPTER_ID, c.chapter_id);
            cv.put(ChapterTable.BOOK_SOURCE_ID, c.book_source_id);
            cv.put(ChapterTable.CHAPTER_STATUS, c.chapter_status);
            cv.put(ChapterTable.CHAPTER_UPDATE_TIME, c.time);
//			cv.put(ChapterTable.GSORT, c.gsort);
            result = db.update(TAB_CHAPTER, cv, ChapterTable.CHAPTER_ID + " = '" + c.chapter_id + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return result != 0;
    }

    /**
     * 全本切源删除当前章节之后的所有章节
     * <p/>
     * updateBookCurrentChapter
     * chapterList
     * sort
     * void
     */
    public synchronized void deleteBookChapters(int sequence) {
        SQLiteDatabase db = null;
        try {
            db = mHelper.getWritableDatabase();
            int i = db.delete(TAB_CHAPTER, ChapterTable.SEQUENCE + ">=" + sequence, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
        }
    }

    /*
    * 根据 章节ID查询_id
    */
    public int getChapterIdByChapterId(String id) {
        SQLiteDatabase db = null;
        Cursor c = null;
        int _id = -1;
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(TAB_CHAPTER, null, ChapterTable.CHAPTER_ID + "='" + id + "'", null, null, null, null, null);
            while (c.moveToNext()) {
                _id = c.getInt(ChapterTable.ID_INDEX);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                c.close();
                db.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return _id;
    }

    public void changeChargeBookState(int _id, int length) {
        SQLiteDatabase db = null;
        Cursor c = null;
        int index = _id;
        int isBought = -1;
        try {
            db = mHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("is_purchased", 1);
            for (int i = 0; i < length; i++) {
                db.update(TAB_CHAPTER, cv, ChapterTable.ID + "='" + index + "'", null);
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
            if (c != null) {
                c.close();
            }
        }
    }

    private class SqliteHelper extends SQLiteOpenHelper {

        public SqliteHelper(Context paramContext) {
            super(paramContext, DATABASE_NAME, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
            paramSQLiteDatabase.execSQL(SQL_CREATE_CHAPTER);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 3) {
                String new_column = "alter table " + TAB_CHAPTER +
                        " add " + "gsort INTEGER";
                db.execSQL(new_column);
            }
            if (oldVersion < 4) {
                String new_column = "alter table " + TAB_CHAPTER +
                        " add " + ChapterTable.VIP + " INTEGER";
                db.execSQL(new_column);
            }
            if (oldVersion < 5) {
                String new_column = "alter table " + TAB_CHAPTER +
                        " add " + ChapterTable.PAID + " INTEGER";
                db.execSQL(new_column);
            }
            if (oldVersion < 6) {
                String new_column;

                if (!checkColumnExist1(db, TAB_CHAPTER, ChapterTable.SPEED)) {
                    new_column = "alter table " + TAB_CHAPTER + " add " + ChapterTable.SPEED + " INTEGER";
                    db.execSQL(new_column);
                }

                if (!checkColumnExist1(db, TAB_CHAPTER, ChapterTable.BOOK_CHAPTER_MD5)) {
                    new_column = "alter table " + TAB_CHAPTER + " add " + ChapterTable.BOOK_CHAPTER_MD5 + " VARCHAR(250)";
                    db.execSQL(new_column);
                }

                if (!checkColumnExist1(db, TAB_CHAPTER, ChapterTable.CMD)) {
                    new_column = "alter table " + TAB_CHAPTER + " add " + ChapterTable.CMD + " VARCHAR(250)";
                    db.execSQL(new_column);
                }

                if (!checkColumnExist1(db, TAB_CHAPTER, ChapterTable.BOOK_ID)) {
                    new_column = "alter table " + TAB_CHAPTER + " add " + ChapterTable.BOOK_ID + " VARCHAR(250)";
                    db.execSQL(new_column);
                }

                if (!checkColumnExist1(db, TAB_CHAPTER, ChapterTable.PARAMETER)) {
                    new_column = "alter table " + TAB_CHAPTER + " add " + ChapterTable.PARAMETER + " VARCHAR(250)";
                    db.execSQL(new_column);
                }

                if (!checkColumnExist1(db, TAB_CHAPTER, ChapterTable.EXTRA_PARAMETER)) {
                    new_column = "alter table " + TAB_CHAPTER + " add " + ChapterTable.EXTRA_PARAMETER + " VARCHAR(250)";
                    db.execSQL(new_column);
                }

                if (!checkColumnExist1(db, TAB_CHAPTER, ChapterTable.API_URL)) {
                    new_column = "alter table " + TAB_CHAPTER + " add " + ChapterTable.API_URL + " VARCHAR(250)";
                    db.execSQL(new_column);
                }

                if (!checkColumnExist1(db, TAB_CHAPTER, ChapterTable.CHAPTER_FORM)) {
                    new_column = "alter table " + TAB_CHAPTER + " add " + ChapterTable.CHAPTER_FORM + " INTEGER";
                    db.execSQL(new_column);
                }
            }

            if (oldVersion < 7) {
                String new_column;

                if (!checkColumnExist1(db, TAB_CHAPTER, ChapterTable.CURL1)) {
                    new_column = "alter table " + TAB_CHAPTER + " add " + ChapterTable.CURL1 + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, TAB_CHAPTER, ChapterTable.WORD_COUNT)) {
                    new_column = "alter table " + TAB_CHAPTER + " add " + ChapterTable.WORD_COUNT + " INTEGER";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, TAB_CHAPTER, ChapterTable.CHAPTER_ID)) {
                    new_column = "alter table " + TAB_CHAPTER + " add " + ChapterTable.CHAPTER_ID + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, TAB_CHAPTER, ChapterTable.BOOK_SOURCE_ID)) {
                    new_column = "alter table " + TAB_CHAPTER + " add " + ChapterTable.BOOK_SOURCE_ID + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, TAB_CHAPTER, ChapterTable.CHAPTER_STATUS)) {
                    new_column = "alter table " + TAB_CHAPTER + " add " + ChapterTable.CHAPTER_STATUS + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, TAB_CHAPTER, ChapterTable.CHAPTER_UPDATE_TIME)) {
                    new_column = "alter table " + TAB_CHAPTER + " add " + ChapterTable.CHAPTER_UPDATE_TIME + " long";
                    db.execSQL(new_column);
                }
            }
        }

    }
}