package net.lzbook.kit.encrypt.v17;

import com.easou.novel.commons.encryp.util.SignatureUtil;

import net.lzbook.kit.encrypt.URLBuilderIntterface;
import net.youqu.token.Token;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLBuilder implements URLBuilderIntterface {


	@Override
	public String buildUrl(String host, String uriTag, Map<String, String> params) {
		params=EncodeMap(params);
		uriTag=encodeUriTag(uriTag);
		String token = Token.getToken(uriTag, params);
		String encode = "";
		try {
			encode = URLEncoder.encode(token, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return host+uriTag+(uriTag.contains("?")?"&":"?")+mapToUrlParams(params);
	}

	private String mapToUrlParams(Map<String,String> params){
		StringBuffer sb = new StringBuffer();
		for(String key : params.keySet()){
			sb.append("&"+key+"="+params.get(key));
		}
		return sb.substring(1);
	}
	
	private Map<String, String> EncodeMap(Map<String,String> map){
		Map<String,String> map1=new HashMap<String, String>();
		for(String str:map.keySet()){
			try {
				map1.put(str, URLEncoder.encode(map.get(str), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return map1;
	}
	
	public String encodeUriTag(String uriTag){
		String uriTag_pre="";
		String uriTag_sub="";
		if(uriTag!=null && uriTag.contains("?")){
			uriTag_pre=uriTag.substring(0,uriTag.indexOf("?"));
			uriTag_sub=uriTag.substring(uriTag.indexOf("?")+1,uriTag.length());
			String[] strs=uriTag_sub.split("&");
			uriTag_sub="";
			for(String s:strs){
				try {
					uriTag_sub=uriTag_sub+"&"+s.split("=")[0]+"="+URLEncoder.encode(s.split("=")[1], "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			uriTag_sub=uriTag_sub.substring(1,uriTag_sub.length());
			return uriTag_pre+"?"+uriTag_sub;
		}else{
			return uriTag;
		}
	}


	/*********************** 处理内容url **************************/
//	Pattern p = Pattern.compile("((\\w+\\.)?quanbennovel\\.com(:\\d+)?)|((\\w+\\.)?caotangteach\\.com(:\\d+)?)|((\\w+\\.)?lzbook\\.net(:\\d+)?)|((\\w+\\.)?wubutianxia\\.com(:\\d+)?)|((\\w+\\.)?youshiit\\.com(:\\d+)?)|((\\w+\\.)?dushixiaoshuo\\.cn(:\\d+)?)|((\\w+\\.)?lsread\\.cn(:\\d+)?)|((\\w+\\.)?0106636\\.cn(:\\d+)?)|((\\w+\\.)?zhuishuwang\\.com(:\\d+)?)|((\\w+\\.)?bookapi\\.cn(:\\d+)?)|((\\test1\\.api\\.)?bookapi\\.cn(:\\d+)?)");
//	Pattern p = Pattern.compile("((\\w+\\.)?quanbennovel\\.com(:\\d+)?)|((\\w+\\.)?caotangteach\\.com(:\\d+)?)|((\\w+\\.)?lzbook\\.net(:\\d+)?)|((\\w+\\.)?wubutianxia\\.com(:\\d+)?)|((\\w+\\.)?youshiit\\.com(:\\d+)?)|((\\w+\\.)?dushixiaoshuo\\.cn(:\\d+)?)|((\\w+\\.)?lsread\\.cn(:\\d+)?)|((\\w+\\.)?0106636\\.cn(:\\d+)?)|((\\w+\\.)?zhuishuwang\\.com(:\\d+)?)|((\\w+\\.)?bookapi\\.cn(:\\d+)?)|((test1\\.api\\.)?bookapi\\.cn(:\\d+)?)|((127\\.0\\.0\\.1)(:\\d+)?)");
	public static String content = "http://api.easou.com/api/bookapp/batch_chapter.m?a=1&session_id=&cid=eef_easou_book&version=002&os=android&udid=%s&appverion=1034&ch=blf1298_12237_001";
	public static String contentPostParam = "gid=%s&nid=%s&sort=%s&gsort=%s&sequence=0&chapter_name=%s";

	@Override
	public String buildContentUrl(String url, Map<String, String> params) {
		params=EncodeMap(params);
		return urlFilter(url, params);
	}

	private String urlFilter(String url, Map<String, String> paramsMap){
		URL u = null;
		try {
			u = new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException("URL error:"+e.getMessage());
		}
		if("/v3/book/chaptersContents".equals(u.getPath())){
			Map<String, String> urlParams = Token.getUrlParams(u.getQuery());
			urlParams.putAll(paramsMap);
			String token = Token.getToken(u.getPath(), urlParams);
			String encode = "";
			try {
				encode = URLEncoder.encode(token, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			StringBuffer sb = new StringBuffer();
			sb.append(u.getProtocol()+"://");
			sb.append(u.getHost());
			sb.append(u.getPort()==-1?"":":"+u.getPort());
			sb.append(u.getPath());
			sb.append("?"+mapToUrlParams(urlParams));
			sb.append("&token="+encode);
			return sb.toString();
		}else if(u.getPath().equals("/api/bookapp/batch_chapter.m")){
			String query = u.getQuery();
			Map<String, String> params = urlParamsToMap(query);
			String postParam = String.format(contentPostParam, params.get("gid"), params.get("nid"), params.get("sort"), params.get("gsort"),  params.get("chapter_name"));
			String original = String.format(content, udid());
			try {
				return reurl(original, postParam);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		}else{
			return url;
		}
	}

	private String udid(){
		return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
	}

	private Map<String,String> urlParamsToMap(String postParam){
		String p = "&"+postParam;
		String urlKey = "&(\\w+)=";
		Pattern regex = Pattern.compile(urlKey);
		Matcher matcher = regex.matcher(p);
		Map<String,String> params = new HashMap<>();
		String[] split = p.split(urlKey);
		int i = 0;
		while(matcher.find()){
			String group = matcher.group();
			params.put(group.substring(1, group.length()-1), split[++i]);
		}
		return params;
	}

	public String reurl(String original, String postParam) throws MalformedURLException{
		URL url = new URL(original);
		String queryParam = url.getQuery();
		Map<String,String> params = new HashMap<>();
		params.putAll(urlParamsToMap(queryParam));
		if(postParam != null && postParam.length() != 0){
			params.putAll(urlParamsToMap(postParam));
		}
		String sign = "";
		try {
			sign = SignatureUtil.sign(url.getPath(), params);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return original+(postParam!=null?"&"+postParam:"")+"&statId="+sign;
	}

	public Map<String,String> getParamsByUrl(URL url) {
        Map<String, String> params = null;
        String query = url.getQuery();
        if(query != null && query.length() > 0) {
            params = new HashMap<String, String>();
            String[] paramStyrs = query.split("&");
            for (String paramStr :paramStyrs) {
                String[] keyvalue = paramStr.split("=");
                if(keyvalue.length == 2) {
                    params.put(keyvalue[0], keyvalue[1]);
                }
            }
        }
        return params;
    }
}
