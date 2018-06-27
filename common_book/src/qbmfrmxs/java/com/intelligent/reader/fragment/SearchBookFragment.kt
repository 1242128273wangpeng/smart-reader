package com.intelligent.reader.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.RelativeLayout
import com.ding.basic.bean.Book
import com.ding.basic.bean.SearchHotBean
import com.ding.basic.bean.SearchRecommendBook
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import com.dingyue.bookshelf.ShelfGridLayoutManager
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.SharedPreUtil
import com.google.gson.Gson
import com.intelligent.reader.R
import com.intelligent.reader.adapter.RecommendBooksAdapter
import com.intelligent.reader.adapter.SearchHotWordAdapter
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfrmxs.frag_search_book.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.StatServiceUtils
import java.util.*
import java.util.concurrent.Callable

/**
 * Function：搜索书籍Fragment
 *
 * Created by JoannChen on 2018/6/20 0020 14:57
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class SearchBookFragment : Fragment(), RecommendBooksAdapter.RecommendItemClickListener {


    private var shareUtil: SharedPreUtil? = null
    private var hotWords: MutableList<SearchHotBean.DataBean>? = ArrayList()
    private var gson: Gson? = null
    private var books: MutableList<Book>? = ArrayList()
    private var recommendBooks: MutableList<SearchRecommendBook.DataBean> = ArrayList()
    private var loadingpage: LoadingPage? = null
    private var contentLayout: RelativeLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.frag_search_book, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shareUtil = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT)
        gson = Gson()
        contentLayout = view.findViewById(R.id.ll_main);
        requestData()

        rl_recommend_search.setOnClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY)
        }
        sfgv_hot_word.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            StatServiceUtils.statAppBtnClick(context,
                    StatServiceUtils.b_search_click_allhotword)
            val hotWord = hotWords?.get(position)
            if (hotWord != null) {
                val data = HashMap<String, String>()
                data.put("topicword", hotWord.word!!)
                data.put("rank", position.toString())
                data.put("type", hotWord.wordType.toString())
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE,
                        StartLogClickUtil.TOPIC, data)
                var budle = Bundle()
                budle.putString("word", hotWord.word)
                RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY, budle)
            }

        })

    }

    fun requestData() {
        if (loadingpage != null) {
            loadingpage!!.onSuccess()
        }

        loadingpage = LoadingPage(requireActivity(), contentLayout)

        resetHotWordList()
        getRecommendData()

        if (loadingpage != null) {
            loadingpage!!.setReloadAction(Callable<Void> {
                resetHotWordList()
                getRecommendData()
                null
            })
        }
    }

    fun resetHotWordList() {

        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestHotWords(object : RequestSubscriber<SearchHotBean>() {
            override fun requestResult(result: SearchHotBean?) {
                parseResult(result, true)
                loadingpage?.onSuccess()
            }

            override fun requestError(message: String) {
                Logger.e("获取搜索热词异常！")
                loadingpage?.onError()
            }

            override fun requestComplete() {

            }
        })

    }


    /**
     * parse result data
     */
    fun parseResult(value: SearchHotBean?, hasNet: Boolean) {
        hotWords!!.clear()
        if (value != null && value.data != null) {
            hotWords = value.data as MutableList<SearchHotBean.DataBean>?
            if (hotWords != null && hotWords!!.size >= 0) {
                if (hasNet) {
                    shareUtil!!.putString(Constants.SERARCH_HOT_WORD, gson!!.toJson(value, SearchHotBean::class.java))
                }
                sfgv_hot_word?.adapter = SearchHotWordAdapter(requireActivity(), hotWords)
            } else {
                shareUtil!!.putString(Constants.SERARCH_HOT_WORD, "")
            }
        }
    }

    //获取推荐书籍
    private fun getRecommendData() {
        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchRecommend(
                getBookOnLineIds(), object : RequestSubscriber<SearchRecommendBook>() {
            override fun requestResult(value: SearchRecommendBook?) {
                if (value != null && value.data != null) {
                    recommendBooks.clear()
                    recommendBooks = value.data
                    view_divider.visibility = View.VISIBLE
                    txt_recommend_title.visibility = View.VISIBLE
                    initRecycleView(value.data)

                } else {
                    view_divider.visibility = View.GONE
                    txt_recommend_title.visibility = View.GONE
                }
            }

            override fun requestError(message: String) {
                view_divider.visibility = View.GONE
                txt_recommend_title.visibility = View.GONE
            }
        })


    }

    fun initRecycleView(books: List<SearchRecommendBook.DataBean>) {

        rcv_recommend.getRecycledViewPool().setMaxRecycledViews(0, 12)
        val layoutManager = ShelfGridLayoutManager(requireContext(), 1)
        rcv_recommend.setLayoutManager(layoutManager)
        rcv_recommend.getItemAnimator().setAddDuration(0)
        rcv_recommend.getItemAnimator().setChangeDuration(0)
        rcv_recommend.getItemAnimator().setMoveDuration(0)
        rcv_recommend.getItemAnimator().setRemoveDuration(0)
        (rcv_recommend.getItemAnimator() as SimpleItemAnimator).supportsChangeAnimations = false

        rcv_recommend.adapter = RecommendBooksAdapter(requireContext(), this, books)


    }

    override fun onItemClick(view: View, position: Int) {

        val book = recommendBooks[position]
        val data = HashMap<String, String>()
        data.put("rank", (position + 1).toString() + "")
        data.put("type", "1")
        data.put("bookid", book?.bookId)
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE,
                StartLogClickUtil.HOTREADCLICK, data)


        if (book != null) {

            val bundle = Bundle()
            bundle.putString("book_id", book.bookId)
            bundle.putString("book_source_id", "")
            bundle.putString("book_chapter_id", book.bookChapterId)
            if (!requireActivity().isFinishing) {
                RouterUtil.navigation(requireActivity(), RouterConfig.COVER_PAGE_ACTIVITY, bundle)
            }
        }


    }

    /**
     * 获取书架上的书Id
     */

    fun getBookOnLineIds(): String {
        books?.clear()
        books = RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).loadBooks() as MutableList<Book>
        val sb = StringBuilder()
        if (books != null && books!!.size > 0) {
            for (i in books!!.indices) {
                val book = books!!.get(i)
                sb.append(book.book_id)
                sb.append(if (i == books!!.size - 1) "" else ",")
            }
            return sb.toString()
        }
        return ""
    }

}