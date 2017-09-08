package net.lzbook.kit.encrypt.v17.util;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;

public class NovelHttpConnection {

	/**
	 * GET 请求
	 * */
	public static Document get(String url) {
		int tryCount = 0, statusCode = 0;
		String sourceUrl = null;
		String query = url.substring(url.indexOf("?") + 1);
		int beginIndex = query.indexOf("http");
		int endIndex = query.indexOf("&", beginIndex);
		if (beginIndex > 0) {
			sourceUrl = query.substring(beginIndex, endIndex > 0 ? endIndex : query.length());
		}
		String host = null;
		String cookie = null;
		URL url2 = null;
		try {
			url2 = new URL(url);
			host = url2.getHost();
			cookie = CookieStore.getInstance().getCookie(host);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		String referer = url;
		while (tryCount < 3 && statusCode != HttpURLConnection.HTTP_OK) {
			Connection con = Jsoup.connect(url);
			try {
				Response res = con.ignoreContentType(true).method(Method.GET).timeout(5000)
						.userAgent(
								"Mozilla/5.0 (Linux; Android 4.4.2; Nexus 4 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36")
						.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
						.header("Accept-Encoding", "gzip, deflate, sdch").header("Accept-Language", "zh-CN,zh;q=0.8")
						.header("Cache-Control", "max-age=0").header("Connection", "keep-alive").header("Host", host)
						.header("Upgrade-Insecure-Requests", "1").header("Cookie", cookie)
						.header("X-Requested-With", "XMLHttpRequest").header("Referer", referer).execute();
				if ((statusCode = res.statusCode()) == HttpURLConnection.HTTP_OK) {
					return res.parse();
				}
			} catch (UnsupportedMimeTypeException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (SocketException | SocketTimeoutException e) {
				e.printStackTrace();
			} catch (HttpStatusException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				tryCount++;
			}
		}
		//子地址二次抓取
		if (sourceUrl != null) {
			try {
				return get(URLDecoder.decode(sourceUrl, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
