package com.intelligent.reader.presenter.read

import android.support.annotation.IdRes
import com.intelligent.reader.presenter.IBaseView
import com.intelligent.reader.presenter.IPresenter
import com.intelligent.reader.reader.ReaderViewModel
import net.lzbook.kit.data.bean.ReadStatus
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
    }


    interface View : IBaseView<Presenter> {

        fun setBookSource(source: String)
        fun setBookMarkImg(@IdRes id: Int)
        fun updateStatus(readStatus: ReadStatus, mReaderViewModel: ReaderViewModel, bookDaoHelper: BookDaoHelper)
        fun show(flag: Boolean)
    }
}