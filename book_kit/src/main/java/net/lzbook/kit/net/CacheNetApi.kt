package net.lzbook.kit.net

import com.quduquxie.network.DataService
import io.reactivex.Observable
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.CacheTaskConfig
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.net.custom.CommonParamsInterceptor
import net.lzbook.kit.request.RequestExecutorDefault
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.BeanParser
import net.lzbook.kit.utils.OpenUDID
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Created by Danny on 2017/12/17.
 */
object CacheNetApi {
    private val okhttp: OkHttpClient by lazy {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder().
                addNetworkInterceptor(httpLoggingInterceptor)
                .addNetworkInterceptor(CommonParamsInterceptor())
                /*.addNetworkInterceptor(StethoInterceptor())*/.build()
    }

    private val cacheApi: ICacheApi by lazy {
        Retrofit.Builder().addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(BookConverterFactory()).
                addConverterFactory(GsonConverterFactory.create())
                .client(okhttp)
                .baseUrl(UrlUtils.getBookNovelDeployHost())
                .build().create(ICacheApi::class.java)
    }




    fun getChapterList(book: Book): Observable<List<Chapter>> {
        if (Constants.QG_SOURCE.equals(book.site)) {
            return Observable.create<List<Chapter>> { emi->
                try {
                    val udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext())
                    val chapters = DataService.getChapterList(RequestExecutorDefault.mContext, book.book_id, 1, Integer.MAX_VALUE - 1, udid)
                    val list = BeanParser.buildOWNChapterList(chapters, 0, chapters.size)
                    if (list != null && !list.isEmpty()) {
                        emi.onNext(list)
                        emi.onComplete()
                    } else {
                        emi.onError(Exception("QG chapter list is null"))
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                    emi.onError(e)
                }
            }
        }
        return cacheApi.getChapterList(book.book_id, book.book_source_id, book)
    }



    fun getTaskConfig(book: Book, type: Int, chapter_id: String): Observable<NetResult<CacheTaskConfig>> {
        return cacheApi.getTaskConfig(book.book_id, book.book_source_id, type, chapter_id)
    }
}