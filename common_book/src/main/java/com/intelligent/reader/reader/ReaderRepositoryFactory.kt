package com.intelligent.reader.reader

import com.intelligent.reader.repository.ReaderRepository
import io.reactivex.Observable
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.SourceItem
import net.lzbook.kit.request.RequestFactory
import net.lzbook.kit.user.bean.RecommendBooksEndResp

/**
 * @desc 阅读模块数据源
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/20 15:43
 */
class ReaderRepositoryFactory private constructor(readerOwnRepository: ReaderRepository) : ReaderRepository {

    private val mReaderOwnRepository: ReaderRepository = readerOwnRepository
    private val mReaderQGRepository: ReaderRepository = ReaderQGRepository.getInstance()
    private var mReaderLocalRepository: ReaderRepository = ReaderLocalRepository.getInstance()

    companion object {
        fun getInstance(readerOwnRepository: ReaderRepository) = ReaderRepositoryFactory(readerOwnRepository)
    }

    override fun getBookEndRecommendBook(recommanded: String, bookId: String): Observable<RecommendBooksEndResp> {
        return mReaderOwnRepository.getBookEndRecommendBook(recommanded, bookId)
    }

    override fun getBookSource(bookId: String): Observable<SourceItem> {
        return mReaderOwnRepository.getBookSource(bookId)
    }

    override fun requestSingleChapter(host: String, chapter: Chapter): Observable<Chapter> {

        if (isChapterCacheExist(host, chapter)) {
            return mReaderLocalRepository.requestSingleChapter(host, chapter)
        } else {
            if (RequestFactory.RequestHost.QG.requestHost == host) {
                return mReaderQGRepository.requestSingleChapter(host, chapter)
            } else {
                return mReaderOwnRepository.requestSingleChapter(host, chapter)
            }
        }
    }

    override fun isChapterCacheExist(host: String, chapter: Chapter?): Boolean {
        if (host == RequestFactory.RequestHost.QG.requestHost) {
            return mReaderQGRepository.isChapterCacheExist(host, chapter)
        } else {
            return mReaderOwnRepository.isChapterCacheExist(host, chapter)
        }
    }

//    override fun batchChapter(dex: Int, downloadFlag: Boolean, chapterMap: MutableMap<String, Chapter>?) {
//        mReaderOwnRepository.batchChapter(dex, downloadFlag, chapterMap)
//    }

//    override fun isNeedDownContent(chapter: Chapter, downloadFlag: Boolean): Boolean =
//            mReaderOwnRepository.isNeedDownContent(chapter, downloadFlag)

    override fun updateBookCurrentChapter(bookId: String, retChapter: Chapter, sequence: Int) {
        mReaderLocalRepository.updateBookCurrentChapter(bookId, retChapter, retChapter.sequence)
    }

    override fun getChapterIdByChapterId(bookId: String, chapter_id: String?): Int {
        return mReaderLocalRepository.getChapterIdByChapterId(bookId, chapter_id)
    }

    override fun changeChargeBookState(bookId: String, chapterIndex: Int, i: Int) {
        mReaderLocalRepository.changeChargeBookState(bookId, chapterIndex, 1)
    }

    override fun writeChapterCache(chapter: Chapter, downloadFlag: Boolean?) {
        mReaderLocalRepository.writeChapterCache(chapter, downloadFlag)
    }

//    override fun paySingleChapter(sourceId: String?, chapterId: String?, chapterName: String?, uid: String?): Observable<SingleChapterBean> {
//        return mReaderQGRepository.paySingleChapter(sourceId, chapterId, chapterName, uid)
//    }

}