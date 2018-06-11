package com.ding.basic.request

/**
 * Created by xian on 18-3-26.
 */

interface ResultCode {
    companion object {
        val OK = 20000

        //请求成功
        val RESULT_SUCCESS = 20000


        //签名为空
        const val SIGN_EMPTY = 6001
        //公钥为空
        const val PUBLIC_KEY_EMPTY = 6002
        //私钥过期
        const val PRIVATE_KEY_EXPIRE = 6003
        //验签不通过
        const val SIGN_INVALID = 6004
    }
}