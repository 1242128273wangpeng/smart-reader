package net.lzbook.kit.net

import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.request.own.OWNParser
import okhttp3.Response
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.Types
import java.lang.reflect.Type

/**
 * Created by Xian on 2017/12/18.
 */
class BookConverterFactory : Converter.Factory() {

    val listChpaterType = Types.newParameterizedType(List::class.java, Chapter::class.java)

    override fun responseBodyConverter(type: Type, annotationArr: Array<Annotation>, retrofit: Retrofit): Converter<Response, *>? {

        val converter =  if (type.toString() == listChpaterType.toString()) {
            chapterConverter
        } else null

        return converter
    }

    object chapterConverter : Converter<Response, List<Chapter>>{
        override fun convert(response: Response?): List<Chapter> {
            val book = response!!.request().tag() as Book
            return OWNParser.parserOwnChapterList(response.body()!!.string(), RequestItem.fromBook(book))
        }
    }
}