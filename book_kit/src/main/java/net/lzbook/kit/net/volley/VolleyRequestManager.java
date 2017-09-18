package net.lzbook.kit.net.volley;


import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import net.lzbook.kit.app.BaseBookApplication;

/**
 * Volley请求管理类
 **/
public class VolleyRequestManager {
    private static RequestQueue requestQueue;

    public synchronized static RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(BaseBookApplication.getGlobalContext());
        }
        return requestQueue;
    }
}
