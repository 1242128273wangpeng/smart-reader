package com.intelligent.reader.presenter.read

import android.support.annotation.IdRes
import com.intelligent.reader.presenter.IBaseView
import com.intelligent.reader.presenter.IPresenter
import com.intelligent.reader.reader.ReaderViewModel
import net.lzbook.kit.data.db.BookDaoHelper

/**
 * Created by xian on 2017/8/17.
 */
interface ReadOption {
    interface Presenter : IPresenter<View> {

        fun cache()
        fun getCacheState(): Int
        fun changeSource()
        fun bookMark(): Int
        fun bookInfo()
        fun back()
        fun showMore()

        fun feedback()

        fun updateStatus()

        fun openWeb()
        fun dismissLoadingPage()
    }


    interface View : IBaseView<Presenter> {

        fun setBookSource(source: String)
        fun setBookMarkImg(@IdRes id: Int)
        fun updateStatus(bookDaoHelper: BookDaoHelper)
        fun show(flag: Boolean)
    }
}