package com.intelligent.reader.presenter.coverPage

import com.ding.basic.bean.Book
import net.lzbook.kit.data.bean.CoverPage

/**
 * Created by zhenXiang on 2017\11\15 0015.
 */

interface CoverPageContract {

    fun showRecommend(recommendBean: ArrayList<Book>) //获取推荐的书
    fun showCoverError()
    fun showRecommendError()
    fun setShelfBtnClickable(clickable: Boolean)
    fun successAddIntoShelf(isAddIntoShelf: Boolean) //是否成功加入书架
    fun showArrow(isQGTitle: Boolean) //是否显示多源的下拉箭头
    fun setCompound()
    fun showCurrentSources(currentSource: String) // 显示来源地址

    fun loadCoverWhenSourceChange() //换源重新加载书籍封面

    fun onStartStatus(isBookSubed: Boolean) //onresume执行时改变状态



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

}
