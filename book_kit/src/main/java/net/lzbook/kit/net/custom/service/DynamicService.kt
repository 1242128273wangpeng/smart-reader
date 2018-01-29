package net.lzbook.kit.net

import net.lzbook.kit.net.custom.CommonParamsInterceptor
import net.lzbook.kit.net.custom.service.DynamicApi
import net.lzbook.kit.request.UrlUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory


/**
 * Created by Danny on 2017/12/17.
 */
object DynamicService {
    private val okhttp: OkHttpClient by lazy {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder().
                addNetworkInterceptor(httpLoggingInterceptor)
                .addNetworkInterceptor(CommonParamsInterceptor())
                .build()
    }

    public val dynamicApi: DynamicApi by lazy {
        Retrofit.Builder().addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(FlieConverterFactory())
                .client(okhttp)
                .baseUrl(UrlUtils.getBookNovelDeployHost())
                .build().create(DynamicApi::class.java)
    }

}