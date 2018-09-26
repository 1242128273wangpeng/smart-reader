package com.intelligent.reader.util

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import android.widget.AdapterView.OnItemClickListener
import android.widget.EditText
import android.widget.ListView
import android.widget.RelativeLayout
import com.ding.basic.bean.*
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import com.dingyue.bookshelf.ShelfGridLayoutManager
import com.google.gson.Gson
import com.intelligent.reader.R
import com.intelligent.reader.activity.CoverPageActivity
import com.intelligent.reader.adapter.*
import com.intelligent.reader.presenter.search.SearchSCView
import com.intelligent.reader.search.SearchPresenter
import com.intelligent.reader.search.SearchViewPresenter
import kotlinx.android.synthetic.mfqbxssc.search_hot_title_layout.view.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.base.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.sp.SPUtils
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.ui.widget.LoadingPage
import java.util.*

class SearchViewHelper(activity: Activity, rootLayout: ViewGroup, searchEditText: EditText, private val mSearchPresenter: SearchPresenter?) : SearchPresenter.SearchSuggestCallBack, SearchHisAdapter.SearchClearCallBack, SearchSCView.View, RecommendBooksAdapter.RecommendItemClickListener {

    private var mContext: Context? = null
    private var activity: Activity? = null
    private var mRootLayout: ViewGroup? = null
    private var mSearchEditText: EditText? = null
    private var mHisListView: ListView? = null
    private var mSuggestListView: ListView? = null
    private var mHotAdapter: SearchHotWordAdapter? = null
    private var mSuggestAdapter: SearchSuggestAdapter? = null
    private var mHisAdapter: SearchHisAdapter? = null
    private var mResources: Resources? = null
    private var mShouldShowHint = true
    private var loadingPage: LoadingPage? = null

    var onHotWordClickListener: ((tag: String?, searchType: String?) -> Unit)? = null
    private var mOnHistoryClickListener: OnHistoryClickListener? = null
    var isFromEditClick = false
    private var relative_parent: RelativeLayout? = null
    var mSearchViewPresenter: SearchViewPresenter? = null
    private val searchHotTitleLayout: View by lazy {
        LayoutInflater.from(mContext).inflate(R.layout.search_hot_title_layout, null)
    }
    private var mHotWords: MutableList<HotWordBean> = ArrayList()
    private var titleRecomDatas: MutableList<SearchRecommendBook.DataBean> = ArrayList()
    private var recommendDatas1: MutableList<SearchRecommendBook.DataBean> = ArrayList()
    private var recommendDatas2: MutableList<SearchRecommendBook.DataBean> = ArrayList()
    private var searchHotWordAdapter: SearchHotWordAdapter? = null
    private var gson: Gson? = null

    init {
        init(activity, activity, rootLayout, searchEditText)
    }

    private fun init(context: Context, activity: Activity, rootLayout: ViewGroup, searchEditText: EditText) {
        mContext = context
        this.activity = activity
        gson = Gson()
        mRootLayout = rootLayout
        mSearchEditText = searchEditText
        if (mContext != null)
            mResources = mContext!!.resources

        mSearchViewPresenter = SearchViewPresenter(this)

        initHisListView()
        initSuggestListView()
        initRecommendView()
        showRecommendView()
    }

    private fun showRecommendView() {
        searchHotTitleLayout.visibility = View.VISIBLE
        mSuggestListView?.setVisibility(View.GONE)
        mHisListView?.setVisibility(View.GONE)
    }


    private fun initRecommendView() {
        setHotTagList()
        mRootLayout?.addView(searchHotTitleLayout)
    }

    private fun setHotTagList() {
        searchHotTitleLayout.gridView.setOnItemClickListener({ parent, view, position, id ->
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.b_search_click_allhotword)
            val hotWord = mHotWords.get(position)
            val data = HashMap<String, String>()
            hotWord.keyword?.let {
                data.put("topicword", it)
            }
            data.put("rank", hotWord.sort.toString())
            hotWord.superscript?.let {
                data.put("type", it)
            }
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TOPIC, data)
            mShouldShowHint = false
            mSearchEditText?.setText(hotWord.keyword)

            mSearchViewPresenter?.isFocus = false
            mSearchViewPresenter?.startSearch(hotWord.keyword, mHotWords.get(position).keywordType.toString(), 0)
        })

        initRecycleType(searchHotTitleLayout.list_recommed)
        initRecycleType(searchHotTitleLayout.list_recommed1)

        if (NetWorkUtils.getNetWorkTypeNew(mContext) == "无") {
            searchHotTitleLayout.linear_root.setVisibility(View.GONE)
        } else {
            if (loadingPage == null && mRootLayout != null && !activity!!.isFinishing()) {
                loadingPage = LoadingPage(activity, mRootLayout, LoadingPage.setting_result)
            }
            getHotWords()
            getRecommendData()
        }

    }

    fun initRecycleType(view: RecyclerView) {
        view.setVisibility(View.VISIBLE)
        view.getRecycledViewPool().setMaxRecycledViews(0, 12)
        val layoutManager = ShelfGridLayoutManager(mContext, 4)
        view.setLayoutManager(layoutManager)
        view.setNestedScrollingEnabled(false)
        view.getItemAnimator().setAddDuration(0)
        view.getItemAnimator().setChangeDuration(0)
        view.getItemAnimator().setMoveDuration(0)
        view.getItemAnimator().setRemoveDuration(0)
        (view.getItemAnimator() as SimpleItemAnimator).supportsChangeAnimations = false
    }

    //获取热词
    fun getHotWords() {

        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchOperationV4(

                object : RequestSubscriber<Result<SearchResult>>() {
                    override fun requestResult(value: Result<SearchResult>?) {
                        if (value != null && value.data != null) {
                            searchHotTitleLayout.linear_root.setVisibility(View.VISIBLE)
                            val result = value.data
                            SPUtils.putDefaultSharedString(Constants.SERARCH_HOT_WORD_YOUHUA, gson?.toJson(result, SearchResult::class.java))
                            parseResult(result)

                        } else {
                            getCacheDataFromShare(true)
                        }
                        loadingPage?.onSuccess()
                    }

                    override fun requestError(message: String) {
                        getCacheDataFromShare(true)
                        loadingPage?.onSuccess()
                    }

                })
    }

    //获取推荐书籍
    private fun getRecommendData() {

        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchRecommend(getBookOnLineIds(), object : RequestSubscriber<SearchRecommendBook> (){
            override fun requestResult(value: SearchRecommendBook?) {
                if (value != null && value.data != null) {
                    titleRecomDatas.clear()
                    titleRecomDatas = value.data
                    searchHotTitleLayout.relative_hot.setVisibility(View.VISIBLE)
                    if (titleRecomDatas.size > 8) {
                        searchHotTitleLayout.relative_hot1.setVisibility(View.VISIBLE)
                    }
                    initRecycleView()
                }

            }

            override fun requestError(message: String) {
            }
        })

    }


    @Synchronized
    fun initRecycleView() {
        recommendDatas1.clear()
        recommendDatas2.clear()
        for (i in 0 until titleRecomDatas.size) {
            if (i < 8) {
                recommendDatas1.add(titleRecomDatas[i])
            } else if (i < 16){
                recommendDatas2.add(titleRecomDatas[i])
            }
        }
        searchHotTitleLayout.list_recommed.setAdapter(RecommendBooksAdapter(mContext, this@SearchViewHelper, recommendDatas1))
        if (recommendDatas2.isNotEmpty())
            searchHotTitleLayout.list_recommed1.setAdapter(RecommendBooksAdapter(mContext, this@SearchViewHelper, recommendDatas2))
    }

    override fun onItemClick(view: View?, position: Int, datas: List<SearchRecommendBook.DataBean>) {

        val dataBean = datas[position]
        val data = HashMap<String, String>()
        data.put("rank", (position + 1).toString() + "")
        data.put("type", "1")
        data.put("bookid", dataBean.bookId)
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.HOTREADCLICK, data)

        mContext?.let {
            val intent = Intent(it, CoverPageActivity::class.java)
            intent.putExtra(Constants.BOOK_ID, dataBean.bookId)
            intent.putExtra(Constants.BOOK_SOURCE_ID, dataBean.id)
            intent.putExtra(Constants.BOOK_CHAPTER_ID, dataBean.bookChapterId)

            it.startActivity(intent)
        }

        mSearchViewPresenter?.isBackSearch = true
        mSearchViewPresenter?.isFocus = true

    }

    /**
     * 获取书架上的书Id
     */
    private fun getBookOnLineIds(): String {
//        books?.clear()
        val books = RequestRepositoryFactory.loadRequestRepositoryFactory(
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
     * if hasn't net getHotWord from sharepreferenecs cache
     */
    fun getCacheDataFromShare(hasNet: Boolean) {
        if (!TextUtils.isEmpty(SPUtils.getDefaultSharedString(Constants.SERARCH_HOT_WORD_YOUHUA))) {
            val cacheHotWords = SPUtils.getDefaultSharedString(Constants.SERARCH_HOT_WORD_YOUHUA)
            val searchResult = gson?.fromJson(cacheHotWords, SearchResult::class.java)
            if (searchResult != null) {
                searchHotTitleLayout.linear_root.setVisibility(View.VISIBLE)
                parseResult(searchResult)
            } else {
                searchHotTitleLayout.linear_root.setVisibility(View.GONE)
            }

        } else {
            if (!hasNet) {
                ToastUtil.showToastMessage("网络不给力哦")
            }
            searchHotTitleLayout.linear_root.setVisibility(View.GONE)
        }
    }

    /**
     * parse result HotWord
     */
    fun parseResult(value: SearchResult) {
        mHotWords.clear()
        mHotWords = value.hotWords

        if (searchHotWordAdapter == null) {
            searchHotWordAdapter = SearchHotWordAdapter(activity, mHotWords)
            searchHotTitleLayout.gridView.setAdapter(searchHotWordAdapter)
        } else {
            searchHotWordAdapter!!.setDatas(mHotWords)
            searchHotWordAdapter!!.notifyDataSetChanged()
        }

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

    override fun showLoading(){
        if (loadingPage == null && this.activity != null) {
            loadingPage = LoadingPage(this.activity, mRootLayout, LoadingPage.setting_result)
        }
    }

    override fun dimissLoading() {
        if(loadingPage != null){
            loadingPage!!.onSuccess()
        }

    }

    fun hideHintList() {
        if (mRootLayout != null)
            mRootLayout!!.visibility = View.GONE
    }

    private fun showSuggestList(searchWord: String) {
        if (mRootLayout!!.getVisibility() == View.GONE) {
            mRootLayout!!.setVisibility(View.VISIBLE)
        }

        searchHotTitleLayout.visibility = View.GONE
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

    fun getShowStatus(): Boolean{
        return mSearchViewPresenter!!.getShowStatus()
    }

    private fun showHisList() {
        if (mSuggestListView != null)
            mSuggestListView!!.visibility = View.GONE
        searchHotTitleLayout.visibility = View.GONE
        if (mHisListView != null)
            mHisListView!!.visibility = View.VISIBLE


        if(mSearchViewPresenter != null){
            mSearchViewPresenter?.initHistoryData(mContext)
        }

        if (mHisAdapter != null) {
            mHisAdapter!!.notifyDataSetChanged()
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
                mSearchViewPresenter?.onSuggestItemClick(mContext, mSearchEditText?.text.toString().trim { it <= ' ' }, arg2)
//                mShouldShowHint = false
//                mSearchEditText!!.setText(mSearchViewPresenter?.getCurrSuggestData())
            }
//
//            if (mSearchPresenter != null) {
//                mSearchPresenter.searchType = mSearchViewPresenter?.getSearchType()
//                mSearchPresenter.word = mSearchViewPresenter?.getCurrSuggestData()
//            }
        }
        mSuggestListView!!.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                hideInputMethod(view)
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {

            }
        })
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

    override fun onSearchResult(suggestList: List<SearchSuggest>, transmitBean: SearchAutoCompleteBeanYouHua) {
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
        fun OnHistoryClick(history: String?, searchType: String?,isAuthor: Int)
    }

    override fun notifyHisData() {
        if (mHisAdapter != null) {
            mHisAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onStartSearch(searchWord: String?, searchType: String?,isAuthor: Int) {
        if (mOnHistoryClickListener != null) {
            mOnHistoryClickListener!!.OnHistoryClick(searchWord, searchType,isAuthor)
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

    override fun setHotWordAdapter(hotWords: MutableList<HotWordBean>?) {
    }

    companion object {
        private val TAG = SearchViewHelper::class.java.simpleName
    }
}
