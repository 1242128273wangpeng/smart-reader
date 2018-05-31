package com.ding.basic.repository

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.ding.basic.bean.*
import com.ding.basic.request.RequestSubscriber
import com.ding.basic.request.ResultCode
import com.ding.basic.rx.SchedulerHelper
import com.ding.basic.util.ChapterCacheUtil
import com.ding.basic.util.DataCache
import com.ding.basic.util.ParserUtil
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.ResourceSubscriber
import net.lzbook.kit.data.db.help.ChapterDaoHelper
import okhttp3.RequestBody
import org.json.JSONException
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

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

    override fun requestDefaultBooks(requestSubscriber: RequestSubscriber<Boolean>) {
        InternetRequestRepository.loadInternetRequestRepository(context).requestDefaultBooks()!!
                .compose(SchedulerHelper.schedulerIOHelper<BasicResult<CoverList>>())
                .doOnNext({
                    if (it != null && it.isAvalable() && it.data?.coverList != null && it.data?.coverList!!.isNotEmpty()) {
                        for (book in it.data?.coverList!!) {
                            if (!TextUtils.isEmpty(book.book_id)) {

                                val localBook = LocalRequestRepository.loadLocalRequestRepository(context).checkBookSubscribe(book.book_id)

                                if (localBook == null) {
                                    LocalRequestRepository.loadLocalRequestRepository(context).insertBook(book)
                                }
                            }
                        }
                    }
                })
                .subscribe({ result ->
                    if (result != null) {
                        if (result.isAvalable() && result.data?.coverList != null && result.data?.coverList!!.isNotEmpty()) {
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

    override fun requestDynamicParameters(requestSubscriber: RequestSubscriber<JsonObject>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestDynamicParameters()!!
                .compose(SchedulerHelper.schedulerIOHelper<JsonObject>())
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

    override fun requestBookDetail(book_id: String, book_source_id: String, book_chapter_id: String, requestSubscriber: RequestSubscriber<Book>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestBookDetail(book_id, book_source_id, book_chapter_id)!!
                .compose(SchedulerHelper.schedulerHelper<BasicResult<Book>>())
                .subscribe({ result ->
                    if (result != null) {
                        if (result.isAvalable()) {
                            requestSubscriber.onNext(result.data)

                            // 已加入书架 书架没有保存book_chapter_id, 在这里保存
                            synchronized(RequestRepositoryFactory::class.java) {
                                if (book_chapter_id == "" && checkBookSubscribe(result!!.data!!.book_id) != null) {
                                    val book = loadBook(result!!.data!!.book_id)
                                    book!!.book_chapter_id = result!!.data!!.book_chapter_id
                                    book!!.last_chapter = result!!.data!!.last_chapter
                                    book!!.desc = result!!.data!!.desc
                                    book!!.genre = result!!.data!!.genre
                                    book!!.sub_genre = result!!.data!!.sub_genre
                                    book!!.book_type = result!!.data!!.book_type

                                    updateBook(book!!)
                                    ChapterDaoHelper.loadChapterDataProviderHelper(context, book!!.book_id).updateBookChapterId(book!!.book_chapter_id)
                                }
                            }
                        } else {
                            requestSubscriber.onError(Throwable("获取书籍信息异常！"))
                        }
                    } else {
                        requestSubscriber.onError(Throwable("获取书籍信息异常！"))
                    }
                }, { throwable ->
                    requestSubscriber.onError(throwable)
                }, {
                    Logger.v("请求书籍信息完成！")
                })
    }

    override fun requestCatalog(book_id: String, book_source_id: String, book_chapter_id: String):Flowable<List<Chapter>> {
        return LocalRequestRepository.loadLocalRequestRepository(context = context).requestCatalog(book_id, book_source_id, book_chapter_id)
                .flatMap { result ->
                    if (result.data == null || result.data!!.chapters == null || result.data!!.chapters!!.isEmpty()) {
                        Logger.d("本地暂无章节目录，封装网络请求！")
                        InternetRequestRepository.loadInternetRequestRepository(context).requestCatalog(book_id, book_source_id, book_chapter_id)
                    } else {
                        Logger.d("本地章节目录正常！")
                        Flowable.create({ emitter ->
                            emitter.onNext(result)
                        }, BackpressureStrategy.BUFFER)
                    }
                }
                .doOnNext { result ->
                    if (result != null && result.isAvalable() && result.data!!.chapters != null && result.data!!.chapters!!.isNotEmpty()) {

                        for (chapter in result.data?.chapters!!) {
                            chapter.host = result.data?.host
                            chapter.book_id = result.data?.book_id!!
                            chapter.book_source_id = result.data?.book_source_id!!
                            chapter.book_chapter_id = result.data?.book_chapter_id!!
                        }

                        val book = LocalRequestRepository.loadLocalRequestRepository(context = context).checkBookSubscribe(book_id)

                        if (book != null) {
                            val chapterDaoHelp = ChapterDaoHelper.loadChapterDataProviderHelper(context, book_id)
                            chapterDaoHelp.insertOrUpdateChapter(result.data!!.chapters!!)
                            book.chapter_count = chapterDaoHelp.getCount()
                            val lastChapter = chapterDaoHelp.queryLastChapter()
                            if (lastChapter != null) {
                                book.last_chapter = lastChapter
                            }
                            LocalRequestRepository.loadLocalRequestRepository(context = context).updateBook(book)
                        } else {
                            var count = -1
                            result.data!!.chapters!!.forEach {
                                count++
                                it.sequence = count
                            }
                        }
                    }
                }
                .map<List<Chapter>> {result->
                    if ((result.code == ResultCode.OK || result.code == 100000) && result.data != null) {
                        result.data!!.chapters!!
                    }else{
                        throw Throwable("获取章节目录异常！")
                    }
                }
    }

    override fun requestBookSources(book_id: String, book_source_id: String, book_chapter_id: String, requestSubscriber: RequestSubscriber<BookSource>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestBookSources(book_id, book_source_id, book_chapter_id)!!
                .compose(SchedulerHelper.schedulerHelper<BasicResult<BookSource>>())
                .subscribe({ result ->
                    if (result != null) {
                        if (result.isAvalable()) {
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
                .debounce(400,TimeUnit.MILLISECONDS)
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

    override fun requestChapterContent(chapter: Chapter): Flowable<Chapter> {
        val content = ChapterCacheUtil.checkChapterCacheExist(chapter)
        return if (content == null || content.isEmpty()) {
            InternetRequestRepository.loadInternetRequestRepository(context = context).requestChapterContent(chapter).map {
                if (it.isAvalable()) {
                    if (it.data!!.content != null) {
                        it.data!!.content = it.data!!.content!!.replace("\\n", "\n")
                        it.data!!.content = it.data!!.content!!.replace("\\n \\n", "\n")
                        it.data!!.content = it.data!!.content!!.replace("\\n\\n", "\n")
                        it.data!!.content = it.data!!.content!!.replace("\\", "")
                    }
                    chapter.content = it.data!!.content
                    chapter
                } else {
                    chapter
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

    @Throws(IOException::class)
    fun requestChapterContentSync(chapter: Chapter): String {
        var basicResult = InternetRequestRepository.loadInternetRequestRepository(context = context).requestChapterContentSync(chapter.chapter_id, chapter.book_id, chapter.book_source_id, chapter.book_chapter_id)?.execute()?.body()
        if (basicResult != null && basicResult.isAvalable()) {

            if (basicResult.data!!.content != null) {
                basicResult.data!!.content = basicResult.data!!.content!!.replace("\\n", "\n")
                basicResult.data!!.content = basicResult.data!!.content!!.replace("\\n \\n", "\n")
                basicResult.data!!.content = basicResult.data!!.content!!.replace("\\n\\n", "\n")
                basicResult.data!!.content = basicResult.data!!.content!!.replace("\\", "")
            }
            return basicResult.data!!.content ?: ""
        }

        throw  IOException("${basicResult?.code} : ${basicResult?.msg}")
    }

    override fun requestBookUpdate(checkBody: RequestBody, books: HashMap<String, Book>, requestSubscriber: RequestSubscriber<List<BookUpdate>>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestBookUpdate(checkBody)!!
                .compose(SchedulerHelper.schedulerIOHelper<BasicResult<UpdateBean>>())
                .subscribe({ result ->
                    if (result != null) {
                        val bookUpdates = ArrayList<BookUpdate>()
                        if (result.isAvalable()) {
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


    override fun requestBookShelfUpdate(checkBody: RequestBody, requestSubscriber: RequestSubscriber<Boolean>) {
        InternetRequestRepository.loadInternetRequestRepository(context = context).requestBookShelfUpdate(checkBody)!!
                .compose(SchedulerHelper.schedulerIOHelper<BasicResult<CoverList>>())
                .subscribe({ result ->
                    if (result != null) {
                        if (result.isAvalable() && result.data?.coverList != null && result.data?.coverList!!.isNotEmpty()) {
                            val loadRepository = LocalRequestRepository.loadLocalRequestRepository(context)
                            var books: ArrayList<Book> = arrayListOf()
                            result.data?.coverList!!.forEach {
                                if (it != null && loadRepository.checkBookSubscribe(it.book_id) != null){
                                    val book = loadRepository.loadBook(it.book_id)
                                    if (book != null){
                                        if (!TextUtils.isEmpty(it.book_chapter_id)) {
                                            book.book_chapter_id = it.book_chapter_id

                                            // 保存在chapter表中
                                            if (book.host == "open.qingoo.cn") {
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
                                        if (it.last_chapter != null) {
                                            book.last_chapter = it.last_chapter
                                        }

                                        //青果书籍有可能book_source_id不对
                                        if (book.book_source_id == "api.qingoo.cn"){
                                            book.book_source_id = book.book_id
                                        }

                                        books.add(book)
                                    }

                                }
                            }
                            if (books.isNotEmpty()){
                                loadRepository.updateBooks(books)
                                requestSubscriber.onNext(true)
                            }else {
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


    fun isChapterCacheExist(chapter: Chapter?): Boolean {
        if (chapter == null) return false
        val chapterContent = DataCache.getChapterFromCache(chapter)
        return chapterContent != null
    }


    @Synchronized
    private fun handleFixInformation(fixBooks: List<BookFix>?, fixContents: List<FixContent>?) {
        if (fixBooks != null && fixBooks.isNotEmpty()) {
            Flowable.fromIterable(fixBooks)
                    .subscribeOn(Schedulers.io())
                    .filter({ !TextUtils.isEmpty(it.book_id) })
                    .subscribe({
                        if (it != null && !TextUtils.isEmpty(it.book_id)) {
                            val book = LocalRequestRepository.loadLocalRequestRepository(context).loadBook(it.book_id!!)
                            if (book != null && !TextUtils.isEmpty(book.book_id)) {
                                if (book.list_version == -1 || book.c_version == -1) {
                                    book.c_version = it.c_version
                                    book.list_version = it.list_version

                                    LocalRequestRepository.loadLocalRequestRepository(context).updateBook(book)
                                    Logger.v("更新书籍ListVersion和ContentVersion")
                                } else {
                                    val bookFix = BookFix()
                                    bookFix.book_id = it.book_id
                                    bookFix.c_version = it.c_version
                                    bookFix.list_version = it.list_version
                                    bookFix.fix_type = 2

                                    LocalRequestRepository.loadLocalRequestRepository(context).insertBookFix(bookFix)
                                    Logger.v("更新BookFix表")
                                }
                            }
                        }
                    })
        }

        if (fixContents != null && !fixContents.isEmpty()) {
            Flowable.fromIterable(fixContents)
                    .subscribeOn(Schedulers.io())
                    .filter { it.chapters != null && !TextUtils.isEmpty(it.book_id) }
                    .subscribe({
                        if (it.chapters != null && it.chapters!!.isNotEmpty()) {

                            val chapterDaoHelp = ChapterDaoHelper.loadChapterDataProviderHelper(context, it.book_id!!)

                            val contextFixState = ContextFixState()

                            var isNoChapterID = false

                            for (chapter in it.chapters!!) {
                                if (TextUtils.isEmpty(chapter.chapter_id)) {
                                    contextFixState.addMsgState(false)
                                    continue
                                }

                                val localChapter = chapterDaoHelp.getChapterById(chapter.chapter_id!!)

                                if (localChapter == null) {
                                    contextFixState.addMsgState(false)
                                    isNoChapterID = true
                                    continue
                                }

                                localChapter.book_id = it.book_id
                                localChapter.book_source_id = it.book_source_id
                                localChapter.book_chapter_id = it.book_chapter_id
                                localChapter.url = chapter.url
                                localChapter.name = chapter.name
                                localChapter.serial_number = chapter.serial_number
                                localChapter.word_count = chapter.word_count
                                localChapter.update_time = chapter.update_time
                                localChapter.vip = chapter.vip
                                localChapter.price = chapter.price

                                val isUpdateChapterByIdSucess = chapterDaoHelp.updateChapter(localChapter)

                                contextFixState.addMsgState(isUpdateChapterByIdSucess)

                                //2.修复章节缓存内容
                                fixChapterContent(localChapter, contextFixState)
                            }

                            if (contextFixState.fixState) {

                                val book = LocalRequestRepository.loadLocalRequestRepository(context).loadBook(it.book_id!!)

                                if (book != null && !TextUtils.isEmpty(book.book_id)) {
                                    book.c_version = it.c_version
                                    book.list_version = it.list_version

                                    LocalRequestRepository.loadLocalRequestRepository(context).updateBook(book)

                                    if (contextFixState.saveFixState) {
                                        val bookFix = BookFix()
                                        bookFix.book_id = book.book_id
                                        bookFix.fix_type = 1
                                        bookFix.c_version = it.c_version
                                        bookFix.list_version = it.list_version

                                        LocalRequestRepository.loadLocalRequestRepository(context).insertBookFix(bookFix)
                                    }
                                }
                            }

                            if (isNoChapterID) {
                                val bookFix = BookFix()
                                bookFix.book_id = it.book_id
                                bookFix.c_version = it.c_version
                                bookFix.list_version = it.list_version
                                bookFix.fix_type = 2
                                LocalRequestRepository.loadLocalRequestRepository(context).insertBookFix(bookFix)
                            }
                        }
                    })
        }
    }

    private fun fixChapterContent(chapter: Chapter?, fixState: ContextFixState) {
        if (chapter != null && DataCache.isNewCacheExists(chapter)) {

            try {
                chapter.content = RequestRepositoryFactory.loadRequestRepositoryFactory(context).requestChapterContentSync(chapter)

                var content = chapter.content
                if (TextUtils.isEmpty(content)) {
                    content = "null"
                }

                fixState.addContState(DataCache.fixChapter(content, chapter))
            } catch (e: Exception) {
                fixState.addContState(false)
                e.printStackTrace()
            }

        }
    }

    fun requestDownTaskConfig(bookID: String, bookSourceID: String
                              , type: Int, startChapterID: String): Flowable<BasicResult<CacheTaskConfig>>? {
        return InternetRequestRepository.loadInternetRequestRepository(context).requestDownTaskConfig(bookID, bookSourceID, type, startChapterID)
    }
}