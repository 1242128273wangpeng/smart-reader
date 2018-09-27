package com.intelligent.reader.util

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
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
import com.google.gson.Gson
import com.intelligent.reader.R
import com.intelligent.reader.activity.CoverPageActivity
import com.intelligent.reader.adapter.RecommendBooksAdapter
import com.intelligent.reader.adapter.SearchHistoryAdapter
import com.intelligent.reader.adapter.SearchSuggestAdapter
import com.intelligent.reader.view.TagContainerLayout
import com.intelligent.reader.view.TagView
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.Tools
import net.lzbook.kit.utils.sp.SPUtils
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.ui.widget.LoadingPage
import java.lang.ref.WeakReference
import java.util.*

class SearchViewHelper(activity: Activity, rootLayout: ViewGroup, searchEditText: EditText, private val mSearchHelper: SearchHelper?) : SearchHelper.SearchSuggestCallBack, SearchHistoryAdapter.onPositionClickListener, RecommendBooksAdapter.RecommendItemClickListener {
    private var mContext: Context? = null
    private var activity: Activity? = null
    private var mRootLayout: ViewGroup? = null

    private var mSearchEditText: EditText? = null
    private var mHistoryListView: ListView? = null
    private var mSuggestListView: ListView? = null

    private var relative_hot: RelativeLayout? = null
    private var search_line: View? = null

    private var mRecommendRecycleView: RecyclerView? = null
    private var mRecommendBooksAdapter: RecommendBooksAdapter? = null
    private var mTagContainerLayout: TagContainerLayout? = null
    private var mHotWords: List<HotWordBean>? = null

    private var mSuggestAdapter: SearchSuggestAdapter? = null
    private var mSuggestList: MutableList<Any>? = ArrayList()


    var onHotWordClickListener: OnHotWordClickListener? = null
    private var mOnHistoryClickListener: OnHistoryClickListener? = null
    private val hotWords = ArrayList<SearchHotBean.DataBean>()
    private var suggest: String? = null
    private var searchType: String? = null
    private var gson: Gson? = null
    private var historyAdapter: SearchHistoryAdapter? = null

    private var searchCommonBean: SearchCommonBeanYouHua? = null
    private var linear_root: LinearLayout? = null

    //运营模块返回标识
    private var isBackSearch = false
    //从标签和作者的webView页面返回是否保留焦点
    var isFocus = true
    private var searchHotTitleLayout: View? = null
    private var isAuthor = 0
    private var recommendBooks: MutableList<SearchRecommendBook.DataBean> = ArrayList()
    private val finalRecommendBooks = ArrayList<SearchRecommendBook.DataBean>()
    private var count = 0//用于标识换一换次数
    private var books: ArrayList<Book> = ArrayList()
    private var loadingPage: LoadingPage? = null
    private var tv_search_title: TextView? = null

    /**
     * 返回isFocus 和 isBackSearch 的值，以此来确定searchBookActivity页面显示的模块
     *
     * @return
     */
    val showStatus: Boolean
        get() = !isBackSearch && isFocus

    /**
     * 获取书架上的书Id
     */

    val bookOnLineIds: String
        get() {
            books.clear()
            val result = RequestRepositoryFactory.loadRequestRepositoryFactory(
                    BaseBookApplication.getGlobalContext()).loadBooks()
            result?.let {
                books.addAll(books)
            }
            val sb = StringBuilder()
            if (books.size > 0) {
                for (i in books.indices) {
                    val book = books[i]
                    sb.append(book.book_id)
                    sb.append(if (i == books.size - 1) "" else ",")
                }
                return sb.toString()
            }
            return ""
        }

    private val mSearchHandler = SearchHandler(this)

    init {
        init(activity, rootLayout, searchEditText)
    }

    private fun init(activity: Activity, rootLayout: ViewGroup, searchEditText: EditText) {
        gson = Gson()
        mContext = activity
        this.activity = activity
        mRootLayout = rootLayout
        mSearchEditText = searchEditText

        showSearchHistory()
        initSuggestListView()
        initRecommendView()
        showRecommendView()
    }

    private fun showRecommendView() {

        if (searchHotTitleLayout != null)
            searchHotTitleLayout!!.visibility = View.VISIBLE
        if (mSuggestListView != null)
            mSuggestListView!!.visibility = View.GONE
        if (mHistoryListView != null)
            mHistoryListView!!.visibility = View.GONE

    }

    fun hideHintList() {
        mRootLayout?.visibility = View.GONE
    }

    fun showHintList() {
        if (mRootLayout != null && mRootLayout!!.visibility == View.GONE)
            mRootLayout!!.visibility = View.VISIBLE
    }

    /**
     * 对searchBookActivity提供控制隐藏显示搜索框下面的内容
     *
     * @param searchWord
     */
    fun showRemainWords(searchWord: String?) {

        //当搜索次为空是显示搜索历史界面
        if (searchWord != null && "" == searchWord || TextUtils.isEmpty(searchWord!!.trim { it <= ' ' })) {
            showHistoryList()
        } else {
            showSuggestList(searchWord)
        }
    }

    private fun showSuggestList(searchWord: String) {
        if (mRootLayout!!.visibility == View.GONE) {
            mRootLayout!!.visibility = View.VISIBLE
        }
        if (mSuggestListView != null) {
            mSuggestListView!!.visibility = View.VISIBLE
        }
        if (mHistoryListView != null)
            mHistoryListView!!.visibility = View.GONE
        if (searchHotTitleLayout != null) {
            searchHotTitleLayout!!.visibility = View.GONE
        }

        // 清空上一个词的联想词结果
        if (mSuggestList != null) {
            mSuggestList!!.clear()
        }
        if (mSuggestAdapter != null) {
            mSuggestAdapter!!.notifyDataSetChanged()
        }

        if (TextUtils.isEmpty(searchWord)) {
            showHistoryList()
        }

        mSearchHelper?.startSearch(searchWord)

    }

    private fun initHistoryMain(context: Context) {
        mHistoryListView = ListView(context)
        if (mHistoryListView != null) {
            mHistoryListView!!.cacheColorHint = ContextCompat.getColor(context, R.color.transparent)
            mHistoryListView!!.divider = ContextCompat.getDrawable(context, R.color.transparent)
            mHistoryListView!!.setSelector(R.drawable.item_selector_white)
        }
    }

    private fun initRecommendView() {

        setHotTagList()
        if (mRootLayout != null) {
            mRootLayout!!.addView(searchHotTitleLayout)
        }
    }


    /**
     * 对外提供一个操作mRecommendListView隐藏显示的方法
     */
    fun hideRecommendListView() {
        if (searchHotTitleLayout != null && searchHotTitleLayout!!.visibility == View.VISIBLE) {
            searchHotTitleLayout!!.visibility = View.GONE
        }
    }

    /**
     * 搜索历史单独抽离成一个页面
     */
    private fun showSearchHistory() {

        if (searchHotTitleLayout != null && searchHotTitleLayout!!.visibility == View.VISIBLE) {
            searchHotTitleLayout!!.visibility = View.GONE
        }

        //初始化搜索历史的ListView
        initHistoryMain(mContext!!)

        historyDatas = Tools.getHistoryWord(mContext)
        historyAdapter = SearchHistoryAdapter(mContext, historyDatas!!)
        if (historyAdapter != null) {
            historyAdapter!!.setPositionClickListener(this)
        }

        if (mHistoryListView != null) {

            mHistoryListView!!.adapter = historyAdapter
            mHistoryListView!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, position ->
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.b_search_click_his_word)
                if (historyDatas != null && !historyDatas!!.isEmpty() && position > -1 &&
                        position < historyDatas!!.size) {
                    val history = historyDatas!![position.toInt()]
                    if (mSearchEditText != null) {
                        mSearchEditText!!.setText(history)
                        //                            mSearchEditText.setSelection(history.length());
                        isFocus = false
                        startSearch(history, "0", 0)

                        val data = HashMap<String, String>()
                        data.put("keyword", history)
                        data.put("rank", position.toString() + "")
                        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BARLIST, data)
                    }
                }
            }
        }

        if (mRootLayout != null) {
            mRootLayout!!.addView(mHistoryListView)
        }

    }

    private fun initHotTagView() {

        searchHotTitleLayout = View.inflate(mContext, R.layout.search_hot_title_layout, null)
        tv_search_title = searchHotTitleLayout!!.findViewById<View>(R.id.tv_search_title) as TextView
        search_line = searchHotTitleLayout!!.findViewById(R.id.search_line)
        linear_root = searchHotTitleLayout!!.findViewById<View>(R.id.linear_root) as LinearLayout

        mTagContainerLayout = searchHotTitleLayout?.findViewById<View>(R.id.tag_container_layout) as TagContainerLayout
        mTagContainerLayout?.setOnTagClickListener(object : TagView.OnTagClickListener {

            override fun onTagClick(position: Int, text: String) {

                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.b_search_click_allhotword)
                val hotWord = mHotWords!![position]
                val data = HashMap<String, String>()
                data.put("rank", hotWord.sort.toString())
                hotWord.keyword?.let { data.put("topicword", it) }
                hotWord.superscript?.let { data.put("type", it) }
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TOPIC, data)

                mSearchEditText?.setText(text)

                isFocus = false
                onHotWordClickListener?.hotWordClick(text, mHotWords!![position].keywordType.toString() + "")
            }

            override fun onTagLongClick(position: Int, text: String) {}

            override fun onTagCrossClick(position: Int) {}
        })

    }


    private fun setHotTagList() {
        initHotTagView()

        mRecommendRecycleView = searchHotTitleLayout!!.findViewById<View>(R.id.list_recommed) as RecyclerView
        relative_hot = searchHotTitleLayout!!.findViewById<View>(R.id.relative_hot) as RelativeLayout
        val tv_change = searchHotTitleLayout!!.findViewById<View>(R.id.tv_change) as TextView
        tv_change.setOnClickListener {
            count += 6
            if (count >= 30) {
                count = 0
            }
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.HOTREADCHANGE)
            initRecycleView(count)
        }

        mRecommendRecycleView?.let {
            it.visibility = View.VISIBLE

            it.recycledViewPool.setMaxRecycledViews(0, 12)
            val layoutManager = ShelfGridLayoutManager(mContext, 3)
            it.layoutManager = layoutManager
            it.isNestedScrollingEnabled = false
            it.itemAnimator.addDuration = 0
            it.itemAnimator.changeDuration = 0
            it.itemAnimator.moveDuration = 0
            it.itemAnimator.removeDuration = 0
            (it.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }


        if (NetWorkUtils.getNetWorkTypeNew(mContext) == "无") {
            linear_root!!.visibility = View.GONE
        } else {
            if (loadingPage == null && mRootLayout != null && !activity!!.isFinishing) {
                loadingPage = LoadingPage(activity, mRootLayout, LoadingPage.setting_result)
            }
            parseGetHotWords()
            parseGetRecommendData()
        }

    }

    //获取热词
    private fun parseGetHotWords() {

        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchOperationV4(

                object : RequestSubscriber<Result<SearchResult>>() {
                    override fun requestResult(value: Result<SearchResult>?) {
                        if (value != null && value.data != null) {
                            linear_root!!.visibility = View.VISIBLE
                            val result = value.data
                            SPUtils.putDefaultSharedString(Constants.SERARCH_HOT_WORD_YOUHUA,
                                    gson!!.toJson(result, SearchResult::class.java))
                            parseResult(result)

                        } else {
                            getCacheDataFromShare(true)
                        }
                        if (loadingPage != null) {
                            loadingPage!!.onSuccess()
                        }
                    }

                    override fun requestError(message: String) {
                        if (loadingPage != null) {
                            loadingPage!!.onSuccess()
                        }
                    }
                })

    }

    //获取推荐书籍
    private fun parseGetRecommendData() {


        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchRecommend(

                bookOnLineIds, object : RequestSubscriber<SearchRecommendBook>() {
            override fun requestResult(result: SearchRecommendBook?) {
                if (result != null && result.data != null) {
                    recommendBooks.clear()
                    recommendBooks = result.data
                    relative_hot!!.visibility = View.VISIBLE
                    search_line!!.visibility = View.VISIBLE
                    initRecycleView(count)
                }
            }

            override fun requestError(message: String) {

            }
        })


    }

    //    //获取本地存储的推荐书籍
    //    public void getRecommendBooksFromCache() {
    //        count = 0;
    //        recommendBooks.clear();
    //        if (mBookDaoHelper == null) {
    //            mBookDaoHelper = BookDaoHelper.getInstance();
    //        }
    //        recommendBooks = mBookDaoHelper.getSearchBooks();
    //        if (recommendBooks != null && recommendBooks.size() > 0) {
    //            relative_hot.setVisibility(View.VISIBLE);
    //            initRecycleView(count);
    //        } else {
    //            relative_hot.setVisibility(View.GONE);
    //        }
    //
    //    }

    @Synchronized
    fun initRecycleView(bookCount: Int) {
        finalRecommendBooks.clear()
        for (i in bookCount until bookCount + 6) {
            if (i < recommendBooks.size) {
                finalRecommendBooks.add(recommendBooks[i])
            }
        }
        if (mRecommendBooksAdapter == null) {
            mRecommendBooksAdapter = RecommendBooksAdapter(mContext, this@SearchViewHelper, finalRecommendBooks)
            mRecommendRecycleView!!.adapter = mRecommendBooksAdapter
        } else {
            mRecommendBooksAdapter!!.notifyDataSetChanged()
        }

    }

    /**
     * 从缓存中获取热词
     */
    fun getCacheDataFromShare(hasNet: Boolean) {
        if (!TextUtils.isEmpty(SPUtils.getDefaultSharedString(Constants.SERARCH_HOT_WORD_YOUHUA))) {
            val cacheHotWords = SPUtils.getDefaultSharedString(Constants.SERARCH_HOT_WORD_YOUHUA)
            val searchResult = gson!!.fromJson(cacheHotWords, SearchResult::class.java)
            if (searchResult != null) {
                linear_root!!.visibility = View.VISIBLE
                parseResult(searchResult)
            } else {
                tv_search_title!!.visibility = View.GONE
                linear_root!!.visibility = View.GONE
            }

        } else {
            if (!hasNet) {
                ToastUtil.showToastMessage("网络不给力哦")
            }
            tv_search_title!!.visibility = View.GONE
            linear_root!!.visibility = View.GONE
        }
    }

    /**
     * parse result HotWord
     */
    fun parseResult(value: SearchResult) {
        hotWords.clear()
        mTagContainerLayout!!.isTagViewClickable = true
        mHotWords = value.hotWords
        if (mHotWords != null) {
            mTagContainerLayout!!.setTags(value.hotWords)
        }
        tv_search_title!!.visibility = View.VISIBLE
        mTagContainerLayout!!.dragEnable = false
        mTagContainerLayout!!.setTagLineMax(2)
    }

    fun initSuggestListView() {

        if (searchHotTitleLayout != null && searchHotTitleLayout!!.visibility == View.VISIBLE) {
            searchHotTitleLayout!!.visibility = View.GONE
        }

        mSearchHelper?.setSearchSuggestCallBack(this)

        mSuggestListView = ListView(activity)
        if (mSuggestListView == null)
            return
        mSuggestListView!!.cacheColorHint = ContextCompat.getColor(mContext!!, R.color.transparent)
        mSuggestListView!!.divider = ContextCompat.getDrawable(mContext!!, R.color.color_divider)
        mSuggestListView!!.setSelector(R.drawable.item_selector_white)
        mSuggestListView!!.visibility = View.GONE
        if (mRootLayout != null) {
            mRootLayout!!.addView(mSuggestListView)
        }
        if (mSuggestList != null) {
            mSuggestList!!.clear()
        }
        if (mSuggestAdapter == null) {
            var inputString = ""
            if (mSearchEditText != null) {
                val editable = mSearchEditText!!.text
                if (editable != null && editable.length > 0) {
                    inputString = editable.toString()
                }
            }
            mSuggestAdapter = SearchSuggestAdapter(activity, mSuggestList, inputString)
        }
        mSuggestListView!!.adapter = mSuggestAdapter
        mSuggestListView!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val obj = mSuggestList!![arg2]
            if (obj is SearchCommonBeanYouHua) {
                searchCommonBean = obj
            } else {
                return@OnItemClickListener
            }
            suggest = searchCommonBean!!.suggest
            searchType = "0"
            isAuthor = 0
            val data = HashMap<String, String>()

            when (searchCommonBean!!.wordtype) {
                "label" -> {
                    searchType = "1"
                    isAuthor = 0
                    isFocus = false
                }
                "author" -> {
                    searchType = "2"
                    isAuthor = searchCommonBean!!.isAuthor
                    isBackSearch = false
                    isFocus = true
                    addHistoryWord(suggest)
                }
                "name" -> {
                    searchType = "3"

                    isFocus = true
                    isBackSearch = false
                    isAuthor = 0
                    val searchCommonBeanYouHua = mSuggestList!![arg2] as SearchCommonBeanYouHua
                    data.put("bookid", searchCommonBeanYouHua.book_id)

                    //统计进入到书籍封面页
                    val data1 = HashMap<String, String>()
                    data1.put("BOOKID", searchCommonBeanYouHua.book_id)
                    data1.put("source", "SEARCH")
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE,
                            StartLogClickUtil.ENTER, data1)


                    val intent = Intent()
                    intent.setClass(activity!!, CoverPageActivity::class.java)
                    val bundle = Bundle()
                    bundle.putString("author", searchCommonBeanYouHua.author)
                    bundle.putString("book_id", searchCommonBeanYouHua.book_id)
                    bundle.putString("book_source_id", searchCommonBeanYouHua.book_source_id)

                    intent.putExtras(bundle)
                    mContext!!.startActivity(intent)
                    addHistoryWord(suggest)
                }
                else -> {
                    searchType = "0"
                    isAuthor = 0
                }
            }
            if (!TextUtils.isEmpty(suggest) && mSearchEditText != null) {

                data.put("keyword", suggest.toString())
                data.put("type", searchType.toString())
                data.put("enterword", mSearchEditText!!.text.toString().trim { it <= ' ' })
                when {
                    arg2 + 1 in 1..2 -> data.put("rank", (arg2 + 1).toString() + "")
                    arg2 + 1 in 4..5 -> data.put("rank", arg2.toString() + "")
                    arg2 + 1 in 7..8 -> data.put("rank", (arg2 - 1).toString() + "")
                    arg2 + 1 > 9 -> data.put("rank", (arg2 - 2).toString() + "")
                }
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TIPLISTCLICK, data)
            }

            if (mSearchEditText != null && searchType != "3") {
                startSearch(suggest, searchType, isAuthor)


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


    fun showHistoryList() {

        if (mSuggestListView != null)
            mSuggestListView!!.visibility = View.GONE
        if (searchHotTitleLayout != null)
            searchHotTitleLayout!!.visibility = View.GONE
        if (mHistoryListView != null)
            mHistoryListView!!.visibility = View.VISIBLE

        if (historyDatas != null && mContext != null) {
            historyDatas!!.clear()
            val historyWord = Tools.getHistoryWord(mContext)
            if (historyWord != null) {
                historyDatas!!.addAll(historyWord)
            }
            if (historyAdapter != null) {
                historyAdapter!!.notifyDataSetChanged()
            }
        }
    }

    fun setSearchWord(word: String) {
        if (mSearchEditText != null) {
            mSearchEditText!!.setText(word)
            //            mSearchEditText.setSelection(mSearchEditText.length());
        }
        addHistoryWord(word)
    }

    private fun startSearch(searchWord: String?, searchType: String?, isAuthor: Int) {
        if (searchWord != null && searchWord != "") {
            addHistoryWord(searchWord)
            mOnHistoryClickListener?.onHistoryClick(searchWord, searchType, isAuthor)

        }
    }

    fun addHistoryWord(keyword: String?) {
        if (historyDatas == null) {
            historyDatas = ArrayList()
        }

        if (keyword == null || keyword == "") {
            return
        }
        if (historyDatas!!.contains(keyword)) {
            historyDatas!!.remove(keyword)
        }

        if (!historyDatas!!.contains(keyword)) {
            val size = historyDatas!!.size
            if (size >= 30) {
                historyDatas!!.removeAt(size - 1)
            }
            historyDatas!!.add(0, keyword)
            Tools.saveHistoryWord(mContext, historyDatas)
        }
        if (historyAdapter != null) {
            historyAdapter!!.notifyDataSetChanged()
        }
    }


    private fun clearHistory(index: Int) {
        if (historyDatas != null && index < historyDatas!!.size)
            historyDatas!!.removeAt(index)
        if (historyAdapter != null)
            historyAdapter!!.notifyDataSetChanged()
        Tools.saveHistoryWord(mContext, historyDatas)
    }

    private fun result(result: List<SearchCommonBeanYouHua>) {
        if (mSuggestList == null)
            return
        mSuggestList!!.clear()
        var index = 0
        for (item in result) {
            if (index > 4)
            // 只显示5个
                break

            mSuggestList!!.add(item)
            index++
        }
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

    override fun onSearchResult(suggestList: List<Any>, transmitBean: SearchAutoCompleteBeanYouHua) {
        if (mSuggestList == null) {
            return
        }
        mSuggestList!!.clear()
        for (item in suggestList) {
            mSuggestList!!.add(item)
        }

        mSearchHandler.post {
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
    }

    override fun onItemClickListener(position: Int) {
        //        ToastUtils.showToastNoRepeat(""+position);
        if (mSearchHandler != null) {
            val message = mSearchHandler.obtainMessage()
            message.arg1 = position
            message.what = 10
            mSearchHandler.handleMessage(message)
        }
    }

    override fun onItemClick(view: View, position: Int) {


        val dataBean = finalRecommendBooks[position]
        val data = HashMap<String, String>()
        data.put("rank", (position + 1).toString() + "")
        data.put("type", "1")
        data.put("bookid", dataBean.bookId)
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.HOTREADCLICK, data)

        val intent = Intent()
        intent.setClass(mContext, CoverPageActivity::class.java)
        val bundle = Bundle()
        bundle.putString("author", dataBean.authorName)
        bundle.putString("book_id", dataBean.bookId)
        bundle.putString("book_source_id", dataBean.id)
        intent.putExtras(bundle)
        mContext?.startActivity(intent)

        isBackSearch = true
        isFocus = true
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

    fun setOnHistoryClickListener(listener: OnHistoryClickListener) {
        mOnHistoryClickListener = listener
    }

    interface OnHotWordClickListener {
        fun hotWordClick(tag: String, searchType: String)
    }

    interface OnHistoryClickListener {
        fun onHistoryClick(history: String, searchType: String?, isAuthor: Int)
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
        if (mSuggestList != null) {
            mSuggestList!!.clear()
            mSuggestList = null
        }

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

        if (historyAdapter != null) {
            historyAdapter = null
        }
        if (loadingPage != null) {
            loadingPage = null
        }

        if (mSuggestAdapter != null) {
            mSuggestAdapter!!.clear()
            mSuggestAdapter = null
        }

        if (historyDatas != null) {
            historyDatas!!.clear()
            historyDatas = null
        }

        if (mSuggestList != null) {
            mSuggestList!!.clear()
            mSuggestList = null
        }
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

    companion object {

        private var historyDatas: ArrayList<String>? = ArrayList()

    }
}
