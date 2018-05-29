package com.ding.basic.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.ding.basic.bean.LocalLog

/**
 * Desc 打点日志 Dao
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/19 0019 14:59
 */
@Dao
interface LocalLogDao : BaseDao<LocalLog> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(log: LocalLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(tList: List<LocalLog>)

    @Query("SELECT * FROM LocalLog")
    fun queryAll(): List<LocalLog>

    @Query("SELECT * FROM LocalLog WHERE type = :type")
    fun query(type: String): List<LocalLog>

    @Query("DELETE FROM LocalLog WHERE time <= :time")
    fun deleteOutOfDate(time: Long)

    @Query("SELECT COUNT(*) FROM LocalLog")
    fun getNumberOfRows(): Int

}