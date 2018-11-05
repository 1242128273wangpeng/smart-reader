package com.ding.basic.dao

import android.arch.persistence.room.*
import com.ding.basic.bean.HistoryInfo

/**
 * Created by yuchao on 2018/3/16 0016.
 */
@Dao
interface HistoryDao: BaseDao<HistoryInfo> {

    /************************** 增 ****************************/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHistoryInfo(historyInfo: HistoryInfo): Long

    /************************** 删 ****************************/

    @Query("DELETE FROM history_info")
    @Throws(Exception::class)
    fun deleteAllHistory()

    @Query("DELETE FROM history_info WHERE browse_time = (SELECT MIN(browse_time) FROM history_info)")
    @Throws(Exception::class)
    fun deleteSmallTime()

    /************************** 改 ****************************/

    @Update
    fun updateHistory(historyInfo: HistoryInfo): Int

    /************************** 查 ****************************/

    @Query("SELECT * FROM history_info ORDER BY browse_time DESC LIMIT :limtNum OFFSET :startNum")
    fun queryByLimt(startNum: Long, limtNum: Long): List<HistoryInfo>

    @Query("SELECT COUNT(*) FROM history_info")
    fun getCount(): Long

}