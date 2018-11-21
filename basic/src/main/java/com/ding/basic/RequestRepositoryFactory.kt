package com.ding.basic

import android.annotation.SuppressLint
import android.arch.persistence.room.EmptyResultSetException
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.ding.basic.bean.*
import com.ding.basic.bean.push.BannerInfo
import com.ding.basic.bean.push.PushInfo
import com.ding.basic.db.repository.LocalRequestRepository
import com.ding.basic.net.RequestSubscriber
import com.ding.basic.net.ResultCode
import com.ding.basic.net.repository.InternetRequestRepository
import com.ding.basic.net.rx.CommonResultMapper
import com.ding.basic.net.rx.SchedulerHelper
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
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.ResourceSubscriber
import net.lzbook.kit.data.book.BookBrowseReqBody
import net.lzbook.kit.data.user.UserBook
import net.lzbook.kit.utils.user.bean.UserNameState
import net.lzbook.kit.utils.user.bean.WXAccess
import net.lzbook.kit.utils.user.bean.WXSimpleInfo
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 数据对外提供者（包含网络数据和DB数据）（Data Module）
 */
class RequestRepositoryFactory private constructor(private val context: Context) {

    private val localRepository = LocalRequestRepository.loadLocalRequestRepository(context)
    private val internetRepository = InternetRequestRepository.loadInternetRequestRepository()

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

    fun requestDefaultBooks(sex: Int, requestSubscriber: RequestSubscriber<Boolean>) {
        internetRepository.requestDefaultBooks(sex)!!
                .compose(SchedulerHelper.schedulerIOHelper<BasicResult<CoverList>>())
                .doOnNext {
                    if (it != null && it.checkResultAvailable() && it.data?.coverList != null && it.data?.coverList!!.isNotEmpty()) {
                        for (book in it.data?.coverList!!) {
                            if (!TextUtils.isEmpty(book.book_id)) {

                                val localBook = localRepository.checkBookSubscribe(book.book_id)

                                if (localBook == null) {
                                    localRepository.insertBook(book)
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
        InternetRequestRepository.loadInternetRequestRepository().requestDefaultBooks(firstType, secondType)!!
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

    fun requestApplicationUpdate(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<ApplicationUpdate>) {
        internetRepository.requestApplicationUpdate(parameters)!!
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

    fun requestDynamicCheck(requestSubscriber: RequestSubscriber<BasicResult<Int>>) {
        internetRepository.requestDynamicCheck()
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

    fun requestDynamicParameters(requestSubscriber: RequestSubscriber<Parameter>) {
        internetRepository.requestDynamicParameters()
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

    fun requestAdControlDynamic(requestSubscriber: RequestSubscriber<AdControlByChannelBean>) {
        internetRepository.requestAdControlDynamic()!!
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

    fun requestBookDetail(book_id: String, book_source_id: String, book_chapter_id: String, requestSubscriber: RequestSubscriber<Book>) {
        internetRepository.requestBookDetail(book_id, book_source_id, book_chapter_id)!!
                .compose(SchedulerHelper.schedulerHelper<BasicResult<Book>>())
                .subscribe({ result ->
                    if (result != null) {
                        when {
                            result.checkResultAvailable() -> {
                                requestSubscriber.onNext(result.data)

                                synchronized(RequestRepositoryFactory::class.java) {
                                    val localBook = localRepository.loadBook(book_id)

                                    if (localBook != null && !TextUtils.isEmpty(localBook.book_id)) {
                                        if (TextUtils.isEmpty(localBook.book_chapter_id) && !TextUtils.isEmpty(result.data?.book_chapter_id)) {
                                            localRepository.updateBookChapterId(localBook.book_id, result.data?.book_chapter_id!!)
                                        }
                                    }
                                }
                            }
                            else -> {
                                Logger.e("封面返回结果异常: $result")
                                requestSubscriber.onError(Throwable("获取书籍信息异常！"))
                            }
                        }
                    } else {
                        Logger.e("封面返回结果为空！")
                        requestSubscriber.onError(Throwable("获取书籍信息异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("请求书籍信息完成！")
                    requestSubscriber.onComplete()
                })
    }

    fun requestCatalog(book_id: String, book_source_id: String, book_chapter_id: String, requestSubscriber: RequestSubscriber<List<Chapter>>, type: Int) {
        localRepository.requestBookCatalog(book_id, book_source_id, book_chapter_id)
                .flatMap { result ->
                    if (result.data == null || result.data!!.chapters == null || result.data!!.chapters!!.isEmpty()) {
                        Logger.d("本地暂无章节目录，封装网络请求！")
                        internetRepository.requestBookCatalog(book_id, book_source_id, book_chapter_id)
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
                        val book = localRepository.checkBookSubscribe(book_id)

                        if (book != null) {
                            localRepository.insertOrUpdateChapter(book_id, resList)
                            book.chapter_count = localRepository.getCount(book_id)
                            if (result.data!!.listVersion!! > book.list_version) {
                                book.list_version = result.data!!.listVersion!!
                            }
                            if (result.data!!.contentVersion!! > book.c_version) {
                                book.c_version = result.data!!.contentVersion!!
                            }

                            val lastChapter = localRepository.queryLastChapter(book_id)

                            if (lastChapter != null) {
                                book.last_chapter = lastChapter
                            }
                            localRepository.updateBook(book)
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
                    if (it != null && (it.code == ResultCode.RESULT_SUCCESS || it.code == ResultCode.LOCAL_RESULT) && it.data != null) {
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
    fun requestBookCatalog(book_id: String, book_source_id: String, book_chapter_id: String, requestSubscriber: RequestSubscriber<List<Chapter>>, type: Int) {
        internetRepository.requestBookCatalog(book_id, book_source_id, book_chapter_id)
                .doOnNext { result ->
                    if (result != null && result.checkResultAvailable() && result.data?.chapters != null && result.data?.chapters!!.isNotEmpty()) {

                        for (chapter in result.data?.chapters!!) {
                            chapter.host = result.data!!.host
                            chapter.book_id = result.data!!.book_id
                            chapter.book_source_id = result.data!!.book_source_id
                            chapter.book_chapter_id = result.data!!.book_chapter_id
                        }

                        val book = localRepository.loadBook(book_id)

                        if (book != null) {
                            localRepository.deleteAllChapters(book_id)
                            localRepository.deleteBookMark(book_id)

                            localRepository.insertOrUpdateChapter(book_id, result.data?.chapters!!)

                            val lastChapter = localRepository.queryLastChapter(book_id)

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

                                localRepository.updateBook(book)
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
                    if (it != null && (it.code == ResultCode.RESULT_SUCCESS || it.code == ResultCode.LOCAL_RESULT) && it.data != null) {
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

    fun requestBookSources(book_id: String, book_source_id: String, book_chapter_id: String, requestSubscriber: RequestSubscriber<BookSource>) {
        internetRepository.requestBookSources(book_id, book_source_id, book_chapter_id)!!
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

    fun requestAutoComplete(word: String, requestSubscriber: RequestSubscriber<SearchAutoCompleteBean>) {
        internetRepository.requestAutoComplete(word)!!
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

    fun requestSearchRecommend(bookIds: String, requestSubscriber: RequestSubscriber<SearchRecommendBook>) {
        internetRepository.requestSearchRecommend(bookIds)!!
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

    fun requestAutoCompleteV4(word: String, requestSubscriber: RequestSubscriber<SearchAutoCompleteBeanYouHua>) {
        internetRepository.requestAutoCompleteV4(word)!!
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

    fun requestAutoCompleteV5(word: String, requestSubscriber: RequestSubscriber<SearchAutoCompleteBeanYouHua>) {
        internetRepository.requestAutoCompleteV5(word)!!
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

    fun requestSearchOperationV4(requestSubscriber: RequestSubscriber<Result<SearchResult>>) {
        internetRepository.requestHotWordsV4()
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

    fun requestHotWords(requestSubscriber: RequestSubscriber<SearchHotBean>) {
        internetRepository.requestHotWords()!!
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

    fun requestShareInformation(): Flowable<BasicResultV4<ShareInformation>>? {
        return internetRepository.requestShareInformation()
    }


    fun requestChapterContent(chapter: Chapter): Flowable<Chapter> {
        val content = ChapterCacheUtil.checkChapterCacheExist(chapter)
        return if (content == null || content.isEmpty() || content == "null") {
            internetRepository.requestChapterContent(chapter).map {
                when {
                    it.checkResultAvailable() -> {
                        if (it.data?.content != null && !TextUtils.isEmpty(it.data?.content)) {
                            it.data?.content = it.data?.content?.replace("\\n", "\n")
                            it.data?.content = it.data?.content?.replace("\\n \\n", "\n")
                            it.data?.content = it.data?.content?.replace("\\n\\n", "\n")
                            it.data?.content = it.data?.content?.replace("\\", "")
                        }
                        chapter.content = it.data?.content
                        chapter.defaultCode = it.data?.defaultCode ?: 0

                        chapter
                    }
                    else -> {
                        throw EmptyResultSetException("接口返回内容异常！: $it : ${chapter.name}")
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
        val basicResult = internetRepository.requestChapterContentSync(chapter.chapter_id, chapter.book_id, chapter.book_source_id, chapter.book_chapter_id)?.execute()?.body()
        if (basicResult != null) {
            if (basicResult.checkResultAvailable()) {
                if (basicResult.data?.content != null) {
                    basicResult.data?.content = basicResult.data?.content?.replace("\\n", "\n")
                    basicResult.data?.content = basicResult.data?.content?.replace("\\n \\n", "\n")
                    basicResult.data?.content = basicResult.data?.content?.replace("\\n\\n", "\n")
                    basicResult.data?.content = basicResult.data?.content?.replace("\\", "")
                }
                return basicResult.data?.content ?: ""
            } else {
                throw IllegalArgumentException("内容接口请求异常！")
            }
        }

        throw  IOException("${basicResult?.code} : ${basicResult?.msg}")
    }

    fun requestBookUpdate(checkBody: RequestBody, books: HashMap<String, Book>, requestSubscriber: RequestSubscriber<List<BookUpdate>>) {
        internetRepository.requestBookUpdate(checkBody)!!
                .compose(SchedulerHelper.schedulerIOHelper<BasicResult<UpdateBean>>())
                .subscribe({ result ->
                    if (result != null) {
                        val bookUpdates = ArrayList<BookUpdate>()
                        if (result.checkResultAvailable()) {
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


    fun requestCoverBatch(checkBody: RequestBody) {
        internetRepository.requestCoverBatch(checkBody)!!
                .compose(SchedulerHelper.schedulerIOHelper<BasicResult<List<Book>>>())
                .subscribe({
                    if (it != null) {
                        when {
                            it.checkResultAvailable() -> {
                                for (book in it.data!!) {
                                    if (!TextUtils.isEmpty(book.book_id) && !TextUtils.isEmpty(book.book_source_id) && !TextUtils.isEmpty(book.book_chapter_id)) {

                                        val localBook = loadBook(book.book_id)

                                        if (localBook != null) {
                                            localBook.status = book.status   //更新书籍状态
                                            localBook.book_chapter_id = book.book_chapter_id
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

                                            updateBook(localBook)
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

    fun requestBookShelfUpdate(checkBody: RequestBody, requestSubscriber: RequestSubscriber<Boolean>) {
        internetRepository.requestBookShelfUpdate(checkBody)!!
                .compose(SchedulerHelper.schedulerIOHelper<BasicResult<CoverList>>())
                .subscribe({ result ->
                    if (result != null) {
                        if (result.checkResultAvailable() && result.data?.coverList != null && result.data?.coverList!!.isNotEmpty()) {
                            val books: ArrayList<Book> = arrayListOf()

                            if (result.data?.fakeQingooBooks != null) {
                                result.data?.fakeQingooBooks?.forEach {
                                    if (it.checkValueValid()) {
                                        val book = localRepository.loadBook(it.from)
                                        if (book != null) {
                                            book.book_id = it.to

                                            localRepository.updateBook(book)

                                            localRepository.deleteAllChapters(it.from)
                                        }
                                    }
                                }
                            }

                            result.data?.coverList?.forEach {
                                val book = localRepository.checkBookSubscribe(it.book_id)
                                if (book != null) {
                                    if (!TextUtils.isEmpty(it.book_chapter_id)) {
                                        book.book_chapter_id = it.book_chapter_id

                                        book.host = it.host

                                        book.book_type = it.book_type

                                        // 保存在chapter表中
                                        if (book.fromQingoo()) {
                                            localRepository.updateBookSourceId(book.book_id, it.book_source_id)
                                        }

                                        localRepository.updateBookChapterId(book.book_id, it.book_chapter_id)
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

                                    val lastChapter = localRepository.queryLastChapter(book.book_id)

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
                                localRepository.updateBooks(books)
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

    fun requestFeedback(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<Boolean>) {
        internetRepository.requestFeedback(parameters)!!
                .compose(SchedulerHelper.schedulerIOHelper<NoBodyEntity>())
                .subscribe({
                    Logger.e("反馈发送！")
                }, {
                    Logger.e("反馈发送异常！")
                }, {
                    Logger.e("反馈发送完成！")
                })
    }


    fun requestLoginAction(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<LoginResp>) {
        internetRepository.requestLoginAction(parameters)!!
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

    fun requestSmsCode(mobile: String, requestSubscriber: RequestSubscriber<BasicResultV4<String>>) {
        internetRepository
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

    fun requestSmsLogin(smsBody: RequestBody, requestSubscriber: RequestSubscriber<BasicResultV4<LoginRespV4>>) {

        internetRepository
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
        internetRepository
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
        internetRepository
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
        internetRepository
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
        internetRepository
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
        internetRepository
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
        internetRepository
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
        internetRepository
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
        return internetRepository
                .uploadBookshelf(body)
    }


    /**
     * 获取刷新token
     */
    fun getRefreshToken(requestSubscriber: RequestSubscriber<BasicResultV4<LoginRespV4>>) {
        internetRepository
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
        val extendsBooks = ArrayList<Book>()
        localRepository.loadBooks()?.let { extendsBooks.addAll(it) }

        extendsBooks.sort()
        return extendsBooks
    }


    /**
     * 合并书架
     */
    private fun mergeBookShelf(data: List<UserBook>) {
        localRepository.deleteShelfBooks()

        if (data.isEmpty()) {
            return
        }
        for (book in data) {
            val saveBook = book.transToBook()
            localRepository.insertBook(saveBook)
        }
    }

    /**
     * 请求用户书架 ,同步书架-----------------------------------------------------------------结束
     */


    /**
     *  同步书签-----------------------------------------------------------------开始
     */

    fun keepBookMark(userId: String, onComplete: (() -> Unit)? = null) {
        internetRepository
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
        localRepository.deleteAllBookMark()
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
                localRepository.insertBookMark(item)
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
        return internetRepository
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
        return localRepository.getBookMarks(bookId)
    }

    /**
     *  同步书签-----------------------------------------------------------------结束
     */

    /**
     * 同步足迹-----------------------------------------------------------------开始
     */
    fun keepBookBrowse(userId: String, onComplete: (() -> Unit)? = null) {
        internetRepository
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
        localRepository.deleteAllHistory()

        if (data.isEmpty()) {
            return
        }

        for (remoteData in data) {
            val historyInfo = remoteData.transToHistoryInfo()
            localRepository.insertHistoryInfo(historyInfo)
        }
    }

    /**
     *  获取上传本地足迹Flowable
     */
    fun getUploadBookBrowseFlowable(userId: String): Flowable<BasicResultV4<String>> {
        val upLoadData = localRepository.queryHistoryPaging(0, 200)
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
        return internetRepository.uploadFootPrint(body)
    }


    /**
     * 同步足迹-----------------------------------------------------------------结束
     */


    fun thirdLogin(thirdBody: RequestBody, requestSubscriber: RequestSubscriber<BasicResultV4<LoginRespV4>>) {
        internetRepository
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
        internetRepository
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
        localRepository.loadBooks()
    }


    fun requestLogoutAction(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<JsonObject>) {
        internetRepository.requestLogoutAction(parameters)!!
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


    fun requestRefreshToken(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<RefreshResp>) {
        internetRepository.requestRefreshToken(parameters)!!
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

    fun requestUserInformation(token: String, appid: String, openid: String, requestSubscriber: RequestSubscriber<QQSimpleInfo>) {
        internetRepository.requestUserInformation(token, appid, openid)!!
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
        internetRepository.requestWXAccessToken(token, appid, openid, authorizationCode)
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
        internetRepository.requestWXUserInfo(token, openid)
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


    fun requestCoverRecommend(book_id: String, recommend: String, requestSubscriber: RequestSubscriber<CoverRecommendBean>) {
        internetRepository.requestCoverRecommend(book_id, recommend)!!
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


    fun requestBookRecommend(book_id: String, shelfBooks: String, requestSubscriber: RequestSubscriber<RecommendBooks>) {
        internetRepository.requestBookRecommend(book_id, shelfBooks)!!
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
    fun requestBookRecommendV4(book_id: String, recommend: String, requestSubscriber: RequestSubscriber<RecommendBooksEndResp>) {
        internetRepository.requestBookRecommendV4(book_id, recommend)!!
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
    fun requestAuthorOtherBookRecommend(author: String, book_id: String, requestSubscriber: RequestSubscriber<java.util.ArrayList<RecommendBean>>) {
        internetRepository.requestAuthorOtherBookRecommend(author, book_id)!!
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
     * 精选（推荐）首页 分类标签数据接口
     */
     fun requestRecommendCateList(packageName: String, categoryNames: String, requestSubscriber: RequestSubscriber<java.util.ArrayList<RecommendCateListBean>>) {
        InternetRequestRepository.loadInternetRequestRepository().requestRecommendCateList(packageName, categoryNames)!!
                .compose(SchedulerHelper.schedulerHelper())
                .subscribeWith(object : ResourceSubscriber<BasicResultV4<java.util.ArrayList<RecommendCateListBean>>>() {
                    override fun onNext(result: BasicResultV4<java.util.ArrayList<RecommendCateListBean>>?) {
                        if (result != null && result.checkResultAvailable()) {
                            requestSubscriber.onNext(result.data)
                        } else {
                            requestSubscriber.onError(Throwable("获取精选推荐分类标签书籍接口请求异常！"))
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
    fun requestSubBook(bookName: String, bookAuthor: String, requestSubscriber: RequestSubscriber<JsonObject>) {
        internetRepository.requestSubBook(bookName, bookAuthor)!!
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


    fun checkChapterCache(chapter: Chapter?): Boolean {
        if (chapter == null) {
            return false
        }

        return DataCache.isChapterCached(chapter)
    }


    fun checkBookSubscribe(book_id: String): Book? {
        return localRepository.checkBookSubscribe(book_id)
    }

    fun insertBook(book: Book): Long {
        return localRepository.insertBook(book)
    }

    fun updateBook(book: Book): Boolean {
        return localRepository.updateBook(book)
    }

    fun updateBooks(books: List<Book>): Boolean {
        return localRepository.updateBooks(books)
    }

    fun deleteBook(book_id: String): Boolean {
        return localRepository.deleteBook(book_id)
    }

    fun deleteBooks(books: List<Book>) {
        return localRepository.deleteBooks(books)
    }

    fun deleteBooksById(books: List<Book>) {
        return localRepository.deleteBooksById(books)
    }

    fun deleteShelfBook() {
        return localRepository.deleteShelfBooks()
    }

    fun loadBook(book_id: String): Book? {
        return localRepository.loadBook(book_id)
    }

    fun loadBooks(): List<Book>? {
        return localRepository.loadBooks()
    }

    fun loadReadBooks(): List<Book>? {
        return localRepository.loadReadBooks()
    }


    fun loadBookCount(): Long {
        return localRepository.loadBookCount()
    }

    fun loadBookShelfIDs(): String {
        return localRepository.loadBookShelfIDs()
    }

    fun loadBookmarkList(book_id: String, requestSubscriber: RequestSubscriber<List<Bookmark>>) {

    }


    fun insertBookFix(bookFix: BookFix) {
        return localRepository.insertBookFix(bookFix)
    }

    fun deleteBookFix(id: String) {
        return localRepository.deleteBookFix(id)
    }

    fun loadBookFixs(): List<BookFix>? {
        return localRepository.loadBookFixs()
    }

    fun loadBookFix(book_id: String): BookFix? {
        return localRepository.loadBookFix(book_id)
    }

    fun updateBookFix(bookFix: BookFix) {
        return localRepository.updateBookFix(bookFix)
    }

    fun insertOrUpdate(user: LoginRespV4) {
        localRepository.insertOrUpdate(user)
    }

    fun queryLoginUser(): LoginRespV4 {
        return localRepository.queryLoginUser()
    }

    fun deleteLoginUser() {
        localRepository.deleteLoginUser()
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
                    val book = localRepository.loadBook(it.book_id)
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
                        localRepository.updateBook(book)
                    }
                }
            }
        }

        if (fixContents != null && !fixContents.isEmpty()) {
            Flowable.fromIterable(fixContents)
                    .subscribeOn(Schedulers.io())
                    .filter { it.chapters != null && !TextUtils.isEmpty(it.book_id) }
                    .subscribe {
                        val book = localRepository.loadBook(it.book_id)
                        if (it.chapters != null && it.chapters!!.isNotEmpty() && book != null) {
                            var localNoChapterID = false

                            for (chapter in it.chapters!!) {
                                if (TextUtils.isEmpty(chapter.chapter_id)) {
                                    continue
                                }

                                val fixChapter = localRepository.getChapterById(it.book_id, chapter.chapter_id)

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

                                localRepository.updateChapter(it.book_id, fixChapter)
                            }

                            book.c_version = it.c_version
                            book.list_version = it.list_version
                            if (localNoChapterID) {
                                // 本地没有章节id的书籍,强制执行目录修复
                                book.force_fix = 1
                                book.list_version_fix = it.list_version
                            }
                            localRepository.updateBook(book)

                        }
                    }
        }
    }

    fun requestDownTaskConfig(bookID: String, bookSourceID: String
                              , type: Int, startChapterID: String
                              , requestSubscriber: RequestSubscriber<BasicResult<CacheTaskConfig>>) {
        internetRepository.requestDownTaskConfig(bookID, bookSourceID, type, startChapterID)!!
                .subscribeBy(
                        onNext = { ret ->
                            requestSubscriber.onNext(ret)
                        },

                        onError = { t ->
                            requestSubscriber.onError(t)
                        }

                )
    }

    fun requestPushTags(udid: String): Flowable<PushInfo> {
        val localFlowable = localRepository
                .requestPushInfo()
        return if (localFlowable != null) {
            localFlowable
        } else {
            internetRepository
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

    fun requestBannerInfo(): Flowable<BannerInfo> {
        val localFlowable = localRepository
                .requestBannerTags()
        return if (localFlowable != null) {
            localFlowable
        } else {
            internetRepository
                    .requestBannerTags()
                    .map(CommonResultMapper())
                    .map {
                        it.updateMillSecs = System.currentTimeMillis()
                        it
                    }
        }
    }

    fun downloadFont(fontName: String): Flowable<ResponseBody> {
        return internetRepository.downloadFont(fontName)
    }

    fun deleteAllBookMark() {
        localRepository.deleteAllBookMark()
    }

    fun deleteAllHistory() {
        localRepository.deleteAllHistory()
    }

    fun deleteBookMark(book_id: String) {
        localRepository.deleteBookMark(book_id)
    }

    fun deleteBookMark(ids: ArrayList<Int>) {
        localRepository.deleteBookMark(ids)
    }

    fun deleteBookMark(book_id: String, sequence: Int, offset: Int) {
        localRepository.deleteBookMark(book_id, sequence, offset)
    }

    fun getBookMarks(book_id: String): java.util.ArrayList<Bookmark> {
        return localRepository.getBookMarks(book_id)
    }

    fun isBookMarkExist(book_id: String, sequence: Int, offset: Int): Boolean {
        return localRepository.isBookMarkExist(book_id, sequence, offset)
    }

    fun insertBookMark(bookMark: Bookmark) {
        localRepository.insertBookMark(bookMark)
    }

    fun getHistoryCount(): Long {
        return localRepository.getHistoryCount()
    }

    fun insertOrUpdateHistory(historyInfo: HistoryInfo): Boolean {
        return localRepository.insertOrUpdateHistory(historyInfo)
    }

    fun deleteSmallTimeHistory() {
        localRepository.deleteSmallTimeHistory()
    }

    fun queryHistoryPaging(startNum: Long, limtNum: Long): java.util.ArrayList<HistoryInfo> {
        return localRepository.queryHistoryPaging(startNum, limtNum)
    }

    fun queryChapterBySequence(book_id: String, sequence: Int): Chapter? {
        return localRepository.queryChapterBySequence(book_id, sequence)
    }

    fun getChapterCount(book_id: String): Int {
        return localRepository.getChapterCount(book_id)
    }

    fun queryAllChapters(book_id: String): List<Chapter> {
        return localRepository.queryAllChapters(book_id)
    }

    fun queryLastChapter(book_id: String): Chapter? {
        return localRepository.queryLastChapter(book_id)
    }

    fun deleteChapters(book_id: String, sequence: Int) {
        localRepository.deleteChapters(book_id, sequence)
    }

    fun deleteAllChapters(book_id: String) {
        localRepository.deleteAllChapters(book_id)
    }

    fun insertOrUpdateChapter(book_id: String, chapterList: List<Chapter>): Boolean {
        return localRepository.insertOrUpdateChapter(book_id, chapterList)
    }

    fun updateChapterBySequence(book_id: String, chapter: Chapter) {
        localRepository.updateChapterBySequence(book_id, chapter)
    }

    fun getCount(book_id: String): Int {
        return localRepository.getCount(book_id)
    }

    /**
     * 升级数据库
     */
    fun upgradeBookDBFromOld(dbName: String): Flowable<Int> {
        return localRepository.upgradeBookDBFromOld(dbName)
    }

    fun upgradeChapterDBFromOld(book_ids: List<String>): Flowable<Int> {
        return localRepository.upgradeChapterDBFromOld(book_ids)
    }

    fun getAllWebFavorite(): List<WebPageFavorite> = localRepository.getAllWebFavorite()
            ?: emptyList()

    fun deleteAllWebFavorite() = localRepository.deleteAllWebFavorite()

    fun deleteWebFavoriteById(id: Int) = localRepository.deleteWebFavoriteById(id)

    /**
     * 添加网页收藏,最大只能10条
     */
    fun addWebFavorite(favorite: WebPageFavorite) {
        // 判断数量
        if (localRepository.getWebFavoriteCount() >= 10) {
            // 删除最老的一条
            localRepository.getAllWebFavorite()?.let { localRepository.deleteWebFavoriteById(it.last().id) }
        }
        localRepository.insertFavorite(favorite)
    }

    fun getWebFavoriteByTitleAndLink(title: String,web_link: String) = localRepository.getByTitleAndLink(title,web_link)

    fun deleteWebFavoriteList(list :List<WebPageFavorite>?){
        list?.forEach {
            deleteWebFavoriteById(it.id)
        }
    }
}