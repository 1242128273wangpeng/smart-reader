package com.dingyue.searchbook

import com.alibaba.android.arouter.facade.annotation.Route
import com.dingyue.searchbook.activity.BaseSearchActivity
import kotlinx.android.synthetic.qbmfrmxs.activity_search_book.*
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

    override fun isShowHistoryFragment(): Boolean = false

    override fun initFragment() {

        // 接收HotWordFragment子条目的点击事件
        val bundle = intent.extras
        val keyword = bundle.getString("keyWord")
        if (bundle.getBoolean("showSearchResult")) {
            inputKeyWord(keyword)
            showEditCursor(false)
            lastFragment = searchResultFragment

            supportFragmentManager.beginTransaction().add(R.id.search_result_hint, searchResultFragment).commit()
            supportFragmentManager.beginTransaction().add(R.id.search_result_hint, historyFragment).hide(historyFragment).commit()
            search_result_btn.postDelayed({
                searchResultFragment.loadKeyWord(keyword)
            }, 100)
        } else {
            lastFragment = historyFragment

            supportFragmentManager.beginTransaction().add(R.id.search_result_hint, historyFragment).commit()
            supportFragmentManager.beginTransaction().add(R.id.search_result_hint, searchResultFragment).hide(searchResultFragment).commit()

            search_result_focus.postDelayed({
                search_result_focus.performClick()
            }, 100)

        }

        supportFragmentManager.beginTransaction().add(R.id.search_result_hint, suggestFragment).hide(suggestFragment).commit()
    }

}

