package net.lzbook.kit.user

import io.reactivex.Observable
import net.lzbook.kit.data.bean.SourceItem
import net.lzbook.kit.net.custom.CommonParamsInterceptor
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.user.bean.RecommendBooksEndResp
import net.lzbook.kit.user.bean.RecommendBooksResp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


/**
 * 项目名称：11m
 * 类描述：
 * 创建人：Zach
 * 创建时间：2017/11/2 0002
 */

object RecommendServiceImp : RecommendService {
    override fun getBookSource(bookId: String?): Observable<SourceItem> {
        return mRecommendService.getBookSource(bookId)
    }

    val mOkHttpClient: OkHttpClient
    val mRecommendService: RecommendService

    init {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
        mOkHttpClient = OkHttpClient.Builder().addNetworkInterceptor(httpLoggingInterceptor).
                addNetworkInterceptor(CommonParamsInterceptor()).build()
        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(mOkHttpClient)
                .baseUrl(UrlUtils.BOOK_NOVEL_DEPLOY_HOST).build()

        RecommendServiceImp.mRecommendService = retrofit.create(RecommendService::class.java)
    }

    override fun getShelfRecommendBook(recommanded: String?): Observable<RecommendBooksResp> {
        return mRecommendService.getShelfRecommendBook(recommanded)
    }

    override fun getBookEndRecommendBook(recommanded: String?, bookId: String?): Observable<RecommendBooksEndResp> {
        return mRecommendService.getBookEndRecommendBook(recommanded, bookId)
    }

}