package net.lzbook.kit.utils;

import com.dingyueads.sdk.Utils.HttpUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

public class BugFeedback {

    private final static int TIMEOUT_CONNECTION = 20000;
    private final static int TIMEOUT_SOCKET = 20000;
    private final static int RETRY_TIME = 3;

    @SuppressWarnings("unchecked")
    protected void doInBackground(Object... params) {
        this.doPost((String) params[0], (Map<String, String>) params[1], (Boolean) params[2]);
    }

    /**
     * 发送异常日志
     * */
    public void doPost(String url, Map<String, String> params, boolean isCachedData) {
        HttpClient httpClient = null;
        HttpPost httpPost;
        int time = 0;
        do {
            try {
                httpClient = com.dingyueads.sdk.Utils.HttpUtils.getHttpClient(TIMEOUT_CONNECTION, TIMEOUT_SOCKET);
                httpPost = HttpUtils.getHttpPost(url);
                ArrayList<BasicNameValuePair> pairs = new ArrayList<>();
                if (null != params && params.size() > 0) {
                    for (Entry<String, String> entry : params.entrySet()) {
                        if (null == entry.getKey()) {
                            continue;
                        }
                        pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                    }
                }
                UrlEncodedFormEntity urlEntity = new UrlEncodedFormEntity(pairs, "utf-8");
                httpPost.setEntity(urlEntity);
                HttpResponse response = httpClient.execute(httpPost);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    AppLog.e("BugFeedback", "BugFeedback false code:" + statusCode);
                    return;
                }
                AppLog.e("BugFeedback", "BugFeedback success");
                break;
            } catch (Exception e) {
                time++;
                if (time < RETRY_TIME) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    continue;
                }
            } finally {
                // 释放连接
                if (null != httpClient) {
                    httpClient.getConnectionManager().shutdown();
                    httpClient = null;
                }
            }
        } while (time < RETRY_TIME);
    }

    public void execute(final Object[] objects) {
        new Thread() {
            public void run() {
                BugFeedback.this.doInBackground(objects);
            }
        }.start();
    }

}
