package com.dingyue.searchbook

import com.alibaba.android.arouter.facade.annotation.Route
import com.dingyue.searchbook.activity.BaseSearchActivity
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

    override fun onClick(v: View) {
        if (v.id == R.id.search_result_input) {
            showEditCursor(true)
            showFragment(historyFragment)
            historyFragment.loadHistoryRecord()
        } else {
            super.onClick(v)
        }
    }

}

