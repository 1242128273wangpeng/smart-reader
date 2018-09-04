package com.ding.basic.bean.push

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

/**
 * Desc push 用户标签
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/9/3 10:47
 */
data class PushInfo(
        var tags: ArrayList<String>? = null,
        var updateMillSecs: Long = 0L, //上次更新时间
        var isFromCache: Boolean = false
) : Serializable