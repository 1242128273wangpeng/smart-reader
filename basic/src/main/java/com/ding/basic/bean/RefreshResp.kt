package com.ding.basic.bean

import com.ding.basic.util.NotProguard
import java.io.Serializable

@NotProguard
class RefreshResp: Serializable {
    var state: String? = null
    var uid: String? = null
    var uid_third: String? = null
    var login_token: String? = null
    var msg: String? = null

    override fun toString(): String {
        return "RefreshResp(state=$state, uid=$uid, uid_third=$uid_third, login_token=$login_token, msg=$msg)"
    }


}