package com.intelligent.reader.net

import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.user.CommonParamsInterceptor
import net.lzbook.kit.user.UserService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Administrator on 2017\9\4 0004.
 */
object NetOwnSearch {

    val ownSearchService: OwnSearchService
    val okHttpClient: OkHttpClient

    init {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC

        okHttpClient = OkHttpClient.Builder().addNetworkInterceptor(httpLoggingInterceptor)
                .addNetworkInterceptor(CommonParamsInterceptor())
                .build()

        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(UrlUtils.BOOK_NOVEL_DEPLOY_HOST).build()

        ownSearchService = retrofit.create(OwnSearchService::class.java)


    }

}