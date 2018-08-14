package net.lzbook.kit.user.bean

/**
 * Desc 微信登录token实体
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/9 0009 18:29
 */
data class WXAccess(
        var access_token: String,
        var expires_in: String,
        var refresh_token: String,
        var openid: String,
        var scope: String,
        var unionid: String
)