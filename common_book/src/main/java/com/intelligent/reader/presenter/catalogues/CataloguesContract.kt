package com.intelligent.reader.presenter.catalogues

import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter

interface CataloguesContract {
    fun requestCatalogSuccess(chapterList: ArrayList<Chapter>) //请求目录页成功
    fun requestCatalogError() //请求目录页失败
    fun notifyDataChange(isCatalog: Boolean, bookmarkList: ArrayList<Bookmark>) //更新标签状态
    fun deleteBookmarks(deleteList: ArrayList<Int>) //删除标签
    fun handOverLay()
    fun changeDownloadButtonStatus() //改变缓存按钮的文字
    fun successAddIntoShelf(isAddIntoShelf: Boolean) //是否成功加入书架
}
