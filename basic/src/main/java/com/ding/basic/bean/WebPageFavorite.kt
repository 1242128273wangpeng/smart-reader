package com.ding.basic.bean

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

/**
 * Desc 网页收藏表
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/30 15:03
 */
@Entity(tableName = "web_page_favorite")
class WebPageFavorite: Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    // 网页标题
    @ColumnInfo(name = "title")
    var webTitle: String = ""

    // 网页链接
    @ColumnInfo(name = "web_link")
    var webLink: String = ""

    // 收藏时间
    @ColumnInfo(name = "create_time")
    var createTime: Long = 0

    @Ignore
    var selected = false

}