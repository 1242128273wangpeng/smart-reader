package net.lzbook.kit.net;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import net.lzbook.kit.input.InputStreamUtils;
import net.lzbook.kit.input.MultiInputStreamHelper;
import net.lzbook.kit.net.volley.request.RetryPolicy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class UtilStringRequest extends StringRequest {

    private Priority priority = Priority.NORMAL;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params;

    public UtilStringRequest(int method, String url, Listener<String> listener, ErrorListener errorListener, boolean shouldCache, Priority priority, MultiInputStreamHelper.IEncoding iEncoding) {
        super(method, url, listener, errorListener);

        setShouldCache(shouldCache);
        this.priority = Priority.NORMAL;
        if (iEncoding != null) {
            this.headers.put("Accept-Encoding", iEncoding.encoding);
        }

    }

    public UtilStringRequest(String url, Map<String, String> params, Listener<String> listener, ErrorListener errorListener) {
        this(params == null ? Method.GET : Method.POST, url, listener, errorListener, true, Priority.NORMAL, MultiInputStreamHelper.IEncoding.ESENCGZIP);
        this.params = params;
        setRetryPolicy(new RetryPolicy());
    }


    public UtilStringRequest(String url, Map<String, String> params, Listener<String> listener, ErrorListener errorListener, int timeOut, int retry, boolean isEZip) {
        this(params == null ? Method.GET : Method.POST, url, listener, errorListener, true, Priority.NORMAL, isEZip ? MultiInputStreamHelper.IEncoding.ESENCGZIP : null);
        this.params = params;
        RetryPolicy retryPolicy = new RetryPolicy(timeOut, retry);
        setRetryPolicy(retryPolicy);
        setShouldCache(true);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return params;
    }

    @Override
    public com.android.volley.Request.Priority getPriority() {
        return priority;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }

    // TODO
    @Override
    public com.android.volley.RetryPolicy getRetryPolicy() {
        return super.getRetryPolicy();
    }

    // TODO
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
            input = MultiInputStreamHelper.getInputStream(response.headers, input);
            parsed = InputStreamUtils.getString(input, HttpHeaderParser.parseCharset(response.headers));
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
