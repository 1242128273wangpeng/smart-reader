package net.lzbook.kit.request;

import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.request.own.OtherRequestExecutor;

public class RequestFactory {

    //枚举请求主机名
    public enum RequestHost {
        YS("b.easou.com"), SG("k.sogou.com"),QG("api.qingoo.cn");

        public final String requestHost;

        RequestHost(String requestHost) {
            this.requestHost = requestHost;
        }

        public String getValue() {
            return requestHost;
        }
    }


    private static String TAG = RequestFactory.class.getSimpleName();

    private static RequestFactory requestFactory = null;

    public RequestFactory() {

    }

    public RequestExecutor requestExecutor(RequestItem requestItem) {
        RequestExecutor requestExecutor;

        if (RequestHost.QG.requestHost.equals(requestItem.host)){
            requestExecutor = new QGRequestExecutor();
        }else {
            requestExecutor = new OtherRequestExecutor();
        }

        return requestExecutor;
    }

}
