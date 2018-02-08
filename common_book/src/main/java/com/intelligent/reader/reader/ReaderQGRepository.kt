package com.intelligent.reader.reader

import com.intelligent.reader.repository.ReaderRepository
import com.quduquxie.network.DataService
import io.reactivex.Observable
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.SourceItem
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.user.bean.RecommendBooksEndResp
import net.lzbook.kit.utils.BeanParser
import net.lzbook.kit.utils.OpenUDID

/**
 * @desc 阅读模块 青果数据源
 * @author wt
 * @data 2017/11/21 18:10
 */
class ReaderQGRepository private constructor() : ReaderRepository {

    companion object {
        fun getInstance() = RepositoryHolder.INSTANCE
    }

    private object RepositoryHolder {
        val INSTANCE = ReaderQGRepository()
    }

    override fun requestSingleChapter(host: String?, chapter: Chapter): Observable<Chapter> {
        val udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext())
        return Observable.create({
            it.onNext(BeanParser.parseToOWNBean(DataService.getChapterFromNet(BaseBookApplication.getGlobalContext(), BeanParser.parseToQGBean(chapter), udid)))
            it.onComplete()
        })
    }

//    override fun paySingleChapter(sourceId: String?, chapterId: String?, chapterName: String?, uid: String?): Observable<SingleChapterBean> {
//        return NetService.userService.requestSingleChapter(sourceId!!, chapterId!!, chapterName!!, uid!!)
//    }

    override fun isChapterCacheExist(host: String, chapter: Chapter?): Boolean {
        if (chapter == null) return false
        return com.quduquxie.network.DataCache.isChapterExists(chapter.chapter_id, chapter.book_id)
    }


    //空实现
    override fun getBookEndRecommendBook(recommanded: String, bookId: String): Observable<RecommendBooksEndResp> = Observable.create(null)

    override fun getBookSource(bookId: String): Observable<SourceItem> = Observable.create(null)
    override fun updateBookCurrentChapter(bookId: String, retChapter: Chapter, sequence: Int) = Unit
    override fun getChapterIdByChapterId(bookId: String, chapter_id: String?): Int = -1
    override fun changeChargeBookState(bookId: String, chapterIndex: Int, i: Int) = Unit
    override fun writeChapterCache(chapter: Chapter?, book: Book){
        chapter?.let {
            if(BookDaoHelper.getInstance().isBookSubed(chapter.book_id) && !com.quduquxie.network.DataCache.isChapterExists(chapter.chapter_id, book.book_id)) {
                com.quduquxie.network.DataCache.saveChapter(chapter.content, chapter.chapter_id, book.book_id)
            }
        }
    }
//    override fun batchChapter(dex: Int, downloadFlag: Boolean, chapterMap: MutableMap<String, Chapter>?) = Unit
}