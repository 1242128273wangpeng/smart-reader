package com.intelligent.reader.search

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.ding.basic.bean.*
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import com.dingyue.bookshelf.ShelfGridLayoutManager
import com.dingyue.contract.util.CommonUtil
import com.google.gson.Gson
import com.intelligent.reader.R
import com.intelligent.reader.activity.CoverPageActivity
import com.intelligent.reader.adapter.RecommendBooksAdapter
import com.intelligent.reader.adapter.SearchHistoryAdapter
import com.intelligent.reader.adapter.SearchHotWordAdapter
import com.intelligent.reader.adapter.SearchSuggestAdapter
import com.intelligent.reader.view.ScrollForGridView
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.*
import java.lang.ref.WeakReference
import java.util.*

class SearchViewHelper(activity: Activity,
                       rootLayout: ViewGroup,
                       searchEditText: EditText,
                       private val mSearchPresenter: SearchPresenter?) :
        SearchPresenter.SearchSuggestCallBack,
        SearchHistoryAdapter.OnPositionClickListener,
        RecommendBooksAdapter.RecommendItemClickListener {


    private val mSearchHandler = SearchHandler(this)

    //热词和推荐的布局View
    private var mHotWordAndRecommendView: View? = null

    /**
     * 热词
     */
    private var mGridView: ScrollForGridView? = null
    private var mHotWords: MutableList<HotWordBean> = ArrayList()
    private var searchHotWordAdapter: SearchHotWordAdapter? = null

    /**
     * 推荐模块
     */
    private var mRecommendRecycleView: RecyclerView? = null
    private var mRecommendBooksAdapter: RecommendBooksAdapter? = null
    private var recommendBooks: MutableList<SearchRecommendBook.DataBean>? = ArrayList()


    /**
     * 自动补全
     */
    private var mSuggestListView: ListView? = null
    private var mSuggestAdapter: SearchSuggestAdapter? = null
    private var mSuggestList: MutableList<Any>? = ArrayList()

    /**
     * 历史记录
     */
    private var mHistoryListView: ListView? = null
    private var historyAdapter: SearchHistoryAdapter? = null
    private var historyList: ArrayList<String>? = ArrayList()


    var onHotWordClickListener: OnHotWordClickListener? = null
    private var onHistoryClickListener: OnHistoryClickListener? = null

    private var books: List<Book>? = ArrayList()
    private var mActivity: Activity? = null
    private var mRootLayout: ViewGroup? = null
    private var mSearchEditText: EditText? = null
    internal var tv_clear_history_search_view: TextView? = null

    private var sharedPreferencesUtils: SharedPreferencesUtils? = null
    private var gson: Gson? = null

    private var mShouldShowHint = true

    private var loadingPage: LoadingPage? = null

    //从标签和作者的webView页面返回是否保留焦点
    var isFocus = true

    init {
        init(activity, rootLayout, searchEditText)
    }

    private fun init(activity: Activity, rootLayout: ViewGroup, searchEditText: EditText) {


        mActivity = activity
        mRootLayout = rootLayout
        mSearchEditText = searchEditText

        gson = Gson()
        sharedPreferencesUtils = SharedPreferencesUtils(
                PreferenceManager.getDefaultSharedPreferences(activity))

        showSearchHistory()
        initSuggestListView()
        initHotTagView()
        setHotTagList()
        initVisibilityView(isHotAndRecommendView = true)

//        initRecommendView()
    }


    /**
     * 【点击事件】历史记录子条目
     */
    override fun onItemCleanBtnClickListener(position: Int) {
        val message = mSearchHandler.obtainMessage()
        message.arg1 = position
        message.what = 10
        mSearchHandler.handleMessage(message)
    }


    /**
     * 【点击事件】搜索推荐书籍子条目
     */
    override fun onItemClick(view: View?, position: Int) {

        val dataBean = recommendBooks?.get(position)
        if (dataBean != null) {
            val data = HashMap<String, String>()
            data.put("rank", (position + 1).toString() + "")
            data.put("type", "1")
            data.put("bookid", dataBean.bookId)

            StartLogClickUtil.upLoadEventLog(mActivity, StartLogClickUtil.SEARCH_PAGE,
                    StartLogClickUtil.HOTREADCLICK, data)

            val intent = Intent(mActivity, CoverPageActivity::class.java)
            intent.putExtra(Constants.BOOK_ID, dataBean.bookId)
            intent.putExtra(Constants.BOOK_SOURCE_ID, dataBean.id)
            intent.putExtra(Constants.BOOK_CHAPTER_ID, dataBean.bookChapterId)

            mActivity?.startActivity(intent)

        }
        isBackSearch = true
        isFocus = true
    }


    /**
     * 【点击事件】自动补全子条目点击事件
     */
    private fun onSuggestItemClick() {

        mSuggestListView?.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val obj = mSuggestList?.get(position)
            if (obj is SearchCommonBeanYouHua) {
                mSearchCommonBeanYouHua = obj
            } else {
                return@OnItemClickListener
            }
            suggest = mSearchCommonBeanYouHua?.suggest
            searchType = "0"
            isAuthor = 0
            val data = HashMap<String, String>()

            when (mSearchCommonBeanYouHua?.wordtype) {
                "label" -> {
                    searchType = "1"
                    isFocus = false
                    isAuthor = 0
                }
                "author" -> {
                    searchType = "2"
                    isFocus = true
                    isBackSearch = false
                    isAuthor = mSearchCommonBeanYouHua?.isAuthor!!
                    addHistoryWord(suggest)
                }
                "name" -> {
                    searchType = "3"
                    isFocus = true
                    isBackSearch = false
                    isAuthor = 0


                    val searchCommonBeanYouHua = mSuggestList?.get(position) as SearchCommonBeanYouHua
                    data.put("bookid", searchCommonBeanYouHua.book_id)

                    //统计进入到书籍封面页
                    val data1 = HashMap<String, String>()
                    data1.put("BOOKID", searchCommonBeanYouHua.book_id)
                    data1.put("source", "SEARCH")
                    StartLogClickUtil.upLoadEventLog(mActivity, StartLogClickUtil.BOOOKDETAIL_PAGE,
                            StartLogClickUtil.ENTER, data1)

                    val intent = Intent(mActivity, CoverPageActivity::class.java)
                    intent.putExtra(Constants.BOOK_ID, searchCommonBeanYouHua.book_id)
                    intent.putExtra(Constants.BOOK_SOURCE_ID, searchCommonBeanYouHua.book_source_id)
                    mActivity?.startActivity(intent)

                    addHistoryWord(suggest)

                }
                else -> {
                    searchType = "0"
                    isAuthor = 0
                }
            }

            if (!TextUtils.isEmpty(suggest) && mSearchEditText != null) {

                data.put("keyword", suggest!!)
                data.put("type", searchType)
                data.put("enterword", mSearchEditText?.text.toString().trim { it <= ' ' })
                when {
                    position + 1 <= 2 -> data.put("rank", (position + 1).toString() + "")
                    position + 1 in 4..5 -> data.put("rank", position.toString() + "")
                    position + 1 in 7..8 -> data.put("rank", (position - 1).toString() + "")
                    position + 1 > 9 -> data.put("rank", (position - 2).toString() + "")
                }
                StartLogClickUtil.upLoadEventLog(mActivity, StartLogClickUtil.SEARCH_PAGE,
                        StartLogClickUtil.TIPLISTCLICK, data)
            }

            if (mSearchEditText != null && searchType != "3") {
                startSearch(suggest, searchType, isAuthor)
            }

        }

        mSuggestListView?.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                hideInputMethod(view)
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {

            }
        })
    }


    /**
     * 自动补全结果集
     */
    override fun onSearchResult(suggestList: List<SearchCommonBeanYouHua>, transmitBean: SearchAutoCompleteBeanYouHua) {
        if (mSuggestList == null) {
            return
        }
        mSuggestList?.clear()
        for (item in suggestList) {
            mSuggestList?.add(item)
        }
        mSearchHandler.post {
            if (mSuggestAdapter != null) {
                var inputString: String? = ""
                if (mSearchEditText != null) {
                    val editable = mSearchEditText?.text
                    if (editable != null && editable.isNotEmpty()) {
                        inputString = editable.toString()
                    }
                }
                if (inputString != null) {
                    mSuggestAdapter?.setEditInput(inputString)
                }
                mSuggestAdapter?.notifyDataSetChanged()
            }
        }
    }


    interface OnHotWordClickListener {
        fun hotWordClick(tag: String, searchType: String)
    }

    interface OnHistoryClickListener {
        fun onHistoryClick(history: String, searchType: String, isAuthor: Int)
    }

    fun setOnHistoryClickListener(listener: OnHistoryClickListener) {
        onHistoryClickListener = listener
    }

    /**
     * 返回isFocus 和 isBackSearch 的值，以此来确定searchBookActivity页面显示的模块
     */
    fun getShowStatus(): Boolean {
        return !isBackSearch && isFocus
    }

    /**
     * 初始化热词列表布局
     */
    private fun initHotTagView() {

        mHotWordAndRecommendView = View.inflate(mActivity, R.layout.search_hot_title_layout, null)

        mRootLayout?.addView(mHotWordAndRecommendView)

        mGridView = mHotWordAndRecommendView?.findViewById<View>(R.id.grid) as ScrollForGridView

        mGridView?.onItemClickListener = OnItemClickListener { _, _, position, _ ->

            StatServiceUtils.statAppBtnClick(mActivity, StatServiceUtils.b_search_click_allhotword)

            val hotWord = mHotWords[position]
            val data = HashMap<String, String>()
            hotWord.keyword?.let { data.put("topicword", it) }
            data.put("rank", hotWord.sort.toString())
            hotWord.superscript?.let { data.put("type", it) }
            StartLogClickUtil.upLoadEventLog(mActivity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TOPIC, data)

            mSearchEditText?.setText(hotWord.keyword)

            isFocus = false

            onHotWordClickListener?.hotWordClick(hotWord.keyword!!, mHotWords[position].keywordType.toString() + "")
        }

    }

    private fun setHotTagList() {

        mRecommendRecycleView = mHotWordAndRecommendView?.findViewById(R.id.list_recommed)
        mRecommendRecycleView?.recycledViewPool?.setMaxRecycledViews(0, 12)

        val layoutManager = ShelfGridLayoutManager(mActivity, 1)
        mRecommendRecycleView?.layoutManager = layoutManager
        mRecommendRecycleView?.itemAnimator?.addDuration = 0
        mRecommendRecycleView?.itemAnimator?.changeDuration = 0
        mRecommendRecycleView?.itemAnimator?.moveDuration = 0
        mRecommendRecycleView?.itemAnimator?.removeDuration = 0
        (mRecommendRecycleView?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false


        if (NetWorkUtils.getNetWorkTypeNew(mActivity) == "无") {
            initVisibilityView(isHotAndRecommendView = false)
        } else {
            parseGetHotWords()
            parseGetRecommendData()
        }

    }


    fun initVisibilityView(isHotAndRecommendView: Boolean = false,
                           isSuggestListView: Boolean = false,
                           isHistoryListView: Boolean = false) {

        mHotWordAndRecommendView?.visibility = if (isHotAndRecommendView) View.VISIBLE else View.GONE
        mSuggestListView?.visibility = if (isSuggestListView) View.VISIBLE else View.GONE
        mHistoryListView?.visibility = if (isHistoryListView) View.VISIBLE else View.GONE


    }


    /**
     * 搜索历史单独抽离成一个页面
     */
    private fun showSearchHistory() {

        initVisibilityView(isHistoryListView = true)

        //初始化搜索历史的ListView
        initHistoryMain()

        historyList = Tools.getHistoryWord(mActivity)
        historyAdapter = SearchHistoryAdapter(mActivity, historyList as ArrayList<String>)
        historyAdapter?.setPositionClickListener(this)


        if (mHistoryListView != null) {

            mHistoryListView?.adapter = historyAdapter
            mHistoryListView?.onItemClickListener = OnItemClickListener { _, _, _, position ->
                StatServiceUtils.statAppBtnClick(mActivity,
                        StatServiceUtils.b_search_click_his_word)
                if (historyList != null && position > -1 && position < historyList!!.size) {
                    val history = historyList?.get(position.toInt())

                    mSearchEditText?.setText(history)
                    mSearchEditText?.setSelection(history!!.length)
                    isFocus = false
                    startSearch(history, "0", 0)

                    val data = HashMap<String, String>()
                    if (history != null) {
                        data.put("keyword", history)
                    }
                    data.put("rank", position.toString() + "")
                    StartLogClickUtil.upLoadEventLog(mActivity,
                            StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BARLIST, data)

                }
            }
        }

        setHistoryHeadersTitleView()
        mRootLayout?.addView(mHistoryListView)

    }

    /**
     * 【解析】获取热词
     */
    private fun parseGetHotWords() {

        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchOperationV4(

                object : RequestSubscriber<Result<SearchResult>>() {
                    override fun requestResult(result: Result<SearchResult>?) {
                        result?.let {
                            val data = it.data
                            sharedPreferencesUtils?.putString(Constants.SERARCH_HOT_WORD_YOUHUA, gson?.toJson(data, SearchResult::class.java))
                            showHotWordsList(data)
                        }


                        if (result == null || result.data == null) {
                            getCacheDataFromShare(true)
                        }
                        loadingPage?.onSuccess()
                    }

                    override fun requestError(message: String) {
                        loadingPage?.onSuccess()
                    }

                })

    }

    /**
     * 【解析】获取推荐书籍
     */
    private fun parseGetRecommendData() {

        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchRecommend(
                getBookOnLineIds(), object : RequestSubscriber<SearchRecommendBook>() {

            override fun requestResult(result: SearchRecommendBook?) {
                if (result != null && result.data != null) {
                    recommendBooks?.clear()
                    recommendBooks = result.data

                    mRecommendBooksAdapter = RecommendBooksAdapter(mActivity, this@SearchViewHelper, recommendBooks)
                    mRecommendRecycleView?.adapter = mRecommendBooksAdapter
                    mRecommendBooksAdapter?.notifyDataSetChanged()
                }
            }

            override fun requestError(message: String) {
            }
        })
    }

    /**
     * 0 ？ 1 标签 2 作者 3 书籍
     */
    private lateinit var searchType: String
    private var suggest: String? = null
    /**
     * 0 非作者页 1 作者页
     */
    private var isAuthor = 0
    private var mSearchCommonBeanYouHua: SearchCommonBeanYouHua? = null
    private var isBackSearch = false //运营模块返回标识

    /**
     * 获取书架上的书Id
     */

    private fun getBookOnLineIds(): String {
//        books?.clear()
        books = RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).loadBooks()

        val sb = StringBuilder()
        books?.let {
            if (it.isNotEmpty()) {
                for (i in it.indices) {
                    val book = it[i]
                    sb.append(book.book_id)
                    sb.append(if (i == it.size - 1) "" else ",")
                }
                return sb.toString()
            }
        }

        return ""
    }


    /**
     * 从缓存中获取热词
     */
    fun getCacheDataFromShare(hasNet: Boolean) {
        if (!TextUtils.isEmpty(
                sharedPreferencesUtils?.getString(Constants.SERARCH_HOT_WORD_YOUHUA))) {
            val cacheHotWords = sharedPreferencesUtils?.getString(
                    Constants.SERARCH_HOT_WORD_YOUHUA)
            val searchResult = gson?.fromJson(cacheHotWords, SearchResult::class.java)
            if (searchResult != null) {
                showHotWordsList(searchResult)
            } else {
                initVisibilityView(isHotAndRecommendView = false)
            }

        } else {
            if (!hasNet) {
                CommonUtil.showToastMessage("网络不给力哦")
            }
            initVisibilityView(isHotAndRecommendView = false)
        }
    }

    private fun startSearch(searchWord: String?, searchType: String, isAuthor: Int) {

        if (searchWord != null && searchWord != "") {

            addHistoryWord(searchWord)
            onHistoryClickListener?.onHistoryClick(searchWord, searchType, isAuthor)

        }
    }


    /**
     * 展示热词列表
     */
    private fun showHotWordsList(result: SearchResult) {
        mHotWords.clear()
        mHotWords = result.hotWords

        if (searchHotWordAdapter == null) {
            searchHotWordAdapter = SearchHotWordAdapter(mActivity, mHotWords)
            mGridView?.adapter = searchHotWordAdapter
        } else {
            searchHotWordAdapter?.setList(mHotWords)
            searchHotWordAdapter?.notifyDataSetChanged()
        }
    }


    /**
     * 对外提供一个操作mRecommendListView隐藏显示的方法
     */
    fun hideRecommendListView() {
        mHotWordAndRecommendView?.visibility = View.GONE
    }

    fun setShowHintEnabled(showHint: Boolean) {
        mShouldShowHint = showHint
    }


    fun showHintList(searchWord: String?) {
        if (!mShouldShowHint) {
            if (mRootLayout != null)
                mRootLayout!!.visibility = View.GONE
            return
        }

        if (mRootLayout != null)
            mRootLayout!!.visibility = View.VISIBLE

        if (searchWord == null || TextUtils.isEmpty(searchWord)) {
            /*showHistoryList()*/
            initVisibilityView(isHistoryListView = true)
        } else {
            showSuggestList(searchWord)
        }
    }

    fun hideHintList() {
        if (mRootLayout != null)
            mRootLayout!!.visibility = View.GONE
    }

    private fun showSuggestList(searchWord: String) {

        initVisibilityView(isSuggestListView = true)

        // 清空上一个词的联想词结果
        mSuggestList?.clear()

        mSuggestAdapter?.notifyDataSetChanged()

        if (TextUtils.isEmpty(searchWord)) {
            /*showHistoryList()*/
            initVisibilityView(isHistoryListView = true)
        }

        mSearchPresenter?.startSearchSuggestData(searchWord)

    }

    private fun initHistoryMain() {
        mHistoryListView = ListView(mActivity)
        mHistoryListView?.let {
            it.cacheColorHint = ContextCompat.getColor(mActivity!!, R.color.transparent)
            it.divider = ContextCompat.getDrawable(mActivity!!, R.color.color_gray_e8e8e8)
            it.dividerHeight = AppUtils.dip2px(mActivity, 0.5f)
            it.setHeaderDividersEnabled(false)
            it.setSelector(R.drawable.item_selector_white)
        }
    }


    private fun initSuggestListView() {

        mSearchPresenter?.setSearchSuggestCallBack(this)

        mSuggestListView = ListView(mActivity)

        mSuggestListView?.let {
            it.cacheColorHint = ContextCompat.getColor(mActivity!!, R.color.transparent)
            it.divider = ContextCompat.getDrawable(mActivity!!, R.color.color_gray_e8e8e8)
            it.dividerHeight = AppUtils.dip2px(mActivity, 0.5f)
            it.setHeaderDividersEnabled(false)
            it.setSelector(R.drawable.item_selector_white)
            it.visibility = View.GONE
        }

        if (mRootLayout != null) {
            mRootLayout?.addView(mSuggestListView)
        }
        if (mSuggestList != null) {
            mSuggestList?.clear()
        }

        if (mSuggestAdapter == null) {
            var inputString = ""
            if (mSearchEditText != null) {
                val editable = mSearchEditText?.text
                if (editable != null && editable.isNotEmpty()) {
                    inputString = editable.toString()
                }
            }
            mSuggestAdapter = SearchSuggestAdapter(mActivity, mSuggestList, inputString)
        }

        mSuggestListView?.adapter = mSuggestAdapter

        onSuggestItemClick()

    }

    /**
     * 对SearchBookActivity提供控制隐藏显示搜索框下面的内容
     */
    fun showRemainWords(searchWord: String?) {

        //当搜索次为空是显示搜索历史界面
        if (searchWord != null && "" == searchWord || TextUtils.isEmpty(searchWord!!.trim { it <= ' ' })) {
            /*showHistoryList()*/
            initVisibilityView(isHistoryListView = true)
        } else {
            showSuggestList(searchWord)
        }
    }

    /* fun showHistoryList() {
         mHistoryListView?.visibility = View.VISIBLE
         mSuggestListView?.visibility = View.GONE
         mHotWordAndRecommendView?.visibility = View.GONE
     }
 */
    fun notifyListChanged() {
        if (mHistoryAdapter != null)
            mHistoryAdapter!!.notifyDataSetChanged()
    }


    fun setSearchWord(word: String?) {
        mShouldShowHint = false

        mSearchEditText.let {
            it?.setText(word)
            it?.setSelection(it.length())
        }

        mShouldShowHint = true
        addHistoryWord(word)
    }


    fun addHistoryWord(keyword: String?) {

        if (keyword == null || keyword == "") {
            return
        }
        if (historyList?.contains(keyword)!!) {
            historyList?.remove(keyword)
        }

        if (!historyList?.contains(keyword)!!) {
            val size = historyList?.size!!
            if (size >= 30) {
                historyList?.removeAt(size - 1)
            }
            historyList?.add(0, keyword)
            Tools.saveHistoryWord(mActivity, historyList)
        }
        if (historyAdapter != null) {
            historyAdapter?.notifyDataSetChanged()
        }
        setHistoryHeadersTitleView()
    }


    fun hideInputMethod(paramView: View?) {
        if (paramView == null || paramView.context == null)
            return
        val imm = paramView.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(paramView.applicationWindowToken, 0)
        }
    }


    fun setHistoryHeadersTitleView() {
        if (mHistoryHeadersTitle == null) {
            return
        }

        if (mHistoryHeadersTitle == null) {
            return
        }
        if (historyList != null && historyList?.size != 0) {
            mHistoryHeadersTitle?.visibility = View.VISIBLE
        } else {
            mHistoryHeadersTitle?.visibility = View.INVISIBLE
        }

    }

    private fun clearHistory(index: Int) {
        if (historyList != null && index < historyList!!.size) {
            historyList!!.removeAt(index)
        }
        setHistoryHeadersTitleView()
        historyAdapter?.notifyDataSetChanged()
        Tools.saveHistoryWord(mActivity, historyList)
    }

    private fun result(result: List<SearchCommonBeanYouHua>) {
        if (mSuggestList == null) {
            return
        }
        mSuggestList?.clear()
        var index = 0
        for (item in result) {
            if (index > 4)
            // 只显示5个
            {
                break
            }

            mSuggestList?.add(item)
            index++
        }
        var inputString: String? = ""
        if (mSearchEditText != null) {
            val editable = mSearchEditText?.text
            if (editable != null && editable.isNotEmpty()) {
                inputString = editable.toString()
            }
        }
        if (mSuggestAdapter != null) {
            if (inputString != null) {
                mSuggestAdapter?.setEditInput(inputString)
            }
            mSuggestAdapter?.notifyDataSetChanged()
        }
    }

    internal class SearchHandler(helper: SearchViewHelper) : Handler() {
        private val reference: WeakReference<SearchViewHelper> = WeakReference(helper)

        override fun handleMessage(msg: Message) {
            val helper = reference.get() ?: return
            when (msg.what) {
                10 -> helper.clearHistory(msg.arg1)

                20 -> helper.result(msg.obj as ArrayList<SearchCommonBeanYouHua>)

                else -> {
                }
            }
        }
    }

    companion object {

        private var mHistoryHeadersTitle: RelativeLayout? = null

        private var mHistoryAdapter: ArrayAdapter<String>? = null

    }


    fun clear() {
        mSearchEditText?.let {
            it.text.clear()
            it.text.clearSpans()
            it.editableText.clearSpans()
            it.text = null
            it.editableText.clear()
            it.clearFocus()

        }
        mSuggestList?.clear()
        mSuggestList = null
    }

    fun onDestroy() {

        if (mActivity != null) {
            mActivity = null
        }

        mRootLayout?.removeAllViews()
        mRootLayout = null

        onHistoryClickListener = null
        onHotWordClickListener = null

        loadingPage = null

        mHistoryHeadersTitle?.removeAllViews()
        mHistoryHeadersTitle = null

        mHistoryAdapter?.clear()
        mHistoryAdapter = null


        mSuggestAdapter?.clear()
        mSuggestAdapter = null

        clear()
    }
}