package com.dingyue.searchbook.interfaces


/**
 * Desc 提供给Presenter层，
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 23:04
 */
interface OnResultListener<in T> {

    fun onSuccess(result: T)
    fun onFail(){}
}