package com.dingyue.searchbook.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SimpleItemAnimator
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
import kotlinx.android.synthetic.qbmfrmxs.fragment_hotword.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.enterCover
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import java.util.*


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

        rl_recommend_search.setOnClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY)
        }
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

        list_recommend.recycledViewPool.setMaxRecycledViews(0, 12)
        val layoutManager = GridLayoutManager(requireContext(), 1)

        list_recommend.layoutManager = layoutManager
        list_recommend.isNestedScrollingEnabled = false
        list_recommend.itemAnimator.addDuration = 0
        list_recommend.itemAnimator.changeDuration = 0
        list_recommend.itemAnimator.moveDuration = 0
        list_recommend.itemAnimator.removeDuration = 0
        (list_recommend.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        list_recommend.adapter = RecommendAdapter(recommendList, this@HotWordFragment)

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

            //跳转到SearchBookActivity,并显示搜索结果页
            val bundle = Bundle()
            bundle.putString("keyWord", bean.keyword)
            bundle.putBoolean("showSearchResult", true)
            RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY, bundle)
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
//
//    /**
//     * 点击搜索框，回调给SearchBookActivity
//     */
//    interface OnSearchInputClick {
//        fun onSearchClick()
//    }

}