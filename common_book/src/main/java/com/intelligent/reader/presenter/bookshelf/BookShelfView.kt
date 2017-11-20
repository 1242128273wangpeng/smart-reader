package com.intelligent.reader.presenter.bookshelf

import com.dingyueads.sdk.Native.YQNativeAdInfo
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.BookUpdate

/**
 * Created by qiantao on 2017/11/14 0014
 */
interface BookShelfView {

    fun onBookListQuery(bookList: ArrayList<Book>)

    fun onBookShelfAdHandle()

    fun onBookDelete()

    fun hideBannerAd()

    fun showBannerAd(adInfo: YQNativeAdInfo)

    fun onSuccessUpdateHandle(updateCount: Int = 0, firstBook: BookUpdate? = null)

}