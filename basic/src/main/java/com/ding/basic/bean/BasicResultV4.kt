package com.ding.basic.bean

import com.ding.basic.request.ResultCode
import java.io.Serializable

class BasicResultV4<T> : Serializable {

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