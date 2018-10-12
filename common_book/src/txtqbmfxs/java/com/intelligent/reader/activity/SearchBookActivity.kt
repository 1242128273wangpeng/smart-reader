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
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.dingyue.contract.CommonContract
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import com.intelligent.reader.search.SearchPresenter
import com.intelligent.reader.search.SearchView
import com.intelligent.reader.search.SearchViewHelper
import com.orhanobut.logger.Logger
import iyouqu.theme.FrameActivity
import kotlinx.android.synthetic.txtqbmfxs.act_search_book.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.utils.*
import java.util.*

import com.dingyue.contract.web.CustomWebClient
import com.dingyue.contract.web.JSInterfaceObject
import com.google.gson.Gson

/**
 * Function：搜索书籍页
 *
 * Created by JoannChen on 2018/6/14 0013 21:08
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
@Route(path = RouterConfig.SEARCH_BOOK_ACTIVITY)
class SearchBookActivity : FrameActivity(), OnClickListener, OnFocusChangeListener,
        SearchViewHelper.OnHistoryClickListener, TextWatcher, OnEditorActionListener, SearchView.AvtView {

    companion object {
        /**
         * 静态变量定义是否在在进入searchBookActivity中初始化显示上次的搜索界面
         */
        @JvmField
        var isStayHistory = false
    }

    /**
     * 记录是否退出当前界面,for:修复退出界面时出现闪影
     */
    private var isBackPressed = false

    private var handler: Handler? = Handler()

    private var customWebClient: CustomWebClient? = null

    private var isSearch = false


    private var loadingPage: LoadingPage? = null
    private var mSearchPresenter: SearchPresenter? = null
    private var mSearchViewHelper: SearchViewHelper? = null

    private var ziyougb: Boolean = false

    private val isNotAuthor = 0//不是作者

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_search_book)

        initView()
        initListener()

        if (mSearchPresenter != null && !TextUtils.isEmpty(mSearchPresenter?.word)) {
            loadDataFromNet(isNotAuthor)
        }
    }


    override fun onJsSearch() {
        if (search_result_content != null) {
            search_result_content.clearCache(true)
            if (loadingPage == null) {
                loadingPage = LoadingPage(this, search_result_main, LoadingPage.setting_result)
            } else {
                loadingPage?.visibility = View.VISIBLE
            }
        }
    }

    override fun onStartLoad(url: String) {

        if (search_result_content == null) return
        search_result_main?.visibility = View.VISIBLE
        handler?.post { loadingData(url) }

        webViewCallback()
    }


    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    private fun initView() {

        if (mSearchPresenter == null) {
            mSearchPresenter = SearchPresenter(this, this)
        }

        if (mSearchViewHelper == null) {
            mSearchViewHelper = SearchViewHelper(this, search_result_hint, etxt_search_input, mSearchPresenter)
        }

        if (Build.VERSION.SDK_INT >= 14) {
            search_result_content.setLayerType(View.LAYER_TYPE_NONE, null)
        }

        customWebClient = CustomWebClient(this, search_result_content)
        customWebClient?.initWebViewSetting()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            search_result_content.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        search_result_content.webViewClient = customWebClient

        if (intent != null) {
            mSearchPresenter?.setInitType(intent)
        }

        if (!TextUtils.isEmpty(mSearchPresenter?.word)) {
            mSearchViewHelper?.setSearchWord(mSearchPresenter?.word)
        }

        search_result_content?.addJavascriptInterface(object : JSInterfaceObject(this@SearchBookActivity) {

            @JavascriptInterface
            override fun startSearchActivity(data: String?) {
                if (data != null && data.isNotEmpty() && !activity.isFinishing) {
                    if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
                        return
                    }

                    try {
                        val search = Gson().fromJson(data, JSSearch()::class.java)

                        runOnMain {
                            if (search?.word != null && (search.word?.isNotEmpty() == true)) {
                                onNoneResultSearch(search.word!!)
                            }
                        }

                        mSearchPresenter?.setHotWordType(search?.word, search?.type)
                        mSearchPresenter?.startLoadData(0)

                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            }

            @JavascriptInterface
            override fun startTabulationActivity(data: String?) {
                if (data != null && data.isNotEmpty() && !activity.isFinishing) {
                    if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
                        return
                    }

                    try {
                        val redirect = Gson().fromJson(data, JSRedirect::class.java)

                        if (redirect?.url != null && redirect.title != null) {
                            val bundle = Bundle()
                            bundle.putString("url", redirect.url)
                            bundle.putString("title", redirect.title)
                            bundle.putString("from", "other")

                            RouterUtil.navigation(activity, RouterConfig.TABULATION_ACTIVITY, bundle)
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            }

        }, "J_search")
    }


    private fun initListener() {

        img_back.antiShakeClick(this)
        img_search.setOnClickListener(this)

        search_result_outcome.setOnClickListener(this)
        search_result_count.setOnClickListener(this)
        search_result_default.setOnClickListener(this)
        search_result_keyword.setOnClickListener(this)
        img_clear.setOnClickListener(this)

        etxt_search_input.setOnClickListener(this)
        etxt_search_input.onFocusChangeListener = this
        etxt_search_input.addTextChangedListener(this)
        etxt_search_input.setOnEditorActionListener(this)

        mSearchViewHelper?.let {
            it.setOnHistoryClickListener(this)
            it.onHotWordClickListener = object : SearchViewHelper.OnHotWordClickListener {
                override fun hotWordClick(tag: String, searchType: String) {
                    mSearchPresenter?.setHotWordType(tag, searchType)
                    loadDataFromNet(isNotAuthor)
                }

            }
        }

    }

    /**
     * 从网络加载数据
     */
    private fun loadDataFromNet(isAuthor: Int) {

        if (mSearchPresenter == null)
            mSearchPresenter = SearchPresenter(this, this)

        if (search_result_count != null) {
            search_result_count.text = null
        }

        if (!TextUtils.isEmpty(mSearchPresenter?.word)) {

            if (isAuthor != 1) {
                etxt_search_input.setText(mSearchPresenter?.word)
            } else {
                mSearchPresenter?.searchType = "2"
                etxt_search_input.setText(Tools.getKeyWord())
            }


            if (isAuthor != 1) {
                search_result_keyword.text = mSearchPresenter?.word
            } else {
                mSearchPresenter?.searchType = "2"
                search_result_keyword.text = Tools.getKeyWord()
            }


            if (mSearchPresenter != null) {
                mSearchViewHelper?.addHistoryWord(mSearchPresenter?.word)
            }

            hideSearchView()

            if (loadingPage == null) {
                loadingPage = LoadingPage(this, search_result_main, LoadingPage.setting_result)
            } else {
                loadingPage?.visibility = View.VISIBLE
            }

            mSearchPresenter?.startLoadData(isAuthor)

        } else {
            showSearchViews()
        }
    }


    private fun loadingData(url: String) {
        if (customWebClient != null) {
            customWebClient?.initParameter()
        }

        search_result_content.clearView()

        if (loadingPage == null) {
            loadingPage = LoadingPage(this, search_result_main, LoadingPage.setting_result)
        } else {
            loadingPage?.visibility = View.VISIBLE
        }

        Logger.e("LoadingData ==> " + url)
        if (!TextUtils.isEmpty(url) && search_result_content != null) {
            try {
                search_result_content.loadUrl(url)
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

        customWebClient?.let {
            it.setLoadingWebViewStart { url ->
                if (mSearchPresenter == null) {
                    mSearchPresenter = SearchPresenter(this, this)
                }
                mSearchPresenter?.setStartedAction()
            }

            it.setLoadingWebViewError {
                Logger.e("onErrorReceived")
                if (loadingPage != null) {
                    loadingPage?.onErrorVisable()
                }
            }

            it.setLoadingWebViewFinish {
                Logger.e("onLoadFinished")
                if (mSearchPresenter == null) {
                    mSearchPresenter = SearchPresenter(this, this)
                }
                mSearchPresenter?.onLoadFinished()
                if (loadingPage != null) {
                    if (isSearch) {
                        hideSearchView()
                    }
                    loadingPage?.onSuccessGone()
                }
            }
        }

        if (loadingPage != null) {
            loadingPage?.setReloadAction(LoadingPage.reloadCallback {
                if (customWebClient != null) customWebClient?.initParameter()
                search_result_content.reload()
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (isStayHistory && mSearchViewHelper != null && mSearchViewHelper?.getShowStatus() == true) {
            if (mSearchPresenter != null && mSearchPresenter?.fromClass != null && mSearchPresenter?.fromClass != "fromClass") {
                val historyDates = Tools.getKeyWord()

                etxt_search_input.requestFocus()
                etxt_search_input.setText(historyDates)
                //设置光标的索引
                val index = etxt_search_input.text
                etxt_search_input.setSelection(index.length)
                showSearchViews()
            }
        }

        if (search_result_content != null) {
            val keyword = etxt_search_input?.text.toString()
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
    }

    override fun onPause() {
        super.onPause()
        if (etxt_search_input != null) etxt_search_input?.clearFocus()
    }

    override fun onStop() {
        super.onStop()
        if (etxt_search_input != null) etxt_search_input?.clearFocus()
    }

    override fun onDestroy() {

        if (search_result_content != null) {
            search_result_content.clearCache(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (search_result_main != null) {
                    search_result_main.removeView(search_result_content)
                }
                search_result_content.stopLoading()
                search_result_content.removeAllViews()
                search_result_content.destroy()
            } else {
                search_result_content.stopLoading()
                search_result_content.removeAllViews()
                search_result_content.destroy()
                if (search_result_main != null) {
                    search_result_main.removeView(search_result_content)
                }
            }
        }

        if (loadingPage != null) {
            loadingPage = null
        }

        if (mSearchViewHelper != null) {
            mSearchViewHelper?.onDestroy()
            mSearchViewHelper = null
        }

        super.onDestroy()

    }

    override fun onBackPressed() {
        isBackPressed = true
        super.onBackPressed()
    }


    /**
     * 点击搜索按钮
     */
    private fun requestSearchResult() {

        val keyword = etxt_search_input.text.toString()

        if (TextUtils.isEmpty(keyword)) return

        if (TextUtils.isEmpty(keyword.trim { it <= ' ' })) {
            this.applicationContext.showToastMessage(R.string.search_click_check_isright)
        } else {
            hideInputMethod(etxt_search_input)

            if (mSearchViewHelper != null) {
                mSearchViewHelper?.addHistoryWord(keyword)

                if (mSearchPresenter?.fromClass != null && mSearchPresenter?.fromClass != "other") {
                    mSearchPresenter?.setHotWordType(keyword, mSearchPresenter?.searchType)
                } else {
                    mSearchPresenter?.setHotWordType(keyword, "0")
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

    private fun showSearchViews() {

        if (NetWorkUtils.getNetWorkType(this) == NetWorkUtils.NETWORK_NONE) return

        isSearch = true

        search_result_default.visibility = View.VISIBLE
        search_result_outcome.visibility = View.GONE
        search_result_content.visibility = View.GONE


        if (!TextUtils.isEmpty(etxt_search_input.text)) {
            search_result_hint.visibility = View.GONE
            mSearchViewHelper?.hideRecommendListView()
            mSearchViewHelper?.showHintList(mSearchPresenter?.word)
        } else {
            etxt_search_input.text = null
            etxt_search_input.editableText.clear()
            etxt_search_input.text.clear()
        }

        etxt_search_input.requestFocus()
        showSoftKeyboard(etxt_search_input)

    }

    private fun hideSearchView() {

        isSearch = false

        search_result_default.visibility = View.GONE
        search_result_outcome.visibility = View.VISIBLE
        search_result_content.visibility = View.VISIBLE
        search_result_hint.visibility = View.GONE

        etxt_search_input.clearFocus()
        mSearchViewHelper?.hideHintList()
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
            R.id.img_back -> {
                val data = HashMap<String, String>()
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BACK, data)
                isBackPressed = true
                finish()
            }
            R.id.img_clear -> {
                ziyougb = true
                etxt_search_input.text = null
                img_clear.visibility = View.GONE
                showSoftKeyboard(etxt_search_input)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCHRESULT_PAGE, StartLogClickUtil.CLEAR)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BARCLEAR)
            }
            R.id.img_search -> requestSearchResult()

            R.id.search_result_outcome -> showSearchViews()
            R.id.search_result_keyword -> showSearchViews()
            R.id.search_result_count -> showSearchViews()
            R.id.search_result_default -> showSearchViews()
            R.id.etxt_search_input -> showSearchViews()

            else -> {
            }
        }
    }


    override fun onFocusChange(view: View, hasFocus: Boolean) {

        if (etxt_search_input == null) return

        if (hasFocus) {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BAR)
            showSoftKeyboard(etxt_search_input)

            search_result_content.visibility = View.GONE

            if (mSearchViewHelper != null) {

                if (TextUtils.isEmpty(mSearchPresenter?.word)) {
                    etxt_search_input.text.clear()
                    etxt_search_input.editableText.clear()
                    etxt_search_input.text = null
                } else {
                    if (!ziyougb) {
                        etxt_search_input.setText(mSearchPresenter?.word)
                        etxt_search_input.setSelection(mSearchPresenter?.word?.length ?: 0)
                    }
                }
                etxt_search_input.setTextColor(ContextCompat.getColor(this, R.color.search_input_text_color))
                search_result_keyword.setTextColor(ContextCompat.getColor(this, R.color.search_input_text_color))

                //判断当用户没有对editText进行操作时（即编辑框没有内容时），显示搜索历史
                if (etxt_search_input.text.toString() == "") {
                    /*mSearchViewHelper?.showHistoryList()*/
                    mSearchViewHelper?.initVisibilityView(isHistoryListView = true)
                }
            }
            ziyougb = true
        } else {
            ziyougb = false
            hideInputMethod(etxt_search_input)
        }
    }


    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }


    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }


    override fun afterTextChanged(editable: Editable) {

        if (mSearchPresenter != null &&
                mSearchPresenter?.word != null &&
                mSearchPresenter?.word?.trim { it <= ' ' } != editable.toString().trim { it <= ' ' }) {

            mSearchPresenter?.searchType = "0"

            if (mSearchPresenter?.fromClass != null && mSearchPresenter?.fromClass != "other") {
                mSearchPresenter?.fromClass = "other"
            }
        }

        if (img_clear == null) {
            return
        }

        if (!TextUtils.isEmpty(editable.toString())) {
            img_clear.visibility = if (etxt_search_input.isFocused) View.VISIBLE else View.GONE
        } else {
            editable.clear()
            img_clear.visibility = View.GONE
            search_result_main?.visibility = View.GONE
        }

        //保存用户搜索词
        Tools.setUserSearchWord(editable.toString())

        //网络请求
        if (mSearchViewHelper != null) {
            mSearchViewHelper?.showRemainWords(AppUtils.deleteAllIllegalChar(editable.toString()))
        }
    }

    override fun onHistoryClick(history: String, searchType: String, isAuthor: Int) {
        mSearchPresenter?.setHotWordType(history, searchType)
        if (searchType != "3") {
            loadDataFromNet(isNotAuthor)
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_GO ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_NEXT ||
                actionId == EditorInfo.IME_ACTION_SEND ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {

            val keyword = etxt_search_input?.text.toString()

            if (keyword.trim { it <= ' ' } == "") {
                this.showToastMessage(R.string.search_click_check_isright)
            } else {
                mSearchViewHelper?.isFocus = false

                hideInputMethod(v)

                mSearchViewHelper?.addHistoryWord(keyword)
                mSearchPresenter?.setHotWordType(keyword, "0")
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

        if (search_result_default != null && search_result_default?.visibility != View.VISIBLE) {
            search_result_default?.visibility = View.VISIBLE
        }

        if (etxt_search_input != null) {
            etxt_search_input?.setText(searchWord)
        }

        if (mSearchViewHelper != null) {
            mSearchViewHelper?.addHistoryWord(searchWord)
            mSearchViewHelper?.hideHintList()
        }

        if (search_result_content != null) {
            search_result_content?.clearView()
            if (loadingPage == null) {
                loadingPage = LoadingPage(this, search_result_main, LoadingPage.setting_result)
            } else {
                loadingPage?.visibility = View.VISIBLE
            }
        }
    }


    /**
     * 弹出软键盘
     */
    private fun showSoftKeyboard(view: View?) {

        if (handler == null) handler = Handler()

        handler?.postDelayed({

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (view != null) {
                imm.showSoftInput(view, 0)
            }
        }, 500)

    }

}