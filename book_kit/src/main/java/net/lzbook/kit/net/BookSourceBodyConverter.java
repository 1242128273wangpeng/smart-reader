package net.lzbook.kit.net;

import net.lzbook.kit.data.bean.SourceItem;
import net.lzbook.kit.request.own.OWNParser;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.Response;
import retrofit2.Converter;

/**
 * @author lijun Lee
 * @desc {@link SourceItem} Converter
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/20 18:44
 */

public class BookSourceBodyConverter implements Converter<Response, SourceItem> {

    @Override
    public SourceItem convert(Response value) throws IOException {
        try {
            return OWNParser.parserBookSource(value.body().string(), null);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }
}
