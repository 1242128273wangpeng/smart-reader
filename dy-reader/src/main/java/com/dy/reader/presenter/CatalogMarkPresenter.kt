package com.dy.reader.presenter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter
import com.ding.basic.util.DataCache
import com.dingyue.statistics.DyStatService
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
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.subscribekt
import net.lzbook.kit.utils.toast.ToastUtil
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * Created by xian on 2017/8/17.
 */
class CatalogMarkPresenter(var view: CatalogMark.View?) : CatalogMark.Presenter {

    override fun onClickFixBook(activity: Activity) {
        DyStatService.onEvent(EventPoint.READPAGE_DIRECTORYREPAIR, mapOf("bookid" to ReaderStatus.book.book_id, "chapterid" to ReaderStatus.currentChapter!!.chapter_id))
    }

    private var requestRepositoryFactory = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())

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
            DyStatService.onEvent(EventPoint.READPAGE_BOOKMARK, mapOf("bookid" to ReaderStatus.book.book_id, "chapterid" to ReaderStatus.currentChapter!!.chapter_id))
        }

        Observable.create<List<Bookmark>> { emitter: ObservableEmitter<List<Bookmark>>? ->

            val list = requestRepositoryFactory.getBookMarks(ReaderStatus.book.book_id!!)

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
        val isChapterExist = DataCache.isChapterCached(chapter)

        if (!isChapterExist && NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
                ToastUtil.showToastMessage(R.string.no_net)
            return
        }

//        (activity as ReaderActivity).onJumpChapter(chapter.sequence, 0)
        EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.CHAPTER_REFRESH, Position(ReaderStatus.book.book_id, chapter.sequence, 0 )))
        DyStatService.onEvent(EventPoint.BOOKCATALOG_CATALOGCHAPTER, mapOf("bookid" to ReaderStatus.book.book_id, "chapterid" to chapter.chapter_id))
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

        DyStatService.onEvent(EventPoint.READPAGE_BOOKMARK, mapOf("bookid" to ReaderStatus.book.book_id, "chapterid" to ReaderStatus.currentChapter!!.chapter_id))

    }


    override fun deleteBookMark(activity: Activity, mark: Bookmark) {

        Observable.create<Boolean> { e: ObservableEmitter<Boolean>? ->

            requestRepositoryFactory.deleteBookMark(mark.book_id!!, mark.sequence, mark.offset)

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

            requestRepositoryFactory.deleteBookMark(ReaderStatus.book.book_id)

            e?.onNext(true)
            e?.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribekt(onNext = {
                    loadBookMark(activity, 2)
                })

    }
}