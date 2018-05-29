package com.ding.basic.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.ding.basic.bean.LocalLog
import com.ding.basic.dao.LocalLogDao

/**
 * Desc 打点日志 database
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/19 0019 15:07
 */
@Database(entities = [LocalLog::class], version = 1)
abstract class LocalLogDataBase : RoomDatabase() {

    abstract fun logDao(): LocalLogDao

    companion object {

        @Volatile
        private var dataBase: LocalLogDataBase? = null

        @JvmStatic
        fun getInstance(context: Context): LocalLogDataBase {
            if (dataBase == null) {
                synchronized(LocalLogDataBase::class.java) {
                    if (dataBase == null) {
                        dataBase = Room
                                .databaseBuilder(
                                        context,
                                        LocalLogDataBase::class.java,
                                        "log.db")
                                .build()
                    }
                }
            }
            return dataBase!!
        }

    }

}