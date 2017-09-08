package net.lzbook.kit.encrypt.v17.util;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class CookieStore {
	
	private Map<String,String> cookiesMap = new ConcurrentHashMap<>();
	private Map<String,Map<String,String>> headersMap = new ConcurrentHashMap<String, Map<String,String>>();
	
	public static Map<String,String> hosts = new HashMap<>();

	static {
		hosts.put("www.snwx.com", "http://www.snwx.com");
//		hosts.put("www.luoqiu.com", "http://www.luoqiu.com");
		hosts.put("www.lewenxiaoshuo.com", "http://www.lewenxiaoshuo.com");
	}
	
	private static CookieStore cs = new CookieStore();
	
	private CookieStore(){}
	
	public static CookieStore getInstance(){
		return cs;
	}
	
	public String getCookie(String host){
		if(!cookiesMap.containsKey(host)){
			if(hosts.containsKey(host)){
				String cookieStr = "";
				try {
					Response execute = Jsoup.connect(hosts.get(host)).timeout(5000).method(Method.GET).ignoreContentType(true).userAgent("Mozilla/5.0 (Linux; Android 4.4.2; Nexus 4 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36")
							.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
							.header("Accept-Encoding", "gzip, deflate, sdch")
							.header("Accept-Language", "zh-CN,zh;q=0.8")
							.header("Cache-Control", "max-age=0")
							.header("Connection", "keep-alive")	
							.header("Host", host)
							.header("Upgrade-Insecure-Requests", "1").execute();
					
					Map<String, String> headers = execute.headers();
					headersMap.put(host, headers);
					
					Map<String, String> cookies = execute.cookies();
					if(cookies.size()>0){
						for ( Entry<String, String> e: cookies.entrySet()) {
							cookieStr+=";"+e.getKey()+"="+e.getValue();
						}
						cookiesMap.put(host, cookieStr.substring(1));
					}else{
						cookiesMap.put(host, "");
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}else{
				return "";
			}
		}
		if(cookiesMap.containsKey(host)){
	    	return cookiesMap.get(host);
	    }else{
	    	return "";
	    }
	}
	

	public Map<String, String> getCookies() {
		return cookiesMap;
	}

	public void setCookies(Map<String, String> cookiesMap) {
		this.cookiesMap = cookiesMap;
	}

	public Map<String, Map<String, String>> getHeadersMap() {
		return headersMap;
	}

	public void setHeadersMap(Map<String, Map<String, String>> headersMap) {
		this.headersMap = headersMap;
	}
	
	
}
