package com.dingyue.searchbook.activity

import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.dingyue.searchbook.R
import com.dingyue.searchbook.activity.BaseSearchActivity
import com.dingyue.searchbook.fragment.HotAndHisFragment
import com.dingyue.searchbook.interfaces.OnResultListener
import kotlinx.android.synthetic.qbzsydq.activity_search_book.*
import net.lzbook.kit.utils.router.RouterConfig


/**
 * Desc 热词和历史记录在一个fragment
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 18:14
 */
@Route(path = RouterConfig.SEARCH_BOOK_ACTIVITY)
class SearchBookActivity : BaseSearchActivity() {

    override fun headLayout(): Int = R.layout.activity_search_book

    private val hotAndHisFragment: HotAndHisFragment by lazy {
        HotAndHisFragment()
    }

    override fun onClick(v: View) {
        if (v.id == R.id.search_result_focus) {
            search_result_focus.visibility = View.GONE
            search_result_default.visibility = View.VISIBLE

            search_result_input.requestFocus()
            showSoftKeyboard(search_result_input)

            showFragment(hotAndHisFragment)
            hotAndHisFragment.loadHistoryRecord()
        } else {
            super.onClick(v)
        }

    }

    override fun initFragment() {
        lastFragment = hotAndHisFragment

        supportFragmentManager.beginTransaction().add(R.id.search_result_hint, hotAndHisFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.search_result_hint, suggestFragment)
                .hide(suggestFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.search_result_hint, searchResultFragment)
                .hide(searchResultFragment).commit()

    }

    override fun initHistoryFragmentListener() {
        hotAndHisFragment.onKeyWordListener = this
    }

    override fun initHotWordFragmentListener() {
        hotAndHisFragment.onResultListener = object : OnResultListener<String> {
            override fun onSuccess(result: String) {
                inputKeyWord(result)
                showEditCursor(false)
                showFragment(searchResultFragment)
                searchResultFragment.loadKeyWord(result)
            }
        }
    }

    override fun showInputEditForNullFragment() {
        showFragment(hotAndHisFragment)
        hotAndHisFragment.loadHistoryRecord()
    }

}

