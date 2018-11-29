package com.ding.basic.db.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.*
import android.arch.persistence.room.migration.Migration
import android.content.Context
import android.database.Cursor
import com.ding.basic.bean.*
import com.ding.basic.db.dao.*

/**
 * Created on 2018/3/13.
 * Created by crazylei.
 * 如果你想升级数据库请按规则书写migration, 并添加调用, 最后不要忘记升级数据库版本
 */
@Database(entities = [Book::class, BookFix::class, Bookmark::class, HistoryInfo::class, SearchRecommendBook.DataBean::class, LoginRespV4::class, WebPageFavorite::class], version = 7)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun fixBookDao(): BookFixDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun searchDao(): SearchDao
    abstract fun userDao(): UserDao
    abstract fun webFavoriteDao(): WebFavoriteDao


    companion object {

        private var bookDatabase: BookDatabase? = null

        fun loadBookDatabase(context: Context): BookDatabase {
            synchronized(BookDatabase::class) {
                if (bookDatabase?.isOpen != true) {
                    bookDatabase = Room.databaseBuilder(context, BookDatabase::class.java, "novel.db")
                            .allowMainThreadQueries()
                            .addMigrations(
                                    migration1_2,
                                    migration2_3,
                                    migration3_4,
                                    migration4_5,
                                    migration5_6,
                                    migration6_7
                            )
                            .build()
                }
                return bookDatabase!!
            }
        }

        private val migration1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `book` ADD COLUMN `uv` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `book` ADD COLUMN `score` REAL NOT NULL DEFAULT 0")
            }
        }

        private val migration2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //增加用户user表
                val sql = "CREATE TABLE IF NOT EXISTS `user` (`account_id` TEXT NOT NULL, `avatar_url` TEXT, `global_number` INTEGER NOT NULL, `name` TEXT, `sex` TEXT, `phone` TEXT, `platform_info` TEXT, `login_channel` TEXT, `pic` TEXT, `token` TEXT, `book_info_version` INTEGER NOT NULL, `book_browse_version` INTEGER NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`account_id`))"
                database.execSQL(sql)

            }
        }

        private val migration3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `book` ADD COLUMN `list_version_fix` INTEGER NOT NULL DEFAULT -1")
                database.execSQL("ALTER TABLE `book` ADD COLUMN `force_fix` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `book` ADD COLUMN `chapter_fix_state` INTEGER DEFAULT 0")
            }
        }

        private val migration4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                updateBookDataBase5(database)
            }
        }

        private val migration5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {

                updateBookDataBase5(database)

                /***
                 * 足迹新增
                 * **/
                if (!checkColumnExist(database, "history_info", "last_chapter_name")) {
                    database.execSQL("ALTER TABLE `history_info` ADD COLUMN `last_chapter_name` TEXT DEFAULT null")
                }

                if (!checkColumnExist(database, "history_info", "last_chapter_update_time")) {
                    database.execSQL("ALTER TABLE `history_info` ADD COLUMN `last_chapter_update_time` INTEGER DEFAULT null")
                }
            }
        }

        private val migration6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 由于chapter表新增comment_count字段, book表中必须新增相应的chapter_comment_count字段
                database.execSQL("ALTER TABLE `book` ADD COLUMN `chapter_comment_count` INTEGER DEFAULT 0")
            }
        }




        private fun updateBookDataBase5(database: SupportSQLiteDatabase) {
            /***
             * 章节内容检测
             * **/
            if (!checkColumnExist(database, "book", "chapter_defaultCode")) {
                database.execSQL("ALTER TABLE `book` ADD COLUMN `chapter_defaultCode` INTEGER DEFAULT 0")
            }

            /***
             * 听书新增
             * **/
            if (!checkColumnExist(database, "book", "can_listen")) {
                database.execSQL("ALTER TABLE `book` ADD COLUMN `can_listen` INTEGER NOT NULL DEFAULT 0")
            }

            /***
             * 本地导入新增
             * **/
            if (!checkColumnExist(database, "book", "file_path")) {
                database.execSQL("ALTER TABLE `book` ADD COLUMN `file_path` TEXT")
            }

            if (!checkColumnExist(database, "book", "chapter_start_position")) {
                database.execSQL("ALTER TABLE `book` ADD COLUMN `chapter_start_position` INTEGER DEFAULT 0")
            }

            if (!checkColumnExist(database, "book", "chapter_end_position")) {
                database.execSQL("ALTER TABLE `book` ADD COLUMN `chapter_end_position` INTEGER DEFAULT 0")
            }

            /***
             * 全网搜新增
             * **/
            database.execSQL("CREATE TABLE IF NOT EXISTS `web_page_favorite` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `web_link` TEXT NOT NULL, `create_time` INTEGER NOT NULL)")
        }

        /***
         * 检查数据库表中的列是否存在
         * **/
        private fun checkColumnExist(dataBase: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
            var result = false
            var cursor: Cursor? = null
            try {
                cursor = dataBase.query("SELECT * FROM $tableName LIMIT 0", null)
                result = cursor != null && cursor.getColumnIndex(columnName) != -1
            } catch (exception: Exception) {
                exception.printStackTrace()
            } finally {
                if (null != cursor && !cursor.isClosed) {
                    cursor.close()
                }
            }

            return result
        }
    }
}