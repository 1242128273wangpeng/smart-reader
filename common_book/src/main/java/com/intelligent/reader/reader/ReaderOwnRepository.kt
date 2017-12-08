package com.intelligent.reader.reader

import android.text.TextUtils
import com.intelligent.reader.repository.ReaderRepository
import io.reactivex.Observable
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.SourceItem
import net.lzbook.kit.net.custom.service.NetService
import net.lzbook.kit.purchase.SingleChapterBean
import net.lzbook.kit.request.RequestExecutorDefault
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.request.own.OtherRequestChapterExecutor
import net.lzbook.kit.user.RecommendService
import net.lzbook.kit.user.bean.RecommendBooksEndResp
import net.lzbook.kit.utils.NetWorkUtils
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.Map

/**
 * @desc 阅读模块 自有数据源
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/20 15:16
 */
class ReaderOwnRepository private constructor(api: RecommendService) : ReaderRepository {

    private val mApi: RecommendService = api

    companion object {
        fun getInstance() = RepositoryHolder.INSTANCE
    }

    private object RepositoryHolder {
        val INSTANCE = ReaderOwnRepository(NetService.recommendService)
    }

    override fun getBookEndRecommendBook(recommanded: String, bookId: String): Observable<RecommendBooksEndResp> {
        return mApi.getBookEndRecommendBook(recommanded, bookId)
    }

    override fun getBookSource(bookId: String): Observable<SourceItem> {
        return mApi.getBookSource(bookId).map { it.book_id = bookId;it }
    }

    override fun requestSingleChapter(host: String?, chapter: Chapter?): Observable<Chapter>? {
        return getSourceChapter(chapter)
    }

    override fun batchChapter(dex: Int, downloadFlag: Boolean, chapterMap: MutableMap<String, Chapter>?) {
        val iterator = chapterMap?.entries?.iterator()!!
        val chapters = ArrayList<Chapter>()
        var index = 0
        while (iterator.hasNext()) {
            if (NetWorkUtils.getNetWorkType(RequestExecutorDefault.mContext) == NetWorkUtils.NETWORK_NONE) {
                RequestExecutorDefault.mRquestChaptersListener.requestFailed(RequestExecutorDefault.RequestChaptersListener.ERROR_TYPE_NETWORK_NONE, "没有网络连接", index)
                return
            } else {
                val entry = iterator.next() as Map.Entry<*, *>
                val chapter = entry.value as Chapter
//                val result = singleChapter(dex, chapter)
                val result = getSourceChapter(chapter)
//                chapters.add(result!!)

            }
            index++
        }
        RequestExecutorDefault.mRquestChaptersListener.requestSuccess(chapters)
    }

    /**
     * 从原网址获取章节内容并转化
     */
    @Throws(Exception::class)
    fun getSourceChapter(chapter: Chapter?): Observable<Chapter>? {
        try {
            val a = URL(chapter?.curl)
            UrlUtils.BOOK_CONTENT = a.host
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return NetService.userService.getChapterContent(chapter?.curl, chapter)
    }

    /**
     * 判断是否需要下载
     */
    override fun isNeedDownContent(chapter: Chapter, downloadFlag: Boolean): Boolean {
        if (downloadFlag) {
            if (net.lzbook.kit.request.DataCache.isChapterExists(chapter.sequence, chapter.book_id)) {
                return false
            }
        } else {
            val content = net.lzbook.kit.request.DataCache.getChapterFromCache(chapter.sequence, chapter.book_id)
            if (!TextUtils.isEmpty(content) && !("null" == content || OtherRequestChapterExecutor.CACHE_EXIST == content) || NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
                if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                    if (content.length <= Constants.CONTENT_ERROR_COUNT) {
                        return true
                    } else {
                        chapter.content = content
                        chapter.isSuccess = true
                        return false
                    }
                } else {
                    chapter.content = content
                    chapter.isSuccess = true
                    return false
                }
            }
        }
        return true
    }

    //空实现
    override fun updateBookCurrentChapter(bookId: String, retChapter: Chapter?, sequence: Int) = Unit

    override fun getChapterIdByChapterId(bookId: String, chapter_id: String?): Int = -1
    override fun changeChargeBookState(bookId: String, chapterIndex: Int, i: Int) = Unit
    override fun writeChapterCache(chapter: Chapter?, downloadFlag: Boolean?) = Unit
    override fun paySingleChapter(sourceId: String?, chapterId: String?, chapterName: String?, uid: String?): Observable<SingleChapterBean> = Observable.create(null)
}