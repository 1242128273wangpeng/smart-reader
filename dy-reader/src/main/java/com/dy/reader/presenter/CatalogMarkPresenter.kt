package com.dy.reader.presenter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.ding.basic.bean.Book
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter
import com.ding.basic.database.helper.BookDataProviderHelper
import com.ding.basic.util.DataCache
import com.dy.reader.R
import com.dy.reader.activity.ReaderActivity
import com.dy.reader.event.EventReaderConfig
import com.dy.reader.page.Position
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.subscribekt
import net.lzbook.kit.utils.toast.ToastUtil
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * Created by xian on 2017/8/17.
 */
class CatalogMarkPresenter(var view: CatalogMark.View?) : CatalogMark.Presenter {

    override fun onClickFixBook(activity: Activity) {
        val data = java.util.HashMap<String, String>()
        data.put("bookid", ReaderStatus.book.book_id!!)
        if (ReaderStatus.currentChapter != null) {
            data.put("chapterid", ReaderStatus.currentChapter!!.chapter_id!!)
        }
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.DIRECTORYREPAIR, data)
    }

    private var mBookDataHelper = BookDataProviderHelper.loadBookDataProviderHelper(BaseBookApplication.getGlobalContext())

    override fun getBook(): Book {
        return ReaderStatus.book
    }

    override fun loadCatalog(reverse: Boolean) {
        view?.setChangeAble(false)
        view?.onLoading()

        if (!ReaderStatus.chapterList.isEmpty()) {
            val list = ReaderStatus.chapterList.clone() as List<Chapter>
            if (reverse) {
                Collections.reverse(list)
                view?.showCatalog(list, 0)
            } else {
                view?.showCatalog(list, ReaderStatus.position.group)
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
            ReaderStatus.book?.let {
                data.put("bookid", it.book_id!!)
            }
            ReaderStatus.chapterId?.let {
                data.put("chapterid", it)
            }
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.BOOKMARK, data)
        }



        Observable.create<List<Bookmark>> { emitter: ObservableEmitter<List<Bookmark>>? ->

            val list = mBookDataHelper.getBookMarks(ReaderStatus.book.book_id!!)

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

        isChapterExist = DataCache.isChapterCached(chapter)

        if (!isChapterExist && NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
                ToastUtil.showToastMessage(R.string.no_net)
            return
        }

//        (activity as ReaderActivity).onJumpChapter(chapter.sequence, 0)
        EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.CHAPTER_REFRESH, Position(ReaderStatus.book.book_id, chapter.sequence, 0 )))


        val data = java.util.HashMap<String, String>()
        data.put("bookid", ReaderStatus.book.book_id)
        data.put("chapterid", chapter.chapter_id!!)
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CATALOG1, data)

    }

    override fun gotoBookMark(activity: Activity, mark: Bookmark) {
        val bundle = Bundle()
        bundle.putInt("sequence", mark.sequence)
        bundle.putInt("offset", mark.offset)
        bundle.putInt("sequence", mark.sequence)
        bundle.putSerializable("book", ReaderStatus.book)
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        if (activity is ReaderActivity) {
            val position = Position(ReaderStatus.book.book_id, mark.sequence)
            position.offset = mark.offset
            EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.FONT_REFRESH, position))
        } else {
            RouterUtil.navigation(activity, RouterConfig.READER_ACTIVITY, bundle, flags)
        }

        val data = java.util.HashMap<String, String>()
        data.put("bookid", ReaderStatus.book.book_id)
        if (ReaderStatus.currentChapter != null) {
            data.put("chapterid", ReaderStatus.currentChapter!!.chapter_id!!)
        }
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.BOOKMARK, data)

    }


    override fun deleteBookMark(activity: Activity, mark: Bookmark) {

        Observable.create<Boolean> { e: ObservableEmitter<Boolean>? ->

            mBookDataHelper.deleteBookMark(mark.book_id!!, mark.sequence, mark.offset)

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

            mBookDataHelper.deleteBookMark(ReaderStatus.book.book_id)

            e?.onNext(true)
            e?.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribekt(onNext = {
                    loadBookMark(activity, 2)
                })

    }
}