package com.intelligent.reader.reader

import com.intelligent.reader.repository.ReaderRepository
import com.quduquxie.network.DataService
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.SourceItem
import net.lzbook.kit.net.custom.service.NetService
import net.lzbook.kit.purchase.SingleChapterBean
import net.lzbook.kit.request.RequestExecutorDefault
import net.lzbook.kit.user.RecommendService
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

    override fun requestSingleChapter(host: String?, chapter: Chapter?): Observable<Chapter> {
        val udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext())
        return Observable.create({ it.onNext(BeanParser.parseToOWNBean(DataService.getChapterFromNet(BaseBookApplication.getGlobalContext(), BeanParser.parseToQGBean(chapter), udid))) })
    }

    override fun paySingleChapter(sourceId: String?, chapterId: String?, chapterName: String?, uid: String?): Observable<SingleChapterBean> {
        return NetService.userService.requestSingleChapter(sourceId!!, chapterId!!, chapterName!!, uid!!)
    }

    //空实现
    override fun getBookEndRecommendBook(recommanded: String, bookId: String): Observable<RecommendBooksEndResp> = Observable.create(null)

    override fun getBookSource(bookId: String): Observable<SourceItem> = Observable.create(null)
    override fun isNeedDownContent(chapter: Chapter, downloadFlag: Boolean): Boolean = false
    override fun updateBookCurrentChapter(bookId: String, retChapter: Chapter?, sequence: Int) = Unit
    override fun getChapterIdByChapterId(bookId: String, chapter_id: String?): Int = -1
    override fun changeChargeBookState(bookId: String, chapterIndex: Int, i: Int) = Unit
    override fun writeChapterCache(chapter: Chapter?, downloadFlag: Boolean?) = Unit
    override fun batchChapter(dex: Int, downloadFlag: Boolean, chapterMap: MutableMap<String, Chapter>?) = Unit
}