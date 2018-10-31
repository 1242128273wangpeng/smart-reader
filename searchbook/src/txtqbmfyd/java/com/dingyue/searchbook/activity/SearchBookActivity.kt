package com.dingyue.searchbook.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.dingyue.searchbook.R
import net.lzbook.kit.utils.router.RouterConfig


/**
 * Desc
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 18:14
 */
@Route(path = RouterConfig.SEARCH_BOOK_ACTIVITY)
class SearchBookActivity : BaseSearchActivity() {

    override fun headLayout(): Int = R.layout.activity_search_book

}

