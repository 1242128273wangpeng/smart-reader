package com.dy.media

import com.dycm_adsdk.callback.ResultCode

/**
 * Desc 广告回调 返回值
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/7 14:40
 */
object MediaCode {

    const val MEDIA_DISABLE = 1111
    const val MEDIA_SUCCESS = 1004
    const val MEDIA_FAILED = 1005
    const val MEDIA_DISMISS = 1012

    fun getCode(stateCode: Int) = ResultCode.parser(stateCode).code

}