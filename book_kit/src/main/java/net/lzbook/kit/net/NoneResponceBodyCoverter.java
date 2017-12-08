package net.lzbook.kit.net;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import net.lzbook.kit.data.NoBodyEntity;

import java.io.IOException;

import okhttp3.Response;
import retrofit2.Converter;

/**
 * Created by Administrator on 2017/11/21 0021.
 */

public class NoneResponceBodyCoverter<T> implements Converter<Response, NoBodyEntity> {

    private Gson gson;
    private TypeAdapter<T> adapter;

    NoneResponceBodyCoverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public NoBodyEntity convert(Response value) throws IOException {
        return new NoBodyEntity();
    }
}
