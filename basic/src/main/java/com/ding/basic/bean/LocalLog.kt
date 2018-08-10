package com.ding.basic.bean

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.google.gson.Gson
import kotlin.collections.Map

/**
 * Desc 打点日志
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/19 0019 14:45
 */
@Entity(tableName = "LocalLog")
class LocalLog() {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var type: String = MAJORITY

    var time: String = System.currentTimeMillis().toString()

    var contentJson: String? = null

    constructor(type: String, content: Map<String, Any>) : this() {
        this.type = type
        this.contentJson = Gson().toJson(content)
    }

    constructor(id: Int, type: String, content: Map<String, Any>) : this(type, content){
        this.id = id
    }

    init {
    }

    companion object {
        @Ignore
        @JvmStatic
        val MAJORITY = "majority"

        @Ignore
        @JvmStatic
        val MINORITY = "minority"
    }
}