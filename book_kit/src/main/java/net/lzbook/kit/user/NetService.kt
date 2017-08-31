package net.lzbook.kit.user

import net.lzbook.kit.request.UrlUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Created by xian on 2017/6/26.
 */
object NetService {

    val userService: UserService
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

        userService = retrofit.create(UserService::class.java)


    }

}