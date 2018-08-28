package net.lzbook.kit.data.search;

import com.ding.basic.bean.SearchCommonBeanYouHua;

/**
 * Created by yuchao on 2018/1/6 0006.
 */

public class SearchSuggest {
    public int type;
    public SearchCommonBeanYouHua commonBean;

    public SearchSuggest(SearchCommonBeanYouHua commonBean, int type) {
        this.commonBean = commonBean;
        this.type = type;
    }

    public SearchSuggest(int type) {
        this.type = type;
    }
}
