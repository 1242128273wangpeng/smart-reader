package com.dingyue.searchbook

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.dingyue.searchbook.fragment.HistoryFragment
import com.dingyue.searchbook.fragment.HotWordFragment
import com.dingyue.searchbook.fragment.SearchResultFragment
import com.dingyue.searchbook.fragment.SuggestFragment
import com.dingyue.searchbook.interfaces.OnKeyWordListener
import com.dingyue.searchbook.interfaces.OnResultListener
import kotlinx.android.synthetic.txtqbmfxs.activity_search_book.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.ui.activity.base.FrameActivity
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.toast.ToastUtil


/**
 * Desc
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 18:14
 */
@Route(path = RouterConfig.SEARCH_BOOK_ACTIVITY)
class SearchBookActivity : FrameActivity(), View.OnClickListener, TextWatcher, OnKeyWordListener {

    private var lastFragment: Fragment? = null

    private val historyFragment: HistoryFragment by lazy {
        HistoryFragment()
    }

    private val hotWordFragment: HotWordFragment by lazy {
        HotWordFragment()
    }

    private val suggestFragment: SuggestFragment by lazy {
        SuggestFragment()
    }

    private val searchResultFragment: SearchResultFragment by lazy {
        SearchResultFragment()
    }

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.activity_search_book)
        initView()
        initListener()
        interceptKeyBoard()
    }

    override fun onClick(v: View) {
        when (v.id) {
            img_back.id -> finish()
            search_result_clear.id -> {

                search_result_input.text = null
                search_result_clear.visibility = View.GONE

                showSoftKeyboard(search_result_input)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BARCLEAR)
            }
            search_result_focus.id
            -> {
                search_result_focus.visibility = View.GONE
                search_result_default.visibility = View.VISIBLE

                search_result_input.requestFocus()
                showSoftKeyboard(search_result_input)

                showFragment(historyFragment)
                historyFragment.loadHistoryRecord()
            }
            search_result_btn.id -> {
                val keyword = search_result_input.text.toString()
                if (TextUtils.isEmpty(keyword.trim())) {
                    ToastUtil.showToastMessage(R.string.search_click_check_isright)
                } else {
                    showFragment(searchResultFragment)
                    searchResultFragment.loadKeyWord(keyword)
                }
            }
        }
    }

    override fun onKeyWord(keyword: String?) {
        inputKeyWord(keyword ?: "")
        search_result_btn.performClick()
    }

    private fun initView() {

        lastFragment = hotWordFragment

        supportFragmentManager.beginTransaction().add(R.id.search_result_hint, hotWordFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.search_result_hint, historyFragment)
                .hide(historyFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.search_result_hint, suggestFragment)
                .hide(suggestFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.search_result_hint, searchResultFragment)
                .hide(searchResultFragment).commit()

    }

    private fun initListener() {

        img_back.setOnClickListener(this)
        search_result_clear.setOnClickListener(this)
        search_result_focus.setOnClickListener(this)
        search_result_input.setOnClickListener(this)
        search_result_input.addTextChangedListener(this)
        search_result_btn.setOnClickListener(this)

        historyFragment.onKeyWordListener = this

        suggestFragment.onSuggestClickListener = object : SuggestFragment.OnSuggestClickListener {
            override fun onSuggestClick(history: String, searchType: String) {
                inputKeyWord(history)
                showFragment(searchResultFragment)
                searchResultFragment.loadKeyWord(history, searchType)
            }

        }

        hotWordFragment.onResultListener = object : OnResultListener<String> {
            override fun onSuccess(result: String) {
                inputKeyWord(result)
                showFragment(searchResultFragment)
                searchResultFragment.loadKeyWord(result)
            }
        }

    }


    /**
     * 设置关键词，将光标移至文字末尾（热词、历史子条目）
     */
    private fun inputKeyWord(keyword: String) {
        search_result_focus.visibility = View.GONE
        search_result_default.visibility = View.VISIBLE
        search_result_input.requestFocus()
        search_result_input.setText(keyword)
        search_result_input.setSelection(keyword.length)
    }

    override fun afterTextChanged(editable: Editable?) {

        if (editable.toString().isNotEmpty() && search_result_input.isFocused) {
            search_result_clear.visibility = View.VISIBLE
        } else {
            search_result_clear.visibility = View.GONE
            editable?.clear()
        }


        val searchWord = AppUtils.deleteAllIllegalChar(editable.toString())

        //保存用户搜索词
        Tools.setUserSearchWord(searchWord)

        //当搜索词为空是显示搜索历史界面
        if (searchWord.isNullOrEmpty()) {
            showFragment(historyFragment)
            historyFragment.loadHistoryRecord()
        } else {
            showFragment(suggestFragment)
            suggestFragment.obtainKeyWord(search_result_input.text.toString())
        }

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    /**
     * 展示所选的Fragment，展示前先隐藏之前的fragment
     */
    private fun showFragment(fragment: Fragment) {
        if (lastFragment != null && lastFragment != fragment) {
            supportFragmentManager.beginTransaction().hide(lastFragment).commit()
        }
        lastFragment = fragment
        supportFragmentManager.beginTransaction().show(fragment).commit()
    }

    /**
     * 处理软键盘事件
     */
    private fun showSoftKeyboard(view: View?) {
        val handler = Handler()
        handler.postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // 弹出软键盘
            if (view != null) {
                imm.showSoftInput(view, 0)
            }
        }, 500)

    }


    /**
     * 拦截键盘的回车事件
     */
    private fun interceptKeyBoard() {

        search_result_input.setOnKeyListener { _, keyCode, _ ->

            when (keyCode) {
                KeyEvent.KEYCODE_ENTER -> {
                    val keyword = search_result_input.text.toString()
                    if (TextUtils.isEmpty(keyword.trim())) {
                        ToastUtil.showToastMessage(R.string.search_click_check_isright)
                    } else {
                        showFragment(searchResultFragment)
                        searchResultFragment.loadKeyWord(keyword)
                        hideKeyboard()
                    }
                    true
                }
                else -> false
            }
        }
    }

}

