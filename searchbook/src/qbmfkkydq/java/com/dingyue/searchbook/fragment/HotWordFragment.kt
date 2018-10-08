package com.dingyue.searchbook.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.bean.HotWordBean
import com.ding.basic.bean.SearchRecommendBook
import com.dingyue.searchbook.R
import com.dingyue.searchbook.adapter.HotWordAdapter
import com.dingyue.searchbook.adapter.RecommendAdapter
import com.dingyue.searchbook.interfaces.OnResultListener
import com.dingyue.searchbook.presenter.HotWordPresenter
import com.dingyue.searchbook.view.IHotWordView
import kotlinx.android.synthetic.qbmfkkydq.fragment_hotword.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.enterCover
import java.util.HashMap
import kotlin.collections.ArrayList


/**
 * Desc 热词和书籍推荐
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:05
 */
class HotWordFragment : Fragment(), IHotWordView, RecommendAdapter.RecommendItemClickListener {

    var onResultListener: OnResultListener<String>? = null

    private var hotWordAdapter: HotWordAdapter? = null
    private val hotWordPresenter: HotWordPresenter by lazy {
        HotWordPresenter(this)
    }


    private var recommendFreeList: ArrayList<SearchRecommendBook.DataBean> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hotword, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hotWordPresenter.onCreate()
        hotWordPresenter.loadHotWordData()
        hotWordPresenter.loadRecommendData()
        initListener()
    }

    private var count = 0//用于标识换一换次数
    private fun initListener(){
//        txt_change.setOnClickListener {
//            count += 8
//            if (count >= 24) {
//                count = 0
//            }
//            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE,
//                    StartLogClickUtil.HOTREADCHANGE)
//            initRecycleView(count)
//        }
    }

//    @Synchronized
//    fun initRecycleView(bookCount: Int) {
//        mRecommendFinalBooks.clear()
//        for (i in bookCount until bookCount + 8) {
//            if (i < mRecommendBooks.size) {
//                mRecommendFinalBooks.add(mRecommendBooks.get(i))
//            }
//        }
//        if (mRecommendBooksAdapter == null) {
//            mRecommendBooksAdapter = RecommendBooksAdapter(mContext, this@SearchViewHelper,
//                    mRecommendFinalBooks)
//            mRecommendRecycleView.setAdapter(mRecommendBooksAdapter)
//        } else {
//            mRecommendBooksAdapter.notifyDataSetChanged()
//        }
//
//    }

    override fun showLoading() {
    }

    override fun hideLoading() {
    }

    override fun showHotWordList(hotWordList: ArrayList<HotWordBean>) {

        hotWordAdapter = HotWordAdapter(hotWordList)
        gridView.adapter = hotWordAdapter

        onHotWordItemClick(hotWordList)
    }

    override fun showRecommendList(recommendList: ArrayList<SearchRecommendBook.DataBean>) {

        recommendFreeList.clear()
        recommendList.forEachIndexed { index, dataBean ->
            if (index < 8) {
                recommendFreeList.add(dataBean)
            }
        }

        list_recommend.layoutManager = GridLayoutManager(context, 4)
        list_recommend.adapter = RecommendAdapter(recommendFreeList, this@HotWordFragment)

    }

    private fun onHotWordItemClick(hotWordList: ArrayList<HotWordBean>) {
        gridView.setOnItemClickListener { _, _, position, _ ->
            StatServiceUtils.statAppBtnClick(context,
                    StatServiceUtils.b_search_click_allhotword)

            val bean = hotWordList[position]
            val data = HashMap<String, String>()
            data.put("topicword", bean.keyword ?: "")
            data.put("rank", bean.sort.toString())
            data.put("type", bean.superscript ?: "")
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TOPIC, data)
            hotWordPresenter.onKeyWord(bean.keyword)
            onResultListener?.onSuccess(bean.keyword ?: "")
        }
    }

    override fun onRecommendItemClick(view: View, position: Int, dataBean: SearchRecommendBook.DataBean) {

        val data = HashMap<String, String>()
        data.put("rank", (position + 1).toString() + "")
        data.put("type", "1")
        data.put("bookid", dataBean.bookId!!)
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.HOTREADCLICK, data)

        requireActivity().enterCover(
                book_id = dataBean.bookId,
                book_source_id = dataBean.id,
                book_chapter_id = dataBean.bookChapterId)

    }


    override fun onDestroy() {
        super.onDestroy()
        hotWordPresenter.onDestroy()
    }
}