package net.lzbook.kit.net;

import net.lzbook.kit.data.NoBodyEntity;

import java.io.IOException;

import okhttp3.Response;
import retrofit2.Converter;

/**
 * Created by Administrator on 2017/11/21 0021.
 */

public class NoneResponceBodyCoverter<T> implements Converter<Response, NoBodyEntity> {


    @Override
    public NoBodyEntity convert(Response value) throws IOException {
        return new NoBodyEntity();
    }
}
