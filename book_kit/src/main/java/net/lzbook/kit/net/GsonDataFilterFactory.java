package net.lzbook.kit.net;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

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
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        //List<Chapter>
        Type listChapterType = Types.newParameterizedType(List.class, Chapter.class);
        //请求之后返回的结构什么都不返回
        Type stringNoneInfo = Types.newParameterizedType(NoBodyEntity.class, NoBodyEntity.class);
        //List<Chapter>
        Type chapterContent = Types.newParameterizedType(Chapter.class);
        //SourceItem
        if (listChapterType.equals(type)) {
            return new BookResponeBodyCoverter<>(gson, adapter);
        } else if (SourceItem.class.equals(type)) {
            return new BookSourceBodyConverter();
        } else if (NoBodyEntity.class.equals(type)) {
            return new NoneResponceBodyCoverter<>(gson, adapter);
        } else if (Chapter.class.equals(type)) {
            return new ChapterContentResponeBodyCoverter(gson, adapter);
        }
        return null;
    }

    @Nullable
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new BookRequestBodyConverter<>(gson, adapter);
    }
}
