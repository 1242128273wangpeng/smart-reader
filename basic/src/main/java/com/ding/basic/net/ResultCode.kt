package com.ding.basic.net

/**
 * Created by xian on 18-3-26.
 */

object ResultCode {
    //本地封装结果
    const val LOCAL_RESULT = 100000

    //请求成功
    const val RESULT_SUCCESS = 20000


    //签名为空
    const val SIGN_EMPTY = 6001
    //公钥为空
    const val PUBLIC_KEY_EMPTY = 6002
    //私钥过期
    const val PRIVATE_KEY_EXPIRE = 6003
    //验签不通过
    const val SIGN_INVALID = 6004
}