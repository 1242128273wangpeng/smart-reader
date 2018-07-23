package com.intelligent.reader.util

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.res.Resources
import android.text.TextUtils
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua
import com.ding.basic.bean.SearchCommonBeanYouHua
import com.ding.basic.bean.SearchHotBean
import com.intelligent.reader.R
import com.intelligent.reader.adapter.SearchHotWordAdapter
import com.intelligent.reader.adapter.SearchSuggestAdapter
import com.intelligent.reader.search.SearchView
import com.intelligent.reader.search.SearchHelpYouHuaPresenter
import com.intelligent.reader.search.SearchPresenter
import com.intelligent.reader.view.ScrollForGridView
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.data.search.SearchCommonBean
import net.lzbook.kit.utils.StatServiceUtils

class SearchViewHelper(activity: Activity, rootLayout: ViewGroup, searchEditText: EditText, private val mSearchPresenter: SearchPresenter?) : SearchPresenter.SearchSuggestCallBack, SearchView.HelpView {


    private var mContext: Context? = null
    private var activity: Activity? = null
    private var mRootLayout: ViewGroup? = null
    private var mSearchEditText: EditText? = null
    private var mHistoryListView: ListView? = null
    private var mHistoryDataListView: ListView? = null
    private lateinit var mHistoryDeleteView: View
    private var mSuggestListView: ListView? = null
    private var mGridView: ScrollForGridView? = null
    private var linear_parent: LinearLayout? = null
    internal var tv_clear_history_search_view: TextView? = null
    private var mSuggestAdapter: SearchSuggestAdapter? = null

    private var mResources: Resources? = null

    private var mShouldShowHint = true

    var onHotWordClickListener: ((tag: String?, searchType: String?) -> Unit)? = null
    private var mOnHistoryClickListener: OnHistoryClickListener? = null
    var context: Context? = null
    private var searchHotWordAdapter: SearchHotWordAdapter? = null
    private var mSearchHelpPresenter: SearchHelpYouHuaPresenter? = null
    private var loadingPage: LoadingPage? = null

    init {
        init(activity, activity, rootLayout, searchEditText)
    }

    private fun init(context: Context, activity: Activity, rootLayout: ViewGroup, searchEditText: EditText) {
        mContext = context
        this.activity = activity
        mRootLayout = rootLayout
        mSearchEditText = searchEditText
        if (mContext != null)
            mResources = mContext!!.resources

        mSearchHelpPresenter = SearchHelpYouHuaPresenter(this)

        initHistoryView()
        initSuggestListView()
    }

    /**
     * 返回isFocus 和 isBackSearch 的值，以此来确定searchBookActivity页面显示的模块
     *
     * @return
     */
    fun getShowStatus(): Boolean {
        return if (mSearchHelpPresenter != null && !mSearchHelpPresenter!!.isBackSearch && mSearchHelpPresenter!!.isFocus) {
            true
        } else {
            false
        }
    }

    fun setShowHintEnabled(showHint: Boolean) {
        mShouldShowHint = showHint
    }

    override fun showLoading() {
        if (loadingPage == null && this.activity != null) {
            loadingPage = LoadingPage(this.activity, mRootLayout, LoadingPage.setting_result)
        }
    }

    override fun dimissLoading() {
        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }

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
            showHistoryList()
        } else {
            showSuggestList(searchWord)
        }
    }

    fun showHitstoryList() {
        mHistoryDataListView!!.visibility = View.VISIBLE
        mHistoryListView!!.visibility = View.GONE
        mSuggestListView!!.visibility = View.GONE
    }

    fun hideHintList() {
        if (mRootLayout != null)
            mRootLayout!!.visibility = View.GONE
    }

    private fun showSuggestList(searchWord: String) {
        if (mSuggestListView != null) {
            mSuggestListView!!.visibility = View.VISIBLE
        }
        if (mHistoryListView != null)
            mHistoryListView!!.visibility = View.GONE

        if (mHistoryDataListView != null)
            mHistoryDataListView!!.visibility = View.GONE

        // 清空上一个词的联想词结果
        if (mSearchHelpPresenter!!.getSuggestData() != null) {
            mSearchHelpPresenter!!.getSuggestData()!!.clear()
        }
        if (mSuggestAdapter != null) {
            mSuggestAdapter!!.notifyDataSetChanged()
        }

        if (TextUtils.isEmpty(searchWord)) {
            showHistoryList()
        }

        mSearchPresenter?.startSearchSuggestData(searchWord)

    }

    private fun initHistoryMain(context: Context) {
        mHistoryListView = ListView(context)
        if (mHistoryListView != null && mResources != null) {
            mHistoryListView!!.cacheColorHint = mResources!!.getColor(R.color.transparent)
            val typeColor = R.color.color_gray_e8e8e8
//            mHistoryListView!!.divider = mResources!!.getDrawable(typeColor)typeColor
//            mHistoryListView!!.dividerHeight = AppUtils.dip2px(mContext, 0.5f)
            mHistoryListView!!.divider = null
            mHistoryListView!!.dividerHeight = 0
            mHistoryListView!!.setHeaderDividersEnabled(false)
            mHistoryListView!!.setSelector(R.drawable.item_selector_white)
        }

        mHistoryDataListView = ListView(context)
        mHistoryDataListView!!.cacheColorHint = mResources!!.getColor(R.color.transparent)
        mHistoryDataListView!!.divider = null
        mHistoryDataListView!!.dividerHeight = 0
        mHistoryDataListView!!.setHeaderDividersEnabled(false)
        mHistoryDataListView!!.setSelector(R.drawable.item_selector_white)
        if (mRootLayout != null) {
            mRootLayout!!.addView(mHistoryDataListView)
            mHistoryDataListView!!.visibility = View.GONE
        }

        mHistoryDeleteView = View.inflate(context, R.layout.search_history_delete_layout, null)
        mHistoryDeleteView.setOnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_his_clear)
            StartLogClickUtil.upLoadEventLog(context,
                    StartLogClickUtil.SEARCH, StartLogClickUtil.BARCLEAR)
            mSearchHelpPresenter?.showDialog(activity)
        }
        mHistoryDataListView!!.addFooterView(mHistoryDeleteView)

    }

    private fun initHistoryView() {

        initHistoryMain(mContext!!)

        setHistoryHeaderHotWord()
        initHistoryHeadersTitleView()

        mSearchHelpPresenter?.initHistoryData(mContext)
        mHistoryAdapter = ArrayAdapter(activity!!, R.layout.item_history_search_view,
                mSearchHelpPresenter?.getHistoryData()!!)

        if (mSearchHelpPresenter?.getHistoryData() != null && mSearchHelpPresenter?.getHistoryData()!!.size == 0) {
            mHistoryDeleteView.visibility = View.GONE
        } else {
            mHistoryDeleteView.visibility = View.VISIBLE
        }

//        if (mHistoryListView != null) {
//            mHistoryListView!!.adapter = mHistoryAdapter
//            mHistoryListView!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, position ->
//                mSearchHelpPresenter?.onHistoryItemClick(mContext, arg0, arg1, arg2, position)
//            }
//        }

        if (mHistoryListView != null) {
            mHistoryListView!!.adapter = null
        }

        if (mHistoryDataListView != null) {
            mHistoryDataListView!!.adapter = mHistoryAdapter
            mHistoryDataListView!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, position ->
                mSearchHelpPresenter?.onHistoryItemClick(mContext, arg0, arg1, arg2, position)
            }
        }

        setHistoryHeadersTitleView()
        if (mRootLayout != null) {
            mRootLayout!!.addView(mHistoryListView)
        }
    }

    private fun setHistoryHeaderHotWord() {
        val hotword_view = initHotWordView()
        if (mHistoryListView != null)
            mHistoryListView!!.addHeaderView(hotword_view)
        resetHotWordList()
    }

    private fun initHotWordView(): View? {
        var listHeader: View? = null
        try {

            listHeader = LayoutInflater.from(activity).inflate(R.layout
                    .layout_hotword_search_view, null)
        } catch (e: InflateException) {
            e.printStackTrace()
        }

        if (listHeader != null) {
            mGridView = listHeader.findViewById(R.id.grid)
            linear_parent = listHeader.findViewById(R.id.linear_parent)
            mGridView!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
                mSearchHelpPresenter?.onHotItemClick(mContext, parent, view, position, id)
            }

        }
        return listHeader
    }

    private fun initSuggestListView() {
        mSearchPresenter?.setSearchSuggestCallBack(this)

        mSuggestListView = ListView(activity)
        if (mSuggestListView == null)
            return
        mSuggestListView!!.cacheColorHint = mResources!!.getColor(R.color.transparent)
        val typeColor = R.color.color_gray_e8e8e8
//        mSuggestListView!!.divider = mResources!!.getDrawable(typeColor)
//        mSuggestListView!!.dividerHeight = AppUtils.dip2px(mContext, 0.5f)
        mSuggestListView!!.dividerHeight = 0
        mSuggestListView!!.divider = null
        mSuggestListView!!.setSelector(R.drawable.item_selector_white)
        mSuggestListView!!.visibility = View.GONE
        if (mRootLayout != null) {
            mRootLayout!!.addView(mSuggestListView)
        }
        if (mSearchHelpPresenter!!.getSuggestData() != null) {
            mSearchHelpPresenter!!.getSuggestData()!!.clear()
        }
        if (mSuggestAdapter == null) {
            var inputString = ""
            if (mSearchEditText != null) {
                val editable = mSearchEditText!!.text
                if (editable != null && editable.length > 0) {
                    inputString = editable.toString()
                }
            }
            mSuggestAdapter = SearchSuggestAdapter(activity, mSearchHelpPresenter!!.getSuggestData(), inputString)
        }
        mSuggestListView!!.adapter = mSuggestAdapter
        mSuggestListView!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->

            if (mSearchEditText != null) {
                mSearchHelpPresenter?.onSuggestItemClick(mContext, mSearchEditText!!.text.toString().trim { it <= ' ' }, arg2)
                mShouldShowHint = false
                mSearchEditText!!.setText(mSearchHelpPresenter?.getCurrSuggestData())
                mSearchEditText!!.setSelection(mSearchHelpPresenter?.getCurrSuggestData()?.length!!)
            }

            if (mSearchPresenter != null) {
                mSearchPresenter.searchType = mSearchHelpPresenter?.getSearchType()
                mSearchPresenter.word = mSearchHelpPresenter?.getCurrSuggestData()
            }

        }
        mSuggestListView!!.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                hideInputMethod(view)
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {

            }
        })
    }


    private fun showHistoryList() {
        if (mHistoryListView != null)
            mHistoryListView!!.visibility = View.VISIBLE
        if (mSuggestListView != null)
            mSuggestListView!!.visibility = View.GONE
        if (mHistoryDataListView != null)
            mHistoryDataListView!!.visibility = View.GONE
    }

    fun notifyListChanged() {
        if (mHistoryAdapter != null)
            mHistoryAdapter!!.notifyDataSetChanged()
    }

    private fun initHistoryHeadersTitleView() {
        if (activity == null)
            return
        try {
            mHistoryHeadersTitle = activity!!.layoutInflater.inflate(R.layout
                    .header_view_history_search_view, null) as RelativeLayout
        } catch (e: InflateException) {
            e.printStackTrace()
        }

        tv_clear_history_search_view = mHistoryHeadersTitle?.findViewById(R.id
                .tv_clear_history_search_view)

//        if (mHistoryListView != null) {
//            mHistoryListView!!.addHeaderView(mHistoryHeadersTitle)
//        }

        if (tv_clear_history_search_view == null)
            return
        tv_clear_history_search_view!!.setOnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_his_clear)

            mSearchHelpPresenter?.showDialog(activity)
        }
    }

    fun setSearchWord(word: String?) {
        mShouldShowHint = false
        if (mSearchEditText != null) {
            mSearchEditText!!.setText(word)
            mSearchEditText!!.setSelection(mSearchEditText!!.length());
        }
        mShouldShowHint = true
        addHistoryWord(word)
    }

    fun addHistoryWord(keyword: String?) {
        mSearchHelpPresenter?.addHistoryWord(keyword)
    }

    private fun resetHotWordList() {
        if (activity == null)
            return

        mSearchHelpPresenter?.resetHotWordList(mContext!!)
    }

    override fun onSearchResult(suggestList: List<Any>, transmitBean: SearchAutoCompleteBeanYouHua) {
        mSearchHelpPresenter?.onSearchResult(suggestList, transmitBean)
    }

    fun setOnHistoryClickListener(listener: OnHistoryClickListener) {
        mOnHistoryClickListener = listener
    }


    interface OnHistoryClickListener {
        fun OnHistoryClick(history: String?, searchType: String?,isAuthor: Int)
    }

    fun clear() {
        if (mSearchEditText != null) {
            mSearchEditText!!.text.clear()
            mSearchEditText!!.text.clearSpans()
            mSearchEditText!!.editableText.clearSpans()
            mSearchEditText!!.text = null
            mSearchEditText!!.editableText.clear()
            mSearchEditText!!.clearFocus()
        }

        mSearchHelpPresenter?.clear()

    }

    fun onDestroy() {
        if (mContext != null) {
            mContext = null
        }

        if (activity != null) {
            activity = null
        }

        if (mRootLayout != null) {
            mRootLayout!!.removeAllViews()
            mRootLayout = null
        }

        if (mOnHistoryClickListener != null) {
            mOnHistoryClickListener = null
        }

        if (onHotWordClickListener != null) {
            onHotWordClickListener = null
        }

        if (mHistoryHeadersTitle != null) {
            mHistoryHeadersTitle!!.removeAllViews()
            mHistoryHeadersTitle = null
        }

        if (mHistoryAdapter != null) {
            mHistoryAdapter!!.clear()
            mHistoryAdapter = null
        }

        if (mSuggestAdapter != null) {
            mSuggestAdapter!!.clear()
            mSuggestAdapter = null
        }

        mSearchHelpPresenter?.onDestroy()

        clear()
    }

    fun hideInputMethod(paramView: View?) {
        if (paramView == null || paramView.context == null)
            return
        val imm = paramView.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(paramView.applicationWindowToken, 0)
        }
    }

    override fun onHistoryClear() {
        mHistoryDeleteView.visibility = View.GONE
    }


    override fun notifyHisData() {
        if (SearchViewHelper.mHistoryAdapter != null) {
            SearchViewHelper.mHistoryAdapter!!.notifyDataSetChanged()
        }
        showHistoryList()
    }

    override fun setHistoryHeadersTitleView() {
        if (mHistoryHeadersTitle == null) {
            return
        }
        if (mSearchHelpPresenter!!.getHistoryData() != null && mSearchHelpPresenter!!.getHistoryData()!!.size != 0) {
            mHistoryHeadersTitle!!.visibility = View.VISIBLE
        } else {
            mHistoryHeadersTitle!!.visibility = View.INVISIBLE
        }
    }

    override fun onStartSearch(searchWord: String?, searchType: String?,isAuthor: Int) {
        if (mOnHistoryClickListener != null) {
            mOnHistoryClickListener!!.OnHistoryClick(searchWord, searchType,isAuthor)
        }
    }

    override fun hotItemClick(hotword: String, searchType: String) {
        if (mSearchEditText != null) {
            mSearchEditText!!.setText(hotword)
        }
        mShouldShowHint = false
        if (onHotWordClickListener != null) {
            onHotWordClickListener!!.invoke(hotword, searchType)
        }
    }

    override fun setHotWordAdapter(hotWords: MutableList<SearchHotBean.DataBean>?) {
        if (searchHotWordAdapter == null) {
            searchHotWordAdapter = SearchHotWordAdapter(activity, hotWords)
            mGridView!!.adapter = searchHotWordAdapter
        } else {
            searchHotWordAdapter!!.setDatas(hotWords)
            searchHotWordAdapter!!.notifyDataSetChanged()
        }
    }

    override fun showLinearParent(show: Boolean) {
        if (show) {
            linear_parent!!.visibility = View.VISIBLE
        } else {
            linear_parent!!.visibility = View.GONE
        }
    }

    override fun onSuggestBack() {
        var inputString: String? = ""
        if (mSearchEditText != null) {
            val editable = mSearchEditText!!.text
            if (editable != null && editable.length > 0) {
                inputString = editable.toString()
            }
        }
        if (mSuggestAdapter != null) {
            if (inputString != null) {
                mSuggestAdapter!!.setEditInput(inputString)
            }
            mSuggestAdapter!!.notifyDataSetChanged()
        }
    }

    override fun setEditText(text: String?) {
        if (mSearchEditText != null) {
            mShouldShowHint = false
            mSearchEditText!!.setText(text)
        }
    }

    companion object {
        private val TAG = SearchViewHelper::class.java.simpleName

        private var mHistoryHeadersTitle: RelativeLayout? = null

        private var mHistoryAdapter: ArrayAdapter<String>? = null

    }
}
