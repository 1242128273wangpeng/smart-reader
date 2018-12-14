package com.dingyue.searchbook.activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import com.baidu.mobstat.StatService
import com.dingyue.searchbook.R
import com.dingyue.searchbook.fragment.HistoryFragment
import com.dingyue.searchbook.fragment.HotWordFragment
import com.dingyue.searchbook.fragment.SearchResultFragment
import com.dingyue.searchbook.fragment.SuggestFragment
import com.dingyue.searchbook.interfaces.OnKeyWordListener
import com.dingyue.searchbook.interfaces.OnResultListener
import com.dingyue.statistics.DyStatService
import kotlinx.android.synthetic.main.activity_base_search.*
import kotlinx.android.synthetic.main.fragment_search_result.*
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.ui.activity.base.FrameActivity
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.toast.ToastUtil
import java.util.*

/**
 * Desc 搜索Activity父类
 *
 * 【特殊壳】
 * 今日多看：没有推荐
 * 全本追书：没有推荐，热词和历史在一个Fragment
 * 全本免费热门小说：热词推荐Fragment在HomeActivity中
 *
 * 【特殊需求】
 * 点击热词，进入搜索结果页，搜索框无焦点，点击搜索框获取焦点时，进入历史记录页（其他壳进入自动补全页）
 * 快读替、今日多看
 *
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 18:14
 */
abstract class BaseSearchActivity : FrameActivity(), View.OnClickListener, TextWatcher, OnKeyWordListener {

    /**
     * 设置标题栏布局
     */
    abstract fun headLayout(): Int

    /**
     * 是否执行TextWatcher
     * 当搜索无结果页点击猜你喜欢子条目时，不执行
     */
    private var isRunTextWatcher = true

    /**
     * 当前显示的Fragment
     */
    var lastFragment: Fragment? = null

    private val hotWordFragment: HotWordFragment by lazy {
        HotWordFragment()
    }

    val historyFragment: HistoryFragment by lazy {
        HistoryFragment()
    }

    val suggestFragment: SuggestFragment by lazy {
        SuggestFragment()
    }

    val searchResultFragment: SearchResultFragment by lazy {
        SearchResultFragment()
    }

    private lateinit var headView: View
    private lateinit var backImgView: ImageView
    private lateinit var clearImgView: ImageView
    private lateinit var searchButton: View // TextView/ImageView
    private lateinit var focusTextView: View // TextView/RelativeLayout
    private lateinit var inputEditText: EditText
    private lateinit var defaultRelativeLayout: RelativeLayout

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.activity_base_search)

        headView = LayoutInflater.from(this).inflate(headLayout(), null, false)
        search_result_head.addView(headView)

        initView()
        initFragment()
        initListener()
        interceptKeyBoard()

        initHistoryFragmentListener()
        initHotWordFragmentListener()
        initSuggestFragmentListener()
        initSearchResultFragmentListener()
    }


    override fun onKeyWord(keyword: String?) {
        inputKeyWord(keyword ?: "")
        searchButton.performClick()
        showEditCursor(false)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_back -> {
                if (searchResultFragment.searchNoResult) DyStatService.onEvent(EventPoint.WEBSEARCHRESULT_BACK) else DyStatService.onEvent(EventPoint.SEARCH_BACK)
                finish()
            }
            R.id.search_result_clear -> {
                isRunTextWatcher = true
                inputEditText.setText("")
                clearImgView.visibility = View.GONE

                showSoftKeyboard(inputEditText)
                if (searchResultFragment.searchNoResult) DyStatService.onEvent(EventPoint.WEBSEARCHRESULT_BARCLEAR) else DyStatService.onEvent(EventPoint.SEARCH_BARCLEAR)
            }
            R.id.search_result_input -> {
                isRunTextWatcher = true
                searchResultFragment.isLoading = false
                showEditCursor(true)

                // 【修复】搜索输入框未输入内容的情况下，不应该显示上一次的搜索补全内容
                if (!inputEditText.isFocusable) {
                    showInputEditClickEvent() //搜索框无焦点的时候调用
                }

                // 【修复】在搜索结果页，点击输入框，应显示自动补全页，目前显示搜索结果页
                if (inputEditText.text.toString().isNotEmpty()) {
                    showInputEditClickEvent()
                }

                searchResultFragment.resetResult()


            }
            R.id.search_result_focus -> {
                focusTextView.visibility = View.GONE
                defaultRelativeLayout.visibility = View.VISIBLE
                searchResultFragment.isLoading = false

                inputEditText.requestFocus()
                showSoftKeyboard(inputEditText)

                showFragment(historyFragment)
                historyFragment.loadHistoryRecord()
                searchResultFragment.resetResult()
                DyStatService.onEvent(EventPoint.SEARCH_BAR)
            }
            R.id.search_result_btn -> {
                doSearchEvent("0")
            }
        }
    }


    /**
     * 拦截键盘的回车事件
     */
    private fun interceptKeyBoard() {
        inputEditText.setOnKeyListener { _, keyCode, _ ->

            when (keyCode) {
                KeyEvent.KEYCODE_ENTER -> {
                    doSearchEvent("1")
                    true
                }
                else -> false
            }
        }
    }


    /**
     * 执行搜索方法
     */
    private fun doSearchEvent(pointType: String) {
        val keyword = inputEditText.text.toString()
        if (TextUtils.isEmpty(keyword.trim())) {
            ToastUtil.showToastMessage(R.string.search_click_check_isright)
        } else {
            if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                return
            }
            if (!searchResultFragment.isLoading) {
                val data = HashMap<String, String>()
                data["type"] = pointType
                data["keyword"] = keyword
                DyStatService.onEvent(EventPoint.SEARCH_SEARCHBUTTON, data)
                showFragment(searchResultFragment)
                searchResultFragment.resetResult()
                searchResultFragment.loadKeyWord(keyword)
                hideKeyboard()
            }
        }
    }

    open fun initHistoryFragmentListener() {
        historyFragment.onKeyWordListener = this
    }

    open fun initHotWordFragmentListener() {
        hotWordFragment.onResultListener = object : OnResultListener<String> {
            override fun onSuccess(result: String) {
                inputKeyWord(result)
                showEditCursor(false)
                showFragment(searchResultFragment)
                searchResultFragment.loadKeyWord(result)
            }
        }
    }

    open fun initSuggestFragmentListener() {
        suggestFragment.onSuggestClickListener = object : SuggestFragment.OnSuggestClickListener {
            override fun onSuggestClick(history: String, searchType: String, isAuthor: Int) {
                inputKeyWord(history)
                showFragment(searchResultFragment)
                searchResultFragment.loadKeyWord(history, searchType, isAuthor)
            }

        }
    }

    open fun initSearchResultFragmentListener() {
        searchResultFragment.onResultListener = object : OnResultListener<String> {
            override fun onSuccess(result: String) {
                isRunTextWatcher = false
                inputKeyWord(result)
                showEditCursor(false)
            }
        }
    }

    open fun initFragment() {

        lastFragment = hotWordFragment

        supportFragmentManager.beginTransaction().add(R.id.search_result_hint, hotWordFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.search_result_hint, historyFragment)
                .hide(historyFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.search_result_hint, suggestFragment)
                .hide(suggestFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.search_result_hint, searchResultFragment)
                .hide(searchResultFragment).commit()

    }

    private fun initView() {

        backImgView = headView.findViewById(R.id.img_back)
        clearImgView = headView.findViewById(R.id.search_result_clear)
        searchButton = headView.findViewById(R.id.search_result_btn)
        focusTextView = headView.findViewById(R.id.search_result_focus)
        inputEditText = headView.findViewById(R.id.search_result_input)
        defaultRelativeLayout = headView.findViewById(R.id.search_result_default)

    }

    private fun initListener() {

        backImgView.setOnClickListener(this)
        clearImgView.setOnClickListener(this)
        searchButton.setOnClickListener(this)

        focusTextView.setOnClickListener(this)
        inputEditText.setOnClickListener(this)
        inputEditText.addTextChangedListener(this)

    }


    override fun afterTextChanged(editable: Editable?) {
        if (!isRunTextWatcher) return

        if (editable.toString().isNotEmpty() && inputEditText.isFocused) {
            clearImgView.visibility = View.VISIBLE
        } else {
            clearImgView.visibility = View.GONE
            editable?.clear()
        }

        val searchWord = AppUtils.deleteAllIllegalChar(editable.toString())

        //保存用户搜索词
        Tools.setUserSearchWord(searchWord)

        //当搜索词为空是显示搜索历史界面
        if (searchWord.isNullOrEmpty()) {
            showInputEditForNullFragment()
        } else {
            showFragment(suggestFragment)
            suggestFragment.obtainKeyWord(inputEditText.text.toString())
        }

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }


    /**
     * 当输入框内容为空时展示的Fragment
     */
    open fun showInputEditForNullFragment() {
        showFragment(historyFragment)
        historyFragment.loadHistoryRecord()
    }

    /**
     * 点击热词，进入搜索结果页，搜索框无焦点，点击搜索框获取焦点时，进入历史记录页（其他壳进入自动补全页）
     * 快读替、今日多看
     */
    open fun showInputEditClickEvent() {

//      showFragment(historyFragment)
//      historyFragment.loadHistoryRecord()

        showFragment(suggestFragment)
        suggestFragment.obtainKeyWord(inputEditText.text.toString())

    }

    /**
     * 设置关键词，将光标移至文字末尾（热词、历史子条目）
     */
    fun inputKeyWord(keyword: String) {
        focusTextView.visibility = View.GONE
        defaultRelativeLayout.visibility = View.VISIBLE
        inputEditText.requestFocus()
        inputEditText.setText(keyword)
        inputEditText.setSelection(keyword.length)
    }


    /**
     * 是否展示输入框的光标：
     * true：展示光标，同时显示清除按钮
     */
    fun showEditCursor(isShowCursor: Boolean) {
        inputEditText.isCursorVisible = isShowCursor
        clearImgView.visibility = if (isShowCursor) View.VISIBLE else View.GONE
    }


    /**
     * 展示所选的Fragment，展示前先隐藏之前的fragment
     */
    fun showFragment(fragment: Fragment) {
        if (lastFragment != null && lastFragment != fragment) {
            supportFragmentManager.beginTransaction().hide(lastFragment).commit()
        }
        lastFragment = fragment
        supportFragmentManager.beginTransaction().show(fragment).commit()
    }


    /**
     * 处理软键盘事件
     */
    fun showSoftKeyboard(view: View?) {
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
     * 隐藏输入法键盘
     */
    override fun hideKeyboard() {
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(this.currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }


    override fun onResume() {
        super.onResume()

        //判断结果页是否展示，调js刷新方法
        if (lastFragment == searchResultFragment) {
            val keyword = inputEditText.text.toString()
            if (keyword.isNotEmpty()) {
                search_result_content?.post {
                    try {
                        search_result_content?.loadUrl("javascript:refreshNew()")
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                        finish()
                    }
                }
            }
        }

        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
    }

}