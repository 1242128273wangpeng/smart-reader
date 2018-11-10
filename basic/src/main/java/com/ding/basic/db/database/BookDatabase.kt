package com.ding.basic.db.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.*
import android.arch.persistence.room.migration.Migration
import android.content.Context
import com.ding.basic.bean.*
import com.ding.basic.db.dao.*

/**
 * Created on 2018/3/13.
 * Created by crazylei.
 * 如果你想升级数据库请按规则书写migration, 并添加调用, 最后不要忘记升级数据库版本
 */
@Database(entities = [Book::class, BookFix::class, Bookmark::class, HistoryInfo::class, SearchRecommendBook.DataBean::class, LoginRespV4::class], version = 5)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun fixBookDao(): BookFixDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun searchDao(): SearchDao
    abstract fun userDao(): UserDao


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
                                    migration4_5
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
//                增加用户user表
                val TABLE_NAME = "user"
                val sql = "CREATE TABLE IF NOT EXISTS `$TABLE_NAME` (`account_id` TEXT NOT NULL, `avatar_url` TEXT, `global_number` INTEGER NOT NULL, `name` TEXT, `sex` TEXT, `phone` TEXT, `platform_info` TEXT, `login_channel` TEXT, `pic` TEXT, `token` TEXT, `book_info_version` INTEGER NOT NULL, `book_browse_version` INTEGER NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`account_id`))"
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
                database.execSQL("ALTER TABLE `book` ADD COLUMN `chapter_defaultCode` INTEGER DEFAULT 0")
            }
        }
    }
}