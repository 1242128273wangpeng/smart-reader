package com.intelligent.reader.presenter.read

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.intelligent.reader.R
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.net.NetOwnBook
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.reader.ReaderViewModel
import com.quduquxie.network.DataCache
import com.quduquxie.network.DataService
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.*
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.request.RequestExecutorDefault
import net.lzbook.kit.utils.*
import java.lang.Exception
import java.util.*

/**
 * Created by xian on 2017/8/17.
 */
class CatalogMarkPresenter(val readStatus: ReadStatus, val dataFactory: ReaderViewModel) : CatalogMark.Presenter {
    override fun onClickFixBook(activity: Activity) {
        val data = java.util.HashMap<String, String>()
        data.put("bookid", readStatus.book_id)
        if (dataFactory != null && dataFactory.currentChapter != null) {
            data.put("chapterid", dataFactory!!.currentChapter!!.chapter_id)
        }
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.DIRECTORYREPAIR, data)
    }

    override var view: CatalogMark.View? = null

    private var mBookDaoHelper = BookDaoHelper.getInstance()
    val requestItem: RequestItem

    init {
        requestItem = RequestItem.fromBook(readStatus.book)
    }

    override fun getBook(): Book {
        return readStatus.book
    }

    override fun loadCatalog(reverse: Boolean) {
        view?.setChangeAble(false)
        view?.onLoading()

        Observable.create<List<Chapter>> { emitter: ObservableEmitter<List<Chapter>>? ->

            val chapterDao = BookChapterDao(BaseBookApplication.getGlobalContext(), readStatus.book.book_id)
            val chapterList = chapterDao.queryBookChapter()
            if (chapterList != null && chapterList.size > 0) {
                emitter?.onNext(chapterList)
                emitter?.onComplete()
            } else {
                if (Constants.SG_SOURCE.equals(requestItem.host)) {
                    emitter?.onError(Exception("error"))
                } else {
                    if (Constants.QG_SOURCE.equals(requestItem.host)) {
                        var chapters: ArrayList<com.quduquxie.bean.Chapter>? = null
                        try {
                            val udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext())
                            chapters = DataService.getChapterList(RequestExecutorDefault.mContext, requestItem.book_id, 1, Integer.MAX_VALUE - 1, udid)
                            val list = BeanParser.buildOWNChapterList(chapters, 0, chapters!!.size)
                            if (list != null && list.size > 0) {
                                emitter?.onNext(list)
                                emitter?.onComplete()
                            } else {
                                emitter?.onError(Exception("error"))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            emitter?.onError(e)
                        }

                    } else {
                        NetOwnBook.requestOwnCatalogList(readStatus.book).subscribekt(
                                onNext = { t ->
                                    emitter?.onNext(t)
                                    emitter?.onComplete()
                                },
                                onError = { err ->
                                    emitter?.onError(err)
                                }
                        )
                    }
                }
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribekt(
                onNext = { ret ->
                    if (!ret.isEmpty()) {
                        if (reverse) {
                            Collections.reverse(ret)
                            view?.showCatalog(ret, 0)
                        } else {
                            view?.showCatalog(ret, readStatus.sequence)
                        }
                    } else {
                        view?.onNetError()
                    }

                    view?.setChangeAble(true)
                }, onError = { e ->
            e.printStackTrace()
            view?.onNetError()

            view?.setChangeAble(true)
        })

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

            val list = mBookDaoHelper.getBookMarks(readStatus.book_id)

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
            isChapterExist = BookHelper.isChapterExist(chapter.sequence, readStatus.book_id)
        }
        if (!isChapterExist && NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            BaseBookApplication.getGlobalContext().toastShort(R.string.no_net)
            return
        }

        readStatus.sequence = chapter.sequence
        (activity as ReadingActivity).onJumpChapter(chapter.sequence, 0)


        val data = java.util.HashMap<String, String>()
        data.put("bookid", readStatus.book_id)
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
        bundle.putSerializable("book", readStatus.book)
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
        data.put("bookid", readStatus.book_id)
        if (dataFactory != null && dataFactory.currentChapter != null) {
            data.put("chapterid", dataFactory!!.currentChapter!!.chapter_id)
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

            mBookDaoHelper.deleteBookMark(readStatus.book_id)

            e?.onNext(true)
            e?.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribekt(onNext = {
                    loadBookMark(activity, 2)
                })

    }
}