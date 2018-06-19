package com.ding.basic.bean

import com.ding.basic.request.ResultCode
import java.io.Serializable

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/18 16:16
 */
class CommonResult<T>: Serializable {

    var respCode: Int = 0

    var data: T? = null

    var message: String? = null

    fun checkResultAvailable(): Boolean {
        return respCode == ResultCode.RESULT_SUCCESS && data != null
    }

    fun checkPrivateKeyExpire(): Boolean {
        return respCode == ResultCode.PRIVATE_KEY_EXPIRE
    }

    override fun toString(): String {
        return "BasicResult(code=$respCode, data=$data, msg=$message)"
    }
}