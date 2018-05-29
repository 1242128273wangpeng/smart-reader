package com.dy.reader.presenter

import android.app.Activity
import com.ding.basic.bean.Book
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter

/**
 * Created by xian on 2017/8/17.
 */
interface CatalogMark {

    interface Presenter {
        fun getBook(): Book
        fun loadCatalog(reverse: Boolean)
        fun loadBookMark(activity: Activity, type: Int)
        fun gotoChapter(activity: Activity, chapter: Chapter)
        fun gotoBookMark(activity: Activity, mark: Bookmark)
        fun deleteBookMark(activity: Activity, mark: Bookmark)
        fun deleteAllBookMark(activity: Activity)
        fun onClickFixBook(activity: Activity)
    }

    interface View {
        fun showCatalog(chapters: List<Chapter>, sequence: Int)
        fun showMark(marks: List<Bookmark>)
        fun setChangeAble(enable: Boolean)
        fun onLoading()
        fun onNetError()
    }
}