package com.dingyue.searchbook.interfaces

import android.os.Bundle


/**
 * Desc：SearchResultModel结果回调
 * 不写在SearchResultView里的原因：
 * view不和model交互，v和p实现result进行中转，m负责调用
 * m处理完结果传给p，p再传给v
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/27 0027 23:03
 */
interface OnSearchResult {

    fun onSearchResult(url:String)

    fun onSearchWordResult(searchWord:String)

    fun onCoverResult(bundle: Bundle)

    fun onAnotherResult(bundle: Bundle)

    fun onTurnReadResult(bundle: Bundle)

    fun onEnterReadResult(bundle: Bundle)



}