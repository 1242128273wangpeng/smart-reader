package net.lzbook.kit.user.bean

import java.io.Serializable

/**
 * Desc 微信登录用户信息
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/9 0009 18:29
 */
data class WXSimpleInfo(
        val country: String,
        val unionid: String,
        val province: String,
        val city: String,
        val openid: String,
        val sex: Int,
        val nickname: String,
        val headimgurl: String,
        val privilege: List<String>
): Serializable
