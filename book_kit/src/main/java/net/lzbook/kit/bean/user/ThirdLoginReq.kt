package net.lzbook.kit.bean.user

import net.lzbook.kit.utils.NotProguard

/**
 * Desc 第三方登录实体
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/8 0008 19:06
 */
@NotProguard
data class ThirdLoginReq(
        var oauthId: String, // 授权id
        var accessToken: String, // 授权token
        var accessTokenSeconds: String, // 授权token有效时间
        var refreshToken: String, // 刷新token
        var refreshTokenSeconds: String,// 刷新token有效时间
        var channel: String, // 登录方式 0-微信 1-QQ
        var name: String, // 用户名
        var sex: String, // 性别
        var avatarUrl: String // 头像 url
) {
    constructor(channel: String) : this("", "", "", "",
            "", channel, "", "", "")

    companion object {
        const val CHANNEL_WX = "0"
        const val CHANNEL_QQ = "1"
    }
}