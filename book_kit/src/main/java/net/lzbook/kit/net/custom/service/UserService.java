package net.lzbook.kit.net.custom.service;


import com.google.gson.JsonObject;

import net.lzbook.kit.user.bean.LoginResp;
import net.lzbook.kit.user.bean.QQSimpleInfo;
import net.lzbook.kit.user.bean.RefreshResp;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by xian on 2017/6/21.
 */

public interface UserService {
    String PATH_LOGIN = "/v3/user/login";
    String PATH_REFRESH = "/v3/user/refLToken";
    String PATH_LOGOUT = "/v3/user/logout";


    @GET(PATH_LOGIN)
    Observable<LoginResp> login(@QueryMap(encoded = false) Map<String, String> params);


    @GET("https://graph.qq.com/user/get_simple_userinfo")
    Observable<QQSimpleInfo> getSimpleUserInfo(@Query("access_token") String token,
                                               @Query("oauth_consumer_key") String appid,
                                               @Query("openid") String openid);


    @GET(PATH_REFRESH)
    Observable<RefreshResp> refreshToken(@QueryMap(encoded = false) Map<String, String> params);

    @GET(PATH_LOGOUT)
    Observable<JsonObject> logout(@QueryMap(encoded = false) Map<String, String> params);
}
