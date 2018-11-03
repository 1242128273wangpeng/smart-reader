package com.dingyue.statistics.dao

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Update

/**
 * Desc Dao 的父类，已经实现了增删改等方法，新 Dao 继承之即可
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/19 0007 14:34
 */
interface BaseDao<in T> {

    @Insert
    fun insert(t: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(t: T)


    @JvmSuppressWildcards
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(tList: List<T>)

    @Insert
    @JvmSuppressWildcards
    fun insert(tList: List<T>)

    @Delete
    fun delete(t: T)

    @Delete
    fun delete(vararg t: T)

    @Delete
    @JvmSuppressWildcards
    fun delete(tList: List<T>)

    @Update
    fun update(t: T)

    @Update
    @JvmSuppressWildcards
    fun update(tList: List<T>)
}