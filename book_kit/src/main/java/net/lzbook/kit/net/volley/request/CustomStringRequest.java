package net.lzbook.kit.net.volley.request;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import net.lzbook.kit.net.InputStreamHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义的StringRequest，主要用于Volley请求
 **/

public class CustomStringRequest extends StringRequest {

    private Priority priority = Priority.NORMAL;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params;

    public CustomStringRequest(int method, String url, Listener<String> listener, ErrorListener errorListener, boolean shouldCache, InputStreamHelper.IEncoding iEncoding) {
        super(method, url, listener, errorListener);
        setShouldCache(shouldCache);
        this.priority = Priority.NORMAL;
        if (iEncoding != null) {
            this.headers.put("Accept-Encoding", iEncoding.encoding);
        }
    }

    public CustomStringRequest(String url, Map<String, String> params, Listener<String> listener, ErrorListener errorListener) {
        this(params == null ? Method.GET : Method.POST, url, listener, errorListener, true, InputStreamHelper.IEncoding.GZIP);
        this.params = params;
        setRetryPolicy(new RetryPolicy());
    }

    public CustomStringRequest(String url, Map<String, String> params, Listener<String> listener, ErrorListener errorListener, int timeOut, int retry, boolean isEZip) {
        this(params == null ? Method.GET : Method.POST, url, listener, errorListener, true, isEZip ? InputStreamHelper.IEncoding.GZIP : null);
        this.params = params;
        RetryPolicy retryPolicy = new RetryPolicy(timeOut, retry);
        setRetryPolicy(retryPolicy);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return params;
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }

    @Override
    public com.android.volley.RetryPolicy getRetryPolicy() {
        return super.getRetryPolicy();
    }

    @Override
    public String getCacheKey() {
        return super.getCacheKey();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        InputStream input = null;
        try {
            input = new ByteArrayInputStream(response.data);
            input = InputStreamHelper.getInputStream(response.headers, input);
            parsed = InputStreamHelper.getString(input, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
