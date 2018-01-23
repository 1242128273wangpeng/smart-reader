package com.intelligent.reader.reader

import android.content.Context
import android.text.TextUtils
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.repository.ReaderRepository
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.SourceItem
import net.lzbook.kit.purchase.SingleChapterBean
import net.lzbook.kit.request.RequestFactory
import net.lzbook.kit.user.bean.RecommendBooksEndResp
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.NetWorkUtils

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

        var downloadFlag = false
        //判断青果
        if (RequestFactory.RequestHost.QG.requestHost == host) {
            //判断青果缓存
            if (com.quduquxie.network.DataCache.isChapterExists(chapter.chapter_id, chapter.book_id)) {
                return mReaderLocalRepository.requestSingleChapter(host, chapter)
            } else {
                return mReaderQGRepository.requestSingleChapter(host, chapter)
            }
        } else {
            if (isNeedDownContent(chapter, downloadFlag)) {//是否需要下载
                return mReaderOwnRepository.requestSingleChapter(host, chapter)
            } else {
                return Observable.create({
                    it.onNext(chapter)
                    it.onComplete()
                })
            }
        }
    }

    override fun batchChapter(dex: Int, downloadFlag: Boolean, chapterMap: MutableMap<String, Chapter>?) {
        mReaderOwnRepository.batchChapter(dex, downloadFlag, chapterMap)
    }

    override fun isNeedDownContent(chapter: Chapter, downloadFlag: Boolean): Boolean {
        return mReaderOwnRepository.isNeedDownContent(chapter, downloadFlag)
    }

    override fun updateBookCurrentChapter(bookId: String, retChapter: Chapter?, sequence: Int) {
        mReaderLocalRepository.updateBookCurrentChapter(bookId, retChapter, retChapter!!.sequence)
    }

    override fun getChapterIdByChapterId(bookId: String, chapter_id: String?): Int {
        return mReaderLocalRepository.getChapterIdByChapterId(bookId, chapter_id)
    }

    override fun changeChargeBookState(bookId: String, chapterIndex: Int, i: Int) {
        mReaderLocalRepository.changeChargeBookState(bookId, chapterIndex, 1)
    }

    override fun writeChapterCache(chapter: Chapter?, downloadFlag: Boolean?) {
        mReaderLocalRepository.writeChapterCache(chapter, downloadFlag)
    }

    override fun paySingleChapter(sourceId: String?, chapterId: String?, chapterName: String?, uid: String?): Observable<SingleChapterBean> {
        return mReaderQGRepository.paySingleChapter(sourceId, chapterId, chapterName, uid)
    }

}