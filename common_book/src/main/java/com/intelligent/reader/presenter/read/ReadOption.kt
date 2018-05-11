package com.intelligent.reader.presenter.read

import android.support.annotation.IdRes
import com.dingyue.contract.IBaseView
import com.dingyue.contract.IPresenter
import net.lzbook.kit.data.db.BookDaoHelper

/**
 * Created by xian on 2017/8/17.
 */
interface ReadOption {
    interface Presenter : IPresenter<View> {

        fun cache()
        fun changeSource()
        fun bookMark(): Int
        fun bookInfo()
        fun back()
        fun showMore()

        fun updateStatus()

        fun openWeb()
        fun dismissLoadingPage()
        fun feedback()
    }


    interface View : IBaseView<Presenter> {

        fun setBookSource(source: String)
        fun setBookMarkImg(@IdRes id: Int)
        fun updateStatus( bookDaoHelper: BookDaoHelper)
        fun show(flag: Boolean)
    }
}