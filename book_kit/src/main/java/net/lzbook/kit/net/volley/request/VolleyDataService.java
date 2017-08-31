package net.lzbook.kit.net.volley.request;

import android.os.Handler;
import android.os.Message;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.lzbook.kit.net.UtilStringRequest;
import net.lzbook.kit.net.volley.VolleyRequestManager;
import net.lzbook.kit.utils.AppLog;

import java.util.Map;

/**
 * Volley请求方法类
 **/
public class VolleyDataService {

    private static String TAG = VolleyDataService.class.getSimpleName();

    /**
     * 对外提供的Volley请求解析类，默认为EZip方式的请求方法
     **/
    public static void publicCode(String url, Map<String, String> parameter, final Handler handler, final int success_code, final int error_code, final Parser parser) {
        publicCode(url, parameter, handler, success_code, error_code, parser, RetryPolicy.DEFAULT_TIMEOUT_MS, RetryPolicy.DEFAULT_MAX_RETRIES, true);
    }

    /**
     * 对外提供的Volley请求解析类
     **/
    public static void publicCode(String url, Map<String, String> parameter, final Handler handler, final int success_code, final int error_code, final Parser parser, final boolean isEZip) {
        publicCode(url, parameter, handler, success_code, error_code, parser, RetryPolicy.DEFAULT_TIMEOUT_MS, RetryPolicy.DEFAULT_MAX_RETRIES, isEZip);
    }

    /**
     * 私有的解析类
     **/
    public static void publicCode(String url, Map<String, String> parameter, final Handler handler, final int success_code, final int error_code, final Parser parser, int time_out, int retries, boolean isEZip) {

        RequestQueue requestQueue = VolleyRequestManager.getRequestQueue();

        Listener<String> listener = new Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    Message msg = handler.obtainMessage();
                    msg.what = success_code;
                    msg.obj = parser.parserMethod(response);
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = handler.obtainMessage();
                    msg.what = error_code;
                    msg.obj = e;
                    handler.sendMessage(msg);
                }
            }
        };

        ErrorListener errorListener = new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Message msg = handler.obtainMessage();
                msg.what = error_code;
                msg.obj = error;
                handler.sendMessage(msg);
            }
        };

        Request<String> request = new CustomStringRequest(url, parameter, listener, errorListener, time_out, retries, isEZip);
        if (parameter == null && url != null) {
            request.setTag(url);
            requestQueue.cancelAll(url);
        }
        requestQueue.add(request);
    }

    /**
     * 默认的回调方式
     **/
    public interface DataServiceCallBack {
        void onSuccess(Object result);

        void onError(Exception error);
    }

    public static void publicCode(String url, Map<String, String> params, final DataServiceCallBack callBack, boolean isEZip, final Parser parser) {

        RequestQueue queue = VolleyRequestManager.getRequestQueue();

        Listener<String> listener = new Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    if (callBack != null) {
                        callBack.onSuccess(parser.parserMethod(response));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    callBack.onError(e);
                }
            }

        };

        ErrorListener errorListener = new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (callBack != null) {
                    callBack.onError(error);
                }
            }
        };

        Request<String> jsonRequest = new UtilStringRequest(url, params, listener, errorListener, RetryPolicy.DEFAULT_TIMEOUT_MS, RetryPolicy.DEFAULT_MAX_RETRIES, isEZip);
        if (params == null && url != null) {
            jsonRequest.setTag(url);
            queue.cancelAll(url);
        }
        queue.add(jsonRequest);
    }

    /**
     * 对外提供的Volley请求解析类，默认为EZip方式的请求方法
     **/
    public static void publicCode(String url, Map<String, String> parameter, final DataServiceCallBack dataServiceCallBack, final Parser parser) {
        publicCode(url, parameter, dataServiceCallBack, parser, RetryPolicy.DEFAULT_TIMEOUT_MS, RetryPolicy.DEFAULT_MAX_RETRIES, true);
    }

    /**
     * 对外提供的Volley请求解析类
     **/
    public static void publicCode(String url, Map<String, String> parameter, final DataServiceCallBack dataServiceCallBack, final Parser parser, boolean isEZip) {
        publicCode(url, parameter, dataServiceCallBack, parser, RetryPolicy.DEFAULT_TIMEOUT_MS, RetryPolicy.DEFAULT_MAX_RETRIES, isEZip);
    }

    /**
     * 私有的解析类
     **/
    public static void publicCode(String url, Map<String, String> parameter, final DataServiceCallBack dataServiceCallBack, final Parser parser, int time_out, int retries, boolean isEZip) {

        RequestQueue requestQueue = VolleyRequestManager.getRequestQueue();

        Listener<String> listener = new Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    if (dataServiceCallBack != null) {
                        dataServiceCallBack.onSuccess(parser.parserMethod(response));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dataServiceCallBack.onError(e);
                }
            }
        };

        ErrorListener errorListener = new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (dataServiceCallBack != null) {
                    dataServiceCallBack.onError(error);
                }
            }
        };

        Request<String> request = new CustomStringRequest(url, parameter, listener, errorListener, time_out, retries, isEZip);
        if (parameter == null && url != null) {
            request.setTag(url);
            requestQueue.cancelAll(url);
        }
        requestQueue.add(request);
    }

    /**
     * Tag方式的回调，Tag主要用于检测是否是对应的返回结果
     **/
    public interface DataServiceTagCallBack {
        void onSuccess(Object result, Object tag);

        void onError(Exception error, Object tag);
    }

    public static void publicCode(String url, Map<String, String> parameter, final DataServiceTagCallBack callBack, final Parser parser, final Object tag) {

        RequestQueue requestQueue = VolleyRequestManager.getRequestQueue();

        Listener<String> listener = new Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    if (callBack != null) {
                        callBack.onSuccess(parser.parserMethod(response), tag);
                        AppLog.e(TAG, "Request Tag: " + tag.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callBack.onError(e, tag);
                }
            }
        };

        ErrorListener errorListener = new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (callBack != null) {
                    callBack.onError(error, tag);
                }
            }
        };

        Request<String> request = new CustomStringRequest(url, parameter, listener, errorListener);
        if (parameter == null && url != null) {
            request.setTag(url);
            requestQueue.cancelAll(url);
        }
        requestQueue.add(request);
    }
}
