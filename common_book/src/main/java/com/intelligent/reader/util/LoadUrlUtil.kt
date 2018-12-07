package com.intelligent.reader.util

import android.os.Bundle
import com.ding.basic.config.WebViewConfig
import com.ding.basic.net.Config
import com.intelligent.reader.fragment.WebViewFragment
import com.orhanobut.logger.Logger
import net.lzbook.kit.constants.ReplaceConstants


/**
 * Desc：bundle传参
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/12/6 0006 11:56
 */
fun fragmentBundle(type: String = "", webViewIndex: String): WebViewFragment {
    val fragment = WebViewFragment()
    val bundle = Bundle()
    bundle.putString("type", type)
    bundle.putString("url", loadWebViewUrl(webViewIndex))
    fragment.arguments = bundle
    return fragment
}

/**
 * WebView加载，判断是加载本地还是在线地址
 */
fun loadWebViewUrl(url: String): String {
    val webViewHost = Config.webViewBaseHost

    val filePath = webViewHost.replace(WebViewConfig.urlPath,
            ReplaceConstants.getReplaceConstants().APP_PATH_CACHE) + "/index.html"

    Logger.e("JoannChen WebView地址: $webViewHost ${Config.webCacheAvailable}")

    return if (Config.webCacheAvailable) {
        "file://$filePath$url"
    } else {
        Config.webViewBaseHost + "/index.html" + url
    }
}
