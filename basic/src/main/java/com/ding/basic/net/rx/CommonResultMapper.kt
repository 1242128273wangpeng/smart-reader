package com.ding.basic.net.rx

import com.ding.basic.bean.CommonResult
import io.reactivex.functions.Function

/**
 * Desc 网络请求结果校验并转换
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/9/1 15:44
 */

class CommonResultMapper<T> : Function<CommonResult<T>, T> {
    override fun apply(t: CommonResult<T>): T {
        val data = t.data
        if (t.checkResultAvailable() && data != null) {
            return data
        } else {
            throw Throwable("请求 $t 失败")
        }
    }
}