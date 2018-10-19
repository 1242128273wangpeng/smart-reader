package com.dingyue.searchbook.fragment

import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.ding.basic.bean.HotWordBean
import com.ding.basic.bean.SearchRecommendBook
import com.dingyue.searchbook.R
import com.dingyue.searchbook.adapter.HotWordAdapter
import com.dingyue.searchbook.adapter.RecommendAdapter
import kotlinx.android.synthetic.mfxsqbyd.fragment_hotword.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.enterCover
import java.util.*


/**
 * Desc 热词和书籍推荐
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:05
 */
class HotWordFragment : BaseHotWordFragment(), RecommendAdapter.RecommendItemClickListener {

    private var recommendFreeList: ArrayList<SearchRecommendBook.DataBean> = ArrayList()

    override fun setLayout(): Int = R.layout.fragment_hotword

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

}