package net.lzbook.kit.data.db;

import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.Bookmark;
import net.lzbook.kit.data.db.table.BookMarkTable;
import net.lzbook.kit.data.db.table.BookTable;
import net.lzbook.kit.data.db.table.FixBookTable;
import net.lzbook.kit.data.db.table.HistoryInforTable;
import net.lzbook.kit.repair_books.bean.BookFix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Dao {

    private static final int version = 16;
    private static final String DATABASE_NAME = ReplaceConstants.getReplaceConstants().DATABASE_NAME;
    private static final String TAB_SITE_PATTERN = "site_pattern";
    // 正则模板表
    private static final String SQL_CREATE_SITE_PATTERN = "create table IF NOT EXISTS " + TAB_SITE_PATTERN
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, site varchar(250) not null, pattern varchar);";
    // 书架表
    private static final String SQL_CREATE_BOOK = "create table IF NOT EXISTS " + BookTable.TABLE_NAME + "("
            + BookTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BookTable.GID + " INTEGER default -1, "
            + BookTable.NID + " INTEGER default -1, "
            + BookTable.NAME + " VARCHAR(250) , "
            + BookTable.AUTHOR + " VARCHAR(250) , "
            + BookTable.IMG_URL + " VARCHAR(250) , "
            + BookTable.CHAPTER_COUNT + " INTEGER , "
            + BookTable.STATUS + " SMALLINT , "
            + BookTable.LAST_UPDATETIME + " long, "
            + BookTable.LAST_SORT + " INTEGER , "
            + BookTable.LAST_CHAPTER_NAME + " VARCHAR(250) , "
            + BookTable.SEQUENCE + " INTEGER default -1, "
            + BookTable.OFFSET + " INTEGER , "
            + BookTable.UPDATE + " INTEGER , "
            + BookTable.BAD_NID + " VARCHAR, "
            + BookTable.SEQUENCE_TIME + " long, "
            + BookTable.G_SORT + " INTEGER, "
            + BookTable.READED + " INTEGER default 0 ,"
            + BookTable.VIP + " INTEGER , "
            + BookTable.COLLECTED + " INTEGER default 0 , "
            + BookTable.AUTO_PAY + " INTEGER default 0, "
            + BookTable.INSERT_TIME + " long ,"
            + BookTable.CATEGORY + " VARCHAR(50) , "
            + BookTable.SPEED_MODE + " INTEGER , "
            + BookTable.BOOK_ID + " VARCHAR(250) , "
            + BookTable.BOOK_SOURCE_ID + " VARCHAR(250) , "
            + BookTable.LAST_CHAPTER_MD5 + " VARCHAR(250) , "
            + BookTable.LAST_CHAPTER_URL + " VARCHAR(250) , "
            + BookTable.PARAMETER + " VARCHAR(250) , "
            + BookTable.EXTRA_PARAMETER + " VARCHAR(250) , "
            + BookTable.INITIALIZATION_STATUS + " INTEGER , "
            + BookTable.SITE + " VARCHAR(250) , "
            + BookTable.UPDATE_TIME + " VARCHAR(250) ,"
            + BookTable.LAST_CHECKUPDATETIME + " long ,"
            + BookTable.LAST_CHAPTER_URL1 + " VARCHAR(250) ,"
            + BookTable.LAST_UPDATESUCESS_TIME + " long ,"
            + BookTable.CHAPTERS_UPDATE_INDEX + " INTEGER default 0 ,"
            + BookTable.LIST_VERSION + " INTEGER default 0 ,"
            + BookTable.C_VERSION + " INTEGER default 0"
            + ");";
    // 书签表
    private static final String SQL_CREATE_BOOK_MARK = "create table if not exists " + BookMarkTable.TABLE_NAME + "("
            + BookMarkTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + BookMarkTable.GID + " INTEGER DEFAULT -1, "
            + BookMarkTable.NID + " INTEGER DEFAULT -1, " + BookMarkTable.SEQUENCE + " INTEGER DEFAULT -1, "
            + BookMarkTable.OFFSET + " INTEGER , " + BookMarkTable.BOOK_URL + " VARCHAR , " + BookMarkTable.SORT
            + " INTEGER , " + BookMarkTable.LAST_UPDATETIME + " LONG , " + BookMarkTable.CHAPTER_NAME
            + " VARCHAR(250) , " + BookMarkTable.CHAPTER_CONTENT + " VARCHAR , "
            + BookMarkTable.BOOK_ID + " VARCHAR(250) , "
            + BookMarkTable.BOOK_SOURCE_ID + " VARCHAR(250) , "
            + BookMarkTable.PARAMETER + " VARCHAR(250) , "
            + BookMarkTable.EXTRA_PARAMETER + " VARCHAR(250) "
            + ");";
    // 浏览历史表
    private static final String SQL_CREATE_HISTORY = "create table IF NOT EXISTS " + HistoryInforTable.TABLE_NAME + "("
            + HistoryInforTable.NAME + " VARCHAR(250) , "
            + HistoryInforTable.BOOK_ID + " VARCHAR(250) PRIMARY KEY , "
            + HistoryInforTable.BOOK_SOURCE_ID + " VARCHAR(250) , "
            + HistoryInforTable.CATEGORY + " VARCHAR(250) , "
            + HistoryInforTable.AUTHOR + " VARCHAR(250) , "
            + HistoryInforTable.IMG_URL + " VARCHAR(250) , "
            + HistoryInforTable.CHAPTER_COUNT + " INTEGER , "
            + HistoryInforTable.STATUS + " INTEGER , "
            + HistoryInforTable.SITE + " VARCHAR(250) , "
            + HistoryInforTable.DESC + " VARCHAR(2000) , "
            + HistoryInforTable.LAST_BROW_TIME + " long, "
            + HistoryInforTable.LAST_CHAPTER_NAME + " VARCHAR(250)"
            + ");";
    // 记录书籍修复状态的表
    private static final String SQL_CREATE_BOOK_FIX = "create table IF NOT EXISTS " + FixBookTable.TABLE_NAME + "("
            + FixBookTable.BOOK_ID + " VARCHAR(250) PRIMARY KEY , "
            + FixBookTable.FIX_TYPE + " INTEGER , "
            + FixBookTable.LIST_VERSION + " INTEGER , "
            + FixBookTable.C_VERSION + " INTEGER , "
            + FixBookTable.DIALOG_FLAG + " INTEGER default 0"
            + ");";
    private static Dao mInstance;
    private SqliteHelper mHelper = null;
    private Context mContext;

    private Dao(Context context) {
        this.mHelper = SqliteHelper.getInstance(context);
        mContext = context;
    }

    public synchronized static Dao getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Dao(context);
        }

        return mInstance;
    }

    /**
     * **********************************book表操作********************************
     */

    private static boolean checkColumnExist1(SQLiteDatabase db, String tableName
            , String columnName) {
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

    /**
     * 根据id删除数据
     */
    public void deleteBookMark(ArrayList<Integer> ids) {
        SQLiteDatabase db = null;
        try {
            db = mHelper.getWritableDatabase();
            db.beginTransaction();
            for (int i = 0; i < ids.size(); i++) {
                db.delete(BookMarkTable.TABLE_NAME, BookMarkTable.ID + " =? ",
                        new String[]{String.valueOf(ids.get(i))});
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.endTransaction();
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public void deleteBookMark(String book_id) {
        SQLiteDatabase db = null;
        try {
            db = mHelper.getWritableDatabase();
            db.beginTransaction();

            db.delete(BookMarkTable.TABLE_NAME, BookMarkTable.BOOK_ID + " = " + "'" + book_id + "'", null);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.endTransaction();
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    /**
     * 添加手动书签
     */
    public void insertBookMark(Bookmark bookMark) {
        SQLiteDatabase db = null;
        try {
            db = mHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(BookMarkTable.SEQUENCE, bookMark.sequence);
            cv.put(BookMarkTable.OFFSET, bookMark.offset);
            cv.put(BookMarkTable.BOOK_URL, bookMark.book_url);
            cv.put(BookMarkTable.SORT, bookMark.sort);
            cv.put(BookMarkTable.LAST_UPDATETIME, bookMark.last_time);
            cv.put(BookMarkTable.CHAPTER_NAME, bookMark.chapter_name);
            cv.put(BookMarkTable.CHAPTER_CONTENT, bookMark.chapter_content);
            cv.put(BookMarkTable.BOOK_ID, bookMark.book_id);
            cv.put(BookMarkTable.BOOK_SOURCE_ID, bookMark.book_source_id);
            cv.put(BookMarkTable.PARAMETER, bookMark.parameter);
            cv.put(BookMarkTable.EXTRA_PARAMETER, bookMark.extra_parameter);
            db.insert(BookMarkTable.TABLE_NAME, null, cv);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

            }
        }
    }

    /**
     * 这个方法不太好 返回当前书签是否已到存在了,这里我觉得在这里判断不如用上面的取得gid下全部的bookMark对象再依次判断
     * 现在写在了bookHelper类里面
     */
    public boolean isBookMarkExist(String book_id, int sequence, int offset) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean isExist = false;
        try {
            db = mHelper.getReadableDatabase();
            cursor = db.query(BookMarkTable.TABLE_NAME, null, BookMarkTable.BOOK_ID + " = " + "'" + book_id + "'" + " and "
                            + BookMarkTable.SEQUENCE + " = " + sequence + " and " + BookMarkTable.OFFSET + " = " + offset,
                    null, null, null, null);

            isExist = cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

            }
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }

        return isExist;
    }

    /**
     * 根据具体的sequence和offset来删除gid下的一条手动书签
     */
    public void deleteBookMark(String book_id, int sequence, int offset) {
        SQLiteDatabase db = null;
        try {
            db = mHelper.getWritableDatabase();
            db.delete(BookMarkTable.TABLE_NAME, BookMarkTable.BOOK_ID + " = " + "'" + book_id + "'" + " and " + BookMarkTable.SEQUENCE
                    + " = " + sequence + " and " + BookMarkTable.OFFSET + " = " + offset, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据book_id取这本书所有的存在的手动书签
     */
    public ArrayList<Bookmark> getBookMarks(String book_id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<Bookmark> bookMarkList = new ArrayList<Bookmark>();
        try {
            db = mHelper.getReadableDatabase();
            cursor = db.query(BookMarkTable.TABLE_NAME, null, BookMarkTable.BOOK_ID + " =? ",
                    new String[]{String.valueOf(book_id)}, null, null, BookMarkTable.LAST_UPDATETIME + " desc");
            if (cursor.moveToFirst()) {
                do {
                    Bookmark bean = new Bookmark();
                    bean.id = cursor.getInt(cursor.getColumnIndex(BookMarkTable.ID));
                    bean.sequence = cursor.getInt(cursor.getColumnIndex(BookMarkTable.SEQUENCE));
                    bean.offset = cursor.getInt(cursor.getColumnIndex(BookMarkTable.OFFSET));
                    bean.sort = cursor.getInt(cursor.getColumnIndex(BookMarkTable.SORT));
                    bean.last_time = cursor.getLong(cursor.getColumnIndex(BookMarkTable.LAST_UPDATETIME));
                    bean.book_url = cursor.getString(cursor.getColumnIndex(BookMarkTable.BOOK_URL));
                    bean.chapter_name = cursor.getString(cursor.getColumnIndex(BookMarkTable.CHAPTER_NAME));
                    bean.chapter_content = cursor.getString(cursor.getColumnIndex(BookMarkTable.CHAPTER_CONTENT));
                    bean.book_id = cursor.getString(cursor.getColumnIndex(BookMarkTable.BOOK_ID));
                    bean.book_source_id = cursor.getString(cursor.getColumnIndex(BookMarkTable.BOOK_SOURCE_ID));
                    bean.parameter = cursor.getString(cursor.getColumnIndex(BookMarkTable.PARAMETER));
                    bean.extra_parameter = cursor.getString(cursor.getColumnIndex(BookMarkTable.EXTRA_PARAMETER));
                    bookMarkList.add(bean);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }

        return bookMarkList;
    }

    /**
     * 订阅小说操作
     *
     * book
     */

    public boolean insertBook(Book book) {
        SQLiteDatabase db = null;
        long result = -1;
        try {
            db = mHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(BookTable.GID, book.gid);
            cv.put(BookTable.NID, book.nid);
            cv.put(BookTable.NAME, book.name);
            cv.put(BookTable.AUTHOR, book.author);
            cv.put(BookTable.IMG_URL, book.img_url);
            cv.put(BookTable.STATUS, book.status);
            cv.put(BookTable.LAST_UPDATETIME, book.last_updatetime_native);
            cv.put(BookTable.LAST_SORT, book.last_sort);
            cv.put(BookTable.CHAPTER_COUNT, book.chapter_count);
            cv.put(BookTable.LAST_CHAPTER_NAME, book.last_chapter_name);
            cv.put(BookTable.UPDATE, book.update_status);
            cv.put(BookTable.BAD_NID, book.bad_nid);
            cv.put(BookTable.G_SORT, book.gsort);
            cv.put(BookTable.READED, book.readed);
            cv.put(BookTable.SEQUENCE, book.sequence);
            cv.put(BookTable.INSERT_TIME, System.currentTimeMillis());
            cv.put(BookTable.CATEGORY, book.category);
            cv.put(BookTable.BOOK_ID, book.book_id);
            cv.put(BookTable.BOOK_SOURCE_ID, book.book_source_id);
            cv.put(BookTable.LAST_CHAPTER_MD5, book.last_chapter_md5);
            cv.put(BookTable.LAST_CHAPTER_URL, book.last_chapter_url);
            cv.put(BookTable.PARAMETER, book.parameter);
            cv.put(BookTable.EXTRA_PARAMETER, book.extra_parameter);
            cv.put(BookTable.INITIALIZATION_STATUS, book.initialization_status);
            cv.put(BookTable.SITE, book.site);
            cv.put(BookTable.LAST_CHECKUPDATETIME, book.last_checkupdatetime);
            cv.put(BookTable.LAST_CHAPTER_URL1, book.last_chapter_url1);
            cv.put(BookTable.LAST_UPDATESUCESS_TIME, book.last_updateSucessTime);
            cv.put(BookTable.CHAPTERS_UPDATE_INDEX, book.chapters_update_index);
            cv.put(BookTable.LIST_VERSION, -1);
            cv.put(BookTable.C_VERSION, -1);

            result = db.insert(BookTable.TABLE_NAME, null, cv);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }

        return result != -1;
    }

    /**
     * 获取全部书籍信息
     */
    public List<Book> getBooks() {
        return getBooks(false);
    }

    /**
     * 获取有书签的小说列表
     *
     * haveBookmark    是否必须有书签
     */
    public List<Book> getBooks(boolean haveBookmark) {
        SQLiteDatabase db = null;
        Cursor c = null;
        List<Book> list = new CopyOnWriteArrayList<>();
        try {
            String where = null;
            if (haveBookmark) {
                where = BookTable.READED + " == 1";
            }
            db = mHelper.getReadableDatabase();
            c = db.query(BookTable.TABLE_NAME, null, where, null, null, null, BookTable.SEQUENCE_TIME + " desc");
            Book item = null;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                item = new Book();
                getBookFromDB(item, c);
                list.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return list;

    }

    private final void getBookFromDB(Book item, Cursor c) {
        item.gid = c.getInt(BookTable.GID_INDEX);
        item.nid = c.getInt(BookTable.NID_INDEX);
        item.sequence = c.getInt(BookTable.SEQUENCE_INDEX);
        if (item.sequence == -2) {
            item.sequence = -1;
        }
        item.offset = c.getInt(BookTable.OFFSET_INDEX);
        item.chapter_count = c.getInt(BookTable.CHAPTER_COUNT_INDEX);
        item.last_sort = c.getInt(BookTable.LAST_SORT_INDEX);
        item.status = c.getInt(BookTable.STATUS_INDEX);
        item.last_updatetime_native = c.getLong(BookTable.LAST_UPDATETIME_INDEX);
        item.last_chapter_name = c.getString(BookTable.LAST_CHAPTER_NAME_INDEX);
        item.name = c.getString(BookTable.NAME_INDEX);
        item.author = c.getString(BookTable.AUTHOR_INDEX);
        item.img_url = c.getString(BookTable.IMG_URL_INDEX);
        item.update_status = c.getInt(BookTable.UPDATE_INDEX);
        item.bad_nid = c.getString(BookTable.BAD_NID_INDEX);
        item.gsort = c.getInt(BookTable.G_SORT_INDEX);
        if (item.gsort == -1) {
            item.gsort = 0;
        }
        item.sequence_time = c.getLong(BookTable.SEQUENCE_TIME_INDEX);
        item.readed = c.getInt(BookTable.READED_INDEX);
        item.insert_time = c.getLong(BookTable.INSERT_TIME_INDEX);
        item.category = c.getString(BookTable.CATEGORY_INDEX);
        item.book_id = c.getString(BookTable.BOOK_ID_INDEX);
        item.book_source_id = c.getString(BookTable.BOOK_SOURCE_ID_INDEX);
        item.last_chapter_md5 = c.getString(BookTable.LAST_CHAPTER_MD5_INDEX);
        item.last_chapter_url = c.getString(BookTable.LAST_CHAPTER_URL_INDEX);
        item.parameter = c.getString(BookTable.PARAMETER_INDEX);
        item.extra_parameter = c.getString(BookTable.EXTRA_PARAMETER_INDEX);
        item.initialization_status = c.getInt(BookTable.INITIALIZATION_STATUS_INDEX);
        item.site = c.getString(BookTable.SITE_INDEX);
        item.last_checkupdatetime = c.getLong(BookTable.LAST_CHECKUPDATETIME_INDEX);
        item.last_chapter_url1 = c.getString(BookTable.LAST_CHAPTER_URL1_INDEX);
        item.last_updateSucessTime = c.getLong(BookTable.LAST_UPTADESUCESS_TIME_INDEX);
        item.chapters_update_index = c.getInt(BookTable.CHAPTERS_UPDATEINDEX_INDEX);
        item.list_version = c.getInt(BookTable.LIST_VERSION_INDEX);
        item.c_version = c.getInt(BookTable.C_VERSION_INDEX);
    }

    /**
     * 根据book_id获取单本书籍信息
     *
     * book_id
     */
    public Book getBook(String book_id) {
        SQLiteDatabase db = null;
        Cursor c = null;
        Book item = new Book();
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(BookTable.TABLE_NAME, null, BookTable.BOOK_ID + "=" + "'" + book_id + "'", null, null, null, null);
            if (c.moveToNext()) {
                getBookFromDB(item, c);
            }
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }
        return item;
    }

    /**
     * 根据gid获取单本书籍信息
     *
     * gid
     */
    public Book getBook(int gid) {
        SQLiteDatabase db = null;
        Cursor c = null;
        Book item = new Book();
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(BookTable.TABLE_NAME, null, BookTable.GID + "=" + gid, null, null, null, null);
            if (c.moveToNext()) {
                getBookFromDB(item, c);
            }
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }
        return item;
    }

    /**
     * 根据parameter获取单本书籍信息
     *
     * parameter
     */
    public Book getBook(String parameter, int type) {
        SQLiteDatabase db = null;
        Cursor c = null;
        Book item = new Book();
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(BookTable.TABLE_NAME, null, BookTable.PARAMETER + "=" + parameter, null, null, null, null);
            if (c.moveToNext()) {
                getBookFromDB(item, c);
            }
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }
        return item;
    }

    /**
     * 获取所有书籍gid
     */
    public ArrayList<String> getBooksGid(boolean haveBookmark) {
        SQLiteDatabase db = null;
        Cursor c = null;
        ArrayList<String> list = new ArrayList<>();
        try {
            String where = null;
            if (haveBookmark) {
                where = BookTable.READED + " == 1";
            }
            db = mHelper.getReadableDatabase();
            c = db.query(BookTable.TABLE_NAME, null, where, null, null, null, BookTable.SEQUENCE_TIME + " desc");
            Book item = null;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                item = new Book();
                getBookFromDB(item, c);
                list.add(item.book_id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return list;

    }

    /**
     * 修改book值
     */
    public boolean updateBook(Book book) {
        long result = 0;
        SQLiteDatabase db = null;
        try {
            db = mHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            if (book.gid != 0) {
                cv.put(BookTable.GID, book.gid);
            }
            if (book.nid != 0) {
                cv.put(BookTable.NID, book.nid);
            }
            if (!TextUtils.isEmpty(book.name)) {
                cv.put(BookTable.NAME, book.name);
            }
            if (!TextUtils.isEmpty(book.author)) {
                cv.put(BookTable.AUTHOR, book.author);
            }
            if (!TextUtils.isEmpty(book.img_url)) {
                cv.put(BookTable.IMG_URL, book.img_url);
            }
            if (book.status != -1) {
                cv.put(BookTable.STATUS, book.status);
            }
            if (book.last_updatetime_native != 0) {
                cv.put(BookTable.LAST_UPDATETIME, book.last_updatetime_native);
            }
            if (book.last_sort != 0) {
                cv.put(BookTable.LAST_SORT, book.last_sort);
            }
            if (book.chapter_count != 0) {
                cv.put(BookTable.CHAPTER_COUNT, book.chapter_count);
            }
            if (!TextUtils.isEmpty(book.last_chapter_name)) {
                cv.put(BookTable.LAST_CHAPTER_NAME, book.last_chapter_name);
            }
            if (book.sequence >= -1) {
                cv.put(BookTable.SEQUENCE, book.sequence);
            }
            if (book.offset != -1) {
                cv.put(BookTable.OFFSET, book.offset);
            }
            if (book.update_status != -1) {
                cv.put(BookTable.UPDATE, book.update_status);
            }
            if (!TextUtils.isEmpty(book.bad_nid)) {
                cv.put(BookTable.BAD_NID, book.bad_nid);
            }
            if (book.sequence_time != 0) {
                cv.put(BookTable.SEQUENCE_TIME, book.sequence_time);
            }
            if (book.gsort != -1) {
                cv.put(BookTable.G_SORT, book.gsort);
            }
            if (book.readed != 0) {
                cv.put(BookTable.READED, book.readed);
            }
            if (!TextUtils.isEmpty(book.category)) {
                cv.put(BookTable.CATEGORY, book.category);
            }
            if (!TextUtils.isEmpty(book.book_id)) {
                cv.put(BookTable.BOOK_ID, book.book_id);
            }

            if (!TextUtils.isEmpty(book.book_source_id)) {
                cv.put(BookTable.BOOK_SOURCE_ID, book.book_source_id);
            }

            cv.put(BookTable.LAST_CHAPTER_MD5, book.last_chapter_md5);

            if (!TextUtils.isEmpty(book.last_chapter_url)) {
                cv.put(BookTable.LAST_CHAPTER_URL, book.last_chapter_url);
            }

            if (!TextUtils.isEmpty(book.parameter)) {
                cv.put(BookTable.PARAMETER, book.parameter);
            }

            if (!TextUtils.isEmpty(book.extra_parameter)) {
                cv.put(BookTable.EXTRA_PARAMETER, book.extra_parameter);
            }

//			cv.put(BookTable.INITIALIZATION_STATUS, book.initialization_status);

            if (!TextUtils.isEmpty(book.site)) {
                cv.put(BookTable.SITE, book.site);
            }

            if (book.last_checkupdatetime != 0) {
                cv.put(BookTable.LAST_CHECKUPDATETIME, book.last_checkupdatetime);
            }

            if (!TextUtils.isEmpty(book.last_chapter_url1)) {
                cv.put(BookTable.LAST_CHAPTER_URL1, book.last_chapter_url1);
            }

            if (book.last_updateSucessTime != 0) {
                cv.put(BookTable.LAST_UPDATESUCESS_TIME, book.last_updateSucessTime);
            }

            if (book.chapters_update_index != 0) {
                cv.put(BookTable.CHAPTERS_UPDATE_INDEX, book.chapters_update_index);
            }

            if (book.list_version != -1) {
                cv.put(BookTable.LIST_VERSION, book.list_version);
            }

            if (book.c_version != -1) {
                cv.put(BookTable.C_VERSION, book.c_version);
            }

            result = db.update(BookTable.TABLE_NAME, cv, BookTable.BOOK_ID + " =? ",
                    new String[]{book.book_id});

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return result != 0;
    }

    /****************重构的方法*****************/

    public boolean updateBookNew(Book book) {
        long result = 0;
        SQLiteDatabase db = null;
        try {
            db = mHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            if (book.gid != 0) {
                cv.put(BookTable.GID, book.gid);
            }
            if (book.nid != 0) {
                cv.put(BookTable.NID, book.nid);
            }
            if (!TextUtils.isEmpty(book.name)) {
                cv.put(BookTable.NAME, book.name);
            }
            if (!TextUtils.isEmpty(book.author)) {
                cv.put(BookTable.AUTHOR, book.author);
            }
            if (!TextUtils.isEmpty(book.img_url)) {
                cv.put(BookTable.IMG_URL, book.img_url);
            }
            if (book.status != -1) {
                cv.put(BookTable.STATUS, book.status);
            }
            if (book.last_updatetime_native != 0) {
                cv.put(BookTable.LAST_UPDATETIME, book.last_updatetime_native);
            }
            if (book.last_sort != 0) {
                cv.put(BookTable.LAST_SORT, book.last_sort);
            }
            if (book.chapter_count != 0) {
                cv.put(BookTable.CHAPTER_COUNT, book.chapter_count);
            }
            if (!TextUtils.isEmpty(book.last_chapter_name)) {
                cv.put(BookTable.LAST_CHAPTER_NAME, book.last_chapter_name);
            }
            if (book.sequence >= -1) {
                cv.put(BookTable.SEQUENCE, book.sequence);
            }
            if (book.offset != -1) {
                cv.put(BookTable.OFFSET, book.offset);
            }
            if (book.update_status != -1) {
                cv.put(BookTable.UPDATE, book.update_status);
            }
            if (!TextUtils.isEmpty(book.bad_nid)) {
                cv.put(BookTable.BAD_NID, book.bad_nid);
            }
            if (book.sequence_time != 0) {
                cv.put(BookTable.SEQUENCE_TIME, book.sequence_time);
            }
            if (book.gsort != -1) {
                cv.put(BookTable.G_SORT, book.gsort);
            }
            if (book.readed != 0) {
                cv.put(BookTable.READED, book.readed);
            }
            if (!TextUtils.isEmpty(book.category)) {
                cv.put(BookTable.CATEGORY, book.category);
            }
            if (!TextUtils.isEmpty(book.book_id)) {
                cv.put(BookTable.BOOK_ID, book.book_id);
            }

            if (!TextUtils.isEmpty(book.book_source_id)) {
                cv.put(BookTable.BOOK_SOURCE_ID, book.book_source_id);
            }

            cv.put(BookTable.LAST_CHAPTER_MD5, book.last_chapter_md5);

            if (!TextUtils.isEmpty(book.last_chapter_url)) {
                cv.put(BookTable.LAST_CHAPTER_URL, book.last_chapter_url);
            }

            if (!TextUtils.isEmpty(book.parameter)) {
                cv.put(BookTable.PARAMETER, book.parameter);
            }

            if (!TextUtils.isEmpty(book.extra_parameter)) {
                cv.put(BookTable.EXTRA_PARAMETER, book.extra_parameter);
            }

            cv.put(BookTable.INITIALIZATION_STATUS, book.initialization_status);

            if (!TextUtils.isEmpty(book.site)) {
                cv.put(BookTable.SITE, book.site);
            }

            if (book.last_checkupdatetime != 0) {
                cv.put(BookTable.LAST_CHECKUPDATETIME, book.last_checkupdatetime);
            }

            if (!TextUtils.isEmpty(book.last_chapter_url1)) {
                cv.put(BookTable.LAST_CHAPTER_URL1, book.last_chapter_url1);
            }

            if (book.last_updateSucessTime != 0) {
                cv.put(BookTable.LAST_UPDATESUCESS_TIME, book.last_updateSucessTime);
            }

            if (book.chapters_update_index != 0) {
                cv.put(BookTable.CHAPTERS_UPDATE_INDEX, book.chapters_update_index);
            }

            if (book.list_version != -1) {
                cv.put(BookTable.LIST_VERSION, book.list_version);
            }

            if (book.c_version != -1) {
                cv.put(BookTable.C_VERSION, book.c_version);
            }

            result = db.update(BookTable.TABLE_NAME, cv, BookTable.BOOK_ID + " =? ",
                    new String[]{book.book_id});

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return result != 0;
    }

    /**
     * 修改book值
     */
    public boolean updateBook(Book book, int gid) {
        long result = 0;
        SQLiteDatabase db = null;
        try {
            db = mHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();

            cv.put(BookTable.GID, book.gid);

            if (book.nid != 0) {
                cv.put(BookTable.NID, book.nid);
            }
            if (!TextUtils.isEmpty(book.name)) {
                cv.put(BookTable.NAME, book.name);
            }
            if (!TextUtils.isEmpty(book.author)) {
                cv.put(BookTable.AUTHOR, book.author);
            }
            if (!TextUtils.isEmpty(book.img_url)) {
                cv.put(BookTable.IMG_URL, book.img_url);
            }
            if (book.status != -1) {
                cv.put(BookTable.STATUS, book.status);
            }
            if (book.last_updatetime_native != 0) {
                cv.put(BookTable.LAST_UPDATETIME, book.last_updatetime_native);
            }
            if (book.last_sort != 0) {
                cv.put(BookTable.LAST_SORT, book.last_sort);
            }
            if (book.chapter_count != 0) {
                cv.put(BookTable.CHAPTER_COUNT, book.chapter_count);
            }
            if (!TextUtils.isEmpty(book.last_chapter_name)) {
                cv.put(BookTable.LAST_CHAPTER_NAME, book.last_chapter_name);
            }
            if (book.sequence >= -1) {
                cv.put(BookTable.SEQUENCE, book.sequence);
            }
            if (book.offset != -1) {
                cv.put(BookTable.OFFSET, book.offset);
            }
            if (book.update_status != -1) {
                cv.put(BookTable.UPDATE, book.update_status);
            }
            if (!TextUtils.isEmpty(book.bad_nid)) {
                cv.put(BookTable.BAD_NID, book.bad_nid);
            }
            if (book.sequence_time != 0) {
                cv.put(BookTable.SEQUENCE_TIME, book.sequence_time);
            }
            if (book.gsort != -1) {
                cv.put(BookTable.G_SORT, book.gsort);
            }
            if (book.readed != 0) {
                cv.put(BookTable.READED, book.readed);
            }
            if (!TextUtils.isEmpty(book.category)) {
                cv.put(BookTable.CATEGORY, book.category);
            }

            if (!TextUtils.isEmpty(book.book_id)) {
                cv.put(BookTable.BOOK_ID, book.book_id);
            }

            if (!TextUtils.isEmpty(book.book_source_id)) {
                cv.put(BookTable.BOOK_SOURCE_ID, book.book_source_id);
            }

            if (!TextUtils.isEmpty(book.last_chapter_md5)) {
                cv.put(BookTable.LAST_CHAPTER_MD5, book.last_chapter_md5);
            }

            if (!TextUtils.isEmpty(book.last_chapter_url)) {
                cv.put(BookTable.LAST_CHAPTER_URL, book.last_chapter_url);
            }

            if (!TextUtils.isEmpty(book.parameter)) {
                cv.put(BookTable.PARAMETER, book.parameter);
            }

            if (!TextUtils.isEmpty(book.extra_parameter)) {
                cv.put(BookTable.EXTRA_PARAMETER, book.extra_parameter);
            }

//			cv.put(BookTable.INITIALIZATION_STATUS, book.initialization_status);

            if (!TextUtils.isEmpty(book.site)) {
                cv.put(BookTable.SITE, book.site);
            }

            cv.put(BookTable.LAST_CHECKUPDATETIME, book.last_checkupdatetime);

            if (!TextUtils.isEmpty(book.last_chapter_url1)) {
                cv.put(BookTable.LAST_CHAPTER_URL1, book.last_chapter_url1);
            }

            if (book.last_updateSucessTime != 0) {
                cv.put(BookTable.LAST_UPDATESUCESS_TIME, book.last_updateSucessTime);
            }

            result = db.update(BookTable.TABLE_NAME, cv, BookTable.GID + " =? ",
                    new String[]{String.valueOf(gid)});

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return result != 0;
    }

    /**
     * 修改book值
     */
    public boolean updateBook(Book book, String parameter) {
        long result = 0;
        SQLiteDatabase db = null;
        try {
            db = mHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();

            cv.put(BookTable.GID, book.gid);

            if (book.nid != 0) {
                cv.put(BookTable.NID, book.nid);
            }
            if (!TextUtils.isEmpty(book.name)) {
                cv.put(BookTable.NAME, book.name);
            }
            if (!TextUtils.isEmpty(book.author)) {
                cv.put(BookTable.AUTHOR, book.author);
            }
            if (!TextUtils.isEmpty(book.img_url)) {
                cv.put(BookTable.IMG_URL, book.img_url);
            }
            if (book.status != -1) {
                cv.put(BookTable.STATUS, book.status);
            }
            if (book.last_updatetime_native != 0) {
                cv.put(BookTable.LAST_UPDATETIME, book.last_updatetime_native);
            }
            if (book.last_sort != 0) {
                cv.put(BookTable.LAST_SORT, book.last_sort);
            }
            if (book.chapter_count != 0) {
                cv.put(BookTable.CHAPTER_COUNT, book.chapter_count);
            }
            if (!TextUtils.isEmpty(book.last_chapter_name)) {
                cv.put(BookTable.LAST_CHAPTER_NAME, book.last_chapter_name);
            }
            if (book.sequence >= -1) {
                cv.put(BookTable.SEQUENCE, book.sequence);
            }
            if (book.offset != -1) {
                cv.put(BookTable.OFFSET, book.offset);
            }
            if (book.update_status != -1) {
                cv.put(BookTable.UPDATE, book.update_status);
            }
            if (!TextUtils.isEmpty(book.bad_nid)) {
                cv.put(BookTable.BAD_NID, book.bad_nid);
            }
            if (book.sequence_time != 0) {
                cv.put(BookTable.SEQUENCE_TIME, book.sequence_time);
            }
            if (book.gsort != -1) {
                cv.put(BookTable.G_SORT, book.gsort);
            }
            if (book.readed != 0) {
                cv.put(BookTable.READED, book.readed);
            }
            if (!TextUtils.isEmpty(book.category)) {
                cv.put(BookTable.CATEGORY, book.category);
            }

            cv.put(BookTable.BOOK_ID, book.book_id);

            cv.put(BookTable.BOOK_SOURCE_ID, book.book_source_id);

            if (!TextUtils.isEmpty(book.last_chapter_md5)) {
                cv.put(BookTable.LAST_CHAPTER_MD5, book.last_chapter_md5);
            }

            if (!TextUtils.isEmpty(book.last_chapter_url)) {
                cv.put(BookTable.LAST_CHAPTER_URL, book.last_chapter_url);
            }

            if (!TextUtils.isEmpty(book.parameter)) {
                cv.put(BookTable.PARAMETER, book.parameter);
            }

            if (!TextUtils.isEmpty(book.extra_parameter)) {
                cv.put(BookTable.EXTRA_PARAMETER, book.extra_parameter);
            }

//			cv.put(BookTable.INITIALIZATION_STATUS, book.initialization_status);

            if (!TextUtils.isEmpty(book.site)) {
                cv.put(BookTable.SITE, book.site);
            }

            cv.put(BookTable.LAST_CHECKUPDATETIME, book.last_checkupdatetime);

            if (!TextUtils.isEmpty(book.last_chapter_url1)) {
                cv.put(BookTable.LAST_CHAPTER_URL1, book.last_chapter_url1);
            }

            if (book.last_updateSucessTime != 0) {
                cv.put(BookTable.LAST_UPDATESUCESS_TIME, book.last_updateSucessTime);
            }

            result = db.update(BookTable.TABLE_NAME, cv, BookTable.PARAMETER + " =? ",
                    new String[]{parameter});

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return result != 0;
    }

    /**
     * 根据gid删除订阅书籍
     */
    public int[] deleteSubBook(Integer... gid) {
        int[] delete_ids = new int[gid.length];
        SQLiteDatabase db = null;
        try {
            db = mHelper.getWritableDatabase();
            db.beginTransaction();
            for (int i = 0; i < gid.length; i++) {
                if (db.delete(BookTable.TABLE_NAME, BookTable.GID + " =? ", new String[]{String.valueOf(gid[i])}) > 0) {
                    delete_ids[i] = gid[i];
                }
            }
            db.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.endTransaction();
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return delete_ids;
    }

    /**
     * 根据book_id删除订阅书籍
     */
    public String[] deleteSubBook(String... book_id) {
        String[] delete_ids = new String[book_id.length];
        SQLiteDatabase db = null;
        try {
            db = mHelper.getWritableDatabase();
            db.beginTransaction();
            for (int i = 0; i < book_id.length; i++) {
                if (db.delete(BookTable.TABLE_NAME, BookTable.BOOK_ID + " =? ", new String[]{book_id[i]}) > 0) {
                    delete_ids[i] = book_id[i];
                }
            }
            db.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.endTransaction();
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return delete_ids;
    }

    /**
     * 增加修复状态信息
     */
    public boolean insertBookFix(BookFix bookFix) {
        SQLiteDatabase db = null;
        long result = -1;
        try {
            db = mHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(FixBookTable.BOOK_ID, bookFix.book_id);
            cv.put(FixBookTable.FIX_TYPE, bookFix.fix_type);
            cv.put(FixBookTable.LIST_VERSION, bookFix.list_version);
            cv.put(FixBookTable.C_VERSION, bookFix.c_version);
            cv.put(FixBookTable.DIALOG_FLAG, bookFix.dialog_flag);

            result = db.insert(FixBookTable.TABLE_NAME, null, cv);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }

        return result != -1;
    }

    /**
     * 删除修复状态信息
     */
    public String[] deleteBookFix(String... book_id) {
        String[] delete_ids = new String[book_id.length];
        SQLiteDatabase db = null;
        try {
            db = mHelper.getWritableDatabase();
            db.beginTransaction();
            for (int i = 0; i < book_id.length; i++) {
                if (db.delete(FixBookTable.TABLE_NAME, FixBookTable.BOOK_ID + " =? ", new String[]{book_id[i]}) > 0) {
                    delete_ids[i] = book_id[i];
                }
            }
            db.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.endTransaction();
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return delete_ids;
    }

    /**
     * 查询所有修复状态信息
     */
    public ArrayList<BookFix> getBookFixs() {
        SQLiteDatabase db = null;
        Cursor c = null;
        ArrayList<BookFix> list = new ArrayList<>();
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(FixBookTable.TABLE_NAME, null, null, null, null, null, null);
            BookFix item = null;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                item = new BookFix();
                item.book_id = c.getString(FixBookTable.BOOK_ID_INDEX);
                item.fix_type = c.getInt(FixBookTable.FIX_TYPE_INDEX);
                item.list_version = c.getInt(FixBookTable.LIST_VERSION_INDEX);
                item.c_version = c.getInt(FixBookTable.C_VERSION_INDEX);
                item.dialog_flag = c.getInt(FixBookTable.DIALOG_FLAG_INDEX);
                list.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return list;

    }

    /**
     * 根据book_id获取修复状态信息
     */
    public BookFix getBookFix(String book_id) {
        SQLiteDatabase db = null;
        Cursor c = null;
        BookFix item = new BookFix();
        try {
            db = mHelper.getReadableDatabase();
            c = db.query(FixBookTable.TABLE_NAME, null, FixBookTable.BOOK_ID + "=" + "'" + book_id + "'", null, null, null, null);
            if (c.moveToNext()) {
                item.book_id = c.getString(FixBookTable.BOOK_ID_INDEX);
                item.fix_type = c.getInt(FixBookTable.FIX_TYPE_INDEX);
                item.list_version = c.getInt(FixBookTable.LIST_VERSION_INDEX);
                item.c_version = c.getInt(FixBookTable.C_VERSION_INDEX);
                item.dialog_flag = c.getInt(FixBookTable.DIALOG_FLAG_INDEX);
            }
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }
        return item;
    }

    /**
     * 修改bookFix值
     */
    public boolean updateBookFix(BookFix bookFix) {
        long result = 0;
        SQLiteDatabase db = null;
        try {
            db = mHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();

            if (!TextUtils.isEmpty(bookFix.book_id)) {
                cv.put(FixBookTable.BOOK_ID, bookFix.book_id);
            }
            if (bookFix.fix_type != 0) {
                cv.put(FixBookTable.FIX_TYPE, bookFix.fix_type);
            }
            cv.put(FixBookTable.LIST_VERSION, bookFix.list_version);
            cv.put(FixBookTable.C_VERSION, bookFix.c_version);
            if (bookFix.dialog_flag != 0) {
                cv.put(FixBookTable.DIALOG_FLAG, bookFix.dialog_flag);
            }

            result = db.update(FixBookTable.TABLE_NAME, cv, FixBookTable.BOOK_ID + " =? ",
                    new String[]{bookFix.book_id});

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return result != 0;
    }

    private static class SqliteHelper extends SQLiteOpenHelper {

        public static final String DROP_TEMP_SUBSCRIBE = "drop table if exists temp_A";
        public static final String TEMP_SQL_CREATE_TABLE_SUBSCRIBE = "alter table "
                + BookTable.TABLE_NAME + " rename to temp_A";
        private static SqliteHelper mInstance;

        private SqliteHelper(Context paramContext) {
            super(paramContext, DATABASE_NAME, null, version);
        }

        public synchronized static SqliteHelper getInstance(Context paramContext) {
            if (mInstance == null) {
                mInstance = new SqliteHelper(paramContext);
            }

            return mInstance;
        }

        public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
            paramSQLiteDatabase.execSQL(SQL_CREATE_BOOK);
            paramSQLiteDatabase.execSQL(SQL_CREATE_SITE_PATTERN);
            paramSQLiteDatabase.execSQL(SQL_CREATE_BOOK_MARK);
            paramSQLiteDatabase.execSQL(SQL_CREATE_HISTORY);
            paramSQLiteDatabase.execSQL(SQL_CREATE_BOOK_FIX);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String new_column = null;
            if (oldVersion < 2) {
                new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.UPDATE + " INTEGER";
                db.execSQL(new_column);
            }

            if (oldVersion < 3) {
                new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.BAD_NID + " VARCHAR";
                db.execSQL(new_column);
            }
            if (oldVersion < 5) {
                new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.SEQUENCE_TIME + " long";
                db.execSQL(new_column);
            }
            if (oldVersion < 6) {
                new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.G_SORT + " INTEGER";
                db.execSQL(new_column);
            }
            if (oldVersion < 9) {
                new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.READED + "  INTEGER default 0";
                db.execSQL(new_column);
            }

            if (oldVersion < 10) {
                Cursor cr = null;
                try {
                    db.beginTransaction();
                    db.execSQL(TEMP_SQL_CREATE_TABLE_SUBSCRIBE);
                    db.execSQL(SQL_CREATE_BOOK);
                    cr = db.rawQuery("select * from temp_A", null);
                    while (cr.moveToNext()) {
                        ContentValues values = new ContentValues();
                        values.put(BookTable.GID, cr.getInt(cr.getColumnIndex(BookTable.GID)));
                        values.put(BookTable.NID, cr.getInt(cr.getColumnIndex(BookTable.NID)));
                        values.put(BookTable.NAME, cr.getString(cr.getColumnIndex(BookTable.NAME)));
                        values.put(BookTable.AUTHOR, cr.getString(cr.getColumnIndex(BookTable.AUTHOR)));
                        values.put(BookTable.IMG_URL, cr.getString(cr.getColumnIndex(BookTable.IMG_URL)));
                        values.put(BookTable.CHAPTER_COUNT, cr.getInt(cr.getColumnIndex(BookTable.CHAPTER_COUNT)));
                        values.put(BookTable.STATUS, cr.getInt(cr.getColumnIndex(BookTable.STATUS)));
                        values.put(BookTable.LAST_UPDATETIME, cr.getLong(cr.getColumnIndex(BookTable.LAST_UPDATETIME)));
                        values.put(BookTable.LAST_SORT, cr.getInt(cr.getColumnIndex(BookTable.LAST_SORT)));
                        values.put(BookTable.LAST_CHAPTER_NAME, cr.getString(cr.getColumnIndex(BookTable.LAST_CHAPTER_NAME)));
                        values.put(BookTable.SEQUENCE, cr.getInt(cr.getColumnIndex(BookTable.SEQUENCE)));
                        values.put(BookTable.OFFSET, cr.getInt(cr.getColumnIndex(BookTable.OFFSET)));
                        values.put(BookTable.UPDATE, cr.getInt(cr.getColumnIndex(BookTable.UPDATE)));
                        values.put(BookTable.BAD_NID, cr.getInt(cr.getColumnIndex(BookTable.BAD_NID)));
                        values.put(BookTable.SEQUENCE_TIME, cr.getLong(cr.getColumnIndex(BookTable.SEQUENCE_TIME)));
                        values.put(BookTable.G_SORT, cr.getInt(cr.getColumnIndex(BookTable.G_SORT)));
                        values.put(BookTable.READED, cr.getInt(cr.getColumnIndex(BookTable.READED)));
                        values.put(BookTable.CATEGORY, cr.getInt(cr.getColumnIndex(BookTable.CATEGORY)));

                        db.insert(BookTable.TABLE_NAME, null, values);
                    }

                    db.execSQL(DROP_TEMP_SUBSCRIBE);
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                    db.execSQL("drop table if exists " + BookTable.TABLE_NAME);
                    db.execSQL(SQL_CREATE_BOOK);
                } finally {
                    db.endTransaction();
                    if (cr != null) {
                        cr.close();
                    }
                }
            }
            if (oldVersion < 11) {
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.CATEGORY)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.CATEGORY +
                            " VARCHAR(50)";
                    db.execSQL(new_column);
                }

            }
            // 12添加书签表
            if (oldVersion < 12) {
                db.execSQL(SQL_CREATE_BOOK_MARK);
            }
            // 13 add SPEED_MODE 字段
            if (oldVersion < 13) {
                // bookmark 表 添加 book_id
                if (!checkColumnExist1(db, BookMarkTable.TABLE_NAME, BookMarkTable.BOOK_ID)) {
                    new_column = "alter table " + BookMarkTable.TABLE_NAME + " add " + BookMarkTable.BOOK_ID + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookMarkTable.TABLE_NAME, BookMarkTable.BOOK_SOURCE_ID)) {
                    new_column = "alter table " + BookMarkTable.TABLE_NAME + " add " + BookMarkTable.BOOK_SOURCE_ID + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookMarkTable.TABLE_NAME, BookMarkTable.PARAMETER)) {
                    new_column = "alter table " + BookMarkTable.TABLE_NAME + " add " + BookMarkTable.PARAMETER + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookMarkTable.TABLE_NAME, BookMarkTable.EXTRA_PARAMETER)) {
                    new_column = "alter table " + BookMarkTable.TABLE_NAME + " add " + BookMarkTable.EXTRA_PARAMETER + " VARCHAR(250)";
                    db.execSQL(new_column);
                }

                // book 表
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.SPEED_MODE)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.SPEED_MODE + "  INTEGER default 0";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.BOOK_ID)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.BOOK_ID + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.BOOK_SOURCE_ID)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.BOOK_SOURCE_ID + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.LAST_CHAPTER_MD5)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.LAST_CHAPTER_MD5 + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.LAST_CHAPTER_URL)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.LAST_CHAPTER_URL + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.PARAMETER)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.PARAMETER + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.EXTRA_PARAMETER)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.EXTRA_PARAMETER + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.INITIALIZATION_STATUS)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.INITIALIZATION_STATUS + " INTEGER default 0";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.SITE)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.SITE + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.UPDATE_TIME)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.UPDATE_TIME + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
            }

            if (oldVersion < 14) {
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.LAST_CHECKUPDATETIME)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.LAST_CHECKUPDATETIME + " long";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.LAST_CHAPTER_URL1)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.LAST_CHAPTER_URL1 + " VARCHAR(250)";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.LAST_UPDATESUCESS_TIME)) {
                    long currTime = System.currentTimeMillis();
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.LAST_UPDATESUCESS_TIME + " long default " + currTime;
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.CHAPTERS_UPDATE_INDEX)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.CHAPTERS_UPDATE_INDEX + " INTEGER default 0";
                    db.execSQL(new_column);
                }
            }

            if (oldVersion < 15) {
                db.execSQL(SQL_CREATE_HISTORY);
            }

            if (oldVersion < 16) {
                db.execSQL(SQL_CREATE_BOOK_FIX);
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.LIST_VERSION)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.LIST_VERSION + " INTEGER default 0";
                    db.execSQL(new_column);
                }
                if (!checkColumnExist1(db, BookTable.TABLE_NAME, BookTable.C_VERSION)) {
                    new_column = "alter table " + BookTable.TABLE_NAME + " add " + BookTable.C_VERSION + " INTEGER default 0";
                    db.execSQL(new_column);
                }
            }

        }
    }

}