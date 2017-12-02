package com.intelligent.reader.util

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.text.TextUtils
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import android.widget.AdapterView.OnItemClickListener
import android.widget.EditText
import android.widget.ListView
import android.widget.RelativeLayout

import com.intelligent.reader.R
import com.intelligent.reader.adapter.SearchHisAdapter
import com.intelligent.reader.adapter.SearchHotWordAdapter
import com.intelligent.reader.adapter.SearchSuggestAdapter

import net.lzbook.kit.data.search.SearchAutoCompleteBean
import net.lzbook.kit.data.search.SearchCommonBean
import net.lzbook.kit.data.search.SearchHotBean
import net.lzbook.kit.utils.StatServiceUtils

import android.content.Context.INPUT_METHOD_SERVICE
import com.intelligent.reader.presenter.search.SearchPresenter
import com.intelligent.reader.presenter.search.SearchView
import com.intelligent.reader.presenter.search.SearchViewPresenter

class SearchViewHelper(activity: Activity, rootLayout: ViewGroup, searchEditText: EditText, private val mSearchPresenter: SearchPresenter?) : SearchPresenter.SearchSuggestCallBack, SearchHisAdapter.SearchClearCallBack, SearchView.View {

    private var mContext: Context? = null
    private var activity: Activity? = null
    private var mRootLayout: ViewGroup? = null
    private var mSearchEditText: EditText? = null
    private var mHotListView: ListView? = null
    private var mHisListView: ListView? = null
    private var mSuggestListView: ListView? = null
    private var mHotAdapter: SearchHotWordAdapter? = null
    private var mSuggestAdapter: SearchSuggestAdapter? = null
    private var mHisAdapter: SearchHisAdapter? = null
    private var mResources: Resources? = null
    private var mShouldShowHint = true
    private var mOnHistoryClickListener: OnHistoryClickListener? = null
    var isFromEditClick = false
    private var relative_parent: RelativeLayout? = null
    private var mSearchViewPresenter: SearchViewPresenter? = null

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

        mSearchViewPresenter = SearchViewPresenter(this)

        initHotListView()
        initHisListView()
        initSuggestListView()
        showHotList()
    }


    fun setShowHintEnabled(showHint: Boolean) {
        mShouldShowHint = showHint
    }

    fun showHintList(searchWord: String?) {
        if (!mShouldShowHint) {
            hideHintList()
            return
        }

        if (mRootLayout != null)
            mRootLayout!!.visibility = View.VISIBLE

        //保证开始输入且续输入空格时显示搜索历史
        if (searchWord == null || TextUtils.isEmpty(searchWord.trim { it <= ' ' }) || isFromEditClick) {
            showHisList()
            isFromEditClick = false
        } else {

            showSuggestList(searchWord)
        }
    }

    fun hideHintList() {
        if (mRootLayout != null)
            mRootLayout!!.visibility = View.GONE
    }

    private fun showSuggestList(searchWord: String) {
        if (mHotListView != null)
            mHotListView!!.visibility = View.GONE
        if (mHisListView != null)
            mHisListView!!.visibility = View.GONE
        if (mSuggestListView != null)
            mSuggestListView!!.visibility = View.VISIBLE

        // 清空上一个词的联想词结果
        if (mSearchViewPresenter!!.getSuggestData() != null) {
            mSearchViewPresenter!!.getSuggestData()!!.clear()
        }
        if (mSuggestAdapter != null) {
            mSuggestAdapter!!.notifyDataSetChanged()
        }

        if (mSearchPresenter != null) {
            mSearchPresenter!!.startSearchSuggestData(searchWord)
        }

    }

    private fun showHisList() {
        if (mSuggestListView != null)
            mSuggestListView!!.visibility = View.GONE
        if (mHotListView != null)
            mHotListView!!.visibility = View.GONE
        if (mHisListView != null)
            mHisListView!!.visibility = View.VISIBLE


        mSearchViewPresenter?.initHistoryData(mContext)

        if (mHisAdapter != null) {
            mHisAdapter!!.notifyDataSetChanged()
        }
    }

    private fun initHotListView() {
        mHotListView = ListView(activity)
        if (mHotListView != null && mResources != null) {
            mHotListView!!.cacheColorHint = mResources!!.getColor(R.color.transparent)
            mHotListView!!.divider = mResources!!.getDrawable(R.color.transparent)
            mHotListView!!.selector = mResources!!.getDrawable(R.color.transparent)
        }

        setHotHeader()

        mSearchViewPresenter?.resetHotWordList(activity!!)

        if (mRootLayout != null) {
            mRootLayout!!.addView(mHotListView)
        }
    }

    private fun initHisListView() {
        mHisListView = ListView(activity)
        if (mHisListView != null && mResources != null) {
            mHisListView!!.cacheColorHint = mResources!!.getColor(R.color.transparent)
            mHisListView!!.divider = mResources!!.getDrawable(R.color.transparent)
            mHisListView!!.setSelector(R.drawable.item_selector_white)
        }

        mSearchViewPresenter?.setHistoryData(mContext)
        mHisAdapter = SearchHisAdapter(activity, mSearchViewPresenter?.getHistoryData())
        mHisAdapter!!.setSearchClearCallBack(this)
        if (mHisListView != null) {

            mHisListView!!.adapter = mHisAdapter
            mHisListView!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, position ->

                mSearchViewPresenter?.onHistoryItemClick(activity, arg0, arg1, arg2, position)

            }
        }

        if (mRootLayout != null) {
            mRootLayout!!.addView(mHisListView)
        }
    }

    override fun onSearchClear(content: String) {
        StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.b_search_click_his_clear)
        clearHistoryItem(content)
    }

    private fun setHotHeader() {
        val hotword_view = initHotWordView()
        if (mHotListView != null)
            mHotListView!!.addHeaderView(hotword_view)
    }

    private fun initHotWordView(): View? {
        var listHeader: View? = null
        try {
            listHeader = LayoutInflater.from(activity).inflate(R.layout
                    .search_hotword_view, null)
            relative_parent = listHeader!!.findViewById(R.id.relative_parent) as RelativeLayout
        } catch (e: InflateException) {
            e.printStackTrace()
        }

        return listHeader
    }

    private fun initSuggestListView() {
        if (mSearchPresenter != null) {
            mSearchPresenter!!.setSearchSuggestCallBack(this)
        }

        mSuggestListView = ListView(activity)
        if (mSuggestListView == null)
            return
        mSuggestListView!!.cacheColorHint = mResources!!.getColor(R.color.transparent)
        mSuggestListView!!.divider = mResources!!.getDrawable(R.color.transparent)
        mSuggestListView!!.setSelector(R.drawable.item_selector_white)
        mSuggestListView!!.visibility = View.GONE
        if (mRootLayout != null) {
            mRootLayout!!.addView(mSuggestListView)
        }
        if (mSearchViewPresenter?.getSuggestData() != null) {
            mSearchViewPresenter?.getSuggestData()!!.clear()
        }
        if (mSuggestAdapter == null) {
            var inputString = ""
            if (mSearchEditText != null) {
                val editable = mSearchEditText!!.text
                if (editable != null && editable.length > 0) {
                    inputString = editable.toString()
                }
            }
            mSuggestAdapter = SearchSuggestAdapter(activity, mSearchViewPresenter?.getSuggestData(), inputString)
        }
        mSuggestListView!!.adapter = mSuggestAdapter
        mSuggestListView!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            if (mSearchEditText != null) {
                mSearchViewPresenter?.onSuggestItemClick(mContext, mSearchEditText!!.text.toString().trim { it <= ' ' }, arg2)
                mShouldShowHint = false
                mSearchEditText!!.setText(mSearchViewPresenter?.getCurrSuggestData())
            }

            if (mSearchPresenter != null) {
                mSearchPresenter.searchType = mSearchViewPresenter?.getSearchType()
                mSearchPresenter.word = mSearchViewPresenter?.getCurrSuggestData()
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


    private fun showHotList() {
        if (mHotListView != null)
            mHotListView!!.visibility = View.VISIBLE
        if (mSuggestListView != null)
            mSuggestListView!!.visibility = View.GONE
        if (mHisListView != null)
            mHisListView!!.visibility = View.GONE
    }

    fun notifyListChanged() {
        if (mHotAdapter != null)
            mHotAdapter!!.notifyDataSetChanged()
    }

    fun setSearchWord(word: String?) {
        mShouldShowHint = false
        if (mSearchEditText != null) {
            mSearchEditText!!.setText(word)
        }
        addHistoryWord(word)
    }

    fun addHistoryWord(keyword: String?) {
        mSearchViewPresenter?.addHistoryWord(keyword)
    }


    private fun clearHistoryItem(item: String) {
        mSearchViewPresenter?.removeHis(item)
        if (mHisAdapter != null)
            mHisAdapter!!.notifyDataSetChanged()
        mSearchViewPresenter?.saveHis()
    }

    override fun onSearchResult(suggestList: List<SearchCommonBean>, transmitBean: SearchAutoCompleteBean) {
        mSearchViewPresenter?.onSearchResult(suggestList, transmitBean)
    }

    fun setOnHistoryClickListener(listener: OnHistoryClickListener) {
        mOnHistoryClickListener = listener
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

        if (mSuggestAdapter != null) {
            mSuggestAdapter!!.clear()
            mSuggestAdapter = null
        }

        mSearchViewPresenter?.onDestroy()

    }

    fun hideInputMethod(paramView: View?) {
        if (paramView == null || paramView.context == null)
            return
        val imm = paramView.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(paramView.applicationWindowToken, 0)
        }
    }

    interface OnHistoryClickListener {
        fun OnHistoryClick(history: String?, searchType: String?)
    }

    override fun notifyHisData() {
        if (mHisAdapter != null) {
            mHisAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onStartSearch(searchWord: String?, searchType: String?) {
        if (mOnHistoryClickListener != null) {
            mOnHistoryClickListener!!.OnHistoryClick(searchWord, searchType)
        }
    }

    override fun hotItemClick(hotword: String, searchType: String) {
        if (mSearchEditText != null) {
            mShouldShowHint = false
            mSearchEditText!!.setText(hotword)
        }
    }

    override fun showLinearParent(show: Boolean) {
        if (show) {
            relative_parent!!.visibility = View.VISIBLE
        } else {
            relative_parent!!.visibility = View.GONE
        }
    }

    override fun setHotWordAdapter(hotWords: MutableList<SearchHotBean.DataBean>?) {
        mHotAdapter = SearchHotWordAdapter(activity, hotWords)
        if (mHotListView != null) {

            mHotListView!!.adapter = mHotAdapter
            mHotListView!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, position ->
                mSearchViewPresenter?.onHotItemClick(activity, arg0, arg1, arg2, position)
            }
        }
    }

    override fun onSuggestBack() {
        if (mSuggestAdapter != null) {
            var inputString: String? = ""
            if (mSearchEditText != null) {
                val editable = mSearchEditText!!.text
                if (editable != null && editable.length > 0) {
                    inputString = editable.toString()
                }
            }
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
    }
}
