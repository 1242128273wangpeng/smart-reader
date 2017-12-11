package net.lzbook.kit.net.custom.service

import net.lzbook.kit.net.GsonDataFilterFactory
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.net.custom.CommonParamsInterceptor
import net.lzbook.kit.net.custom.StringConverterFactory
import net.lzbook.kit.user.RecommendService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.properties.Delegates

/**
 * Created by yuchao on 2017/11/9 0009.
 */
object NetService {

    val okHttpClient: OkHttpClient
    var userService: UserService by Delegates.notNull<UserService>()
    var ownSearchService: OwnSearchService by Delegates.notNull<OwnSearchService>()
    var ownBookService: OwnBookService by Delegates.notNull<OwnBookService>()
    var recommendService: RecommendService by Delegates.notNull<RecommendService>()

    init {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC

        val httpLoggingBodyInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        okHttpClient = OkHttpClient.Builder().addNetworkInterceptor(httpLoggingInterceptor)
                .addNetworkInterceptor(httpLoggingBodyInterceptor)
                .addNetworkInterceptor(CommonParamsInterceptor())
                .build()

        initService()
    }

    fun initService() {
        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonDataFilterFactory.create())
                .addConverterFactory(StringConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(UrlUtils.getBookNovelDeployHost()).build()

        userService = retrofit.create(UserService::class.java)
        ownSearchService = retrofit.create(OwnSearchService::class.java)
        ownBookService = retrofit.create(OwnBookService::class.java)
        recommendService = retrofit.create(RecommendService::class.java)
    }

}