package com.intelligent.reader.reader

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.intelligent.reader.repository.ReaderRepository
import io.reactivex.Observable
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.SourceItem
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.request.DataCache
import net.lzbook.kit.request.RequestFactory
import net.lzbook.kit.request.WriteFileFailException
import net.lzbook.kit.user.bean.RecommendBooksEndResp

/**
 * @desc 阅读模块 本地数据源
 * @author wt
 * @data 2017/11/21 18:10
 */
class ReaderLocalRepository(context: Context) : ReaderRepository {

    var mContext = context
    var mBookDaoHelper: BookDaoHelper = BookDaoHelper.getInstance()
    val CACHE_EXIST = "isChapterExists"

    companion object {
        fun getInstance() = RepositoryHolder.INSTANCE
    }

    private object RepositoryHolder {
        @SuppressLint("StaticFieldLeak")
        val INSTANCE = ReaderLocalRepository(BaseBookApplication.getGlobalContext())
    }

    override fun requestSingleChapter(host: String, chapter: Chapter): Observable<Chapter> {
        // 青果缓存
        if (RequestFactory.RequestHost.QG.requestHost == host) {
            return Observable.create({
                chapter.content = com.quduquxie.network.DataCache.getChapterFromCache(chapter.chapter_id, chapter.book_id)
                chapter.isSuccess = true
                it.onNext(chapter)
                it.onComplete()
            })
            // 智能缓存
        } else {
            return Observable.create({
                chapter.content = net.lzbook.kit.request.DataCache.getChapterFromCache(chapter)
                chapter.isSuccess = true
                it.onNext(chapter)
                it.onComplete()
            })
        }
    }

    override fun updateBookCurrentChapter(bookId: String, retChapter: Chapter?, sequence: Int) {
        retChapter?.let {
            BookChapterDao(mContext, bookId).updateBookCurrentChapter(retChapter, retChapter.sequence)
        }
    }

    override fun getChapterIdByChapterId(bookId: String, chapter_id: String?): Int {
        return BookChapterDao(mContext, bookId).getChapterIdByChapterId(chapter_id)
    }

    override fun changeChargeBookState(bookId: String, chapterIndex: Int, i: Int) {
        BookChapterDao(mContext, bookId).changeChargeBookState(chapterIndex, i)
    }

    override fun writeChapterCache(chapter: Chapter?, book: Book) {
        chapter?.let {
            if (mBookDaoHelper.isBookSubed(chapter.book_id) && !DataCache.isChapterExists(chapter)) {

                var content = chapter.content
                if (TextUtils.isEmpty(content)) {
                    content = "null"
                }

                val write_success = DataCache.saveChapter(content, chapter)

                if (!write_success) {
                    throw WriteFileFailException()
                }
            }
        }
    }

    override fun isChapterCacheExist(host: String?, chapter: Chapter?): Boolean = false
    override fun getBookEndRecommendBook(recommanded: String, bookId: String): Observable<RecommendBooksEndResp> = Observable.create(null)
    override fun getBookSource(bookId: String): Observable<SourceItem> = Observable.create(null)
    //    override fun isNeedDownContent(chapter: Chapter, downloadFlag: Boolean): Boolean = false
//    override fun batchChapter(dex: Int, downloadFlag: Boolean, chapterMap: MutableMap<String, Chapter>?) = Unit

//    override fun paySingleChapter(sourceId: String?, chapterId: String?, chapterName: String?, uid: String?): Observable<SingleChapterBean> = Observable.create(null)

}