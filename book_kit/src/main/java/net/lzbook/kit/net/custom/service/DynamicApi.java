package net.lzbook.kit.net.custom.service;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by xian on 2017/6/21.
 */

public interface DynamicApi {

    // cdn智能
    String DYNAMIC_ZN = "https://public.lsread.cn/dpzn/{packageName}.json";

    // cdn传媒
    String DYNAMIC_CM = "https://public.dingyueads.com/dpzn/{packageName}.json";

    // cdn原创
    String DYNAMIC_YC = "https://public.qingoo.cn/dpzn/{packageName}.json";

    @GET()
    Observable<ResponseBody> requestCDNDynamicPar(@Url String url);

}
