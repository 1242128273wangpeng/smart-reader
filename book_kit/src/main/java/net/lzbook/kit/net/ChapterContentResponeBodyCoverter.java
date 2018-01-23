package net.lzbook.kit.net;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.utils.AppLog;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Response;
import retrofit2.Converter;

/**
 * Created by Administrator on 2017/11/15 0015.
 */

public class ChapterContentResponeBodyCoverter implements Converter<Response, Chapter> {

    @Override
    public Chapter convert(Response value) throws IOException {
        Chapter chapter = (Chapter) value.request().tag();
        try {
            JSONObject jsonObject = new JSONObject(value.body().string());
            if (jsonObject.opt("content") != null) {
                chapter.content = jsonObject.getString("content");
            }
//            if(!jsonObject.isNull("good")){
//                chapter.mGold=(jsonObject.getInt("good"));
//            }
//            if(!jsonObject.isNull("price")){
//                chapter.mPrice=(jsonObject.getInt("price"));
//            }
            if (!jsonObject.isNull("word_count")) {
                chapter.word_count = (jsonObject.getInt("word_count"));
            }
//            if(!jsonObject.isNull("normal")){
//                chapter.mExContent=(jsonObject.getInt("normal"));
//            }
//            if(!jsonObject.isNull("auto_flag")){
//                chapter.mAutoFlag=(jsonObject.getString("auto_flag"));
//                AppLog.e("自动购买","Content接口返回的chapter.mAutoFlagw:"+chapter.mAutoFlag);
//            }
            if (!TextUtils.isEmpty(chapter.content)) {
                chapter.content = chapter.content.replace("\\n", "\n");
                chapter.content = chapter.content.replace("\\n\\n", "\n");
                chapter.content = chapter.content.replace("\\n \\n", "\n");
                chapter.content = chapter.content.replace("\\", "");
            }
            return chapter;
        } catch (JSONException e) {
            e.printStackTrace();
            chapter.status = Chapter.Status.SOURCE_ERROR;
            throw new IOException(e);
        }
    }
}
