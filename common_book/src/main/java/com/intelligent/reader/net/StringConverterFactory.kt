package com.intelligent.reader.net

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.io.OutputStreamWriter
import java.lang.reflect.Type
import java.nio.charset.Charset

class StringRequestBodyConverter internal constructor() : Converter<String, RequestBody> {

    @Throws(IOException::class)
    override fun convert(value: String): RequestBody {
        val buffer = Buffer()
        val writer = OutputStreamWriter(buffer.outputStream(), UTF_8)
        writer.write(value)
        writer.close()
        return RequestBody.create(MEDIA_TYPE, buffer.readByteString())
    }

    companion object {
        private val MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8")
        private val UTF_8 = Charset.forName("UTF-8")
    }
}

class StringResponseBodyConverter : Converter<ResponseBody, String> {
    @Throws(IOException::class)
    override fun convert(value: ResponseBody): String {
        try {
            return value.string()
        } finally {
            value.close()
        }
    }
}

class StringConverterFactory private constructor() : Converter.Factory() {

    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>,
                                       retrofit: Retrofit): Converter<ResponseBody, *> {
        return StringResponseBodyConverter()
    }

    override fun requestBodyConverter(type: Type,
                                      parameterAnnotations: Array<Annotation>, methodAnnotations: Array<Annotation>, retrofit: Retrofit): Converter<*, RequestBody> {
        return StringRequestBodyConverter()
    }

    companion object {

        fun create(): StringConverterFactory {
            return StringConverterFactory()
        }
    }
}