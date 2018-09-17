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
import com.ding.basic.bean.Chapter
import com.ding.basic.dao.ChapterDao
import com.ding.basic.database.BookDatabase

/**
 * The Room database that contains the Chapter table
 * 如果你想升级数据库请按规则书写migration, 并添加调用, 最后不要忘记升级数据库版本
 */
@Database(entities = arrayOf(Chapter::class), version = 2)
abstract class ChaptersDatabase : RoomDatabase() {

    abstract fun chapterDao(): ChapterDao


    companion object {

        const val CHAPTER_DATABASE = "chapter_"

        fun loadChapterDatabase(context: Context, book_id: String): ChaptersDatabase {
            synchronized(BookDatabase::class) {
                return Room.databaseBuilder(context.applicationContext, ChaptersDatabase::class.java, "$CHAPTER_DATABASE$book_id.db")
                        .allowMainThreadQueries()
                        .addMigrations(migration1_2)
                        .build()
            }
        }

        private val migration1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `chapters` ADD COLUMN `fix_state` INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

}
