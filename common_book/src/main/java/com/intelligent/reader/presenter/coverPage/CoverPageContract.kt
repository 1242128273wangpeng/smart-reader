package com.intelligent.reader.presenter.coverPage

import com.ding.basic.bean.Book

/**
 * Created by zhenXiang on 2017\11\15 0015.
 */

interface CoverPageContract {

    /***
     * 加载封面页失败
     * **/
    fun showLoadingFail()

    /***
     * 加载封面页成功
     * **/
    fun showLoadingSuccess()

    /***
     * 显示封面页详情
     * **/
    fun showCoverDetail(book: Book?)

    /***
     * 更改下载按钮状态
     * **/
    fun changeDownloadButtonStatus()

    /***
     * 添加、移除书架状态
     * **/
    fun insertBookShelfResult(result: Boolean)

    /***
     * 更改添加书架按钮状态
     * **/
    fun changeShelfButtonClickable(clickable: Boolean)

    /***
     * 书籍订阅状态改变
     * **/
    fun bookSubscribeState(subscribe: Boolean)

    /***
     * 封面页推荐成功
     * **/
    fun showRecommendSuccess(recommendBean: ArrayList<Book>)

    /***
     * 封面页推荐失败
     * **/
    fun showRecommendFail()
}
