package net.lzbook.kit.bean.user

import net.lzbook.kit.utils.NotProguard

/**
 * Created by xian on 2017/6/21.
 */
@NotProguard
class LoginResp {
    var state: String = ""
    var nickname: String = ""
    var head_portrait: String = ""
    var sex: String = ""
    var login_token: String = ""
    var uid: String = ""
    var uid_third: String = ""
    var msg: String = ""

    override fun toString(): String {
        return "LoginResp(state='$state', nickname='$nickname', head_portrait='$head_portrait', sex='$sex', login_token='$login_token', uid='$uid', uid_third='$uid_third', msg='$msg')"
    }

}
