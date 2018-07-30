package net.lzbook.kit.user.bean

import net.lzbook.kit.utils.NotProguard

/**
 * Desc 请描述这个文件
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/10 0010 18:23
 */
@NotProguard
data class AvatarReq(
        val suffix: String,
        val avatar: String
)