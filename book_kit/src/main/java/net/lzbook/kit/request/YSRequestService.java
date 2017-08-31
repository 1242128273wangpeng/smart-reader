package net.lzbook.kit.request;

import net.lzbook.kit.net.volley.request.Parser;
import net.lzbook.kit.net.volley.request.VolleyDataService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by iyouqu on 2016/11/16.
 */
public class YSRequestService extends VolleyDataService{
    public static String TAG = YSRequestService.class.getSimpleName();
    public static void getSearchSuggestData(VolleyDataService.DataServiceTagCallBack dataServiceTagCallBack, String url) {
        publicCode(url, null, dataServiceTagCallBack, new Parser() {
            @Override
            public Object parserMethod(String response) throws Exception {
                return getSearchSuggest(response);
            }
        }, url);
    }

    /**
     * 获取搜索建议
     *
     * @param json
     * @return
     * @throws JSONException
     */
    public static ArrayList<String> getSearchSuggest(String json) throws JSONException {
        JSONObject jsonRoot = new JSONObject(json);
        if (!jsonRoot.getBoolean("success")) {
            return null;
        }
        JSONArray items = jsonRoot.getJSONArray("items");
        if (items != null) {
            int length = items.length();
            if (length > 0) {
                ArrayList<String> result = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    String item = (String) items.get(i);
                    if (item != null) {
                        item = item.trim();
                        if (!result.contains(item)){
                            result.add(item);
                        }
                    }
                }
                return result;
            }
        }
        return null;
    }
}
