package net.lzbook.kit.request.own;

import net.lzbook.kit.constants.Constants;
import com.ding.basic.bean.Book;
import com.ding.basic.bean.BookUpdate;
import com.ding.basic.bean.Chapter;
import com.ding.basic.bean.UpdateBean;
import com.ding.basic.bean.UpdateBook;

import net.lzbook.kit.data.bean.LogData;
import net.lzbook.kit.data.bean.ParseJarBean;
import net.lzbook.kit.data.bean.Source;
import net.lzbook.kit.data.bean.SourceItem;
import net.lzbook.kit.repair_books.RepairHelp;
import net.lzbook.kit.utils.AppLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class OWNParser {

    private static final String TAG = OWNParser.class.getSimpleName();
    private static final String DOWN_INDEX = "down_index";

    public static ArrayList<Chapter> parserOwnChapterList(String jsonString, Book book) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        if (!jsonObject.getBoolean("success")) {
            return null;
        }

        try {
            if (!jsonObject.isNull("update_type")) {
//                requestItem.update_type = jsonObject.getInt("update_type");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<Chapter> chapterList = new ArrayList<>();
        JSONArray jsonArray = jsonObject.getJSONArray("items");
        for (int i = 0; i < jsonArray.length(); i++) {
            Chapter chapter = new Chapter();
            JSONObject chapterObject = jsonArray.getJSONObject(i);
            chapter.setBook_id(book.getBook_id());
            chapter.setBook_source_id(chapterObject.getString("book_souce_id"));
            chapter.setChapter_id(chapterObject.getString("id"));
            chapter.setChapter_status(chapterObject.getString("status"));
            chapter.setName(chapterObject.getString("name"));
            chapter.setHost(chapterObject.getString("host"));
            chapter.setUpdate_time(chapterObject.getLong("update_time"));
            chapter.setSequence(i);
            chapterList.add(chapter);
        }
        return chapterList;
    }

    public static ArrayList<Chapter> parserOwnChapterListNew(String jsonString, Book bookVo) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        if (!jsonObject.getBoolean("success")) {
            return null;
        }

        ArrayList<Chapter> chapterList = new ArrayList<>();
        JSONArray jsonArray = jsonObject.getJSONArray("items");
        for (int i = 0; i < jsonArray.length(); i++) {
            Chapter chapter = new Chapter();
            JSONObject chapterObject = jsonArray.getJSONObject(i);
            chapter.setBook_id(bookVo.getBook_id());
            chapter.setBook_source_id(chapterObject.getString("book_souce_id"));
            chapter.setChapter_id(chapterObject.getString("id"));
            chapter.setChapter_status(chapterObject.getString("status"));
            chapter.setName(chapterObject.getString("name"));
            chapter.setHost(chapterObject.getString("host"));
            chapter.setUpdate_time(chapterObject.getLong("update_time"));
            chapter.setSequence(i);
            chapterList.add(chapter);
        }
        return chapterList;
    }

    public static SourceItem parserBookSource(String jsonString, String book_id) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);

        if (!jsonObject.getBoolean("success")) {
            return null;
        }
        SourceItem sourceItem = new SourceItem();
        sourceItem.success = jsonObject.getBoolean("success");
        sourceItem.error_log = jsonObject.getString("error_log");
        sourceItem.params = jsonObject.getString("params");
        sourceItem.total = jsonObject.getInt("total");
        sourceItem.book_id = book_id;

        if (!jsonObject.isNull("items")) {
            JSONArray sourcesJsonArray = jsonObject.getJSONArray("items");
            sourceItem.sourceList = new ArrayList<>();
            for (int i = 0; i < sourcesJsonArray.length(); i++) {
                Source sourceBean = new Source();
                JSONObject sourceObject = sourcesJsonArray.getJSONObject(i);
                sourceBean.book_id = sourceObject.getString("book_id");
                sourceBean.book_source_id = sourceObject.getString("book_source_id");
                sourceBean.host = sourceObject.getString("host");
                sourceBean.terminal = sourceObject.getString("terminal");
                sourceBean.dex = sourceObject.getInt("dex");
                if (!sourceObject.isNull("last_chapter")) {
                    JSONObject lastChapter = sourceObject.getJSONObject("last_chapter");
                    sourceBean.last_chapter_name = lastChapter.getString("name");
                    sourceBean.update_time = lastChapter.getLong("update_time");
                }
                JSONObject source = sourceObject.getJSONObject("source");
                LinkedHashMap<String, String> bookSource = new LinkedHashMap<>();
                Iterator<String> keys = source.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = (String) source.get(key);
                    bookSource.put(key, value);
                }
                sourceBean.source = bookSource;
                sourceItem.sourceList.add(sourceBean);
            }
        }
        return sourceItem;
    }

    public static HashMap<String, List<Source>> parserBooksSource(String jsonString, String book_id) throws JSONException {
        HashMap<String, List<Source>> listHashMap = new HashMap<>();

        ArrayList<String> bookIdList = new ArrayList<>();
        if (!TextUtils.isEmpty(book_id)) {
            String[] book_ids = book_id.split("\\$\\$");

            for (int i = 0; i < book_ids.length; i++) {
                bookIdList.add(book_ids[i]);
            }
        }

        JSONObject jsonObject = new JSONObject(jsonString);
        if (!jsonObject.getBoolean("success")) {
            return null;
        }

        if (!jsonObject.isNull("shift_host")) {
            Constants.USER_TRANFER_DESTINATION = jsonObject.getString("shift_host");
        }

        if (!jsonObject.isNull("items")) {
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray sourceJsonArray = jsonArray.getJSONArray(i);
                ArrayList<Source> sourceList = new ArrayList<>();
                for (int j = 0; j < sourceJsonArray.length(); j++) {
                    JSONObject sourceObject = sourceJsonArray.getJSONObject(j);
                    Source source = new Source();
                    source.book_id = sourceObject.getString("book_id");
                    source.book_source_id = sourceObject.getString("book_source_id");
                    source.host = sourceObject.getString("host");
                    source.url = sourceObject.getString("url");
                    source.terminal = sourceObject.getString("terminal");
                    source.dex = sourceObject.getInt("dex");
                    if (!sourceObject.isNull("last_chapter")) {
                        JSONObject lastChapter = sourceObject.getJSONObject("last_chapter");
                        source.last_chapter_name = lastChapter.getString("name");
                        source.update_time = lastChapter.getLong("update_time");
                    }
                    JSONObject sourceJSONObject = sourceObject.getJSONObject("source");
                    LinkedHashMap<String, String> bookSource = new LinkedHashMap<>();
                    Iterator<String> keys = sourceJSONObject.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String value = (String) sourceJSONObject.get(key);
                        bookSource.put(key, value);
                    }
                    source.source = bookSource;
                    sourceList.add(source);
                }
                try {
                    String bookId = sourceJsonArray.getJSONObject(0).getString("book_id");
                    if (bookIdList.contains(bookId)) {
                        listHashMap.put(bookId, sourceList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return listHashMap;
    }

    public static String logDataToJson(ArrayList<LogData> logDataList) {
        String jsonResult = "";
        try {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < logDataList.size(); i++) {
                LogData logData = logDataList.get(i);
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("type", logData.type);
                jsonObj.put("bookName", logData.bookName);
                jsonObj.put("authorName", logData.site);
                jsonObj.put("site", logData.site);
                jsonObj.put("bookId", logData.bookId);
                jsonObj.put("bookSourceId", logData.bookSourceId);
                jsonArray.put(jsonObj);
            }
            jsonResult = jsonArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonResult;
    }

    public static int getStartDownIndex(Context ctt, int gid) {
        SharedPreferences prefer = ctt.getSharedPreferences(DOWN_INDEX, Context.MODE_PRIVATE);
        return prefer.getInt(String.valueOf(gid), -1);
    }

    public static void writeDownIndex(Context ctt, String book_id, boolean fromMark, int downIndex) {
        SharedPreferences prefer = ctt.getSharedPreferences(DOWN_INDEX, Context.MODE_PRIVATE);
        if (downIndex < 0) {
            downIndex = 0;
        }
        prefer.edit().putInt(book_id, downIndex).apply();
    }

    public static ArrayList<Book> parserOwnUpdateShelfBooks(String json) throws JSONException {
        ArrayList<Book> books = new ArrayList<>();
        AppLog.i(TAG, "parserOwnUpdateShelfBooks Res: " + json);
        if (!TextUtils.isEmpty(json)) {
            JSONObject updateObject = new JSONObject(json);
            if (!updateObject.getBoolean("success")) {
                return books;
            }

            JSONArray jsonArray = updateObject.getJSONArray("covers");
            for (int i = 0; i < jsonArray.length(); i++) {
                Book book = new Book();
                JSONObject obj = jsonArray.getJSONObject(i).getJSONObject("book_vo");
                if (!obj.isNull("book_id")) {
                    book.setBook_id(obj.getString("book_id"));
                }
                book.setStatus(obj.getString("status"));

                books.add(book);
            }
        }
        return books;
    }

    public static JSONObject parserDynamic(String json) throws JSONException {
        if (!TextUtils.isEmpty(json)) {
            JSONObject jsonObject = new JSONObject(json);
            if (!jsonObject.getBoolean("success")) {
                return null;
            }
            return jsonObject.getJSONObject("map");
        }
        return null;
    }


    public static LogData logDataEncap(String type, Book book) {
        LogData logData = new LogData();
        logData.type = type;
        logData.bookName = book.getName();
        logData.authorName = book.getAuthor();
        logData.site = book.getHost();
        logData.bookId = book.getBook_id();
        logData.bookSourceId = book.getBook_source_id();
        return logData;
    }

    /*public static SensitiveWords getSensitiveWords(String json) throws JSONException {
        JSONObject jsonRoot = new JSONObject(json);
        if (!jsonRoot.getBoolean("sucess") || jsonRoot.isNull("model")) {
            return null;
        }

        SensitiveWords sensitiveWords = new SensitiveWords();
        sensitiveWords.sucess = jsonRoot.getBoolean("sucess");
        JSONObject jsonObject = jsonRoot.optJSONObject("model");
        sensitiveWords.type = jsonObject.getString("type");
        sensitiveWords.digest = jsonObject.getString("digest");
        JSONArray jsonArray = jsonObject.getJSONArray("list");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add((String) jsonArray.get(i));
        }
        sensitiveWords.list = list;
        AppLog.e(TAG, "SensitiveWords: " + list.toString());
        return sensitiveWords;
    }*/
}