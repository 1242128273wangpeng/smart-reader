package com.ding.basic.bean

import com.ding.basic.request.ResultCode
import java.io.Serializable

class /**/BasicResult<T> : Serializable {

    var code: Int = 0

    var data: T? = null

    var msg: String? = null

    fun checkResultAvailable(): Boolean {
        return code == ResultCode.RESULT_SUCCESS && data != null
    }

    fun checkPrivateKeyExpire(): Boolean {
        return code == ResultCode.PRIVATE_KEY_EXPIRE
    }

    override fun toString(): String {
        return "BasicResult(code=$code, data=$data, msg=$msg)"
    }
}