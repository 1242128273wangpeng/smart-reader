package net.lzbook.kit.net.volley.request;

import com.android.volley.DefaultRetryPolicy;

/**
 * Volley重试策略类
 **/

public class RetryPolicy extends DefaultRetryPolicy{

	//超时时间
    public static final int DEFAULT_TIMEOUT_MS = 1000 * 30;

    //重复请求次数
    public static final int DEFAULT_MAX_RETRIES = 3;

    //计算请求时间相关参数
    public static final float DEFAULT_BACKOFF_Multiplier = 1f;
    
    public RetryPolicy() {
        super(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_Multiplier);
    }
    
    public RetryPolicy(int time_out) {
        super(time_out, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_Multiplier);
    }
    
    public RetryPolicy(int time_out, int retries) {
        super(time_out, retries, DEFAULT_BACKOFF_Multiplier);
    }
}
