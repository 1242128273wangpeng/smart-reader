/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.observability.persistence

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import android.content.Context
import android.database.Cursor
import com.ding.basic.bean.Chapter
import com.ding.basic.dao.ChapterDao
import com.ding.basic.database.BookDatabase
import com.ding.basic.database.table.ChapterOldTable
import com.ding.basic.database.table.ChapterTable

/**
 * The Room database that contains the Chapter table
 */
@Database(entities = arrayOf(Chapter::class), version = 1)
abstract class ChaptersDatabase : RoomDatabase() {

    abstract fun chapterDao(): ChapterDao


    companion object {

        const val CHAPTER_DATABASE = "chapter_"

        fun loadChapterDatabase(context: Context, book_id: String): ChaptersDatabase {
            synchronized(BookDatabase::class) {
//                com.orhanobut.logger.Logger.e("loadChapterDatabase, loadChapterDatabasename = $CHAPTER_DATABASE$book_id.db")
                return Room.databaseBuilder(context!!.applicationContext, ChaptersDatabase::class.java, "$CHAPTER_DATABASE$book_id.db")
                        .allowMainThreadQueries()
                        .build()
            }
        }
    }

}
