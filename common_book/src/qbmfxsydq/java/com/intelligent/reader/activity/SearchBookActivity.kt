package com.intelligent.reader.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
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
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.util.CommonUtil
import com.intelligent.reader.R
import com.intelligent.reader.util.SearchHelper
import com.intelligent.reader.util.SearchViewHelper
import iyouqu.theme.FrameActivity
import kotlinx.android.synthetic.qbmfxsydq.activity_search_book.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.utils.*
import java.util.*

@Route(path = RouterConfig.SEARCH_BOOK_ACTIVITY)
open class SearchBookActivity : FrameActivity(), OnClickListener, OnFocusChangeListener, SearchViewHelper.OnHistoryClickListener, TextWatcher, OnEditorActionListener, SearchHelper.JsCallSearchCall, SearchHelper.StartLoadCall, SearchHelper.JsNoneResultSearchCall {

    private var mSearchViewHelper: SearchViewHelper? = null
    private var mSearchHelper: SearchHelper? = null

    private var handler: Handler? = Handler()

    private var customWebClient: CustomWebClient? = null
    private var jsInterfaceHelper: JSInterfaceHelper? = null

    internal var isSearch = false
    //记录是否退出当前界面,for:修复退出界面时出现闪影
    internal var isBackPressed = false

    private var loadingPage: LoadingPage? = null

    internal var ziyougb: Boolean = false

    override fun onJsSearch() {
        search_result_content!!.clearView()
        if (loadingPage == null) {
            loadingPage = LoadingPage(this, search_result_main, LoadingPage.setting_result)
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

    @SuppressLint("JavascriptInterface")
    private fun initView() {

        mSearchHelper = SearchHelper(this)

        mSearchViewHelper = SearchViewHelper(this, search_result_hint!!, search_result_input!!, mSearchHelper)

        initListener()

        if (Build.VERSION.SDK_INT >= 11) {
            search_result_content.setLayerType(View.LAYER_TYPE_NONE, null)
        }

        if (search_result_content != null) {
            customWebClient = CustomWebClient(this, search_result_content)
        }

        if (search_result_content != null && customWebClient != null) {
            customWebClient?.setWebSettings()
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

        search_result_back!!.setOnClickListener(this)
        search_result_button!!.setOnClickListener(this)
        search_result_outcome!!.setOnClickListener(this)
        search_result_count!!.setOnClickListener(this)
        search_result_default!!.setOnClickListener(this)
        search_result_keyword!!.setOnClickListener(this)
        search_result_clear!!.setOnClickListener(this)

        if (search_result_input != null) {
            search_result_input!!.setOnClickListener(this)
            search_result_input!!.onFocusChangeListener = this
            search_result_input!!.addTextChangedListener(this)
            search_result_input!!.setOnEditorActionListener(this)
        }

        if (mSearchViewHelper != null) {
            mSearchViewHelper!!.setOnHistoryClickListener(this)
            mSearchViewHelper!!.onHotWordClickListener = object : SearchViewHelper.OnHotWordClickListener {
                override fun hotWordClick(tag: String, searchType: String) {
                    if (mSearchHelper != null) {
                        mSearchHelper!!.setHotWordType(tag, searchType)
                    }
                    loadDataFromNet(isNotAuthor)
                }
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
        if (mSearchViewHelper != null && !TextUtils.isEmpty(mSearchHelper!!.word)) {
            mSearchViewHelper!!.setSearchWord(mSearchHelper!!.word)
        }

    }

    private fun loadDataFromNet(isAuthor: Int) {

        if (mSearchHelper == null) {
            mSearchHelper = SearchHelper(this)
        }

        search_result_count?.text = null

        if (TextUtils.isEmpty(mSearchHelper?.word)) {
            showSearchViews()
        } else {
            hideSearchView()

            if (isAuthor != 1) {
                search_result_input?.setText(mSearchHelper?.word)
                search_result_keyword?.text = mSearchHelper?.word
            } else {
                mSearchHelper?.searchType = "2"
                search_result_input?.setText(Tools.getKeyWord())
                search_result_keyword?.text = Tools.getKeyWord()
            }
            search_result_input?.setTextColor(ContextCompat.getColor(this, R.color.search_title_hint))
            search_result_keyword?.setTextColor(ContextCompat.getColor(this, R.color.search_title_hint))

            mSearchViewHelper?.addHistoryWord(mSearchHelper?.word)

            if (loadingPage == null) {
                loadingPage = LoadingPage(this, search_result_main, LoadingPage.setting_result)
            }

            mSearchHelper?.startLoadData(isAuthor)
        }
    }


    private fun startLoading(handler: Handler?, url: String) {
        if (search_result_content == null) return

        search_result_main.visibility = View.VISIBLE
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
        if (isStayHistory && mSearchViewHelper != null && mSearchViewHelper!!.showStatus) {
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

    override fun onDestroy() {
        if (mSearchHelper != null) {
            mSearchHelper!!.onDestroy()
        }

        search_result_content?.clearCache(true) //清空缓存
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (search_result_main != null) {
                search_result_main?.removeView(search_result_content)
            }
            search_result_content?.stopLoading()
            search_result_content?.removeAllViews()
            search_result_content?.destroy()
        } else {
            search_result_content?.stopLoading()
            search_result_content?.removeAllViews()
            search_result_content?.destroy()
            search_result_main?.removeView(search_result_content)
        }

        loadingPage = null

        mSearchViewHelper?.onDestroy()
        mSearchViewHelper = null

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

            if (mSearchViewHelper != null) {
                mSearchViewHelper!!.hideRecommendListView()
            }

            if (mSearchViewHelper != null) {
                //                String finalContent = AppUtils.deleteAllIllegalChar(mSearchHelper.getWord());
                mSearchViewHelper!!.showHintList()
                //                mSearchViewHelper.showRemainWords(finalContent);
            }

        } else {
            search_result_input!!.text = null
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

        if (mSearchViewHelper != null) {
            mSearchViewHelper!!.hideHintList()
        }
        if (search_result_hint != null) {
            search_result_hint!!.visibility = View.GONE
        }
    }

    private fun backAction() {
        isBackPressed = true
        finish()
    }

    private fun dealSoftKeyboard(view: View?) {
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
                    search_result_input!!.text = null
                if (search_result_clear != null)
                    search_result_clear!!.visibility = View.GONE
                dealSoftKeyboard(search_result_input)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCHRESULT_PAGE, StartLogClickUtil.CLEAR)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BARCLEAR)
            }

            R.id.search_result_outcome,
            R.id.search_result_default,
            R.id.search_result_keyword,
            R.id.search_result_count,
            R.id.search_result_input -> showSearchViews()

            R.id.search_result_button -> {
                var keyword: String? = null

                mSearchViewHelper?.isFocus = false

                if (search_result_input != null) {
                    keyword = search_result_input!!.text.toString()
                }
                if (keyword != null && TextUtils.isEmpty(keyword.trim { it <= ' ' })) {
                    CommonUtil.showToastMessage(R.string.search_click_check_isright)
                } else {
                    hideInputMethod(search_result_input)
                    if (keyword != null && !TextUtils.isEmpty(keyword.trim { it <= ' ' }) && mSearchViewHelper != null) {
                        mSearchViewHelper!!.addHistoryWord(keyword)
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

            if (mSearchViewHelper != null && search_result_input != null) {
                if (mSearchHelper == null) {
                    mSearchHelper = SearchHelper(this)
                }
                if (TextUtils.isEmpty(mSearchHelper!!.word)) {
                    search_result_input!!.text.clear()
                    search_result_input!!.editableText.clear()
                    search_result_input!!.text = null
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
                    mSearchViewHelper!!.showHistoryList()
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


        if (mSearchHelper != null &&
                mSearchHelper?.word != null &&
                mSearchHelper!!.word.trim { it <= ' ' } != s.toString().trim { it <= ' ' }) {
            mSearchHelper?.searchType = "0"
        }

        if (mSearchHelper?.fromClass != null && mSearchHelper?.fromClass != "other") {
            mSearchHelper?.fromClass = "other"
        }

        if (search_result_clear == null) {
            return
        }

        if (!TextUtils.isEmpty(s.toString())) {
            search_result_clear.visibility = if (search_result_input.isFocused) View.VISIBLE else View.GONE
        } else {
            s.clear()
            search_result_clear.visibility = View.GONE
            search_result_main.visibility = View.GONE
        }

        //保存用户搜索词
        Tools.setUserSearchWord(s.toString())

        //网络请求
        if (mSearchViewHelper != null) {
            val finalContent = AppUtils.deleteAllIllegalChar(s.toString())
            mSearchViewHelper!!.showRemainWords(finalContent)
        }
    }

    override fun onHistoryClick(history: String, searchType: String?, isAuthor: Int) {
        if (mSearchHelper == null) {
            mSearchHelper = SearchHelper(this)
        }
        mSearchHelper?.setHotWordType(history, searchType)
        if ("3" != searchType) {
            loadDataFromNet(isAuthor)
        }
    }


    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_GO ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_NEXT ||
                actionId == EditorInfo.IME_ACTION_SEND ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {

            val keyword = search_result_input!!.text.toString()

            if (keyword.trim { it <= ' ' } == "") {
                CommonUtil.showToastMessage(R.string.search_click_check_isright)
            } else {
                mSearchViewHelper?.isFocus = false

                hideInputMethod(v)

                mSearchViewHelper?.addHistoryWord(keyword)
                mSearchHelper?.setHotWordType(keyword, "0")
                loadDataFromNet(isNotAuthor)

                val data = HashMap<String, String>()
                data.put("type", "1")
                data.put("keyword", keyword)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.SEARCHBUTTON, data)

            }
            return true
        }

        return false
    }

    override fun onNoneResultSearch(searchWord: String) {

        search_result_default?.visibility = View.VISIBLE

        search_result_input?.setText(searchWord)

        mSearchViewHelper?.addHistoryWord(searchWord)
        mSearchViewHelper?.hideHintList()

        search_result_content?.clearView()
        if (loadingPage == null) {
            loadingPage = LoadingPage(this, search_result_main, LoadingPage.setting_result)
        }
    }

    companion object {
        //静态变量定义是否在在进入searchBookActivity中初始化显示上次的搜索界面
        var isStayHistory = false

        val isNotAuthor = 0//不是作者
    }
}