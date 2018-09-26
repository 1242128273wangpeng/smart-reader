package com.intelligent.reader.util

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.res.Resources
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua
import com.ding.basic.bean.SearchHotBean
import com.intelligent.reader.R
import com.intelligent.reader.adapter.SearchHistoryAdapter
import com.intelligent.reader.adapter.SearchHotWordAdapter
import com.intelligent.reader.adapter.SearchSuggestAdapter
import com.intelligent.reader.presenter.search.SearchView
import com.intelligent.reader.view.ScrollForGridView
import net.lzbook.kit.appender_loghub.StartLogClickUtil

import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.Tools
import net.lzbook.kit.ui.widget.LoadingPage

class SearchViewHelper(activity: Activity, rootLayout: ViewGroup, searchEditText: EditText, private val mSearchPresenter: SearchPresenter?) : SearchPresenter.SearchSuggestCallBack, SearchView.HelpView,SearchHistoryAdapter.onPositionClickListener {



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
    private var mHistoryAdapter: SearchHistoryAdapter? = null
    private var mHistoryHeadersTitle: RelativeLayout? = null
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

        mHistoryDataListView = ListView(context)
//        mHistoryDataListView!!.cacheColorHint = mResources!!.getColor(R.color.search_history_divider)
        val typeColor = R.color.search_history_divider
        mHistoryDataListView!!.divider = mResources!!.getDrawable(typeColor)
        mHistoryDataListView!!.dividerHeight = AppUtils.dip2px(mContext, 0.5f)
        mHistoryDataListView!!.setHeaderDividersEnabled(false)
        mHistoryDataListView!!.setSelector(R.drawable.item_selector_white)
        if (mRootLayout != null) {
            mRootLayout!!.addView(mHistoryDataListView)
            mHistoryDataListView!!.visibility = View.GONE
        }

        mHistoryDeleteView = View.inflate(context, R.layout.search_history_delete_layout, null)

        mHistoryHeadersTitle = mHistoryDeleteView.findViewById<RelativeLayout>(R.id.rl_contain) as RelativeLayout
        mHistoryDeleteView.findViewById<TextView>(R.id.txt_delete_his).setOnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_his_clear)
            StartLogClickUtil.upLoadEventLog(context,
                    StartLogClickUtil.SEARCH, StartLogClickUtil.BARCLEAR)
            mSearchHelpPresenter?.showDialog(activity)
        }
        mHistoryDataListView!!.addHeaderView(mHistoryDeleteView)

    }

    private fun initHistoryView() {

        initHistoryMain(mContext!!)


        mSearchHelpPresenter?.initHistoryData(mContext)
        mHistoryAdapter = SearchHistoryAdapter(activity!!, mSearchHelpPresenter?.getHistoryData()!!)
        mHistoryAdapter?.setPositionClickListener(this)
        if (mSearchHelpPresenter?.getHistoryData() != null && mSearchHelpPresenter?.getHistoryData()!!.size == 0) {
            mHistoryDeleteView.visibility = View.GONE
        } else {
            mHistoryDeleteView.visibility = View.VISIBLE
        }


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


    fun setSearchWord(word: String?) {
        mShouldShowHint = false
        if (mSearchEditText != null) {
            mSearchEditText!!.setText(word)
            //            mSearchEditText.setSelection(mSearchEditText.length());
        }
        mShouldShowHint = true
        addHistoryWord(word)
    }

    fun addHistoryWord(keyword: String?) {
        mSearchHelpPresenter?.addHistoryWord(keyword)
    }


    override fun onSearchResult(suggestList: List<Any>, transmitBean: SearchAutoCompleteBeanYouHua) {
        mSearchHelpPresenter?.onSearchResult(suggestList, transmitBean)
    }

    fun setOnHistoryClickListener(listener: OnHistoryClickListener) {
        mOnHistoryClickListener = listener
    }


    interface OnHistoryClickListener {
        fun OnHistoryClick(history: String?, searchType: String?)
    }

    override fun onItemClickListener(index: Int) {
        if (mSearchHelpPresenter != null && index < mSearchHelpPresenter?.getHistoryData()!!.size){
            mSearchHelpPresenter?.getHistoryData()!!.removeAt(index)
        }
        if (mHistoryAdapter != null)
            mHistoryAdapter!!.notifyDataSetChanged()
        setHistoryHeadersTitleView()
        Tools.saveHistoryWord(mContext, mSearchHelpPresenter?.getHistoryData())
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
        if (mHistoryAdapter != null) {
            mHistoryAdapter!!.notifyDataSetChanged()
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
            mHistoryHeadersTitle!!.visibility = View.GONE
        }
    }

    override fun onStartSearch(searchWord: String?, searchType: String?) {
        if (mOnHistoryClickListener != null) {
            mOnHistoryClickListener!!.OnHistoryClick(searchWord, searchType)
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


}
