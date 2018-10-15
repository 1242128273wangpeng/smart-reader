package com.ding.basic.repository

import android.annotation.SuppressLint
import android.arch.persistence.room.EmptyResultSetException
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.ding.basic.Config
import com.ding.basic.bean.*
import com.ding.basic.bean.push.BannerInfo
import com.ding.basic.bean.push.PushInfo
import com.ding.basic.database.helper.BookDataProviderHelper
import com.ding.basic.request.RequestSubscriber
import com.ding.basic.request.ResultCode
import com.ding.basic.rx.CommonResultMapper
import com.ding.basic.rx.SchedulerHelper
import com.ding.basic.util.AESUtil
import com.ding.basic.util.ChapterCacheUtil
import com.ding.basic.util.DataCache
import com.ding.basic.util.ParserUtil
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe
import io.reactivex.*
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.ResourceSubscriber
import net.lzbook.kit.data.book.*
import net.lzbook.kit.data.db.help.ChapterDaoHelper
import net.lzbook.kit.data.user.UserBook
import net.lzbook.kit.user.bean.UserNameState
import net.lzbook.kit.user.bean.WXAccess
import net.lzbook.kit.user.bean.WXSimpleInfo
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RequestRepositoryFactory private constructor(private val context: Context) : RequestRepository {

    companion object {

        @Volatile
        @SuppressLint("StaticFieldLeak")
        private var requestRepositoryFactory: RequestRepositoryFactory? = null

        fun loadRequestRepositoryFactory(context: Context): RequestRepositoryFactory {
            if (requestRepositoryFactory == null) {
                synchronized(RequestRepositoryFactory::class.java) {
                    if (requestRepositoryFactory == null) {
                        requestRepositoryFactory = RequestRepositoryFactory(context)
                    }
                }
            }
            return requestRepositoryFactory!!
        }
    }

    override fun requestDefaultBooks(sex: Int, requestSubscriber: RequestSubscriber<Boolean>) {
        InternetRequestRepository.loadInternetRequestRepository(context).requestDefaultBooks(sex)!!
                .compose(SchedulerHelper.schedulerIOHelper<BasicResult<CoverList>>())
                .doOnNext {
                    if (it != null && it.checkResultAvailable() && it.data?.coverList != null && it.data?.coverList!!.isNotEmpty()) {
                        for (book in it.data?.coverList!!) {
                            if (!TextUtils.isEmpty(book.book_id)) {

                                val localBook = LocalRequestRepository.loadLocalRequestRepository(context).checkBookSubscribe(book.book_id)

                                if (localBook == null) {
                                    LocalRequestRepository.loadLocalRequestRepository(context).insertBook(book)
                                }
                            }
                        }
                    }
                }
                .subscribe({ result ->
                    if (result != null) {
                        if (result.checkResultAvailable() && result.data?.coverList != null && result.data?.coverList!!.isNotEmpty()) {
                            requestSubscriber.onNext(true)
                        } else {
                            requestSubscriber.onError(Throwable("获取默认书籍异常！"))
                        }
                    } else {
                        requestSubscriber.onError(Throwable("获取默认书籍异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("请求默认书籍完成！")
                })
    }

    fun requestDefaultBooks(firstType: String, secondType: String, requestSubscriber: RequestSubscriber<Boolean>) {
        InternetRequestRepository.loadInternetRequestRepository(context).requestDefaultBooks(firstType, secondType)!!
                .compose(SchedulerHelper.schedulerIOHelper<BasicResult<CoverList>>())
                .doOnNext {
                    if (it != null && it.checkPrivateKeyExpire()) {
                        requestAuthAccess {
                            if (it) {
                                requestDefaultBooks(firstType, secondType, requestSubscriber)
                            }
                        }
                    } else if (it != null && it.checkResultAvailable() && it.data?.coverList != null && it.data?.coverList!!.isNotEmpty()) {
                        for (book in it.data?.coverList!!) {
                            if (!TextUtils.isEmpty(book.book_id)) {

                                val localBook = LocalRequestRepository.loadLocalRequestRepository(context).checkBookSubscribe(book.book_id)

                                if (localBook == null) {
                                    LocalRequestRepository.loadLocalRequestRepository(context).insertBook(book)
                                }
                            }
                        }
                    }
                }
                .subscribe({ result ->
                    if (result != null) {
                        if (result.checkResultAvailable() && result.data?.coverList != null && result.data?.coverList!!.isNotEmpty()) {
                            requestSubscriber.onNext(true)
                        } else {
                            requestSubscriber.onError(Throwable("获取默认书籍异常！"))
                        }
                    } else {
                        requestSubscriber.onError(Throwable("获取默认书籍异常！"))
                    }
                }, { throwable ->
                    throwable.printStackTrace()
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("请求默认书籍完成！")
                })
    }

    override fun requestApplicationUpdate(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<ApplicationUpdate>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestApplicationUpdate(parameters)!!
                .compose(SchedulerHelper.schedulerIOHelper<JsonObject>())
                .subscribe({ result ->
                    if (result != null) {
                        try {
                            val applicationUpdate = ParserUtil.parserApplicationUpdate(result.toString())
                            requestSubscriber.onNext(applicationUpdate)
                        } catch (exception: JSONException) {
                            exception.printStackTrace()
                            requestSubscriber.onError(Throwable("获取版本更新异常！"))
                        }
                    } else {
                        requestSubscriber.onError(Throwable("获取版本更新异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("获取版本更新完成！")
                })
    }

    override fun requestDynamicCheck(requestSubscriber: RequestSubscriber<BasicResult<Int>>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestDynamicCheck()
                .compose(SchedulerHelper.schedulerIOHelper<BasicResult<Int>>())
                .subscribe({ result ->
                    if (result != null) {
                        requestSubscriber.onNext(result)
                    } else {
                        requestSubscriber.onError(Throwable("获取动态参数校验接口异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("获取动态参数校验接口完成！")
                })
    }

    override fun requestDynamicParameters(requestSubscriber: RequestSubscriber<Parameter>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestDynamicParameters()
                .compose(SchedulerHelper.schedulerIOHelper<Parameter>())
                .subscribe({ result ->
                    if (result != null) {
                        requestSubscriber.onNext(result)
                    } else {
                        requestSubscriber.onError(Throwable("获取动态参数异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("请求动态参数完成！")
                })
    }

    override fun requestAdControlDynamic(requestSubscriber: RequestSubscriber<AdControlByChannelBean>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestAdControlDynamic()!!
                .compose(SchedulerHelper.schedulerIOHelper<AdControlByChannelBean>())
                .subscribe({ result ->
                    if (result != null) {
                        requestSubscriber.onNext(result)
                    } else {
                        requestSubscriber.onError(Throwable("获取广告动态参数异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("请求广告动态参数完成！")
                })
    }

    override fun requestBookDetail(book_id: String, book_source_id: String, book_chapter_id: String, requestSubscriber: RequestSubscriber<Book>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestBookDetail(book_id, book_source_id, book_chapter_id)!!
                .compose(SchedulerHelper.schedulerHelper<BasicResult<Book>>())
                .subscribe({ result ->
                    if (result != null) {
                        when {
                            result.checkPrivateKeyExpire() -> requestAuthAccess {
                                if (it) {
                                    requestBookDetail(book_id, book_source_id, book_chapter_id, requestSubscriber)
                                } else {
                                    requestSubscriber.onError(Throwable("鉴权请求异常！"))
                                }
                            }
                            result.checkResultAvailable() -> {
                                requestSubscriber.onNext(result.data)

                                synchronized(RequestRepositoryFactory::class.java) {
                                    val localBook = LocalRequestRepository.loadLocalRequestRepository(context).loadBook(book_id)

                                    if (localBook != null && !TextUtils.isEmpty(localBook.book_id)) {
                                        if (TextUtils.isEmpty(localBook.book_chapter_id) && !TextUtils.isEmpty(result.data?.book_chapter_id)) {
                                            ChapterDaoHelper.loadChapterDataProviderHelper(context, localBook.book_id).updateBookChapterId(result.data?.book_chapter_id!!)
                                        }
                                    }
                                }
                            }
                            else -> requestSubscriber.onError(Throwable("获取书籍信息异常！"))
                        }
                    } else {
                        requestSubscriber.onError(Throwable("获取书籍信息异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("请求书籍信息完成！")
                    requestSubscriber.onComplete()
                })
    }

    override fun requestCatalog(book_id: String, book_source_id: String, book_chapter_id: String, requestSubscriber: RequestSubscriber<List<Chapter>>, type: Int) {
        LocalRequestRepository.loadLocalRequestRepository(context).requestBookCatalog(book_id, book_source_id, book_chapter_id)
                .flatMap { result ->
                    if (result.data == null || result.data!!.chapters == null || result.data!!.chapters!!.isEmpty()) {
                        Logger.d("本地暂无章节目录，封装网络请求！")
                        InternetRequestRepository.loadInternetRequestRepository(context).requestBookCatalog(book_id, book_source_id, book_chapter_id)
                    } else {
                        Logger.d("本地章节目录正常！")
                        Flowable.create({ emitter ->
                            emitter.onNext(result)
                        }, BackpressureStrategy.BUFFER)
                    }
                }
                .doOnNext { result ->
                    if (result != null && result.checkResultAvailable() && result.data?.chapters != null && result.data?.chapters!!.isNotEmpty()) {

                        val resList = noRepeatList(result.data!!.chapters!!)

                        for (chapter in resList) {
                            chapter.host = result.data!!.host
                            chapter.book_id = result.data!!.book_id
                            chapter.book_source_id = result.data!!.book_source_id
                            chapter.book_chapter_id = result.data!!.book_chapter_id
                        }

                        result.data?.chapters = resList
                        val book = LocalRequestRepository.loadLocalRequestRepository(context).checkBookSubscribe(book_id)

                        if (book != null) {
                            val chapterDaoHelp = ChapterDaoHelper.loadChapterDataProviderHelper(context, book_id)
                            chapterDaoHelp.insertOrUpdateChapter(resList)
                            book.chapter_count = chapterDaoHelp.getCount()
                            if (result.data!!.listVersion!! > book.list_version) {
                                book.list_version = result.data!!.listVersion!!
                            }
                            if (result.data!!.contentVersion!! > book.c_version) {
                                book.c_version = result.data!!.contentVersion!!
                            }

                            val lastChapter = chapterDaoHelp.queryLastChapter()

                            if (lastChapter != null) {
                                book.last_chapter = lastChapter
                            }
                            LocalRequestRepository.loadLocalRequestRepository(context).updateBook(book)
                        } else {
                            var count = -1
                            resList.forEach {
                                count++
                                it.sequence = count
                            }
                        }
                    }
                }
                .compose(SchedulerHelper.schedulerHelper(type))
                .subscribe({
                    if (it.checkPrivateKeyExpire()) {
                        requestAuthAccess {
                            if (it) {
                                requestCatalog(book_id, book_source_id, book_chapter_id, requestSubscriber, type)
                            } else {
                                requestSubscriber.onError(Throwable("鉴权请求异常！"))
                            }
                        }
                    } else if ((it.code == ResultCode.RESULT_SUCCESS || it.code == ResultCode.LOCAL_RESULT) && it.data != null) {
                        requestSubscriber.onNext(it.data?.chapters)
                    } else {
                        requestSubscriber.onError(Throwable("获取章节目录异常！"))
                    }
                }, {
                    requestSubscriber.onError(it)
                }, {
                    Logger.v("请求目录信息完成！")
                    requestSubscriber.onComplete()
                })
    }

    fun noRepeatList(list: List<Chapter>): ArrayList<Chapter> {
        val mewList = ArrayList<Chapter>()
        for (i in 0 until list.size) {
            if (!mewList.contains(list[i])) {
                mewList.add(list[i])
            }
        }
        return mewList
    }


    /***
     * 用于换源操作，其他拉取目录请调用requestCatalog()方法
     * **/
    override fun requestBookCatalog(book_id: String, book_source_id: String, book_chapter_id: String, requestSubscriber: RequestSubscriber<List<Chapter>>, type: Int) {
        InternetRequestRepository.loadInternetRequestRepository(context).requestBookCatalog(book_id, book_source_id, book_chapter_id)
                .doOnNext { result ->
                    if (result != null && result.checkResultAvailable() && result.data?.chapters != null && result.data?.chapters!!.isNotEmpty()) {

                        for (chapter in result.data?.chapters!!) {
                            chapter.host = result.data!!.host
                            chapter.book_id = result.data!!.book_id
                            chapter.book_source_id = result.data!!.book_source_id
                            chapter.book_chapter_id = result.data!!.book_chapter_id
                        }

                        val book = LocalRequestRepository.loadLocalRequestRepository(context).loadBook(book_id)

                        if (book != null) {
                            val chapterDaoHelp = ChapterDaoHelper.loadChapterDataProviderHelper(context, book_id)
                            chapterDaoHelp.deleteAllChapters()
                            BookDataProviderHelper.loadBookDataProviderHelper(context).deleteBookMark(book_id)

                            chapterDaoHelp.insertOrUpdateChapter(result.data?.chapters!!)

                            val lastChapter = chapterDaoHelp.queryLastChapter()

                            if (lastChapter != null) {
                                book.host = result.data?.host
                                book.book_id = result.data!!.book_id
                                book.chapter_count = result.data?.chapterCount!!
                                book.book_source_id = result.data!!.book_source_id
                                book.book_chapter_id = result.data!!.book_chapter_id
                                if (result.data!!.listVersion!! > book.list_version) {
                                    book.list_version = result.data!!.listVersion!!
                                }
                                if (result.data!!.contentVersion!! > book.c_version) {
                                    book.c_version = result.data!!.contentVersion!!
                                }

                                book.last_chapter = lastChapter

                                LocalRequestRepository.loadLocalRequestRepository(context).updateBook(book)
                            }
                        } else {
                            var count = -1
                            result.data?.chapters?.forEach {
                                count++
                                it.sequence = count
                            }
                        }
                    }
                }
                .compose(SchedulerHelper.schedulerHelper(type))
                .subscribe({
                    if (it.checkPrivateKeyExpire()) {
                        requestAuthAccess {
                            if (it) {
                                requestBookCatalog(book_id, book_source_id, book_chapter_id, requestSubscriber, type)
                            } else {
                                requestSubscriber.onError(Throwable("鉴权请求异常！"))
                            }
                        }
                    } else if ((it.code == ResultCode.RESULT_SUCCESS || it.code == ResultCode.LOCAL_RESULT) && it.data != null) {
                        val resList = noRepeatList(it.data!!.chapters!!)

                        for (chapter in resList) {
                            chapter.host = it.data!!.host
                            chapter.book_id = it.data!!.book_id
                            chapter.book_source_id = it.data!!.book_source_id
                            chapter.book_chapter_id = it.data!!.book_chapter_id
                        }

                        requestSubscriber.onNext(resList)
                    } else {
                        requestSubscriber.onError(Throwable("获取章节目录异常！"))
                    }
                }, {
                    requestSubscriber.onError(it)
                }, {
                    Logger.v("请求目录信息完成！")
                    requestSubscriber.onComplete()
                })
    }

    override fun requestBookSources(book_id: String, book_source_id: String, book_chapter_id: String, requestSubscriber: RequestSubscriber<BookSource>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestBookSources(book_id, book_source_id, book_chapter_id)!!
                .compose(SchedulerHelper.schedulerHelper<BasicResult<BookSource>>())
                .subscribe({ result ->
                    if (result != null) {
                        if (result.checkResultAvailable()) {
                            requestSubscriber.onNext(result.data)
                        } else {
                            requestSubscriber.onError(Throwable("获取来源列表异常！"))
                        }
                    } else {
                        requestSubscriber.onError(Throwable("获取来源列表异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("请求来源列表完成！")
                })
    }

    override fun requestAutoComplete(word: String, requestSubscriber: RequestSubscriber<SearchAutoCompleteBean>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestAutoComplete(word)!!
                .debounce(400, TimeUnit.MILLISECONDS)
                .compose(SchedulerHelper.schedulerHelper<SearchAutoCompleteBean>())
                .subscribe({ result ->
                    if (result != null) {
                        requestSubscriber.onNext(result)
                    } else {
                        requestSubscriber.onError(Throwable("获取自动补全异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("获取自动补全完成！")
                })
    }

    override fun requestSearchRecommend(bookIds: String, requestSubscriber: RequestSubscriber<SearchRecommendBook>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestSearchRecommend(bookIds)!!
                .compose(SchedulerHelper.schedulerHelper<SearchRecommendBook>())
                .subscribe({ result ->
                    if (result != null) {
                        requestSubscriber.onNext(result)
                    } else {
                        requestSubscriber.onError(Throwable("获取推荐异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("获取推荐完成！")
                })
    }

    override fun requestAutoCompleteV4(word: String, requestSubscriber: RequestSubscriber<SearchAutoCompleteBeanYouHua>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestAutoCompleteV4(word)!!
                .debounce(400, TimeUnit.MILLISECONDS)
                .compose(SchedulerHelper.schedulerHelper<SearchAutoCompleteBeanYouHua>())
                .subscribe({ result ->
                    if (result != null) {
                        requestSubscriber.onNext(result)
                    } else {
                        requestSubscriber.onError(Throwable("获取自动补全异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("获取自动补全完成！")
                })
    }

    override fun requestAutoCompleteV5(word: String, requestSubscriber: RequestSubscriber<SearchAutoCompleteBeanYouHua>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestAutoCompleteV5(word)!!
                .debounce(400, TimeUnit.MILLISECONDS)
                .compose(SchedulerHelper.schedulerHelper<SearchAutoCompleteBeanYouHua>())
                .subscribe({ result ->
                    if (result != null) {
                        requestSubscriber.onNext(result)
                    } else {
                        requestSubscriber.onError(Throwable("获取自动补全异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("获取自动补全完成！")
                })
    }

    override fun requestSearchOperationV4(requestSubscriber: RequestSubscriber<Result<SearchResult>>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestHotWordsV4()
                .compose(SchedulerHelper.schedulerHelper<Result<SearchResult>>())
                .subscribe({ result ->
                    if (result != null) {
                        requestSubscriber.requestResult(result)
                    } else {
                        requestSubscriber.onError(Throwable("获取搜索热词异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("获取搜索热词完成！")
                })
    }

    override fun requestHotWords(requestSubscriber: RequestSubscriber<SearchHotBean>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestHotWords()!!
                .compose(SchedulerHelper.schedulerHelper<SearchHotBean>())
                .subscribe({ result ->
                    if (result != null) {
                        requestSubscriber.requestResult(result)
                    } else {
                        requestSubscriber.onError(Throwable("获取搜索热词异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("获取搜索热词完成！")
                })
    }

    override fun requestShareInformation(): Flowable<BasicResultV4<ShareInformation>>? {
        return InternetRequestRepository.loadInternetRequestRepository(context = context).requestShareInformation()
    }


    override fun requestChapterContent(chapter: Chapter): Flowable<Chapter> {
        val content = ChapterCacheUtil.checkChapterCacheExist(chapter)
        return if (content == null || content.isEmpty() || content == "null") {
            InternetRequestRepository.loadInternetRequestRepository(context = context).requestChapterContent(chapter).map {
                when {
                    it.checkPrivateKeyExpire() -> {
                        requestAuthAccess(null)
//                        StatService.onEvent(context, "request_content_access", "请求内容鉴权失败")
                        throw IllegalAccessException("接口鉴权失败！")
                    }
                    it.checkResultAvailable() -> {
                        if (it.data?.content != null && !TextUtils.isEmpty(it.data?.content)) {

                            if (it.data?.content == "文章内容较短,可能非正文,正在抓紧修复中...") {
//                                StatService.onEvent(context, "request_content_exception", "请求内容内容异常: ${chapter.chapter_id}")
                            }

                            it.data?.content = it.data?.content?.replace("\\n", "\n")
                            it.data?.content = it.data?.content?.replace("\\n \\n", "\n")
                            it.data?.content = it.data?.content?.replace("\\n\\n", "\n")
                            it.data?.content = it.data?.content?.replace("\\", "")
                        } else {
//                            StatService.onEvent(context, "request_content_empty", "请求内容返回为空: ${chapter.chapter_id}")
                        }
                        chapter.content = it.data?.content

                        chapter
                    }
                    else -> {
//                        StatService.onEvent(context, "request_content_error", "请求内容结果异常: ${chapter.chapter_id}")
                        throw EmptyResultSetException("接口返回内容异常！")
                    }
                }
            }
        } else {
            chapter.content = content

            Flowable.create({
                it.onNext(chapter)
                it.onComplete()
            }, BackpressureStrategy.BUFFER)
        }
    }

    /**
     * 同步的请求章节内容的方法
     */
    @Throws(IOException::class)
    @Synchronized
    fun requestChapterContentSync(chapter: Chapter): String {
        val basicResult = InternetRequestRepository.loadInternetRequestRepository(context = context).requestChapterContentSync(chapter.chapter_id, chapter.book_id, chapter.book_source_id, chapter.book_chapter_id)?.execute()?.body()
        if (basicResult != null) {
            if (basicResult.checkPrivateKeyExpire()) {
                if (requestAuthAccessSync()) {
                    return requestChapterContentSync(chapter)
                }

            } else if (basicResult.checkResultAvailable()) {
                if (basicResult.data!!.content != null) {
                    basicResult.data!!.content = basicResult.data!!.content!!.replace("\\n", "\n")
                    basicResult.data!!.content = basicResult.data!!.content!!.replace("\\n \\n", "\n")
                    basicResult.data!!.content = basicResult.data!!.content!!.replace("\\n\\n", "\n")
                    basicResult.data!!.content = basicResult.data!!.content!!.replace("\\", "")
                }
                return basicResult.data!!.content ?: ""
            }
        }

        throw  IOException("${basicResult?.code} : ${basicResult?.msg}")
    }

    override fun requestBookUpdate(checkBody: RequestBody, books: HashMap<String, Book>, requestSubscriber: RequestSubscriber<List<BookUpdate>>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestBookUpdate(checkBody)!!
                .compose(SchedulerHelper.schedulerIOHelper<BasicResult<UpdateBean>>())
                .subscribe({ result ->
                    if (result != null) {
                        val bookUpdates = ArrayList<BookUpdate>()
                        if (result.checkPrivateKeyExpire()) {
                            requestAuthAccess {
                                if (it) {
                                    requestBookUpdate(checkBody, books, requestSubscriber)
                                } else {
                                    requestSubscriber.onError(Throwable("鉴权请求异常！"))
                                }
                            }
                        } else if (result.checkResultAvailable()) {
                            val updateBooks = result.data!!.books
                            if (updateBooks != null) {
                                var bookUpdate: BookUpdate
                                for (i in updateBooks.indices) {
                                    val updateBook = updateBooks[i]
                                    bookUpdate = BookUpdate()
                                    var book: Book?

                                    if (TextUtils.isEmpty(updateBook.book_id)) {
                                        continue
                                    } else {
                                        book = books[updateBook.book_id]
                                    }

                                    if (book == null) {
                                        continue
                                    }

                                    val chapterList = ArrayList<Chapter>()
                                    val chapters = updateBook.chapters
                                    if (chapters != null) {
                                        for (index in chapters.indices) {
                                            val chapter = chapters[index]
                                            chapter.book_id = book.book_id
                                            chapter.book_source_id = book.book_source_id
                                            chapter.book_chapter_id = book.book_chapter_id

                                            chapterList.add(chapter)
                                        }
                                        bookUpdate.book_id = book.book_id
                                        bookUpdate.chapterList = chapterList
                                        bookUpdates.add(bookUpdate)
                                    }
                                }
                            }
                            //处理FixBook、FixContent
                            handleFixInformation(result.data?.fix_books, result.data?.fix_contents)

                            requestSubscriber.onNext(bookUpdates)
                        } else {
                            requestSubscriber.onError(Throwable("获取书籍更新异常！"))
                        }
                    } else {
                        requestSubscriber.onError(Throwable("获取书籍更新异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("请求书籍更新完成！")
                    requestSubscriber.requestComplete()
                })
    }


    override fun requestCoverBatch(checkBody: RequestBody) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestCoverBatch(checkBody)!!
                .compose(SchedulerHelper.schedulerIOHelper<BasicResult<List<Book>>>())
                .subscribe({
                    if (it != null) {
                        when {
                            it.checkPrivateKeyExpire() -> requestAuthAccess({
                                if (it) {
                                    requestCoverBatch(checkBody)
                                }
                            })
                            it.checkResultAvailable() -> {
                                for (book in it.data!!) {
                                    if (!TextUtils.isEmpty(book.book_id) && !TextUtils.isEmpty(book.book_source_id) && !TextUtils.isEmpty(book.book_chapter_id)) {

                                        val localBook = RequestRepositoryFactory.loadRequestRepositoryFactory(context).loadBook(book.book_id)

                                        if (localBook != null) {
                                            localBook.status = book!!.status   //更新书籍状态
                                            localBook.book_chapter_id = book!!.book_chapter_id
                                            localBook.name = book.name
                                            localBook.desc = book.desc
                                            localBook.book_type = book.book_type
                                            localBook.book_id = book.book_id
                                            localBook.host = book.host
                                            localBook.author = book.author
                                            localBook.book_source_id = book.book_source_id
                                            localBook.img_url = book.img_url
                                            localBook.label = book.label
                                            localBook.sub_genre = book.sub_genre
                                            localBook.genre = book.genre
                                            localBook.score = book.score

                                            RequestRepositoryFactory.loadRequestRepositoryFactory(context).updateBook(localBook)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Logger.e("更新书籍结果异常！")
                    }
                }, { throwable ->
                    throwable.printStackTrace()
                    Logger.e("更新书籍异常: " + throwable.toString())
                }, {
                    Logger.e("更新书籍完成！")
                })
    }

    override fun requestBookShelfUpdate(checkBody: RequestBody, requestSubscriber: RequestSubscriber<Boolean>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestBookShelfUpdate(checkBody)!!
                .compose(SchedulerHelper.schedulerIOHelper<BasicResult<CoverList>>())
                .subscribe({ result ->
                    if (result != null) {
                        if (result.checkResultAvailable() && result.data?.coverList != null && result.data?.coverList!!.isNotEmpty()) {
                            val loadRepository = LocalRequestRepository.loadLocalRequestRepository(context)
                            val books: ArrayList<Book> = arrayListOf()

                            if (result.data?.fakeQingooBooks != null) {
                                result.data?.fakeQingooBooks?.forEach {
                                    if (it.checkValueValid()) {
                                        val book = loadRepository.loadBook(it.from)
                                        if (book != null) {
                                            book.book_id = it.to

                                            loadRepository.updateBook(book)

                                            ChapterDaoHelper.loadChapterDataProviderHelper(context, it.from).deleteAllChapters()
                                        }
                                    }
                                }
                            }

                            result.data?.coverList?.forEach {
                                val book = loadRepository.checkBookSubscribe(it.book_id)
                                if (book != null) {
                                    if (!TextUtils.isEmpty(it.book_chapter_id)) {
                                        book.book_chapter_id = it.book_chapter_id

                                        book.host = it.host

                                        book.book_type = it.book_type

                                        // 保存在chapter表中
                                        if (book.fromQingoo()) {
                                            ChapterDaoHelper.loadChapterDataProviderHelper(context, book.book_id).updateBookSourceId(it.book_source_id)
                                        }

                                        ChapterDaoHelper.loadChapterDataProviderHelper(context, book.book_id).updateBookChapterId(it.book_chapter_id)
                                    }
                                    if (!TextUtils.isEmpty(it.desc)) {
                                        book.desc = it.desc
                                    }
                                    if (!TextUtils.isEmpty(it.status)) {
                                        book.status = it.status
                                    }
                                    book.genre = it.genre
                                    book.sub_genre = it.sub_genre
                                    book.book_type = it.book_type

                                    val lastChapter = ChapterDaoHelper.loadChapterDataProviderHelper(context, book.book_id).queryLastChapter()

                                    if (lastChapter != null) {
                                        book.last_chapter = lastChapter
                                    } else {
                                        if (it.last_chapter != null) {
                                            book.last_chapter = it.last_chapter
                                        }
                                    }

                                    //青果书籍有可能book_source_id不对
                                    if (book.book_source_id == "api.qingoo.cn") {
                                        book.book_source_id = book.book_id
                                    }

                                    books.add(book)
                                }
                            }
                            if (books.isNotEmpty()) {
                                loadRepository.updateBooks(books)
                                requestSubscriber.onNext(true)
                            } else {
                                requestSubscriber.onError(Throwable("获取默认书籍异常！"))
                            }
                        } else {
                            requestSubscriber.onError(Throwable("获取默认书籍异常！"))
                        }
                    } else {
                        requestSubscriber.onError(Throwable("获取默认书籍异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("请求默认书籍完成！")
                })
    }

    override fun requestFeedback(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<Boolean>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestFeedback(parameters)!!
                .compose(SchedulerHelper.schedulerIOHelper<NoBodyEntity>())
                .subscribe({
                    Logger.e("反馈发送！")
                }, {
                    Logger.e("反馈发送异常！")
                }, {
                    Logger.e("反馈发送完成！")
                })
    }


    override fun requestLoginAction(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<LoginResp>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestLoginAction(parameters)!!
                .compose(SchedulerHelper.schedulerHelper<LoginResp>())
                .subscribeWith(object : ResourceSubscriber<LoginResp>() {
                    override fun onNext(result: LoginResp?) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })
    }

    override fun requestSmsCode(mobile: String, requestSubscriber: RequestSubscriber<BasicResultV4<String>>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .requestSmsCode(mobile)
                ?.compose(SchedulerHelper.schedulerHelper<BasicResultV4<String>>())
                ?.subscribeWith(object : ResourceSubscriber<BasicResultV4<String>>() {
                    override fun onNext(result: BasicResultV4<String>) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })

    }

    override fun requestSmsLogin(smsBody: RequestBody, requestSubscriber: RequestSubscriber<BasicResultV4<LoginRespV4>>) {

        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .requestSmsLogin(smsBody)
                ?.compose(SchedulerHelper.schedulerHelper<BasicResultV4<LoginRespV4>>())
                ?.subscribeWith(object : ResourceSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun onNext(result: BasicResultV4<LoginRespV4>) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })
    }

    fun requestLogout(requestSubscriber: RequestSubscriber<BasicResultV4<String>>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .requestLogout()
                ?.compose(SchedulerHelper.schedulerHelper<BasicResultV4<String>>())
                ?.subscribeWith(object : ResourceSubscriber<BasicResultV4<String>>() {
                    override fun onNext(result: BasicResultV4<String>) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })
    }

    fun uploadUserAvatar(avatarBody: RequestBody, requestSubscriber: RequestSubscriber<BasicResultV4<LoginRespV4>>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .uploadUserAvatar(avatarBody)
                .compose(SchedulerHelper.schedulerHelper<BasicResultV4<LoginRespV4>>())
                .subscribeWith(object : ResourceSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun onNext(result: BasicResultV4<LoginRespV4>) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })

    }

    fun requestUserNameState(requestSubscriber: RequestSubscriber<BasicResultV4<UserNameState>>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .requestUserNameState()
                .compose(SchedulerHelper.schedulerHelper<BasicResultV4<UserNameState>>())
                .subscribeWith(object : ResourceSubscriber<BasicResultV4<UserNameState>>() {
                    override fun onNext(result: BasicResultV4<UserNameState>) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })
    }

    fun uploadUserGender(genderBody: RequestBody, requestSubscriber: RequestSubscriber<BasicResultV4<LoginRespV4>>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .uploadUserGender(genderBody)
                .compose(SchedulerHelper.schedulerHelper<BasicResultV4<LoginRespV4>>())
                .subscribeWith(object : ResourceSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun onNext(result: BasicResultV4<LoginRespV4>) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })

    }

    fun uploadUserName(nameBody: RequestBody, requestSubscriber: RequestSubscriber<BasicResultV4<LoginRespV4>>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .uploadUserName(nameBody)
                .compose(SchedulerHelper.schedulerHelper<BasicResultV4<LoginRespV4>>())
                .subscribeWith(object : ResourceSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun onNext(result: BasicResultV4<LoginRespV4>) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })

    }

    fun bindPhoneNumber(phoneBody: RequestBody, requestSubscriber: RequestSubscriber<BasicResultV4<LoginRespV4>>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .bindPhoneNumber(phoneBody)
                .compose(SchedulerHelper.schedulerHelper<BasicResultV4<LoginRespV4>>())
                .subscribeWith(object : ResourceSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun onNext(result: BasicResultV4<LoginRespV4>) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })

    }

    /**
     * 请求用户书架 ,同步书架-----------------------------------------------------------------开始
     */
    fun keepUserBookShelf(accountId: String, onComplete: (() -> Unit)? = null) {
        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .requestBookShelf(accountId)
                .compose(SchedulerHelper.schedulerHelper<BasicResultV4<List<UserBook>>>())
                .doOnNext { remoteBookShelf ->
                    if (remoteBookShelf.data != null && remoteBookShelf.data!!.isNotEmpty()) {
                        mergeBookShelf(remoteBookShelf.data!!)
                        Log.d("keepBookShelf", "thread : " + Thread.currentThread() + " 服务器已有数据 存入数据库 data : " + remoteBookShelf.data.toString())
                    } else {
                        Log.d("keepBookShelf", "thread : " + Thread.currentThread() + " 服务器无数据 上传数据")
                    }
                }
                .flatMap {
                    if (it.data != null && it.data!!.isNotEmpty()) {
                        Flowable.create(object : FlowableOnSubscribe<BasicResultV4<String>> {
                            override fun subscribe(emitter: FlowableEmitter<BasicResultV4<String>>) {
                                emitter.onNext(BasicResultV4())
                            }


                        }, BackpressureStrategy.BUFFER)
                    } else {
                        getUploadBookShelfFlowable(accountId)
                    }
                }.subscribeWith(object : ResourceSubscriber<BasicResultV4<String>>() {
                    override fun onNext(it: BasicResultV4<String>?) {
                        onComplete?.invoke()
                        Log.d("keepBookShelf", "thread : " + Thread.currentThread() +
                                if (it?.data == null) " 服务器已有数据或本地无数据" else " 服务器无数据 上传成功 data : " + it.toString())

                    }

                    override fun onError(t: Throwable?) {
                        onComplete?.invoke()
                        if (t != null) {
                            Log.d("keepBookShelf", "fail : " + t.message)
                        }
                    }

                    override fun onComplete() {

                    }

                })

    }

    /**
     * 获取上传书架Flowable
     */
    fun getUploadBookShelfFlowable(accountId: String): Flowable<BasicResultV4<String>> {
        val bookList = queryAllBook()

        val bookReqBody = getBookReqBody(accountId, bookList)
        val body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), Gson().toJson(bookReqBody))
        return InternetRequestRepository.loadInternetRequestRepository(context = context)
                .uploadBookshelf(body)
    }


    /**
     * 获取刷新token
     */
    fun getRefreshToken(requestSubscriber: RequestSubscriber<BasicResultV4<LoginRespV4>>) {
       InternetRequestRepository.loadInternetRequestRepository(context = context)
                .refreshToken()
               ?.compose(SchedulerHelper.schedulerHelper<BasicResultV4<LoginRespV4>>())
               ?.subscribeWith(object : ResourceSubscriber<BasicResultV4<LoginRespV4>>() {
                   override fun onNext(result: BasicResultV4<LoginRespV4>) {
                       requestSubscriber.onNext(result)
                   }

                   override fun onError(throwable: Throwable) {
                       requestSubscriber.onError(throwable)
                   }

                   override fun onComplete() {
                       requestSubscriber.onComplete()
                   }
               })
    }

    /**
     * 获取 BookReqBody
     */
    private fun getBookReqBody(userId: String, bookList: List<Book>): BookReqBody {
        val userBookList = ArrayList<BookBody>()
        for (book in bookList) {
            val bookBody = BookBody(book.book_id, book.book_source_id, book.offset, book.sequence, book.host, book.img_url
                    , book.name, book.author, book.last_read_time, book.chapter_count, book.last_chapter?.name, book.last_chapter?.update_time)
            userBookList.add(bookBody)
        }
        return BookReqBody(userId, userBookList)
    }


    private fun queryAllBook(): List<Book> {
        val repository = LocalRequestRepository.loadLocalRequestRepository(context = context)
        val extendsBooks = java.util.ArrayList<Book>()
        repository.loadBooks()?.let { extendsBooks.addAll(it) }

        Collections.sort(extendsBooks)
        return extendsBooks
    }


    /**
     * 合并书架
     */
    private fun mergeBookShelf(data: List<UserBook>) {
        val repository = LocalRequestRepository.loadLocalRequestRepository(context = context)
        repository.deleteShelfBooks()

        if (data.isEmpty()) {
            return
        }

        for (book in data) {
            val saveBook = book.transToBook()
            repository.insertBook(saveBook)
        }
    }

    /**
     * 请求用户书架 ,同步书架-----------------------------------------------------------------结束
     */


    /**
     *  同步书签-----------------------------------------------------------------开始
     */

    fun keepBookMark(userId: String, onComplete: (() -> Unit)? = null) {
        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .requestBookMarks(userId)
                .compose(SchedulerHelper.schedulerHelper<BasicResultV4<List<UserMarkBook>>>())
                .doOnNext { remoteBookMarks ->
                    if (remoteBookMarks.data != null && remoteBookMarks.data!!.isNotEmpty()) {
                        mergeBookMark(remoteBookMarks.data!!)
                        Log.d("keepBookMark", "thread : " + Thread.currentThread() + " 服务器已有数据 存入数据库 data : " + remoteBookMarks.data.toString())
                    } else {
                        Log.d("keepBookMark", "thread : " + Thread.currentThread() + " 服务器无数据 上传数据")
                    }
                }
                .flatMap { remoteBookMarks ->
                    if (remoteBookMarks.data != null && remoteBookMarks.data!!.isNotEmpty()) {
                        Flowable.create({ emitter -> emitter.onNext(BasicResultV4()) }, BackpressureStrategy.BUFFER)
                    } else {
                        getUploadBookMarkFlowable(userId);
                    }
//

                }.subscribeWith(object : ResourceSubscriber<BasicResultV4<String>>() {
                    override fun onNext(it: BasicResultV4<String>?) {
                        onComplete?.invoke()
                        Log.d("keepBookMark", "thread : " + Thread.currentThread() +
                                if (it?.data == null) " 服务器已有数据或本地无数据" else " 服务器无数据 上传成功 data : " + it.toString())

                    }

                    override fun onError(t: Throwable?) {
                        onComplete?.invoke()
                        if (t != null) {
                            Log.d("keepBookMark", "fail : " + t.message)
                        }
                    }

                    override fun onComplete() {

                    }

                })

    }

    @Suppress("SENSELESS_COMPARISON")
    private fun mergeBookMark(data: List<UserMarkBook>) {
        val bookDataProviderHelper = BookDataProviderHelper.loadBookDataProviderHelper(context = context)
        bookDataProviderHelper.deleteAllBookMark()
        if (data.isEmpty()) {
            return
        }
        val bookMarkList = ArrayList<Bookmark>()
        bookFor@ for (remoteData in data) {
            val bookId = remoteData.bookId
            val bookSourceId = remoteData.bookSourceId
            val bookMarks = remoteData.marks

            // 如果此本书没有书签，则跳过本书遍历
            if (bookMarks == null) {
                continue@bookFor
            }
            for (bookMark in bookMarks) {

                var saveBookMark = Bookmark()
                saveBookMark.book_id = bookId
                saveBookMark.book_source_id = bookSourceId
                saveBookMark.sequence = bookMark.sequence
                saveBookMark.offset = bookMark.offset
                saveBookMark.chapter_name = bookMark.chapterName
                saveBookMark.chapter_content = bookMark.markContent
                saveBookMark.insert_time = bookMark.addTimeStr.toLong()

                bookMarkList.add(saveBookMark)
            }
        }
        if (bookMarkList.isNotEmpty()) {
            for (item in bookMarkList) {
                bookDataProviderHelper.insertBookMark(item)
            }
        }

    }

    /**
     * 获取上传书签Flowable
     */
    fun getUploadBookMarkFlowable(accountId: String): Flowable<BasicResultV4<String>> {
        val bookList = queryAllBook()

        val bookMarkBody = getBookMarkBody(accountId, bookList)
        Log.d("keepBookMark", "upload data : " + bookMarkBody.toString())
        val body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), Gson().toJson(bookMarkBody))
        return InternetRequestRepository.loadInternetRequestRepository(context = context)
                .uploadBookMarks(body)
    }


    /**
     * 获取 BookMarkBody
     */
    private fun getBookMarkBody(userId: String, bookList: List<Book>): BookMarkBody {
        val bookBodyList = ArrayList<UserMarkBook>()
        for (book in bookList) {
            val bookmarkList = queryLatestBookMark(book.book_id)
            val bookMarkBodyList = ArrayList<UserMark>()
            for (bookmark in bookmarkList) {
                val bookMarkBody = UserMark.create(bookmark)
                bookMarkBodyList.add(bookMarkBody)
            }
            val bookBody = UserMarkBook(book.book_id, book.book_source_id, bookMarkBodyList)
            bookBodyList.add(bookBody)
        }
        return BookMarkBody(userId, bookBodyList)
    }


    /**
     *  根据bookId获取书籍最近100条书签
     */

    fun queryLatestBookMark(bookId: String): ArrayList<Bookmark> {
        val bookDataProviderHelper = BookDataProviderHelper.loadBookDataProviderHelper(context = context)
        return bookDataProviderHelper.getBookMarks(bookId)
    }

    /**
     *  同步书签-----------------------------------------------------------------结束
     */

    /**
     * 同步足迹-----------------------------------------------------------------开始
     */
    fun keepBookBrowse(userId: String, onComplete: (() -> Unit)? = null) {
        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .requestFootPrint(userId)
                .compose(SchedulerHelper.schedulerHelper<BasicResultV4<List<UserBook>>>())
                .doOnNext { remoteBookBrowses ->
                    if (remoteBookBrowses.data != null && remoteBookBrowses.data!!.isNotEmpty()) {
                        mergeBookBrowe(remoteBookBrowses.data!!)
                        Log.d("keepBookBrowse", "thread : " + Thread.currentThread() + " 服务器已有数据 存入数据库 data : " + remoteBookBrowses.data.toString())

                    } else {
                        Log.d("keepBookBrowse", "thread : " + Thread.currentThread() + " 服务器无数据 上传数据")
                    }
                }
                .flatMap { remoteBookBrowses ->
                    if (remoteBookBrowses.data != null && remoteBookBrowses.data!!.isNotEmpty()) {
                        Flowable.create(object : FlowableOnSubscribe<BasicResultV4<String>> {
                            override fun subscribe(emitter: FlowableEmitter<BasicResultV4<String>>) {
                                emitter.onNext(BasicResultV4())

                            }

                        }, BackpressureStrategy.BUFFER)
                    } else {
                        getUploadBookBrowseFlowable(userId);
                    }

                }.subscribeWith(object : ResourceSubscriber<BasicResultV4<String>>() {
                    override fun onNext(it: BasicResultV4<String>?) {
                        onComplete?.invoke()
                        Log.d("keepBookMark", "thread : " + Thread.currentThread() +
                                if (it?.data == null) " 服务器已有数据或本地无数据" else " 服务器无数据 上传成功 data : " + it.toString())

                    }

                    override fun onError(t: Throwable?) {
                        onComplete?.invoke()
                        if (t != null) {
                            Log.d("keepBookMark", "fail : " + t.message)
                        }
                    }

                    override fun onComplete() {

                    }

                })
    }

    /**
     * 合并本地足迹
     */
    private fun mergeBookBrowe(data: List<UserBook>) {

        var mBookDataHelper: BookDataProviderHelper = BookDataProviderHelper.loadBookDataProviderHelper(context = context)
        mBookDataHelper.deleteAllHistory()

        if (data.isEmpty()) {
            return
        }


        for (remoteData in data) {
            val historyInfo = remoteData.transToHistoryInfo()
            mBookDataHelper.insertHistoryInfo(historyInfo)
        }


    }

    /**
     *  获取上传本地足迹Flowable
     */
    fun getUploadBookBrowseFlowable(userId: String): Flowable<BasicResultV4<String>> {
        var mBookDataHelper: BookDataProviderHelper = BookDataProviderHelper.loadBookDataProviderHelper(context = context)
        val upLoadData = mBookDataHelper.queryHistoryPaging(0, 200)
        val bookInfoBodyList = ArrayList<BookBrowseReqBody.BookInfoBody>()
        for (i in upLoadData.indices) {
            val upData = upLoadData[i]
            val bookInfoBody = BookBrowseReqBody.BookInfoBody(upData.book_id, upData.book_source_id, upData.browse_time.toString(),
                    upData.host.toString(), upData.img_url.toString(), upData.name.toString(), upData.author.toString())
            bookInfoBodyList.add(bookInfoBody)
        }

        val bookBrowseReqBody = BookBrowseReqBody(userId, bookInfoBodyList)

        Log.d("keepBookBrowse", "upload data : " + bookBrowseReqBody.toString())
        val body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), Gson().toJson(bookBrowseReqBody))
        return InternetRequestRepository.loadInternetRequestRepository(context = context)
                .uploadFootPrint(body)

    }


    /**
     * 同步足迹-----------------------------------------------------------------结束
     */


    fun thirdLogin(thirdBody: RequestBody, requestSubscriber: RequestSubscriber<BasicResultV4<LoginRespV4>>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .thirdLogin(thirdBody)
                .compose(SchedulerHelper.schedulerHelper<BasicResultV4<LoginRespV4>>())
                .subscribeWith(object : ResourceSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun onNext(result: BasicResultV4<LoginRespV4>) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })


    }

    fun bindThirdAccount(accountBody: RequestBody, requestSubscriber: RequestSubscriber<BasicResultV4<LoginRespV4>>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .bindThirdAccount(accountBody)
                .compose(SchedulerHelper.schedulerHelper<BasicResultV4<LoginRespV4>>())
                .subscribeWith(object : ResourceSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun onNext(result: BasicResultV4<LoginRespV4>) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })


    }


    fun keepBookShelf() {
        val repository = LocalRequestRepository.loadLocalRequestRepository(context = context)
        repository.loadBooks()

    }


    override fun requestLogoutAction(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<JsonObject>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestLogoutAction(parameters)!!
                .compose(SchedulerHelper.schedulerHelper<JsonObject>())?.subscribeWith(object : ResourceSubscriber<JsonObject>() {
                    override fun onNext(result: JsonObject?) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })
    }


    override fun requestRefreshToken(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<RefreshResp>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestRefreshToken(parameters)!!
                .compose(SchedulerHelper.schedulerHelper<RefreshResp>())
                .subscribeWith(object : ResourceSubscriber<RefreshResp>() {
                    override fun onNext(result: RefreshResp?) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })
    }

    override fun requestUserInformation(token: String, appid: String, openid: String, requestSubscriber: RequestSubscriber<QQSimpleInfo>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestUserInformation(token, appid, openid)!!
                .compose(SchedulerHelper.schedulerHelper<QQSimpleInfo>())
                .subscribeWith(object : ResourceSubscriber<QQSimpleInfo>() {
                    override fun onNext(result: QQSimpleInfo?) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })
    }

    fun requestWXAccessToken(token: String, appid: String, openid: String, authorizationCode: String, requestSubscriber: RequestSubscriber<WXAccess>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .requestWXAccessToken(token, appid, openid, authorizationCode)
                .compose(SchedulerHelper.schedulerHelper<WXAccess>())
                .subscribeWith(object : ResourceSubscriber<WXAccess>() {
                    override fun onNext(result: WXAccess?) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })
    }

    fun requestWXUserInfo(token: String, openid: String, requestSubscriber: RequestSubscriber<WXSimpleInfo>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context)
                .requestWXUserInfo(token, openid)
                .compose(SchedulerHelper.schedulerHelper<WXSimpleInfo>())
                .subscribeWith(object : ResourceSubscriber<WXSimpleInfo>() {
                    override fun onNext(result: WXSimpleInfo?) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })
    }


    override fun requestCoverRecommend(book_id: String, recommend: String, requestSubscriber: RequestSubscriber<CoverRecommendBean>) {
        InternetRequestRepository.loadInternetRequestRepository(context).requestCoverRecommend(book_id, recommend)!!
                .compose(SchedulerHelper.schedulerHelper<CoverRecommendBean>())
                .subscribeWith(object : ResourceSubscriber<CoverRecommendBean>() {
                    override fun onNext(result: CoverRecommendBean?) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })
    }


    override fun requestBookRecommend(book_id: String, shelfBooks: String, requestSubscriber: RequestSubscriber<RecommendBooks>) {
        InternetRequestRepository.loadInternetRequestRepository(context).requestBookRecommend(book_id, shelfBooks)!!
                .compose(SchedulerHelper.schedulerHelper())
                .subscribeWith(object : ResourceSubscriber<CommonResult<RecommendBooks>>() {
                    override fun onNext(result: CommonResult<RecommendBooks>?) {
                        if (result != null && result.checkResultAvailable()) {
                            requestSubscriber.onNext(result.data)
                        } else {
                            requestSubscriber.onError(Throwable("推荐接口请求异常！"))
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })
    }


    /**
     * 完结页推荐（兼容数据融合前的项目）
     */
    override fun requestBookRecommendV4(book_id: String, recommend: String, requestSubscriber: RequestSubscriber<RecommendBooksEndResp>) {
        InternetRequestRepository.loadInternetRequestRepository(context).requestBookRecommendV4(book_id, recommend)!!
                .compose(SchedulerHelper.schedulerHelper<RecommendBooksEndResp>())
                .subscribeWith(object : ResourceSubscriber<RecommendBooksEndResp>() {
                    override fun onNext(result: RecommendBooksEndResp?) {
                        requestSubscriber.onNext(result)
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })
    }


    /**
     * 该作者的其他作品推荐
     */
    override fun requestAuthorOtherBookRecommend(author: String, book_id: String, requestSubscriber: RequestSubscriber<java.util.ArrayList<RecommendBean>>) {
        InternetRequestRepository.loadInternetRequestRepository(context).requestAuthorOtherBookRecommend(author, book_id)!!
                .compose(SchedulerHelper.schedulerHelper())
                .subscribeWith(object : ResourceSubscriber<CommonResult<java.util.ArrayList<RecommendBean>>>() {
                    override fun onNext(result: CommonResult<java.util.ArrayList<RecommendBean>>?) {
                        if (result != null && result.checkResultAvailable()) {
                            requestSubscriber.onNext(result.data)
                        } else {
                            requestSubscriber.onError(Throwable("作者其他作品推荐接口请求异常！"))
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        requestSubscriber.onError(throwable)
                    }

                    override fun onComplete() {
                        requestSubscriber.onComplete()
                    }
                })
    }


    /**
     * 搜索无结果页  订阅
     */
    override fun requestSubBook(bookName: String, bookAuthor: String, requestSubscriber: RequestSubscriber<JsonObject>) {
        InternetRequestRepository.loadInternetRequestRepository(context).requestSubBook(bookName, bookAuthor)!!
                .compose(SchedulerHelper.schedulerHelper())
                .subscribeWith(object : RequestSubscriber<JsonObject>() {
                    override fun requestResult(result: JsonObject?) {
                        if (result != null) {
                            requestSubscriber.onNext(result)
                        } else {
                            requestSubscriber.onError(Throwable("接口请求异常！"))
                        }
                    }

                    override fun requestError(message: String) {
                        requestSubscriber.onError(Throwable("接口请求异常！"))
                    }

                })
    }


    override fun checkChapterCache(chapter: Chapter?): Boolean {
        if (chapter == null) {
            return false
        }

        return DataCache.isChapterCached(chapter)
    }


    override fun checkBookSubscribe(book_id: String): Book? {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).checkBookSubscribe(book_id)
    }

    override fun insertBook(book: Book): Long {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).insertBook(book)
    }

    override fun updateBook(book: Book): Boolean {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).updateBook(book)
    }

    override fun updateBooks(books: List<Book>): Boolean {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).updateBooks(books)
    }

    override fun deleteBook(book_id: String): Boolean {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).deleteBook(book_id)
    }

    override fun deleteBooks(books: List<Book>) {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).deleteBooks(books)
    }

    override fun deleteBooksById(books: List<Book>) {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).deleteBooksById(books)
    }

    override fun deleteShelfBook() {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).deleteShelfBooks()
    }

    override fun loadBook(book_id: String): Book? {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).loadBook(book_id)
    }

    override fun loadBooks(): List<Book>? {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).loadBooks()
    }

    override fun loadReadBooks(): List<Book>? {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).loadReadBooks()
    }


    override fun loadBookCount(): Long {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).loadBookCount()
    }

    override fun loadBookShelfIDs(): String {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).loadBookShelfIDs()
    }

    override fun loadBookmarkList(book_id: String, requestSubscriber: RequestSubscriber<List<Bookmark>>) {

    }


    override fun insertBookFix(bookFix: BookFix) {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).insertBookFix(bookFix)
    }

    override fun deleteBookFix(id: String) {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).deleteBookFix(id)
    }

    override fun loadBookFixs(): List<BookFix>? {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).loadBookFixs()
    }

    override fun loadBookFix(book_id: String): BookFix? {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).loadBookFix(book_id)
    }

    override fun updateBookFix(bookFix: BookFix) {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).updateBookFix(bookFix)
    }

    fun insertOrUpdate(user: LoginRespV4) {
        LocalRequestRepository.loadLocalRequestRepository(context = context).insertOrUpdate(user)
    }

    fun queryLoginUser(): LoginRespV4 {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).queryLoginUser()
    }

    fun deleteLoginUser() {
        LocalRequestRepository.loadLocalRequestRepository(context = context).deleteLoginUser()
    }


    fun isChapterCacheExist(chapter: Chapter?): Boolean {
        if (chapter == null) return false
        val chapterContent = DataCache.getChapterFromCache(chapter)
        return chapterContent != null
    }


    @Synchronized
    private fun handleFixInformation(fixBooks: List<BookFix>?, fixContents: List<FixContent>?) {
        if (fixBooks != null && fixBooks.isNotEmpty()) {
            fixBooks.forEach {
                if (it != null && !TextUtils.isEmpty(it.book_id)) {
                    val book = LocalRequestRepository.loadLocalRequestRepository(context).loadBook(it.book_id)
                    if (book != null && !TextUtils.isEmpty(book.book_id)) {
                        if (book.list_version == -1 || book.c_version == -1) {
                            // 这里返回的version是后端按照书籍加入书架时间查找到的最新version
                            // 为什么要求按照加入书架时间返回version的解释:
                            // 考虑满足条件的以下用户:
                            // 1)用户在9月1号加入书架拉取目录,没有执行check接口但关闭了应用
                            // 2)后端在9月2号进行了书籍的目录修复
                            // 3)用户在9月3日打开了应用执行第一次check接口
                            // 后端直接返回最新版本会导致用户永远无法接受9月2号的书籍修改
                            book.c_version = it.c_version
                            book.list_version = it.list_version

                            Logger.v("更新书籍ListVersion和ContentVersion")
                        } else {
                            // 目录修复后c_version应保持与后端一致
                            book.c_version = it.c_version
                            book.list_version_fix = it.list_version

                            Logger.v("记录ListVersion和ContentVersion, 等待用户触发目录修复")
                        }
                        LocalRequestRepository.loadLocalRequestRepository(context).updateBook(book)
                    }
                }
            }
        }

        if (fixContents != null && !fixContents.isEmpty()) {
            Flowable.fromIterable(fixContents)
                    .subscribeOn(Schedulers.io())
                    .filter { it.chapters != null && !TextUtils.isEmpty(it.book_id) }
                    .subscribe({
                        val book = LocalRequestRepository.loadLocalRequestRepository(context).loadBook(it.book_id)
                        if (it.chapters != null && it.chapters!!.isNotEmpty() && book != null) {

                            val chapterDaoHelp = ChapterDaoHelper.loadChapterDataProviderHelper(context, it.book_id)

                            var localNoChapterID = false

                            for (chapter in it.chapters!!) {
                                if (TextUtils.isEmpty(chapter.chapter_id)) {
                                    continue
                                }

                                val fixChapter = chapterDaoHelp.getChapterById(chapter.chapter_id)

                                if (fixChapter == null) {
                                    localNoChapterID = true
                                    continue
                                }

                                fixChapter.book_id = it.book_id
                                fixChapter.book_source_id = it.book_source_id
                                fixChapter.book_chapter_id = it.book_chapter_id
                                fixChapter.url = chapter.url
                                fixChapter.name = chapter.name
                                fixChapter.serial_number = chapter.serial_number
                                fixChapter.word_count = chapter.word_count
                                fixChapter.update_time = chapter.update_time
                                fixChapter.vip = chapter.vip
                                fixChapter.price = chapter.price

                                val content = ChapterCacheUtil.checkChapterCacheExist(fixChapter)
                                if (content != null && !content.isEmpty() && content != "null") {
                                    // 删除本地缓存
                                    DataCache.deleteChapterCache(fixChapter)
                                    fixChapter.setWaitFix(true)

                                    if (fixChapter.sequence == book.sequence) {
                                        book.offset = 0
                                    }
                                }

                                chapterDaoHelp.updateChapter(fixChapter)
                            }

                            book.c_version = it.c_version
                            book.list_version = it.list_version
                            if (localNoChapterID) {
                                // 本地没有章节id的书籍,强制执行目录修复
                                book.force_fix = 1
                                book.list_version_fix = it.list_version
                            }
                            LocalRequestRepository.loadLocalRequestRepository(context).updateBook(book)

                        }
                    })
        }
    }

//    private fun fixChapterContent(chapter: Chapter?, fixState: ContextFixState) {
//        if (chapter != null && DataCache.isNewCacheExists(chapter)) {
//
//            try {
//                chapter.content = RequestRepositoryFactory.loadRequestRepositoryFactory(context).requestChapterContentSync(chapter)
//
//                var content = chapter.content
//                if (TextUtils.isEmpty(content)) {
//                    content = "null"
//                }
//
//                fixState.addContState(DataCache.fixChapter(content, chapter))
//            } catch (e: Exception) {
//                fixState.addContState(false)
//                e.printStackTrace()
//            }
//
//        }
//    }

    fun requestDownTaskConfig(bookID: String, bookSourceID: String
                              , type: Int, startChapterID: String
                              , requestSubscriber: RequestSubscriber<BasicResult<CacheTaskConfig>>) {
        InternetRequestRepository.loadInternetRequestRepository(context).requestDownTaskConfig(bookID, bookSourceID, type, startChapterID)!!
                .subscribeBy(
                        onNext = { ret ->
                            if (ret.checkPrivateKeyExpire()) {
                                requestAuthAccess {
                                    if (it) {
                                        requestDownTaskConfig(bookID, bookSourceID, type, startChapterID, requestSubscriber)
                                    } else {
                                        requestSubscriber.onError(Throwable("鉴权请求异常！"))
                                    }
                                }
                            } else {
                                requestSubscriber.onNext(ret)
                            }
                        },

                        onError = { t ->
                            requestSubscriber.onError(t)
                        }

                )
    }

    @Synchronized
    override fun requestAuthAccess(callback: ((Boolean) -> Unit)?) {
        InternetRequestRepository.loadInternetRequestRepository(context).requestAuthAccess()!!
                .compose(SchedulerHelper.schedulerHelper<BasicResult<String>>())
                .subscribeWith(object : ResourceSubscriber<BasicResult<String>>() {
                    override fun onNext(result: BasicResult<String>?) {
                        if (result != null && result.checkResultAvailable()) {
                            Logger.e("鉴权请求结果正常！")

                            val message = AESUtil.decrypt(result.data!!, Config.loadAccessKey())

                            if (message != null && message.isNotEmpty()) {
                                val access = Gson().fromJson(message, Access::class.java)
                                if (access != null) {
                                    if (access.publicKey != null) {
                                        Config.insertPublicKey(access.publicKey!!)
                                    }

                                    if (access.privateKey != null) {
                                        Config.insertPrivateKey(access.privateKey!!)
                                    }

                                    if (access.expire > 0 ) {
                                        Config.insertAuthExpire(access.expire.toLong())
                                    }
                                }
                            }

                            callback?.invoke(true)
                        } else {
                            Logger.e("鉴权请求结果异常！")
                            callback?.invoke(false)
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        Logger.e("鉴权请求异常！")
                        callback?.invoke(false)
                    }

                    override fun onComplete() {
                        Logger.e("鉴权请求完成！")
                    }
                })
    }

    override fun requestPushTags(udid: String): Flowable<PushInfo> {
        val localFlowable = LocalRequestRepository.loadLocalRequestRepository(context)
                .requestPushInfo()
        return if (localFlowable != null) {
            localFlowable
        } else {
            InternetRequestRepository.loadInternetRequestRepository(context)
                    .requestPushTags(udid)
                    .map(CommonResultMapper())
                    .flatMap {
                        val pushInfo = PushInfo()
                        pushInfo.tags = it
                        pushInfo.updateMillSecs = System.currentTimeMillis()
                        Flowable.create<PushInfo>({ emitter ->
                            emitter.onNext(pushInfo)
                            emitter.onComplete()
                        }, BackpressureStrategy.BUFFER)
                    }
        }
    }

    override fun requestBannerInfo(): Flowable<BannerInfo> {
        val localFlowable = LocalRequestRepository.loadLocalRequestRepository(context)
                .requestBannerTags()
        return if (localFlowable != null) {
            localFlowable
        } else {
            InternetRequestRepository.loadInternetRequestRepository(context)
                    .requestBannerTags()
                    .map(CommonResultMapper())
                    .map {
                        it.updateMillSecs = System.currentTimeMillis()
                        it
                    }
        }
    }

    override fun requestAuthAccessSync(): Boolean {
        try {
            val result = InternetRequestRepository.loadInternetRequestRepository(context).requestAuthAccessSync().execute().body()

            if (result != null && result.checkResultAvailable()) {
                Logger.e("鉴权请求结果正常！")

                val message = AESUtil.decrypt(result.data!!, Config.loadAccessKey())

                if (message != null && message.isNotEmpty()) {
                    val access = Gson().fromJson(message, Access::class.java)
                    if (access != null) {
                        if (access.publicKey != null) {
                            Config.insertPublicKey(access.publicKey!!)
                        }

                        if (access.privateKey != null) {
                            Config.insertPrivateKey(access.privateKey!!)
                        }

                        if (access.expire > 0 ) {
                            Config.insertAuthExpire(access.expire.toLong())
                        }
                    }
                    Logger.e("鉴权结果PublicKey: " + access.publicKey + " : " + access.privateKey)
                }

                return true
            } else {
                Logger.e("鉴权请求结果异常！")
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * 下载语音插件
     */
    fun downloadVoicePlugin(): Flowable<ResponseBody> {
        return InternetRequestRepository.loadInternetRequestRepository(context).downloadVoicePlugin()
    }

    /**
     * 获取兴趣列表
     */
    fun getInterestList(requestSubscriber: RequestSubscriber<BasicResult<List<Interest>>>) {
        InternetRequestRepository.loadInternetRequestRepository(context).getInterest()!!
                .compose(SchedulerHelper.schedulerHelper<BasicResult<List<Interest>>>())
                .subscribe({
                    if (it != null) {
                        when {
                            it.checkPrivateKeyExpire() -> requestAuthAccess {
                                if (it) {
                                    getInterestList(requestSubscriber)
                                }
                            }
                            it.checkResultAvailable() -> {
                                requestSubscriber.requestResult(it)
                            }
                        }
                    } else {
                        Logger.e(" 获取兴趣列表结果异常！")
                        requestSubscriber.requestError("获取兴趣列表结果异常")
                    }
                }, { throwable ->
                    throwable.printStackTrace()
                    Logger.e(" 获取兴趣列表异常: " + throwable.toString())
                }, {
                    Logger.e(" 获取兴趣列表完成！")
                })
    }
}