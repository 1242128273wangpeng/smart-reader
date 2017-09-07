package net.lzbook.kit.net.volley;


import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.content.Context;

/**
 * Volley请求管理类
 **/
public class VolleyRequestManager {
    private static RequestQueue requestQueue;

    public static void init(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public synchronized static RequestQueue getRequestQueue() {
        if (requestQueue != null) {
            return requestQueue;
        } else {
            throw new IllegalStateException("Not initialized");
        }
    }
}
