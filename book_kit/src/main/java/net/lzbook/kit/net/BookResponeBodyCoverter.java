package net.lzbook.kit.net;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.request.own.OWNParser;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import okhttp3.Response;
import retrofit2.Converter;

/**
 * Created by Administrator on 2017/11/15 0015.
 */

public class BookResponeBodyCoverter<T> implements Converter<Response, List<Chapter>> {

    @Override
    public List<Chapter> convert(Response value) throws IOException {
        RequestItem tag = (RequestItem) value.request().tag();
        try {
            return OWNParser.parserOwnChapterList(value.body().string(), tag);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IOException(e);
        }

    }
}
