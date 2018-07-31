package com.ding.basic.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.ding.basic.bean.LoginRespV4

/**
 * Date: 2018/7/30 20:06
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc:
 */
@Dao
interface UserDao : BaseDao<LoginRespV4> {


    @Query("SELECT * FROM user")
    fun queryUserInfo(): LoginRespV4

    @Query("DELETE FROM user")
    fun deleteUsers()


}