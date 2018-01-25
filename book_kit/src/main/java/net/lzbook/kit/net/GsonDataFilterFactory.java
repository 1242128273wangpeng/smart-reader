package net.lzbook.kit.net;

import com.google.gson.Gson;

import net.lzbook.kit.data.NoBodyEntity;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.SourceItem;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;


import io.reactivex.annotations.Nullable;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.Types;


/**
 * Created by Administrator on 2017/11/15 0015.
 */

public class GsonDataFilterFactory extends Converter.Factory {

    private final Gson gson;

    private GsonDataFilterFactory(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        this.gson = gson;
    }

    public static GsonDataFilterFactory create() {
        return create(new Gson());
    }

    public static GsonDataFilterFactory create(Gson gson) {
        return new GsonDataFilterFactory(gson);
    }

    @Nullable
    @Override
    public Converter<Response, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        //List<Chapter>
        Type listChapterType = Types.newParameterizedType(List.class, Chapter.class);

        //SourceItem
        if (listChapterType.equals(type) || type.toString().equals(listChapterType.toString())) {
            return new BookResponeBodyCoverter<>();
        } else if (SourceItem.class.equals(type)) {
            return new BookSourceBodyConverter();
        } else if (NoBodyEntity.class.equals(type)) {
            return new NoneResponceBodyCoverter<>();
        } else if (Chapter.class.equals(type)) {
            return new ChapterContentResponeBodyCoverter();
        }
        return null;
    }

    @Nullable
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return new BookRequestBodyConverter<>();
    }
}
