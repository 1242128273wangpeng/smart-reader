package com.ding.basic.bean

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull
import com.ding.basic.database.migration.FieldMigration
import com.google.gson.annotations.SerializedName

/**
 * Date: 2018/7/27 16:51
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 登录接口返回数据，用户信息， v4 接口
 */
@Entity(tableName = "user")
class LoginRespV4 {
    @PrimaryKey
    @ColumnInfo(name = "account_id")
    var account_id: String = ""

    @ColumnInfo(name = "avatar_url")
    var avatar_url: String? = null

    @ColumnInfo(name = "global_number")
    var global_number: String? = null

    @ColumnInfo(name = "name")
    var name: String? = null

    @ColumnInfo(name = "sex")
    var gender: String? = null

    @ColumnInfo(name = "phone")
    var phone_number: String? = null

    @ColumnInfo(name = "platform_info")
    var link_channel: String? = null

    @ColumnInfo(name = "login_channel")
    var login_channel: String? = null

    @ColumnInfo(name = "pic")
    var pic: String? = null

    @ColumnInfo(name = "token")
    var token: String? = null

    @ColumnInfo(name = "book_info_version")
    var bookInfoVersion: Int = 0

    @ColumnInfo(name = "book_browse_version")
    var bookBrowseVersion: Int = 0

    @ColumnInfo(name = "active")
    var active: Int = 0

    @FieldMigration(oldName = "is_new")
    @ColumnInfo(name = "new")//Room 框架中字段不能以is开头
    @SerializedName("is_new")
    var new: Int = 0

    override fun toString(): String {
        return "LoginRespV4(account_id=$account_id, avatar_url=$avatar_url, global_number=$global_number, name=$name, gender=$gender, phone_number=$phone_number, link_channel=$link_channel, login_channel=$login_channel, pic=$pic, token=$token, bookInfoVersion=$bookInfoVersion, bookBrowseVersion=$bookBrowseVersion, active=$active, is_new=$new)"
    }


}