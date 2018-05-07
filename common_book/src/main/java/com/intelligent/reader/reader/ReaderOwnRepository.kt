package com.intelligent.reader.reader

import com.intelligent.reader.repository.ReaderRepository
import io.reactivex.Observable
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.SourceItem
import net.lzbook.kit.net.custom.service.NetService
import net.lzbook.kit.net.custom.service.UserService
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.user.bean.RecommendBooksEndResp
import java.net.MalformedURLException
import java.net.URL

/**
 * @desc 阅读模块 自有数据源
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/20 15:16
 */
class ReaderOwnRepository private constructor(api: UserService) : ReaderRepository {

    private val mApi: UserService = api

    companion object {
        fun getInstance() = RepositoryHolder.INSTANCE
    }

    private object RepositoryHolder {
        val INSTANCE = ReaderOwnRepository(NetService.userService)
    }

    override fun getBookEndRecommendBook(recommanded: String, bookId: String): Observable<RecommendBooksEndResp> {
        return mApi.getBookEndRecommendBook(recommanded, bookId)
    }

    override fun getBookSource(bookId: String): Observable<SourceItem> {
        return mApi.getBookSource(bookId).map { it.book_id = bookId;it }
    }

    override fun requestSingleChapter(host: String?, chapter: Chapter): Observable<Chapter> =
            getSourceChapter(chapter)

//    override fun batchChapter(dex: Int, downloadFlag: Boolean, chapterMap: MutableMap<String, Chapter>?) {
//        val iterator = chapterMap?.entries?.iterator()!!
//        val chapters = ArrayList<Chapter>()
//        var index = 0
//        while (iterator.hasNext()) {
//            if (NetWorkUtils.getNetWorkType(RequestExecutorDefault.mContext) == NetWorkUtils.NETWORK_NONE) {
//                RequestExecutorDefault.mRquestChaptersListener.requestFailed(RequestExecutorDefault.RequestChaptersListener.ERROR_TYPE_NETWORK_NONE, "没有网络连接", index)
//                return
//            } else {
//                val entry = iterator.next() as Map.Entry<*, *>
//                val chapter = entry.value as Chapter
////                val result = singleChapter(dex, chapter)
//                val result = getSourceChapter(chapter)
////                chapters.add(result!!)
//
//            }
//            index++
//        }
//        RequestExecutorDefault.mRquestChaptersListener.requestSuccess(chapters)
//    }

    //&& chapterContent.length >= Constants.CONTENT_ERROR_COUNT
    override fun isChapterCacheExist(host: String, chapter: Chapter?): Boolean {
        if (chapter == null) return false
        val chapterContent = net.lzbook.kit.request.DataCache.getChapterFromCache(chapter)
        return chapterContent != null
    }

    /**
     * 从原网址获取章节内容并转化
     */
    private fun getSourceChapter(chapter: Chapter): Observable<Chapter> {

        if (chapter.curl != null) {
//            return Observable.create<Chapter> {
//                val a = URL(chapter.curl)
//                UrlUtils.BOOK_CONTENT = a.host
//                chapter.curl?.let { NetService.userService.getChapterContent(it, chapter) }
//            }
            val a = URL(chapter.curl)
            UrlUtils.BOOK_CONTENT = a.host
            return NetService.userService.getChapterContent(chapter.curl!!, chapter)
        } else {
            return Observable.create { e->e.onError(Throwable("content error")) }
        }

//        val a = URL(chapter.curl)
//        UrlUtils.BOOK_CONTENT = a.host
//        return NetService.userService.getChapterContent(chapter.curl!!, chapter)
    }

    //空实现
    override fun updateBookCurrentChapter(bookId: String, retChapter: Chapter, sequence: Int) = Unit

    override fun getChapterIdByChapterId(bookId: String, chapter_id: String?): Int = -1
    override fun changeChargeBookState(bookId: String, chapterIndex: Int, i: Int) = Unit
    override fun writeChapterCache(chapter: Chapter, book: Book) = Unit
//    override fun paySingleChapter(sourceId: String?, chapterId: String?, chapterName: String?, uid: String?): Observable<SingleChapterBean> = Observable.create(null)
}