package com.intelligent.reader.reader

import android.content.Context
import android.text.TextUtils
import com.intelligent.reader.repository.ReaderRepository
import io.reactivex.Observable
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.SourceItem
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.purchase.SingleChapterBean
import net.lzbook.kit.request.DataCache
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
        val INSTANCE = ReaderLocalRepository(BaseBookApplication.getGlobalContext())
    }

    override fun requestSingleChapter(host: String?, chapter: Chapter?): Observable<Chapter> {
        return Observable.create({
            chapter?.content = com.quduquxie.network.DataCache.getChapterFromCache(chapter?.chapter_id, chapter?.book_id)
            chapter?.isSuccess = true
            it.onNext(chapter!!)
        })
    }

    override fun updateBookCurrentChapter(bookId: String, retChapter: Chapter?, sequence: Int) {
        BookChapterDao(mContext, bookId).updateBookCurrentChapter(retChapter, retChapter!!.sequence)
    }

    override fun getChapterIdByChapterId(bookId: String, chapter_id: String?): Int {
        return BookChapterDao(mContext, bookId).getChapterIdByChapterId(chapter_id)
    }

    override fun changeChargeBookState(bookId: String, chapterIndex: Int, i: Int) {
        BookChapterDao(mContext, bookId).changeChargeBookState(chapterIndex, i)
    }

    override fun writeChapterCache(chapter: Chapter?, downloadFlag: Boolean) {
        if (chapter != null && mBookDaoHelper.isBookSubed(chapter.book_id)) {
            var content = chapter.content
            if (TextUtils.isEmpty(content)) {
                content = "null"
            }
            val write_success: Boolean
            if (downloadFlag && content == CACHE_EXIST) {
                write_success = true
            } else {
                write_success = DataCache.saveChapter(content, chapter.sequence, chapter.book_id)
            }

            if (downloadFlag && !write_success) {
                throw WriteFileFailException()
            }
        }
    }

    //空实现
    override fun getBookEndRecommendBook(recommanded: String, bookId: String): Observable<RecommendBooksEndResp> = Observable.create(null)

    override fun getBookSource(bookId: String): Observable<SourceItem> = Observable.create(null)
    override fun isNeedDownContent(chapter: Chapter, downloadFlag: Boolean): Boolean = false
    override fun batchChapter(dex: Int, downloadFlag: Boolean, chapterMap: MutableMap<String, Chapter>?) = Unit
    override fun paySingleChapter(sourceId: String?, chapterId: String?, chapterName: String?, uid: String?): Observable<SingleChapterBean> = Observable.create(null)

}