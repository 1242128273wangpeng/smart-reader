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
import kotlinx.android.synthetic.mfqbxssc.fragment_hotword.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.ui.widget.LoadingPage
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

    private var loadingPage: LoadingPage? = null

    private var hotWordAdapter: HotWordAdapter? = null

    private var recommendFreeList: ArrayList<SearchRecommendBook.DataBean> = ArrayList()
    private var recommendWantList: ArrayList<SearchRecommendBook.DataBean> = ArrayList()

    private val hotWordPresenter: HotWordPresenter by lazy {
        HotWordPresenter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hotword, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hotWordPresenter.onCreate()
        hotWordPresenter.loadHotWordData()
        hotWordPresenter.loadRecommendData()
    }

    override fun showLoading() {
        hideLoading()
        loadingPage = LoadingPage(requireActivity(), search_result_main, LoadingPage.setting_result)
    }

    override fun hideLoading() {
        loadingPage?.onSuccessGone()
    }

    override fun showHotWordList(hotWordList: ArrayList<HotWordBean>) {

        hotWordAdapter = HotWordAdapter(hotWordList)
        gridView.adapter = hotWordAdapter

        onHotWordItemClick(hotWordList)
    }

    override fun showRecommendList(recommendList: ArrayList<SearchRecommendBook.DataBean>) {

        recommendFreeList.clear()
        recommendWantList.clear()
        recommendList.forEachIndexed { index, dataBean ->
            if (index < 8) {
                recommendFreeList.add(dataBean)
            } else if (index < 16) {
                recommendWantList.add(dataBean)
            }
        }

        list_recommend1.layoutManager = GridLayoutManager(context, 4)
        list_recommend1.adapter = RecommendAdapter(recommendFreeList, this@HotWordFragment)

        list_recommend2.layoutManager = GridLayoutManager(context, 4)
        list_recommend2.adapter = RecommendAdapter(recommendWantList, this@HotWordFragment)

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