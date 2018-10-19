package com.dingyue.searchbook.fragment

import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import com.ding.basic.bean.HotWordBean
import com.ding.basic.bean.SearchRecommendBook
import com.dingyue.searchbook.R
import com.dingyue.searchbook.adapter.HotWordAdapter
import com.dingyue.searchbook.adapter.RecommendAdapter
import kotlinx.android.synthetic.txtqbdzs.fragment_hotword.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
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
class HotWordFragment : BaseHotWordFragment(), RecommendAdapter.RecommendItemClickListener,
        HotWordAdapter.HotWordClickListener {

    override fun setLayout(): Int = R.layout.fragment_hotword

    override fun initView() {
        txt_change.setOnClickListener {
            hotWordPresenter.loadRecommendData(false)
        }
    }

    override fun showHotWordList(hotWordList: ArrayList<HotWordBean>) {

        recyclerView_hotWord.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
        recyclerView_hotWord.adapter = HotWordAdapter(hotWordList, this)
        recyclerView_hotWord.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                outRect?.right = AppUtils.dip2px(context, 16f)
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

}