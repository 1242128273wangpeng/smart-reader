package net.lzbook.kit.utils;


import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.input.MultiInputStreamHelper;
import net.lzbook.kit.net.custom.AndroidHttpClient;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.params.HttpProtocolParams;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.List;

public class HttpUtils {

    private static final String Accept_Encoding = "gzip,esenc";
    private static HttpClient androidHttpClient;

    public synchronized static HttpClient getHttpClient() {
        if (androidHttpClient == null) {
            androidHttpClient = AndroidHttpClient.newInstance("");
            HttpProtocolParams.setUseExpectContinue(androidHttpClient.getParams(), false);
        }
        return androidHttpClient;
    }

    private static void setEncoding(HttpRequest request) {
        request.addHeader("Accept-Encoding", Accept_Encoding);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        request.addHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.3; zh-cn; SM-N7508V Build/JLS36C) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
    }

    private static String getEncoding(HttpResponse response) {
        Header[] header1 = response.getHeaders("Accept-Encoding");
        Header[] header2 = response.getHeaders("Content-Encoding");
        Header[] header3 = response.getHeaders("User-Agent");
        StringBuilder encoding = new StringBuilder();
        if (header1 != null) {
            for (int i = 0; i < header1.length; i++) {
                encoding.append(header1[i].getValue());
            }
        }
        if (header2 != null) {
            for (int i = 0; i < header2.length; i++) {
                encoding.append(header2[i].getValue());
            }
        }
        if (header3 != null) {
            for (int i = 0; i < header3.length; i++) {
                encoding.append(header3[i].getValue());
            }
        }
        return encoding.toString();
    }

    public static HttpResponse getGzipEntityAndThrow(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        setEncoding(get);
        HttpResponse response = getHttpClient().execute(get);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            get.abort();
            AppLog.d("http", "get status==>" + response.getStatusLine().getStatusCode() + "  url==>" + url);
        }
        return response;
    }

    public static HttpResponse tryGetGzipEntityAndThrow(String url) throws IOException {
        try {
            AppLog.e("NetUtils", "NetUtils tryGetGzipEntityAndThrow: " + url);
            return getGzipEntityAndThrow(url);
        } catch (Exception e) {
            try {
                AppLog.e("NetUtils", "NetUtils tryGetGzipEntityAndThrow: " + url);
                return getGzipEntityAndThrow(url);
            } catch (Exception e1) {
                AppLog.e("NetUtils", "NetUtils tryGetGzipEntityAndThrow: " + url);
                return getGzipEntityAndThrow(url);

            }
        }
    }

    public static InputStream getZIPInputStreamAndThrow(String url) throws IOException {
        HttpResponse response = tryGetGzipEntityAndThrow(url);
        if (response == null) {
            return null;
        }
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            return MultiInputStreamHelper.getInputStream(getEncoding(response), entity.getContent());
        }
        return null;
    }

    public static InputStream postZIPOrThrow(String url, List<NameValuePair> formparams) throws IOException {
        UrlEncodedFormEntity urlEntity = new UrlEncodedFormEntity(formparams, Constants.CHARSET);

        InputStream inputStream = urlEntity.getContent();
        byte[] buffer = new byte[4 * 1024];
        int n;
        while ((n = inputStream.read(buffer)) != -1) {
            AppLog.e("PostZIPOrThrow", new String(buffer, 0, n));
        }

        AppLog.e("PostZIPOrThrow", "PostZIPOrThrow: " + formparams.size());
        HttpPost post = new HttpPost(url);
        post.setEntity(urlEntity);
        setEncoding(post);
        HttpResponse response = null;
        for (int i = 0; i < 3; i++) {

            try {
                AppLog.e("response", "response:" + i);
                response = getHttpClient().execute(post);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    break;
                }
            } catch (ConnectTimeoutException e1) {
                AppLog.e("response", "ConnectTimeoutException:" + i);
                e1.printStackTrace();
                continue;
            } catch (SocketTimeoutException e2) {
                AppLog.e("response", "SocketTimeoutException:" + i);
                e2.printStackTrace();
                continue;
            } catch (IOException e) {
                if (i == 2) {
                    AppLog.e("response", "IOException:" + i);
                    throw e;
                }
            }
        }
        AppLog.d("http", "post status==>" + response.getStatusLine().getStatusCode() + "  url==>" + url);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return MultiInputStreamHelper.getInputStream(getEncoding(response), entity.getContent());
            } else {
                AppLog.e("postZIPOrThrow", "entity == null");
            }
        } else {
            post.abort();
            if (response.getStatusLine().getStatusCode() == 502) {
                throw new IOException();
            }
            AppLog.d("http", "post status==>" + response.getStatusLine().getStatusCode() + "  url==>" + url);
        }
        return null;
    }
}
