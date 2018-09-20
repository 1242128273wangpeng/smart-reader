package net.lzbook.kit.utils.encrypt;

import java.util.Map;

/**
 * 替换类：URLBuilder
 */
public interface URLBuilderIntterface {

    @Deprecated
    String buildUrl(String host, String uriTag, Map<String, String> params);

    @Deprecated
    String buildContentUrl(String url, Map<String, String> params);

}
