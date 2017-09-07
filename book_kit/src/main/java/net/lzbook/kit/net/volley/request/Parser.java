package net.lzbook.kit.net.volley.request;

import org.json.JSONException;

/**
 * 抽象的解析类
 **/
public abstract class Parser {
    public abstract Object parserMethod(String response) throws JSONException, Exception;
}
