package com.intelligent.reader.net

import io.reactivex.Observable
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.request.own.OWNParser
import net.lzbook.kit.user.CommonParamsInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by xian on 2017/8/19.
 */
object NetOwnBook {

    val ownBookService: OwnBookService
    val okHttpClient: OkHttpClient

    init {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC

        okHttpClient = OkHttpClient.Builder().addNetworkInterceptor(httpLoggingInterceptor)
                .addNetworkInterceptor(CommonParamsInterceptor())
                .build()

        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(StringConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(UrlUtils.BOOK_NOVEL_DEPLOY_HOST).build()

        ownBookService = retrofit.create(OwnBookService::class.java)

    }


    fun requestOwnCatalogList(book: Book): Observable<List<Chapter>> {
        val observable = ownBookService.requestOwnCatalogList(book.book_id, book.book_source_id).map { t: String? ->
            var ownChapterList = listOf<Chapter>()
            if (t != null) {
                ownChapterList = OWNParser.parserOwnChapterList(t, RequestItem.fromBook(book))

                //更新数据库
                val bookDaoHelper = BookDaoHelper.getInstance(BaseBookApplication.getGlobalContext())
                if (!ownChapterList.isEmpty() &&
                        bookDaoHelper.isBookSubed(book.book_id)) {
                    val chapterDao = BookChapterDao(BaseBookApplication.getGlobalContext(), book.book_id)
                    chapterDao.insertBookChapter(ownChapterList)
                    val lastChapter = ownChapterList.get(ownChapterList.size - 1)
                    val book = Book()
                    book.book_id = book.book_id
                    book.book_source_id = book.book_source_id
                    book.site = book.site
                    book.chapter_count = chapterDao.getCount()
                    book.last_updatetime_native = lastChapter.time
                    book.last_chapter_name = lastChapter.chapter_name
                    book.last_chapter_md5 = lastChapter.book_chapter_md5
                    book.last_updateSucessTime = System.currentTimeMillis()
                    bookDaoHelper.updateBook(book)
                }

            } else {
                ownChapterList = listOf<Chapter>()
            }

            ownChapterList
        }

        return observable
    }

}