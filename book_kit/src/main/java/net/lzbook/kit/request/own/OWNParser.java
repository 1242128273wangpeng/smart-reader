package net.lzbook.kit.request.own;

import com.google.gson.Gson;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookUpdate;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.CoverPage;
import net.lzbook.kit.data.bean.LogData;
import net.lzbook.kit.data.bean.ParseJarBean;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.bean.Source;
import net.lzbook.kit.data.bean.SourceItem;
import net.lzbook.kit.data.update.UpdateBean;
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

    /**
     * 自有数据的解析
     **/
    public static CoverPage parserOwnCoverInfo(String jsonString) throws JSONException {
        CoverPage coverPage = new CoverPage();

        JSONObject jsonObject = new JSONObject(jsonString);
        if (!jsonObject.getBoolean("success")) {
            return null;
        }

        CoverPage.BookVoBean bookVo = new CoverPage.BookVoBean();
        JSONObject bookVoObject = jsonObject.getJSONObject("book_vo");
        bookVo.name = bookVoObject.getString("name");
        bookVo.author = bookVoObject.getString("author");
        bookVo.desc = bookVoObject.getString("desc");
        bookVo.labels = bookVoObject.getString("labels");
        bookVo.img_url = bookVoObject.getString("img_url");
        bookVo.url = bookVoObject.getString("url");

        if (!bookVoObject.isNull("status")) {
            if ("SERIALIZE".equals(bookVoObject.getString("status"))) {
                bookVo.status = 1;
            } else if ("FINISH".equals(bookVoObject.getString("status"))) {
                bookVo.status = 2;
            }
        }

        if (!bookVoObject.isNull("source")) {
            JSONObject sourceObject = bookVoObject.getJSONObject("source");
            if (!sourceObject.isNull("gid")) {
                bookVo.parameter = sourceObject.getString("gid");
            }

            if (!sourceObject.isNull("md")) {
                bookVo.parameter = sourceObject.getString("md");
            }
        }

        bookVo.book_id = bookVoObject.getString("book_id");
        bookVo.book_source_id = bookVoObject.getString("book_source_id");
        bookVo.host = bookVoObject.getString("host");
        bookVo.dex = bookVoObject.getInt("dex");

        if (!bookVoObject.isNull("last_chapter")) {
            JSONObject chapterObject = bookVoObject.getJSONObject("last_chapter");
            bookVo.serial_number = chapterObject.getInt("serial_number");
            bookVo.update_time = chapterObject.getLong("update_time");
            bookVo.last_chapter_name = chapterObject.getString("name");
        }

        JSONArray sourceArray = jsonObject.getJSONArray("sources");
        List<CoverPage.SourcesBean> sourcesBeanList = new ArrayList<>();
        for (int i = 0; i < sourceArray.length(); i++) {
            CoverPage.SourcesBean sourcesBean = new CoverPage.SourcesBean();
            JSONObject sourceObject = sourceArray.getJSONObject(i);
            sourcesBean.book_id = sourceObject.getString("book_id");
            sourcesBean.book_source_id = sourceObject.getString("book_source_id");
            sourcesBean.host = sourceObject.getString("host");
            //sourcesBean.url = sourceObject.getString("url");
            sourcesBean.terminal = sourceObject.getString("terminal");
            sourcesBean.update_time = sourceObject.getLong("update_time");
            JSONObject bookSourceObject = sourceObject.getJSONObject("bookSourceVO");
            sourcesBean.dex = bookSourceObject.getInt("dex");

            if (!bookSourceObject.isNull("label")) {
                sourcesBean.labels = bookSourceObject.getString("label");
            }

            if (!bookSourceObject.isNull("wordCountDescp")) {
                sourcesBean.wordCountDescp = bookSourceObject.getString("wordCountDescp");
            }
            if (!bookSourceObject.isNull("readerCountDescp")) {
                sourcesBean.readerCountDescp = bookSourceObject.getString("readerCountDescp");
            }

            if (!bookSourceObject.isNull("score")) {
                sourcesBean.score = bookSourceObject.getDouble("score");
            }

            if (!sourceObject.isNull("last_chapter")) {
                JSONObject chapterObject = sourceObject.getJSONObject("last_chapter");
                sourcesBean.last_chapter_name = chapterObject.getString("name");
                sourcesBean.update_time = chapterObject.getLong("updateTime");
            }
            JSONObject sourceJSONObject = sourceObject.getJSONObject("source");
            HashMap<String, String> bookSource = new HashMap<>();
            Iterator<String> keys = sourceJSONObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = (String) sourceJSONObject.get(key);
                bookSource.put(key, value);
            }
            sourcesBean.source = bookSource;
            sourcesBeanList.add(sourcesBean);

        }

        coverPage.bookVo = bookVo;
        coverPage.sources = sourcesBeanList;
        return coverPage;
    }

    public static ArrayList<Chapter> parserOwnChapterList(String jsonString, RequestItem requestItem) throws JSONException {
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
            chapter.book_id = requestItem.book_id;
            chapter.book_source_id = chapterObject.getString("book_souce_id");
            chapter.chapter_id = chapterObject.getString("id");
            chapter.chapter_status = chapterObject.getString("status");
            chapter.parameter = requestItem.parameter;
            chapter.extra_parameter = requestItem.extra_parameter;
            chapter.chapter_name = chapterObject.getString("name");
            chapter.sort = chapterObject.getInt("serial_number");
            chapter.site = chapterObject.getString("host");
            chapter.curl = chapterObject.getString("url");
            chapter.curl1 = chapterObject.getString("url1");
            chapter.time = chapterObject.getLong("update_time");
            chapter.api_url = chapter.curl;
            chapter.chapter_form = 1;
            chapter.sequence = i;
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
            chapter.book_id = bookVo.book_id;
            chapter.book_source_id = chapterObject.getString("book_souce_id");
            chapter.chapter_id = chapterObject.getString("id");
            chapter.chapter_status = chapterObject.getString("status");
            chapter.parameter = bookVo.parameter;
            chapter.extra_parameter = bookVo.extra_parameter;
            chapter.chapter_name = chapterObject.getString("name");
            chapter.sort = chapterObject.getInt("serial_number");
            chapter.site = chapterObject.getString("host");
            chapter.curl = chapterObject.getString("url");
            chapter.curl1 = chapterObject.getString("url1");
            chapter.time = chapterObject.getLong("update_time");
            chapter.api_url = chapter.curl;
            chapter.chapter_form = 1;
            chapter.sequence = i;
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

    public static ArrayList<Book> parserOwnDefaultBook(String json, Context context) throws JSONException {
        ArrayList<Book> books = new ArrayList<>();
        AppLog.i(TAG, "parserOwnDefaultBook Res" + json);
        if (!TextUtils.isEmpty(json)) {
            JSONObject defaultObject = new JSONObject(json);
            JSONArray jsonArray = defaultObject.getJSONArray("book_vos");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Book iBook = new Book();
                iBook.name = jsonObject.getString("name");
                iBook.author = jsonObject.getString("author");
                iBook.desc = jsonObject.getString("desc");
                iBook.category = jsonObject.getString("labels");

                iBook.img_url = jsonObject.getString("img_url");

                if ("FINISH".equals(jsonObject.getString("status"))) {
                    iBook.status = 2;
                } else {
                    iBook.status = 1;
                }

                iBook.book_id = jsonObject.getString("book_id");
                iBook.book_source_id = jsonObject.getString("book_source_id");
                iBook.site = jsonObject.getString("host");
                if (!jsonObject.isNull("dex")) {
                    iBook.dex = Integer.parseInt(jsonObject.getString("dex"));
                }

                if (!jsonObject.isNull("source")) {
                    JSONObject sourceObject = jsonObject.getJSONObject("source");
                    Iterator<String> keys = sourceObject.keys();
                    ArrayList<String> list = new ArrayList<>();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String value = (String) sourceObject.get(key);
                        list.add(value);
                    }
                    if (list.size() > 0) {
                        iBook.parameter = list.get(0);
                    }
                    if (list.size() > 1) {
                        iBook.extra_parameter = list.get(1);
                    }
                }

                JSONObject chapterObject = jsonObject.getJSONObject("last_chapter");
                iBook.last_chapter_name = chapterObject.getString("name");
                iBook.chapter_count = chapterObject.getInt("serial_number");
                if (!chapterObject.isNull("update_time")) {
                    iBook.last_updatetime_native = chapterObject.getLong("update_time");
                }
                if (!chapterObject.isNull("url")) {
                    iBook.last_chapter_url = chapterObject.getString("url");
                }
                if (!chapterObject.isNull("url1")) {
                    iBook.last_chapter_url1 = chapterObject.getString("url1");
                }
                iBook.book_type = 0;
                iBook.insert_time = System.currentTimeMillis();
                iBook.last_checkupdatetime = iBook.insert_time;
                iBook.last_updateSucessTime = iBook.insert_time;
                books.add(iBook);
            }
        }
        return books;
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
                    book.book_id = obj.getString("book_id");
                }
                if ("FINISH".equals(obj.getString("status"))) {
                    book.status = 2;
                } else {
                    book.status = 1;
                }
                if (!obj.isNull("dex")) {
                    book.dex = obj.getInt("dex");
                }
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

    public static ArrayList<BookUpdate> parserBookUpdateInfo(String json, HashMap<String, Book> bookItems) throws Exception {
        ArrayList<BookUpdate> lists = new ArrayList<>();
        AppLog.i(TAG, "parserBookUpdateInfo checkRes" + json);
        JSONObject jsonObject = new JSONObject(json);
        if (20000 != jsonObject.getInt("respCode")) {
            return null;
        }

        JSONObject jsonData = jsonObject.getJSONObject("data");

        UpdateBean updateBean = new Gson().fromJson(jsonData.toString(), UpdateBean.class);

        AppLog.i(TAG, "parserBookUpdateInfo updateBean" + updateBean.toString());

        List<UpdateBean.UpdateBookBean> items = updateBean.getUpdate_book();

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                UpdateBean.UpdateBookBean itemsBean = items.get(i);
                BookUpdate bookUpdate = new BookUpdate();
                Book book;
                if (TextUtils.isEmpty(itemsBean.getBook_id())) {
                    continue;
                } else {
                    book = bookItems.get(itemsBean.getBook_id());
                    bookUpdate.book_id = itemsBean.getBook_id();
                }
                if (book == null) {
                    continue;
                }

                ArrayList<Chapter> chapterList = new ArrayList<>();
                List<UpdateBean.UpdateBookBean.ChaptersBeanX> chapters = itemsBean.getChapters();
                if (chapters != null) {
                    for (int j = 0; j < chapters.size(); j++) {
                        UpdateBean.UpdateBookBean.ChaptersBeanX c = chapters.get(j);
                        Chapter chapter = new Chapter();
                        chapter.book_id = book.book_id;
                        chapter.parameter = book.parameter;
                        chapter.extra_parameter = book.extra_parameter;
                        chapter.chapter_name = c.getName();
                        chapter.sort = c.getSerial_number();
                        chapter.site = c.getHost();
                        chapter.curl = c.getUrl();

                        chapter.word_count = c.getWord_count();

                        chapter.chapter_id = c.getId();
                        chapter.time = c.getUpdate_time();
                        chapter.book_source_id = c.getBook_souce_id();
                        chapter.chapter_status = c.getStatus();
                        chapter.api_url = chapter.curl;
                        chapter.chapter_form = 1;
                        chapterList.add(chapter);
                    }
                    bookUpdate.chapterList = chapterList;
                    lists.add(bookUpdate);
                }

            }
        }

        RepairHelp.parserData(updateBean);

        return lists;
    }

    public static ParseJarBean parserJarUpdateInfo(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if (!jsonObject.getBoolean("success")) {
            return null;
        }
        ParseJarBean parseJarBean = new ParseJarBean();
        parseJarBean.version = jsonObject.getInt("version");
        parseJarBean.url = jsonObject.getString("url");
        parseJarBean.md5 = jsonObject.getString("md5");
        parseJarBean.dynamicPackage = jsonObject.getString("dynamic_package");
        return parseJarBean;
    }

    public static LogData logDataEncap(String type, Book book) {
        LogData logData = new LogData();
        logData.type = type;
        logData.bookName = book.name;
        logData.authorName = book.author;
        logData.site = book.site;
        logData.bookId = book.book_id;
        logData.bookSourceId = book.book_source_id;
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