package com.ding.basic.bean

import java.io.Serializable

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/12 14:48
 */
class Access :Serializable {
    var expire: Int = 0
    var publicKey: String? = null
    var privateKey: String? = null
}