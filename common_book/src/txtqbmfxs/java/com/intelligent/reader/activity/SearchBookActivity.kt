package com.intelligent.reader.activity

import com.intelligent.reader.R
import com.intelligent.reader.search.SearchHelper
import com.intelligent.reader.util.SearchViewHelper

import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.HWEditText
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.CustomWebClient
import net.lzbook.kit.utils.JSInterfaceHelper
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.Tools

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.InflateException
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.dingyue.contract.router.RouterConfig

import java.util.HashMap

import iyouqu.theme.FrameActivity

/**
 * Function：搜索书籍页
 *
 * Created by JoannChen on 2018/6/14 0013 21:08
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
@Route(path = RouterConfig.SEARCH_BOOK_ACTIVITY)
class SearchBookActivity : FrameActivity(), OnClickListener, OnFocusChangeListener, SearchViewHelper.OnHistoryClickListener, TextWatcher, OnEditorActionListener, SearchHelper.JsCallSearchCall, SearchHelper.StartLoadCall, SearchHelper.JsNoneResultSearchCall {

    private var search_result_back: ImageView? = null
    private var search_result_button: ImageView? = null
    private var search_result_outcome: RelativeLayout? = null
    private var search_result_count: TextView? = null
    private var search_result_keyword: TextView? = null
    private var search_result_default: RelativeLayout? = null
    private var search_result_clear: ImageView? = null
    private var search_result_input: HWEditText? = null
    private var search_result_main: RelativeLayout? = null
    private var search_result_content: WebView? = null
    private var search_result_hint: FrameLayout? = null

    private var searchViewHelper: SearchViewHelper? = null
    private var handler: Handler? = Handler()

    private var customWebClient: CustomWebClient? = null
    private var jsInterfaceHelper: JSInterfaceHelper? = null

    internal var isSearch = false
    //记录是否退出当前界面,for:修复退出界面时出现闪影
    internal var isBackPressed = false

    private var loadingPage: LoadingPage? = null

    private var mSearchHelper: SearchHelper? = null

    internal var ziyougb: Boolean = false
    override fun onJsSearch() {
        if (search_result_content != null) {
            search_result_content!!.clearView()
            if (loadingPage == null) {
                loadingPage = LoadingPage(this, search_result_main, LoadingPage.setting_result)
            }
        }
    }

    override fun onStartLoad(url: String) {
        startLoading(handler, url)
        webViewCallback()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_search_book)
        } catch (e: InflateException) {
            e.printStackTrace()
            return
        }

        initData()
        initView()
        if (mSearchHelper != null && !TextUtils.isEmpty(mSearchHelper!!.word)) {
            loadDataFromNet(isNotAuthor)
        }
    }

    private fun initView() {
        search_result_back = findViewById(R.id.search_result_back) as ImageView
        search_result_button = findViewById(R.id.search_result_button) as ImageView
        search_result_outcome = findViewById(R.id.search_result_outcome) as RelativeLayout
        if (search_result_outcome != null) {
            search_result_outcome!!.visibility = View.VISIBLE
        }
        search_result_count = findViewById(R.id.search_result_count) as TextView
        search_result_keyword = findViewById(R.id.search_result_keyword) as TextView
        search_result_default = findViewById(R.id.search_result_default) as RelativeLayout
        search_result_clear = findViewById(R.id.search_result_clear) as ImageView
        if (search_result_clear != null) {
            search_result_clear!!.visibility = View.GONE
        }
        search_result_input = findViewById(R.id.search_result_input) as HWEditText
        search_result_main = findViewById(R.id.search_result_main) as RelativeLayout

        search_result_content = findViewById(R.id.search_result_content) as WebView

        search_result_hint = findViewById(R.id.search_result_hint) as FrameLayout

        if (mSearchHelper == null) {
            mSearchHelper = SearchHelper(this)
        }

        if (searchViewHelper == null) {
            searchViewHelper = SearchViewHelper(this, search_result_hint, search_result_input, mSearchHelper)
        }

        initListener()

        if (Build.VERSION.SDK_INT >= 11) {
            search_result_content!!.setLayerType(View.LAYER_TYPE_NONE, null)
        }

        if (search_result_content != null) {
            customWebClient = CustomWebClient(this, search_result_content)
        }

        if (search_result_content != null && customWebClient != null) {
            customWebClient!!.setWebSettings()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                search_result_content!!.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            search_result_content!!.webViewClient = customWebClient
        }

        if (search_result_content != null) {
            jsInterfaceHelper = JSInterfaceHelper(this, search_result_content)
        }

        if (jsInterfaceHelper != null && search_result_content != null) {
            search_result_content!!.addJavascriptInterface(jsInterfaceHelper, "J_search")
            mSearchHelper!!.initJSHelp(jsInterfaceHelper)
        }

    }


    private fun initListener() {
        if (mSearchHelper != null) {
            mSearchHelper!!.setJsCallSearchCall(this)
            mSearchHelper!!.setJsNoneResultSearchCall(this)
            mSearchHelper!!.setStartLoadCall(this)
        }

        if (search_result_back != null) {
            search_result_back!!.setOnClickListener(this)
        }

        if (search_result_button != null) {
            search_result_button!!.setOnClickListener(this)
        }

        if (search_result_outcome != null) {
            search_result_outcome!!.setOnClickListener(this)
        }

        if (search_result_count != null) {
            search_result_count!!.setOnClickListener(this)
        }

        if (search_result_default != null) {
            search_result_default!!.setOnClickListener(this)
        }

        if (search_result_keyword != null) {
            search_result_keyword!!.setOnClickListener(this)
        }

        if (search_result_clear != null) {
            search_result_clear!!.setOnClickListener(this)
        }

        if (search_result_input != null) {
            search_result_input!!.setOnClickListener(this)
            search_result_input!!.onFocusChangeListener = this
            search_result_input!!.addTextChangedListener(this)
            search_result_input!!.setOnEditorActionListener(this)
        }

        if (searchViewHelper != null) {
            searchViewHelper!!.setOnHistoryClickListener(this)
            searchViewHelper!!.onHotWordClickListener = SearchViewHelper.OnHotWordClickListener { tag, searchType ->
                if (mSearchHelper != null) {
                    mSearchHelper!!.setHotWordType(tag, searchType)
                }
                loadDataFromNet(isNotAuthor)
            }

        }
    }

    private fun initData() {
        if (mSearchHelper == null) {
            mSearchHelper = SearchHelper(this)
        }
        val intent = intent
        if (intent != null) {
            mSearchHelper!!.setInitType(intent)
        }
        if (searchViewHelper != null && !TextUtils.isEmpty(mSearchHelper!!.word)) {
            searchViewHelper!!.setSearchWord(mSearchHelper!!.word)
        }

        if (bookDaoHelper == null) {
            bookDaoHelper = BookDaoHelper.getInstance()
        }
    }

    private fun loadDataFromNet(isAuthor: Int) {

        if (mSearchHelper == null) {
            mSearchHelper = SearchHelper(this)
        }

        if (search_result_count != null) {
            search_result_count!!.text = null
        }

        if (!TextUtils.isEmpty(mSearchHelper!!.word)) {
            if (search_result_input != null)
                if (isAuthor != 1) {
                    search_result_input!!.setText(mSearchHelper!!.word)
                    //                    search_result_input.setTextColor(getResources().getColor(R.color.search_title_hint));
                } else {
                    mSearchHelper!!.searchType = "2"
                    search_result_input!!.setText(Tools.getKeyWord())
                    //                    search_result_input.setTextColor(getResources().getColor(R.color.search_title_hint));
                }
            if (search_result_keyword != null) {
                if (isAuthor != 1) {
                    search_result_keyword!!.text = mSearchHelper!!.word
                    //                    search_result_keyword.setTextColor(getResources().getColor(R.color.search_title_hint));
                } else {
                    mSearchHelper!!.searchType = "2"
                    search_result_keyword!!.text = Tools.getKeyWord()
                    //                    search_result_keyword.setTextColor(getResources().getColor(R.color.search_title_hint));
                }
            }

            if (searchViewHelper != null) {
                searchViewHelper!!.addHistoryWord(mSearchHelper!!.word)
            }

            hideSearchView()

            if (loadingPage == null) {
                loadingPage = LoadingPage(this, search_result_main, LoadingPage.setting_result)
            }

            mSearchHelper!!.startLoadData(isAuthor)

        } else {
            showSearchViews()
        }
    }


    private fun startLoading(handler: Handler?, url: String) {
        if (search_result_content == null) {
            return
        }
        search_result_main!!.visibility = View.VISIBLE
        handler?.post { loadingData(url) } ?: loadingData(url)
    }

    private fun loadingData(url: String) {
        if (customWebClient != null) {
            customWebClient!!.doClear()
        }
        AppLog.e(TAG, "LoadingData ==> " + url)
        if (!TextUtils.isEmpty(url) && search_result_content != null) {
            try {
                search_result_content!!.loadUrl(url)
            } catch (e: NullPointerException) {
                e.printStackTrace()
                this.finish()
            }

        }
    }

    private fun webViewCallback() {

        if (search_result_content == null) {
            return
        }

        if (customWebClient != null) {
            customWebClient!!.setStartedAction { url ->
                AppLog.e(TAG, "onLoadStarted: " + url)
                if (mSearchHelper == null) {
                    mSearchHelper = SearchHelper(this@SearchBookActivity)
                }
                mSearchHelper!!.setStartedAction()
            }

            customWebClient!!.setErrorAction {
                AppLog.e(TAG, "onErrorReceived")
                if (loadingPage != null) {
                    AppLog.e(TAG, "loadingPage != Null")
                    loadingPage!!.onErrorVisable()
                }
            }

            customWebClient!!.setFinishedAction {
                AppLog.e(TAG, "onLoadFinished")
                if (mSearchHelper == null) {
                    mSearchHelper = SearchHelper(this@SearchBookActivity)
                }
                mSearchHelper!!.onLoadFinished()
                if (loadingPage != null) {
                    if (isSearch) {
                        hideSearchView()
                    }
                    loadingPage!!.onSuccessGone()
                }
            }
        }

        if (loadingPage != null) {
            loadingPage!!.setReloadAction(LoadingPage.reloadCallback {
                AppLog.e(TAG, "doReload")
                if (customWebClient != null) {
                    customWebClient!!.doClear()
                }
                search_result_content!!.reload()
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (isSatyHistory && searchViewHelper != null && searchViewHelper!!.showStatus) {
            if (mSearchHelper != null && mSearchHelper!!.fromClass != null && mSearchHelper!!.fromClass != "fromClass") {
                val historyDates = Tools.getKeyWord()

                if (search_result_input != null) {
                    search_result_input!!.requestFocus()
                    search_result_input!!.setText(historyDates)
                    //设置光标的索引
                    val index = search_result_input!!.text
                    search_result_input!!.setSelection(index.length)
                    showSearchViews()
                }
            }

        }
    }

    /**
     * 重载方法
     */
    override fun onPause() {
        super.onPause()
    }

    /**
     * 重载方法
     */
    override fun onStop() {
        super.onStop()
    }

    /**
     * 重载方法
     */
    override fun onDestroy() {
        if (mSearchHelper != null) {
            mSearchHelper!!.onDestroy()
        }
        if (search_result_content != null) {
            search_result_content!!.clearCache(true) //清空缓存
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (search_result_main != null) {
                    search_result_main!!.removeView(search_result_content)
                }
                search_result_content!!.stopLoading()
                search_result_content!!.removeAllViews()
                //search_result_content.destroy();
            } else {
                search_result_content!!.stopLoading()
                search_result_content!!.removeAllViews()
                //search_result_content.destroy();
                if (search_result_main != null) {
                    search_result_main!!.removeView(search_result_content)
                }
            }
            search_result_content = null
        }

        if (loadingPage != null) {
            loadingPage = null
        }

        if (searchViewHelper != null) {
            searchViewHelper!!.onDestroy()
            searchViewHelper = null
        }

        super.onDestroy()

    }

    override fun onBackPressed() {
        isBackPressed = true
        super.onBackPressed()
    }


    private fun showSearchViews() {
        if (NetWorkUtils.getNetWorkType(this) == NetWorkUtils.NETWORK_NONE) {
            return
        }
        isSearch = true
        if (search_result_outcome != null && search_result_outcome!!.visibility != View.GONE) {
            search_result_outcome!!.visibility = View.GONE
        }

        if (search_result_default != null && search_result_default!!.visibility != View.VISIBLE) {
            search_result_default!!.visibility = View.VISIBLE
        }

        if (search_result_content != null && search_result_content!!.visibility != View.GONE) {
            search_result_content!!.visibility = View.GONE
        }

        if (mSearchHelper == null) {
            mSearchHelper = SearchHelper(this)
        }
        mSearchHelper!!.word = search_result_input!!.text.toString()

        if (!TextUtils.isEmpty(mSearchHelper!!.word)) {

            //            search_result_input.setText(word);
            //            search_result_input.setSelection(word.length());

            if (search_result_hint != null) {
                search_result_hint!!.visibility = View.GONE
            }

            if (searchViewHelper != null) {
                searchViewHelper!!.hideRecommendListView()
            }

            if (searchViewHelper != null) {
                //                String finalContent = AppUtils.deleteAllIllegalChar(mSearchHelper.getWord());
                searchViewHelper!!.showHintList()
                //                searchViewHelper.showRemainWords(finalContent);
            }

        } else {
            search_result_input!!.setText(null)
            search_result_input!!.editableText.clear()
            search_result_input!!.text.clear()
        }
        search_result_input!!.requestFocus()
        dealSoftKeyboard(search_result_input)
    }

    private fun hideSearchView() {
        isSearch = false

        if (search_result_outcome != null && search_result_outcome!!.visibility != View.VISIBLE && !isBackPressed) {
            search_result_outcome!!.visibility = View.VISIBLE
        }

        if (search_result_default != null && search_result_default!!.visibility != View.GONE) {
            search_result_default!!.visibility = View.GONE
        }

        if (search_result_content != null && search_result_content!!.visibility != View.VISIBLE && !isBackPressed) {
            search_result_content!!.visibility = View.VISIBLE
        }

        if (search_result_input != null) {
            search_result_input!!.clearFocus()
        }

        if (searchViewHelper != null) {
            searchViewHelper!!.hideHintList()
        }
        if (search_result_hint != null) {
            search_result_hint!!.visibility = View.GONE
        }
    }

    protected fun backAction() {
        isBackPressed = true
        finish()
    }

    fun dealSoftKeyboard(view: View?) {
        if (handler == null) {
            handler = Handler()
        }
        handler!!.postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            // 弹出软键盘
            if (view != null) {
                imm.showSoftInput(view, 0)
            }
        }, 500)

    }

    fun hideInputMethod(paramView: View?) {
        if (paramView == null || paramView.context == null)
            return
        val imm = paramView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(paramView.applicationWindowToken, 0)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.search_result_back -> {
                val data1 = HashMap<String, String>()
                data1.put("type", "1")
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BACK, data1)
                backAction()
            }

            R.id.search_result_clear -> {
                ziyougb = true
                if (search_result_input != null)
                    search_result_input!!.setText(null)
                if (search_result_clear != null)
                    search_result_clear!!.visibility = View.GONE
                dealSoftKeyboard(search_result_input)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCHRESULT_PAGE, StartLogClickUtil.CLEAR)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BARCLEAR)
            }

            R.id.search_result_outcome, R.id.search_result_keyword, R.id.search_result_count ->
                //                if (search_result_input != null)
                //                    search_result_input.setSelection(search_result_input.length());
                showSearchViews()

            R.id.search_result_default, R.id.search_result_input -> showSearchViews()

            R.id.search_result_button -> {
                var keyword: String? = null

                if (searchViewHelper != null) {
                    searchViewHelper!!.isFocus = false
                }

                if (search_result_input != null) {
                    keyword = search_result_input!!.text.toString()
                }
                if (keyword != null && TextUtils.isEmpty(keyword.trim { it <= ' ' })) {
                    showToastShort(R.string.search_click_check_isright)
                } else {
                    hideInputMethod(search_result_input)
                    if (keyword != null && !TextUtils.isEmpty(keyword.trim { it <= ' ' }) && searchViewHelper != null) {
                        searchViewHelper!!.addHistoryWord(keyword)
                        if (mSearchHelper == null) {
                            mSearchHelper = SearchHelper(this)
                        }

                        if (mSearchHelper!!.fromClass != null && mSearchHelper!!.fromClass != "other") {
                            if (mSearchHelper!!.searchType != null) {
                                mSearchHelper!!.setHotWordType(keyword, mSearchHelper!!.searchType)
                                AppLog.e("type14", mSearchHelper!!.searchType + "===")
                            }
                        } else {

                            if (mSearchHelper!!.searchType != null && mSearchHelper!!.searchType != "0") {
                                AppLog.e("type12", mSearchHelper!!.searchType + "===")
                                mSearchHelper!!.setHotWordType(keyword, mSearchHelper!!.searchType)
                            } else {
                                AppLog.e("type12", 0.toString() + "===")
                                mSearchHelper!!.setHotWordType(keyword, "0")
                                mSearchHelper!!.searchType = "0"
                            }
                        }
                        loadDataFromNet(isNotAuthor)

                        val data = HashMap<String, String>()
                        data.put("type", "0")
                        data.put("keyword", keyword)
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.SEARCHBUTTON, data)
                    } else {
                        showSearchViews()
                    }
                }
            }

            else -> {
            }
        }
    }

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        if (search_result_input == null)
            return
        if (hasFocus) {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BAR)
            dealSoftKeyboard(search_result_input)

            if (search_result_content != null && search_result_content!!.visibility != View.GONE) {
                search_result_content!!.visibility = View.GONE
            }

            if (searchViewHelper != null && search_result_input != null) {
                if (mSearchHelper == null) {
                    mSearchHelper = SearchHelper(this)
                }

                if (TextUtils.isEmpty(mSearchHelper!!.word)) {
                    search_result_input!!.text.clear()
                    search_result_input!!.editableText.clear()
                    search_result_input!!.setText(null)
                } else {
                    if (!ziyougb) {
                        search_result_input!!.setText(mSearchHelper!!.word)
                        search_result_input!!.setSelection(mSearchHelper!!.word.length)
                    }
                }
                search_result_keyword!!.setTextColor(resources.getColor(R.color.search_input_text_color))
                search_result_input!!.setTextColor(resources.getColor(R.color.search_input_text_color))
                //判断当用户没有对editText进行操作时（即编辑框没有内容时），显示搜索历史
                if (search_result_input!!.text.toString() == "") {
                    searchViewHelper!!.showHistoryList()
                }
            }
            ziyougb = true
        } else {
            ziyougb = false
            hideInputMethod(search_result_input)
        }
    }


    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }


    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }


    override fun afterTextChanged(s: Editable) {
        if (mSearchHelper != null && mSearchHelper!!.word != null) {
            if (mSearchHelper!!.fromClass != null) {
                if (mSearchHelper!!.word.trim { it <= ' ' } != s.toString().trim { it <= ' ' }) {
                    AppLog.e("typ11", "typ111")
                    if (mSearchHelper!!.fromClass != "other") {
                        AppLog.e("typ", "typ")
                        mSearchHelper!!.fromClass = "other"
                    }
                    mSearchHelper!!.searchType = "0"
                }
            } else {
                if (mSearchHelper!!.word.trim { it <= ' ' } != s.toString().trim { it <= ' ' }) {
                    mSearchHelper!!.searchType = "0"
                }
            }
        }

        if (search_result_clear == null) {
            return
        }

        if (!TextUtils.isEmpty(s.toString())) {
            if (search_result_input!!.isFocused == true) {
                search_result_clear!!.visibility = View.VISIBLE
            } else {
                search_result_clear!!.visibility = View.GONE
            }
        } else {
            search_result_clear!!.visibility = View.GONE
            s.clear()
            search_result_main!!.visibility = View.GONE
        }

        //保存用户搜索词
        Tools.setUserSearchWord(s.toString())

        //网络请求
        if (searchViewHelper != null) {
            val finalContent = AppUtils.deleteAllIllegalChar(s.toString())
            searchViewHelper!!.showRemainWords(finalContent)
        }
    }

    override fun OnHistoryClick(history: String, searchType: String, isAuthor: Int) {
        if (mSearchHelper == null) {
            mSearchHelper = SearchHelper(this)
        }
        mSearchHelper!!.setHotWordType(history, searchType)
        if ("3" == searchType) {

        } else {
            loadDataFromNet(isAuthor)
        }
    }


    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_NEXT ||
                actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            var keyword: String? = null
            if (search_result_input != null) {
                keyword = search_result_input!!.text.toString()
            }
            if (keyword != null && keyword.trim { it <= ' ' } == "") {
                showToastShort(R.string.search_click_check_isright)
            } else {

                searchViewHelper!!.isFocus = false
                hideInputMethod(v)
                if (keyword != null && keyword != "" && searchViewHelper != null) {
                    searchViewHelper!!.addHistoryWord(keyword)
                    mSearchHelper!!.setHotWordType(keyword, "0")
                    loadDataFromNet(isNotAuthor)

                    val data = HashMap<String, String>()
                    data.put("type", "1")
                    data.put("keyword", keyword)
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.SEARCHBUTTON, data)
                }
            }
            return true
        }

        return false
    }

    override fun onNoneResultSearch(searchWord: String) {

        if (search_result_default != null && search_result_default!!.visibility != View.VISIBLE) {
            search_result_default!!.visibility = View.VISIBLE
        }

        if (search_result_input != null) {
            search_result_input!!.setText(searchWord)
        }

        if (searchViewHelper != null) {
            searchViewHelper!!.addHistoryWord(searchWord)
            searchViewHelper!!.hideHintList()
        }

        if (search_result_content != null) {
            search_result_content!!.clearView()
            if (loadingPage == null) {
                loadingPage = LoadingPage(this, search_result_main, LoadingPage.setting_result)
            }
        }
    }

    companion object {
        //静态变量定义是否在在进入searchBookActivity中初始化显示上次的搜索界面
        var isSatyHistory = false

        val isNotAuthor = 0//不是作者
    }
}