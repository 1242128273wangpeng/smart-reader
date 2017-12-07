package net.lzbook.kit.net.token;

import com.easou.novel.commons.encryp.util.SignatureUtil;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 项目名称：KdqbxsGit
 * 类描述：
 * 创建人：Zach
 * 创建时间：2017/11/22 0022
 */

public class Token {
    private static final String AESKEY = "7A3II/M5Ja0S4gdf";
    private static final String URLKEY = "mVjdXyqwjlEptwYY";
    public static String content = "http://api.easou.com/api/bookapp/batch_chapter.m?a=1&session_id=&cid=eef_easou_book&version=002&os=android&udid=%s&appverion=1034&ch=blf1298_12237_001";
    public static String contentPostParam = "gid=%s&nid=%s&sort=%s&gsort=%s&sequence=0&chapter_name=%s";

    public Token() {
    }

    public static HashMap<String, String> getUrlParams(String param) {
        HashMap map = new HashMap();
        if (!"".equals(param) && null != param) {
            String[] params = param.split("&");

            for (int i = 0; i < params.length; ++i) {
                String[] p = params[i].split("=");
                if (p.length == 2) {
                    map.put(p[0], p[1]);
                }
            }

            return map;
        } else {
            return map;
        }
    }

    public static String getUrlParamsByMap(Map<String, String> map, boolean isSort) {
        if (map != null && !map.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            ArrayList keys = new ArrayList(map.keySet());
            if (isSort) {
                Collections.sort(keys);
            }

            for (int s = 0; s < keys.size(); ++s) {
                String key = (String) keys.get(s);
                String value = ((String) map.get(key)).toString();
                sb.append(key + "=" + value);
                sb.append("&");
            }

            String var7 = sb.toString();
            if (var7.endsWith("&")) {
                var7 = var7.substring(0, var7.lastIndexOf("&"));
            }

            return var7;
        } else {
            return "";
        }
    }

    private static long getHash(String uri, Map<String, String> params) {
        String paramString = getUrlParamsByMap(params, true);
        StringBuilder url = new StringBuilder();
        url.append(uri).append(paramString).append("mVjdXyqwjlEptwYY");
        return (long) MurmurHash.hash32(url.toString());
    }

    private static String encrypt(String content) throws Exception {
        return AES.encryptAES(content, "7A3II/M5Ja0S4gdf");
    }

    private static String decrypt(String content) throws Exception {
        return AES.decryptAES(content, "7A3II/M5Ja0S4gdf");
    }


    public static boolean verify(String uri, Map<String, String> params, String token) {
        long hash = getHash(uri, params);

        String decToken;
        try {
            decToken = decrypt(token);
        } catch (Exception var9) {
            var9.printStackTrace();
            return false;
        }

        String[] sp = decToken.split("_");
        if (sp != null && sp.length == 2) {
            long longToken = Long.parseLong(sp[0]);
            return hash == longToken;
        } else {
            return false;
        }
    }

    public static boolean verify1(String uri, Map<String, String> params, String token) {
        uri = encodeUriTag(uri);
        params = EncodeMap(params);
        long hash = getHash(uri, params);

        String decToken;
        try {
            decToken = decrypt(token);
        } catch (Exception var9) {
            var9.printStackTrace();
            return false;
        }

        String[] sp = decToken.split("_");
        if (sp != null && sp.length == 2) {
            long longToken = Long.parseLong(sp[0]);
            return hash == longToken;
        } else {
            return false;
        }
    }

    public static Map<String, String> EncodeMap(Map<String, String> map) {
        HashMap map1 = new HashMap();
        Iterator var2 = map.keySet().iterator();

        while (var2.hasNext()) {
            String str = (String) var2.next();

            try {
                map1.put(str, URLEncoder.encode((String) map.get(str), "UTF-8"));
            } catch (UnsupportedEncodingException var5) {
                var5.printStackTrace();
            }
        }

        return map1;
    }

    public static String encodeUriTag(String uriTag) {
        String uriTag_pre;
        String uriTag_sub;
        if (uriTag != null && uriTag.contains("?")) {
            uriTag_pre = uriTag.substring(0, uriTag.indexOf("?"));
            uriTag_sub = uriTag.substring(uriTag.indexOf("?") + 1, uriTag.length());
            String[] strs = uriTag_sub.split("&");
            uriTag_sub = "";
            String[] var4 = strs;
            int var5 = strs.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                String s = var4[var6];

                try {
                    uriTag_sub = uriTag_sub + "&" + s.split("=")[0] + "=" + URLEncoder.encode(s.split("=")[1], "UTF-8");
                } catch (UnsupportedEncodingException var9) {
                    var9.printStackTrace();
                }
            }

            uriTag_sub = uriTag_sub.substring(1, uriTag_sub.length());
            return uriTag_pre + "?" + uriTag_sub;
        } else {
            return uriTag;
        }
    }

    public static void main(String[] args) throws Exception {
        String uri = "/api/bookapp/cover.m";
        String param = "gid=8149313&session_id=&cid=eef_easou_book&version=002&os=android&udid=2116DC7E2F93E1ED54B6B477720F8454&appverion=1034&ch=nice&statId=HOKFHTb2KGVPfZeh9LTzk3vlfOhtM";
        String token = getToken(uri, getUrlParams(param));
        System.out.println(token);
        boolean b = verify(uri, getUrlParams(param), token);
        System.out.println(b);
    }

    public static String getToken(String uri, Map<String, String> params) {
        long hash = getHash(uri, params);
        String content = hash + "_" + System.currentTimeMillis();
        String token = "";

        try {
            token = encrypt(content);
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return token;
    }

    public static Map<String, String> urlParamsToMap(String postParam) {
        String p = "&" + postParam;
        String urlKey = "&(\\w+)=";
        Pattern regex = Pattern.compile(urlKey);
        Matcher matcher = regex.matcher(p);
        Map<String, String> params = new HashMap<>();
        String[] split = p.split(urlKey);
        int i = 0;
        while (matcher.find()) {
            String group = matcher.group();
            params.put(group.substring(1, group.length() - 1), split[++i]);
        }
        return params;
    }

    public static String udid() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    public static String reurl(String original, String postParam) throws MalformedURLException {
        URL url = new URL(original);
        String queryParam = url.getQuery();
        Map<String, String> params = new HashMap<>();
        params.putAll(urlParamsToMap(queryParam));
        if (postParam != null && postParam.length() != 0) {
            params.putAll(urlParamsToMap(postParam));
        }
        String sign = "";
        try {
            sign = SignatureUtil.sign(url.getPath(), params);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return original + (postParam != null ? "&" + postParam : "") + "&statId=" + sign;
    }

    public static String mapToUrlParams(Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        for (String key : params.keySet()) {
            sb.append("&" + key + "=" + params.get(key));
        }
        return sb.substring(1);
    }
}
