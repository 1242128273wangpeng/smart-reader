package com.intelligent.reader.presenter.read

import android.app.Activity
import com.intelligent.reader.presenter.IBaseView
import com.intelligent.reader.presenter.IPresenter
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.Bookmark
import net.lzbook.kit.data.bean.Chapter

/**
 * Created by xian on 2017/8/17.
 */
interface CatalogMark {

    interface Presenter : IPresenter<View> {
        fun getBook(): Book
        fun loadCatalog(reverse: Boolean)
        fun loadBookMark(activity: Activity, type: Int)
        fun gotoChapter(activity: Activity, chapter: Chapter)
        fun gotoBookMark(activity: Activity, mark: Bookmark)
        fun deleteBookMark(activity: Activity, mark: Bookmark)
        fun deleteAllBookMark(activity: Activity)
        fun onClickFixBook(activity: Activity)
    }

    interface View : IBaseView<Presenter> {
        fun showCatalog(chapters: List<Chapter>, sequence: Int)
        fun showMark(marks: List<Bookmark>)
        fun setChangeAble(enable: Boolean)
        fun onLoading()
        fun onNetError()
    }
}