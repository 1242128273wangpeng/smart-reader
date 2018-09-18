package com.ding.basic.request;

import com.ding.basic.Config;
import com.ding.basic.bean.BasicResult;
import com.ding.basic.bean.CacheTaskConfig;
import com.ding.basic.net.api.RequestAPI;

import org.junit.Before;

import java.util.HashMap;

import io.reactivex.functions.Consumer;

/**
 * Created by xian on 18-3-26.
 */
public class RequestAPITest {

    @Before
    public void before() {
//        Config.INSTANCE.initializeLogger();

        HashMap<String, String> parameters = new HashMap<>();

        String packageName = "cc.kdqbxs.reader";
        parameters.put("packageName", packageName);

        String version = String.valueOf(34);
        parameters.put("version", version);

        String channelId = "DEBUG";
        parameters.put("channelId", channelId);

        String os = "android";
        parameters.put("os", os);

        String udid = "5679e8479857496fb499fe2616972aaf";
        parameters.put("udid", udid);

        parameters.put("longitude", "0.0");

        parameters.put("latitude", "0.0");

        parameters.put("cityCode", "");

        parameters.put("loginToken", "");

        Config.INSTANCE.insertRequestParameters(parameters);
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public void requestDownTaskConfig() throws Exception {
//        URL	https://k.zhuishuwang.com/v3/book/596a2eada813820ec10069b0/596a2eada813820ec10069b1/cover?channelId=DEBUG&packageName=cc.kdqbxs.reader&longitude=0.0&loginToken=&version=34&latitude=0.0&udid=5679e8479857496fb499fe2616972aaf&cityCode=&os=android&token=v0%2FQoySx4rQGpM7VG2O8xOxWND1UmMnuJiApY29OEMw%3D
//        GET /v3/book/chaptersContents?udid=5679e8479857496fb499fe2616972aaf&channelId=DEBUG&packageName=cc.kdqbxs.reader&os=android&version=34&chapterId=596a2eada813820ec10069b2&token=cBgoOdf0iuex%2FFyIPaoKirfH4sHmOx0HCJdMtLb82j0%3D HTTP/1.1
        RequestAPI.INSTANCE.requestDownTaskConfig("596a2eada813820ec10069b0",
                "596a2eada813820ec10069b1",
                1, "596a2eada813820ec10069b2")
                .subscribe(
                        new Consumer<BasicResult<CacheTaskConfig>>() {
                            @Override
                            public void accept(BasicResult<CacheTaskConfig> cacheTaskConfigBasicResult) throws Exception {
                                System.out.println(cacheTaskConfigBasicResult.toString());
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                            }
                        }
                );
    }

}