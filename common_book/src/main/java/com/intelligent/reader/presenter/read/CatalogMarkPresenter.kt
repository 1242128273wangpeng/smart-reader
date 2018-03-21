package com.intelligent.reader.presenter.read

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.intelligent.reader.R
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.read.mode.ReadState
import com.quduquxie.network.DataCache
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.Bookmark
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.subscribekt
import net.lzbook.kit.utils.toastShort
import java.util.*

/**
 * Created by xian on 2017/8/17.
 */
class CatalogMarkPresenter : CatalogMark.Presenter {
    override fun onClickFixBook(activity: Activity) {
        val data = java.util.HashMap<String, String>()
        data.put("bookid", ReadState.book.book_id)
        if (ReadState.currentChapter != null) {
            data.put("chapterid", ReadState.currentChapter!!.chapter_id)
        }
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.DIRECTORYREPAIR, data)
    }

    override var view: CatalogMark.View? = null

    private var mBookDaoHelper = BookDaoHelper.getInstance()
    val requestItem: RequestItem

    init {
        requestItem = RequestItem.fromBook(ReadState.book)
    }

    override fun getBook(): Book {
        return ReadState.book
    }

    override fun loadCatalog(reverse: Boolean) {
        view?.setChangeAble(false)
        view?.onLoading()

        if (!ReadState.chapterList.isEmpty()) {
            if (reverse) {
                Collections.reverse(ReadState.chapterList)
                view?.showCatalog(ReadState.chapterList, 0)
            } else {
                view?.showCatalog(ReadState.chapterList, ReadState.sequence)
            }
        } else {
            view?.onNetError()
        }

        view?.setChangeAble(true)

    }

    override fun loadBookMark(activity: Activity, type: Int) {

        view?.setChangeAble(false)
        if (type == 1) {
            val data = java.util.HashMap<String, String>()
            ReadState.book?.let {
                data.put("bookid", it.book_id)
            }
            ReadState.chapterId?.let {
                data.put("chapterid", it)
            }
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.BOOKMARK, data)
        }



        Observable.create<List<Bookmark>> { emitter: ObservableEmitter<List<Bookmark>>? ->

            val list = mBookDaoHelper.getBookMarks(ReadState.book.book_id)

            emitter?.onNext(list)
            emitter?.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribekt(onNext = { ret ->
                    view?.showMark(ret)
                })

        view?.setChangeAble(true)

    }

    override fun gotoChapter(activity: Activity, chapter: Chapter) {
        val isChapterExist: Boolean
        if (requestItem.host == Constants.QG_SOURCE) {
            isChapterExist = DataCache.isChapterExists(chapter.chapter_id, chapter.book_id)
        } else {
            isChapterExist = BookHelper.isChapterExist(chapter)
        }
        if (!isChapterExist && NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            BaseBookApplication.getGlobalContext().toastShort(R.string.no_net, false)
            return
        }

        ReadState.sequence = chapter.sequence
        (activity as ReadingActivity).onJumpChapter(chapter.sequence, 0)


        val data = java.util.HashMap<String, String>()
        data.put("bookid", ReadState.book_id)
        data.put("chapterid", chapter.chapter_id)
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CATALOG1, data)

    }

    override fun gotoBookMark(activity: Activity, mark: Bookmark) {
        var bundle = Bundle()
        bundle.putInt("sequence", mark.sequence)
        bundle.putInt("offset", mark.offset)
        bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
        bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
        bundle.putInt("sequence", mark.sequence)
        bundle.putSerializable("book", ReadState.book)
        val intent = Intent()
        intent.putExtras(bundle)

        intent.setClass(activity, ReadingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (activity is ReadingActivity) {
            activity.onJumpChapter(mark.sequence, mark.offset)
        } else {
            activity.startActivity(intent)
        }

        val data = java.util.HashMap<String, String>()
        data.put("bookid", ReadState.book_id)
        if (ReadState.currentChapter != null) {
            data.put("chapterid", ReadState.currentChapter!!.chapter_id)
        }
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.BOOKMARK, data)

    }


    override fun deleteBookMark(activity: Activity, mark: Bookmark) {

        Observable.create<Boolean> { e: ObservableEmitter<Boolean>? ->

            mBookDaoHelper.deleteBookMark(mark.book_id, mark.sequence, mark.offset, 0)

            e?.onNext(true)
            e?.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribekt(onNext = {
                    loadBookMark(activity, 2)
                })
    }

    override fun deleteAllBookMark(activity: Activity) {
        Observable.create<Boolean> { e: ObservableEmitter<Boolean>? ->

            mBookDaoHelper.deleteBookMark(ReadState.book_id)

            e?.onNext(true)
            e?.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribekt(onNext = {
                    loadBookMark(activity, 2)
                })

    }
}