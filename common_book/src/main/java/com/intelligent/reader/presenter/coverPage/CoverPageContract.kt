package com.intelligent.reader.presenter.coverPage

import net.lzbook.kit.data.bean.CoverPage

/**
 * Created by zhenXiang on 2017\11\15 0015.
 */

interface CoverPageContract {

    fun showRecommend(recommendBean: ArrayList<Book>) //获取推荐的书
    fun showCoverError()
    fun showRecommendError()
    fun changeDownloadButtonStatus() //改变缓存按钮的文字
    fun setShelfBtnClickable(clickable: Boolean)
    fun successAddIntoShelf(isAddIntoShelf: Boolean) //是否成功加入书架
    fun showLoadingSuccess() //加载成功
    fun showArrow(isQGTitle: Boolean) //是否显示多源的下拉箭头
    fun setCompound()
    fun showCurrentSources(currentSource: String) // 显示来源地址

    fun showCoverDetail(bookVo: CoverPage.BookVoBean) //显示书籍封面页

    fun loadCoverWhenSourceChange() //换源重新加载书籍封面

    fun onStartStatus(isBookSubed: Boolean) //onresume执行时改变状态


}
