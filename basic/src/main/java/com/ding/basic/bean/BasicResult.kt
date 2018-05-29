package com.ding.basic.bean

import com.ding.basic.request.ResultCode
import java.io.Serializable

class BasicResult<T> : Serializable {

    var code: Int = 0

    var data: T? = null

    var msg: String? = null

    fun isAvalable(): Boolean {
        return code == ResultCode.OK && data != null
    }

    override fun toString(): String {
        return "BasicResult(code=$code, data=$data, msg=$msg)"
    }
}