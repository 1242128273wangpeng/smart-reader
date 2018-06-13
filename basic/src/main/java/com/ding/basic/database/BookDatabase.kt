package com.ding.basic.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.*
import android.arch.persistence.room.migration.Migration
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.ding.basic.bean.*
import com.ding.basic.dao.*
import com.ding.basic.database.table.*


/**
 * Created on 2018/3/13.
 * Created by crazylei.
 */
@Database(entities = [Book::class, BookFix::class, Bookmark::class, HistoryInfo::class, SearchRecommendBook.DataBean::class], version = 2)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    abstract fun fixBookDao() : BookFixDao
    abstract fun bookmarkDao() : BookmarkDao
    abstract fun historyDao() : HistoryDao
    abstract fun searchDao() : SearchDao

    companion object {

        private var bookDatabase: BookDatabase? = null

        fun loadBookDatabase(context: Context): BookDatabase {
            synchronized(BookDatabase::class) {
                if (bookDatabase?.isOpen != true) {
                    bookDatabase = Room.databaseBuilder(context, BookDatabase::class.java, "novel.db")
                            .allowMainThreadQueries()
                            .build()
                }
                return this.bookDatabase!!
            }
        }
    }
}