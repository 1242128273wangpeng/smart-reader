package com.intelligent.reader.presenter.coverPage

import android.support.annotation.IdRes
import com.intelligent.reader.presenter.IBaseView
import com.intelligent.reader.presenter.read.ReadOption
import com.intelligent.reader.read.help.IReadDataFactory
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.CoverPage
import net.lzbook.kit.data.bean.ReadStatus
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.data.recommend.CoverRecommendBean

/**
 * Created by zhenXiang on 2017\11\15 0015.
 */

interface CoverPageContract {

    fun showRecommend(recommendBean: ArrayList<Book>) //获取推荐的书
    fun showCoverError()
    fun showRecommendError()
    fun changeDownloadButtonStatus(type: Int) //改变缓存按钮的文字
    fun successAddIntoShelf(isAddIntoShelf: Boolean) //是否成功加入书架
    fun showLoadingSuccess() //加载成功
    fun showArrow(isShow: Boolean, isQGTitle: Boolean) //是否显示多源的下拉箭头
    fun showCurrentSources(currentSource: String) // 显示来源地址

    fun showCoverDetail(bookVo: CoverPage.BookVoBean) //显示书籍封面页

    fun loadCoverWhenSourceChange() //换源重新加载书籍封面

    fun onStartStatus(isBookSubed: Boolean) //onresume执行时改变状态


}
