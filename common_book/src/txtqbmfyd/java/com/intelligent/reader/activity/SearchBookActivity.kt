package com.intelligent.reader.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
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
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.intelligent.reader.R
import com.intelligent.reader.presenter.search.SearchPresenter
import com.intelligent.reader.presenter.search.SearchView
import com.intelligent.reader.util.SearchViewHelper
import iyouqu.theme.FrameActivity
import kotlinx.android.synthetic.txtqbmfyd.activity_search_book.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.router.RouterConfig
import net.lzbook.kit.utils.*
import java.util.*

@Route(path = RouterConfig.SEARCH_BOOK_ACTIVITY)
class SearchBookActivity : FrameActivity(), OnClickListener, OnFocusChangeListener, SearchViewHelper.OnHistoryClickListener, TextWatcher, OnEditorActionListener, SearchView.AvtView {

//    private var search_result_back: ImageView? = null
//    private var search_result_button: ImageView? = null
//    private var search_result_outcome: RelativeLayout? = null
//    private var search_result_count: TextView? = null
//    private var search_result_keyword: TextView? = null
//    private var search_result_default: RelativeLayout? = null
//    private var search_result_clear: ImageView? = null
//    private var search_result_input: HWEditText? = null
//    private var search_result_main: RelativeLayout? = null
//    private var search_result_content: WebView? = null
//    private var search_result_hint: FrameLayout? = null

    private var searchViewHelper: SearchViewHelper? = null
    private var bookDaoHelper: BookDaoHelper? = null
    private var handler: Handler? = Handler()

    private var customWebClient: CustomWebClient? = null
    private var jsInterfaceHelper: JSInterfaceHelper? = null

    internal var isSearch = false
    //记录是否退出当前界面,for:修复退出界面时出现闪影
    internal var isBackPressed = false

    private var loadingPage: LoadingPage? = null

    private var mSearchPresenter: SearchPresenter? = null

    override fun onJsSearch() {
//        if (search_result_content != null) {
        search_result_content.clearView()
        if (loadingPage == null) {
            loadingPage = LoadingPage(this, search_result_main, LoadingPage.setting_result)
        }
//        }
    }

    override fun onStartLoad(url: String) {
        startLoading(handler, url)
        webViewCallback()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLog.e(TAG, "StartActivity")
        try {
            setContentView(R.layout.activity_search_book)
        } catch (e: InflateException) {
            e.printStackTrace()
            return
        }

        initData()
        initView()
        if (mSearchPresenter != null && !TextUtils.isEmpty(mSearchPresenter!!.word)) {
            loadDataFromNet()
        }
    }

    @SuppressLint("JavascriptInterface")
    private fun initView() {
        search_result_content.topShadow = img_head_shadow
        search_result_outcome.visibility = View.VISIBLE
        search_result_clear.visibility = View.GONE
        if (mSearchPresenter == null) {
            mSearchPresenter = SearchPresenter(this, this)
        }

        if (searchViewHelper == null) {
            searchViewHelper = SearchViewHelper(this, search_result_hint!!, search_result_input!!, mSearchPresenter)
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
            search_result_content!!.setWebViewClient(customWebClient)
        }

        if (search_result_content != null) {
            jsInterfaceHelper = JSInterfaceHelper(this, search_result_content)
        }

        if (jsInterfaceHelper != null && search_result_content != null) {
            search_result_content.addJavascriptInterface(jsInterfaceHelper, "J_search")
            mSearchPresenter!!.initJSHelp(jsInterfaceHelper)
        }

    }


    private fun initListener() {

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
//            search_result_input!!.setOnClickListener(this)
            search_result_input!!.onFocusChangeListener = this
            search_result_input!!.addTextChangedListener(this)
            search_result_input!!.setOnEditorActionListener(this)
        }

        if (searchViewHelper != null) {
            searchViewHelper!!.setOnHistoryClickListener(this)
            searchViewHelper!!.onHotWordClickListener = { tag, searchType ->
                if (mSearchPresenter != null) {
                    mSearchPresenter!!.setHotWordType(tag, searchType)
                }
                loadDataFromNet()
            }

        }
    }

    private fun initData() {
        if (mSearchPresenter == null) {
            mSearchPresenter = SearchPresenter(this, this)
        }
        val intent = intent
        if (intent != null) {
            mSearchPresenter!!.setInitType(intent)
        }
        if (searchViewHelper != null && !TextUtils.isEmpty(mSearchPresenter!!.word)) {
            searchViewHelper!!.setSearchWord(mSearchPresenter!!.word)
        }

        if (bookDaoHelper == null) {
            bookDaoHelper = BookDaoHelper.getInstance()
        }
    }

    private fun loadDataFromNet() {

        if (mSearchPresenter == null) {
            mSearchPresenter = SearchPresenter(this, this)
        }

        if (search_result_count != null) {
            search_result_count!!.text = null
        }

        if (!TextUtils.isEmpty(mSearchPresenter!!.word)) {
            if (search_result_input != null)
                search_result_input!!.setText(mSearchPresenter!!.word)

            if (search_result_keyword != null) {
                search_result_keyword!!.text = mSearchPresenter!!.word
            }

            if (searchViewHelper != null) {
                searchViewHelper!!.addHistoryWord(mSearchPresenter!!.word)
                searchViewHelper!!.setShowHintEnabled(false)
            }

            hideSearchView(false)

            if (loadingPage == null) {
                loadingPage = LoadingPage(this, search_result_main, LoadingPage.setting_result)
            }
            mSearchPresenter!!.startLoadData()

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
                if (mSearchPresenter == null) {
                    mSearchPresenter = SearchPresenter(this@SearchBookActivity, this@SearchBookActivity)
                }
                mSearchPresenter!!.setStartedAction()
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
                if (mSearchPresenter == null) {
                    mSearchPresenter = SearchPresenter(this@SearchBookActivity, this@SearchBookActivity)
                }
                mSearchPresenter!!.onLoadFinished()
                if (loadingPage != null) {
                    if (isSearch) {
                        hideSearchView(false)
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

    }

    /**
     * 重载方法
     */
    override fun onPause() {
        super.onPause()
        if (search_result_input != null) {
            search_result_input!!.clearFocus()
        }

    }

    /**
     * 重载方法
     */
    override fun onStop() {
        super.onStop()
        if (search_result_input != null) {
            search_result_input!!.clearFocus()
        }
    }

    /**
     * 重载方法
     */
    override fun onDestroy() {

        if (mSearchPresenter == null) {
            mSearchPresenter = SearchPresenter(this, this)
        }
        mSearchPresenter!!.onDestroy()
        mSearchPresenter = null

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
//            search_result_content = null
        }

        if (loadingPage != null) {
            loadingPage = null
        }

        if (searchViewHelper != null) {
            searchViewHelper!!.onDestroy()
            searchViewHelper = null
        }

        try {
            setContentView(R.layout.common_empty)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
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

        if (mSearchPresenter == null) {
            mSearchPresenter = SearchPresenter(this, this)
        }
        mSearchPresenter!!.word = search_result_input!!.text.toString()

        if (!TextUtils.isEmpty(mSearchPresenter!!.word)) {

            //            search_result_input.setText(word);
            //            search_result_input.setSelection(word.length());

//            if (search_result_hint != null) {
            search_result_hint.visibility = View.VISIBLE
//            }

            if (searchViewHelper != null) {
                searchViewHelper!!.setShowHintEnabled(true)
            }

            if (searchViewHelper != null) {
                searchViewHelper!!.showHintList(mSearchPresenter!!.word)
            }

        } else {
            search_result_input!!.text = null
            search_result_input!!.editableText.clear()
            search_result_input!!.text.clear()
        }
        search_result_input!!.requestFocus()
        dealSoftKeyboard(search_result_input)
    }

    private fun hideSearchView(isBack: Boolean) {
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
            searchViewHelper!!.setShowHintEnabled(false)
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
                if (search_result_input != null)
                    search_result_input!!.text = null
                if (search_result_clear != null)
                    search_result_clear!!.visibility = View.GONE

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
                if (search_result_input != null) {
                    keyword = search_result_input!!.text.toString()
                }
                if (keyword != null && TextUtils.isEmpty(keyword.trim { it <= ' ' })) {
                    showToastShort(R.string.search_click_check_isright)
                } else {
                    hideInputMethod(search_result_input)
                    if (keyword != null && !TextUtils.isEmpty(keyword.trim { it <= ' ' }) && searchViewHelper != null) {
                        searchViewHelper!!.addHistoryWord(keyword)
                        if (mSearchPresenter == null) {
                            mSearchPresenter = SearchPresenter(this, this)
                        }

                        if (mSearchPresenter!!.fromClass != null && mSearchPresenter!!.fromClass != "other") {
                            if (mSearchPresenter!!.searchType != null) {
                                mSearchPresenter!!.setHotWordType(keyword, mSearchPresenter!!.searchType!!)
                                AppLog.e("type14", mSearchPresenter!!.searchType!! + "===")
                            }
                        } else {

                            if (mSearchPresenter!!.searchType != null && mSearchPresenter!!.searchType != "0") {
                                AppLog.e("type12", mSearchPresenter!!.searchType!! + "===")
                                mSearchPresenter!!.setHotWordType(keyword, mSearchPresenter!!.searchType!!)
                            } else {
                                AppLog.e("type12", 0.toString() + "===")
                                mSearchPresenter!!.setHotWordType(keyword, "0")
                                mSearchPresenter!!.searchType = "0"
                            }
                        }
                        loadDataFromNet()

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

    internal var ziyougb: Boolean = false

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        if (search_result_input == null)
            return
        if (hasFocus) {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BAR)
            dealSoftKeyboard(search_result_input)
            if (searchViewHelper != null && search_result_input != null) {
                searchViewHelper!!.setShowHintEnabled(true)
                if (mSearchPresenter == null) {
                    mSearchPresenter = SearchPresenter(this, this)
                }
                if (TextUtils.isEmpty(mSearchPresenter!!.word)) {
                    search_result_input!!.text.clear()
                    search_result_input!!.editableText.clear()
                    search_result_input!!.text = null
                    searchViewHelper!!.showHitstoryList()
                } else {
                    if (!ziyougb) {
                        search_result_input!!.setText(mSearchPresenter!!.word)
                        search_result_input!!.setSelection(mSearchPresenter!!.word!!.length)
                    }
                    searchViewHelper!!.setShowHintEnabled(true)
                    val finalContent = AppUtils.deleteAllIllegalChar(mSearchPresenter!!.word)
                    searchViewHelper!!.showHintList(finalContent)
                    searchViewHelper!!.notifyListChanged()
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
        if (mSearchPresenter != null && mSearchPresenter!!.word != null) {
            if (mSearchPresenter!!.fromClass != null) {
                if (mSearchPresenter!!.word!!.trim { it <= ' ' } != s.toString().trim { it <= ' ' }) {
                    AppLog.e("typ11", "typ111")
                    if (mSearchPresenter!!.fromClass != "other") {
                        AppLog.e("typ", "typ")
                        mSearchPresenter!!.fromClass = "other"
                    }
                    mSearchPresenter!!.searchType = "0"
                }
            } else {
                if (mSearchPresenter!!.word!!.trim { it <= ' ' } != s.toString().trim { it <= ' ' }) {
                    mSearchPresenter!!.searchType = "0"
                }
            }
        }

        if (searchViewHelper != null) {
            val finalContent = AppUtils.deleteAllIllegalChar(s.toString())
            searchViewHelper!!.showHintList(finalContent)
            searchViewHelper!!.notifyListChanged()
        }

        if (search_result_clear == null) {
            return
        }

        if (!TextUtils.isEmpty(s.toString())) {
            search_result_clear!!.visibility = View.VISIBLE
        } else {
            search_result_clear!!.visibility = View.GONE
            s.clear()
            search_result_main!!.visibility = View.GONE
        }
    }

    override fun OnHistoryClick(history: String?, searchType: String?) {
        if (mSearchPresenter == null) {
            mSearchPresenter = SearchPresenter(this, this)
        }
        mSearchPresenter!!.setHotWordType(history, searchType)
        loadDataFromNet()
    }


    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
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

                hideInputMethod(v)
                if (keyword != null && keyword != "" && searchViewHelper != null) {
                    searchViewHelper!!.addHistoryWord(keyword)
                    mSearchPresenter!!.setHotWordType(keyword, "0")
                    loadDataFromNet()

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

        if (search_result_default.visibility != View.VISIBLE) {
            search_result_default.visibility = View.VISIBLE
        }

        search_result_input.setText(searchWord)
        if (searchViewHelper != null) {
            searchViewHelper?.addHistoryWord(searchWord)
        }

        search_result_content.clearView()
        if (loadingPage == null) {
            loadingPage = LoadingPage(this, search_result_main, LoadingPage.setting_result)
        }
    }

}