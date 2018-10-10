package com.dingyue.searchbook.fragment

import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
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
import kotlinx.android.synthetic.qbmfxsydq.fragment_hotword.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.enterCover
import java.util.*


/**
 * Desc 热词和书籍推荐
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:05
 */
class HotWordFragment : Fragment(), IHotWordView,
        HotWordAdapter.HotWordClickListener,
        RecommendAdapter.RecommendItemClickListener {

    var onResultListener: OnResultListener<String>? = null

    private var loadingPage: LoadingPage? = null

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
        hotWordPresenter.loadRecommendData(true)

        txt_change.setOnClickListener {
            hotWordPresenter.loadRecommendData()
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

        recyclerView_hotWord.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
        recyclerView_hotWord.adapter = HotWordAdapter(hotWordList, this)
        recyclerView_hotWord.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                outRect?.right = AppUtils.dip2px(context, 22f)
                outRect?.bottom = AppUtils.dip2px(context, 22f)
            }
        })
    }

    override fun showRecommendList(recommendList: ArrayList<SearchRecommendBook.DataBean>) {

        recyclerView_recommend.layoutManager = GridLayoutManager(context, 3)
        recyclerView_recommend.adapter = RecommendAdapter(recommendList, this)

    }

    override fun onHotWordItemClick(view: View, position: Int, dataBean: HotWordBean) {

        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_allhotword)

        val data = HashMap<String, String>()
        data.put("topicword", dataBean.keyword ?: "")
        data.put("rank", dataBean.sort.toString())
        data.put("type", dataBean.superscript ?: "")
        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TOPIC, data)
        hotWordPresenter.onKeyWord(dataBean.keyword)
        onResultListener?.onSuccess(dataBean.keyword ?: "")

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