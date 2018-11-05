package com.dingyue.searchbook.fragment

import com.ding.basic.bean.HotWordBean
import com.dingyue.searchbook.R
import com.dingyue.searchbook.adapter.HotWordAdapter
import com.dingyue.statistics.DyStatService
import kotlinx.android.synthetic.txtqbmfyd.fragment_hotword.*
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.StatServiceUtils
import java.util.*


/**
 * Desc 热词
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:05
 */
class HotWordFragment : BaseHotWordFragment() {

    override fun setLayout(): Int = R.layout.fragment_hotword

    override fun showHotWordList(hotWordList: ArrayList<HotWordBean>) {

        hotWordAdapter = HotWordAdapter(hotWordList)
        gridView.adapter = hotWordAdapter

        onHotWordItemClick(hotWordList)
    }

    private fun onHotWordItemClick(hotWordList: ArrayList<HotWordBean>) {
        gridView.setOnItemClickListener { _, _, position, _ ->
            StatServiceUtils.statAppBtnClick(context,
                    StatServiceUtils.b_search_click_allhotword)

            val bean = hotWordList[position]
            val data = HashMap<String, String>()
            data["topicword"] = bean.keyword.orEmpty()
            data["rank"] = bean.sort.toString()
            data["type"] = bean.superscript.orEmpty()
            DyStatService.onEvent(EventPoint.SEARCH_TOPIC, data)
            hotWordPresenter.onKeyWord(bean.keyword)
            onResultListener?.onSuccess(bean.keyword ?: "")
        }
    }

}