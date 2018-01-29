package net.lzbook.kit.net

import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Created by Xian on 2017/12/18.
 */
class FlieConverterFactory : Converter.Factory() {


    override fun responseBodyConverter(type: Type, annotationArr: Array<Annotation>, retrofit: Retrofit): Converter<Response, *>? {
        return stringValue
    }

    object stringValue : Converter<Response, ResponseBody> {
        override fun convert(response: Response?): ResponseBody? {
            return response?.body()
        }
    }
}