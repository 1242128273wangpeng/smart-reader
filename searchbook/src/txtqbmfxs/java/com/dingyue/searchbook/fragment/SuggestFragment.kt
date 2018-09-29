package com.dingyue.searchbook.fragment

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import com.ding.basic.bean.SearchCommonBeanYouHua
import com.dingyue.searchbook.R
import com.dingyue.searchbook.adapter.SuggestAdapter
import com.dingyue.searchbook.presenter.SuggestPresenter
import net.lzbook.kit.utils.enterCover
import com.dingyue.searchbook.view.ISuggestView
import kotlinx.android.synthetic.txtqbmfxs.fragment_listview.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import java.util.HashMap

/**
 * Desc 自动补全
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:05
 */
class SuggestFragment : Fragment(), ISuggestView {

    private var mView: View? = null
    private lateinit var mKeyWord: String

    private var itemGapViewCount = 0 // 用于打点 记录自动补全的type不为 书籍，作者，标签

    private lateinit var searchCommonBean: SearchCommonBeanYouHua

    private val suggestPresenter: SuggestPresenter by lazy {
        SuggestPresenter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_listview, container, false)
        suggestPresenter.onCreate()
        return mView
    }

    override fun showLoading() {
    }

    override fun hideLoading() {
    }

    override fun showSuggestList(suggestList: MutableList<Any>) {

        listView.adapter = SuggestAdapter(suggestList, mKeyWord)

        onSuggestItemClick(suggestList)
        onSuggestScrollListener()

    }

    fun obtainKeyWord(key: String) {
        mKeyWord = key
        suggestPresenter.loadSuggestData(mKeyWord)
    }


    override fun onDestroy() {
        super.onDestroy()
        suggestPresenter.onDestroy()
    }


    private fun onSuggestScrollListener() {

        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                hideInputMethod(view)
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int,
                                  totalItemCount: Int) {

            }
        })
    }

    private fun onSuggestItemClick(suggestList: MutableList<Any>) {
        listView.setOnItemClickListener { _, _, position, _ ->
            val obj = suggestList[position]
            if (obj is SearchCommonBeanYouHua) {
                searchCommonBean = obj
            } else {
                return@setOnItemClickListener
            }

            var suggest = searchCommonBean.suggest
            var searchType = "0"
            var isAuthor = 0
            val data = HashMap<String, String>()


            when (searchCommonBean.wordtype) {
                "label" -> {
                    searchType = "1"
                    isAuthor = 0
//                    isFocus = false
                }
                "author" -> {
                    searchType = "2"
                    isAuthor = searchCommonBean.isAuthor
//                    isBackSearch = false
//                    isFocus = true
//                    addHistoryWord(suggest)
                }
                "name" -> {
                    searchType = "3"
//                    isFocus = true
//                    isBackSearch = false
                    isAuthor = 0
                    data.put("bookid", searchCommonBean.book_id)

                    //统计进入到书籍封面页
                    val data1 = HashMap<String, String>()
                    data1.put("BOOKID", searchCommonBean.book_id)
                    data1.put("source", "SEARCH")
                    StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ENTER, data1)

                    requireActivity().enterCover(
                            author = searchCommonBean.author,
                            book_id = searchCommonBean.book_id,
                            book_source_id = searchCommonBean.book_source_id)
//                    addHistoryWord(suggest)
                }
                else -> {
                    searchType = "0"
                    isAuthor = 0
                }
            }

//            if (!TextUtils.isEmpty(suggest) && mSearchEditText != null && mSuggestList != null) {
            if (!TextUtils.isEmpty(suggest)) {
                data.put("keyword", suggest)
                data.put("type", searchType)
//                data.put("enterword", mSearchEditText.getText().toString().trim({ it <= ' ' }))

                itemGapViewCount = (0 until position).count { suggestList[it] !is SearchCommonBeanYouHua }

                data.put("rank", (position + 1 - itemGapViewCount).toString())
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TIPLISTCLICK, data)
            }

//            if (mSearchEditText != null && searchType != "3") {
//                startSearch(suggest, searchType, isAuthor)
//            }

        }
    }


    private fun hideInputMethod(paramView: View?) {
        if (paramView == null || paramView.context == null) {
            return
        }
        val imm = paramView.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(paramView.applicationWindowToken, 0)
        }
    }

}