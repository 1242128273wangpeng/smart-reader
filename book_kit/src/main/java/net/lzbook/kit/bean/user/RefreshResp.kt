package net.lzbook.kit.bean.user

import net.lzbook.kit.utils.NotProguard

/**
 * Created by xian on 2017/6/28.
 */
@NotProguard
class RefreshResp {
    var state: String? = null
    var uid: String? = null
    var uid_third: String? = null
    var login_token: String? = null
    var msg: String? = null

    override fun toString(): String {
        return "RefreshResp(state=$state, uid=$uid, uid_third=$uid_third, login_token=$login_token, msg=$msg)"
    }


}