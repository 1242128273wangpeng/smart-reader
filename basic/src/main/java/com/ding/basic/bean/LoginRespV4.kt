package com.ding.basic.bean

import android.arch.persistence.room.Entity

/**
 * Date: 2018/7/27 16:51
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 登录接口返回数据，用户信息， v4 接口
 */
@Entity(tableName = "user")
class LoginRespV4 {
     var account_id: String? = null

     var avatar_url: String? = null

     var global_number: String? = null

     var name: String? = null

     var gender: String? = null

     var phone_number: String? = null

     var link_channel: String? = null

     var login_channel: String? = null

     var pic: String? = null

     var token: String? = null

     var bookInfoVersion: Int = 0

     var bookBrowseVersion: Int = 0

     var active: Int = 0

     var is_new: Int = 0

    override fun toString(): String {
        return "LoginRespV4(account_id=$account_id, avatar_url=$avatar_url, global_number=$global_number, name=$name, gender=$gender, phone_number=$phone_number, link_channel=$link_channel, login_channel=$login_channel, pic=$pic, token=$token, bookInfoVersion=$bookInfoVersion, bookBrowseVersion=$bookBrowseVersion, active=$active, is_new=$is_new)"
    }


}