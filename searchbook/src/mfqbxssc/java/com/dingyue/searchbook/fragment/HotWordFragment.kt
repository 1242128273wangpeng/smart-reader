package com.dingyue.searchbook.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.bean.HotWordBean
import com.ding.basic.bean.SearchRecommendBook
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.searchbook.adapter.HotWordAdapter
import com.dingyue.searchbook.adapter.RecommendAdapter
import com.dingyue.searchbook.presenter.HotWordPresenter
import com.dingyue.searchbook.view.IHotWordView
import com.example.searchbook.R
import kotlinx.android.synthetic.mfqbxssc.fragment_hotword.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import java.util.HashMap


/**
 * Desc 热词和书籍推荐
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:05
 */
class HotWordFragment : Fragment(), IHotWordView, RecommendAdapter.RecommendItemClickListener {


    private var mView: View? = null
    private var mPresenter: HotWordPresenter? = null

    private var hotWordAdapter: HotWordAdapter? = null

    private var recommendFreeList: ArrayList<SearchRecommendBook.DataBean> = ArrayList()
    private var recommendWantList: ArrayList<SearchRecommendBook.DataBean> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_hotword, container, false)
        mPresenter = HotWordPresenter(this)
        mPresenter?.onCreate()
        mPresenter?.loadHotWordData()
        return mView
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter?.onDestroy()
        mPresenter = null
    }

    override fun showLoading() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hideLoading() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showHotWordList(hotWordList: ArrayList<HotWordBean>) {
        if (hotWordAdapter == null) {
            hotWordAdapter = HotWordAdapter(requireActivity(), hotWordList)
            gridViewHotWord.adapter = hotWordAdapter
        } else {
            hotWordAdapter?.setData(hotWordList)
            hotWordAdapter?.notifyDataSetChanged()
        }
    }

    override fun showRecommendFreeList(recommendList: ArrayList<SearchRecommendBook.DataBean>) {

        for (i in 0 until recommendList.size) {
            if (i < 8) {
                recommendFreeList.add(recommendList[i])
            } else if (i < 16) {
                recommendWantList.add(recommendList[i])
            }
        }

        list_recommend.adapter = RecommendAdapter(recommendFreeList, this@HotWordFragment)
        if (recommendWantList.isNotEmpty()) {
            list_recommend1.adapter = RecommendAdapter(recommendWantList, this@HotWordFragment)
        }


    }

    override fun showRecommendWantList(recommendList: ArrayList<SearchRecommendBook.DataBean>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRecommendItemClick(view: View, position: Int, books: List<SearchRecommendBook.DataBean>) {

        val dataBean = books[position]
        val data = HashMap<String, String>()
        data.put("rank", (position + 1).toString() + "")
        data.put("type", "1")
        data.put("bookid", dataBean.bookId!!)
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.HOTREADCLICK, data)

        val bundle = Bundle()
        bundle.putString(RouterUtil.BOOK_ID, dataBean.bookId)
        bundle.putString(RouterUtil.BOOK_SOURCE_ID, dataBean.id)
        bundle.putString(RouterUtil.BOOK_CHAPTER_ID, dataBean.bookChapterId)
        RouterUtil.navigation(requireActivity(), RouterConfig.COVER_PAGE_ACTIVITY, bundle)

        mPresenter?.isBackSearch = true
        mPresenter?.isFocus = true
    }


}